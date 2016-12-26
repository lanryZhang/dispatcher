
package com.ifeng.ipserver.service.impl;

import java.util.Map;

import org.w3c.dom.Element;

import com.ifeng.common.conf.ConfigException;
import com.ifeng.common.conf.ConfigRoot;
import com.ifeng.common.misc.Logger;
import com.ifeng.ipserver.bean.Area;
import com.ifeng.ipserver.service.exception.IpServerException;
import com.ifeng.ipserver.service.impl.node.Live3GNode;


/**
 *<title>Live3GAreaNodeManagerImpl<title>
 *<pre>
 *</pre>
 *
 * @author <a href="mailto:chengyong@ifeng.com">banban</a>
 *
 *  Copyright © 2014 Phoenix New Media Limited All Rights Reserved.
 */
public class Live3GAreaNodeManagerImpl extends AbstractAreaNodeManagerDecorator {
	private static Logger log = Logger.getLogger(Live3GAreaNodeManagerImpl.class);
	private Map live3gMap;
	public String getNodeByArea(Area area) throws  IpServerException{
		String node = null;
		Live3GNode livenode = null;		
		if(null!=area){
			String netName = area.getNetName(); 
			livenode = (Live3GNode) live3gMap.get(netName);
			if(null==livenode){
				livenode = (Live3GNode) live3gMap.get(area.getProvince()+netName);
			}
		}
		if(null==livenode){
			livenode = (Live3GNode) live3gMap.get("other");
		}
	/*	node = livenode.getNodeIp(Boolean.FALSE);*/
		return node;
	}


	@Override
	public Object config(ConfigRoot configRoot, Object parent, Element configEle)
			throws ConfigException {
		super.config(configRoot, parent, configEle);
		this.live3gMap =  (Map) configRoot.createChildObject(this, configEle,
				"live-node-map", true);
		return this;
	}

}
