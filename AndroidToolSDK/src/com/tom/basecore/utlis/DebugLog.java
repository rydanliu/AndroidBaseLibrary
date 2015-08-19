/*
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tom.basecore.utlis;

import android.util.Log;

import com.tom.basecore.BuildConfig;

import java.util.Locale;

/**
 * Description:日志工具类
 * User： yuanzeyao.
 * Date： 2015-07-02 18:43
 */
public class DebugLog {
    public static boolean DEBUG = BuildConfig.DEBUG;


    public static void log(String tag,String message){
        if(DEBUG){
            Log.d(tag,message);
        }
    }
    public static void v(String tag,String format, Object... args) {
        if (DEBUG) {
            Log.v(tag, buildMessage(format, args));
        }
    }

    public static void d(String tag,String format, Object... args) {
        if(DEBUG){
            Log.d(tag, buildMessage(format, args));
        }
    }

    public static void e(String tag,String format, Object... args) {
        if (DEBUG) {
            Log.e(tag, buildMessage(format, args));
        }
    }

    public static void e(String tag,Throwable tr, String format, Object... args) {
        if(DEBUG){
            Log.e(tag, buildMessage(format, args), tr);
        }
    }

    public static void wtf(String tag,String format, Object... args) {
        if(DEBUG){
            Log.wtf(tag, buildMessage(format, args));
        }

    }

    public static void wtf(String tag,Throwable tr, String format, Object... args) {
        if(DEBUG){
            Log.wtf(tag, buildMessage(format, args), tr);
        }
    }

    /**
     * Formats the caller's provided message and prepends useful info like
     * calling thread ID and method name.
     */
    private static String buildMessage(String format, Object... args) {
        String msg = (args == null) ? format : String.format(Locale.US, format, args);
        StackTraceElement[] trace = new Throwable().fillInStackTrace().getStackTrace();

        String caller = "<unknown>";
        // Walk up the stack looking for the first caller outside of DebugLog.
        // It will be at least two frames up, so start there.
        for (int i = 2; i < trace.length; i++) {
            Class<?> clazz = trace[i].getClass();
            if (!clazz.equals(DebugLog.class)) {
                String callingClass = trace[i].getClassName();
                callingClass = callingClass.substring(callingClass.lastIndexOf('.') + 1);
                callingClass = callingClass.substring(callingClass.lastIndexOf('$') + 1);

                caller = callingClass + "." + trace[i].getMethodName();
                break;
            }
        }
        return String.format(Locale.US, "[%d] %s: %s",
                Thread.currentThread().getId(), caller, msg);
    }
}
