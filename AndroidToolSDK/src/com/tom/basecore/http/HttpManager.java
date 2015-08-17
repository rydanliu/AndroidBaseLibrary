package com.tom.basecore.http;

/**
 * Description:用于管理所有的http请求
 * User： yuanzeyao.
 * Date： 2015-08-14 16:34
 */
public class HttpManager {
    private AsyncHttpClient mHttpClient;

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


}
