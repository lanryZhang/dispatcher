package com.ifeng.ipserver.service.impl.node.bandwidth;

import java.util.Map;
import org.w3c.dom.Element;
import com.ifeng.common.conf.ConfigException;
import com.ifeng.common.conf.ConfigRoot;
import com.ifeng.common.conf.Configurable;
import com.ifeng.common.misc.Logger;

/**

  @author :chenyong
  @version 1.0
  @date 2013-3-5
 */

public class NewBandwidthGetterImpl implements BandwidthGetter,Configurable{
	private static Logger log = Logger.getLogger(NewBandwidthGetterImpl.class);
	private Map bandwidthMap;
	private String key;
	

	@Override
	public long getBandwidth() {
		long bandwidthKb  = 0;	
		String bandString = (String)bandwidthMap.get(key);
		
		long bandwidthBit = 0;
		if(null!=bandString){
			bandwidthBit = Long.parseLong(bandString);
		}
		bandwidthKb  = bandwidthBit/1000;
		log.info("key="+key+" value="+bandwidthKb+" ovalue="+bandwidthBit);
		return bandwidthKb;
	}
	
	@Override
	public Object config(ConfigRoot configRoot, Object parent, Element configEle)
			throws ConfigException {
		this.bandwidthMap = (Map) configRoot.createChildObject(parent,
				configEle, "bandwidth-map");
		this.key = (String) configRoot.createChildObject(parent,
				configEle, "key");
		return this;
	}

	

}
