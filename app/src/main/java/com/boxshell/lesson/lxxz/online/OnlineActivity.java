package com.boxshell.lesson.lxxz.online;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;

import com.boxshell.lesson.lxxz.R;

import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

public class OnlineActivity extends AppCompatActivity
        implements OnlineActivityFragment.OnFragmentInteractionListener,
        OfflineFragment.OnFragmentOfflineListener,
        SelectDialogFragment.OnFragmentInteractionListener
{
    private static final String TAG = "lxxz";
    private static final String ROOT_URL = "http://iot.boxshell.cn/status";
    private static final String PING_URL = "http://iot.boxshell.cn";
    private Config mConfig;
    private boolean bNeedCheck;
    private boolean bNeedUpdate;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        bNeedCheck = true;
        bNeedUpdate = true;


        Log.d(TAG, "into OnlineActivity onCreate()");

        setContentView(R.layout.activity_online);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);



        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorAccent)));

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                OnlineActivityFragment frag = (OnlineActivityFragment)getSupportFragmentManager().findFragmentByTag("online");
                frag.checkNetworkAvailability();
                //Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                //        .setAction("Action", null).show();
            }
        });

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // First time init, create the UI.
        /*if (savedInstanceState == null) {
            getFragmentManager().beginTransaction().add(R.id.fragmentOnlineMain,
                    new OnlineActivityFragment()).commit();
        }*/

        // Add fragment into the page
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.fragmentContainer, new OnlineActivityFragment(), "online")
                    .commit();

        }

        mConfig = new Config(this);


        Log.d(TAG, "out of OnlineActivity onCreate()");
    }

    @Override
    protected void onStop() {
        mConfig.save(this);
        super.onStop();
    }

    /**
     * To check if the server can be reached
     * @return
     *//*
    @Override
    public boolean bInternetWorking() {
        boolean success = false;
        try {
            URL url = new URL("http://iot.boxshell.cn");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(5000);
            connection.connect();
            success = connection.getResponseCode() == 200;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return success;
    }*/
    /*@Override
    public boolean bInternetWorking(){
        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

        if (networkInfo != null && networkInfo.isConnected()){
            return true;
        }
        else
            return false;
    }
*/

    // implement the interface in the main activity
    @Override
    public void onFragmentInteractionOffline() {
        Log.d(TAG, "into onFragmentInteractionOffline()");
        getSupportFragmentManager().beginTransaction()
                //.setCustomAnimations(R.anim.enter,R.anim.exit,R.anim.pop_enter,R.anim.pop_exit)
                .replace(R.id.fragmentContainer, new OfflineFragment(), "offline")
                .commit();
    }
/*
    @Override
    public boolean bInternetWorking() {
        return false;
    }*/

    @Override
    public void onFragmentOfflineRefresh(Uri uri) {
        Log.d(TAG, "into onFragmentOfflineRefresh(Uri uri)");

        getSupportFragmentManager().beginTransaction()
                //.setCustomAnimations(R.anim.enter_right,R.anim.exit_right,R.anim.enter,R.anim.exit)
                .replace(R.id.fragmentContainer, new OnlineActivityFragment(), "online")
                .commit();

    }

    @Override
    public boolean bNeedCheckNetworkAvailability() {
        return bNeedCheck;
    }

    @Override
    public void disableCheckNetworkAvailability() {
        bNeedCheck = false;
    }

    @Override
    public boolean bNeedCheckUpdate() {
        return bNeedUpdate;
    }

    @Override
    public void markFab() {
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        //fab.setBackgroundColor(getResources().getColor(R.color.colorAccent));
        fab.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorAccent)));
    }

    @Override
    public void demarkFab() {
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        //fab.setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));
        fab.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorPrimary)));
    }

    public Config getConfig(){
        return mConfig;
    }

    @Override
    public void confirmUpdate(String surl) {
        Log.d(Config.TAG,"Confirm to download latest APP from:" + surl);

        new FetchAppTask(new JsonListener() {
            @Override
            public void callback(String s) {
                //after downloading finished
                if(s.equals("true")) {
                    Log.d(Config.TAG, "file downloaded");

                }else{
                    Log.d(Config.TAG,"file download failed");
                }
                mConfig.bNeedUpdateAPP = false;

            }
        }).execute(Config.PING_URL + surl);

    }

    @Override
    public void cancelUpdate() {
        Log.d(Config.TAG, "Cancel update APP");
        mConfig.bNeedUpdateAPP = false;
    }


    class FetchAppTask extends AsyncTask<String ,Integer, String> {

        public JsonListener mListener;
        private ProgressBar pBar;

        //public String path;

        public FetchAppTask(JsonListener listener){
            this.mListener = listener;
            //context = contex;
            //this.mUrl = url;
        }


        @Override
        protected void onPreExecute() {
            android.support.v4.app.Fragment frag = getSupportFragmentManager().findFragmentByTag("online");
            pBar = (ProgressBar) frag.getView().findViewById(R.id.progressBar);
            pBar.setVisibility(View.VISIBLE);
            pBar.setMax(100);
            pBar.setProgress(0);

            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... params) {
            boolean result = true;

            try{
                URL url = new URL(params[0]);
                Log.d(Config.TAG, "Begin to download: " + params[0]);

                URLConnection connection = url.openConnection();
                connection.connect();

                int fileLength = connection.getContentLength();
                Log.d(Config.TAG, "File length is: " + String.valueOf(fileLength));

                // download the file
                InputStream input = new BufferedInputStream(url.openStream());
                OutputStream output = new FileOutputStream(Config.APP_DOWNLOAD_NAME);

                byte data[] = new byte[1024];
                long total = 0;
                int count;
                while ((count = input.read(data)) != -1) {
                    total += count;
                    publishProgress((int) (total * 100 / fileLength));
                    output.write(data, 0, count);
                }

                output.flush();
                output.close();
                input.close();

            }catch (Exception e){
                result = false;
                e.printStackTrace();
            }finally {

            }
            return String.valueOf(result);
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            pBar.setProgress(values[0]);
            super.onProgressUpdate(values);
        }

        @Override
        protected void onPostExecute(String s) {
            if(s.equals("true")) {
                Intent i = new Intent();
                i.setAction(Intent.ACTION_VIEW);
                i.setDataAndType(Uri.fromFile(new File(Config.APP_DOWNLOAD_NAME)), "application/vnd.android.package-archive");
                Log.d("Lofting", "About to install new .apk");
                //mListener.startActivity(i);
                startActivity(i);
            }
            pBar.setVisibility(View.GONE);

            mListener.callback(s);
            super.onPostExecute(s);
        }
    }
}
