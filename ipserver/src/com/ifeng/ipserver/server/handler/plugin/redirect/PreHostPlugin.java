package com.ifeng.ipserver.server.handler.plugin.redirect;

import java.util.Map;

import org.w3c.dom.Element;

import com.ifeng.common.conf.ConfigException;
import com.ifeng.common.conf.ConfigRoot;
import com.ifeng.common.conf.Configurable;
import com.ifeng.common.misc.Logger;
import com.ifeng.common.plugin.core.abst.AbstLogicPlugin;
import com.ifeng.ipserver.server.handler.ServiceContext;
import com.ifeng.ipserver.server.message.HttpRequestMessage;


/**
 *<title>PreHostPlugin<title>
 *<pre>
 *</pre>
 *
 * @author <a href="mailto:chengyong@ifeng.com">banban</a>
 *
 *  Copyright © 2014 Phoenix New Media Limited All Rights Reserved.
 */
public class PreHostPlugin extends AbstLogicPlugin implements Configurable {
	
	private Map hostMap;
	

	private static Logger log = Logger.getLogger(PreHostPlugin.class);
	@Override
	public Object execute(Object o) {
		Map contextMap = (Map) o;
		HttpRequestMessage request = (HttpRequestMessage) contextMap
				.get(ServiceContext.REQUEST_MESSAGE);
		String context = request.getContext();
		String host = "";
		if (context != null && context.indexOf("/") > 0){
			host = context.substring(0,context.indexOf("/"));
		}
		RequestHost  requestHost = (RequestHost) hostMap.get(host);
		
		if(null==requestHost){
			requestHost =  (RequestHost) hostMap.get("other");
		}
		
		//log.info("requestHost status="+requestHost.getHttpStatus()+" threshold"+requestHost.getThreshold());
		contextMap.put("requestHost", requestHost);
		return Boolean.TRUE;
	}
	
	@Override
	public Object config(ConfigRoot configRoot, Object parent, Element configEle)
			throws ConfigException {
		this.hostMap =  (Map) configRoot.createChildObject(this, configEle,
				"host-map", true);
		return this;
	}
	
	public Map getHostMap() {
		return hostMap;
	}
	public void setHostMap(Map hostMap) {
		this.hostMap = hostMap;
	}


}
