package com.trasselback.rapgenius.data;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringEscapeUtils;
import org.jsoup.Jsoup;
import org.jsoup.select.Elements;

public class Explanations extends URLObject {
	private String dataLink = "";	// Artist Name + SongID + URL
	private String artistName = "";
	private String songID = "";
	private String explanationID = "";

	public Explanations(String clickedLink) {
		dataLink = clickedLink;
	}

	public void retrieveName() {
		Pattern pattern = Pattern.compile("\\[(.+?)\\]");
		Matcher matcher = pattern.matcher(dataLink);
		if (matcher.find())
			artistName = dataLink.substring(matcher.start(1), matcher.end(1));
		else
			artistName = "Not found.\nDid you lose internet connection?";
	}

	public void retrieveUrl() {
		retrieveSongID();
		dataLink += "/";
		Pattern pattern = Pattern.compile("/(\\d+?)/");
		Matcher matcher = pattern.matcher(dataLink);
		if (matcher.find()) {
			explanationID = dataLink.substring(matcher.start(1), matcher.end(1));
			// Explanation was verified by the artist
			if (dataLink.contains("*"))
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
		Matcher matcher = pattern.matcher(dataLink);
		if (matcher.find()) {
			songID = dataLink.substring(matcher.start(1), matcher.end(1));
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
		return artistName;
	}
}
