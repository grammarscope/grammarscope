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
import org.grammarscope.graph.DependencyGraphParse
import org.grammarscope.graph.DependencySettings
import org.grammarscope.graph.SentenceGraph

class DependencyGraphParseActivity : GraphParseActivity<Token, Token>() {

    private var settings: CommonSettings? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        settings = DependencySettings(this)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        settings = DependencySettings(this)
    }

    // M E N U

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        if (item.itemId == R.id.graph_settings) {
            startActivity(Intent(this, DependencySettingsActivity::class.java))
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun makeParse(): BaseParse<out SentenceGraph<Token, Token>> {
        return DependencyGraphParse(this, reverse = settings?.reverseDirection == true)
    }

    override fun configurator(): Configurator<Token, Token> {
        return DependencyGraphConfigurator(DependencyGraphConfiguration(settings as DependencySettings))
    }
}
