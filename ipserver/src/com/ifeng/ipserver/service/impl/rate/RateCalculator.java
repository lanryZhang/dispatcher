package com.ifeng.ipserver.service.impl.rate;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.collections.map.AbstractMapDecorator;
import org.w3c.dom.Element;

import com.ifeng.common.conf.ConfigException;
import com.ifeng.common.conf.ConfigRoot;
import com.ifeng.common.conf.Configurable;
import com.ifeng.common.misc.TimeLimitedMap;

/**
 * <title> RateCalculator</title>
 * 
 * <pre>
 * 	      这是一个速率计算器，用于统计和计算速率。核心能力是根据给定的步长计算速率。反馈的速率永远为过去一段时间的速率。
 * <br>
 * </pre>
 * 
 * * 配置方式为：
 * 
 * &lt;... type="com.ifeng.ipserver.service.impl.rate.RateCalculator"&gt;
 * &lt;interval .../&gt; &lt;period .../&gt; &lt/...&gt 其中： interval 类型： int
 * 单位为秒 用途：以多长时间为周期，统计速率。 period 类型：int 单位为毫秒 用途：速率更新的频繁度。
 * 
 * Copyright © 2012 Phoenix New Media Limited All Rights Reserved.
 * 
 * @author <a href="mailto:yudf@ifeng.com">Yu Dengfeng</a>
 * @author <a href="mailto:jinmy@ifeng.com">Jin Mingyan</a>
 */
public class RateCalculator implements Configurable {
	// 单位为秒，即计算多少秒的平均速率
	private int interval;

	// 存储未被计算的请求量
	private Integer secondCount = new Integer(0);

	// 任务执行的间隔，单位为毫秒
	private int period;

	// 一个带有超时机制的存储空间，由于存放每period间隔的请求量数据，定时过期“旧”的数据。新的数据用于计算速率
	private TimeLimitedMap map;

	// 过去一段时间 ( interval 约束） 平均每秒处理的请求量
	private long rate = 0;
	
	private long lastTime;
	
	private Timer updateRateTimer;

	public long getRate() {
		return rate;
	}

	// 暴露给外部调用的统计方法
	public void stat() {
		synchronized (secondCount) {
			secondCount++;
		}
	}

	// 定期更新时调用的方法
	private void update() {
		synchronized (secondCount) {
			// 放入新的数据，将请求数清零
			map.put(System.currentTimeMillis(), secondCount);
			secondCount = new Integer(0);
		}
		long sum = 0;
		int count = 0;
		// 遍历记录的请求数据，计算速率
		synchronized (map) {
			Iterator it = map.values().iterator();
			while (it.hasNext()) {
				count++;
				sum = sum + (Integer) it.next();
			}
		}
		if (sum == 0) {
			rate = 0;
		} else {
			if(lastTime==0){
				rate = sum * 100000 / (count * period);
			}else{
				rate = sum * 100000 /(count*(System.currentTimeMillis() - lastTime));
			}
			lastTime = System.currentTimeMillis();
		}
	}

	private class UpdateTask extends TimerTask {
		@Override
		public void run() {
			update();
		}
	}

	@Override
	public Object config(ConfigRoot configRoot, Object parent, Element configEle)
			throws ConfigException {
		this.interval = (Integer) configRoot.createChildObject(parent,
				configEle, "interval");
		this.period = (Integer) configRoot.createChildObject(parent, configEle,
				"period");
		map = new TimeLimitedMap(new AbstractMapDecorator(new HashMap()) {
			@Override
			public Object remove(Object key) {
				synchronized(map){
					return super.remove(key);
				}
			}
		}, interval * 1000);
	    updateRateTimer = new Timer("updateRateTimer");
		updateRateTimer.schedule(new UpdateTask(), period, period);
		return this;
	}

	public Timer getUpdateRateTimer() {
		return updateRateTimer;
	}
	
	
}
