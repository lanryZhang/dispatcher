package com.ifeng.ipserver.conf.plugin;

import java.util.Map;

import org.w3c.dom.Element;

import com.ifeng.common.conf.ConfigException;
import com.ifeng.common.conf.ConfigRoot;
import com.ifeng.common.conf.Configurable;
import com.ifeng.common.dm.DataManager;
import com.ifeng.common.dm.DataManagerException;
import com.ifeng.common.dm.QueryResult;
import com.ifeng.common.misc.Logger;
import com.ifeng.common.plugin.core.abst.AbstLogicPlugin;
/**
 * <title> DataGetterPlugin </title>
 * 
 * <pre>
 * 本类是一个“插件”， 此插件用于集成到一个责任链中，完成从数据库中取得一个结果集的能力。
 * 本类的配置方式为：
 * &lt;... type="com.ifeng.ipserver.base.plugin。DataGetterPlugin"&gt;
 * 	 &lt;data-manager .../&gt;
 * 	 &lt;context-key .../&gt;
 * 	 &lt;param-map .../&gt;
 * &lt/...&gt 
 *其中：
 *	data-manager 
 *		类型： com.ifeng.common.dm.DataManager的实现
 *		用途：用于声明一个获取数据对应的数据库映射，包括对应的数据库位置和表，具体请参见具体的实现类。
 *	context-key
 *		 类型：java.lang.String
 *		用途：从数据库中取出的数据，将被转化为一个list 放入上下文中（上下文是指调用插件对应的责任链传递数据所使用的数据总线）
 *			context-key是指上下文中该list对应的key值（默认的上下文类型为Map)
 *	param-map
 *		类型：java.util.Map
 *		用途：用于传递参数，具体的规则请参见com.ifeng.common.dm.DataManager的query方法
 * </pre>
 * 
 * Copyright © 2012 Phoenix New Media Limited All Rights Reserved.
 * 
 * @author <a href="mailto:qilp@ifeng.com">Qi Lupeng</a>
 * @author <a href="mailto:jinmy@ifeng.com">Jin Mingyan</a>
 */
public class DataGetterPlugin extends AbstLogicPlugin implements Configurable{
	private static final Logger log = Logger.getLogger(DataGetterPlugin.class);
	// 数据存储
	private DataManager dataManager;
	// 放入上下文中对应的key值
	private String contextKey;
	// 参数
	private Map paramMap;
	
	/* (non-Javadoc)
	 * @see com.ifeng.common.plugin.core.itf.IntfPlugin#execute(java.lang.Object)
	 */
	@Override
	public Object execute(Object o) {
		try {
			if(log.isDebugEnabled()){
				log.debug("start query "+contextKey+" from db.");
			}
			QueryResult queryResult = dataManager.query(paramMap,null);
			if(log.isDebugEnabled()){
				log.debug("end query "+contextKey+" from db, find "+queryResult.getRowCount()+" items form db.");
			}
			((Map)(o)).put(contextKey, queryResult.getData(0, queryResult.getRowCount()));
			return Boolean.TRUE;
		} catch (DataManagerException e) {
			log.error("Cann't get data from db,key is "+contextKey,e);
		}
		return Boolean.FALSE;
	}

	/* (non-Javadoc)
	 * @see com.ifeng.common.conf.Configurable#config(com.ifeng.common.conf.ConfigRoot, java.lang.Object, org.w3c.dom.Element)
	 */
	@Override
	public Object config(ConfigRoot configRoot, Object parent, Element configEle)
			throws ConfigException {
		this.dataManager = (DataManager)configRoot.createChildObject(parent, configEle, "data-manager");
		this.contextKey = (String)configRoot.createChildObject(parent, configEle, "context-key");
		this.paramMap = (Map)configRoot.createChildObject(parent, configEle, "param-map");
		return this;
	}
}
