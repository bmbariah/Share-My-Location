package exo.mbariah.sharemylocation;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import exo.mbariah.sharemylocation.dbutility.Db;

/**
 * Created by Mbaria on 19/06/2016.
 */

public class SplashScreenActivity extends AppCompatActivity {

    Db user_db;
    String user_id;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.splash);

        // DB connection
        user_db = new Db(this);

        try {
            user_db.open();
            user_id = user_db.getData();
            user_db.close();
        } catch (Exception e) {
            user_db.close();
        }

        if (user_id == null) {
            // Start Log in Activity
            Intent intent = new Intent(SplashScreenActivity.this,
                    LogInActivity.class);

            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            startActivity(intent);
            overridePendingTransition(0, 0);
            finish();

        } else {

            Intent intent = new Intent(SplashScreenActivity.this,
                    GcmActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            startActivity(intent);
            overridePendingTransition(0, 0);
            finish();
        }
    }


}