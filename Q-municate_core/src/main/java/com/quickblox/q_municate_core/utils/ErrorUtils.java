package com.quickblox.q_municate_core.utils;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

public class ErrorUtils {

    public static void showError(Context context, Exception e) {
        String errorMsg = !TextUtils.isEmpty(e.getMessage()) ? e.getMessage() : ConstsCore.EMPTY_STRING;
        Toast.makeText(context, errorMsg, Toast.LENGTH_LONG).show();
        e.printStackTrace();
    }

    public static void showError(Context context, String error) {
        Toast.makeText(context, error, Toast.LENGTH_LONG).show();
    }

    public static Toast getErrorToast(Context context, String error) {
        return Toast.makeText(context, error, Toast.LENGTH_LONG);
    }

    public static void logError(String tag, Exception e) {
        String errorMsg = !TextUtils.isEmpty(e.getMessage()) ? e.getMessage() : ConstsCore.EMPTY_STRING;
        Log.e(tag, errorMsg, e);
    }

    public static void logError(String tag, String msg) {
        String errorMsg = !TextUtils.isEmpty(msg) ? msg : ConstsCore.EMPTY_STRING;
        Log.e(tag, errorMsg);
    }

    public static void logError(Exception e) {
        e.printStackTrace();
    }
}