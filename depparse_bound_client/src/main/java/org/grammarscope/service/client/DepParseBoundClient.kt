package org.grammarscope.service.client

import android.content.Context
import org.depparse.Sentence

class DepParseBoundClient(context: Context, service: String) : BoundClient<Array<Sentence>>(context, service)
