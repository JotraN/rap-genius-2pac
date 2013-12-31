package com.trasselback.rapgenius.data;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

public class Lyrics extends URLObject {
	private String artistName = "";
	private String songName = "";
	private String songID = "";
	private String searchMessage = "";

	public Lyrics(String searchInput) {
		searchMessage = searchInput;
		// Cleans up searches like A$AP or J. Cole
		songName = searchMessage.trim().replace("$", "s").replace(".", "");
		url = "http://rapgenius.com/" + songName.replace(' ', '-') + "-lyrics";
	}

	public void retrieveName() {
		if (pageDocument != null) {
			artistName = pageDocument.title();
			// Remove unnecessary words from title
			artistName = artistName.replaceAll("Lyrics \\|.+?Genius", "");
		}
	}

	public void retrievePage() {
		retrieveSongID();
		Elements content = pageDocument.getElementsByClass("lyrics");
		htmlPage = content.toString().replaceAll(
				"href=\".+?\".+?\"",
				"href=\"explanation_clicked:[" + artistName + "]" + songID
						+ "http://rapgenius.com/");
		htmlPage = htmlPage.replaceAll("<a\\s+?class=\"no_annotation.+?>", "");
		// Fixes bug where some rough explanations don't load.
		htmlPage = htmlPage.replace("=\"rough", "=\"accepted");
		if (htmlPage.contains("needs_exegesis")) {
			replaceArtistIdentifiedExplanations();
		}
	}

	public void retrieveSongID() {
		songID = pageDocument.getElementsByAttribute("data-pusher_channel")
				.toString();
		Pattern pattern = Pattern.compile("data-id=\"(\\d+?)\"");
		Matcher matcher = pattern.matcher(songID);
		if (matcher.find()) {
			songID = songID.substring(matcher.start(1), matcher.end(1));
		}
	}

	public void replaceArtistIdentifiedExplanations() {
		// Identify needs_exegesis editorials i.e. artist explanations
		htmlPage = htmlPage.replace(
				"needs_exegesis\" href=\"explanation_clicked:",
				"needs_exegesis\" href=\"explanation_clicked:*");
		// Add a star to URLs associated with artist explanations to be
		// identified to change its color
		Pattern pattern = Pattern
				.compile("href=\"(.+?)\" data-editorial-state=\"needs_exegesis\"");
		Matcher matcher = pattern.matcher(htmlPage);
		boolean found = false;
		if (matcher.find())
			found = true;
		while (found) {
			String href = htmlPage.substring(matcher.start(1), matcher.end(1));
			htmlPage = htmlPage.replace(href, href + "*");
			// look for next link
			if (!matcher.find(matcher.end(1)))
				found = false;
		}
	}

	// Google the song and name, looking for a rap genius link
	public void googleIt() {
		String searchUrl = "http://google.com/search?q="
				+ searchMessage.replace(" ", "+") + "+site:rapgenius.com"
				+ "&as_qdr=all&num=20";
		try {
			// UserAgent necessary to connect to google
			Document searchPage = Jsoup
					.connect(searchUrl)
					.userAgent(
							"Mozilla/5.0 (Windows NT 6.2; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/29.0.1547.2 Safari/537.36")
					.referrer("http://www.google.com").get();
			Elements content = searchPage.getElementsByClass("r");
			String googleHTML = content.toString();
			findRapGeniusLinks(googleHTML);
		} catch (IOException e) {
			artistName += songName + " not found.";
			htmlPage = "There was a problem with finding the lyrics.";
		}
	}

	public void findRapGeniusLinks(String googlePage) {
		Pattern pattern = Pattern
				.compile("<a href=\".+rapgenius\\.com.+lyrics\".+>.+</a>");
		Matcher matcher = pattern.matcher(googlePage);
		boolean foundLink = false;
		String rapGeniusLinks = "";
		if (matcher.find()) {
			foundLink = true;
		}
		while (foundLink) {
			rapGeniusLinks += googlePage.substring(matcher.start(),
					matcher.end());
			if (!matcher.find(matcher.end()))
				foundLink = false;
		}
		htmlPage = "<br>Did you mean any of the following?<br>"
				+ rapGeniusLinks
						.replace("href=\"", "href=\"song_clicked:")
						.replace("</a>", "</a><br>")
						.replace("http://rapgenius.com", "")
						// Replace rock.rapgenius.com poetry.rapgenius.com
						// news.rapgenius.com
						.replaceAll("http://\\w+.rapgenius.com", "")
						// Format the search results
						.replaceAll(" Lyrics.+?Rap Genius", "")
						.replaceAll("-\\s+Rap Genius", "")
						.replaceAll("\\s*?|\\s*?Poetry Genius", "")
						.replaceAll("- Poetry.+?Rap Genius", "");
	}

	public String getName() {
		return artistName;
	}
}
