package com.example.falldetectiondnn;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

class DatabaseHelper extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = "SensorDataDB.sqlite";
    public static final String TABLE_NAME = "SensorDataTable";

    private static final String AccX = "AccX";
    private static final String AccY = "AccY";
    private static final String AccZ = "AccZ";
    private static final String GyroX = "GyroX";
    private static final String GyroY = "GyroY";
    private static final String GyroZ = "GyroZ";

    public DatabaseHelper(Context context) {
        super(context,DATABASE_NAME,null,1);
    }
    @Override
    public void onCreate(SQLiteDatabase db) {

        String query = "CREATE TABLE " + TABLE_NAME + " ("
                + AccX + " TEXT,"
                + AccY + " TEXT,"
                + AccZ + " TEXT,"
                + GyroX + " TEXT,"
                + GyroY + " TEXT,"
                + GyroZ + " TEXT)";

        // at last we are calling a exec sql
        // method to execute above sql query
        db.execSQL(query);
    }
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS "+ TABLE_NAME);
        onCreate(db);
    }
    public boolean insert(float[] values) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(AccX, String.valueOf(values[0]));
        contentValues.put(AccY, String.valueOf(values[1]));
        contentValues.put(AccZ, String.valueOf(values[2]));
        contentValues.put(GyroX, String.valueOf(values[3]));
        contentValues.put(GyroY, String.valueOf(values[4]));
        contentValues.put(GyroZ, String.valueOf(values[5]));
        db.insert(TABLE_NAME, null, contentValues);
        db.close();
        return true;
    }
}

