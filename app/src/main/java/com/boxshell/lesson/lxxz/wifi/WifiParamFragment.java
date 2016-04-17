package com.boxshell.lesson.lxxz.wifi;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.preference.EditTextPreference;
import android.support.v7.preference.ListPreference;
import android.support.v4.app.Fragment;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.boxshell.lesson.lxxz.R;
import com.boxshell.lesson.lxxz.online.Config;
import com.boxshell.lesson.lxxz.online.JsonListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link WifiParamFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link WifiParamFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class WifiParamFragment extends PreferenceFragmentCompat {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    private static Socket mSocket;

    public WifiParamFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment WifiParamFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static WifiParamFragment newInstance(String param1, String param2) {
        WifiParamFragment fragment = new WifiParamFragment();
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
        addPreferencesFromResource(R.xml.wifi_param_preference);

        // door open duration
        final ListPreference lstDoorDur = (ListPreference) getPreferenceManager().findPreference("wifi_param_doordur");
        lstDoorDur.setSummary(lstDoorDur.getEntry());
        lstDoorDur.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object o) {
                ListPreference listPreference = (ListPreference) preference;
                ((ListPreference) preference).setValue(o.toString());
                listPreference.setSummary(listPreference.getEntry());

                new FetchTcpPacketTask(new JsonListener() {
                    @Override
                    public void callback(String s) {
                        if (s == null) {
                            Log.d(Config.TAG, "update doordur failed");
                        }
                    }
                }).execute(Config.strPacketSet("STATE", "DOORDUR", lstDoorDur.getValue()));

                return true;
            }
        });

        // srcid
        final EditTextPreference srcidPref = (EditTextPreference) getPreferenceManager().findPreference("wifi_param_srcid");
        srcidPref.setSummary(srcidPref.getText());
        srcidPref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object o) {
                ((EditTextPreference)srcidPref).setText(o.toString());
                preference.setSummary(o.toString());

                new FetchTcpPacketTask(new JsonListener() {
                    @Override
                    public void callback(String s) {
                        if(s == null){
                            Log.d(Config.TAG, "update srcid failed");
                        }
                    }
                }).execute(Config.strPacketSet("STATE", "SRCID", srcidPref.getText()));

                return true;
            }
        });

        // doorcode
        final EditTextPreference doorcodePref = (EditTextPreference) getPreferenceManager().findPreference("wifi_param_doorcode");
        doorcodePref.setSummary(doorcodePref.getText());
        doorcodePref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object o) {
                ((EditTextPreference)doorcodePref).setText(o.toString());
                preference.setSummary(o.toString());

                new FetchTcpPacketTask(new JsonListener() {
                    @Override
                    public void callback(String s) {
                        if(s == null){
                            Log.d(Config.TAG, "update doorcode failed");
                        }
                    }
                }).execute(Config.strPacketSet("STATE", "DOORCODE", doorcodePref.getText()));

                return true;
            }
        });

        // ap ssid
        EditTextPreference apssidPref = (EditTextPreference) getPreferenceManager().findPreference("wifi_param_apssid");
        apssidPref.setSummary(apssidPref.getText());
        apssidPref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object o) {
                Log.d(Config.TAG, "ap ssid is:" + o.toString());
                try {
                    String strTemp = o.toString();
                    URLEncoder.encode(strTemp, "utf-8");
                    Log.d(Config.TAG, strTemp);
                    Log.d(Config.TAG,String.valueOf(strTemp.length()));
                    byte[] utf8Bytes = strTemp.getBytes("utf-8");
                    Log.d(Config.TAG, String.valueOf(utf8Bytes.length));
                    for(int i=0; i< utf8Bytes.length; i++){
                        Log.d(Config.TAG,String.valueOf(utf8Bytes[i]));
                    }

                    new FetchTcpPacketTask(new JsonListener() {
                        @Override
                        public void callback(String s) {
                            if(s == null){
                                Log.d(Config.TAG, "update ap ssid failed");
                            }
                        }
                    }).execute(Config.strPacketSet("STATE", "APSSID", strTemp));

                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                preference.setSummary(o.toString());



                return true;
            }
        });

        // ap ip
        EditTextPreference apipPref = (EditTextPreference) getPreferenceManager().findPreference("wifi_param_ap_ip");
        apipPref.setSummary(apipPref.getText());
        apipPref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object o) {
                ((EditTextPreference)preference).setText(o.toString());
                preference.setSummary(o.toString());

                new FetchTcpPacketTask(new JsonListener() {
                    @Override
                    public void callback(String s) {
                        if(s == null){
                            Log.d(Config.TAG, "update ap ip failed");
                        }
                    }
                }).execute(Config.strPacketSet("STATE", "APIP", ((EditTextPreference) preference).getText() ));
                return true;
            }
        });

        //ap netmask
        EditTextPreference apnetmask = (EditTextPreference) getPreferenceManager().findPreference("wifi_param_ap_netmask");
        apnetmask.setSummary(apnetmask.getText());
        apnetmask.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object o) {
                ((EditTextPreference)preference).setText(o.toString());
                preference.setSummary(o.toString());
                new FetchTcpPacketTask(new JsonListener() {
                    @Override
                    public void callback(String s) {
                        if(s == null){
                            Log.d(Config.TAG, "update ap netmask failed");
                        }
                    }
                }).execute(Config.strPacketSet("STATE", "APNETMASK", ((EditTextPreference) preference).getText()));

                return true;
            }
        });

        // ap security type
        ListPreference apListPref = (ListPreference) getPreferenceManager().findPreference("wifi_param_ap_security");
        apListPref.setSummary(apListPref.getEntry());

        // ap security key
        EditTextPreference apKeyPref = (EditTextPreference) getPreferenceManager().findPreference("wifi_param_ap_security_key");
        apKeyPref.setSummary(apKeyPref.getText());
        apKeyPref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object o) {
                ((EditTextPreference)preference).setText(o.toString());
                preference.setSummary(o.toString());
                new FetchTcpPacketTask(new JsonListener() {
                    @Override
                    public void callback(String s) {
                        if(s == null){
                            Log.d(Config.TAG, "update ap key failed");
                        }
                    }
                }).execute(Config.strPacketSet("STATE", "APKEY", ((EditTextPreference) preference).getText()));

                return true;
            }
        });

        // sta ssid
        EditTextPreference stassidPref = (EditTextPreference) getPreferenceManager().findPreference("wifi_param_stassid");
        stassidPref.setSummary(stassidPref.getText());
        stassidPref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object o) {
                ((EditTextPreference)preference).setText(o.toString());
                preference.setSummary(o.toString());



                new FetchTcpPacketTask(new JsonListener() {
                    @Override
                    public void callback(String s) {
                        if(s == null){
                            Log.d(Config.TAG, "update sta ssid failed");
                        }
                    }
                }).execute(Config.strPacketSet("STATE", "STASSID", ((EditTextPreference) preference).getText()));

                return true;
            }
        });
        // sta security type
        ListPreference staListPref = (ListPreference) getPreferenceManager().findPreference("wifi_param_sta_security");
        staListPref.setSummary(staListPref.getEntry());

        // sta security key
        EditTextPreference staKeyPref = (EditTextPreference) getPreferenceManager().findPreference("wifi_param_sta_security_key");
        staKeyPref.setSummary(staKeyPref.getText());
        staKeyPref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object o) {
                ((EditTextPreference)preference).setText(o.toString());
                preference.setSummary(o.toString());
                new FetchTcpPacketTask(new JsonListener() {
                    @Override
                    public void callback(String s) {
                        if(s == null){
                            Log.d(Config.TAG, "update sta key failed");
                        }
                    }
                }).execute(Config.strPacketSet("STATE", "STAKEY", ((EditTextPreference) preference).getText()));

                return true;
            }
        });
    }

    @Override
    public void onCreatePreferences(Bundle bundle, String s) {


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        return super.onCreateView(inflater, container, savedInstanceState);
    }

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
            Toast.makeText(getContext(), "请打开WIFI", Toast.LENGTH_LONG).show();
            return;
        }

        new CreateSocketTask(new JsonListener() {
            @Override
            public void callback(String s) {
                if(mSocket == null){
                    Log.d(Config.TAG, "Socket create failure.");
                    return;
                }
                new FetchDevInfoTask(new JsonListener() {
                    @Override
                    public void callback(String s) {

                        if(s == null){
                            Log.d(Config.TAG, "fail to get info from dev");
                            return;
                        }
                        Log.d(Config.TAG, "Loop info:" + s);

                        // use this info to update current parameters
                        udpdatePreferences(s);

                    }
                }).execute("");

            }
        }).execute();

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
    private void udpdatePreferences(String s){

        try{
            JSONObject jObj = new JSONObject(s);
            JSONArray jArr = jObj.getJSONArray("contentArray");

            for(int i=0; i< jArr.length(); i++){

                JSONObject jItem = (JSONObject) jArr.get(i);
                updatePrefer(jItem);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    private void updatePrefer(JSONObject obj){
        try {
            if(obj.getString("STATUS").equals("NOK")) {
                Log.d(Config.TAG, "Wrong received packet:"+ obj.toString());
                return;
            }

            if(obj.getString("TARGET").equals("STATE")){
                String subtarget = obj.getString("SUBTARGET");

                if(subtarget.equals("DOORDUR")){
                    ListPreference lstDoorDur = (ListPreference) getPreferenceManager().findPreference("wifi_param_doordur");
                    lstDoorDur.setValue(obj.getString("CONTENT"));
                    lstDoorDur.setSummary(lstDoorDur.getEntry());
                }
                else if(subtarget.equals("SRCID")){
                    EditTextPreference srcidPref = (EditTextPreference) getPreferenceManager().findPreference("wifi_param_srcid");
                    srcidPref.setText(obj.getString("CONTENT"));
                    srcidPref.setSummary(srcidPref.getText());
                }
                else if(subtarget.equals("DOORCODE")){
                    EditTextPreference doorcodePref = (EditTextPreference) getPreferenceManager().findPreference("wifi_param_doorcode");
                    doorcodePref.setText(obj.getString("CONTENT"));
                    doorcodePref.setSummary(doorcodePref.getText());
                }
                else if(subtarget.equals("APSSID")){
                    EditTextPreference apssidPref = (EditTextPreference) getPreferenceManager().findPreference("wifi_param_apssid");
                    apssidPref.setText(obj.getString("CONTENT"));
                    apssidPref.setSummary(apssidPref.getText());
                }
                else if(subtarget.equals("APIP")){
                    EditTextPreference apipPref = (EditTextPreference) getPreferenceManager().findPreference("wifi_param_ap_ip");
                    apipPref.setText(obj.getString("CONTENT"));
                    apipPref.setSummary(apipPref.getText());
                }
                else if(subtarget.equals("APNETMASK")){
                    EditTextPreference apnetmask = (EditTextPreference) getPreferenceManager().findPreference("wifi_param_ap_netmask");
                    apnetmask.setText(obj.getString("CONTENT"));
                    apnetmask.setSummary(apnetmask.getText());
                }
                else if(subtarget.equals("APKEY")){
                    EditTextPreference apKeyPref = (EditTextPreference) getPreferenceManager().findPreference("wifi_param_ap_security_key");
                    apKeyPref.setText(obj.getString("CONTENT"));
                    apKeyPref.setSummary(apKeyPref.getText());
                }
                else if(subtarget.equals("STASSID")){
                    EditTextPreference stassidPref = (EditTextPreference) getPreferenceManager().findPreference("wifi_param_stassid");
                    stassidPref.setText(obj.getString("CONTENT"));
                    stassidPref.setSummary(stassidPref.getText());
                }
                else if(subtarget.equals("STAKEY")){
                    EditTextPreference staKeyPref = (EditTextPreference) getPreferenceManager().findPreference("wifi_param_sta_security_key");
                    staKeyPref.setText(obj.getString("CONTENT"));
                    staKeyPref.setSummary(staKeyPref.getText());
                }
            }else{
                Log.d(Config.TAG, "unrecognized packet type");
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }


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
                if (mSocket != null) {
                    mSocket.close();
                }

                mSocket = new Socket(mListener.getCurIp(), Config.TCP_PORT);
                mSocket.setSoTimeout(Config.TCP_TIMEOUT);

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

                Log.d(Config.TAG, "Received TCP packet:" + line);

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

    class FetchDevInfoTask extends AsyncTask<String, Integer, String> {
        public JsonListener mListen;
        private  List<String> mPackets = new ArrayList<String>(){};

        public FetchDevInfoTask(JsonListener listener) {
            this.mListen = listener;

            mPackets.add(Config.strPacketGet("STATE", "DOORDUR"));
            mPackets.add(Config.strPacketGet("STATE", "SRCID"));
            mPackets.add(Config.strPacketGet("STATE", "APSSID"));
            mPackets.add(Config.strPacketGet("STATE", "APIP"));
            mPackets.add(Config.strPacketGet("STATE", "APNETMASK"));
            mPackets.add(Config.strPacketGet("STATE", "APKEY"));
            mPackets.add(Config.strPacketGet("STATE", "STASSID"));
            mPackets.add(Config.strPacketGet("STATE", "STAKEY"));
            mPackets.add(Config.strPacketGet("STATE", "DOORCODE"));
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

        }

        protected String doInBackground(String... params) {
            try {

                OutputStream out = mSocket.getOutputStream();
                BufferedReader input = new BufferedReader(new InputStreamReader(mSocket.getInputStream()));

                PrintWriter output = new PrintWriter(out);
                String line;

                //Log.d(Config.TAG, jObj.toString());

                JSONObject jObj = new JSONObject();
                JSONArray  jArr = new JSONArray();

                for(int i=0; i< mPackets.size();i++ ){

                    output.println(mPackets.get(i).toString());
                    output.flush();
                    line = input.readLine();
                    Log.d(Config.TAG, "Received TCP packet:" + line);
                    try {

                        JSONObject tempObject = new JSONObject(line);

                        jArr.put(tempObject);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }
                try {
                    jObj.put("contentArray", jArr);

                    return jObj.toString();
                }catch (JSONException e){
                    e.printStackTrace();
                }
                return null;

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
        String getCurIp();
        void  die();
        public boolean isWifiConnected();
    }
}
