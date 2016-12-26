package com.ifeng.ipserver.service.impl.node;

import com.ifeng.common.conf.ConfigException;
import com.ifeng.common.conf.ConfigRoot;
import com.ifeng.common.conf.Configurable;
import org.w3c.dom.*;

import java.util.ArrayList;
import java.util.List;

/**
 * 描述每一组节点分组状况
 * Created by gutc on 2015/10/13.
 */
public class GroupNode implements Configurable {
    private List<String> nodes;

    @Override
    public Object config(ConfigRoot configRoot, Object o, Element element) throws ConfigException {
        NodeList list = element.getElementsByTagName("node");
        nodes = new ArrayList<String>();
        for(int i = 0; i < list.getLength();i++){
            Node node = list.item(i);
            String content = node.getTextContent();
            nodes.add(content);
        }
        return this;
    }

    public List<String> getNodes() {
        return nodes;
    }

    public void setNodes(List<String> nodes) {
        this.nodes = nodes;
    }
}
