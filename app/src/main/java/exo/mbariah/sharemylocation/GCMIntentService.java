package exo.mbariah.sharemylocation;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.StyleSpan;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.Random;

import exo.mbariah.sharemylocation.Chat.MessageActivity;
import exo.mbariah.sharemylocation.dbutility.GCM_DB;

/**
 * Created by Mbaria on 19/06/2016.
 */

public class GCMIntentService extends IntentService {

    public static final int N_ID = 1000;
    public static final String KEY_MSG = "message";
    public static final String KEY_URL = "url";
    private final StyleSpan mBoldSpan = new StyleSpan(Typeface.BOLD);
    NotificationManager mNotificationManager;
    String title = null;
    Cursor cur;
    String id;
    GCM_DB the_db;
    int c;

    public GCMIntentService() {
        super(GCMIntentService.class.getName());
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Bundle extras = intent.getExtras();

        the_db = new GCM_DB(this);

        if (!extras.isEmpty()) {
            // read extras as sent from server
            id = extras.getString("user_id");
            String message = extras.getString("message");
            title = extras.getString("url");
            sendNotification(message + "\n");
            // + serverTime);
        }
        // Release the wake lock provided by the WakefulBroadcastReceiver.
        GCMBroadcastReceiver.completeWakefulIntent(intent);
    }

    private void sendNotification(String msg) {

        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        int num = 0;
        Random random = new Random();
        int m = random.nextInt(9999 - 1000) + 1000;

        String x = m + "";

        DateFormat df = new SimpleDateFormat(" dd LLL, HH:mm", Locale.US);
        String date = df.format(Calendar.getInstance().getTime());
        String time = "Sent on:" + date;

        Intent i = new Intent(this, MainActivity.class);
        i.setAction("gcm");

        //opened after clicking
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, i, 0);

        try {
            the_db.open();
            c = the_db.getCount();
            cur = the_db.getmsgData();

            the_db.createmsgEntry(x, title, msg);
            the_db.createmsgEntry2(id, title, msg, time, "1");

        } catch (Exception e) {
            the_db.close();
        }

        int imsg = cur.getColumnIndex(KEY_MSG);
        int ititle = cur.getColumnIndex(KEY_URL);

        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction(MessageActivity.NOTIFY_ACTIVITY_ACTION);
        sendBroadcast(broadcastIntent);

        NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();
        //inboxStyle.setBigContentTitle(makeNotificationLine(title, ""));

        if (c == 0) {
            num++;
            inboxStyle.addLine(msg);
        } else {
            for (cur.moveToFirst(); !cur.isAfterLast(); cur.moveToNext()) {
                num++;
                inboxStyle.setBigContentTitle("Share My Location");
                inboxStyle.addLine(msg);
                inboxStyle.addLine(cur.getString(imsg));
            }
        }

        inboxStyle.setSummaryText(num + " new notifications");

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("Share My Location")
                .setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE | Notification.DEFAULT_LIGHTS)
                .setContentText(msg);
        mBuilder.setPriority(NotificationCompat.PRIORITY_DEFAULT);
        mBuilder.setAutoCancel(true);
        mBuilder.setStyle(inboxStyle);
        mBuilder.setContentIntent(contentIntent);
        the_db.close();

        mNotificationManager.notify(N_ID, mBuilder.build());

    }

    //Set Title in bold
    private SpannableString makeNotificationLine(String title, String text) {
        final SpannableString spannableString;
        if (title != null && title.length() > 0) {
            spannableString = new SpannableString(String.format("%s  %s", title, text));
            spannableString.setSpan(mBoldSpan, 0, title.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        } else {
            spannableString = new SpannableString(text);
        }
        return spannableString;
    }

}
