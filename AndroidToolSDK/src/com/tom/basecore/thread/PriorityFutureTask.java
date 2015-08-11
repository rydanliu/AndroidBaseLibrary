package com.tom.basecore.thread;

import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

/**
 * Description:支持中断和优先级的任务
 * User： yuanzeyao.
 * Date： 2015-08-10 14:24
 */
public class PriorityFutureTask<T> extends FutureTask<T> implements IPriorityInterface {
    private Callable<T> mCallable;
    public PriorityFutureTask(Callable<T> callable) {
        super(callable);
        this.mCallable=callable;
    }

    @Override
    public void setPriority(int priority) {

    }

    @Override
    public int getPriority() {
        if (mCallable != null && mCallable instanceof IPriorityInterface) {
            return ((IPriorityInterface) mCallable).getPriority();
        }
        return Thread.NORM_PRIORITY;
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        try {
            if (mCallable != null && mCallable instanceof XCallableTask) {
                ((XCallableTask) mCallable).cancel();
            }
        } finally {
            return super.cancel(mayInterruptIfRunning);
        }
    }
}
