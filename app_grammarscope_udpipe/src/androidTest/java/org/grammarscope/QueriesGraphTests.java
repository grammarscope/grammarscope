/*
 * Copyright (c) 2019. Bernard Bou <1313ou@gmail.com>.
 */

package org.grammarscope;

import junit.framework.TestCase;

import org.grammarscope.udpipe.R;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import androidx.annotation.NonNull;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class QueriesGraphTests extends TestCase
{
	@NonNull
	@Rule
	public ActivityScenarioRule<MainActivity> testRule = new ActivityScenarioRule<>(MainActivity.class);

	@Before
	public void before()
	{
		Seq.do_menu_check(R.string.action_as_graph, true);
	}

	@Test
	public void queries()
	{
		Do.test_queries_graph();
	}
}