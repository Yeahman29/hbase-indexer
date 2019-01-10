/*
 * Copyright 2012 NGDATA nv
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
package com.ngdata.sep.impl;

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.Server;
import org.apache.hadoop.hbase.ServerName;
import org.apache.hadoop.hbase.client.ClusterConnection;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.shaded.protobuf.generated.AdminProtos;
import org.apache.hadoop.hbase.shaded.protobuf.generated.AdminProtos.ClearCompactionQueuesRequest;
import org.apache.hadoop.hbase.shaded.protobuf.generated.AdminProtos.ClearCompactionQueuesResponse;
import org.apache.hadoop.hbase.shaded.protobuf.generated.AdminProtos.ClearRegionBlockCacheRequest;
import org.apache.hadoop.hbase.shaded.protobuf.generated.AdminProtos.ClearRegionBlockCacheResponse;
import org.apache.hadoop.hbase.shaded.protobuf.generated.AdminProtos.CloseRegionRequest;
import org.apache.hadoop.hbase.shaded.protobuf.generated.AdminProtos.CloseRegionResponse;
import org.apache.hadoop.hbase.shaded.protobuf.generated.AdminProtos.CompactRegionRequest;
import org.apache.hadoop.hbase.shaded.protobuf.generated.AdminProtos.CompactRegionResponse;
import org.apache.hadoop.hbase.shaded.protobuf.generated.AdminProtos.ExecuteProceduresRequest;
import org.apache.hadoop.hbase.shaded.protobuf.generated.AdminProtos.ExecuteProceduresResponse;
import org.apache.hadoop.hbase.shaded.protobuf.generated.AdminProtos.FlushRegionRequest;
import org.apache.hadoop.hbase.shaded.protobuf.generated.AdminProtos.FlushRegionResponse;
import org.apache.hadoop.hbase.shaded.protobuf.generated.AdminProtos.GetOnlineRegionRequest;
import org.apache.hadoop.hbase.shaded.protobuf.generated.AdminProtos.GetOnlineRegionResponse;
import org.apache.hadoop.hbase.shaded.protobuf.generated.AdminProtos.GetRegionInfoRequest;
import org.apache.hadoop.hbase.shaded.protobuf.generated.AdminProtos.GetRegionInfoResponse;
import org.apache.hadoop.hbase.shaded.protobuf.generated.AdminProtos.GetRegionLoadRequest;
import org.apache.hadoop.hbase.shaded.protobuf.generated.AdminProtos.GetRegionLoadResponse;
import org.apache.hadoop.hbase.shaded.protobuf.generated.AdminProtos.GetServerInfoRequest;
import org.apache.hadoop.hbase.shaded.protobuf.generated.AdminProtos.GetServerInfoResponse;
import org.apache.hadoop.hbase.shaded.protobuf.generated.AdminProtos.GetStoreFileRequest;
import org.apache.hadoop.hbase.shaded.protobuf.generated.AdminProtos.GetStoreFileResponse;
import org.apache.hadoop.hbase.shaded.protobuf.generated.AdminProtos.OpenRegionRequest;
import org.apache.hadoop.hbase.shaded.protobuf.generated.AdminProtos.OpenRegionResponse;
import org.apache.hadoop.hbase.shaded.protobuf.generated.AdminProtos.ReplicateWALEntryRequest;
import org.apache.hadoop.hbase.shaded.protobuf.generated.AdminProtos.ReplicateWALEntryResponse;
import org.apache.hadoop.hbase.shaded.protobuf.generated.AdminProtos.RollWALWriterRequest;
import org.apache.hadoop.hbase.shaded.protobuf.generated.AdminProtos.RollWALWriterResponse;
import org.apache.hadoop.hbase.shaded.protobuf.generated.AdminProtos.StopServerRequest;
import org.apache.hadoop.hbase.shaded.protobuf.generated.AdminProtos.StopServerResponse;
import org.apache.hadoop.hbase.shaded.protobuf.generated.AdminProtos.UpdateConfigurationRequest;
import org.apache.hadoop.hbase.shaded.protobuf.generated.AdminProtos.UpdateConfigurationResponse;
import org.apache.hadoop.hbase.shaded.protobuf.generated.AdminProtos.UpdateFavoredNodesRequest;
import org.apache.hadoop.hbase.shaded.protobuf.generated.AdminProtos.UpdateFavoredNodesResponse;
import org.apache.hadoop.hbase.shaded.protobuf.generated.AdminProtos.WarmupRegionRequest;
import org.apache.hadoop.hbase.shaded.protobuf.generated.AdminProtos.WarmupRegionResponse;
import org.apache.hadoop.hbase.shaded.protobuf.generated.QuotaProtos.GetSpaceQuotaSnapshotsRequest;
import org.apache.hadoop.hbase.shaded.protobuf.generated.QuotaProtos.GetSpaceQuotaSnapshotsResponse;
import org.apache.hadoop.hbase.security.User;
import org.apache.hadoop.hbase.shaded.protobuf.generated.RPCProtos.RequestHeader;
import org.apache.hadoop.hbase.zookeeper.ZKWatcher;
import org.apache.hbase.thirdparty.com.google.protobuf.Message;

import com.google.protobuf.RpcController;
import com.google.protobuf.ServiceException;

/**
 *
 */


