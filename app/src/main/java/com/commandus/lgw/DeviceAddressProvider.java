package com.commandus.lgw;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;

import com.commandus.lgw.LoraDeviceAddress;

import java.util.HashMap;

public class DeviceAddressProvider extends ContentProvider {

    static final String PROVIDER_NAME = "lora";
    // content URI
    static final String URL = "content://" + PROVIDER_NAME;
    static final String URL_ABP = URL + "/abp";

    // parsing the content URI
    public static final Uri CONTENT_URI = Uri.parse(URL);
    public static final Uri CONTENT_URI_ABP = Uri.parse(URL_ABP);
    public static final String FN_ID = "id";
    public static final String FN_ADDR = "addr";
    public static final String FN_DEVEUI = "deveui";
    public static final String FN_NWKSKEY = "nwkSKey";
    public static final String FN_APPSKEY = "appSKey";
    public static final String FN_NAME = "name";

    public static final int F_ID = 0;
    public static final int F_ADDRESS = 1;
    public static final int F_EUI = 2;
    public static final int F_NWKSKEY = 3;
    public static final int F_APPSKEY = 4;
    public static final int F_NAME = 5;

    public static final String[] PROJECTION = {
            FN_ID, FN_ADDR, FN_DEVEUI, FN_NWKSKEY, FN_APPSKEY, FN_NAME
    };

    static final UriMatcher uriMatcher;
    static final int M_ADDRESSES = 1;
    static final int M_ADDRESS = 2;

    static {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(PROVIDER_NAME, "abp", M_ADDRESSES);
        uriMatcher.addURI(PROVIDER_NAME, "abp/*", M_ADDRESS);
    }
    private SQLiteDatabase db;
    static private final String DATABASE_NAME = "lora_address";
    static private final String TABLE_NAME = "abp";

    // declaring version of the database
    static private final int DATABASE_VERSION = 1;

    // sql query to create the table
    static private final String SQL_CREATE_TABLE = "CREATE TABLE " + TABLE_NAME
            + " (" + FN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + FN_ADDR + " TEXT, "
            + FN_DEVEUI + " TEXT, "
            + FN_NWKSKEY + " TEXT, "
            + FN_APPSKEY + " TEXT, "
            + FN_NAME + " TEXT);";
    static private final String SQL_CREATE_INDEX_1 = "CREATE INDEX idx_name ON " + TABLE_NAME + " (" + FN_NAME + ")";
    static private final String SQL_CREATE_INDEX_2 = "CREATE INDEX idx_address ON " + TABLE_NAME + " (" + FN_ADDR + ")";

    static private final String SQL_DROP_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;
    static private final String SQL_DROP_INDEX_1 = "DROP INDEX IF EXISTS idx_name";
    static private final String SQL_DROP_INDEX_2 = "DROP INDEX IF EXISTS idx_address";
    static private HashMap<String, String> PROJECTION_MAP;

