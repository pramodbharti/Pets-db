package com.example.android.pets.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.example.android.pets.data.PetsContract.PetsEntry;

/**
 * Created by pramod on 18/1/18.
 */

public class PetsSQLiteHelper extends SQLiteOpenHelper {

    public static final String LOG_TAG = PetsSQLiteHelper.class.getSimpleName();
    private static final String DATABASE_NAME = "shelter.db";
    private static final int DATABASE_VERSION = 1;

    public PetsSQLiteHelper(Context context){
        super(context,DATABASE_NAME,null,DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        String SQL_PETS_CREATE = "CREATE TABLE " + PetsEntry.TABLE_NAME + " ("
                + PetsEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + PetsEntry.COLUMN_NAME + " TEXT NOT NULL, "
                + PetsEntry.COLUMN_BREED + " TEXT, "
                + PetsEntry.COLUMN_GENDER + " INTEGER NOT NULL, "
                + PetsEntry.COLUMN_WEIGHT + " INTEGER NOT NULL DEFAULT 0);";
        Log.e(LOG_TAG,SQL_PETS_CREATE);
        sqLiteDatabase.execSQL(SQL_PETS_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }
}
