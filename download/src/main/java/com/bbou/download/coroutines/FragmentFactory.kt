package com.bbou.download.coroutines

import com.bbou.download.preference.Settings

/**
 * Fragment factory
 *
 * @param downloader downloader
 * @return fragment that matches downloader
 */
fun toFragment(downloader: Settings.Downloader): DownloadBaseFragment {
    return when (downloader) {
        Settings.Downloader.DOWNLOAD -> DownloadFragment()
        Settings.Downloader.DOWNLOAD_ZIP -> DownloadZipFragment()
    }
}
