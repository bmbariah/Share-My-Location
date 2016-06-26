package exo.mbariah.sharemylocation;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import exo.mbariah.sharemylocation.Fragments.ListFragment;
import exo.mbariah.sharemylocation.dbutility.GCM_DB;

/**
 * Created by Boniface Mbaria on 19/06/2016.
 */

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private long backPressedTime = 0;
    private GCM_DB the_db;
    Toolbar toolbar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        assert toolbar != null;
        toolbar.setTitle(Html.fromHtml("<font color=#ffffff>" + getString(R.string.toolbarTitle) + "</font>"));
        setSupportActionBar(toolbar);
        //toolbar.setNavigationIcon(R.drawable.ic_toolbar_arrow);
        toolbar.setNavigationOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Toast.makeText(getApplicationContext(), "clicking the toolbar!", Toast.LENGTH_SHORT).show();
                    }
                }
        );

        ImageView inbox = (ImageView) findViewById(R.id.inbox);
        assert inbox != null;
        inbox.getBackground().setAlpha(220);  // 50% transparent

        ImageView in = (ImageView) findViewById(R.id.about_us);
        assert in != null;
        in.getBackground().setAlpha(220);  // 50% transparent

        inbox.setOnClickListener(this);
        in.setOnClickListener(this);


        the_db = new GCM_DB(this);

        try {
            the_db.open();
            the_db.deleteData();
            the_db.close();
        } catch (Exception e) {
            the_db.close();
        }


    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.activity_log_in, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.action_settings:

                Toast.makeText(this, "Settings Clicked !", Toast.LENGTH_SHORT).show();

                break;
            case R.id.action_logout:

                this.deleteDatabase("ClientDB");
                this.deleteDatabase("HomeStoriesDB");
                this.deleteDatabase("GCM_DB");

                SharedPreferences prefs3 = this.getSharedPreferences(
                        "g_Key", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs3.edit();
                editor.clear();
                editor.apply();

                // Go to log in screen
                this.finish();
                Intent logIn = new Intent(this, LogInActivity.class);
                logIn.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(logIn);
                this.finish();

                break;


        }
        return false;
    }

    @Override
    public void onClick(View view) {

        switch (view.getId()) {

            case R.id.inbox:
               /* getSupportFragmentManager()
                        .beginTransaction()
                        .add(R.id.frame, ListFragment.newInstance(), "list")
                        .commit();*/

                ListFragment fragmentOne = new ListFragment();
                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                transaction.add(R.id.frame, fragmentOne.newInstance());
                transaction.addToBackStack(null);
                transaction.commit();

                break;
            case R.id.about_us:
                Intent in = new Intent(this, About.class);
                startActivity(in);
                break;
        }
    }


    @Override
    public void onBackPressed() {
        long t = System.currentTimeMillis();

        if (getSupportFragmentManager().getBackStackEntryCount() == 0) {
            this.finish();
            if (t - backPressedTime > 3000) {    // 2 secs
                backPressedTime = t;
                Toast.makeText(this, "Press back again to exit",
                        Toast.LENGTH_SHORT).show();
            } else {
                // clean up
                finish();
                super.onBackPressed();
            }
        } else {
            getSupportFragmentManager().popBackStack();
        }
    }

}
