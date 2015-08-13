package com.tom.basecore.httptask;

import android.content.Context;
import android.text.TextUtils;

import com.tom.basecore.http.AsyncHttpClient;
import com.tom.basecore.http.AsyncHttpResponseHandler;
import com.tom.basecore.http.PersistentCookieStore;
import com.tom.basecore.http.RequestHandle;
import com.tom.basecore.http.RequestParams;
import com.tom.basecore.thread.XThreadPoolExecutor;
import com.tom.basecore.utlis.LogUtil;

import org.apache.http.Header;
import org.apache.http.client.CookieStore;

/**
 * Description:所有http请求的基类
 * <pre>
 *     1、使用setXXX or enableXXX方法设置的属性，建议改写子类的{@link #onPreExecute(AsyncHttpClient, Context, Object...)}方法，并在此方法中调用setXXX
 *        比如设置代理，设置优先级，设置CookieStore,设置是否可以缓存
 *     2、对于所有的getXXX方法，会自动被调用，如果getXXX返回的数据不满足http请求，那么直接在子类中改写此方法，并返回需要的数据即可，如超时时间，请求参数等等
 *     3、该异步请求默认使用的线程池是{@link AsyncHttpClient#defaultThreadPool},如果需要将http请求指定到其他线程池那么使用
 *     {@link #BaseHttpRequest(XThreadPoolExecutor)}构造方法，并传入相应的线程池
 * </pre>
 * User： yuanzeyao.
 * Date： 2015-08-12 10:45
 */
public abstract class BaseHttpRequest {

    public static final String TAG = "BaseHttpRequest";

    public static final int MIN_TIME_OUT = 1000;//最小超时时间

    private AsyncHttpClient mHttpClient;

    private RequestHandle mRequestHandler;
    //默认支持缓存
    private boolean enableCache = true;

    public BaseHttpRequest() {
        mHttpClient = new AsyncHttpClient();
    }

    public BaseHttpRequest(XThreadPoolExecutor mExecutor) {
        mHttpClient = new AsyncHttpClient();
        if (mExecutor != null && !mExecutor.isShutdown()) {
            mHttpClient.setThreadPool(mExecutor);
        }
    }

    /**
     * 默认使用GET请求，如果需要使用POST请求，改写此方法即可
     *
     * @return
     */
    protected HTTP_METHOD getMethod() {
        return HTTP_METHOD.GET;
    }

    /**
     * 如果要修改超时时间，在子类改写此方法，返回需要设置到超时时间
     * 如果返回值小于1000，则设置无效
     *
     * @return
     */
    protected int getTimeOut() {
        return 0;
    }

    protected abstract String getUrl(Context mContext, Object... params);

    /**
     * 默认返回空的RequestParams，如果有一些参数你需要拼接到{@link #getUrl(Context, Object...)}
     * 可以将这些参数已RequestParams形式返回
     *
     * @return
     * @see RequestParams
     */
    protected RequestParams getRequestParams() {
        return null;
    }

    /**
     * 如果需要给http请求设置head，返回要设置的head信息返回即可
     *
     * @return
     * @see Header
     */
    protected Header[] getHeaders() {
        return null;
    }

    /**
     * 如果需要给http请求设置UserAgent，返回需要设置的UserAgent即可
     *
     * @return
     */
    protected String getUserAgent() {
        return null;
    }

    /**
     * 主要用于{@link BaseHttpRequest.HTTP_METHOD#POST}
     * 表明你发送的数据类型
     */
    protected String getContentType() {
        return null;
    }

    /**
     * 在进行网络请求时，进行一些设置或者前置判断，可以在子类中改写此方法对httpClient
     * 进行某些设置，如超时时间，设置UserAgent等等
     * 注意：在改写此方法时，需要先调用<code>super.onPreExecute(AsyncHttpClient mHttpClient,Context mContext,Object...params)</code>
     *
     * @param mHttpClient
     * @param mContext
     * @param params
     * @return 返回false, 则表示网络请求停止，返回true,表示开始网络请求
     */
    protected boolean onPreExecute(AsyncHttpClient mHttpClient, Context mContext, Object... params) {
        LogUtil.d(TAG, "BaseHttpRequest onPreExecute");
        if (mHttpClient == null) {
            mHttpClient = new AsyncHttpClient();
        }

        if (TextUtils.isEmpty(getUrl(mContext, params))) {
            throw new NullPointerException("BaseHttpRequest getUrl can't return null!");
        }

        if (getMethod() == null) {
            throw new NullPointerException("BaseHttpRequest getMethod can't return null Object!");
        }
        //设置超时时间
        if (getTimeOut() >= MIN_TIME_OUT) {
            mHttpClient.setTimeout(getTimeOut());
        }
        //设置用户代理
        if (TextUtils.isEmpty(getUserAgent())) {
            mHttpClient.setUserAgent(getUserAgent());
        }
        return true;
    }

    public final boolean execute(Context mContext, AsyncHttpResponseHandler mHandler, Object... params) {

        //进行网络请求前的准备工作
        if (!onPreExecute(mHttpClient, mContext, params)) {
            LogUtil.d(TAG, "BaseHttpRequest execute fail for onPreExecute return false!");
            return false;
        }

        if (getMethod().ordinal() == HTTP_METHOD.GET.ordinal()) {
            mRequestHandler = mHttpClient.get(mContext, getUrl(mContext, params), getHeaders(), getRequestParams(), mHandler);
        } else if (getMethod().ordinal() == HTTP_METHOD.POST.ordinal()) {
            mRequestHandler = mHttpClient.post(mContext, getUrl(mContext, params), getHeaders(), getRequestParams(), getContentType(), mHandler);
        }
        return true;
    }

    /**
     * 取消http请求
     *
     * @param mayInterruptIfRunning
     */
    public void cancel(final boolean mayInterruptIfRunning) {
        if (mRequestHandler != null) {
            mRequestHandler.cancel(mayInterruptIfRunning);
            mRequestHandler = null;
        }
    }

    /**
     * 设置http请求的优先级
     *
     * @param mPriority
     */
    public void setPriority(int mPriority) {
        if (mHttpClient != null) {
            mHttpClient.setPriority(mPriority);
        }
    }

    /**
     * 设置CookieStore，通常使用{@link PersistentCookieStore}
     *
     * @param cookieStore
     */
    public void setCookieStore(CookieStore cookieStore) {
        if (mHttpClient != null)
            mHttpClient.setCookieStore(cookieStore);
    }

    /**
     * 设置代理
     *
     * @param proxy 主机名  ip地址或者dns
     * @param port  端口号
     */
    public void setProxy(String proxy, int port) {
        if (mHttpClient != null)
            mHttpClient.setProxy(proxy, port);
    }

    /**
     * 该请求是否允许使用缓存
     *
     * @param enableCache
     */
    public void enableCache(boolean enableCache) {
        this.enableCache = enableCache;
    }

    /**
     * http请求方式
     */
    protected enum HTTP_METHOD {
        GET,
        POST
    }
}
