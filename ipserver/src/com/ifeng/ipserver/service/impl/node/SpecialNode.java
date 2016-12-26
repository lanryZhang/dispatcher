package com.ifeng.ipserver.service.impl.node;

import com.ifeng.common.conf.ConfigException;
import com.ifeng.common.conf.ConfigRoot;
import com.ifeng.common.conf.Configurable;
import org.w3c.dom.Element;

/**
 * 描述每一个特殊节点信息
 * Created by gutc on 2015/10/13.
 */
public class SpecialNode implements Configurable {
    private String url;
    private String cdnId;
    private String url3G;
    private String urlLive;

    @Override
    public Object config(ConfigRoot configRoot, Object o, Element element) throws ConfigException {
        this.cdnId = (String) configRoot.createChildObject(o,element, "cdn-id");
        this.url =  (String) configRoot.createChildObject(o,element, "url");
        this.url3G = (String) configRoot.createChildObject(o,element, "url3G");
        this.urlLive = (String) configRoot.createChildObject(o,element, "urlLive");
        return this;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getCdnId() {
        return cdnId;
    }

    public void setCdnId(String cdnId) {
        this.cdnId = cdnId;
    }

    public String getUrl3G() {
        return url3G;
    }

    public void setUrl3G(String url3G) {
        this.url3G = url3G;
    }

    public String getUrlLive() {
        return urlLive;
    }

    public void setUrlLive(String urlLive) {
        this.urlLive = urlLive;
    }
}
