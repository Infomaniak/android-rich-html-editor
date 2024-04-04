package com.infomaniak.library.htmlricheditor

import android.content.Context
import java.io.BufferedReader

fun Context.readAsset(fileName: String): String {
    return assets
        .open(fileName)
        .bufferedReader()
        .use(BufferedReader::readText)
}
