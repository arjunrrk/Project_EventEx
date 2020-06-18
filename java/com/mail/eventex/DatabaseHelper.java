package com.mail.eventex;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.widget.Toast;

public class DatabaseHelper extends SQLiteOpenHelper {
    public DatabaseHelper(Context context) {
        super(context, "userbase.db", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS Authorization (email_id TEXT PRIMARY KEY, access_token TEXT,refresh_token TEXT, history_id TEXT DEFAULT 'null')");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }

    Cursor readData(String table_name) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        if (db != null) {
            try {
                String query = "SELECT * FROM " + table_name;
                cursor = db.rawQuery(query, null);
            } catch (Exception e) {
                Log.e("ERROR", "readData: " + e.getMessage());
            }
        }
        return cursor;
    }

    Cursor readStarredData(String table_name) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        if (db != null) {
            try {
                String query = "SELECT * FROM " + table_name + " WHERE starred = 'true'";
                cursor = db.rawQuery(query, null);
            } catch (Exception e) {
                Log.e("ERROR", "readData: " + e.getMessage());
            }
        }
        return cursor;
    }

    Cursor readDateSortedData(String table_name) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        if (db != null) {
            try {
                String query = "SELECT * FROM " + table_name + " ORDER BY date ASC";
                cursor = db.rawQuery(query, null);
            } catch (Exception e) {
                Log.e("ERROR", "readData: " + e.getMessage());
            }
        }
        return cursor;
    }
}
