package com.trasselback.rapgenius;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

public class SearchActivity extends SherlockActivity {
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

	private void setupActionBar() {
			getSupportActionBar().setDisplayHomeAsUpEnabled(true);
			getSupportActionBar().setDisplayShowTitleEnabled(false);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getSupportMenuInflater().inflate(R.menu.search, menu);
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
		
		// Update text size
		int size = Integer.parseInt(sharedPref.getString(
				SettingsFragment.KEY_PREF_TEXT_SIZE, "22"));
		favorites.setTextSize(TypedValue.COMPLEX_UNIT_SP, size + 10);
		nameField.setTextSize(TypedValue.COMPLEX_UNIT_SP, size + 10);

		// Update colors
		String color = sharedPref.getString(
				SettingsFragment.KEY_PREF_DEFAULT_TEXT_COLOR, "Default");
		if (!color.contains("Default")) {
			ColorManager.setColor(getApplicationContext(), favorites, color);
		} else
			songField.setTextColor(getResources().getColor(R.color.Gray));
		color = sharedPref.getString(SettingsFragment.KEY_PREF_TITLE_COLOR,
				"Default");
		if (!color.contains("Default")) {
			ColorManager.setColor(getApplicationContext(), nameField, color);
		} else
			nameField.setTextColor(getResources().getColor(R.color.LightGray));
		color = sharedPref.getString(SettingsFragment.KEY_PREF_FAVORITES_COLOR,
				"Default");
		if (!color.contains("Default")) {
			ColorManager.setLinkColor(getApplicationContext(), favorites,
					color);
		} else
			favorites.setLinkTextColor(getResources().getColor(
					R.color.Red));
		color = sharedPref.getString(
				SettingsFragment.KEY_PREF_BACKGROUND_COLOR, "Default");
		if (!color.contains("Default")) {
			ColorManager.setBackgroundColor(this, color);
		} else
			getWindow().setBackgroundDrawableResource(R.color.LightBlack);
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