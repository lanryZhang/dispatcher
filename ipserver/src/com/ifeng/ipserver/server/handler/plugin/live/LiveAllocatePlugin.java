package com.ifeng.ipserver.server.handler.plugin.live;

import com.ifeng.common.conf.ConfigException;
import com.ifeng.common.conf.ConfigRoot;
import com.ifeng.common.conf.Configurable;
import com.ifeng.common.misc.Logger;
import com.ifeng.common.plugin.core.abst.AbstLogicPlugin;
import com.ifeng.ipserver.bean.Area;
import com.ifeng.ipserver.server.handler.ServiceContext;
import com.ifeng.ipserver.server.message.HttpRequestMessage;
import com.ifeng.ipserver.server.message.HttpResponseMessage;
import com.ifeng.ipserver.service.impl.node.DynamicLiveNode;
import com.ifeng.ipserver.service.intf.AreaNodeManager;
import com.sdicons.json.mapper.JSONMapper;
import com.sdicons.json.mapper.MapperException;
import org.w3c.dom.Element;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * <title> LiveAllocatePlugin</title>
 * 
 * <pre>
 * 	    直播请求处理插件
 * <br>
 * </pre>
 * * 配置方式为：
 * 
 * &lt;... type="com.ifeng.ipserver.server.handler.plugin。live.LiveAllocatePlugin"&gt;
 * 	 &lt;config-file-path .../&gt;
 *   &lt;period .../&gt;
 * &lt/...&gt 
 *其中：
 *	config-file-path
 *		类型：String 
 *		用途：直播配置文件对应的路径
 *  period
 *  	类型：long
 *  	用途：配置文件修改自动更新的检测时间间隔
 *
 * Copyright © 2012 Phoenix New Media Limited All Rights Reserved.
 * TODO 这个类写的不咋地，有空再优化吧 -- jinmy
 * @author <a href="mailto:jl@ifeng.com">Jin Lin</a>
 * @author <a href="mailto:jinmy@ifeng.com">Jin Mingyan</a>
 */
public class LiveAllocatePlugin extends AbstLogicPlugin implements Configurable {

	private long lastModifyTime = 0;
	private Map map;
	private AreaNodeManager areaNodeManager;

	private static Logger log = Logger.getLogger(LiveAllocatePlugin.class);


	@Override
	public Object execute(Object o) {
		Map contextMap = (Map) o;
		HttpRequestMessage request = (HttpRequestMessage) contextMap
				.get(ServiceContext.REQUEST_MESSAGE);
		HttpResponseMessage response = (HttpResponseMessage) contextMap
				.get(ServiceContext.RESPONSE_MESSAGE);
		Area area = (Area) contextMap.get(ServiceContext.LOCATION_MESSAGE);
		String channelCode = request.getParameter("cid");
		String ip = (String) contextMap.get(ServiceContext.REMOTE_IP);
		String deviceType = (String) contextMap.get(ServiceContext.DEVICE_TYPE);
		String gid = request.getParameter("gid");
		if(gid.equals("")){
			gid = "null";
		}

		String node = null;
		LiveEntity entity = null;
		String netName = null;
		if (area != null) {
			try {
				node = areaNodeManager.getNodeByArea(area,0,false);
				netName = area.getNetName();
			} catch (Exception e) {
				log.error("Got Exception When areaNodeManager.getNodeByArea ", e);
			}
		}
		if(null!=node){//存在node
			if(map.containsKey(node)){//配置中存在
				try{
					entity =  ((DynamicLiveNode)map.get(node)).getAddr(channelCode, Boolean.FALSE);
				}catch(Exception e){
					log.error("get addr error: " + e);
				}
			}else{//配置中不存在
				try{
					entity =  ((DynamicLiveNode)map.get("default")).getSelf(node);//url：本身节点，cdnid：ifengP2P
				}catch(Exception e){
					log.error("get addr self error: " + e);
				}
			}
		}else{//不存在node
			try{
				entity =  ((DynamicLiveNode)map.get("default")).getAddr(channelCode, Boolean.FALSE);//走默认节点，电信
			}catch(Exception e){
				log.error("get default addr error: " + e);
			}
		}
		if(entity.getCndid().equals("overflow")){
			entity.setUrl("overflow:"+entity.getRealIp().substring(7,entity.getRealIp().lastIndexOf(":")));
		}
		entity.setCid(channelCode);
		return this.handleResponse(netName, request, response, ip, entity,node,area,deviceType,gid);
	}

	private boolean handleResponse(String sp, HttpRequestMessage request,
			HttpResponseMessage response, String ip,
			LiveEntity entity,String node,Area area,String deviceType,String gid) {
		/*if (null == entity) {
			// 没查到频道信息
			log.info("LiveAllocation Service: ip= " + ip
					+ " node: null"
					+ " deviceType: "+deviceType
					+ " channel code= " + request.getParameter("cid")
					+ " return: null "
					+ " cdn: null"
					+ " netname is : null province is : null city is : null");
			response.handleRequest("Live Allocation sp info Not Found",
					response, HttpResponseMessage.HTTP_STATUS_NOT_FOUND);
			return false;
		} else {*/
			Map<String, String> result = new HashMap<String, String>();
			result.put("cid", entity.getCid());
			result.put("link", entity.getUrl());
			result.put("cdnid", entity.getCndid());
			result.put("netname", sp);

			JSONMapper mapper = new JSONMapper();
			String jsonResult = "";
			try {
				jsonResult = JSONMapper.toJSON(result).render(false);
			} catch (MapperException e) {
				log.error("Error When Convert to Json", e);
			}

			response.setLastModified(new SimpleDateFormat(
					"EEE, dd MMM yyyy HH:mm:ss zzz").format(new Date()));

			Calendar tenYearLater = Calendar.getInstance();
			tenYearLater.add(Calendar.YEAR, 10);// 10年以后
			response.setExpires(new SimpleDateFormat(
					"EEE, dd MMM yyyy HH:mm:ss zzz").format(tenYearLater
					.getTime()));

			response.setCacheControl("max-age=315360000");
			response.handleRequest(jsonResult, response,
					HttpResponseMessage.HTTP_STATUS_SUCCESS);

			String net="netname is : null province is : null city is : null";
            if(area!=null){
				net = area.toString();
			}
		    String nodeRecode=entity.getRealIp();
		    if(entity.getRealIp().indexOf("http://")>-1){
				nodeRecode = entity.getRealIp().substring(7,entity.getRealIp().lastIndexOf(":"));
			}
			log.info("LiveAllocation Service: ip= " + ip
					+ " node: "+nodeRecode
					+ " deviceType: "+deviceType
					+ " channel code= " + entity.getCid()
					+ " return: " +entity.getUrl()
					+ " cdn: "+entity.getCndid()
					+" "+net
					+" overflow: "+entity.getOverflow()
					+" gid: "+gid);
			return true;
		/*}*/
	}


	@Override
	public Object config(ConfigRoot configRoot, Object parent, Element configEle)
			throws ConfigException {
		this.map =  (Map) configRoot.createChildObject(parent, configEle,
				"dynamic-node-map");
		this.areaNodeManager = (AreaNodeManager) configRoot.createChildObject(this, configEle,
				"area-node-manager", true);

		return this;
	}
}
