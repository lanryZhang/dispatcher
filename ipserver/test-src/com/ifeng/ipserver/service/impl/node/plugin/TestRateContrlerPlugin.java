package com.ifeng.ipserver.service.impl.node.plugin;

import java.util.HashMap;
import java.util.Map;

import com.ifeng.common.conf.ConfigRoot;

import junit.framework.TestCase;


public class TestRateContrlerPlugin extends TestCase{
	public void testExcute(){
		ConfigRoot configRoot = new ConfigRoot(TestAreaPlugin.class.getResource("test_rate_plugin.xml"),System.getProperties());
		RateContrlerPlugin rateContrlerPlugin = (RateContrlerPlugin)configRoot.getValue("ratePlugin");
		Map context = new HashMap();
		context.put("nowRatePercent", 50);
		assertFalse((Boolean)rateContrlerPlugin.execute(context));
		context.put("nowRatePercent", 60);
		assertFalse((Boolean)rateContrlerPlugin.execute(context));
		context.put("nowRatePercent", 70);
		assertFalse((Boolean)rateContrlerPlugin.execute(context));
		
		int trueCount = 0;
		int falseCount = 0;
		context.put("nowRatePercent", 80);
		for(int i=0;i<100000;i++){
			boolean result = (Boolean)rateContrlerPlugin.execute(context);
			if(result){
				trueCount++;
			}else{
				falseCount++;
			}
		}
		System.out.println(trueCount+" "+falseCount);
		assertTrue((trueCount>=30000*0.95)&&(trueCount<=30000*1.05));
		
		trueCount = 0;
		falseCount = 0;
		context.put("nowRatePercent", 85);
		for(int i=0;i<100000;i++){
			boolean result = (Boolean)rateContrlerPlugin.execute(context);
			if(result){
				trueCount++;
			}else{
				falseCount++;
			}
		}
		System.out.println(trueCount+" "+falseCount);
		assertTrue((trueCount>=30000*0.95)&&(trueCount<=30000*1.05));
		
		
		trueCount = 0;
		falseCount = 0;
		context.put("nowRatePercent", 89);
		for(int i=0;i<100000;i++){
			boolean result = (Boolean)rateContrlerPlugin.execute(context);
			if(result){
				trueCount++;
			}else{
				falseCount++;
			}
		}
		System.out.println(trueCount+" "+falseCount);
		assertTrue((trueCount>=30000*0.95)&&(trueCount<=30000*1.05));
		
		rateContrlerPlugin = (RateContrlerPlugin)configRoot.getValue("ratePluginMax");
		trueCount = 0;
		falseCount = 0;
		context.put("nowRatePercent", 100);
		for(int i=0;i<100000;i++){
			boolean result = (Boolean)rateContrlerPlugin.execute(context);
			if(result){
				trueCount++;
			}else{
				falseCount++;
			}
		}
		System.out.println(trueCount+" "+falseCount);
		assertTrue((trueCount==100000));
		
	}
}
