package com.trasselback.rapgenius.helpers;

import android.app.Activity;
import android.content.Context;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.trasselback.rapgenius.R;

public class ColorManager {
	public enum Color {
		DEFAULT(0), RED(1), ORANGE(2), YELLOW(3), GREEN(4), BLUE(5), PURPLE(6), GRAY(
				7), WHITE(8), BLACK(9);

		Color(int colorValue) {
			this.colorValue = colorValue;
		}

		private int colorValue;

		public int getValue() {
			return colorValue;
		}
	}

	public static void setColor(Context context, TextView textView, int color) {
		if (color == Color.DEFAULT.getValue())
			return;
		else if (color == Color.RED.getValue())
			textView.setTextColor(context.getResources().getColor(R.color.Red));
		else if (color == Color.ORANGE.getValue())
			textView.setTextColor(context.getResources().getColor(
					R.color.Orange));
		else if (color == Color.YELLOW.getValue())
			textView.setTextColor(context.getResources().getColor(
					R.color.Yellow));
		else if (color == Color.GREEN.getValue())
			textView.setTextColor(context.getResources()
					.getColor(R.color.Green));
		else if (color == Color.BLUE.getValue())
			textView.setTextColor(context.getResources().getColor(R.color.Blue));
		else if (color == Color.PURPLE.getValue())
			textView.setTextColor(context.getResources().getColor(
					R.color.Purple));
		else if (color == Color.GRAY.getValue())
			textView.setTextColor(context.getResources().getColor(R.color.Gray));
		else if (color == Color.BLACK.getValue())
			textView.setTextColor(context.getResources()
					.getColor(R.color.Black));
		else if (color == Color.WHITE.getValue())
			textView.setTextColor(context.getResources()
					.getColor(R.color.White));
	}

	public static void setLinkColor(Context context, TextView textView,
			int color) {
		if (color == Color.DEFAULT.getValue())
			return;
		else if (color == Color.RED.getValue())
			textView.setLinkTextColor(context.getResources().getColor(
					R.color.Red));
		else if (color == Color.ORANGE.getValue())
			textView.setLinkTextColor(context.getResources().getColor(
					R.color.Orange));
		else if (color == Color.YELLOW.getValue())
			textView.setLinkTextColor(context.getResources().getColor(
					R.color.Yellow));
		else if (color == Color.GREEN.getValue())
			textView.setLinkTextColor(context.getResources().getColor(
					R.color.Green));
		else if (color == Color.BLUE.getValue())
			textView.setLinkTextColor(context.getResources().getColor(
					R.color.Blue));
		else if (color == Color.PURPLE.getValue())
			textView.setLinkTextColor(context.getResources().getColor(
					R.color.Purple));
		else if (color == Color.GRAY.getValue())
			textView.setLinkTextColor(context.getResources().getColor(
					R.color.Gray));
		else if (color == Color.BLACK.getValue())
			textView.setLinkTextColor(context.getResources().getColor(
					R.color.Black));
		else if (color == Color.WHITE.getValue())
			textView.setLinkTextColor(context.getResources().getColor(
					R.color.White));
	}

	public static void setBackgroundColor(Activity activity, int color) {
		if (color == Color.RED.getValue())
			activity.getWindow().setBackgroundDrawableResource(R.color.Red);
		else if (color == Color.ORANGE.getValue())
			activity.getWindow().setBackgroundDrawableResource(R.color.Orange);
		else if (color == Color.YELLOW.getValue())
			activity.getWindow().setBackgroundDrawableResource(R.color.Yellow);
		else if (color == Color.GREEN.getValue())
			activity.getWindow().setBackgroundDrawableResource(R.color.Green);
		else if (color == Color.BLUE.getValue())
			activity.getWindow().setBackgroundDrawableResource(R.color.Blue);
		else if (color == Color.PURPLE.getValue())
			activity.getWindow().setBackgroundDrawableResource(R.color.Purple);
		else if (color == Color.GRAY.getValue())
			activity.getWindow().setBackgroundDrawableResource(R.color.Gray);
		else if (color == Color.BLACK.getValue())
			activity.getWindow().setBackgroundDrawableResource(R.color.Black);
		else if (color == Color.WHITE.getValue())
			activity.getWindow().setBackgroundDrawableResource(R.color.White);
	}

