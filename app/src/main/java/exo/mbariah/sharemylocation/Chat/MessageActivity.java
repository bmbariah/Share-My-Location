package exo.mbariah.sharemylocation.Chat;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.rey.material.widget.ProgressView;

import org.apache.commons.io.IOUtils;
import org.json.JSONObject;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

import exo.mbariah.sharemylocation.MapsActivity;
import exo.mbariah.sharemylocation.R;
import exo.mbariah.sharemylocation.dbutility.Db;
import exo.mbariah.sharemylocation.dbutility.GCM_DB;

public class MessageActivity extends AppCompatActivity {

    private ListView mListView;
    Button mButtonSend;
    ImageView mImageView;

    private EditText mEditTextMessage;
    private String time;
    private GCM_DB the_db;
    private String message;
    ProgressView pv_circular;
    BroadcastReceiver broadcastReceiver;
    Cursor cur;
    Db info;
    String client_id;
    String value, gcm, url, name;
    int imsg, ititle, iint, itime, isend, imsgid;

    public static final String KEY_ROWID = "_id";
    public static final String KEY_MSG = "message";
    public static final String KEY_MSGID = "user_id";
    public static final String KEY_URL = "url";
    public static final String KEY_TIME = "time";
    public static final String KEY_SEND_ID = "sender_id";
    public static final String NOTIFY_ACTIVITY_ACTION = "notify_activity";

    public String API_KEY = "AIzaSyDLauD5kJdAzT7ZTjSOjc7_YglBt5FzvHU";

