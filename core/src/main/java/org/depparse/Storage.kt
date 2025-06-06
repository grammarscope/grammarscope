package org.depparse

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.os.Environment
import android.util.Log
import android.util.Pair
import java.io.File
import java.util.EnumMap
import java.util.TreeSet
import androidx.core.content.edit

object Storage {

    private const val TAG = "Storage"

    /**
     * Shared preferences
     */
    private const val PREFERENCES_DEVICE = "org.grammarscope_preferences_device"

    /**
     * App storage preference name
     */
    private const val PREF_DEPPARSER_STORAGE = "pref_storage"

    /**
     * App sub directory
     */
    private const val DEPPARSER_DIR = "depparser" + '/'

    /**
     * Cached depparser storage
     */
    private var depparserStorage: File? = null

    /**
     * Cached external storage
     */
    private var extStorage: String? = null

    /**
     * Get data cache
     *
     * @param context context
     * @return data cache
     */
    fun getCacheDir(context: Context): File {
        // external is first choice
        var cache = context.externalCacheDir

        // internal is second choice
        if (cache == null) {
            cache = context.cacheDir
        }
        return File(cache!!.absolutePath)
    }

    private val externalStorage: String?
        /**
         * Get external storage
         *
         * @return external storage directory
         */
        get() {
            if (extStorage == null) {
                extStorage = discoverExternalStorage()
            }
            return extStorage
        }

    /**
     * Discover external storage
     *
     * @return (cached) external storage directory
     */
    @SuppressLint("ObsoleteSdkInt")
    private fun discoverExternalStorage(): String? {

        // S E C O N D A R Y

        // all secondary sdcards (all exclude primary) separated by ":"
        val secondaryStoragesStr = System.getenv("SECONDARY_STORAGE")

        // add all secondary storages
        if (secondaryStoragesStr != null && secondaryStoragesStr.isNotEmpty()) {
            // all secondary sdcards split into array
            val secondaryStorages = secondaryStoragesStr
                .split(File.pathSeparator.toRegex())
                .dropLastWhile { it.isEmpty() }
                .toTypedArray()
            if (secondaryStorages.isNotEmpty()) {
                return secondaryStorages[0]
            }
        }

        // P R I M A R Y E M U L A T E D

        // primary emulated sdcard
        val emulatedStorageTarget = System.getenv("EMULATED_STORAGE_TARGET")
        if (emulatedStorageTarget != null && emulatedStorageTarget.isNotEmpty()) {
            // device has emulated extStorage; external extStorage paths should have userId burned into them.
            val userId: String = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) {
                ""
            } else {
                val path = Environment.getExternalStorageDirectory().absolutePath
                val folders = path.split(File.separator.toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                val lastFolder = folders[folders.size - 1]
                var isDigit = false
                try {
                    lastFolder.toInt()
                    isDigit = true
                } catch (_: NumberFormatException) {
                    // ignore
                }
                if (isDigit) lastFolder else ""
            }

            // /extStorage/emulated/0[1,2,...]
            return if (userId.isNotEmpty()) {
                emulatedStorageTarget + File.separatorChar + userId
            } else emulatedStorageTarget
        }

        // P R I M A R Y N O N E M U L A T E D

        // primary physical sdcard (not emulated)
        val externalStorage = System.getenv("EXTERNAL_STORAGE")

        // device has physical external extStorage; use plain paths.
        return if (externalStorage != null && externalStorage.isNotEmpty()) {
            externalStorage
        } else null

        // EXTERNAL_STORAGE undefined; falling back to default.
        // return "/extStorage/sdcard0";
    }

