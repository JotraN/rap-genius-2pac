package com.trasselback.rapgenius.data;

import java.io.IOException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

public class NewsFeed extends URLObject {
	// Jsoup document for home page
	private Document homePage;

	public NewsFeed() {
		url = "http://rapgenius.com";
	}

	public boolean openURL() {
		try {
			homePage = Jsoup.connect("http://rapgenius.com").timeout(10000)
					.get();
			return true;
		} catch (IOException e) {
			try {
				// Try looking for it again if any network pipes broke the first
				// time
				homePage = Jsoup.connect("http://rapgenius.com").timeout(10000)
						.get();
				return true;
			} catch (IOException e1) {
				htmlPage = "There was a problem with connecting to Rap Genius.<br>Rap Genius may be down.";
				return false;
			}
		}
	}

	public void retrievePage() {
		Elements content = homePage.getElementsByClass("newsfeed");
		htmlPage = content.toString().replace("</span>", "</span><br>")
				.replace("href=\"", "href=\"song_clicked:")
				// Format the page
				.replace("<p class=\"label\">", "").replace("</p>", "<br>")
				// Remove HTML header
				.replaceAll("<h1.+?</h1>", "");
		if (htmlPage.length() != 0 && htmlPage.contains("<a"))
			htmlPage = htmlPage.substring(0, htmlPage.lastIndexOf("<a"));
	}
}