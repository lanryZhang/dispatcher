package com.ifeng.ipserver.conf.plugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Element;

import com.ifeng.common.conf.ConfigException;
import com.ifeng.common.conf.ConfigRoot;
import com.ifeng.common.conf.Configurable;
import com.ifeng.common.misc.Logger;
import com.ifeng.common.plugin.core.abst.AbstLogicPlugin;
import com.ifeng.ipserver.bean.AreaToCname;
import com.ifeng.ipserver.bean.db.ChannelAndGroupToNode;
import com.ifeng.ipserver.bean.db.ChannelAndNodeToCname;
import com.ifeng.ipserver.bean.db.ChannelAndNodeToCname.ChannelAndNodeToCnamePK;
import com.ifeng.ipserver.bean.db.GovAndNetnameToGroup;
import com.ifeng.ipserver.bean.db.Govment;

/**
 * <title> GovementToNodeGeneratorPlugin </title>
 * 
 * <pre>
 * 本类是一个“插件”， 此插件用于集成到一个责任链中，完成通过查询上下文中数据库的原始信息，将这些信息转化为一个 区域（省,市,运营商） --> 具体的Node的列表。
 * 本类的配置方式为：
 * &lt;... type="com.ifeng.ipserver.base.plugin。GovementToNodeGeneratorPlugin"&gt;
 * 	 &lt;govement-list-context-key .../&gt;
 * 	 &lt;channelAndNodeToCname-list-context-key .../&gt;
 * 	 &lt;govAndNetnameToGroup-list-context-key .../&gt;
 * 	 &lt;channelAndGroupToNode-list-context-key .../&gt;
 * 	 &lt;context-key .../&gt;
 * &lt/...&gt 
 *其中：
 *	govement-list-context-key 
 *		类型： java.lang.String
 *		用途：上下文中的govement list ,由其他责任链插件从数据库中取得放入上下文中。
 *  channelAndNodeToCname-list-context-key
 *		类型： java.lang.String
 *		用途：上下文中的channelAndNodeToCname list ,由其他责任链插件从数据库中取得放入上下文中。
 *  govAndNetnameToGroup-list-context-key
 *		类型： java.lang.String
 *		用途：上下文中的govAndNetnameToGroup list ,由其他责任链插件从数据库中取得放入上下文中。
 *  channelAndGroupToNode-list-context-key
 *		类型： java.lang.String
 *		用途：上下文中的channelAndGroupToNode list ,由其他责任链插件从数据库中取得放入上下文中。
 *	context-key	
 *		 类型：java.lang.String
 *		用途：将运算完成的list 放入上下文中（上下文是指调用插件对应的责任链传递数据所使用的数据总线）
 *			context-key是指上下文中该list对应的key值（默认的上下文类型为Map)
 * </pre>
 * 
 * Copyright © 2012 Phoenix New Media Limited All Rights Reserved.
 * 
 * @author <a href="mailto:qilp@ifeng.com">Qi Lupeng</a>
 * @author <a href="mailto:jinmy@ifeng.com">Jin Mingyan</a>
 */

public class GovementToNodeGeneratorPlugin extends AbstLogicPlugin implements Configurable{
	
	private static final Logger log = Logger.getLogger(GovementToNodeGeneratorPlugin.class);
	private String govementListContextKey;
	private String channelAndNodeToCnameListContextKey;
	private String govAndNetnameToGroupListContextKey;
	private String channelAndGroupToNodeListContextKey;
	private String contextKey;
	
