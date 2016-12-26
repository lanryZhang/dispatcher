package com.ifeng.ipserver.server.codec;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.util.HashMap;
import java.util.Map;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;
import org.apache.mina.filter.codec.demux.MessageDecoderAdapter;
import org.apache.mina.filter.codec.demux.MessageDecoderResult;
import com.ifeng.common.misc.Logger;
import com.ifeng.ipserver.server.message.HttpRequestMessage;

/**
 * <title> HttpRequestDecoder </title>
 * 
 * <pre>
 * ipserver服务端解码器
 * 
 * </pre>
 * 
 * Copyright © 2012 Phoenix New Media Limited All Rights Reserved.
 * 
 * @author <a href="mailto:jl@ifeng.com">Jin Lin</a>
 * @author <a href="mailto:jinmy@ifeng.com">Jin Mingyan</a>
 */
public class HttpRequestDecoder extends MessageDecoderAdapter {
	private static Logger log = Logger.getLogger(HttpRequestDecoder.class);
    private static final byte[] CONTENT_LENGTH = new String("Content-Length:")
            .getBytes();

    private CharsetDecoder decoder = Charset.defaultCharset().newDecoder();

    private HttpRequestMessage request = null;

    public HttpRequestDecoder() {
    }

    private boolean messageComplete(IoBuffer in) throws Exception {
        int last = in.remaining() - 1;
        if (in.remaining() < 4)
            return false;

        // to speed up things we check if the Http request is a GET or POST
        if (in.get(0) == (byte) 'G' && in.get(1) == (byte) 'E'
                && in.get(2) == (byte) 'T') {
            // Http GET request therefore the last 4 bytes should be 0x0D 0x0A 0x0D 0x0A
            return (in.get(last) == (byte) 0x0A
                    && in.get(last - 1) == (byte) 0x0D
                    && in.get(last - 2) == (byte) 0x0A && in.get(last - 3) == (byte) 0x0D);
        } else if (in.get(0) == (byte) 'P' && in.get(1) == (byte) 'O'
                && in.get(2) == (byte) 'S' && in.get(3) == (byte) 'T') {
            // Http POST request
            // first the position of the 0x0D 0x0A 0x0D 0x0A bytes
            int eoh = -1;
            for (int i = last; i > 2; i--) {
                if (in.get(i) == (byte) 0x0A && in.get(i - 1) == (byte) 0x0D
                        && in.get(i - 2) == (byte) 0x0A
                        && in.get(i - 3) == (byte) 0x0D) {
                    eoh = i + 1;
                    break;
                }
            }
            if (eoh == -1)
                return false;
            for (int i = 0; i < last; i++) {
                boolean found = false;
                for (int j = 0; j < CONTENT_LENGTH.length; j++) {
                    if (in.get(i + j) != CONTENT_LENGTH[j]) {
                        found = false;
                        break;
                    }
                    found = true;
                }
                if (found) {
                    // retrieve value from this position till next 0x0D 0x0A
                    StringBuilder contentLength = new StringBuilder();
                    for (int j = i + CONTENT_LENGTH.length; j < last; j++) {
                        if (in.get(j) == 0x0D)
                            break;
                        contentLength.append(new String(
                                new byte[] { in.get(j) }));
                    }
                    // if content-length worth of data has been received then the message is complete
                    return (Integer.parseInt(contentLength.toString().trim())
                            + eoh == in.remaining());
                }
            }
        } else if (in.get(0) == (byte) 'H' && in.get(1) == (byte) 'E'
                && in.get(2) == (byte) 'A' && in.get(3) == (byte) 'D') {
        	return true;
        }

        // the message is not complete and we need more data
        return false;
    }

    private HttpRequestMessage decodeBody(IoBuffer in) {
        request = new HttpRequestMessage();
        try {
            request.setHeaders(parseRequest(new StringReader(in
                    .getString(decoder))));
            return request;
        } catch (CharacterCodingException ex) {
            log.warn("HttpRequestMessage CharacterCodingException:"+ex.getMessage());
        }

        return null;
    }

