package com.ifeng.ipserver.service.impl.node.plugin;

import com.ifeng.common.conf.ConfigException;
import com.ifeng.common.conf.ConfigRoot;
import com.ifeng.common.conf.Configurable;
import com.ifeng.common.misc.Logger;
import com.ifeng.common.plugin.core.abst.AbstLogicPlugin;
import org.w3c.dom.Element;

import java.util.Map;


/**
 * Created by zhanglr on 2015/8/27.
 */
public class GetCdnUrlPlugin extends AbstLogicPlugin implements Configurable {
    private String url;
    private int minPercent;
    private int maxPercent;
    private String backCdnid;
/*    private String live3gUrl;*/
    private static final Logger log = Logger.getLogger(GetCdnUrlPlugin.class);
    @Override
    public Object execute(Object o) {
        Map map = (Map)o;
        int random =(Integer)map.get("random");
        //log.info("random:" + random + "min:" + minPercent + "max:" + maxPercent);
        if (random >= minPercent && random <= maxPercent){
            map.put("cdn-url",url);
            map.put("back-cdnid",backCdnid);
    /*        map.put("live3g-url",live3gUrl);*/
            return Boolean.TRUE;
        }
        return Boolean.FALSE;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public int getMinPercent() {
        return minPercent;
    }

    public void setMinPercent(int minPercent) {
        this.minPercent = minPercent;
    }

    public int getMaxPercent() {
        return maxPercent;
    }

    public void setMaxPercent(int maxPercent) {
        this.maxPercent = maxPercent;
    }

    public String getBackCdnid() {
        return backCdnid;
    }

    public void setBackCdnid(String backCdnid) {
        this.backCdnid = backCdnid;
    }

 /*   public String getLive3gUrl() {
        return live3gUrl;
    }

    public void setLive3gUrl(String live3gUrl) {
        this.live3gUrl = live3gUrl;
    }
*/
    @Override
    public Object config(ConfigRoot configRoot, Object parent, Element configEle)
            throws ConfigException {
        this.url =  (String) configRoot.createChildObject(this, configEle,
                "url", true);
        this.backCdnid =  (String) configRoot.createChildObject(this, configEle,
                "back-cdnid", true);
        this.minPercent =  (Integer) configRoot.createChildObject(this, configEle,
                "min-percent", true);
        this.maxPercent =  (Integer) configRoot.createChildObject(this, configEle,
                "max-percent", true);
    /*    this.live3gUrl = (String) configRoot.createChildObject(this, configEle,
                "live3g-url", true);*/
        return this;
    }
}
