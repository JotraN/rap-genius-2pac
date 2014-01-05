package com.trasselback.rapgenius.helpers;

import java.util.regex.Pattern;

import android.text.Selection;
import android.text.Spannable;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.TextView;

// Makes links select-able
public class LinkSelectableMovementMethod extends LinkMovementMethod {
	@Override
	public boolean canSelectArbitrarily() {
		return true;
	}

	@Override
	public void initialize(TextView widget, Spannable text) {
		// Prevent crashes if Hebrew user
		if(Pattern.matches("(\\p{InHebrew})+", text.toString())){
			return;
		}
		Selection.setSelection(text, text.length());
	}

	@Override
	public void onTakeFocus(TextView view, Spannable text, int dir) {
		// Prevent crashes if Hebrew user
		if(Pattern.matches("(\\p{InHebrew})+", view.toString())){
			return;
		}
		if ((dir & (View.FOCUS_FORWARD | View.FOCUS_DOWN)) != 0) {
			if (view.getLayout() == null) {
				// Stop application from crashing for Arabic users
				try {
					Selection.setSelection(text, text.length());
				} catch (StringIndexOutOfBoundsException e) {
				}
			}
		} else {
			// Stop application from crashing for Arabic users
			try {
				Selection.setSelection(text, text.length());
			} catch (StringIndexOutOfBoundsException e) {
			}
		}
	}
}