    private Map parseRequest(Reader is) {
        Map<String, String[]> map = new HashMap<String, String[]>();
        BufferedReader rdr = new BufferedReader(is);

        try {
            // Get request URL.
            String line = rdr.readLine();
            String[] url = line.split(" ");
            if (url.length < 3 || "/favicon.ico".equals(url[1]))
                return map;

            map.put("URI", new String[] { line });
            //log.info("URL:"+line);
            map.put("Method", new String[] { url[0].toUpperCase() });
            map.put("Context", new String[] { url[1].substring(1) });
            map.put("Protocol", new String[] { url[2] });
            // Read header
            while ((line = rdr.readLine()) != null && line.length() > 0) {
                String[] tokens = line.split(": ");
                if(tokens.length>=2){
                	map.put(tokens[0], new String[] { tokens[1] });
//                	if (tokens[0].toUpperCase().equals("USER-AGENT")){
//                		log.info(tokens[0]+":"+tokens[1]);
//                	}
                }
            }

            // If method 'POST' then read Content-Length worth of data
            if (url[0].equalsIgnoreCase("POST")) {
                int len = Integer.parseInt(map.get("Content-Length")[0]);
                char[] buf = new char[len];
                if (rdr.read(buf) == len) {
                    line = String.copyValueOf(buf);
                }
            } else if (url[0].equalsIgnoreCase("GET")||url[0].equalsIgnoreCase("HEAD")) {
                int idx = url[1].indexOf('?');
                if (idx != -1) {
                    map.put("Context",
                            new String[] { url[1].substring(1, idx) });
                    line = url[1].substring(idx + 1);
                } else {
                    line = null;
                }
            }
            if (line != null) {
            	map.put("PARAMETERSURL", new String[]{line});
                String[] match = line.split("\\&");
                for (int i = 0; i < match.length; i++) {
                    String[] params = new String[1];
                    String[] tokens = match[i].split("=");
                    switch (tokens.length) {
                    case 0:
                        map.put("@".concat(match[i]), new String[] {});
                        break;
                    case 1:
                        map.put("@".concat(tokens[0]), new String[] {});
                        break;
                    default:
                        String name = "@".concat(tokens[0]);
                        if (map.containsKey(name)) {
                            params = map.get(name);
                            String[] tmp = new String[params.length + 1];
                            for (int j = 0; j < params.length; j++)
                                tmp[j] = params[j];
                            params = null;
                            params = tmp;
                        }
                        params[params.length - 1] = tokens[1].trim();
                        map.put(name, params);
                    }
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        return map;
    }

	/* 
	* decodable
	*  Return NEED_DATA if the whole header is not read yet.
	* @param iosession
	* @param iobuffer
	* @return 
	* @see org.apache.mina.filter.codec.demux.MessageDecoder#decodable(org.apache.mina.core.session.IoSession, org.apache.mina.core.buffer.IoBuffer) 
	*/
	
	@Override
	public MessageDecoderResult decodable(IoSession iosession, IoBuffer iobuffer) {
		 // Return NEED_DATA if the whole header is not read yet.
        try {
            return messageComplete(iobuffer) ? MessageDecoderResult.OK
                    : MessageDecoderResult.NEED_DATA;
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return MessageDecoderResult.NOT_OK;
	}

	/* 
	* decode
	* Return NEED_DATA if the body is not fully read.
	* @param iosession
	* @param iobuffer
	* @param protocoldecoderoutput
	* @return
	* @throws Exception 
	* @see org.apache.mina.filter.codec.demux.MessageDecoder#decode(org.apache.mina.core.session.IoSession, org.apache.mina.core.buffer.IoBuffer, org.apache.mina.filter.codec.ProtocolDecoderOutput) 
	*/
	@Override
	public MessageDecoderResult decode(IoSession iosession, IoBuffer iobuffer,
			ProtocolDecoderOutput protocoldecoderoutput) throws Exception {
		  // Try to decode body
        HttpRequestMessage m = decodeBody(iobuffer);

        // Return NEED_DATA if the body is not fully read.
        if (m == null)
            return MessageDecoderResult.NEED_DATA;

        protocoldecoderoutput.write(m);

        return MessageDecoderResult.OK;
	}
}
