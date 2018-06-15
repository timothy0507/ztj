package ztj.util;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * 获取元素
 * 
 * @author Timothy
 * @version 1.0
 * @date 2011-9-15
 */
public class SnatchElement {

	/**
	 * 获取指定标签
	 * 
	 * @param content
	 *            文本
	 * @param tag
	 *            标签
	 * @return 标签值
	 */
	public static List<String> getElement(String content, String tag) {
		if (content == null || tag == null) {
			return null;
		}
		List<String> result = new ArrayList<String>();
		tag = "<" + tag.toLowerCase();
		String srcName = "src=";
		int index = 0;
		while (true) {
			int tagIndex = content.toLowerCase().indexOf(tag, index);
			if (tagIndex == -1) {
				break;
			}
			int srcIndex = content.indexOf(srcName, tagIndex + tag.length());
			int signIndex = content.indexOf(" ", srcIndex + srcName.length());
			index = signIndex;
			String element = content.substring(srcIndex + srcName.length(), signIndex);
			String leftSign = String.valueOf(element.charAt(0));
			if ("\"".equals(leftSign)) {
				element = element.substring(1, element.length());
			}
			String rightSign = String.valueOf(element.charAt(element.length() - 1));
			if ("\"".equals(rightSign)) {
				element = element.substring(0, element.length() - 1);
			}
			result.add(element);
		}

		return result;
	}

	/**
	 * 获取有效URL集合
	 * 
	 * @param content
	 *            网页内容
	 * @param tag
	 *            标签
	 * @return URL列表
	 */
	public static List<String> getUrlByElement(String content, String tag) {
		List<String> result = new ArrayList<String>();
		List<String> urlList = getElement(content, tag);
		for (String url : urlList) {
			if (canConnect(url)) {
				result.add(url);
			}
		}

		return result;
	}

	/**
	 * 验证URL是否有效
	 * 
	 * @param location
	 *            地址
	 * @return 是否有效
	 */
	public static synchronized Boolean canConnect(String location) {
		Boolean result = false;
		int count = 0;
		while (count < 3) {
			try {
				URL url = new URL(location);
				HttpURLConnection uc = (HttpURLConnection) url.openConnection();
				int state = uc.getResponseCode();
				if (state == 200) {
					result = true;
				}
				break;
			} catch (IOException e) {
				count++;
				e.printStackTrace();
			}
		}

		return result;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String content = "<P>\r<TABLE>\r\r<TR>\r<TD class=postcontent ondblclick=\"ajaxget('modcp.php?action=editmessage&amp;pid=22672&amp;tid=13052', 'postmessage_22672')\">\r<DIV class=\"postmessage defaultpost\">\r<DIV><IMG src=\"http://bbs.gangqinwang.com/attachments/month_0806/20080608_79c1ffce15f39471e8cc9tiqy2zjKsYb.gif\" border=0> <BR><IMG src=\"http://bbs.gangqinwang.com/images/Beijing2008/attachimg.gif\" border=0> <IMG src=\"http://bbs.gangqinwang.com/attachments/month0806/20080608_81dcc78cbb1a2ec34a731j0wnfPlEsgv.jpg\" border=0> <BR><IMG src=\"http://bbs.gangqinwang.com/attachments/month_0806/20080608_c1c4c6bab50f5246da9d02MTv2YogtHK.jpg\" border=0> <BR><BR><BR><BR><IMG src=\"http://bbs.gangqinwang.com/attachments/month_0806/20080608_9d433b7d4a4f3bd84d2abA80DhaY0icQ.jpg\" border=0> <BR><IMG src=\"http://bbs.gangqinwang.com/attachments/month_0806/20080608_d831c69bfdd1a0af2aa2GCcFxYDoYtTM.jpg\" border=0> <BR><IMG src=\"http://bbs.gangqinwang.com/attachments/month_0806/20080608_bd09860592ec487c763bFiNL3bhesNko.jpg\" border=0> </DIV></DIV>\r<DIV></DIV></TD></TR>\r<TR>\r<TD class=postauthor>\r<DIV class=imicons style=\"DISPLAY: none\"></DIV></TD>\r<TD class=postcontent>\r<DIV class=postactions></DIV></TD></TR></TABLE></P>";
		List<String> urlList = SnatchElement.getElement(content, "img");
		for (String url : urlList) {
			System.out.print(url);
			if (canConnect(url)) {
				System.out.println("有效！");
			} else {
				System.out.println("无效！");
			}
		}
		System.out.println("oK");
	}

}
