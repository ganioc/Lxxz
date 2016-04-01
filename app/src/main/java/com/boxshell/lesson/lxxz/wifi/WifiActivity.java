package com.boxshell.lesson.lxxz.wifi;

import android.app.ProgressDialog;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.DhcpInfo;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
//import android.support.v7.app.AlertDialog;
import android.text.format.Formatter;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.boxshell.lesson.lxxz.R;
import com.boxshell.lesson.lxxz.online.Config;
import com.boxshell.lesson.lxxz.online.JsonListener;
import com.boxshell.lesson.lxxz.online.SelectDownloadFragment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

public class WifiActivity extends AppCompatActivity
        implements WifiHomeFragment.OnFragmentInteractionListener,
        NavigationView.OnNavigationItemSelectedListener,
        WifiNoneFragment.OnFragmentInteractionListener,
        WifiStatusFragment.OnFragmentInteractionListener,
        WifiOperateFragment.OnFragmentInteractionListener,
        WifiParamFragment.OnFragmentInteractionListener,
        WifiUpgradeFragment.OnFragmentInteractionListener,
        FileUploadFragment.OnFragmentInteractionListener{

    private static final String TAG = "lxxz";
    private String mIpAddress;
    private InetAddress mBroadcastAddress;
    public JSONObject mJsonObj;

    private String curIp;
    private String curDevId;
    private String curMac;

    //private Socket mSocket;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wifi);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        /*FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });*/

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        /*ProgressBar mProgressBar = (ProgressBar)findViewById(R.id.progressBarWifi);
        mProgressBar.setVisibility(View.GONE);*/


        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.fragmentContainer, new WifiHomeFragment(), "wifihome")
                    .commit();
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.wifi, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if(!isWifiConnected()){
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragmentContainer, new WifiHomeFragment(), "wifihome")
                    .commit();

            DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
            drawer.closeDrawer(GravityCompat.START);
            return true;
        }

        if (id == R.id.nav_dev_info) {
            // Handle the camera action
            Log.d(Config.TAG, "click get dev info");

            // if its wifistatus fragment now, call getStatus()
            if (getSupportFragmentManager().findFragmentByTag("wifistatus") == null) {
                showDevStatus();

            } else {
                Log.d(Config.TAG, "wifistatus is already there");
                WifiStatusFragment frag = (WifiStatusFragment) getSupportFragmentManager().findFragmentByTag("wifistatus");

                frag.getStatus();
            }

            //else, replace the fragment

        } else if (id == R.id.nav_dev_param) {
            Log.d(Config.TAG, "click get dev param");
            WifiParamFragment frag = (WifiParamFragment) getSupportFragmentManager().findFragmentByTag("wifiparam");

            if (frag != null) {

            } else {
                showDevParam();
            }
        } else if (id == R.id.nav_dev_op) {
            Log.d(Config.TAG, "click operate on dev");
            WifiOperateFragment frag = (WifiOperateFragment) getSupportFragmentManager().findFragmentByTag("wifioperate");
            if (frag != null) {

            } else {
                showDevOperate();
            }
        } else if (id == R.id.nav_dev_upgrade) {
            Log.d(Config.TAG, "click upgate dev");
            WifiUpgradeFragment frag = (WifiUpgradeFragment) getSupportFragmentManager().findFragmentByTag("wifiupgrade");
            if (frag != null) {

            } else {
                showDevUpgrade();
            }
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    @Override
    public boolean isWifiConnected() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        boolean canConnect = false;

        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

        if (networkInfo != null) {
            if (networkInfo.isConnected()) {
                if (networkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
                    canConnect = true;
                    Log.d(Config.TAG, "WIFI connection is fine");
                }
            }
        }

        return canConnect;
    }

    @Override
    public void showUploadDialog(String filename) {
        // display the dialog for firmware uploading
        FragmentManager fm = getSupportFragmentManager();
        final FileUploadFragment ulFragment = new FileUploadFragment();
        ulFragment.setFilename(filename);

        ulFragment.show(fm, "uploaddialog");

        // then the dlg is created
        fm.executePendingTransactions();

        ProgressDialog dlg = (ProgressDialog) ulFragment.getDialog();


        if(dlg!= null) {
            Button btn = dlg.getButton(DialogInterface.BUTTON_NEUTRAL);

            btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d(Config.TAG, "下载button clicked");

                    v.setEnabled(false);
                    // start the download task
                    //ulFragment.mTask.
                    ulFragment.mTask.execute();
                }
            });
        }else{
            Log.d(Config.TAG, "The dlg is empty");
        }
    }

    @Override
    public void wifiNotConnected() {
        // show wifi not connected fragment
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragmentContainer, new WifiNoneFragment(), "wifinone")
                .commit();
    }

    @Override
    public void wifiHome() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragmentContainer, new WifiHomeFragment(), "wifihome")
                .commit();
    }

    @Override
    public void getIpAddress(JsonListener jListener) {


        new FetchIpAddress(jListener).execute();


    }

    @Override
    public void udpBroadcast(JsonListener jListener) {

        JSONObject obj = new JSONObject();
        try {
            obj.put("srcid", Config.DEFAULT_SRC_ID);
            obj.put("dstid", Config.BROADCAST_DST_ID);
            obj.put("cmd", 1);
            Log.d(Config.TAG, obj.toString());
            new FetchUdpTask(jListener).execute(obj.toString());

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void showDevDialog(String s) {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        List<String> listItems = new ArrayList<String>();

        try {
            JSONObject jObj = new JSONObject(s);
            JSONArray aObj = jObj.getJSONArray("dev");
            Log.d(Config.TAG, "dev num is: " + String.valueOf(aObj.length()));
            JSONObject devObj;

            /*Log.d(Config.TAG, devObj.getString("srcid"));
            Log.d(Config.TAG, devObj.getString("ip"));*/

            int num = aObj.length();

            for (int i = 0; i < num; i++) {
                devObj = (JSONObject) aObj.get(i);
                listItems.add(devObj.getString("ip") + " " + devObj.getString("srcid") + " " +
                        devObj.getString("mac"));
            }

            // display the dialog
            builder.setTitle(R.string.wifi_pick_dev)
                    .setItems(listItems.toArray(new CharSequence[listItems.size()]),
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    // The 'which' argument contains the index position
                                    // of the selected item
                                    Log.d(Config.TAG, String.valueOf(which));
                                    ListView lw = ((AlertDialog) dialog).getListView();
                                    Object item = lw.getAdapter().getItem(which);
                                    Log.d(Config.TAG, item.toString());

                                    curDevId = getIdFromStr(item.toString());
                                    curIp = getIpFromStr(item.toString());
                                    curMac = getMacFromStr(item.toString());

                                    Log.d(Config.TAG, "click: id " + curDevId);
                                    Log.d(Config.TAG, "click: ip " + curIp);
                                    Log.d(Config.TAG, "click: mac " + curMac);
                                    //
                                    showDevStatus();
                                }
                            });

            AlertDialog dialog = builder.create();
            dialog.show();


        } catch (JSONException e) {
            e.printStackTrace();
        }


    }

    private String getIdFromStr(String str) {
        return str.split(" ")[1];
    }

    private String getIpFromStr(String str) {
        return str.split(" ")[0];
    }

    private String getMacFromStr(String str) {
        return str.split(" ")[2];
    }

    @Override
    public String getCurIp() {
        return curIp;
    }

    @Override
    public String getCurId() {
        return curDevId;
    }

    @Override
    public String getCurMac() {
        return curMac;
    }

    @Override
    public void die() {
        Log.d(Config.TAG, "die entry()");
        finish();
    }

    /**
     * dev status
     * id
     * ip
     * hw version
     * fw version
     * <p/>
     * get from TCP link communication
     */
    private void showDevStatus() {
        //switch to status display fragment
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragmentContainer, new WifiStatusFragment(), "wifistatus")
                .commit();

    }

    private void showDevParam() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragmentContainer, new WifiParamFragment(), "wifiparam")
                .commit();

    }

    private void showDevOperate() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragmentContainer, new WifiOperateFragment(), "wifioperate")
                .commit();

    }

    private void showDevUpgrade() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragmentContainer, new WifiUpgradeFragment(), "wifiupgrade")
                .commit();

    }

    class FetchIpAddress extends AsyncTask<String, Integer, String> {
        public JsonListener mListener;
        private ProgressBar pBar;

        public FetchIpAddress(JsonListener listener) {
            this.mListener = listener;
        }

        @Override
        protected void onPreExecute() {
            pBar = (ProgressBar) findViewById(R.id.progressBarWifi);
            /*if(pBar == null){
                Log.d(Config.TAG, "Can not find progressbar");
            }else {
                pBar.setVisibility(View.VISIBLE);
            }*/
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... params) {

            WifiManager wifiMgr = (WifiManager) getSystemService(WIFI_SERVICE);
            WifiInfo wifiInfo = wifiMgr.getConnectionInfo();
            int ip = wifiInfo.getIpAddress();
            String ipAddress1 = Formatter.formatIpAddress(ip);
            String ipAddress = BigInteger.valueOf(wifiMgr.getDhcpInfo().netmask).toString();
            Log.d(Config.TAG, "New ip address is:" + ipAddress1);
            mIpAddress = ipAddress1;

            // get broadcast address
            //WifiManager wifi = mContext.getSystemService(Context.WIFI_SERVICE);
            DhcpInfo dhcp = wifiMgr.getDhcpInfo();
            // handle null somehow
            Log.d(Config.TAG, "ip:" + String.valueOf(dhcp.ipAddress));
            Log.d(Config.TAG, "gateway:" + String.valueOf(dhcp.gateway));
            Log.d(Config.TAG, "netmask:" + String.valueOf(dhcp.netmask));


            //int broadcast = (dhcp.ipAddress & dhcp.netmask) | ~dhcp.netmask;
            int broadcast = (dhcp.ipAddress);

            byte[] quads = new byte[4];
            for (int k = 0; k < 4; k++) {
                quads[k] = (byte) ((broadcast >> k * 8) & 0xFF);
                Log.d(Config.TAG, String.valueOf(quads[k]));

            }

            quads[3] = (byte) 0xFF;
            //
            try {

                mBroadcastAddress = InetAddress.getByAddress(quads);
                Log.d(Config.TAG, "Broadcast ip address:" + mBroadcastAddress.toString());
            } catch (Exception e) {
                e.printStackTrace();
            }

            return ipAddress1;

        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
        }

        @Override
        protected void onPostExecute(String s) {
            mListener.callback(s);
            //pBar.setVisibility(View.GONE);

            super.onPostExecute(s);
        }
    }

    class FetchUdpTask extends AsyncTask<String, Integer, String> {
        public JsonListener mListener;
        private ProgressBar pBar;

        public FetchUdpTask(JsonListener listener) {
            this.mListener = listener;
        }


        @Override
        protected void onPreExecute() {
            this.pBar = (ProgressBar) findViewById(R.id.progressBarWifi);
            if (pBar == null) {
                Log.d(Config.TAG, "No progress bar at all in onPreExecute FetchUdpTask");
            }
            //this.pBar.setVisibility(View.VISIBLE);
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... params) {

            byte[] message = params[0].getBytes();

            DatagramPacket p = new DatagramPacket(message,
                    message.length,
                    mBroadcastAddress,
                    Integer.valueOf(Config.UDP_PORT));

            Log.d(Config.TAG, "Message: " + params[0]);

            try {
                DatagramSocket s = new DatagramSocket();
                s.setBroadcast(true);   // set it to broadcast type
                s.setSoTimeout(Config.UDP_TIMEOUT);  // set the timeout value
                s.send(p);

                /*s.receive(p);

                String text = new String(message, 0, p.getLength());
                Log.d(Config.TAG, "message:" + text);*/

                byte[] buf = new byte[1024];
                JSONArray objArray = new JSONArray();
                mJsonObj = new JSONObject();
                //JSONObject jObjAll = new JSONObject();

                try {
                    while (true) {
                        DatagramPacket packet = new DatagramPacket(buf, buf.length);
                        s.receive(packet);
                        String response = new String(packet.getData(), 0, packet.getLength());
                        Log.d(Config.TAG, "Received response " + response);
                        packet.getAddress();
                        Log.d(Config.TAG, "Received ip: " + packet.getAddress());

                        // put srcid, ip address into an array
                        JSONObject jObj = new JSONObject();
                        jObj.put("ip", packet.getAddress().toString().replace("/", ""));
                        jObj.put("srcid", getStringFromJson("srcid", response));
                        jObj.put("mac", getMacFromArpCache(jObj.getString("ip")));
                        objArray.put(jObj);
                    }
                } catch (SocketTimeoutException e) {
                    Log.d(Config.TAG, "Receive timed out");
                } finally {
                    s.close();
                    mJsonObj.put("dev", objArray);
                    return mJsonObj.toString();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
        }

        @Override
        protected void onPostExecute(String s) {
            //this.pBar.setVisibility(View.GONE);

            Log.d(Config.TAG, "after udp broadcast");
            Log.d(Config.TAG, s);
            this.mListener.callback(s);
            super.onPostExecute(s);
        }
    }

    private String getStringFromJson(String id, String str) {
        try {
            JSONObject obj = new JSONObject(str);

            return obj.getString(id);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;

    }

    private static String getMacFromArpCache(String ip) {
        if (ip == null)
            return null;
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader("/proc/net/arp"));
            String line;
            while ((line = br.readLine()) != null) {
                String[] splitted = line.split(" +");
                if (splitted != null && splitted.length >= 4 && ip.equals(splitted[0])) {
                    // Basic sanity check
                    String mac = splitted[3];
                    if (mac.matches("..:..:..:..:..:..")) {
                        return mac.toUpperCase();
                    } else {
                        return null;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                br.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }
}
