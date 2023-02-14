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

public class DeviceAddressProvider extends ContentProvider {

    static final String PROVIDER_NAME = "lora.address";
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
    public static final int F_ADDR = 1;
    public static final int F_DEVEUI = 2;
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
    static private final String[] SQL_CREATE_CLAUSES = {
            "CREATE TABLE " + TABLE_NAME
                    + " (" + FN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + FN_ADDR + " TEXT, "
                    + FN_DEVEUI + " TEXT, "
                    + FN_NWKSKEY + " TEXT, "
                    + FN_APPSKEY + " TEXT, "
                    + FN_NAME + " TEXT);",

            "CREATE INDEX idx_name ON " + TABLE_NAME + " (" + FN_NAME + ")",

            "CREATE INDEX idx_address ON " + TABLE_NAME + " (" + FN_ADDR + ")"
    };

    static private final String[] SQL_DROP_CLAUSES = {
            "DROP INDEX IF EXISTS idx_name",
            "DROP INDEX IF EXISTS idx_address",
            "DROP TABLE IF EXISTS " + TABLE_NAME
    };
    // static private HashMap<String, String> PROJECTION_MAP;

    // creating a database
    private static class DatabaseHelper extends SQLiteOpenHelper {
        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            for (String clause : SQL_CREATE_CLAUSES) {
                db.execSQL(clause);
            }
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            for (String clause : SQL_DROP_CLAUSES) {
                db.execSQL(clause);
            }
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
                //qb.setProjectionMap(PROJECTION_MAP);
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
        Cursor cursorCount = dbCount.rawQuery("SELECT count(*) FROM " + TABLE_NAME, null);
        cursorCount.moveToFirst();
        int r = cursorCount.getInt(0);
        cursorCount.close();
        dbCount.close();
        return r;
    }

    private static final String SELECT_ALL_PREFIX = "SELECT " + FN_ID + ", " + FN_ADDR + ", "
            + FN_DEVEUI + ", " + FN_NWKSKEY + ", " + FN_APPSKEY+ ", " + FN_NAME
            + " FROM " + TABLE_NAME;

    public static LoraDeviceAddress getById(Context context, long id) {
        DatabaseHelper dbHelper = new DatabaseHelper(context);
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery(SELECT_ALL_PREFIX + " WHERE " + FN_ID + " = ? ",
                new String[]{Long.toString(id)});
        if (!cursor.moveToFirst())
            return null;
        LoraDeviceAddress r = new LoraDeviceAddress(
            cursor.getInt(F_ID),
            cursor.getString(F_ADDR),
            cursor.getString(F_DEVEUI),
            cursor.getString(F_NWKSKEY),
            cursor.getString(F_APPSKEY),
            cursor.getString(F_NAME)
        );
        cursor.close();
        db.close();
        return r;
    }

    public static LoraDeviceAddress getByAddress(Context context, String deviceAddress) {
        DatabaseHelper dbHelper = new DatabaseHelper(context);
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery(SELECT_ALL_PREFIX + " WHERE " + FN_ADDR + " = ? ",
                new String[]{ deviceAddress.toLowerCase() });
        if (!cursor.moveToFirst())
            return null;
        LoraDeviceAddress r = new LoraDeviceAddress(
                cursor.getInt(F_ID),
                cursor.getString(F_ADDR),
                cursor.getString(F_DEVEUI),
                cursor.getString(F_NWKSKEY),
                cursor.getString(F_APPSKEY),
                cursor.getString(F_NAME)
        );
        cursor.close();
        db.close();
        return r;
    }

    public static LoraDeviceAddress getByDevEui(Context context, String devEui) {
        DatabaseHelper dbHelper = new DatabaseHelper(context);
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery(SELECT_ALL_PREFIX + " WHERE " + FN_DEVEUI + " = ? ",
                new String[]{ devEui.toLowerCase() });
        if (!cursor.moveToFirst())
            return null;
        LoraDeviceAddress r = new LoraDeviceAddress(
                cursor.getInt(F_ID),
                cursor.getString(F_ADDR),
                cursor.getString(F_DEVEUI),
                cursor.getString(F_NWKSKEY),
                cursor.getString(F_APPSKEY),
                cursor.getString(F_NAME)
        );
        cursor.close();
        db.close();
        return r;
    }

    public static void add(Context context, LoraDeviceAddress address) {
        DatabaseHelper dbHelper = new DatabaseHelper(context);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.insert(TABLE_NAME, FN_ID, address.getContentValues());
        db.close();
    }

    public static void rm(Context context, long id) {
        // remove device
        DatabaseHelper dbHelper = new DatabaseHelper(context);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete(TABLE_NAME, FN_ID + " = ?", new String[]{Long.toString(id)});
        db.close();
    }

    public static void clear(Context context) {
        DatabaseHelper dbHelper = new DatabaseHelper(context);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete(TABLE_NAME,null, null);
        db.close();
    }

    public static void update(Context context, LoraDeviceAddress address) {
        if (address.id == 0) {
            add(context, address);
            return;
        }
        DatabaseHelper dbHelper = new DatabaseHelper(context);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.update(TABLE_NAME, address.getContentValues(), FN_ID + " = ?",
                new String[]{ Long.toString(address.id)});
        db.close();
    }

    public static String toJson(Context context) {
        DatabaseHelper dbHelper = new DatabaseHelper(context);
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery(SELECT_ALL_PREFIX + " ORDER BY " + FN_NAME,null);
        StringBuilder b = new StringBuilder();
        b.append("[");
        boolean isFirst = true;
        if (cursor.moveToFirst()) {
            while (true) {
                if (isFirst)
                    isFirst = false;
                else
                    b.append(", ");
                LoraDeviceAddress r = new LoraDeviceAddress(
                    cursor.getInt(F_ID),
                    cursor.getString(F_ADDR),
                    cursor.getString(F_DEVEUI),
                    cursor.getString(F_NWKSKEY),
                    cursor.getString(F_APPSKEY),
                    cursor.getString(F_NAME)
                );
                b.append(r.toJson());
                if (!cursor.moveToNext())
                    break;
            }
        }
        b.append("]");
        cursor.close();
        db.close();
        return b.toString();
    }

}
