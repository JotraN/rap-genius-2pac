package com.trasselback.rapgenius.helpers;

import java.io.IOException;
import java.io.InputStream;

import android.content.Context;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.trasselback.rapgenius.R;

public class DrawerAdapterList extends ArrayAdapter<String> {
	private final Context context;
	private final int resource;

	public DrawerAdapterList(Context context, int resource) {
		super(context, resource);
		this.context = context;
		this.resource = resource;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View v = inflater.inflate(resource, parent, false);

		TextView textView = (TextView) v.findViewById(R.id.textView);
		// Have to manually set text because adapter isn't just textview
		textView.setText(this.getItem(position));
		changeBackground(position, v);
		// Set font
		Typeface tf = Typeface.createFromAsset(context.getAssets(),
				"fonts/roboto_condensed_light.ttf");
		textView.setTypeface(tf);
		return v;
	}

	private void changeBackground(int position, View v) {
		ImageView imageView = (ImageView) v.findViewById(R.id.imageView);
		InputStream ims = null;
		// Change background and icon depending on position
		switch (position) {
		case 0:
			try {
				ims = context.getAssets().open("ic_menu_home.png");
			} catch (IOException e) {
				e.printStackTrace();
			}
			v.setBackgroundColor(context.getResources().getColor(R.color.LightBlue));
			break;
		case 1:
			try {
				ims = context.getAssets().open("ic_menu_star.png");
			} catch (IOException e) {
				e.printStackTrace();
			}
			v.setBackgroundColor(context.getResources().getColor(R.color.Red));
			break;
		case 2:
			try {
				ims = context.getAssets().open("ic_menu_manage.png");
			} catch (IOException e) {
				e.printStackTrace();
			}
			v.setBackgroundColor(context.getResources().getColor(R.color.Green));
			break;
		case 3:
			try {
				if (this.getItem(position) == context.getResources().getString(
						R.string.drawer_title_back_to_lyrics))
					ims = context.getAssets().open("ic_menu_revert.png");
				else
					ims = context.getAssets().open("ic_menu_info_details.png");
			} catch (IOException e) {
				e.printStackTrace();
			}
			v.setBackgroundColor(context.getResources().getColor(R.color.Yellow));
			break;
		default:
			v.setBackgroundColor(context.getResources().getColor(R.color.Orange));
			break;
		}
		Drawable d = Drawable.createFromStream(ims, null);
		imageView.setImageDrawable(d);
	}

}
