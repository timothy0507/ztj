package ztj.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

import pianoweb.model.Generator;

/**
 * 页面抓取
 * 
 * @author Timothy
 * @version 1.0
 * @date 2011-9-15
 */
public class PageSnatch {

	private String webRootPath;

	/**
	 * 通过网址抓取内容并另存为文件（注：filename不包含扩展名）
	 * 
	 * @param generator
	 *            Generator
	 * @throws IOException
	 */
	public synchronized void snatch(Generator generator) throws IOException {
		int count = 0;
		while (true) {
			try {
				String filename = "";
				if (generator.getFilename() == null
						|| "".equals(generator.getFilename())) {
					filename = getFileName(generator.getUrl())
							+ Generator.HTML_EXT_NAME;
				} else {
					filename = generator.getFilename()
							+ Generator.HTML_EXT_NAME;
				}
				String filepath = "";
				if (generator.getFilepath() == null
						|| "".equals(generator.getFilepath())) {
					filepath = getWebRootPath();
				} else {
					filepath = getWebRootPath() + generator.getFilepath();
				}
				File file = new File(filepath, filename);

				// 如果不需要重写，并且文件存在，返回
				if (!generator.isRewrite() && file.exists()) {
					return;
				}

				// 打开页面输入流
				URL url = new URL(generator.getUrl());
				InputStream is = url.openStream();
				InputStreamReader isr = new InputStreamReader(is);
				BufferedReader br = new BufferedReader(isr);

				// 打开文件写入流
				FileWriter fw = new FileWriter(file);
				BufferedWriter bw = new BufferedWriter(fw);

				// 按行循环写入文件
				String line;
				while ((line = br.readLine()) != null) {
					line = line.trim();
					if (line.length() != 0) {
						bw.write(line);
						bw.write("\r\n");
					}
				}

				// 刷新缓冲流
				bw.flush();
				fw.flush();
				// 关闭所有输入输出对象
				br.close();
				isr.close();
				is.close();
				bw.close();
				fw.close();
				break;
			} catch (IOException e) {
				count++;
				if (count == 2) {
					throw e;
				}
			}
		}
	}

	/**
	 * 通过网址抓取网页内容
	 * 
	 * @param location
	 *            网址
	 * @return 网页内容
	 * @throws IOException
	 */
	public synchronized String snatchAsString(String location)
			throws IOException {
		StringBuffer result = new StringBuffer();
		int count = 0;
		while (true) {
			try {
				URL url = new URL(location);
				InputStream is = url.openStream();
				InputStreamReader isr = new InputStreamReader(is);
				BufferedReader br = new BufferedReader(isr);
				String line;
				while ((line = br.readLine()) != null) {
					result.append(line + "\r\n");
				}
				br.close();
				isr.close();
				is.close();
				break;
			} catch (IOException e) {
				count++;
				if (count == 3) {
					throw e;
				}
			}
		}

		return result.toString();
	}

	/**
	 * 提取文件名
	 * 
	 * @param address
	 *            网址
	 * @return 文件名
	 */
	private String getFileName(String address) {
		if (address == null) {
			return null;
		}
		int barPosition = address.lastIndexOf("/");
		int pointPosition = address.lastIndexOf(".");
		String filename = address.substring(barPosition + 1, pointPosition);

		return filename;
	}

	/**
	 * 获取站点根目录所在系统磁盘路径
	 * 
	 * @return 磁盘路径
	 */
	private String getWebRootPath() {
		if (webRootPath == null) {
			ClassLoader classLoader = Thread.currentThread()
					.getContextClassLoader();
			URL url = classLoader.getResource("");
			File classDirFile = new File(url.getPath());
			File rootDirFile = classDirFile.getParentFile().getParentFile();
			webRootPath = rootDirFile.getAbsolutePath() + File.separatorChar;
		}

		return webRootPath;
	}

	/**
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		Generator generator = new Generator();
		generator.setUrl("http://www.xinaiqin.com/aboutus.html");
		generator.setFilepath("E:/Project/pianoweb/");
		PageSnatch pageSnatch = new PageSnatch();
		System.out.println(pageSnatch.getWebRootPath());
		// pageSnatch.snatch(page);
		System.out.println("oK");
	}

}
