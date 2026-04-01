package org.grammarscope

import android.os.Bundle
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import edu.uci.ics.jung.AugmentedGraph
import edu.uci.ics.jung.Visualizer
import org.depparse.EdgeToEdge.updateBottomMargin
import org.depparse.Label
import org.depparse.Token
import org.grammarscope.common.R
import org.grammarscope.graph.SentenceGraph

abstract class GraphParseActivity<V : Token, E : Label> : GraphBaseParseActivity<V, E, SentenceGraph<V, E>?>() {

    override val layout: Int
        get() = R.layout.activity_graph

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)

        // handle window insets
        val fabRefreshMarginBottom = (fabRefresh.layoutParams as ViewGroup.MarginLayoutParams).bottomMargin
        ViewCompat.setOnApplyWindowInsetsListener(rootView!!) { _, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            fabRefresh.updateBottomMargin(systemBars, initialMargin = fabRefreshMarginBottom)
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
