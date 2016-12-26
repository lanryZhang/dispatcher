package com.ifeng.ipserver.service.impl.node.plugin;

import java.util.Map;
import org.w3c.dom.Element;

import com.ifeng.common.conf.ConfigException;
import com.ifeng.common.conf.ConfigRoot;
import com.ifeng.common.conf.Configurable;
import com.ifeng.common.plugin.core.abst.AbstLogicPlugin;
/**
 * <title> RateContrlerPlugin </title>
 * 
 * <pre>
 * 	    根据速率百分比情况分配一定百分比的请求
 * <br>
 * </pre>
 * * 配置方式为：
 * 
 * &lt;... type="com.ifeng.ipserver.service.impl.node.plugin。RateContrlerPlugin"&gt;
 * 	 &lt;min-percent .../&gt;
 *   &lt;max-percent .../&gt;
 *   &lt;outof-percent .../&gt;
 * &lt/...&gt 
 *其中：
 *	min-percent
 *		类型： int
 *		用途：启动此速率控制的最小百分比
 *  max-percent
 *		类型： int
 *		用途：启动此速率控制的最大百分比
 *  outof-percent
 *		类型： int
 *		用途：符合最小和最大区间以后，溢出的百分比，即有多少百分比的请求将被溢出处理 对应的逻辑为返回TRUE
 *  
 *
 * Copyright © 2012 Phoenix New Media Limited All Rights Reserved.
 * 
 * @author <a href="mailto:chenyong@ifeng.com">banban</a>
 * @author <a href="mailto:jinmy@ifeng.com">Jin Mingyan</a>
 */
public class RateContrlerPlugin extends AbstLogicPlugin implements Configurable{
	//最小控制百分比
	private int minPercent;
	//最大控制百分比
	private int maxPercent;
	//溢出百分比
	private int outOfPercent;
	
	@Override
	public Object execute(Object o) {
		Map context = (Map)o;
		int nowRatePercent = (Integer)context.get("nowRatePercent");
		if((nowRatePercent>=minPercent)&&(nowRatePercent<maxPercent)){
			if(outOfPercent>=100){
				return Boolean.TRUE;
			}
			if((Math.random()*100)<=outOfPercent){
				return Boolean.TRUE;
			}
		}
		return Boolean.FALSE;
	}

	/* (non-Javadoc)
	 * @see com.ifeng.common.conf.Configurable#config(com.ifeng.common.conf.ConfigRoot, java.lang.Object, org.w3c.dom.Element)
	 */
	@Override
	public Object config(ConfigRoot configRoot, Object parent, Element configEle)
			throws ConfigException {
		this.minPercent =  (Integer) configRoot.createChildObject(this, configEle,
				"min-percent", true);
		this.maxPercent =  (Integer) configRoot.createChildObject(this, configEle,
				"max-percent", true);
		this.outOfPercent =  (Integer) configRoot.createChildObject(this, configEle,
				"outof-percent", true);
		return this;
	}
}
