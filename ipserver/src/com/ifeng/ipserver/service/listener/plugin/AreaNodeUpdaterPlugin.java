package com.ifeng.ipserver.service.listener.plugin;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import org.w3c.dom.Element;

import com.ifeng.common.conf.ConfigException;
import com.ifeng.common.conf.ConfigRoot;
import com.ifeng.common.conf.Configurable;
import com.ifeng.common.email.IEmail;
import com.ifeng.common.misc.FileTools;
import com.ifeng.common.misc.Logger;
import com.ifeng.common.plugin.core.abst.AbstLogicPlugin;
import com.ifeng.ipserver.bean.Area;
import com.ifeng.ipserver.service.intf.AreaNodeManager;
/**
 * <title> AreaNodeUpdaterPlugin </title>
 * 
 * <pre>
 * 本类是一个“插件”， 此插件用于集成到一个责任链中，完成更新AreaNodeManager对应的哈希表的能力。
 * 本类的配置方式为：
 * &lt;... type="com.ifeng.ipserver.service.listener.plugin。AreaNodeUpdaterPlugin"&gt;
 * 	 &lt;file-path .../&gt;
 * 	 &lt;area-node-manager .../&gt;
 * 	 &lt;separator .../&gt;
 * &lt/...&gt 
 *其中：
 *	file-path
 *		类型：String
 *		用途：用户声明一个本地文件存储的路径
 *  area-node-manager
 *		类型：AreaNodeManager
 *		用途：AreaNodeManager的接口实现，实现了区域查询对应的CDN节点的能力
 *  separator
 *		类型：String
 *		用途：存储映射信息本地文件中，每行中的不同属性之间的分隔符
 * </pre>
 * 
 * Copyright © 2012 Phoenix New Media Limited All Rights Reserved.
 * 
 * @author <a href="mailto:yudf@ifeng.com">Yu Dengfeng</a>
 * @author <a href="mailto:jinmy@ifeng.com">Jin Mingyan</a>
 */
public class AreaNodeUpdaterPlugin extends AbstLogicPlugin implements Configurable{
	private static final Logger log = Logger.getLogger(AreaNodeUpdaterPlugin.class);
	private AreaNodeManager areaNodeManager;
	private String filePath;
	private String separator;
	
	@Override
	public Object execute(Object o) {
		BufferedReader br = null;
		try {
			Map map = Collections.synchronizedMap(new HashMap());
			br = FileTools.getInputStream(filePath);
			String line = br.readLine();
			while((line!=null)&&(line.trim().length()>0)){
				StringTokenizer st = new StringTokenizer(line,separator);
				if(st.countTokens()>=4){
					Area area = new Area();
					area.setNetName(st.nextToken());
					area.setProvince(st.nextToken());
					area.setCity(st.nextToken());
					map.put(area, st.nextToken());
				}
				line = br.readLine();
			}
			if(map.size()>0){
				log.info("Set a new map,size is "+map.size());
				areaNodeManager.setAreaToNodeMap(map);
			}
			return Boolean.TRUE;
		} catch (FileNotFoundException e) {
			log.error("Cann't read file: "+filePath,e);
			return Boolean.FALSE;
		}catch (IOException e) {
			log.error("Read file catch exception: "+filePath,e);
			return Boolean.FALSE;
		}finally{
			if(br!=null){
				try {
					br.close();
				} catch (IOException e) {
				}
			}
		}
	}
	@Override
	public Object config(ConfigRoot configRoot, Object parent, Element configEle)
			throws ConfigException {
		this.filePath = (String)configRoot.createChildObject(parent, configEle, "file-path");
		this.areaNodeManager = (AreaNodeManager)configRoot.createChildObject(parent, configEle, "area-node-manager");
		this.separator = (String)configRoot.createChildObject(parent, configEle, "separator");
		return this;
	}
	
}
