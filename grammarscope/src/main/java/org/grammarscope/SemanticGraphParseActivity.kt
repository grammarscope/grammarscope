package org.grammarscope

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.view.MenuItem
import edu.uci.ics.jung.settings.Configurator
import org.depparse.Token
import org.depparse.common.BaseParse
import org.grammarscope.common.R
import org.grammarscope.graph.CommonSettings
import org.grammarscope.graph.SemanticGraphParse
import org.grammarscope.graph.SemanticSettings
import org.grammarscope.graph.SentenceGraph
import org.grammarscope.semantics.Relation
import org.grammarscope.semantics.SemanticRelations

class SemanticGraphParseActivity : GraphParseActivity<Token, Relation>() {

    private var settings: CommonSettings? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        settings = SemanticSettings(this)
        SemanticRelations.read(this)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        settings = SemanticSettings(this)
    }

    // M E N U

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        if (item.itemId == R.id.graph_settings) {
            startActivity(Intent(this, SemanticSettingsActivity::class.java))
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun makeParse(): BaseParse<SentenceGraph<Token, Relation>> {
        return SemanticGraphParse(this, reverse = settings?.reverseDirection == true)
    }

    override fun configurator(): Configurator<Token, Relation> {
        return SemanticGraphConfigurator(SemanticGraphConfiguration(settings as SemanticSettings))
    }
}
