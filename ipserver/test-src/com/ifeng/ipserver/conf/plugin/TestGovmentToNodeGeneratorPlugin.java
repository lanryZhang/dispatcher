package com.ifeng.ipserver.conf.plugin;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import com.ifeng.common.conf.ConfigRoot;
import com.ifeng.common.dm.DataManager;
import com.ifeng.common.misc.FileTools;
import com.ifeng.common.plugin.core.itf.IntfPlugin;
import com.ifeng.ipserver.bean.AreaToCname;
import com.ifeng.ipserver.bean.db.GovAndNetnameToGroup;
import com.ifeng.ipserver.bean.db.IpBase;

import junit.framework.TestCase;

public class TestGovmentToNodeGeneratorPlugin extends TestCase{
	private ConfigRoot configRoot = null;
	public void setUp() throws Exception{
		super.setUp();
		configRoot = new ConfigRoot(TestGovmentToNodeGeneratorPlugin.class.getResource("test_govmentToNode_generator.xml"),System.getProperties());
		
	}
	public void tearDown() throws Exception{
		super.tearDown();
	}
	public void testGenerator(){
		Map contextMap = (Map)configRoot.getValue("contextMap");
		IntfPlugin plugin = (IntfPlugin)configRoot.getValue("govmentToNodeGenerator");
		Boolean result = (Boolean)plugin.execute(contextMap);
		assertTrue(result.booleanValue());
		List resultList = (List)contextMap.get("govmentToNodeList");
		assertEquals(8,resultList.size());
		AreaToCname areaToCname = new AreaToCname();
		areaToCname.setCity("朝阳区");
		areaToCname.setIpOrCname("1.1.1.1");
		areaToCname.setNetName("中国电信");
		areaToCname.setProvince("北京市");
		assertEquals(areaToCname,resultList.get(0));
	}
}
