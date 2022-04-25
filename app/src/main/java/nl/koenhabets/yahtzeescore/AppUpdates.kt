package nl.koenhabets.yahtzeescore

import android.content.Context
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class AppUpdates(var context: Context) {
    var versionInfo: JSONObject
    fun getInfo(): JSONObject {
        var jsonObject = JSONObject()
        try {
            val url = URL("https://koenhabets.nl/yahtzee-update-info.json")
            val con = url.openConnection() as HttpURLConnection
            con.requestMethod = "GET"
            val `in` = BufferedReader(
                InputStreamReader(con.inputStream)
            )
            var inputLine: String?
            val content = StringBuilder()
            while (`in`.readLine().also { inputLine = it } != null) {
                content.append(inputLine)
            }
            `in`.close()
            con.disconnect()
            jsonObject = JSONObject(content.toString())
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return jsonObject
    }

    fun getVersionText(): String? {
        try {
            val pInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            val verCode = pInfo.versionCode
            if (versionInfo.getInt("flexibleVersion") > verCode) {
                var updateText = ""
                if (versionInfo.has("updateText")) {
                    updateText = versionInfo.getString("updateText")
                }
                return updateText
            }
            val versions = versionInfo.getJSONArray("versions")
            for (i in 0 until versions.length()) {
                val version = versions.getJSONObject(i)
                if (version.getInt("version") == verCode) {
                    return version.getString("updateText")
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    init {
        versionInfo = getInfo()
    }
}