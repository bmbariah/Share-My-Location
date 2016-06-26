package exo.mbariah.sharemylocation.Fragments;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.rey.material.widget.ProgressView;

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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import exo.mbariah.sharemylocation.Chat.MessageActivity;
import exo.mbariah.sharemylocation.R;
import exo.mbariah.sharemylocation.dbutility.Db;
import exo.mbariah.sharemylocation.dbutility.HomeStoriesDB;


/**
 * Created by Mbaria on 20-Jun-16.
 */

@SuppressLint("NewApi")
public class ListFragment extends Fragment {


    public ArrayList<HashMap<String, ?>> inboxList = null;
    public Context context;
    Db info;
    String client_id;
    int pre = 0;
    ProgressView pv_circular;
    HomeStoriesDB homeDb;
    boolean r_is_running = false;
    Animation anime;
    String user_id_DB, name_DB, reg_DB, count_DB;
    JSONArray jArray;
    boolean no_net;
    ListView list;
    NetworkClass my_task;
    View rootView;
    private int lastPosition = -1;
    private HomeStoriesDB homeStoriesDB;

    public ListFragment newInstance() {
        return new ListFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        context = getActivity();
        info = new Db(context);
        inboxList = new ArrayList<>();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.users, container, false);
        pv_circular = (ProgressView) rootView.findViewById(R.id.prog);
        rootView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        my_task = new NetworkClass();

        return rootView;
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        MenuItem item = menu.findItem(R.id.action_settings);
        item.setVisible(false);

