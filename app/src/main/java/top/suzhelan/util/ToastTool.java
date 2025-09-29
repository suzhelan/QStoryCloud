package top.suzhelan.util;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import top.suzhelan.HookEnv;


public class ToastTool {
    public static void show(Object content) {
        new Handler(Looper.getMainLooper()).post(() -> {
            Context activity = HookEnv.getHostAppContext();
            Toast.makeText(activity, String.valueOf(content), Toast.LENGTH_LONG).show();
        });
    }

}
