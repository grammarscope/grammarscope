package org.grammarscope;

import android.view.View;
import android.widget.CheckBox;

import org.hamcrest.Matcher;

import androidx.annotation.DrawableRes;
import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.test.platform.app.InstrumentationRegistry;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.hasSibling;
import static androidx.test.espresso.matcher.ViewMatchers.isCompletelyDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withChild;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.instanceOf;

class Matchers
{
	@NonNull
	static Matcher<View> withMenuIdOrText(@IdRes int id, @StringRes int menuText)
	{
		Matcher<View> matcher = withId(id);
		try
		{
			onView(matcher).check(matches(isDisplayed()));
			return matcher;
		}
		catch (Exception NoMatchingViewException)
		{
			openActionBarOverflowOrOptionsMenu(InstrumentationRegistry.getInstrumentation().getTargetContext());
			return withText(menuText);
		}
	}

	@NonNull
	static Matcher<View> withDrawable(@DrawableRes final int resourceId)
	{
		return new DrawableMatcher(resourceId);
	}

	@NonNull
	static Matcher<View> withNoDrawable()
	{
		return new DrawableMatcher(-1);
	}


	@NonNull
	static Matcher<View> checkboxWithMenuItem(@SuppressWarnings("SameParameterValue") @StringRes int titleId)
	{
		/*
		menu item
			title+shortcut
				title
				shortcut
			arrow
			checkbox

		allOf(
			instanceOf(AppCompatCheckBox.class),
			isCompletelyDisplayed(),
			hasSibling(withChild(withText(titleId)))
		)

		withText->title
		withChild(title)->title+shortcut
		hasSibling(title+shortcut)->arrow,checkbox
		isCompletelyDisplayed()+instanceOf(AppCompatCheckBox.class) eliminates arrow
		*/
		return allOf(instanceOf(CheckBox.class), hasSibling(withChild(withText(titleId))), isCompletelyDisplayed());
	}
}