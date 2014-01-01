package com.trasselback.rapgenius.data;

import java.io.IOException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public abstract class URLObject {
	protected String htmlPage = "";
	protected String url = "";
	protected Document pageDocument = null;

	public abstract void retrievePage();

	public boolean isOnline(Context context) {
		ConnectivityManager connMgr = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
		return (networkInfo != null && networkInfo.isConnected());
	}

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
