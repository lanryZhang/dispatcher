package com.ifeng.ipserver.service.impl.rate;

import com.ifeng.common.conf.ConfigException;
import com.ifeng.common.conf.ConfigRoot;
import com.ifeng.common.conf.Configurable;
import com.ifeng.common.misc.Logger;
import org.w3c.dom.Element;

import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class LiveRateCaculator implements Configurable {
	private static ConcurrentHashMap<String, Integer> statMap = new ConcurrentHashMap<String, Integer>();
	private static ConcurrentHashMap<String, Integer> rateMap = new ConcurrentHashMap<String, Integer>();
	private AtomicInteger requestNum = new AtomicInteger(0);
	private int increaseRate;
	private String key;
	private Timer timer;
	private int delay;
	private int period;
	private LimitedQueue<Integer> rateQueue;
	private static final Logger log = Logger.getLogger(LiveRateCaculator.class);
	
	public void stat(){
		requestNum.incrementAndGet();
//		synchronized (requestNum) {
//			requestNum++;
//		}
	}
	
	/**
	 * 有限长度队列，当达到设置的最大长度是，push方法执行完毕，会降队列中的head挤掉。
	 * @author zhanglr
	 *
	 * @param <T>
	 */
	private class LimitedQueue<T> implements Iterable<T>{
		private Node<T> head;
		private int limitedLength;
		private int size;
		
		private Node<T> tail;
		
		public LimitedQueue(int limitedLength){
			head = null;
			tail = null;
			this.limitedLength =limitedLength; 
			size =0;
		}
		
		public void push(T item){
			Node<T> t = new Node<T>();
			t.setItem(item);
			t.setNext(null);
			
			if (size == limitedLength){
				poll();
			}
			
			if (tail != null){
				tail.next = t;
				tail = t;
				
			}
			else {
				head = t;
				tail = t;
			}
			
			size++;
		}
		
		public T poll(){
			if (null != head){
				Node<T> t = head;
				head = head.next;
				size--;
				return t.getItem();
			}
			return null;
		}
		
		@SuppressWarnings("hiding")
		private class Node<T>{
			private T item;
			private Node<T> next;
			
			public T getItem() {
				return item;
			}
			public void setItem(T item) {
				this.item = item;
			}
			public Node<T> getNext() {
				return next;
			}
			public void setNext(Node<T> next) {
				this.next = next;
			}
		}
		
		private class Itr implements Iterator<T>{
			private Node<T> p;
			
			public Itr(){
				p = head;
			}
			
			@Override
			public boolean hasNext() {
				return null != p;
			}

			@Override
			public T next() {
				try {
					if (null != p){
						T t = p.getItem();
						p = p.getNext();
						return t;
					}
				} catch (Exception e) {
				}
				return null;
			}

			@Override
			public void remove() {
				
			}
		}
		public int getSize() {
			return size;
		}

		@Override
		public Iterator<T> iterator() {
			return new Itr();
		}
	}

	
	public void update() {
		int historyCount = 0;
		if (statMap.containsKey(key)){
			historyCount = statMap.get(key);
		}
		
		increaseRate = (requestNum.get() - historyCount) / (period / 1000);
		rateQueue.push(increaseRate);
		
		statMap.put(key, requestNum.get());
		for (Entry<String, Integer> itemEntry : statMap.entrySet()) {
			log.info("请求数 Key:"+itemEntry.getKey()+","+itemEntry.getValue());
		}
		
		rateMap.put(key, getAvgRate());
		requestNum.set(0);

		for (Entry<String, Integer> itemEntry : rateMap.entrySet()) {
			log.info("增长速率 Key:"+itemEntry.getKey()+","+itemEntry.getValue());
		}
		
		String t = "";
		
		for (int ie: rateQueue) {
			t+=ie+",";
		}
		log.info("rateQueue:" + t);
	}
	
	/**
	 * 计算平均速率
	 * @return
	 */
	private int getAvgRate(){
		int sum = 0;
		for (int el : rateQueue) {
			sum+=el;
		}
		if (rateQueue.getSize() != 0)
			return sum / rateQueue.getSize();
		return 0;
	}
	
	private class UpdateTask extends TimerTask{
		@Override
		public void run() {
			update();
		}
	}
	
	@Override
	public Object config(ConfigRoot root, Object parent, Element el) throws ConfigException {
		this.delay = (Integer) root.createChildObject(parent,
				el, "delay");
		this.period = (Integer) root.createChildObject(parent,
				el, "period");
		this.key = (String) root.createChildObject(parent,
				el, "chid");
		timer = new Timer("updateLiveRate");
		rateQueue = new LimitedQueue<Integer>(6);
		timer.schedule(new UpdateTask(), delay, period);
		
		return this;
	}

	public int getIncreaseRate() {
		return increaseRate;
	}

	public void setIncreaseRate(int increaseRate) {
		this.increaseRate = increaseRate;
	}

	public static ConcurrentHashMap<String, Integer> getRateMap() {
		return rateMap;
	}

	public static ConcurrentHashMap<String, Integer> getStatMap() {
		return statMap;
	}
	
	public Integer getRequestNum() {
		return requestNum.get();
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}
}
