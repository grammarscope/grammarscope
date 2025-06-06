package org.depparse.common

import org.depparse.Sentence
import java.util.function.Consumer

class Parse(consumer: Consumer<CharSequence?>) : BaseParse<CharSequence>(consumer) {

    override fun toR(sentences: Array<Sentence>?): CharSequence? {
        return sentences?.let {
            return Decorator.toStyledCharSequence(
                sentences,
                Factories.textFactory,
                Factories.dependentFactory,
                Factories.headFactory,
                Factories.labelFactory,
                Factories.rootLabelFactory,
                Factories.enhancedLabelFactory,
            )
        }
    }
}