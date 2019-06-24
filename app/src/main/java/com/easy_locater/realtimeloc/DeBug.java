package com.easy_locater.realtimeloc;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;


public class DeBug {
    /**
     * This Class is just to display log messages
     *
     * @param TAG--to see the log under this particular Tag in Info
     * @param MSG--Message you want to print.
     */
    static boolean toShowLog = BuildConfig.DEBUG;

    public static void showLog(String TAG, String MSG) {
        try {
            if (TAG != null)
                if (toShowLog)
                    Log.i(TAG, MSG + " " + "Thread: " + Thread.currentThread().getName());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void largeLog(String tag, String content) {
        try {
            if (content.length() > 4000) {
                showLog(tag, content.substring(0, 4000));
                largeLog(tag, content.substring(4000));
            } else {
                showLog(tag, content);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void showToast(Context context, String MSG) {
        if (MSG != null)
            if (toShowLog)
                Toast.makeText(context, MSG, Toast.LENGTH_SHORT).show();
    }



}
