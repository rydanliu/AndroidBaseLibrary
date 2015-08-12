package com.tom.basecore.thread;

import java.util.concurrent.RunnableFuture;

/**
 * Description:支持中断和优先级的任务
 * User： yuanzeyao.
 * Date： 2015-08-10 14:40
 */
 abstract class CancelableCallable<T> implements IPriorityInterface {
    private int priority=Thread.NORM_PRIORITY;

    public void cancel() {

    }

    public abstract RunnableFuture<T> newTaskFor();

    public abstract RunnableFuture<T> newTaskFor(T value);

    @Override
    public int getPriority() {
        return priority;
    }

    @Override
    public void setPriority(int priority) {
        this.priority = priority;
    }
}
