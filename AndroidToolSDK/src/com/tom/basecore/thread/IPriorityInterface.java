package com.tom.basecore.thread;

/**
 * Description: 设置线程池中task的优先级
 * User： yuanzeyao.
 * Date： 2015-08-10 14:10
 */
public interface IPriorityInterface {
    /**
     * 设置任务的优先级
     * @param priority
     *          {@value Thread#MAX_PRIORITY} or {@value Thread#MIN_PRIORITY} or {@value Thread#NORM_PRIORITY}
     */
    public void setPriority(int priority);

    /**
     * 获取任务的优先级
     * @return
     */
    public int getPriority();

}
