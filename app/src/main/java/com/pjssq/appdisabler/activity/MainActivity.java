package com.pjssq.appdisabler.activity;

import android.app.ActivityManager;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Switch;
import android.widget.TextView;
import                 android.app.ActivityManager.RunningAppProcessInfo;


import com.pjssq.appdisabler.domain.AppInfo;
import com.pjssq.appdisabler.R;
import com.pjssq.appdisabler.utils.DBHelper;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MainActivity extends ActionBarActivity {

    List<String> appInstalledList;

    public  DBHelper dbHelper=null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        dbHelper=new DBHelper(getApplicationContext());
        new Thread(r).start();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    public void init(int type){
        ListView appLv= (ListView) findViewById(R.id.appList);
       // appInstalledList=new ArrayList();
        ArrayList<AppInfo> appList = new ArrayList<AppInfo>(); //用来存储获取的应用信息数据　　　　　
         List<PackageInfo> packages = getPackageManager().getInstalledPackages(0);
        PackageManager pm=getPackageManager();
        for(PackageInfo packageInfo:packages) {
            AppInfo tmpInfo = new AppInfo();
            tmpInfo.enabled=packageInfo.applicationInfo.enabled;
            if (tmpInfo.enabled&&type==TYPE_DISABLED)continue;
            if (!(tmpInfo.enabled)&&type==TYPE_ENABLED)continue;
            if((packageInfo.applicationInfo.flags& ApplicationInfo.FLAG_SYSTEM)!=0)continue;
            tmpInfo.appName = packageInfo.applicationInfo.loadLabel(pm).toString();
            tmpInfo.packageName = packageInfo.packageName;
            tmpInfo.versionName = packageInfo.versionName;
            tmpInfo.versionCode = packageInfo.versionCode;
            tmpInfo.appIcon = packageInfo.applicationInfo.loadIcon(pm);
            appList.add(tmpInfo);

        }//好啦 这下手机上安装的应用数据都存在appList里了。
        Collections.sort(appList);
       int a=1;
//        List<HashMap<String,Object>> data=new ArrayList<HashMap<String, Object>>();
//        for(PackageInfo packageInfo:packages){
//                        if (packageInfo.applicationInfo.enabled&&type==TYPE_DISABLED)continue;
//            if (!(packageInfo.applicationInfo.enabled)&&type==TYPE_ENABLED)continue;
//            if((packageInfo.applicationInfo.flags& ApplicationInfo.FLAG_SYSTEM)!=0)continue;
//            HashMap<String,Object> map=new HashMap<String, Object>();
//            map.put("icon",packageInfo.applicationInfo.loadIcon(pm));
//            map.put("appName",packageInfo.applicationInfo.loadLabel(pm).toString());
//            map.put("appPackageName",packageInfo.packageName);
//            map.put("enabled",packageInfo.applicationInfo.enabled);
//            map.put("start",null);
//            data.add(map);
//        }

        List<HashMap<String,Object>> data=new ArrayList<HashMap<String, Object>>();
        for(AppInfo appInfo:appList){
            HashMap<String,Object> map=new HashMap<String, Object>();
            map.put("icon",appInfo.appIcon);
            map.put("appName",appInfo.appName);
            map.put("appPackageName",appInfo.packageName);
            map.put("enabled",appInfo.enabled);
            data.add(map);
        }
        SimpleAdapter simpleAdapter=new SimpleAdapter(this,data,R.layout.app_item,new String[]{"icon","appName","appPackageName","enabled","start"},new int[]{R.id.icon,R.id.appName,R.id.appPackageName,R.id.disableSwitch,R.id.start});
        appLv.setAdapter(simpleAdapter);
        simpleAdapter.setViewBinder(new SimpleAdapter.ViewBinder() {
            @Override
            public boolean setViewValue(final View view, Object data, String textRepresentation) {
                if(view instanceof ImageView && data instanceof Drawable){
                    ImageView iv = (ImageView)view;
                    iv.setImageDrawable((Drawable)data);
                    return true;
                }else if (view.getId()==R.id.disableSwitch){

                    Switch st=(Switch)view;
                    st.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                            String appName= ((TextView)(((LinearLayout) (view.getParent().getParent())).findViewById(R.id.appPackageName))).getText().toString();
                            if (isChecked) {

                                dbHelper.delete(appName);
                                enableApp(((TextView) (((LinearLayout) (view.getParent().getParent())).findViewById(R.id.appPackageName))).getText().toString());
                            } else {
                                ContentValues values = new ContentValues();
                                values.put("name", appName);
                                dbHelper.insert(values);
                                disableApp(((TextView)(((LinearLayout) (view.getParent().getParent())).findViewById(R.id.appPackageName))).getText().toString());
                            }
                        }
                    });
                    boolean enabled=(Boolean)data;
                    st.setChecked(enabled);
                    return  true;
                }else if(view.getId()==R.id.start){
                    Button startBtn=(Button)view;

                    startBtn.setOnClickListener(new Button.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            String appName= ((TextView)(((LinearLayout) (view.getParent().getParent())).findViewById(R.id.appPackageName))).getText().toString();
                            enableApp(appName);

                            boolean flag=true;
                        while (flag){
                            List<PackageInfo> packages = getPackageManager().getInstalledPackages(0);
                            for(PackageInfo pack:packages){
                                if (pack.applicationInfo.packageName.equals(appName)){
                                    flag=false;
                                    break;
                                };
                            }
                        }
                            try {
                                Thread.sleep(1000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            Intent intent = getPackageManager().getLaunchIntentForPackage(appName);
                            startActivity(intent);
                        }
                    });

                    return  true;
                }
                else{
                    return false;
                }
            }
        });
    }
