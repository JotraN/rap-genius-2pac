package com.trasselback.rapgenius;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Html;
import android.text.Html.ImageGetter;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.URLSpan;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.MenuItem;

public class ForumThreadActivity extends SherlockActivity {
	private TextView nameField;
	private View mLoadingView;
	private View mContent;
	private URLObject urlObject;
	private ListView forumPosts;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_forum_thread);
		setupActionBar();
		initialize();
		startLyrics();
	}

	private void setupActionBar() {
		getSupportActionBar().setTitle("FORUM THREAD");
		getSupportActionBar().setDisplayShowHomeEnabled(false);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setDisplayShowTitleEnabled(true);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			onBackPressed();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private void initialize() {
		nameField = (TextView) findViewById(R.id.nameText);
		Typeface tf = Typeface.createFromAsset(getAssets(),
				"fonts/roboto_thin.ttf");
		nameField.setTypeface(tf);
		mLoadingView = findViewById(R.id.loadingView);
		mContent = findViewById(R.id.infoView);
		mContent.setVisibility(View.GONE);

		((ProgressBar) mLoadingView).setIndeterminateDrawable(getResources()
				.getDrawable(R.xml.progress_animation));

		// necessary for 2.3 for some reason
		forumPosts = (ListView) findViewById(R.id.postsList);
	}

	// @Override
	// public void onResume() {
	// super.onResume();
	// SharedPreferences sharedPref = PreferenceManager
	// .getDefaultSharedPreferences(this);
	//
	// // Update text size
	// int size = Integer.parseInt(sharedPref.getString(
	// SettingsFragment.KEY_PREF_TEXT_SIZE, "22"));
	// lyricsField.setTextSize(TypedValue.COMPLEX_UNIT_SP, size);
	// nameField.setTextSize(TypedValue.COMPLEX_UNIT_SP, size + 10);
	//
	// // Update colors
	// String color = sharedPref.getString(
	// SettingsFragment.KEY_PREF_DEFAULT_TEXT_COLOR, "Default");
	// if (!color.contains("Default")) {
	// ColorManager.setColor(this, lyricsField, color);
	// } else
	// lyricsField.setTextColor(getResources().getColor(R.color.Gray));
	// color = sharedPref.getString(SettingsFragment.KEY_PREF_TITLE_COLOR,
	// "Default");
	// if (!color.contains("Default")) {
	// ColorManager.setColor(this, nameField, color);
	// } else
	// nameField.setTextColor(getResources().getColor(R.color.LightGray));
	// color = sharedPref.getString(
	// SettingsFragment.KEY_PREF_EXPLAINED_LYRICS_COLOR, "Default");
	// if (!color.contains("Default")) {
	// ColorManager.setLinkColor(this, lyricsField, color);
	// } else
	// lyricsField.setLinkTextColor(getResources()
	// .getColor(R.color.Orange));
	// color = sharedPref.getString(
	// SettingsFragment.KEY_PREF_BACKGROUND_COLOR, "Default");
	// if (!color.contains("Default")) {
	// ColorManager.setBackgroundColor(this, color);
	// } else
	// getWindow().setBackgroundDrawableResource(R.color.LightBlack);
	// }

	// Find what started lyrics activity and continue from there
	private void startLyrics() {
		if (getIntent().getData() != null) {
			if (getIntent().getDataString().contains("forum_thread_clicked")) {
				new RetrieveForumThreadTask().execute();
			}
		}
	}

	private class RetrieveForumThreadTask extends AsyncTask<Void, Void, String> {

		@Override
		protected String doInBackground(Void... params) {
			urlObject = new ForumThread(getIntent().getDataString().replace(
					"forum_thread_clicked:", "http://rapgenius.com"));
			try {
				ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
				NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
				if (networkInfo != null && networkInfo.isConnected()) {
					if (urlObject.openURL()) {
						((ForumThread) urlObject).retrieveName();
						((ForumThread) urlObject).retrievePosts();
					}
					return urlObject.getPage();
				} else
					return "No internet connection found.";
			} catch (Exception ex) {
				return "There was a problem getting information about your network status.";
			}
		}

		protected void onPostExecute(String result) {
			nameField.setText(((ForumThread) urlObject).getName());
			ListAdapter posts = new ListAdapter(getBaseContext(),
					R.layout.posts_list_item);
			for (String x : ((ForumThread) urlObject).getPosts())
				posts.add(x);
			forumPosts.setAdapter(posts);
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1)
				CrossfadeAnimation.crossfade(getApplicationContext(), mContent,
						mLoadingView);
			else {
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
			v.setBackgroundColor(getResources().getColor(R.color.LighterBlack));
			TextView x = (TextView) v;
			SharedPreferences sharedPref = PreferenceManager
					.getDefaultSharedPreferences(getApplicationContext());
			String color = sharedPref.getString(
					SettingsFragment.KEY_PREF_FAVORITES_COLOR, "Default");
			if (!color.contains("Default"))
				ColorManager.setColor(getApplicationContext(), x, color);
			else
				x.setTextColor(getResources().getColor(R.color.Gray));
			int size = Integer.parseInt(sharedPref.getString(
					SettingsFragment.KEY_PREF_TEXT_SIZE, "22"));
			x.setTextSize(TypedValue.COMPLEX_UNIT_SP, size);
			x.setText(Html.fromHtml(getItem(position)));

			// Remove underline from links
			// Need to use spannablestring for list
			Spannable text = (Spannable) new SpannableString(x.getText());
			URLSpan[] spans = text.getSpans(0, text.length(), URLSpan.class);
			for (URLSpan span : spans) {
				int start = text.getSpanStart(span);
				int end = text.getSpanEnd(span);
				text.removeSpan(span);
				// Grabs URL part of span and override text-decoration
				span = new URLOverride(span.getURL());
				text.setSpan(span, start, end, 0);
				// Color links dark green if verified by artist
				if (span.getURL().toString().contains("*"))
					text.setSpan(
							new ForegroundColorSpan(Color
									.argb(255, 38, 135, 31)), start, end, 0);
			}
			x.setText(text);
			// Makes links click-able
			x.setMovementMethod(new LinkSelectableMovementMethod());
			return x;
		}
	}
}