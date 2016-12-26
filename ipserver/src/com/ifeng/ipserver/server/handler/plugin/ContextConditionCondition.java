package com.ifeng.ipserver.server.handler.plugin;

import java.util.Map;

import com.ifeng.common.plugin.core.abst.AbstLogicPlugin;
import com.ifeng.ipserver.server.handler.ServiceContext;
import com.ifeng.ipserver.server.message.HttpRequestMessage;


/**
 * <title> ContextSwitchCondition </title>
 * 
 * <pre>
 * 	这是一个条件插件，获得请求的context。也就是http://ips.ifeng.com/之后的部分
 * 	
 * </pre>
 * 
 * Copyright © 2012 Phoenix New Media Limited All Rights Reserved.
 * 
 * @author <a href="mailto:jl@ifeng.com">Jin Lin</a>
 * @author <a href="mailto:jinmy@ifeng.com">Jin Mingyan</a>
 */
public class ContextConditionCondition extends AbstLogicPlugin {
	
	/* (non-Javadoc)
	 * @see com.ifeng.common.plugin.core.itf.IntfPlugin#execute(java.lang.Object)
	 */
	@Override
	public Object execute(Object o) {
		Map contextMap =(Map)o;
		HttpRequestMessage request =(HttpRequestMessage)contextMap.get(ServiceContext.REQUEST_MESSAGE);		
		String context = request.getContext();
		if(context.contains("wideo.ifeng.com")){
			return Boolean.TRUE;
		}else{
			return Boolean.FALSE;
		}
	}
}


