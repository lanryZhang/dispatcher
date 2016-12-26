package com.ifeng.ipserver.service.listener;

import java.io.IOException;
import java.net.InetAddress;
import org.w3c.dom.Element;

import com.ifeng.common.conf.ConfigException;
import com.ifeng.common.conf.ConfigRoot;
import com.ifeng.common.misc.Logger;
import com.ifeng.common.misc.NetAddressUtil;
import com.ifeng.ipserver.service.email.SimpleEmailPlugin;
import com.ifeng.ipserver.tools.HttpTools;
/**
 * <title> HttpFileTimeStampListener</title>
 * 
 * <pre>
 * 用于监听一个http链接对应的时间戳，与本地的时间戳对比，进而决定是否执行某一种更新配置的业务逻辑。
 * 配置方式为：
 * 
 * &lt;... type="com.ifeng.ipserver.service.listener。HttpFileTimeStampListener"&gt;
 * 	 &lt;timestamp-file-path .../&gt;
 * 	 &lt;local-plugin .../&gt;
 * 	 &lt;net-plugin .../&gt;
 *   &lt;period .../&gt;
 *   &lt;http-file-url .../&gt;
 *   &lt;conn-timeout .../&gt;
 *   &lt;read-timeout .../&gt;
 * &lt/...&gt 
 *其中：
 *	timestamp-file-path
 *		类型： String
 *		用途：本地存储上次更新时间的文件对应的路径。
 *	local-plugin
 *		类型：com.ifeng.common.plugin.core.itf.IntfPlugin
 *		用途：用本地存储的配置文件初始化的业务流程，使用插件结构来描述。
 *	net-plugin
 *		类型：com.ifeng.common.plugin.core.itf.IntfPlugin
 *		用途：用远程存储的配置文件初始化的业务流程，使用插件结构来描述。
 *  http-file-url
 *		类型：String
 *		用途：访问远程文件的url.
 *  conn-timeout
 *		类型：Integer
 *		用途：http连接超时时间
 *  read-timeout
 *		类型：Integer
 *		用途：http数据读取超时时间
 * <br>
 * </pre>
 * 
 * Copyright © 2012 Phoenix New Media Limited All Rights Reserved.
 * 
 * @author <a href="mailto:yudf@ifeng.com">Yu Dengfeng</a>
 * @author <a href="mailto:jinmy@ifeng.com">Jin Mingyan</a>
 */
public class HttpFileTimeStampListener extends AbstractTimeStampListener{
	private static final Logger log = Logger.getLogger(HttpFileTimeStampListener.class);
	private String httpFileUrl;
	private int connTimeout;
	private int readTimeout;
	private SimpleEmailPlugin emailPlugin;
	
	@Override
	public long getModifyTime() {
		try {
			long res = Long.parseLong(HttpTools.downLoad(httpFileUrl, connTimeout, readTimeout));
			if (emailPlugin != null){
				emailPlugin.initSendEmailTimes(httpFileUrl,0);
			}
			return res;
		} catch (IOException e) {
			try {
				emailPlugin.setMsg("Error Message:IPS Url " + httpFileUrl + " Can't get connection </br>" + "from IP: "
					+NetAddressUtil.getLocalAddress("/"));
				emailPlugin.setSubject("error message from ips");
				emailPlugin.sendEmailLimit(httpFileUrl);
				log.info("email has sent successfully,"+httpFileUrl);
			} catch (Exception e2) {
			}
			log.error("Cann't get url for:"+httpFileUrl,e);
			return -1;
		}
	}
	@Override
	public Object config(ConfigRoot configRoot, Object parent, Element configEle)
			throws ConfigException {
		this.httpFileUrl = (String) configRoot.createChildObject(parent,
				configEle, "http-file-url");
		this.connTimeout = (Integer) configRoot.createChildObject(parent,
				configEle, "conn-timeout");
		this.readTimeout = (Integer) configRoot.createChildObject(parent,
				configEle, "read-timeout");
		this.emailPlugin = (SimpleEmailPlugin) configRoot.createChildObject(parent, configEle, "plugin");
		super.config(configRoot, parent, configEle);
		return this;
	}
	public SimpleEmailPlugin getEmailPlugin() {
		return emailPlugin;
	}
	public void setEmailPlugin(SimpleEmailPlugin emailPlugin) {
		this.emailPlugin = emailPlugin;
	}
}
