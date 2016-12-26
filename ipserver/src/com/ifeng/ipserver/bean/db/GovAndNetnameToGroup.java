package com.ifeng.ipserver.bean.db;

import java.io.Serializable;

import org.apache.commons.lang3.StringUtils;
/**
 * <title> GovAndNetnameToGroup </title>
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
public class GovAndNetnameToGroup {

	private GroupGovNetnamePK groupGovNetnamePK = new GroupGovNetnamePK();
	private long groupId;

	public long getGovId() {
		return groupGovNetnamePK.getGovId();
	}

	public void setGovId(long govId) {
		this.groupGovNetnamePK.setGovId(govId);
	}

	public long getGroupId() {
		return groupId;
	}

	public void setGroupId(long groupId) {
		this.groupId = groupId;
	}

	public String getNetname() {
		return groupGovNetnamePK.getNetname();
	}

	public void setNetname(String netname) {
		this.groupGovNetnamePK.setNetname(netname);
	}

	public GroupGovNetnamePK getGroupGovNetnamePK() {
		return groupGovNetnamePK;
	}

	public void setGroupGovNetnamePK(GroupGovNetnamePK groupGovNetnamePK) {
		this.groupGovNetnamePK = groupGovNetnamePK;
	}

	@Override
	public boolean equals(Object o) {
		if (o == this)
			return true;
		if (!(o instanceof GovAndNetnameToGroup))
			return false;
		GovAndNetnameToGroup o1 = (GovAndNetnameToGroup) o;
		return o1.groupId == this.groupId
				&& o1.groupGovNetnamePK.equals(this.groupGovNetnamePK);
	}

	public static class GroupGovNetnamePK implements Serializable {
		private static final long serialVersionUID = -231308049961887630L;
		private long govId;
		private String netname;

		public long getGovId() {
			return govId;
		}

		public void setGovId(long govId) {
			this.govId = govId;
		}

		public String getNetname() {
			return netname;
		}

		public void setNetname(String netname) {
			this.netname = netname;
		}

		@Override
		public boolean equals(Object o) {
			if (o == this)
				return true;
			if (!(o instanceof GroupGovNetnamePK))
				return false;
			GroupGovNetnamePK o1 = (GroupGovNetnamePK) o;
			return o1.govId == this.govId
					&& StringUtils.equals(o1.netname, this.netname);
		}
	}
}
