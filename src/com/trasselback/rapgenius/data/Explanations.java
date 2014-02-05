package com.trasselback.rapgenius.data;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;

public class Explanations extends URLObject {
	private String dataLink = ""; // Artist Name + SongID + URL
	private String artistName = "";
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

	public boolean retrievedUrl() {
		dataLink += "/";
		// Explanation explained by the artist
		boolean artistExplanationLink = false;
		if (dataLink.contains("*")) {
			dataLink = dataLink.replace("*", "");
			artistExplanationLink = true;
		}
		// Look for explanation id
		Pattern pattern = Pattern.compile("/(\\d+?)/");
		Matcher matcher = pattern.matcher(dataLink);
		if (matcher.find()) {
			explanationID = dataLink
					.substring(matcher.start(1), matcher.end(1));
			if (artistExplanationLink)
				// Increment ID because Rap Genius increments explanations
				// if it was verified by an artist
				explanationID = (Integer.parseInt(explanationID) + 1) + "";
			url = "http://rapgenius.com/" + explanationID;
			return true;
		} else
			return false;
	}

	public boolean openedURL() {
		try {
			pageDocument = Jsoup.connect(url).ignoreContentType(true)
					.timeout(10000).get();
			return true;
		} catch (IOException e) {
			try {
				// Try again if any network pipes broke initially
				pageDocument = Jsoup.connect(url).ignoreContentType(true)
						.timeout(10000).get();
				return true;
			} catch (IOException e1) {
				htmlPage = "There was a problem with connecting to Rap Genius.<br>Rap Genius may be down<br>or your internet connection may be too weak.";
				return false;
			}
		}
	}

	public void retrievePage() {
		Element dataIDs = pageDocument.getElementById("main");
		String text = dataIDs.toString();
		// Remove images
		htmlPage = text.toString().replaceAll("<img.+?src.+?/>", "");
		htmlPage = htmlPage.replaceAll("<p class=\"video.+?/p>", "");
		// Fix any local Rap Genius links
		htmlPage = htmlPage.replace("href=\"/", "href=\"http://rapgenius.com/");
	}

	public String getName() {
		return artistName;
	}
}
