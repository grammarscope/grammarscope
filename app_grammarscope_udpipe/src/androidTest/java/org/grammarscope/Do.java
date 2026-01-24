/*
 * Copyright (c) 2019. Bernard Bou <1313ou@gmail.com>.
 */

package org.grammarscope;

import android.view.View;

import androidx.annotation.IdRes;

import org.grammarscope.common.R;
import org.hamcrest.Matcher;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.swipeDown;
import static androidx.test.espresso.action.ViewActions.swipeUp;
import static androidx.test.espresso.assertion.ViewAssertions.doesNotExist;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static org.hamcrest.CoreMatchers.anyOf;

class Do
{
	static void test_toggle_bind()
	{
		final Matcher<View> indicator = withId(R.id.loaded_indicator);
		final Matcher<View> warn = Matchers.withDrawable(R.drawable.ic_warn);
		final Matcher<View> ok = Matchers.withDrawable(R.drawable.ic_ok);

		onView(indicator).check(matches(anyOf(warn, ok)));

		Wait.pause(5);
		boolean wasStarted = ToBoolean.test(indicator, ok);
		for (int i = 0; i < 10; i++)
		{
			wasStarted = test_toggle_bind(wasStarted);
			wasStarted = test_toggle_bind(wasStarted);
		}
	}

	static private boolean test_toggle_bind(boolean wasStarted)
	{
		Seq.do_menu(R.id.bind, wasStarted ? R.string.action_unbind : R.string.action_bind);
		Wait.pause(wasStarted ? 1 : 3);
		onView(withId(R.id.loaded_indicator)).check(matches(Matchers.withDrawable(wasStarted ? R.drawable.ic_warn : R.drawable.ic_ok)));
		return !wasStarted;
	}

	static void test_queries_text()
	{
		String[] sentences = DataUtils.getSentenceList();
		if (sentences == null)
		{
			return;
		}
		for (String sentence : sentences)
		{
			Seq.do_click(R.id.clear);
			Seq.do_type(R.id.query, sentence);

			Wait.until(R.id.fab_dependencies, 2);
			Seq.do_click(R.id.fab_dependencies);
			Wait.pause(3); // snackbar

			Wait.until(org.depparse.common.R.id.parsed, 3);
			onView(withId(org.depparse.common.R.id.parsed)) //
					.check(matches(isDisplayed())) //
			;
			onView(withId(org.depparse.common.R.id.parsed)) //
					.perform(swipeUp()) //
					.perform(swipeDown()) //
			;
			pages(R.id.fab_next, R.id.fab_prev);
			Seq.do_pressBack();

			Wait.until(R.id.fab_semantics, 10);
			Seq.do_click(R.id.fab_semantics);
			Wait.pause(3); // snackbar

			Wait.until(org.depparse.common.R.id.parsed, 10);
			onView(withId(org.depparse.common.R.id.parsed)) //
					.check(matches(isDisplayed()));
			onView(withId(org.depparse.common.R.id.parsed)) //
					.perform(swipeUp()) //
					.perform(swipeDown()) //
			;
			pages(R.id.fab_next, R.id.fab_prev);
			Seq.do_pressBack();
		}
	}

	static void test_queries_graph()
	{
		String[] sentences = DataUtils.getSentenceList();
		if (sentences == null)
		{
			return;
		}
		for (String sentence : sentences)
		{
			Seq.do_click(R.id.clear);
			Seq.do_type(R.id.query, sentence);

			Wait.until(R.id.fab_dependencies, 2);
			Seq.do_click(R.id.fab_dependencies);
			Wait.pause(3); // snackbar

			Wait.until(R.id.visualization_viewer, 10);
			onView(withId(R.id.visualization_viewer)).check(matches(isDisplayed())) //
					.perform(swipeUp()) //
					.perform(swipeDown()) //
			;
			pages(R.id.fab_next, R.id.fab_prev);
			Seq.do_pressBack();

			Wait.until(R.id.fab_semantics, 10);
			Seq.do_click(R.id.fab_semantics);
			Wait.pause(3); // snackbar

			Wait.until(R.id.visualization_viewer, 10);
			onView(withId(R.id.visualization_viewer)) //
					.check(matches(isDisplayed())) //
					.perform(swipeUp()) //
					.perform(swipeDown()) //
			;

			pages(R.id.fab_next, R.id.fab_prev);
			Seq.do_pressBack();
		}
	}

	static private void pages(@SuppressWarnings("SameParameterValue") @IdRes int nextId, @SuppressWarnings("SameParameterValue") @IdRes int prevId)
	{
		boolean hasNext = !ToBoolean.testAssertion(withId(nextId), doesNotExist());
		if (hasNext)
		{
			boolean isNextVisible = ToBoolean.test(withId(nextId), isDisplayed());
			while (isNextVisible)
			{
				onView(withId(nextId)).perform(click());
				Wait.pause(1);
				isNextVisible = ToBoolean.test(withId(nextId), isDisplayed());
			}
		}
		boolean hasPrev = !ToBoolean.testAssertion(withId(prevId), doesNotExist());
		if (hasPrev)
		{
			boolean isNextVisible = ToBoolean.test(withId(prevId), isDisplayed());
			while (isNextVisible)
			{
				onView(withId(prevId)).perform(click());
				Wait.pause(1);
				isNextVisible = ToBoolean.test(withId(prevId), isDisplayed());
			}
		}
	}
}