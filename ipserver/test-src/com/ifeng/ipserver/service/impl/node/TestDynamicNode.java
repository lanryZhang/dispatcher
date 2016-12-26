/*
package com.ifeng.ipserver.service.impl.node;

import com.ifeng.common.conf.ConfigRoot;

import junit.framework.TestCase;

public class TestDynamicNode extends TestCase{
	public void testExcute(){
		ConfigRoot configRoot = new ConfigRoot(TestDynamicNode.class.getResource("test_dynamic.xml"),System.getProperties());
		DynamicNode dynamicNode = (DynamicNode)configRoot.getValue("dynamicNode");
		int normalCount=0;
		int errorCount=0;
		int count =0;
		for(int i=0;i<2000000;i++){
			if(dynamicNode.getIpOrCname("中国联通", "山东省").equals("dilian")){
				errorCount++;
			}else{
				normalCount++;
			}
			
			
			count++;
			if(count%1000==0){
				try {
					Thread.currentThread().sleep(10);
				} catch (InterruptedException e) {
				}
			}
			if(count%1000000==0){
				System.out.println("normalCount is "+normalCount+" errorCount is "+errorCount);
			}
		}
		assertTrue(normalCount>900000);
		assertTrue(normalCount<1100000);
	}
}
*/
