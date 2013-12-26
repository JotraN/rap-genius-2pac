package com.trasselback.rapgenius.data;

import java.io.IOException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public abstract class URLObject {
	protected String htmlPage = "";
	protected String url = "";
	protected Document pageDocument = null;

	public abstract void retrievePage();

	public boolean openedURL() {
		try {
			pageDocument = Jsoup.connect(url).timeout(10000).get();
			return true;
		} catch (IOException e) {
			return false;
		}
	};

	public String getPage() {
		return htmlPage;
	};
}
