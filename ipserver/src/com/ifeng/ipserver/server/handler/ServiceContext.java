package com.ifeng.ipserver.server.handler;

/**
 * <title> ServiceContext </title>
 * 
 * <pre>
 * 	上下文静态声明。
 * 	
 * </pre>
 * 
 * Copyright © 2012 Phoenix New Media Limited All Rights Reserved.
 * 
 * @author <a href="mailto:jl@ifeng.com">Jin Lin</a>
 * @author <a href="mailto:jinmy@ifeng.com">Jin Mingyan</a>
 */

public class ServiceContext {
	private ServiceContext(){}
	public static final String SESSION="SESSION";
	public static final String REQUEST_MESSAGE="REQUEST_MESSAGE";
	public static final String REMOTE_IP="REMOTE_IP";
	public static final String HTTP_CONTEXT= "HTTP_CONTEXT";
	public static final String RESPONSE_MESSAGE="RESPONSE_MESSAGE";
	public static final String LOCATION_MESSAGE="LOCATION_MESSAGE";
	public static final String PARAMETER_IP ="PARAMETER_IP";
	public static final String HTTP_METHOD= "HTTP_METHOD";
	public static final String DEVICE_TYPE = "DEVICE_TYPE";
	public static final String FILTER_URL = "FILTER_URL";
}


