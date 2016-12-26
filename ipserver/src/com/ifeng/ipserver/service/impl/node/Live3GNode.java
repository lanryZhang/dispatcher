
package com.ifeng.ipserver.service.impl.node;

import com.ifeng.common.conf.ConfigException;
import com.ifeng.common.conf.ConfigRoot;
import com.ifeng.common.conf.Configurable;
import com.ifeng.common.misc.Logger;
import com.ifeng.common.plugin.core.itf.IntfPlugin;
import com.ifeng.ipserver.server.handler.plugin.live.LiveEntity;
import com.ifeng.ipserver.service.impl.node.bandwidth.BandwidthGetter;
import com.ifeng.ipserver.service.impl.rate.RateCalculator;
import org.w3c.dom.Element;

import java.util.*;


/**
 *<title>Live3GNode<title>
 *<pre>
 *</pre>
 *
 * @author <a href="mailto:chengyong@ifeng.com">banban</a>
 *
 *  Copyright © 2014 Phoenix New Media Limited All Rights Reserved.
 */
public class Live3GNode implements  Configurable {
	private static final Logger log = Logger.getLogger(Live3GNode.class);
	private RateCalculator rateCalculator;
	private String key;
	private String backIp;
	private String ip;
	private IntfPlugin getIpPlugin;
	private long maxBandwidth;
	private int minRatePercent;
	private IntfPlugin plugin;
	private int period;
	private BandwidthGetter bandwidthGetter;
	private long lastBandwidth = -1;
	private Timer updateRateTimer;
	private Map nodeMap;
	private Map groupMap;
	private Map channelMap;


	// 需要控制的带宽对应的tps速率 这个是一个动态的数字 随着运行不停的变化 初始值为-1 即不控制
	private int limitedRate = -1;

	// 定期更新时调用的方法
	private void update(){
		try{
			long nowBandwidth = bandwidthGetter.getBandwidth();
			/*log.info("The node "+ip+" bandwidth is "+nowBandwidth+"kb/s");*/

			if(nowBandwidth == lastBandwidth){

				/*log.info("live3GV node:"+ip+" limitedRate is "+limitedRate+" percent is "+((rateCalculator.getRate()*100)/limitedRate));*/
				return;
			}else{
				lastBandwidth = nowBandwidth;
			}

			if((nowBandwidth!=0)){
				if(rateCalculator.getRate()!=0){
					limitedRate = ((int)(maxBandwidth/(nowBandwidth/rateCalculator.getRate())));
				}else{
					limitedRate = ((int)(maxBandwidth*100/nowBandwidth));
				}
			}else{
				limitedRate = -1;
				/*log.warn("live3GV limited rate is unlimited ,because nowBandwidth is "+nowBandwidth+" or now rate is "+rateCalculator.getRate());*/
			}
		}catch(Throwable e){
			log.error("live3GV update error", e);
		}
		if(limitedRate == 0){
			limitedRate=1;
		}
	}


	public LiveEntity getNodeIp(String chid,Boolean isGroup){
		LiveEntity liveEntity = new LiveEntity();
		rateCalculator.stat();

		String returnIp = null;
		if(null!=getIpPlugin){
			Map ipMap = new HashMap();
			int ranValue = (int) (Math.random()*100);
			ipMap.put("random-value", ranValue);
			getIpPlugin.execute(ipMap);
			returnIp = (String) ipMap.get("return-ip");
		}
		if(null == returnIp){
			returnIp = ip;
		}

		if (key.equals("true")){
			int nowRatePercent = (int)((rateCalculator.getRate()*100)/limitedRate);
			if(rateCalculator.getRate()==0){
				nowRatePercent = ((10000)/limitedRate);
			}
			if(nowRatePercent>minRatePercent){
				Map context = new HashMap();
				context.put("nowRatePercent", nowRatePercent);
				if((Boolean)plugin.execute(context)){
					//look for same group node
					/*if(!isGroup){
						List<String> group = getSameNodeList();
						LiveEntity groupEntity = getReturnIpByGroupNode(chid,group);
						if(groupEntity!=null){
							return groupEntity;
						}
					}*/
					Object obj = channelMap.get(chid);
					String backUrl =returnIp;
					String cdnid="";
					if (obj instanceof IntfPlugin){
						IntfPlugin cdnPlugin = (IntfPlugin)obj;
						int random = (int) (Math.random() * 100);
						context.put("random", random);
						cdnPlugin.execute(context);
						backUrl = (String)context.get("live3g-url");
						cdnid = (String)context.get("back-cdnid");
					}else if (obj instanceof String){
						backUrl = (String)obj;
					}
					liveEntity.setCndid(cdnid);
					liveEntity.setUrl(backUrl);
					if(backUrl.equals(returnIp)){
						liveEntity.setOverflow("false");
						liveEntity.setRealIp(backUrl);
					}else{
						liveEntity.setOverflow("true");
						liveEntity.setRealIp(ip);
					}
					return liveEntity;
				}
			}
		}


		liveEntity.setUrl(returnIp);
		liveEntity.setOverflow("false");
		liveEntity.setRealIp(returnIp);
		return liveEntity;
	}