	/* (non-Javadoc)
	 * @see com.ifeng.common.conf.Configurable#config(com.ifeng.common.conf.ConfigRoot, java.lang.Object, org.w3c.dom.Element)
	 */
	@Override
	public Object config(ConfigRoot configRoot, Object parent, Element configEle)
			throws ConfigException {
		this.govementListContextKey = (String)configRoot.createChildObject(parent, configEle, "govement-list-context-key");
		this.channelAndNodeToCnameListContextKey = (String)configRoot.createChildObject(parent, configEle, "channelAndNodeToCname-list-context-key");
		this.govAndNetnameToGroupListContextKey = (String)configRoot.createChildObject(parent, configEle, "govAndNetnameToGroup-list-context-key");
		this.channelAndGroupToNodeListContextKey = (String)configRoot.createChildObject(parent, configEle, "channelAndGroupToNode-list-context-key");
		this.contextKey = (String)configRoot.createChildObject(parent, configEle, "context-key");
		return this;
	}
	/* (non-Javadoc)
	 * @see com.ifeng.common.plugin.core.itf.IntfPlugin#execute(java.lang.Object)
	 */
	@Override
	public Object execute(Object o) {
		//得到上下文中的已经从数据库中取得的list
		Map context = (Map)o;
		List<Govment> govmentList = (List) context.get(govementListContextKey);
		List channelAndNodeToCnameList = (List) context.get(channelAndNodeToCnameListContextKey);
		List govAndNetnameToGroupList = (List) context.get(govAndNetnameToGroupListContextKey);
		List channelAndGroupToNodeList = (List) context.get(channelAndGroupToNodeListContextKey);
		
		if((null==govmentList)||(null==channelAndNodeToCnameList)||(null==govAndNetnameToGroupList)||(null==channelAndGroupToNodeList)){
			log.error("govmentList or channelGroupNodeList or groupGovementNetnameList or nodeChannelList is null");
			return Boolean.FALSE;
		}
		
		//将list转化为对应的map以便查询，其中各个map的索引关系请参加下面的私有方法
		Map channelAndNodeToCnameMap = this.buildChannelAndNodeToCnameMap(channelAndNodeToCnameList);
		Map channelAndGroupToNodeMap = this.buildChannelAndGroupToNodeMap(channelAndGroupToNodeList);
		Map govAndNetnameToGroupMap = this.buildGovAndNetnameToGroupList(govAndNetnameToGroupList);
		
		List result = new ArrayList();
		
		
		Map<Long,String> provinceMap = new HashMap();
		
		//查出所有的省份
		for(Govment gov : govmentList){
			if(gov.getParentId()==1000){
				provinceMap.put(gov.getId(), gov.getName());
				continue;
			}
		}
		
		for(Govment gov : govmentList){
			//是一个省级单位，则忽略
			if(gov.getParentId()==1000){
				continue;
			}
			//得到该区域对应的group， 返回为一个list ， 包含该区域+各个运营商 --》 具体的group 。 group为一个地域的集合
			List<GovAndNetnameToGroup> groupGovNetnameList =  (List<GovAndNetnameToGroup>)govAndNetnameToGroupMap.get(gov.getId());
			if(null == groupGovNetnameList){
				log.info("when getting groupid by govid, maybe there is no groupid mapping the govid : "+gov.getId());
				continue;
			}
			//针对这些地域集合
			for(GovAndNetnameToGroup govAndNetnameToGroup : groupGovNetnameList){
				//得到地域 及 相关的频道对应的所有node映射关系
				ChannelAndGroupToNode channelAndGroupToNode = (ChannelAndGroupToNode)channelAndGroupToNodeMap.get(govAndNetnameToGroup.getGroupId());
				if(null == channelAndGroupToNode){
					log.info("when getting nodeid by groupid, maybe there is no nodeid mapping the groupid : "+govAndNetnameToGroup.getGroupId());
					continue;
				}
				//查询node和channel对应的cname
				ChannelAndNodeToCnamePK channelAndNodeToCnamePK = new ChannelAndNodeToCnamePK();
				channelAndNodeToCnamePK.setChannelId(channelAndGroupToNode.getChannelId());
				channelAndNodeToCnamePK.setNodeId(channelAndGroupToNode.getNodeId());
				ChannelAndNodeToCname channelAndNodeToCname = (ChannelAndNodeToCname)channelAndNodeToCnameMap.get(channelAndNodeToCnamePK);
				
				if(null == channelAndNodeToCname){
					log.info("when getting cname by nodeid&channel, maybe there is no Node mapping the nodeid&channel : "+channelAndGroupToNode.getNodeId()+"&"+channelAndGroupToNode.getChannelId());
					continue;
				}
				if(null == channelAndNodeToCname.getType()){
					log.info("the column type is null in Node object");
					continue;
				}
				
				AreaToCname areaToCname = new AreaToCname();
				areaToCname.setCity(gov.getName());
				areaToCname.setIpOrCname(channelAndNodeToCname.getType().equals("A")?channelAndNodeToCname.getIp():channelAndNodeToCname.getCname());
				areaToCname.setNetName(govAndNetnameToGroup.getNetname());
				areaToCname.setProvince(provinceMap.get(gov.getParentId()));
				result.add(areaToCname);
			}
		}
		
		context.put(contextKey, result);
		return Boolean.TRUE;
	}
	
	
	/**
	 * 将上下文中的list转化为一个map以方便查询。
	 * 区域和运营针对一个group时，因为一个区域内有多个运营商，故最终构建的结果是：
	 *  key:govId
	 *  value:list
	 *  	  list 为一个GovAndNetnameToGroup列表
	 * 
	 * @param list 内涵的数据为GovAndNetnameToGroup列表
	 * @return map key:govId value:list list 为一个GovAndNetnameToGroup列表
	 */
	private Map buildGovAndNetnameToGroupList(List list){
		Map result = new HashMap();
		for(int i=0;i<list.size();i++){
			GovAndNetnameToGroup govAndNetnameToGroup= (GovAndNetnameToGroup)list.get(i);
			if(result.containsKey(govAndNetnameToGroup.getGovId())){
				((List)result.get(govAndNetnameToGroup.getGovId())).add(govAndNetnameToGroup);
			}else{
				List listIn = new ArrayList();
				listIn.add(govAndNetnameToGroup);
				result.put(govAndNetnameToGroup.getGovId(),listIn);
			}
		}
		return result;
	}
	
	/**
	 * 将上下文中的list转化为一个map以方便查询。
	 * key为groupId ,value 为对应的ChannelAndGroupToNode
	 * 
	 * @param list 内涵的数据为ChannelAndGroupToNode列表
	 * @return map key:groupId value:ChannelAndGroupToNode
	 */
	private Map buildChannelAndGroupToNodeMap(List list){
		Map result = new HashMap();
		for(int i=0;i<list.size();i++){
			ChannelAndGroupToNode channelAndGroupToNode= (ChannelAndGroupToNode)list.get(i);
			result.put(channelAndGroupToNode.getGroupId(), channelAndGroupToNode);
		}
		return result;
	}
	/**
	 * 将上下文中的list转化为一个map以方便查询。
	 * key为ChannelAndNodeToCnamePK ,value 为对应的ChannelAndNodeToCname
	 * 
	 * @param list 内涵的数据为ChannelAndNodeToCname列表
	 * @return map key:ChannelAndNodeToCnamePK value:ChannelAndNodeToCname
	 */
	private Map buildChannelAndNodeToCnameMap(List list){
		Map result = new HashMap();
		for(int i=0;i<list.size();i++){
			ChannelAndNodeToCname channelAndNodeToCname= (ChannelAndNodeToCname)list.get(i);
			result.put(channelAndNodeToCname.getChannelAndNodeToCnamePK(), channelAndNodeToCname);
		}
		return result;
	}
}
