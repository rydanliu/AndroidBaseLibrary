package com.tom.basecore.utlis;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;

import java.io.File;
import java.io.FileFilter;
import java.util.regex.Pattern;

/**
 * Description:和app相关的工具类
 * User： yuanzeyao.
 * Date： 2015-08-06 13:02
 */
public class AppUtils {

    /**
     * 获取CPU的核的数量
     * @return
     *        返回CPU核的个数
     */
    public static int getNumCores() {
        try {
            //Get directory containing CPU info
            File dir = new File("/sys/devices/system/cpu/");
            //Filter to only list the devices we care about
            File[] files = dir.listFiles(new FileFilter() {

                @Override
                public boolean accept(File pathname) {
                    //Check if filename is "cpu", followed by a single digit number
                    if (Pattern.matches("cpu[0-9]", pathname.getName())) {
                        return true;
                    }
                    return false;
                }

            });
            //Return the number of cores (virtual CPU devices)
            return files.length;
        } catch (Exception e) {
            e.printStackTrace();
            return 1;
        }
    }

    /**
     * 处理HttpURLConnection在2.2版本之前的一个Bug
     * http://android-developers.blogspot.com/2011/09/androids-http-clients.html
     */
    public static void disableConnectionReuseIfNecessary() {
        // HTTP connection reuse which was buggy pre-froyo
        if (hasHttpConnectionBug()) {
            System.setProperty("http.keepAlive", "false");
        }
    }

    /**
     * 判断HttpURLConnection在当前系统上是否有bug
     * http://android-developers.blogspot.com/2011/09/androids-http-clients.html
     *
     * @return
     */
    public static boolean hasHttpConnectionBug() {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.FROYO;
    }


    /**
     * 拿到sd卡的缓存目录，如果当前系统提供了{@link Context#getExternalCacheDir()}方法，那么调用此方法
     * 如果没有提供，则自己构建
     * @param context
     * @return
     */
    public static File getExternalCacheDir(Context context) {
        if (hasExternalCacheDir()) {
            return context.getExternalCacheDir();
        }
        // Before Froyo we need to construct the external cache dir ourselves
        final String cacheDir = "/Android/data/" + context.getPackageName() + "/cache/";
        return new File(Environment.getExternalStorageDirectory().getPath() + cacheDir);
    }

    /**
     * 判断当前系统是否有内置sd卡缓存目录的方法(2.2 以及以后才有)
     * @return
     */
    public static boolean hasExternalCacheDir() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO;
    }

    /**
     *判断sd卡是内置还是外置
     * @return True if external storage is removable (like an SD card), false
     * otherwise.
     */
    public static boolean isExternalStorageRemovable() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
            return Environment.isExternalStorageRemovable();
        }
        return true;
    }

    /**
     * 计算给定路径可用空间
     *
     * @param path The path to check
     * @return The space available in bytes
     */
    @SuppressWarnings("deprecation")
    @SuppressLint("NewApi")
    public static long getUsableSpace(File path) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
            return path.getUsableSpace();
        }
        final StatFs stats = new StatFs(path.getPath());
        return (long) stats.getBlockSize() * (long) stats.getAvailableBlocks();
    }

    /**
     * 判断手机是否有外置存储空间
     *
     * @return
     */
    public static boolean hasExternalStorage() {
        return Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState());
    }

    /**
     * 清除应用程序缓存
     *
     * @param context
     * @param filepath
     */
    public static void cleanApplicationData(Context context, String... filepath) {
        cleanInternalCache(context);
        cleanExternalCache(context);
        cleanDatabases(context);
        cleanSharedPreference(context);
        cleanFiles(context);
        for (String file : filepath) {
            FileUtils.deleteFile(new File(file));
        }
    }

    /**
     * Clean cache directory
     *
     * @param context
     */
    public static void cleanInternalCache(Context context) {
        FileUtils.deleteFile(context.getCacheDir());
    }

    /**
     * Clean database directory
     *
     * @param context
     */
    public static void cleanDatabases(Context context) {
        String[] databases = context.databaseList();
        if (databases != null) {
            for (String database : databases) {
                context.deleteDatabase(database);
            }
        }
    }

    /**
     * Delete database directory
     *
     * @param context
     */
    public static void deleteDatabasesDir(Context context) {
        FileUtils.deleteFile(new File(context.getApplicationInfo().dataDir + "/databases"));
    }

    /**
     * Clean shared_prefs directory
     *
     * @param context
     */
    public static void cleanSharedPreference(Context context) {
        FileUtils.deleteFile(new File(context.getApplicationInfo().dataDir + "/shared_prefs"));
    }

    /**
     * Delete database by name.
     *
     * @param context
     * @param dbName
     */
    public static void cleanDatabaseByName(Context context, String dbName) {
        context.deleteDatabase(dbName);
    }


    /**
     * Clean files directory
     *
     * @param context
     */
    public static void cleanFiles(Context context) {
        FileUtils.deleteFile(context.getFilesDir());
    }

    /**
     * Clean external cache directory.
     *
     * @param context
     */
    @SuppressLint("NewApi")
    public static void cleanExternalCache(Context context) {
        if (Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED)) {
            FileUtils.deleteFile(context.getExternalCacheDir());
        }
    }
}
