package com.ifeng.ipserver.conf.plugin;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Map;

import org.w3c.dom.Element;

import com.ifeng.common.conf.ConfigException;
import com.ifeng.common.conf.ConfigRoot;
import com.ifeng.common.conf.Configurable;
import com.ifeng.common.misc.FileTools;
import com.ifeng.common.misc.Logger;
import com.ifeng.common.plugin.core.abst.AbstLogicPlugin;
/**
 * <title> FileListenerPlugin </title>
 * 
 * <pre>
 * 本类是一个“插件”， 此插件用于集成到一个责任链中，完成监听一个文件(或文件夹)的修改时间的变化，如果 相对上次检查发生了变化，则返回true ,否则返回false.
 * 本类的配置方式为：
 * &lt;... type="com.ifeng.ipserver.base.plugin。FileListenerPlugin"&gt;
 * 	 &lt;listen-path .../&gt;
 * 	 &lt;lasttime-file-path .../&gt;
 * 	 &lt;modify-time-key .../&gt;
 * &lt/...&gt 
 *其中：
 *	listen-path 
 *		类型：java.lang.String 
 *		用途：监听的文件路径
 *	lasttime-file-path
 *		 类型：java.lang.String
 *		用途: 存储上一次修改时间文件的路径
 *  modify-time-key
 *  	 类型：java.lang.String
 *  	 用途： 放入上下文对应时间戳的key。
 * </pre>
 * 
 * Copyright © 2012 Phoenix New Media Limited All Rights Reserved.
 * 
 * @author <a href="mailto:qilp@ifeng.com">Qi Lupeng</a>
 * @author <a href="mailto:jinmy@ifeng.com">Jin Mingyan</a>
 */
public class FileListenerPlugin extends AbstLogicPlugin implements Configurable{
	private String listenPath;
	private String lasttimeFilePath;
	private String modifyTimeKey;
	public String getModifyTimeKey() {
		return modifyTimeKey;
	}

	public void setModifyTimeKey(String modifyTimeKey) {
		this.modifyTimeKey = modifyTimeKey;
	}

	public String getListenPath() {
		return listenPath;
	}

	public void setListenPath(String listenPath) {
		this.listenPath = listenPath;
	}

	public String getLasttimeFilePath() {
		return lasttimeFilePath;
	}

	public void setLasttimeFilePath(String lasttimeFilePath) {
		this.lasttimeFilePath = lasttimeFilePath;
	}

	private static final Logger log = Logger.getLogger(FileListenerPlugin.class);
	/* (non-Javadoc)
	 * @see com.ifeng.common.plugin.core.itf.IntfPlugin#execute(java.lang.Object)
	 */
	@Override
	public Object execute(Object o) {
		//得到文件的上次修改时间， 数据来自本地的一个文件存储
		long lastTime = 0;
		BufferedReader bf = null;
		try {
			bf = FileTools.getInputStream(lasttimeFilePath);
			try {
				String line = bf.readLine();
				lastTime = Long.parseLong(line);
			} catch (IOException e) {
				log.error("Catch exception for read "+lasttimeFilePath+". The timestamp will be zero",e);
			}
			
		} catch (FileNotFoundException e) {
			log.warn("There isn't timestamp file "+lasttimeFilePath+".");
		}finally{
			if(bf!=null){
				try {
					bf.close();
				} catch (IOException e) {
				}
			}
		}
		//判断时间是否相同
		File file = new File(listenPath);
		long lastModifyTime = file.lastModified();
		if(lastModifyTime>lastTime){
			((Map)o).put(this.modifyTimeKey, new Long(lastModifyTime));
			return Boolean.TRUE;
		}else{
			return Boolean.FALSE;
		}
	}
	
	/* (non-Javadoc)
	 * @see com.ifeng.common.conf.Configurable#config(com.ifeng.common.conf.ConfigRoot, java.lang.Object, org.w3c.dom.Element)
	 */
	@Override
	public Object config(ConfigRoot configRoot, Object parent, Element configEle)
			throws ConfigException {
		this.listenPath = (String)configRoot.createChildObject(parent, configEle, "listen-path");
		this.lasttimeFilePath = (String)configRoot.createChildObject(parent, configEle, "lasttime-file-path");
		this.modifyTimeKey = (String)configRoot.createChildObject(parent, configEle, "modify-time-key");
		return this;
	}
}
