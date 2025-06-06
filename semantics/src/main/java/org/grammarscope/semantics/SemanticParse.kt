package org.grammarscope.semantics

import org.depparse.Sentence
import org.depparse.common.BaseParse
import org.depparse.common.Factories
import java.util.function.Consumer

class SemanticParse(consumer: Consumer<CharSequence?>) : BaseParse<CharSequence>(consumer) {

    override fun toR(sentences: Array<Sentence>?): CharSequence? {
        return sentences?.let {
            val analyses = SemanticAnalyzer().analyze(*sentences)
            Analysis.toStringBuilder(
                analyses,
                Factories.textFactory,
                Factories.predicateFactory,
                Factories.subjectFactory,
                Factories.objectFactory,
                Factories.termFactory,
                Factories.predicate2Factory
            )
        }
    }
}
