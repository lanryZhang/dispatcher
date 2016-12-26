package com.ifeng.ipserver.service.impl.node.plugin;

import java.util.HashMap;
import java.util.Map;

import com.ifeng.common.conf.ConfigRoot;

import junit.framework.TestCase;

public class TestAreaPlugin extends TestCase{
	public void testExcute(){
		ConfigRoot configRoot = new ConfigRoot(TestAreaPlugin.class.getResource("test_area_plugin.xml"),System.getProperties());
		AreaPlugin areaPlugin = (AreaPlugin)configRoot.getValue("areaPlugin");
		Map context = new HashMap();
		context.put("netName", "中国电信");
		context.put("province", "北京市");
		assertFalse((Boolean)areaPlugin.execute(context));
		context.put("netName", "中国电信");
		context.put("province", "天津市");
		assertFalse((Boolean)areaPlugin.execute(context));
		context.put("netName", "中国电信");
		context.put("province", "上海市");
		assertTrue((Boolean)areaPlugin.execute(context));
		context.put("netName", "中国电信");
		context.put("province", "北京");
		assertTrue((Boolean)areaPlugin.execute(context));
		context.put("netName", "中国移动");
		context.put("province", "北京市");
		assertTrue((Boolean)areaPlugin.execute(context));
	}
}
