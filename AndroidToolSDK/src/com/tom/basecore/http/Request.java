package com.tom.basecore.http;

import android.content.Context;
import android.text.TextUtils;

import org.apache.http.Header;

/**
 * Description:所有http请求的基类
 * User： yuanzeyao.
 * Date： 2015-08-14 17:23
 */
public abstract class Request<T> {

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


    //请求对应的上下文
    private Context mContext;
    //http请求的标识符，主要用来根据某一个tag进行取消操作
    private Object mTag=null;

    public Request(Context mContext,Method mMethod, String mUrl) {
        this.mContext=mContext;
        this.mMethod = mMethod;
        this.mUrl = mUrl;
    }

    /**
     * 拿到当前http请求方法 {@link Method}.
     */
    public Method getMethod() {
        return mMethod;
    }

    public Context getContext(){
        return mContext;
    }

    public String getUrl(){
        return mUrl;
    }

    /**
     * 为改请求设置一个tag，当需要根据某一个tag取消http请求时会用到
     *
     * @return
     */
    public Request<?> setTag(Object tag) {
        mTag = tag;
        return this;
    }

    /**
     * 拿到当前请求的tag，用于根据tag取消http请求
     * @see Request#setTag(Object)
     */
    public Object getTag() {
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

    public void setMaxRetry(int mMaxRetry){
        this.maxRetry=mMaxRetry;
    }

    public int getMaxRetry(){
        return maxRetry;
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
    public enum Priority {
        LOW,
        NORMAL,
        HIGH,
        IMMEDIATE
    }

    /**
     * 支持的http请求方式
     */
    public enum Method {
        GET,
        POST;
    }
}
