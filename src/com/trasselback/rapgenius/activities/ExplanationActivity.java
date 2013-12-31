package com.trasselback.rapgenius.activities;

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
import com.trasselback.rapgenius.R;
import com.trasselback.rapgenius.data.Explanations;
import com.trasselback.rapgenius.helpers.ColorManager;
import com.trasselback.rapgenius.helpers.CrossfadeAnimation;
import com.trasselback.rapgenius.helpers.RemoveUnderLine;
import com.trasselback.rapgenius.preferences.SettingsFragment;

public class ExplanationActivity extends SherlockActivity {
	private TextView nameField, explanationsField;
	private View loadingView;
	private View contentView;
	private Explanations explanation;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_explanation);
		setupActionBar();
		initialize();
		grabExplanation();
	}

	private void setupActionBar() {
		getSupportActionBar().setTitle("EXPLANATION");
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

	private void initialize() {
		nameField = (TextView) findViewById(R.id.nameText);
		Typeface tf = Typeface.createFromAsset(getAssets(),
				"fonts/roboto_thin.ttf");
		nameField.setTypeface(tf);
		explanationsField = (TextView) findViewById(R.id.lyricsText);
		loadingView = findViewById(R.id.loadingView);
		contentView = findViewById(R.id.infoView);
		contentView.setVisibility(View.GONE);

//		((ProgressBar) loadingView).setIndeterminateDrawable(getResources()
//				.getDrawable(R.xml.progress_animation));
		// Needed for Android 2.3
		explanationsField.setMovementMethod(new LinkMovementMethod());
	}

	@Override
	public void onResume() {
		super.onResume();
		checkSettings();
	}

	private void checkSettings() {
		SharedPreferences sharedPref = PreferenceManager
				.getDefaultSharedPreferences(this);
		// Update text size
		int size = Integer.parseInt(sharedPref.getString(
				SettingsFragment.KEY_PREF_TEXT_SIZE, "20"));
		explanationsField.setTextSize(TypedValue.COMPLEX_UNIT_SP, size);
		nameField.setTextSize(TypedValue.COMPLEX_UNIT_SP, size + 10);

		// Update colors
		int textColor = Integer.parseInt(sharedPref.getString(
				SettingsFragment.KEY_PREF_DEFAULT_TEXT_COLOR, "Default"));
		ColorManager.setColor(this, explanationsField, textColor);
		int titleColor = Integer.parseInt(sharedPref.getString(
				SettingsFragment.KEY_PREF_TITLE_COLOR, "Default"));
		ColorManager.setColor(this, nameField, titleColor);
		int linkColor = Integer.parseInt(sharedPref.getString(
				SettingsFragment.KEY_PREF_EXPLAINED_LYRICS_COLOR, "Default"));
		ColorManager.setLinkColor(this, explanationsField, linkColor);
		int backgroundColor = Integer.parseInt(sharedPref.getString(
				SettingsFragment.KEY_PREF_BACKGROUND_COLOR, "Default"));
		ColorManager.setBackgroundColor(this, backgroundColor);
	}

	private void grabExplanation() {
		if (getIntent().getData() != null)
			// Make sure explanations activity was started by explanation link
			if (getIntent().getDataString().contains("explanation_clicked"))
				new RetrieveExplanationsTask().execute();
	}

	private class RetrieveExplanationsTask extends
			AsyncTask<Void, Void, String> {

		@Override
		protected String doInBackground(Void... params) {
			explanation = new Explanations(getIntent().getDataString());
			try {
				ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
				NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
				if (networkInfo != null && networkInfo.isConnected()) {
					explanation.retrieveUrl();
					explanation.retrieveName();
					if (explanation.openedURL())
						explanation.retrievePage();
					return explanation.getPage();
				} else
					return "No internet connection found.";
			} catch (Exception ex) {
				return "There was a problem getting information about your network status.";
			}
		}

		protected void onPostExecute(String result) {
			nameField.setText(explanation.getName());
			explanationsField.setText(Html.fromHtml(result));
			RemoveUnderLine.removeUnderline(explanationsField);
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1)
				CrossfadeAnimation.crossfade(getApplicationContext(), contentView,
						loadingView);
			else {
				explanationsField.setMovementMethod(new LinkMovementMethod());
				contentView.setVisibility(View.VISIBLE);
				loadingView.setVisibility(View.GONE);
			}
		}
	}
}
