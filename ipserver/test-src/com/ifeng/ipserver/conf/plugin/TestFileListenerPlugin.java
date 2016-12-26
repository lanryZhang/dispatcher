package com.ifeng.ipserver.conf.plugin;


import java.util.HashMap;

import com.ifeng.common.conf.ConfigRoot;


import junit.framework.TestCase;

public class TestFileListenerPlugin extends TestCase{
	public void setUp() throws Exception{
		super.setUp();
		
	}
	public void tearDown() throws Exception{
		super.tearDown();
	}
	public void testPrinter(){
		FileListenerPlugin listener1 =new FileListenerPlugin();
		listener1.setListenPath(TestFileListenerPlugin.class.getResource("test-file").getFile());
		listener1.setLasttimeFilePath(TestFileListenerPlugin.class.getResource("timestamp0").getFile());
		listener1.setModifyTimeKey("modifytime");
		Boolean result = (Boolean)listener1.execute(new HashMap());
		assertTrue(result.booleanValue());
		FileListenerPlugin listener2 = new FileListenerPlugin();
		listener2.setListenPath(TestFileListenerPlugin.class.getResource("test-file").getFile());
		listener2.setLasttimeFilePath(TestFileListenerPlugin.class.getResource("timestampfull").getFile());
		listener2.setModifyTimeKey("modifytime");
		Boolean result2 = (Boolean)listener2.execute(new HashMap());
		assertFalse(result2.booleanValue());
		FileListenerPlugin listener3 =  new FileListenerPlugin();
		listener3.setListenPath(TestFileListenerPlugin.class.getResource("test-file").getFile());
		listener3.setLasttimeFilePath("adsflkaj");
		listener2.setModifyTimeKey("modifytime");
		Boolean result3 = (Boolean)listener3.execute(new HashMap());
		assertTrue(result3.booleanValue());
	}
}
