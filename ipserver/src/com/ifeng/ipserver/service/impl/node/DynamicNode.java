package com.ifeng.ipserver.service.impl.node;

import com.ifeng.common.conf.ConfigException;
import com.ifeng.common.conf.ConfigRoot;
import com.ifeng.common.conf.Configurable;
import com.ifeng.common.misc.Logger;
import com.ifeng.common.plugin.core.itf.IntfPlugin;
import com.ifeng.ipserver.bean.Area;
import com.ifeng.ipserver.service.impl.node.bandwidth.BandwidthGetter;
import com.ifeng.ipserver.service.impl.rate.LiveRateGetter;
import com.ifeng.ipserver.service.impl.rate.RateCalculator;
import org.w3c.dom.Element;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

/**
 * <title> DynamicNode</title>
 * 
 * <pre>
 * 	     区别于静态的node，这是一个带有带宽控制能力的Node。
 * 	      基本的策略是使用RateCalculator的计算能力，计算速率。然后根据配置的最大带宽和BandwidthGetter查询到的当前带宽，来设定一个极限速率。
 * 	      根据极限速率和目前速率的百分比关系，来决策是否进入一个控制流程，在控制流程中会决定是否由此节点处理对应的请求。
 * <br>
 * </pre>
 * 
 * * 配置方式为：
 * 
 * &lt;... type="com.ifeng.ipserver.service.impl.node。DynamicNode"&gt;
 * &lt;rate-calculator .../&gt; &lt;bak-ipOrCname .../&gt; &lt;ipOrCname
 * .../&gt; &lt;max-bandwidth .../&gt; &lt;min-rate-percent .../&gt; &lt;plugin
 * .../&gt; &lt;period .../&gt; &lt;bandwidth-getter .../&gt; &lt/...&gt 其中：
 * rate-calculator 类型： RateCalculator 速率计算器 用途：用于计算过去一段时间（来自配置）的平均速率
 * bak-ipOrCname 类型：String 用途：备份节点的路径 ipOrCname 类型：String 用途：正式节点的路径
 * max-bandwidth 类型：int 单位kB/s 用途：最大带宽限制 min-rate-percent 类型：int 单位百分比 如80即80%
 * 用途：启动速率控制的带宽消耗百分比 plugin 类型：com.ifeng.common.plugin.core.itf.IntfPlugin
 * 用途：带宽控制插件，使用一个可变结构来描述带宽限制的算法。 bandwidth-getter
 * 类型：com.ifeng.ipserver.service.impl.node.bandwidth.BandwidthGetter
 * 用途：获取借点带宽消耗的能力
 * 
 * Copyright © 2012 Phoenix New Media Limited All Rights Reserved.
 * 
 * @author <a href="mailto:chenyong@ifeng.com">banban</a>
 * @author <a href="mailto:jinmy@ifeng.com">Jin Mingyan</a>
 */
public class DynamicNode implements Configurable {
	private RateCalculator rateCalculator;
	private String bakIpOrCname;
	private String ipOrCname;
	// 允许的最大带宽
	private IntfPlugin getIpPlugin;
	private long maxBandwidth;
	private int minRatePercent;
	private IntfPlugin plugin;
	private IntfPlugin rateLimitDependencyPlugin;
	private int period;
	private BandwidthGetter bandwidthGetter;
	private long lastBandwidth = -1;
	private Timer updateRateTimer;
	private Map dynamicnodeMap;
	private String dependencyKey;
	private int threshold;
	private LiveRateGetter liveRateGetter;
	/*
	 * 节点依赖类型
	 * 1 同一个入口，多个出口
	 * 该值需要配置在被依赖的节点中，以保证请求落在节点上后，被依赖的节点也会跟着计数。
	 */
	private String dependencyMode = null;
	
	// 需要控制的带宽对应的tps速率 这个是一个动态的数字 随着运行不停的变化 初始值为-1 即不控制
	private int limitedRate = -1;

