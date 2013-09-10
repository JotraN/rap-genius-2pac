package com.trasselback.rapgenius;

import java.io.IOException;
import java.util.ArrayList;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import android.util.Log;

public class ForumThread implements URLObject {
	private Document forumsPage;
	private String page = "";
	private String url = "";
	private String name = "";
	private ArrayList<String> posts = new ArrayList<String>();

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
				forumsPage = Jsoup.connect(url).timeout(10000).get();
				return true;
			} catch (IOException e1) {
				page = "There was a problem with connecting to Rap Genius.<br>Rap Genius may be down.";
				return false;
			}
		}
	}

	public void retrieveName() {
		name = forumsPage.title().replace(" | Rap Genius", "");
	}

	@Override
	public void retrievePage() {
	}

	public void retrievePosts() {
		Elements content = forumsPage.getElementsByClass("embedly_pro");
		Elements content1 = forumsPage.getElementsByClass("user_details");
		for (int i = 0; i < content.toArray().length; i++) {
			posts.add(content.toArray()[i].toString()
					.replace("href=\"/", "href=\"http://rapgenius.com/")
					.replaceAll("<h\\d+?>", "<h6>")
					.replaceAll("</h\\d+?>", "</h6>")
					// TODO Remove link
					.replace("><img", "><a href=\"http://google.com\"><img")
					+ "<br>Posted by:"
					+ content1.toArray()[i]
							.toString()
							.replaceAll("<p.+?>", ", ")
							.replaceAll("<div.+?>", "")
							.replace("Editor", "")
							.replace("href=\"/", "href=\"http://rapgenius.com/")
							.replaceAll("</?h\\d+?>", ""));
		}
	}

	@Override
	public String getPage() {
		return page;
	}

	public ArrayList<String> getPosts() {
		return posts;
	}

	public String getName() {
		return name;
	}
}
