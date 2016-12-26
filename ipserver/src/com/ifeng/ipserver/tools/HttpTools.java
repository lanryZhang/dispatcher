package com.ifeng.ipserver.tools;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import com.ifeng.common.misc.Logger;

public class HttpTools {
	private static Logger logger = Logger.getLogger(HttpTools.class);

	//工具类，不需要实例化
	private HttpTools(){}

	private static void initHttpURLConnectionAsBrowser(
			HttpURLConnection httpURLConnection) {
		if (httpURLConnection != null) {
			httpURLConnection.setRequestProperty("Accept", "*/*");
			httpURLConnection.setRequestProperty("User-Agent",
					"Mozilla/5.0 (compatible; MSIE 6.0; Windows NT)");
			httpURLConnection.setRequestProperty("Pragma", "no-cache");
			httpURLConnection.setRequestProperty("Cache-Control", "no-cache");
			httpURLConnection.setRequestProperty("Connection", "close");
		}
	}

	public static String downLoad(String url,int connTimeout,int readTimeout) throws IOException {
			InputStream in = null;
			ByteArrayOutputStream bs = new ByteArrayOutputStream();
			HttpURLConnection connection = null;
			try {
				URL urlOPbj = new URL(url);
				connection = (HttpURLConnection) urlOPbj.openConnection();
				initHttpURLConnectionAsBrowser(connection);
				connection.setConnectTimeout(connTimeout);
				connection.setReadTimeout(readTimeout);
				int state = connection.getResponseCode();
				String stateString = String.valueOf(state);
				if (!stateString.matches("[2-3][0-9]{2}")) {
					throw new IOException(
							"Can't get connection. The http response code is "
									+ state);
				}
				in = connection.getInputStream();
				int len = 0;
				List<Byte> byteList = new ArrayList<Byte>();
				try {
					while ((len = in.read()) != -1) {
						byteList.add(new Byte((byte) len));
					}
				} catch (Exception e) {
					logger.error("Got Exception When Download ", e);
				} finally {
					in.close();
				}
				int listSize = byteList.size();
				byte[] contentBytes = new byte[listSize];
				for (int byteCount = 0; byteCount < listSize; byteCount++) {
					contentBytes[byteCount] = byteList.get(byteCount);
				}
				String s = new String(contentBytes);
				connection.disconnect();
				return s;
			} catch (IOException e) {
				logger.error("Got Exception When Download ", e);
			} finally {
				try {
					if (in != null) {
						in.close();
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				try {
					if (bs != null) {
						bs.close();
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				if (connection != null) {
					connection.disconnect();
				}
			}
		throw new IOException("Can't get connection,over try.The Url is :"
				+ url);
	}
}
