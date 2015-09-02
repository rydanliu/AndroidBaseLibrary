package com.tom.basecore.task;


/**
 * Description:抽象任务对象，所有的具体任务需要继承此类
 * User： yuanzeyao.
 * Date： 2015-08-26 14:42
 */
public abstract class TaskBean<B> implements Comparable {
    private Priority mPriority=Priority.NORMAL;
    private boolean isCanceled=false;

    private ITaskCallback mCallback;

    public B mObj;

    public Object mResult;


    public void setPriority(Priority mPriority){
        this.mPriority=mPriority;
    }

    public Priority getPriority(){
        return mPriority;
    }

    public void cancel(){
        isCanceled=true;
    }

    public boolean isCanceled(){
        return isCanceled;
    }

    public void setCallBack(ITaskCallback mCallback){
        this.mCallback=mCallback;
    }

    public ITaskCallback getCallBack(){
        return mCallback;
    }

    public abstract Object doInBackground();


    public static enum Priority {
        LOW,
        NORMAL,
        HIGH,
        IMMEDIATE
    }

    /**
     * 任务执行完毕回调接口
     */
    public interface ITaskCallback{

        public void onError();

        public void onSuccess(Object objects);

        public void onCancel();
    }

    @Override
    public int compareTo(Object another) {
        return ((TaskBean)another).mPriority.ordinal()-this.mPriority.ordinal();
    }
}
