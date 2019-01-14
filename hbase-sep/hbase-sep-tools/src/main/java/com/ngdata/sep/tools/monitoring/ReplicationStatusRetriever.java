/*
 * Copyright 2013 NGDATA nv
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.ngdata.sep.tools.monitoring;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.protobuf.HBaseZeroCopyByteString;
import com.ngdata.sep.tools.monitoring.ReplicationStatus.HLogInfo;
import com.ngdata.sep.tools.monitoring.ReplicationStatus.Status;
import com.ngdata.sep.util.zookeeper.ZooKeeperItf;
import org.apache.commons.io.IOUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hbase.ClusterMetrics;
import org.apache.hadoop.hbase.ClusterStatus;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.HConstants;
import org.apache.hadoop.hbase.ServerName;
import org.apache.hadoop.hbase.client.Admin;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.ConnectionFactory;
import org.apache.hadoop.hbase.client.HBaseAdmin;
import org.apache.hadoop.hbase.client.replication.ReplicationAdmin;
import org.apache.hadoop.hbase.replication.ReplicationPeerConfig;
import org.apache.hadoop.hbase.replication.ReplicationQueuesClientZKImpl;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.hadoop.hbase.util.FSUtils;
import org.apache.hadoop.hbase.util.HBaseConfTool;
import org.apache.hadoop.hbase.wal.FSHLogProvider;
import org.apache.hadoop.hbase.wal.WAL;
import org.apache.hadoop.hbase.zookeeper.MasterAddressTracker;
import org.apache.hadoop.hbase.zookeeper.ZKUtil;
import org.apache.hadoop.hbase.zookeeper.ZKWatcher;
import org.apache.hadoop.hbase.zookeeper.ZooKeeperHelper;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.data.Stat;

import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * Collects replication status information.
 *
 * <p>Usage: first call {@link #collectStatusFromZooKeepeer()}, then optionally call
 * {@link #addStatusFromJmx(ReplicationStatus)} for more information.
 */
public class ReplicationStatusRetriever {
    private final ZooKeeperItf zk;
    private final FileSystem fileSystem;
    private final Path hbaseRootDir;
    private final Path hbaseOldLogDir;
    public static final int HBASE_JMX_PORT = 10102;
    private Configuration conf;
    private String hbasePathOption;

    public ReplicationStatusRetriever(ZooKeeperItf zk, int hbaseMasterPort,String hbasePathOption) throws InterruptedException, IOException, KeeperException {
        this.zk = zk;
        this.hbasePathOption=hbasePathOption;
        
         this.conf = getHBaseConf(zk, hbaseMasterPort,hbasePathOption);

        if (!"true".equalsIgnoreCase(conf.get("hbase.replication"))) {
            throw new RuntimeException("HBase replication is not enabled.");
        }

        
        fileSystem = FileSystem.get(conf);
        hbaseRootDir = FSUtils.getRootDir(conf);
        hbaseOldLogDir = new Path(hbaseRootDir, HConstants.HREGION_OLDLOGDIR_NAME);
        
        
    }

    private Configuration getHBaseConf(ZooKeeperItf zk, int hbaseMasterPort,String hbasePathOption) throws KeeperException, InterruptedException, IOException {
        // Read the HBase/Hadoop configuration via the master web ui
        // This is debatable, but it avoids any pitfalls with conf dirs and also works with launch-test-lily
       
    	Configuration config = HBaseConfiguration.create(); 	
    	String hbaseMasterHostName = MasterAddressTracker.getMasterAddress(new ZKWatcher(config, null, null)).getHostnameLowerCase();
    	
    	ReplicationQueuesClientZKImpl rq = new ReplicationQueuesClientZKImpl(new ZKWatcher(config, null, null),config,null);
    	rq.getAllWALs();
    	System.out.println("rq" + rq.getAllWALs());
    	System.out.println("rq2" + rq.getAllPeersFromHFileRefsQueue());
    	System.out.println("rq3" + 	rq.getListOfReplicators()	);
    	System.out.println("rq4" + 	rq.getReplicableHFiles("Indexer_test")	);
    	System.out.println("rq5" + 	rq.getAllQueues("jb-i20-11.p.infra.arkea.com,16020,1547114026192")	);
    	System.out.println("zk1"+ ZKUtil.getReplicationZnodesDump(new ZKWatcher(config, null, null)));
    	
    	String url = String.format("http://%s:%d/conf", hbaseMasterHostName, hbaseMasterPort);
        System.out.println("Reading HBase configuration from " + url);
        byte[] data = readUrl(url);

        Configuration conf = new Configuration();
        conf.addResource(new ByteArrayInputStream(data));
        System.out.println("conf:"+conf);
        
        return conf;
    }

