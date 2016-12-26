package com.ifeng.ipserver.service.listener;

import java.io.File;
import java.util.Timer;
import java.util.TimerTask;

import org.w3c.dom.Element;

import com.ifeng.common.conf.ConfigException;
import com.ifeng.common.conf.ConfigRoot;
import com.ifeng.common.conf.Configurable;
import com.ifeng.common.misc.Logger;
import com.ifeng.common.plugin.core.itf.IntfPlugin;

/**
 * 
 * 
 * 本地文件有更新时，执行文件更新对应的操作

  @author :chenyong
  @version 1.0
  @date 2012-9-3
 */

public class FileUpdateListener implements Configurable {
	private static final Logger log = Logger
			.getLogger(FileUpdateListener.class);
	//要更新的文件地址
	private String filepath;
	//文件最后修改时间
	private long lastModifyTime=0; 
	//文件修改插件
	private IntfPlugin plugin; 
	//文件更新间隔
	private long period;
	
	
	
	public void init(boolean start){
		
		File file = new File(filepath);
		long modifyTime = file.lastModified();		
		if(start){
			lastModifyTime = modifyTime;
			log.info("Init the dynamicnode file lastModifyTime:"+lastModifyTime);
		}else{
			if(modifyTime > lastModifyTime){				
				plugin.execute(filepath);
				lastModifyTime = modifyTime;
			}
		}		
	}
	
	@Override
	public Object config(ConfigRoot configRoot, Object parent, Element configEle)
			throws ConfigException {
		this.filepath = (String) configRoot.createChildObject(parent,
				configEle, "file-path");
		this.plugin =  (IntfPlugin) configRoot.createChildObject(parent,
				configEle, "plugin");
		this.period =   (Long) configRoot.createChildObject(parent,
				configEle, "period");
		Timer updateTimer = new Timer("nodeUpdateTimer");
		updateTimer.schedule(new UpdateTask(), period, period);
		init(true);
		return this;
	}
	
	private class UpdateTask extends TimerTask{

		@Override
		public void run() {
			init(false);
		}	
	}

}
