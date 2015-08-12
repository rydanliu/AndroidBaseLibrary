package com.tom.basecore.thread;

import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;
import java.util.concurrent.RunnableFuture;

/**
 * Description:支持优先级和中断的Runnable
 * User： yuanzeyao.
 * Date： 2015-08-10 14:38
 */
public abstract class XRunnable<T> extends CancelableCallable<T> implements Runnable {

    @Override
    public RunnableFuture<T> newTaskFor(T value) {
        Callable<T> callable= new RunnableAdapter<T>(this,value);
        FutureTask<T> mFutureTask = new PriorityFutureTask<T>(callable);
        return mFutureTask;
    }

    @Override
    public RunnableFuture<T> newTaskFor() {
        throw new UnsupportedOperationException("XRunnable is not support newTaskFor()!!");
    }

    /**
     * 将XRunnable变为XCallable的适配器接口
     * @param <T>
     */
    static final class RunnableAdapter<T> extends XCallable<T> {
        final XRunnable<T> task;
        final T result;
        RunnableAdapter(XRunnable<T> task, T result) {
            this.task = task;
            this.result = result;
        }
        public T call() {
            task.run();
            return result;
        }

        @Override
        public int getPriority() {
            if(task!=null)
                return task.getPriority();
            return super.getPriority();
        }

        @Override
        public void setPriority(int priority) {
            if(task!=null)
                task.setPriority(priority);
            super.setPriority(priority);
        }
    }
}
