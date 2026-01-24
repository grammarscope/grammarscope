package org.grammarscope

import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bbou.capture.Capture.captureAndSave
import com.bbou.capture.Capture.captureAndShare
import com.bbou.capture.Capture.getBackgroundFromTheme
import org.depparse.common.TextBaseParseActivity
import org.grammarscope.common.R
import com.bbou.capture.R as CaptureR
import org.depparse.common.R as CommonR

abstract class TextParseActivity : TextBaseParseActivity() {

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
        val view = activity.findViewById<View>(CommonR.id.parsed)
        if (view != null && view.width > 0 && view.height > 0) {
            return view
        }
        Toast.makeText(activity, CaptureR.string.status_capture_no_view, Toast.LENGTH_SHORT).show()
        return null
    }
}
