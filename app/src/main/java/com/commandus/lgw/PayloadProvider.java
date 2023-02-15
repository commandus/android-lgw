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
import android.util.Log;

import java.util.HashMap;

public class PayloadProvider extends ContentProvider {
    static final String PROVIDER_NAME = "lora.data";
    // content URI
    static final String URL = "content://" + PROVIDER_NAME;
    static final String URL_PAYLOAD = URL + "/payload";

    // parsing the content URI
    public static final Uri CONTENT_URI = Uri.parse(URL);
    public static final Uri CONTENT_URI_PAYLOAD = Uri.parse(URL_PAYLOAD);
    public static final String FN_ID = "id";
    public static final String FN_RECEIVED = "received";
    public static final String FN_DEVEUI = "eui";
    public static final String FN_DEVNAME = "name";
    // hex
    public static final String FN_PAYLOAD = "payload";
    public static final String FN_FREQUENCY = "frequency";
    public static final String FN_RSSI = "rssi";
    public static final String FN_LSNR = "lsnr";

    public static final int F_ID = 0;
    public static final int F_RECEIVED = 1;
    public static final int F_DEVEUI = 2;
    public static final int F_DEVNAME = 3;
    public static final int F_PAYLOAD = 4;
    public static final int F_FREQUENCY = 5;
    public static final int F_RSSI = 6;
    public static final int F_LSNR = 7;

    public static final String[] PROJECTION = {
        FN_ID, FN_RECEIVED, FN_DEVEUI, FN_DEVNAME, FN_PAYLOAD, FN_FREQUENCY, FN_RSSI, FN_LSNR
    };

    static final UriMatcher uriMatcher;
    static final int M_PAYLOAD_LIST = 1;
    static final int M_PAYLOAD = 2;

    static {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(PROVIDER_NAME, "payload", M_PAYLOAD_LIST);
        uriMatcher.addURI(PROVIDER_NAME, "payload/*", M_PAYLOAD);
    }
    private SQLiteDatabase db;
    static private final String DATABASE_NAME = "lora_payload";
    static private final String TABLE_NAME = "payload";

    // declaring version of the database
    static private final int DATABASE_VERSION = 1;

    // sql query to create the table
    static private final String[] SQL_CREATE_CLAUSES = {"CREATE TABLE " + TABLE_NAME
        + " (" + FN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
        + FN_RECEIVED + " INTEGER, "
        + FN_DEVEUI + " TEXT, "
        + FN_DEVNAME + " TEXT, "
        + FN_PAYLOAD + " TEXT, "
        + FN_FREQUENCY + " INTEGER, "
        + FN_RSSI + " INTEGER, "
        + FN_LSNR + " REAL);",
        "CREATE INDEX idx_deveui ON " + TABLE_NAME + " (" + FN_DEVEUI + ")",
        "CREATE INDEX idx_name ON " + TABLE_NAME + " (" + FN_DEVNAME + ")",
        "CREATE INDEX idx_received ON " + TABLE_NAME + " (" + FN_RECEIVED + ")"
    };

    static private final String[] SQL_DROP_CLAUSES = {
        "DROP INDEX IF EXISTS idx_deveui",
        "DROP INDEX IF EXISTS idx_name",
        "DROP INDEX IF EXISTS idx_received",
        "DROP TABLE IF EXISTS " + TABLE_NAME
    };

    static private HashMap<String, String> PROJECTION_MAP;

