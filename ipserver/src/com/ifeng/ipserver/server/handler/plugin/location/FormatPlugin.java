package com.ifeng.ipserver.server.handler.plugin.location;

import java.util.Map;

import com.ifeng.common.misc.Logger;
import com.ifeng.common.plugin.core.abst.AbstLogicPlugin;
import com.ifeng.ipserver.bean.Area;
import com.ifeng.ipserver.server.handler.ServiceContext;
import com.ifeng.ipserver.server.message.HttpRequestMessage;
import com.ifeng.ipserver.server.message.HttpResponseMessage;
import com.sdicons.json.mapper.JSONMapper;
import com.sdicons.json.mapper.MapperException;

/**
 * <title> FormatPlugin</title>
 * 
 * <pre>
 * 	    格式化查询到的区域信息，根据参数不同格式化为json格式或者普通文本格式。
 * <br>
 * </pre>
 *
 * Copyright © 2012 Phoenix New Media Limited All Rights Reserved.
 * 
 * @author <a href="mailto:jl@ifeng.com">Jin Lin</a>
 * @author <a href="mailto:jinmy@ifeng.com">Jin Mingyan</a>
 */
public class FormatPlugin extends AbstLogicPlugin{
	private static Logger log = Logger.getLogger(FormatPlugin.class);
	
	/* (non-Javadoc)
	 * @see com.ifeng.common.plugin.core.itf.IntfPlugin#execute(java.lang.Object)
	 */
	@Override
	public Object execute(Object o) {
		Map contextMap = (Map) o;

		HttpResponseMessage response = (HttpResponseMessage) contextMap
				.get(ServiceContext.RESPONSE_MESSAGE);
		HttpRequestMessage request = (HttpRequestMessage) contextMap
				.get(ServiceContext.REQUEST_MESSAGE);
		Area area = (Area) contextMap
				.get(ServiceContext.LOCATION_MESSAGE);
		if(area == null){
			response.handleRequest("Location Not Found", response,
					HttpResponseMessage.HTTP_STATUS_SUCCESS);
			return Boolean.TRUE;
		}
		String format = request.getParameter("format");
		
		if (null != format && "json".equals(format)) {
			JSONMapper mapper = new JSONMapper();
			String jsonResult = "";
			try {
				jsonResult = JSONMapper.toJSON(area).render(false);
			} catch (MapperException e) {
				log.error("Error When Convert to Json", e);
			}
			response.handleRequest(jsonResult, response,
					HttpResponseMessage.HTTP_STATUS_SUCCESS);
		} else {
			String paraIp = (String) contextMap
					.get(ServiceContext.PARAMETER_IP);

					String province = area.getProvince();
					String city = area.getCity();
					String sp = area.getNetName();
					response.handleRequest("ip=" + paraIp + "<br>province=" + province
							+ "<br>city=" + city + "<br>sp=" + sp, response,
							HttpResponseMessage.HTTP_STATUS_SUCCESS);
		}
		return Boolean.TRUE;
	}
	
}


