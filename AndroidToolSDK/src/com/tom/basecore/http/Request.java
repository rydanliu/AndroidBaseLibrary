package com.tom.basecore.http;

import android.text.TextUtils;

import com.tom.basecore.utlis.FileUtils;

import org.apache.http.Header;

/**
 * Description:所有http请求的基类
 * User： yuanzeyao.
 * Date： 2015-08-14 17:23
 */
public class Request<T> {
    public static final String DEFAULT_TAG="DEFAULT_TAG";

    //http 请求的方法
    private final Method mMethod;
    //http 请求的url
    private final String mUrl;
    //http请求是否需要缓存
    private boolean mShouldCache = true;
    //http请求是否取消
    private boolean mCanceled = false;
    //默认没有设置用户代理
    private String mUserAgent="";
    //上网代理的ip或者dns
    private String mProxy_ip="";
    //上网代理的端口号
    private int mProxy_port=-1;
    //http请求的优先级
    private Priority mPriority=Priority.NORMAL;
    //该http请求携带参数
    private RequestParams mParams=null;
    //该请求携带的头信息
    private Header[] mHeaders=null;
    //是否修正无响应的错误  see https://code.google.com/p/android/issues/detail?id=13117
    private boolean fixNoHttpResponseException=false;
    //连接超时时间
    private int mConnectionTimeout = AsyncHttpClient.DEFAULT_SOCKET_TIMEOUT;
    //设置Socket超时时间
    private int mSocketTimeout = AsyncHttpClient.DEFAULT_SOCKET_TIMEOUT;
    //最大的重试次数
    private int maxRetry=AsyncHttpClient.DEFAULT_MAX_RETRIES;
    //任务指定的回调接口
    private final ResponseHandlerInterface mHandler;
    //http请求的标识符，主要用来根据某一个tag进行取消操作
    private String mTag=DEFAULT_TAG;

    public Request(Method mMethod, String mUrl,ResponseHandlerInterface mHandler) {
        this.mMethod = mMethod;
        this.mUrl=Utils.notNull(mUrl,"mUrl");
        this.mHandler = Utils.notNull(mHandler, "client");
        this.mHandler.setRequest(this);
    }

    /**
     * 拿到当前http请求方法 {@link Method}.
     */
    public Method getMethod() {
        return mMethod;
    }

    public String getUrl(){
        return mUrl;
    }

    /**
     * 为改请求设置一个tag，当需要根据某一个tag取消http请求时会用到
     *
     * @return
     */
    public Request<?> setTag(String tag) {
        if(!TextUtils.isEmpty(tag)){
            mTag = tag;
        }
        return this;
    }

    /**
     * 拿到当前请求的tag，用于根据tag取消http请求
     * @see Request#setTag(String)
     */
    public String getTag() {
        return mTag;
    }

    /**
     * 设置是否需要缓存
     * @param shouldCache
     * @return
     */
    public Request<?> setShouldCache(boolean shouldCache) {
        this.mShouldCache = shouldCache;
        return this;
    }

    /**
     * 是否需要使用缓存
     * @return
     */
    public final boolean shouldCache() {
        return mShouldCache;
    }

    /**
     * 设置请求需要使用的参数
     * @param mParams
     */
    public void setRequestParams(RequestParams mParams){
        this.mParams=mParams;
    }

    /**
     * 返回该http请求需要携带参数，默认是null,如果某个http请求需要携带
     * 头信息，改写此方法即可
     * @return
     */
    public RequestParams getRequestParams() {
        return mParams;
    }

    /**
     * 设置本次请求需要添加的头信息
     * @param mHeaders
     */
    public void setHeaders(Header[] mHeaders){
        this.mHeaders=mHeaders;
    }
    /**
     * 返回该http请求需要携带的头信息，默认是null,如果某个http请求需要携带
     * 头信息，改写此方法即可
     * @return
     */
    protected Header[] getHeaders() {
        return null;
    }

    /**
     * 获取http请求的优先级
     * @see {@link com.tom.basecore.http.Request.Priority}
     * @return
     */
    public Priority getPriority(){
        return Priority.NORMAL;
    }