    public static String toJson(Context context) {
        PayloadProvider.DatabaseHelper dbHelper = new PayloadProvider.DatabaseHelper(context);
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery(SELECT_PREFIX + " FROM " + TABLE_NAME + " ORDER BY " + FN_RECEIVED,null);
        StringBuilder b = new StringBuilder();
        b.append("[");
        boolean isFirst = true;
        if (cursor.moveToFirst()) {
            while (true) {
                if (isFirst)
                    isFirst = false;
                else
                    b.append(", ");
                Payload r = new Payload(
                    cursor.getString(F_DEVEUI),
                    cursor.getString(F_DEVNAME),
                    cursor.getString(F_PAYLOAD),
                    cursor.getInt(F_FREQUENCY),
                    cursor.getInt(F_RSSI),
                    cursor.getFloat(F_LSNR)
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

    private static class DatabaseHelper extends SQLiteOpenHelper {
        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            for (String clause:  SQL_CREATE_CLAUSES) {
                db.execSQL(clause);
            }
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            for (String clause:  SQL_DROP_CLAUSES) {
                db.execSQL(clause);
            }
            onCreate(db);
        }
    }


    public PayloadProvider() {
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        int count;
        switch (uriMatcher.match(uri)) {
            case M_PAYLOAD:
                String id = uri.getPathSegments().get(1);
                count = db.delete(TABLE_NAME, FN_ID +  " = " + id +
                        (!TextUtils.isEmpty(selection) ? " AND (" + selection + ')' : ""), selectionArgs);
                break;
            case M_PAYLOAD_LIST:
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
            case M_PAYLOAD:
                return "vnd.android.cursor.dir/payload";
            case M_PAYLOAD_LIST:
                return "vnd.android.cursor.item/payload";
            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        Log.i("LGW", values.toString());
        long rowID = db.insert(TABLE_NAME, "", values);
        if (rowID > 0) {
            Uri r = ContentUris.withAppendedId(CONTENT_URI_PAYLOAD, rowID);
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
            case M_PAYLOAD_LIST:
                qb.setProjectionMap(PROJECTION_MAP);
                break;
            case M_PAYLOAD:
                qb.appendWhere( FN_ID + " = " + uri.getPathSegments().get(1));
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
        if (sortOrder == null || sortOrder.equals("")) {
            sortOrder = FN_RECEIVED;
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
            case M_PAYLOAD_LIST:
                count = db.update(TABLE_NAME, values, selection, selectionArgs);
                break;
            case M_PAYLOAD:
                count = db.update(TABLE_NAME, values, FN_ID + " = " + uri.getPathSegments().get(1) +
                        (!TextUtils.isEmpty(selection) ? " AND (" + selection + ')' : ""), selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    // helper functions

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
    private static final String SELECT_PREFIX = "SELECT " + FN_ID + ", " + FN_RECEIVED + ", "
            + FN_DEVEUI + ", " + FN_DEVNAME + ", " + FN_PAYLOAD+ ", " + FN_FREQUENCY + ", "
            + FN_RSSI + ", " + FN_LSNR;

    public static Payload getById(Context context, long id) {
        DatabaseHelper dbHelper = new DatabaseHelper(context);
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery(SELECT_PREFIX
            + " FROM " + TABLE_NAME + " WHERE " + FN_ID + " = ? ",
            new String[]{Long.toString(id)});
        if (!cursor.moveToFirst())
            return null;
        Payload r = new Payload(
            cursor.getLong(F_ID),
            cursor.getLong(F_RECEIVED),
            cursor.getString(F_DEVEUI),
            cursor.getString(F_DEVNAME),
            cursor.getString(F_PAYLOAD),
            cursor.getInt(F_FREQUENCY),
            cursor.getInt(F_RSSI),
            cursor.getFloat(F_LSNR)
        );
        cursor.close();
        db.close();
        return r;
    }

    public static Payload getLastByDevEui(Context context, String devEui) {
        DatabaseHelper dbHelper = new DatabaseHelper(context);
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery(SELECT_PREFIX
            + " FROM " + TABLE_NAME + " WHERE " + FN_DEVEUI + " = ?"
            + " ORDER BY " + FN_RECEIVED + " DESC",
            new String[]{ devEui.toLowerCase() });
        if (!cursor.moveToFirst())
            return null;
        Payload r = new Payload(
            cursor.getLong(F_ID),
            cursor.getLong(F_RECEIVED),
            cursor.getString(F_DEVEUI),
            cursor.getString(F_DEVNAME),
            cursor.getString(F_PAYLOAD),
            cursor.getInt(F_FREQUENCY),
            cursor.getInt(F_RSSI),
            cursor.getFloat(F_LSNR)
        );
        cursor.close();
        db.close();
        return r;
    }

    public static void add(Context context, Payload payload) {
        DatabaseHelper dbHelper = new DatabaseHelper(context);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.insert(TABLE_NAME, FN_ID, payload.getContentValues());
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

    public static void update(Context context, Payload payload) {
        if (payload.id == 0) {
            add(context, payload);
            return;
        }
        DatabaseHelper dbHelper = new DatabaseHelper(context);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.update(TABLE_NAME, payload.getContentValues(), FN_ID + " = ?",
                new String[]{ Long.toString(payload.id)});
        db.close();
    }

}