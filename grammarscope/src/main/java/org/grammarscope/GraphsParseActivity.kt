package org.grammarscope

import android.os.Bundle
import android.util.Log
import android.view.View
import com.google.android.material.floatingactionbutton.FloatingActionButton
import edu.uci.ics.jung.AugmentedGraph
import edu.uci.ics.jung.Visualizer
import org.depparse.Label
import org.depparse.Token
import org.grammarscope.common.R
import org.grammarscope.graph.SentenceGraph

abstract class GraphsParseActivity<V : Token, E : Label> : GraphBaseParseActivity<V, E, List<SentenceGraph<V, E>>?>() {

    private var graphs: List<SentenceGraph<V, E>>? = null
    protected var current = 0
    private lateinit var fabPrev: FloatingActionButton
    private lateinit var fabNext: FloatingActionButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fabPrev = findViewById(R.id.fab_prev)
        fabPrev.setOnClickListener { v: View? -> onFABPrevClick(v) }
        fabNext = findViewById(R.id.fab_next)
        fabNext.setOnClickListener { v: View? -> onFABNextClick(v) }
    }

    override val layout: Int
        get() = R.layout.activity_graph_nav

    override fun accept(result: List<SentenceGraph<V, E>>?) {
        graphs = result
        update()
    }

    private fun update() {
        if (graphs == null) {
            return
        }
        val n = graphs!!.size
        if (n == 0) {
            return
        }
        graph = graphs!![current]
        if (this.graph != null && graph!!.network.nodes().isNotEmpty() && graph!!.network.edges().isNotEmpty()) {

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

            // fabs
            updateFabs(current, n)
        }
    }

    private fun updateFabs(i: Int, n: Int) {
        if (i <= 0) {
            fabPrev.hide()
        } else {
            fabPrev.show()
        }
        if (i >= n - 1) {
            fabNext.hide()
        } else {
            fabNext.show()
        }
    }

    open fun onFABPrevClick(v: View?) {
        Log.d(TAG, "Prev")
        if (current - 1 >= 0) {
            current--
            update()
        }
    }

    open fun onFABNextClick(v: View?) {
        Log.d(TAG, "Next")
        val n = if (graphs == null) 0 else graphs!!.size
        if (current + 1 < n) {
            current++
            update()
        }
    }

    companion object {

        private const val TAG = "GraphsParseA"
    }
}
