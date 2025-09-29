package top.suzhelan.core

import android.R.attr.path
import android.content.Context
import androidx.core.net.toUri
import dalvik.system.DexClassLoader
import de.robv.android.xposed.IXposedHookZygoteInit.StartupParam
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam

import top.linl.qstorycloud.log.QSLog
import top.suzhelan.HookEnv

import java.io.File

/**
 * 模块加载器，真正加载模块apk的地方
 */
class ModuleLoader {
    /**
     * 判断是否有条件加载本地的模块
     */
    private fun hasConditionLoading(path: String): Boolean {
        //模块文件为空
        val moduleApkFile = File(path)
        return moduleApkFile.exists()
    }

    private val isOpenSafeMode: Boolean
        /**
         * 判断是否开启了安全模式
         */
        get() {
            //这里用到了跨进程通讯
            //获取内容提供者
            val contentResolver = HookEnv.getHostAppContext().contentResolver
            //查询是否开启了安全模式,开启了则不加载
            val uri = "content://qstorycloud.suzhelan.top/common/query".toUri()
            val cursor = contentResolver.query(
                uri,
                arrayOf("value"),
                "name=?",
                arrayOf("safe_mode"),
                null
            )
            var openState: String? = "false"
            if (cursor != null && cursor.moveToNext()) {
                openState = cursor.getString(0)
                cursor.close()
            }
            return openState.toBoolean()
        }

    /**
     * 尝试加载模块
     */
    fun readyToLoad(path: String) {
        //打开了安全模式不加载模块
        if (this.isOpenSafeMode) return
        if (hasConditionLoading(path)) {
            //加载模块
            loadModuleAPKAndHook(path)
        }
    }

    /**
     * 加载模块并执行模块的Hook
     *
     * @param pluginApkPath 模块apk路径
     */
    private fun loadModuleAPKAndHook(pluginApkPath: String) {
        val optimizedDirectoryFile = File(HookEnv.getHostAppContext().getDir("qstory_cloud_oat", Context.MODE_PRIVATE).absolutePath)
        // 构建插件的DexClassLoader类加载器，参数：
        // 1、包含dex的apk文件或jar文件的路径，
        // 2、apk、jar解压缩生成dex存储的目录需要是data/data/包名/app_xxx的路径 一般通过context.getDir("dirName", Context.MODE_PRIVATE)获取
        // 3、本地library库目录，一般为null，
        // 4、父ClassLoader， 如果是模块要用XposedBridge.class.getClassLoader(),不能用其他的
        try {
            val dexClassLoader = DexClassLoader(
                pluginApkPath, optimizedDirectoryFile.path,
                null, XposedBridge::class.java.classLoader
            )
            //获取插件的入口类 也就是实现了IXposedHookLoadPackage, IXposedHookZygoteInit的类
            val entryHookClass = dexClassLoader.loadClass("lin.xposed.hook.InitInject")
            val hookInjectInstance: Any = entryHookClass.getConstructor().newInstance()
            //初始化zygote 一定是比handleLoadPackage先调用的
            val initZygoteMethod = entryHookClass.getMethod("initZygote", StartupParam::class.java)
            //反射new实例
            val constructor = StartupParam::class.java.getDeclaredConstructor()
            constructor.isAccessible = true
            val startupParam = constructor.newInstance()
            startupParam.modulePath = pluginApkPath
            startupParam.startsSystemServer = false
            initZygoteMethod.invoke(hookInjectInstance, startupParam)
            //正常的hook初始化流程
            val entryHookMethod =
                entryHookClass.getMethod("handleLoadPackage", LoadPackageParam::class.java)
            entryHookMethod.invoke(hookInjectInstance, HookEnv.getLoadPackageParam())
        } catch (e: Exception) {
            QSLog.e("ModuleLoader", e)
        }
    }
}
