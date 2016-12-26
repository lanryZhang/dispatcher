package com.ifeng.ipserver.bean.db;

/**
 * <title> ChannelToNode</title>
 * 
 * <pre>
 * ipserver需要从iNms数据库中得到配置数据，配置数据如下：<br>
 * 		1. 地址段【开始地址 - 结束地址】 --> 区域【运营商,省,市】
 * 		2. 区域【运营商,省,市】 + 频道【对应一个二级域名，如video01.ifeng.com】 --> cdn节点<br>
 * 其中:
 *    频道对应的cdn节点信息在此类中给予定义。
 * </pre>
 * 
 * Copyright © 2012 Phoenix New Media Limited All Rights Reserved.
 * 
 * @author <a href="mailto:qilp@ifeng.com">Qi Lupeng</a>
 * @author <a href="mailto:jinmy@ifeng.com">Jin Mingyan</a>
 */
public class ChannelAndNodeToCname {
	private ChannelAndNodeToCnamePK channelAndNodeToCnamePK = new ChannelAndNodeToCnamePK();
	private String type;
	private String ip;
	private String cname;

	@Override
	public boolean equals(Object o) {
		if (o == this)
			return true;
		if (!(o instanceof ChannelAndNodeToCname))
			return false;
		ChannelAndNodeToCname o1 = (ChannelAndNodeToCname) o;
		return o1.channelAndNodeToCnamePK.equals(this.channelAndNodeToCnamePK)
				&& o1.type.equals(this.type) && o1.ip.equals(this.ip)
				&& o1.cname.equals(this.cname);
	}

	@Override
	public int hashCode() {
		return this.channelAndNodeToCnamePK.hashCode() * 61
				+ this.type.hashCode() * 31 + this.ip.hashCode() * 13
				+ this.cname.hashCode();
	}

	public long getChannelId() {
		return channelAndNodeToCnamePK.getChannelId();
	}

	public void setChannelId(long channelId) {
		channelAndNodeToCnamePK.setChannelId(channelId);
	}

	public String getCname() {
		return cname;
	}

	public void setCname(String cname) {
		this.cname = cname;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public long getNodeId() {
		return channelAndNodeToCnamePK.getNodeId();
	}

	public void setNodeId(long nodeId) {
		channelAndNodeToCnamePK.setNodeId(nodeId);
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public ChannelAndNodeToCnamePK getChannelAndNodeToCnamePK() {
		return channelAndNodeToCnamePK;
	}

	public void setChannelAndNodeToCnamePK(
			ChannelAndNodeToCnamePK channelAndNodeToCnamePK) {
		this.channelAndNodeToCnamePK = channelAndNodeToCnamePK;
	}

	public long getChannelId1() {
		return channelAndNodeToCnamePK.getChannelId();
	}

	public void setChannelId1(long channelId1) {
		this.channelAndNodeToCnamePK.setChannelId(channelId1);
	}

	public static class ChannelAndNodeToCnamePK {
		private long channelId;
		private long nodeId;

		@Override
		public boolean equals(Object o) {
			if (o == this)
				return true;
			if (!(o instanceof ChannelAndNodeToCnamePK))
				return false;
			ChannelAndNodeToCnamePK o1 = (ChannelAndNodeToCnamePK) o;
			return o1.channelId == this.channelId && o1.nodeId == this.nodeId;
		}

		@Override
		public int hashCode() {
			return (new Long(this.channelId)).hashCode() * 17
					+ (new Long(this.nodeId)).hashCode();
		}

		public long getChannelId() {
			return channelId;
		}

		public void setChannelId(long channelId) {
			this.channelId = channelId;
		}

		public long getNodeId() {
			return nodeId;
		}

		public void setNodeId(long nodeId) {
			this.nodeId = nodeId;
		}

	}
}
