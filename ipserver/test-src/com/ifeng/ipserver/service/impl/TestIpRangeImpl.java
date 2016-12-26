package com.ifeng.ipserver.service.impl;

import com.ifeng.common.misc.IpV4Address;
import com.ifeng.common.misc.RangeSet;
import com.ifeng.common.misc.RangeSet.Range;
import com.ifeng.ipserver.bean.Area;
import com.ifeng.ipserver.service.exception.IpServerException;

import junit.framework.TestCase;

public class TestIpRangeImpl extends TestCase{
	public void testGetAreaByIp(){
		IpRangeImpl impl = new IpRangeImpl();
		RangeSet rangeSet = new RangeSet();
		Area area1 = new Area();
		area1.setCity("广州市");
		area1.setProvince("广东省");
		area1.setNetName("中国电信");
		rangeSet.add(new Range(new IpV4Address("1.1.1.1"),new IpV4Address("1.1.1.254"),area1));
		Area area2 = new Area();
		area2.setCity("苏州市");
		area2.setProvince("江苏省");
		area2.setNetName("中国联通");
		rangeSet.add(new Range(new IpV4Address("1.1.2.1"),new IpV4Address("1.1.2.254"),area2));
		impl.setIpRangeSet(rangeSet);
		try {
			assertEquals(area1,impl.getAreaByIp(new IpV4Address("1.1.1.3")));
		} catch (IpServerException e) {
			assertFalse(true);
		}
		try {
			assertEquals(area2,impl.getAreaByIp(new IpV4Address("1.1.2.3")));
		} catch (IpServerException e) {
			assertFalse(true);
		}
		try {
			assertNull(impl.getAreaByIp(new IpV4Address("1.1.3.3")));
		} catch (IpServerException e) {
			assertFalse(true);
		}
		
		
	}
}
