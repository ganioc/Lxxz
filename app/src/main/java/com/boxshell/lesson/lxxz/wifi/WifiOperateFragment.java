package com.boxshell.lesson.lxxz.wifi;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.preference.CheckBoxPreference;
import android.support.v7.preference.Preference;
import android.support.v4.app.Fragment;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.boxshell.lesson.lxxz.R;
import com.boxshell.lesson.lxxz.online.Config;
import com.boxshell.lesson.lxxz.online.JsonListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link WifiOperateFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link WifiOperateFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class WifiOperateFragment extends PreferenceFragmentCompat {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    public static Socket mSocket;

    public WifiOperateFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment WifiOperateFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static WifiOperateFragment newInstance(String param1, String param2) {
        WifiOperateFragment fragment = new WifiOperateFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

        addPreferencesFromResource(R.xml.wifi_operate_preference);

        Preference reset = getPreferenceManager().findPreference("resetButton");
        reset.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Log.d(Config.TAG, "reset clicked");

                restartDev();

                return true;
            }
        });

        Preference savereset = getPreferenceManager().findPreference("saveResetButton");
        savereset.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Log.d(Config.TAG, "savereset clicked");

                saveDev();

                return true;
            }
        });

        CheckBoxPreference checkPref = (CheckBoxPreference) getPreferenceManager().findPreference("wifi_operate_mode");
        checkPref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(final Preference preference, Object o) {
                boolean bSwitch = o.toString().equals("true");

                if (bSwitch) {
                    new FetchTcpPacketTask(new JsonListener() {
                        @Override
                        public void callback(String s) {
                            if (s.length() > 0) {
                                Log.d(Config.TAG, "0 back");
                                Toast.makeText(getContext(), "改为AP", Toast.LENGTH_SHORT).show();
                                preference.setSummary(R.string.wifi_start_ap);
                            }
                        }
                    }).execute(getStringRoleAp());
                } else {
                    new FetchTcpPacketTask(new JsonListener() {
                        @Override
                        public void callback(String s) {
                            if (s.length() > 0) {
                                Log.d(Config.TAG, "1 back");
                                Toast.makeText(getContext(), "改为STA", Toast.LENGTH_SHORT).show();
                                preference.setSummary(R.string.wifi_start_sta);
                            }
                        }
                    }).execute(getStringRoleSta());
                }
                return true;
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

       /* Button resetBtn = (Button)getActivity().findViewById(R.id.wifi_operate_reset_btn);
        resetBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(Config.TAG, "reset button clicked");
            }
        });*/
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onCreatePreferences(Bundle bundle, String s) {


    }/*

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_wifi_operate, container, false);
    }*/

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onResume() {
        super.onResume();

        if(!mListener.isWifiConnected()){
            Toast.makeText(getContext(),"请打开WIFI", Toast.LENGTH_LONG).show();
            return;
        }

        // start the socket
        new CreateSocketTask(new JsonListener() {
            @Override
            public void callback(String s) {
                if (mSocket == null) {
                    Log.d(Config.TAG, "Can't connect to the device, socket create fail");
                } else {
                    try {
                        JSONObject jObj = new JSONObject();


                        jObj.put("CMD", "RD");
                        jObj.put("TARGET", "ROLE");
                        jObj.put("CONTENT", "");

                        new FetchTcpPacketTask(new JsonListener() {
                            @Override
                            public void callback(String s) {
                                Log.d(Config.TAG, "received role:" + s);

                                if (s.length() > 0) {
                                    try {
                                        JSONObject obj = new JSONObject(s);
                                        String role = obj.getString("CONTENT");

                                        CheckBoxPreference pref = (CheckBoxPreference) getPreferenceManager().findPreference("wifi_operate_mode");

                                        if (pref != null) {

                                        } else {
                                            return;
                                        }

                                        if (role.equals("ROLE_AP")) {
                                            pref.setSummary(R.string.wifi_start_ap);
                                            pref.setChecked(true);
                                        } else if (role.equals("ROLE_STA")) {
                                            pref.setSummary(R.string.wifi_start_sta);
                                            pref.setChecked(false);
                                        } else {
                                            Log.d(Config.TAG, "Role name not known");
                                        }
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }

                                }
                            }
                        }).execute(jObj.toString());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }
            }
        }).execute("");

    }

    @Override
    public void onPause() {
        super.onPause();

        // destroy the socket
        try {
            if (mSocket != null) {
                mSocket.close();
                Log.d(Config.TAG, "socked closed");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    String getStringRoleAp() {
        try {
            JSONObject obj = new JSONObject();
            obj.put("CMD", "WR");
            obj.put("TARGET", "ROLE");
            obj.put("CONTENT", "ROLE_AP");

            return obj.toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return null;
    }

    String getStringRoleSta() {
        try {
            JSONObject obj = new JSONObject();
            obj.put("CMD", "WR");
            obj.put("TARGET", "ROLE");
            obj.put("CONTENT", "ROLE_STA");

            return obj.toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return null;
    }

    String getStringRestart() {
        try {
            JSONObject obj = new JSONObject();
            obj.put("CMD", "WR");
            obj.put("TARGET", "BOOT");
            obj.put("CONTENT", "");

            return obj.toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return null;
    }
    String getStringSave() {
        try {
            JSONObject obj = new JSONObject();
            obj.put("CMD", "WR");
            obj.put("TARGET", "SAVE");
            obj.put("CONTENT", "");

            return obj.toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return null;
    }

    void restartDev(){
        new FetchTcpPacketTask(new JsonListener() {
            @Override
            public void callback(String s) {

                if (s == null) {
                    Toast.makeText(getContext(), "重启中", Toast.LENGTH_SHORT).show();

                    Thread thread = new Thread() {
                        @Override
                        public void run() {
                            try {
                                Thread.sleep(2500); // As I am using LENGTH_LONG in Toast
                                //Your_Activity.this.finish();
                                mListener.die();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    };
                    thread.start();

                    return;
                } else if (s.length() > 0) {
                    Toast.makeText(getContext(), "重启", Toast.LENGTH_SHORT).show();


                }
            }
        }).execute(getStringRestart());
    }

    void saveDev(){

        new FetchTcpPacketTask(new JsonListener() {
            @Override
            public void callback(String s) {
                if(s == null){

                }else if(s.length()>0){
                    Log.d(Config.TAG, "get save response:" + s);
                    Toast.makeText(getContext(), "保存中", Toast.LENGTH_LONG).show();

                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            //Do something after 100ms
                            restartDev();
                        }
                    }, 2500);

                }
            }
        }).execute(getStringSave());
    }
    /*
    public void onResetClick(){
        Log.d(Config.TAG, "reset button clicked");
    }
*/

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);

        String getCurId();

        String getCurIp();
        void  die();
        public boolean isWifiConnected();
    }

    class CreateSocketTask extends AsyncTask<String, Integer, String> {

        public JsonListener mListen;

        public CreateSocketTask(JsonListener liste) {
            this.mListen = liste;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... params) {
            try {
                if (WifiOperateFragment.mSocket != null) {
                    WifiOperateFragment.mSocket.close();
                }

                WifiOperateFragment.mSocket = new Socket(mListener.getCurIp(), Config.TCP_PORT);
                WifiOperateFragment.mSocket.setSoTimeout(Config.TCP_TIMEOUT);

                Log.d(Config.TAG, "After socket setup");
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            this.mListen.callback(s);
        }
    }

    class FetchTcpPacketTask extends AsyncTask<String, Integer, String> {
        public JsonListener mListen;

        public FetchTcpPacketTask(JsonListener listener) {
            this.mListen = listener;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

        }

        protected String doInBackground(String... params) {
            try {

                OutputStream out = mSocket.getOutputStream();

                PrintWriter output = new PrintWriter(out);

                //Log.d(Config.TAG, jObj.toString());

                output.println(params[0]);

                output.flush();

                BufferedReader input = new BufferedReader(new InputStreamReader(mSocket.getInputStream()));

                String line = input.readLine();

                Log.d(Config.TAG, line);

                return line;

            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            this.mListen.callback(s);
        }
    }
}
