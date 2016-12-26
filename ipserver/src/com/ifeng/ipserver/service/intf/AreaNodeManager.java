package com.ifeng.ipserver.service.intf;

import java.util.Map;

import com.ifeng.ipserver.bean.Area;
import com.ifeng.ipserver.server.handler.plugin.live.LiveEntity;
import com.ifeng.ipserver.service.exception.IpServerException;

/**
 * <title> AreaNodeManager</title>
 * 
 * <pre>
 * ipserver功能定义接口，ips核心的能力就是两个能力：
 *    1 根据ip找到对应的“区域”
 *    2 根据区域找到对应的服务节点
 * 而具体的查找方式，依赖的数据和对应的规则可能是有多种变化的。
 * 定义一个这样的接口够，然后用不同的实现技巧去实现这个接口进而达成业务逻辑变化的能力。
 * 本接口实现的是第二个能力，即根据区域找到对应的服务节点
 * <br>
 * </pre>
 * 
 * Copyright © 2012 Phoenix New Media Limited All Rights Reserved.
 * 
 * @author <a href="mailto:yudf@ifeng.com">Yu Dengfeng</a>
 * @author <a href="mailto:jinmy@ifeng.com">Jin Mingyan</a>
 */
public interface AreaNodeManager {
	/**
	 * 查询一个区域对应的CDN节点(Node)
	 * 
	 * @param area 区域
	 * @return String 为CDN节点对应的字符串
	 * 
	 * @throws IpServerException 当发生异常时用此异常约束信息
	 */
	String getNodeByArea(Area area, int threshold, boolean isHead) throws  IpServerException;
	/**
	 * 得到查询对应的Map
	 * 
	 * @return Map
	 */
	Map getAreaToNodeMap();
	/**
	 * 设置查询对应的map
	 * 
	 * @param areaToNodeMap 
	 */
	void setAreaToNodeMap(Map areaToNodeMap);


}
