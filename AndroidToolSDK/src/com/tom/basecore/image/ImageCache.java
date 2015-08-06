/*
 * Copyright (C) 2012 The Android Open Source Project
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

package com.tom.basecore.image;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.util.Log;

import com.tom.basecore.utlis.AppUtils;
import com.tom.basecore.utlis.LogUtil;
import com.tom.basecore.utlis.OSVersionUtils;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.SoftReference;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashSet;
import java.util.Iterator;

/**
 * 图片缓存器
 */
public class ImageCache {
    private static final String TAG = "ImageCache";
    private static final int DISK_CACHE_INDEX = 0;
    //磁盘缓存类
    private DiskLruCache mDiskLruCache;
    //内存缓存类
    private LruCache<String, BitmapDrawable> mMemoryCache;
    //缓存配置参数
    private ImageCacheBuilder mCacheParams;
    //同步锁
    private final Object mDiskCacheLock = new Object();
    //标识磁盘缓存是否正在启动
    private boolean mDiskCacheStarting = true;
    private HashSet<SoftReference<Bitmap>> mReusableBitmaps;

    /**
     * 根据指定的缓存参数，创建图片缓存
     * @param cacheParams
     */
    private ImageCache(ImageCacheBuilder cacheParams) {
        init(cacheParams);
    }

    /**
     * 根据指定的配置参数，初始化内存缓存
     * @param cacheParams
     */
    private void init(ImageCacheBuilder cacheParams) {
        mCacheParams = cacheParams;

        // 如果配置参数中指定内存缓存可用，则创建内存缓存
        if (mCacheParams.memoryCacheEnabled) {
            LogUtil.d(TAG, "Memory cache created (size = " + mCacheParams.memCacheSize + ")");
            // If we're running on Honeycomb or newer, then
            if (OSVersionUtils.hasHoneycomb()) {
                mReusableBitmaps = new HashSet<SoftReference<Bitmap>>();
            }
            mMemoryCache = new LruCache<String, BitmapDrawable>(mCacheParams.memCacheSize) {
                /**
                 * 通知某一个对象从缓存中移走
                 */
                @Override
                protected void entryRemoved(boolean evicted, String key,
                                            BitmapDrawable oldValue, BitmapDrawable newValue) {
                    if (RecyclingBitmapDrawable.class.isInstance(oldValue)) {
                        ((RecyclingBitmapDrawable) oldValue).setIsCached(false);
                    } else {
                        if (OSVersionUtils.hasHoneycomb()) {
                            mReusableBitmaps.add(new SoftReference<Bitmap>(oldValue.getBitmap()));
                        }
                    }
                }

                @Override
                protected int sizeOf(String key, BitmapDrawable value) {
                    final int bitmapSize = getBitmapSize(value) / 1024;
                    return bitmapSize == 0 ? 1 : bitmapSize;
                }
            };
        }
        //通常磁盘缓存初始化不在这里进行，需要在子线程中初始化，因为涉及到磁盘操作
        if (cacheParams.initDiskCacheOnCreate) {
            initDiskCache();
        }
    }

    /**
     * 初始化磁盘缓存，由于包含访问磁盘操作，所以不要在UI线程中调用，所以通常不会创建缓存的时候初始化磁盘缓存
     */
    public void initDiskCache() {
        synchronized (mDiskCacheLock) {
            if (mDiskLruCache == null || mDiskLruCache.isClosed()) {
                File diskCacheDir = mCacheParams.diskCacheDir;
                if (mCacheParams.diskCacheEnabled && diskCacheDir != null) {
                    if (!diskCacheDir.exists()) {
                        diskCacheDir.mkdirs();
                    }
                    if (AppUtils.getUsableSpace(diskCacheDir) > mCacheParams.diskCacheSize) {
                        try {
                            mDiskLruCache = DiskLruCache.open(
                                    diskCacheDir, 1, 1, mCacheParams.diskCacheSize);
                            LogUtil.d(TAG, "Disk cache initialized");
                        } catch (final IOException e) {
                            LogUtil.e(TAG, "initDiskCache - " + e);
                        }
                    }
                }
            }
            mDiskCacheStarting = false;
            mDiskCacheLock.notifyAll();
        }
    }

