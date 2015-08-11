package com.tom.basecore.thread;

/**
 * Description: 设置线程池中task的优先级
 * User： yuanzeyao.
 * Date： 2015-08-10 14:10
 */
public interface IPriorityInterface {
    //设置线程池中任务的优先级
    public void setPriority(int priority);
    //获取线程池中任务的优先级
    public int getPriority();

}
