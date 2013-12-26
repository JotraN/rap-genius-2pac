package com.trasselback.rapgenius.helpers;

import android.text.TextPaint;
import android.text.style.URLSpan;

// Override normal URLSpan to remove underline from the links
public class URLOverride extends URLSpan {
	public URLOverride(String url) {
		super(url);
	}
	@Override
	public void updateDrawState(TextPaint ds) {
		super.updateDrawState(ds);
		// Remove underline from links
		ds.setUnderlineText(false);
	}
}