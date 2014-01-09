package com.trasselback.rapgenius.activities;

import java.io.IOException;
import java.io.InputStream;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.MenuItem.OnActionExpandListener;
import com.trasselback.rapgenius.R;
import com.trasselback.rapgenius.fragments.FavoritesFragment;
import com.trasselback.rapgenius.fragments.HomePageFragment;
import com.trasselback.rapgenius.fragments.LyricsFragment;
import com.trasselback.rapgenius.fragments.MoreSongsFragment;
import com.trasselback.rapgenius.helpers.FavoritesManager;
import com.trasselback.rapgenius.preferences.SettingsActivity;
import com.trasselback.rapgenius.preferences.SettingsPreferenceActivity;

public class MainActivity extends SherlockFragmentActivity implements
		FavoritesFragment.OnFavoriteSelectedListener,
		MoreSongsFragment.OnMoreSongsSelectedListener {
	public final static String EXTRA_MESSAGE = "com.trasselback.rapgenius.MESSAGE";
	private static boolean lyricsLoaded = false;
	private boolean hideFavoritesIcon = true;
	private MenuItem favoritesItem;
	private EditText searchText;
	private String[] drawerTitles;
	private DrawerLayout drawerLayout;
	private ListView drawerList;
	private ActionBarDrawerToggle drawerToggle;
	private ListAdapter adapter;
	private Handler delayHandler;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		initialize();
		setupActionBar();

		// Check to see if activity was started by a home song being clicked
		boolean homeSongClicked = getIntent().getData() != null
				&& getIntent().getDataString().contains("clicked")
				&& !lyricsLoaded;
		if (homeSongClicked)
			loadHomeSong();

	}

	private void initialize() {
		drawerTitles = getResources().getStringArray(R.array.navigation_array);
		drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		drawerList = (ListView) findViewById(R.id.left_drawer);

		// Set the adapter for the list view
		adapter = new ListAdapter(this, R.layout.drawer_list_item);
		for (String title : drawerTitles) {
			adapter.add(title);
		}
		drawerList.setAdapter(adapter);
		drawerList.setOnItemClickListener(new DrawerItemClickListener());

		drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

		drawerToggle = new ActionBarDrawerToggle(this, drawerLayout,
				R.drawable.ic_blank, R.string.open_drawer,
				R.string.close_drawer);

		// Set the drawer toggle as the DrawerListener
		drawerLayout.setDrawerListener(drawerToggle);

		// Set to home fragment by default
		selectItem(0);
	}

	private void setupActionBar() {
		getSupportActionBar().setDisplayShowTitleEnabled(true);
		// Need to set manually because actionbarSherlock bug
		getSupportActionBar().setLogo(R.drawable.ic_drawer);
		getSupportActionBar().setDisplayUseLogoEnabled(true);
		getSupportActionBar().setHomeButtonEnabled(true);
	}

	public void loadHomeSong() {
		Fragment fragment = new LyricsFragment();
		Bundle song = new Bundle();
		song.putString(EXTRA_MESSAGE, getIntent().getDataString());
		fragment.setArguments(song);
		FragmentManager fragmentManager = getSupportFragmentManager();
		fragmentManager.beginTransaction()
				.replace(R.id.content_frame, fragment).commit();
		setTitle("LYRICS");
		if (adapter.getPosition("MORE SONGS") == -1)
			adapter.add("MORE SONGS");
		if (adapter.getPosition("BACK TO LYRICS") != -1)
			adapter.remove("BACK TO LYRICS");
		hideFavoritesIcon = false;
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		// Sync the toggle state after onRestoreInstanceState has occurred.
		drawerToggle.syncState();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		// Pass any configuration change to the drawer toggles
		drawerToggle.onConfigurationChanged(newConfig);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getSupportMenuInflater().inflate(R.menu.main, menu);

		manageFavoritesItem(menu);

		// MenuItem to close/open search bar
		final MenuItem searchItem = menu.findItem(R.id.action_search);

		delayHandler = new Handler();
		searchItem.setOnActionExpandListener(new expandedSearchBar());

		// Get search from search action view
		View searchView = (View) menu.findItem(R.id.action_search)
				.getActionView();
		searchText = (EditText) searchView.findViewById(R.id.search_text);

		final MenuItem favsItem = menu.findItem(R.id.action_favorite);

		searchText.setOnEditorActionListener(new OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView v, int actionId,
					KeyEvent event) {
				changeToLyricsFragment(searchItem, favsItem);
				return false;
			}
		});
		return true;
	}

	private void manageFavoritesItem(Menu menu) {
		favoritesItem = menu.findItem(R.id.action_favorite);
		if (hideFavoritesIcon)
			favoritesItem.setVisible(false);
		else if (FavoritesManager.checkFavorites(this,
				LyricsFragment.artistNameSongName))
			favoritesItem.setIcon(R.drawable.ic_star_pressed);
	}

	// Search bar that shows/hides keyboard when search is expanded/collapsed
	private class expandedSearchBar implements OnActionExpandListener {
		InputMethodManager keyboard = (InputMethodManager) getApplicationContext()
				.getSystemService(Context.INPUT_METHOD_SERVICE);

		@Override
		public boolean onMenuItemActionExpand(MenuItem item) {
			// Wait for edit text view to load before calling focus
			delayHandler.postDelayed(new Runnable() {
				@Override
				public void run() {
					searchText.clearFocus();
					if (android.os.Build.VERSION.SDK_INT < 11)
						keyboard.toggleSoftInput(
								InputMethodManager.SHOW_FORCED,
								InputMethodManager.HIDE_IMPLICIT_ONLY);
					else
						keyboard.showSoftInput(searchText,
								InputMethodManager.SHOW_IMPLICIT);
					searchText.requestFocus();
				}
			}, 1);
			return true;
		}

		@Override
		public boolean onMenuItemActionCollapse(MenuItem item) {
			if (android.os.Build.VERSION.SDK_INT < 11)
				keyboard.hideSoftInputFromWindow(searchText.getWindowToken(), 0);
			else
				keyboard.hideSoftInputFromWindow(searchText.getWindowToken(),
						InputMethodManager.HIDE_NOT_ALWAYS);
			return true;
		}
	};

	private void changeToLyricsFragment(MenuItem searchItem, MenuItem favsItem) {
		// Change to lyrics fragment
		Fragment fragment = new LyricsFragment();
		Bundle song = new Bundle();
		song.putString(MainActivity.EXTRA_MESSAGE, searchText.getText()
				.toString());
		fragment.setArguments(song);
		FragmentManager fragmentManager = getSupportFragmentManager();
		fragmentManager.beginTransaction()
				.replace(R.id.content_frame, fragment).commit();

		// Update drawer
		addMoreSongsItem();
		setTitle(getResources().getString(R.string.drawer_title_lyrics));

		// Reload favorites icon
		if (FavoritesManager.checkFavorites(getApplicationContext(), searchText
				.getText().toString())) {
			favsItem.setIcon(R.drawable.ic_star_pressed);
		} else {
			favsItem.setIcon(R.drawable.ic_star_not_pressed);
		}

		searchText.setText("");
		searchItem.collapseActionView();
		favsItem.setVisible(true);
		hideFavoritesIcon = false;
	}

	private void addMoreSongsItem() {
		String moreSongsTitle = getResources().getString(
				R.string.drawer_title_more_songs);
		String backLyricsTitle = getResources().getString(
				R.string.drawer_title_back_to_lyrics);

		if (adapter.getPosition(moreSongsTitle) == -1)
			adapter.add(moreSongsTitle);
		if (adapter.getPosition(backLyricsTitle) != -1)
			adapter.remove(backLyricsTitle);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Add drawer toggle, temporary fix for SherlockActionBar
		if (item.getItemId() == android.R.id.home)
			if (drawerLayout.isDrawerOpen(drawerList))
				drawerLayout.closeDrawer(drawerList);
			else
				drawerLayout.openDrawer(drawerList);

		// Handle clicks on the action bar items
		switch (item.getItemId()) {
		case R.id.action_favorite:
			FavoritesManager.addFavorites(this,
					LyricsFragment.artistNameSongName);
			if (FavoritesManager.checkFavorites(this,
					LyricsFragment.artistNameSongName)) {
				item.setIcon(R.drawable.ic_star_pressed);
			} else {
				item.setIcon(R.drawable.ic_star_not_pressed);
			}
			return true;
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
			// Clear choice
			drawerList.setItemChecked(-1, true);
		}
	}

	private void selectItem(int position) {
		// update the main content by replacing fragments
		Fragment fragment = null;
		FragmentManager fragmentManager = getSupportFragmentManager();

		switch (position) {
		case 0:
			fragment = new HomePageFragment();
			drawerList.setItemChecked(position, true);
			setTitle(drawerTitles[position]);
			if (!hideFavoritesIcon) {
				favoritesItem.setVisible(false);
				hideFavoritesIcon = true;
			}
			cleanUpDrawer();
			break;
		case 1:
			fragment = new FavoritesFragment();
			drawerList.setItemChecked(position, true);
			setTitle(drawerTitles[position]);
			if (!hideFavoritesIcon) {
				favoritesItem.setVisible(false);
				hideFavoritesIcon = true;
			}
			cleanUpDrawer();
			break;
		case 2:
			Intent intent;
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
				intent = new Intent(this, SettingsActivity.class);
			else
				intent = new Intent(this, SettingsPreferenceActivity.class);
			startActivity(intent);
			break;
		case 3:
			fragment = setupFourthItem();
			break;
		}
		fragmentManager.beginTransaction()
				.replace(R.id.content_frame, fragment).commit();
		drawerLayout.closeDrawer(drawerList);
	}

	@Override
	public void setTitle(CharSequence title) {
		getSupportActionBar().setTitle(title);
	}

	private void cleanUpDrawer() {
		String moreSongs = getResources().getString(
				R.string.drawer_title_more_songs);
		String backToLyrics = getResources().getString(
				R.string.drawer_title_back_to_lyrics);
		
		if (adapter.getPosition(moreSongs) != -1)
			adapter.remove(moreSongs);
		if (adapter.getPosition(backToLyrics) != -1)
			adapter.remove(backToLyrics);
	}

	private Fragment setupFourthItem() {
		Fragment fragment = null;
		String moreSongs = getResources().getString(
				R.string.drawer_title_more_songs);
		String backToLyrics = getResources().getString(
				R.string.drawer_title_back_to_lyrics);

		if (adapter.getItem(3).contains(moreSongs)) {
			fragment = new MoreSongsFragment();
			Bundle song = new Bundle();
			song.putString(EXTRA_MESSAGE, LyricsFragment.artistNameSongName);
			fragment.setArguments(song);
			drawerList.setItemChecked(3, true);
			setTitle(getResources().getString(R.string.drawer_title_more_songs));
			if (adapter.getPosition(backToLyrics) == -1)
				adapter.add(backToLyrics);
			if (!hideFavoritesIcon) {
				favoritesItem.setVisible(false);
				hideFavoritesIcon = true;
			}
			adapter.remove(moreSongs);
		} else if (adapter.getItem(3).contains(backToLyrics)) {
			fragment = new LyricsFragment();
			Bundle song = new Bundle();
			song.putString(EXTRA_MESSAGE, LyricsFragment.artistNameSongName);
			fragment.setArguments(song);
			setTitle(getResources().getString(R.string.drawer_title_lyrics));
			if (adapter.getPosition(moreSongs) == -1)
				adapter.add(moreSongs);
			favoritesItem.setVisible(true);
			hideFavoritesIcon = false;
			adapter.remove(backToLyrics);
		}
		return fragment;
	}

	// List adapter for drawer
	private class ListAdapter extends ArrayAdapter<String> {
		private final Context context;
		private final int resource;

		public ListAdapter(Context context, int resource) {
			super(context, resource);
			this.context = context;
			this.resource = resource;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			LayoutInflater inflater = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View v = inflater.inflate(resource, parent, false);

			ImageView imageView = (ImageView) v.findViewById(R.id.imageView);
			TextView textView = (TextView) v.findViewById(R.id.textView);
			// Have to manually set text because adapter isn't just text view
			textView.setText(adapter.getItem(position));

			InputStream ims = null;

			// Change background and icon depending on position
			switch (position) {
			case 0:
				try {
					ims = context.getAssets().open("ic_menu_home.png");
				} catch (IOException e) {
					e.printStackTrace();
				}
				v.setBackgroundColor(getResources().getColor(R.color.LightBlue));
				break;
			case 1:
				try {
					ims = context.getAssets().open("ic_menu_star.png");
				} catch (IOException e) {
					e.printStackTrace();
				}
				v.setBackgroundColor(getResources().getColor(R.color.Red));
				break;
			case 2:
				try {
					ims = context.getAssets().open("ic_menu_manage.png");
				} catch (IOException e) {
					e.printStackTrace();
				}
				v.setBackgroundColor(getResources().getColor(R.color.Green));
				break;
			case 3:
				try {
					if (adapter.getItem(position) == getResources().getString(
							R.string.drawer_title_more_songs))
						ims = context.getAssets().open(
								"ic_menu_info_details.png");
					else
						ims = context.getAssets().open("ic_menu_revert.png");
				} catch (IOException e) {
					e.printStackTrace();
				}
				v.setBackgroundColor(getResources().getColor(R.color.Yellow));
				break;
			default:
				v.setBackgroundColor(getResources().getColor(R.color.Orange));
				break;
			}
			Drawable d = Drawable.createFromStream(ims, null);
			imageView.setImageDrawable(d);

			// Set font
			Typeface tf = Typeface.createFromAsset(getAssets(),
					"fonts/roboto_condensed_light.ttf");
			textView.setTypeface(tf);
			return v;
		}
	}

	@Override
	public void onFavoriteSelected(int position) {
		String favsString = FavoritesManager.getFavorites(this);
		String[] favsArray = favsString.split("<BR>");
		Fragment fragment = new LyricsFragment();
		Bundle song = new Bundle();
		song.putString(EXTRA_MESSAGE, favsArray[position]);
		fragment.setArguments(song);
		FragmentManager fragmentManager = getSupportFragmentManager();
		fragmentManager.beginTransaction()
				.replace(R.id.content_frame, fragment).commit();
		setTitle("LYRICS");
		if (adapter.getPosition("MORE SONGS") == -1)
			adapter.add("MORE SONGS");
		favoritesItem.setVisible(true);
		// Reload favorites icon
		if (FavoritesManager.checkFavorites(getApplicationContext(),
				favsArray[position])) {
			favoritesItem.setIcon(R.drawable.ic_star_pressed);
		} else {
			favoritesItem.setIcon(R.drawable.ic_star_not_pressed);
		}
		hideFavoritesIcon = false;
	}

	@Override
	public void onMoreSongsSelected(String songName) {
		Fragment fragment = new LyricsFragment();
		Bundle song = new Bundle();
		song.putString(EXTRA_MESSAGE, songName);
		fragment.setArguments(song);
		FragmentManager fragmentManager = getSupportFragmentManager();
		fragmentManager.beginTransaction()
				.replace(R.id.content_frame, fragment).commit();
		setTitle("LYRICS");
		if (adapter.getPosition("MORE SONGS") == -1)
			adapter.add("MORE SONGS");
		favoritesItem.setVisible(true);
		// Reload favorites icon
		if (FavoritesManager.checkFavorites(getApplicationContext(),
				songName.toString())) {
			favoritesItem.setIcon(R.drawable.ic_star_pressed);
		} else {
			favoritesItem.setIcon(R.drawable.ic_star_not_pressed);
		}
		hideFavoritesIcon = false;
		adapter.remove("BACK TO LYRICS");
	}

}
