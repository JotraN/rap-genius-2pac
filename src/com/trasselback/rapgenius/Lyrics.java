package com.trasselback.rapgenius;

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
	private String message = "";
	private Document lyricsPage;
	
	Lyrics(String x) {
		// Rap Genius ignores punctuation in names, remove extra spaces,
		// cleaning inputed data
		// TODO replace song_clicked in function?
		message = x;
		song = x.trim().replaceAll("[\\.\\(\\),']", "").replace("-", " ")
				.replace("$", "s").replace("song_clicked:", "");
		url = "http://rapgenius.com/" + song.replace(' ', '-') + "-lyrics";
	}

	public boolean openURL() {
		try {
			lyricsPage = Jsoup.connect(url).timeout(10000).get();
			return true;
		} catch (IOException e) {
			name += song + " not found.";
			page = "There was a problem with finding the lyrics.";
			searchIt();
			return false;
		}
	}

	public void retrieveName() {
		name = lyricsPage.title().replaceAll("Lyrics \\|.+?Genius", "");
	}

	public void retrievePage() {
		Elements content = lyricsPage.getElementsByClass("lyrics");
		page = content.toString()
				.replace(
						"href=\"",
						"href=\"explanation_clicked:[" + name
								+ "]http://rapgenius.com");
		page = page.replaceAll("<a.+?class=\"no_annotation.+?>", "");
		// TODO Strange bug where rough explanations don't load.
		page = page.replace("rough", "accepted");
		if (page.contains("needs_exegesis")) {
			// Identify needs_exegesis editorials i.e. artist explanations
			page = page.replace("needs_exegesis\" href=\"explanation_clicked:",
					"needs_exegesis\" href=\"explanation_clicked:*");
			// Add a star to URLs associated with artist explanations
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

	public void searchIt() {
		String searchUrl = "http://google.com/search?q="
				+ message.replace(" ", "+") + "+rapgenius"
				+ "&as_qdr=all&num=20";
		try {
			Document searchPage = Jsoup
					.connect(searchUrl)
					.userAgent(
							"Mozilla/5.0 (Windows NT 6.2; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/29.0.1547.2 Safari/537.36")
					.referrer("http://www.google.com").get();
			Elements content = searchPage.getElementsByClass("r");
			String searchPageString = content.toString();
			Pattern pattern = Pattern
					.compile("<a href=\".+rapgenius\\.com.+lyrics\".+>.+</a>");
			Matcher matcher = pattern.matcher(searchPageString);
			boolean found = false;
			String searchResult = "";
			if (matcher.find()) {
				found = true;
			}
			while (found) {
				searchResult += searchPageString.substring(matcher.start(),
						matcher.end());
				if (!matcher.find(matcher.end()))
					found = false;
			}
			page += "<br>Did you mean any of the following?<br>"
					+ searchResult
							.replace("href=\"", "href=\"song_clicked:")
							.replace("</a>", "</a><br>")
							.replace("http://rapgenius.com", "")
							// Replace rock.rapgenius poetry.rapgenius
							// news.rapgenius
							.replaceAll("http://\\w+.rapgenius.com", "")
							// Formatting
							.replaceAll(" Lyrics.+?<em>Rap Genius</em>", "")
							.replaceAll("-\\s+<em>Rap Genius</em>", "")
							.replaceAll("\\s*?|\\s*?Poetry Genius", "")
							.replaceAll("- Poetry.+?<em>Rap Genius</em>", "");
		} catch (IOException e) {
		}
	}

	public String getName() {
		return name;
	}

	public String getPage() {
		return page;
	}
}
