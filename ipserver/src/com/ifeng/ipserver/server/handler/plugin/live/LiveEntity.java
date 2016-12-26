package com.ifeng.ipserver.server.handler.plugin.live;

/**
 *<title>LiveEntity<title>
 *<pre>
 *</pre>
 *
 * @author <a href="mailto:chengyong@ifeng.com">banban</a>
 *
 *  Copyright © 2014 Phoenix New Media Limited All Rights Reserved.
 */
public class LiveEntity {

	private String cndid;
	private String cid;
	private String url;
	private String overflow;
	private String realIp;
	public String getCndid() {
		return cndid;
	}
	public void setCndid(String cndid) {
		this.cndid = cndid;
	}
	public String getCid() {
		return cid;
	}
	public void setCid(String cid) {
		this.cid = cid;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}

	public String getOverflow() {
		return overflow;
	}

	public void setOverflow(String overflow) {
		this.overflow = overflow;
	}

	public String getRealIp() {
		return realIp;
	}

	public void setRealIp(String realIp) {
		this.realIp = realIp;
	}
}
