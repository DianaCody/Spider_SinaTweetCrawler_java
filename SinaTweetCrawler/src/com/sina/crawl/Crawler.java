package com.sina.crawl;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Vector;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import org.apache.http.client.ClientProtocolException;

/** * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 * @filename Crawler.java
 * @version  1.0
 * @note     Main Class: Crawl html pages from sina_tweet, and save to local file,
 *                       then finally trans to txt and xml files
 * @author   DianaCody
 * @since    2014-09-27 15:23:28
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
 
public class Crawler {
	
	/** 1.搜索页面是否存在 */
	public boolean isExistResult(String html) {
		boolean isExist = true;
		Pattern pExist = Pattern.compile("\\\\u6ca1\\\\u6709\\\\u627e\\\\u5230\\\\u76f8"
				+ "\\\\u5173\\\\u7684\\\\u5fae\\\\u535a\\\\u5462\\\\uff0c\\\\u6362\\\\u4e2a"
				+ "\\\\u5173\\\\u952e\\\\u8bcd\\\\u8bd5\\\\u5427\\\\uff01");//没有找到相关的微博呢，换个关键词试试吧！
		Matcher mExist = pExist.matcher(html);
		if(mExist.find()) {
			isExist = false;
		}
		return isExist;
	}
	
	public static void main(String[] args) throws ClientProtocolException, URISyntaxException, IOException, InterruptedException {
		long starttime = System.currentTimeMillis();
		String[] searchwords = {"samsung", "iPhone6", "htc", "huawei", "xiaomi", "zte", "lenovo", "mx", "coolpad",
				"google", "IBM", "Microsoft", "Amazon", "Intel", "Apple"};
		System.out.println("");
		File dirGetTweetSub = new File("e:/tweet/tweethtml/");
		dirGetTweetSub.mkdirs();
		File dirGetTweetTxtSub = new File("e:/tweet/tweettxt/");
		dirGetTweetTxtSub.mkdirs();
		File dirGetTweetXmlSub = new File("e:/tweet/tweetxml/");
		dirGetTweetXmlSub.mkdirs();
		Vector<String> ip = new Vector<String>();
		ip = FileWR.getLines("e:/tweet/IPrepo.txt");
		int ipNum = ip.size();
		int iIP = 0;
		for(int n=0; n<searchwords.length; n++) {
			String searchword = searchwords[n];
			String dirPath = "e:/tweet/tweethtml/" + searchword;
			File f = new File(dirPath);
			f.mkdirs();
			int totalPage = 50;
			System.out.println("Start to download html pages of the topic: " + searchword);
			String html;
			for(int i=totalPage; i>0; i--) {
				String hostName = ip.get(iIP).split(":")[0];
				int port = Integer.parseInt(ip.get(iIP).split(":")[1]);
				html = new LoadHTML().getHTML("http://s.weibo.com/wb/" + searchword + "&nodup=1&page=" + String.valueOf(i), hostName, port);
				int iReconn = 0;
				while(html.equals("null")) {
					html = new LoadHTML().getHTML("http://s.weibo.com/wb/" + searchword + "&nodup=1&page=" + String.valueOf(i), hostName, port);
					iReconn ++;
					System.out.println(ip.get(iIP) + " reconnected" + iReconn + " times.");
					//connnect over 4 times, then break.
					if(iReconn == 4) {
						break;
					}
				}
				if(html.equals("null")) {
					System.out.println("Failed 3 times, now trying a new IP from IPrepo...");
					if(iIP == ipNum-1) {
						System.out.println("All valid proxy IPs have been tried, still cannot get all data, now trying a valid proxy IP list again...");
						iIP = 0;
						System.out.println("IP: " + ip.get(iIP) + ", start connecting...");
					}
					else {
						iIP ++;
						System.out.println("IP: " + ip.get(iIP) + ", start connecting...");
					}
					i ++;
				}

			}
			System.out.println("topic \"" + searchword + "\"crawling has been done!****");
			System.out.println("Begin writing the tweets to local files: txt & xml");
			String saveTXTPath = "e:/tweet/tweettxt/" + searchword + ".txt";
			HTMLParser htmlParser = new HTMLParser();
			Vector<String> tweets = htmlParser.write2txt(searchword, dirPath, saveTXTPath);
			String saveXMLPath = "e:/tweet/tweetxml/" + "/" + searchword + ".xml";
			htmlParser.writeVector2xml(tweets, saveXMLPath);
			System.out.println("Save to txt & xml files succeed.");
			
			long endtime = System.currentTimeMillis();
			System.out.println((double)(endtime-starttime)/60000 + "mins");
		}
	}

}
