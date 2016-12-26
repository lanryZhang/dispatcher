package com.ifeng.ipserver.service.email;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.mail.Message.RecipientType;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.w3c.dom.Element;

import com.ifeng.common.conf.ConfigException;
import com.ifeng.common.conf.ConfigRoot;
import com.ifeng.common.conf.Configurable;
import com.ifeng.common.email.IEmail;
import com.ifeng.common.email.MailAuthenticator;

public class EmailPlugin implements IEmail,Configurable{
	private String addr;
	private String subject;
	private String msg;
	private String uname;
	private String pwd;
	private String host;
	private Map<Object, Integer> limitMap;
	private int maxSendTimes;
	

	private final transient Properties properties = System.getProperties();
	private transient MailAuthenticator authenticator;
	private transient Session session;
//	private Logger logger = Logger.getLogger(EmailPlugin.class);
	
	@Override
	public void sendEmail(String address, String subject, String msg) {
		try {
			MimeMessage message = new MimeMessage(session);
			
			message.setFrom(new InternetAddress(authenticator.getUsername()+"@ifeng.com"));
			String[] addrs = address.split(",");
			InternetAddress[] addrArr = new InternetAddress[address.split(",").length];

			for (int i = 0; i < addrArr.length;i++){
				addrArr[i] = new InternetAddress(addrs[i]);
			}
			message.setRecipients(RecipientType.TO, addrArr);
			message.setSubject(subject);
			message.setContent(msg,"text/html;charset=utf-8");
			Transport.send(message);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void sendEmail() {
		sendEmail(addr,subject,msg);
	}
	
	@Override
	public void sendEmailLimit(Object key) {
		if (limitMap != null){
			int num = limitMap.get(key) == null?0:limitMap.get(key);
			if (num < maxSendTimes){
				sendEmail();
			}
			limitMap.put(key, ++num);
		}
	}
	
	@Override
	public Object config(ConfigRoot configRoot, Object parent, Element configEle) throws ConfigException {
			this.addr = (String)configRoot.createChildObject(parent, configEle, "addr");
			this.uname = (String)configRoot.createChildObject(parent, configEle, "uname");
			this.pwd = (String)configRoot.createChildObject(parent, configEle, "pwd");
			this.host = (String)configRoot.createChildObject(parent, configEle, "host");
			this.maxSendTimes = (Integer)configRoot.createChildObject(parent, configEle, "maxSendTimes");
			init();
			return this;
	}
	
	public void initSendEmailTimes(String key,int num){
		if (limitMap != null){
			limitMap.put(key, num);
		}
	}
	private void init(){
		properties.put("mail.smtp.auth", true);
		properties.put("mail.smtp.host", host);
		authenticator = new MailAuthenticator(uname, pwd);
		session = Session.getDefaultInstance(properties, authenticator);
		setLimitMap(new HashMap<Object, Integer>());
	}
	
	public String getAddr() {
		return addr;
	}

	public String getSubject() {
		return subject;
	}

	public String getMsg() {
		return msg;
	}

	public String getUname() {
		return uname;
	}

	public String getPwd() {
		return pwd;
	}

	public void setAddr(String addr) {
		this.addr = addr;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}

	public void setUname(String uname) {
		this.uname = uname;
	}

	public void setPwd(String pwd) {
		this.pwd = pwd;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public Map<Object, Integer> getLimitMap() {
		return limitMap;
	}

	public void setLimitMap(Map<Object, Integer> limitMap) {
		this.limitMap = limitMap;
	}

	public int getMaxSendTimes() {
		return maxSendTimes;
	}

	public void setMaxSendTimes(int maxSendTimes) {
		this.maxSendTimes = maxSendTimes;
	}
}
