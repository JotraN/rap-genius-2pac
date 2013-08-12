package com.trasselback.rapgenius;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

public class SearchActivity extends Activity {
	public final static String EXTRA_MESSAGE = "com.trasselback.rapgenius.MESSAGE";
	private EditText songField;
	private TextView favorites, nameField;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_search);
		// Show the Up button in the action bar.
		setupActionBar();
		initialize();
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private void setupActionBar() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			// Show the Up button in the action bar.
			getActionBar().setDisplayHomeAsUpEnabled(true);
			getActionBar().setDisplayShowTitleEnabled(false);
			getActionBar().setBackgroundDrawable(getResources().getDrawable(R.color.Red));
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.search, menu);
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
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private void initialize() {
		songField = (EditText) findViewById(R.id.songName);
		songField.setOnEditorActionListener(new OnEditorActionListener() {

			@Override
			public boolean onEditorAction(TextView v, int actionId,
					KeyEvent event) {
				Intent intent = new Intent(SearchActivity.this,
						LyricsActivity.class);
				intent.putExtra(EXTRA_MESSAGE, songField.getText().toString());
				startActivity(intent);
				return false;
			}
		});
		nameField = (TextView) findViewById(R.id.nameText);
		favorites = (TextView) findViewById(R.id.lyricsText);
		favorites.setMovementMethod(LinkMovementMethod.getInstance());

		favorites.setText(Html.fromHtml(FavoritesManager
				.getFavorites(getApplicationContext())));
		// TODO Delete this after everyone updates
		FavoritesManager.updateFavorites(getApplicationContext(),
				FavoritesManager.getFavorites(getApplicationContext()));
		RemoveUnderLine.removeUnderline(favorites);
	}

	@Override
	public void onResume() {
		super.onResume();
		favorites.setText(Html.fromHtml(FavoritesManager
				.getFavorites(getApplicationContext())));
		RemoveUnderLine.removeUnderline(favorites);
		SharedPreferences sharedPref = PreferenceManager
				.getDefaultSharedPreferences(this);
		int size = Integer.parseInt(sharedPref.getString(
				SettingsFragment.KEY_PREF_TEXT_SIZE, "22"));
		favorites.setTextSize(TypedValue.COMPLEX_UNIT_SP, size + 10);
		nameField.setTextSize(TypedValue.COMPLEX_UNIT_SP, size + 10);
	}

	private void openSettings() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			Intent intent = new Intent(SearchActivity.this,
					SettingsActivity.class);
			startActivity(intent);
		} else {
			Intent intent = new Intent(SearchActivity.this,
					SettingsPreferenceActivity.class);
			startActivity(intent);
		}
	}
}