public class BaseHRegionServer implements AdminProtos.AdminService.BlockingInterface, Server, org.apache.hadoop.hbase.ipc.PriorityFunction {

    
    
  
    

    @Override
    public Configuration getConfiguration() {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public ZKWatcher getZooKeeper() {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public ServerName getServerName() {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public void abort(String s, Throwable throwable) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public boolean isAborted() {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public void stop(String s) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public boolean isStopped() {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public int getPriority(RequestHeader header, Message param,User user) {
        return org.apache.hadoop.hbase.HConstants.NORMAL_QOS;
    }

    @Override
    public long getDeadline(RequestHeader header, Message param) {
        return 0;
    }

    @Override
    public org.apache.hadoop.hbase.client.ClusterConnection getConnection() {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public org.apache.hadoop.hbase.zookeeper.MetaTableLocator getMetaTableLocator() {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public org.apache.hadoop.hbase.CoordinatedStateManager getCoordinatedStateManager() {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public org.apache.hadoop.hbase.ChoreService getChoreService() {
        throw new UnsupportedOperationException("Not implemented");
    }

    
	

	

	@Override
	public Connection createConnection(Configuration conf) throws IOException {
		 
		return null;
	}

	@Override
	public ClusterConnection getClusterConnection() {
		 
		return null;
	}

	@Override
	public ClearCompactionQueuesResponse clearCompactionQueues(
			org.apache.hbase.thirdparty.com.google.protobuf.RpcController arg0, ClearCompactionQueuesRequest arg1)
			throws org.apache.hbase.thirdparty.com.google.protobuf.ServiceException {
		 
		throw new UnsupportedOperationException("Not implemented");
	}

	@Override
	public GetRegionInfoResponse getRegionInfo(org.apache.hbase.thirdparty.com.google.protobuf.RpcController controller,
			GetRegionInfoRequest request) throws org.apache.hbase.thirdparty.com.google.protobuf.ServiceException {
		 
		throw new UnsupportedOperationException("Not implemented");
	}

	@Override
	public GetStoreFileResponse getStoreFile(org.apache.hbase.thirdparty.com.google.protobuf.RpcController controller,
			GetStoreFileRequest request) throws org.apache.hbase.thirdparty.com.google.protobuf.ServiceException {
		 
		throw new UnsupportedOperationException("Not implemented");
	}

	@Override
	public GetOnlineRegionResponse getOnlineRegion(
			org.apache.hbase.thirdparty.com.google.protobuf.RpcController controller, GetOnlineRegionRequest request)
			throws org.apache.hbase.thirdparty.com.google.protobuf.ServiceException {
		 
		throw new UnsupportedOperationException("Not implemented");
	}

	@Override
	public OpenRegionResponse openRegion(org.apache.hbase.thirdparty.com.google.protobuf.RpcController controller,
			OpenRegionRequest request) throws org.apache.hbase.thirdparty.com.google.protobuf.ServiceException {
		 
		throw new UnsupportedOperationException("Not implemented");
	}

	@Override
	public WarmupRegionResponse warmupRegion(org.apache.hbase.thirdparty.com.google.protobuf.RpcController controller,
			WarmupRegionRequest request) throws org.apache.hbase.thirdparty.com.google.protobuf.ServiceException {
		 
		throw new UnsupportedOperationException("Not implemented");
	}

	@Override
	public CloseRegionResponse closeRegion(org.apache.hbase.thirdparty.com.google.protobuf.RpcController controller,
			CloseRegionRequest request) throws org.apache.hbase.thirdparty.com.google.protobuf.ServiceException {
		 
		throw new UnsupportedOperationException("Not implemented");
	}

	@Override
	public FlushRegionResponse flushRegion(org.apache.hbase.thirdparty.com.google.protobuf.RpcController controller,
			FlushRegionRequest request) throws org.apache.hbase.thirdparty.com.google.protobuf.ServiceException {
		 
		throw new UnsupportedOperationException("Not implemented");
	}

	@Override
	public CompactRegionResponse compactRegion(org.apache.hbase.thirdparty.com.google.protobuf.RpcController controller,
			CompactRegionRequest request) throws org.apache.hbase.thirdparty.com.google.protobuf.ServiceException {
		 
		throw new UnsupportedOperationException("Not implemented");
	}

	@Override
	public ReplicateWALEntryResponse replicateWALEntry(
			org.apache.hbase.thirdparty.com.google.protobuf.RpcController controller, ReplicateWALEntryRequest request)
			throws org.apache.hbase.thirdparty.com.google.protobuf.ServiceException {
		 
		throw new UnsupportedOperationException("Not implemented");
	}

	@Override
	public ReplicateWALEntryResponse replay(org.apache.hbase.thirdparty.com.google.protobuf.RpcController controller,
			ReplicateWALEntryRequest request) throws org.apache.hbase.thirdparty.com.google.protobuf.ServiceException {
		 
		throw new UnsupportedOperationException("Not implemented");
	}

	@Override
	public RollWALWriterResponse rollWALWriter(org.apache.hbase.thirdparty.com.google.protobuf.RpcController controller,
			RollWALWriterRequest request) throws org.apache.hbase.thirdparty.com.google.protobuf.ServiceException {
		 
		throw new UnsupportedOperationException("Not implemented");
	}

	@Override
	public GetServerInfoResponse getServerInfo(org.apache.hbase.thirdparty.com.google.protobuf.RpcController controller,
			GetServerInfoRequest request) throws org.apache.hbase.thirdparty.com.google.protobuf.ServiceException {
		 
		throw new UnsupportedOperationException("Not implemented");
	}

	@Override
	public StopServerResponse stopServer(org.apache.hbase.thirdparty.com.google.protobuf.RpcController controller,
			StopServerRequest request) throws org.apache.hbase.thirdparty.com.google.protobuf.ServiceException {
		 
		throw new UnsupportedOperationException("Not implemented");
	}

	@Override
	public UpdateFavoredNodesResponse updateFavoredNodes(
			org.apache.hbase.thirdparty.com.google.protobuf.RpcController controller, UpdateFavoredNodesRequest request)
			throws org.apache.hbase.thirdparty.com.google.protobuf.ServiceException {
		 
		throw new UnsupportedOperationException("Not implemented");
	}

	@Override
	public UpdateConfigurationResponse updateConfiguration(
			org.apache.hbase.thirdparty.com.google.protobuf.RpcController controller,
			UpdateConfigurationRequest request)
			throws org.apache.hbase.thirdparty.com.google.protobuf.ServiceException {
		 
		throw new UnsupportedOperationException("Not implemented");
	}

	@Override
	public GetRegionLoadResponse getRegionLoad(org.apache.hbase.thirdparty.com.google.protobuf.RpcController controller,
			GetRegionLoadRequest request) throws org.apache.hbase.thirdparty.com.google.protobuf.ServiceException {
		 
		throw new UnsupportedOperationException("Not implemented");
	}

	@Override
	public ClearRegionBlockCacheResponse clearRegionBlockCache(
			org.apache.hbase.thirdparty.com.google.protobuf.RpcController controller,
			ClearRegionBlockCacheRequest request)
			throws org.apache.hbase.thirdparty.com.google.protobuf.ServiceException {
		 
		throw new UnsupportedOperationException("Not implemented");
	}

	@Override
	public ExecuteProceduresResponse executeProcedures(
			org.apache.hbase.thirdparty.com.google.protobuf.RpcController controller, ExecuteProceduresRequest request)
			throws org.apache.hbase.thirdparty.com.google.protobuf.ServiceException {
		 
		throw new UnsupportedOperationException("Not implemented");
	}

	@Override
	public GetSpaceQuotaSnapshotsResponse getSpaceQuotaSnapshots(
			org.apache.hbase.thirdparty.com.google.protobuf.RpcController arg0, GetSpaceQuotaSnapshotsRequest arg1)
			throws org.apache.hbase.thirdparty.com.google.protobuf.ServiceException {
		 
		throw new UnsupportedOperationException("Not implemented");
	}

}
