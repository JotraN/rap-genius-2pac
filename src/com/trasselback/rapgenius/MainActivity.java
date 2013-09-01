package com.trasselback.rapgenius;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
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

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.MenuItem.OnActionExpandListener;

public class MainActivity extends SherlockFragmentActivity {
	public final static String EXTRA_MESSAGE = "com.trasselback.rapgenius.MESSAGE";
	private static boolean lyricsLoaded = false;

	private EditText search_text;

	private String[] mDrawerTitles;
	private DrawerLayout mDrawerLayout;
	private ListView mDrawerList;
	private ActionBarDrawerToggle mDrawerToggle;
	private CharSequence mTitle;
	private ListAdapter adapter;

	private Handler mHandler;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		mDrawerTitles = getResources().getStringArray(R.array.navigation_array);
		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		mDrawerList = (ListView) findViewById(R.id.left_drawer);

		// Set the adapter for the list view
		adapter = new ListAdapter(this, R.layout.drawer_list_item);
		for (String x : mDrawerTitles) {
			adapter.add(x);
		}
		mDrawerList.setAdapter(adapter);
		mDrawerList.setOnItemClickListener(new DrawerItemClickListener());

		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

		mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
				R.drawable.ic_blank, R.string.open_drawer,
				R.string.close_drawer);

		// Set the drawer toggle as the DrawerListener
		mDrawerLayout.setDrawerListener(mDrawerToggle);

		getSupportActionBar().setDisplayShowTitleEnabled(true);
		getSupportActionBar().setLogo(R.drawable.ic_drawer);
		getSupportActionBar().setDisplayUseLogoEnabled(true);
		getSupportActionBar().setHomeButtonEnabled(true);

		selectItem(0);
		if (getIntent().getData() != null
				&& getIntent().getDataString().contains("clicked")
				&& !lyricsLoaded) {
			Fragment fragment = new LyricsFragment();
			Bundle song = new Bundle();
			song.putString(EXTRA_MESSAGE, getIntent().getDataString());
			fragment.setArguments(song);
			FragmentManager fragmentManager = getSupportFragmentManager();
			fragmentManager.beginTransaction()
					.replace(R.id.content_frame, fragment).commit();
			setTitle("Lyrics");
			if (adapter.getPosition("More songs") == -1)
				adapter.add("More songs");
		}
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		// Sync the toggle state after onRestoreInstanceState has occurred.
		mDrawerToggle.syncState();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		// Pass any configuration change to the drawer toggles
		mDrawerToggle.onConfigurationChanged(newConfig);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getSupportMenuInflater().inflate(R.menu.main, menu);

		// Hide favorites icon by default
		menu.findItem(R.id.action_favorite).setVisible(false);

		// MenuItem to close search
		final MenuItem searchItem = menu.findItem(R.id.action_search);
		mHandler = new Handler();
		searchItem.setOnActionExpandListener(new OnActionExpandListener() {
			InputMethodManager keyboard = (InputMethodManager) getApplicationContext()
					.getSystemService(Context.INPUT_METHOD_SERVICE);

			@Override
			public boolean onMenuItemActionExpand(MenuItem item) {
				// Wait for edit text to load before calling focus
				mHandler.postDelayed(new Runnable() {
					@Override
					public void run() {
						search_text.clearFocus();
						if (android.os.Build.VERSION.SDK_INT < 11) {
							keyboard.toggleSoftInput(
									InputMethodManager.SHOW_FORCED,
									InputMethodManager.HIDE_IMPLICIT_ONLY);
						} else {
							keyboard.showSoftInput(search_text,
									InputMethodManager.SHOW_IMPLICIT);
						}
						search_text.requestFocus();
					}
				}, 1);
				return true;
			}

			@Override
			public boolean onMenuItemActionCollapse(MenuItem item) {
				if (android.os.Build.VERSION.SDK_INT < 11) {
					keyboard.hideSoftInputFromWindow(
							search_text.getWindowToken(), 0);
				} else {
					keyboard.hideSoftInputFromWindow(
							search_text.getWindowToken(),
							InputMethodManager.HIDE_NOT_ALWAYS);
				}
				return true;
			}
		});

		// Get search from search action view
		View v = (View) menu.findItem(R.id.action_search).getActionView();
		search_text = (EditText) v.findViewById(R.id.search_text);

		// search_text.clearFocus();

		search_text.setOnEditorActionListener(new OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView v, int actionId,
					KeyEvent event) {
				Fragment fragment = new LyricsFragment();
				Bundle song = new Bundle();
				song.putString(MainActivity.EXTRA_MESSAGE, search_text
						.getText().toString());
				fragment.setArguments(song);
				FragmentManager fragmentManager = getSupportFragmentManager();
				fragmentManager.beginTransaction()
						.replace(R.id.content_frame, fragment).commit();
				adapter.add("More songs");
				search_text.setText("");
				searchItem.collapseActionView();

				// startActivity(intent);
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
		default:
			return super.onOptionsItemSelected(item);
		}
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
		// update the main content by replacing fragments
		Fragment fragment = null;
		switch (position) {
		case 0:
			fragment = new HomePageFragment();
			FragmentManager fragmentManager = getSupportFragmentManager();
			fragmentManager.beginTransaction()
					.replace(R.id.content_frame, fragment).commit();
			mDrawerList.setItemChecked(position, true);
			setTitle(mDrawerTitles[position]);
			adapter.remove("More songs");
			break;
		case 1:
			fragment = new FavoritesFragment();
			FragmentManager fragmentManager1 = getSupportFragmentManager();
			fragmentManager1.beginTransaction()
					.replace(R.id.content_frame, fragment).commit();
			mDrawerList.setItemChecked(position, true);
			setTitle(mDrawerTitles[position]);
			adapter.remove("More songs");
			break;

		case 2:
			Intent intent;
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
				intent = new Intent(this, SettingsActivity.class);
			else
				intent = new Intent(this, SettingsPreferenceActivity.class);
			startActivity(intent);
			adapter.remove("More songs");
			break;

		case 3:
			Intent intent1;
			intent1 = new Intent(this, MoreSongsActivity.class);
			intent1.putExtra(MainActivity.EXTRA_MESSAGE, LyricsFragment.message);
			startActivity(intent1);
			break;
		}
		mDrawerLayout.closeDrawer(mDrawerList);
	}

	@Override
	public void setTitle(CharSequence title) {
		mTitle = title;
		getSupportActionBar().setTitle(mTitle);
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
