package com.ifeng.ipserver.server;

import com.ifeng.common.conf.ConfigRoot;
import com.ifeng.common.misc.Logger;
import com.ifeng.ipserver.server.codec.HttpServerProtocolCodecFactory;
import org.apache.mina.core.filterchain.DefaultIoFilterChainBuilder;
import org.apache.mina.core.service.IoHandler;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.transport.socket.SocketAcceptor;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;

import java.net.InetSocketAddress;

/**
 * <title> Server </title>
 * 
 * <pre>
 * ipserver服务器类，带有main函数，为服务的启动类。
 * 启动时，需要设置IPSERVER_HOME环境变量以便找到对应的配置文件。
 * 默认的服务端口为80 ， 如果需要其他端口，则需要跟-port {port}
 * 
 * </pre>
 * 
 * Copyright © 2012 Phoenix New Media Limited All Rights Reserved.
 * 
 * @author <a href="mailto:jl@ifeng.com">Jin Lin</a>
 * @author <a href="mailto:jinmy@ifeng.com">Jin Mingyan</a>
 */

public class Server {
	private static Logger log = Logger.getLogger(Server.class);
	
    /** Default HTTP port */
    private static int DEFAULT_PORT = 80;

    /** Tile server revision number */
    public static final String VERSION_STRING = "$Revision: 555855 $ $Date: 2007-07-13 12:19:00 +0900 (Fri, 13 Jul 2007) $";
    
    public static String projectHome = null;
    
    public static String getProjectHome() {
		return projectHome;
	}
    
	public static void main(String[] args) {
		int port = DEFAULT_PORT;
 /* System.setProperty("IPSERVER_HOME", "D:\\ideaWorkplace\\ipserver");*/
        
        projectHome = System.getProperty("IPSERVER_HOME");
        if(null==projectHome){
        	log.info("IPSERVER_HOME is null");
        	System.exit(0);
        }
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("-port")) {
                port = Integer.parseInt(args[i + 1]);
            }      
        }

        try {
        	//创建一个非阻塞的Server端Socket,用NIO
	        SocketAcceptor acceptor = new NioSocketAcceptor();
	        //创建接收数据的过滤器
	        DefaultIoFilterChainBuilder chain = acceptor.getFilterChain();
	        chain.addLast("protocolFilter", new ProtocolCodecFilter(
                    new HttpServerProtocolCodecFactory()));
	        
	        String pluginConfigFile=getProjectHome()+"/conf/server.xml";     	
	        ConfigRoot configRoot = new ConfigRoot(pluginConfigFile,System.getProperties());
	        
	        IoHandler handler = (IoHandler)configRoot.getValue("handler");
	    	
	        acceptor.setReuseAddress(true);
	        acceptor.getSessionConfig().setReuseAddress(true);
	        acceptor.getSessionConfig().setReceiveBufferSize(1024);
	        acceptor.getSessionConfig().setSendBufferSize(1024);
	        acceptor.getSessionConfig().setTcpNoDelay(true);
	        acceptor.getSessionConfig().setSoLinger(-1);
	        acceptor.setBacklog(10240);

	        acceptor.setHandler(handler);
            //绑定端口,启动服务器
	        acceptor.bind(new InetSocketAddress(port));
	        if(log.isInfoEnabled()){
	        	log.info("Server now listening on port " + port);
	        	log.info("Infomation Loading Finished");
	        }
        } catch (Exception ex) {
            log.error("Got Exception When Start Server ",ex);
        }
    }
}
