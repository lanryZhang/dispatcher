package com.ifeng.ipserver.service.impl.node.plugin;

import java.util.Map;

import org.w3c.dom.Element;

import com.ifeng.common.conf.ConfigException;
import com.ifeng.common.conf.ConfigRoot;
import com.ifeng.common.conf.Configurable;
import com.ifeng.common.plugin.core.abst.AbstLogicPlugin;
/**
 * <title> AreaPlugin</title>
 * 
 * <pre>
 * 	    区域判别的插件，用于过滤不需要带宽控制的区域
 * <br>
 * </pre>
 * * 配置方式为：
 * 
 * &lt;... type="com.ifeng.ipserver.service.impl.node.plugin。AreaPlugin"&gt;
 * 	 &lt;area-map .../&gt;
 * &lt/...&gt 
 *其中：
 *	area-map
 *		类型： Map key:String value:null
 *		用途：配置区域列表，不需要设置value 其中区域的各位为：“运营商省份” 需要注意要与inms中的命名规则统一
 *
 * Copyright © 2012 Phoenix New Media Limited All Rights Reserved.
 * 
 * @author <a href="mailto:yudf@ifeng.com">Yu Dengfeng</a>
 * @author <a href="mailto:jinmy@ifeng.com">Jin Mingyan</a>
 */
public class AreaPlugin extends AbstLogicPlugin implements Configurable{
	//不进行带宽控制的区域列表，用于某些只有固定节点的服务区域，或者要最大限度的保障服务质量的区域。
	private Map areaMap;
	
	@Override
	public Object execute(Object o) {
		Map context = (Map)o;
		String area = (String)context.get("netName") + (String)context.get("province");
		if(areaMap.containsKey(area)){
			return Boolean.FALSE;
		}
		return Boolean.TRUE;
	}

	/* (non-Javadoc)
	 * @see com.ifeng.common.conf.Configurable#config(com.ifeng.common.conf.ConfigRoot, java.lang.Object, org.w3c.dom.Element)
	 */
	@Override
	public Object config(ConfigRoot configRoot, Object parent, Element configEle)
			throws ConfigException {
		this.areaMap =  (Map) configRoot.createChildObject(this, configEle,
				"area-map", true);
		return this;
	}
}
