package com.ifeng.ipserver.server.message;

import java.io.ByteArrayOutputStream;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.mina.core.buffer.IoBuffer;

import com.ifeng.ipserver.server.Server;

/**
 * <title> HttpResponseMessage </title>
 * 
 * <pre>
 * 	应答消息描述。
 * 
 * </pre>
 * 
 * Copyright © 2012 Phoenix New Media Limited All Rights Reserved.
 * 
 * @author <a href="mailto:jl@ifeng.com">Jin Lin</a>
 * @author <a href="mailto:jinmy@ifeng.com">Jin Mingyan</a>
 */

public class HttpResponseMessage {
	/** HTTP response codes */
	public static final int HTTP_STATUS_SUCCESS = 200;

	public static final int HTTP_STATUS_NOT_FOUND = 404;

	public static final int HTTP_STATUS_MOVED_TEMPORARILY = 302;

	public static final int HTTP_STATUS_MOVED_PERMANENTLY = 301;

	/** Map<String, String> */
	private Map<String, String> headers = new HashMap<String, String>();

	/** Storage for body of HTTP response. */
	private ByteArrayOutputStream body = new ByteArrayOutputStream(1024);

	private int responseCode = HTTP_STATUS_SUCCESS;

	private String location = "";

	public HttpResponseMessage() {
		// headers.put("Location",
		// "http://video.ifeng.com/video05/2011/08/09/72a4ae28-24a2-41a6-80f7-1b7da89ce957.mp4");
		headers.put("Server", "HttpServer (" + Server.VERSION_STRING + ')');
		headers.put("Cache-Control", "private");
		// headers.put("Content-Type", "text/html; charset=utf-8");
		// headers.put("Connection", "keep-alive");
		headers.put("Access-Control-Allow-Origin", "*");
		headers.put("Date", new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz").format(new Date()));
		headers.put("Last-Modified", new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz").format(new Date()));
	}

	public void setLastModified(String date) {
		headers.put("Last-Modified", date);
	}

	public void setExpires(String date) {
		headers.put("Expires", date);
	}

	public void setCacheControl(String cache) {
		headers.put("Cache-Control", cache);
	}

	public Map getHeaders() {
		return headers;
	}

	public void setContentType(String contentType) {
		headers.put("Content-Type", contentType);
	}

	public void setResponseCode(int responseCode) {
		this.responseCode = responseCode;
	}

	public int getResponseCode() {
		return this.responseCode;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		headers.put("Location", location);
	}

	public void appendBody(byte[] b) {
		try {
			body.write(b);
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	public void appendBody(String s) {
		try {
			body.write(s.getBytes());
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	public IoBuffer getBody() {
		return IoBuffer.wrap(body.toByteArray());
	}

	public int getBodyLength() {
		return body.size();
	}

	/**
	 * 对请求进行响应
	 * 
	 * @param info
	 *            - 返回文本或跳转地址
	 * @param response
	 *            - 响应对象
	 * @param httpStatus
	 *            - 响应状态(200,404,302)
	 * @return
	 */
	public HttpResponseMessage handleRequest(String info, HttpResponseMessage response, int httpStatus) {
		response.setResponseCode(httpStatus);
		switch (httpStatus) {
		case HttpResponseMessage.HTTP_STATUS_SUCCESS:
			response.setContentType("text/html; charset=utf-8");
			response.appendBody(info);
			break;
		case HttpResponseMessage.HTTP_STATUS_MOVED_TEMPORARILY:
			response.setLocation(info);
			break;
		case HttpResponseMessage.HTTP_STATUS_NOT_FOUND:
			response.setContentType("text/html; charset=utf-8");
			response.appendBody("404 Not Found.");
			break;
		case HttpResponseMessage.HTTP_STATUS_MOVED_PERMANENTLY:
			response.setLocation(info);
			break;
		}
//		 response.setResponseCode(HttpResponseMessage.HTTP_STATUS_SUCCESS);
//		 response.setContentType("text/html; charset=utf-8");
//		 response.appendBody("");
		return response;
	}
}
