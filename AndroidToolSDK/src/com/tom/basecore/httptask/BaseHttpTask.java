package com.tom.basecore.httptask;

import android.content.Context;
import android.text.TextUtils;

import com.tom.basecore.http.AsyncHttpClient;
import com.tom.basecore.http.AsyncHttpResponseHandler;
import com.tom.basecore.http.RequestHandle;
import com.tom.basecore.http.RequestParams;
import com.tom.basecore.thread.XThreadPoolExecutor;
import com.tom.basecore.utlis.LogUtil;

import org.apache.http.Header;

/**
 * Description:所有http请求的基类
 * User： yuanzeyao.
 * Date： 2015-08-12 10:45
 */
public abstract class BaseHttpTask {

    public static final String TAG = "BaseHttpTask";

    public static final int TIME_OUT = 10 * 1000;//默认超时时间是10秒

    private AsyncHttpClient mHttpClient;

    private RequestHandle mRequestHandler;

    public BaseHttpTask() {
        mHttpClient = new AsyncHttpClient();
    }

    public BaseHttpTask(XThreadPoolExecutor mExecutor) {
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
     * 如果要为http请求设置代理，那么返回代理字符串即可
     * 注意：返回字符串格式：　"hostname:port"
     *
     * @return
     */
    protected String getProxy() {
        return null;
    }

    /**
     * 主要用于{@link com.tom.basecore.httptask.BaseHttpTask.HTTP_METHOD#POST}
     * 表明你发送的数据类型
     */
    protected String getContentType() {
        return null;
    }

    /**
     * http请求方式
     */
    protected enum HTTP_METHOD {
        GET,
        POST
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
        LogUtil.d(TAG, "BaseHttpTask onPreExecute");
        if (mHttpClient == null) {
            mHttpClient = new AsyncHttpClient();
        }

        if (TextUtils.isEmpty(getUrl(mContext, params))) {
            throw new NullPointerException("BaseHttpTask getUrl can't return null!");
        }

        if (getMethod() == null) {
            throw new NullPointerException("BaseHttpTask getMethod can't return null Object!");
        }
        //可以加一些更多的前置判断
        return true;
    }

    public final boolean execute(Context mContext, AsyncHttpResponseHandler mHandler, Object... params) {

        //进行网络请求前的准备工作
        if (!onPreExecute(mHttpClient, mContext, params)) {
            LogUtil.d(TAG, "BaseHttpTask execute fail for onPreExecute return false!");
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
        if (mRequestHandler != null)
            mRequestHandler.cancel(mayInterruptIfRunning);
    }

    public void setPriority(int mPriority) {
        if (mHttpClient != null) {
            LogUtil.d("yzy","BaseHttpTask set Priority:"+mPriority);
            mHttpClient.setPriority(mPriority);
        }
    }


}
