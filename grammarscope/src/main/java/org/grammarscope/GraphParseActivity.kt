package org.grammarscope

import android.os.Bundle
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton
import edu.uci.ics.jung.AugmentedGraph
import edu.uci.ics.jung.Visualizer
import org.depparse.Label
import org.depparse.Token
import org.grammarscope.common.R
import org.grammarscope.graph.SentenceGraph

abstract class GraphParseActivity<V : Token, E : Label> : GraphBaseParseActivity<V, E, SentenceGraph<V, E>?>() {

    override val layout: Int
        get() = R.layout.activity_graph

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // handle window insets
        val fabRefresh = findViewById<FloatingActionButton>(R.id.fab_refresh)
        val fabRefreshMarginEnd = (fabRefresh.layoutParams as ViewGroup.MarginLayoutParams).marginEnd
        val fabRefreshMarginBottom = (fabRefresh.layoutParams as ViewGroup.MarginLayoutParams).bottomMargin
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.coord_layout)) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val appBarLayout = findViewById<AppBarLayout>(R.id.app_bar_layout)
            val container = findViewById<ViewGroup>(R.id.container)
            appBarLayout.setPadding(systemBars.left, 0, systemBars.right, 0)
            container.setPadding(systemBars.left, container.paddingTop, systemBars.right, systemBars.bottom)

            fabRefresh.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                marginEnd = fabRefreshMarginEnd + systemBars.right
                bottomMargin = fabRefreshMarginBottom + systemBars.bottom
            }
            insets
        }
    }

    override fun accept(result: SentenceGraph<V, E>?) {

        this.graph = result
        if (result != null && result.network.nodes().isNotEmpty() && result.network.edges().isNotEmpty()) {

            // attach graph to view
            Visualizer.visualize(
                viewer,
                configurator,
                graph as AugmentedGraph<V, E>,
            )

            // repaint
            val w = viewer.width
            val h = viewer.height
            if (w != 0 && h != 0) {
                viewer.repaint()
            }
        }
    }
}
