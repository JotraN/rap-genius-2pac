package com.trasselback.rapgenius.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
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
import android.widget.ProgressBar;
import android.widget.TextView;

import com.trasselback.rapgenius.R;
import com.trasselback.rapgenius.data.NewsFeed;
import com.trasselback.rapgenius.helpers.ColorManager;
import com.trasselback.rapgenius.helpers.CrossfadeAnimation;
import com.trasselback.rapgenius.helpers.RemoveUnderLine;
import com.trasselback.rapgenius.preferences.SettingsFragment;

public class HomePageFragment extends Fragment {
	public final static String EXTRA_MESSAGE = "com.trasselback.rapgenius.MESSAGE";
	private TextView lyricsField;
	private ProgressBar loadingView;
	private View contentView;
	private NewsFeed news;
	private AsyncTask<Void, Void, String> retrieveTask;
	private static boolean homeLoaded = false;
	private static String homeData = ""; // Home page data

	public HomePageFragment() {
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_home_page_layout,
				container, false);
		return rootView;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		initialize();
	}

	private void initialize() {
		lyricsField = (TextView) getView().findViewById(R.id.lyricsText);
		loadingView = (ProgressBar) getView().findViewById(R.id.loadingView);
		contentView = getView().findViewById(R.id.scrollView1);
		contentView.setVisibility(View.GONE);

		// makes links operable
		lyricsField.setMovementMethod(LinkMovementMethod.getInstance());

		retrieveTask = new RetrieveNewsFeed();
	}

	@Override
	public void onPause() {
		super.onPause();
		// Stop retrieving news feed if switching fragments
		retrieveTask.cancel(true);
		// If still loading, content wasn't loaded.
		if (loadingView.isShown()) {
			homeLoaded = false;
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		loadHome();
		checkSettings();
	}

	private void loadHome() {
		SharedPreferences sharedPref = PreferenceManager
				.getDefaultSharedPreferences(getActivity());
		boolean loadHomeEnabled = sharedPref.getBoolean(
				SettingsFragment.KEY_PREF_LOAD_HOME, true);

		// Restart task if it was cancelled and never completed
		if (!homeLoaded) {
			// Reset retrieveTask to RetrieveNewsFeed
			retrieveTask = new RetrieveNewsFeed();
			if (loadHomeEnabled) {
				loadingView.setVisibility(View.VISIBLE);
				contentView.setVisibility(View.GONE);
				retrieveTask.execute();
			} else {
				lyricsField.setText(R.string.default_home);
				contentView.setVisibility(View.VISIBLE);
				loadingView.setVisibility(View.GONE);
			}
		} else {
			if (loadHomeEnabled) {
				loadingView.setVisibility(View.GONE);
				contentView.setVisibility(View.VISIBLE);
				// Check to make sure home data exists
				if (homeData.length() > 5) {
					lyricsField.setText(Html.fromHtml(homeData));
				} else
					retrieveTask.execute();
				RemoveUnderLine.removeUnderline(lyricsField);
			} else {
				lyricsField.setText(R.string.default_home);
				contentView.setVisibility(View.VISIBLE);
				loadingView.setVisibility(View.GONE);
			}
		}
	}

	private void checkSettings() {
		try {
			SharedPreferences sharedPref = PreferenceManager
					.getDefaultSharedPreferences(getActivity());
			// Update text size
			int size = Integer.parseInt(sharedPref.getString(
					SettingsFragment.KEY_PREF_TEXT_SIZE, "20"));
			lyricsField.setTextSize(TypedValue.COMPLEX_UNIT_SP, size);

			// Update colors
			int textColor = Integer.parseInt(sharedPref.getString(
					SettingsFragment.KEY_PREF_DEFAULT_TEXT_COLOR, "0"));
			ColorManager.setColor(getActivity(), lyricsField, textColor);
			int linkColor = Integer.parseInt(sharedPref.getString(
					SettingsFragment.KEY_PREF_HOME_PAGE_COLOR, "0"));
			ColorManager.setLinkColor(getActivity(), lyricsField, linkColor);
			int backgroundColor = Integer.parseInt(sharedPref.getString(
					SettingsFragment.KEY_PREF_BACKGROUND_COLOR, "0"));
			ColorManager.setBackgroundColor(getActivity(), backgroundColor);
		} catch (NumberFormatException ex) {
			clearSettings();
		}
	}

	// TODO Delete after several updates 2.7.6
	// Needed to reset settings for those who updated and are still using old
	// color settings
	private void clearSettings() {
		Editor editor = getActivity().getSharedPreferences(
				SettingsFragment.KEY_PREF_TEXT_SIZE, Context.MODE_PRIVATE)
				.edit();
		editor.clear();
		editor.commit();
		editor = getActivity().getSharedPreferences(
				SettingsFragment.KEY_PREF_BACKGROUND_COLOR,
				Context.MODE_PRIVATE).edit();
		editor.clear();
		editor.commit();
		editor = getActivity().getSharedPreferences(
				SettingsFragment.KEY_PREF_DEFAULT_TEXT_COLOR,
				Context.MODE_PRIVATE).edit();
		editor.clear();
		editor.commit();
		editor = getActivity().getSharedPreferences(
				SettingsFragment.KEY_PREF_EXPLAINED_LYRICS_COLOR,
				Context.MODE_PRIVATE).edit();
		editor.clear();
		editor.commit();
		editor = getActivity()
				.getSharedPreferences(
						SettingsFragment.KEY_PREF_FAVORITES_COLOR,
						Context.MODE_PRIVATE).edit();
		editor.clear();
		editor.commit();
		editor = getActivity()
				.getSharedPreferences(
						SettingsFragment.KEY_PREF_HOME_PAGE_COLOR,
						Context.MODE_PRIVATE).edit();
		editor.clear();
		editor.commit();
		editor = getActivity().getSharedPreferences(
				SettingsFragment.KEY_PREF_TITLE_COLOR, Context.MODE_PRIVATE)
				.edit();
		editor.clear();
		editor.commit();
	}

	private class RetrieveNewsFeed extends AsyncTask<Void, Void, String> {

		@Override
		protected String doInBackground(Void... names) {
			news = new NewsFeed();
			if (news.isOnline(getActivity())) {
				if (news.openURL())
					news.retrievePage();
				return news.getPage();
			} else
				return getString(R.string.error_no_internet);
		}

		@Override
		protected void onPreExecute() {
			if (retrieveTask != null)
				// Doesn't look like this gets called
				if (retrieveTask.isCancelled())
					return;
		}

		@Override
		protected void onPostExecute(String result) {
			homeData = result;
			lyricsField.setText(Html.fromHtml(result));
			RemoveUnderLine.removeUnderline(lyricsField);
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1)
				CrossfadeAnimation.crossfade(getActivity(), contentView,
						loadingView);
			else {
				contentView.setVisibility(View.VISIBLE);
				loadingView.setVisibility(View.GONE);
			}
			homeLoaded = true;
		}
	}
}
