package com.ifeng.ipserver.server.handler.plugin.location;

import com.ifeng.common.conf.ConfigException;
import com.ifeng.common.conf.ConfigRoot;
import com.ifeng.common.conf.Configurable;
import com.ifeng.common.misc.IpV4Address;
import com.ifeng.common.misc.Logger;
import com.ifeng.common.plugin.core.abst.AbstLogicPlugin;
import com.ifeng.ipserver.bean.Area;
import com.ifeng.ipserver.server.handler.ServiceContext;
import com.ifeng.ipserver.server.message.HttpRequestMessage;
import com.ifeng.ipserver.server.message.HttpResponseMessage;
import com.ifeng.ipserver.service.intf.IpRangeManager;
import org.w3c.dom.Element;

import java.util.Map;

/**
 * <title> LocationPlugin</title>
 * 
 * <pre>
 * 	    根据请求中的ip地址获得用户的区域。
 * <br>
 * </pre>
 * * 配置方式为：
 * 
 * &lt;... type="com.ifeng.ipserver.server.handler.plugin。location。LocationPlugin"&gt;
 * 	 &lt;ip-range-manager .../&gt;
 * &lt/...&gt 
 *其中：
 *	ip-range-manager
 *		类型：IpRangeManager 
 *		用途：根据ip地址查询区域的管理器
 *
 * Copyright © 2012 Phoenix New Media Limited All Rights Reserved.
 * 
 * @author <a href="mailto:jl@ifeng.com">Jin Lin</a>
 * @author <a href="mailto:jinmy@ifeng.com">Jin Mingyan</a>
 */
public class LocationPlugin extends AbstLogicPlugin implements Configurable{
	private static Logger log = Logger.getLogger(LocationPlugin.class);
	private IpRangeManager ipRangeManager;
	/* (non-Javadoc)
	 * @see com.ifeng.common.plugin.core.itf.IntfPlugin#execute(java.lang.Object)
	 */
	@Override
	public Object execute(Object o) {
		Map contextMap = (Map) o;

		HttpRequestMessage request = (HttpRequestMessage) contextMap
				.get(ServiceContext.REQUEST_MESSAGE);
		HttpResponseMessage response = (HttpResponseMessage) contextMap
				.get(ServiceContext.RESPONSE_MESSAGE);
		contextMap.put(ServiceContext.HTTP_METHOD, request.getMethod());
		String ip = request.getParameter("ip");
		if(ip==null||"".equals(ip)){
			ip = (String)contextMap.get(ServiceContext.REMOTE_IP);
		}
		if (null == ip || "".equals(ip)) { // ip地址为空
			response.handleRequest("IP Address Missing", response,
					HttpResponseMessage.HTTP_STATUS_SUCCESS);
			return Boolean.FALSE;
		} else {
			//ip="49.80.171.81";
			//ip="218.202.64.2";
			//ip = "61.143.194.2";
			//ip="221.224.125.32";
			//ip="2000:0000:0000:0000:0001:2345:6789:abcd";
			// 根据ip查找地域
			Area area = null;
			try {
				area = ipRangeManager.getAreaByIp(new IpV4Address(ip));
			} catch (Exception e) {
				area=null;
			}
			if(area==null){
				//response.handleRequest("Location Not Found", response,
				//		HttpResponseMessage.HTTP_STATUS_SUCCESS);
				log.info("Location Service: ip=" + ip
						+ " Location Not Found");
				return Boolean.TRUE;
			} else {
				contextMap.put(ServiceContext.LOCATION_MESSAGE, area);
				contextMap.put(ServiceContext.PARAMETER_IP, ip);
				return Boolean.TRUE;
			}
		}
	}
	
	/* (non-Javadoc)
	 * @see com.ifeng.common.conf.Configurable#config(com.ifeng.common.conf.ConfigRoot, java.lang.Object, org.w3c.dom.Element)
	 */
	@Override
	public Object config(ConfigRoot configRoot, Object parent, Element configEle)
			throws ConfigException {
		this.ipRangeManager =  (IpRangeManager) configRoot.createChildObject(this, configEle,
				"ip-range-manager", true);
		return this;
	}
}


