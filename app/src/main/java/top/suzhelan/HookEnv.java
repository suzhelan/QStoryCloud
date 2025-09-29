package top.suzhelan;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import de.robv.android.xposed.callbacks.XC_LoadPackage;

@SuppressLint("StaticFieldLeak")
public class HookEnv {
    //目标包名 如果通用填.+
    private static final String targetPackageName = "com.tencent.mobileqq|com.tencent.tim";
    /**
     * 当前宿主包名
     */
    private static String currentHostAppPackageName;
    /**
     * 当前宿主进程名称
     */
    private static String processName;
    /**
     * 模块路径
     */
    private static String moduleApkPath;
    /**
     * 宿主apk路径
     */
    private static String hostApkPath;
    /**
     * 宿主版本名称
     */
    private static String versionName;
    /**
     * 宿主版本号
     */
    private static int versionCode;
    /**
     * 全局的Context
     */
    private static Context hostAppContext;

    /**
     * 宿主加载参数
     */
    private static XC_LoadPackage.LoadPackageParam loadPackageParam;

    public static XC_LoadPackage.LoadPackageParam getLoadPackageParam() {
        return loadPackageParam;
    }

    public static void setLoadPackageParam(XC_LoadPackage.LoadPackageParam loadPackageParam) {
        HookEnv.loadPackageParam = loadPackageParam;
    }

    public static String getTargetPackageName() {
        return targetPackageName;
    }

    public static void setCurrentHostAppPackageName(String currentHostAppPackageName) {
        HookEnv.currentHostAppPackageName = currentHostAppPackageName;
    }

    public static Context getHostAppContext() {
        return hostAppContext;
    }

    public static void setHostAppContext(Context hostAppContext) {
        HookEnv.hostAppContext = hostAppContext;
        PackageManager packageManager = hostAppContext.getPackageManager();
        try {
            PackageInfo packageInfo = packageManager.getPackageInfo(hostAppContext.getPackageName(), 0);
            setVersionCode(packageInfo.versionCode);
            setVersionName(packageInfo.versionName);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    public static String getCurrentHostAppPackageName() {
        return currentHostAppPackageName;
    }

    public static String getModuleApkPath() {
        return moduleApkPath;
    }

    public static void setModuleApkPath(String moduleApkPath) {
        HookEnv.moduleApkPath = moduleApkPath;
    }

    public static String getHostApkPath() {
        return hostApkPath;
    }

    public static void setHostApkPath(String hostApkPath) {
        HookEnv.hostApkPath = hostApkPath;
    }

    public static String getVersionName() {
        return versionName;
    }

    public static void setVersionName(String versionName) {
        HookEnv.versionName = versionName;
    }

    public static int getVersionCode() {
        return versionCode;
    }

    public static void setVersionCode(int versionCode) {
        HookEnv.versionCode = versionCode;
    }

    public static boolean isMainProcess() {
        return getProcessName().equals("com.tencent.mobileqq") || getProcessName().equals("com.tencent.tim");
    }

    public static String getProcessName() {
        return processName;
    }

    public static void setProcessName(String processName) {
        HookEnv.processName = processName;
    }

    public static String getPath() {
        return hostAppContext.getDataDir().getAbsolutePath() + "/QStoryCloud";
    }
}
