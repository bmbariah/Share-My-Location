package exo.mbariah.sharemylocation.dbutility;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Mbaria on 20-Jun-16.
 */
public class GCM_DB {

    public static final String KEY_ROWID = "_id";
    public static final String KEY_MSGID = "user_id";
    public static final String KEY_SEND_ID = "sender_id";
    public static final String KEY_MSG = "message";
    public static final String KEY_URL = "url";
    public static final String KEY_TIME = "time";


    private static final String DATABASE_NAME = "GCM_DB";
    private static final String DATABASE_TABLE = "gcmTBL";
    private static final String DATABASE_TABLE_MSG = "MessageTBL";
    private static final int DATABASE_VERSION = 4;

    private static DbHelper ourHelper;
    private static SQLiteDatabase ourDatabase;
    private final Context ourContext;

    public GCM_DB(Context c) {
        ourContext = c;
    }

    public GCM_DB open() throws SQLException {
        ourHelper = new DbHelper(ourContext);
        ourDatabase = ourHelper.getWritableDatabase();
        return this;
    }

    public void close() {
        ourHelper.close();
    }

    //Insert data into notifications table..
    public long createmsgEntry(String s2, String s3, String s4) {

        ContentValues cv = new ContentValues();
        cv.put(KEY_MSGID, s2);
        cv.put(KEY_URL, s3);
        cv.put(KEY_MSG, s4);

        return ourDatabase.insert(DATABASE_TABLE, null, cv);

    }

    //Messages table
    public long createmsgEntry2(String s2, String s3, String s4, String s5, String s6) {

        ContentValues cv = new ContentValues();
        cv.put(KEY_MSGID, s2);
        cv.put(KEY_URL, s3);
        cv.put(KEY_MSG, s4);
        cv.put(KEY_TIME, s5);
        cv.put(KEY_SEND_ID, s6);

        return ourDatabase.insert(DATABASE_TABLE_MSG, null, cv);

    }

    public Cursor getmsgData() {
        String[] columns = new String[]{KEY_ROWID, KEY_MSGID, KEY_URL, KEY_MSG};
        Cursor c = ourDatabase.query(DATABASE_TABLE, columns, null, null, null,
                null, null);

        /*String result = "";
        int id = c.getColumnIndex(KEY_MSGID);
        int ititle = c.getColumnIndex(KEY_URL);
        int imsg = c.getColumnIndex(KEY_MSG);
        for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
            result = result + "Title: " + c.getString(ititle) + "\n" + System.getProperty("line.separator")
                    + "Message: " + c.getString(imsg);
        }*/
        // System.out.println("GCM .." + result);
        return c;

    }

    public Cursor getNotif(String id) {
        String[] columns = new String[]{KEY_ROWID, KEY_MSGID, KEY_URL, KEY_TIME, KEY_MSG,KEY_SEND_ID};
        String whereClause = "user_id =?";
        String[] whereArgs = new String[]{id};
        Cursor c = ourDatabase.query(DATABASE_TABLE_MSG, columns, whereClause, whereArgs, null,
                null, null);
        return c;

    }

    public void deleteNotifID(String id) {

        ourDatabase = ourHelper.getWritableDatabase();
        ourDatabase.execSQL("DELETE FROM " + DATABASE_TABLE_MSG + " WHERE "
                + KEY_ROWID + " = " + id);

    }

    public String getNotifications() {
        String[] columns = new String[]{KEY_ROWID, KEY_MSGID, KEY_URL, KEY_MSG};
        Cursor c = ourDatabase.query(DATABASE_TABLE_MSG, columns, null, null, null,
                null, null);

        String result = "";
        int id = c.getColumnIndex(KEY_MSGID);
        int ititle = c.getColumnIndex(KEY_URL);
        int imsg = c.getColumnIndex(KEY_MSG);
        for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
            result = result + "Title: " + c.getString(ititle) + "\n" + System.getProperty("line.separator")
                    + "Message: " + c.getString(imsg);
        }

        return result;

    }

    public int getCount() {
        String[] columns = new String[]{KEY_ROWID, KEY_MSGID, KEY_URL, KEY_MSG};
        Cursor c = ourDatabase.query(DATABASE_TABLE, columns, null, null, null,
                null, null);
        return c.getCount();
    }

    public int getTableCount() {
        String[] columns = new String[]{KEY_ROWID, KEY_MSGID, KEY_URL, KEY_MSG};
        Cursor c = ourDatabase.query(DATABASE_TABLE_MSG, columns, null, null, null,
                null, null);
        return c.getCount();
    }

    public void deleteData() {

        ourDatabase = ourHelper.getWritableDatabase();
        ourDatabase.execSQL("DELETE FROM " + DATABASE_TABLE + " WHERE "
                + KEY_ROWID + " > 0");

    }

    public void deleteData2() {

        ourDatabase = ourHelper.getWritableDatabase();
        ourDatabase.execSQL("DELETE FROM " + DATABASE_TABLE_MSG + " WHERE "
                + KEY_ROWID + " > 0");

    }

    private static class DbHelper extends SQLiteOpenHelper {

        public DbHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);

        }

        @Override
        public void onCreate(SQLiteDatabase db) {

            db.execSQL("CREATE TABLE " + DATABASE_TABLE + " ("
                    + KEY_ROWID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + KEY_MSGID + " TEXT NOT NULL ,"
                    + KEY_MSG + " TEXT NOT NULL,"
                    + KEY_URL + " TEXT NOT NULL );");

            db.execSQL("CREATE TABLE " + DATABASE_TABLE_MSG + " ("
                    + KEY_ROWID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + KEY_MSGID + " TEXT NOT NULL ,"
                    + KEY_MSG + " TEXT NOT NULL ,"
                    + KEY_TIME + " TEXT NOT NULL ,"
                    + KEY_URL + " TEXT NOT NULL ,"
                    + KEY_SEND_ID + " TEXT NOT NULL );");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

            db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE);
            db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE_MSG);
            onCreate(db);
        }

    }

}
