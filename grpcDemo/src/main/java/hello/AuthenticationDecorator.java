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

import io.grpc.*;

/**
 * Active authentication header by Config.Agent.AUTHENTICATION
 *
 * @author wu-sheng, zhang xin
 */
public class AuthenticationDecorator implements ChannelDecorator {
    private static final Metadata.Key<String> AUTH_HEAD_HEADER_NAME =
        Metadata.Key.of("Authentication", Metadata.ASCII_STRING_MARSHALLER);

    @Override
    public Channel build(Channel channel) {

        return ClientInterceptors.intercept(channel, new ClientInterceptor() {
            @Override
            public <REQ, RESP> ClientCall<REQ, RESP> interceptCall(MethodDescriptor<REQ, RESP> method,
                                                                   CallOptions options, Channel channel) {
                return new ForwardingClientCall.SimpleForwardingClientCall<REQ, RESP>(channel.newCall(method, options)) {
                    @Override
                    public void start(Listener<RESP> responseListener, Metadata headers) {
                        headers.put(AUTH_HEAD_HEADER_NAME, "");

                        super.start(responseListener, headers);
                    }
                };
            }
        });
    }
}
