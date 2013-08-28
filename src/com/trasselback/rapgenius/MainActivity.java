package com.trasselback.rapgenius;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
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
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.MenuItem.OnActionExpandListener;

public class MainActivity extends SherlockActivity {
	public final static String EXTRA_MESSAGE = "com.trasselback.rapgenius.MESSAGE";

	private TextView nameField, lyricsField;
	private EditText search_text;
	private View mLoadingView;
	private View mContent;
	private URLObject urlObject;

	private String[] mDrawerTitles;
	private DrawerLayout mDrawerLayout;
	private ListView mDrawerList;
	private ActionBarDrawerToggle mDrawerToggle;

	private Handler mHandler;

	private boolean contentLoaded = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
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
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		// Sync the toggle state after onRestoreInstanceState has occurred.
		mDrawerToggle.syncState();
	}

	@Override
	public void onResume() {
		super.onResume();
		SharedPreferences sharedPref = PreferenceManager
				.getDefaultSharedPreferences(this);
		// If not already loaded
		if (!contentLoaded) {
			// Get load home setting
			boolean loadHome = sharedPref.getBoolean(
					SettingsFragment.KEY_PREF_LOAD_HOME, false);

			if (loadHome) {
				mLoadingView.setVisibility(View.VISIBLE);
				mContent.setVisibility(View.GONE);
				new RetrieveNewsFeed().execute();
			} else {
				lyricsField.setText("Home disabled in settings.");
				mContent.setVisibility(View.VISIBLE);
				mLoadingView.setVisibility(View.GONE);
			}
		}
		// Update text size
		int size = Integer.parseInt(sharedPref.getString(
				SettingsFragment.KEY_PREF_TEXT_SIZE, "22"));
		lyricsField.setTextSize(TypedValue.COMPLEX_UNIT_SP, size);
		nameField.setTextSize(TypedValue.COMPLEX_UNIT_SP, size + 10);

		// Update colors
		String color = sharedPref.getString(
				SettingsFragment.KEY_PREF_DEFAULT_TEXT_COLOR, "Default");
		if (!color.contains("Default")) {
			ColorManager.setColor(getApplicationContext(), lyricsField, color);
		} else
			lyricsField.setTextColor(getResources().getColor(R.color.Gray));
		color = sharedPref.getString(SettingsFragment.KEY_PREF_TITLE_COLOR,
				"Default");
		if (!color.contains("Default")) {
			ColorManager.setColor(getApplicationContext(), nameField, color);
		} else
			nameField.setTextColor(getResources().getColor(R.color.LightGray));
		color = sharedPref.getString(SettingsFragment.KEY_PREF_HOME_PAGE_COLOR,
				"Default");
		if (!color.contains("Default")) {
			ColorManager.setLinkColor(getApplicationContext(), lyricsField,
					color);
		} else
			lyricsField.setLinkTextColor(getResources().getColor(
					R.color.LightBlue));
		color = sharedPref.getString(
				SettingsFragment.KEY_PREF_BACKGROUND_COLOR, "Default");
		if (!color.contains("Default")) {
			ColorManager.setBackgroundColor(this, color);
		} else
			getWindow().setBackgroundDrawableResource(R.color.LightBlack);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getSupportMenuInflater().inflate(R.menu.main, menu);

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
						keyboard.hideSoftInputFromWindow(search_text.getWindowToken(), 0);
					 } else {
					    keyboard.hideSoftInputFromWindow(search_text.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
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
				Intent intent = new Intent(MainActivity.this,
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
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private void initialize() {
		nameField = (TextView) findViewById(R.id.nameText);
		lyricsField = (TextView) findViewById(R.id.lyricsText);
		mLoadingView = findViewById(R.id.loadingView);
		mContent = findViewById(R.id.scrollView1);
		mContent.setVisibility(View.GONE);
		nameField.setVisibility(View.VISIBLE);

		nameField.setText("Home");
		((ProgressBar) mLoadingView).setIndeterminateDrawable(getResources()
				.getDrawable(R.xml.progress_animation));

		// makes links operable
		lyricsField.setMovementMethod(LinkMovementMethod.getInstance());
	}

	private class RetrieveNewsFeed extends AsyncTask<Void, Void, String> {

		@Override
		protected String doInBackground(Void... names) {
			urlObject = new NewsFeed();
			ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
			if (networkInfo != null && networkInfo.isConnected()) {
				if (urlObject.openURL())
					urlObject.retrievePage();
				contentLoaded = true;
				return urlObject.getPage();
			} else
				return "No internet connection found.";
		}

		protected void onPostExecute(String result) {
			lyricsField.setText(Html.fromHtml(result));
			RemoveUnderLine.removeUnderline(lyricsField);
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1)
				CrossfadeAnimation.crossfade(getApplicationContext(), mContent,
						mLoadingView);
			else {
				mContent.setVisibility(View.VISIBLE);
				mLoadingView.setVisibility(View.GONE);
			}
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
