package com.trasselback.rapgenius;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.view.View;

public class CrossfadeAnimation {

	@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR1)
	public static void crossfade(Context context, View mContent, final View mLoadingView) {
		int mShortAnimationDuration = context.getResources().getInteger(
				android.R.integer.config_shortAnimTime);

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
}
