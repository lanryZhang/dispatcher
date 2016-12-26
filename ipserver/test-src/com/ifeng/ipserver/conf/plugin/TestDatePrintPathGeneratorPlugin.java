package com.ifeng.ipserver.conf.plugin;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;

import com.ifeng.common.conf.ConfigRoot;
import com.ifeng.common.dm.DataManager;
import com.ifeng.ipserver.bean.db.GovAndNetnameToGroup;

import junit.framework.TestCase;

public class TestDatePrintPathGeneratorPlugin extends TestCase{
	private ConfigRoot configRoot = null;
	public void setUp() throws Exception{
		super.setUp();
		configRoot = new ConfigRoot(TestDatePrintPathGeneratorPlugin.class.getResource("test_date_generator.xml"),System.getProperties());
	}
	public void testGenerator(){
		DatePrintPathGeneratorPlugin plugin = (DatePrintPathGeneratorPlugin)configRoot.getValue("dateGenerator");
		HashMap context = new HashMap();
		Boolean result = (Boolean)plugin.execute(context);
		assertTrue(result.booleanValue());
		SimpleDateFormat dateFm = new SimpleDateFormat(".yyyy-MM-dd(HH:mm:ss)");
		String dateTime = dateFm.format(new java.util.Date());
		String path = (String)context.get("test");
		assertTrue(path.startsWith("areaNode."));
		assertTrue(path.endsWith(dateTime));
	}
}
