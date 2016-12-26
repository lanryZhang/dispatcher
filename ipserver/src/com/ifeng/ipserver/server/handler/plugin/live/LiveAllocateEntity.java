package com.ifeng.ipserver.server.handler.plugin.live;

import java.io.OutputStream;

import com.ifeng.common.conf.ConfigException;
import com.ifeng.common.conf.ConfigRoot;
import com.ifeng.common.conf.Configurable;

import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Serializer;
/**
 * <title> LiveAllocateEntity </title>
 * 
 * <pre>
 * 	直播配置对应的Pojo，用于描述直播请求分配关系
 * 	
 * </pre>
 * 
 * Copyright © 2012 Phoenix New Media Limited All Rights Reserved.
 * 
 * @author <a href="mailto:jl@ifeng.com">Jin Lin</a>
 * @author <a href="mailto:jinmy@ifeng.com">Jin Mingyan</a>
 */

public class LiveAllocateEntity implements Configurable{
	private String netname;// 运营商名
	private String cdnid;// CDN服务商ID
	private String cdnname;// CDN服务商名
	private String chcode;// 频道代码
	private String chname;// 频道名称
	private String url;// 播放地址
	private int percent;// 占比，基数为100

	public Element getXML() {
		Element live = new Element("live");
		Element netnameEle = new Element("netname");
		netnameEle.appendChild(netname);
		Element cdnidEle = new Element("cdnid");
		cdnidEle.appendChild(cdnid);
		Element cdnnameEle = new Element("cdnname");
		cdnnameEle.appendChild(cdnname);
		Element chcodeEle = new Element("chcode");
		chcodeEle.appendChild(chcode);
		Element chnameEle = new Element("chname");
		chnameEle.appendChild(chname);
		Element percentEle = new Element("percent");
		percentEle.appendChild(Integer.toString(percent));
		Element urlEle = new Element("url");
		urlEle.appendChild(Integer.toString(percent));
		live.appendChild(netnameEle);
		live.appendChild(cdnidEle);
		live.appendChild(cdnnameEle);
		live.appendChild(chcodeEle);
		live.appendChild(chnameEle);
		live.appendChild(percentEle);
		live.appendChild(urlEle);
		return live;
	}

//	public LiveAllocateEntity(Element ele) {
//		netname = ele.getFirstChildElement("netname").getValue();
//		cdnid = ele.getFirstChildElement("cdnid").getValue();
//		cdnname = ele.getFirstChildElement("cdnname").getValue();
//		chcode = ele.getFirstChildElement("chcode").getValue();
//		chname = ele.getFirstChildElement("chname").getValue();
//		percent = Integer.parseInt(ele.getFirstChildElement("percent").getValue());
//		url = ele.getFirstChildElement("url").getValue();
//	}

	public static void format(OutputStream os, Document doc) throws Exception {
		Serializer serializer = new Serializer(os, "UTF-8");
		serializer.setIndent(4);
		serializer.setMaxLength(60);
		serializer.write(doc);
		serializer.flush();
	}

	public String getNetname() {
		return netname;
	}

	public void setNetname(String netname) {
		this.netname = netname;
	}

	public String getCdnid() {
		return cdnid;
	}

	public void setCdnid(String cdnid) {
		this.cdnid = cdnid;
	}

	public String getCdnname() {
		return cdnname;
	}

	public void setCdnname(String cdnname) {
		this.cdnname = cdnname;
	}

	public String getChcode() {
		return chcode;
	}

	public void setChcode(String chcode) {
		this.chcode = chcode;
	}

	public String getChname() {
		return chname;
	}

	public void setChname(String chname) {
		this.chname = chname;
	}

	public int getPercent() {
		return percent;
	}

	public void setPercent(int percent) {
		this.percent = percent;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	@Override
	public String toString() {
		return netname + " " + cdnid + " " + cdnname + " " + chcode + " "
				+ chname + " " + percent + " " + url;
	}

	@Override
	public Object config(ConfigRoot configRoot, Object parent,
			org.w3c.dom.Element configEle) throws ConfigException {
		this.cdnid = (String) configRoot.createChildObject(parent,
				configEle, "cdn-id");
		this.cdnname = (String) configRoot.createChildObject(parent,
				configEle, "cdn-name");
		this.chcode = (String) configRoot.createChildObject(parent,
				configEle, "ch-code");
		this.chname = (String) configRoot.createChildObject(parent,
				configEle, "ch-name");
		this.netname = (String) configRoot.createChildObject(parent,
				configEle, "net-name");
		this.percent = (Integer) configRoot.createChildObject(parent,
				configEle, "percent");
		this.url = (String) configRoot.createChildObject(parent,
				configEle, "url");
		return this;
	}
}
