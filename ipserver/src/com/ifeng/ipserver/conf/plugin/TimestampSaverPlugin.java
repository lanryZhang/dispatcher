package com.ifeng.ipserver.conf.plugin;


import java.io.BufferedWriter;
import java.io.File;
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
 * <title> TimestampSaverPlugin </title>
 * 
 * <pre>
 * 本类是一个“插件”， 此插件用于集成到一个责任链中，将一个上下文中的时间戳写入一个文件中.
 * 本类的配置方式为：
 * &lt;... type="com.ifeng.ipserver.base.plugin。TimestampSaverPlugin"&gt;
 * 
 * 	 &lt;lasttime-file-path .../&gt;
 * 	 &lt;modify-time-key .../&gt;
 * 
 * &lt/...&gt 
 *其中：
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
public class TimestampSaverPlugin extends AbstLogicPlugin implements Configurable{
	private String lasttimeFilePath;
	private String modifyTimeKey;
	public String getModifyTimeKey() {
		return modifyTimeKey;
	}

	public void setModifyTimeKey(String modifyTimeKey) {
		this.modifyTimeKey = modifyTimeKey;
	}


	public String getLasttimeFilePath() {
		return lasttimeFilePath;
	}

	public void setLasttimeFilePath(String lasttimeFilePath) {
		this.lasttimeFilePath = lasttimeFilePath;
	}

	private static final Logger log = Logger.getLogger(TimestampSaverPlugin.class);
	/* (non-Javadoc)
	 * @see com.ifeng.common.plugin.core.itf.IntfPlugin#execute(java.lang.Object)
	 */
	@Override
	public Object execute(Object o) {
		File file = new File(this.lasttimeFilePath);
		file.deleteOnExit();
		BufferedWriter bw = null;
		try {
			bw = FileTools.getBufferedWriter(file);
			bw.write(Long.toString((Long)((Map)o).get(this.modifyTimeKey)));
			bw.flush();
			return Boolean.TRUE;
		} catch (IOException e) {
			log.error("Cann't get bufferwriter for file :"+this.lasttimeFilePath);
			return Boolean.FALSE;
		}finally{
			if(bw!=null){
				try {
					bw.close();
				} catch (IOException e) {
				}
			}
		}
		
	}
	
	/* (non-Javadoc)
	 * @see com.ifeng.common.conf.Configurable#config(com.ifeng.common.conf.ConfigRoot, java.lang.Object, org.w3c.dom.Element)
	 */
	
	@Override
	public Object config(ConfigRoot configRoot, Object parent, Element configEle)
			throws ConfigException {
		this.lasttimeFilePath = (String)configRoot.createChildObject(parent, configEle, "lasttime-file-path");
		this.modifyTimeKey = (String)configRoot.createChildObject(parent, configEle, "modify-time-key");
		return this;
	}
}
