package com.sina.crawl;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.text.ParseException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.cookie.Cookie;
import org.apache.http.cookie.CookieOrigin;
import org.apache.http.cookie.CookieSpec;
import org.apache.http.cookie.CookieSpecProvider;
import org.apache.http.cookie.MalformedCookieException;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.DefaultProxyRoutePlanner;
import org.apache.http.impl.cookie.BestMatchSpecFactory;
import org.apache.http.impl.cookie.BrowserCompatSpec;
import org.apache.http.impl.cookie.BrowserCompatSpecFactory;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;

/** * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * 
 * @filename LoadHTML.java
 * @version  1.0
 * @note     (1) Download html pages according to url (search keywords),
 *           (2) Defined 3 methods to get html: normal, custom cookie policy, and proxyIP.
 * @author   DianaCody
 * @since    2014-09-27 15:23:28
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
public class LoadHTML {
	
	/** 一般方法 */
	public String[] getHTML(String url) throws ClientProtocolException, IOException {
		String[] html = new String[2];
		html[1] = "null";
		RequestConfig requestConfig = RequestConfig.custom()
				.setSocketTimeout(2000) //设置socket超时
				.setConnectTimeout(2000) //设置connect超时
				.build();
		CloseableHttpClient httpClient = HttpClients.custom()
				.setDefaultRequestConfig(requestConfig)
				.build();
		HttpGet httpGet = new HttpGet(url);
		try {
			CloseableHttpResponse response = httpClient.execute(httpGet);
			//System.out.println(response.getStatusLine().getStatusCode());
			html[0] = String.valueOf(response.getStatusLine().getStatusCode());
			html[1] = EntityUtils.toString(response.getEntity(), "utf-8");
			//System.out.println(html);
		} catch (IOException e) {
			System.out.println("Connection timeout...");
		}
		return html;
	}
	
	/** cookie方法的getHTMl(): 设置cookie策略,防止cookie rejected问题,拒绝写入cookie
	 *  --重载,3参数:url, hostName, port */
	public String getHTML(String url, String hostName, int port) throws URISyntaxException, ClientProtocolException, IOException {
		//自定义的cookie策略,解决cookie rejected问题(cookie拒绝写入)
		HttpHost proxy = new HttpHost(hostName, port);
		DefaultProxyRoutePlanner routePlanner = new DefaultProxyRoutePlanner(proxy);
		CookieSpecProvider cookieSpecProvider = new CookieSpecProvider() {
			public CookieSpec create(HttpContext context) {
				return new BrowserCompatSpec() {
					@Override
					public void validate(Cookie cookie, CookieOrigin origin) throws MalformedCookieException {
						//Oh, I am easy;
					}
				};
			}
		};
		Registry<CookieSpecProvider> r = RegistryBuilder
				.<CookieSpecProvider> create()
				.register(CookieSpecs.BEST_MATCH, new BestMatchSpecFactory())
				.register(CookieSpecs.BROWSER_COMPATIBILITY, new BrowserCompatSpecFactory())
				.register("easy", cookieSpecProvider)
				.build();
		RequestConfig requestConfig = RequestConfig.custom()
				.setCookieSpec("easy")
				.setSocketTimeout(4000) //设置socket超时
				.setConnectTimeout(4000) //设置connect超时
				.build();
		CloseableHttpClient httpClient = HttpClients.custom()
				.setDefaultCookieSpecRegistry(r)
				.setRoutePlanner(routePlanner)
				.build();
		HttpGet httpGet = new HttpGet(url);
		httpGet.setConfig(requestConfig);
		String html = "null";
		try {
			CloseableHttpResponse response = httpClient.execute(httpGet);
			html = EntityUtils.toString(response.getEntity(), "utf-8");
		} catch (IOException e) {
			System.out.println("Connection timeout...");
		}
		return html;
	}
	
	/** proxy代理IP方法 */
	public String getHTMLbyProxy(String targetURL, String hostName, int port) throws ClientProtocolException, IOException {
		HttpHost proxy = new HttpHost(hostName, port);
		String html = "null";
		DefaultProxyRoutePlanner routePlanner = new DefaultProxyRoutePlanner(proxy);
		RequestConfig requestConfig = RequestConfig.custom()
				.setSocketTimeout(2000) //设置socket超时
				.setConnectTimeout(2000) //设置connect超时
				.build();
		CloseableHttpClient httpClient = HttpClients.custom()
				.setRoutePlanner(routePlanner)
				.setDefaultRequestConfig(requestConfig)
				.build();
		HttpGet httpGet = new HttpGet(targetURL); //"http://iframe.ip138.com/ic.asp"
		try {
			CloseableHttpResponse response = httpClient.execute(httpGet);
			int statusCode = response.getStatusLine().getStatusCode();
			//System.out.println(response.getStatusLine().getStatusCode());
			if(statusCode == HttpStatus.SC_OK) {
				html = EntityUtils.toString(response.getEntity(), "gb2312");
			}
			response.close();
			//System.out.println(html);
		} catch (IOException e) {
			System.out.println("Connection timeout...");
		}
		return html;
	}

}
