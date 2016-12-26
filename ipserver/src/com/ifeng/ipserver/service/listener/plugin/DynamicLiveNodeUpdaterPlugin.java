package com.ifeng.ipserver.service.listener.plugin;

import com.ifeng.common.conf.ConfigException;
import com.ifeng.common.conf.ConfigRoot;
import com.ifeng.common.conf.Configurable;
import com.ifeng.common.misc.Logger;
import com.ifeng.common.plugin.core.itf.IntfPlugin;
import com.ifeng.ipserver.service.impl.node.DynamicLiveNode;
import org.w3c.dom.Element;

import java.util.*;
import java.util.Map.Entry;

/**
 * 更新动态节点，检测到dynamicnode文件有更新则更新具备流量控制能力的动态节点。

  @author :chenyong
  @version 1.0
  @date 2012-9-3
 */

public class DynamicLiveNodeUpdaterPlugin implements IntfPlugin,Configurable {
	private static final Logger log = Logger.getLogger(DynamicLiveNodeUpdaterPlugin.class);
	private String mapname;
	private Map dynamicMap;
	@Override
	public Object execute(Object o) {
		String filepath = (String) o;
	//	filepath = filepath.substring(0,filepath.lastIndexOf("/"))+"/server.xml";
		ConfigRoot root = null;
		try{
			root = new ConfigRoot(filepath, System.getProperties());
			Map dymap = (Map) root.getValue(mapname);
			Iterator it = dymap.entrySet().iterator();
			while(it.hasNext()){
				Entry entry = (Entry) it.next();
				String nodeid = (String) entry.getKey();
				DynamicLiveNode node = (DynamicLiveNode) entry.getValue();
				if(!dynamicMap.containsKey(nodeid)){ //如果原来不存在此节点，则添加到map中
					dynamicMap.put(nodeid, node);
				}else{ //如果有则将最新数据更新进去
					DynamicLiveNode dyNode = (DynamicLiveNode) dynamicMap.get(nodeid);
					Timer timer = dyNode.getUpdateRateTimer();
					timer.cancel();
					dyNode.getRateCalculator().getUpdateRateTimer().cancel();
					dynamicMap.put(nodeid, node);
				}
			}

			Set dnSet = dynamicMap.keySet();
			List delListKey =new ArrayList();
			for(Object key:dnSet){ //如果原来map有新数据中没有的数据则删除数据
				if(!dymap.containsKey(key)){
					delListKey.add(key);
				}
			}
			for(Object key:delListKey){
				DynamicLiveNode tempNode = (DynamicLiveNode) dynamicMap.get(key);
				tempNode.getUpdateRateTimer().cancel();
				tempNode.getRateCalculator().getUpdateRateTimer().cancel();
				dynamicMap.remove(key);
			}
		}catch(Throwable e){
			log.warn("liveNodeMap update failed:" + e);
			return Boolean.FALSE;
		}

		log.info("liveNodeMap is update");
		return Boolean.TRUE;
	}
	@Override
	public Object config(ConfigRoot configRoot, Object parent, Element configEle)
			throws ConfigException {
		this.dynamicMap =  (Map) configRoot.createChildObject(parent,
				configEle, "dynamic-map");
		this.mapname =   (String) configRoot.createChildObject(parent,
				configEle, "map-name");
		return this;
	}

}
