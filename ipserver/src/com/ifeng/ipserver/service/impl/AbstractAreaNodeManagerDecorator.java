package com.ifeng.ipserver.service.impl;

import java.util.Map;

import com.ifeng.ipserver.server.handler.plugin.live.LiveEntity;
import org.w3c.dom.Element;

import com.ifeng.common.conf.ConfigException;
import com.ifeng.common.conf.ConfigRoot;
import com.ifeng.common.conf.Configurable;
import com.ifeng.ipserver.bean.Area;
import com.ifeng.ipserver.service.exception.IpServerException;
import com.ifeng.ipserver.service.intf.AreaNodeManager;
/**
 * <title> AbstractAreaNodeManagerDecorator</title>
 * 
 * <pre>
 * 本业务逻辑层采用“修饰模式"构建，目的是为了保持对业务逻辑变化的适应能力。此类为修饰器<br>
 * </pre>
 * 
 * Copyright © 2012 Phoenix New Media Limited All Rights Reserved.
 * 
 * @author <a href="mailto:chenyong@ifeng.com">banban</a>
 * @author <a href="mailto:jinmy@ifeng.com">Jin Mingyan</a>
 */
public abstract class AbstractAreaNodeManagerDecorator implements AreaNodeManager, Configurable{
	private AreaNodeManager areaNodeManager; 
	@Override
	public Object config(ConfigRoot configRoot, Object parent, Element configEle)
			throws ConfigException {
		this.areaNodeManager = (AreaNodeManager) configRoot.createChildObject(this, configEle,
				"area-node-manager", true);
		return this;
	}
	@Override
	public String getNodeByArea(Area area,int threshold,boolean isHead) throws  IpServerException{
		return this.areaNodeManager.getNodeByArea(area,threshold,isHead);
	}

	@Override
	public Map getAreaToNodeMap() {
		return this.areaNodeManager.getAreaToNodeMap();
	}

	@Override
	public void setAreaToNodeMap(Map areaToNodeMap) {
		this.areaNodeManager.setAreaToNodeMap(areaToNodeMap);
	}
}
