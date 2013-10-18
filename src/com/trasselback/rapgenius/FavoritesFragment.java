package com.trasselback.rapgenius;

import java.util.Locale;

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
	private OnFavoriteSelectedListener mCallback;

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
		nameField.setText("Favorited Songs");
		favsSearch = (EditText) getView().findViewById(R.id.favsSearch);
		favsSearch.setTypeface(tf);
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
			mCallback = (OnFavoriteSelectedListener) activity;
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

		color = sharedPref.getString(SettingsFragment.KEY_PREF_TITLE_COLOR,
				"Default");
		if (!color.contains("Default")) {
			ColorManager.setColor(getActivity(), nameField, color);
		} else
			nameField.setTextColor(getResources().getColor(R.color.LightGray));
		int size = Integer.parseInt(sharedPref.getString(
				SettingsFragment.KEY_PREF_TEXT_SIZE, "22"));

		nameField.setTextSize(TypedValue.COMPLEX_UNIT_SP, size + 10);
		listView = (ListView) getView().findViewById(R.id.favsList);
		String favsString = FavoritesManager.getFavorites(getActivity())
				.toUpperCase(Locale.ENGLISH);
		favsSearch.setVisibility(View.GONE);
		if (favsString != "") {
			String[] favsArray = favsString.split("<BR>");
			if (sharedPref.getBoolean(SettingsFragment.KEY_PREF_FAVS_SEARCH,
					true)) {
				if (favsArray.length > 10) {
					nameField.setVisibility(View.GONE);
					favsSearch.setVisibility(View.VISIBLE);
					favsSearch.addTextChangedListener(new TextWatcher() {

						@Override
						public void onTextChanged(CharSequence s, int start,
								int before, int count) {
							for (int i = 0; i < listView.getCount(); i++) {
								if (listView
										.getItemAtPosition(i)
										.toString()
										.contains(
												s.toString().toUpperCase(
														Locale.getDefault()))) {
									listView.smoothScrollToPosition(i);
								}
							}
						}

						@Override
						public void beforeTextChanged(CharSequence s,
								int start, int count, int after) {
							// TODO Auto-generated method stub

						}

						@Override
						public void afterTextChanged(Editable s) {
							// TODO Auto-generated method stub

						}
					});
				}
			} else
				nameField.setVisibility(View.VISIBLE);
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
			String color = sharedPref.getString(
					SettingsFragment.KEY_PREF_FAVORITES_COLOR, "Default");
			if (!color.contains("Default"))
				ColorManager.setColor(getActivity(), x, color);
			else
				x.setTextColor(getResources().getColor(R.color.Red));
			int size = Integer.parseInt(sharedPref.getString(
					SettingsFragment.KEY_PREF_TEXT_SIZE, "22"));
			x.setTextSize(TypedValue.COMPLEX_UNIT_SP, size);
			return x;
		}
	}
}