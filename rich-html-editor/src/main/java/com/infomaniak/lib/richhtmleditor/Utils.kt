package com.infomaniak.lib.richhtmleditor

import android.content.Context
import java.io.BufferedReader

internal fun Context.readAsset(fileName: String): String {
    return assets
        .open(fileName)
        .bufferedReader()
        .use(BufferedReader::readText)
}
