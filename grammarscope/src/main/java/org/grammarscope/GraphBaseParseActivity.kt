package org.grammarscope

import android.content.res.AssetManager
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.lifecycleScope
import com.bbou.capture.Capture.captureAndSave
import com.bbou.capture.Capture.captureAndShare
import com.bbou.capture.Capture.getBackgroundFromTheme
import edu.uci.ics.jung.AugmentedGraph
import edu.uci.ics.jung.Visualizer
import edu.uci.ics.jung.settings.Configurator
import edu.uci.ics.jung.visualization.VisualizationViewer
import edu.uci.ics.jung.visualization.renderers.RenderContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.depparse.Label
import org.depparse.Token
import org.depparse.common.AppMode.isNightMode
import org.depparse.common.BaseParse
import org.depparse.common.BaseParseActivity
import org.grammarscope.common.R
import org.grammarscope.graph.GraphColors
import org.grammarscope.graph.SentenceGraph

abstract class GraphBaseParseActivity<V : Token, E : Label, G> : BaseParseActivity<G>() {

    protected lateinit var colorSettings: ColorSettings

    protected lateinit var assetManager: AssetManager

    protected lateinit var viewer: VisualizationViewer<V, E>

    protected lateinit var context: RenderContext<V, E>

    open var graph: SentenceGraph<V, E>? = null

    lateinit var configurator: Configurator<V, E>

    // A B S T R A C T

    protected abstract fun makeParse(): BaseParse<out G?>

    // L I F E C Y C L E

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // statusbar
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val isNightMode = isNightMode(this)
        val controller = WindowInsetsControllerCompat(window, window.decorView)
        controller.isAppearanceLightStatusBars = !isNightMode

        viewer = findViewById(R.id.visualization_viewer)

        GraphColors.setColorsFromResources(this)
        colorSettings = ColorSettings(this)
        assetManager = resources.assets

        // handle window insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.coord_layout)) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(0, 0, 0, systemBars.bottom)
            insets
        }
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        val fabRefresh = findViewById<ImageButton>(R.id.fab_refresh)
        fabRefresh.setOnClickListener { onFABRefreshClick() }
    }

    override fun onStart() {
        super.onStart()
        configurator = configurator()
    }

    // P A R S E

    /**
     * Run parsing
     *
     * @param source source
     */
    override fun runParse(source: String?) {
        if (source.isNullOrEmpty()) {
            return
        }
        Log.d(TAG, "Parse run on '$source'")
        lifecycleScope.launch {
            makeParse().runAndCallback(Dispatchers.Default, source)
        }
    }

    /**
     * Build configuration that can be built before graph becomes available - called once from activity's onStart()
     */
    protected abstract fun configurator(): Configurator<V, E>

    override fun pending() {
        // none
    }

    // M E N U

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.graph, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        when (item.itemId) {
            R.id.action_capture -> {
                val view = capturedView(this)
                if (view != null) {
                    val bg = getBackgroundFromTheme(this)
                    captureAndSave(view, this, backGround = bg)
                }
                return true
            }

            R.id.action_share_capture -> {
                val view = capturedView(this)
                if (view != null) {
                    val bg = getBackgroundFromTheme(this)
                    captureAndShare(view, this, backGround = bg)
                }
                return true
            }

            else -> return super.onOptionsItemSelected(item)
        }
    }

    private fun capturedView(activity: AppCompatActivity): View? {
        val view = activity.findViewById<View>(R.id.visualization_viewer)
        if (view != null && view.width > 0 && view.height > 0) {
            return view
        }
        Toast.makeText(activity, R.string.status_capture_no_view, Toast.LENGTH_SHORT).show()
        return null
    }

    // C O M M A N D

    private fun onFABRefreshClick() {
        Log.d(TAG, "Refresh")
        if (graph != null && graph!!.network.nodes().isNotEmpty()) {
            // recompute config
            configurator = configurator()

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

    companion object {

        private const val TAG = "GraphBaseParseA"
    }
}
