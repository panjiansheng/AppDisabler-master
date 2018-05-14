package com.pjssq.appdisabler.domain;

import android.graphics.drawable.Drawable;
import android.util.Log;

import java.text.Collator;
import java.util.Comparator;
import java.util.Locale;

/**
 * Created by Pan on 2016-05-25.
 */
public class AppInfo implements  Comparable{
    public String appName="";
    public String packageName="";
    public String versionName="";
    public int versionCode=0;
    public Drawable appIcon=null;
    public boolean enabled=true;

    public static  final Comparator cmp= Collator.getInstance(Locale.CHINA  );
    public void print()
    {
        Log.v("app", "Name:" + appName + " Package:" + packageName);
        Log.v("app","Name:"+appName+" versionName:"+versionName);
        Log.v("app","Name:"+appName+" versionCode:"+versionCode);
    }

    @Override
    public int compareTo(Object another) {
       // Comparator cmp= Collator.getInstance(Locale.CHINA  );
        return  cmp.compare(this.appName, ((AppInfo) another).appName);
       // return this.appName.compareTo(((AppInfo)another).appName);
    }
}
