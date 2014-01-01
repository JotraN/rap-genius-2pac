package com.trasselback.rapgenius.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.trasselback.rapgenius.R;
import com.trasselback.rapgenius.activities.MainActivity;
import com.trasselback.rapgenius.data.Lyrics;
import com.trasselback.rapgenius.helpers.CacheManager;
import com.trasselback.rapgenius.helpers.ColorManager;
import com.trasselback.rapgenius.helpers.CrossfadeAnimation;
import com.trasselback.rapgenius.helpers.RemoveUnderLine;
import com.trasselback.rapgenius.preferences.SettingsFragment;

public class LyricsFragment extends Fragment {
	private TextView nameField, lyricsField;
	private View loadingView;
	private View contentView;
	private Lyrics lyrics;
	private AsyncTask<String, Void, String> retrieveTask;
	private boolean cacheLyricsEnabled = true;
	private static boolean taskStarted = false;
	// Static variable to pass to MainActivity easily
	public static String artistNameSongName = "";

	public LyricsFragment() {
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_lyrics_layout,
				container, false);
		return rootView;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		initialize();
		startLyrics();
	}

	@Override
	public void onPause() {
		super.onPause();
		retrieveTask.cancel(true);
		// If still loading, task was interrupted and needs to be restarted
		if (loadingView.isShown())
			taskStarted = false;
	}

	private void initialize() {
		nameField = (TextView) getView().findViewById(R.id.nameText);
		Typeface tf = Typeface.createFromAsset(getActivity().getAssets(),
				"fonts/roboto_thin.ttf");
		nameField.setTypeface(tf);
		lyricsField = (TextView) getView().findViewById(R.id.lyricsText);
		loadingView = getView().findViewById(R.id.loadingView);
		contentView = getView().findViewById(R.id.infoView);
		contentView.setVisibility(View.GONE);
		
		// Needed for Android 2.3
		lyricsField.setMovementMethod(LinkMovementMethod.getInstance());

		SharedPreferences sharedPref = PreferenceManager
				.getDefaultSharedPreferences(getActivity());
		cacheLyricsEnabled = sharedPref.getBoolean(
				SettingsFragment.KEY_PREF_CACHE_LYRICS, true);

		retrieveTask = new RetrieveLyricsTask();
	}

	@Override
	public void onResume() {
		super.onResume();
		// Restart retrieve task if it was cancelled
		if (!taskStarted && loadingView.isShown()) {
			retrieveTask = new RetrieveLyricsTask();
			startLyrics();
		}
		checkSettings();
	}

	private void checkSettings() {
		try{
		SharedPreferences sharedPref = PreferenceManager
				.getDefaultSharedPreferences(getActivity());
		// Update text size
		int size = Integer.parseInt(sharedPref.getString(
				SettingsFragment.KEY_PREF_TEXT_SIZE, "20"));
		lyricsField.setTextSize(TypedValue.COMPLEX_UNIT_SP, size);
		nameField.setTextSize(TypedValue.COMPLEX_UNIT_SP, size + 10);

		// Update colors
		int textColor = Integer.parseInt(sharedPref.getString(
				SettingsFragment.KEY_PREF_DEFAULT_TEXT_COLOR, "0"));
		ColorManager.setColor(getActivity(), lyricsField, textColor);
		int titleColor = Integer.parseInt(sharedPref.getString(
				SettingsFragment.KEY_PREF_TITLE_COLOR, "0"));
		ColorManager.setColor(getActivity(), nameField, titleColor);
		int linkColor = Integer.parseInt(sharedPref.getString(
				SettingsFragment.KEY_PREF_EXPLAINED_LYRICS_COLOR, "0"));
		ColorManager.setLinkColor(getActivity(), lyricsField, linkColor);
		int backgroundColor = Integer.parseInt(sharedPref.getString(
				SettingsFragment.KEY_PREF_BACKGROUND_COLOR, "0"));
		ColorManager.setBackgroundColor(getActivity(), backgroundColor);
		} catch(NumberFormatException ex){
			clearSettings();
		}
	}

	// Needed to reset settings for those who updated and are still using old color settings
	private void clearSettings() {
		Editor editor = getActivity().getSharedPreferences(SettingsFragment.KEY_PREF_TEXT_SIZE,
				Context.MODE_PRIVATE).edit();
		editor.clear();
		editor.commit();
		editor = getActivity().getSharedPreferences(SettingsFragment.KEY_PREF_BACKGROUND_COLOR,
				Context.MODE_PRIVATE).edit();
		editor.clear();
		editor.commit();
		editor = getActivity().getSharedPreferences(SettingsFragment.KEY_PREF_DEFAULT_TEXT_COLOR,
				Context.MODE_PRIVATE).edit();
		editor.clear();
		editor.commit();
		editor = getActivity().getSharedPreferences(SettingsFragment.KEY_PREF_EXPLAINED_LYRICS_COLOR,
				Context.MODE_PRIVATE).edit();
		editor.clear();
		editor.commit();
		editor = getActivity().getSharedPreferences(SettingsFragment.KEY_PREF_FAVORITES_COLOR,
				Context.MODE_PRIVATE).edit();
		editor.clear();
		editor.commit();
		editor = getActivity().getSharedPreferences(SettingsFragment.KEY_PREF_HOME_PAGE_COLOR,
				Context.MODE_PRIVATE).edit();
		editor.clear();
		editor.commit();
		editor = getActivity().getSharedPreferences(SettingsFragment.KEY_PREF_TITLE_COLOR,
				Context.MODE_PRIVATE).edit();
		editor.clear();
		editor.commit();
	}
	
	// Find what started lyrics fragment and clean input
	private void startLyrics() {
		artistNameSongName = getArguments().getString(
				MainActivity.EXTRA_MESSAGE);
		// Home song clicked
		if (artistNameSongName.contains("song_clicked"))
			// remove the -lyrics at the end of the URL
			artistNameSongName = artistNameSongName.substring(
					artistNameSongName.indexOf("/") + 1,
					artistNameSongName.length() - "-lyrics".length());
		// Favorite song clicked
		else if (artistNameSongName.contains("fav_clicked"))
			artistNameSongName = artistNameSongName
					.substring(artistNameSongName.indexOf(":") + 1);
		if (CacheManager.getCache(getActivity(), artistNameSongName).length() <= 0)
			retrieveTask.execute(artistNameSongName);
		else
			setCache();
	}

	private void setCache() {
		loadingView.setVisibility(View.GONE);
		contentView.setVisibility(View.VISIBLE);
		String cachedData = CacheManager.getCache(getActivity(),
				artistNameSongName);
		// If cached data actually exists
		if (cachedData.length() > 5) {
			String nameData = cachedData.substring(0, cachedData.indexOf('<'));
			String lyricsData = cachedData.substring(cachedData.indexOf('<'));
			nameField.setText(nameData);
			lyricsField.setText(Html.fromHtml(lyricsData));
			RemoveUnderLine.removeUnderline(lyricsField);
		} else {
			String nameData = getString(R.string.error_cache_loading);
			String lyricsData = getString(R.string.error_reload);
			nameField.setText(nameData);
			lyricsField.setText(Html.fromHtml(lyricsData));
			RemoveUnderLine.removeUnderline(lyricsField);
		}
	}

	private class RetrieveLyricsTask extends AsyncTask<String, Void, String> {

		@Override
		protected String doInBackground(String... names) {
			lyrics = new Lyrics(names[0]);
			// Checking connection sometimes throws exception
			try {
				if(lyrics.isOnline(getActivity())){
					if (lyrics.openedURL()) {
						lyrics.retrieveName();
						lyrics.retrievePage();
					} else
						lyrics.googleIt();
					return lyrics.getPage();
				} else
					return getString(R.string.error_no_internet);
			} catch (Exception ex) {
				return getString(R.string.error_network_check);
			}
		}

		@Override
		protected void onPreExecute() {
			if (retrieveTask != null)
				if (retrieveTask.isCancelled())
					return;
			taskStarted = false;
		}

		@Override
		protected void onPostExecute(String result) {
			nameField.setText(lyrics.getName());
			lyricsField.setText(Html.fromHtml(result));
			RemoveUnderLine.removeUnderline(lyricsField);
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1)
				CrossfadeAnimation.crossfade(getActivity(), contentView,
						loadingView);
			else {
				contentView.setVisibility(View.VISIBLE);
				loadingView.setVisibility(View.GONE);
			}
			if (cacheLyricsEnabled)
				CacheManager.saveData(getActivity(), artistNameSongName,
						nameField.getText().toString() + result);
			taskStarted = true;
		}
	}
}
