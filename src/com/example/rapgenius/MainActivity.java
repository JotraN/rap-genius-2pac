package com.example.rapgenius;

import java.io.File;

import com.example.rapgenius.RemoveFavoritesDialogFragment.RemoveFavoritesDialogListener;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.DialogFragment;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.text.Spannable;
import android.text.method.LinkMovementMethod;
import android.text.style.ForegroundColorSpan;
import android.text.style.URLSpan;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

@TargetApi(Build.VERSION_CODES.GINGERBREAD)
@SuppressLint("NewApi")
public class MainActivity extends Activity implements
		RemoveFavoritesDialogListener {
	public final static String EXTRA_MESSAGE = "com.example.rapgenius.MESSAGE";

	private TextView nameField, lyricsField;
	private View mLoadingView;
	private View mContent;
	private int mShortAnimationDuration;
	private URLObject urlObject;

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		initialize();

		new RetrieveNewsFeed().execute();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle presses on the action bar items
		switch (item.getItemId()) {
		case R.id.action_search:
			openSearch();
			return true;
		case R.id.action_delete:
			DialogFragment dialog = new RemoveFavoritesDialogFragment();
			dialog.show(getFragmentManager(), "RemoveFavoritesDialogFragment");
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private void openSearch() {
		Intent intent = new Intent(MainActivity.this, SearchActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(intent);
	}

	private void initialize() {
		nameField = (TextView) findViewById(R.id.nameText);
		lyricsField = (TextView) findViewById(R.id.lyricsText);
		mLoadingView = findViewById(R.id.loadingView);
		mContent = findViewById(R.id.scrollView1);
		mContent.setVisibility(View.GONE);
		nameField.setVisibility(View.VISIBLE);

		mShortAnimationDuration = getResources().getInteger(
				android.R.integer.config_shortAnimTime);

		nameField.setText("Home");

		// makes links operable
		lyricsField.setMovementMethod(LinkMovementMethod.getInstance());
	}

	private class RetrieveNewsFeed extends AsyncTask<Void, Void, String> {

		@Override
		protected String doInBackground(Void... names) {
			urlObject = new NewsFeed();
			if (urlObject.openURL())
				urlObject.retrievePage();
			return urlObject.getPage();
		}

		protected void onPostExecute(String result) {
			lyricsField.setText(Html.fromHtml(result));
			removeUnderline(lyricsField);
			crossfade();
		}
	}

	private void removeUnderline(TextView textView) {
		Spannable text = (Spannable) textView.getText();
		URLSpan[] spans = text.getSpans(0, text.length(), URLSpan.class);
		for (URLSpan span : spans) {
			int start = text.getSpanStart(span);
			int end = text.getSpanEnd(span);
			text.removeSpan(span);
			// Grabs URL part of span
			span = new URLOverride(span.getURL());
			text.setSpan(span, start, end, 0);
			// Color links dark green if verified by artist
			if (span.getURL().toString().contains("*"))
				text.setSpan(
						new ForegroundColorSpan(Color.argb(255, 38, 135, 31)),
						start, end, 0);
			// Else color dark orange
			else
				text.setSpan(
						new ForegroundColorSpan(Color.argb(255, 186, 95, 34)),
						start, end, 0);
		}
		textView.setText(text);
	}

	@SuppressLint("NewApi")
	@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR1)
	private void crossfade() {
		mContent.setAlpha(0f);
		mContent.setVisibility(View.VISIBLE);

		mContent.animate().alpha(1f).setDuration(mShortAnimationDuration)
				.setListener(null);

		mLoadingView.animate().alpha(0f).setDuration(mShortAnimationDuration)
				.setListener(new AnimatorListenerAdapter() {
					@Override
					public void onAnimationEnd(Animator animation) {
						mLoadingView.setVisibility(View.GONE);
					}
				});
	}

	@Override
	public void onDialogPositiveClick(DialogFragment dialog) {
		File file = new File(this.getFilesDir(), "favorites");
		file.delete();
	}

	@Override
	public void onDialogNegativeClick(DialogFragment dialog) {
		dialog.dismiss();
	}
}
