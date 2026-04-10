package com.example.odidoplayer

import android.util.Base64
import org.json.JSONArray
import org.json.JSONObject

object ClearKeyUtils {
    private fun hexToBase64Url(hex: String): String {
        val bytes = hex.chunked(2).map { it.toInt(16).toByte() }.toByteArray()
        return Base64.encodeToString(bytes, Base64.URL_SAFE or Base64.NO_PADDING or Base64.NO_WRAP)
    }

    fun createClearKeyJson(kidHex: String, keyHex: String): String {
        val keyObject = JSONObject().apply {
            put("kty", "oct")
            put("kid", hexToBase64Url(kidHex))
            put("k", hexToBase64Url(keyHex))
        }
        return JSONObject().apply {
            put("keys", JSONArray().put(keyObject))
            put("type", "temporary")
        }.toString()
    }
}
