package com.trasselback.rapgenius;

import java.io.IOException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

public class NewsFeed implements URLObject {
	private String page = "";
	private Document homePage;

	public NewsFeed() {
	}

	public boolean openURL() {
		try {
			homePage = Jsoup.connect("http://rapgenius.com").get();
			return true;
		} catch (IOException e) {
			page = "Home page not loaded.";
			return false;
		}
	}

	public void retrievePage() {
		Elements content = homePage.getElementsByClass("newsfeed");
		page = content.toString().replace("</span>", "</span><br>")
				.replace("href=\"", "href=\"song_clicked:")
				// Formatting
				.replace("<p class=\"label\">", "").replace("</p>", "<br>")
				// remove header
				.replaceAll("<h1.+?</h1>", "");
		// Remove trailing links
		page = page.substring(0, page.length() - 260);
	}

	public String getPage() {
		return page;
	}

}
