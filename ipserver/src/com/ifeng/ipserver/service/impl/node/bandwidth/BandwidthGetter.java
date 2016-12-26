package com.ifeng.ipserver.service.impl.node.bandwidth;
/**
 * <title> BandwidthGetter</title>
 * 
 * <pre>
 * 	     得到对应cdn节点的带宽接口。
 * 	  带宽的单位统一为kB/s
 * <br>
 * </pre>
 * Copyright © 2012 Phoenix New Media Limited All Rights Reserved.
 * 
 * @author <a href="mailto:yudf@ifeng.com">Yu Dengfeng</a>
 * @author <a href="mailto:jinmy@ifeng.com">Jin Mingyan</a>
 */
public interface BandwidthGetter {
	long getBandwidth();
}
