package top.suzhelan.task

import android.content.Context
import androidx.work.WorkManager
import de.robv.android.xposed.XposedBridge
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import top.suzhelan.HookEnv
import top.suzhelan.config.ModuleConfig
import top.suzhelan.entity.LocalModuleInfo
import top.suzhelan.entity.UpdateInfo
import top.suzhelan.net.UpdateApi
import top.suzhelan.util.ToastTool

class CheckUpdateTask(private val context: Context) {

    @OptIn(DelicateCoroutinesApi::class)
    fun execute() {
        //捕获异常
        GlobalScope.launch {
            while (true) {
                val work = WorkRegularly(context)
                work.doWork()
                delay(1000 * 60 * 10)
            }
        }.invokeOnCompletion {
            // 捕获异常
            it?.let {
                XposedBridge.log(it)
            }
        }
    }
}

class WorkRegularly(private val context: Context) {
    fun doWork() {
        performTask()
    }

    private fun performTask() {
        val downloadPath = HookEnv.getPath() + "/apk"
        val currentVersion = if (ModuleConfig.hasModuleInfo()) {
            ModuleConfig.getCurrentModuleInfo().versionCode
        } else {
            0
        }
        val api = UpdateApi()
        val hasUpdate = api.hasUpdate(currentVersion)
        if (hasUpdate.hasUpdate || hasUpdate.isForceUpdate) {
            val updateInfo = api.getUpdateInfo()
            ToastTool.show("正在更新到${updateInfo.versionName}")
            api.download(context, updateInfo, downloadPath) { isSuccess ->
                if (isSuccess) {
                    downloadSuccess(updateInfo, downloadPath)
                    ToastTool.show("下载更新成功,请重启QQ")
                } else {
                    // 下载失败处理
                    ToastTool.show("下载更新失败:${updateInfo.versionName}")
                }
            }
        }
    }

    private fun downloadSuccess(
        updateInfo: UpdateInfo,
        path: String
    ) {
        val moduleInfo = LocalModuleInfo(
            versionCode = updateInfo.versionCode,
            versionName = updateInfo.versionName,
            path = path,
            isRead = false,
            updateLog = updateInfo.updateLog
        )
        ModuleConfig.setCurrentModuleInfo(moduleInfo)
        WorkManager.getInstance(context).cancelAllWork()
    }
}
