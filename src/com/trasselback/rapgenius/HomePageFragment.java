package com.trasselback.rapgenius;

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
	private URLObject urlObject;
	private AsyncTask<Void, Void, String> retrieveTask;

	private boolean contentLoaded = false;

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
	}

	@Override
	public void onResume() {
		super.onResume();
		SharedPreferences sharedPref = PreferenceManager
				.getDefaultSharedPreferences(getActivity());
		// If not already loaded
		if (!contentLoaded) {
			// Get load home setting
			boolean loadHome = sharedPref.getBoolean(
					SettingsFragment.KEY_PREF_LOAD_HOME, false);

			if (loadHome) {
				mLoadingView.setVisibility(View.VISIBLE);
				mContent.setVisibility(View.GONE);
				retrieveTask.execute();
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
			urlObject = new NewsFeed();
			ConnectivityManager connMgr = (ConnectivityManager) getActivity()
					.getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
			if (networkInfo != null && networkInfo.isConnected()) {
				if (urlObject.openURL())
					urlObject.retrievePage();
				contentLoaded = true;
				return urlObject.getPage();
			} else
				return "No internet connection found.";
		}

		@Override
		protected void onPreExecute() {
			if (retrieveTask.isCancelled())
				return;
		}

		@Override
		protected void onPostExecute(String result) {
			lyricsField.setText(Html.fromHtml(result));
			RemoveUnderLine.removeUnderline(lyricsField);
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1)
				CrossfadeAnimation.crossfade(getActivity(), mContent,
						mLoadingView);
			else {
				mContent.setVisibility(View.VISIBLE);
				mLoadingView.setVisibility(View.GONE);
			}
		}
	}
}
