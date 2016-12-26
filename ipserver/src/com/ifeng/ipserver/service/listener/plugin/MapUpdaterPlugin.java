
package com.ifeng.ipserver.service.listener.plugin;

import com.ifeng.common.conf.ConfigException;
import com.ifeng.common.conf.ConfigRoot;
import com.ifeng.common.conf.Configurable;
import com.ifeng.common.misc.Logger;
import com.ifeng.common.plugin.core.itf.IntfPlugin;
import org.w3c.dom.Element;

import java.util.HashMap;
import java.util.Map;


/**
 *<title>Live3GUpdatePlugin<title>
 *<pre>
 *</pre>
 *
 * @author <a href="mailto:chengyong@ifeng.com">banban</a>
 *
 *  Copyright © 2014 Phoenix New Media Limited All Rights Reserved.
 */
public class MapUpdaterPlugin implements Configurable, IntfPlugin {
	private static final Logger log = Logger
			.getLogger(MapUpdaterPlugin.class);
	private String mapname;
	private Map    updateMap;

	@Override
	public Object execute(Object o) {
		String filepath = (String) o;
		//filepath = filepath.substring(0,filepath.lastIndexOf("/"))+"/server.xml";
		ConfigRoot root = null;
		try{			
			root = new ConfigRoot(filepath, System.getProperties());			
			Map liveUpdateMap = (Map) root.getValue(mapname);
			Map map = new HashMap();
			for(Object key:updateMap.keySet()){
				map.put(key, updateMap.get(key));
			}
			for(Object key:liveUpdateMap.keySet()){
				updateMap.put(key, liveUpdateMap.get(key));
			}
			for(Object key:map.keySet()){
				if(!liveUpdateMap.containsKey(key)){
					updateMap.remove(key);
				}
			}
						
			//liveMap = liveUpdateMap;
			log.info(mapname+" map is updated");
		}catch(Throwable e){
			log.info(mapname+" map update error"+e);
			return Boolean.FALSE;
		}	
		return Boolean.TRUE;
	}

	@Override
	public Object config(ConfigRoot configRoot, Object parent, Element configEle)
			throws ConfigException {
		this.updateMap =  (Map) configRoot.createChildObject(parent,
				configEle, "update-map");
		this.mapname =   (String) configRoot.createChildObject(parent,
				configEle, "map-name");
		return this;
	}

}
