package com.ifeng.ipserver.service.impl.node.plugin;

import java.util.Map;

import org.w3c.dom.Element;

import com.ifeng.common.conf.ConfigException;
import com.ifeng.common.conf.ConfigRoot;
import com.ifeng.common.conf.Configurable;
import com.ifeng.common.plugin.core.abst.AbstLogicPlugin;

public class RateLimitDependencyControllerPlugin extends AbstLogicPlugin implements Configurable{

	private int minIncreateRate;
	private int maxIncreateRate;
	private int minRequestNum;
	private int virtualNowpercent;

	@Override
	public Object execute(Object arg0) {
		Map map = (Map)arg0;
		int rate = (Integer) map.get("liveIncreaseRate");
		int requestNum = (Integer) map.get("requestNum");
		
		if (rate > minIncreateRate && rate <=maxIncreateRate
				&& requestNum >= minRequestNum){
			map.put("virtualNowpercent", virtualNowpercent);
			return true;
		}
		return false;
	}

	@Override
	public Object config(ConfigRoot root, Object parent, Element el) throws ConfigException {
		this.minIncreateRate =  (Integer) root.createChildObject(parent,
				el, "min-increate-rate");
		this.maxIncreateRate =  (Integer) root.createChildObject(parent,
				el, "max-increate-rate");
		this.minRequestNum =  (Integer) root.createChildObject(parent,
				el, "min-request-num");
		this.virtualNowpercent =  (Integer) root.createChildObject(parent,
				el, "virtual-nowpercent");
		return this;
	}

	public int getMinIncreateRate() {
		return minIncreateRate;
	}

	public int getMaxIncreateRate() {
		return maxIncreateRate;
	}

	public int getMinRequestNum() {
		return minRequestNum;
	}

	public int getVirtualNowpercent() {
		return virtualNowpercent;
	}

	public void setMinIncreateRate(int minIncreateRate) {
		this.minIncreateRate = minIncreateRate;
	}

	public void setMaxIncreateRate(int maxIncreateRate) {
		this.maxIncreateRate = maxIncreateRate;
	}

	public void setMinRequestNum(int minRequestNum) {
		this.minRequestNum = minRequestNum;
	}

	public void setVirtualNowpercent(int virtualNowpercent) {
		this.virtualNowpercent = virtualNowpercent;
	}
}