	/**
	 * 得到本节点的同组的其他节点
	 * @return List
	 */
	private List getSameNodeList(){
		List<String> nodeListExceptSelf = new ArrayList<String>();
		if(groupMap!=null){
			Iterator entries = groupMap.entrySet().iterator();
			while (entries.hasNext()) {
				Map.Entry entry = (Map.Entry) entries.next();
				GroupNode group = (GroupNode) entry.getValue();
				for(String st:group.getNodes()){
					if(st.equals(ip)){
						List<String> nodeList =((GroupNode) entry.getValue()).getNodes();
						for(String node:nodeList){
							if(!node.equals(ip)){
								nodeListExceptSelf.add(node);
							}
						}
						return nodeListExceptSelf;
					}
				}
			}
		}
		return nodeListExceptSelf;
	}


	/**
	 * 遍历同组所有节点，如果有未满的，返回该节点（点播）
	 * @return
	 */
	private LiveEntity getReturnIpByGroupNode(String chid,List<String> keys){
		if (nodeMap != null && keys != null){
			for (String key : keys) {
				if (nodeMap.containsKey(key)){
					Live3GNode node = (Live3GNode) nodeMap.get(key);
					LiveEntity liveEntity = node.getNodeIp(chid,Boolean.TRUE);
					String returnIp = liveEntity.getUrl();
					if (!returnIp.equals(node.getBackIp())){
						log.info("Use Group Node " + key );
						return liveEntity;
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

	@Override
	public Object config(ConfigRoot configRoot, Object parent, Element configEle) throws ConfigException {
		this.rateCalculator = (RateCalculator) configRoot.createChildObject(parent, configEle, "rate-calculator");
		this.key = (String) configRoot.createChildObject(parent, configEle, "key");
		this.backIp = (String) configRoot.createChildObject(parent, configEle, "back-ip");
		this.ip = (String) configRoot.createChildObject(parent, configEle, "ip");
		this.maxBandwidth = (Long) configRoot.createChildObject(parent, configEle, "max-bandwidth");
		this.minRatePercent = (Integer) configRoot.createChildObject(parent, configEle, "min-rate-percent");
		this.plugin = (IntfPlugin) configRoot.createChildObject(parent, configEle, "plugin");
		this.period = (Integer) configRoot.createChildObject(parent, configEle, "period");
		this.bandwidthGetter = (BandwidthGetter) configRoot.createChildObject(parent, configEle, "bandwidth-getter");
		this.getIpPlugin = (IntfPlugin) configRoot.createChildObject(parent, configEle, "ip-plugin");
		this.nodeMap = (Map) configRoot.getValue("live3GNodeMap");
		this.groupMap = (Map) configRoot.getValue("live3gGroupMap");
		this.channelMap = (Map) configRoot.getValue("live3gChannelMap");
		updateRateTimer = new Timer("updateBandwidthTimer");
		updateRateTimer.schedule(new UpdateTask(), 120000, period);

		return this;
	}

	public RateCalculator getRateCalculator() {
		return rateCalculator;
	}

	public void setRateCalculator(RateCalculator rateCalculator) {
		this.rateCalculator = rateCalculator;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getBackIp() {
		return backIp;
	}

	public void setBackIp(String backIp) {
		this.backIp = backIp;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public Map getNodeMap() {
		return nodeMap;
	}

	public void setNodeMap(Map nodeMap) {
		this.nodeMap = nodeMap;
	}

	public IntfPlugin getGetIpPlugin() {
		return getIpPlugin;
	}

	public void setGetIpPlugin(IntfPlugin getIpPlugin) {
		this.getIpPlugin = getIpPlugin;
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

	public void setUpdateRateTimer(Timer updateRateTimer) {
		this.updateRateTimer = updateRateTimer;
	}

	public Map getGroupMap() {
		return groupMap;
	}

	public void setGroupMap(Map groupMap) {
		this.groupMap = groupMap;
	}

	public int getLimitedRate() {
		return limitedRate;
	}

	public void setLimitedRate(int limitedRate) {
		this.limitedRate = limitedRate;
	}

	public long getLastBandwidth() {
		return lastBandwidth;
	}

	public void setLastBandwidth(long lastBandwidth) {
		this.lastBandwidth = lastBandwidth;
	}

	public String getIp() {
		return ip;
	}
}
