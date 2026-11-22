package com.example.azimuth_mobile.data.remote

import android.net.Uri

object BackendConfig {
    const val ORIGIN = "https://azimuth-5rph.onrender.com"
    const val API_BASE_URL = "$ORIGIN/api/v1/"

    fun resolveAssetUrl(path: String?): String? {
        if (path.isNullOrBlank()) return null
        if (path.startsWith("https://") || path.startsWith("http://")) return path

        val builder = Uri.parse(ORIGIN).buildUpon()
        path.trimStart('/').split('/').forEach { segment ->
            builder.appendPath(segment)
        }
        return builder.build().toString()
    }
}
