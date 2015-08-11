package com.tom.basecore.thread;


import com.tom.basecore.utlis.LogUtil;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.RunnableFuture;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Description:支持任务优先级和可及时中断任务的线程池
 * 注意：提交到线程池的Runnable或者Callable需要继承自{@link ICancelableTask}
 * User： yuanzeyao.
 * Date： 2015-07-09 13:44
 */
public class XThreadPoolExecutor extends ThreadPoolExecutor {
    public static final String TAG="BaseAsyncTask";
    public XThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue, ThreadFactory threadFactory) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory);
    }

    @Override
    protected <T> RunnableFuture<T> newTaskFor(Callable<T> callable) {

        if (callable instanceof ICancelableTask) {
            LogUtil.d(TAG, "Callable->newTaskFor ICancelableTask!!");
            return ((ICancelableTask) callable).newTaskFor();
        }
        LogUtil.d(TAG, "Runnable->newTaskFor normal!!");
        return super.newTaskFor(callable);
    }

    @Override
    protected <T> RunnableFuture<T> newTaskFor(Runnable runnable, T value) {
        if (runnable instanceof ICancelableTask) {
            LogUtil.d(TAG, "Runnable->newTaskFor ICancelableTask!!");
            return ((ICancelableTask) runnable).newTaskFor(value);
        }
        LogUtil.d(TAG, "Runnable->newTaskFor normal!!");
        return super.newTaskFor(runnable, value);
    }


}
