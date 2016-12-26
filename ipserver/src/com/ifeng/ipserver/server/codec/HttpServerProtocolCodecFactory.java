/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *  
 *    http://www.apache.org/licenses/LICENSE-2.0
 *  
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License. 
 *  
 */
package com.ifeng.ipserver.server.codec;

import org.apache.mina.filter.codec.demux.DemuxingProtocolCodecFactory;

import com.ifeng.ipserver.server.message.HttpResponseMessage;

/**
 * <title> HttpServerProtocolCodecFactory </title>
 * 
 * <pre>
 * 	协议处理工厂类
 * </pre>
 * 
 * Copyright © 2012 Phoenix New Media Limited All Rights Reserved.
 * 
 * @author <a href="mailto:jl@ifeng.com">Jin Lin</a>
 * @author <a href="mailto:jinmy@ifeng.com">Jin Mingyan</a>
 */
public class HttpServerProtocolCodecFactory extends
        DemuxingProtocolCodecFactory {
    public HttpServerProtocolCodecFactory() {
        super.addMessageDecoder(HttpRequestDecoder.class);
        super.addMessageEncoder(HttpResponseMessage.class, HttpResponseEncoder.class);
    }
}
