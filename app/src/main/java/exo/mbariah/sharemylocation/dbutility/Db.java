package exo.mbariah.sharemylocation.dbutility;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class Db {
    public static final String KEY_ROWID = "_id";
    public static final String KEY_CLIENTID = "client_id";

    public static final String KEY_NAME = "name";
    public static final String KEY_EMAIL = "client_email";

    private static final String DATABASE_NAME = "ClientDB";
    private static final String DATABASE_TABLE = "ClientTable";
    private static final int DATABASE_VERSION = 3;

    private static DbHelper ourHelper;
    private static SQLiteDatabase ourDatabase;
    private final Context ourContext;

    public Db(Context c) {
        ourContext = c;
    }

    public Db open() throws SQLException {
        ourHelper = new DbHelper(ourContext);
        ourDatabase = ourHelper.getWritableDatabase();
        return this;
    }

    public void close() {
        ourHelper.close();
    }

    //here..
    public long createEntry(String s2, String s3, String s4) {

        ContentValues cv = new ContentValues();
        cv.put(KEY_CLIENTID, s2);
        cv.put(KEY_EMAIL, s3);
        cv.put(KEY_NAME, s4);

        return ourDatabase.insert(DATABASE_TABLE, null, cv);

    }

    public String getData() {
        String[] columns = new String[]{KEY_ROWID, KEY_CLIENTID, KEY_NAME, KEY_EMAIL};
        Cursor c = ourDatabase.query(DATABASE_TABLE, columns, null, null, null,
                null, null);
        String result = "";
        int iClientid = c.getColumnIndex(KEY_CLIENTID);

        c.moveToLast();
        result = result + c.getString(iClientid);
        // close();// Added
        return result;
    }

    public String getEmail() {

        String[] columns = new String[]{KEY_ROWID, KEY_CLIENTID, KEY_NAME, KEY_EMAIL};
        Cursor c = ourDatabase.query(DATABASE_TABLE, columns, null, null, null,
                null, null);
        String result = "";
        int iEmail = c.getColumnIndex(KEY_EMAIL);
        c.moveToLast();
        result = result + c.getString(iEmail);

        return result;
    }

    public String getName() {

        String[] columns = new String[]{KEY_ROWID, KEY_CLIENTID, KEY_NAME, KEY_EMAIL};
        Cursor c = ourDatabase.query(DATABASE_TABLE, columns, null, null, null,
                null, null);
        String result = "";
        int iName = c.getColumnIndex(KEY_NAME);
        c.moveToLast();
        result = result + c.getString(iName);
        return result;
    }


    public void deleteData() {

        ourDatabase = ourHelper.getWritableDatabase();
        ourDatabase.execSQL("DELETE FROM " + DATABASE_TABLE + " WHERE "
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
                    + KEY_CLIENTID + " TEXT NOT NULL ,"
                    + KEY_NAME + " TEXT NOT NULL,"
                    + KEY_EMAIL + " TEXT NOT NULL );");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

            db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE);
            onCreate(db);
        }

    }

}
