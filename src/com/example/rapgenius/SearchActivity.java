package com.example.rapgenius;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import com.example.rapgenius.RemoveFavoritesDialogFragment.RemoveFavoritesDialogListener;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.DialogFragment;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.text.Spannable;
import android.text.method.LinkMovementMethod;
import android.text.style.ForegroundColorSpan;
import android.text.style.URLSpan;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class SearchActivity extends Activity implements
		RemoveFavoritesDialogListener {
	public final static String EXTRA_MESSAGE = "com.example.rapgenius.MESSAGE";
	private EditText songField;
	private TextView favorites;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_search);
		// Show the Up button in the action bar.
		setupActionBar();
		songField = (EditText) findViewById(R.id.songName);
		songField.setOnEditorActionListener(new OnEditorActionListener() {

			@Override
			public boolean onEditorAction(TextView v, int actionId,
					KeyEvent event) {
				Intent intent = new Intent(SearchActivity.this,
						LyricsActivity.class);
				intent.putExtra(EXTRA_MESSAGE, songField.getText().toString());
				startActivity(intent);
				return false;
			}
		});

		favorites = (TextView) findViewById(R.id.lyricsText);
		favorites.setMovementMethod(LinkMovementMethod.getInstance());

		favorites.setText(Html.fromHtml(getFavorites()));
		removeUnderline(favorites);
		
	}

	/**
	 * Set up the {@link android.app.ActionBar}, if the API is available.
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private void setupActionBar() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			// Show the Up button in the action bar.
			getActionBar().setDisplayHomeAsUpEnabled(true);
			getActionBar().setDisplayShowTitleEnabled(false);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.search, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			onBackPressed();
			return true;
		case R.id.action_delete:
			DialogFragment dialog = new RemoveFavoritesDialogFragment();
			dialog.show(getFragmentManager(), "RemoveFavoritesDialogFragment");
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	private void removeUnderline(TextView textView) {
		Spannable text = (Spannable) textView.getText();
		URLSpan[] spans = text.getSpans(0, text.length(), URLSpan.class);
		for (URLSpan span : spans) {
			int start = text.getSpanStart(span);
			int end = text.getSpanEnd(span);
			text.removeSpan(span);
			// Grabs URL part of span and override text-decoration
			span = new URLOverride(span.getURL());
			text.setSpan(span, start, end, 0);
			// Color links red
			text.setSpan(new ForegroundColorSpan(Color.argb(255, 139, 0, 0)),
					start, end, 0);
		}
		textView.setText(text);
	}

	private String getFavorites() {
		String favs = "";

		try {
			InputStream inputStream = openFileInput("favorites");

			if (inputStream != null) {
				InputStreamReader inputStreamReader = new InputStreamReader(
						inputStream);
				BufferedReader bufferedReader = new BufferedReader(
						inputStreamReader);
				String line = "";
				StringBuilder stringBuilder = new StringBuilder();

				while ((line = bufferedReader.readLine()) != null) {
					stringBuilder.append(line);
				}

				inputStream.close();
				favs = stringBuilder.toString();
			}
		} catch (FileNotFoundException e) {
		} catch (IOException e) {
		}

		return favs;
	}

	@Override
	public void onDialogPositiveClick(DialogFragment dialog) {
		File file = new File(this.getFilesDir(), "favorites");
		file.delete();
		// Reload activity to clear list
		finish();
		startActivity(getIntent());
	}

	@Override
	public void onDialogNegativeClick(DialogFragment dialog) {
		dialog.dismiss();
	}
}
