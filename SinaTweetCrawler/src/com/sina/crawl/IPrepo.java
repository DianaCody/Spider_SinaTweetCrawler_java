package com.sina.crawl;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.http.client.ClientProtocolException;

/** * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * 
 * @filename IPrepo.java
 * @version  1.0
 * @note     Get local IP repository
 *             (1) Find candidate IPs from proxyIP website;
 *             (2) Verify IPs to use them;
 * @author   DianaCody
 * @since    2014-09-27 15:23:28
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
public class IPrepo {
	
	/** candidate IPs from proxyIP website */
	public static Vector<String> getProxyIPs(String html) throws ClientProtocolException, IOException {
		Vector<String> IPs = new Vector<String>();
		Pattern p = Pattern.compile("<td>\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}</td>\\n+?\\s+?<td>\\d{1,5}</td>"); //<td>\d{1,3}.\d{1,3}.\d{1,3}.\d{1,3}</td>\n+?\s+?<td>\d{1,5}</td>
		Matcher m = p.matcher(html);
		String s, ip, port, address;
		while(m.find()) {
			s = m.group();
			ip = s.split("\\n+?\\s+?")[0].replace("<td>", "").replace("</td>", "");
			port = s.split("\\n+?\\s+?")[1].replace("<td>", "").replace("</td>", "");
			address = ip + ":" +port;
			if(Integer.parseInt(port) < 65535) { //port:0~65535
				if(! IPs.contains(address)) {
					IPs.add(address);
				}
			}
			System.out.println("Find an ip address: " + address);
		}
		return IPs;
	}
	
	/**  Find proxy IPs from website "http://www.xici.net.co/" */
	public static Vector<String> getIPsPageLinks(String ipLibURL) throws ClientProtocolException, URISyntaxException, IOException {
		Vector<String> IPsPageLinks = new Vector<String>();
		IPsPageLinks.add("http://www.xici.net.co/nn/"); //national High anonymity IPs
		IPsPageLinks.add("http://www.xici.net.co/nt/"); //national transparent IPs
		IPsPageLinks.add("http://www.xici.net.co/wn/"); //international High anonymity IPs
		IPsPageLinks.add("http://www.xici.net.co/wt/"); //international transparent IPs
		IPsPageLinks.add("http://www.xici.net.co/qq/"); //qq proxy IPs
		return IPsPageLinks;
	}
	
	/**
	 * Get all unverified proxy in all IP library links.
	 * @param a specified URL "http://www.xici.net.co/"
	 * @return a String Vector contains all unverified IPs
	 */
	public static Vector<String> getAllProxyIPs(String ipLibURL) throws ClientProtocolException, IOException, URISyntaxException {
		Vector<String> IPsPageLinks = getIPsPageLinks(ipLibURL); //"http://www.xici.net.co/"
		Vector<String> onePageIPs = new Vector<String>();
		Vector<String> allIPs = new Vector<String>();
		for(int i=0; i<IPsPageLinks.size(); i++) {
			String url = IPsPageLinks.get(i);
			String[] html = new LoadHTML().getHTML(url);
			int page = 2;
			while(! html[0].equals("404")) {
				//System.out.println("×´Ì¬Âë " + html[0]);
				System.out.println("start finding proxy IPs under this link: " + url);
				onePageIPs = getProxyIPs(html[1]);
				for(int j=0; j<onePageIPs.size(); j++) {
					String s = onePageIPs.get(j);
					if(! allIPs.contains(s)) {
						allIPs.add(s);
					}
				}
				if(url.endsWith("/\\d+?/?")) {//page like "http://www.xici.net.co/3" --the 3rd page
					url = url + page + ".html";
				}
				else {
					url = url + ".html";
				}
				//System.out.println("page= " + page);
				html = new LoadHTML().getHTML(url);
				//System.out.println("×´Ì¬Âë " + html[0]);
				page ++;
			}
		}
		System.out.println("total proxy IP number: " + allIPs.size());
		return allIPs;
	}
	
	/** Test all proxy IPs and select a valid one from all candidate proxy IPs.
	 * @param Vector<String>: All proxy IPs
	 * @return Vector<String>: All valid IPs
	 * @throws ClientProtocolException, IOException
	 */
	public static Vector<String> getValidProxyIPs(Vector<String> allIPs) throws ClientProtocolException, IOException {
		System.out.println("Start getting valid proxy IPs...");
		Vector<String> validHostname = new Vector<String>();
		Vector<String> validIPs = new Vector<String>();
		int validIPNum = 0;
		for(int i=0; i<allIPs.size(); i++) {
			System.out.println("No." + i + " ip be verified.");
			if(i == 100) {
				break;
			}
			String ip = allIPs.get(i);
			String hostName = ip.split(":")[0];
			int port = Integer.parseInt(ip.split(":")[1]);
			//ÔÚÏß²éÑ¯ÈÏÖ¤IPÍøÕ¾:"http://iprame.ip138.com/ic.asp"
			String varifyURL = "http://iframe.ip138.com/ic.asp";//http://ip.uee.cn/";
			String html = new LoadHTML().getHTMLbyProxy(varifyURL, hostName, port);
			int iReconn = 0;
			while(html.equals("null")) { //reconnect 2 times (total 3 times connection)
				if(iReconn == 2) {
					break;
				}
				html = new LoadHTML().getHTMLbyProxy(varifyURL, hostName, port);
				iReconn ++;
			}
			Pattern p = Pattern.compile("\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}");
			Matcher m = p.matcher(html);
			String s;
			if(m.find()) {
				s = m.group();
				if(! validHostname.contains(s)) {
					validHostname.add(s);
					validIPs.add(s+":"+String.valueOf(port));
					//bw.write(s+"\r\n"); //write a valid proxy ip
					validIPNum ++;
					System.out.println("Valid proxy IP"+s+": "+String.valueOf(port));
				}
			}
			else {
				System.out.println("Html doesn't contain an IP.");
			}
		}
		System.out.println("Total number of valid IPs: " + validIPNum);
		return validIPs;
	}
	
	/**
	 * Verify all valid IPs then save to the IPrepo (data file).
	 * @param validIPs -the Vector<String> contains all valid IPs
	 * @param repoPath -a String giving a path to save all final usable IPs. "Final usable" IP indicate
	 * @return IPrepo
	 * @throws ClientProtocolException, IOException
	 */
	public Vector<String> classifyIPs(Vector<String> validIPs, String repoPath) throws ClientProtocolException, IOException {
		final String verificationURL = "http://s.weibo.com/wb/IPhone&nodup=1&page=1";
		//Vector<String> utf8IPs = new Vector<String>();
		Vector<String> IPrepo = new Vector<String>();
		String ip;
		//int ConnectionTimes = 0;
		for(int i=0; i<validIPs.size(); i++) {
			ip = validIPs.get(i);
			String html = new LoadHTML().getHTMLbyProxy(verificationURL, ip.split(":")[0], Integer.parseInt(ip.split(":")[1]));
			int iReconnectTimes = 0;
			while(html.equals("null")) {
				if(iReconnectTimes == 4) {
					break;
				}
				html = new LoadHTML().getHTMLbyProxy(verificationURL, ip.split(":")[0], Integer.parseInt(ip.split(":")[1]));
				iReconnectTimes ++;
				System.out.println(ip+" is reconnecting the "+iReconnectTimes+" times");
			}
		}
		return IPrepo;
	}
	
	public static void main(String[] args) throws ClientProtocolException, IOException, URISyntaxException, InterruptedException {
		long starttime = System.currentTimeMillis();
		String ipLibURL = "http://www.xici.net.co/";
		Vector<String> allIPs = getAllProxyIPs(ipLibURL);
		Vector<String> validIPs = getValidProxyIPs(allIPs);
		Vector<String> IPrepo = new IPrepo().classifyIPs(validIPs, "e:/tweet/IPrepo.txt");
		FileWR.write2txt(allIPs, "e:/tweet/allIPs.txt");
		FileWR.write2txt(validIPs, "e:/tweet/validIPs.txt");
		FileWR.write2txt(IPrepo, "e:/tweet/IPrepo.txt");
		
		long endtime = System.currentTimeMillis();
		System.out.println((double)(endtime-starttime)/60000 + "mins");
	}

}
