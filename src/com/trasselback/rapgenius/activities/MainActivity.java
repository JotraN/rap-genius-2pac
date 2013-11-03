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
	private boolean hideFavs = true;
	private MenuItem favItem;

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

		// TODO Delete later
		FavoritesManager.updateFavorites(getApplicationContext());

		initialize();
		setupActionBar();

		// Check to see if activity started by home song clicked
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
			setTitle("LYRICS");
			if (adapter.getPosition("MORE SONGS") == -1)
				adapter.add("MORE SONGS");
			if (adapter.getPosition("BACK TO LYRICS") != -1)
				adapter.remove("BACK TO LYRICS");
			hideFavs = false;
		}
	}

	private void setupActionBar() {
		getSupportActionBar().setDisplayShowTitleEnabled(true);
		getSupportActionBar().setLogo(R.drawable.ic_drawer);
		getSupportActionBar().setDisplayUseLogoEnabled(true);
		getSupportActionBar().setHomeButtonEnabled(true);
	}

	private void initialize() {
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

		// Set to home fragment
		selectItem(0);
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

		favItem = menu.findItem(R.id.action_favorite);
		if (hideFavs)
			favItem.setVisible(false);
		else if (FavoritesManager.checkFavorites(this, LyricsFragment.message))
			favItem.setIcon(R.drawable.ic_star_pressed);

		// MenuItem to close search bar
		final MenuItem searchItem = menu.findItem(R.id.action_search);

		mHandler = new Handler();
		searchItem.setOnActionExpandListener(new OnActionExpandListener() {
			InputMethodManager keyboard = (InputMethodManager) getApplicationContext()
					.getSystemService(Context.INPUT_METHOD_SERVICE);

			@Override
			public boolean onMenuItemActionExpand(MenuItem item) {
				// Wait for edit text view to load before calling focus
				mHandler.postDelayed(new Runnable() {
					@Override
					public void run() {
						search_text.clearFocus();
						if (android.os.Build.VERSION.SDK_INT < 11)
							keyboard.toggleSoftInput(
									InputMethodManager.SHOW_FORCED,
									InputMethodManager.HIDE_IMPLICIT_ONLY);
						else
							keyboard.showSoftInput(search_text,
									InputMethodManager.SHOW_IMPLICIT);
						search_text.requestFocus();
					}
				}, 1);
				return true;
			}

			@Override
			public boolean onMenuItemActionCollapse(MenuItem item) {
				if (android.os.Build.VERSION.SDK_INT < 11)
					keyboard.hideSoftInputFromWindow(
							search_text.getWindowToken(), 0);
				else
					keyboard.hideSoftInputFromWindow(
							search_text.getWindowToken(),
							InputMethodManager.HIDE_NOT_ALWAYS);
				return true;
			}
		});

		// Get search from search action view
		View v = (View) menu.findItem(R.id.action_search).getActionView();
		search_text = (EditText) v.findViewById(R.id.search_text);

		final MenuItem favsItem = menu.findItem(R.id.action_favorite);

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
				if (adapter.getPosition("MORE SONGS") == -1)
					adapter.add("MORE SONGS");
				if (adapter.getPosition("BACK TO LYRICS") != -1)
					adapter.remove("BACK TO LYRICS");

				setTitle("LYRICS");
				// Reload favorites icon
				if (FavoritesManager.checkFavorites(getApplicationContext(),
						search_text.getText().toString())) {
					favsItem.setIcon(R.drawable.ic_star_pressed);
				} else {
					favsItem.setIcon(R.drawable.ic_star_not_pressed);
				}
				search_text.setText("");
				searchItem.collapseActionView();
				favsItem.setVisible(true);
				hideFavs = false;
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
		case R.id.action_favorite:
			FavoritesManager.addFavorites(this, LyricsFragment.message);
			if (FavoritesManager.checkFavorites(this, LyricsFragment.message)) {
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
			mDrawerList.setItemChecked(-1, true);
		}
	}

	private void selectItem(int position) {
		// update the main content by replacing fragments
		Fragment fragment = null;
		FragmentManager fragmentManager = getSupportFragmentManager();

		switch (position) {
		case 0:
			fragment = new HomePageFragment();
			fragmentManager.beginTransaction()
					.replace(R.id.content_frame, fragment).commit();
			mDrawerList.setItemChecked(position, true);
			setTitle(mDrawerTitles[position]);
			if (!hideFavs) {
				favItem.setVisible(false);
				hideFavs = true;
			}
			cleanUpDrawer();
			break;
		case 1:
			fragment = new FavoritesFragment();
			fragmentManager = getSupportFragmentManager();
			fragmentManager.beginTransaction()
					.replace(R.id.content_frame, fragment).commit();
			mDrawerList.setItemChecked(position, true);
			setTitle(mDrawerTitles[position]);
			if (!hideFavs) {
				favItem.setVisible(false);
				hideFavs = true;
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
		// case 3:
		// fragment = new ForumsFragment();
		// fragmentManager.beginTransaction()
		// .replace(R.id.content_frame, fragment).commit();
		// mDrawerList.setItemChecked(position, true);
		// setTitle(mDrawerTitles[position]);
		// if (!hideFavs) {
		// favItem.setVisible(false);
		// hideFavs = true;
		// }
		// cleanUpDrawer();
		// break;
		case 3:
			if (adapter.getItem(3).contains("MORE SONGS")) {
				fragment = new MoreSongsFragment();
				Bundle song = new Bundle();
				song.putString(EXTRA_MESSAGE, LyricsFragment.message);
				fragment.setArguments(song);
				fragmentManager = getSupportFragmentManager();
				fragmentManager.beginTransaction()
						.replace(R.id.content_frame, fragment).commit();
				mDrawerList.setItemChecked(position, true);
				setTitle("MORE SONGS");
				if (adapter.getPosition("BACK TO LYRICS") == -1)
					adapter.add("BACK TO LYRICS");
				if (!hideFavs) {
					favItem.setVisible(false);
					hideFavs = true;
				}
				adapter.remove("MORE SONGS");
			} else if (adapter.getItem(3).contains("BACK TO LYRICS")) {
				fragment = new LyricsFragment();
				Bundle song = new Bundle();
				song.putString(EXTRA_MESSAGE, LyricsFragment.message);
				fragment.setArguments(song);
				fragmentManager.beginTransaction()
						.replace(R.id.content_frame, fragment).commit();
				setTitle("LYRICS");
				if (adapter.getPosition("MORE SONGS") == -1)
					adapter.add("MORE SONGS");
				favItem.setVisible(true);
				hideFavs = false;
				adapter.remove("BACK TO LYRICS");
			}
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
					if (adapter.getItem(position) == "MORE SONGS")
						ims = context.getAssets().open("ic_menu_info_details.png");
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
			TextView x = (TextView) textView;
			Typeface tf = Typeface.createFromAsset(getAssets(),
					"fonts/roboto_condensed_light.ttf");
			x.setTypeface(tf);
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
		favItem.setVisible(true);
		// Reload favorites icon
		if (FavoritesManager.checkFavorites(getApplicationContext(),
				favsArray[position])) {
			favItem.setIcon(R.drawable.ic_star_pressed);
		} else {
			favItem.setIcon(R.drawable.ic_star_not_pressed);
		}
		hideFavs = false;
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
		favItem.setVisible(true);
		// Reload favorites icon
		if (FavoritesManager.checkFavorites(getApplicationContext(),
				songName.toString())) {
			favItem.setIcon(R.drawable.ic_star_pressed);
		} else {
			favItem.setIcon(R.drawable.ic_star_not_pressed);
		}
		hideFavs = false;
		adapter.remove("BACK TO LYRICS");
	}

	public void cleanUpDrawer() {
		if (adapter.getPosition("MORE SONGS") != -1)
			adapter.remove("MORE SONGS");
		if (adapter.getPosition("BACK TO LYRICS") != -1)
			adapter.remove("BACK TO LYRICS");
	}
}
