package org.grammarscope;

import junit.framework.TestCase;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import androidx.annotation.NonNull;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.filters.LargeTest;
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner;

@SuppressWarnings("WeakerAccess")
@RunWith(AndroidJUnit4ClassRunner.class)
@LargeTest
public class QueriesTextTests extends TestCase
{
	@NonNull
	@Rule
	public ActivityScenarioRule<MainActivity> testRule = new ActivityScenarioRule<>(MainActivity.class);

	@Before
	public void before()
	{
		Seq.do_menu_check(R.string.action_as_graph, false);
	}

	@Test
	public void queries()
	{
		Do.test_queries_text();
	}
}