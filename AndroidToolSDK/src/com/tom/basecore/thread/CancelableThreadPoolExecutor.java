package com.tom.basecore.thread;


import com.tom.basecore.utlis.LogUtil;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.RunnableFuture;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Description:
 * User： yuanzeyao.
 * Date： 2015-07-09 13:44
 */
public class CancelableThreadPoolExecutor extends ThreadPoolExecutor {
    public static final String TAG="BaseAsyncTask";
    public CancelableThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue, ThreadFactory threadFactory) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory);
    }

    @Override
    protected <T> RunnableFuture<T> newTaskFor(Callable<T> callable) {

        if(callable instanceof BaseAsyncTask.CallableTask)
        {
            LogUtil.d(TAG, "newTaskFor CallableTask!!");
            return ((BaseAsyncTask.CallableTask)callable).newTask();
        }
        LogUtil.d(TAG, "newTaskFor Callable!!");
        return super.newTaskFor(callable);
    }
}
