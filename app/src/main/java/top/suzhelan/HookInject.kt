package top.suzhelan

import android.content.Context
import android.content.ContextWrapper
import android.util.Log
import com.alibaba.fastjson2.JSON.config
import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.IXposedHookZygoteInit
import de.robv.android.xposed.IXposedHookZygoteInit.StartupParam
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam
import top.suzhelan.util.ActivityTools
import top.linl.qstorycloud.util.SpHelper
import top.suzhelan.task.MainTask
import java.lang.reflect.Method
import java.util.concurrent.atomic.AtomicBoolean

class HookInject : IXposedHookLoadPackage, IXposedHookZygoteInit {

    @Throws(Throwable::class)
    override fun initZygote(startupParam: StartupParam) {
        HookEnv.setModuleApkPath(startupParam.modulePath)
    }

    private fun initAppContext(applicationContext: Context) {
        //获取和设置全局上下文和类加载器
        HookEnv.setHostAppContext(applicationContext) //context
        HookEnv.setHostApkPath(applicationContext.applicationInfo.sourceDir) //apk path
        ActivityTools.injectResourcesToContext(applicationContext)
        //初始化mmkv和自定义路径
        val dataDir = HookEnv.getPath()+"/config"
        SpHelper.initialize(dataDir)
        val mainTask = MainTask(applicationContext)
        mainTask.load()
    }

    @Throws(Throwable::class)
    override fun handleLoadPackage(loadPackageParam: LoadPackageParam) {
        val packageName = loadPackageParam.packageName
        if (!loadPackageParam.isFirstApplication) return
        if (!packageName.matches(HookEnv.getTargetPackageName().toRegex())) return
        //设置当前应用包名
        HookEnv.setCurrentHostAppPackageName(packageName)
        //设置进程名 方便在判断当前运行的是否主进程
        HookEnv.setProcessName(loadPackageParam.processName)
        HookEnv.setLoadPackageParam(loadPackageParam)
        XposedBridge.hookMethod(
            getAppContextInitMethod(loadPackageParam),
            object : XC_MethodHook() {
                override fun afterHookedMethod(param: MethodHookParam) {
                    if (IS_INJECT.getAndSet(true)) return
                    val wrapper = param.thisObject as ContextWrapper
                    initAppContext(wrapper.baseContext)
                }
            })
    }

    /**
     * 获取application的onCreate方法
     */
    private fun getAppContextInitMethod(loadParam: LoadPackageParam): Method {
        try {
            if (loadParam.appInfo.name != null) {
                val clz = loadParam.classLoader.loadClass(loadParam.appInfo.name)
                return try {
                    clz.getDeclaredMethod("attachBaseContext", Context::class.java)
                } catch (i: Throwable) {
                    try {
                        clz.getDeclaredMethod("onCreate")
                    } catch (e: Throwable) {
                        try {
                            clz.getSuperclass()
                                .getDeclaredMethod("attachBaseContext", Context::class.java)
                        } catch (m: Throwable) {
                            clz.getSuperclass().getDeclaredMethod("onCreate")
                        }
                    }
                }
            }
        } catch (o: Throwable) {
            XposedBridge.log("[error]" + Log.getStackTraceString(o))
        }
        try {
            return ContextWrapper::class.java.getDeclaredMethod(
                "attachBaseContext",
                Context::class.java
            )
        } catch (u: Throwable) {
            XposedBridge.log("[error]" + Log.getStackTraceString(u))
            return ContextWrapper::class.java.getDeclaredMethod("onCreate")
        }
    }

    companion object {
        private val IS_INJECT = AtomicBoolean()
    }
}