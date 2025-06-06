package org.grammarscope;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.test.platform.app.InstrumentationRegistry;

class DataUtils
{
	static private final String LIST_FILE = "tests/sentence.list";

	@NonNull
	static public String arrayToString(@NonNull int... a)
	{
		final StringBuilder sb = new StringBuilder();
		sb.append('{');
		boolean first = true;
		for (int i : a)
		{
			if (first)
			{
				first = false;
			}
			else
			{
				sb.append(',');
			}
			sb.append(i);
		}
		sb.append('}');
		return sb.toString();
	}

	// S A M P L E S

	@Nullable
	static String[] getSentenceList()
	{
		//return SENTENCE_LIST;
		return readSentenceList();
	}

	@Nullable
	static private String[] readSentenceList()
	{
		final Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
		final AssetManager assets = context.getResources().getAssets();
		final List<String> list = new ArrayList<>();
		try (InputStream is = assets.open(DataUtils.LIST_FILE); //
		     Reader reader = new InputStreamReader(is); //
		     BufferedReader br = new BufferedReader(reader) //
		)
		{
			//final FileReader reader = new FileReader(dataFile);
			String line;
			while ((line = br.readLine()) != null)
			{
				list.add(line.trim());
			}
			br.close();
			return list.toArray(new String[0]);
		}
		catch (IOException e)
		{
			//Log.d("Read", "Error " + dataFile.getAbsolutePath(), e);
			Log.e("Read", "Error " + DataUtils.LIST_FILE, e);
			return null;
		}
	}
}
