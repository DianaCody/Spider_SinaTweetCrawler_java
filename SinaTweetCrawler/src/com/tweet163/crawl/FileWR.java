package com.tweet163.crawl;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Vector;

/** * * * * * * * * * * * * * * * * * * * * * *
 * @filename FileWR.java
 * @version  1.0
 * @note     Write and Read from files
 * @author   DianaCody
 * @since    2014-09-27 15:23:28
 * * * * * * * * * * * * * * * * * * * * * * */

public class FileWR {
	public static Vector<String> getLines(String path) throws IOException {
		Vector<String> lines = new Vector<String>();
		File f = new File(path); //"e:/tweet/validIPs.txt"
		FileReader fr = new FileReader(f);
		BufferedReader br = new BufferedReader(fr);
		String s;
		while((s = br.readLine()) != null) {
			lines.add(s);
		}
		br.close();
		return lines;
	}
	
	public static void write2txt(Vector<String> vector, String savePath) throws IOException {
		File f = new File(savePath);
		FileWriter fw = new FileWriter(f);
		BufferedWriter bw = new BufferedWriter(fw);
		for(int i=0; i<vector.size(); i++) {
			bw.write(vector.get(i) + "\r\n");
			//System.out.println(vector.get(i));
		}
		bw.close();
	}
	
	/** (公共方法)把String写到本地文件 */
	public static void writeString(String s, String savePath) throws IOException {
		File f = new File(savePath);
		FileWriter fw = new FileWriter(f);
		BufferedWriter bw = new BufferedWriter(fw);
		bw.write(s);
		bw.close();
	}
	
	/** 由html文件得到微博 */
	public static String html2String(String htmlPath) throws IOException {
		String html = "";
		File f = new File(htmlPath);
		FileReader fr = new FileReader(f);
		BufferedReader br = new BufferedReader(fr);
		String s;
		while((s=br.readLine()) != null) {
			html += s;
		}
		br.close();
		return html;
	}
	
	/** 把某关键字搜索到的微博写到txt文件里去 */
	public static void writeVector(Vector<String> vector, String savePath) throws IOException {
		File f = new File(savePath);
		FileWriter fw = new FileWriter(f);
		BufferedWriter bw = new BufferedWriter(fw);
		for(int i=0; i<vector.size(); i++) {
			bw.write(vector.get(i) + "\r\n");
		}
		bw.close();
	}
}
