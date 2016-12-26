package com.ifeng.ipserver.service.impl.rate;

import com.ifeng.common.conf.ConfigRoot;

import junit.framework.TestCase;

public class TestRateCalculator extends TestCase{
	public void testRate(){
		ConfigRoot configRoot = new ConfigRoot(TestRateCalculator.class.getResource("test_rate.xml"),System.getProperties());
		RateCalculator rateCalculator = (RateCalculator)configRoot.getValue("rateCalculator");
		assertEquals(0,rateCalculator.getRate());
		rateCalculator.stat();
		try {
			Thread.currentThread().sleep(1000);
		} catch (InterruptedException e) {
		}
		assertEquals(1,rateCalculator.getRate());
		for(int i=0;i<999;i++){
			rateCalculator.stat();
		}
		try {
			Thread.currentThread().sleep(1000);
		} catch (InterruptedException e) {
		}
		assertEquals(500,rateCalculator.getRate());
		for(int i=0;i<1000;i++){
			rateCalculator.stat();
		}
		try {
			Thread.currentThread().sleep(1000);
		} catch (InterruptedException e) {
		}
		assertEquals(666,rateCalculator.getRate());
		for(int i=0;i<1000;i++){
			rateCalculator.stat();
		}
		try {
			Thread.currentThread().sleep(1000);
		} catch (InterruptedException e) {
		}
		assertEquals(750,rateCalculator.getRate());
	}
}