        MenuItem item2 = menu.findItem(R.id.action_logout);
        item2.setVisible(false);

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        New();
    }


    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.refrsh, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // TODO Auto-generated method stub

        switch (item.getItemId()) {

            case R.id.action_refresh:


                if (checkInternetConnection()) {
                    new NetworkClass().execute("");

                } else {
                    no_net = true;
                    new NetworkClass().execute("");

                }


                break;

        }
        return true;
    }


    public void New() {

        try {
            info.open();
            client_id = info.getData();
            info.close();
        } catch (Exception e) {
            info.close();
        }

        if (checkInternetConnection()) {

            my_task.execute("");

        } else {
            no_net = true;
            my_task.execute("");

        }
    }

    @Override
    public void onSaveInstanceState(final Bundle outState) {
        super.onSaveInstanceState(outState);
    }


    private boolean checkInternetConnection() {
        ConnectivityManager cm = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        // test for connection
        if (cm.getActiveNetworkInfo() != null
                && cm.getActiveNetworkInfo().isAvailable()
                && cm.getActiveNetworkInfo().isConnected()) {
            return true;
        } else {
            //Log.v("", "Internet Connection Not Present");
            return false;
        }
    }


    private void insertInHomeDb() {

        homeStoriesDB.open();
        homeStoriesDB.createEntry(user_id_DB, name_DB,
                reg_DB, count_DB);
        homeStoriesDB.close();

    }

    private void clearList() {
        if (inboxList != null)
        inboxList.clear();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    public JSONObject getDetails() {
        JSONObject deviceDetail = new JSONObject();

        try {

            deviceDetail.put("client_id", client_id);

        } catch (Exception e) {
            e.printStackTrace();

        }
        return deviceDetail;
    }


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

    // -------------------------------xxx----------------------------
    public class NetworkClass extends AsyncTask<String, Integer, String> {

        URL url;
        private String result;

        @Override
        protected void onPreExecute() {
            r_is_running = true;
            clearList();

            if (pre == 0 && !no_net) {
                pv_circular.start();

            }
        }

        @Override
        protected String doInBackground(String... arg0) {

            //runs in background thread
            //List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();

            if (!no_net) {

                HashMap<String, String> params = new HashMap<>();

                JSONObject jsonObject = getDetails();
                try {

                    params.put("jsonObj", "" + jsonObject);
                    url = new URL("http://beta.reelforge.com/reelapp/local/getusers.php");

                    HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                    urlConnection.setRequestMethod("POST");
                    urlConnection.setDoOutput(true);//allow output json
                    urlConnection.setDoInput(true);// allow us to receive input
                    urlConnection.setChunkedStreamingMode(0);

                    // Write/Embed serialized JSON data to output stream.
                    OutputStream out = new BufferedOutputStream(urlConnection.getOutputStream());
                    BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out, "UTF-8"));
                    writer.write(getPostData(params));
                    writer.close();
                    out.close();

                    urlConnection.setConnectTimeout(6000);
                    int status = urlConnection.getResponseCode();

                    Log.i("Status: ", "" + status);

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
                    urlConnection.disconnect();
                    Log.i("Here: ", "" + result);
                    // Close streams and disconnect.
                    if (result == null) {
                        //case 500..server down
                        getActivity().runOnUiThread(new Runnable() {
                            public void run() {
                                Toast.makeText(context, "No users available. Check again later",
                                        Toast.LENGTH_LONG).show();
                            }
                        });
                    } else {
                        jArray = new JSONArray(result);


                        //***************** HomeStoriesDB **************//*
                        homeStoriesDB = new HomeStoriesDB(context);
                        homeStoriesDB.open();
                        homeStoriesDB.deleteData();
                        homeStoriesDB.close();


                        //*******************************************//*
                        for (int i = 0; i < jArray.length(); i++) {
                            try {

                                HashMap<String, Object> map = new HashMap<>();

                                JSONObject json = jArray.getJSONObject(i);
                                reg_DB = json.getString("gcm_regid");
                                user_id_DB = json.getString("user_id");
                                name_DB = json.getString("username");
                                count_DB = json.getString("updated_at");

                                map.put("username", name_DB);
                                map.put("gcm_regid", reg_DB);
                                map.put("user_id", user_id_DB);
                                map.put("updated_at", count_DB);

                                inboxList.add(map);

                                insertInHomeDb();


                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                        }
                    }


                } catch (Exception e) {
                    //e.printStackTrace();
                    //System.out.println("Error insert: " + e.toString());
                    LoadOffline();

                }
            } else {

                LoadOffline();

            }

            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            //stop refresh if any

            try {
                final MyAdapter adapter = new MyAdapter(context, inboxList,
                        R.layout.users_list_row, new String[]{}, new int[]{
                        R.id.name, R.id.name,
                        R.id.name, R.id.name,
                        R.id.name, R.id.name});

                if (pre == 1) {
                    list.setVisibility(View.VISIBLE);
                    list.setEnabled(true);
                }

                list = (ListView) ((Activity) context).findViewById(R.id.users_list);

                list.setAdapter(adapter);

                list.setTextFilterEnabled(true);

                if (pv_circular.isShown()) {
                    pv_circular.stop();
                }


                list.setOnItemClickListener(new OnItemClickListener() {

                    @Override
                    public void onItemClick(AdapterView<?> arg0, View view,
                                            int f, long id) {

                        try {
                            int type = Integer.parseInt((String) inboxList.get(
                                    f).get("user_id"));
                            String gcm = (inboxList.get(f).get("gcm_regid")).toString();
                            String name = (inboxList.get(f).get("username")).toString();


                            Intent it = new Intent(context, MessageActivity.class);
                            it.putExtra("user_id", String.valueOf(type));
                            it.putExtra("gcm_regid", gcm);
                            it.putExtra("username", name);
                            startActivity(it);

                            /*Intent it = new Intent(context, MessageActivity.class);
                            it.putExtra("user_id", inboxList.get(Integer.parseInt(user_id_DB)));
                            startActivity(it);*/


                        } catch (Exception e) {
                            System.out.println("497: " + e.toString());
                        }
                    }

                });


            } catch (Exception e) {


                Toast.makeText(context, "Server Not Responding",
                        Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }

            r_is_running = false;
        }

    }

    public void LoadOffline() {

        homeDb = new HomeStoriesDB(context);
        homeDb.open();
        inboxList = homeDb.offlineData();
        homeDb.close();
    }

    private class MyAdapter extends SimpleAdapter {


        public MyAdapter(Context context, List<? extends Map<String, ?>> data,
                         int resource, String[] from, int[] to) {
            super(context, data, resource, from, to);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final int pos = position;
            int x;

            if (convertView == null) {
                LayoutInflater inflater = LayoutInflater.from(context);
                convertView = inflater.inflate(R.layout.users_list_row,
                        null, false);
            } else {
            }

            @SuppressWarnings("unchecked")
            HashMap<String, Object> data = (HashMap<String, Object>) getItem(position);

            TextView title = (TextView) convertView.findViewById(R.id.name);
            title.setText((String) data.get("username"));
            title.setTypeface(null, Typeface.BOLD);


            if (list.getCount() != 0) {
                x = R.anim.up_from_bottom;
            } else {
                x = R.anim.my_anim;

            }

            anime = AnimationUtils.loadAnimation(context, (position > lastPosition) ? x : R.anim.down_from_top);
            convertView.startAnimation(anime);
            lastPosition = position;

            return convertView;
        }


    }


}
