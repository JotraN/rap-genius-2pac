package com.example.rapgenius;

import java.io.File;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.text.Spannable;
import android.text.method.LinkMovementMethod;
import android.text.style.ForegroundColorSpan;
import android.text.style.URLSpan;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ProgressBar;
import android.widget.TextView;

@TargetApi(Build.VERSION_CODES.GINGERBREAD)
@SuppressLint("NewApi")
public class MainActivity extends Activity {
	public final static String EXTRA_MESSAGE = "com.example.rapgenius.MESSAGE";

	private TextView nameField, lyricsField;
	private ProgressBar loading;
	private URLObject urlObject;

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		initialize();

		new RetrieveNewsFeed().execute();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle presses on the action bar items
		switch (item.getItemId()) {
		case R.id.action_search:
			openSearch();
			return true;
		case R.id.action_delete:
			File file = new File(this.getFilesDir(), "favorites");
			file.delete();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private void openSearch() {
		Intent intent = new Intent(MainActivity.this, SearchActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(intent);
	}

	private void initialize() {
		nameField = (TextView) findViewById(R.id.nameText);
		lyricsField = (TextView) findViewById(R.id.lyricsText);
		loading = (ProgressBar) findViewById(R.id.progressBar1);

		// makes links operable
		lyricsField.setMovementMethod(LinkMovementMethod.getInstance());
	}

	private class RetrieveNewsFeed extends AsyncTask<Void, Void, String> {

		@Override
		protected String doInBackground(Void... names) {
			loading.setIndeterminate(true);
			urlObject = new NewsFeed();
			if (urlObject.openURL())
				urlObject.retrievePage();
			return urlObject.getPage();
		}

		protected void onPostExecute(String result) {
			nameField.setText("Home");
			lyricsField.setText(Html.fromHtml(result));
			loading.setIndeterminate(false);
			removeUnderline(lyricsField);
		}
	}

	private void removeUnderline(TextView textView) {
		Spannable text = (Spannable) textView.getText();
		URLSpan[] spans = text.getSpans(0, text.length(), URLSpan.class);
		for (URLSpan span : spans) {
			int start = text.getSpanStart(span);
			int end = text.getSpanEnd(span);
			text.removeSpan(span);
			// Grabs URL part of span and override text-decoration
			span = new URLOverride(span.getURL());
			text.setSpan(span, start, end, 0);
			// Color links dark green if verified by artist
			if (span.getURL().toString().contains("*"))
				text.setSpan(
						new ForegroundColorSpan(Color.argb(255, 38, 135, 31)),
						start, end, 0);
			// Else color dark orange
			else
				text.setSpan(
						new ForegroundColorSpan(Color.argb(255, 186, 95, 34)),
						start, end, 0);
		}
		textView.setText(text);
	}
}
