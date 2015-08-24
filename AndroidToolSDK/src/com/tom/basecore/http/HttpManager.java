package com.tom.basecore.http;

import android.content.Context;
import android.text.TextUtils;

import com.tom.basecore.http.cache.DiskBasedCache;
import com.tom.basecore.utlis.AppUtils;
import com.tom.basecore.utlis.DebugLog;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.WeakHashMap;

/**
 * Description:用于管理所有的http请求
 * <pre>
 *     1、默认情况下，所有的http请求使用共同的{@link AsyncHttpClient}对象，除非
 *     在{@link Request}中进行特殊的设置.如设置了重试次数等，具体见{@link Request#isBaseRequest()}
 *     2、如果要启动http缓存功能，那么需要提前调用{@link #initHttpDiskCache(Context)}
 * </pre>
 * User： yuanzeyao.
 * Date： 2015-08-14 16:34
 */
public class HttpManager {
    public static final String TAG="HttpManager";
    /**标识 使用没有过期的缓存的状态码 */
    public static final int STATUS_CODE_LOCAL=-1;
    /**标识 使用过期的缓存的状态码 */
    public static final int STATUS_CODE_LOCAL_EXPIRED=-2;
    private AsyncHttpClient mHttpClient;
    /**磁盘缓存文件夹*/
    private static final String mCacheDirName="http";
    /**磁盘缓存目录*/
    private File mHttpCacheDir;
    /**磁盘缓存*/
    private DiskBasedCache mHttpDiskCache;
    /**磁盘缓存默认大小*/
    private static final int HTTP_CACHE_SIZE = 10 * 1024 * 1024; // 10MB
    /**磁盘缓存是否已经初始化*/
    private boolean mHttpDiskCacheInit = false;
    /**存放当前正在进行的相同的http请求*/
    private final Map<String, Queue<Request<?>>> mWaitingRequests =
            new HashMap<String, Queue<Request<?>>>();
    /**存放没有完成的http请求*/
    private final Map<String, List<RequestHandle>> requestMap=
            Collections.synchronizedMap(new WeakHashMap<String, List<RequestHandle>>());

    /**单例实现*/
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
     *
     * @param mRequest
     * @return
     */
    public void performRequest(Request<?> mRequest) {
         performRequest(mRequest, true);
    }

    private void performRequest(Request<?> mRequest,boolean checkRepet){
        if (mRequest == null) {
            throw new NullPointerException("performRequest:mRequest should not be null!");
        }
        DebugLog.d(TAG,"Request key:"+mRequest.getCacheKey());
        if (mRequest.shouldCache() && checkRepet) {
            synchronized (mWaitingRequests) {
                String cacheKey = mRequest.getCacheKey();
                if (mWaitingRequests.containsKey(cacheKey)) {
                    // There is already a request in flight. Queue up.
                    Queue<Request<?>> stagedRequests = mWaitingRequests.get(cacheKey);
                    if (stagedRequests == null) {
                        stagedRequests = new LinkedList<Request<?>>();
                    }
                    stagedRequests.add(mRequest);
                    mWaitingRequests.put(cacheKey, stagedRequests);
                    DebugLog.d(TAG,"Request for cacheKey=%s is in flight, putting on hold.", cacheKey);
                    return ;
                } else {
                    // Insert 'null' queue for this cacheKey, indicating there is now a request in
                    // flight.
                    mWaitingRequests.put(cacheKey, null);
                }
            }
        }
        AsyncHttpClient mClient = getHttpClient(mRequest);
        if (mClient == null) {
            throw new NullPointerException("performRequest:mHttpClient should not be null!");
        }
        mRequest.onPrepareRequest(mClient);
        RequestHandle mHandler=null;
        if (mRequest.getMethod().ordinal() == Request.Method.POST.ordinal()) {
            mHandler=mClient.post(mRequest);
        } else {
            mHandler=mClient.get(mRequest);
        }
        updateRequestMap(mRequest,mHandler);
    }