public static  final int TYPE_ALL=0;
    public static  final int TYPE_ENABLED=1;
    public static  final int TYPE_DISABLED=2;
    public void onClick(View view){
        switch (view.getId()){
            case R.id.button:
                init(TYPE_ALL);
                break;
            case R.id.button1:
                init(TYPE_ENABLED);
                break;
            case R.id.button2:
                init(TYPE_DISABLED);
                break;
        }
    }
    public void enableApp(String appPackageName){

        try {
            Process localProcess=Runtime.getRuntime().exec("su");
            String cmd="pm enable "+appPackageName+"\n";
            DataOutputStream dataOutputStream=new DataOutputStream(localProcess.getOutputStream());
            dataOutputStream.writeBytes(cmd);
            dataOutputStream.flush();
            dataOutputStream.writeBytes("exit\n");

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void disableApp(String appPackageName){
        try {
            Process localProcess=Runtime.getRuntime().exec("su");
            String cmd="pm disable "+appPackageName+"\n";
            DataOutputStream dataOutputStream=new DataOutputStream(localProcess.getOutputStream());
            dataOutputStream.writeBytes(cmd);
            dataOutputStream.flush();
            dataOutputStream.writeBytes("exit\n");

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

Handler handler=new Handler();
    Runnable r=new Runnable() {
        @Override
        public void run() {
            Cursor c = dbHelper.query();
            ActivityManager am = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
            String forgapp=null;
            List<RunningAppProcessInfo> runnings = am.getRunningAppProcesses();
            for(RunningAppProcessInfo running : runnings){

                if(running.importance == RunningAppProcessInfo.IMPORTANCE_FOREGROUND
                        || running.importance == RunningAppProcessInfo.IMPORTANCE_VISIBLE){
                    forgapp=running.processName;
                }else{
                    continue;
                }
                break;

            }
            Set<String > apps=new HashSet<String>();
            while (c.moveToNext()){

                String appName=c.getString(c.getColumnIndex("name"));
                apps.add(appName);

            }
            apps.remove(forgapp);
            for(String app:apps){
                disableApp(app);
            }

            try {
                Thread.sleep(60*60*1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    };
}



//        try {
//            Process localProcess=Runtime.getRuntime().exec("su");
//            String cmd="pm list packages\n";
//            DataOutputStream dataOutputStream=new DataOutputStream(localProcess.getOutputStream());
//            dataOutputStream.writeBytes(cmd);
//            dataOutputStream.flush();
//            dataOutputStream.writeBytes("exit\n");
//            InputStream inputStream = localProcess
//                    .getInputStream();
//            InputStreamReader inputStreamReader = new InputStreamReader(
//                    inputStream);
//            BufferedReader bufferedReader = new BufferedReader(
//                    inputStreamReader);
//            String line = "";
//            StringBuilder stringBuilder = new StringBuilder(
//                    line);
//            while ((line = bufferedReader.readLine()) != null) {
//                stringBuilder.append(line);
//                stringBuilder.append("|");
//
//            }
//            String  a= stringBuilder.toString();
//            String b=a.replace("package:", "");
//            String[] appArray=stringBuilder.toString().replace("package:", "").split("\\|");
//            appInstalledList= Arrays.asList(appArray);
//            String c=appInstalledList.get(2);
//           int aa=1;
//        } catch (IOException e) {
//            e.printStackTrace();
//        }