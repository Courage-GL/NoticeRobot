package com.xiaoalei.notice.robot;

import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Chronometer;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;

import java.util.Calendar;


/**
 * @author xiaoalei
 */
public class Main2Activity extends AppCompatActivity {
    private boolean hasBind = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
    }


    public void startNotice(View v) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(this)) {
                Toast.makeText(this, "当前无权限，请授权", Toast.LENGTH_SHORT);
                new GlobalDialogSingle(this, "", "当前未获取悬浮窗权限", "去开启", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        startActivityForResult(new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName())), 0);
                    }
                }).show();

            } else {
                moveTaskToBack(true);
                Intent intent = new Intent(Main2Activity.this, FloatWinfowServices.class);
                hasBind = bindService(intent, mVideoServiceConnection, Context.BIND_AUTO_CREATE);
            }
        }
    }

    ServiceConnection mVideoServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            // 获取服务的操作对象
            FloatWinfowServices.MyBinder binder = (FloatWinfowServices.MyBinder) service;
            binder.getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 0) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (!Settings.canDrawOverlays(this)) {
                    Toast.makeText(this, "授权失败", Toast.LENGTH_SHORT).show();
                } else {

                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            Intent intent = new Intent(Main2Activity.this, FloatWinfowServices.class);
                            hasBind = bindService(intent, mVideoServiceConnection, Context.BIND_AUTO_CREATE);
                            moveTaskToBack(true);
                        }
                    }, 1000);

                }
            }
        }
    }


    @Override
    protected void onRestart() {
        super.onRestart();
        Log.d("RemoteView", "重新显示了");
        //不显示悬浮框
        if (hasBind) {
            unbindService(mVideoServiceConnection);
            hasBind = false;
        }

    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Log.d("RemoteView", "重新显示了onNewIntent");
        if (!hasBind){
            if (checkPackInfo("com.example.abner.xywy_net")) {
                Intent intent1 = getPackageManager().getLaunchIntentForPackage("com.example.abner.xywy_net");
                if (intent1 != null) {
                    intent1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent1);
                }
            } else {
                Toast.makeText(this, "没有找到该软件", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d("RemoteView", "被销毁");
    }


    private boolean checkPackInfo(String packname) {
        PackageInfo packageInfo = null;
        try {
            packageInfo = getPackageManager().getPackageInfo(packname, 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return packageInfo != null;
    }

}
