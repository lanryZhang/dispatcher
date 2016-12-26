package com.ifeng.ipserver.service.impl.node.bandwidth;

import java.net.InetAddress;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.w3c.dom.Element;

import com.ifeng.common.conf.ConfigException;
import com.ifeng.common.conf.ConfigRoot;
import com.ifeng.common.conf.Configurable;
import com.ifeng.common.misc.Logger;
import com.ifeng.common.misc.NetAddressUtil;
import com.ifeng.ipserver.service.email.SimpleEmailPlugin;
import com.ifeng.ipserver.tools.HttpTools;

/**
 * @author :chenyong
 * @version 1.0
 * @date 2013-3-5
 */

public class BandwidthUpdate implements Configurable {

	private static Logger log = Logger.getLogger(BandwidthUpdate.class);

	private Map bandwidthMap;
	private String url;
	private int connTimeout;
	private int readTimeout;
	private int period;
	private String type;
	private SimpleEmailPlugin emailPlugin;

	public void init() {

		JSONArray ja = getBandwidth();
		if (null != ja) {

			for (int i = 0; i < ja.size(); i++) {
				JSONObject jo = (JSONObject) ja.get(i);
				if (jo.getString("TYPE").equals(type)) {
					// log.info("key="+jo.getString("IDCNAME")+"  value="+jo.getString("LASTVALUE"));
					bandwidthMap.put(jo.getString("IDCNAME"), jo.getString("LASTVALUE"));
				}
			}
		}
	}

	public JSONArray getBandwidth() {

		JSONArray bwJsonArr = null;
		String bandwidth = null;
		try {
			bandwidth = HttpTools.downLoad(url, connTimeout, readTimeout);
			bwJsonArr = JSONArray.fromObject(bandwidth);
			if (emailPlugin != null) {
				emailPlugin.initSendEmailTimes(url, 0);
			}
		} catch (Throwable e) {
			try {
				emailPlugin.setMsg("Error Message:IPS Url " + url + " Can't get connection </br>" + "from IP: " + NetAddressUtil.getLocalAddress("/"));
				emailPlugin.setSubject("error message from ips");
				emailPlugin.sendEmailLimit(url);
			} catch (Exception e2) {
				// e2.printStackTrace();
			}
			log.error("get bandwidth error" + e);
		}
		return bwJsonArr;
	}

	@Override
	public Object config(ConfigRoot configRoot, Object parent, Element configEle) throws ConfigException {
		this.bandwidthMap = (Map) configRoot.createChildObject(parent, configEle, "bandwidth-map");
		this.connTimeout = (Integer) configRoot.createChildObject(parent, configEle, "conn-timeout");
		this.readTimeout = (Integer) configRoot.createChildObject(parent, configEle, "read-timeout");
		this.type = (String) configRoot.createChildObject(parent, configEle, "typeValue");
		this.period = (Integer) configRoot.createChildObject(parent, configEle, "period");
		this.url = (String) configRoot.createChildObject(parent, configEle, "url");
		this.emailPlugin = (SimpleEmailPlugin) configRoot.createChildObject(parent, configEle, "plugin");
		Timer bandwidthUpdate = new Timer("bandwidthUpdate");
		bandwidthUpdate.schedule(new BandwidthUpdateTask(), period, period);
		init();
		return this;
	}

	public SimpleEmailPlugin getEmailPlugin() {
		return emailPlugin;
	}

	public void setEmailPlugin(SimpleEmailPlugin emailPlugin) {
		this.emailPlugin = emailPlugin;
	}

	public class BandwidthUpdateTask extends TimerTask {

		@Override
		public void run() {
			init();
		}
	}

}