    // creating a database
    private static class DatabaseHelper extends SQLiteOpenHelper {
        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(SQL_CREATE_TABLE);
            db.execSQL(SQL_CREATE_INDEX_1);
            db.execSQL(SQL_CREATE_INDEX_2);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL(SQL_DROP_INDEX_2);
            db.execSQL(SQL_DROP_INDEX_1);
            db.execSQL(SQL_DROP_TABLE);
            onCreate(db);
        }
    }

    public DeviceAddressProvider() {
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        int count;
        switch (uriMatcher.match(uri)) {
            case M_ADDRESS:
                String id = uri.getPathSegments().get(1);
                count = db.delete(TABLE_NAME, FN_ID +  " = " + id +
                    (!TextUtils.isEmpty(selection) ? " AND (" + selection + ')' : ""), selectionArgs);
                break;
            case M_ADDRESSES:
                count = db.delete(TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    @Override
    public String getType(Uri uri) {
        switch (uriMatcher.match(uri)) {
            case M_ADDRESSES:
                return "vnd.android.cursor.dir/address";
            case M_ADDRESS:
                return "vnd.android.cursor.item/address";
            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        long rowID = db.insert(TABLE_NAME, "", values);
        if (rowID > 0) {
            Uri r = ContentUris.withAppendedId(CONTENT_URI_ABP, rowID);
            getContext().getContentResolver().notifyChange(r, null);
            return r;
        }
        return null;
    }

    @Override
    public boolean onCreate() {
        Context context = getContext();
        DatabaseHelper dbHelper = new DatabaseHelper(context);
        db = dbHelper.getWritableDatabase();
        return (db != null);
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(TABLE_NAME);
        switch (uriMatcher.match(uri)) {
            case M_ADDRESSES:
                qb.setProjectionMap(PROJECTION_MAP);
                break;
            case M_ADDRESS:
                qb.appendWhere( FN_ID + " = " + uri.getPathSegments().get(1));
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
        if (sortOrder == null || sortOrder.equals("")) {
            sortOrder = FN_NAME;
        }
        Cursor c = qb.query(db, projection, selection, selectionArgs, null,
                null, sortOrder);
        c.setNotificationUri(getContext().getContentResolver(), uri);
        return c;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        int count;
        switch (uriMatcher.match(uri)) {
            case M_ADDRESSES:
                count = db.update(TABLE_NAME, values, selection, selectionArgs);
                break;
            case M_ADDRESS:
                count = db.update(TABLE_NAME, values, FN_ID + " = " + uri.getPathSegments().get(1) +
                                (!TextUtils.isEmpty(selection) ? " AND (" +selection + ')' : ""), selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    public static int count(Context context) {
        DatabaseHelper dbHelper = new DatabaseHelper(context);
        SQLiteDatabase dbCount = dbHelper.getReadableDatabase();
        Cursor cursorCount= dbCount.rawQuery("SELECT count(*) FROM " + TABLE_NAME, null);
        cursorCount.moveToFirst();
        int r =  cursorCount.getInt(0);
        cursorCount.close();
        return r;
    }

    public static LoraDeviceAddress getById(Context context, long id) {
        DatabaseHelper dbHelper = new DatabaseHelper(context);
        SQLiteDatabase dbCount = dbHelper.getReadableDatabase();
        Cursor cursor = dbCount.rawQuery("SELECT " + FN_ID + ", " + FN_ADDR + ", "
                + FN_DEVEUI + ", " + FN_NWKSKEY + ", " + FN_APPSKEY+ ", " + FN_NAME
                + " FROM " + TABLE_NAME + " WHERE " + FN_ID + " = ? ",
                new String[]{Long.toString(id)});
        if (!cursor.moveToFirst())
            return null;
        LoraDeviceAddress r = new LoraDeviceAddress(
            cursor.getInt(F_ID),
            cursor.getString(F_ADDRESS),
            cursor.getString(F_ADDRESS),
            cursor.getString(F_NWKSKEY),
            cursor.getString(F_APPSKEY),
            cursor.getString(F_NAME)
        );
        cursor.close();
        return r;
    }

    public static void add(Context context, LoraDeviceAddress address) {
        DatabaseHelper dbHelper = new DatabaseHelper(context);
        SQLiteDatabase dbCount = dbHelper.getWritableDatabase();
        Cursor cursor = dbCount.rawQuery("INSERT INTO " + TABLE_NAME + " ("
            + FN_ADDR + ", " + FN_DEVEUI + ", " + FN_NWKSKEY + ", " + FN_APPSKEY + ", " + FN_NAME
            + ") VALUES(?, ?, ?, ?, ?)",
            new String[]{address.addr, address.devEui.toString(),
                address.nwkSKey.toString(), address.appSKey.toString(), address.name});
        cursor.close();
    }

    public static void clear(Context context) {
        DatabaseHelper dbHelper = new DatabaseHelper(context);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        Cursor cursor = db.rawQuery("DELETE FROM " + TABLE_NAME,null);
        cursor.close();
    }

    public static void update(Context context, LoraDeviceAddress address) {
        if (address.id == 0) {
            add(context, address);
            return;
        }
        DatabaseHelper dbHelper = new DatabaseHelper(context);
        SQLiteDatabase dbCount = dbHelper.getWritableDatabase();
        Cursor cursor = dbCount.rawQuery("UPDATE " + TABLE_NAME
            + " SET " + FN_ADDR
            + " = ?, SET " + FN_DEVEUI
            + " = ?, SET " + FN_NWKSKEY
            + " = ?, SET " + FN_APPSKEY
            + " = ?, SET " + FN_NAME
            + " WHERE " + FN_ID + " = ?",
                new String[]{address.addr, address.devEui.toString(), address.nwkSKey.toString(),
                        address.appSKey.toString(), address.name, Long.toString(address.id)
        });
        cursor.close();
    }

}