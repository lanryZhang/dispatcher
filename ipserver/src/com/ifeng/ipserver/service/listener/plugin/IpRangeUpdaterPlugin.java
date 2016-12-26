package com.ifeng.ipserver.service.listener.plugin;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.StringTokenizer;
import javax.swing.text.html.HTMLDocument.HTMLReader.ParagraphAction;

import org.w3c.dom.Element;

import com.ifeng.common.conf.ConfigException;
import com.ifeng.common.conf.ConfigRoot;
import com.ifeng.common.conf.Configurable;
import com.ifeng.common.misc.FileTools;
import com.ifeng.common.misc.IpV4Address;
import com.ifeng.common.misc.Logger;
import com.ifeng.common.misc.RangeSet;
import com.ifeng.common.misc.RangeSet.Range;
import com.ifeng.common.plugin.core.abst.AbstLogicPlugin;
import com.ifeng.ipserver.bean.Area;
import com.ifeng.ipserver.service.intf.IpRangeManager;
/**
 * <title> IpRangeUpdaterPlugin </title>
 * 
 * <pre>
 * 本类是一个“插件”， 此插件用于集成到一个责任链中，完成更新ipRangeManager接口中对应存储地址段到区域之间映射关系的平衡二叉树的能力。
 * 本类的配置方式为：
 * &lt;... type="com.ifeng.ipserver.service.listener.plugin。IpRangeUpdaterPlugin"&gt;
 * 	 &lt;file-path .../&gt;
 * 	 &lt;iprange-manager .../&gt;
 * 	 &lt;separator .../&gt;
 * &lt/...&gt 
 *其中：
 *	file-path
 *		类型：String
 *		用途：用户声明一个本地文件存储的路径
 *  iprange-manager
 *		类型：IpRangeManager
 *		用途：IpRangeManager的接口实现，实现了查询ip地址对应的区域的能力
 *  separator
 *		类型：String
 *		用途：存储映射信息本地文件中，每行中的不同属性之间的分隔符
 * </pre>
 * 
 * Copyright © 2012 Phoenix New Media Limited All Rights Reserved.
 * 
 * @author <a href="mailto:yudf@ifeng.com">Yu Dengfeng</a>
 * @author <a href="mailto:jinmy@ifeng.com">Jin Mingyan</a>
 */
public class IpRangeUpdaterPlugin extends AbstLogicPlugin implements Configurable{
	private static final Logger log = Logger.getLogger(IpRangeUpdaterPlugin.class);
	private IpRangeManager ipRangeManager;
	private String filePath;
	private String separator;
	//private String tempfile;
	@Override
	public Object execute(Object o) {
		BufferedReader br = null;
		try {
			RangeSet rangeSet = new RangeSet();
			br = FileTools.getInputStream(filePath);
			String line = br.readLine();
			while((line!=null)&&(line.trim().length()>0)){
				StringTokenizer st = new StringTokenizer(line,separator);
				if(st.countTokens()>=5){
					IpV4Address startIp = new IpV4Address(st.nextToken());
					IpV4Address endIp = new IpV4Address(st.nextToken());
					Area area = new Area();
					area.setNetName(st.nextToken());
					area.setProvince(st.nextToken());
					area.setCity(st.nextToken());
					Range range = new Range(startIp,endIp,area);
					rangeSet.add(range);
				}
				line = br.readLine();
			}
			
			//readTempFile(rangeSet);
			
			if(rangeSet.size()>0){
				log.info("Set a new rangeSet,size is "+rangeSet.size());
				ipRangeManager.setIpRangeSet(rangeSet);
			}
			return Boolean.TRUE;
		} catch (FileNotFoundException e) {
			log.error("Cann't read file: "+filePath,e);
			return Boolean.FALSE;
		}catch (IOException e) {
			log.error("Read file catch exception: "+filePath,e);
			return Boolean.FALSE;
		}catch(Exception e){
			return Boolean.FALSE;
		}
		finally{
			if(br!=null){
				try {
					br.close();
				} catch (IOException e) {
				}
			}
		}
	}
	
//	private void readTempFile(RangeSet rangeSet) throws Exception{
//		BufferedReader br = null;
//		
//		try {
//			if (rangeSet == null){
//				rangeSet = new RangeSet();
//			}
//			br = FileTools.getInputStream(tempfile);
//			String line = br.readLine();
//			while((line!=null)&&(line.trim().length()>0)){
//				StringTokenizer st = new StringTokenizer(line,separator);
//				if(st.countTokens()>=5){
//					IpV4Address startIp = new IpV4Address(st.nextToken());
//					IpV4Address endIp = new IpV4Address(st.nextToken());
//					Area area = new Area();
//					area.setNetName(st.nextToken());
//					area.setProvince(st.nextToken());
//					area.setCity(st.nextToken());
//					Range range = new Range(startIp,endIp,area);
//					rangeSet.add(range);
//				}
//				line = br.readLine();
//			}
//		} catch (Exception e) {
//			log.error("Read tempfile catch exception: "+tempfile,e);
//			throw e;
//		}
//		finally{
//			if (br!=null){
//				try {
//					br.close();
//				} catch (Exception e2) {
//					throw e2;
//				}
//			}
//		}
//	}
	@Override
	public Object config(ConfigRoot configRoot, Object parent, Element configEle)
			throws ConfigException {
		this.filePath = (String)configRoot.createChildObject(parent, configEle, "file-path");
		this.ipRangeManager = (IpRangeManager)configRoot.createChildObject(parent, configEle, "iprange-manager");
		this.separator = (String)configRoot.createChildObject(parent, configEle, "separator");
		//this.tempfile =  (String)configRoot.createChildObject(parent, configEle, "tempfile");
		return this;
	}
}
