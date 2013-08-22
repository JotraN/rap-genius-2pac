package com.trasselback.rapgenius;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.widget.DrawerLayout;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

public class LyricsActivity extends SherlockActivity {
	private TextView nameField, lyricsField;
	private View mLoadingView;
	private View mContent;
	private URLObject urlObject;
	private String message = "";

	private String[] mDrawerTitles;
	private DrawerLayout mDrawerLayout;
	private ListView mDrawerList;

	private boolean hideFavs = false;
	private boolean cacheLyricsEnabled = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_lyrics);
		setupActionBar();
		initialize();
		startLyrics();

		mDrawerTitles = getResources().getStringArray(R.array.navigation_array);
		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		mDrawerList = (ListView) findViewById(R.id.left_drawer);

		// Set the adapter for the list view
		ListAdapter adapter = new ListAdapter(this, R.layout.drawer_list_item);
		for (String x : mDrawerTitles) {
			adapter.add(x);
		}
		mDrawerList.setAdapter(adapter);
		mDrawerList.setOnItemClickListener(new DrawerItemClickListener());

	}

	private void setupActionBar() {
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setDisplayShowTitleEnabled(false);
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getSupportMenuInflater().inflate(R.menu.lyrics, menu);
		if (hideFavs) {
			MenuItem item = menu.findItem(R.id.action_favorite);
			item.setVisible(false);
		}
		if (FavoritesManager.checkFavorites(this, message)) {
			MenuItem item = menu.findItem(R.id.action_favorite);
			item.setIcon(R.drawable.ic_star_pressed);
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
			FavoritesManager.addFavorites(this, message);
			if (FavoritesManager.checkFavorites(this, message)) {
				item.setIcon(R.drawable.ic_star_pressed);
			} else {
				item.setIcon(R.drawable.ic_star_not_pressed);
			}

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

		((ProgressBar) mLoadingView).setIndeterminateDrawable(getResources()
				.getDrawable(R.xml.progress_animation));

		// necessary for 2.3 for some reason
		lyricsField.setMovementMethod(LinkMovementMethod.getInstance());

		// Get cache lyrics setting
		SharedPreferences sharedPref = PreferenceManager
				.getDefaultSharedPreferences(this);
		cacheLyricsEnabled = sharedPref.getBoolean(
				SettingsFragment.KEY_PREF_CACHE_LYRICS, false);

	}

	@Override
	public void onResume() {
		super.onResume();
		SharedPreferences sharedPref = PreferenceManager
				.getDefaultSharedPreferences(this);

		// Update text size
		int size = Integer.parseInt(sharedPref.getString(
				SettingsFragment.KEY_PREF_TEXT_SIZE, "22"));
		lyricsField.setTextSize(TypedValue.COMPLEX_UNIT_SP, size);
		nameField.setTextSize(TypedValue.COMPLEX_UNIT_SP, size + 10);

		// Update colors
		String color = sharedPref.getString(
				SettingsFragment.KEY_PREF_DEFAULT_TEXT_COLOR, "Default");
		if (!color.contains("Default")) {
			ColorManager.setColor(this, lyricsField, color);
		} else
			lyricsField.setTextColor(getResources().getColor(R.color.Gray));
		color = sharedPref.getString(SettingsFragment.KEY_PREF_TITLE_COLOR,
				"Default");
		if (!color.contains("Default")) {
			ColorManager.setColor(this, nameField, color);
		} else
			nameField.setTextColor(getResources().getColor(R.color.LightGray));
		color = sharedPref.getString(
				SettingsFragment.KEY_PREF_EXPLAINED_LYRICS_COLOR, "Default");
		if (!color.contains("Default")) {
			ColorManager.setLinkColor(this, lyricsField, color);
		} else
			lyricsField.setLinkTextColor(getResources()
					.getColor(R.color.Orange));
		color = sharedPref.getString(
				SettingsFragment.KEY_PREF_BACKGROUND_COLOR, "Default");
		if (!color.contains("Default")) {
			ColorManager.setBackgroundColor(this, color);
		} else
			getWindow().setBackgroundDrawableResource(R.color.LightBlack);
	}

	private void openSettings() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			Intent intent = new Intent(LyricsActivity.this,
					SettingsActivity.class);
			startActivity(intent);
		} else {
			Intent intent = new Intent(LyricsActivity.this,
					SettingsPreferenceActivity.class);
			startActivity(intent);
		}
	}

	private void openSearch() {
		Intent intent = new Intent(LyricsActivity.this, SearchActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(intent);
	}

	private void setCache() {
		mLoadingView.setVisibility(View.GONE);
		mContent.setVisibility(View.VISIBLE);
		String cachedData = CacheManager.getCache(this, message);
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
			if (CacheManager.getCache(this, message).length() <= 0)
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
			if (CacheManager.getCache(this, message).length() <= 0)
				new RetrieveLyricsTask().execute(message);
			else
				setCache();
		} else if (getIntent().getDataString().contains("fav_clicked")) {
			message = getIntent().getDataString();
			message = message.substring(message.indexOf(":") + 1);
			if (CacheManager.getCache(this, message).length() <= 0)
				new RetrieveLyricsTask().execute(message);
			else
				setCache();
		}
	}

	private class RetrieveLyricsTask extends AsyncTask<String, Void, String> {

		@Override
		protected String doInBackground(String... names) {
			urlObject = new Lyrics(names[0]);
			ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
			if (networkInfo != null && networkInfo.isConnected()) {
				if (urlObject.openURL()) {
					((Lyrics) urlObject).retrieveName();
					urlObject.retrievePage();
				}
				return urlObject.getPage();

			} else
				return "No internet connection found.";
		}

		protected void onPostExecute(String result) {
			nameField.setText(((Lyrics) urlObject).getName());
			lyricsField.setText(Html.fromHtml(result));
			RemoveUnderLine.removeUnderline(lyricsField);
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1)
				CrossfadeAnimation.crossfade(getApplicationContext(), mContent,
						mLoadingView);
			else {
				// lyricsField.setMovementMethod(new LinkMovementMethod());
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
			ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
			if (networkInfo != null && networkInfo.isConnected()) {
				((Explanations) urlObject).retrieveName();
				urlObject.retrievePage();
				return urlObject.getPage();
			} else
				return "No internet connection found.";
		}

		protected void onPostExecute(String result) {
			nameField.setText(((Explanations) urlObject).getName());
			lyricsField.setText(Html.fromHtml(result));
			RemoveUnderLine.removeUnderline(lyricsField);
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1)
				CrossfadeAnimation.crossfade(getApplicationContext(), mContent,
						mLoadingView);
			else {
				lyricsField.setMovementMethod(new LinkMovementMethod());
				mContent.setVisibility(View.VISIBLE);
				mLoadingView.setVisibility(View.GONE);
			}
		}

	}

	private class DrawerItemClickListener implements
			ListView.OnItemClickListener {
		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
				long arg3) {
			selectItem(arg2);

		}
	}

	private void selectItem(int position) {
		Intent intent = null;
		switch (position) {
		case 0:
			intent = new Intent(this, MainActivity.class);
			break;
		case 1:
			intent = new Intent(this, SearchActivity.class);
			break;
		case 2:
			intent = new Intent(this, SettingsActivity.class);
			break;
		default:
			break;
		}
		// Highlight the selected item, update the title, and close the drawer
		mDrawerList.setItemChecked(position, true);
		mDrawerLayout.closeDrawer(mDrawerList);

		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(intent);
	}

	private class ListAdapter extends ArrayAdapter<String> {
		public ListAdapter(Context context, int resource) {
			super(context, resource);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View v = super.getView(position, convertView, parent);
			switch (position) {
			case 0:
				v.setBackgroundColor(getResources().getColor(R.color.LightBlue));
				break;
			case 1:
				v.setBackgroundColor(getResources().getColor(R.color.Red));
				break;
			case 2:
				v.setBackgroundColor(getResources().getColor(R.color.Green));
				break;
			default:
				v.setBackgroundColor(getResources().getColor(R.color.Orange));
				break;
			}
			return v;
		}
	}
}
