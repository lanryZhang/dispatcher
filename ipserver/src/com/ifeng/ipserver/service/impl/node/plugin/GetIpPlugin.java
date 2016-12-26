package com.ifeng.ipserver.service.impl.node.plugin;

import java.util.Map;

import org.w3c.dom.Element;

import com.ifeng.common.conf.ConfigException;
import com.ifeng.common.conf.ConfigRoot;
import com.ifeng.common.conf.Configurable;
import com.ifeng.common.plugin.core.abst.AbstLogicPlugin;

/**

  @author :chenyong
  @version 1.0
  @date 2012-10-30
 */

public class GetIpPlugin extends AbstLogicPlugin implements Configurable {
	
	//最小控制百分比
		private int minPercent;
		//最大控制百分比
		private int maxPercent;
		//溢出百分比
		private String ip;

	@Override
	public Object execute(Object o) {
		Map context = (Map)o;
		int ranPercent = (Integer) context.get("random-value");
		if(ranPercent>=minPercent && ranPercent<maxPercent){
			context.put("return-ip", ip);
			return Boolean.TRUE;
		}
		return Boolean.FALSE;
	}

	@Override
	public Object config(ConfigRoot configRoot, Object parent, Element configEle)
			throws ConfigException {
		this.minPercent =  (Integer) configRoot.createChildObject(this, configEle,
				"min-percent", true);
		this.maxPercent =  (Integer) configRoot.createChildObject(this, configEle,
				"max-percent", true);
		this.ip =   (String) configRoot.createChildObject(this, configEle,
				"ip", true);
		return this;
	}

}
