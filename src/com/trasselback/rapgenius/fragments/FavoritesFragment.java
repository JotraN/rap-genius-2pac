package com.trasselback.rapgenius.fragments;

import java.util.Locale;

import com.trasselback.rapgenius.R;
import com.trasselback.rapgenius.helpers.ColorManager;
import com.trasselback.rapgenius.helpers.FavoritesManager;
import com.trasselback.rapgenius.preferences.SettingsFragment;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

public class FavoritesFragment extends Fragment {
	private ListView listView;
	private TextView nameField;
	private EditText favsSearch;
	private OnFavoriteSelectedListener callback;

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
		nameField = (TextView) getView().findViewById(R.id.nameText);
		Typeface tf = Typeface.createFromAsset(getActivity().getAssets(),
				"fonts/roboto_thin.ttf");
		nameField.setTypeface(tf);
		nameField.setText(R.string.default_favorite);
		favsSearch = (EditText) getView().findViewById(R.id.favsSearch);
		favsSearch.setTypeface(tf);
		listView = (ListView) getView().findViewById(R.id.favsList);
	}

	// Container Activity must implement this interface
	public interface OnFavoriteSelectedListener {
		public void onFavoriteSelected(int position);
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		// This makes sure that the container activity has implemented
		// the callback interface. If not, it throws an exception
		try {
			callback = (OnFavoriteSelectedListener) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString()
					+ " must implement OnHeadlineSelectedListener");
		}
	}

	@Override
	public void onResume() {
		super.onResume();

		favsSearch.setVisibility(View.GONE);

		String favoritesFile = FavoritesManager.getFavorites(getActivity())
				.toUpperCase(Locale.ENGLISH);
		
		if (favoritesFile != "") {
			String[] favsArray = favoritesFile.split("<BR>");
			SharedPreferences sharedPref = PreferenceManager
					.getDefaultSharedPreferences(getActivity());
			boolean favoritesSearchEnabled = sharedPref.getBoolean(SettingsFragment.KEY_PREF_FAVS_SEARCH, true);
			if (favoritesSearchEnabled) {
				if (favsArray.length > 10) {
					nameField.setVisibility(View.GONE);
					favsSearch.setVisibility(View.VISIBLE);
					searchFavorites();
				}
			} else
				nameField.setVisibility(View.VISIBLE);
			setupFavsAdapter(favsArray);
		}

		checkSettings();
	}

	private void searchFavorites() {
		favsSearch.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				for (int i = 0; i < listView.getCount(); i++) {
					String item = listView.getItemAtPosition(i).toString();
					// Scroll to item position if item contains search text
					if (item.contains(s.toString().toUpperCase(
							Locale.getDefault())))
						listView.smoothScrollToPosition(i);
				}
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}

			@Override
			public void afterTextChanged(Editable s) {
			}
		});
	}

	private void setupFavsAdapter(String[] favorites) {
		FavoritesListAdapter favs = new FavoritesListAdapter(getActivity(),
				R.layout.favs_list_item);
		for (String fav : favorites)
			favs.add(fav);
		listView.setAdapter(favs);
		listView.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View v,
					int position, long id) {
				callback.onFavoriteSelected(position);
			}
		});
	}

	private void checkSettings() {
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
	}

	private class FavoritesListAdapter extends ArrayAdapter<String> {
		public FavoritesListAdapter(Context context, int resource) {
			super(context, resource);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View v = super.getView(position, convertView, parent);
			TextView x = (TextView) v;
			SharedPreferences sharedPref = PreferenceManager
					.getDefaultSharedPreferences(getActivity());
			int favsColor = Integer.parseInt(sharedPref.getString(
					SettingsFragment.KEY_PREF_FAVORITES_COLOR, "0"));
			ColorManager.setColor(getActivity(), x, favsColor);
			int size = Integer.parseInt(sharedPref.getString(
					SettingsFragment.KEY_PREF_TEXT_SIZE, "22"));
			x.setTextSize(TypedValue.COMPLEX_UNIT_SP, size);
			return x;
		}
	}
}