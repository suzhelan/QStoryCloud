package top.suzhelan.net

import android.app.ActivityManager
import android.app.ActivityManager.RunningAppProcessInfo
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import androidx.core.app.NotificationCompat
import okhttp3.OkHttpClient
import okhttp3.Request
import top.suzhelan.util.ActivityTools
import top.suzhelan.entity.UpdateInfo
import top.suzhelan.qstorycloud.R
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.DecimalFormat

class DownloadTask(private val context: Context,private val updateInfo: UpdateInfo) {
    private val notificationFlag = (System.currentTimeMillis() / 2).toInt()
    private lateinit var builder: NotificationCompat.Builder
    private lateinit var notificationManager: NotificationManager

    init {
        initializeNotification()
    }

    private fun isAppForeground(context: Context): Boolean {
        val activityManager =
            context.getSystemService(Service.ACTIVITY_SERVICE) as ActivityManager
        val runningAppProcessInfoList =
            activityManager.runningAppProcesses
        if (runningAppProcessInfoList == null) {
            return false
        }

        for (processInfo in runningAppProcessInfoList) {
            if (processInfo.processName == context.packageName
                && (processInfo.importance ==
                        RunningAppProcessInfo.IMPORTANCE_FOREGROUND)
            ) {
                return true
            }
        }
        return false
    }

    private fun initializeNotification() {
        // 创建一个通知频道 NotificationChannel
        val channel =
            NotificationChannel(CHANNEL_ID, "QStoryCloud", NotificationManager.IMPORTANCE_DEFAULT)
        //桌面小红点
        channel.enableLights(false)
        //通知显示
        channel.lockscreenVisibility = Notification.VISIBILITY_PUBLIC
        notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    private fun sendProgressNotification(size: Int) {
        builder = NotificationCompat.Builder(context, CHANNEL_ID)
        ActivityTools.injectResourcesToContext(context)
        var contentText =
            "下载中 大小" + getNetFileSizeDescription(size.toLong()) + " 切换QQ到后台可查看具体进度"
        if (size == 0) {
            contentText = "准备开始下载"
        }
        builder.setContentTitle("QStory正在云更新到" + updateInfo.versionName) //设置标题
            .setSmallIcon(R.mipmap.ic_launcher_round) //设置小图标
            .setPriority(NotificationCompat.PRIORITY_MAX) //设置通知的优先级
            .setWhen(System.currentTimeMillis())
            .setAutoCancel(false) //设置通知被点击一次不自动取消
            .setOngoing(true)
            .setSound(null)
            .setContentText(contentText) //设置内容;
        notificationManager.notify(notificationFlag, builder.build())
    }

    private fun updateNotification(max: Int, progress: Int) {
        //qq在前台时通知被查看后会自动消失 因此只让qq在后台时通知进度
        //应用在前台不更新进度
        if (isAppForeground(context)) return

        if (progress >= 0) {
            builder.setContentText(
                "进度:" + getNetFileSizeDescription(progress.toLong()) + "/" + getNetFileSizeDescription(
                    max.toLong()
                )
            )
            builder.setProgress(max, progress, false)
        }
        if (progress == max) {
            builder.setContentText("下载完成")
            builder.setAutoCancel(true)
            builder.setOngoing(false)
        }
        notificationManager.notify(notificationFlag, builder.build())
    }

    private fun sendDownloadSuccessNotification() {
        val builder: NotificationCompat.Builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle("QStory已更新到" + updateInfo.versionName) //设置标题
            .setSmallIcon(R.mipmap.ic_launcher_round) //设置小图标
            .setPriority(NotificationCompat.PRIORITY_MAX) //设置通知的优先级
            .setAutoCancel(false) //设置通知被点击一次不自动取消
            .setOngoing(true)
            .setContentText("请手动重启QQ,模块将会生效")
        //设置内容

        notificationManager.notify(notificationFlag + 0xFF, builder.build())
    }

    private fun getNetFileSizeDescription(size: Long): String {
        val bytes = StringBuffer()
        val format = DecimalFormat("###.0")
        if (size >= 1024 * 1024 * 1024) {
            val i = (size / (1024.0 * 1024.0 * 1024.0))
            bytes.append(format.format(i)).append("GB")
        } else if (size >= 1024 * 1024) {
            val i = (size / (1024.0 * 1024.0))
            bytes.append(format.format(i)).append("MB")
        } else if (size >= 1024) {
            val i = (size / (1024.0))
            bytes.append(format.format(i)).append("KB")
        } else {
            if (size <= 0) {
                bytes.append("0B")
            } else {
                bytes.append(size.toInt()).append("B")
            }
        }
        return bytes.toString()
    }


    /**
     * 真正被外部调用的下载方法
     *
     * @param url  下载链接
     * @param path 路径
     */
    @Throws(IOException::class)
    fun download(url: String, path: String, onRequest: (Boolean) -> Unit) {
        val downloadPath = File(path)
        if (!downloadPath.getParentFile()!!.exists()) {
            downloadPath.getParentFile()!!.mkdirs()
        }
        if (downloadPath.exists()) {
            downloadPath.delete()
        }
        if (!downloadPath.exists()) {
            downloadPath.createNewFile()
        }
        sendProgressNotification(0)

        val client = OkHttpClient.Builder().build()
        val request = Request.Builder()
            .url(url)
            .addHeader("User-Agent", "Android")
            .addHeader("Accept", "*/*")
            .addHeader("Connection", "keep-alive")
            .get()
            .build()
        val call = client.newCall(request)
        call.execute().use { response ->
            BufferedInputStream(response.body.byteStream()).use { bufIn ->
                BufferedOutputStream(
                    FileOutputStream(downloadPath)
                ).use { bufOut ->
                    //总字节数
                    val size = response.body.contentLength()
                    sendProgressNotification(size.toInt())
                    //发送通知
                    var downloadSize: Long = 0
                    var len: Int
                    val buf = ByteArray(2048) //2k
                    while ((bufIn.read(buf).also { len = it }) != -1) {
                        bufOut.write(buf, 0, len)
                        downloadSize += len.toLong()
                        updateNotification(size.toInt(), downloadSize.toInt())
                    }
                    bufOut.flush()
                }
            }
        }
        onRequest(true)
        sendDownloadSuccessNotification()
    }

    companion object {
        private const val CHANNEL_ID = "QStoryCloud"
    }
}
