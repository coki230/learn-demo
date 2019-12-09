/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package hello;

import io.grpc.Channel;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;

import java.util.*;

/**
 * @author wusheng, zhang xin
 */
public class GRPCChannelManager {

    private volatile GRPCChannel managedChannel = null;
    private volatile List<String> grpcServers;

    public void init() throws Exception {
        grpcServers = Arrays.asList("cloud-monitor:11800");
        String server = grpcServers.get(0);
        String[] ipAndPort = server.split(":");
        managedChannel = GRPCChannel.newBuilder(ipAndPort[0], Integer.parseInt(ipAndPort[1]))
                .addManagedChannelBuilder(new StandardChannelBuilder())
                .addManagedChannelBuilder(new TLSChannelBuilder())
                .addChannelDecorator(new AgentIDDecorator())
                .addChannelDecorator(new AuthenticationDecorator())
                .build();
    }


    public Channel getChannel() {
        return managedChannel.getChannel();
    }


    private boolean isNetworkError(Throwable throwable) {
        if (throwable instanceof StatusRuntimeException) {
            StatusRuntimeException statusRuntimeException = (StatusRuntimeException) throwable;
            return statusEquals(statusRuntimeException.getStatus(),
                Status.UNAVAILABLE,
                Status.PERMISSION_DENIED,
                Status.UNAUTHENTICATED,
                Status.RESOURCE_EXHAUSTED,
                Status.UNKNOWN
            );
        }
        return false;
    }

    private boolean statusEquals(Status sourceStatus, Status... potentialStatus) {
        for (Status status : potentialStatus) {
            if (sourceStatus.getCode() == status.getCode()) {
                return true;
            }
        }
        return false;
    }
}