    private long parseHLogPositionFrom(byte[] data) {
      Method method;
      try {
        // this method is available for hbase-0.96 and above
        method = ZKUtil.class.getMethod("parseHLogPositionFrom", byte[].class);
        Preconditions.checkNotNull(method);
      } catch (SecurityException e) {
        method = null;
      } catch (NoSuchMethodException e) {
        method = null;
      }
      
      long position;
      if (method != null) {
        // this is correct for hbase-0.96 and above 
        try {
          // i.e. position = ZKUtil.parseHLogPositionFrom(data);
          position = (Long) method.invoke(null, data);
        } catch (IllegalArgumentException e) {
          throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
          throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
          throw new RuntimeException(e);
        }
      } else {
        // this is correct for hbase-0.94 and below
        try {
          position = Long.parseLong(new String(data, "UTF-8"));
        } catch (NumberFormatException e) {
          throw new RuntimeException(e);
        } catch (UnsupportedEncodingException e) {
          throw new RuntimeException(e);
        }
      }
      return position;
    }

    private byte[] readUrl(String url) throws IOException {
    	
    	
    	URL u = new URL(url);
    	HttpURLConnection con = (HttpURLConnection) u.openConnection();
    	return IOUtils.toByteArray(con.getInputStream());
    	
    	
    	/*
        DefaultHttpClient httpclient = new DefaultHttpClient();
        HttpGet httpGet = new HttpGet(url);

        HttpResponse response = httpclient.execute(httpGet);

        try {
            HttpEntity entity = response.getEntity();
            return IOUtils.toByteArray(entity.getContent());
        } finally {
            if (response.getEntity() != null) {
                EntityUtils.consume(response.getEntity());
            }
            httpGet.releaseConnection();
        }
        */
    }

    public ReplicationStatus collectStatusFromZooKeepeer() throws Exception {
        Map<String, Map<String, Status>> statusByPeerAndServer = Maps.newHashMap();

        /*
        String regionServerPath = "/hbase-unsecure/replication/rs";
        List<String> regionServers = zk.getChildren(regionServerPath, false);
        */
        
        
        
        Connection connection = ConnectionFactory.createConnection(conf);
        Admin admin = connection.getAdmin();    
        ClusterMetrics metrics = admin.getClusterMetrics();
        Set<ServerName> rs = metrics.getLiveServerMetrics().keySet();
        
         ReplicationPeerConfig toto = admin.getReplicationPeerConfig("Indexer_test"); 
         //admin.getRe
        
        System.out.println(" toto:"+toto);

        for (ServerName server : rs) {
            String peersPath = hbasePathOption + "/replication/rs/" + server;
            
            List<String> peers;
            
            
            try {
                peers = zk.getChildren(peersPath, false);
            } catch (KeeperException.NoNodeException e) {
                // server was removed since we called getChildren, skip it
                continue;
            }

            
            for (String peer : peers) {
                // The peer nodes are either real peers or recovered queues, we make no distinction for now
                String hlogsPath = peersPath + "/" + peer;

                SortedSet<String> logs;
                try {
                    // The hlogs are not correctly sorted when we get them from ZK
                    logs = new TreeSet<String>(Collections.reverseOrder());
                    logs.addAll(zk.getChildren(hlogsPath, false));
                } catch (KeeperException.NoNodeException e) {
                    // peer was removed since we called getChildren, skip it
                    continue;
                }

                
                
                for (String log : logs) {
                    Map<String, Status> statusByServer = statusByPeerAndServer.get(peer);
                    if (statusByServer == null) {
                        statusByServer = new TreeMap<String, Status>();
                        statusByPeerAndServer.put(peer, statusByServer);
                    }
                    Status status = statusByServer.get(server.getServerName());
                    if (status == null) {
                        status = new Status();
                        statusByServer.put(server.getServerName(), status);
                    }

                    
                    
                    try {
                        Stat stat = new Stat();
                        byte[] data = zk.getData(hlogsPath + "/" + log, false, stat);
                        System.out.println("hlogsPath:"+hlogsPath + "/" + log);
                        // Determine position in hlog, if already started on the hlog
                        long position = -1;
                        if (data != null && data.length > 0) {
                            data = removeMetaData(data);
                            position = parseHLogPositionFrom(data);
                        }

                        HLogInfo hlogInfo = new HLogInfo(log);
                        hlogInfo.size = getLogFileSize(server, log);
                        hlogInfo.position = position;
                        status.hlogs.add(hlogInfo);
                    } catch (KeeperException.NoNodeException e) {
                        // fine, node was removed since we called getChildren
                    }
                }
            }
        }

        return new ReplicationStatus(statusByPeerAndServer);
    }
    
