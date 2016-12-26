package com.ifeng.ipserver.bean.db;

import java.io.Serializable;

/**
 * <title> ChannelGroupToNode(频道 + 群组 --> 节点) </title>
 * 
 * <pre>
 * ipserver需要从iNms数据库中得到配置数据，配置数据如下：<br>
 * 		1. 地址段【开始地址 - 结束地址】 --> 区域【运营商,省,市】
 * 		2. 区域【运营商,省,市】 + 频道【对应一个二级域名，如video01.ifeng.com】 --> cdn节点<br>
 * 其中:
 *    区域信息在iNMS中单独存储，但在与频道映射cdn节点时，实现将区域进行了分组，分组在iNMS中为 Group，分组便于操作人员
 * 完成相关操作。
 *    本类的主要映射目的，即将这个分组(GROUP) + 频道 与 对应的Node关系取出。
 * 
 * </pre>
 * 
 * Copyright © 2012 Phoenix New Media Limited All Rights Reserved.
 * 
 * @author <a href="mailto:qilp@ifeng.com">Qi Lupeng</a>
 * @author <a href="mailto:jinmy@ifeng.com">Jin Mingyan</a>
 */

public class ChannelAndGroupToNode {
	// nodeId（CDN节点信息）
	private long nodeId;
	// 联合主键 主键为GroupId + ChannelId
	private ChannelAndGroupToNodePK channelAndGroupToNodePK = new ChannelAndGroupToNodePK();

	public long getChannelId() {
		return channelAndGroupToNodePK.getChannelId();
	}

	public void setChannelId(long channelId) {
		channelAndGroupToNodePK.setChannelId(channelId);
	}
	
	
	public long getChannelId1() {
		return channelAndGroupToNodePK.getChannelId();
	}

	public void setChannelId1(long channelId1) {
		channelAndGroupToNodePK.setChannelId(channelId1);
	}

	public long getGroupId() {
		return channelAndGroupToNodePK.getGroupId();
	}

	public void setGroupId(long groupId) {
		channelAndGroupToNodePK.setGroupId(groupId);
	}

	public long getNodeId() {
		return nodeId;
	}

	public void setNodeId(long nodeId) {
		this.nodeId = nodeId;
	}

	public ChannelAndGroupToNodePK getChannelAndGroupToNodePK() {
		return channelAndGroupToNodePK;
	}

	public void setChannelAndGroupToNodePK(
			ChannelAndGroupToNodePK channelAndGroupToNodePK) {
		this.channelAndGroupToNodePK = channelAndGroupToNodePK;
	}

	@Override
	public boolean equals(Object o) {
		if (o == this)
			return true;
		if (!(o instanceof ChannelAndGroupToNode))
			return false;
		ChannelAndGroupToNode o1 = (ChannelAndGroupToNode) o;
		return o1.nodeId == this.nodeId
				&& o1.channelAndGroupToNodePK
						.equals(this.channelAndGroupToNodePK);
	}

	public static class ChannelAndGroupToNodePK implements Serializable {

		private static final long serialVersionUID = -6784685973661744775L;

		private long channelId;
		private long groupId;

		public long getChannelId() {
			return channelId;
		}

		public void setChannelId(long channelId) {
			this.channelId = channelId;
		}

		public long getGroupId() {
			return groupId;
		}

		public void setGroupId(long groupId) {
			this.groupId = groupId;
		}

		@Override
		public boolean equals(Object o) {
			if (o == this)
				return true;
			if (!(o instanceof ChannelAndGroupToNodePK))
				return false;
			ChannelAndGroupToNodePK o1 = (ChannelAndGroupToNodePK) o;
			return o1.channelId == this.channelId && o1.groupId == this.groupId;
		}
		@Override
		public int hashCode() {
			return (new Long(this.channelId)).hashCode() * 17
					+ (new Long(this.groupId)).hashCode();
		}
	}
}
