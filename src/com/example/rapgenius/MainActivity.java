package com.example.rapgenius;

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

public class MainActivity extends Activity {
	public final static String EXTRA_MESSAGE = "com.example.rapgenius.MESSAGE";

	private TextView nameField, lyricsField;
	private View mLoadingView;
	private View mContent;
	private URLObject urlObject;

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		initialize();
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
		case R.id.action_settings:
			openSettings();
			return true;
		case R.id.action_search:
			openSearch();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private void initialize() {
		nameField = (TextView) findViewById(R.id.nameText);
		lyricsField = (TextView) findViewById(R.id.lyricsText);
		mLoadingView = findViewById(R.id.loadingView);
		mContent = findViewById(R.id.scrollView1);
		mContent.setVisibility(View.GONE);
		nameField.setVisibility(View.VISIBLE);

		nameField.setText("Home");

		// makes links operable
		lyricsField.setMovementMethod(LinkMovementMethod.getInstance());

		// Get load home setting
		SharedPreferences sharedPref = PreferenceManager
				.getDefaultSharedPreferences(this);
		boolean loadHome = sharedPref.getBoolean(
				SettingsFragment.KEY_PREF_LOAD_HOME, false);
		if (loadHome)
			new RetrieveNewsFeed().execute();
		else {
			lyricsField.setText("Home disabled in settings.");
			mContent.setVisibility(View.VISIBLE);
			mLoadingView.setVisibility(View.GONE);
		}
	}

	private void openSettings() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			Intent intent = new Intent(MainActivity.this,
					SettingsActivity.class);
			startActivity(intent);
		} else {
			Intent intent = new Intent(MainActivity.this,
					SettingsPreferenceActivity.class);
			startActivity(intent);
		}
	}

	private void openSearch() {
		Intent intent = new Intent(MainActivity.this, SearchActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(intent);
	}

	private class RetrieveNewsFeed extends AsyncTask<Void, Void, String> {

		@Override
		protected String doInBackground(Void... names) {
			urlObject = new NewsFeed();
			if (urlObject.openURL())
				urlObject.retrievePage();
			return urlObject.getPage();
		}

		protected void onPostExecute(String result) {
			lyricsField.setText(Html.fromHtml(result));
			RemoveUnderLine.removeUnderline(lyricsField);
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1)
				CrossfadeAnimation.crossfade(getApplicationContext(), mContent,
						mLoadingView);
			else {
				mContent.setVisibility(View.VISIBLE);
				mLoadingView.setVisibility(View.GONE);
			}
		}
	}
}
