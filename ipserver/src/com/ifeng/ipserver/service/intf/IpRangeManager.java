package com.ifeng.ipserver.service.intf;

import com.ifeng.common.misc.IpAddress;
import com.ifeng.common.misc.RangeSet;
import com.ifeng.ipserver.bean.Area;
import com.ifeng.ipserver.service.exception.IpServerException;

/**
 * <title> IpRangeManager</title>
 * 
 * <pre>
 * ipserver功能定义接口，ips核心的能力就是两个能力：
 *    1 根据ip找到对应的“区域”
 *    2 根据区域找到对应的服务节点
 * 而具体的查找方式，依赖的数据和对应的规则可能是有多种变化的。
 * 定义一个这样的接口够，然后用不同的实现技巧去实现这个接口进而达成业务逻辑变化的能力。
 * 本接口约束的是第一个能力，即根据ip找到对应的哦区域
 * <br>
 * </pre>
 * 
 * Copyright © 2012 Phoenix New Media Limited All Rights Reserved.
 * 
 * @author <a href="mailto:yudf@ifeng.com">Yu Dengfeng</a>
 * @author <a href="mailto:jinmy@ifeng.com">Jin Mingyan</a>
 */
public interface IpRangeManager {
	/**
	 * 根据ip地址得到对应的区域。
	 * 
	 * @param ip地址 为com.ifeng.common.misc.IpAddress类型，具体的实现类为:
	 * 		com.ifeng.common.misc.IpV4Address & com.ifeng.commmon.misc.IpV6Address
	 * 
	 * @return Area 对应的区域 ， 当没有对应的区域时，返回null.
	 * @throws IpServerException 发生异常时，使用此异常抛出相关的信息
	 */
	Area getAreaByIp(IpAddress ip) throws IpServerException;
	/**
	 * 得到查询依赖的“范围集合” 
	 * 
	 * @return RangeSet 参见com.ifeng.common.misc.RangeSet
	 */
	RangeSet getIpRangeSet();
	/**
	 * 设置查询以来的“范围集合”
	 * 
	 * @param ipRangeSet 为com.ifeng.common.misc.RangeSet类型 
	 */
	void setIpRangeSet(RangeSet ipRangeSet);
}
