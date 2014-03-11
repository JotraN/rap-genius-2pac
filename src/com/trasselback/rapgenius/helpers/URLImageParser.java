package com.trasselback.rapgenius.helpers;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.text.Html.ImageGetter;
import android.widget.TextView;

public class URLImageParser implements ImageGetter {
	private Context context;
	private TextView container;

	@SuppressWarnings("deprecation")
	// Needed to replace drawable with downloaded one
	private class URLDrawable extends BitmapDrawable {
		protected Drawable drawable;

		@Override
		public void draw(Canvas canvas) {
			// override the draw to refresh function later
			if (drawable != null) {
				drawable.draw(canvas);
			}
		}
	}

	public URLImageParser(TextView t, Context c) {
		context = c;
		container = t;
	}

	public Drawable getDrawable(String source) {
		URLDrawable urlDrawable = new URLDrawable();

		ImageGetterAsyncTask asyncTask = new ImageGetterAsyncTask(urlDrawable);

		asyncTask.execute(source);
		return urlDrawable;
	}

	public class ImageGetterAsyncTask extends AsyncTask<String, Void, Drawable> {
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
			float scale = context.getResources().getDisplayMetrics().density;
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
				float scale = context.getResources().getDisplayMetrics().density;
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