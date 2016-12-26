package com.ifeng.ipserver.service.impl.node.bandwidth;

import java.io.IOException;
import org.w3c.dom.Element;
import com.ifeng.common.conf.ConfigException;
import com.ifeng.common.conf.ConfigRoot;
import com.ifeng.common.conf.Configurable;
import com.ifeng.common.misc.Logger;
import com.ifeng.ipserver.tools.HttpTools;

/**
 * 获取节点的当前带宽占用
  @author :chenyong
  @version 1.0
  @date 2012-8-16
 */

public class BandwidthGetterImpl implements BandwidthGetter,Configurable{
	private static Logger log = Logger.getLogger(BandwidthGetterImpl.class);
	private String url;
	private int connTimeout;
	private int readTimeout;
	private int lastband=-1;

	@Override
	public long getBandwidth() {
		String bandString = null;
		int band = 0;
        try {
			bandString = HttpTools.downLoad(url, connTimeout, readTimeout);
			int outindex = bandString.indexOf("<th scope=\"row\">Out</th>");
			String firstIntercept = bandString.substring(outindex);
			String[] result = firstIntercept.split("\\n");
			String str = result[3].trim();
			int index_kb = str.indexOf("kb/s");
			int index_mb = str.indexOf("Mb/s");
			String secondIntercept ="0";
			if(index_kb!=-1){
				secondIntercept = str.substring(4,index_kb).trim();
				float bandb = Float.valueOf(secondIntercept);	
				band = (int)bandb;
			}
			if(index_mb!=-1){
				secondIntercept = str.substring(4,index_mb).trim();
				float bandb = Float.valueOf(secondIntercept);	
				band = (int)(bandb*1000);
			}
			
			//String secondIntercept = firstIntercept.substring(0, index_kb).trim();
			//log.info("Get bandwidth from "+url+".The bandwidth is "+band+"kb/s");
		} catch (IOException e) {
			//log.info("Get bandwidth from "+url+" error");
		}catch(StringIndexOutOfBoundsException e){
			//log.info("Get bandwidth from "+url+" errorinfo is:StringIndexOutOfBoundsException");
		}
        if(band == 0 && lastband > 0){
        	band = lastband;
        }else{
        	lastband = band;
        }
		return band;
	}
	
	@Override
	public Object config(ConfigRoot configRoot, Object parent, Element configEle)
			throws ConfigException {
		this.url = (String)configRoot.createChildObject(parent, configEle, "url");
		this.connTimeout = (Integer)configRoot.createChildObject(parent, configEle, "conn-timeout");
		this.readTimeout = (Integer)configRoot.createChildObject(parent, configEle, "read-timeout");
		return this;
	} 

}
