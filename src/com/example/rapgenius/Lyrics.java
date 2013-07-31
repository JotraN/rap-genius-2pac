package com.example.rapgenius;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

public class Lyrics implements URLObject {
	private String name = "";
	private String page = "";
	private String url = "";
	private String song = "";
	private Document lyricsPage;

	Lyrics(String x) {
		// Rap Genius ignores punctuation in names, remove extra spaces,
		// cleaning name
		song = x.trim().replaceAll("[\\.\\(\\),']", "").replace("-", " ")
				.replace("$", "s");
		url = "http://rapgenius.com/" + song.replace(' ', '-') + "-lyrics";
	}

	public boolean openURL() {
		try {
			lyricsPage = Jsoup.connect(url).get();
			return true;
		} catch (IOException e) {
			name += song + " not found.";
			page += "Lyrics not found. If you believe this is an error try re-entering.";
			return false;
		}
	}

	public void retrieveName() {
		name = lyricsPage.title().replace("Lyrics | Rap Genius", "");
	}

	public void retrievePage() {
		Elements content = lyricsPage.getElementsByClass("lyrics");
		page = content.toString()
				.replace(
						"href=\"",
						"href=\"explanation_clicked:[" + name
								+ "]http://rapgenius.com");
		page = page.replaceAll("<a.+?class=\"no_annotation.+?>", "");

		if (page.contains("needs_exegesis")) {
			// Identify needs_exegesis editorials i.e. artist explanations
			page = page.replace("needs_exegesis\" href=\"explanation_clicked:",
					"needs_exegesis\" href=\"explanation_clicked:*");
			Pattern pattern = Pattern
					.compile("href=\"(.+?)\" data-editorial-state=\"needs_exegesis\"");
			Matcher matcher = pattern.matcher(page);
			boolean found = false;
			if (matcher.find())
				found = true;
			while (found) {
				String href = page.substring(matcher.start(1), matcher.end(1));
				page = page.replace(href, href + "*");
				// look for next link
				if (!matcher.find(matcher.end(1)))
					found = false;
			}
		}
	}

	public String getName() {
		return name;
	}

	public String getPage() {
		return page;
	}
}
