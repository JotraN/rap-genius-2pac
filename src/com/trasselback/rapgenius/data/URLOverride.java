package com.trasselback.rapgenius.data;

import android.text.TextPaint;
import android.text.style.URLSpan;

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