package com.trasselback.rapgenius;

import java.util.Locale;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class FavoritesFragment extends Fragment {
	private ListView listView;

	public FavoritesFragment() {
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_favorites_layout,
				container, false);
		return rootView;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
	}

	OnHeadlineSelectedListener mCallback;

	// Container Activity must implement this interface
	public interface OnHeadlineSelectedListener {
		public void onFavoriteSelected(int position);
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		// This makes sure that the container activity has implemented
		// the callback interface. If not, it throws an exception
		try {
			mCallback = (OnHeadlineSelectedListener) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString()
					+ " must implement OnHeadlineSelectedListener");
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		SharedPreferences sharedPref = PreferenceManager
				.getDefaultSharedPreferences(getActivity());
		// Update colors
		String color = sharedPref.getString(
				SettingsFragment.KEY_PREF_BACKGROUND_COLOR, "Default");
		if (!color.contains("Default")) {
			ColorManager.setBackgroundColor(getActivity(), color);
		} else
			getActivity().getWindow().setBackgroundDrawableResource(
					R.color.LightBlack);

		listView = (ListView) getView().findViewById(R.id.favsList);
		String favsString = FavoritesManager.getFavorites(getActivity())
				.toUpperCase(Locale.ENGLISH);
		String[] favsArray = favsString.split("<BR>");
		ListAdapter favs = new ListAdapter(getActivity(),
				R.layout.favs_list_item);
		for (String fav : favsArray)
			favs.add(fav);
		listView.setAdapter(favs);
		listView.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View v,
					int position, long id) {
				mCallback.onFavoriteSelected(position);
			}
		});
	}

	private class ListAdapter extends ArrayAdapter<String> {
		public ListAdapter(Context context, int resource) {
			super(context, resource);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View v = super.getView(position, convertView, parent);
			SharedPreferences sharedPref = PreferenceManager
					.getDefaultSharedPreferences(getActivity());
			String color = sharedPref.getString(
					SettingsFragment.KEY_PREF_DEFAULT_TEXT_COLOR, "Default");
			color = sharedPref.getString(
					SettingsFragment.KEY_PREF_FAVORITES_COLOR, "Default");
			if (!color.contains("Default")) {
				if (color.equals("Orange"))
					v.setBackgroundColor(getResources()
							.getColor(R.color.Orange));
				else if (color.equals("Yellow"))
					v.setBackgroundColor(getResources()
							.getColor(R.color.Yellow));
				else if (color.equals("Green"))
					v.setBackgroundColor(getResources().getColor(R.color.Green));
				else if (color.equals("Blue"))
					v.setBackgroundColor(getResources().getColor(R.color.Blue));
				else if (color.equals("Purple"))
					v.setBackgroundColor(getResources()
							.getColor(R.color.Purple));
				else if (color.equals("Gray"))
					v.setBackgroundColor(getResources().getColor(R.color.Gray));
				else if (color.equals("White"))
					v.setBackgroundColor(getResources().getColor(R.color.White));
				else if (color.equals("Black"))
					v.setBackgroundColor(getResources().getColor(R.color.Black));
				else
					v.setBackgroundColor(getResources().getColor(R.color.Red));
			} else
				v.setBackgroundColor(getResources().getColor(R.color.Red));
			return v;
		}
	}
}