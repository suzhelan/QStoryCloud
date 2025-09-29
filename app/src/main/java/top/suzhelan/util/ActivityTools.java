package top.suzhelan.util;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.view.View;
import android.view.ViewGroup;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import top.suzhelan.HookEnv;
import top.suzhelan.qstorycloud.R;


public class ActivityTools {

    /**
     * 获取该activity所有view
     */

    public static List<View> getAllChildViews(Activity activity) {
        View view = activity.getWindow().getDecorView();
        return getAllChildViews(view);

    }

    public static List<View> getAllChildViews(View view) {
        List<View> allChildren = new ArrayList<>();
        if (view instanceof ViewGroup vp) {
            for (int i = 0; i < vp.getChildCount(); i++) {
                View views = vp.getChildAt(i);
                allChildren.add(views);
                //递归调用
                allChildren.addAll(getAllChildViews(views));
            }
        }
        return allChildren;
    }
    /*
     * 注入Res资源到上下文
     */
    public static void injectResourcesToContext(Context context) {
        Resources resources = context.getResources();
        try {
            //如果能获取到自己的资源说明是自己的Activity或已经注入过了
            resources.getString(R.string.app_name);
        } catch (Exception e) {
            try {
                AssetManager assetManager = resources.getAssets();
                Method method = AssetManager.class.getDeclaredMethod("addAssetPath", String.class);
                method.invoke(assetManager, HookEnv.getModuleApkPath());
                //再次尝试读自己的资源
                resources.getString(R.string.app_name);
            } catch (Exception ex) {
            }
        }
    }

    /*
     * 结束进程
     */
    public static void killAppProcess(Context context) {
        //注意：不能先杀掉主进程，否则逻辑代码无法继续执行，需先杀掉相关进程最后杀掉主进程
        ActivityManager mActivityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> mList = mActivityManager.getRunningAppProcesses();
        for (ActivityManager.RunningAppProcessInfo runningAppProcessInfo : mList) {
            if (runningAppProcessInfo.pid != android.os.Process.myPid()) {
                android.os.Process.killProcess(runningAppProcessInfo.pid);
            }
        }
        android.os.Process.killProcess(android.os.Process.myPid());
        System.exit(0);
    }

    public static void exitAPP(Context context) {
        ActivityManager activityManager = (ActivityManager) context.getApplicationContext().getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.AppTask> appTaskList = activityManager.getAppTasks();
        for (ActivityManager.AppTask appTask : appTaskList) {
            appTask.finishAndRemoveTask();
        }
    }
}
