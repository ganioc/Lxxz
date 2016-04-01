package com.boxshell.lesson.lxxz.wifi;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.boxshell.lesson.lxxz.R;
import com.boxshell.lesson.lxxz.online.Config;
import com.boxshell.lesson.lxxz.online.JsonListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Formatter;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link FileUploadFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link FileUploadFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FileUploadFragment extends DialogFragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER


    private OnFragmentInteractionListener mListener;
    public UploadFileTask mTask;
    private String filename;
    private int BUFFER_SIZE = 1024;

    public FileUploadFragment() {
        // Required empty public constructor
        mTask = new UploadFileTask(new JsonListener() {
            @Override
            public void callback(String s) {
                if(s == null){
                    // the task is not finished
                    Toast.makeText(getContext(), "文件上传任务失败", Toast.LENGTH_LONG).show();


                }
                else if(s.equals("true")){
                    // the task is finished
                    Toast.makeText(getContext(), "文件上传任务成功, 设备准备重启", Toast.LENGTH_LONG).show();
                    // begin to quit and reboot

                    final Handler h1 = new Handler();

                    final Runnable r2 = new Runnable() {

                        @Override
                        public void run() {
                            // do second thing
                            mListener.die();
                        }
                    };

                    h1.postDelayed(r2, 4000);

                }else{
                    Toast.makeText(getContext(), "文件上传成功，切换出现问题", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        final ProgressDialog dialog = new ProgressDialog(getActivity());
        //
        dialog.setTitle("文件上传");
        dialog.setMessage( getFilename() + " 上传到设备");
        dialog.setIndeterminate(false);
        dialog.setCancelable(true);
        dialog.setButton(DialogInterface.BUTTON_NEGATIVE, "取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Log.d(Config.TAG, "dialog cancel clicked");
                mTask.cancel(true);
            }
        });
        dialog.setButton(DialogInterface.BUTTON_NEUTRAL, "上传", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);

        dialog.setCanceledOnTouchOutside(false);
        /*dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                Log.d(Config.TAG, "Dialog is canceled");

                // cancel the upload task
                mTask.cancel(true);

            }
        });*/
        //dialog.setOnCancelListener();


        // etc...
        return dialog;
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment FileUploadFragment.
     */
    // TODO: Rename and change types and number of parameters
/*    public static FileUploadFragment newInstance(String param1, String param2) {
        FileUploadFragment fragment = new FileUploadFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }*/
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
    public void setFilename(String name){
        filename = name;
    }
    public String getFilename(){
        return filename;
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
    }

    class UploadFileTask extends AsyncTask<String, Integer, String> {
        public JsonListener mJsoner;
        private Socket mSocket;
        private boolean running= false;

        public UploadFileTask(JsonListener listener){
            mJsoner = listener;
        }

        @Override
        protected void onPreExecute() {
            this.running = false;
            super.onPreExecute();

        }


        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            ProgressDialog dlg = (ProgressDialog)getDialog();
            // read local file
            // sent to the device, update
            if(dlg != null) {
                dlg.setProgress(values[0]);
            }
        }

        @Override
        protected void onCancelled() {
            Log.d(Config.TAG, "*** in onCancelled  ()");
            running = false;
            super.onCancelled();

            try {
                if(mSocket!= null) {
                    mSocket.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        int checkFeedbackStatus(String line){
            JSONObject jObj;
            int out = -1;

            try{
                jObj = new JSONObject(line);
                out = Integer.valueOf(jObj.getString("CONTENT"));
                return out;

            }catch (JSONException e) {
                e.printStackTrace();
            }
            return out;
        }

        boolean checkStatusOK(String line){
            JSONObject jObj;
            boolean out = false;

            try{
                jObj = new JSONObject(line);
                String status = jObj.getString("STATUS");
                if(status.equals("OK")){
                    out = true;
                }


            }catch (JSONException e) {
                e.printStackTrace();
            }
            return out;
        }

        @Override
        protected String doInBackground(String... params) {
            Log.d(Config.TAG, "Begin to upload the file");
            try {
                mSocket = new Socket( mListener.getCurIp(), Config.TCP_PORT);
                mSocket.setSoTimeout(Config.TCP_TIMEOUT);

                Log.d(Config.TAG, "要上传的文件:" + getFilename());
                running = true;
                byte[] buffer; // buffer is 4KB in size
                int bytesRead; // bytes read into the buffer
                int fileSize, counter = 0, feedback;
                //byte character;

                //Formatter formatter = new Formatter();

                // read file
                File f = getContext().getFilesDir();
                Log.d(Config.TAG, f.toString());

                String name = f.toString() + "/" + getFilename();
                Log.d(Config.TAG, "file name is:"+ name);

                // open the file , read it's size and it's content
                File srcFile = new File(name);

                Log.d(Config.TAG,srcFile.getName() + " " + srcFile.length());
                FileInputStream fileInputStream = new FileInputStream(srcFile);

                buffer = new byte[BUFFER_SIZE];

                // This is used for TCP communication
                OutputStream out = mSocket.getOutputStream();
                BufferedReader input = new BufferedReader(new InputStreamReader(mSocket.getInputStream()));
                //
                //
                PrintWriter output = new PrintWriter(out);
                //DataOutputStream output = new DataOutputStream(out);
                String line;

                // read state for a try
                output.write(Config.strPacketGet("STATE", "STATE"));
                Log.d(Config.TAG, "Transmited packet:" + Config.strPacketGet("STATE", "STATE"));
                output.flush();
                line = input.readLine();
                Log.d(Config.TAG, "Received TCP packet:" + line);

                // set upload type
                output.write(Config.strPacketSet("UPLOAD", "TYPE", "APP"));
                Log.d(Config.TAG, "Transmited packet:" + Config.strPacketSet("UPLOAD", "TYPE", "APP"));
                output.flush();
                line = input.readLine();
                Log.d(Config.TAG, "Received TCP packet:" + line);

                // set upload length
                output.write(Config.strPacketSet("UPLOAD", "LENGTH", "" + srcFile.length()));
                Log.d(Config.TAG, "Transmited packet:" + Config.strPacketSet("UPLOAD", "LENGTH", "" + srcFile.length()));
                output.flush();
                line = input.readLine();
                Log.d(Config.TAG, "Received TCP packet:" + line);

                // send a packet to  start init()
                output.write(Config.strPacketSet("UPLOAD", "START", "1"));
                Log.d(Config.TAG, "Transmited packet:" + Config.strPacketSet("UPLOAD", "START", "1"));
                output.flush();
                line = input.readLine();
                Log.d(Config.TAG, "Received TCP packet:" + line);

                // send a packet to indicate start sending file packet
                output.write(Config.strPacketSet("STATE", "STATE", "1"));
                Log.d(Config.TAG, "Transmited packet:" + Config.strPacketSet("STATE", "STATE", "1"));
                output.flush();
                line = input.readLine();
                Log.d(Config.TAG, "Received TCP packet:" + line);


                bytesRead = fileInputStream.read(buffer,0, BUFFER_SIZE);
                fileSize = (int)srcFile.length();

                DataOutputStream outputBin = new DataOutputStream(out);

                while (!isCancelled()&& bytesRead > 0) {

                    publishProgress((counter * 100/ fileSize) );
                    Log.d(Config.TAG,"publish:" + ((counter* 100)/fileSize)*100);

                    //Log.d(Config.TAG, formatter.toString());
                    //Log.d(Config.TAG, "Send " + bytesRead + " bytes to device");
                    //send the packet out
                    outputBin.write(buffer, 0, bytesRead);
                    outputBin.flush();
                    line = input.readLine();
                    //Log.d(Config.TAG, "Received Upload res packet:" + line);

                    // check if there is an error or not
                    feedback = checkFeedbackStatus(line);

                    if(feedback == 0){
                        Log.d(Config.TAG, "Feedback: upload finished");
                    }
                    else if(feedback == -1){
                        Log.d(Config.TAG, "Feedback: upload error");
                        throw(new IOException("Upload Error, from device side"));
                    }
                    else{
                        counter += feedback;
                        //Log.d(Config.TAG, "Feedback:" + feedback);
                    }

                    bytesRead = fileInputStream.read(buffer, 0 , BUFFER_SIZE);
                    //Log.d(Config.TAG, ".");

                    /*try{
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }*/
                }
                //outputBin.close();
                Log.d(Config.TAG, counter + " bytes send for " + fileSize + " bytes file");

                //output = new PrintWriter(out);

                output.write(Config.strPacketGet("STATE", "STATE"));
                Log.d(Config.TAG, "Transmited packet:" + Config.strPacketGet("STATE", "STATE"));
                output.flush();
                line = input.readLine();
                Log.d(Config.TAG, "Received TCP packet:" + line);

                // send a packet to  start deinit(), then the device will reboot
                output.write(Config.strPacketSet("UPLOAD", "START", "0"));
                Log.d(Config.TAG, "Transmited packet:" + Config.strPacketSet("UPLOAD", "START", "0"));
                output.flush();
                line = input.readLine();
                Log.d(Config.TAG, "Received TCP packet:" + line);



                //outputBin.close();
                //fileInputStream.close();
                //output.close();

                // send a packet to restart it
                output.write(Config.strPacketSet("BOOT","",""));
                Log.d(Config.TAG, "Transmited BOOT packet:" + Config.strPacketSet("BOOT","",""));
                output.flush();


                if(counter == fileSize && checkStatusOK(line)) {
                    publishProgress(100);
                    output.write(Config.strPacketSet("BOOT", "", "0"));
                    Log.d(Config.TAG, "Transmited packet:" + Config.strPacketSet("UPLOAD", "START", "0"));

                    return "true";
                }else {
                    return "false";
                }

            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }
        @Override
        protected void onPostExecute(String s) {
            Log.d(Config.TAG, "post upload the file");
            try {
                if(mSocket!= null) {
                    Log.d(Config.TAG,"Socket closed");
                    mSocket.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            super.onPostExecute(s);
            this.mJsoner.callback(s);

        }

    }
}