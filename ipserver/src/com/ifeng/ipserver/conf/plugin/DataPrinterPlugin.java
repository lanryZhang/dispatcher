package com.ifeng.ipserver.conf.plugin;

import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Element;

import com.ifeng.common.conf.ConfigException;
import com.ifeng.common.conf.ConfigRoot;
import com.ifeng.common.conf.Configurable;
import com.ifeng.common.misc.FileTools;
import com.ifeng.common.misc.Logger;
import com.ifeng.common.plugin.core.abst.AbstLogicPlugin;
import com.ifeng.ipserver.bean.print.Printable;

/**
 * <title> DatePrintPathGeneratorPlugin </title>
 * 
 * <pre>
 * 本类是一个“插件”， 此插件用于集成到一个责任链中，完成将一个待打印的list打印到一个目标文件中。
 * 本类的配置方式为：
 * &lt;... type="com.ifeng.ipserver.base.plugin。DataPrinterPlugin"&gt;
 * 	 &lt;print-list-key .../&gt;
 * 	 &lt;print-file-path-key .../&gt;
 *   &lt;separator .../&gt;
 * &lt/...&gt 
 *其中：
 *	print-list-key 
 *		类型：java.lang.String 
 *		用途：待打印的list在上下文中的键值
 *	print-file-path-key
 *		 类型：java.lang.String
 *		用途: 打印的目标文件路径
 *  separator
 *		 类型：java.lang.String
 *		用途: 一行为一个对象，对象内部的属性由对象自己来决定，separator为属性之间的分隔符
 * </pre>
 * 
 * Copyright © 2012 Phoenix New Media Limited All Rights Reserved.
 * 
 * @author <a href="mailto:qilp@ifeng.com">Qi Lupeng</a>
 * @author <a href="mailto:jinmy@ifeng.com">Jin Mingyan</a>
 */
public class DataPrinterPlugin extends AbstLogicPlugin implements Configurable{
	private static final Logger log = Logger.getLogger(DataPrinterPlugin.class);
	//待打印的list在上下文中的键值
	private String printListKey;
	//打印的目标文件路径
	private String printFilePathKey;
	//一行为一个对象，对象内部的属性由对象自己来决定，separator为属性之间的分隔符
	private String separator;
	
	/* (non-Javadoc)
	 * @see com.ifeng.common.plugin.core.itf.IntfPlugin#execute(java.lang.Object)
	 */
	@Override
	public Object execute(Object o) {
		Map contextMap = (Map)o;
		List printList = (List)contextMap.get(printListKey);
		PrintStream out = null;
		try {
			out = FileTools.getOutputStream(true, (String)contextMap.get(printFilePathKey));
			for(int	i=0;i<printList.size();i++){
				Printable printable = (Printable)printList.get(i);
				out.println(printable.getPrintString(separator));
			}
		} catch (FileNotFoundException e) {
			log.error("Cann't create file for "+ contextMap.get(printFilePathKey),e);
			return Boolean.FALSE;
		}finally{
			if(out!=null){
				out.close();
			}
		}
		
		return Boolean.TRUE;
	}

	/* (non-Javadoc)
	 * @see com.ifeng.common.conf.Configurable#config(com.ifeng.common.conf.ConfigRoot, java.lang.Object, org.w3c.dom.Element)
	 */
	@Override
	public Object config(ConfigRoot configRoot, Object parent, Element configEle)
			throws ConfigException {
		this.printListKey = (String)configRoot.createChildObject(parent, configEle, "print-list-key");
		this.printFilePathKey = (String)configRoot.createChildObject(parent, configEle, "print-file-path-key");
		this.separator = (String)configRoot.createChildObject(parent, configEle, "separator");
		return this;
	}

}
