package com.ifeng.ipserver.service.listener.plugin;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.net.InetAddress;

import org.w3c.dom.Element;

import com.ifeng.common.conf.ConfigException;
import com.ifeng.common.conf.ConfigRoot;
import com.ifeng.common.conf.Configurable;
import com.ifeng.common.misc.FileTools;
import com.ifeng.common.misc.Logger;
import com.ifeng.common.misc.NetAddressUtil;
import com.ifeng.common.plugin.core.abst.AbstLogicPlugin;
import com.ifeng.ipserver.service.email.SimpleEmailPlugin;
import com.ifeng.ipserver.tools.HttpTools;

/**
 * <title> DownloaderPlugin </title>
 * 
 * <pre>
 *  本类是一个“插件”， 此插件用于集成到一个责任链中，完成一个url链接取得一个文件下载到本地的能力。
 *  本类的配置方式为：
 *  &lt;... type="com.ifeng.ipserver.service.listener.plugin。DownloaderPlugin"&gt;
 *  	 &lt;url .../&gt;
 *  	 &lt;file-path .../&gt;
 *  	 &lt;conn-timeout .../&gt;
 *    &lt;read-timeout .../&gt;
 *  &lt/...&gt 
 * 其中：
 * 	url 
 * 		类型： String
 * 		用途：用于声明http链接地址，对应的能力为取得文件对应的url。
 * 	file-path
 * 		类型：String
 * 		用途：用户声明一个本地文件存储的路径
 *   conn-timeout
 * 		类型：Integer
 * 		用途：http连接超时时间
 *   read-timeout
 * 		类型：Integer
 * 		用途：http数据读取超时时间
 * </pre>
 * 
 * Copyright © 2012 Phoenix New Media Limited All Rights Reserved.
 * 
 * @author <a href="mailto:yudf@ifeng.com">Yu Dengfeng</a>
 * @author <a href="mailto:jinmy@ifeng.com">Jin Mingyan</a>
 */
public class DownloaderPlugin extends AbstLogicPlugin implements Configurable {
	private String url;
	private String filePath;
	private int connTimeout;
	private int readTimeout;
	private SimpleEmailPlugin emailPlugin;

	private static final Logger log = Logger.getLogger(DownloaderPlugin.class);

	@Override
	public Object execute(Object o) {
		PrintStream ps = null;
		try {
			String data = HttpTools.downLoad(url, connTimeout, readTimeout);
			(new File(filePath)).delete();
			ps = FileTools.getOutputStream(true, filePath);
			ps.write(data.getBytes());
			ps.flush();
			if (emailPlugin != null){
				emailPlugin.initSendEmailTimes(url,0);
			}
			return Boolean.TRUE;
		} catch (FileNotFoundException e) {
			log.error("Cann't get write file for:" + filePath, e);
			try {
				emailPlugin.setMsg("Error Message:IPS Url " + url + " Can't get connection </br>" + "from IP: "
						+NetAddressUtil.getLocalAddress("/"));
				emailPlugin.setSubject("error message from ips");
				emailPlugin.sendEmailLimit(url);
				
			} catch (Exception e2) {
			}
			return Boolean.FALSE;
		} catch (IOException e) {
			log.error("Cann't get data from " + url, e);

			try {
				emailPlugin.setMsg("Error Message:IPS Url " + url + " Can't get connection </br>" + "from IP: "
						+NetAddressUtil.getLocalAddress("/"));
				emailPlugin.setSubject("error message from ips");
				emailPlugin.sendEmailLimit(url);
			} catch (Exception e2) {
			}

			return Boolean.FALSE;
		} finally {
			if (ps != null) {
				ps.close();
			}
		}
	}

	@Override
	public Object config(ConfigRoot configRoot, Object parent, Element configEle) throws ConfigException {
		this.url = (String) configRoot.createChildObject(parent, configEle, "url");
		this.filePath = (String) configRoot.createChildObject(parent, configEle, "file-path");
		this.connTimeout = (Integer) configRoot.createChildObject(parent, configEle, "conn-timeout");
		this.readTimeout = (Integer) configRoot.createChildObject(parent, configEle, "read-timeout");
		this.emailPlugin = (SimpleEmailPlugin) configRoot.createChildObject(parent, configEle, "plugin");
		return this;
	}
	
	public SimpleEmailPlugin getEmailPlugin() {
		return emailPlugin;
	}

	public void setEmailPlugin(SimpleEmailPlugin emailPlugin) {
		this.emailPlugin = emailPlugin;
	}
	
}