    /**
     * 添加图片到内存缓存和磁盘缓存
     * @param data
     *          图片存储唯一标识符
     * @param value
     *          需要存储的BitmapDrawable
     */
    public void addBitmapToCache(String data, BitmapDrawable value) {
        if (data == null || value == null) {
            return;
        }

        // Add to memory cache
        if (mMemoryCache != null) {
            if (RecyclingBitmapDrawable.class.isInstance(value)) {
                ((RecyclingBitmapDrawable) value).setIsCached(true);
            }
            mMemoryCache.put(data, value);
        }

        synchronized (mDiskCacheLock) {
            // Add to disk cache
            if (mDiskLruCache != null) {
                final String key = hashKeyForDisk(data);
                OutputStream out = null;
                try {
                    DiskLruCache.Snapshot snapshot = mDiskLruCache.get(key);
                    if (snapshot == null) {
                        final DiskLruCache.Editor editor = mDiskLruCache.edit(key);
                        if (editor != null) {
                            out = editor.newOutputStream(DISK_CACHE_INDEX);
                            value.getBitmap().compress(
                                    mCacheParams.compressFormat, mCacheParams.compressQuality, out);
                            editor.commit();
                            out.close();
                        }
                    } else {
                        snapshot.getInputStream(DISK_CACHE_INDEX).close();
                    }
                } catch (final IOException e) {
                    Log.e(TAG, "addBitmapToCache - " + e);
                } catch (Exception e) {
                    Log.e(TAG, "addBitmapToCache - " + e);
                } finally {
                    try {
                        if (out != null) {
                            out.close();
                        }
                    } catch (IOException e) {
                    }
                }
            }
        }
    }

    /**
     * 从内存缓存中拿图片
     *
     * @param data
     *          图片存储的唯一标识符
     * @return
     *          返回一个BitmapDrawable,如果没有拿到则返回Null
     */
    public BitmapDrawable getBitmapFromMemCache(String data) {
        BitmapDrawable memValue = null;

        if (mMemoryCache != null) {
            memValue = mMemoryCache.get(data);
        }

        if (memValue != null) {
            LogUtil.d(TAG, "Memory cache hit");
        }

        return memValue;
    }

    /**
     * 从磁盘缓存中拿图片
     *
     * @param data
     *          图片存储的唯一标识符
     * @return
     *          返回一个BitmapDrawable,如果没有拿到则返回Null
     */
    public Bitmap getBitmapFromDiskCache(String data) {
        final String key = hashKeyForDisk(data);
        Bitmap bitmap = null;

        synchronized (mDiskCacheLock) {
            while (mDiskCacheStarting) {
                try {
                    mDiskCacheLock.wait();
                } catch (InterruptedException e) {}
            }
            if (mDiskLruCache != null) {
                InputStream inputStream = null;
                try {
                    final DiskLruCache.Snapshot snapshot = mDiskLruCache.get(key);
                    if (snapshot != null) {
                            LogUtil.d(TAG, "Disk cache hit");
                        inputStream = snapshot.getInputStream(DISK_CACHE_INDEX);
                        if (inputStream != null) {
                            FileDescriptor fd = ((FileInputStream) inputStream).getFD();

                            // Decode bitmap, but we don't want to sample so give
                            // MAX_VALUE as the target dimensions
                            bitmap = ImageResizer.decodeSampledBitmapFromDescriptor(
                                    fd, Integer.MAX_VALUE, Integer.MAX_VALUE, this);
                        }
                    }
                } catch (final IOException e) {
                    Log.e(TAG, "getBitmapFromDiskCache - " + e);
                } finally {
                    try {
                        if (inputStream != null) {
                            inputStream.close();
                        }
                    } catch (IOException e) {}
                }
            }
            return bitmap;
        }
    }

