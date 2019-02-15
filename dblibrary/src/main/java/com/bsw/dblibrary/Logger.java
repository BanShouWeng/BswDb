package com.bsw.dblibrary;

import android.text.TextUtils;
import android.util.Log;

import java.io.PrintWriter;
import java.io.StringWriter;


/**
 * 日志
 *
 * @author bsw
 */
public class Logger {
    private final int ERROR = 56;
    private final int DEBUG = 57;
    private final int INFO = 58;

    public <T extends Exception> void e(String tag, T exception) {
        StringWriter stackTrace = new StringWriter();
        exception.printStackTrace(new PrintWriter(stackTrace));
        String logContent = stackTrace.toString();
        printDefault(ERROR, tag, logContent);
    }

    public void e(String tag, String message) {
        printDefault(ERROR, tag, message);
    }

    public void e(String message) {
        printDefault(ERROR, "Logger", message);
    }

    public void i(String tag, String message) {
        printDefault(INFO, tag, message);
    }

    public void i(String message) {
        printDefault(INFO, "Logger", message);
    }

    public void d(String tag, String message) {
        printDefault(DEBUG, "Logger", message);
    }

    private void printDefault(int type, String tag, String msg) {
        if (TextUtils.isEmpty(tag)) {
            tag = "Logger";
        }
        int index = 0;
        int maxLength = 4000;
        int countOfSub = msg.length() / maxLength;

        if (countOfSub > 0) {  // The log is so long
            for (int i = 0; i < countOfSub; i++) {
                String sub = msg.substring(index, index + maxLength);
                printSub(type, tag, sub);
                index += maxLength;
            }
        } else {
            printSub(type, tag, msg);
        }
    }

    private void printSub(int type, String tag, String sub) {
        if (tag == null) {
            tag = "Logger";
        }
        Log.e(tag, sub);
    }
}
