package exo.mbariah.sharemylocation.dbutility;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Mbaria on 20-Jun-16.
 */

public class HomeStoriesDB {

    public static final String KEY_ROW_ID = "_id";
    public static final String KEY_USER_ID = "user_id";
    public static final String KEY_NAME = "username";
    public static final String KEY_REG_ID = "reg_id";
    public static final String KEY_COUNT = "count";


    private static final String DATABASE_NAME = "HomeStoriesDB";
    private static final String DATABASE_TABLE = "HomeStoryTable";
    private static final int DATABASE_VERSION = 1;
    public static SQLiteDatabase ourDatabase = null;
    private static DbHelper ourHelper;
    private final Context ourContext;


    public HomeStoriesDB(Context c) {
        ourContext = c;
    }

    public HomeStoriesDB open() throws SQLException {
        ourHelper = new DbHelper(ourContext);
        ourDatabase = ourHelper.getWritableDatabase();
        return this;
    }

    public void close() {
        ourHelper.close();
    }

    public long createEntry(String user_id_DB, String name_DB,
                            String reg_id_DB, String count_DB) {

        // TODO Auto-generated method stub
        ContentValues cv = new ContentValues();
        cv.put(KEY_USER_ID, user_id_DB);
        cv.put(KEY_NAME, name_DB);
        cv.put(KEY_REG_ID, reg_id_DB);
        cv.put(KEY_COUNT, count_DB);


        return ourDatabase.insert(DATABASE_TABLE, null, cv);

    }

    public void deleteData() {
        ourDatabase = ourHelper.getWritableDatabase();
        ourDatabase.execSQL("DELETE FROM " + DATABASE_TABLE + " WHERE "
                + KEY_ROW_ID + " > 0");
    }

    /*public String getUsername(String user_id) {
        String result = "";
        String[] columns = new String[]{KEY_ROW_ID, KEY_USER_ID, KEY_NAME,
                KEY_REG_ID, KEY_COUNT };
        Cursor c = ourDatabase.query(DATABASE_TABLE, columns, KEY_USER_ID
                + " = " + user_id, null, null, null, null);
        if (c != null) {
            c.moveToFirst();
            String title = c.getString(2);
            result = title;
        }
        return result;
    }*/



    public ArrayList<HashMap<String, ?>> offlineData() {

        ArrayList<HashMap<String, ?>> inboxList = new ArrayList<HashMap<String, ?>>();

        // TODO Auto-generated method stub
        String[] columns = new String[]{KEY_ROW_ID, KEY_USER_ID, KEY_NAME,
                KEY_REG_ID, KEY_COUNT};

        Cursor c = ourDatabase.query(DATABASE_TABLE, // Table name
                columns,null,null, null, null, null);

        int iRow = c.getColumnIndex(KEY_ROW_ID);
        int iUser = c.getColumnIndex(KEY_USER_ID);
        int iName = c.getColumnIndex(KEY_NAME);
        int iReg = c.getColumnIndex(KEY_REG_ID);
        int iCount = c.getColumnIndex(KEY_COUNT);



        for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {

            HashMap<String, Object> map = new HashMap<String, Object>();
            map.put("user_id", c.getString(iUser));
            map.put("username", c.getString(iName));
            map.put("gcm_regid", c.getString(iReg));
            map.put("updated_at", c.getString(iCount));

            inboxList.add(map);
        }
        return inboxList;
    }

    private static class DbHelper extends SQLiteOpenHelper {

        public DbHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);

        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            // TODO Auto-generated method stub

            db.execSQL("CREATE TABLE " + DATABASE_TABLE + " (" + KEY_ROW_ID
                    + " INTEGER PRIMARY KEY AUTOINCREMENT, " + KEY_USER_ID
                    + " TEXT NOT NULL, " + KEY_NAME + " TEXT NOT NULL, "
                    + KEY_REG_ID + " TEXT NOT NULL, " + KEY_COUNT
                    + " TEXT NOT NULL);");

        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            // TODO Auto-generated method stub
            db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE);
            onCreate(db);
        }
    }

}