    /**
     * External storage directories
     */
    @Suppress("unused")
    val storageDirectories: Map<StorageType, Array<String>>
        get() {
            // result set of paths
            val dirs: MutableMap<StorageType, Array<String>> = EnumMap(StorageType::class.java)

            // P R I M A R Y

            // primary emulated sdcard
            val emulatedStorageTarget = System.getenv("EMULATED_STORAGE_TARGET")
            if (emulatedStorageTarget != null && emulatedStorageTarget.isNotEmpty()) {
                // device has emulated extStorage; external extStorage paths should have userId burned into them.
                val path = Environment.getExternalStorageDirectory().absolutePath
                val folders = path.split(File.separator.toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                val lastFolder = folders[folders.size - 1]
                var isDigit = false
                try {
                    lastFolder.toInt()
                    isDigit = true
                } catch (_: NumberFormatException) {
                    // ignore
                }
                val userId: String = if (isDigit) lastFolder else ""

                // /extStorage/emulated/0[1,2,...]
                if (userId.isEmpty()) {
                    dirs[StorageType.PRIMARY_PHYSICAL] = arrayOf(emulatedStorageTarget)
                } else {
                    dirs[StorageType.PRIMARY_PHYSICAL] = arrayOf(emulatedStorageTarget + File.separatorChar + userId)
                }
            } else {
                // primary physical sdcard (not emulated)
                val externalStorage = System.getenv("EXTERNAL_STORAGE")

                // device has physical external extStorage; use plain paths
                if (externalStorage != null && externalStorage.isNotEmpty()) {
                    dirs[StorageType.PRIMARY_EMULATED] = arrayOf(externalStorage)
                } else {
                    // EXTERNAL_STORAGE undefined; falling back to default.
                    dirs[StorageType.PRIMARY_EMULATED] = arrayOf("/extStorage/sdcard0")
                }
            }

            // S E C O N D A R Y

            // all secondary sdcards (all exclude primary) separated by ":"
            val secondaryStoragesStr = System.getenv("SECONDARY_STORAGE")

            // add all secondary storages
            if (secondaryStoragesStr != null && secondaryStoragesStr.isNotEmpty()) {
                // all secondary sdcards split into array
                val secondaryStorages = secondaryStoragesStr.split(File.pathSeparator.toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                if (secondaryStorages.isNotEmpty()) {
                    dirs[StorageType.SECONDARY] = secondaryStorages
                }
            }
            return dirs
        }

    /**
     * Get depparser storage
     *
     * @return depparser storage directory
     */
    @SuppressLint("CommitPrefEdits", "ApplySharedPref")
    fun getAppStorage(context: Context): File {
        // if cached return cache
        if (depparserStorage != null) {
            return depparserStorage!!
        }

        // test if already discovered in this context
        val sharedPref = context.getSharedPreferences(PREFERENCES_DEVICE, Context.MODE_PRIVATE)
        val pref = sharedPref.getString(PREF_DEPPARSER_STORAGE, null)
        if (pref != null) {
            depparserStorage = File(pref)
            if (qualifies(depparserStorage!!)) {
                return depparserStorage!!
            }
        }

        // discover
        depparserStorage = discoverAppStorage(context)
        val path = depparserStorage!!.absolutePath

        // flag as discovered
        sharedPref.edit(commit = true) { putString(PREF_DEPPARSER_STORAGE, path) }
        return depparserStorage!!
    }

    /**
     * Discover App storage
     *
     * @param context context
     * @return App storage
     */
    private fun discoverAppStorage(context: Context): File {
        // application-specific secondary storage or primary (KITKAT)
        try {
            val dirs = context.getExternalFilesDirs(null)
            if (dirs != null && dirs.isNotEmpty()) {
                // preferably secondary storage
                for (i in 1 until dirs.size) {
                    if (qualifies(dirs[i])) {
                        return dirs[i]
                    }
                }
                // fall back on primary storage
                if (qualifies(dirs[0])) {
                    return dirs[0]
                }
            }
        } catch (_: Throwable) {
            // ignore
        }
        var dir: File?

        // application-specific primary storage
        dir = context.getExternalFilesDir(null)
        if (dir != null && qualifies(dir)) {
            return dir
        }
        dir = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
        if (dir != null && qualifies(dir)) {
            return dir
        }
        dir = context.getExternalFilesDir("Documents")
        if (dir != null && qualifies(dir)) {
            return dir
        }

        // top-level public external storage directory (KITKAT for DIRECTORY_DOCUMENTS)
        dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
        if (qualifies(dir)) {
            return dir
        }

        // top-level public in external
        try {
            val storage = externalStorage
            if (storage != null) {
                dir = File(storage, DEPPARSER_DIR)
                if (qualifies(dir)) {
                    return dir
                }
            }
        } catch (_: Throwable) {
            // ignore
        }

        // internal private storage
        return context.filesDir
    }

    /**
     * Whether the dir qualifies as depparser storage
     *
     * @param dir candidate dir
     * @return true if it qualifies
     */
    private fun qualifies(dir: File): Boolean {

        // log
        Log.d(TAG, "storage state of " + dir + ": " + Environment.getExternalStorageState(dir))

        // either mkdirs() creates dir or it is already a dir
        return dir.mkdirs() || dir.isDirectory // || dir.canWrite())
    }

    /**
     * Directories as types and values
     */
    @Suppress("unused")
    val directoriesTypesValues: Pair<Array<CharSequence?>, Array<CharSequence>>
        get() {
            val types: MutableList<CharSequence?> = ArrayList()
            val values: MutableList<CharSequence> = ArrayList()
            val dirs = directories
            for (dir in dirs) {
                // types
                types.add(dir.type.toDisplay())

                // value
                values.add(dir.file!!.absolutePath)
            }
            return Pair(types.toTypedArray<CharSequence?>(), values.toTypedArray<CharSequence>())
        }

    private val directories: Collection<Directory>
        /**
         * Get list of directories
         *
         * @return list of storage directories
         */
        get() {
            val tags = arrayOf(
                Environment.DIRECTORY_PODCASTS,
                Environment.DIRECTORY_RINGTONES,
                Environment.DIRECTORY_ALARMS,
                Environment.DIRECTORY_NOTIFICATIONS,
                Environment.DIRECTORY_PICTURES,
                Environment.DIRECTORY_MOVIES,
                Environment.DIRECTORY_DOWNLOADS,
                Environment.DIRECTORY_DCIM
            )
            val result: MutableSet<Directory> = TreeSet()
            var dir: File?

            // P U B L I C

            // top-level public external storage directory
            for (tag in tags) {
                dir = Environment.getExternalStoragePublicDirectory(tag)
                if (dir.exists()) {
                    result.add(Directory(dir, DirType.PUBLIC_EXTERNAL_PRIMARY))
                }
            }
            dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
            if (dir.exists()) {
                result.add(Directory(dir, DirType.PUBLIC_EXTERNAL_PRIMARY))
            }

            // top-level public in external
            dir = Environment.getExternalStorageDirectory()
            if (dir != null) {
                if (dir.exists()) {
                    result.add(Directory(dir, DirType.PUBLIC_EXTERNAL_PRIMARY))
                }
            }

            // S E C O N D A R Y

            // all secondary sdcards split into array
            val secondaries = discoverSecondaryExternalStorage()
            if (secondaries != null) {
                for (secondary in secondaries) {
                    dir = secondary
                    if (dir.exists()) {
                        result.add(Directory(dir, DirType.PUBLIC_EXTERNAL_SECONDARY))
                    }
                }
            }

            // P R I M A R Y

            // primary emulated sdcard
            dir = discoverPrimaryEmulatedExternalStorage()
            if (dir != null) {
                if (dir.exists()) {
                    result.add(Directory(dir, DirType.PUBLIC_EXTERNAL_PRIMARY))
                }
            }
            dir = discoverPrimaryPhysicalExternalStorage()
            if (dir != null) {
                if (dir.exists()) {
                    result.add(Directory(dir, DirType.PUBLIC_EXTERNAL_PRIMARY))
                }
            }
            result.add(Directory(File("/storage"), DirType.PUBLIC_EXTERNAL_PRIMARY))
            return result
        }

    /**
     * Discover primary emulated external storage directory
     *
     * @return primary emulated external storage directory
     */
    private fun discoverPrimaryEmulatedExternalStorage(): File? {
        // primary emulated sdcard
        val emulatedStorageTarget = System.getenv("EMULATED_STORAGE_TARGET")
        if (emulatedStorageTarget != null && emulatedStorageTarget.isNotEmpty()) {
            // device has emulated extStorage
            // external extStorage paths should have userId burned into them
            val userId = userId

            // /extStorage/emulated/0[1,2,...]
            return if ( /*userId == null ||*/userId.isEmpty()) {
                File(emulatedStorageTarget)
            } else {
                File(emulatedStorageTarget + File.separatorChar + userId)
            }
        }
        return null
    }

    /**
     * Discover primary physical external storage directory
     *
     * @return primary physical external storage directory
     */
    private fun discoverPrimaryPhysicalExternalStorage(): File? {
        val externalStorage = System.getenv("EXTERNAL_STORAGE")
        // device has physical external extStorage; use plain paths.
        return if (externalStorage != null && externalStorage.isNotEmpty()) {
            File(externalStorage)
        } else null
    }

    /**
     * Discover secondary external storage directories
     *
     * @return secondary external storage directories
     */
    private fun discoverSecondaryExternalStorage(): Array<File>? {
        // all secondary sdcards (all except primary) separated by ":"
        var secondaryStoragesEnv = System.getenv("SECONDARY_STORAGE")
        if (secondaryStoragesEnv == null || secondaryStoragesEnv.isEmpty()) {
            secondaryStoragesEnv = System.getenv("EXTERNAL_SDCARD_STORAGE")
        }

        // addItem all secondary storages
        if (secondaryStoragesEnv != null && secondaryStoragesEnv.isNotEmpty()) {
            // all secondary sdcards split into array
            val paths = secondaryStoragesEnv.split(File.pathSeparator.toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            val dirs: MutableList<File> = ArrayList()
            for (path in paths) {
                val dir = File(path)
                if (dir.exists()) {
                    dirs.add(dir)
                }
            }
            return dirs.toTypedArray<File>()
        }
        return null
    }

    // U S E R I D

    private val userId: String
        get() {
            val path = Environment.getExternalStorageDirectory().absolutePath
            val folders = path.split(File.separator.toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            val lastFolder = folders[folders.size - 1]
            var isDigit = false
            try {
                lastFolder.toInt()
                isDigit = true
            } catch (_: NumberFormatException) {
                // ignore
            }
            return if (isDigit) lastFolder else ""
        }

    // E N U M S

    /**
     * Storage types
     */
    enum class StorageType {

        PRIMARY_EMULATED,
        PRIMARY_PHYSICAL,
        SECONDARY
    }

    /**
     * Directory type
     *
     * @author [Bernard Bou](mailto:1313ou@gmail.com)
     */
    enum class DirType {

        AUTO,
        APP_EXTERNAL_SECONDARY,
        APP_EXTERNAL_PRIMARY,
        PUBLIC_EXTERNAL_SECONDARY,
        PUBLIC_EXTERNAL_PRIMARY,
        APP_INTERNAL;

        fun toDisplay(): String {
            return when (this) {
                AUTO -> "auto (internal or adopted)"
                APP_EXTERNAL_SECONDARY -> "secondary"
                APP_EXTERNAL_PRIMARY -> "primary"
                PUBLIC_EXTERNAL_PRIMARY -> "public primary"
                PUBLIC_EXTERNAL_SECONDARY -> "public secondary"
                APP_INTERNAL -> "internal"
            }
        }

        companion object {

            /**
             * Compare (sort by preference)
             *
             * @param type1 type 1
             * @param type2 type 2
             * @return order
             */
            fun compare(type1: DirType, type2: DirType): Int {
                val i1 = type1.ordinal
                val i2 = type2.ordinal
                return i1.compareTo(i2)
            }
        }
    }

    /**
     * Directory with type
     *
     * @author [Bernard Bou](mailto:1313ou@gmail.com)
     */
    class Directory internal constructor(val file: File?, val type: DirType) : Comparable<Directory> {

        val value: CharSequence
            get() = if (DirType.AUTO == type) {
                DirType.AUTO.toString()
            } else file!!.absolutePath

        override fun hashCode(): Int {
            return type.hashCode() * 7 + value.hashCode() * 13
        }

        override fun equals(other: Any?): Boolean {
            return other is Directory && type == other.type
        }

        override fun compareTo(other: Directory): Int {
            val t = DirType.compare(type, other.type)
            return if (t != 0) {
                t
            } else value.toString().compareTo(other.value.toString())
        }
    }
}
