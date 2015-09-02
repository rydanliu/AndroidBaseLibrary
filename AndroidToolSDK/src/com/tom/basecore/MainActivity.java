package com.tom.basecore;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.tom.basecore.task.TaskBean;
import com.tom.basecore.task.TaskDispatcher;
import com.tom.basecore.utlis.DebugLog;


public class MainActivity extends Activity {
  public static final String TAG="MainActivity";
  private TaskDispatcher mDispatcher=new TaskDispatcher();



  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    mDispatcher.start();
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.main, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    // Handle action bar item clicks here. The action bar will
    // automatically handle clicks on the Home/Up button, so long
    // as you specify a parent activity in AndroidManifest.xml.
    int id = item.getItemId();
    if (id == R.id.action_settings) {
      return true;
    }
    return super.onOptionsItemSelected(item);
  }

  public void onStartTask(View view){
    TaskBean<String> task1=new TaskBean<String>() {
      @Override
      public Object doInBackground() {
        try {
          Thread.sleep(2000);
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
        return "gavin";
      }
    };

    task1.setCallBack(new TaskBean.ITaskCallback() {
      @Override
      public void onError() {

      }

      @Override
      public void onSuccess(Object objects) {
        DebugLog.d(TAG,"result:%s",objects.toString());
      }

      @Override
      public void onCancel() {

      }
    });

    TaskBean<String> task2=new TaskBean<String>() {
      @Override
      public Object doInBackground() {
        try {
          Thread.sleep(2000);
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
        return "gavin2";
      }
    };

    task2.setCallBack(new TaskBean.ITaskCallback() {
      @Override
      public void onError() {

      }

      @Override
      public void onSuccess(Object objects) {
        DebugLog.d(TAG,"result:%s",objects.toString());
      }

      @Override
      public void onCancel() {

      }
    });

    TaskBean<String> task3=new TaskBean<String>() {
      @Override
      public Object doInBackground() {
        try {
          Thread.sleep(2000);
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
        return "gavin3";
      }
    };

    task3.setCallBack(new TaskBean.ITaskCallback() {
      @Override
      public void onError() {

      }

      @Override
      public void onSuccess(Object objects) {
        DebugLog.d(TAG,"result:%s",objects.toString());
      }

      @Override
      public void onCancel() {

      }
    });
    task3.setPriority(TaskBean.Priority.HIGH);




    mDispatcher.addTask(task1);
    mDispatcher.addTask(task2);
    mDispatcher.addTask(task3);

  }

  public void onCancelTask(View view){

  }
}
