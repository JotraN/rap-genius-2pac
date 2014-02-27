package com.trasselback.rapgenius.activities;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Canvas;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Html;
import android.text.Html.ImageGetter;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.util.TypedValue;
import android.view.View;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.MenuItem;
import com.trasselback.rapgenius.R;
import com.trasselback.rapgenius.data.Explanations;
import com.trasselback.rapgenius.helpers.ColorManager;
import com.trasselback.rapgenius.helpers.CrossfadeAnimation;
import com.trasselback.rapgenius.helpers.RemoveUnderLine;
import com.trasselback.rapgenius.preferences.SettingsFragment;

public class ExplanationActivity extends SherlockActivity {
	private TextView nameField, explanationsField;
	private View loadingView;
	private View contentView;
	private Explanations explanation;
	private URLImageParser imageParser;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		overridePendingTransition(R.anim.right_in, R.anim.hold);
		setContentView(R.layout.activity_explanation);
		setupActionBar();
		initialize();
		grabExplanation();
	}

	private void setupActionBar() {
		getSupportActionBar().setTitle("EXPLANATION");
		getSupportActionBar().setDisplayShowHomeEnabled(false);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setDisplayShowTitleEnabled(true);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			onBackPressed();
			overridePendingTransition(R.anim.hold, R.anim.left_in);
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
		explanationsField = (TextView) findViewById(R.id.lyricsText);
		loadingView = findViewById(R.id.loadingView);
		contentView = findViewById(R.id.infoView);
		contentView.setVisibility(View.GONE);

		// Needed for Android 2.3
		explanationsField.setMovementMethod(new LinkMovementMethod());

		imageParser = new URLImageParser(explanationsField, this);

	}

	@Override
	public void onResume() {
		super.onResume();
		checkSettings();
	}

	private void checkSettings() {
		try {
			SharedPreferences sharedPref = PreferenceManager
					.getDefaultSharedPreferences(this);
			// Update text size
			int size = Integer.parseInt(sharedPref.getString(
					SettingsFragment.KEY_PREF_TEXT_SIZE, "20"));
			explanationsField.setTextSize(TypedValue.COMPLEX_UNIT_SP, size);
			nameField.setTextSize(TypedValue.COMPLEX_UNIT_SP, size + 10);

			// Update colors
			int textColor = Integer.parseInt(sharedPref.getString(
					SettingsFragment.KEY_PREF_DEFAULT_TEXT_COLOR, "0"));
			ColorManager.setColor(this, explanationsField, textColor);
			int titleColor = Integer.parseInt(sharedPref.getString(
					SettingsFragment.KEY_PREF_TITLE_COLOR, "0"));
			ColorManager.setColor(this, nameField, titleColor);
			int linkColor = Integer.parseInt(sharedPref.getString(
					SettingsFragment.KEY_PREF_EXPLAINED_LYRICS_COLOR, "0"));
			ColorManager.setLinkColor(this, explanationsField, linkColor);
			int backgroundColor = Integer.parseInt(sharedPref.getString(
					SettingsFragment.KEY_PREF_BACKGROUND_COLOR, "0"));
			ColorManager.setBackgroundColor(this, backgroundColor);
			int actionBarColor = Integer.parseInt(sharedPref.getString(
					SettingsFragment.KEY_PREF_ACTION_BAR_COLOR, "0"));
			ColorManager.setActionBarColorExplanation(this, actionBarColor);
		} catch (NumberFormatException ex) {
			clearSettings();
		}
	}

	// Needed to reset settings for those who updated and are still using old
	// color settings
	private void clearSettings() {
		Editor editor = getSharedPreferences(
				SettingsFragment.KEY_PREF_TEXT_SIZE, Context.MODE_PRIVATE)
				.edit();
		editor.clear();
		editor.commit();
		editor = getSharedPreferences(
				SettingsFragment.KEY_PREF_BACKGROUND_COLOR,
				Context.MODE_PRIVATE).edit();
		editor.clear();
		editor.commit();
		editor = getSharedPreferences(
				SettingsFragment.KEY_PREF_DEFAULT_TEXT_COLOR,
				Context.MODE_PRIVATE).edit();
		editor.clear();
		editor.commit();
		editor = getSharedPreferences(
				SettingsFragment.KEY_PREF_EXPLAINED_LYRICS_COLOR,
				Context.MODE_PRIVATE).edit();
		editor.clear();
		editor.commit();
		editor = getSharedPreferences(
				SettingsFragment.KEY_PREF_FAVORITES_COLOR, Context.MODE_PRIVATE)
				.edit();
		editor.clear();
		editor.commit();
		editor = getSharedPreferences(
				SettingsFragment.KEY_PREF_HOME_PAGE_COLOR, Context.MODE_PRIVATE)
				.edit();
		editor.clear();
		editor.commit();
		editor = getSharedPreferences(SettingsFragment.KEY_PREF_TITLE_COLOR,
				Context.MODE_PRIVATE).edit();
		editor.clear();
		editor.commit();
		editor = getSharedPreferences(
				SettingsFragment.KEY_PREF_ACTION_BAR_COLOR,
				Context.MODE_PRIVATE).edit();
		editor.clear();
		editor.commit();
	}

	private void grabExplanation() {
		if (getIntent().getData() != null)
			// Make sure explanations activity was started by explanation link
			if (getIntent().getDataString().contains("explanation_clicked"))
				new RetrieveExplanationsTask().execute();
	}

	private class RetrieveExplanationsTask extends
			AsyncTask<Void, Void, String> {

		@Override
		protected String doInBackground(Void... params) {
			explanation = new Explanations(getIntent().getDataString());
			if (explanation.isOnline(getApplicationContext())) {
				if (!explanation.retrievedUrl())
					return getString(R.string.error_explanation);
				explanation.retrieveName();
				if (explanation.openedURL())
					explanation.retrievePage();
				return explanation.getPage();
			} else
				return getString(R.string.error_no_internet);
		}

		protected void onPostExecute(String result) {
			nameField.setText(explanation.getName());
			Spanned htmlSpan = Html.fromHtml(result, imageParser, null);
			explanationsField.setText(htmlSpan);
			RemoveUnderLine.removeUnderline(explanationsField);
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1)
				CrossfadeAnimation.crossfade(getApplicationContext(),
						contentView, loadingView);
			else {
				explanationsField.setMovementMethod(new LinkMovementMethod());
				contentView.setVisibility(View.VISIBLE);
				loadingView.setVisibility(View.GONE);
			}
		}
	}

	@SuppressWarnings("deprecation")
	public class URLDrawable extends BitmapDrawable {
		protected Drawable drawable;

		@Override
		public void draw(Canvas canvas) {
			// override the draw to refresh function later
			if (drawable != null) {
				drawable.draw(canvas);
			}
		}
	}

	public class URLImageParser implements ImageGetter {
		Context context;
		TextView container;

		public URLImageParser(TextView t, Context c) {
			context = c;
			container = t;
		}

		public Drawable getDrawable(String source) {
			URLDrawable urlDrawable = new URLDrawable();

			ImageGetterAsyncTask asyncTask = new ImageGetterAsyncTask(
					urlDrawable);

			asyncTask.execute(source);
			return urlDrawable;
		}

		public class ImageGetterAsyncTask extends
				AsyncTask<String, Void, Drawable> {
			URLDrawable urlDrawable;

			public ImageGetterAsyncTask(URLDrawable d) {
				urlDrawable = d;
			}

			@Override
			protected Drawable doInBackground(String... params) {
				String source = params[0];
				return fetchDrawable(source);
			}

			@Override
			protected void onPostExecute(Drawable result) {
				// Scales image/container if space is available in container
				int width = (int) (result.getIntrinsicWidth());
				int height = (int) (result.getIntrinsicHeight());
				float scale = getResources().getDisplayMetrics().density;
				if (width * scale < container.getWidth()) {
					width = (int) (result.getIntrinsicWidth() * scale);
					height = (int) (result.getIntrinsicHeight() * scale);
				}

				urlDrawable.setBounds(0, 0, 0 + width, 0 + height);

				// Change to downloaded image
				urlDrawable.drawable = result;

				// Invalidate TextView to redraw image
				URLImageParser.this.container.invalidate();

				// Resize TextView height to accommodate for image 
				// 4.0+ devices
				URLImageParser.this.container
						.setHeight((URLImageParser.this.container.getHeight() + height));
				// Needed for devices before 4.0
				URLImageParser.this.container.setEllipsize(null);
			}

			public Drawable fetchDrawable(String urlString) {
				try {
					InputStream is = fetch(urlString);
					Drawable drawable = Drawable.createFromStream(is, "src");
					
					// Scales image if space is available in container
					int width = (int) (drawable.getIntrinsicWidth());
					int height = (int) (drawable.getIntrinsicHeight());
					float scale = getResources().getDisplayMetrics().density;
					if (width * scale < container.getWidth()) {
						width = (int) (drawable.getIntrinsicWidth() * scale);
						height = (int) (drawable.getIntrinsicHeight() * scale);
					}

					drawable.setBounds(0, 0, 0 + width, 0 + height);
					return drawable;
				} catch (Exception e) {
					return null;
				}
			}

			private InputStream fetch(String urlString)
					throws MalformedURLException, IOException {
				DefaultHttpClient httpClient = new DefaultHttpClient();
				HttpGet request = new HttpGet(urlString);
				HttpResponse response = httpClient.execute(request);
				return response.getEntity().getContent();
			}
		}
	}
}
