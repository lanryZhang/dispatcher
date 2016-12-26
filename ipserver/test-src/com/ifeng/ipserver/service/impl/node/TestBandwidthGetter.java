/*
package com.ifeng.ipserver.service.impl.node;

import java.util.Timer;
import java.util.TimerTask;

import com.ifeng.ipserver.service.impl.node.bandwidth.BandwidthGetter;
import com.ifeng.ipserver.service.impl.rate.RateCalculator;

public class TestBandwidthGetter implements BandwidthGetter{
	private RateCalculator rateCalculator;
	public RateCalculator getRateCalculator() {
		return rateCalculator;
	}
	public void setRateCalculator(RateCalculator rateCalculator) {
		this.rateCalculator = rateCalculator;
		Timer updateRateTimer = new Timer("updateBandwidthTimer");
		updateRateTimer.schedule(new UpdateTask(), 5000, 5000);
	}
	*/
/*public int getBandwidth() {
		return ((int)(rateCalculator.getRate()*40*((Math.random()/10)+0.95)));
	}*//*

	private void log(){
		System.out.println("rate is "+rateCalculator.getRate());
	}
	private class UpdateTask extends TimerTask {
		public void run() {
			log();
		}
	}
	public static void main(String args[]){
		System.out.println(Integer.MAX_VALUE/60/1000);
	}
}
*/
