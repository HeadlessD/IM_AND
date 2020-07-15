package com.qbao.newim.util;

import android.util.Log;

import com.qbao.newim.configure.Configuration;

/**
 * Created by shiyunjie on 17/3/1.
 */

public class Logger
{
    public static int LOG_LEVEL = 0;
    public static final int VERBOSE = 1;
    public static final int DEBUG = 2;
    public static final int INFO = 3;
    public static final int WARNING = 4;
    public static final int ERROR = 5;

    public static void SetLevel(int level)
    {
        LOG_LEVEL = level;
    }

    public static void version(String TAG, String info)
    {
        if (LOG_LEVEL > VERBOSE)
            return;
        Log.v(TAG, getFileLineMethod()+ " " + info);
    }

    public static void debug(String TAG, String info)
    {
        if (LOG_LEVEL > DEBUG)
            return;

        Log.d(TAG, getFileLineMethod()+ " " + info);
    }

    public static void info(String TAG, String info)
    {
        if (LOG_LEVEL > INFO)
            return;
        Log.i(TAG, getFileLineMethod()+ " " + info);
    }

    public static void warning(String TAG, String info)
    {
        if (LOG_LEVEL > WARNING)
            return;

        Log.w(TAG, getFileLineMethod()+ " " + info);
    }

    public static void error(String TAG, String info)
    {
        if (LOG_LEVEL > ERROR)
            return;
        Log.e(TAG, getFileLineMethod()+ " " + info);
    }

    public static String getFileLineMethod()
    {
        StackTraceElement traceElement = ((new Exception()).getStackTrace())[2];
        StringBuffer toStringBuffer = new StringBuffer("[").append(
                traceElement.getFileName()).append(" | ").append(
                traceElement.getLineNumber()).append(" | ").append(
                traceElement.getMethodName()).append("]");
        return toStringBuffer.toString();

    }

    public static String _FILE_()
    {
        StackTraceElement traceElement = ((new Exception()).getStackTrace())[2];
        return traceElement.getFileName();

    }

    public static String _CLASS_()
    {
        StackTraceElement traceElement = ((new Exception()).getStackTrace())[2];
        return traceElement.getClassName();
    }


    public static String _FUNC_()
    {
        StackTraceElement traceElement = ((new Exception()).getStackTrace())[2];
        return traceElement.getMethodName();
    }


    public static int _LINE_()
    {
        StackTraceElement traceElement = ((new Exception()).getStackTrace())[2];
        return traceElement.getLineNumber();
    }

    public static String Log_Tag = "QBIM";

    public static void v(String msg) {
        if(Configuration.DEBUG){
            Log.v(Log_Tag, msg);
        }}
    public static void e(String msg) {

        if(Configuration.DEBUG){
            Log.e(Log_Tag, msg);
        }
    }
    public static void i(String msg) {

        if(Configuration.DEBUG){
            Log.i(Log_Tag, msg);
        }
    }
    public static void w(String msg) {
        if(Configuration.DEBUG){
            Log.w(Log_Tag, msg);
        }
    }
}