    private ChatMessageAdapter mAdapter;
    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.message);
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            value = extras.getString("user_id");
            gcm = extras.getString("gcm_regid");
            name = extras.getString("username");

            //Toast.makeText(this, value, Toast.LENGTH_SHORT).show();
        }

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        assert toolbar != null;
        toolbar.setTitle(Html.fromHtml("<font color=#ffffff>" + name + "</font>"));
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.mipmap.back_btn);
        toolbar.setNavigationOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        MessageActivity.this.finish();
                    }
                }
        );

        url = "empty";
        the_db = new GCM_DB(this);
        info = new Db(this);


        try {
            info.open();
            client_id = info.getData();
            info.close();
        } catch (Exception e) {
            info.close();
        }


        mListView = (ListView) findViewById(R.id.listView);
        mButtonSend = (Button) findViewById(R.id.btn_send);
        mEditTextMessage = (EditText) findViewById(R.id.et_message);
        mImageView = (ImageView) findViewById(R.id.iv_image);
        pv_circular = (ProgressView) findViewById(R.id.progress);

        mAdapter = new ChatMessageAdapter(this, new ArrayList<ChatMessage>());
        mListView.setAdapter(mAdapter);

        scanDB();


        mButtonSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                message = mEditTextMessage.getText().toString();

                InputMethodManager inputManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);

                if (TextUtils.isEmpty(message) && url.equals("empty")) {
                    return;
                }

                DateFormat df = new SimpleDateFormat(" dd LLL, HH:mm", Locale.US);
                String date = df.format(Calendar.getInstance().getTime());
                time = "Sent on:" + date;

                if (checkInternetConnection()) {
                    String[] myTaskParams = {message, url, gcm};
                    new sendToServer().execute(myTaskParams);
                    //sendToServer(message, "url", gcm);
                } else {

                    AlertDialog.Builder builder = new AlertDialog.Builder(
                            MessageActivity.this);
                    builder.setTitle("Network Error");
                    builder.setMessage("Please check your internet connection.")
                            .setCancelable(true)
                            .setPositiveButton("OK",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                        }
                                    });
                    AlertDialog alert = builder.create();
                    alert.show();
                }
                mEditTextMessage.setText("");
            }
        });

        mImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                openMaps();
            }
        });


    }

    @Override
    protected void onStart() {
        super.onStart();
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals(NOTIFY_ACTIVITY_ACTION)) {
                    //Toast.makeText(context,"Refresh",Toast.LENGTH_SHORT).show();
                    mAdapter.clear();
                    mAdapter.notifyDataSetChanged();
                    scanDB();
                    mListView.smoothScrollToPosition(mAdapter.getCount(), -1);

                }
            }
        };

        IntentFilter filter = new IntentFilter(NOTIFY_ACTIVITY_ACTION);
        registerReceiver(broadcastReceiver, filter);
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(broadcastReceiver);
    }

    private void openMaps() {
        Intent intent = new Intent(this, MapsActivity.class);
        //intent.putExtra("client_name", client_name);
        //startActivity(intent);
        startActivityForResult(intent, 22);// Activity is started with requestCode 2
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // check if the request code is same as what is passed  here it is 2
        if (requestCode == 22) {
            url = data.getStringExtra("url");
            //Log.e("TAG", url);
            Toast.makeText(this, "Link received. Add text and click send", Toast.LENGTH_LONG).show();
        }
    }

    private void scanDB() {

        try {

            the_db.open();
            int count = the_db.getTableCount();
            //Log.i("DB Total", String.valueOf(count));
            if (count != 0) {
                cur = the_db.getNotif(value);

                imsg = cur.getColumnIndex(KEY_MSG);
                ititle = cur.getColumnIndex(KEY_URL);
                iint = cur.getColumnIndex(KEY_ROWID);
                itime = cur.getColumnIndex(KEY_TIME);
                isend = cur.getColumnIndex(KEY_SEND_ID);
                imsgid = cur.getColumnIndex(KEY_MSGID);


                for (cur.moveToFirst(); !cur.isAfterLast(); cur.moveToNext()) {

                    if (cur.getString(isend).equals("0")) {
                        myMessage(cur.getString(imsg), cur.getString(ititle), cur.getString(itime));

                    } else {
                        mimicOtherMessage(cur.getString(imsg), cur.getString(ititle), cur.getString(itime));

                    }

                }
                // System.out.println("GCM .." + result);

               /* for (int i = 1; i <= count; i++) {
                    //mimicOtherMessage(cur.getString(imsg), cur.getString(itime));
                    mimicOtherMessage("One", "Date");
               }*/

            }


            the_db.close();

        } catch (Exception e) {
            the_db.close();
        } finally {
            if (the_db != null) {
                the_db.close();
            }
        }


    }


    private void sendMessage(String message, String url, String time) {
        try {
            the_db.open();

            ChatMessage chatMessage = new ChatMessage(message, url, time, true, false);
            the_db.createmsgEntry2(value, url, message, time, "0");

            mAdapter.add(chatMessage);

        } catch (Exception e) {
            the_db.close();
        } finally {
            if (the_db != null) {
                the_db.close();
            }
        }

        //mimicOtherMessage(message);
    }

    public class sendToServer extends AsyncTask<String, Void, String[]> {

        @Override
        protected void onPreExecute() {
            pv_circular.start();

        }

        @Override
        protected String[] doInBackground(String... params) {

            try {

                String msg, url, gcm;

                msg = params[0];
                url = params[1];
                gcm = params[2];

                // Prepare JSON containing the GCM message content. What to send and where to send.
                JSONObject jGcmData = new JSONObject();
                JSONObject jData = new JSONObject();
                jData.put("message", msg);
                jData.put("user_id", client_id);
                jData.put("url", url);

                jGcmData.put("to", gcm);
                // What to send in GCM message.
                jGcmData.put("data", jData);

                // Create connection to send GCM Message request.
                URL web_url = new URL("https://android.googleapis.com/gcm/send");
                HttpURLConnection conn = (HttpURLConnection) web_url.openConnection();
                conn.setRequestProperty("Authorization", "key=" + API_KEY);
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);

                // Send GCM message content.
                OutputStream outputStream = conn.getOutputStream();
                outputStream.write(jGcmData.toString().getBytes());

                // Read GCM response.
                InputStream inputStream = conn.getInputStream();
                String resp = IOUtils.toString(inputStream);
                System.out.println(resp);

            } catch (Exception e) {
                e.printStackTrace();

                Log.e("Error", e.toString());

                MessageActivity.this.runOnUiThread(new Runnable() {
                    public void run() {
                        pv_circular.stop();
                        Toast.makeText(getApplicationContext(), "Message sending failed. Please Try again", Toast.LENGTH_SHORT).show();

                    }

                });
            }

            return params;

        }

        @Override
        protected void onPostExecute(String[] result) {
            if (pv_circular.isShown()) {
                pv_circular.stop();
                //dialog.dismiss();
                String msg, url;

                msg = result[0];
                url = result[1];

                sendMessage(msg, url, time);


            }
        }

    }

    private void myMessage(String message, String url, String time) {
        ChatMessage chatMessage = new ChatMessage(message, url, time, true, false);
        mAdapter.add(chatMessage);

        //mimicOtherMessage(message);
    }

    private void mimicOtherMessage(String message, String url, String time) {
        ChatMessage chatMessage = new ChatMessage(message, url, time, false, false);
        mAdapter.add(chatMessage);
    }

   /* private void sendImage(String time) {
        ChatMessage chatMessage = new ChatMessage(null, time, true, true);
        mAdapter.add(chatMessage);

        //mimicOtherImage();
    }

    private void mimicOtherImage(String time) {
        ChatMessage chatMessage = new ChatMessage(null, time, false, true);
        mAdapter.add(chatMessage);
    }*/


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.refrsh, menu);
        return false;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        return super.onOptionsItemSelected(item);
    }

    private boolean checkInternetConnection() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        // test for connection
        if (cm.getActiveNetworkInfo() != null
                && cm.getActiveNetworkInfo().isAvailable()
                && cm.getActiveNetworkInfo().isConnected()) {
            return true;
        } else {
            Log.v("TAG", "Internet Connection Not Present");
            return false;
        }
    }
}
