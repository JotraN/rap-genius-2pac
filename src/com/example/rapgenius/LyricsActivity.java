package com.example.rapgenius;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
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
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class LyricsActivity extends Activity {
	private TextView nameField, lyricsField;
	private View mLoadingView;
	private View mContent;
	private int mShortAnimationDuration;
	private URLObject urlObject;
	private String message = "";
	private boolean hideFavs = false;

	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_lyrics);
		// Show the Up button in the action bar.
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			getActionBar().setDisplayHomeAsUpEnabled(true);
			getActionBar().setDisplayShowTitleEnabled(false);
		}

		initialize();

		if (getIntent().getData() == null) {
			message = getIntent().getStringExtra(MainActivity.EXTRA_MESSAGE);
			new RetrieveLyricsTask().execute(message);
		} else if (getIntent().getDataString().contains("explanation_clicked")) {
			// Hide favorites icon
			hideFavs = true;
			new RetrieveExplanationsTask().execute();
		} else if (getIntent().getDataString().contains("song_clicked")) {
			message = getIntent().getDataString();
			// remove the -lyrics at the end of the URL
			message = message.substring(message.indexOf("/") + 1,
					message.length() - 7);
			new RetrieveLyricsTask().execute(message);
		} else if (getIntent().getDataString().contains("fav_clicked")) {
			message = getIntent().getDataString();
			message = message.substring(message.indexOf(":") + 1);
			new RetrieveLyricsTask().execute(message);
		}

	}

	/**
	 * Set up the {@link android.app.ActionBar}, if the API is available.
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private void setupActionBar() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			getActionBar().setDisplayHomeAsUpEnabled(true);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.lyrics, menu);
		if (hideFavs) {
			MenuItem item = menu.findItem(R.id.action_favorite);
			item.setVisible(false);
			this.invalidateOptionsMenu();
		}
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			onBackPressed();
			return true;
		case R.id.action_settings:
			openSettings();
			return true;
		case R.id.action_search:
			openSearch();
			return true;
		case R.id.action_favorite:
			addFavorite();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private void initialize() {
		nameField = (TextView) findViewById(R.id.nameText);
		lyricsField = (TextView) findViewById(R.id.lyricsText);
		mLoadingView = findViewById(R.id.loadingView);
		mContent = findViewById(R.id.infoView);
		mContent.setVisibility(View.GONE);

		mShortAnimationDuration = getResources().getInteger(
				android.R.integer.config_shortAnimTime);
		// makes links operable
		lyricsField.setMovementMethod(LinkMovementMethod.getInstance());
	}

	private void openSettings() {
		Intent intent = new Intent(LyricsActivity.this, SettingsActivity.class);
		startActivity(intent);
	}

	private void openSearch() {
		Intent intent = new Intent(LyricsActivity.this, SearchActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(intent);
	}

	private class RetrieveLyricsTask extends AsyncTask<String, Void, String> {

		@Override
		protected String doInBackground(String... names) {
			urlObject = new Lyrics(names[0]);
			if (urlObject.openURL()) {
				((Lyrics) urlObject).retrieveName();
				urlObject.retrievePage();
			}
			return urlObject.getPage();
		}

		@SuppressLint("NewApi")
		protected void onPostExecute(String result) {
			nameField.setText(((Lyrics) urlObject).getName());
			lyricsField.setText(Html.fromHtml(result));
			removeUnderline(lyricsField);
			crossfade();
		}
	}

	private class RetrieveExplanationsTask extends
			AsyncTask<Void, Void, String> {

		@Override
		protected String doInBackground(Void... params) {
			urlObject = new Explanations(getIntent().getDataString());
			((Explanations) urlObject).retrieveName();
			urlObject.retrievePage();
			return urlObject.getPage();
		}

		protected void onPostExecute(String result) {
			nameField.setText(((Explanations) urlObject).getName());
			lyricsField.setText(Html.fromHtml(result));
			removeUnderline(lyricsField);
			crossfade();
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

	private String getFavorites() {
		String favs = "";

		try {
			InputStream inputStream = openFileInput("favorites");

			if (inputStream != null) {
				InputStreamReader inputStreamReader = new InputStreamReader(
						inputStream);
				BufferedReader bufferedReader = new BufferedReader(
						inputStreamReader);
				String line = "";
				StringBuilder stringBuilder = new StringBuilder();

				while ((line = bufferedReader.readLine()) != null) {
					stringBuilder.append(line);
				}

				inputStream.close();
				favs = stringBuilder.toString();
			}
		} catch (FileNotFoundException e) {
		} catch (IOException e) {
		}

		return favs;
	}

	private void addFavorite() {
		String currFavs = getFavorites();
		// TODO better dash removal for favorites list
		String message = this.message.replace("-", " ");
		// Removes song if already in file
		if (currFavs.contains(message)) {
			FileOutputStream outputStream;
			try {
				outputStream = openFileOutput("favorites", Context.MODE_PRIVATE);
				currFavs = currFavs.replace("<a href=\"fav_clicked:" + message
						+ "\">" + message + "</a><br>", "");
				outputStream.write(currFavs.getBytes());
				outputStream.close();
			} catch (Exception e) {
				e.printStackTrace();
			}

			Context context = getApplicationContext();
			CharSequence text = "Song removed from favorites list.";
			int duration = Toast.LENGTH_SHORT;

			Toast toast = Toast.makeText(context, text, duration);
			toast.show();
		}
		// Sets limit on favorites list
		else if (currFavs.length() <= 650) {
			String favorites = currFavs + "<a href=\"fav_clicked:" + message
					+ "\">" + message + "</a><br>\n";
			FileOutputStream outputStream;
			try {
				outputStream = openFileOutput("favorites", Context.MODE_PRIVATE);
				outputStream.write(favorites.getBytes());
				outputStream.close();
			} catch (Exception e) {
				e.printStackTrace();
			}

			Context context = getApplicationContext();
			CharSequence text = "Song added to favorites list.";
			int duration = Toast.LENGTH_SHORT;

			Toast toast = Toast.makeText(context, text, duration);
			toast.show();
		} else {
			Context context = getApplicationContext();
			CharSequence text = "Can't add, favorites list full.";
			int duration = Toast.LENGTH_SHORT;

			Toast toast = Toast.makeText(context, text, duration);
			toast.show();
		}
	}

	@SuppressLint("NewApi")
	private void crossfade() {
		mContent.setAlpha(0f);
		mContent.setVisibility(View.VISIBLE);

		mContent.animate().alpha(1f).setDuration(mShortAnimationDuration)
				.setListener(null);

		mLoadingView.animate().alpha(0f).setDuration(mShortAnimationDuration)
				.setListener(new AnimatorListenerAdapter() {
					@Override
					public void onAnimationEnd(Animator animation) {
						mLoadingView.setVisibility(View.GONE);
					}
				});
	}

}
