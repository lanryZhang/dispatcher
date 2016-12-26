package com.ifeng.ipserver.server.codec;

import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.Map.Entry;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolEncoderOutput;
import org.apache.mina.filter.codec.demux.MessageEncoder;

import com.ifeng.ipserver.server.message.HttpResponseMessage;

/**
 * <title> HttpResponseEncoder </title>
 * 
 * <pre>
 * ipserver服务端编码器
 * 
 * </pre>
 * 
 * Copyright © 2012 Phoenix New Media Limited All Rights Reserved.
 * 
 * @author <a href="mailto:jl@ifeng.com">Jin Lin</a>
 * @author <a href="mailto:jinmy@ifeng.com">Jin Mingyan</a>
 */
public class HttpResponseEncoder implements MessageEncoder {
    private static final Set<Class<?>> TYPES;

    static {
        Set<Class<?>> types = new HashSet<Class<?>>();
        types.add(HttpResponseMessage.class);
        TYPES = Collections.unmodifiableSet(types);
    }

    private static final byte[] CRLF = new byte[] { 0x0D, 0x0A };

    public HttpResponseEncoder() {
    }

    @Override
	public void encode(IoSession session, Object message,
            ProtocolEncoderOutput out) throws Exception {
        HttpResponseMessage msg = (HttpResponseMessage) message;
        IoBuffer buf = IoBuffer.allocate(256);
        // Enable auto-expand for easier encoding
        buf.setAutoExpand(true);

        try {
            // output all headers except the content length
            CharsetEncoder encoder = Charset.defaultCharset().newEncoder();
            buf.putString("HTTP/1.1 ", encoder);
            buf.putString(String.valueOf(msg.getResponseCode()), encoder);
            switch (msg.getResponseCode()) {
            case HttpResponseMessage.HTTP_STATUS_SUCCESS:
                buf.putString(" OK", encoder);
                break;
            case HttpResponseMessage.HTTP_STATUS_NOT_FOUND:
                buf.putString(" Not Found", encoder);
                break;
                
            case HttpResponseMessage.HTTP_STATUS_MOVED_TEMPORARILY:
                buf.putString(" Moved Temporarily", encoder);
                break;
            }
            buf.put(CRLF);
            for (Iterator it = msg.getHeaders().entrySet().iterator(); it
                    .hasNext();) {
                Entry entry = (Entry) it.next();
                buf.putString((String) entry.getKey(), encoder);
                buf.putString(": ", encoder);
                buf.putString((String) entry.getValue(), encoder);
                buf.put(CRLF);
            }
            // now the content length is the body length
            buf.putString("Content-Length: ", encoder);
            buf.putString(String.valueOf(msg.getBodyLength()), encoder);
            buf.put(CRLF);
            buf.put(CRLF);
            // add body
            buf.put(msg.getBody());
        } catch (CharacterCodingException ex) {
            ex.printStackTrace();
        }

        buf.flip();
        out.write(buf);
    }

    public Set<Class<?>> getMessageTypes() {
        return TYPES;
    }
}