    /**
     * @param options - BitmapFactory.Options with out* options populated
     * @return Bitmap that case be used for inBitmap
     */
    protected Bitmap getBitmapFromReusableSet(BitmapFactory.Options options) {
        Bitmap bitmap = null;

        if (mReusableBitmaps != null && !mReusableBitmaps.isEmpty()) {
            final Iterator<SoftReference<Bitmap>> iterator = mReusableBitmaps.iterator();
            Bitmap item;

            while (iterator.hasNext()) {
                item = iterator.next().get();

                if (null != item && item.isMutable()) {
                    // Check to see it the item can be used for inBitmap
                    if (canUseForInBitmap(item, options)) {
                        bitmap = item;

                        // Remove from reusable set so it can't be used again
                        iterator.remove();
                        break;
                    }
                } else {
                    // Remove from the set if the reference has been cleared.
                    iterator.remove();
                }
            }
        }

        return bitmap;
    }

    /**
     * 清空内存缓存和磁盘缓存，由于此方法包含磁盘访问操作，所以不要在UI线程中调用
     */
    public void clearCache() {
        if (mMemoryCache != null) {
            mMemoryCache.evictAll();
                LogUtil.d(TAG, "Memory cache cleared");
        }

        synchronized (mDiskCacheLock) {
            mDiskCacheStarting = true;
            if (mDiskLruCache != null && !mDiskLruCache.isClosed()) {
                try {
                    mDiskLruCache.delete();
                        LogUtil.d(TAG, "Disk cache cleared");
                } catch (IOException e) {
                    Log.e(TAG, "clearCache - " + e);
                }
                mDiskLruCache = null;
                initDiskCache();
            }
        }
    }

    /**
     * 将内存中没有写入磁盘的数据写入磁盘，此方法包含访问磁盘操作，所以不要在UI线程中调用
     */
    public void flush() {
        synchronized (mDiskCacheLock) {
            if (mDiskLruCache != null) {
                try {
                    mDiskLruCache.flush();
                    LogUtil.d(TAG, "Disk cache flushed");
                } catch (IOException e) {
                    LogUtil.e(TAG, "flush - " + e);
                }
            }
        }
    }

    /**
        关闭磁盘操作，由于包含访问磁盘操作，所以不要在UI线程中调用此方法
     */
    public void close() {
        synchronized (mDiskCacheLock) {
            if (mDiskLruCache != null) {
                try {
                    if (!mDiskLruCache.isClosed()) {
                        mDiskLruCache.close();
                        mDiskLruCache = null;
                            LogUtil.d(TAG, "Disk cache closed");
                    }
                } catch (IOException e) {
                    LogUtil.e(TAG, "close - " + e);
                }
            }
        }
    }

    /**
     * 缓存的配置参数类，需要配置的参数如下：
     * 1、内存缓存大小
     * 2、磁盘缓存大小
     * 3、磁盘缓存目录
     * 4、图片压缩格式和图片压缩率
     * 5、是否启动内存缓存
     * 6、是否启用磁盘缓存
     * 7、是否在创建缓存时初始化磁盘缓存
     */
    public static class ImageCacheBuilder {
        //默认的内存缓存大小
        private static final int DEFAULT_MEM_CACHE_SIZE = 1024 * 5; // 5MB
        //默认的磁盘缓存大小
        private static final int DEFAULT_DISK_CACHE_SIZE = 1024 * 1024 * 10; // 10MB
        //压缩格式
        private static final CompressFormat DEFAULT_COMPRESS_FORMAT = CompressFormat.JPEG;
        //压缩率70%
        private static final int DEFAULT_COMPRESS_QUALITY = 70;
        //是否使用内存缓存
        private static final boolean DEFAULT_MEM_CACHE_ENABLED = true;
        //是否使用磁盘缓存
        private static final boolean DEFAULT_DISK_CACHE_ENABLED = true;
        //磁盘缓存是否在创建就初始化
        private static final boolean DEFAULT_INIT_DISK_CACHE_ON_CREATE = false;

