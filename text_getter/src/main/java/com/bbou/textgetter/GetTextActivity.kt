/*
 * Copyright (c) 2019. Bernard Bou <1313ou@gmail.com>.
 */
package com.bbou.textgetter

import android.app.SearchManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.ResultReceiver
import android.provider.MediaStore
import android.util.Log
import android.view.inputmethod.InputMethodManager
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.widget.ViewPager2
import com.bbou.textrecog.ImageUtils
import com.bbou.textrecog.R
import com.bbou.textrecog.RecognizedTextViewModel
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import java.io.BufferedInputStream
import java.io.File
import java.io.IOException
import java.util.Objects

class GetTextActivity : AppCompatActivity() {

    var textFromFileModel: TextFromFileViewModel? = null
    private var imageFromFileModel: ImageFromFileViewModel? = null
    var textModel: RecognizedTextViewModel? = null
    var sentencesModel: SentencesViewModel? = null
    private var activityDocumentResultLauncher: ActivityResultLauncher<Intent>? = null
    private var activityCameraResultLauncher: ActivityResultLauncher<Intent>? = null
    private var activityImageResultLauncher: ActivityResultLauncher<Intent>? = null
    private lateinit var pager: ViewPager2
    private lateinit var tabTitles: Array<String>

    // L I F E C Y C L E

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // edge to edge
        enableEdgeToEdge()

        // layout
        setContentView(R.layout.activity_gettext)
        var lang = intent.getStringExtra(ARG_LANG)
        if (lang == null) {
            lang = "en"
        }

        // titles
        tabTitles = getResources().getStringArray(R.array.tabs)

        // models
        textFromFileModel = ViewModelProvider(this, TextFromFileViewModel.Factory(this))[TextFromFileViewModel::class.java]
        textModel = ViewModelProvider(this)[RecognizedTextViewModel::class.java]
        sentencesModel = ViewModelProvider(this, SentencesViewModel.Factory(this, lang))[SentencesViewModel::class.java]
        imageFromFileModel = ViewModelProvider(this, ImageFromFileViewModel.Factory(this))[ImageFromFileViewModel::class.java]
        imageFromFileModel!!.output!!.observe(this) { bm: Bitmap? -> textModel!!.input.value = bm }

