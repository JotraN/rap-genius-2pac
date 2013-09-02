package com.trasselback.rapgenius;

import android.graphics.Color;
import android.text.Spannable;
import android.text.style.ForegroundColorSpan;
import android.text.style.URLSpan;
import android.widget.TextView;

public class RemoveUnderLine {
	public static void removeUnderline(TextView textView) {
		Spannable text = (Spannable) textView.getText();
		URLSpan[] spans = text.getSpans(0, text.length(), URLSpan.class);
		for (URLSpan span : spans) {
			int start = text.getSpanStart(span);
			int end = text.getSpanEnd(span);
			text.removeSpan(span);
			// Grabs URL part of span and override text-decoration
			span = new URLOverride(span.getURL());
			text.setSpan(span, start, end, 0);
			// Color links dark green if verified by artist
			if (span.getURL().toString().contains("*"))
				text.setSpan(
						new ForegroundColorSpan(Color.argb(255, 38, 135, 31)),
						start, end, 0);
		}
		textView.setText(text);
		// Makes links click-able
		textView.setMovementMethod(new LinkSelectableMovementMethod());
	}
}