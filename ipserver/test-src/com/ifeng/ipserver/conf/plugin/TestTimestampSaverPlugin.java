package com.ifeng.ipserver.conf.plugin;


import java.io.BufferedReader;
import java.util.HashMap;
import java.util.Map;
import com.ifeng.common.misc.FileTools;
import junit.framework.TestCase;

public class TestTimestampSaverPlugin extends TestCase{
	public void setUp() throws Exception{
		super.setUp();
		
	}
	public void tearDown() throws Exception{
		super.tearDown();
	}
	public void testPrinter(){
		TimestampSaverPlugin saver =  new TimestampSaverPlugin();
		saver.setLasttimeFilePath((TestTimestampSaverPlugin.class.getResource("timestampsaved").getFile()));
		saver.setModifyTimeKey("modifytime");
		Map context = new HashMap();
		context.put("modifytime", new Long(1000));
		Boolean result3 = (Boolean)saver.execute(context);
		assertTrue(result3.booleanValue());
		BufferedReader br;
		try {
			br = FileTools.getInputStream(TestFileListenerPlugin.class.getResource("timestampsaved").getFile());
			String line = br.readLine();
			assertEquals("1000",line);
		} catch (Exception e) {
			e.printStackTrace();
			assertTrue(false);
		}
	}
}
