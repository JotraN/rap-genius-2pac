package com.trasselback.rapgenius;

import android.content.Context;
import android.preference.DialogPreference;
import android.util.AttributeSet;

public class OverridedDialogPreference extends DialogPreference {

	public OverridedDialogPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
		
	}

	@Override
	protected void onDialogClosed(boolean positiveResult) {
		super.onDialogClosed(positiveResult);
		persistBoolean(positiveResult);
	}

}
