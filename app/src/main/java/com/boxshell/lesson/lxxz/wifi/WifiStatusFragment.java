package com.boxshell.lesson.lxxz.wifi;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
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
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link WifiStatusFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link WifiStatusFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class WifiStatusFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;
    //private Socket mSocket;

    public WifiStatusFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment WifiStatusFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static WifiStatusFragment newInstance(String param1, String param2) {
        WifiStatusFragment fragment = new WifiStatusFragment();
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
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        Log.d(Config.TAG, "into wifistatusfragment");
        View view = inflater.inflate(R.layout.fragment_wifi_status, container, false);

        TextView idView = (TextView) view.findViewById(R.id.wifiStatusId);
        TextView ipView = (TextView) view.findViewById(R.id.wifiStatusIp);
        TextView macView = (TextView) view.findViewById(R.id.wifiStatusMac);

        Log.d(Config.TAG, "id is:" + mListener.getCurId());
        Log.d(Config.TAG, "ip is:" + mListener.getCurIp());
        Log.d(Config.TAG, "mac is:" + mListener.getCurMac());

        idView.setText(mListener.getCurId());
        ipView.setText(mListener.getCurIp());
        macView.setText(mListener.getCurMac());

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // get more information through a TCP link
        //mListener.initSocket();


    }

    @Override
    public void onResume() {
        super.onResume();

        if(!mListener.isWifiConnected()){
            Toast.makeText(getContext(), "请打开WIFI", Toast.LENGTH_LONG).show();
            return;
        }
        else {
            //mListener.getDevStatus();
            getStatus();
        }

    }

    @Override
    public void onPause() {
        super.onPause();
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

    public void getStatus(){

        /*if(!mListener.isWifiConnected()){
            Toast.makeText(getContext(), "请打开WIFI", Toast.LENGTH_LONG).show();
            return;
        }*/

        try {
            JSONObject jObj = new JSONObject();


            jObj.put("CMD", "RD");
            jObj.put("TARGET", "ROLE");
            jObj.put("CONTENT", "");


            new FetchTcpPacketTask(new JsonListener() {
                @Override
                public void callback(String s) {
                    Log.d(Config.TAG, "received tcp data: " + s);

                    try {

                        JSONObject obj = new JSONObject(s);
                        TextView roleView = (TextView) getActivity().findViewById(R.id.wifiStatusRole);
                        roleView.setText(obj.getString("CONTENT"));


                        JSONObject jObj2 = new JSONObject();
                        jObj2.put("CMD", "RD");
                        jObj2.put("TARGET", "STATE");
                        jObj2.put("CONTENT", "FWVER");

                        new FetchTcpPacketTask(new JsonListener() {
                            @Override
                            public void callback(String s) {
                                try {
                                    JSONObject obj = new JSONObject(s);
                                    TextView roleView = (TextView) getActivity().findViewById(R.id.wifiStatusFwVer);
                                    roleView.setText(obj.getString("CONTENT"));

                                }catch (JSONException e){
                                    e.printStackTrace();
                                }

                            }
                        }).execute(jObj2.toString());

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }
            }).execute(jObj.toString());
        }catch (JSONException e){
            e.printStackTrace();
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

        String getCurId();

        String getCurIp();

        String getCurMac();

        //void getDevStatus();

        //void initSocket();
        public boolean isWifiConnected();

    }

    class FetchTcpPacketTask extends AsyncTask<String, Integer, String> {
        public JsonListener mListen;
        private Socket mSocket;

        public FetchTcpPacketTask(JsonListener listener) {
            this.mListen = listener;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

        }

        @Override
        protected String doInBackground(String... params) {
            try {
                //String message = params[0];
                mSocket = new Socket( mListener.getCurIp(), Config.TCP_PORT);
                mSocket.setSoTimeout(Config.TCP_TIMEOUT);

                Log.d(Config.TAG, "After socket setup");

                OutputStream out = mSocket.getOutputStream();

                PrintWriter output = new PrintWriter(out);

                //Log.d(Config.TAG, jObj.toString());

                output.println(params[0]);

                output.flush();

                BufferedReader input = new BufferedReader(new InputStreamReader(mSocket.getInputStream()));

                Log.d(Config.TAG, "After create read buffer");
                //read line(s)
                String line;

                /*while( (line= input.readLine()) != null){
                    Log.d(Config.TAG, "Read:" + line);
                }*/

                line = input.readLine();

                return line;


            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(String s) {

            super.onPostExecute(s);
            try {
                if(mSocket!= null) {
                    mSocket.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            mListen.callback(s);
        }
    }
}