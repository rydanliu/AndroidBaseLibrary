package com.tom.basecore.thread;

import com.tom.basecore.utlis.AppUtils;
import com.tom.basecore.utlis.LogUtil;
import com.tom.basecore.utlis.OSVersionUtils;

import java.util.Comparator;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Description:创建线程池的工具类，此类创建的线程池都是基于{@link XThreadPoolExecutor}
 * <pre>
 *     1、通过此类创建的线程池支持任务的优先级和支持任务快速中断
 *     2、默认线程池核心线程有{@link #CORE_POOL_SIZE}*处理器核数 个，最大线程数{@link #MAXIMUM_POOL_SIZE}*处理器核数，等待
 *       队列的长度为{@link #TASK_QUEUE_SIZE}
 *     3、核心线程运行超时 {@link ThreadPoolExecutor#allowCoreThreadTimeOut(boolean)}
 *     4、当线程池中的任务超过{@link #MAXIMUM_POOL_SIZE}*处理器核数+{@link #TASK_QUEUE_SIZE}时,会移除优先级最低的任务
 * </pre>
 * User： yuanzeyao.
 * Date： 2015-07-08 10:42
 */
public class ThreadPoolManager {

    public static final String TAG = "ThreadPoolManager";

    //线程池中核心进程数量
    private static final int CORE_POOL_SIZE = 3;
    //线程池中最大进程数
    private static final int MAXIMUM_POOL_SIZE = 50;
    //线程最大的闲置时间
    private static final int KEEP_ALIVE = 1;
    //任务队列默认大小
    private static final int TASK_QUEUE_SIZE = 20;
    //任务优先级比较器
    private static Comparator<Runnable> mCompartor = new Comparator<Runnable>() {
        @Override
        public int compare(Runnable lhs, Runnable rhs) {
            if (lhs instanceof IPriorityInterface && rhs instanceof IPriorityInterface) {

                int result=((IPriorityInterface) rhs).getPriority()-((IPriorityInterface) lhs).getPriority();
                LogUtil.d(TAG,"compare result:"+result);
                return result;
            }
            LogUtil.d(TAG,"compare:0");
            return 0;
        }
    };

    private static RejectedExecutionHandler mHandler=new RejectedExecutionHandler() {

        @Override
        public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
            removeLowestPriorityTask(r,executor);
        }
    };

    /**
     * 创建默认配置的线程池，此线程池支持快速中断和优先级排序
     *此线程池支持优先级排序
     * @return
     */
    public static synchronized XThreadPoolExecutor createPriorityAndDefaultThreadPool() {
        int numCores = AppUtils.getNumCores();
        BlockingQueue<Runnable> mPoolWorkQueue =
                new BoundedPriorityBlockingQueue<Runnable>(numCores * TASK_QUEUE_SIZE, mCompartor);

        XThreadPoolExecutor mExecutor = new XThreadPoolExecutor(numCores*CORE_POOL_SIZE, numCores * MAXIMUM_POOL_SIZE, numCores * KEEP_ALIVE,
                TimeUnit.SECONDS, mPoolWorkQueue, new ThreadFactory() {
            private final AtomicInteger mCount = new AtomicInteger(1);

            @Override
            public Thread newThread(Runnable r) {
                return new Thread(r, "PriorityAndDefaultThread#" + mCount.getAndIncrement());
            }
        },mHandler);
        if (OSVersionUtils.hasGingerbread()) {
            //允许核心进程超时
            mExecutor.allowCoreThreadTimeOut(true);
        }
        return mExecutor;
    }

    /**
     * 创建指定配置的线程池，此线程池支持快速中断和优先级排序
     * 此线程池支持优先级排序
     * @param core_pool_size
     * @param max_pool_size
     * @param task_queue_size
     * @param keepAliveTime
     * @return
     */
    public static synchronized XThreadPoolExecutor createPriorityThreadPool(int core_pool_size, int max_pool_size, int task_queue_size, long keepAliveTime) {
        if (core_pool_size < 0 || max_pool_size < 0 || task_queue_size < 0 || keepAliveTime < 0) {
            throw new IllegalArgumentException("core_pool_size or max_pool_size or task_queue_size or keepAliveTime need greator than zero!! ");
        }
        int numCores = AppUtils.getNumCores();
        BlockingQueue<Runnable> mPoolWorkQueue =
                new BoundedPriorityBlockingQueue<Runnable>(numCores * task_queue_size, mCompartor);
        XThreadPoolExecutor mExecutor = new XThreadPoolExecutor(numCores*core_pool_size, numCores * max_pool_size, numCores * keepAliveTime,
                TimeUnit.SECONDS, mPoolWorkQueue, new ThreadFactory() {
            private final AtomicInteger mCount = new AtomicInteger(1);

            @Override
            public Thread newThread(Runnable r) {
                return new Thread(r, "PriorityThread#" + mCount.getAndIncrement());
            }
        },mHandler);

        if (OSVersionUtils.hasGingerbread()) {
            //允许核心进程超时
            mExecutor.allowCoreThreadTimeOut(true);
        }
        return mExecutor;
    }


    /**
     * 类似{@link Executors#newCachedThreadPool()}
     * 此线程不支持优先级排序
     * @return
     */
    public static synchronized XThreadPoolExecutor createCacheThreadPool() {
        XThreadPoolExecutor mExecutor = new XThreadPoolExecutor(0, Integer.MAX_VALUE,
                60L, TimeUnit.SECONDS,
                new SynchronousQueue<Runnable>(), new ThreadFactory() {
            private final AtomicInteger mCount = new AtomicInteger(1);

            @Override
            public Thread newThread(Runnable r) {
                return new Thread(r, "CacheThread#" + mCount.getAndIncrement());
            }
        });
        return mExecutor;
    }

    /**
     * 类似{@link Executors#newSingleThreadExecutor()}
     * 此线程池支持优先级排序
     * @return
     */
    public static synchronized XThreadPoolExecutor createSingleThreadPool(int task_queue_size) {
        if (task_queue_size < 0) {
            throw new IllegalArgumentException("task_queue_size need greator than zero!! ");
        }
        int numCores = AppUtils.getNumCores();
        BlockingQueue<Runnable> mPoolWorkQueue =
                new BoundedPriorityBlockingQueue<Runnable>(numCores * task_queue_size, mCompartor);
        XThreadPoolExecutor mExecutor = new XThreadPoolExecutor(1, 1, 0L,
                TimeUnit.SECONDS, mPoolWorkQueue, new ThreadFactory() {
            private final AtomicInteger mCount = new AtomicInteger(1);

            @Override
            public Thread newThread(Runnable r) {
                return new Thread(r, "SingleThread#" + mCount.getAndIncrement());
            }
        },mHandler);
        return mExecutor;
    }

    /**
     * 类似{@link Executors#newFixedThreadPool(int)}
     * 此线程池支持优先级排序
     * @param core_pool_size
     * @return
     */
    public static synchronized XThreadPoolExecutor createFixThreadPool(int core_pool_size, int task_queue_size) {
        if (task_queue_size < 0 || core_pool_size < 0) {
            throw new IllegalArgumentException("task_queue_size or core_pool_size need greator than zero!! ");
        }
        int numCores = AppUtils.getNumCores();
        BlockingQueue<Runnable> mPoolWorkQueue =
                new BoundedPriorityBlockingQueue<Runnable>(numCores * task_queue_size, mCompartor);
        XThreadPoolExecutor mExecutor = new XThreadPoolExecutor(core_pool_size, core_pool_size, 0L,
                TimeUnit.SECONDS, mPoolWorkQueue, new ThreadFactory() {
            private final AtomicInteger mCount = new AtomicInteger(1);

            @Override
            public Thread newThread(Runnable r) {
                return new Thread(r, "FixThread#" + mCount.getAndIncrement());
            }
        },mHandler);
        return mExecutor;
    }

    /**
     * 当线程池满时，移除最低优先级的任务
     * @param r
     * @param executor
     */
    private static void removeLowestPriorityTask(Runnable r, ThreadPoolExecutor executor) {
        if (!executor.isShutdown()) {
            BlockingQueue<Runnable> blockingQueue = executor.getQueue();
            if (blockingQueue instanceof BoundedPriorityBlockingQueue && ((BoundedPriorityBlockingQueue) blockingQueue).removeMinPriority()) {
                executor.execute(r);
            }
        }
    }
}
