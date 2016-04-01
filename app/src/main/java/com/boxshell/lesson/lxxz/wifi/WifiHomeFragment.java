package com.boxshell.lesson.lxxz.wifi;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.boxshell.lesson.lxxz.R;
import com.boxshell.lesson.lxxz.online.Config;
import com.boxshell.lesson.lxxz.online.JsonListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link WifiHomeFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link WifiHomeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class WifiHomeFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;
    //private ProgressBar mProgressBar;

    public WifiHomeFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment WifiHomeFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static WifiHomeFragment newInstance(String param1, String param2) {
        WifiHomeFragment fragment = new WifiHomeFragment();
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
        Log.d(Config.TAG, "Into wifihomefragment oncreateview");

        View view = inflater.inflate(R.layout.fragment_wifi_home, container, false);



        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        pingDevice();

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

/*        mProgressBar = (ProgressBar)getActivity().findViewById(R.id.progressBarWifi);
        mProgressBar.setVisibility(View.VISIBLE);*/



    }

    /**
     * ping device on the WIFI network
     */
    private void pingDevice(){
        if (mListener.isWifiConnected()) {
            // if wifi is connected do search work
            Log.d(Config.TAG, "Wifi network is connected");
            // send tcp packet out to gather information

            try {
                mListener.getIpAddress(new JsonListener() {
                    @Override
                    public void callback(String s) {
                        Log.d(Config.TAG, "Get Ip address is:");
                        Log.d(Config.TAG, s);

                        // then get udp broadcast result
                        mListener.udpBroadcast(new JsonListener() {
                            @Override
                            public void callback(String s) {
                                Log.d(Config.TAG, "Get udp broadcast info");

                                Log.d(Config.TAG,s);

                                // if there is no feedback
                                if(s==null){
                                    ProgressBar pBar = (ProgressBar)getActivity().findViewById(R.id.progressBarWifi);
                                    pBar.setVisibility(View.GONE);

                                    Log.d(Config.TAG, "Can not find the device");
                                    // change to WifiNoneFragment
                                }

                                try {
                                    JSONObject jObj = new JSONObject(s);
                                    JSONArray aObj = jObj.getJSONArray("dev");
                                    Log.d(Config.TAG, "dev num is: " + String.valueOf(aObj.length()));

                                    if(aObj.length() == 0){
                                        // switch to WifiNoneFragment
                                        mListener.wifiNotConnected();

                                    }else {

                                        JSONObject devObj = (JSONObject) aObj.get(0);

                                        Log.d(Config.TAG, devObj.getString("srcid"));
                                        Log.d(Config.TAG, devObj.getString("ip"));

                                        // display the dialog
                                        mListener.showDevDialog(s);
                                    }

                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                //otherwise
                            }
                        });
                    }
                });
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else {
            mListener.wifiNotConnected();
        }
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

        public boolean isWifiConnected();

        void wifiNotConnected();

        public void getIpAddress(JsonListener jListener) throws JSONException;

        public void udpBroadcast(JsonListener jListener);

        public void showDevDialog(String s);
    }


}
