package exo.mbariah.sharemylocation;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v7.app.AlertDialog;
import android.text.InputType;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import com.rey.material.widget.CheckBox;
import com.rey.material.widget.EditText;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

import exo.mbariah.sharemylocation.dbutility.Db;

/**
 * Created by Mbaria on 19/06/2016.
 */

@SuppressLint("NewApi")
public class LogInActivity extends Activity implements OnClickListener {

    /**
     * Called when the activity is first created.
     */
    EditText userNameET, passwordET, emailNameET;
    CheckBox check_ET;
    Button logInET;
    String result = null;
    String userNameStr = null, passwordStr = null, emailNameStr = null;
    Db entry;
    String TAG;
    JSONObject deviceDetail;
    View parent;
    private String hashed;
    private long backPressedTime = 0;
    private String s = null,
            s2 = null, s3 = null, s4 = null, s5 = null, s6;
    private int height;
    private int width;

    @TargetApi(Build.VERSION_CODES.GINGERBREAD)
    @SuppressLint("NewApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // setContentView(R.layout.main);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.login_form);
        parent = findViewById(R.id.parent);
        getWidthHight();

        emailNameET = (EditText) findViewById(R.id.emailNameET);
        userNameET = (EditText) findViewById(R.id.userNameET);
        passwordET = (EditText) findViewById(R.id.passwordET);
        check_ET = (CheckBox) findViewById(R.id.checkboxET);

        logInET = (Button) findViewById(R.id.logInBtn);
        logInET.setOnClickListener(this);

        int SDK_INT = Build.VERSION.SDK_INT;

        if (SDK_INT > 8) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                    .permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }


    }

    public void getWidthHight() {
        DisplayMetrics metrics = new DisplayMetrics();
        WindowManager manager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        manager.getDefaultDisplay().getMetrics(metrics);
        height = metrics.heightPixels;
        width = metrics.widthPixels;
    }


    public void onCheckboxClicked(View view) {

        switch (view.getId()) {


            case R.id.checkboxET:

                if (check_ET.isChecked()) {
                    //show password
                    passwordET.setInputType(InputType.TYPE_CLASS_TEXT);

                } else {
                    //hide password
                    passwordET.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);

                }
                break;

            default:
                break;
        }

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.logInBtn:
                try {
                    // dialog.show();
                    if (checkInternetConnection()) {
                        getDetails(); // grab username and password
                        new NetworkClass().execute(""); // Run the network class
                    } else {

                        AlertDialog.Builder builder = new AlertDialog.Builder(
                                LogInActivity.this);
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

                    // logIn();
                    // dialog.dismiss();
                    // validate();
                } catch (Exception e) {
                    //System.out.println(e.toString());
                }
                break;
            default:
                showAlert();
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.activity_log_in, menu);
        return true;
    }


    @Override
    public void onBackPressed() {
        long t = System.currentTimeMillis();
        if (t - backPressedTime > 3000) {    // 2 secs
            backPressedTime = t;
            Toast.makeText(this, "Press back again to exit",
                    Toast.LENGTH_SHORT).show();
        } else {
            // clean up
            finish();
            super.onBackPressed();
        }
    }

    public void getDetails() {
        emailNameStr = emailNameET.getText().toString();
        userNameStr = userNameET.getText().toString();
        passwordStr = passwordET.getText().toString();
        BoniMd5Hash(passwordStr);

        deviceDetail = new JSONObject();

        try {

            deviceDetail.put("username", userNameStr);
            deviceDetail.put("password", hashed);
            deviceDetail.put("email", emailNameStr);


            //System.out.println("Json : " + deviceDetail);
        } catch (Exception e) {
            e.printStackTrace();

        }
    }


    public void showAlert() {
        LogInActivity.this.runOnUiThread(new Runnable() {
            public void run() {

                AlertDialog.Builder builder = new AlertDialog.Builder(
                        LogInActivity.this);
                builder.setTitle("Login Error");
                builder.setMessage("Check your data.")
                        .setCancelable(true)
                        .setPositiveButton("OK",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                    }
                                });
                AlertDialog alert = builder.create();
                alert.show();

            }
        });

    }

    public final String BoniMd5Hash(String password) {

        try {

            MessageDigest digest = MessageDigest.getInstance("MD5");
            digest.update(password.getBytes());
            byte messageDigest[] = digest.digest();

            StringBuffer MD5Hash = new StringBuffer();
            for (int i = 0; i < messageDigest.length; i++) {
                String h = Integer.toHexString(0xFF & messageDigest[i]);
                while (h.length() < 2)
                    h = "0" + h;
                MD5Hash.append(h);
            }

            hashed = MD5Hash.toString();

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        return hashed;

    }

    private boolean checkInternetConnection() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        // test for connection
        if (cm.getActiveNetworkInfo() != null
                && cm.getActiveNetworkInfo().isAvailable()
                && cm.getActiveNetworkInfo().isConnected()) {
            return true;
        } else {
            Log.v(TAG, "Internet Connection Not Present");
            return false;
        }
    }

    @Override
    protected void onStop() {
        // Toast.makeText(this, "onStop", Toast.LENGTH_SHORT).show();
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        // Toast.makeText(this, "onDestroy", Toast.LENGTH_SHORT).show();
        super.onDestroy();
    }


    //everything happens here
    private String getPostData(HashMap<String, String> params) throws UnsupportedEncodingException {
        StringBuilder result = new StringBuilder();
        boolean first = true;
        for (Map.Entry<String, String> entry : params.entrySet()) {
            if (first)
                first = false;
            else
                result.append("&");

            result.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
        }

        return result.toString();
    }

    public class NetworkClass extends AsyncTask<String, Integer, String> {
        ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            progressDialog = ProgressDialog.show(LogInActivity.this, "",
                    "Connecting...");
            progressDialog.show();
        }

        @Override
        protected String doInBackground(String... params) {

            HashMap<String, String> par = new HashMap<>();
            par.put("jsonObj", "" + deviceDetail);

            try {

                URL url = new URL("http://beta.reelforge.com/reelapp/local/login2.php");
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("POST");
                urlConnection.setDoOutput(true);//allow output json
                urlConnection.setDoInput(true);// allow us to receive input
                urlConnection.setChunkedStreamingMode(0); // Using default chunk size

                // Write serialized JSON data to our refined output stream.
                OutputStream out = new BufferedOutputStream(urlConnection.getOutputStream());
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out, "UTF-8"));
                writer.write(getPostData(par));
                // Closing streams.
                writer.close();
                out.close();

                urlConnection.setConnectTimeout(6000);
                int status = urlConnection.getResponseCode();

                switch (status) {
                    case 200:
                    case 201: {

                        //Handle input
                        BufferedReader br = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                        StringBuilder sb = new StringBuilder();
                        String line;
                        while ((line = br.readLine()) != null) {
                            sb.append(line + "\n");
                        }
                        br.close();
                        result = sb.toString();
                    }
                }

                // Close streams and disconnect.
                urlConnection.disconnect();

                Log.v("Login response ", result);

                JSONArray jArray = new JSONArray(result);
                for (int i = 0; i < jArray.length(); i++) {
                    JSONObject jObject = jArray.getJSONObject(i);
                    s = jObject.getString("res");
                    s2 = jObject.getString("id");
                    s3 = jObject.getString("email");
                    s4 = jObject.getString("username");

                }


                return s;
            } catch (Exception e) {

                Log.e("Error Here", e + "");

            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            progressDialog.dismiss();
            if (s != null) {

                Toast.makeText(LogInActivity.this, "Login Successful", Toast.LENGTH_SHORT).show();
                // saving the id/data to a local DB
                try {
                    entry = new Db(LogInActivity.this);
                    entry.open();
                    entry.createEntry(s2, s3, s4);
                    entry.close();

                } catch (Exception e) {

                    Log.v("Error Log ", e + "");


                }
                Intent intent = new Intent(LogInActivity.this, GcmActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivity(intent);
                overridePendingTransition(0, 0);
                finish();


            } else {
                Toast.makeText(LogInActivity.this, "Server Error. Try Again Later", Toast.LENGTH_LONG).show();

            }
        }

    }




}
