package com.trasselback.rapgenius.data;

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
			homePage = Jsoup.connect("http://rapgenius.com").timeout(10000)
					.get();
			return true;
		} catch (IOException e) {
			try {
				homePage = Jsoup.connect("http://rapgenius.com").timeout(10000)
						.get();
				return true;
			} catch (IOException e1) {
				page = "There was a problem with connecting to Rap Genius.<br>Rap Genius may be down.";
				return false;
			}
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
		if (page.length() != 0 && page.contains("<a"))
			page = page.substring(0, page.lastIndexOf("<a"));
	}

	public String getPage() {
		return page;
	}

}