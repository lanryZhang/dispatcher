package com.ifeng.ipserver.conf.plugin;

import java.text.SimpleDateFormat;
import java.util.Map;

import org.w3c.dom.Element;

import com.ifeng.common.conf.ConfigException;
import com.ifeng.common.conf.ConfigRoot;
import com.ifeng.common.conf.Configurable;
import com.ifeng.common.plugin.core.abst.AbstLogicPlugin;
/**
 * <title> DatePrintPathGeneratorPlugin </title>
 * 
 * <pre>
 * 本类是一个“插件”， 此插件用于集成到一个责任链中，完成生成一个带时间后缀的打印路径的能力。
 * 本类的配置方式为：
 * &lt;... type="com.ifeng.ipserver.base.plugin。DatePrintPathGeneratorPlugin"&gt;
 * 	 &lt;base-path .../&gt;
 * 	 &lt;context-key .../&gt;
 * &lt/...&gt 
 *其中：
 *	base-path 
 *		类型：java.lang.String 
 *		用途：基础路径，插件会生成一个基础路径后面带有时间后缀的新路径，后缀格式为".yyyy-MM-dd(HH:mm:ss)"
 *	context-key
 *		 类型：java.lang.String
 *		用途: 将生成的路径放入上下文中使用的key值
 * </pre>
 * 
 * Copyright © 2012 Phoenix New Media Limited All Rights Reserved.
 * 
 * @author <a href="mailto:qilp@ifeng.com">Qi Lupeng</a>
 * @author <a href="mailto:jinmy@ifeng.com">Jin Mingyan</a>
 */
public class DatePrintPathGeneratorPlugin extends AbstLogicPlugin implements Configurable{
	//基础路径，转化后会在基础路径之后加上对应的后缀
	private String basePath;
	//放入上下文的键值，会将转化后的结果放入上下文中
	private String contextKey;
	/* (non-Javadoc)
	 * @see com.ifeng.common.plugin.core.itf.IntfPlugin#execute(java.lang.Object)
	 */
	@Override
	public Object execute(Object o) {
		SimpleDateFormat dateFm = new SimpleDateFormat(".yyyy-MM-dd(HH:mm:ss)");
		String dateTime = dateFm.format(new java.util.Date());
		Map context =(Map)o;
		context.put(contextKey, basePath+dateTime);
		return Boolean.TRUE;
	}
	
	/* (non-Javadoc)
	 * @see com.ifeng.common.conf.Configurable#config(com.ifeng.common.conf.ConfigRoot, java.lang.Object, org.w3c.dom.Element)
	 */
	@Override
	public Object config(ConfigRoot configRoot, Object parent, Element configEle)
			throws ConfigException {
		this.contextKey = (String)configRoot.createChildObject(parent, configEle, "context-key");
		this.basePath = (String)configRoot.createChildObject(parent, configEle, "base-path");
		return this;
	}
}
