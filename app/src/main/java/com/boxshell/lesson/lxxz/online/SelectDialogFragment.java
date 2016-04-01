package com.boxshell.lesson.lxxz.online;


import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.boxshell.lesson.lxxz.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class SelectDialogFragment extends DialogFragment {
    private String version, url;
    private OnFragmentInteractionListener mListener;

    public SelectDialogFragment() {
        // Required empty public constructor

    }
    public void setVersion(String v){
        version = v;
    }
    public void setUrl(String u){
        url = u;
    }

    @Override
    public void onAttach(Activity activity) {
        mListener = (OnFragmentInteractionListener)activity;
        super.onAttach(activity);
    }

    @Override
    public void onDetach() {
        mListener = null;
        super.onDetach();
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        //return super.onCreateDialog(savedInstanceState);
        return new AlertDialog.Builder(getActivity())
                // Set Dialog Icon
                //.setIcon(R.)
                        // Set Dialog Title
                .setTitle("更新")
                        // Set Dialog Message
                .setMessage("现在就更新为"+ version +"版本吗?")

                        // Positive button
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // Do something else
                        mListener.confirmUpdate(url);
                    }
                })

                        // Negative Button
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // Do something else
                        mListener.cancelUpdate();

                    }
                }).create();
    }

    public interface OnFragmentInteractionListener{
        public void confirmUpdate(String url);
        public void cancelUpdate();
    }
}
