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

public class LyricsFragment extends Fragment {
	private TextView nameField, lyricsField;
	private View mLoadingView;
	private View mContent;
	private URLObject urlObject;
	public static String message = "";

	private boolean cacheLyricsEnabled = false;

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

	private void initialize() {
		nameField = (TextView) getView().findViewById(R.id.nameText);
		lyricsField = (TextView) getView().findViewById(R.id.lyricsText);
		mLoadingView = getView().findViewById(R.id.loadingView);
		mContent = getView().findViewById(R.id.infoView);
		mContent.setVisibility(View.GONE);

		((ProgressBar) mLoadingView).setIndeterminateDrawable(getResources()
				.getDrawable(R.xml.progress_animation));

		// necessary for 2.3 for some reason
		lyricsField.setMovementMethod(LinkMovementMethod.getInstance());

		// Get cache lyrics setting
		SharedPreferences sharedPref = PreferenceManager
				.getDefaultSharedPreferences(getActivity());
		cacheLyricsEnabled = sharedPref.getBoolean(
				SettingsFragment.KEY_PREF_CACHE_LYRICS, false);
	}

	@Override
	public void onResume() {
		super.onResume();
		SharedPreferences sharedPref = PreferenceManager
				.getDefaultSharedPreferences(getActivity());

		// Update text size
		int size = Integer.parseInt(sharedPref.getString(
				SettingsFragment.KEY_PREF_TEXT_SIZE, "22"));
		lyricsField.setTextSize(TypedValue.COMPLEX_UNIT_SP, size);
		nameField.setTextSize(TypedValue.COMPLEX_UNIT_SP, size + 10);

		// Update colors
		String color = sharedPref.getString(
				SettingsFragment.KEY_PREF_DEFAULT_TEXT_COLOR, "Default");
		if (!color.contains("Default")) {
			ColorManager.setColor(getActivity(), lyricsField, color);
		} else
			lyricsField.setTextColor(getResources().getColor(R.color.Gray));
		color = sharedPref.getString(SettingsFragment.KEY_PREF_TITLE_COLOR,
				"Default");
		if (!color.contains("Default")) {
			ColorManager.setColor(getActivity(), nameField, color);
		} else
			nameField.setTextColor(getResources().getColor(R.color.LightGray));
		color = sharedPref.getString(
				SettingsFragment.KEY_PREF_EXPLAINED_LYRICS_COLOR, "Default");
		if (!color.contains("Default")) {
			ColorManager.setLinkColor(getActivity(), lyricsField, color);
		} else
			lyricsField.setLinkTextColor(getResources()
					.getColor(R.color.Orange));
		color = sharedPref.getString(
				SettingsFragment.KEY_PREF_BACKGROUND_COLOR, "Default");
		if (!color.contains("Default")) {
			ColorManager.setBackgroundColor(getActivity(), color);
		} else
			getActivity().getWindow().setBackgroundDrawableResource(
					R.color.LightBlack);
	}

	private void setCache() {
		mLoadingView.setVisibility(View.GONE);
		mContent.setVisibility(View.VISIBLE);
		String cachedData = CacheManager.getCache(getActivity(), message);
		String nameData = cachedData.substring(0, cachedData.indexOf('<'));
		String lyricsData = cachedData.substring(cachedData.indexOf('<'));
		nameField.setText(nameData);
		lyricsField.setText(Html.fromHtml(lyricsData));
		RemoveUnderLine.removeUnderline(lyricsField);
	}

	// Find what started lyrics activity and continue from there
	private void startLyrics() {
		if (getArguments().getString(MainActivity.EXTRA_MESSAGE).contains(
				"song_clicked")) {
			message = getArguments().getString(MainActivity.EXTRA_MESSAGE);
			// remove the -lyrics at the end of the URL
			message = message.substring(message.indexOf("/") + 1,
					message.length() - 7);
			if (CacheManager.getCache(getActivity(), message).length() <= 0)
				new RetrieveLyricsTask().execute(message);
			else
				setCache();
		} else if (getArguments().getString(MainActivity.EXTRA_MESSAGE)
				.contains("fav_clicked")) {
			message = getArguments().getString(MainActivity.EXTRA_MESSAGE);
			message = message.substring(message.indexOf(":") + 1);
			if (CacheManager.getCache(getActivity(), message).length() <= 0)
				new RetrieveLyricsTask().execute(message);
			else
				setCache();
		}
		// Search text
		else {
			message = getArguments().getString(MainActivity.EXTRA_MESSAGE);
			if (CacheManager.getCache(getActivity(), message).length() <= 0)
				new RetrieveLyricsTask().execute(message);
			else
				setCache();
		}
		
		getActivity().getIntent().removeExtra(MainActivity.EXTRA_MESSAGE);
	}

	private class RetrieveLyricsTask extends AsyncTask<String, Void, String> {

		@Override
		protected String doInBackground(String... names) {
			urlObject = new Lyrics(names[0]);
			ConnectivityManager connMgr = (ConnectivityManager) getActivity()
					.getSystemService(Context.CONNECTIVITY_SERVICE);
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
				CrossfadeAnimation.crossfade(getActivity(), mContent,
						mLoadingView);
			else {
				mContent.setVisibility(View.VISIBLE);
				mLoadingView.setVisibility(View.GONE);
			}
			if (cacheLyricsEnabled)
				CacheManager.saveData(getActivity(), message, nameField
						.getText().toString() + result);
		}
	}
}
