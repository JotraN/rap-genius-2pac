package com.trasselback.rapgenius.fragments;

import com.trasselback.rapgenius.R;
import com.trasselback.rapgenius.data.NewsFeed;
import com.trasselback.rapgenius.helpers.ColorManager;
import com.trasselback.rapgenius.helpers.CrossfadeAnimation;
import com.trasselback.rapgenius.helpers.RemoveUnderLine;
import com.trasselback.rapgenius.preferences.SettingsFragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
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

public class HomePageFragment extends Fragment {
	public final static String EXTRA_MESSAGE = "com.trasselback.rapgenius.MESSAGE";

	private TextView lyricsField;
	private ProgressBar mLoadingView;
	private View mContent;
	private NewsFeed news;
	private AsyncTask<Void, Void, String> retrieveTask;

	private static boolean contentLoaded = false;
	// holds home page data
	private static String homeData = "";

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

	@Override
	public void onPause() {
		super.onPause();
		// Stop retrieving news feed if switching fragments
		retrieveTask.cancel(true);
		// If still loading, content wasn't loaded.
		if (mLoadingView.isShown()) {
			contentLoaded = false;
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		SharedPreferences sharedPref = PreferenceManager
				.getDefaultSharedPreferences(getActivity());
		boolean loadHome = sharedPref.getBoolean(
				SettingsFragment.KEY_PREF_LOAD_HOME, true);

		// Restart task if it was cancelled and never completed
		if (!contentLoaded) {
			// Reset retrieveTask to RetrieveNewsFeed
			retrieveTask = new RetrieveNewsFeed();
			if (loadHome) {
				mLoadingView.setVisibility(View.VISIBLE);
				mContent.setVisibility(View.GONE);
				retrieveTask.execute();
			} else {
				lyricsField.setText("Home disabled in settings.");
				mContent.setVisibility(View.VISIBLE);
				mLoadingView.setVisibility(View.GONE);
			}
		} else {
			if (loadHome) {
				mLoadingView.setVisibility(View.GONE);
				mContent.setVisibility(View.VISIBLE);
				// Check to make sure home data exists
				if (homeData.length() > 5) {
					lyricsField.setText(Html.fromHtml(homeData));
				} else
					retrieveTask.execute();
				RemoveUnderLine.removeUnderline(lyricsField);
			} else {
				lyricsField.setText("Home disabled in settings.");
				mContent.setVisibility(View.VISIBLE);
				mLoadingView.setVisibility(View.GONE);
			}
		}
		// Update text size
		int size = Integer.parseInt(sharedPref.getString(
				SettingsFragment.KEY_PREF_TEXT_SIZE, "22"));
		lyricsField.setTextSize(TypedValue.COMPLEX_UNIT_SP, size);

		// Update colors
		String color = sharedPref.getString(
				SettingsFragment.KEY_PREF_DEFAULT_TEXT_COLOR, "Default");
		if (!color.contains("Default")) {
			ColorManager.setColor(getActivity(), lyricsField, color);
		} else
			lyricsField.setTextColor(getResources().getColor(R.color.Gray));
		color = sharedPref.getString(SettingsFragment.KEY_PREF_TITLE_COLOR,
				"Default");
		color = sharedPref.getString(SettingsFragment.KEY_PREF_HOME_PAGE_COLOR,
				"Default");
		if (!color.contains("Default")) {
			ColorManager.setLinkColor(getActivity(), lyricsField, color);
		} else
			lyricsField.setLinkTextColor(getResources().getColor(
					R.color.LightBlue));
		color = sharedPref.getString(
				SettingsFragment.KEY_PREF_BACKGROUND_COLOR, "Default");
		if (!color.contains("Default")) {
			ColorManager.setBackgroundColor(getActivity(), color);
		} else
			getActivity().getWindow().setBackgroundDrawableResource(
					R.color.LightBlack);
	}

	private void initialize() {
		lyricsField = (TextView) getView().findViewById(R.id.lyricsText);
		mLoadingView = (ProgressBar) getView().findViewById(R.id.loadingView);
		mContent = getView().findViewById(R.id.scrollView1);
		mContent.setVisibility(View.GONE);

		// makes links operable
		lyricsField.setMovementMethod(LinkMovementMethod.getInstance());

		retrieveTask = new RetrieveNewsFeed();
	}

	private class RetrieveNewsFeed extends AsyncTask<Void, Void, String> {

		@Override
		protected String doInBackground(Void... names) {
			news = new NewsFeed();
			try {
				ConnectivityManager connMgr = (ConnectivityManager) getActivity()
						.getSystemService(Context.CONNECTIVITY_SERVICE);
				NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
				if (networkInfo != null && networkInfo.isConnected()) {
					if (news.openURL())
						news.retrievePage();
					return news.getPage();
				} else
					return "No internet connection found.";
			} catch (Exception ex) {
				return "There was a problem getting information about your network status.";
			}
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
				CrossfadeAnimation.crossfade(getActivity(), mContent,
						mLoadingView);
			else {
				mContent.setVisibility(View.VISIBLE);
				mLoadingView.setVisibility(View.GONE);
			}
			contentLoaded = true;
		}
	}
}
