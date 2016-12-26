package com.ifeng.ipserver.server.handler.plugin.location;

import com.ifeng.common.conf.ConfigException;
import com.ifeng.common.conf.ConfigRoot;
import com.ifeng.common.conf.Configurable;
import com.ifeng.common.misc.Logger;
import com.ifeng.common.plugin.core.abst.AbstLogicPlugin;
import com.ifeng.ipserver.bean.Area;
import com.ifeng.ipserver.server.handler.ServiceContext;
import com.ifeng.ipserver.server.handler.plugin.live.LiveEntity;
import com.ifeng.ipserver.server.message.HttpRequestMessage;
import com.ifeng.ipserver.server.message.HttpResponseMessage;
import com.ifeng.ipserver.service.impl.node.SpecialNode;
import com.ifeng.ipserver.service.intf.AreaNodeManager;
import com.sdicons.json.mapper.JSONMapper;
import com.sdicons.json.mapper.MapperException;
import org.w3c.dom.Element;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 直播请求节点过滤
 * 当直播请求进来时，判断是否为特殊节点，如果为特殊节点，直接返回特殊节点设置内容，不进行原始逻辑的运行；如果不是特殊节点，则进入原始逻辑；
 * 其中原始逻辑指的是，一般节点的逻辑；
 * 返回值为特殊节点设置的cdnId和url
 *
 * Created by gutc on 2015/10/13.
 */
public class IpFilterLivePlugin extends AbstLogicPlugin implements Configurable {

    private static Logger log = Logger.getLogger(IpFilterLivePlugin.class);
    private Map specialNodeMap;
    private AreaNodeManager areaNodeManager;

    @Override
    public Object execute(Object o) {
        Map contextMap = (Map) o;
        HttpRequestMessage request = (HttpRequestMessage) contextMap
                .get(ServiceContext.REQUEST_MESSAGE);
        HttpResponseMessage response = (HttpResponseMessage) contextMap
                .get(ServiceContext.RESPONSE_MESSAGE);
        Area area = (Area) contextMap.get(ServiceContext.LOCATION_MESSAGE);
        String channelCode = request.getParameter("cid");
        String ip = (String) contextMap.get(ServiceContext.REMOTE_IP);

        String node = null;
        LiveEntity entity = null;
        String netName = null;
        if (area != null) {
            try {
                node = areaNodeManager.getNodeByArea(area,0,false);
                netName = area.getNetName();
            } catch (Exception e) {
                log.error("Got Exception When areaNodeManager.getNodeByArea", e);
            }
        }
        if(node!=null){
            if(specialNodeMap.containsKey(node)){
                SpecialNode specialNode = (SpecialNode) specialNodeMap.get(node);
                entity = new LiveEntity();
                entity.setCndid(specialNode.getCdnId());
                entity.setUrl(specialNode.getUrlLive());
                entity.setCid(channelCode);
            }
        }
        if(entity!=null){
             log.info("Special Live Service:remoteip is " + ip + " node is special " + node+" target url is "+ entity.getUrl());
             this.handleResponse(netName, request, response, ip, entity);
             return Boolean.FALSE;
        }
        return Boolean.TRUE;
    }

    private boolean handleResponse(String sp, HttpRequestMessage request,
                                   HttpResponseMessage response, String ip,
                                   LiveEntity entity) {
        if (null == entity) {
            log.info("LiveAllocation Service: ip=" + ip + " channel code="
                    + request.getParameter("cid")
                    + " Live Allocation no channel info");
            response.handleRequest("Live Allocation sp info Not Found",
                    response, HttpResponseMessage.HTTP_STATUS_NOT_FOUND);
            return false;
        } else {
            Map<String, String> result = new HashMap<String, String>();
            result.put("cid", entity.getCid());
            result.put("link", entity.getUrl());
            result.put("cdnid", entity.getCndid());
            result.put("netname", sp);

            JSONMapper mapper = new JSONMapper();
            String jsonResult = "";
            try {
                jsonResult = JSONMapper.toJSON(result).render(false);
            } catch (MapperException e) {
                log.error("Error When Convert to Json", e);
            }

            response.setLastModified(new SimpleDateFormat(
                    "EEE, dd MMM yyyy HH:mm:ss zzz").format(new Date()));

            Calendar tenYearLater = Calendar.getInstance();
            tenYearLater.add(Calendar.YEAR, 10);//
            response.setExpires(new SimpleDateFormat(
                    "EEE, dd MMM yyyy HH:mm:ss zzz").format(tenYearLater
                    .getTime()));

            response.setCacheControl("max-age=315360000");
            response.handleRequest(jsonResult, response,
                    HttpResponseMessage.HTTP_STATUS_SUCCESS);

            log.info("LiveAllocation Service: remote ip=" + ip +" is special node"
                    + " channel code=" + entity.getCid()
                    + " sp: " + sp + " return: "
                    +entity.getUrl()+" cdn: "+entity.getCndid());
            return true;
        }
    }

    @Override
    public Object config(ConfigRoot configRoot, Object o, Element element) throws ConfigException {
        this.specialNodeMap =  (Map) configRoot.createChildObject(o, element, "special-node-map");
        this.areaNodeManager = (AreaNodeManager) configRoot.createChildObject(o, element, "area-node-manager",true);
        return this;
    }


}
