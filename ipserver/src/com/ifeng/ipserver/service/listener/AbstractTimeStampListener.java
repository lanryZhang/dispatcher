package com.ifeng.ipserver.service.listener;

import com.ifeng.common.conf.ConfigException;
import com.ifeng.common.conf.ConfigRoot;
import com.ifeng.common.conf.Configurable;
import com.ifeng.common.misc.FileTools;
import com.ifeng.common.misc.Logger;
import com.ifeng.common.plugin.core.itf.IntfPlugin;
import org.w3c.dom.Element;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

/**
 * <title> AbstractTimeStampListener</title>
 * 
 * <pre>
 * 这个是一个抽象类，用于监听一个时间戳，与本地存储的时间戳做比对，当发生改变时执行一个业务逻辑。
 * 	  初始化时会进行第一次判断，比较本地的时间戳与被监听的时间戳的值，如果本地的较小，则执行对应的业务逻辑并更新本地的时间戳。
 * 	  如果本地的时间戳在初始化时不存在，则直接用认为是0.
 *   有两个业务逻辑分支可选，具体请参考子类对应的配置。
 * <br>
 * </pre>
 * 
 * Copyright © 2012 Phoenix New Media Limited All Rights Reserved.
 * 
 * @author <a href="mailto:yudf@ifeng.com">Yu Dengfeng</a>
 * @author <a href="mailto:jinmy@ifeng.com">Jin Mingyan</a>
 */

public abstract class AbstractTimeStampListener implements Configurable {
	private static final Logger log = Logger
			.getLogger(AbstractTimeStampListener.class);
	//存储系统认为的上一次修改时间
	private long lastModifyTime = 0;
	//存储上一次修改时间对应的文件路径
	private String timestampFilePath;
	//需要执行的过程 initFromLocalPlugin 使用本地的配置进行初始化 initFromNetPlugin 使用远程的配置初始化
	private IntfPlugin initFromLocalPlugin;
	private IntfPlugin initFromNetPlugin;
	//配置更新的周期
	private long period;
	
	/**
	 * 初始化
	 * 
	 * @param start 是否是启动以后第一次
	 */
	private void init(boolean start) {
		long nowModifyTime = getModifyTime();
		if(this.lastModifyTime<nowModifyTime){
			initFromNetPlugin.execute(Collections.synchronizedMap(new HashMap()));
			this.setLastModifyTime(nowModifyTime);
			this.lastModifyTime = nowModifyTime;
		}else if(start){
			initFromLocalPlugin.execute(Collections.synchronizedMap(new HashMap()));
		}
	}
	
	private long getLastModifyTime(){
		BufferedReader br = null;
		try {
			br = FileTools.getInputStream(timestampFilePath);
			return Long.parseLong(br.readLine());
		} catch (Exception e) {
			log.info(
					"Cann't get timestamp file ,maybe this's the first running, so will create a new one.",
					e);
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
				}
			}
		}
		return 0;
	}
	private boolean setLastModifyTime(long lastModifyTime){
		File file = new File(this.timestampFilePath);
		file.delete();
		BufferedWriter bw = null;
		try {
			bw = FileTools.getBufferedWriter(file);
			bw.write(Long.toString(lastModifyTime));
			bw.flush();
			return true;
		} catch (IOException e) {
			log.error("Cann't get bufferwriter for file :"+this.timestampFilePath);
		}finally{
			if(bw!=null){
				try {
					bw.close();
				} catch (IOException e) {
				}
			}
		}
		return false;
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.ifeng.common.conf.Configurable#config(com.ifeng.common.conf.ConfigRoot
	 * , java.lang.Object, org.w3c.dom.Element)
	 */
	@Override
	public Object config(ConfigRoot configRoot, Object parent, Element configEle)
			throws ConfigException {
		this.timestampFilePath = (String) configRoot.createChildObject(parent,
				configEle, "timestamp-file-path");
		this.initFromLocalPlugin = (IntfPlugin) configRoot.createChildObject(parent,
				configEle, "local-plugin");
		this.initFromNetPlugin = (IntfPlugin) configRoot.createChildObject(parent,
				configEle, "net-plugin");
		this.period = (Long) configRoot.createChildObject(parent,
				configEle, "period");
		Timer updateTimer = new Timer("updateTimer");
		updateTimer.schedule(new UpdateTask(), period, period);
		lastModifyTime =getLastModifyTime();
		if(lastModifyTime==0){
			init(false);
		}else{
			init(true);
		}
		return this;
	}
	
	private class UpdateTask extends TimerTask {
		@Override
		public void run() {
			init(false);
		}
	}
	public abstract long getModifyTime();
}
