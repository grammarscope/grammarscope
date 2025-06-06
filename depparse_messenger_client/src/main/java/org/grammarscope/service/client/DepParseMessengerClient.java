package org.grammarscope.service.client;

import android.content.Context;
import androidx.annotation.NonNull;

import org.depparse.Sentence;
import org.grammarscope.result.Parceler;

@SuppressWarnings("WeakerAccess")
public class DepParseMessengerClient extends MessengerClient<Sentence[]>
{
	/**
	 * Constructor
	 *
	 * @param context0 context
	 * @param service0 service full name (pkg/class)
	 */
	public DepParseMessengerClient(final Context context0, @NonNull final String service0)
	{
		super(context0, service0, new Parceler());
	}
}
