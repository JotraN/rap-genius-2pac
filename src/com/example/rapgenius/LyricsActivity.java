package com.example.rapgenius;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

public class LyricsActivity extends Activity {
	private TextView nameField, lyricsField;
	private View mLoadingView;
	private View mContent;
	private URLObject urlObject;
	private String message = "";
	private boolean hideFavs = false;
	private boolean cacheLyricsEnabled = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_lyrics);

		setupActionBar();
		initialize();
		startLyrics();
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private void setupActionBar() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			getActionBar().setDisplayHomeAsUpEnabled(true);
			getActionBar().setDisplayShowTitleEnabled(false);
		}
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
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
			FavoritesManager.addFavorites(getApplicationContext(), message);
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

		// makes links operable
		lyricsField.setMovementMethod(LinkMovementMethod.getInstance());

		// Get cache lyrics setting
		SharedPreferences sharedPref = PreferenceManager
				.getDefaultSharedPreferences(this);
		cacheLyricsEnabled = sharedPref.getBoolean(
				SettingsFragment.KEY_PREF_CACHE_LYRICS, false);
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

	private void setCache() {
		mLoadingView.setVisibility(View.GONE);
		mContent.setVisibility(View.VISIBLE);
		String cachedData = CacheManager.getCache(getApplicationContext(),
				message);
		String nameData = cachedData.substring(0, cachedData.indexOf('<'));
		String lyricsData = cachedData.substring(cachedData.indexOf('<'));
		nameField.setText(nameData);
		lyricsField.setText(Html.fromHtml(lyricsData));
		RemoveUnderLine.removeUnderline(lyricsField);
	}

	// Find what started lyrics activity and continue from there
	private void startLyrics() {
		if (getIntent().getData() == null) {
			message = getIntent().getStringExtra(MainActivity.EXTRA_MESSAGE);
			if (CacheManager.getCache(getApplicationContext(), message)
					.length() <= 0)
				new RetrieveLyricsTask().execute(message);
			else
				setCache();
		} else if (getIntent().getDataString().contains("explanation_clicked")) {
			// Hide favorites icon
			hideFavs = true;
			new RetrieveExplanationsTask().execute();
		} else if (getIntent().getDataString().contains("song_clicked")) {
			message = getIntent().getDataString();
			// remove the -lyrics at the end of the URL
			message = message.substring(message.indexOf("/") + 1,
					message.length() - 7);
			if (CacheManager.getCache(getApplicationContext(), message)
					.length() <= 0)
				new RetrieveLyricsTask().execute(message);
			else
				setCache();
		} else if (getIntent().getDataString().contains("fav_clicked")) {
			message = getIntent().getDataString();
			message = message.substring(message.indexOf(":") + 1);
			if (CacheManager.getCache(getApplicationContext(), message)
					.length() <= 0)
				new RetrieveLyricsTask().execute(message);
			else
				setCache();
		}
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

		protected void onPostExecute(String result) {
			nameField.setText(((Lyrics) urlObject).getName());
			lyricsField.setText(Html.fromHtml(result));
			RemoveUnderLine.removeUnderline(lyricsField);
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
				CrossfadeAnimation.crossfade(getApplicationContext(), mContent,
						mLoadingView);
			else {
				mContent.setVisibility(View.VISIBLE);
				mLoadingView.setVisibility(View.GONE);
			}
			if (cacheLyricsEnabled)
				CacheManager.saveData(getApplicationContext(), message,
						nameField.getText().toString() + result);
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
			RemoveUnderLine.removeUnderline(lyricsField);
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
				CrossfadeAnimation.crossfade(getApplicationContext(), mContent,
						mLoadingView);
			else {
				mContent.setVisibility(View.VISIBLE);
				mLoadingView.setVisibility(View.GONE);
			}
		}

	}
}
