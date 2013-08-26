package com.trasselback.rapgenius;

import java.io.IOException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

public class MoreSongs {
	private String name = "More Songs by ";
	private String page = "";
	private String url = "";
	private String song = "";
	private Document lyricsPage;

	MoreSongs(String x) {
		// Rap Genius ignores punctuation in names, remove extra spaces,
		// cleaning inputed data
		// TODO replace song_clicked in function?
		song = x.trim().replaceAll("[\\.\\(\\),']", "").replace("-", " ")
				.replace("$", "s").replace("song_clicked:", "");
		url = "http://rapgenius.com/" + song.replace(' ', '-') + "-lyrics";
	}

	public boolean openURL() {
		try {
			lyricsPage = Jsoup.connect(url).timeout(10000).get();
			return true;
		} catch (IOException e) {
			try {
				lyricsPage = Jsoup.connect(url).timeout(10000).get();
				return true;
			} catch (IOException e1) {
				name = "More songs not found.";
				page = "There was a problem accessing Rap Genius.<br>Try reloading.";
				return false;
			}
		}
	}

	public void retrieveName() {
		name += lyricsPage.title().replaceAll(" –.+Lyrics \\| Rap Genius", "");
	}

	public void retrievePage() {
		Elements content = lyricsPage.getElementsByClass("song_list");
		page = content.toString()
				.replace(
						"href=\"",
						"href=\"song_clicked:").replace("</a>", "</a><br>");
	}

	public String getName() {
		return name;
	}

	public String getPage() {
		return page;
	}
}
