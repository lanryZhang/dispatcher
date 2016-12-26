package com.ifeng.ipserver.server.handler.plugin.location;

import com.ifeng.common.conf.ConfigException;
import com.ifeng.common.conf.ConfigRoot;
import com.ifeng.common.conf.Configurable;
import com.ifeng.common.misc.Logger;
import com.ifeng.common.plugin.core.abst.AbstLogicPlugin;
import com.ifeng.ipserver.bean.Area;
import com.ifeng.ipserver.server.handler.DeviceType;
import com.ifeng.ipserver.server.handler.ServiceContext;
import com.ifeng.ipserver.server.handler.plugin.redirect.RequestHost;
import com.ifeng.ipserver.server.message.HttpRequestMessage;
import com.ifeng.ipserver.server.message.HttpResponseMessage;
import com.ifeng.ipserver.service.impl.node.SpecialNode;
import com.ifeng.ipserver.service.intf.AreaNodeManager;
import org.w3c.dom.Element;

import java.util.Map;

/**
 * 点播请求节点过滤
 * 当点播请求进来时，判断是否为特殊节点，如果为特殊节点，直接返回特殊节点设置内容，不进行原始逻辑的运行；如果不是特殊节点，则进入原始逻辑；
 * 其中原始逻辑指的是，一般节点的逻辑；
 * 返回值为特殊节点设置的url
 * Created by gutc on 2015/10/13.
 */
public class IpFileterPlugin extends AbstLogicPlugin implements Configurable {
    private static Logger log = Logger.getLogger(IpFileterPlugin.class);
    private Map specialNodeMap;
    private AreaNodeManager areaNodeManager;

    @Override
    public Object execute(Object o) {

        Map contextMap = (Map) o;
        HttpRequestMessage request = (HttpRequestMessage) contextMap.get(ServiceContext.REQUEST_MESSAGE);
        HttpResponseMessage response = (HttpResponseMessage) contextMap.get(ServiceContext.RESPONSE_MESSAGE);
        RequestHost requestHost = (RequestHost) contextMap.get("requestHost");
        String deviceType = (String) contextMap.get(ServiceContext.DEVICE_TYPE);
        Area area = (Area) contextMap.get(ServiceContext.LOCATION_MESSAGE);
        String ip = (String) contextMap.get(ServiceContext.REMOTE_IP);

        int httpStatus = requestHost.getHttpStatus();// HttpResponseMessage.HTTP_STATUS_MOVED_TEMPORARILY;
        if (deviceType .equals(DeviceType.ANDROID) ) { // android请求状态需要设置为301
            httpStatus = HttpResponseMessage.HTTP_STATUS_MOVED_PERMANENTLY;
        }

        String node = null;
        String targetUrl = null;
        if (area != null) {
            try {
                node = areaNodeManager.getNodeByArea(area,0,false);
            } catch (Exception e) {
                log.error("Got Exception When areaNodeManager.getNodeByArea", e);
            }
        }
        if(node!=null){
            if(specialNodeMap.containsKey(node)){
                SpecialNode specialNode = (SpecialNode) specialNodeMap.get(node);
                targetUrl = specialNode.getUrl();
            }
        }
        if(targetUrl!=null){
            log.info("Special Service: IP: " + ip + " from: "+deviceType+" node is Special "+node+" Redirect To " + targetUrl);
            response.handleRequest(targetUrl, response, httpStatus);
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
