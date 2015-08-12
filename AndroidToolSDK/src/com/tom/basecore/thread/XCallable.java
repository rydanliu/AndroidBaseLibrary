package com.tom.basecore.thread;

import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;
import java.util.concurrent.RunnableFuture;

/**
 * Description:支持删除并且支持优先级的Callable
 * User： yuanzeyao.
 * Date： 2015-08-10 14:11
 */
public abstract class XCallable<T> extends CancelableCallable implements Callable<T>{
    @Override
    public RunnableFuture newTaskFor() {
        FutureTask<T> mFutureTask = new PriorityFutureTask<T>(this);
        return mFutureTask;
    }

    @Override
    public RunnableFuture newTaskFor(Object value) {
        throw new UnsupportedOperationException("XRunnable is not support newTaskFor(Object value)!!");
    }
}
