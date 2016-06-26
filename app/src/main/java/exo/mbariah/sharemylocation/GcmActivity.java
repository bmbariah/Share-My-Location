package exo.mbariah.sharemylocation;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.StrictMode;
import android.view.Window;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.rey.material.widget.ProgressView;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import exo.mbariah.sharemylocation.dbutility.Db;

/**
 * Created by Mbaria on 19/06/2016.
 */

public class GcmActivity extends Activity {


    protected static final int MSG_REGISTER_WITH_GCM = 101;
    protected static final int MSG_REGISTER_WEB_SERVER = 102;
    protected static final int MSG_REGISTER_WEB_SERVER_SUCCESS = 103;
    protected static final int MSG_REGISTER_WEB_SERVER_FAILURE = 104;
    private static final String PREF_GCM_REG_ID = "PREF_GCM_REG_ID";
    private static final String GCM_SENDER_ID = "196783849382";
    private static final String WEB_SERVER_URL = "http://beta.reelforge.com/reelapp/local/gcm/register_user.php";
    private static final int ACTION_PLAY_SERVICES_DIALOG = 100;
    GoogleCloudMessaging gcm;
    ProgressView pv_circular;
    GCMRegistrationTask first;
    Handler myhandler = new Handler();
    WebServerRegistrationTask second;
    String id,name;
    Context context;
    Db the_db;
    Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {

            first = new GCMRegistrationTask();
            second = new WebServerRegistrationTask();

            switch (msg.what) {

                case MSG_REGISTER_WITH_GCM:
                    first.execute();
                    break;
                case MSG_REGISTER_WEB_SERVER:
                    second.execute();
                    break;
                case MSG_REGISTER_WEB_SERVER_SUCCESS:
                    Toast.makeText(getApplicationContext(),
                            "Successfully Registered", Toast.LENGTH_SHORT).show();
                    break;
                case MSG_REGISTER_WEB_SERVER_FAILURE:
                    Toast.makeText(getApplicationContext(),
                            "Failed Device Registration",
                            Toast.LENGTH_SHORT).show();
                    break;
                default:
                    break;
            }
            return false;
        }
    });
    Runnable myFirstTask = new Runnable() {
        public void run() {
            second.cancel(true);
            pv_circular.stop();
            Toast.makeText(context, "Request Timed Out",
                    Toast.LENGTH_SHORT).show();

            Intent intent = new Intent(GcmActivity.this, MainActivity.class);

            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            startActivity(intent);
            overridePendingTransition(0, 0);
            finish();
        }
    };
    Runnable mySecondTask = new Runnable() {
        public void run() {
            first.cancel(true);
            Toast.makeText(context, "Request Timed Out",
                    Toast.LENGTH_SHORT).show();
            pv_circular.stop();

            Intent intent = new Intent(GcmActivity.this, MainActivity.class);

            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            startActivity(intent);
            overridePendingTransition(0, 0);
            finish();

        }
    };
    private SharedPreferences prefs;
    private String gcmRegId;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences prefs2;
        context = this;
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        /*************** For ICS Compatibility **************/
        int SDK_INT = android.os.Build.VERSION.SDK_INT;

        if (SDK_INT > 8) {

            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                    .permitAll().build();

            StrictMode.setThreadPolicy(policy);
        }
        /******************************************************/
        setContentView(R.layout.welcome);


        the_db = new Db(this);


        // Check device for Play Services APK.
        if (isGoogelPlayInstalled()) {
            gcm = GoogleCloudMessaging.getInstance(getApplicationContext());

            // Read saved registration id from shared preferences.
            gcmRegId = this.getSharedPreferences().getString(PREF_GCM_REG_ID, null);

            prefs2 = getApplicationContext().getSharedPreferences(
                    "g_Key", Context.MODE_PRIVATE);

            String key = prefs2.getString(PREF_GCM_REG_ID, null);

            if (key == null) {
                //empty not registered...
                handler.sendEmptyMessage(MSG_REGISTER_WITH_GCM);
            } else {
                // regIdView.setText(gcmRegId);
                //handler.sendEmptyMessage(MSG_REGISTER_WITH_GCM);
                //Toast.makeText(getApplicationContext(), "Already registered", Toast.LENGTH_SHORT).show();
                OpenWelcome();
            }
        } else {
            OpenWelcome();

        }

    }


    private boolean isGoogelPlayInstalled() {
        int resultCode = GooglePlayServicesUtil
                .isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this,
                        ACTION_PLAY_SERVICES_DIALOG).show();
            } else {
                Toast.makeText(getApplicationContext(),
                        "Google Play Service is not installed",
                        Toast.LENGTH_SHORT).show();
                finish();
            }
            return false;
        }
        return true;

    }

    private void OpenWelcome() {

        Intent intent = new Intent(GcmActivity.this, MainActivity.class);

        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        startActivity(intent);
        overridePendingTransition(0, 0);
        finish();

    }

    private SharedPreferences getSharedPreferences() {
        if (prefs == null) {
            prefs = getApplicationContext().getSharedPreferences(
                    "g_Key", Context.MODE_PRIVATE);
        }
        return prefs;
    }

    public void saveInSharedPref(String result) {

        SharedPreferences.Editor editor = getSharedPreferences().edit();
        editor.putString(PREF_GCM_REG_ID, result);
        editor.apply();
    }

    private class GCMRegistrationTask extends AsyncTask<Void, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            pv_circular = (ProgressView) findViewById(R.id.progress_pv_circular);
            pv_circular.start();

            myhandler.postDelayed(mySecondTask, 10000);

            try {

                the_db.open();
                id = the_db.getData();
                name=the_db.getName();

                the_db.close();

            } catch (Exception e) {
                the_db.close();
            } finally {
                if (the_db != null) {
                    the_db.close();
                }
            }

        }

        @Override
        protected String doInBackground(Void... params) {

            if (gcm == null && isGoogelPlayInstalled()) {
                gcm = GoogleCloudMessaging.getInstance(getApplicationContext());
            }
            try {
                gcmRegId = gcm.register(GCM_SENDER_ID);
            } catch (Exception e) {
                e.printStackTrace();
            }

            return gcmRegId;
        }

        @Override
        protected void onPostExecute(String result) {
            if (result != null) {
                // Toast.makeText(getApplicationContext(), "registered with GCM",Toast.LENGTH_LONG).show();
                //regIdView.setText(result);
                saveInSharedPref(result);

                myhandler.removeCallbacks(mySecondTask);

                handler.sendEmptyMessage(MSG_REGISTER_WEB_SERVER);
            }
        }


    }

    private class WebServerRegistrationTask extends AsyncTask<Void, Void, String> {


        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            pv_circular = (ProgressView) findViewById(R.id.progress_pv_circular);
            pv_circular.start();

            myhandler.postDelayed(myFirstTask, 10000);
        }

        @Override
        protected String doInBackground(Void... params) {
            URL url;
            try {
                url = new URL(WEB_SERVER_URL);

                Map<String, String> dataMap = new HashMap<>();
                dataMap.put("regId", gcmRegId);
                dataMap.put("user_Id", id);
                dataMap.put("username", name);


                StringBuilder postBody = new StringBuilder();
                Iterator iterator = dataMap.entrySet().iterator();

                while (iterator.hasNext()) {
                    Map.Entry param = (Map.Entry) iterator.next();
                    postBody.append(param.getKey()).append('=')
                            .append(param.getValue());
                    if (iterator.hasNext()) {
                        postBody.append('&');
                    }
                }
                String body = postBody.toString();
                byte[] bytes = body.getBytes();

                HttpURLConnection conn = null;
                try {
                    conn = (HttpURLConnection) url.openConnection();
                    conn.setDoOutput(true);
                    conn.setUseCaches(false);
                    conn.setFixedLengthStreamingMode(bytes.length);
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Content-Type",
                            "application/x-www-form-urlencoded;charset=UTF-8");

                    OutputStream out = conn.getOutputStream();
                    out.write(bytes);

                    out.close();

                    int status = conn.getResponseCode();
                    if (status == 200) {
                        // Request success
                        handler.sendEmptyMessage(MSG_REGISTER_WEB_SERVER_SUCCESS);
                    } else {
                        throw new IOException("Request failed with error code "
                                + status);
                    }
                } catch (IOException io) {
                    io.printStackTrace();
                    handler.sendEmptyMessage(MSG_REGISTER_WEB_SERVER_FAILURE);
                } finally {
                    if (conn != null) {
                        conn.disconnect();
                    }
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
                handler.sendEmptyMessage(MSG_REGISTER_WEB_SERVER_FAILURE);
            }

            return null;
        }

        @Override
        protected void onPostExecute(String result) {

            myhandler.removeCallbacks(myFirstTask);
            pv_circular.stop();

            OpenWelcome();

        }

    }
}
