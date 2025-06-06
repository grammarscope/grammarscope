package org.grammarscope;

import android.view.View;
import android.widget.Spinner;

import org.hamcrest.Matcher;

import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.test.platform.app.InstrumentationRegistry;

import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.longClick;
import static androidx.test.espresso.action.ViewActions.pressBack;
import static androidx.test.espresso.action.ViewActions.swipeUp;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isChecked;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayingAtLeast;
import static androidx.test.espresso.matcher.ViewMatchers.isNotChecked;
import static androidx.test.espresso.matcher.ViewMatchers.isRoot;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withSpinnerText;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;

class Seq
{
	static void do_pressBack()
	{
		onView(isRoot()).perform(pressBack());
	}

	static void do_type(@SuppressWarnings("SameParameterValue") @IdRes final int editTextViewId, @NonNull final String text)
	{
		onView(withId(editTextViewId)) //
				.check(matches(isDisplayed())) //
				.perform(typeText(text))  //
		;
	}

	static void do_click(@IdRes final int buttonId)
	{
		onView(withId(buttonId)) //
				.check(matches(isDisplayed())) //
				.perform(click())  //
		;
	}

	static void do_longclick(@IdRes final int buttonId)
	{
		onView(withId(buttonId)) //
				.check(matches(isDisplayed())) //
				.perform(longClick())  //
		;
	}

	static void do_menu(@SuppressWarnings("SameParameterValue") @IdRes int menuId, @StringRes int menuText)
	{
		onView(Matchers.withMenuIdOrText(menuId, menuText)).perform(click());
	}

	static void do_options_menu(@StringRes int menuText)
	{
		openActionBarOverflowOrOptionsMenu(InstrumentationRegistry.getInstrumentation().getTargetContext());
		onView(withText(menuText)).perform(click());
	}

	static void do_menu_check(@SuppressWarnings("SameParameterValue") final @StringRes int resId, boolean mustBeChecked)
	{
		openActionBarOverflowOrOptionsMenu(InstrumentationRegistry.getInstrumentation().getTargetContext());
		Matcher<View> v = Matchers.checkboxWithMenuItem(R.string.action_as_graph);
		boolean wasChecked = ToBoolean.test(v, isChecked());
		if ((mustBeChecked && !wasChecked) || (!mustBeChecked && wasChecked))
		{
			onView(v).perform(click());
			openActionBarOverflowOrOptionsMenu(InstrumentationRegistry.getInstrumentation().getTargetContext());
		}

		Matcher<View> v2 = Matchers.checkboxWithMenuItem(R.string.action_as_graph);
		onView(v2).check(matches(mustBeChecked ? isChecked() : isNotChecked()));
		do_pressBack();
	}

	static void do_choose(@IdRes int spinnerId, final String targetText)
	{
		// expand spinner
		onView(allOf(withId(spinnerId), instanceOf(Spinner.class))) //
				.perform(click());

		// do_click view matching text
		onData(allOf(is(instanceOf(String.class)), is(targetText))) //
				.perform(click());

		// check
		onView(withId(spinnerId)) //
				.check(matches(withSpinnerText(containsString(targetText))));
	}

	static void do_swipeUp(@IdRes final int viewId)
	{
		onView(withId(viewId)) //
				.check(matches(isDisplayed())) //
				.perform( //
						Actions.onlyIf(swipeUp(), isDisplayingAtLeast(1)) //
						//, swipeUp() //
				);
	}
}