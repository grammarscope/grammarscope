package com.bbou.textgetter

import android.content.Context
import android.os.Build
import android.os.Environment
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.nio.file.Files
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object Copy {

    /**
     * Copy input stream to temp file
     *
     * @param is input stream
     * @throws IOException io exception
     */
    @Throws(IOException::class)
    fun copyToFile(`is`: InputStream, context: Context): File {
        val file = createCacheImageFile(context)
        copyToFile(`is`, file)
        return file
    }

    /**
     * Copy input stream to file
     *
     * @param is   input stream (to be closed)
     * @param dest dest file
     * @throws IOException io exception
     */
    @Throws(IOException::class)
    fun copyToFile(`is`: InputStream, dest: File) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) Files.newOutputStream(dest.toPath()) else FileOutputStream(dest).use { os ->
            val buffer = ByteArray(1024)
            var length: Int
            while (`is`.read(buffer).also { length = it } > 0) {
                os.write(buffer, 0, length)
            }
            os.flush()
        }
        `is`.close()
    }

    /**
     * Create image file in cache
     *
     * @return file
     * @throws IOException IO exception
     */
    @Throws(IOException::class)
    private fun createCacheImageFile(context: Context): File {

        // Directory
        val cache = context.cacheDir

        // Create an image file name
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val imageFileName = "tmp_" + timeStamp + "_"
        return File.createTempFile(imageFileName, ".jpg", cache)
    }

    /**
     * Create image file
     *
     * @return file
     * @throws IOException IO exception
     */
    @Throws(IOException::class)
    fun createImageFile(context: Context): File {

        // Directory
        val dir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)!!
        if (!dir.exists()) {
            dir.mkdirs()
        }

        // Create an image file name
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val imageFileName = "snap_" + timeStamp + "_"
        return File.createTempFile(imageFileName, ".jpg", dir)
    }
}
