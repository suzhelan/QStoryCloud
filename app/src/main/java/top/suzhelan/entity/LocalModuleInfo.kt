package top.suzhelan.entity

/**
 * 本地数据模型
 */
data class LocalModuleInfo(
    val versionCode: Int,
    val versionName: String,
    val path: String,
    val isRead: Boolean,
    val updateLog: String
)