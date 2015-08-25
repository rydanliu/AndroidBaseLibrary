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
    String url="http://iface.iqiyi.com/api/initLogin?id=861022009464118&key=20371202b2a9358f98e4363d19215591&version=6.5.2&os=4.4.2&ua=HUAWEI+P6-T00&access_type=1&network=1&login=0&resolution=720*1184&include_all=1&init_type=0&getvr=1&vip=1&softc=2013-02-18-16-15-22&gps=121.402696,31.176531&type=json&udid=14ab8021a96aef45&ppid=&openudid=14ab8021a96aef45&uniqid=585fade54592a7bc6d5c5d231ab8c994&vip=0&auth=&usertype=-1&adappid=1&cpu=1500000&gpu=&scdensity=2.0&size=4.3&sign=975d772c7538f0e7882f52086e331612&isCrash=0&baiduid=1068819993644935279&miid=&idfv=ACE44ACE6219CA3A825ECFDD09CFC20F&p_type=8&client_type=gphone&player_id=qc_100001_100086&s_ids=1000000000322&platform=GPhone&adpic2=1&reddot=1&predown=1&msg=2&types=2&dpi=320&mac_md5=268cd9ac8136ea1f8405fea88f0a9bd1&isNew=0&isJailBreak=1&its=1440142613&uts=1440142613&android_id=14ab8021a96aef45&screen_status=1&view_mode=1&pushid=&mac=3c%3Adf%3Abd%3Aba%3A70%3A77&ns=H4sIAAAAAAAAALVUQZLAIAj7kAdAFPn%2FxxYJtnb3vBdGQEMItCJq0nhpn03SMk%2FmxsNYm3SdEVFya%2BzaexNm89aJR5w93kW2K5WVyRS2L7Ww3L1sZe944lSE7ImwyQw%2B0k1PPDErcr0CZqcxZtkPk%2BU2f9UCH3BIhMSM%2B1%2BCCGSucMFpxp1PVSbfiFkJd9zHKtpIdrIlzQZL3fh0efEFOgjgEYsQ1xk17l4PVetHGxRBYHdQNICO2lAAytQcUSl1SAbooGhkk3%2BFwCgugf%2FeOfAZuYRLeJAEAFhnqzUayKnd73GIiB4LZZnWsjbEA0NU3yr7nY0ReywrdpRUwk7hva6ibU1bTQOp%2FFh32nydIky0m%2B9BSUxsHaR3Q2DNv0u7%2FWzkddF6Brd7q4L0Fr7UePYk%2FTddGH7m%2FKTPtm7tt3%2Bpmumz10r%2Fhn7N5h%2FQn%2B%2FZkX9el%2BrP6%2FoYsEo7f%2F0gXv%2Bd2tm0%2Bin5rdJ1TIAfvidvNRsFAAA%3D&arid=&pps=0&cupid_uid=861022009464118&secure_p=GPhone&req_times=1&req_sn=1440146137744";
    Request<String> mRequest=new Request<String>(Request.Method.GET,url,new TextHttpResponseHandler(){

      @Override
      public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
        DebugLog.d("yzy", "onFailure...");
      }

      @Override
      public void onSuccess(int statusCode, Header[] headers, String responseString) {
        if(statusCode==HttpManager.STATUS_CODE_LOCAL){
          DebugLog.d("yzy","getData from local and statusCode:%d ",statusCode);
        }else if(statusCode==HttpManager.STATUS_CODE_LOCAL_EXPIRED){
          DebugLog.d("yzy", "getData from local and statusCode:%d ",statusCode);
        }else {
          DebugLog.d("yzy", "getData from net ! and statusCode:%d",statusCode);
        }
      }

      @Override
      public void onCancel() {
        super.onCancel();
        DebugLog.d("yzy","onCancel!");
      }
    });


    mRequest.setCacheTimeOut(10*1000);
    HttpManager.getInstance().performRequest(mRequest);
    HttpManager.getInstance().performRequest(mRequest);
    HttpManager.getInstance().performRequest(mRequest);
    HttpManager.getInstance().performRequest(mRequest);
    HttpManager.getInstance().performRequest(mRequest);
    HttpManager.getInstance().performRequest(mRequest);
    HttpManager.getInstance().performRequest(mRequest);
    HttpManager.getInstance().performRequest(mRequest);
    HttpManager.getInstance().performRequest(mRequest);
    HttpManager.getInstance().performRequest(mRequest);
    HttpManager.getInstance().performRequest(mRequest);
//    HttpManager.getInstance().performRequest(mRequest);
//    HttpManager.getInstance().performRequest(mRequest);
//    HttpManager.getInstance().performRequest(mRequest);
//    HttpManager.getInstance().performRequest(mRequest);
//    HttpManager.getInstance().performRequest(mRequest);
//    HttpManager.getInstance().performRequest(mRequest);
//    HttpManager.getInstance().performRequest(mRequest);
//    HttpManager.getInstance().performRequest(mRequest);
//    HttpManager.getInstance().performRequest(mRequest);
//    HttpManager.getInstance().performRequest(mRequest);
//    HttpManager.getInstance().performRequest(mRequest);
//    HttpManager.getInstance().performRequest(mRequest);
//    HttpManager.getInstance().performRequest(mRequest);
  }

  public void onCancelTask(View view){
    HttpManager.getInstance().cancelRequestByTag(Request.DEFAULT_TAG,true);
  }
}
