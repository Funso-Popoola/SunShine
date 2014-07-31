package com.hoh.sunshine.sunshine.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.hoh.sunshine.sunshine.data.WeatherContract.LocationEntry;
import com.hoh.sunshine.sunshine.data.WeatherContract.WeatherEntry;
/**
 * Created by funso on 7/30/14.
 */
public class WeatherDBHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "weather.db";

    public WeatherDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public static String getDateBaseName(){
        return DATABASE_NAME;
    }
    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

        final String LOCATION_TABLE_CREATION_SQL = "CREATE TABLE " + LocationEntry.TABLE_NAME +
                                                    " ( " + LocationEntry._ID + " INTEGER PRIMARY KEY, " +
                                                            LocationEntry.COLUMN_LOC_NAME + " TEXT NOT NULL, " +
                                                            LocationEntry.COLUMN_LOCATION_SETTING + " TEXT UNIQUE NOT NULL, " +
                                                            LocationEntry.COLUMN_COORD_LAT + " REAL NOT NULL, " +
                                                            LocationEntry.COLUMN_COORD_LONG + " REAL NOT NULL, " +
                                                            "UNIQUE ( " + LocationEntry.COLUMN_LOCATION_SETTING + ") " +
                                                      "ON CONFLICT IGNORE );";

        final String WEATHER_TABLE_CREATION_SQL = "CREATE TABLE " + WeatherEntry.TABLE_NAME +
                                                  " ( " + WeatherEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                                                        + WeatherEntry.COLUMN_LOC_KEY + " TEXT NOT NULL, "
                                                        + WeatherEntry.COLUMN_DATETEXT + " TEXT NOT NULL, "
                                                        + WeatherEntry.COLUMN_WEATHER_ID + " TEXT NOT NULL, "
                                                        + WeatherEntry.COLUMN_SHORT_DESC + " TEXT NOT NULL, "
                                                        + WeatherEntry.COLUMN_MIN_TEMP + " REAL NOT NULL, "
                                                        + WeatherEntry.COLUMN_MAX_TEMP + " REAL NOT NULL, "
                                                        + WeatherEntry.COLUMN_HUMIDITY + " REAL NOT NULL, "
                                                        + WeatherEntry.COLUMN_PRESSURE + " REAL NOT NULL, "
                                                        + WeatherEntry.COLUMN_WIND_SPEED + " REAL NOT NULL, "
                                                        + WeatherEntry.COLUMN_DEGREES + " REAL NOT NULL, "
                                                        + "FOREIGN KEY ( "
                                                                            + WeatherEntry.COLUMN_LOC_KEY
                                                                     +" ) "
                                                        + "REFERENCES "
                                                                        + LocationEntry.TABLE_NAME
                                                                                + " ( "
                                                                                        + LocationEntry._ID
                                                                                + " ), "

                                                        + " UNIQUE ("
                                                                    + WeatherEntry.COLUMN_DATETEXT + ", "
                                                                    + WeatherEntry.COLUMN_LOC_KEY
                                                                    +" ) ON CONFLICT REPLACE );";

        sqLiteDatabase.execSQL(LOCATION_TABLE_CREATION_SQL);
        sqLiteDatabase.execSQL(WEATHER_TABLE_CREATION_SQL);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i2) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + WeatherEntry.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + LocationEntry.TABLE_NAME);
        onCreate(sqLiteDatabase);
    }
}
