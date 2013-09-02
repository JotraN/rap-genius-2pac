package com.trasselback.rapgenius;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
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

public class MoreSongsFragment extends Fragment {
	private TextView nameField;
	private ListView songsList;
	private ListAdapter songs;
	private View mLoadingView;
	private View mContent;
	private String message = "";
	private MoreSongs moreSongs;
	private AsyncTask<String, Void, String> retrieveTask;
	private OnMoreSongsSelectedListener mCallback;
	private String[] songsArray;

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
		mLoadingView = getView().findViewById(R.id.loadingView);
		mContent = getView().findViewById(R.id.infoView);
		mContent.setVisibility(View.GONE);
		retrieveTask = new RetrieveMoreSongs();
		message = getArguments().getString(MainActivity.EXTRA_MESSAGE);
		retrieveTask.execute(message);
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
			mCallback = (OnMoreSongsSelectedListener) activity;
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
			retrieveTask.execute(message);
		}
		SharedPreferences sharedPref = PreferenceManager
				.getDefaultSharedPreferences(getActivity());
		int size = Integer.parseInt(sharedPref.getString(
				SettingsFragment.KEY_PREF_TEXT_SIZE, "22"));
		nameField.setTextSize(TypedValue.COMPLEX_UNIT_SP, size + 10);

		// Update colors
		String color = sharedPref.getString(
				SettingsFragment.KEY_PREF_BACKGROUND_COLOR, "Default");
		if (!color.contains("Default")) {
			ColorManager.setBackgroundColor(getActivity(), color);
		} else
			getActivity().getWindow().setBackgroundDrawableResource(
					R.color.LightBlack);
		color = sharedPref.getString(SettingsFragment.KEY_PREF_TITLE_COLOR,
				"Default");
		if (!color.contains("Default")) {
			ColorManager.setColor(getActivity(), nameField, color);
		} else
			nameField.setTextColor(getResources().getColor(R.color.LightGray));

		songsList = (ListView) getView().findViewById(R.id.songsList);
		songs = new ListAdapter(getActivity(), R.layout.more_songs_list_item);
		songsList.setAdapter(songs);
		songsList.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View v,
					int position, long id) {
				mCallback.onMoreSongsSelected(nameField.getText().toString()
						.replace("More Songs by ", "")
						+ " " + songs.getItem(position));
			}
		});
		if (songs.isEmpty() && songsArray != null)
			for (String x : songsArray)
				songs.add(x);
	}

	private class RetrieveMoreSongs extends AsyncTask<String, Void, String> {

		@Override
		protected String doInBackground(String... names) {
			moreSongs = new MoreSongs(names[0]);
			ConnectivityManager connMgr = (ConnectivityManager) getActivity()
					.getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
			if (networkInfo != null && networkInfo.isConnected()) {
				if (moreSongs.openURL()) {
					moreSongs.retrievePage();
					moreSongs.retrieveName();
				}
				return moreSongs.getPage();
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
			nameField.setText(moreSongs.getName());
			songsArray = result.split("<br>");
			for (String x : songsArray)
				songs.add(x);
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1)
				CrossfadeAnimation.crossfade(getActivity(), mContent,
						mLoadingView);
			else {
				// lyricsField.setMovementMethod(new LinkMovementMethod());
				mContent.setVisibility(View.VISIBLE);
				mLoadingView.setVisibility(View.GONE);
			}
		}
	}

	private class ListAdapter extends ArrayAdapter<String> {
		public ListAdapter(Context context, int resource) {
			super(context, resource);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View v = super.getView(position, convertView, parent);
			TextView x = (TextView) v;
			SharedPreferences sharedPref = PreferenceManager
					.getDefaultSharedPreferences(getActivity());
			String color = sharedPref
					.getString(
							SettingsFragment.KEY_PREF_EXPLAINED_LYRICS_COLOR,
							"Default");
			if (!color.contains("Default"))
				ColorManager.setColor(getContext(), x, color);
			else
				x.setTextColor(getResources().getColor(R.color.Orange));
			int size = Integer.parseInt(sharedPref.getString(
					SettingsFragment.KEY_PREF_TEXT_SIZE, "22"));
			x.setTextSize(TypedValue.COMPLEX_UNIT_SP, size);
			return x;
		}
	}
}