	private static final Logger log = Logger.getLogger(DynamicNode.class);
	// 定期更新时调用的方法
	private void update() {
		try {
			
			long nowBandwidth = bandwidthGetter.getBandwidth();
			log.info("The node " + ipOrCname + " bandwidth is " + nowBandwidth + "kb/s");
			if (nowBandwidth == lastBandwidth) {
				log.info("node:" + ipOrCname + " limitedRate is " + limitedRate + " percent is " + ((rateCalculator.getRate() * 100) / limitedRate));
				return;
			} else {
				lastBandwidth = nowBandwidth;
			}
			// if((nowBandwidth!=0)&&(rateCalculator.getRate()!=0)){
			//log.info("rateCalculator.getRate()=" + rateCalculator.getRate());
			if ((nowBandwidth != 0)) {
				if (rateCalculator.getRate() != 0) {
					limitedRate = ((int) (maxBandwidth / (nowBandwidth / rateCalculator.getRate())));
				} else {
					limitedRate = ((int) (maxBandwidth * 100 / nowBandwidth));
				}

			} else {
				limitedRate = -1;
				log.warn("limited rate is unlimited ,because nowBandwidth is " + nowBandwidth + " or now rate is " + rateCalculator.getRate());
			}
			
		} catch (Throwable e) {
			log.error("Dynamicnode update error", e);
		}
		if (limitedRate == 0) {
			limitedRate = 1;
		}
		log.info("node:" + ipOrCname + " limitedRate is " + limitedRate + " percent is " + ((rateCalculator.getRate() * 100) / limitedRate)
				+ " limited updated");
	}

	/**
	 * 得到对应的cname
	 * 

	 * @return String ipOrCname
	 */
	public String getIpOrCname(Area area, int threshold, Boolean isHead) {

		String netName = area.getNetName();
		String province = area.getProvince();
		this.threshold = threshold;
		
		String returnIp = getReturnIpByDependencyNode(area,dependencyKey);
		if (null != returnIp){
			return returnIp;
		}
		
		int nowRatePercent = (int) ((rateCalculator.getRate() * 100) / limitedRate);
		if (rateCalculator.getRate() == 0) {
			nowRatePercent = ((10000) / limitedRate);
		}
		if (rateLimitDependencyPlugin != null){
			int liveIncreaseRate = liveRateGetter.getLiveIncreaseRate();
			int requestNum = liveRateGetter.getLiveRequestNum();
			
			HashMap<String, Object> context = new HashMap<String, Object>();
			context.put("liveIncreaseRate", liveIncreaseRate);
			context.put("requestNum", requestNum);
			int res = 0;
			if ((Boolean)rateLimitDependencyPlugin.execute(context)){
				res = (Integer) context.get("virtualNowpercent");
			}
			if (res > 0){
				nowRatePercent = res;
				log.info("直播增长速率:"+liveIncreaseRate+",虚拟切分比例："+nowRatePercent);
			}
		}
		
		if (nowRatePercent > minRatePercent && nowRatePercent > threshold) {
			Map context = new HashMap();
			context.put("nowRatePercent", nowRatePercent);
			context.put("netName", netName);
			context.put("province", province);
			if ((Boolean) plugin.execute(context)) {
				log.info("Out a request for " + netName + province);
				return bakIpOrCname;
			}
		}
		/*
		 *  只有返回本节点的请求才需要计数
		 *  如果是同一个入口，多个出口的被依赖节点也需要计数
		 */
		if (!isHead || (dependencyMode != null && dependencyMode.equals("1"))) {
			rateCalculator.stat();
		}
		returnIp = null;
		if (null != getIpPlugin) {
			Map ipMap = new HashMap();
			int ranValue = (int) (Math.random() * 100);
			ipMap.put("random-value", ranValue);
			getIpPlugin.execute(ipMap);
			returnIp = (String) ipMap.get("return-ip");
			
			//多入口多出口节点，需要根据各个节点流量判断是否切分
			//当同一地区同一运营商对应多个VDN节点时，该方法起作用
			if (!returnIp.equals(ipOrCname)){
				String tempIp = getReturnIpByDependencyNode(area,returnIp);
			 	if (null != tempIp){
			 		return tempIp;
			 	}
		 	}
		}

		if (null == returnIp) {
			returnIp = ipOrCname;
		}
		log.info("node " + ipOrCname + " return real ip " + returnIp);
		return returnIp;
	}

