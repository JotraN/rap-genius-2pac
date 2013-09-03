package com.trasselback.rapgenius;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import android.util.Log;

public class ForumThread implements URLObject {
	private Document forumsPage;
	private String page = "";
	private String url = "";
	private String name = "";

	public ForumThread(String url) {
		this.url = url;
	}

	@Override
	public boolean openURL() {
		try {
			forumsPage = Jsoup.connect(url).timeout(10000).get();
			return true;
		} catch (IOException e) {
			try {
				forumsPage = Jsoup
						.connect(url)
						.timeout(10000).get();
				return true;
			} catch (IOException e1) {
				page = "There was a problem with connecting to Rap Genius.<br>Rap Genius may be down.";
				return false;
			}
		}
	}

	public void retrieveName() {
		Pattern pattern = Pattern.compile("\\[(.+?)\\]");
		Matcher matcher = pattern.matcher(forumsPage.toString());
		if (matcher.find())
			name = forumsPage.toString().substring(matcher.start(1), matcher.end(1));
		else
			name = "Not found.\nDid you lose internet connection?";
	}

	@Override
	public void retrievePage() {
		Log.v("sss", "asass");
		Elements content = forumsPage.getElementsByClass("embedly_pro");
		page = content.toString()
				.replace("href=\"", "href=\"http://rapgenius.com")
				.replaceAll("\\s*?<h.+?>", "").replaceAll("\\s*?</h.+?>", "")
				.replaceAll("\\s*?<div.+?>", "")
				.replaceAll("\\s*?<a tool.+?>\\s*?</a>", "")
				.replaceAll("\\s*?<p.+?>", "<br>")
				.replaceAll("</p>\\s*?", "<br><br>").replace("</div>", "");
		Log.v("sss", "asddddddddddddddass");

	}

	@Override
	public String getPage() {
		return page;
	}

	public String getName() {
		return name;
	}
}
