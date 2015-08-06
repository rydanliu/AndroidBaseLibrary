package com.tom.basecore.thread;

import android.text.TextUtils;

import com.tom.basecore.utlis.AppUtils;

import java.util.HashMap;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Description:管理所有的线程池
 * 根据不同线程池的名称管理不同的线程池
 * 默认线程池的核心线程有5*处理器核数
 * 默认最大线程数50*处理器核数
 * 等待队列长度为10
 * 所以当线程池中加入的任务数超过 50*处理器核数+10时，就无法添加任务，会抛出RejectedExecutionException异常
 * User： yuanzeyao.
 * Date： 2015-07-08 10:42
 */
public class ThreadPoolManager {

    public static final String TAG="ThreadPoolManager";

    private static HashMap<String,ThreadPoolExecutor> mExecutors=new HashMap<String,ThreadPoolExecutor>();

    //线程池中核心进程数量
    private static final int CORE_POOL_SIZE=3;
    //线程池中最大进程数
    private static final int MAXIMUM_POOL_SIZE=50;
    //线程最大的闲置时间
    private static final int KEEP_ALIVE=1;
    //线程工厂
    private static final ThreadFactory mThreadFactory = new ThreadFactory() {
        @Override
        public Thread newThread(Runnable r) {
            return new Thread(r, "mThreadFactory#" + mCount.getAndIncrement());
        }
        private final AtomicInteger mCount = new AtomicInteger(1);

    };

    /**
     * 根据指定线程池的名称，拿到一个线程池
     * 该线程池核心线程有5个，最大线程池
     * @param pool_name
     *          线程池的名称
     * @return
     *          返回一个线程池
     */
    public static synchronized ThreadPoolExecutor getThreadPool(String pool_name) {
       return getThreadPool(pool_name,CORE_POOL_SIZE);
    }

    /**
     * 根据指定的线程池名称，拿到一个线程池
     * @param pool_name
     *        线程池的名称
     * @param core_pool_size
     *        线程池中核心线程的大小
     * @return
     *        返回一个线程池
     */
    public static synchronized ThreadPoolExecutor getThreadPool(String pool_name, int core_pool_size) throws IllegalStateException {
        if(TextUtils.isEmpty(pool_name))
        {
            throw new IllegalStateException("pool_name can not be null!");
        }

        if(core_pool_size<=0)
        {
            core_pool_size=CORE_POOL_SIZE;
        }
        ThreadPoolExecutor mExecutor=mExecutors.get(pool_name);
        if (mExecutor!=null) {
            if(mExecutor.getCorePoolSize()!=core_pool_size)
            {
                mExecutor.setCorePoolSize(core_pool_size);
            }
            return mExecutors.get(pool_name);
        } else {
            int numCores = AppUtils.getNumCores();
            BlockingQueue<Runnable> mPoolWorkQueue =
                    new LinkedBlockingQueue<Runnable>(10);
            mExecutor = new CancelableThreadPoolExecutor(numCores * core_pool_size, numCores * MAXIMUM_POOL_SIZE, numCores * KEEP_ALIVE,
                    TimeUnit.SECONDS, mPoolWorkQueue, mThreadFactory);
            mExecutors.put(pool_name, mExecutor);
            return mExecutor;
        }
    }

    /**
     * 类似{@link Executors#newCachedThreadPool()}
     * @param mPoolName
     * @return
     */
    public static  synchronized ThreadPoolExecutor getCacheThredPool(String mPoolName)
    {
        if(TextUtils.isEmpty(mPoolName))
        {
            throw new IllegalStateException("pool_name can not be null!");
        }

        ThreadPoolExecutor mExecutor = mExecutors.get(mPoolName);
        if(mExecutor==null)
        {
            mExecutor = new CancelableThreadPoolExecutor(0, Integer.MAX_VALUE,
                    60L, TimeUnit.SECONDS,
                    new SynchronousQueue<Runnable>(), mThreadFactory);
            mExecutors.put(mPoolName, mExecutor);
        }
        return mExecutor;
    }

    /**
     * 类似{@link Executors#newSingleThreadExecutor()}
     * @param mPoolName
     * @return
     */
    public static synchronized ThreadPoolExecutor getSingleThreadPool(String mPoolName)
    {
        if(TextUtils.isEmpty(mPoolName))
        {
            throw new IllegalStateException("pool_name can not be null!");
        }
        ThreadPoolExecutor mExecutor = mExecutors.get(mPoolName);

        if(mExecutor==null)
        {
            mExecutor = new CancelableThreadPoolExecutor(1, 1, 0L,
                    TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(), mThreadFactory);
            mExecutors.put(mPoolName, mExecutor);
        }
        return mExecutor;
    }

    /**
     * 类似{@link Executors#newFixedThreadPool(int)}
     * @param mPoolName
     * @param core_pool_size
     * @return
     */
    public static synchronized ThreadPoolExecutor getFixThreadPool(String mPoolName,int core_pool_size)
    {
        if(TextUtils.isEmpty(mPoolName))
        {
            throw new IllegalStateException("pool_name can not be null!");
        }
        ThreadPoolExecutor mExecutor = mExecutors.get(mPoolName);

        if(mExecutor==null)
        {
            mExecutor = new CancelableThreadPoolExecutor(core_pool_size, core_pool_size, 0L,
                    TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(), mThreadFactory);
            mExecutors.put(mPoolName, mExecutor);
        }
        return mExecutor;
    }

    /**
     * 关闭所有的线程池
     */
    public static synchronized void shutDownNow()
    {
        for(ThreadPoolExecutor mExecutor :mExecutors.values())
        {
            if(mExecutor!=null)
                mExecutor.shutdownNow();
        }
    }





}
