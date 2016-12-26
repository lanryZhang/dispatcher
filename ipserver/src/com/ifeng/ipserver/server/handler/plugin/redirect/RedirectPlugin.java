package com.ifeng.ipserver.server.handler.plugin.redirect;

import com.ifeng.common.conf.ConfigException;
import com.ifeng.common.conf.ConfigRoot;
import com.ifeng.common.conf.Configurable;
import com.ifeng.common.misc.IpV4Address;
import com.ifeng.common.misc.Logger;
import com.ifeng.common.plugin.core.abst.AbstLogicPlugin;
import com.ifeng.ipserver.bean.Area;
import com.ifeng.ipserver.server.handler.DeviceType;
import com.ifeng.ipserver.server.handler.ServiceContext;
import com.ifeng.ipserver.server.message.HttpRequestMessage;
import com.ifeng.ipserver.server.message.HttpResponseMessage;
import com.ifeng.ipserver.service.impl.node.DynamicNewNode;
import com.ifeng.ipserver.service.intf.AreaNodeManager;
import org.w3c.dom.Element;

import java.util.Map;

/**
 * <title> RedirectPlugin</title>
 * 
 * <pre>
 * 	    根据区域将用户请求302到对应的节点
 * <br>
 * </pre>
 * 
 * Copyright © 2012 Phoenix New Media Limited All Rights Reserved.
 * 
 * @author <a href="mailto:jl@ifeng.com">Jin Lin</a>
 * @author <a href="mailto:jinmy@ifeng.com">Jin Mingyan</a>
 */
public class RedirectPlugin extends AbstLogicPlugin implements Configurable {
	private static Logger log = Logger.getLogger(RedirectPlugin.class);

	private Map map;
	private AreaNodeManager areaNodeManager;

	@Override
	public Object execute(Object o) {

		Map contextMap = (Map) o;
		HttpRequestMessage request = (HttpRequestMessage) contextMap.get(ServiceContext.REQUEST_MESSAGE);
		HttpResponseMessage response = (HttpResponseMessage) contextMap.get(ServiceContext.RESPONSE_MESSAGE);
		String remoteIp = (String) contextMap.get(ServiceContext.REMOTE_IP);
		String context = request.getContext();
		RequestHost requestHost = (RequestHost) contextMap.get("requestHost");
		String deviceType = (String) contextMap.get(ServiceContext.DEVICE_TYPE);
		String targetUrl = null;
		String gid = request.getParameter("gid");
		if(gid.equals("")){
			gid = "null";
		}

		int httpStatus = requestHost.getHttpStatus();// HttpResponseMessage.HTTP_STATUS_MOVED_TEMPORARILY;
		if (deviceType == DeviceType.ANDROID) { // android请求状态需要设置为301
			httpStatus = HttpResponseMessage.HTTP_STATUS_MOVED_PERMANENTLY;
		}
		//log.info("request from "+deviceType+",status = " +httpStatus );

		boolean isHead = false;
		if (contextMap.get(ServiceContext.HTTP_METHOD).equals(HttpRequestMessage.HTTP_HEAD)) {
			isHead = true;
		}
		
		// 如果有参数，跳转时需要加到url里
		targetUrl = "http://" + context;

		if (request.getHeaders().containsKey("PARAMETERSURL")) {
			targetUrl = targetUrl + "?" + ((String[]) request.getHeaders().get("PARAMETERSURL"))[0];
		}

		Area area = (Area) contextMap.get(ServiceContext.LOCATION_MESSAGE);

		String node = null;
		String url ;
		String overflow = "false";
		String realIp ;
		if (area != null) {
			try {
				node = areaNodeManager.getNodeByArea(area, requestHost.getThreshold(), isHead);
			} catch (Exception e) {
				log.error("Got Exception When areaNodeManager.getNodeByArea", e);
				response.handleRequest(targetUrl, response, httpStatus);
			}
		}

		if(map.containsKey(node)){
			  Map<String,String> returnMap =  ((DynamicNewNode)map.get(node)).getIpOrCname(area, requestHost.getThreshold(),isHead,Boolean.FALSE);
			  url = returnMap.get("url");
			  overflow = returnMap.get("overflow");
			  realIp = returnMap.get("realIp");
		}else {
			url = node;
			realIp=node;
		}
		if (null != url) { // 能够查到节点
			if (!IpV4Address.isValidString(url)) {
				url = "video19.ifeng.com";
			}
			targetUrl = "http://" + url + context.substring(context.indexOf("/"));
			if (request.getHeaders().containsKey("PARAMETERSURL")) {
				targetUrl = targetUrl + "?" + ((String[]) request.getHeaders().get("PARAMETERSURL"))[0];
			}

			log.info("Redirect Service: IP: " + remoteIp + " node: "+realIp+" from: "+deviceType+" Redirect To " + targetUrl+" "+area.toString()+" overflow: "+overflow+" gid: "+gid);
			response.handleRequest(targetUrl, response, httpStatus);

		} else { // 未查到节点
			targetUrl = "http://video19.ifeng.com" + context.substring(context.indexOf("/"));
			log.info("Redirect Service: IP: " + remoteIp +" node: "+realIp+" from: "+deviceType+ " Redirect To " + targetUrl+" netname is : null province is : null city is : null overflow: false"+" gid: "+gid);
			response.handleRequest(targetUrl, response, httpStatus);
		}

		return Boolean.TRUE;
	}

	public String getVideo0x(String src) {
		String keyword = src.substring(src.lastIndexOf("/") + 1, src.lastIndexOf("/") + 2);
		Integer num = Integer.parseInt(keyword, 16);
		num = num >> 1;
		String disc = src.substring(0, 7);// disc=video05
		String discNumberDecade = disc.substring(5, 6); // 盘符十位

		String discNumberUnit = disc.substring(6);// 盘符个位
		if (discNumberDecade.equals("0")) {
			Integer discNumber = new Integer(discNumberUnit);
			if (discNumber >= 6) {
				return "video" + (new Integer(num + 10)).toString();
			} else {
				return "video0" + num.toString();
			}
		} else {
			return "video" + (new Integer(num + 10)).toString();
		}
	}

	@Override
	public Object config(ConfigRoot configRoot, Object parent, Element configEle) throws ConfigException {
		this.map =  (Map) configRoot.createChildObject(parent, configEle,
				"dynamic-node-map");
		this.areaNodeManager = (AreaNodeManager) configRoot.createChildObject(this, configEle,
				"area-node-manager", true);

		return this;
	}
}
