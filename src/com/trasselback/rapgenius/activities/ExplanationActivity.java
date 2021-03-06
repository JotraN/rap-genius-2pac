package com.trasselback.rapgenius.activities;

import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Html;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.util.TypedValue;
import android.view.View;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.MenuItem;
import com.trasselback.rapgenius.R;
import com.trasselback.rapgenius.data.Explanations;
import com.trasselback.rapgenius.helpers.ColorManager;
import com.trasselback.rapgenius.helpers.CrossfadeAnimation;
import com.trasselback.rapgenius.helpers.RemoveUnderLine;
import com.trasselback.rapgenius.helpers.URLImageParser;
import com.trasselback.rapgenius.preferences.SettingsFragment;

public class ExplanationActivity extends SherlockActivity {
	private TextView nameField, explanationsField;
	private View loadingView;
	private View contentView;
	private Explanations explanation;
	private URLImageParser imageParser;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		overridePendingTransition(R.anim.right_in, R.anim.hold);
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
			overridePendingTransition(R.anim.hold, R.anim.left_in);
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

		// Needed for Android 2.3
		explanationsField.setMovementMethod(new LinkMovementMethod());

		imageParser = new URLImageParser(explanationsField, this);

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
				SettingsFragment.KEY_PREF_DEFAULT_TEXT_COLOR, "0"));
		ColorManager.setColor(this, explanationsField, textColor);
		int titleColor = Integer.parseInt(sharedPref.getString(
				SettingsFragment.KEY_PREF_TITLE_COLOR, "0"));
		ColorManager.setColor(this, nameField, titleColor);
		int linkColor = Integer.parseInt(sharedPref.getString(
				SettingsFragment.KEY_PREF_EXPLAINED_LYRICS_COLOR, "0"));
		ColorManager.setLinkColor(this, explanationsField, linkColor);
		int backgroundColor = Integer.parseInt(sharedPref.getString(
				SettingsFragment.KEY_PREF_BACKGROUND_COLOR, "0"));
		ColorManager.setBackgroundColor(this, backgroundColor);
		int actionBarColor = Integer.parseInt(sharedPref.getString(
				SettingsFragment.KEY_PREF_ACTION_BAR_COLOR, "0"));
		ColorManager.setActionBarColorExplanation(this, actionBarColor);
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
			if (explanation.isOnline(getApplicationContext())) {
				if (!explanation.retrievedUrl())
					return getString(R.string.error_explanation);
				explanation.retrieveName();
				if (explanation.openedURL())
					explanation.retrievePage();
				return explanation.getPage();
			} else
				return getString(R.string.error_no_internet);
		}

		protected void onPostExecute(String result) {
			nameField.setText(explanation.getName());
			Spanned htmlSpan = Html.fromHtml(result, imageParser, null);
			explanationsField.setText(htmlSpan);
			RemoveUnderLine.removeUnderline(explanationsField);
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1)
				CrossfadeAnimation.crossfade(getApplicationContext(),
						contentView, loadingView);
			else {
				explanationsField.setMovementMethod(new LinkMovementMethod());
				contentView.setVisibility(View.VISIBLE);
				loadingView.setVisibility(View.GONE);
			}
		}
	}
}
