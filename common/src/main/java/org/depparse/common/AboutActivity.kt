package org.depparse.common

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.bbou.donate.DonateActivity
import com.bbou.others.OthersActivity

/**
 * About activity
 *
 * @author [Bernard Bou](mailto:1313ou@gmail.com)
 */
class AboutActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // content
        setContentView(R.layout.activity_about)

        // toolbar
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        // set up the action bar
        val actionBar = supportActionBar!!
        actionBar.displayOptions = ActionBar.DISPLAY_SHOW_HOME or ActionBar.DISPLAY_HOME_AS_UP or ActionBar.DISPLAY_SHOW_TITLE
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // inflate the menu; this adds items to the type bar if it is present.
        menuInflater.inflate(R.menu.about, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.help -> {
                WebActivity.tryStart(this, "file:///android_asset/help/en/index.html", usesJavaScript = true, local = true)
                return true
            }

            R.id.reference -> {
                WebActivity.tryStart(this, "file:///android_asset/reference/index.html", usesJavaScript = true, local = true)
                return true
            }

            R.id.reference_uds -> {
                WebActivity.tryStart(this, "file:///android_asset/reference/uds.html", usesJavaScript = true, local = true)
                return true
            }

            R.id.reference_online -> {
                WebActivity.tryStart(this, "https://universaldependencies.org/u/dep/all.html", usesJavaScript = true, local = false)
                return true
            }

            R.id.others -> {
                startActivity(Intent(this, OthersActivity::class.java))
                return true
            }

            R.id.donate -> {
                startActivity(Intent(this, DonateActivity::class.java))
                return true
            }

            else -> {
                return super.onOptionsItemSelected(item)
            }
        }
    }
}
