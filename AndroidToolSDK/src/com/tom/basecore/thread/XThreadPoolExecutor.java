package com.tom.basecore.thread;


import com.tom.basecore.utlis.DebugLog;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.RunnableFuture;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Description:支持任务优先级和可及时中断任务的线程池
 * 注意：提交到线程池的Runnable或者Callable需要继承自{@link CancelableCallable}
 * User： yuanzeyao.
 * Date： 2015-07-09 13:44
 */
public class XThreadPoolExecutor extends ThreadPoolExecutor {
    public static final String TAG="XThreadPoolExecutor";
    public XThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue, ThreadFactory threadFactory) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory);
    }

    public XThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue, ThreadFactory threadFactory,RejectedExecutionHandler handler) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory,handler);
    }

    @Override
    protected <T> RunnableFuture<T> newTaskFor(Callable<T> callable) {

        if (callable instanceof CancelableCallable) {
            DebugLog.d(TAG, "Callable->newTaskFor CancelableCallable!!");
            return ((CancelableCallable) callable).newTaskFor();
        }
        DebugLog.d(TAG, "Runnable->newTaskFor normal!!");
        return super.newTaskFor(callable);
    }

    @Override
    protected <T> RunnableFuture<T> newTaskFor(Runnable runnable, T value) {
        if (runnable instanceof CancelableCallable) {
            DebugLog.d(TAG, "Runnable->newTaskFor CancelableCallable!!");
            return ((CancelableCallable) runnable).newTaskFor(value);
        }
        DebugLog.d(TAG, "Runnable->newTaskFor normal!!");
        return super.newTaskFor(runnable, value);
    }

    @Override
    protected void beforeExecute(Thread t, Runnable r) {
        super.beforeExecute(t, r);
        DebugLog.d(TAG, "XThreadPoolExecutor->beforeExecute Thread Name:" + t.getName());
    }

    @Override
    protected void afterExecute(Runnable r, Throwable t) {
        super.afterExecute(r, t);
        DebugLog.d(TAG, "XThreadPoolExecutor->afterExecute !" + ((PriorityFutureTask) r).getPriority());
    }
}
