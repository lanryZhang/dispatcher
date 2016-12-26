package com.ifeng.ipserver.service.impl.rate;

import org.w3c.dom.Element;

import com.ifeng.common.conf.ConfigException;
import com.ifeng.common.conf.ConfigRoot;
import com.ifeng.common.conf.Configurable;

public class LiveRateGetter implements Configurable{
	private String key;
	
	public int getLiveIncreaseRate(){
		if (LiveRateCaculator.getRateMap().containsKey(key))
			return LiveRateCaculator.getRateMap().get(key);
		return 0;
	}
	
	public int getLiveRequestNum(){
		if (LiveRateCaculator.getRateMap().containsKey(key))
			return LiveRateCaculator.getStatMap().get(key);
		return 0;
	}

	@Override
	public Object config(ConfigRoot root, Object parent, Element el) throws ConfigException {
		this.key = (String) root.createChildObject(parent,
				el, "chid");
		return this;
	}
}
