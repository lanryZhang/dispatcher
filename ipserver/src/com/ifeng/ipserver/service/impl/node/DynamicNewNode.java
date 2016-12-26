package com.ifeng.ipserver.service.impl.node;

import com.ifeng.common.conf.ConfigException;
import com.ifeng.common.conf.ConfigRoot;
import com.ifeng.common.conf.Configurable;
import com.ifeng.common.misc.Logger;
import com.ifeng.common.plugin.core.itf.IntfPlugin;
import com.ifeng.ipserver.bean.Area;
import com.ifeng.ipserver.service.impl.node.bandwidth.BandwidthGetter;
import com.ifeng.ipserver.service.impl.rate.RateCalculator;
import org.w3c.dom.Element;

import java.util.*;


/**
 *
 * 直播和点播的动态点
 * 基本的策略是使用RateCalculator的计算能力，计算速率。然后根据配置的最大带宽和BandwidthGetter查询到的当前带宽，来设定一个极限速率。
 * 根据极限速率和目前速率的百分比关系，来决策是否进入一个控制流程，在控制流程中会决定是否由此节点处理对应的请求。
 * author gutc 2015-10-10
 */
public class DynamicNewNode implements Configurable {
    private RateCalculator rateCalculator;
    private String bakIpOrCname;
    private String ipOrCname;
    private IntfPlugin getIpPlugin;
    private long maxBandwidth;
    private int minRatePercent;
    private IntfPlugin plugin;
    private int period;
    private BandwidthGetter bandwidthGetter;
    private long lastBandwidth = -1;
    private Timer updateRateTimer;
    private Map dynamicnodeMap;
    private int threshold;
    private Map groupMap;
    private double bandwidthReference;
    private String isReference;


    // 需要控制的带宽对应的tps速率 这个是一个动态的数字 随着运行不停的变化 初始值为-1 即不控制
    private int limitedRate = -1;

    private static final Logger log = Logger.getLogger(DynamicNewNode.class);
    // 定期更新时调用的方法
    private void update(){
        try{
            long nowBandwidth = bandwidthGetter.getBandwidth();
            int maxBindwidthFinal;
            if(isReference.equals("true")){
                 maxBindwidthFinal = (int) (maxBandwidth*bandwidthReference);
            }else {
                 maxBindwidthFinal = (int) maxBandwidth;
            }


            if(nowBandwidth == lastBandwidth){
               /* log.info("node:"+ipOrCname+" limitedRate is "+limitedRate+" percent is "+((rateCalculator.getRate()*100)/limitedRate));*/
                return;
            }else{
                lastBandwidth = nowBandwidth;
            }

            if((nowBandwidth!=0)){
                if(rateCalculator.getRate()!=0){
                    limitedRate = ((int) (maxBindwidthFinal / (nowBandwidth / rateCalculator.getRate())));
                }else{
                    limitedRate = ((int) (maxBindwidthFinal * 100 / nowBandwidth));
                }
            }else{
                limitedRate = -1;
               /* log.warn("limited rate is unlimited ,because nowBandwidth is "+nowBandwidth+" or now rate is "+rateCalculator.getRate());*/
            }
        }catch(Throwable e){
            log.error("DynamicNewnode update error", e);
        }
        if(limitedRate == 0){
            limitedRate=1;
        }
    }

    /**
     * 得到对应的cname （点播）
     *

     * @return String ipOrCname
     */
    public Map<String,String> getIpOrCname(Area area, int threshold, Boolean isHead,Boolean isGroup) {
        String netName = area.getNetName();
        String province = area.getProvince();
        this.threshold = threshold;
        Map<String,String> map = new HashMap();


        int nowRatePercent = (int) ((rateCalculator.getRate() * 100) / limitedRate);
        if (rateCalculator.getRate() == 0) {
            nowRatePercent = ((10000) / limitedRate);
        }
        log.info("node " + ipOrCname + " nowRatePercent is " + nowRatePercent + " minRatePercent is " + minRatePercent);




        if (nowRatePercent > minRatePercent || nowRatePercent > threshold) {
            Map context = new HashMap();
            context.put("nowRatePercent", nowRatePercent);
            context.put("netName", netName);
            context.put("province", province);
            if ((Boolean) plugin.execute(context)) {
                //look for the same group node
                if(!isGroup){
                    List<String> group = getSameNodeList();
                    String tempIp = getReturnIpByGroupNode(area,group);
                    if(tempIp!=null){
                        map.put("overflow","false");
                        map.put("url",tempIp);
                        map.put("realIp",tempIp);
                        return map;
                    }
                }
                map.put("overflow","true");
                map.put("url",bakIpOrCname);
                map.put("realIp",ipOrCname);
                return map;
            }
        }
		/*
		 *  只有返回本节点的请求才需要计数
		 *  如果是同一个入口，多个出口的被依赖节点也需要计数
		 */
        if (!isHead) {
            rateCalculator.stat();
        }
        String returnIp = null;
        if (null != getIpPlugin) {
            Map ipMap = new HashMap();
            int ranValue = (int) (Math.random() * 100);
            ipMap.put("random-value", ranValue);
            getIpPlugin.execute(ipMap);
            returnIp = (String) ipMap.get("return-ip");
        }

        if (null == returnIp) {
            returnIp = ipOrCname;
        }
       // log.info("node " + ipOrCname + " return self ip " + returnIp);

        map.put("overflow","false");
        map.put("url",returnIp);
        map.put("realIp",returnIp);
        return map;
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
                    if(st.equals(ipOrCname)){
                        List<String> nodeList =((GroupNode) entry.getValue()).getNodes();
                        for(String node:nodeList){
                            if(!node.equals(ipOrCname)){
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
     * @param area
     * @return
     */
    private String getReturnIpByGroupNode(Area area,List<String> keys){
        if (dynamicnodeMap != null && keys != null){
            for (String key : keys) {
                if (dynamicnodeMap.containsKey(key)){
                    DynamicNewNode node = (DynamicNewNode) dynamicnodeMap.get(key);
                    String returnIp =  node.getIpOrCname(area, node.getThreshold(), false,Boolean.TRUE).get("url");
                    if (!returnIp.equals(node.getBakIpOrCname())){
                        log.info("Use Group Node " + key );
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
        this.dynamicnodeMap = (Map) configRoot.getValue("nodeMap");
        this.bandwidthReference = (Double)configRoot.createChildObject(parent, configEle, "bandwidthReference");
        this.groupMap = (Map) configRoot.getValue("groupMap");
        this.isReference = (String) configRoot.createChildObject(parent, configEle, "isReference");
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

    public String getIsReference() {
        return isReference;
    }

    public void setIsReference(String isReference) {
        this.isReference = isReference;
    }

    public double getBandwidthReference() {
        return bandwidthReference;
    }

    public void setBandwidthReference(double bandwidthReference) {
        this.bandwidthReference = bandwidthReference;
    }
}
