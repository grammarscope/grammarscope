package org.depparse.common

import org.depparse.IProvider
import org.depparse.Sentence
import org.depparse.Unique

class UniqueProvider : Unique<IProvider<Array<Sentence>>?>() {

    fun kill() {
        val zombie: IProvider<Array<Sentence>>? = super.consume()
        zombie?.kill()
    }

    companion object {

        val SINGLETON = UniqueProvider()
    }
}
