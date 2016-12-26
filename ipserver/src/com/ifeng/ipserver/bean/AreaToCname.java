package com.ifeng.ipserver.bean;

import com.ifeng.ipserver.bean.print.Printable;

/**
 * <title> AreaToCname</title>
 * 
 * <pre>
 * ipserver需要从iNms数据库中得到配置数据，配置数据如下：<br>
 * 		1. 地址段【开始地址 - 结束地址】 --> 区域【运营商,省,市】
 * 		2. 区域【运营商,省,市】 + 频道【对应一个二级域名，如video01.ifeng.com】 --> cdn节点<br>
 * 其中:
 *    配置数据2，用此类给予展示，未包含频道数据，原因是打印以后的文件名称将包含频道数据。一个文件中为一个AreaToCname列表，即一个频道对应的
 *    区域 --> cname信息， cName即对应的CDN地址，其中商业CDN地址不在ipserver的关注访问内，因为ipserver的302方式不适用于所有的商业CDN
 *    ,自建CDN Cname的格式为一个ip地址。
 * </pre>
 * 
 * Copyright © 2012 Phoenix New Media Limited All Rights Reserved.
 * 
 * @author <a href="mailto:qilp@ifeng.com">Qi Lupeng</a>
 * @author <a href="mailto:jinmy@ifeng.com">Jin Mingyan</a>
 */
public class AreaToCname implements Printable {
	private String netName;
	private String province;
	private String city;
	private String ipOrCname;

	public String getNetName() {
		return netName;
	}

	public void setNetName(String netName) {
		this.netName = netName;
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

	public String getIpOrCname() {
		return ipOrCname;
	}

	public void setIpOrCname(String ipOrCname) {
		this.ipOrCname = ipOrCname;
	}

	@Override
	public boolean equals(Object o) {
		if (o == this)
			return true;
		if (!(o instanceof AreaToCname))
			return false;
		AreaToCname o1 = (AreaToCname) o;
		return o1.netName.equals(this.netName)
				&& o1.province.equals(this.province)
				&& o1.city.equals(this.city)
				&& o1.ipOrCname.equals(this.ipOrCname);
	}

	@Override
	public String getPrintString(String separator) {
		StringBuffer sb = new StringBuffer();
		sb.append(this.getNetName());
		sb.append(separator);
		sb.append(this.getProvince());
		sb.append(separator);
		sb.append(this.getCity());
		sb.append(separator);
		sb.append(this.getIpOrCname());
		return sb.toString();
	}

}
