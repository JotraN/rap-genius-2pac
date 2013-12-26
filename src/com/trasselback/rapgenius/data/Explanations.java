package com.trasselback.rapgenius.data;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringEscapeUtils;
import org.jsoup.Jsoup;
import org.jsoup.select.Elements;

public class Explanations extends URLObject {
	// Explanation link clicked + app identifiers ([name]URL)
	private String data = "";
	private String name = "";
	private String songID = "";
	private String explanationID = "";

	public Explanations(String x) {
		data = x;
	}

	public void retrieveName() {
		Pattern pattern = Pattern.compile("\\[(.+?)\\]");
		Matcher matcher = pattern.matcher(data);
		if (matcher.find())
			name = data.substring(matcher.start(1), matcher.end(1));
		else
			name = "Not found.\nDid you lose internet connection?";
	}

	public void retrieveUrl() {
		retrieveSongID();
		data += "/";
		Pattern pattern = Pattern.compile("/(\\d+?)/");
		Matcher matcher = pattern.matcher(data);
		if (matcher.find()) {
			explanationID = data.substring(matcher.start(1), matcher.end(1));
			// Explanation was verified by the artist
			if (data.contains("*"))
				// Increment ID because Rap Genius increments explanations if it
				// was verified by an artist
				explanationID = (Integer.parseInt(explanationID) + 1) + "";
			url = "http://rapgenius.com/annotations/for_song_page?song_id="
					+ songID;
		} else
			url = "Not found.\nDid you lose internet connection?";
	}

	public void retrieveSongID() {
		Pattern pattern = Pattern.compile("](\\d+?)h");
		Matcher matcher = pattern.matcher(data);
		if (matcher.find()) {
			songID = data.substring(matcher.start(1), matcher.end(1));
		} else
			url = "Not found.\nDid you lose internet connection?";
	}

	public boolean openedURL() {
		try {
			pageDocument = Jsoup.connect(url).ignoreContentType(true).get();
			return true;
		} catch (IOException e) {
			try {
				// Try again if any network pipes broke initially
				pageDocument = Jsoup.connect(url).ignoreContentType(true).get();
				return true;
			} catch (IOException e1) {
				htmlPage += "Explanation failed to load.<br>Rap Genius may be down.";
				return false;
			}
		}
	}

	public void retrievePage() {
		htmlPage = pageDocument.toString().replace("\\n", "")
				.replace("\\&quot;", "").replace("&quot;", "\"");
		pageDocument = Jsoup.parse(htmlPage);
		Elements content = pageDocument.getElementsByClass("annotation_body");
		htmlPage = content.toString();
		pageDocument = Jsoup.parse(htmlPage);
		String text = pageDocument.getElementsByAttributeValue("data-id",
				explanationID).toString();
		// Remove images
		htmlPage = text.toString().replaceAll("<img.+?src.+?/>", "");
		htmlPage = htmlPage.replaceAll("<p class=\"video.+?/p>", "");
		// Convert UNICODE
		htmlPage = StringEscapeUtils.unescapeJava(htmlPage);
		// Fix any local Rap Genius links
		htmlPage = htmlPage.replace("href=\"/", "href=\"http://rapgenius.com/");

	}

	public String getName() {
		return name;
	}
}
