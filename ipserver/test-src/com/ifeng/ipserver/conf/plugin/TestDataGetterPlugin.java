package com.ifeng.ipserver.conf.plugin;

import java.util.HashMap;
import java.util.List;

import com.ifeng.common.conf.ConfigRoot;
import com.ifeng.common.dm.DataManager;
import com.ifeng.ipserver.bean.db.GovAndNetnameToGroup;

import junit.framework.TestCase;

public class TestDataGetterPlugin extends TestCase{
	private ConfigRoot configRoot = null;
	public void setUp() throws Exception{
		super.setUp();
		configRoot = new ConfigRoot(TestDataGetterPlugin.class.getResource("test_data_getter.xml"),System.getProperties());
		
		DataManager dm = (DataManager)configRoot.getValue("testdm");
		for(int i=0;i<3;i++){
			GovAndNetnameToGroup group = new GovAndNetnameToGroup();
			group.setGovId(i);
			group.setGroupId(i+1);
			group.setNetname(Integer.toString(i+2));
			dm.add(group, null);
		}
	}
	public void tearDown() throws Exception{
		super.tearDown();
		DataManager dm = (DataManager)configRoot.getValue("testdm");
		for(int i=0;i<3;i++){
			GovAndNetnameToGroup group = new GovAndNetnameToGroup();
			group.setGovId(i);
			group.setGroupId(i+1);
			group.setNetname(Integer.toString(i+2));
			dm.delete(group, null);
		}
	}
	public void testGetter(){
		DataGetterPlugin plugin = (DataGetterPlugin)configRoot.getValue("dataGetter");
		HashMap context = new HashMap();
		Boolean result = (Boolean)plugin.execute(context);
		assertTrue(result.booleanValue());
		List list = (List)context.get("test");
		assertEquals(3,list.size());
		for(int i=0;i<3;i++){
			GovAndNetnameToGroup group = new GovAndNetnameToGroup();
			group.setGovId(i);
			group.setGroupId(i+1);
			group.setNetname(Integer.toString(i+2));
			assertEquals(group,list.get(i));
		}
	}
}