    public void addStatusFromJmx(ReplicationStatus replicationStatus) throws Exception {
        JmxConnections jmxConnections = new JmxConnections();

        for (String peerId : replicationStatus.getPeersAndRecoveredQueues()) {
            for (String server : replicationStatus.getServers(peerId)) {
                Status status = replicationStatus.getStatus(peerId, server);
                String hostName = ServerName.parseHostname(server);

                MBeanServerConnection connection = jmxConnections.getConnector(hostName, HBASE_JMX_PORT).getMBeanServerConnection();

                ObjectName replSourceBean = new ObjectName("hadoop:service=Replication,name=ReplicationSource for " + URLEncoder.encode(peerId, "UTF8"));
                try {
                    status.ageOfLastShippedOp = (Long)connection.getAttribute(replSourceBean, "ageOfLastShippedOp");
                } catch (AttributeNotFoundException e) {
                    // could be the case if the queue disappeared since we read info from ZK
                } catch (InstanceNotFoundException e) {
                    // could be the case if the queue disappeared since we read info from ZK
                }

                // The following mbean is only available when using NGDATA's ForkedReplicationSource
                ObjectName replSourceInfoBean = new ObjectName("hadoop:service=Replication,name=ReplicationSourceInfo for " + URLEncoder.encode(peerId, "UTF8"));
                try {
                    status.selectedPeerCount = (Integer)connection.getAttribute(replSourceInfoBean, "SelectedPeerCount");
                    status.timestampOfLastShippedOp = (Long)connection.getAttribute(replSourceInfoBean, "TimestampLastShippedOp");
                    status.sleepReason = (String)connection.getAttribute(replSourceInfoBean, "SleepReason");
                    status.sleepMultiplier = (Integer)connection.getAttribute(replSourceInfoBean, "SleepMultiplier");
                    status.timestampLastSleep = (Long)connection.getAttribute(replSourceInfoBean, "TimestampLastSleep");
                } catch (AttributeNotFoundException e) {
                    // could be the case if the queue disappeared since we read info from ZK
                } catch (InstanceNotFoundException e) {
                    // could be the case if the queue disappeared since we read info from ZK
                    // or the ForkedReplicationSource isn't used
                }
            }
        }

        jmxConnections.close();
    }

    /**
     *
     * @param serverName the 'unique-over-restarts' name, i.e. hostname with start code suffix
     * @param hlogName name of HLog
     */
    private long getLogFileSize(ServerName serverName, String hlogName) throws IOException {
    	
    	
    	
    	
        Path hbaseLogDir = new Path(hbaseRootDir, FSHLogProvider.getWALDirectoryName(serverName.getServerName()));
        Path path = new Path(hbaseLogDir, hlogName);
        try {
            FileStatus status = fileSystem.getFileStatus(path);
            return status.getLen();
        } catch (FileNotFoundException e) {
            Path oldLogPath = new Path(hbaseOldLogDir, hlogName);
            try {
                return fileSystem.getFileStatus(oldLogPath).getLen();
            } catch (FileNotFoundException e2) {
                // TODO there is still another place to look for log files, cfr dead region servers, see openReader in replicationsource
                System.err.println("HLog not found at : " + path + " or " + oldLogPath);
                return -1;
            }
        }
    }

    private static final byte MAGIC =(byte) 0XFF;
    private static final int MAGIC_SIZE = Bytes.SIZEOF_BYTE;
    private static final int ID_LENGTH_OFFSET = MAGIC_SIZE;
    private static final int ID_LENGTH_SIZE =  Bytes.SIZEOF_INT;

    /** This method was copied from RecoverableZooKeeper in the HBase 0.94 source tree. */
    public byte[] removeMetaData(byte[] data) {
        if(data == null || data.length == 0) {
            return data;
        }
        // check the magic data; to be backward compatible
        byte magic = data[0];
        if(magic != MAGIC) {
            return data;
        }

        int idLength = Bytes.toInt(data, ID_LENGTH_OFFSET);
        int dataLength = data.length-MAGIC_SIZE-ID_LENGTH_SIZE-idLength;
        int dataOffset = MAGIC_SIZE+ID_LENGTH_SIZE+idLength;

        byte[] newData = new byte[dataLength];
        System.arraycopy(data, dataOffset, newData, 0, dataLength);
        for (byte b:newData)
        {
        	System.out.println("b:"+Byte.toString(b));
        }
        return newData;
    }
}
