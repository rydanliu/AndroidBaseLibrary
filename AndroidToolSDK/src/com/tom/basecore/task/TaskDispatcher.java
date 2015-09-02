package com.tom.basecore.task;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Process;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;

/**
 * Description:任务管理器，使用一个{@link java.util.concurrent.BlockingQueue}存放需要
 * 执行的{@link TaskBean},并使用单线程逐个执行，{@link TaskBean}
 *
 * User： yuanzeyao.
 * Date： 2015-08-26 14:32
 */
public class TaskDispatcher extends Thread{

    public static final int MSG_COMPLETE=1;
    public static final int MSG_ERROR=2;
    public static final int MSG_CANCEL=3;

    private final BlockingQueue<TaskBean<?>> mQueue=new PriorityBlockingQueue<TaskBean<?>>();

    private volatile boolean mQuit = false;

    private Handler mHandler=new Handler(Looper.getMainLooper()){
        @Override
        public void handleMessage(Message msg) {
            TaskBean<?> mBean=(TaskBean)msg.obj;
            if(mBean==null || mBean.getCallBack()==null){
                return;
            }
            switch (msg.what){
                case MSG_COMPLETE:
                    mBean.getCallBack().onSuccess(mBean.mResult);
                    break;
                case MSG_CANCEL:
                    mBean.getCallBack().onError();
                    break;
                case MSG_ERROR:
                    mBean.getCallBack().onCancel();
                    break;
            }
        }
    };


    @Override
    public void run() {
        Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
        TaskBean<?> mBean = null;
        while (true) {
            try {
                mBean = mQueue.take();
            } catch (InterruptedException e) {
                // We may have been interrupted because it was time to quit.
                if (mQuit) {
                    return;
                }
                continue;
            }

            try {
                if (mBean != null && !mBean.isCanceled()) {
                    Object mObj = mBean.doInBackground();
                    mBean.mResult = mObj;
                    Message msg = mHandler.obtainMessage();
                    msg.what = MSG_COMPLETE;
                    msg.obj = mBean;
                    mHandler.sendMessage(msg);
                } else {
                    if (mBean != null) {
                        Message msg = mHandler.obtainMessage();
                        msg.what = MSG_CANCEL;
                        msg.obj = mBean;
                        mHandler.sendMessage(msg);
                    }
                }

            } catch (Exception e) {
                if (mBean != null) {
                    Message msg = mHandler.obtainMessage();
                    msg.what = MSG_ERROR;
                    msg.obj = mBean;
                    mHandler.sendMessage(msg);
                }
            }
        }

    }

    public void addTask(TaskBean<?> mBean){
        if(mBean!=null){
            try {
                mQueue.put(mBean);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    public void quit(){
        mQuit=true;
        interrupt();
    }


}
