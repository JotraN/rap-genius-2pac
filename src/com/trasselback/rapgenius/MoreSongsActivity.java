package com.trasselback.rapgenius;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

public class MoreSongsActivity extends SherlockActivity {
	private TextView nameField, songsView;
	private View mLoadingView;
	private View mContent;
	private String message;
	private MoreSongs moreSongs;
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_more_songs);
		setupActionBar();
		
		nameField = (TextView) findViewById(R.id.nameText);
		songsView = (TextView) findViewById(R.id.lyricsText);
		mLoadingView = findViewById(R.id.loadingView);
		mContent = findViewById(R.id.infoView);
		mContent.setVisibility(View.GONE);

		((ProgressBar) mLoadingView).setIndeterminateDrawable(getResources()
				.getDrawable(R.xml.progress_animation));

		// necessary for 2.3 for some reason
		songsView.setMovementMethod(LinkMovementMethod.getInstance());
		
		message = getIntent().getStringExtra(MainActivity.EXTRA_MESSAGE);
		new RetrieveMoreSongs().execute(message);

	}

	private void setupActionBar() {
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setDisplayShowTitleEnabled(false);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		//getMenuInflater().inflate(R.menu.more_songs, menu);
		return true;
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
	
	private class RetrieveMoreSongs extends AsyncTask<String, Void, String> {

		@Override
		protected String doInBackground(String... names) {
			moreSongs = new MoreSongs(names[0]);
			ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
			if (networkInfo != null && networkInfo.isConnected()) {
				if (moreSongs.openURL()) {
					moreSongs.retrievePage();
					moreSongs.retrieveName();
				}
				return moreSongs.getPage();

			} else
				return "No internet connection found.";
		}

		protected void onPostExecute(String result) {
			nameField.setText(moreSongs.getName());
			songsView.setText(Html.fromHtml(result));
			RemoveUnderLine.removeUnderline(songsView);
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1)
				CrossfadeAnimation.crossfade(getApplicationContext(), mContent,
						mLoadingView);
			else {
				// lyricsField.setMovementMethod(new LinkMovementMethod());
				mContent.setVisibility(View.VISIBLE);
				mLoadingView.setVisibility(View.GONE);
			}
		}
	}
}
