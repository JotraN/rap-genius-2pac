package com.trasselback.rapgenius.preferences;

import java.io.File;
import java.text.DecimalFormat;

import com.trasselback.rapgenius.R;
import com.trasselback.rapgenius.helpers.CacheManager;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;

@SuppressWarnings("deprecation")
public class SettingsPreferenceActivity extends PreferenceActivity implements
		OnSharedPreferenceChangeListener {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.pref_general);

		// Get cache file size
		DecimalFormat deciFormat = new DecimalFormat("#.####");
		Preference pref = findPreference(SettingsFragment.KEY_PREF_CLEAR_CACHE);
		pref.setSummary(String.valueOf(deciFormat.format(CacheManager
				.getCacheSize(getApplicationContext()))) + " MB used.");
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
				.getDefaultSharedPreferences(getApplicationContext());

		// Check remove favorites dialog preference
		if (prefs.getBoolean((SettingsFragment.KEY_PREF_REMOVE_FAV), true)) {
			File file = new File(getApplicationContext().getFilesDir(),
					"favorites");
			file.delete();
			// Reset preference to false since dialog preference persists
			Editor editor = prefs.edit();
			editor.putBoolean(SettingsFragment.KEY_PREF_REMOVE_FAV, false);
			editor.commit();
		}
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
		if (key.equals(SettingsFragment.KEY_PREF_CLEAR_CACHE)) {
			if (sharedPreferences.getBoolean(SettingsFragment.KEY_PREF_CLEAR_CACHE, true)) {
				CacheManager.deleteCache(getApplicationContext());
				// Reset preference to false since dialog preference persists
				Editor editor = sharedPreferences.edit();
				editor.putBoolean(SettingsFragment.KEY_PREF_CLEAR_CACHE, false);
				editor.commit();
				// Update summary
				Preference pref = findPreference(key);
				pref.setSummary("0 MB");
			}
		}
	}
}
