package com.ifeng.ipserver.service.impl;

import java.util.Map;

import com.ifeng.ipserver.server.handler.plugin.live.LiveEntity;
import com.ifeng.ipserver.service.impl.node.DynamicNewNode;
import org.w3c.dom.Element;

import com.ifeng.common.conf.ConfigException;
import com.ifeng.common.conf.ConfigRoot;
import com.ifeng.ipserver.bean.Area;
import com.ifeng.ipserver.service.exception.IpServerException;
import com.ifeng.ipserver.service.impl.node.DynamicNode;
/**
 * <title> DynamicAreaNodeManagerImpl</title>
 * 
 * <pre>
 * 	    动态区域-->Node管理器，在需要带宽控制的场景使用。
 *   可以使用dynamicNodeMap来配置那些Node是带有带宽控制能力的。
 * <br>
 * </pre>
 * * 配置方式为：
 * 
 * &lt;... type="com.ifeng.ipserver.service.impl。DynamicAreaNodeManagerImpl"&gt;
 * 	 &lt;dynamic-node-map .../&gt;
 * 	 &lt;area-node-manager .../&gt;
 * &lt/...&gt 
 *其中：
 *	dynamic-node-map
 *		类型： Map key:String value:DynamicNode
 *		用途：具有动态带宽控制能力的Node列表。
 *	area-node-manager
 *		类型：AreaNodeManager
 *		用途：内部的AreaNodeManager，代码会先调用这个manager，然后根据结果决定是否采用动态控制策略。
 *
 * Copyright © 2012 Phoenix New Media Limited All Rights Reserved.
 * 
 * @author <a href="mailto:yudf@ifeng.com">Yu Dengfeng</a>
 * @author <a href="mailto:jinmy@ifeng.com">Jin Mingyan</a>
 */
public class DynamicAreaNodeManagerImpl extends AbstractAreaNodeManagerDecorator{
	//动态Node列表key:string value:DynamicNode
	private Map dynamicNodeMap;
	
	/* (non-Javadoc)
	 * @see com.ifeng.ipserver.service.impl.AbstractAreaNodeManagerDecorator#getNodeByArea(com.ifeng.ipserver.bean.Area)
	 */
	@Override
	public String getNodeByArea(Area area,int threshold,boolean isHead) throws  IpServerException{
		String node = super.getNodeByArea(area,threshold,isHead);
		if(dynamicNodeMap.containsKey(node)){
			return ((DynamicNewNode)dynamicNodeMap.get(node)).getIpOrCname(area, threshold,isHead,Boolean.FALSE).get("url");
		}else{
			return node;
		}
	}

	/* (non-Javadoc)
	 * @see com.ifeng.ipserver.service.impl.AbstractAreaNodeManagerDecorator#config(com.ifeng.common.conf.ConfigRoot, java.lang.Object, org.w3c.dom.Element)
	 */
	@Override
	public Object config(ConfigRoot configRoot, Object parent, Element configEle)
			throws ConfigException {
		super.config(configRoot, parent, configEle);
		this.dynamicNodeMap =  (Map) configRoot.createChildObject(this, configEle,
				"dynamic-node-map", true);
		return this;
	}
}
