package com.boxshell.lesson.lxxz.online;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by yangjun on 16/3/2.
 */
public class Config {
    public static final String TAG = "lxxz";
    public static String APP_VERSION = "0.6";
    public static final String ROOT_URL = "http://iot.boxshell.cn/status";
    public static final String PING_URL = "http://iot.boxshell.cn";
    //public static final String SITE_URL = "http://iot.boxshell.cn";

    public static final String APP_URL = "/lvxin/get/latest_app";


    // url list
    public static final String FIRMWARE_DIR = "/lvxin/get/firmware";
    public static final String APP_DOWNLOAD_NAME = "/sdcard/lvxin.app";

    private String mSite;

    public boolean bNeedUpdateAPP;

    public static int UDP_PORT = 50011;
    public static int TCP_PORT = 50012;
    public static int UDP_TIMEOUT = 5000;
    public static int TCP_TIMEOUT = 5000;

    public static String DEFAULT_SRC_ID =   "1234567890123456";
    public static String BROADCAST_DST_ID = "ffffffffffffffff";

    //public static String CMD_RD_STATE_FWVER = "{}"


    public Config(Context context){
        SharedPreferences settings = context.getSharedPreferences(TAG,0);

        mSite = settings.getString("site","http://iot.boxshell.cn");

        bNeedUpdateAPP = true;

        PackageInfo pinfo = null;
        try {
            pinfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        APP_VERSION = pinfo.versionName;

    }

    public String getSite(){
        return mSite;
    }
    public void setSite(String site){
        mSite = site;
    }

    public void save(Context context){
        SharedPreferences settings = context.getSharedPreferences(TAG,0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString("site", mSite);

        editor.commit();
    }

    public static String strPacketGet(String name, String content){
        try{
            JSONObject obj = new JSONObject();
            obj.put("CMD", "RD");
            obj.put("TARGET", name.toUpperCase());
            obj.put("CONTENT", content.toUpperCase());

            return obj.toString();

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String strPacketSet(String name, String subtarget, String content){
        try{
            JSONObject obj = new JSONObject();
            obj.put("CMD", "WR");
            obj.put("TARGET", name.toUpperCase());
            if(subtarget==null || subtarget.length()<1){
                ;
            }else{
                obj.put("SUBTARGET", subtarget);
            }
            obj.put("CONTENT", content.toUpperCase());
            return obj.toString();

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }
}
