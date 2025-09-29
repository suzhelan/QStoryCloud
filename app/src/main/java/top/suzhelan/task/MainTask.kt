package top.suzhelan.task

import android.content.Context
import top.suzhelan.HookEnv
import top.suzhelan.config.ModuleConfig
import top.suzhelan.core.ModuleLoader

/**
 * 加载流程
 * 1.检查本地是否有合适的版本和文件加载
 * 2.检查更新和初始化
 * 3.下载更新和提示重启
 */
class MainTask(
    private val context: Context
) {
    fun load() {
        //首先进行初始化以及加载
        if (ModuleConfig.hasModuleInfo()) {
            val moduleInfo = ModuleConfig.getCurrentModuleInfo()
            val loader = ModuleLoader()
            loader.readyToLoad(moduleInfo.path)
        } else {
            //如果没有更新 则进入初始化流程
        }
        if (HookEnv.isMainProcess()) {
            //检查更新任务
            val checkUpdateTask = CheckUpdateTask(context)
            checkUpdateTask.execute()
        }
    }
}