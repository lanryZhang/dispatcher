package com.ifeng.ipserver.service.impl;

import java.util.Map;

import com.ifeng.ipserver.bean.Area;
import com.ifeng.ipserver.service.exception.IpServerException;
import com.ifeng.ipserver.service.intf.AreaNodeManager;
/**
 * <title> AreaNodeManagerImpl</title>
 * 
 * <pre>
 * ipserver功能定义接口，ips核心的能力就是两个能力：
 *    1 根据ip找到对应的“区域”
 *    2 根据区域找到对应的服务节点
 * 本类实现的是第二个能力，即根据区域找到对应的服务节点。
 * 是最普通的查找能力，即根据预先的映射关系（表现为一个map）来查找对应关系
 * <br>
 * </pre>
 * 
 * Copyright © 2012 Phoenix New Media Limited All Rights Reserved.
 * 
 * @author <a href="mailto:yudf@ifeng.com">Yu Dengfeng</a>
 * @author <a href="mailto:jinmy@ifeng.com">Jin Mingyan</a>
 */
public class AreaNodeManagerImpl implements AreaNodeManager{
	private Map areaToNodeMap;

	@Override
	public Map getAreaToNodeMap() {
		return areaToNodeMap;
	}

	@Override
	public void setAreaToNodeMap(Map areaToNodeMap) {
		this.areaToNodeMap = areaToNodeMap;
	}

	@Override
	public String getNodeByArea(Area area,int threshold,boolean isHead) throws IpServerException {
		return (String)areaToNodeMap.get(area);
	}


}
