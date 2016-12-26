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
 *<title>DynamicLiveNode<title>
 *<pre>
 *</pre>
 *
 * @author gutc
 *
 */
public class DynamicLiveNode implements Configurable {
	
	private String cdnname;
	private String cndid;
	private String backCndid;
	private long maxBandwidth;
	private long lastBandwidth = -1;
	private BandwidthGetter bandwidthGetter;
	private RateCalculator rateCalculator;
	private Map backUrlMap;
	private String url;
	private IntfPlugin plugin;
	private IntfPlugin getUrlPlugin;
	private String key;
	private int limitedRate = -1;
	private int minRatePercent;
	private int period;
	private Timer updateRateTimer;
	private Map groupMap;
	private Map liveNodeMap;
	
	private static final Logger log = Logger.getLogger(DynamicLiveNode.class);
	
	private void update(){
      try{
			long nowBandwidth = bandwidthGetter.getBandwidth();
			/*log.info("The node "+url+" bandwidth is "+nowBandwidth+"kb/s");*/
			
			if(nowBandwidth == lastBandwidth){
				
			/*log.info("Live node:"+url+" limitedRate is "+limitedRate+" percent is "+((rateCalculator.getRate()*100)/limitedRate));*/
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
				/*log.warn("Live limited rate is unlimited ,because nowBandwidth is "+nowBandwidth+" or now rate is "+rateCalculator.getRate());*/
			}
		}catch(Throwable e){
			log.error("DynamicLivenode update error", e);
		}
		if(limitedRate == 0){
			limitedRate=1;
		}
	}
	
