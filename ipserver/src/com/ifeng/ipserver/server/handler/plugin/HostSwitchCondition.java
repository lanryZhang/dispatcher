/**
 *
 */
package com.ifeng.ipserver.server.handler.plugin;

import java.util.Map;

import com.ifeng.common.misc.Logger;
import com.ifeng.common.plugin.core.abst.AbstLogicPlugin;
import com.ifeng.ipserver.server.handler.ServiceContext;
import com.ifeng.ipserver.server.message.HttpRequestMessage;


/**
 *<title>HostSwitchCondition<title>
 *<pre>
 *</pre>
 *
 * @author <a href="mailto:chengyong@ifeng.com">banban</a>
 *
 *  Copyright © 2014 Phoenix New Media Limited All Rights Reserved.
 */
public class HostSwitchCondition  extends AbstLogicPlugin{
	private static Logger log = Logger.getLogger(HostSwitchCondition.class);
	/* (non-Javadoc)
	 * @see com.ifeng.common.plugin.core.itf.IntfPlugin#execute(java.lang.Object)
	 */
	@Override
	public Object execute(Object o) {
		Map contextMap =(Map)o;
		HttpRequestMessage request =(HttpRequestMessage)contextMap.get(ServiceContext.REQUEST_MESSAGE);		
		String host = request.getHost();
		host = host.split(":")[0];
		//log.info(host);
		return host;
	}

}