    /**
     * 设置http请求的优先级
     * @param mPriority
     * @see {@link com.tom.basecore.http.Request.Priority}
     */
    public void setPriority(Priority mPriority){
        this.mPriority=mPriority;
    }

    /**
     * 设置连接超时时间
     * @param mTimeOut
     */
    public void setConnectionTimeout(int mTimeOut){
        this.mConnectionTimeout=mTimeOut;
    }

    /**
     * 获取连接超时时间
     * @return
     */
    public int getConnectionTimeout(){
        return mConnectionTimeout;
    }

    /**
     * 设置响应超时时间/Socket超时时间
     * @param mTimeOut
     */
    public void setSocketTimeout(int mTimeOut){
        this.mSocketTimeout=mTimeOut;
    }

    /**
     * 获取当前请求的Socket超时时间
     * @return
     */
    public int getSocketTimeout(){
        return this.mSocketTimeout;
    }

    /**
     * 设置用户代理
     * @param mUserAgent
     */
    public void setUserAgent(String mUserAgent){
        this.mUserAgent=mUserAgent;
    }

    public String getContentType() {
        return "application/x-www-form-urlencoded; charset=UTF-8";
    }

    /**
     * 设置是否修正无响应错误
     * @param fixNoHttpResponseException
     */
    public void setFixNoHttpResponseException(boolean fixNoHttpResponseException){
        this.fixNoHttpResponseException=fixNoHttpResponseException;
    }

    /**
     * 获取是否修正无响应错误
     * @return
     */
    public boolean getFixNoHttpResponseException(){
        return this.fixNoHttpResponseException;
    }

    /**
     * 设置最大重试次数
     * @param mMaxRetry
     */
    public void setMaxRetry(int mMaxRetry){
        this.maxRetry=mMaxRetry;
    }

    /**
     * 获取最大的重试次数
     * @return
     */
    public int getMaxRetry(){
        return maxRetry;
    }

    /**
     * 取消此http请求
     */
    public void cancel() {
        mCanceled = true;
    }

    /**
     * 此http请求是否已经取消
     */
    public boolean isCanceled() {
        return mCanceled;
    }

    /**
     * 获取http请求的缓存key
     * @return
     */
    public String getCacheKey(){
        if(!TextUtils.isEmpty(mUrl)){
            return FileUtils.hashKeyForDisk(mUrl);
        }
        return "";
    }

    /**
     * 获取任务回调接口
     * @return
     */
    public ResponseHandlerInterface getResponseHandler(){
        return mHandler;
    }

    /**
     * 为httpClient设置一些配置信息，如用户代理，超时时间
     * @param mHttpClient
     */
    public void onPrepareRequest(AsyncHttpClient mHttpClient) {
        if(mHttpClient==null)
            return;
        if(!TextUtils.isEmpty(mUserAgent)){
            mHttpClient.setUserAgent(mUserAgent);
        }
        if (mConnectionTimeout != AsyncHttpClient.DEFAULT_SOCKET_TIMEOUT) {
            mHttpClient.setConnectTimeout(mConnectionTimeout);
        }
        if(mSocketTimeout!=AsyncHttpClient.DEFAULT_SOCKET_TIMEOUT){
            mHttpClient.setResponseTimeout(mSocketTimeout);
        }
        if(!TextUtils.isEmpty(mProxy_ip) && mProxy_port!=-1){
            mHttpClient.setProxy(mProxy_ip,mProxy_port);
        }

    }

    /**
     * 判断是否需要重新定制{@link AsyncHttpClient}
     * @return
     */
    public boolean isBaseRequest(){
        if(maxRetry ==AsyncHttpClient.DEFAULT_MAX_RETRIES
                && !fixNoHttpResponseException){
            return true;
        }else {
            return false;
        }
    }

    /**
     * http 请求的优先级
     */
    public static enum Priority {
        LOW,
        NORMAL,
        HIGH,
        IMMEDIATE
    }

    /**
     * 支持的http请求方式
     */
    public static enum Method {
        GET,
        POST;
    }
}
