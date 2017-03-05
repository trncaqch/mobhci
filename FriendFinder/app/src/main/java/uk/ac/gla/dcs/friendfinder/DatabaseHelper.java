package uk.ac.gla.dcs.friendfinder;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import org.altbeacon.beacon.Beacon;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static volatile SQLiteDatabase mDatabase;
    private static DatabaseHelper mInstance = null;
    private static Context mContext;

    private static final String DB_NAME = "database.db";
    private static final int DB_VERSION = 1;

    public static final String FRIENDS_TABLE = "FRIENDS";
    public static final String FRIENDS_COLUMN_ID = "_ID";
    public static final String FRIENDS_COLUMN_NAME = "NAME";
    public static final String FRIENDS_COLUMN_PHONE = "PHONE";
    public static final String FRIENDS_COLUMN_BEACON_ID1 = "BEACONID1";
    public static final String FRIENDS_COLUMN_BEACON_ID2 = "BEACONID2";
    public static final String FRIENDS_COLUMN_BEACON_ID3 = "BEACONID3";
    public static final String FRIENDS_COLUMN_LAST_STRENGTH = "LASTSTRENGTH";
    public static final String FRIENDS_COLUMN_ADDED = "ADDED";
    public static final String FRIENDS_COLUMN_TIMESTAMP = "TIMESTAMP";
    public static final String FRIENDS_COLUMN_NOTIFICATIONS = "NOTIFICATIONS";

    private static final String DB_CREATE_FRIENDS_TABLE =
            "CREATE TABLE " + FRIENDS_TABLE + " ("
                    + FRIENDS_COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + FRIENDS_COLUMN_NAME + " TEXT NOT NULL, " + FRIENDS_COLUMN_PHONE + " TEXT, " + FRIENDS_COLUMN_BEACON_ID1 + " TEXT, " + FRIENDS_COLUMN_BEACON_ID2 + " TEXT, " + FRIENDS_COLUMN_BEACON_ID3 + " TEXT, " + FRIENDS_COLUMN_LAST_STRENGTH + " INTEGER, " + FRIENDS_COLUMN_ADDED + " INTEGER, " + FRIENDS_COLUMN_TIMESTAMP + " INTEGER, " + FRIENDS_COLUMN_NOTIFICATIONS + " INTEGER DEFAULT 1 )";


    public static synchronized DatabaseHelper getInstance(Context context) {

        if (mInstance == null) {
            mInstance = new DatabaseHelper(context.getApplicationContext());
            try {
                mInstance.open();
            }
            catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return mInstance;
    }

    private DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
        mContext = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(DB_CREATE_FRIENDS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    @Override
    public void onConfigure(SQLiteDatabase db){
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true);
    }

    public synchronized void open() throws SQLException {
        mDatabase = getWritableDatabase();
    }

    public synchronized void close() {
        mDatabase.close();
    }

    public synchronized long addNewFriend(String name, String phone, Beacon beacon) {
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.FRIENDS_COLUMN_NAME, name);
        values.put(DatabaseHelper.FRIENDS_COLUMN_PHONE, phone);
        values.put(DatabaseHelper.FRIENDS_COLUMN_BEACON_ID1, beacon.getId1().toString());
        values.put(DatabaseHelper.FRIENDS_COLUMN_BEACON_ID2, beacon.getId2().toString());
        values.put(DatabaseHelper.FRIENDS_COLUMN_BEACON_ID3, beacon.getId3().toString());
        return mDatabase.insertWithOnConflict(DatabaseHelper.FRIENDS_TABLE, null, values, SQLiteDatabase.CONFLICT_IGNORE);
    }

    public synchronized long addNewFriend(String name, String phone, String beaconId1, String beaconId2, String beaconId3) {
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.FRIENDS_COLUMN_NAME, name);
        values.put(DatabaseHelper.FRIENDS_COLUMN_PHONE, phone);
        values.put(DatabaseHelper.FRIENDS_COLUMN_BEACON_ID1, beaconId1);
        values.put(DatabaseHelper.FRIENDS_COLUMN_BEACON_ID2, beaconId2);
        values.put(DatabaseHelper.FRIENDS_COLUMN_BEACON_ID3, beaconId3);
        return mDatabase.insertWithOnConflict(DatabaseHelper.FRIENDS_TABLE, null, values, SQLiteDatabase.CONFLICT_IGNORE);
    }

    public synchronized Friend getFriendById(long friendId) {
        Cursor cursor = mDatabase.query(
                DatabaseHelper.FRIENDS_TABLE, // table
                new String[]{DatabaseHelper.FRIENDS_COLUMN_ID, DatabaseHelper.FRIENDS_COLUMN_NAME, DatabaseHelper.FRIENDS_COLUMN_PHONE, DatabaseHelper.FRIENDS_COLUMN_BEACON_ID1, DatabaseHelper.FRIENDS_COLUMN_BEACON_ID2, DatabaseHelper.FRIENDS_COLUMN_BEACON_ID3, DatabaseHelper.FRIENDS_COLUMN_LAST_STRENGTH, DatabaseHelper.FRIENDS_COLUMN_ADDED, DatabaseHelper.FRIENDS_COLUMN_TIMESTAMP, DatabaseHelper.FRIENDS_COLUMN_NOTIFICATIONS}, // column names
                DatabaseHelper.FRIENDS_COLUMN_ID + " = ?", // where clause
                new String[]{friendId + ""}, // where params
                null, // groupby
                null, // having
                null);  // orderby
        cursor.moveToFirst();
        Friend friend = null;
        if (!cursor.isAfterLast()) {
            String friendName = getStringFromColumnName(cursor, DatabaseHelper.FRIENDS_COLUMN_NAME);
            String friendPhone = getStringFromColumnName(cursor, DatabaseHelper.FRIENDS_COLUMN_PHONE);
            String friendBeaconId1 = getStringFromColumnName(cursor, DatabaseHelper.FRIENDS_COLUMN_BEACON_ID1);
            String friendBeaconId2 = getStringFromColumnName(cursor, DatabaseHelper.FRIENDS_COLUMN_BEACON_ID2);
            String friendBeaconId3 = getStringFromColumnName(cursor, DatabaseHelper.FRIENDS_COLUMN_BEACON_ID3);
            long friendLastStrength = getLongFromColumnName(cursor, DatabaseHelper.FRIENDS_COLUMN_LAST_STRENGTH);
            long friendAdded = getLongFromColumnName(cursor, DatabaseHelper.FRIENDS_COLUMN_ADDED);
            long friendTimestamp = getLongFromColumnName(cursor, DatabaseHelper.FRIENDS_COLUMN_TIMESTAMP);
            long friendNotifications = getLongFromColumnName(cursor, DatabaseHelper.FRIENDS_COLUMN_NOTIFICATIONS);
            friend = new Friend(friendId, friendName, friendPhone, friendBeaconId1, friendBeaconId2, friendBeaconId3, friendLastStrength, friendTimestamp, friendAdded, friendNotifications);
            cursor.moveToNext();
        }
        cursor.close();
        return friend;
    }

    public synchronized int updateTimestampAndStrengthForBeacon(String beaconId1, String beaconId2, String beaconId3, long strength) {
        ContentValues cv = new ContentValues();
        cv.put(FRIENDS_COLUMN_TIMESTAMP,new java.util.Date().getTime());
        cv.put(FRIENDS_COLUMN_LAST_STRENGTH,strength);
        return mDatabase.update(DatabaseHelper.FRIENDS_TABLE, cv, FRIENDS_COLUMN_BEACON_ID1 + "=? AND " + FRIENDS_COLUMN_BEACON_ID2 + "=? AND " + FRIENDS_COLUMN_BEACON_ID3 + "=?", new String[]{beaconId1 + "", beaconId2 + "", beaconId3 + ""});
    }

    public synchronized int updateTimestampAndStrengthForBeacon(Beacon beacon, long strength) {
        return this.updateTimestampAndStrengthForBeacon(beacon.getId1().toString(), beacon.getId2().toString(), beacon.getId3().toString(), strength);
    }

    public synchronized Friend getFriendByBeacon(String beaconId1, String beaconId2, String beaconId3) {
        Cursor cursor = mDatabase.query(
                DatabaseHelper.FRIENDS_TABLE, // table
                new String[]{DatabaseHelper.FRIENDS_COLUMN_ID, DatabaseHelper.FRIENDS_COLUMN_NAME, DatabaseHelper.FRIENDS_COLUMN_PHONE, DatabaseHelper.FRIENDS_COLUMN_BEACON_ID1, DatabaseHelper.FRIENDS_COLUMN_BEACON_ID2, DatabaseHelper.FRIENDS_COLUMN_BEACON_ID3, DatabaseHelper.FRIENDS_COLUMN_LAST_STRENGTH, DatabaseHelper.FRIENDS_COLUMN_ADDED, DatabaseHelper.FRIENDS_COLUMN_TIMESTAMP, DatabaseHelper.FRIENDS_COLUMN_NOTIFICATIONS}, // column names
                DatabaseHelper.FRIENDS_COLUMN_BEACON_ID1 + " = ? AND " + FRIENDS_COLUMN_BEACON_ID2 + "=? AND " + FRIENDS_COLUMN_BEACON_ID3 + "=?", // where clause
                new String[]{beaconId1 + "", beaconId2 + "", beaconId3 + ""}, // where params
                null, // groupby
                null, // having
                null);  // orderby
        cursor.moveToFirst();
        Friend friend = null;
        if (!cursor.isAfterLast()) {
            long friendId = getLongFromColumnName(cursor, DatabaseHelper.FRIENDS_COLUMN_ID);
            String friendName = getStringFromColumnName(cursor, DatabaseHelper.FRIENDS_COLUMN_NAME);
            String friendPhone = getStringFromColumnName(cursor, DatabaseHelper.FRIENDS_COLUMN_PHONE);
            String friendBeaconId1 = getStringFromColumnName(cursor, DatabaseHelper.FRIENDS_COLUMN_BEACON_ID1);
            String friendBeaconId2 = getStringFromColumnName(cursor, DatabaseHelper.FRIENDS_COLUMN_BEACON_ID2);
            String friendBeaconId3 = getStringFromColumnName(cursor, DatabaseHelper.FRIENDS_COLUMN_BEACON_ID3);
            long friendLastStrength = getLongFromColumnName(cursor, DatabaseHelper.FRIENDS_COLUMN_LAST_STRENGTH);
            long friendAdded = getLongFromColumnName(cursor, DatabaseHelper.FRIENDS_COLUMN_ADDED);
            long friendTimestamp = getLongFromColumnName(cursor, DatabaseHelper.FRIENDS_COLUMN_TIMESTAMP);
            long friendNotifications = getLongFromColumnName(cursor, DatabaseHelper.FRIENDS_COLUMN_NOTIFICATIONS);
            friend = new Friend(friendId, friendName, friendPhone, friendBeaconId1, friendBeaconId2, friendBeaconId3, friendLastStrength, friendTimestamp, friendAdded, friendNotifications);
            cursor.moveToNext();
        }
        cursor.close();
        return friend;
    }

    public synchronized void deleteFriend(Friend friend) {
        this.deleteFriend(friend.getId());
    }

    public synchronized void deleteFriend(long friendId) {
        String whereClause = FRIENDS_COLUMN_ID + "=?";
        String[] whereArgs = new String[] { friendId + "" };
        mDatabase.delete(FRIENDS_TABLE, whereClause, whereArgs);
    }

    public synchronized List<Friend> getAllFriends() {
        List<Friend> friends = new ArrayList<>();
        Cursor cursor = mDatabase.query(
                DatabaseHelper.FRIENDS_TABLE, // table
                new String[]{DatabaseHelper.FRIENDS_COLUMN_ID, DatabaseHelper.FRIENDS_COLUMN_NAME, DatabaseHelper.FRIENDS_COLUMN_PHONE, DatabaseHelper.FRIENDS_COLUMN_BEACON_ID1, DatabaseHelper.FRIENDS_COLUMN_BEACON_ID2, DatabaseHelper.FRIENDS_COLUMN_BEACON_ID3, DatabaseHelper.FRIENDS_COLUMN_LAST_STRENGTH, DatabaseHelper.FRIENDS_COLUMN_ADDED, DatabaseHelper.FRIENDS_COLUMN_TIMESTAMP, DatabaseHelper.FRIENDS_COLUMN_NOTIFICATIONS}, // column names
                null, // where clause
                null, // where params
                null, // groupby
                null, // having
                DatabaseHelper.FRIENDS_COLUMN_NAME); // orderby
        cursor.moveToFirst();
        Friend friend = null;

        while (!cursor.isAfterLast()) {
            Log.d("DATABASE","Reading something");
            long friendId = getLongFromColumnName(cursor, DatabaseHelper.FRIENDS_COLUMN_ID);
            String friendName = getStringFromColumnName(cursor, DatabaseHelper.FRIENDS_COLUMN_NAME);
            String friendPhone = getStringFromColumnName(cursor, DatabaseHelper.FRIENDS_COLUMN_PHONE);
            String friendBeaconId1 = getStringFromColumnName(cursor, DatabaseHelper.FRIENDS_COLUMN_BEACON_ID1);
            String friendBeaconId2 = getStringFromColumnName(cursor, DatabaseHelper.FRIENDS_COLUMN_BEACON_ID2);
            String friendBeaconId3 = getStringFromColumnName(cursor, DatabaseHelper.FRIENDS_COLUMN_BEACON_ID3);
            long friendLastStrength = getLongFromColumnName(cursor, DatabaseHelper.FRIENDS_COLUMN_LAST_STRENGTH);
            long friendAdded = getLongFromColumnName(cursor, DatabaseHelper.FRIENDS_COLUMN_ADDED);
            long friendTimestamp = getLongFromColumnName(cursor, DatabaseHelper.FRIENDS_COLUMN_TIMESTAMP);
            long friendNotifications = getLongFromColumnName(cursor, DatabaseHelper.FRIENDS_COLUMN_NOTIFICATIONS);
            friend = new Friend(friendId, friendName, friendPhone, friendBeaconId1, friendBeaconId2, friendBeaconId3, friendLastStrength, friendTimestamp, friendAdded, friendNotifications);
            Log.d("DATABASE","Read " + friend.toString() + "  -- " + friendName);
            friends.add(friend);
            cursor.moveToNext();
        }
        cursor.close();
        return friends;
    }

    private synchronized long getLongFromColumnName(Cursor cursor, String columnName) {
        int columnIndex = cursor.getColumnIndex(columnName);
        return cursor.getLong(columnIndex);
    }

    public synchronized int setNotificationsEnabledForFriend(long friendId, boolean enabled) {
        ContentValues cv = new ContentValues();
        cv.put(FRIENDS_COLUMN_NOTIFICATIONS,enabled ? 1 : 0);

        return mDatabase.update(DatabaseHelper.FRIENDS_TABLE, cv, FRIENDS_COLUMN_ID + "=?", new String[]{friendId + ""});
    }

    public synchronized String getStringFromColumnName(Cursor cursor, String columnName) {
        int columnIndex = cursor.getColumnIndex(columnName);
        return cursor.getString(columnIndex);
    }

}