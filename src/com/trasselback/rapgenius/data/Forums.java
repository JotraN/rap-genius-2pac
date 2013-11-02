package com.trasselback.rapgenius.data;

import java.io.IOException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

public class Forums implements URLObject {
	private Document forumsPage;
	private String page = "";

	@Override
	public boolean openURL() {
		try {
			forumsPage = Jsoup.connect("http://rapgenius.com/rap-genius-forum")
					.timeout(10000).get();
			return true;
		} catch (IOException e) {
			try {
				forumsPage = Jsoup
						.connect("http://rapgenius.com/rap-genius-forum")
						.timeout(10000).get();
				return true;
			} catch (IOException e1) {
				page = "There was a problem with connecting to Rap Genius.<br>Rap Genius may be down.";
				return false;
			}
		}
	}

	@Override
	public void retrievePage() {
		Element content = forumsPage.getElementById("discussion_container");
		page = content.toString()
				.replace("href=\"", "href=\"forum_thread_clicked:")
				.replaceAll("\\s*?<h.+?>", "").replaceAll("\\s*?</h.+?>", "")
				.replaceAll("\\s*?<div.+?>", "")
				.replaceAll("\\s*?<a tool.+?>\\s*?</a>", "")
				.replaceAll("\\s*?<p.+?>", "<br>")
				.replaceAll("</p>\\s*?", "<br><br>")
				.replace("</div>", "");
	}

	@Override
	public String getPage() {
		return page;
	}

}
