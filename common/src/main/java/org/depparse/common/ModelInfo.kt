package org.depparse.common

import android.content.Context
import org.depparse.Storage.getAppStorage
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.io.IOException

data class ModelInfo(val lang: String, val name: String, val more1: String?, val more2: String?) {

    val isEnglish: Boolean
        get() = "English" == lang
    val isFrench: Boolean
        get() = "French" == lang

    companion object {

        fun read(context: Context): ModelInfo? {
            val dir = getAppStorage(context)
            val file = File(dir, "model")
            try {
                BufferedReader(FileReader(file)).use { br ->
                    //                          UDPIPE                              SYNTAXNET
                    // lang_strict camelcase    Czech                               French
                    // language camelcase       Czech-pdt                           French-ParTUT
                    // language lowercase       czech-pdt
                    // model                    czech-pdt-ud-2.3-181115.udpipe
                    val lang = br.readLine() // first line = lang
                    val name = br.readLine() // second line = name
                    val more1 = try {
                        br.readLine() // third line = lang lowercase
                    } catch (_: IOException) {
                        null
                    }
                    val more2 = try {
                        br.readLine() // fourth line = model file
                    } catch (_: IOException) {
                        null
                    }
                    return ModelInfo(lang, name, more1, more2)
                }
            } catch (_: IOException) {
            }
            return null
        }

        fun modelToString(context: Context): String {
            val modelInfo = read(context)
            return if (modelInfo == null) {
                "No model"
            } else {
                val model = context.getString(R.string.model_is, modelInfo.name)
                val lang = context.getString(R.string.language_is, modelInfo.lang)
                return "$model\n$lang"
            }
        }
    }
}
