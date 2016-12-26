package com.ifeng.ipserver.bean.db;

import com.ifeng.ipserver.bean.print.Printable;

/**
 * <title> IpBase</title>
 * 
 * <pre>
 * ipserver需要从iNms数据库中得到配置数据，配置数据如下：<br>
 * 		1. 地址段【开始地址 - 结束地址】 --> 区域【运营商,省,市】
 * 		2. 区域【运营商,省,市】 + 频道【对应一个二级域名，如video01.ifeng.com】 --> cdn节点<br>
 * 其中:
 *    地址段到对应的运营商、省、市信息在此类中给予定义。
 * </pre>
 * 
 * Copyright © 2012 Phoenix New Media Limited All Rights Reserved.
 * 
 * @author <a href="mailto:qilp@ifeng.com">Qi Lupeng</a>
 * @author <a href="mailto:jinmy@ifeng.com">Jin Mingyan</a>
 */
public class IpBase implements Printable {
	// 唯一索引，inms数据库中叫做seq
	private long seq;
	// 开始地址
	private String startIp;
	// 结束地址
	private String endIp;
	// 运营商信息
	private String netname;
	// 省份
	private String province;
	// 城市
	private String city;

	public long getSeq() {
		return seq;
	}

	public void setSeq(long seq) {
		this.seq = seq;
	}

	public String getStartIp() {
		return startIp;
	}

	public void setStartIp(String startIp) {
		this.startIp = startIp;
	}

	public String getEndIp() {
		return endIp;
	}

	public void setEndIp(String endIp) {
		this.endIp = endIp;
	}

	public String getNetname() {
		return netname;
	}

	public void setNetname(String netname) {
		this.netname = netname;
	}

	public String getProvince() {
		return province;
	}

	public void setProvince(String province) {
		this.province = province;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	@Override
	public boolean equals(Object o) {
		if (o == this)
			return true;
		if (!(o instanceof IpBase))
			return false;
		IpBase o1 = (IpBase) o;
		return o1.seq == this.seq && o1.startIp.equals(this.startIp)
				&& o1.endIp.equals(this.endIp)
				&& o1.netname.equals(this.netname)
				&& o1.province.equals(this.province)
				&& o1.city.equals(this.city);
	}

	@Override
	public String getPrintString(String separator) {
		StringBuffer sb = new StringBuffer();
		sb.append(this.getStartIp());
		sb.append(separator);
		sb.append(this.getEndIp());
		sb.append(separator);
		sb.append(this.getNetname());
		sb.append(separator);
		sb.append(this.getProvince());
		sb.append(separator);
		sb.append(this.getCity());
		return sb.toString();
	}
}
