/*
 * Copyright (c) 2019. Bernard Bou <1313ou@gmail.com>.
 */

package org.grammarscope;

import android.view.View;

import org.hamcrest.Matcher;

import androidx.annotation.NonNull;
import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;

public class Actions
{
	@NonNull
	static ViewAction onlyIf(@NonNull final ViewAction action, @NonNull final Matcher<View> constraints)
	{
		return new ViewAction()
		{
			@NonNull
			@Override
			public Matcher<View> getConstraints()
			{
				return constraints;
			}

			@Override
			public String getDescription()
			{
				return action.getDescription();
			}

			@Override
			public void perform(UiController uiController, View view)
			{
				action.perform(uiController, view);
			}
		};
	}
}
