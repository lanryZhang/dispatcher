package com.ifeng.ipserver.service.email;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.w3c.dom.Element;

import com.ifeng.common.conf.ConfigException;
import com.ifeng.common.conf.ConfigRoot;
import com.ifeng.common.conf.Configurable;
import com.ifeng.common.email.IEmail;
import com.ifeng.ipserver.tools.HttpTools;

public class SimpleEmailPlugin  implements IEmail,Configurable {
	
	private String maillist;
	private String copyto;
	private String subject;
	private String msg;
	private String systemname;
	private String sendMailUrl;
	
	private Map<Object, Integer> limitMap;
	private int maxSendTimes;
	private int minErrorTimes;
	
	private Logger logger = Logger.getLogger(EmailPlugin.class);
	
	@Override
	public Object config(ConfigRoot configRoot, Object parent, Element configEle) throws ConfigException {
		this.setMaillist((String)configRoot.createChildObject(parent, configEle, "maillist"));
		this.setCopyto((String)configRoot.createChildObject(parent, configEle, "copyto"));
		this.setSendMailUrl((String)configRoot.createChildObject(parent, configEle, "sendMailUrl"));
		this.setMaxSendTimes((Integer)configRoot.createChildObject(parent, configEle, "maxSendTimes"));
		this.setMinErrorTimes((Integer)configRoot.createChildObject(parent, configEle, "minErrorTimes"));
		
		limitMap = new HashMap<Object, Integer>();
		return this;
	}

	@Override
	public void sendEmail() {
		sendEmail(maillist,subject,msg);
	}

	@Override
	public void sendEmail(String maillist, String subject, String msg) {
		try {
			String data = "?reqid=DcsHdazzT6McMRiBmJGzrnBy3yS5jbea&servname=prc&group=mail-service&ver=1.0.0&fn=CMSMail&args.user=teapesm&";
			data += "args.ars="+maillist.replace(" ", "%20") +"&args.txt="+msg.replace(" ", "%20")+"&args.sub="+subject.replace(" ", "%20")+"&args.acc="+copyto.replace(" ", "%20");//"?maillist="+URLEncoder.encode(maillist,"UTF-8") +"&content="+URLEncoder.encode(msg,"UTF-8")+"&title="+URLEncoder.encode(subject,"UTF-8")+"&copyto="+URLEncoder.encode(copyto,"UTF-8");
			String url;
			url = sendMailUrl +data;
			HttpTools.downLoad(url, 30 * 1000, 30 * 1000);
			logger.info("email has sent successfully,"+msg);
		} catch (Exception e) {
			logger.error(sendMailUrl +" 邮件发送失败.");
		}
	}

	@Override
	public void sendEmailLimit(Object key) {
		if (limitMap != null){
			int num = limitMap.get(key) == null?0:limitMap.get(key);
			logger.info("Error times :" + num);
			
			if (num > minErrorTimes && num < maxSendTimes + minErrorTimes){
				sendEmail();
			}
			limitMap.put(key, ++num);
		}
	}

	public void initSendEmailTimes(String key,int num){
		if (limitMap != null){
			limitMap.put(key, num);
		}
	}
	
	public String getSendMailUrl() {
		return sendMailUrl;
	}

	public void setSendMailUrl(String sendMailUrl) {
		this.sendMailUrl = sendMailUrl;
	}
	
	public String getMaillist() {
		return maillist;
	}

	public void setMaillist(String maillist) {
		this.maillist = maillist;
	}

	public String getCopyto() {
		return copyto;
	}

	public void setCopyto(String copyto) {
		this.copyto = copyto;
	}

	public String getSubject() {
		return subject;
	}

	public String getMsg() {
		return msg;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}
	
	public String getSystemname() {
		return systemname;
	}

	public void setSystemname(String systemname) {
		this.systemname = systemname;
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

	public int getMinErrorTimes() {
		return minErrorTimes;
	}

	public void setMinErrorTimes(int minErrorTimes) {
		this.minErrorTimes = minErrorTimes;
	}

}
