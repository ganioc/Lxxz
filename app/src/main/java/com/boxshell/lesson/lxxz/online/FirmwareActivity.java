package com.boxshell.lesson.lxxz.online;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;

import com.boxshell.lesson.lxxz.R;

import java.awt.font.TextAttribute;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

public class FirmwareActivity extends AppCompatActivity
        implements FirmwareListFragment.OnFragmentInteractionListener,
        SelectDownloadFragment.OnFragmentInteractionListener {
    private static final String TAG = "lxxz";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fimware);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

 /*       FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });*/
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Begin to download the firmware list
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.fragmentContainer, new FirmwareListFragment(), "firmware")
                    .commit();
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_fimware, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        switch (id) {
            case R.id.action_settings:
                Log.d(TAG, "Firmware setting clicked");

                break;
            case R.id.action_clear:
                Log.d(Config.TAG, "Firmware clear clicked");

                clearLocalFiles("bin");

                FirmwareListFragment frag = (FirmwareListFragment)getSupportFragmentManager().findFragmentByTag("firmware");

                if ( frag != null) {
                    frag.getFirmwareFromServer();
                }

                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    @Override
    public void download(String url, String name) {
        Log.d(Config.TAG, "download " + url);

        SharedPreferences settings = getSharedPreferences(Config.TAG, 0);

        String site = settings.getString("site", "http://iot.boxshell.cn");

        File f = getFilesDir();
        Log.d(Config.TAG, "output to " + f.toString());

        new FetchFileTask(new JsonListener() {
            @Override
            public void callback(String s) {
                // update listFragment view to update file status
                if(s.equals("true")) {
                    Log.d(Config.TAG, "Download finished");
                }

                FirmwareListFragment frag = (FirmwareListFragment)getSupportFragmentManager().findFragmentByTag("firmware");

                if ( frag != null) {
                    frag.getFirmwareFromServer();
                }
                

            }
        },f.toString() + "/" + name).execute(site + url);
    }

    @Override
    public void cancelDownload() {
        Log.d(Config.TAG, "download canceled");
    }

    @Override
    public void showDownloadDlg(String name, String url) {
        FragmentManager fm = getSupportFragmentManager();
        SelectDownloadFragment  sdlFragment = new SelectDownloadFragment();
        sdlFragment.setFile(name);
        sdlFragment.setUrl(url);
        sdlFragment.show(fm, "selectDownload");
    }

    void clearLocalFiles(String ext){
        // to delete all files with ext as file extension

        File f = getFilesDir();
        Log.d(Config.TAG, f.toString());
        File file[] = f.listFiles();

        for(int i=0; i< file.length; i++){

            Log.d(Config.TAG, file[i].getName() + ":" + file[i].length());
            if(file[i].getName().contains("." + ext)){
                Log.d(Config.TAG, file[i].getName() + " is deleted");
                file[i].delete();
            }

        }

    }

    class FetchFileTask extends AsyncTask<String ,Integer, String> {

        public JsonListener mListener;
        private ProgressBar pBar;
        private String mOutputPath;

        //public String path;

        public FetchFileTask(JsonListener listener, String path){
            this.mListener = listener;
            this.mOutputPath = path;
        }
        @Override
        protected void onPreExecute() {
            android.support.v4.app.Fragment frag = getSupportFragmentManager().findFragmentByTag("firmware");
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

                // save it in a different place
                OutputStream output = new FileOutputStream(mOutputPath);

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
                /*Intent i = new Intent();
                i.setAction(Intent.ACTION_VIEW);
                i.setDataAndType(Uri.fromFile(new File(Config.APP_DOWNLOAD_NAME)), "application/vnd.android.package-archive");
                Log.d("Lofting", "About to install new .apk");
                //mListener.startActivity(i);
                startActivity(i);*/

            }
            pBar.setVisibility(View.GONE);

            mListener.callback(s);
            super.onPostExecute(s);
        }
    }
}
