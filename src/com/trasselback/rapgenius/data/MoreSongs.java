package com.trasselback.rapgenius.data;

import java.io.IOException;

import org.jsoup.Jsoup;
import org.jsoup.select.Elements;

public class MoreSongs extends URLObject{
	private String artistName = "More Songs by ";
	private String songName = "";


	public MoreSongs(String x) {
		// Removes punctuation and extra spaces in names, clean the inputed data
		songName = x.trim().replaceAll("[\\.\\(\\),']", "").replace("-", " ")
				.replace("$", "s").replace("song_clicked:", "");
		url = "http://rapgenius.com/" + songName.replace(' ', '-') + "-lyrics";
	}

	public boolean openURL() {
		try {
			pageDocument = Jsoup.connect(url).timeout(10000).get();
			return true;
		} catch (IOException e) {
			try {
				// Try again if any network pipes broke initially
				pageDocument = Jsoup.connect(url).timeout(10000).get();
				return true;
			} catch (IOException e1) {
				artistName = "More songs not found.";
				htmlPage = "There was a problem accessing Rap Genius.<br>Try reloading.";
				return false;
			}
		}
	}

	public void retrieveName() {
		artistName += pageDocument.title().replaceAll(" –.+?\\|.+?Genius", "");
	}

	public void retrievePage() {
		Elements content = pageDocument.getElementsByClass("song_list");
		htmlPage = content.toString()
				.replaceAll("<span class=\"track_number\">.+?</span>", "")
				.replaceAll("\\s*?<.+?\">\\s*", "").replace("</a>", "<br>");
		htmlPage = htmlPage.replaceAll("\\s*</.+?>\\s*", "").replace("\n", "");
	}

	public String getName() {
		return artistName;
	}
}
