package com.ifeng.ipserver.server.message;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

/**
 * <title> HttpRequestMessage </title>
 * 
 * <pre>
 * 	请求消息描述。
 * 	
 * </pre>
 * 
 * Copyright © 2012 Phoenix New Media Limited All Rights Reserved.
 * 
 * @author <a href="mailto:jl@ifeng.com">Jin Lin</a>
 * @author <a href="mailto:jinmy@ifeng.com">Jin Mingyan</a>
 */
public class HttpRequestMessage {
    /** Map<String, String[]> */
    private Map headers = null;
    
    public static final String HTTP_GET = "GET";
    public static final String HTTP_POST = "POST";
    public static final String HTTP_HEAD = "HEAD";

    public void setHeaders(Map headers) {
        this.headers = headers;
    }

    public Map getHeaders() {
        return headers;
    }

    public String getContext() {
        String[] context = (String[]) headers.get("Context");
        return context == null ? "" : context[0];
    }
    public String getReferer() {
        String[] context = (String[]) headers.get("Referer");
        return context == null ? "" : context[0];
    }
    public String getUserAgent() {
        String[] context = (String[]) headers.get("User-Agent");
        return context == null ? "" : context[0];
    }
    public String getHost() {
        String[] context = (String[]) headers.get("Host");
        return context == null ? "" : context[0];
    }
    
    public String getMethod(){
    	 String[] context = (String[]) headers.get("Method");
    	  return context == null ? "" : context[0];
    }
    
    
    public String getHttpXforward(){
    	String[] context = (String[]) headers.get("X-Forwarded-For");
    	return context == null ? "" : context[0];
    }
    
    public String getParameter(String name) {
        String[] param = (String[]) headers.get("@".concat(name));
        return param == null ? "" : param[0];
    }

    public String[] getParameters(String name) {
        String[] param = (String[]) headers.get("@".concat(name));
        return param == null ? new String[] {} : param;
    }

    public String[] getHeader(String name) {
        return (String[]) headers.get(name);
    }

    @Override
	public String toString() {
        StringBuilder str = new StringBuilder();

        Iterator it = headers.entrySet().iterator();
        while (it.hasNext()) {
            Entry e = (Entry) it.next();
            str.append(e.getKey() + " : "
                    + arrayToString((String[]) e.getValue(), ',') + "\n");
        }
        return str.toString();
    }

    public static String arrayToString(String[] s, char sep) {
        if (s == null || s.length == 0)
            return "";
        StringBuffer buf = new StringBuffer();
        if (s != null) {
            for (int i = 0; i < s.length; i++) {
                if (i > 0)
                    buf.append(sep);
                buf.append(s[i]);
            }
        }
        return buf.toString();
    }
}
