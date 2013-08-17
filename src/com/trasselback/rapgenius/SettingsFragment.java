package com.trasselback.rapgenius;

import java.io.File;
import java.text.DecimalFormat;

import android.annotation.TargetApi;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Build;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class SettingsFragment extends PreferenceFragment implements
		OnSharedPreferenceChangeListener {
	public static final String KEY_PREF_LOAD_HOME = "pref_key_load_home";
	public static final String KEY_PREF_REMOVE_FAV = "pref_key_remove_favorites";
	public static final String KEY_PREF_CACHE_LYRICS = "pref_key_cache_lyrics";
	public static final String KEY_PREF_CLEAR_CACHE = "pref_key_clear_cache";
	public static final String KEY_PREF_TEXT_SIZE = "pref_key_text_size";
	public static final String KEY_PREF_DEFAULT_TEXT_COLOR = "pref_key_default_text_color";
	public static final String KEY_PREF_TITLE_COLOR = "pref_key_title_color";
	public static final String KEY_PREF_HOME_PAGE_COLOR = "pref_key_home_page_color";
	public static final String KEY_PREF_EXPLAINED_LYRICS_COLOR = "pref_key_explained_lyrics_color";
	public static final String KEY_PREF_FAVORITES_COLOR = "pref_key_favorites_color";
	public static final String KEY_PREF_BACKGROUND_COLOR = "pref_key_background_color";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Load the preferences from an XML resource
		addPreferencesFromResource(R.xml.pref_general);

		// Get cache file size
		DecimalFormat deciFormat = new DecimalFormat("#.####");
		Preference pref = findPreference(KEY_PREF_CLEAR_CACHE);
		pref.setSummary(String.valueOf(deciFormat.format(CacheManager
				.getCacheSize(getActivity()))) + " MB used.");
	}

	@Override
	public void onResume() {
		super.onResume();
		getPreferenceManager().getSharedPreferences()
				.registerOnSharedPreferenceChangeListener(this);
	}

	@Override
	public void onPause() {
		super.onPause();
		getPreferenceManager().getSharedPreferences()
				.unregisterOnSharedPreferenceChangeListener(this);
		SharedPreferences prefs = android.preference.PreferenceManager
				.getDefaultSharedPreferences(getActivity());

		// Check remove favorites dialog preference
		if (prefs.getBoolean((KEY_PREF_REMOVE_FAV), true)) {
			File file = new File(getActivity().getFilesDir(), "favorites");
			file.delete();
			// Reset preference to false since dialog preference persists
			Editor editor = prefs.edit();
			editor.putBoolean(KEY_PREF_REMOVE_FAV, false);
			editor.commit();
		}
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
		if (key.equals(KEY_PREF_CLEAR_CACHE)) {
			if (sharedPreferences.getBoolean(KEY_PREF_CLEAR_CACHE, true)) {
				CacheManager.deleteCache(getActivity());
				// Reset preference to false since dialog preference persists
				Editor editor = sharedPreferences.edit();
				editor.putBoolean(KEY_PREF_CLEAR_CACHE, false);
				editor.commit();
				// Update summary
				Preference pref = findPreference(key);
				pref.setSummary("0 MB");
			}
		}
	}

}
