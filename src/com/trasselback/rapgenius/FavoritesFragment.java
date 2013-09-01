package com.trasselback.rapgenius;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class FavoritesFragment extends Fragment {
	private TextView favorites;

	public FavoritesFragment() {
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_search_layout, container,
				false);
		return rootView;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		initialize();
	}

	private void initialize() {
		favorites = (TextView) getView().findViewById(R.id.lyricsText);
		favorites.setMovementMethod(LinkMovementMethod.getInstance());

		favorites.setText(Html.fromHtml(FavoritesManager
				.getFavorites(getActivity())));
		RemoveUnderLine.removeUnderline(favorites);
	}

	@Override
	public void onResume() {
		super.onResume();
		favorites.setText(Html.fromHtml(FavoritesManager
				.getFavorites(getActivity())));
		RemoveUnderLine.removeUnderline(favorites);
		SharedPreferences sharedPref = PreferenceManager
				.getDefaultSharedPreferences(getActivity());

		// Update text size
		int size = Integer.parseInt(sharedPref.getString(
				SettingsFragment.KEY_PREF_TEXT_SIZE, "22"));
		favorites.setTextSize(TypedValue.COMPLEX_UNIT_SP, size + 10);

		// Update colors
		String color = sharedPref.getString(
				SettingsFragment.KEY_PREF_DEFAULT_TEXT_COLOR, "Default");
		color = sharedPref.getString(SettingsFragment.KEY_PREF_FAVORITES_COLOR,
				"Default");
		if (!color.contains("Default")) {
			ColorManager
					.setLinkColor(getActivity(), favorites, color);
		} else
			favorites.setLinkTextColor(getResources().getColor(R.color.Red));
		color = sharedPref.getString(
				SettingsFragment.KEY_PREF_BACKGROUND_COLOR, "Default");
		if (!color.contains("Default")) {
			ColorManager.setBackgroundColor(getActivity(), color);
		} else
			getActivity().getWindow().setBackgroundDrawableResource(R.color.LightBlack);
	}
}