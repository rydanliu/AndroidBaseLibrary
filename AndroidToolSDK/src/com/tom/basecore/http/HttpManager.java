package com.tom.basecore.http;

import android.content.Context;

import com.tom.basecore.http.cache.DiskBasedCache;
import com.tom.basecore.utlis.AppUtils;
import com.tom.basecore.utlis.DebugLog;

import java.io.File;

/**
 * Description:用于管理所有的http请求
 * <pre>
 *     1、默认情况下，所有的http请求使用共同的{@link AsyncHttpClient}对象，除非
 *     在{@link Request}中进行特殊的设置.如设置了重试次数等，具体见{@link Request#isBaseRequest()}
 *     2、如果要启动http缓存功能，那么需要提前调用{@link #initHttpDiskCache(Context)}
 *     3、
 * </pre>
 * User： yuanzeyao.
 * Date： 2015-08-14 16:34
 */
public class HttpManager {
    public static final String TAG="HttpManager";
    public static final int STATUS_CODE_LOCAL=-1;
    public static final int STATUS_CODE_LOCAL_EXPIRED=-2;
    private AsyncHttpClient mHttpClient;
    private static final String mCacheDirName="http";
    //磁盘缓存目录
    private File mHttpCacheDir;
    //磁盘缓存
    private DiskBasedCache mHttpDiskCache;
    //磁盘缓存默认大小
    private static final int HTTP_CACHE_SIZE = 10 * 1024 * 1024; // 10MB
    //磁盘缓存是否已经初始化
    private boolean mHttpDiskCacheInit = false;

    private static class SingtonHolder {
        private static HttpManager mInstance=new HttpManager();
    }

    private HttpManager() {
        mHttpClient = new AsyncHttpClient();
    }

    public static HttpManager getInstance() {
        return SingtonHolder.mInstance;
    }

    /**
     * 执行http网络请求
     * @param mRequest
     * @param mHandler
     * @return
     */
    public RequestHandle performRequest(Request<?> mRequest, ResponseHandlerInterface mHandler) {
        if (mRequest == null) {
            throw new NullPointerException("performRequest:mRequest should not be null!");
        }
        AsyncHttpClient mClient=getHttpClient(mRequest);
        if (mClient == null) {
            throw new NullPointerException("performRequest:mHttpClient should not be null!");
        }
        mRequest.onPrepareRequest(mClient);
        if (mRequest.getMethod().ordinal() == Request.Method.POST.ordinal()) {
            return mClient.post(mRequest.getContext(), mRequest.getUrl(), mRequest.getHeaders()
                    , mRequest.getRequestParams(), mRequest.getContentType(), mHandler);
        } else {
            return mClient.get(mRequest.getContext(), mRequest.getUrl(), mRequest.getHeaders()
                    , mRequest.getRequestParams(), mHandler);
        }
    }

    /**
     * 拿到一个{@link AsyncHttpClient} 如果指定{@link Request}的重试次数和是否修正无响应错误
     * 都等于默认值，那么就是用默认的{@link #mHttpClient},否则自己创建一个
     * @param mRequest
     * @return
     */
    private AsyncHttpClient getHttpClient(Request<?> mRequest) {
        if (mRequest == null) {
            throw new NullPointerException("getHttpClient mRequest is null!");
        }
        if (mRequest.isBaseRequest()) {
            return mHttpClient;
        } else {
            return createHttpClient(mRequest);
        }
    }

    /**
     * 创建一个{@link AsyncHttpClient}
     * @param mRequest
     * @return
     */
    private AsyncHttpClient createHttpClient(Request<?> mRequest) {
        AsyncHttpClient mClient = new AsyncHttpClient(mRequest.getFixNoHttpResponseException());
        mClient.setMaxRetriesAndTimeout(mRequest.getMaxRetry(), mRequest.getConnectionTimeout());
        return mClient;
    }

    public DiskBasedCache getHttpDiskCache(){
        return mHttpDiskCache;
    }

    public boolean isDiskCacheCanUse(){
        return mHttpDiskCache!=null && mHttpDiskCacheInit;
    }

    /**
     * 初始化http请求的磁盘缓存
     * @return
     */
    public void initHttpDiskCache(final Context mContext) {
        new Thread(){
            @Override
            public void run() {
                mHttpCacheDir = AppUtils.getDiskCacheDir(mContext, mCacheDirName);
                if (!mHttpCacheDir.exists()) {
                    mHttpCacheDir.mkdirs();
                }
                DebugLog.d(TAG, "initHttpDiskCache-->" + mHttpCacheDir.getAbsolutePath());
                if (AppUtils.getUsableSpace(mHttpCacheDir) > HTTP_CACHE_SIZE) {
                    try {
                        mHttpDiskCache = new DiskBasedCache(mHttpCacheDir, HTTP_CACHE_SIZE);
                        mHttpDiskCache.initialize();
                        mHttpDiskCacheInit = true;
                        DebugLog.d(TAG, "initHttpDiskCache cache has initialized");
                    } catch (Exception e) {
                        mHttpDiskCache = null;
                    }
                } else {
                    DebugLog.d(TAG, "initHttpDiskCache-->no space" + AppUtils.getUsableSpace(mHttpCacheDir));
                }
            }
        }.start();
    }
}
