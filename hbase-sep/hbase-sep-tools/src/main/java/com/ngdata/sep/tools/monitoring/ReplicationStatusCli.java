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

import com.google.common.collect.ImmutableList;
import com.ngdata.sep.util.io.Closer;
import com.ngdata.sep.util.zookeeper.ZkUtil;
import com.ngdata.sep.util.zookeeper.ZooKeeperItf;
import joptsimple.OptionException;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;
import org.apache.log4j.LogManager;
import org.apache.log4j.PropertyConfigurator;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
public class ReplicationStatusCli {
	
	Log log = LogFactory.getLog(ReplicationStatusCli.class);

    public static void main(String[] args) throws Exception {
    	
        new ReplicationStatusCli().run(args);
    }

    public void run(String[] args) throws Exception {
        LogManager.resetConfiguration();
        PropertyConfigurator.configure(getClass().getResource("log4j.properties"));
        String hbaseZkDefaultNode = "localhost";
        int hbaseZkDefaultPort = 0;
        int hbaseMasterDefaultPort=0;
        String hbaseZkDefaultPath="/hbase";
        String hbaseZkDefaultConnectionString="";
        
        Configuration config = HBaseConfiguration.create();
        System.out.println("hbase config:"+config);
        // args can be read from hbase-site.xml, if no hbase-site then take options
        if (null != config)
        {
        	hbaseZkDefaultNode = config.get("hbase.zookeeper.quorum");
        	hbaseZkDefaultPort = config.getInt("hbase.zookeeper.property.clientPort",2181);
        	String[] nodes = hbaseZkDefaultNode.split(",");
        	for (String node:nodes)
        	{
        		hbaseZkDefaultConnectionString=hbaseZkDefaultConnectionString + node+":"+hbaseZkDefaultPort+",";
        	}
        	
        	hbaseZkDefaultConnectionString=hbaseZkDefaultConnectionString.substring(0, hbaseZkDefaultConnectionString.length()-1);		
        	System.out.println("hbaseZkDefaultConnectionString:"+hbaseZkDefaultConnectionString);
        	hbaseMasterDefaultPort= config.getInt("hbase.master.info.port", 60010);
        	System.out.println("hbaseMasterDefaultPort:"+hbaseMasterDefaultPort);
        	hbaseZkDefaultPath = config.get("zookeeper.znode.parent");
        	System.out.println("hbaseZkDefaultPath:"+hbaseZkDefaultPath);

        }
        

        OptionParser parser =  new OptionParser();
        OptionSpec enableJmxOption = parser.accepts("enable-jmx",
                "use JMX to retrieve info from HBase regionservers (port " + ReplicationStatusRetriever.HBASE_JMX_PORT + ")");
        OptionSpec<String> zkOption = parser
                .acceptsAll(ImmutableList.of("z"), "ZooKeeper connection string, defaults to localhost")
                .withRequiredArg().ofType(String.class)
                .defaultsTo(hbaseZkDefaultConnectionString);
        
        OptionSpec<Integer> hbaseMasterPortOption = parser
                .acceptsAll(ImmutableList.of("hbase-master-port"), "HBase Master web ui port number")
                .withRequiredArg().ofType(Integer.class)
                .defaultsTo(hbaseMasterDefaultPort);
        
        OptionSpec<String> hbasePathOption = parser
                .acceptsAll(ImmutableList.of("p"), "ZooKeeper hbase base path, defaults to /hbase")
                .withRequiredArg().ofType(String.class)
                .defaultsTo(hbaseZkDefaultPath);
        

        OptionSet options = null;
        try {
            options = parser.parse(args);
        } catch (OptionException e) {
            System.out.println("Error parsing command line options:");
            System.out.println(e.getMessage());
            parser.printHelpOn(System.out);
            System.exit(1);
        }

        boolean enableJmx = options.has(enableJmxOption);
        String zkConnectString = options.valueOf(zkOption);

        System.out.println("Connecting to Zookeeper " + zkConnectString + "...");
        ZooKeeperItf zk = ZkUtil.connect(zkConnectString, 30000);

        ReplicationStatusRetriever retriever = new ReplicationStatusRetriever(zk, options.valueOf(hbaseMasterPortOption),options.valueOf(hbasePathOption));
        ReplicationStatus replicationStatus = retriever.collectStatusFromZooKeepeer();

        if (enableJmx) {
            retriever.addStatusFromJmx(replicationStatus);
        } else {
            System.out.println();
            System.out.println("Hint: use --enable-jmx to retrieve more info from HBase regionservers");
            System.out.println("      For this, you need to start HBase regionservers with:");
            System.out.println("      -Dcom.sun.management.jmxremote.ssl=false -Dcom.sun.management.jmxremote.authenticate=false -Dcom.sun.management.jmxremote.port=10102");
            System.out.println();
        }

        ReplicationStatusReport.printReport(replicationStatus, System.out);

        Closer.close(zk);
    }
}
