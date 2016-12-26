package com.ifeng.ipserver.server.handler.plugin.location;

import com.ifeng.common.conf.ConfigException;
import com.ifeng.common.conf.ConfigRoot;
import com.ifeng.common.conf.Configurable;
import com.ifeng.common.misc.Logger;
import com.ifeng.common.plugin.core.abst.AbstLogicPlugin;
import com.ifeng.ipserver.bean.Area;
import com.ifeng.ipserver.server.handler.ServiceContext;
import com.ifeng.ipserver.server.message.HttpRequestMessage;
import com.ifeng.ipserver.server.message.HttpResponseMessage;
import com.ifeng.ipserver.service.impl.node.SpecialNode;
import com.ifeng.ipserver.service.intf.AreaNodeManager;
import org.w3c.dom.Element;

import java.util.Map;

/**
 * Created by gutc on 2015/10/16.
 */
public class IpFilterLive3GPlugin extends AbstLogicPlugin implements Configurable {
    private static Logger log = Logger.getLogger(IpFilterLive3GPlugin.class);
    private Map specialNodeMap;
    private AreaNodeManager areaNodeManager;

    @Override
    public Object execute(Object o) {
        Map contextMap = (Map) o;
        HttpRequestMessage request = (HttpRequestMessage) contextMap
                .get(ServiceContext.REQUEST_MESSAGE);
        HttpResponseMessage response = (HttpResponseMessage) contextMap
                .get(ServiceContext.RESPONSE_MESSAGE);
        String remoteIp = (String) contextMap.get(ServiceContext.REMOTE_IP);
        Area area = (Area) contextMap.get(ServiceContext.LOCATION_MESSAGE);
        String context = request.getContext();
        String node = null;
        String targetUrl = null;
        if (area!=null){
            try {
                node = areaNodeManager.getNodeByArea(area, 0, false);
            } catch (Exception e) {
                log.info("live3G remoteIp="+remoteIp+" get node error"+e);
            }
        }
        if(node!=null){
            if(specialNodeMap.containsKey(node)){
                SpecialNode specialNode = (SpecialNode) specialNodeMap.get(node);
                targetUrl = specialNode.getUrl3G();
            }
        }
        if(targetUrl!=null){
            log.info("Redirect Service: IP: " + remoteIp+" special node "+node+" Redirect To " + targetUrl);
            response.handleRequest(targetUrl, response,
                    HttpResponseMessage.HTTP_STATUS_MOVED_PERMANENTLY);
            return Boolean.FALSE;
        }
        return Boolean.TRUE;
    }

    @Override
    public Object config(ConfigRoot configRoot, Object o, Element element) throws ConfigException {
        this.specialNodeMap =  (Map) configRoot.createChildObject(o, element, "special-node-map");
        this.areaNodeManager = (AreaNodeManager) configRoot.createChildObject(o, element, "area-node-manager",true);
        return this;
    }
}
