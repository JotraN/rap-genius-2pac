package com.trasselback.rapgenius.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.trasselback.rapgenius.R;
import com.trasselback.rapgenius.activities.MainActivity;
import com.trasselback.rapgenius.data.MoreSongs;
import com.trasselback.rapgenius.helpers.ColorManager;
import com.trasselback.rapgenius.helpers.CrossfadeAnimation;
import com.trasselback.rapgenius.preferences.SettingsFragment;

public class MoreSongsFragment extends Fragment {
	private TextView nameField;
	private ListView songsList;
	private MoreSongsListAdapter songs;
	private View loadingView;
	private View contentView;
	private String artistNameSongName = "";
	private MoreSongs moreSongs;
	private AsyncTask<String, Void, String> retrieveTask;
	private OnMoreSongsSelectedListener callback;
	private String[] songsArray;

	public MoreSongsFragment() {
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_more_songs_layout,
				container, false);
		return rootView;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		nameField = (TextView) getView().findViewById(R.id.nameText);
		Typeface tf = Typeface.createFromAsset(getActivity().getAssets(),
				"fonts/roboto_thin.ttf");
		nameField.setTypeface(tf);
		loadingView = getView().findViewById(R.id.loadingView);
		contentView = getView().findViewById(R.id.infoView);
		contentView.setVisibility(View.GONE);
		retrieveTask = new RetrieveMoreSongs();
		artistNameSongName = getArguments().getString(
				MainActivity.EXTRA_MESSAGE);
		if (!artistNameSongName
				.contains("There was a problem with finding the lyrics."))
			retrieveTask.execute(artistNameSongName);
		else
			nameField.setText("Song not found.");
	}

	@Override
	public void onPause() {
		super.onPause();
		retrieveTask.cancel(true);
	}

	// Container Activity must implement this interface
	public interface OnMoreSongsSelectedListener {
		public void onMoreSongsSelected(String songName);
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		// This makes sure that the container activity has implemented
		// the callback interface. If not, it throws an exception
		try {
			callback = (OnMoreSongsSelectedListener) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString()
					+ " must implement OnHeadlineSelectedListener");
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		// Restart retrieve task if it was cancelled
		if (retrieveTask.isCancelled()) {
			retrieveTask = new RetrieveMoreSongs();
			if (!artistNameSongName
					.contains("There was a problem with finding the lyrics."))
				retrieveTask.execute(artistNameSongName);
			else
				nameField.setText("Song not found.");
		}

		checkSettings();

		songsList = (ListView) getView().findViewById(R.id.songsList);
		songs = new MoreSongsListAdapter(getActivity(),
				R.layout.more_songs_list_item);
		songsList.setAdapter(songs);
		songsList.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View v,
					int position, long id) {
				callback.onMoreSongsSelected(nameField.getText().toString()
						.replace("More Songs by ", "")
						+ " " + songs.getItem(position));
			}
		});
		if (songs.isEmpty() && songsArray != null)
			for (String x : songsArray)
				songs.add(x);
	}

	private void checkSettings() {
		try {
			SharedPreferences sharedPref = PreferenceManager
					.getDefaultSharedPreferences(getActivity());
			// Update text size
			int size = Integer.parseInt(sharedPref.getString(
					SettingsFragment.KEY_PREF_TEXT_SIZE, "20"));
			nameField.setTextSize(TypedValue.COMPLEX_UNIT_SP, size + 10);

			// Update colors
			int titleColor = Integer.parseInt(sharedPref.getString(
					SettingsFragment.KEY_PREF_TITLE_COLOR, "0"));
			ColorManager.setColor(getActivity(), nameField, titleColor);
			int backgroundColor = Integer.parseInt(sharedPref.getString(
					SettingsFragment.KEY_PREF_BACKGROUND_COLOR, "0"));
			ColorManager.setBackgroundColor(getActivity(), backgroundColor);
		} catch (NumberFormatException ex) {
			clearSettings();
		}
	}

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

	private class RetrieveMoreSongs extends AsyncTask<String, Void, String> {

		@Override
		protected String doInBackground(String... names) {
			moreSongs = new MoreSongs(names[0]);
			if (moreSongs.isOnline(getActivity())) {
				if (moreSongs.openURL()) {
					moreSongs.retrievePage();
					moreSongs.retrieveName();
				}
				return moreSongs.getPage();
			} else
				return getString(R.string.error_no_internet);
		}

		@Override
		protected void onPreExecute() {
			if (retrieveTask != null)
				if (retrieveTask.isCancelled())
					return;
		}

		@Override
		protected void onPostExecute(String result) {
			nameField.setText(moreSongs.getName());
			songsArray = result.split("<br>");
			for (String x : songsArray)
				songs.add(x);
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1)
				CrossfadeAnimation.crossfade(getActivity(), contentView,
						loadingView);
			else {
				// lyricsField.setMovementMethod(new LinkMovementMethod());
				contentView.setVisibility(View.VISIBLE);
				loadingView.setVisibility(View.GONE);
			}
		}
	}

	private class MoreSongsListAdapter extends ArrayAdapter<String> {
		public MoreSongsListAdapter(Context context, int resource) {
			super(context, resource);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View v = super.getView(position, convertView, parent);
			TextView x = (TextView) v;
			// TODO Delete try and catch after several updates 2.7.6
			try {
				SharedPreferences sharedPref = PreferenceManager
						.getDefaultSharedPreferences(getActivity());
				int color = Integer.parseInt(sharedPref.getString(
						SettingsFragment.KEY_PREF_EXPLAINED_LYRICS_COLOR,
						"Default"));
				ColorManager.setColor(getContext(), x, color);

				int size = Integer.parseInt(sharedPref.getString(
						SettingsFragment.KEY_PREF_TEXT_SIZE, "22"));
				x.setTextSize(TypedValue.COMPLEX_UNIT_SP, size);
			} catch (NumberFormatException ex) {
				clearSettings();
			}
			return x;
		}
	}
}