        public int memCacheSize = DEFAULT_MEM_CACHE_SIZE;
        public int diskCacheSize = DEFAULT_DISK_CACHE_SIZE;
        public final File diskCacheDir;
        public CompressFormat compressFormat = DEFAULT_COMPRESS_FORMAT;
        public int compressQuality = DEFAULT_COMPRESS_QUALITY;
        public boolean memoryCacheEnabled = DEFAULT_MEM_CACHE_ENABLED;
        public boolean diskCacheEnabled = DEFAULT_DISK_CACHE_ENABLED;
        public boolean initDiskCacheOnCreate = DEFAULT_INIT_DISK_CACHE_ON_CREATE;

        public ImageCache build() {
            return new ImageCache(this);
        }

        public ImageCacheBuilder setMemCacheSize(int memCacheSize) {
            this.memCacheSize = memCacheSize;
            return this;
        }

        public ImageCacheBuilder setMemCacheSizePercent(float percent) {
            if (percent < 0.05f || percent > 0.8f) {
                throw new IllegalArgumentException("setMemCacheSizePercent - percent must be "
                        + "between 0.05 and 0.8 (inclusive)");
            }
            memCacheSize = Math.round(percent * Runtime.getRuntime().maxMemory() / 1024);
            return this;
        }

        public ImageCacheBuilder setCompressFormat(CompressFormat mCompressFormat) {
            this.compressFormat = mCompressFormat;
            return this;
        }

        public ImageCacheBuilder setDiskCacheSize(int diskCacheSize) {
            this.diskCacheSize = diskCacheSize;
            return this;
        }

        public ImageCacheBuilder setCompressQuality(int compressQuality) {
            this.compressQuality = compressQuality;
            return this;
        }

        public ImageCacheBuilder setMemoryCacheEnable(boolean memoryCacheEnabled) {
            this.memoryCacheEnabled = memoryCacheEnabled;
            return this;
        }

        public ImageCacheBuilder setDiskCacheEnable(boolean diskCacheEnabled) {
            this.diskCacheEnabled = diskCacheEnabled;
            return this;
        }

        public ImageCacheBuilder setInitDiskCacheOnCreate(boolean initDiskCacheOnCreate) {
            this.initDiskCacheOnCreate = initDiskCacheOnCreate;
            return this;
        }

        /**
         * @param context
         *          上下文环境
         * @param diskCacheDirectoryName
         *          磁盘缓存目录
         */
        public ImageCacheBuilder(Context context, String diskCacheDirectoryName) {
            diskCacheDir = getDiskCacheDir(context, diskCacheDirectoryName);
        }

    }

    /**
     * @param candidate - Bitmap to check
     * @param targetOptions - Options that have the out* value populated
     * @return true if <code>candidate</code> can be used for inBitmap re-use with
     *      <code>targetOptions</code>
     */
    private static boolean canUseForInBitmap(
            Bitmap candidate, BitmapFactory.Options targetOptions) {
        int width = targetOptions.outWidth / targetOptions.inSampleSize;
        int height = targetOptions.outHeight / targetOptions.inSampleSize;

        return candidate.getWidth() == width && candidate.getHeight() == height;
    }

    /**
     * 拿到一个磁盘目录，如果磁盘已经挂载或者有内置sd卡，则只用内置sd卡，否则使用 Internal cache dir
     *
     * @param context The context to use
     * @param uniqueName A unique directory name to append to the cache dir
     * @return The cache dir
     */
    public static File getDiskCacheDir(Context context, String uniqueName) {
        final String cachePath= AppUtils.hasExternalStorage() || AppUtils.isExternalStorageRemovable()?
                AppUtils.getExternalCacheDir(context).getPath():context.getCacheDir().getPath();

        return new File(cachePath + File.separator + uniqueName);
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

    /**
     * 计算图片大小
     * @param value
     * @return size in bytes
     */
    @TargetApi(12)
    public static int getBitmapSize(BitmapDrawable value) {
        Bitmap bitmap = value.getBitmap();

        if (OSVersionUtils.hasHoneycombMR1()) {
            return bitmap.getByteCount();
        }
        // Pre HC-MR1
        return bitmap.getRowBytes() * bitmap.getHeight();
    }
}
