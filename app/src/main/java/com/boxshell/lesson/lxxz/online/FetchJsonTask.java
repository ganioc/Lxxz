package com.boxshell.lesson.lxxz.online;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by yangjun on 16/3/2.
 */
public class FetchJsonTask extends AsyncTask<String ,Integer, String>{
    public JsonListener mListener;
    private static final String TAG = "lxxz";

    public FetchJsonTask(JsonListener listener){
        this.mListener = listener;
    }

    @Override
    protected String doInBackground(String... params) {
        try {
            return downloadUrl(params[0] );
        } catch (IOException e) {
            return "";
        }
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

    // Given a URL, establishes an HttpUrlConnection and retrieves
// the web page content as a InputStream, which it returns as
// a string.
    private String downloadUrl(String myurl) throws IOException {
        InputStream is = null;
        // Only display the first 500 characters of the retrieved
        // web page content.
        //int len = 500;
        Log.d(TAG, "download from:" + myurl);

        try {
            URL url = new URL(myurl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(5000 /* milliseconds */);
            conn.setConnectTimeout(8000 /* milliseconds */);
            conn.setRequestMethod("GET");
            conn.setDoInput(true);
            // Starts the query
            conn.connect();
            int response = conn.getResponseCode();
            Log.d(TAG, "The response is: " + response);
            is = conn.getInputStream();

            // Convert the InputStream into a string
            String contentAsString = readIt(is);
            Log.d(TAG, contentAsString);
            return contentAsString;

            // Makes sure that the InputStream is closed after the app is
            // finished using it.
        } catch (Exception e){
            Log.d(Config.TAG, "Something is wrong");
            e.printStackTrace();
        }finally{
            if (is != null) {
                is.close();
            }
            //return "";
        }
        return "";
    }
    private String readIt(InputStream stream) throws IOException, UnsupportedEncodingException {
/*        Reader reader = null;
        reader = new InputStreamReader(stream, "UTF-8");
        char[] buffer = new char[len];
        reader.read(buffer);
        return new String(buffer);*/
        BufferedReader br = new BufferedReader(new InputStreamReader(stream));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) {
            sb.append(line + "\n");
        }
        br.close();
        return sb.toString();
    }
}
