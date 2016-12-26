package com.ifeng.ipserver.server.handler;

import com.ifeng.common.misc.IpV4Address;
import com.ifeng.common.misc.Logger;
import com.ifeng.common.plugin.core.itf.IntfPlugin;
import com.ifeng.ipserver.server.message.HttpRequestMessage;
import com.ifeng.ipserver.server.message.HttpResponseMessage;
import org.apache.mina.core.future.IoFutureListener;
import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;


/**
 * <title> PluginServerHandler </title>
 * 
 * <pre>
 * 	消息处理器，采用插件结构依赖plugin变量来描述具体的处理流程。
 * 	
 * </pre>
 * 
 * Copyright © 2012 Phoenix New Media Limited All Rights Reserved.
 * 
 * @author <a href="mailto:chenyong@ifeng.com">banban</a>
 * @author <a href="mailto:jinmy@ifeng.com">Jin Mingyan</a>
 */
public class PluginServerHandler extends IoHandlerAdapter {
	private static Logger log = Logger.getLogger(PluginServerHandler.class);
	
	private IntfPlugin plugin;
	
	@Override
	public void sessionOpened(IoSession session) {
		session.getConfig().setIdleTime(IdleStatus.BOTH_IDLE, 1);
		if(log.isDebugEnabled()){
			log.debug("incomming client : " + session.getRemoteAddress());
		}
	}
	

    @Override
	public void messageReceived(IoSession session, Object message) {
    	HttpRequestMessage request = (HttpRequestMessage) message;
		String remoteIp = request.getHttpXforward();
		if(null==remoteIp || !IpV4Address.isValidString(remoteIp)){
			InetSocketAddress inetSocketAddress = (InetSocketAddress) session
					.getRemoteAddress();
			remoteIp = inetSocketAddress.getAddress().getHostAddress();
		}else{
			log.info("get remoteip from x-forward "+remoteIp);
		}
		
		String context = request.getContext();
		HttpResponseMessage response = new HttpResponseMessage();		

		if (remoteIp.equals("127.0.0.1") || remoteIp.equals("localhost")) {// 本机请求
			response.handleRequest("Localhost Request", response,
					HttpResponseMessage.HTTP_STATUS_SUCCESS);
		}else if (null == context || "".equals(context)){
			response.handleRequest("Context Is Empty", response,
					HttpResponseMessage.HTTP_STATUS_SUCCESS);
		}else {// 非本机请求
	    	Map<String, Object> contextMap = new HashMap<String, Object>();
	    	contextMap.put(ServiceContext.REQUEST_MESSAGE, request);
	    	contextMap.put(ServiceContext.RESPONSE_MESSAGE, response);	    	
	    	contextMap.put(ServiceContext.REMOTE_IP, remoteIp);
	    	contextMap.put(ServiceContext.DEVICE_TYPE, getDeviceType(request));
	    	plugin.execute(contextMap);
		}
		if (response != null) {
			session.write(response).addListener(IoFutureListener.CLOSE);
		}
    }
    
	@Override
	public void messageSent(IoSession session, Object message) {
	}

	@Override
	public void sessionIdle(IoSession session, IdleStatus status) {
		// session.getConfig().setIdleTime(IdleStatus.BOTH_IDLE, 60);
		if(log.isDebugEnabled()){
			log.debug("*** IDLE #" + session.getIdleCount(IdleStatus.BOTH_IDLE)
					+ " ***");
		}
		session.close(true);
	}

	@Override
	public void exceptionCaught(IoSession session, Throwable cause) {
//		logger.error("Got Exception", cause);
		//cause.printStackTrace();
		session.close(true);
		// Wait until the connection is closed
	}

	private String getDeviceType(HttpRequestMessage request ){
		String[] deviceTypes = new String[] {"ANDROID","IPHONE","WINDOWS PHONE","IPAD","WINDOWS"};
		//log.info("userAgent: "+request.getUserAgent());
		for (String dt : deviceTypes) {
			if (request.getUserAgent().toUpperCase().contains(dt)){
				return dt.replace(" ","");
			}
		}
		return "UNKNOWN";
	}
	public IntfPlugin getPlugin() {
		return plugin;
	}

	public void setPlugin(IntfPlugin plugin) {
		this.plugin = plugin;
	}
      
}