	/**
	 * 如果节点配置了依赖节点，需要读取所依赖的节点当前是否已经跑到临界值，
	 * 跑到临界值，则跳过本结点其他判断，直接使用依赖节点的返回值。
	 * @param area
	 * @return
	 */
	private String getReturnIpByDependencyNode(Area area,String keys){
		if (dynamicnodeMap != null && keys != null){
			String[] keyArr = keys.split(",");
			for (String key : keyArr) {
				if (dynamicnodeMap.containsKey(key)){
					DynamicNode node = (DynamicNode) dynamicnodeMap.get(key);
					
					String returnIp = node.getIpOrCname(area, node.getThreshold(), true);
					if (returnIp.equals(node.getBakIpOrCname())){
						log.info("Use Dependency Node " + key );
						return returnIp;
					}
				}
				
			}
		}
		return null;
	}
	private class UpdateTask extends TimerTask {
		@Override
		public void run() {
			update();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.ifeng.common.conf.Configurable#config(com.ifeng.common.conf.ConfigRoot
	 * , java.lang.Object, org.w3c.dom.Element)
	 */
	@Override
	public Object config(ConfigRoot configRoot, Object parent, Element configEle) throws ConfigException {
		this.rateCalculator = (RateCalculator) configRoot.createChildObject(parent, configEle, "rate-calculator");
		this.bakIpOrCname = (String) configRoot.createChildObject(parent, configEle, "bak-ipOrCname");
		this.ipOrCname = (String) configRoot.createChildObject(parent, configEle, "ipOrCname");
		this.maxBandwidth = (Long) configRoot.createChildObject(parent, configEle, "max-bandwidth");
		this.minRatePercent = (Integer) configRoot.createChildObject(parent, configEle, "min-rate-percent");
		this.plugin = (IntfPlugin) configRoot.createChildObject(parent, configEle, "plugin");
		this.period = (Integer) configRoot.createChildObject(parent, configEle, "period");
		this.bandwidthGetter = (BandwidthGetter) configRoot.createChildObject(parent, configEle, "bandwidth-getter");
		this.getIpPlugin = (IntfPlugin) configRoot.createChildObject(parent, configEle, "ip-plugin");
		this.dynamicnodeMap = (Map) configRoot.createChildObject(parent, configEle, "dynamicnodeMap");
		this.dependencyKey = (String) configRoot.createChildObject(parent, configEle, "dependencyKey");
		this.dependencyMode = (String) configRoot.createChildObject(parent, configEle, "dependencyMode");
		this.liveRateGetter = (LiveRateGetter) configRoot.createChildObject(parent, configEle, "live-rate-getter");
		this.rateLimitDependencyPlugin = (IntfPlugin)configRoot.createChildObject(parent, configEle, "rate-limit-dependency-plugin");
		updateRateTimer = new Timer("updateBandwidthTimer");
		updateRateTimer.schedule(new UpdateTask(), 120000, period);
		return this;
	}

	public int getLimitedRate() {
		return limitedRate;
	}

	public void setLimitedRate(int limitedRate) {
		this.limitedRate = limitedRate;
	}
	
	public RateCalculator getRateCalculator() {
		return rateCalculator;
	}

	public void setRateCalculator(RateCalculator rateCalculator) {
		this.rateCalculator = rateCalculator;
	}

	public String getBakIpOrCname() {
		return bakIpOrCname;
	}

	public void setBakIpOrCname(String bakIpOrCname) {
		this.bakIpOrCname = bakIpOrCname;
	}

	public String getIpOrCname() {
		return ipOrCname;
	}

	public void setIpOrCname(String ipOrCname) {
		this.ipOrCname = ipOrCname;
	}

	public long getMaxBandwidth() {
		return maxBandwidth;
	}

	public void setMaxBandwidth(long maxBandwidth) {
		this.maxBandwidth = maxBandwidth;
	}

	public int getMinRatePercent() {
		return minRatePercent;
	}

	public void setMinRatePercent(int minRatePercent) {
		this.minRatePercent = minRatePercent;
	}

	public IntfPlugin getPlugin() {
		return plugin;
	}

	public void setPlugin(IntfPlugin plugin) {
		this.plugin = plugin;
	}

	public int getPeriod() {
		return period;
	}

	public void setPeriod(int period) {
		this.period = period;
	}

	public BandwidthGetter getBandwidthGetter() {
		return bandwidthGetter;
	}

	public void setBandwidthGetter(BandwidthGetter bandwidthGetter) {
		this.bandwidthGetter = bandwidthGetter;
	}

	public Timer getUpdateRateTimer() {
		return updateRateTimer;
	}

	public IntfPlugin getGetIpPlugin() {
		return getIpPlugin;
	}

	public void setGetIpPlugin(IntfPlugin getIpPlugin) {
		this.getIpPlugin = getIpPlugin;
	}

	public int getThreshold() {
		return threshold;
	}

	public void setThreshold(int threshold) {
		this.threshold = threshold;
	}

	public String isDependencyMode() {
		return dependencyMode;
	}

	public void setDependencyMode(String dependencyMode) {
		this.dependencyMode = dependencyMode;
	}

	public LiveRateGetter getLiveRateGetter() {
		return liveRateGetter;
	}

	public void setLiveRateGetter(LiveRateGetter liveRateGetter) {
		this.liveRateGetter = liveRateGetter;
	}

	public IntfPlugin getRateLimitDependencyPlugin() {
		return rateLimitDependencyPlugin;
	}

	public void setRateLimitDependencyPlugin(IntfPlugin rateLimitDependencyPlugin) {
		this.rateLimitDependencyPlugin = rateLimitDependencyPlugin;
	}

}
