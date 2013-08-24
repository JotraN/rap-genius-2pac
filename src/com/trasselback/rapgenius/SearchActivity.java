package com.trasselback.rapgenius;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

public class SearchActivity extends SherlockActivity {
	public final static String EXTRA_MESSAGE = "com.trasselback.rapgenius.MESSAGE";
	private TextView favorites, nameField;
	private EditText search_text;

	private String[] mDrawerTitles;
	private DrawerLayout mDrawerLayout;
	private ListView mDrawerList;
	private ActionBarDrawerToggle mDrawerToggle;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_search);
		// Show the Up button in the action bar.
		initialize();

		mDrawerTitles = getResources().getStringArray(R.array.navigation_array);
		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		mDrawerList = (ListView) findViewById(R.id.left_drawer);

		// Set the adapter for the list view
		ListAdapter adapter = new ListAdapter(this, R.layout.drawer_list_item);
		for (String x : mDrawerTitles) {
			adapter.add(x);
		}
		mDrawerList.setAdapter(adapter);
		mDrawerList.setOnItemClickListener(new DrawerItemClickListener());

		// Set back arrow to blank
		mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
				R.drawable.ic_blank, R.string.open_drawer,
				R.string.close_drawer);

		// Set the drawer toggle as the DrawerListener
		mDrawerLayout.setDrawerListener(mDrawerToggle);
		setupActionBar();
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		// Sync the toggle state after onRestoreInstanceState has occurred.
		mDrawerToggle.syncState();
	}

	private void setupActionBar() {
		getSupportActionBar().setDisplayShowTitleEnabled(true);
		getSupportActionBar().setLogo(R.drawable.ic_drawer);
		getSupportActionBar().setDisplayUseLogoEnabled(true);
		getSupportActionBar().setHomeButtonEnabled(true);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getSupportMenuInflater().inflate(R.menu.search, menu);

		// MenuItem to close search
		final MenuItem searchItem = menu.findItem(R.id.action_search);
		// Get search from search action view
		View v = (View) menu.findItem(R.id.action_search).getActionView();
		search_text = (EditText) v.findViewById(R.id.search_text);
		search_text.setOnEditorActionListener(new OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView v, int actionId,
					KeyEvent event) {
				InputMethodManager keyboard = (InputMethodManager) getApplicationContext()
						.getSystemService(Context.INPUT_METHOD_SERVICE);
				keyboard.hideSoftInputFromWindow(search_text.getWindowToken(),
						0);
				Intent intent = new Intent(SearchActivity.this,
						LyricsActivity.class);
				intent.putExtra(EXTRA_MESSAGE, search_text.getText().toString());
				intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

				search_text.setText("");
				searchItem.collapseActionView();

				startActivity(intent);
				return false;
			}
		});
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Add drawer toggle, temporary fix for SherlockActionBar
		if (item.getItemId() == android.R.id.home)
			if (mDrawerLayout.isDrawerOpen(mDrawerList))
				mDrawerLayout.closeDrawer(mDrawerList);
			else
				mDrawerLayout.openDrawer(mDrawerList);

		// Handle presses on the action bar items
		switch (item.getItemId()) {
		case R.id.action_search:
			search_text.requestFocus();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private void initialize() {
		nameField = (TextView) findViewById(R.id.nameText);
		favorites = (TextView) findViewById(R.id.lyricsText);
		favorites.setMovementMethod(LinkMovementMethod.getInstance());

		favorites.setText(Html.fromHtml(FavoritesManager
				.getFavorites(getApplicationContext())));
		RemoveUnderLine.removeUnderline(favorites);
	}

	@Override
	public void onResume() {
		super.onResume();
		favorites.setText(Html.fromHtml(FavoritesManager
				.getFavorites(getApplicationContext())));
		RemoveUnderLine.removeUnderline(favorites);
		SharedPreferences sharedPref = PreferenceManager
				.getDefaultSharedPreferences(this);

		// Update text size
		int size = Integer.parseInt(sharedPref.getString(
				SettingsFragment.KEY_PREF_TEXT_SIZE, "22"));
		favorites.setTextSize(TypedValue.COMPLEX_UNIT_SP, size + 10);
		nameField.setTextSize(TypedValue.COMPLEX_UNIT_SP, size + 10);

		// Update colors
		String color = sharedPref.getString(
				SettingsFragment.KEY_PREF_DEFAULT_TEXT_COLOR, "Default");
		color = sharedPref.getString(SettingsFragment.KEY_PREF_TITLE_COLOR,
				"Default");
		if (!color.contains("Default")) {
			ColorManager.setColor(getApplicationContext(), nameField, color);
		} else
			nameField.setTextColor(getResources().getColor(R.color.LightGray));
		color = sharedPref.getString(SettingsFragment.KEY_PREF_FAVORITES_COLOR,
				"Default");
		if (!color.contains("Default")) {
			ColorManager
					.setLinkColor(getApplicationContext(), favorites, color);
		} else
			favorites.setLinkTextColor(getResources().getColor(R.color.Red));
		color = sharedPref.getString(
				SettingsFragment.KEY_PREF_BACKGROUND_COLOR, "Default");
		if (!color.contains("Default")) {
			ColorManager.setBackgroundColor(this, color);
		} else
			getWindow().setBackgroundDrawableResource(R.color.LightBlack);
	}

	private class DrawerItemClickListener implements
			ListView.OnItemClickListener {
		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
				long arg3) {
			selectItem(arg2);

		}
	}

	private void selectItem(int position) {
		Intent intent = null;
		switch (position) {
		case 0:
			intent = new Intent(this, MainActivity.class);
			break;
		case 1:
			intent = new Intent(this, SearchActivity.class);
			break;
		case 2:
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
				intent = new Intent(this, SettingsActivity.class);
			else
				intent = new Intent(this, SettingsPreferenceActivity.class);
			break;
		default:
			break;
		}
		// Highlight the selected item, update the title, and close the drawer
		mDrawerList.setItemChecked(position, true);
		mDrawerLayout.closeDrawer(mDrawerList);

		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(intent);
	}

	private class ListAdapter extends ArrayAdapter<String> {
		public ListAdapter(Context context, int resource) {
			super(context, resource);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View v = super.getView(position, convertView, parent);
			switch (position) {
			case 0:
				v.setBackgroundColor(getResources().getColor(R.color.LightBlue));
				break;
			case 1:
				v.setBackgroundColor(getResources().getColor(R.color.Red));
				break;
			case 2:
				v.setBackgroundColor(getResources().getColor(R.color.Green));
				break;
			default:
				v.setBackgroundColor(getResources().getColor(R.color.Orange));
				break;
			}
			return v;
		}
	}
}