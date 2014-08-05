package com.hoh.sunshine.sunshine.app.test;

import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.net.Uri;
import android.test.AndroidTestCase;
import android.util.Log;

import com.hoh.sunshine.sunshine.data.WeatherDBHelper;
import com.hoh.sunshine.sunshine.data.WeatherContract.WeatherEntry;
import com.hoh.sunshine.sunshine.data.WeatherContract.LocationEntry;

/**
 * Created by funso on 7/30/14.
 */
public class TestProvider extends AndroidTestCase {

    public static final String LOG_TAG = TestDb.class.getSimpleName();

    public void testDeleteDb() throws Throwable {

        //first, wipe any old data to ensure clean test start
        mContext.deleteDatabase(WeatherDBHelper.getDateBaseName());

    }

    public void testInsertReadProvider() {

        // Test data we're going to insert into the DB to see if it works.
        String testLocationSetting = "99705";
        String testCityName = "North Pole";
        double testLatitude = 64.7488;
        double testLongitude = -147.353;

        // If there's an error in those massive SQL table creation Strings,
        // errors will be thrown here when you try to get a writable database.
        WeatherDBHelper dbHelper = new WeatherDBHelper(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        // Create a new map of values, where column names are the keys
        ContentValues values = new ContentValues();
        values.put(LocationEntry.COLUMN_LOCATION_SETTING, testLocationSetting);
        values.put(LocationEntry.COLUMN_LOC_NAME, testCityName);
        values.put(LocationEntry.COLUMN_COORD_LAT, testLatitude);
        values.put(LocationEntry.COLUMN_COORD_LONG, testLongitude);

        long locationRowId;

        //locationRowId = db.insert(LocationEntry.TABLE_NAME, null, values);
        Uri retUri = getContext().getContentResolver().insert(LocationEntry.CONTENT_URI, values);
        locationRowId = ContentUris.parseId(retUri);


        // Verify we got a row back.
        assertTrue(locationRowId != -1);
        Log.d(LOG_TAG, "New row id: " + locationRowId);


        // Data's inserted.  IN THEORY.  Now pull some out to stare at it and verify it made
        // the round trip.

        // Specify which columns you want.
        String[] columns = {
                LocationEntry._ID,
                LocationEntry.COLUMN_LOCATION_SETTING,
                LocationEntry.COLUMN_LOC_NAME,
                LocationEntry.COLUMN_COORD_LAT,
                LocationEntry.COLUMN_COORD_LONG
        };

        // A cursor is your primary interface to the query results.
        Cursor cursor = getContext().getContentResolver().query(LocationEntry.CONTENT_URI,
                null,
                null,
                null,
                null);

        // If possible, move to the first row of the query results.
        if (cursor.moveToFirst()) {
            // Get the value in each column by finding the appropriate column index.
            int locationIndex = cursor.getColumnIndex(LocationEntry.COLUMN_LOCATION_SETTING);
            String location = cursor.getString(locationIndex);

            int nameIndex = cursor.getColumnIndex((LocationEntry.COLUMN_LOC_NAME));
            String name = cursor.getString(nameIndex);

            int latIndex = cursor.getColumnIndex((LocationEntry.COLUMN_COORD_LAT));
            double latitude = cursor.getDouble(latIndex);

            int longIndex = cursor.getColumnIndex((LocationEntry.COLUMN_COORD_LONG));
            double longitude = cursor.getDouble(longIndex);

            // Hooray, data was returned!  Assert that it's the right data, and that the database
            // creation code is working as intended.
            // Then take a break.  We both know that wasn't easy.
            assertEquals(testCityName, name);
            assertEquals(testLocationSetting, location);
            assertEquals(testLatitude, latitude);
            assertEquals(testLongitude, longitude);

            // Fantastic.  Now that we have a location, add some weather!
        } else {
            // That's weird, it works on MY machine...
            fail("No values returned :(");
        }

        //now inserting and verifying with the weather table

        String [] weatherColumns = {
                WeatherEntry.COLUMN_LOC_KEY,
                WeatherEntry.COLUMN_DATETEXT,
                WeatherEntry.COLUMN_DEGREES,
                WeatherEntry.COLUMN_HUMIDITY,
                WeatherEntry.COLUMN_PRESSURE,
                WeatherEntry.COLUMN_MAX_TEMP,
                WeatherEntry.COLUMN_MIN_TEMP,
                WeatherEntry.COLUMN_SHORT_DESC,
                WeatherEntry.COLUMN_WIND_SPEED,
                WeatherEntry.COLUMN_WEATHER_ID
        };

        String [] weatherReadings = {((Long)locationRowId).toString(), "20141205", "1.1", "1.2", "1.3", "75", "65","Asteroids", "5.5", "321"};
        ContentValues weatherValues = new ContentValues();

        for(int i = 0; i < weatherColumns.length; i++){
            if(i == 1 || i == 7){
                weatherValues.put(weatherColumns[i], weatherReadings[i]);
                continue;
            }

            weatherValues.put(weatherColumns[i], Double.parseDouble(weatherReadings[i]));
        }


        //long weatherRowId = db.insert(WeatherEntry.TABLE_NAME, null, weatherValues);
        Uri weaUriWithRowId = getContext().getContentResolver().insert(WeatherEntry.CONTENT_URI, weatherValues);
        long weatherRowId = ContentUris.parseId(weaUriWithRowId);
        assertTrue(weatherRowId != -1);

        // A cursor is your primary interface to the query results.
        Cursor weatherCursor = getContext().getContentResolver().query(WeatherEntry.CONTENT_URI,
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                null  // sort order
        );

        if (!weatherCursor.moveToFirst()) {
            fail("No weather data returned!");
        }


        for(int i = 0; i < weatherColumns.length; i++){
            if(i == 1 || i == 7){
                assertEquals(weatherCursor.getString(
                        weatherCursor.getColumnIndex(weatherColumns[i])), weatherValues.getAsString(weatherColumns[i]));
                continue;
            }

            assertEquals(weatherCursor.getDouble(
                    weatherCursor.getColumnIndex(weatherColumns[i])), weatherValues.getAsDouble(weatherColumns[i]));
        }

        weatherCursor.close();
        dbHelper.close();
    }

    public void testGetType() {
        // content://com.hoh.sunshine.sunshine/weather/
        String type = mContext.getContentResolver().getType(WeatherEntry.CONTENT_URI);
        // vnd.android.cursor.dir/com.hoh.sunshine.sunshine/weather
        assertEquals(WeatherEntry.CONTENT_TYPE, type);

        String testLocation = "94074";
        // content://com.hoh.sunshine.sunshine/weather/94074
        type = mContext.getContentResolver().getType(
                WeatherEntry.buildWeatherLocation(testLocation));
        // vnd.android.cursor.dir/com.hoh.sunshine.sunshine/weather
        assertEquals(WeatherEntry.CONTENT_TYPE, type);

        String testDate = "20140612";
        // content://com.hoh.sunshine.sunshine/weather/94074/20140612
        type = mContext.getContentResolver().getType(
                WeatherEntry.buildWeatherLocationWithDate(testLocation, testDate));
        // vnd.android.cursor.item/com.hoh.sunshine.sunshine/weather
        assertEquals(WeatherEntry.CONTENT_ITEM_TYPE, type);

        // content://com.hoh.sunshine.sunshine/location/
        type = mContext.getContentResolver().getType(LocationEntry.CONTENT_URI);
        // vnd.android.cursor.dir/com.hoh.sunshine.sunshine/location
        assertEquals(LocationEntry.CONTENT_TYPE, type);

        // content://com.hoh.sunshine.sunshine/location/1
        type = mContext.getContentResolver().getType(LocationEntry.buildLocationUri(1L));
        // vnd.android.cursor.item/com.hoh.sunshine.sunshine/location
        assertEquals(LocationEntry.CONTENT_ITEM_TYPE, type);
    }
}
