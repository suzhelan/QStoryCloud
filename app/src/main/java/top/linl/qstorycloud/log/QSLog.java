package top.linl.qstorycloud.log;

import android.util.Log;

public class QSLog {
    public static int MODE = 0;

    public static void d(String tag, String msg) {
        if (MODE == 0) {
            Log.d(tag, msg);
        }
    }
    public static void e(String tag, Throwable msg) {
        if (MODE == 0) {
            Log.e(tag, Log.getStackTraceString(msg));
        }
    }
    public static void i(String tag, String msg) {
        if (MODE == 0) {
            Log.i(tag, msg);
        }
    }

}
