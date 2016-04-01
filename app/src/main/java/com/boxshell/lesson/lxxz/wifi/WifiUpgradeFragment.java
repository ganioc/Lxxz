package com.boxshell.lesson.lxxz.wifi;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v4.app.ListFragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.TextView;
import android.widget.Toast;

import com.boxshell.lesson.lxxz.R;
import com.boxshell.lesson.lxxz.online.Config;
import com.boxshell.lesson.lxxz.online.FirmwareListItem;
import com.boxshell.lesson.lxxz.online.JsonListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link WifiUpgradeFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link WifiUpgradeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class WifiUpgradeFragment extends ListFragment implements AdapterView.OnItemClickListener,
        AdapterView.OnItemLongClickListener{
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;
    private List<FileListItem> mItems = new ArrayList<FileListItem>();

    public WifiUpgradeFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment WifiUpgradeFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static WifiUpgradeFragment newInstance(String param1, String param2) {
        WifiUpgradeFragment fragment = new WifiUpgradeFragment();
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
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_wifi_upgrade, container,false);




        //return inflater.inflate(R.layout.fragment_wifi_upgrade, container, false);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        new FetchLocalFirmwareTask(new JsonListener() {
            @Override
            public void callback(String s) {
                // parse json data create the list and display it


                try {
                    JSONObject jObj = new JSONObject(s);
                    JSONArray jArray = jObj.getJSONArray("content");

                    //mItems.clear();
                    Log.d(Config.TAG, "Get filename list from local:");

                    for(int i=0; i< jArray.length(); i++){
                        JSONObject obj = (JSONObject)jArray.get(i);
                        //fileList.add(obj.getString("filename"));
                        Log.d(Config.TAG, obj.getString("filename"));
                        mItems.add(new FileListItem(
                                ContextCompat.getDrawable(getContext(), android.R.drawable.ic_menu_save),
                                obj.getString("filename")
                        ));
                    }

                    if(mItems.size()>0){
                        setListAdapter(new FileListAdapter(getActivity(), mItems));
                        TextView txtView = (TextView)getActivity().findViewById(R.id.textWifiUpdateNo);
                        txtView.setVisibility(View.INVISIBLE);


                    }else{
                        TextView txtView = (TextView)getActivity().findViewById(R.id.textWifiUpdateNo);
                        txtView.setVisibility(View.VISIBLE);
                    }


                } catch (JSONException e) {
                    e.printStackTrace();
                }


            }
        }).execute("");

        getListView().setOnItemClickListener(this);
        getListView().setOnItemLongClickListener(this);

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
        // start your app here
    }

    @Override
    public void onPause() {
        super.onPause();
        // release your socket occupation here

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Log.d(Config.TAG, "Item: " + position);
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        TextView txtName = (TextView) view.findViewById(R.id.fileName);

        Log.d(Config.TAG,"Clicked:" + txtName.getText().toString());
        mListener.showUploadDialog(txtName.getText().toString());

        return false;
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
        public void showUploadDialog(String filename);
    }

    class FetchLocalFirmwareTask extends AsyncTask<String ,Integer, String> {
        public JsonListener mListener;

        public FetchLocalFirmwareTask(JsonListener listener){
            //fileList = new ArrayList<String>(){};
            mListener = listener; }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }



        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
        }

        @Override
        protected String doInBackground(String... params) {
            JSONObject jObj = new JSONObject();
            JSONArray jArray = new JSONArray();

            try {


                Log.d(Config.TAG, "into get frimware from local");
                String path = Environment.getExternalStorageDirectory().toString();
                Log.d(Config.TAG, path);

                File f = getContext().getFilesDir();
                Log.d(Config.TAG, f.toString());
                File file[] = f.listFiles();

                for (int i = 0; i < file.length; i++) {
                    JSONObject obj = new JSONObject();

                    String name = file[i].getName();

                    if(name.contains(".bin")) {
                        obj.put("filename", name);

                        jArray.put(obj);
                        Log.d(Config.TAG, file[i].getName() + ":" + file[i].length());
                    }
                }
                Log.d(Config.TAG, "out of get firmware from local");

                jObj.put("content", jArray);
            }catch (JSONException e){
                e.printStackTrace();
            }
            return jObj.toString();

        }

        @Override
        protected void onPostExecute(String s) {

            this.mListener.callback(s);

            super.onPostExecute(s);
        }
    }
}
