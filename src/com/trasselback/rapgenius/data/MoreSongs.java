package com.trasselback.rapgenius.data;

import java.io.IOException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

public class MoreSongs {
	private String name = "More Songs by ";
	private String page = "";
	private String url = "";
	private String song = "";
	// Jsoup document for more songs page
	private Document morePage;

	public MoreSongs(String x) {
		// Removes punctuation and extra spaces in names, clean the inputed data
		song = x.trim().replaceAll("[\\.\\(\\),']", "").replace("-", " ")
				.replace("$", "s").replace("song_clicked:", "");
		url = "http://rapgenius.com/" + song.replace(' ', '-') + "-lyrics";
	}

	public boolean openURL() {
		try {
			morePage = Jsoup.connect(url).timeout(10000).get();
			return true;
		} catch (IOException e) {
			try {
				// Try looking for it again if any network pipes broke the first
				// time
				morePage = Jsoup.connect(url).timeout(10000).get();
				return true;
			} catch (IOException e1) {
				name = "More songs not found.";
				page = "There was a problem accessing Rap Genius.<br>Try reloading.";
				return false;
			}
		}
	}

	public void retrieveName() {
		name += morePage.title().replaceAll(" –.+?\\|.+?Genius", "");
	}

	public void retrievePage() {
		Elements content = morePage.getElementsByClass("song_list");
		page = content.toString()
				.replaceAll("<span class=\"track_number\">.+?</span>", "")
				.replaceAll("\\s*?<.+?\">\\s*", "").replace("</a>", "<br>");
		page = page.replaceAll("\\s*</.+?>\\s*", "").replace("\n", "");
	}

	public String getName() {
		return name;
	}

	public String getPage() {
		return page;
	}
}
