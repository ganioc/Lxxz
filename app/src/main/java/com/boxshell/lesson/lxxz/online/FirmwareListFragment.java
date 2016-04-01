package com.boxshell.lesson.lxxz.online;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.ListFragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.NestedScrollView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.boxshell.lesson.lxxz.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link FirmwareListFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link FirmwareListFragment#newInstance} factory method to
 * create an instance of this fragment.
 *
 * async listview
 * custom listview list item
 *
 */
public class FirmwareListFragment extends ListFragment implements AdapterView.OnItemClickListener,
        AdapterView.OnItemLongClickListener{
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private String mSite;

    private OnFragmentInteractionListener mListener;

    /*String[] numbers_text = new String[] { "one", "two", "three", "four",
            "five", "six", "seven", "eight", "nine", "ten", "eleven",
            "twelve", "thirteen", "fourteen", "fifteen" };
    String[] numbers_digits = new String[] { "1", "2", "3", "4", "5", "6", "7",
            "8", "9", "10", "11", "12", "13", "14", "15" };
    String[] firmware_list;*/

    private List<FirmwareListItem> mItems;

    public FirmwareListFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment FirmwareListFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static FirmwareListFragment newInstance(String param1, String param2) {
        FirmwareListFragment fragment = new FirmwareListFragment();
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

        SharedPreferences settings = getContext().getSharedPreferences(Config.TAG, 0);

        mSite = settings.getString("site","http://iot.boxshell.cn");// root network address

        Log.d(Config.TAG, "Current site is:" + mSite);
    }

    /**
     * android.R.layout.simple_list_item_1
     * @param inflater
     * @param container
     * @param savedInstanceState
     * @return
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_firmware_list,container,false);

        // Below code is very important to make the UI shrink smoothly
        NestedScrollView nsv = (NestedScrollView)getActivity().findViewById(R.id.scrollviewNested);
        nsv.setFillViewport(true);



        //return inflater.inflate(R.layout.fragment_firmware_list, container, false);
        //return super.onCreateView(inflater,container,savedInstanceState);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        getFirmwareFromServer();

        getListView().setOnItemClickListener(this);
        getListView().setOnItemLongClickListener(this);

        ProgressBar pBar = (ProgressBar)getActivity().findViewById(R.id.progressBar);
        pBar.setVisibility(View.GONE);

    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {

        //Log.d(Config.TAG, numbers_digits[(int) id]);

        super.onListItemClick(l, v, position, id);
    }

    @Override
    public void onAttach(Context context) {
        mListener = (OnFragmentInteractionListener) context;
        super.onAttach(context);

    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Log.d(Config.TAG, "Item: " + position);
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        Log.d(Config.TAG, "Item long clicked:" + position);
        TextView txtView = (TextView) view.findViewById(R.id.firmwareDescrip);
        TextView txtName = (TextView) view.findViewById(R.id.firmwareName);
        Log.d(Config.TAG, txtView.getText().toString());

        mListener.showDownloadDlg(txtName.getText().toString(), txtView.getText().toString());

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
        void showDownloadDlg(String name, String url );
    }
    private List<String> getFirmwareFromLocal(){

        List<String> fileList = new ArrayList<String>(){};

        Log.d(Config.TAG, "into get frimware from local");
        String path = Environment.getExternalStorageDirectory().toString();
        Log.d(Config.TAG, path);

        File f = getContext().getFilesDir();
        Log.d(Config.TAG, f.toString());
        File file[] = f.listFiles();

        for(int i=0; i< file.length; i++){
            fileList.add(file[i].getName());
            Log.d(Config.TAG, file[i].getName() + ":" + file[i].length());
        }
        Log.d(Config.TAG, "out of get firmware from local");
        return fileList;
    }

    public void getFirmwareFromServer(){
        new FetchJsonTask(new JsonListener() {
            @Override
            public void callback(String s) {

                List<String> fileList = getFirmwareFromLocal();

                Log.d(Config.TAG,"get firmware list");
                Log.d(Config.TAG, s);

                if(s.length()> 0){
                    try {
                        JSONObject jsonObj = new JSONObject(s);
                        JSONArray jsonArray = jsonObj.getJSONArray("content");

                        mItems = new ArrayList<FirmwareListItem>();
                        Log.d(Config.TAG, "get firmware list string representation");

                        for(int i=0; i< jsonArray.length(); i++) {
                            Log.d(Config.TAG, jsonArray.getJSONObject(i).toString());

                            JSONObject firmwareObj = jsonArray.getJSONObject(i);

                            String name = firmwareObj.getString("name")+ "." + firmwareObj.getString("type");

                            if(!fileList.contains(new String(name))) {

                                mItems.add(new FirmwareListItem(
                                        ContextCompat.getDrawable(getContext(), android.R.drawable.ic_menu_info_details),
                                        //ContextCompat.getDrawable(getContext(), android.R.drawable.ic_menu_save),
                                        name,
                                        firmwareObj.getString("url")));
                            }else{
                                mItems.add(new FirmwareListItem(
                                        //ContextCompat.getDrawable(getContext(), android.R.drawable.ic_menu_info_details),
                                        ContextCompat.getDrawable(getContext(), android.R.drawable.ic_menu_save),
                                        name,
                                        firmwareObj.getString("url")));
                            }

                            Log.d(Config.TAG, firmwareObj.getString("name"));
                            Log.d(Config.TAG, firmwareObj.getString("url"));

                        }
                        setListAdapter(new FirmwareLIstAdapter(getActivity(),mItems));
                    }
                    catch (JSONException e){
                        e.printStackTrace();
                    }
                    finally {

                    }
                }
                else{
                    Log.d(Config.TAG, "String length is less than 0");
                    ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                            getContext(), R.layout.simple_firmware_list_item_1 ,
                            new String[]{});
                    setListAdapter(adapter);

                }
            }
        }).execute(mSite + Config.FIRMWARE_DIR);


    }


}