    public LiveEntity getAddr(String chid,Boolean isGroup){
    	LiveEntity liveEntity = new LiveEntity();

		rateCalculator.stat();

    	if(key.equals("true")){
			int nowRatePercent = (int)((rateCalculator.getRate()*100)/limitedRate);
			if(rateCalculator.getRate()==0){
			    nowRatePercent = ((10000)/limitedRate);
			}

			/*log.info("Live node " + url + " nowRatePercent is " + nowRatePercent + " minRatePercent is " + minRatePercent);*/
			
			if(nowRatePercent>minRatePercent){
				Map context = new HashMap();
				context.put("nowRatePercent", nowRatePercent);
				if((Boolean)plugin.execute(context)){
					//look at the same group node
					if(!isGroup){
						List<String> group = getSameNodeList();
						LiveEntity le = getReturnIpByGroupNodeLive(chid,group);
						if(le!=null){
							liveEntity =le;
							return liveEntity;
						}
					}
					Object obj = backUrlMap.get(chid);
					String backUrl = "";
					String bcid = "overflow";//CDN Id
					if (obj instanceof IntfPlugin){
						IntfPlugin cdnPlugin = (IntfPlugin)obj;
						int random = (int) (Math.random() * 100);
						context.put("random",random);
						cdnPlugin.execute(context);
						backUrl = (String)context.get("cdn-url");
						bcid = (String)context.get("back-cdnid");
					}else if (obj instanceof String){
						backUrl = (String)obj;
						bcid = backCndid;
					}

					liveEntity.setUrl(backUrl);
					liveEntity.setCndid(bcid);
					liveEntity.setOverflow("true");
					liveEntity.setRealIp(url);
					return liveEntity;
				}
			}
    	}
		String returnIp = null;
		if(null!=getUrlPlugin){
			Map ipMap = new HashMap();
			int ranValue = (int) (Math.random()*100);
			ipMap.put("random-value", ranValue);
			getUrlPlugin.execute(ipMap);
			returnIp = (String) ipMap.get("return-ip");
		}
		if(returnIp==null){
			returnIp = url;			
		}
		liveEntity.setUrl(returnIp);
		liveEntity.setCndid(cndid);
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
					if(url.indexOf(st)!=-1){
						List<String> nodeList =((GroupNode) entry.getValue()).getNodes();
						for(String node:nodeList){
							if(url.indexOf(node)==-1){
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
	 *遍历同组所有节点，如果有未满的，返回该节点（直播）
	 * @param chid
	 * @return
	 */
	private LiveEntity getReturnIpByGroupNodeLive(String chid,List<String> keys){
		if (liveNodeMap != null && keys != null && keys.size()>0){
			for (String key : keys) {
				if (liveNodeMap.containsKey(key)){
					DynamicLiveNode node = (DynamicLiveNode) liveNodeMap.get(key);
					LiveEntity entity = node.getAddr(chid,Boolean.TRUE);
					if (entity.getCndid().equals(node.getCndid())){
						log.info("Use Group Node " + key );
						return entity;
					}
				}
			}
		}
		return null;
	}

	/**
	 * 直播流程调用方法，得到对应的LiveEntity(直播),
	 * 当节点不存在于配置节点map中，默认节点default调用该方法，
	 *返回自身ip和IfengP2P

	 * @return LiveEntity
	 */
	public LiveEntity getSelf(String node){
		LiveEntity liveEntity = new LiveEntity();
		liveEntity.setUrl("http://"+node+":80");
		liveEntity.setCndid(cndid);
		liveEntity.setOverflow("false");
		liveEntity.setRealIp(node);
		return liveEntity;
	}


	/**
	 * 直播流程调用方法，得到对应的LiveEntity(直播),默认节点default调用方法
	 *

	 * @return LiveEntity
	 */
	public LiveEntity getDefault(String chid){
		Map context = new HashMap();
		LiveEntity liveEntity = new LiveEntity();
		Object obj = backUrlMap.get(chid);
		String backUrl = "";
		String bcid = "";//CDN Id
		if (obj instanceof IntfPlugin){
			IntfPlugin cdnPlugin = (IntfPlugin)obj;
			int random = (int) (Math.random() * 100);
			context.put("random",random);
			cdnPlugin.execute(context);
			backUrl = (String)context.get("cdn-url");
			bcid = (String)context.get("back-cdnid");
			/*log.info("Live node "+url+" return real cdn "+bcid);*/
		}else if (obj instanceof String){
			backUrl = (String)obj;
			bcid = backCndid;
			/*log.info("Live node "+url+" return real cdn "+bcid);*/
		}
		liveEntity.setUrl(backUrl);
		liveEntity.setCndid(bcid);
		return liveEntity;
	}

	@Override
	public Object config(ConfigRoot configRoot, Object parent, Element configEle)
			throws ConfigException {	

		this.plugin = (IntfPlugin) configRoot.createChildObject(parent,
				configEle, "plugin");
		this.getUrlPlugin = (IntfPlugin) configRoot.createChildObject(parent,
				configEle, "url-plugin");
		this.bandwidthGetter = (BandwidthGetter) configRoot.createChildObject(parent,
				configEle, "bandwidth-getter");
		this.backUrlMap  =  (Map) configRoot.createChildObject(parent,
				configEle, "back-url-map");
		this.url  = (String) configRoot.createChildObject(parent,
				configEle, "url");
		this.maxBandwidth =  (Long) configRoot.createChildObject(parent,
				configEle, "max-bandwidth");
		this.minRatePercent =  (Integer) configRoot.createChildObject(parent,
				configEle, "min-rate-percent");
		this.rateCalculator = (RateCalculator) configRoot.createChildObject(parent,
				configEle, "rate-calculator");
		this.key =  (String) configRoot.createChildObject(parent,
				configEle, "key");
		this.backCndid =  (String) configRoot.createChildObject(parent,
				configEle, "back-cndid");
		this.cndid =  (String) configRoot.createChildObject(parent,
				configEle, "cndid");
		this.period = (Integer) configRoot.createChildObject(parent,
				configEle, "period");
		this.groupMap = (Map) configRoot.getValue("liveGroupMap");
		this.liveNodeMap = (Map) configRoot.getValue("liveNodeMap");
		updateRateTimer = new Timer("updateBandwidthTimer");
		updateRateTimer.schedule(new UpdateTask(), 1000, period);
		
		return this;
	}
	
	
	private class UpdateTask extends TimerTask { 
		@Override
		public void run() {
			update();
		}
	}


	public String getCdnname() {
		return cdnname;
	}

	public void setCdnname(String cdnname) {
		this.cdnname = cdnname;
	}

	public BandwidthGetter getBandwidthGetter() {
		return bandwidthGetter;
	}

	public void setBandwidthGetter(BandwidthGetter bandwidthGetter) {
		this.bandwidthGetter = bandwidthGetter;
	}

	public RateCalculator getRateCalculator() {
		return rateCalculator;
	}

	public void setRateCalculator(RateCalculator rateCalculator) {
		this.rateCalculator = rateCalculator;
	}

	public IntfPlugin getPlugin() {
		return plugin;
	}

	public void setPlugin(IntfPlugin plugin) {
		this.plugin = plugin;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getCndid() {
		return cndid;
	}

	public void setCndid(String cndid) {
		this.cndid = cndid;
	}

	public String getBackCndid() {
		return backCndid;
	}

	public void setBackCndid(String backCndid) {
		this.backCndid = backCndid;
	}

	public int getLimitedRate() {
		return limitedRate;
	}

	public void setLimitedRate(int limitedRate) {
		this.limitedRate = limitedRate;
	}

	public int getMinRatePercent() {
		return minRatePercent;
	}

	public void setMinRatePercent(int minRatePercent) {
		this.minRatePercent = minRatePercent;
	}

	public long getMaxBandwidth() {
		return maxBandwidth;
	}

	public void setMaxBandwidth(long maxBandwidth) {
		this.maxBandwidth = maxBandwidth;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public IntfPlugin getGetUrlPlugin() {
		return getUrlPlugin;
	}

	public void setGetUrlPlugin(IntfPlugin getUrlPlugin) {
		this.getUrlPlugin = getUrlPlugin;
	}

	public int getPeriod() {
		return period;
	}

	public void setPeriod(int period) {
		this.period = period;
	}

	public Timer getUpdateRateTimer() {
		return updateRateTimer;
	}

	public void setUpdateRateTimer(Timer updateRateTimer) {
		this.updateRateTimer = updateRateTimer;
	}


	public Map getBackUrlMap() {
		return backUrlMap;
	}

	public void setBackUrlMap(Map backUrlMap) {
		this.backUrlMap = backUrlMap;
	}

	public Map getGroupMap() {
		return groupMap;
	}

	public void setGroupMap(Map groupMap) {
		this.groupMap = groupMap;
	}

	public Map getLiveNodeMap() {
		return liveNodeMap;
	}

	public void setLiveNodeMap(Map liveNodeMap) {
		this.liveNodeMap = liveNodeMap;
	}
}


