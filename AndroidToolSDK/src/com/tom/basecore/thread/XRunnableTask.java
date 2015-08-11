package com.tom.basecore.thread;

import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.RunnableFuture;

/**
 * Description:支持优先级和中断的Runnable
 * User： yuanzeyao.
 * Date： 2015-08-10 14:38
 */
public abstract class XRunnableTask<T> extends ICancelableTask<T> implements Runnable {

    @Override
    public RunnableFuture<T> newTaskFor(T value) {
        Callable<T> callable= Executors.callable(this, value);
        FutureTask<T> mFutureTask = new PriorityFutureTask<T>(callable);
        return mFutureTask;
    }

    @Override
    public RunnableFuture<T> newTaskFor() {
        throw new UnsupportedOperationException("XRunnableTask is not support newTaskFor()!!");
    }
}