	public static void setActionBarColor(Activity activity, int color) {
		if (color == Color.RED.getValue())
			((SherlockFragmentActivity)activity).getSupportActionBar().setBackgroundDrawable(activity.getResources().getDrawable(R.color.Red));
		else if (color == Color.ORANGE.getValue())
			((SherlockFragmentActivity)activity).getSupportActionBar().setBackgroundDrawable(activity.getResources().getDrawable(R.color.Orange));
		else if (color == Color.YELLOW.getValue())
			((SherlockFragmentActivity)activity).getSupportActionBar().setBackgroundDrawable(activity.getResources().getDrawable(R.color.Yellow));
		else if (color == Color.GREEN.getValue())
			((SherlockFragmentActivity)activity).getSupportActionBar().setBackgroundDrawable(activity.getResources().getDrawable(R.color.Green));
		else if (color == Color.BLUE.getValue())
			((SherlockFragmentActivity)activity).getSupportActionBar().setBackgroundDrawable(activity.getResources().getDrawable(R.color.Blue));
		else if (color == Color.PURPLE.getValue())
			((SherlockFragmentActivity)activity).getSupportActionBar().setBackgroundDrawable(activity.getResources().getDrawable(R.color.Purple));
		else if (color == Color.GRAY.getValue())
			((SherlockFragmentActivity)activity).getSupportActionBar().setBackgroundDrawable(activity.getResources().getDrawable(R.color.Gray));
		else if (color == Color.BLACK.getValue())
			((SherlockFragmentActivity)activity).getSupportActionBar().setBackgroundDrawable(activity.getResources().getDrawable(R.color.Black));
		else if (color == Color.WHITE.getValue())
			((SherlockFragmentActivity)activity).getSupportActionBar().setBackgroundDrawable(activity.getResources().getDrawable(R.color.White));
	}
	
	public static void setActionBarColorExplanation(Activity activity, int color) {
		if (color == Color.RED.getValue())
			((SherlockActivity)activity).getSupportActionBar().setBackgroundDrawable(activity.getResources().getDrawable(R.color.Red));
		else if (color == Color.ORANGE.getValue())
			((SherlockActivity)activity).getSupportActionBar().setBackgroundDrawable(activity.getResources().getDrawable(R.color.Orange));
		else if (color == Color.YELLOW.getValue())
			((SherlockActivity)activity).getSupportActionBar().setBackgroundDrawable(activity.getResources().getDrawable(R.color.Yellow));
		else if (color == Color.GREEN.getValue())
			((SherlockActivity)activity).getSupportActionBar().setBackgroundDrawable(activity.getResources().getDrawable(R.color.Green));
		else if (color == Color.BLUE.getValue())
			((SherlockActivity)activity).getSupportActionBar().setBackgroundDrawable(activity.getResources().getDrawable(R.color.Blue));
		else if (color == Color.PURPLE.getValue())
			((SherlockActivity)activity).getSupportActionBar().setBackgroundDrawable(activity.getResources().getDrawable(R.color.Purple));
		else if (color == Color.GRAY.getValue())
			((SherlockActivity)activity).getSupportActionBar().setBackgroundDrawable(activity.getResources().getDrawable(R.color.Gray));
		else if (color == Color.BLACK.getValue())
			((SherlockActivity)activity).getSupportActionBar().setBackgroundDrawable(activity.getResources().getDrawable(R.color.Black));
		else if (color == Color.WHITE.getValue())
			((SherlockActivity)activity).getSupportActionBar().setBackgroundDrawable(activity.getResources().getDrawable(R.color.White));
	}
	
}
