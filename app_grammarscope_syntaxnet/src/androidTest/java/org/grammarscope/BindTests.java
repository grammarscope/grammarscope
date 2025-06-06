package org.grammarscope;

import junit.framework.TestCase;

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
public class BindTests extends TestCase
{
	@NonNull
	@Rule
	public ActivityScenarioRule<MainActivity> testRule = new ActivityScenarioRule<>(MainActivity.class);

	@SuppressWarnings("EmptyMethod")
	@Before
	public void before()
	{
	}

	@Test
	public void bind_unbind()
	{
		Do.test_toggle_bind();
	}
}