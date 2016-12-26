package com.ifeng.ipserver.service.impl;


import java.util.List;

import com.ifeng.common.misc.IpAddress;
import com.ifeng.common.misc.Logger;
import com.ifeng.common.misc.RangeSet;
import com.ifeng.common.misc.RangeSet.Range;
import com.ifeng.ipserver.bean.Area;
import com.ifeng.ipserver.service.exception.IpServerException;
import com.ifeng.ipserver.service.intf.IpRangeManager;
/**
 * <title> IpRangeImpl</title>
 * 
 * <pre>
 * ip范围查询接口的实现类，实现了根据ip地址得到对应的区域的能力。<br>
 * </pre>
 * 
 * Copyright © 2012 Phoenix New Media Limited All Rights Reserved.
 * 
 * @author <a href="mailto:yudf@ifeng.com">Yu Dengfeng</a>
 * @author <a href="mailto:jinmy@ifeng.com">Jin Mingyan</a>
 */
public class IpRangeImpl implements IpRangeManager{
	//查询以来的平衡二叉树，具体的构造方式参见com.ifeng.common.misc.RangeSet
	private RangeSet ipRangeSet;
	/* (non-Javadoc)
	 * @see com.ifeng.ipserver.service.intf.IpRangeManager#getIpRangeSet()
	 */
	@Override
	public RangeSet getIpRangeSet() {
		return ipRangeSet;
	}
	/* (non-Javadoc)
	 * @see com.ifeng.ipserver.service.intf.IpRangeManager#setIpRangeSet(com.ifeng.common.misc.RangeSet)
	 */
	@Override
	public void setIpRangeSet(RangeSet ipRangeSet) {
		this.ipRangeSet = ipRangeSet;
	}
	private static final Logger log = Logger.getLogger(IpRangeImpl.class);
	/* (non-Javadoc)
	 * @see com.ifeng.ipserver.service.intf.IpRangeManager#getAreaByIp(com.ifeng.common.misc.IpAddress)
	 */
	@Override
	public Area getAreaByIp(IpAddress ip) throws IpServerException {
		List areaList = ipRangeSet.inRanges(ip);
		if(areaList.size()==0){
			return null;
		}else if(areaList.size()==1){
			return ((Area)((Range)areaList.get(0)).getParam());
		}else{
			log.warn("Too many area was found for "+ip+" will use the first one ");
			return ((Area)((Range)areaList.get(0)).getParam());
		}
	}
}
