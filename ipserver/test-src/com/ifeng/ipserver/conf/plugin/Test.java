package com.ifeng.ipserver.conf.plugin;

import java.io.IOException;
import java.util.Map;

import net.sf.json.JSONArray;

import org.apache.derby.tools.sysinfo;

import com.ifeng.common.conf.ConfigRoot;
import com.ifeng.common.misc.RangeSet;
import com.ifeng.common.misc.RangeSet.Range;
import com.ifeng.ipserver.service.impl.node.Live3GNode;
import com.ifeng.ipserver.tools.HttpTools;



/**
 *<title>Test<title>
 *<pre>
 *</pre>
 *
 * @author <a href="mailto:chengyong@ifeng.com">banban</a>
 *
 *  Copyright © 2014 Phoenix New Media Limited All Rights Reserved.
 */
public class Test {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
//		ConfigRoot root = new ConfigRoot("D:/workspace/ipserver2/conf/server2.xml", System.getProperties());
//		
//		Map  map = (Map) root.getValue("live3GMap");
//		
//		System.out.println(((Live3GNode)map.get("联通")).getIp());
		
//		try {
//			String ret = HttpTools.downLoad("http://211.151.175.250/api/network/networkapi.php?type=lastvalue",3000, 3000);
//		    System.out.println(JSONArray.fromObject(ret));
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		
		
		Range r1 = new Range(0, 3, "a");
		Range r2 = new Range(2, 12, "b");
		Range r3 = new Range(3, 13, "c");
		
		RangeSet rs = new RangeSet();
		rs.add(r1);	
		rs.add(r2);	
		rs.add(r3);	
		
		System.out.println(rs.inRanges(3));
		
		

	}

}
