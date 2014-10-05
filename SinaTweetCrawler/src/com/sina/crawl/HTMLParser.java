package com.sina.crawl;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.dom4j.DocumentHelper;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/** * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 * @filename HTMLParser.java
 * @version  1.0
 * @note     Parse HTML pages, and write result to txt file and xml file with dom4j
 * @author   DianaCody
 * @since    2014-09-27 15:23:28
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
public class HTMLParser {
	public Vector<String> splitHTML(String html) {
		Vector<String> pieces = new Vector<String>();
		Pattern p = Pattern.compile("<dl class=\"feed_list\".+?<dd class=\"clear\">");
		Matcher m = p.matcher(html);
		while(m.find()) {
			pieces.add(m.group());
		}
		return pieces;
	}
	
	public String parse(String html) {
		String s = "";
		Document doc = Jsoup.parse(html);
		Elements userNames = doc.select("dt[class].face > a");
		Elements userids = doc.select("span > a[action-data]");
		Elements dates = doc.select("a[date]");
		Elements tweetids = doc.select("dl[mid]");
		Elements tweets = doc.select("p > em");
		Elements forwardNums = doc.select("a:contains(转发)");
		Elements commentNums = doc.select("a:contains(评论)");
		for(Element userName : userNames) {
			String attr = userName.attr("title");
			s += "<userName> " + attr + " </userName>";
		}
		for(Element userid : userids) {
			String attr = userid.attr("action-data");
			attr = attr.substring(attr.indexOf("uid="));
			Pattern p = Pattern.compile("[0-9]+");
			Matcher m = p.matcher(attr);
			if(m.find()) {
				attr = m.group();
			}
			s += "<userid> " + attr + " </userid>";
		}
		for(Element date : dates) {
			String attr = date.text();
			s += "<date> " + attr + " </date>";
		}
		for(Element tweetid : tweetids) {
			String attr = tweetid.attr("mid");
			s += "<tweetid> " + attr + " </tweetid>";
		}
		for(Element tweet : tweets) {
			String attr = tweet.text();
			s += "<tweetContent> " + attr + " </tweetContent>";
		}
		for(Element forwardNum : forwardNums) {
			String attr = forwardNum.text();
			if(attr.equals("转发")) {
				attr = "0";
			}
			else {
				if(!attr.contains("转发(")) {
					attr = "0";
				}
				else {
					attr = attr.substring(attr.indexOf("转发(")+3, attr.indexOf(")"));
				}
			}
			System.out.println(attr);
			s += "<forwardNum> " + attr + " </forwardNum>";
		}
		for(Element commentNum : commentNums) {
			String attr = commentNum.text();
			if(attr.equals("评论")) {
				attr = "0";
			}
			else {
				if(!attr.contains("评论(")) {
					attr = "0";
				}
				else {
					attr = attr.substring(attr.indexOf("评论(")+3, attr.indexOf(""));
				}
			}
			System.out.println(attr);
			s += "<commentNum> " + attr + " </commentNum>";
		}
		//System.out.println(s);
		return s;
	}
	
	public Vector<String> write2txt(String searchword, String dirPath, String saveTXTPath) throws IOException {
		Vector<String> tweets = new Vector<String>();
		String onePiece;
		File f = new File(saveTXTPath); //建立一个空文件
		FileWriter fw = new FileWriter(f);
		BufferedWriter bw = new BufferedWriter(fw);
		//dirPath = "e:/tweet/tweettxt/";
		for(int page=0; page<50; page++) {
			String path = dirPath + "/" + searchword + page + ".html";
			File ff = new File(path);
			if(ff.exists()) {
				String html = FileWR.html2String(path);
				Vector<String> pieces = new HTMLParser().splitHTML(html);
				for(int i=0; i<pieces.size(); i++) {
					onePiece = pieces.get(i);
					if(onePiece.contains("feed_list_forwardContent")) {
						Pattern p = Pattern.compile("feed_list_forwadContent.+?<p class=\"info W_linkb W_textb");
						Matcher m = p.matcher(onePiece);
						if(m.find()) {
							onePiece = onePiece.replace(m.group(), "");
						}
					}
					String s = new HTMLParser().parse(onePiece);
					tweets.add(s);
					bw.write(s+"\r\n"); //每次写完一条之后要换行!
				}
			}
		}
		bw.close();
		return tweets;
	}
	
	public void writeVector2xml(Vector<String> vector, String saveXMLPath) throws IOException {
		int vectorSize = vector.size();
		String oneIniTweet;
		OutputFormat format = OutputFormat.createPrettyPrint();
		format.setEncoding("GB2312"); //xml被识别格式仅为gb2312,默认utf8不被识别
		File f = new File(saveXMLPath);
		f.createNewFile(); //建立一个空xml文件
		FileWriter fw = new FileWriter(f);
		org.dom4j.Document document = DocumentHelper.createDocument();
		org.dom4j.Element rootElement = document.addElement("tweets"); //根节点tweets
		rootElement.addAttribute("totalNumber", String.valueOf(vectorSize)); //设置属性:总条目数
		for(int j=0; j<vectorSize; j++) {
			oneIniTweet = vector.get(j);
			String userName = oneIniTweet.substring(oneIniTweet.indexOf("<userName> ")+12, oneIniTweet.indexOf(" </userName>"));
			String userid = oneIniTweet.substring(oneIniTweet.indexOf("<userid> ")+10, oneIniTweet.indexOf(" </userid>"));
			String date = oneIniTweet.substring(oneIniTweet.indexOf("<date> ")+8, oneIniTweet.indexOf(" </date>"));
			String tweetid = oneIniTweet.substring(oneIniTweet.indexOf("<tweetid> ")+11, oneIniTweet.indexOf(" </tweetid>"));
			String forwardNum = oneIniTweet.substring(oneIniTweet.indexOf("<forwardNum> ")+14, oneIniTweet.indexOf(" </forwardNum>"));
			String commentNum = oneIniTweet.substring(oneIniTweet.indexOf("<commentNum> ")+14, oneIniTweet.indexOf(" </commentNum>"));
			String tweetContent = oneIniTweet.substring(oneIniTweet.indexOf("<tweetContent> ")+17, oneIniTweet.indexOf(" </tweetContent>"));
			org.dom4j.Element tweetElement = rootElement.addElement("tweet");
			tweetElement.addAttribute("userName", userName);
			tweetElement.addAttribute("userid", userid);
			tweetElement.addAttribute("date", date);
			tweetElement.addAttribute("tweetid", tweetid);
			tweetElement.addAttribute("forwardNum", forwardNum);
			tweetElement.addAttribute("commentNum", commentNum);
			tweetElement.setText(tweetContent); // 设置节点文本内容
		}
		XMLWriter xw = new XMLWriter(fw, format);
		xw.write(document);
		xw.close();
	}
}
