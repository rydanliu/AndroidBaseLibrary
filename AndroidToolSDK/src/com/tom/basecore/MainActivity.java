package com.tom.basecore;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.tom.basecore.http.HttpManager;
import com.tom.basecore.http.Request;
import com.tom.basecore.http.TextHttpResponseHandler;
import com.tom.basecore.utlis.DebugLog;

import org.apache.http.Header;

public class MainActivity extends Activity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    HttpManager.getInstance().initHttpDiskCache(this);
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
    DebugLog.d("yzy","onStartTask...");
    Request<String> mRequest=new Request<String>(Request.Method.GET,"https://www.baidu.com/",new TextHttpResponseHandler(){

      @Override
      public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
        DebugLog.d("yzy","onFailure...");
      }

      @Override
      public void onSuccess(int statusCode, Header[] headers, String responseString) {
        if(statusCode==HttpManager.STATUS_CODE_LOCAL){
          DebugLog.d("yzy","getData from local and not expired! ");
        }else if(statusCode==HttpManager.STATUS_CODE_LOCAL_EXPIRED){
          DebugLog.d("yzy","getData from local and  expired! ");
        }else {
          DebugLog.d("yzy","getData from net ! ");
        }
      }
    });
    //mRequest.setShouldCache(false);
    HttpManager.getInstance().performRequest(mRequest);
  }
}
