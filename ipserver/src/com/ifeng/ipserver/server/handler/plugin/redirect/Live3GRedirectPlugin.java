/**
 *
 */
package com.ifeng.ipserver.server.handler.plugin.redirect;

import com.ifeng.common.conf.ConfigException;
import com.ifeng.common.conf.ConfigRoot;
import com.ifeng.common.conf.Configurable;
import com.ifeng.common.misc.Logger;
import com.ifeng.common.plugin.core.abst.AbstLogicPlugin;
import com.ifeng.ipserver.bean.Area;
import com.ifeng.ipserver.server.handler.ServiceContext;
import com.ifeng.ipserver.server.handler.plugin.live.LiveEntity;
import com.ifeng.ipserver.server.message.HttpRequestMessage;
import com.ifeng.ipserver.server.message.HttpResponseMessage;
import com.ifeng.ipserver.service.impl.node.Live3GNode;
import com.ifeng.ipserver.service.intf.AreaNodeManager;
import org.w3c.dom.Element;

import java.util.Map;


/**
 *<title>Live3GRedirectPlugin<title>
 *<pre>
 *</pre>
 *
 * @author <a href="mailto:chengyong@ifeng.com">banban</a>
 *
 *  Copyright © 2014 Phoenix New Media Limited All Rights Reserved.
 */
public class Live3GRedirectPlugin extends AbstLogicPlugin implements Configurable {
	private static Logger log = Logger.getLogger(Live3GRedirectPlugin.class);
	private AreaNodeManager areaNodeManager;
	private Map nodeMap;


	@Override
	public Object execute(Object o) {
		Map contextMap = (Map) o;
		HttpRequestMessage request = (HttpRequestMessage) contextMap
				.get(ServiceContext.REQUEST_MESSAGE);
		HttpResponseMessage response = (HttpResponseMessage) contextMap
				.get(ServiceContext.RESPONSE_MESSAGE);
		String remoteIp = (String) contextMap.get(ServiceContext.REMOTE_IP);
		Area area = (Area) contextMap.get(ServiceContext.LOCATION_MESSAGE);
		String deviceType = (String) contextMap.get(ServiceContext.DEVICE_TYPE);
		String context = request.getContext();
		String chid= (context.substring(context.lastIndexOf("/")+1,context.lastIndexOf("."))+".m3u8").toLowerCase();
		String gid = request.getParameter("gid");
		if(gid.equals("")){
			gid = "null";
		}

		String node = null;
		String targetUrl;
		LiveEntity liveEntity;
		String overflow = "false";
		if(area!=null){
			try {
				node = areaNodeManager.getNodeByArea(area,0,false);
			} catch (Exception e) {
				log.info("live.3gv.ifeng.com remoteIp="+remoteIp+" get node error"+e);
			}
		}
		String realIp=node;
		if(nodeMap.containsKey(node)){
			liveEntity =  ((Live3GNode)nodeMap.get(node)).getNodeIp(chid,Boolean.FALSE);
			node = liveEntity.getUrl();
			overflow = liveEntity.getOverflow();
			realIp = liveEntity.getRealIp();
		}
		if(node==null||node==""){// 未查到节点，走默认节点
			liveEntity=  ((Live3GNode)nodeMap.get("default")).getNodeIp(chid,Boolean.FALSE);
			node = liveEntity.getUrl();
			overflow = liveEntity.getOverflow();
			realIp = liveEntity.getRealIp();
		}
		if(node.indexOf("://")==-1){
			targetUrl = "http://"+node+"/"+context;
		}else{
			targetUrl = node;
		}
		if (request.getHeaders().containsKey("PARAMETERSURL")) {
			targetUrl = targetUrl + "?" + ((String[]) request.getHeaders().get("PARAMETERSURL"))[0];
		}
		if(area!=null){
			log.info("3GRedirect Service: IP: " + remoteIp + " node: "+realIp+" from: "+deviceType+" Redirect To " + targetUrl +" "+area.toString()+" overflow: "+overflow+" gid: "+gid);
		}else{
			log.info("3GRedirect Service: IP: " + remoteIp +" node: "+realIp+" from: "+deviceType+ " Redirect To " + targetUrl+" netname is : null province is : null city is : null overflow: "+overflow+" gid: "+gid);
		}

		response.handleRequest(targetUrl, response, HttpResponseMessage.HTTP_STATUS_MOVED_PERMANENTLY);
		return Boolean.TRUE;
	}

	@Override
	public Object config(ConfigRoot configRoot, Object parent, Element configEle) throws ConfigException {
		this.nodeMap =  (Map) configRoot.createChildObject(parent, configEle,
				"dynamic-node-map");
		this.areaNodeManager = (AreaNodeManager) configRoot.createChildObject(this, configEle,
				"area-node-manager", true);

		return this;
	}
}