        // result launchers
        activityDocumentResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            val success = result.resultCode == RESULT_OK
            Log.d(TAG, "Document fetcher " + if (success) "succeeded" else "failed")
            if (success) {
                val data = result.data
                if (data != null) {
                    val uri = data.data
                    Log.d(TAG, "Uri  = $uri")
                    textFromFileModel!!.input.value = uri
                }
            }
        }
        activityImageResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            val success = result.resultCode == RESULT_OK
            Log.d(TAG, "Image fetcher " + if (success) "succeeded" else "failed")
            if (success) {
                val data = result.data
                if (data != null) {
                    val uri = data.data
                    Log.d(TAG, "Uri  = $uri")
                    imageFromFileModel!!.input.value = uri
                }
            }
        }
        activityCameraResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            val success = result.resultCode == RESULT_OK
            Log.d(TAG, "Image fetcher " + if (success) "succeeded" else "failed")
            if (success) {
                val bitmap = getBitmapFromFile(currentPhotoPath!!)
                currentPhotoPath?.let { File(it).delete() }
                textModel!!.input.value = bitmap
            }
        }

        // pager
        pager = findViewById(R.id.view_pager)
        val pagerAdapter = PagerAdapter(this, receiver, CODE_SENTENCE_QUERY, KEY_SENTENCE_QUERY)
        pager.setAdapter(pagerAdapter)
        pager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {

            override fun onPageSelected(position: Int) {
                closeKeyboard()
            }

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}
        })

        // tabs
        val tabs = findViewById<TabLayout>(R.id.tabs)
        TabLayoutMediator(tabs, pager) { tab: TabLayout.Tab, position: Int -> tab.text = tabTitles[position] }.attach()
        val fabRecog = findViewById<FloatingActionButton>(R.id.fab_recog)
        fabRecog.setOnClickListener {
            tryTakePicture()
        }
        val fabDocument = findViewById<FloatingActionButton>(R.id.fab_document)
        fabDocument.setOnClickListener { trySelectDocument() }
        val fabImage = findViewById<FloatingActionButton>(R.id.fab_image)
        fabImage.setOnClickListener { trySelectImage() }

        // handle window insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.coord_layout)) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(0, 0, 0, systemBars.bottom)
            insets
        }
    }

    /**
     * Text recognition + Sentence detection + Sentence selection receiver
     */
    private val receiver: ResultReceiver = object : ResultReceiver(Handler(Looper.getMainLooper())) {

        override fun onReceiveResult(resultCode: Int, resultData: Bundle) {
            super.onReceiveResult(resultCode, resultData)
            when (resultCode) {
                CODE_SENTENCE_QUERY -> {
                    val query = resultData.getString(KEY_SENTENCE_QUERY)
                    if (query != null) {
                        tryStartWithText(this@GetTextActivity, query)
                    }
                }

                CODE_SENTENCE_QUERY + 1 -> Log.d(TAG, "Received failure code $resultCode")
            }
        }
    }

    // S E L E C T   D O C U M E N T

    /**
     * Dispatch select-document intent
     */
    private fun trySelectDocument() {
        val chooseFile = Intent(Intent.ACTION_GET_CONTENT)
        chooseFile.addCategory(Intent.CATEGORY_OPENABLE)
        chooseFile.type = "text/plain"
        val intent = Intent.createChooser(chooseFile, "Choose a file")
        activityDocumentResultLauncher!!.launch(intent)
    }

    // S E L E C T   I M A G E

    /**
     * Dispatch select-image intent
     */
    private fun trySelectImage() {
        val chooseFile = Intent(Intent.ACTION_GET_CONTENT)
        chooseFile.addCategory(Intent.CATEGORY_OPENABLE)
        chooseFile.type = "image/*"
        val intent = Intent.createChooser(chooseFile, "Choose a file")
        activityImageResultLauncher!!.launch(intent)
    }

    // T A K E   P H O T O

    /**
     * Capture file path
     */
    private var currentPhotoPath: String? = null

    /**
     * Dispatch take-picture intent
     */
    private fun tryTakePicture() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)

        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(packageManager) != null) {
            // Create the File where the photo should go
            try {
                val photoFile = Copy.createImageFile(this)

                // Save a file: path for use with ACTION_VIEW intents
                currentPhotoPath = photoFile.absolutePath
                Log.d(TAG, "Photo $currentPhotoPath")
                val applicationId = applicationContext.packageName
                val authority = "${applicationId}.fileprovider"
                val photoURI = FileProvider.getUriForFile(this, authority, photoFile)
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                activityCameraResultLauncher!!.launch(takePictureIntent)
            } catch (e: IOException) {
                Log.e(TAG, " Error occurred while creating photo file $e")
            }
        }
    }

    // H E L P E R S

    /**
     * Close keyboard
     */
    private fun closeKeyboard() {
        val view = this.currentFocus
        if (view != null) {
            val imm = (getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager)
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }

    companion object {

        private const val TAG = "GetTextA"
        const val ARG_LANG = "lang"

        // R E C E I V E R

        private const val CODE_SENTENCE_QUERY = 0x300
        private const val KEY_SENTENCE_QUERY = "com.bbou.textrecog.SENTENCE_QUERY"

        /**
         * Start query
         *
         * @param activity activity
         * @param query    query
         */
        private fun tryStartWithText(activity: AppCompatActivity, query: String) {
            val context = activity.applicationContext
            val name = context.getString(R.string.search_target)
            val component = ComponentName(context, name)
            val intent = Intent()
            intent.component = component
            intent.action = Intent.ACTION_SEARCH
            intent.putExtra(SearchManager.QUERY, query)
            activity.startActivity(intent)
        }

        /**
         * Make bitmap from file
         *
         * @param filePath file path
         * @return bitmap
         */
        private fun getBitmapFromFile(filePath: String): Bitmap? {
            try {
                return ImageUtils.makeBitmap(filePath)
            } catch (e: IOException) {
                Log.e(TAG, " Error occurred while getting bitmap from file $e")
            }
            return null
        }

        /**
         * Make bitmap from asset
         *
         * @param context context
         * @param path    path in assets
         * @return bitmap
         */
        @Suppress("unused")
        private fun getBitmapFromAsset(context: Context, path: String): Bitmap? {
            val assetManager = context.assets
            try {
                assetManager.open(path).use { `is` -> return ImageUtils.makeBitmap(`is`) }
            } catch (e: IOException) {
                Log.e(TAG, " Error occurred while getting bitmap from asset $e")
            }
            return null
        }

        @Suppress("unused")
        private fun getBitmapFromUri(context: Context, uri: Uri): Bitmap? {
            try {
                context.contentResolver.openInputStream(uri).use { `is` -> BufferedInputStream(Objects.requireNonNull(`is`)).use { bis -> return ImageUtils.makeBitmap(bis) } }
            } catch (e: IOException) {
                Log.e(TAG, " Error occurred while getting bitmap from uri $e")
            }
            return null
        }
    }
}