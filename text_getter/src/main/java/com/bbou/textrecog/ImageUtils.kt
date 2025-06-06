/*
 * Copyright (c) 2019. Bernard Bou <1313ou@gmail.com>.
 */
package com.bbou.textrecog

import android.content.ContentResolver
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.annotation.RequiresApi
import androidx.exifinterface.media.ExifInterface
import java.io.IOException
import java.io.InputStream

object ImageUtils {

    @RequiresApi(api = Build.VERSION_CODES.Q)
    fun getRotationFromMediaStore(context: Context, uri: Uri): Int {
        val columns = arrayOf(MediaStore.Images.Media.ORIENTATION)
        val cursor = context.contentResolver.query(uri, columns, null, null, null)
        var orientation = 0
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                val orientationColumnIndex = cursor.getColumnIndex(columns[0])
                orientation = cursor.getInt(orientationColumnIndex)
            }
            cursor.close()
        }
        return orientation
    }

    @Throws(IOException::class)
    fun getRotationFromFileExif(uri: Uri): Int {
        val path = uri.path
        if (path != null) {
            val exif = ExifInterface(path)
            return exif.getRotationDegrees()
        }
        return 0
    }

    @Throws(IOException::class)
    fun getRotationFromInputStreamExif(context: Context, uri: Uri): Int {
        context.contentResolver.openInputStream(uri).use { `is` ->
            if (`is` == null) {
                return 0
            }
            val exif = ExifInterface(`is`)
            return exif.getRotationDegrees()
        }
    }

    /**
     * Returns how much we have to rotate
     */
    fun getRotationForImage(context: Context, uri: Uri): Int {
        return try {
            if (ContentResolver.SCHEME_CONTENT == uri.scheme) {
                return getRotationFromInputStreamExif(context, uri)
                //return getRotationFromMediaStore(context, uri);
            } else if (ContentResolver.SCHEME_FILE == uri.scheme) {
                return getRotationFromFileExif(uri)
            }
            0
        } catch (e: IOException) {
            0
        }
    }

    fun makeBitmap(`is`: InputStream): Bitmap {
        `is`.mark(Int.MAX_VALUE)
        val bitmap = BitmapFactory.decodeStream(`is`)
        val degrees: Int = try {
            `is`.reset()
            val exif = ExifInterface(`is`)
            exif.getRotationDegrees()
        } catch (_: Exception) {
            90
        }
        if (degrees == 0) {
            return bitmap
        }
        val matrix = Matrix()
        matrix.preRotate(degrees.toFloat())
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }

    @Throws(IOException::class)
    fun makeBitmap(filePath: String): Bitmap {
        val bitmap = BitmapFactory.decodeFile(filePath)
        val exif = ExifInterface(filePath)
        val degrees = exif.getRotationDegrees()
        if (degrees == 0) {
            return bitmap
        }
        val matrix = Matrix()
        matrix.preRotate(degrees.toFloat())
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }
}
