package com.trasselback.rapgenius;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.TypedValue;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.MenuItem;

public class MoreSongsActivity extends SherlockActivity {
	private TextView nameField, songsField;
	private View mLoadingView;
	private View mContent;
	private String message;
	private MoreSongs moreSongs;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_more_songs);
		setupActionBar();

		nameField = (TextView) findViewById(R.id.nameText);
		Typeface tf = Typeface.createFromAsset(getAssets(),
				"fonts/roboto_thin.ttf");
		nameField.setTypeface(tf);
		songsField = (TextView) findViewById(R.id.lyricsText);
		mLoadingView = findViewById(R.id.loadingView);
		mContent = findViewById(R.id.infoView);
		mContent.setVisibility(View.GONE);

		((ProgressBar) mLoadingView).setIndeterminateDrawable(getResources()
				.getDrawable(R.xml.progress_animation));

		// necessary for 2.3 for some reason
		songsField.setMovementMethod(LinkMovementMethod.getInstance());

		message = getIntent().getStringExtra(MainActivity.EXTRA_MESSAGE);
		new RetrieveMoreSongs().execute(message);
	}

	private void setupActionBar() {
		getSupportActionBar().setTitle("MORE SONGS");
		getSupportActionBar().setDisplayShowHomeEnabled(false);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setDisplayShowTitleEnabled(true);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			onBackPressed();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		SharedPreferences sharedPref = PreferenceManager
				.getDefaultSharedPreferences(this);

		// Update text size
		int size = Integer.parseInt(sharedPref.getString(
				SettingsFragment.KEY_PREF_TEXT_SIZE, "22"));
		songsField.setTextSize(TypedValue.COMPLEX_UNIT_SP, size);
		nameField.setTextSize(TypedValue.COMPLEX_UNIT_SP, size + 10);

		// Update colors
		String color = sharedPref.getString(
				SettingsFragment.KEY_PREF_DEFAULT_TEXT_COLOR, "Default");
		if (!color.contains("Default")) {
			ColorManager.setColor(this, songsField, color);
		} else
			songsField.setTextColor(getResources().getColor(R.color.Gray));
		color = sharedPref.getString(SettingsFragment.KEY_PREF_TITLE_COLOR,
				"Default");
		if (!color.contains("Default")) {
			ColorManager.setColor(this, nameField, color);
		} else
			nameField.setTextColor(getResources().getColor(R.color.LightGray));
		color = sharedPref.getString(
				SettingsFragment.KEY_PREF_EXPLAINED_LYRICS_COLOR, "Default");
		if (!color.contains("Default")) {
			ColorManager.setLinkColor(this, songsField, color);
		} else
			songsField
					.setLinkTextColor(getResources().getColor(R.color.Orange));
		color = sharedPref.getString(
				SettingsFragment.KEY_PREF_BACKGROUND_COLOR, "Default");
		if (!color.contains("Default")) {
			ColorManager.setBackgroundColor(this, color);
		} else
			getWindow().setBackgroundDrawableResource(R.color.LightBlack);
	}

	private class RetrieveMoreSongs extends AsyncTask<String, Void, String> {

		@Override
		protected String doInBackground(String... names) {
			moreSongs = new MoreSongs(names[0]);
			ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
			if (networkInfo != null && networkInfo.isConnected()) {
				if (moreSongs.openURL()) {
					moreSongs.retrievePage();
					moreSongs.retrieveName();
				}
				return moreSongs.getPage();

			} else
				return "No internet connection found.";
		}

		protected void onPostExecute(String result) {
			nameField.setText(moreSongs.getName());
			songsField.setText(Html.fromHtml(result));
			RemoveUnderLine.removeUnderline(songsField);
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1)
				CrossfadeAnimation.crossfade(getApplicationContext(), mContent,
						mLoadingView);
			else {
				// lyricsField.setMovementMethod(new LinkMovementMethod());
				mContent.setVisibility(View.VISIBLE);
				mLoadingView.setVisibility(View.GONE);
			}
		}
	}
}
