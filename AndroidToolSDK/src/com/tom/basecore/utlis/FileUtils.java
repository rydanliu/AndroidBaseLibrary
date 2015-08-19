package com.tom.basecore.utlis;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.text.format.Formatter;

import java.io.File;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Description:和文件操作相关的工具类
 * User： yuanzeyao.
 * Date： 2015-08-06 13:01
 */
public class FileUtils {
    /**
     * Delete file(include not empty directory)
     *
     * @param file
     */
    public static void deleteFile(File file) {
        if (file.exists()) {
            if (file.isDirectory()) {
                File[] files = file.listFiles();
                if (file != null) {
                    for (File file2 : files) {
                        deleteFile(file2);
                    }
                }

            }
            file.delete();
        }
    }

    /**
     * Parse a content uri to a file.
     * Some file manager return Uri like "file:///sdcard/test.mp4",
     * In this case Uri.getPath() get the file path in file system,
     * so can create a file object with this path, if this file is exists,
     * means parse file success.
     * Some file manager such as Gallery, return Uri like "content://video/8323",
     * In this case Uri.getPath() can't get file path in file system,
     * but can user ContentResolver to get file path from media database.
     *
     * @param uri
     * @return
     */
    public static File parseUriToFile(Context context, Uri uri) {
        if (uri == null) {
            return null;
        }
        File file = null;
        String path = uri.getPath();
        file = new File(path); //If this file is exists, means parse file success.
        if (!file.exists()) {
            //Use ContentResolver to get file path from media database.
            ContentResolver cr = context.getContentResolver();
            String[] pro = new String[]{MediaStore.MediaColumns.DATA,};
            Cursor cursor = cr.query(uri, pro, null, null, null);
            if (cursor != null) {
                String[] cs = cursor.getColumnNames();
                for (String string : cs) {
                    System.out.println(string);
                }
                if (cursor.moveToFirst()) {
                    int index = cursor.getColumnIndex(MediaStore.MediaColumns.DATA);
                    path = cursor.getString(index);
                    if (null != path && !"".equals(path)) {
                        file = new File(path);
                        if (!file.exists()) {
                            file = null;
                        }
                    }
                }
                cursor.close();
            }
        }
        return file;
    }

    /**
     * Get file size(include directory sub files)
     *
     * @param file
     * @return
     */
    public static long getFileSize(File file) {
        long size = 0L;
        if (file.exists()) {
            size += file.length();
            if (file.isDirectory()) {
                File[] files = file.listFiles();
                if(files!=null)
                {
                    for (File file2 : files) {
                        size += getFileSize(file2);
                    }
                }
            }
        }
        return size;
    }

    /**
     * Change byte to KB/MB/GB...（keep two float point）
     *
     * @param context
     * @param size
     * @return
     */
    public static String formatByte(Context context, long size) {
        return Formatter.formatFileSize(context, size);// Change byte to KB or MB, etc.
    }

    /**
     * Change byte to KB/MB/GB...(Keep Integer)
     *
     * @param size
     * @return
     */
    public static String formatByteFixed(long size) {
        if (size <= 0) return "0B";
        if (size < 1024) return size + "B";
        else size = size / 1024;
        if (size < 1024) return size + "KB";
        else size = size / 1024;
        if (size < 1024) return size + "MB";
        else size = size / 1024;
        if (size < 1024) return size + "GB";
        else size = size / 1024;
        if (size < 1024) return size + "TB";
        else size = size / 1024;
        if (size < 1024) return size + "PB";
        else size = size / 1024;
        if (size < 1024) return size + "EB";
        else size = size / 1024;
        if (size < 1024) return size + "ZB";
        else size = size / 1024;
        if (size < 1024) return size + "YB";
        else size = size / 1024;
        if (size < 1024) return size + "NB";
        else size = size / 1024;
        if (size < 1024) return size + "DB";
        else size = size / 1024;
        return size + "CB";
    }

    /**
     * 对图片url进行md5编码，作为缓存的key
     */
    public static String hashKeyForDisk(String key) {
        String cacheKey;
        try {
            final MessageDigest mDigest = MessageDigest.getInstance("MD5");
            mDigest.update(key.getBytes());
            cacheKey = bytesToHexString(mDigest.digest());
        } catch (NoSuchAlgorithmException e) {
            cacheKey = String.valueOf(key.hashCode());
        }
        return cacheKey;
    }

    private static String bytesToHexString(byte[] bytes) {
        // http://stackoverflow.com/questions/332079
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < bytes.length; i++) {
            String hex = Integer.toHexString(0xFF & bytes[i]);
            if (hex.length() == 1) {
                sb.append('0');
            }
            sb.append(hex);
        }
        return sb.toString();
    }
}