    /**
     * 跟新正在进行的http请求列表
     * @param mRequest
     * @param mHandler
     */
    private void updateRequestMap(Request<?> mRequest, RequestHandle mHandler) {
        if (mRequest == null || mHandler == null) {
            return;
        }
        List<RequestHandle> requestList;
        // Add request to request map
        synchronized (requestMap) {
            requestList = requestMap.get(mRequest.getTag());
            if (requestList == null) {
                requestList = Collections.synchronizedList(new LinkedList<RequestHandle>());
                requestMap.put(mRequest.getTag(), requestList);
            }
        }
        requestList.add(mHandler);
        Iterator<RequestHandle> iterator = requestList.iterator();
        while (iterator.hasNext()) {
            if (iterator.next().shouldBeGarbageCollected()) {
                iterator.remove();
            }
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
     * 注意：在某些手机上需要：<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
     * @return
     */
    public void initHttpDiskCache(final Context mContext) {
        new Thread(){
            @Override
            public void run() {
                mHttpCacheDir = AppUtils.getDiskCacheDir(mContext, mCacheDirName);
                DebugLog.d(TAG, "initHttpDiskCache-->" + mHttpCacheDir.getAbsolutePath());
                if (!mHttpCacheDir.exists()) {
                    DebugLog.d(TAG,"mHttpCacheDir is not exist, create it!!");
                    if(mHttpCacheDir.mkdirs()){
                        DebugLog.d(TAG,"mHttpCacheDir create Success!!");
                    }
                }
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
                    DebugLog.d(TAG, "initHttpDiskCache-->space not enough:" + AppUtils.getUsableSpace(mHttpCacheDir));
                }
            }
        }.start();
    }

    /**
     * 通过制定的{@link Request}已经完成
     * @param mRequest
     */
    public void notifyFinish(Request<?> mRequest){
        if (mRequest.shouldCache()) {
            synchronized (mWaitingRequests) {
                String cacheKey = mRequest.getCacheKey();
                Queue<Request<?>> waitingRequests = mWaitingRequests.remove(cacheKey);
                if (waitingRequests != null) {
                        DebugLog.d(TAG,"Releasing %d waiting requests for cacheKey=%s.",
                                waitingRequests.size(), cacheKey);
                    for(Request item : waitingRequests){
                        if(!item.isCanceled()){
                            performRequest(mRequest,false);
                        }
                    }
                }
            }
        }
    }

    /**
     * 取消指定列表中的Http请求任务
     * @param requestList
     * @param mayInterruptIfRunning
     */
    public void cancelRequests(final List<RequestHandle> requestList, final boolean mayInterruptIfRunning) {
        if (requestList != null) {
            for (RequestHandle requestHandle : requestList) {
                requestHandle.cancel(mayInterruptIfRunning);
            }
        }
    }

    /**
     * 取消当前正在执行或者没有开始执行的http请求任务
     * @param mayInterruptIfRunning
     */
    public void cancelAllRequests(boolean mayInterruptIfRunning) {
        for (List<RequestHandle> requestList : requestMap.values()) {
            if (requestList != null) {
                for (RequestHandle requestHandle : requestList) {
                    requestHandle.cancel(mayInterruptIfRunning);
                }
            }
        }
        requestMap.clear();
        synchronized (mWaitingRequests){
            mWaitingRequests.clear();
        }
    }

    /**
     * 根据指定的tag，取消正在执行或者没有执行的http请求任务
     * @param tag
     * @param mayInterruptIfRunning
     */
    public void cancelRequestByTag(String tag, boolean mayInterruptIfRunning) {
        if (TextUtils.isEmpty(tag)) {
            return;
        }
        List<RequestHandle> requestList = requestMap.get(tag);
        if (requestList != null) {
            cancelRequests(requestList, mayInterruptIfRunning);
        }

        synchronized (mWaitingRequests) {
            for (Queue<Request<?>> requests : mWaitingRequests.values()) {
                if (requests != null) {
                    for (Request request : requests) {
                        if (request != null && request.getTag().equals(tag)) {
                            request.cancel();
                        }
                    }
                }
            }
        }
    }
}
