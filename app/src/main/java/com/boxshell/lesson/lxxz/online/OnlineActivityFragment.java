package com.boxshell.lesson.lxxz.online;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.boxshell.lesson.lxxz.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * A placeholder fragment containing a simple view.
 */
public class OnlineActivityFragment extends Fragment implements View.OnClickListener {
    private static final String TAG = "lxxz";
    //private Button btnOffline;
    private OnFragmentInteractionListener mListener;
    // CardView cardFirmware, cardHelp;
    private ProgressBar mProgressBar;

    public OnlineActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        Log.d(TAG, "into OnlineActivityFragment onCreateView()");

        return inflater.inflate(R.layout.fragment_online, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        /*btnOffline = (Button)getActivity().findViewById(R.id.btnOffline);
        btnOffline.setOnClickListener(this);*/

        ((CardView)getActivity().findViewById(R.id.cardFirmware)).setOnClickListener(this);
        ((CardView)getActivity().findViewById(R.id.cardHelp)).setOnClickListener(this);
        mProgressBar = (ProgressBar)getActivity().findViewById(R.id.progressBar);
        mProgressBar.setVisibility(View.GONE);


        // Start the network checking facility here
        if(mListener.bNeedCheckNetworkAvailability()) {
            checkNetworkAvailability();
        }

        super.onActivityCreated(savedInstanceState);
    }

    public void checkNetworkAvailability(){
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                new checkNetworkTask(new JsonListener() {
                    @Override
                    public void callback(String s) {
                        if (s.equals("true")) {
                            Log.d(TAG, "Network available");
                            // change fab color
                            mListener.markFab();

                            // then start a task to visit the site  fetch a JSON string
                            new FetchJsonTask(new JsonListener() {
                                @Override
                                public void callback(String s) {
                                    Log.d(Config.TAG, "Fetch JSON succeed");
                                    Log.d(Config.TAG, s);
                                    if (s.length() > 0) {
                                        try {
                                            JSONObject jsonObj = new JSONObject(s);
                                            mListener.getConfig().setSite(jsonObj.getString("site"));

                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        } finally {

                                        }

                                    }
                                    // if needs to check update
                                        checkAppUpdate();
                                }
                            }).execute(Config.ROOT_URL);

                        } else {
                            Log.d(TAG, "Network unavailable");
                            //change fab color
                            mListener.demarkFab();

                            //mListener.onFragmentInteractionOffline();
                        }
                    }
                }).execute(Config.PING_URL);
            }
        }, 100);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
/*            case R.id.btnOffline:
                Log.d(TAG, "btn OffLine");
                mListener.onFragmentInteractionOffline();
                break;*/
            case R.id.cardFirmware:
                Log.d(TAG, "firmware clicked");
                Intent intent = new Intent(getActivity(), FirmwareActivity.class);
                startActivity(intent);
                break;
            case R.id.cardHelp:
                Log.d(TAG, "help clicked");
                Intent intent1 = new Intent(getActivity(), HelpActivity.class);
                startActivity(intent1);
                break;
            default:
                break;
        }


    }

    @Override
    public void onAttach(Context context) {

        mListener = (OnFragmentInteractionListener)context;
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        mListener = null;

        super.onDetach();
    }

    public interface OnFragmentInteractionListener{
        public void onFragmentInteractionOffline();
        public Config getConfig();
        public boolean bNeedCheckNetworkAvailability();
        public boolean bNeedCheckUpdate();
        public void markFab();
        public void demarkFab();
        //public boolean bInternetWorking();
    }

    public ProgressBar getProgressBar(){
        return mProgressBar;
    }

    private class checkNetworkTask extends AsyncTask<String ,Integer, String>{
        public JsonListener mListener;

        public checkNetworkTask(JsonListener listener){
            this.mListener = listener;
        }

        @Override
        protected String doInBackground(String... params) {
            boolean success = false;
            try {
                URL url = new URL(params[0]);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setConnectTimeout(5000);
                connection.connect();
                success = connection.getResponseCode() == 200;
            } catch (IOException e) {
                e.printStackTrace();
            }
            return String.valueOf(success);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
        }

        @Override
        protected void onPostExecute(String s) {
            mListener.callback(s);
            super.onPostExecute(s);
        }
    }

    private void checkAppUpdate(){

        if(mListener.getConfig().bNeedUpdateAPP) {
            new FetchJsonTask(new JsonListener() {
                @Override
                public void callback(String s) {
                    Log.d(Config.TAG, "get app version");
                    Log.d(Config.TAG,s);
                    // display a dialog , whether to download the new APP
                    if(s.length()>0){
                        try{
                            JSONObject jsonObj = new JSONObject(s);
                            String ver = jsonObj.getString("version");
                            Float newVer = Float.valueOf(ver);
                            Float curVer = Float.valueOf(mListener.getConfig().APP_VERSION);
                            String url = jsonObj.getString("url"); // APP download url

                            if(curVer < newVer){
                                Log.d(Config.TAG, "Do you want to update APP " + ver);

                                // Bring out a dialog
                                FragmentManager fm = getActivity().getSupportFragmentManager();
                                SelectDialogFragment  sdfragment = new SelectDialogFragment();
                                sdfragment.setVersion(ver);
                                sdfragment.setUrl(url);
                                sdfragment.show(fm, "selectDialog");

                            }
                            else{
                                Log.d(Config.TAG, "No need to update APP");
                                mListener.getConfig().bNeedUpdateAPP = false;
                            }

                        }catch (JSONException e){
                            e.printStackTrace();
                        }finally {

                        }

                    }else {
                        Log.d(Config.TAG, "Can't get any thing");
                    }

                }
            }).execute(mListener.getConfig().getSite()  + Config.APP_URL);
        }
    }

}
