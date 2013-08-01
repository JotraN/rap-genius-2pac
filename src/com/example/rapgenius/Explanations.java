package com.example.rapgenius;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

public class Explanations implements URLObject {
	private String data = "";
	private String name = "";
	private String url = "";
	private String page = "";
	private Document explainPage;

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
		Pattern pattern = Pattern.compile("/(\\d+?)/");
		Matcher matcher = pattern.matcher(data);
		if (matcher.find()) {
			String nums = data.substring(matcher.start(1), matcher.end(1));
			// Increment nums because RG increments it if verified by artist
			if (data.contains("*"))
				nums = (Integer.parseInt(nums) + 1) + "";
			url = "http://rapgenius.com/" + nums;
		} else
			url = "Not found.\nDid you lose internet connection?";
	}

	public boolean openURL() {
		retrieveUrl();
		try {
			explainPage = Jsoup.connect(url).get();
			return true;
		} catch (IOException e) {
			page += "Explanation failed to load.";
			return false;
		}
	}

	public void retrievePage() {
		if (openURL()) {
			Element content = explainPage.getElementById("main");
			// TODO load images
			page = content.toString().replaceAll("<img (?:alt.+ )?src.+>", "");
		}
	}

	public String getName() {
		return name;
	}

	public String getPage() {
		return page;
	}
}
