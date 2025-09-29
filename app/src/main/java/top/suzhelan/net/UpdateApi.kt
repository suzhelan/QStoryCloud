package top.suzhelan.net

import android.content.Context
import com.alibaba.fastjson2.JSON
import de.robv.android.xposed.XposedBridge
import okhttp3.Headers
import okhttp3.OkHttpClient
import okhttp3.Request
import top.suzhelan.entity.HasUpdate
import top.suzhelan.entity.UpdateInfo

class UpdateApi {
    private val androidUserAgent: String =
        "Mozilla/5.0 (Linux; Android 10; K) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/121.0.0.0 Mobile Safari/537.36"


    private val headers = Headers.Builder()
        .add("User-Agent", androidUserAgent)
        .add("Accept", "*/*")
        .add("Connection", "keep-alive")
        .build()

    private val checkUrl = "https://qstory.suzhelan.top/update/hasUpdate"
    private val downloadUrl = "https://qstory.suzhelan.top/update/download"
    private val updateLogUrl = "https://qstory.suzhelan.top/update/getUpdateLog"

    fun hasUpdate(currentVersion: Int): HasUpdate {
        val client = OkHttpClient().newBuilder()
            .build()
        val request: Request = Request.Builder()
            .url("$checkUrl?version=$currentVersion")
            .headers(headers)
            .get()
            .build()
        val response = client.newCall(request).execute()
        val result = JSON.parseObject(response.body.string())
        response.close()
        val data = result.getJSONObject("data")
        return JSON.parseObject(data.toString(), HasUpdate::class.java)
    }

    fun getUpdateInfo(): UpdateInfo {
        val client = OkHttpClient().newBuilder()
            .build()
        val request: Request = Request.Builder()
            .url("$updateLogUrl?version=0")
            .headers(headers)
            .get()
            .build()
        val response = client.newCall(request).execute()
        val result = JSON.parseObject(response.body.string())
        response.close()
        val data = result.getJSONArray("data")
        val updateInfoList = JSON.parseArray(data.toString(), UpdateInfo::class.java)
        return updateInfoList.first()
    }

    fun download(
        context: Context,
        updateInfo: UpdateInfo,
        path: String,
        onRequest: (Boolean) -> Unit
    ) {
        val task = DownloadTask(context, updateInfo)
        task.download(downloadUrl, path, onRequest)
    }
}