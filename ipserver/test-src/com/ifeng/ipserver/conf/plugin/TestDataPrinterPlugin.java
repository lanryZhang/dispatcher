package com.ifeng.ipserver.conf.plugin;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.StringTokenizer;

import com.ifeng.common.conf.ConfigRoot;
import com.ifeng.common.dm.DataManager;
import com.ifeng.common.misc.FileTools;
import com.ifeng.ipserver.bean.db.GovAndNetnameToGroup;
import com.ifeng.ipserver.bean.db.IpBase;

import junit.framework.TestCase;

public class TestDataPrinterPlugin extends TestCase{
	private ConfigRoot configRoot = null;
	public void setUp() throws Exception{
		super.setUp();
		configRoot = new ConfigRoot(TestDataPrinterPlugin.class.getResource("test_data_printer.xml"),System.getProperties());
		
	}
	public void tearDown() throws Exception{
		super.tearDown();
	}
	public void testPrinter(){
		DataPrinterPlugin plugin = (DataPrinterPlugin)configRoot.getValue("dataPrinter");
		List list = new ArrayList();
		for(int i=0;i<3;i++){
			IpBase ipBase = new IpBase();
			ipBase.setCity("city"+i);
			ipBase.setEndIp("endip"+i);
			ipBase.setNetname("netname"+i);
			ipBase.setProvince("province"+i);
			ipBase.setStartIp("startIp"+i);
			list.add(ipBase);
		}
		
		HashMap context = new HashMap();
		context.put("testlist", list);
		context.put("printFile", TestDataPrinterPlugin.class.getResource("test-file").getFile());
		Boolean result = (Boolean)plugin.execute(context);
		assertTrue(result.booleanValue());
		try {
			BufferedReader reader = FileTools.getInputStream(TestDataPrinterPlugin.class.getResource("test-file").getFile());
			try {
				for(int i=0;i<3;i++){
					IpBase ipBase = new IpBase();
					ipBase.setCity("city"+i);
					ipBase.setEndIp("endip"+i);
					ipBase.setNetname("netname"+i);
					ipBase.setProvince("province"+i);
					ipBase.setStartIp("startIp"+i);
					String line = reader.readLine();
					IpBase ipBase1 = new IpBase();
					StringTokenizer st = new StringTokenizer(line,",");
					ipBase1.setStartIp(st.nextToken());
					ipBase1.setEndIp(st.nextToken());
					ipBase1.setNetname(st.nextToken());
					ipBase1.setProvince(st.nextToken());
					ipBase1.setCity(st.nextToken());
					assertEquals(ipBase,ipBase1);
				}
				
			} catch (IOException e) {
				assertTrue(false);
			}
		} catch (FileNotFoundException e) {
			assertTrue(false);
		}
		
	}
}
