package top.suzhelan.config

import top.linl.qstorycloud.util.SpHelper
import top.suzhelan.entity.LocalModuleInfo
import java.io.File

object ModuleConfig {
    private val sp = SpHelper("LocalModuleInfoConfig")

    private const val CURRENT_UPDATE_INFO_KEY = "currentLocalModuleInfo"

    fun getCurrentModuleInfo(): LocalModuleInfo {
        return sp.getObject(CURRENT_UPDATE_INFO_KEY, LocalModuleInfo::class.java)!!
    }

    fun hasModuleInfo(): Boolean {
        return sp.containKey(CURRENT_UPDATE_INFO_KEY) && File(getCurrentModuleInfo().path).exists()
    }

    fun setCurrentModuleInfo(moduleInfo: LocalModuleInfo) {
        sp.put(CURRENT_UPDATE_INFO_KEY, moduleInfo)
    }
}