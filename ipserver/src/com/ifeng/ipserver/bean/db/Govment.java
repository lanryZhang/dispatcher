package com.ifeng.ipserver.bean.db;

/**
 * <title> Govment(区域，个人决定叫domain更合适，但是在iNMS中叫做Gov 所以，暂时和iNMS的命名一致) </title>
 * 
 * <pre>
 * ipserver需要从iNms数据库中得到配置数据，配置数据如下：<br>
 * 		1. 地址段【开始地址 - 结束地址】 --> 区域【运营商,省,市】
 * 		2. 区域【运营商,省,市】 + 频道【对应一个二级域名，如video01.ifeng.com】 --> cdn节点<br>
 * 其中:
 *    区域信息中的省市在此类中定义，用于和运营商一起映射Group使用，具体的映射关系在其他类中定义。
 * </pre>
 * 
 * Copyright © 2012 Phoenix New Media Limited All Rights Reserved.
 * 
 * @author <a href="mailto:qilp@ifeng.com">Qi Lupeng</a>
 * @author <a href="mailto:jinmy@ifeng.com">Jin Mingyan</a>
 */
public class Govment {

	// ID 信息，唯一索引
	private long id;

	// 区域的名称
	private String name;

	// 根区域的id
	private long rootId;

	// 上级区域的id
	private long parentId;

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public long getRootId() {
		return rootId;
	}

	public void setRootId(long rootId) {
		this.rootId = rootId;
	}

	public long getParentId() {
		return parentId;
	}

	public void setParentId(long parentId) {
		this.parentId = parentId;
	}

	@Override
	public boolean equals(Object o) {
		if (o == this)
			return true;
		if (!(o instanceof Govment))
			return false;
		Govment o1 = (Govment) o;
		return o1.id == this.id && o1.rootId == this.rootId
				&& o1.parentId == this.parentId
				&& o1.name.equals(this.parentId);
	}

}
