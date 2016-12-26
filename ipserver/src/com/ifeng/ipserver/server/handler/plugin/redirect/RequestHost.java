package com.ifeng.ipserver.server.handler.plugin.redirect;

import org.w3c.dom.Element;

import com.ifeng.common.conf.ConfigException;
import com.ifeng.common.conf.ConfigRoot;
import com.ifeng.common.conf.Configurable;


/**
 *<title>RequestHost<title>
 *<pre>
 *</pre>
 *
 * @author <a href="mailto:chengyong@ifeng.com">banban</a>
 *
 *  Copyright © 2014 Phoenix New Media Limited All Rights Reserved.
 */
public class RequestHost implements Configurable{
	
	private int httpStatus;
	private int threshold;
	
	@Override
	public Object config(ConfigRoot configRoot, Object parent, Element configEle)
			throws ConfigException {
		this.httpStatus =   (Integer) configRoot.createChildObject(this, configEle,
				"http-status", true);
		this.threshold =   (Integer) configRoot.createChildObject(this, configEle,
				"threshold", true);
		return this;
	}
	
	
	public int getHttpStatus() {
		return httpStatus;
	}
	public void setHttpStatus(int httpStatus) {
		this.httpStatus = httpStatus;
	}
	public int getThreshold() {
		return threshold;
	}
	public void setThreshold(int threshold) {
		this.threshold = threshold;
	}

	

}
