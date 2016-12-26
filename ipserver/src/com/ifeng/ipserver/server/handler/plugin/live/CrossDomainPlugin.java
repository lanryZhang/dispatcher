package com.ifeng.ipserver.server.handler.plugin.live;

import java.io.IOException;
import java.util.Map;

import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.ParsingException;
import nu.xom.ValidityException;

import com.ifeng.common.plugin.core.abst.AbstLogicPlugin;
import com.ifeng.ipserver.server.handler.ServiceContext;
import com.ifeng.ipserver.server.message.HttpResponseMessage;

/**
 * <title> CrossDomainPlugin</title>
 * 
 * <pre>
 * 	    请求跨域文件的插件。
 * <br>
 * </pre>
 *
 * Copyright © 2012 Phoenix New Media Limited All Rights Reserved.
 * 
 * @author <a href="mailto:jl@ifeng.com">Jin Lin</a>
 */
public class CrossDomainPlugin extends AbstLogicPlugin{
	
	private String loadFile;
	
	
	public void setLoadFile(String loadFile) throws ParsingException, IOException {
      try{
		Document doc = new Builder().build(loadFile);
        String crossDomainInfo = (doc.toXML());
		this.loadFile = crossDomainInfo;
      }catch(Throwable e){
    	  e.printStackTrace();
      }
	}


	
	@Override
	public Object execute(Object o) {
		Map contextMap = (Map) o;
		HttpResponseMessage response = (HttpResponseMessage) contextMap
				.get(ServiceContext.RESPONSE_MESSAGE);
		response.handleRequest(loadFile, response, HttpResponseMessage.HTTP_STATUS_SUCCESS);
		return Boolean.TRUE;
	}

}


