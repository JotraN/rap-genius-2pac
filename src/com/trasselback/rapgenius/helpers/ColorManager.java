package com.trasselback.rapgenius.helpers;

import com.trasselback.rapgenius.R;

import android.app.Activity;
import android.content.Context;
import android.widget.TextView;

public class ColorManager {
	public static void setColor(Context context, TextView textView, String color) {
		if (color.contains("Red"))
			textView.setTextColor(context.getResources().getColor(R.color.Red));
		else if (color.contains("Orange"))
			textView.setTextColor(context.getResources().getColor(
					R.color.Orange));
		else if (color.contains("Yellow"))
			textView.setTextColor(context.getResources().getColor(
					R.color.Yellow));
		else if (color.contains("Green"))
			textView.setTextColor(context.getResources()
					.getColor(R.color.Green));
		else if (color.contains("Blue"))
			textView.setTextColor(context.getResources().getColor(R.color.Blue));
		else if (color.contains("Purple"))
			textView.setTextColor(context.getResources().getColor(
					R.color.Purple));
		else if (color.contains("Gray"))
			textView.setTextColor(context.getResources().getColor(R.color.Gray));
		else if (color.contains("White"))
			textView.setTextColor(context.getResources()
					.getColor(R.color.White));
		else if (color.contains("Black"))
			textView.setTextColor(context.getResources()
					.getColor(R.color.Black));
	}

	public static void setLinkColor(Context context, TextView textView,
			String color) {
		if (color.contains("Red"))
			textView.setLinkTextColor(context.getResources().getColor(
					R.color.Red));
		else if (color.contains("Orange"))
			textView.setLinkTextColor(context.getResources().getColor(
					R.color.Orange));
		else if (color.contains("Yellow"))
			textView.setLinkTextColor(context.getResources().getColor(
					R.color.Yellow));
		else if (color.contains("Green"))
			textView.setLinkTextColor(context.getResources().getColor(
					R.color.Green));
		else if (color.contains("Blue"))
			textView.setLinkTextColor(context.getResources().getColor(
					R.color.Blue));
		else if (color.contains("Purple"))
			textView.setLinkTextColor(context.getResources().getColor(
					R.color.Purple));
		else if (color.contains("Gray"))
			textView.setLinkTextColor(context.getResources().getColor(
					R.color.Gray));
		else if (color.contains("White"))
			textView.setLinkTextColor(context.getResources().getColor(
					R.color.White));
		else if (color.contains("Black"))
			textView.setLinkTextColor(context.getResources().getColor(
					R.color.Black));
	}

	public static void setBackgroundColor(Activity activity, String color) {
		if (color.contains("Red"))
			activity.getWindow().setBackgroundDrawableResource(R.color.Red);
		else if (color.contains("Orange"))
			activity.getWindow().setBackgroundDrawableResource(R.color.Orange);
		else if (color.contains("Yellow"))
			activity.getWindow().setBackgroundDrawableResource(R.color.Yellow);
		else if (color.contains("Green"))
			activity.getWindow().setBackgroundDrawableResource(R.color.Green);
		else if (color.contains("Blue"))
			activity.getWindow().setBackgroundDrawableResource(R.color.Blue);
		else if (color.contains("Purple"))
			activity.getWindow().setBackgroundDrawableResource(R.color.Purple);
		else if (color.contains("Gray"))
			activity.getWindow().setBackgroundDrawableResource(R.color.Gray);
		else if (color.contains("White"))
			activity.getWindow().setBackgroundDrawableResource(R.color.White);
		else if (color.contains("Black"))
			activity.getWindow().setBackgroundDrawableResource(R.color.Black);
	}

}
