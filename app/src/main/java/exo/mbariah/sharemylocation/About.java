package exo.mbariah.sharemylocation;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.view.View;


/**
 * Created by Mbaria on 19/06/2016.
 */
public class About extends AppCompatActivity {

    Toolbar toolbar;

    public void onCreate(Bundle saved) {
        super.onCreate(saved);
        setContentView(R.layout.about);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        assert toolbar != null;
        toolbar.setTitle(Html.fromHtml("<font color=#ffffff>" + getString(R.string.about_title) + "</font>"));
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.mipmap.back_btn);
        toolbar.setNavigationOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        About.this.finish();

                    }
                }
        );


    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}

