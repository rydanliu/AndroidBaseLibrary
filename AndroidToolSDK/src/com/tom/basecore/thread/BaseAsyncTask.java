package com.tom.basecore.thread;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.tom.basecore.utlis.LogUtil;

import java.util.concurrent.FutureTask;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Description:提供安全中断机制的异步任务
 * {@link #onPreExecute()} 此方法可能在UI线程，也可能在子线程，决定因素在于BaseAsyncTask是在哪个线程创建
 * {@link #doInBackground(Object[])} 在{@link #mExecutor} 或者
 * 在{@link #executeOnExecutor(XThreadPoolExecutor, Object[])}指定的线程池中运行
 * {@link #onPostExecute(Object)} 在UI线程运行
 * <pre>
 *     1、该类使用方法类似系统自带AsyncTask,在构建BaseAsyncTask时，可以指定该
 *     异步任务在哪个线程池运行，默认是在“BaseAsyncTask”线程池中运行
 *     2、针对不可中断线程和可中断线程都提供了安全的中断机制
 *          如果异步任务已经支持中断，那么不用指定mCallable，
 *          如果异步任务不支持中断，那么务必在子类的构造函数中初始化mCallable属性，如果使用匿名内部类，
 *          则在{@link #onPreExecute()}设置，不然可能导致无法中断线程无法立即中断
 *     3、异步任务可以在子线程中创建，如果在子线程创建，那么{@link #onPreExecute()}在子线程中调用，
 *        但是onPostExecute一直是在UI线程调用
 *     4、此异步任务只能执行一次，这点和AsyncTask一样
 * </pre>
 * User： yuanzeyao.
 * Date： 2015-07-08 15:45
 */
public abstract class BaseAsyncTask<Params, Progress, Result> {
    public static final String TAG_ = "BaseAsyncTask";
    //任务执行完毕
    private static final int MESSAGE_POST_RESULT = 0x1;
    //跟新进度
    private static final int MESSAGE_POST_PROGRESS = 0x2;
    //正在执行的FuturTask，主要用来取消任务
    private FutureTask<Result> mFuture;
    //对于无法中断的后台任务，需要自己设置一个CallableTask的子类，并实现cancel方法
    protected CallableTask mCallable;
    //该后台任务是否已经执行过
    private final AtomicBoolean mInvoker = new AtomicBoolean();
    //该后台任务是否已经被取消
    protected final AtomicBoolean mCancelled = new AtomicBoolean();
    //将消息分发到UI下线程处理
    private static InnerHandler mHandler = new InnerHandler(Looper.getMainLooper());
    //默认线程池
    private static XThreadPoolExecutor mExecutor= ThreadPoolManager.createPriorityAndDefaultThreadPool();

    /**
     * 任务执行前的回调方法
     * 注意：此回调在启动任务的线程中执行
     *
     * @return 如果返回true, 则 {@link #doInBackground(Object[])}会被调用，否则不会调用
     */
    protected boolean onPreExecute() {
        return true;
    }

    /**
     * 在后台异步执行的回调
     *
     * @param params 异步任务需要使用的参数
     * @return 返回执行结果
     */
    protected abstract Result doInBackground(Params... params);

    /**
     * 用来通知UI线程进度
     *
     * @param values
     */
    protected final void publishProgress(Progress values) {
        if (!mCancelled.get()) {
            mHandler.obtainMessage(MESSAGE_POST_PROGRESS, new AsyncTaskResult<Progress>(this, values)).sendToTarget();
            ;
        }
    }

    protected void onProgressUpdate(Progress values) {

    }


    /**
     * 后台任务执行完毕后的回调
     * 注意：此回调在UI线程中执行
     *
     * @param o
     */
    protected void onPostExecute(Result o) {

    }

    /**
     * 任务被取消的回调
     *
     * @param mResult
     */
    protected void onCancelled(Result mResult) {

    }

    /**
     * 判断当前任务是否已经取消
     *
     * @return
     */
    public boolean isCancelled() {
        return mCancelled.get();
    }

    /**
     * 执行任务
     *
     * @param params
     */
    public void execute(final Params... params) {
        executeOnExecutor(mExecutor,params);
    }

    /**
     * 让任务在指定的线程池上运行
     * @param executor
     * @param params
     */
    public void executeOnExecutor(XThreadPoolExecutor executor, Params... params) {
        if (executor == null) {
            throw new NullPointerException("Cannot execute task: executor is null!");
        }
        if (mInvoker.get()) {
            throw new IllegalStateException("Cannot execute task:"
                    + " the task has already been executed "
                    + "(a task can be executed only once)");
        }
        if (!onPreExecute()) {
            LogUtil.d(TAG_, "onPreExecute return false,so return!!");
            if (mCancelled.get()) {
                mHandler.obtainMessage(MESSAGE_POST_RESULT, new AsyncTaskResult<Result>(BaseAsyncTask.this, null)).sendToTarget();
            }
            return;
        }
        if (mCallable == null) {
            LogUtil.d(TAG_, "mCallable is null,so is possible interruptable task!!");
            mCallable = new CallableTask() {
                @Override
                public void cancel() {

                }
            };
        } else {
            LogUtil.d(TAG_, "mCallable is not null,so is possible noninterruptable task!!");
        }
        mCallable.setParams(params);
        mFuture = (FutureTask<Result>) executor.submit(mCallable);
    }

    /**
     * 取消任务
     *
     * @param mayInterruptIfRunning
     * @return
     */
    public final boolean cancel(boolean mayInterruptIfRunning) {
        if (mFuture != null) {
            mCancelled.set(true);
            return mFuture.cancel(mayInterruptIfRunning);
        }
        return false;
    }


    public static class InnerHandler<Result> extends Handler {

        public InnerHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            AsyncTaskResult result = (AsyncTaskResult) msg.obj;
            switch (msg.what) {
                case MESSAGE_POST_PROGRESS:
                    result.mTask.onProgressUpdate(result.mResult);
                    break;
                case MESSAGE_POST_RESULT:
                    if (result.mTask.mCancelled.get()) {
                        result.mTask.onCancelled(result.mResult);
                    } else {
                        result.mTask.onPostExecute(result.mResult);
                    }
                    break;
            }
        }
    }

    /**
     * 异步任务返回结果的封装
     *
     * @param <Data>
     */
    private static class AsyncTaskResult<Data> {
        public BaseAsyncTask mTask;

        public Data mResult;

        public AsyncTaskResult(BaseAsyncTask mTask, Data mResult) {
            this.mTask = mTask;
            this.mResult = mResult;
        }
    }

    /**
     *支持中断的任务
     */
    public class CallableTask extends XCallable<Result> {
        private Params[] params;

        public final void setParams(Params[] params) {
            this.params = params;
        }

        public void cancel() {

        }

        @Override
        public final Result call() throws Exception {
            mInvoker.set(true);
            Result mResult = null;
            try {
                mResult = doInBackground(params);
            } catch (Exception e) {
                e.printStackTrace();
            }
            mHandler.obtainMessage(MESSAGE_POST_RESULT, new AsyncTaskResult<Result>(BaseAsyncTask.this, mResult)).sendToTarget();
            return mResult;
        }
    }
}
