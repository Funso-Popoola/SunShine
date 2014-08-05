package com.hoh.sunshine.sunshine.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.util.Log;

import com.hoh.sunshine.sunshine.data.WeatherContract;
/**
 * Created by funso on 7/31/14.
 */
public class WeatherProvider extends ContentProvider {

    public static final int WEATHER = 100;

    public static final int WEATHER_WITH_LOCATION = 101;

    public static final int WEATHER_WITH_LOCATION_AND_DATE = 102;

    public static final int LOCATION = 300;

    public static final int LOCATION_WITH_ID = 301;

    private WeatherDBHelper mOpenHelper;

    public static final UriMatcher uriMatcher = buildUriMatcher();

    private static final SQLiteQueryBuilder sWeatherByLocationSettingQueryBuilder;

    static{
        sWeatherByLocationSettingQueryBuilder = new SQLiteQueryBuilder();
        sWeatherByLocationSettingQueryBuilder.setTables(
                WeatherContract.WeatherEntry.TABLE_NAME + " INNER JOIN " +
                        WeatherContract.LocationEntry.TABLE_NAME +
                        " ON " + WeatherContract.WeatherEntry.TABLE_NAME +
                        "." + WeatherContract.WeatherEntry.COLUMN_LOC_KEY +
                        " = " + WeatherContract.LocationEntry.TABLE_NAME +
                        "." + WeatherContract.LocationEntry._ID);
    }

    private static final String sLocationSettingSelection =
            WeatherContract.LocationEntry.TABLE_NAME+
                    "." + WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING + " = ? ";

    private static final String sLocationSettingsWithStartDate =
            WeatherContract.LocationEntry.TABLE_NAME+
                    "." + WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING + " = ? AND " +
                    WeatherContract.WeatherEntry.COLUMN_DATETEXT + " >= ? ";

    private static final String sLocationSettingWithDate =
            WeatherContract.LocationEntry.TABLE_NAME +
                    "." + WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING + " = ? AND " +
                    WeatherContract.WeatherEntry.COLUMN_DATETEXT + " = ? ";

    private Cursor getWeatherByLocationSetting(Uri uri, String [] projection, String sortOrder){

        String locationSetting = WeatherContract.WeatherEntry.getLocationSettingFromUri(uri);
        String startDate = WeatherContract.WeatherEntry.getStartDateFromUri(uri);

        String selection;
        String [] selectionArgs;

        if(startDate == null){
            selection = sLocationSettingSelection;
            selectionArgs = new String []{locationSetting};
        }else {
            selection = sLocationSettingsWithStartDate;
            selectionArgs = new String[]{locationSetting, startDate};
        }

        return mOpenHelper.getReadableDatabase().
                query(sWeatherByLocationSettingQueryBuilder.getTables(),
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);

        /*

        return sWeatherByLocationSettingQueryBuilder.query(mOpenHelper.getReadableDatabase(),
                projection,
                sLocationSettingSelection,
                new String[]{locationSetting, startDate},
                null,
                null,
                sortOrder
        );
         */
    }

    private Cursor getWeatherByLocationSettingAndDate(Uri uri, String [] projection, String sortOrder){
        String locationSetting = WeatherContract.WeatherEntry.getLocationSettingFromUri(uri);
        String date = WeatherContract.WeatherEntry.getDateFromUri(uri);

        String selection = sLocationSettingWithDate;
        String [] selectionArgs = {locationSetting, date};

        return sWeatherByLocationSettingQueryBuilder.
                query(mOpenHelper.getReadableDatabase(),
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);


    }
    @Override
    public boolean onCreate() {
        mOpenHelper = new WeatherDBHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Cursor mCursor = null;
        final int match = uriMatcher.match(uri);
        switch (match){
            case WEATHER:
                mCursor = mOpenHelper.getWritableDatabase().
                        query(WeatherContract.WeatherEntry.TABLE_NAME,
                                projection,
                                selection,
                                selectionArgs,
                                null,
                                null,
                                sortOrder);
                break;
            case WEATHER_WITH_LOCATION:
                mCursor = getWeatherByLocationSetting(uri, projection, sortOrder);
                break;
            case WEATHER_WITH_LOCATION_AND_DATE:
                mCursor = getWeatherByLocationSettingAndDate(uri, projection, sortOrder);
                break;
            case LOCATION:
                mCursor = mOpenHelper.getWritableDatabase().
                        query(WeatherContract.LocationEntry.TABLE_NAME,
                                projection,
                                selection,
                                selectionArgs,
                                null,
                                null,
                                sortOrder);
                break;
            case LOCATION_WITH_ID:
                selection = WeatherContract.LocationEntry._ID + " = '" + ContentUris.parseId(uri) + "'";

                mCursor = mOpenHelper.getWritableDatabase().
                        query(WeatherContract.LocationEntry.TABLE_NAME,
                                projection,
                                selection,
                                null,
                                null,
                                null,
                                sortOrder);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri" + uri);


        }
        if(mCursor != null){
            mCursor.setNotificationUri(getContext().getContentResolver(), uri);
        }

        return mCursor;
    }

    @Override
    public String getType(Uri uri) {
        final int match = uriMatcher.match(uri);
        switch (match){
            case WEATHER:
                return WeatherContract.WeatherEntry.CONTENT_TYPE;
            case WEATHER_WITH_LOCATION:
                return WeatherContract.WeatherEntry.CONTENT_TYPE;
            case WEATHER_WITH_LOCATION_AND_DATE:
                return WeatherContract.WeatherEntry.CONTENT_ITEM_TYPE;
            case LOCATION:
                return WeatherContract.LocationEntry.CONTENT_TYPE;
            case LOCATION_WITH_ID:
                return WeatherContract.LocationEntry.CONTENT_ITEM_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown Uri" + uri);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = uriMatcher.match(uri);
        Uri retUri = null;

        switch (match){
            case WEATHER:
                long weatherRowId = db.insert(WeatherContract.WeatherEntry.TABLE_NAME, null, contentValues);
                if( weatherRowId > 0)
                    retUri = WeatherContract.WeatherEntry.buildWeatherUri(weatherRowId);
                else
                    throw new SQLException("Failed to insert into database" + uri);
                break;
            case LOCATION:
                long locationRowId = db.insert(WeatherContract.LocationEntry.TABLE_NAME, null, contentValues);
                if(locationRowId > 0)
                    retUri = WeatherContract.LocationEntry.buildLocationUri(locationRowId);
                else
                    throw new SQLException("Failed to insert into database" + uri);
                break;
            default:
                throw new UnsupportedOperationException("Unknown Uri" + uri);
        }
        if(retUri != null)
            getContext().getContentResolver().notifyChange(uri, null);
        return retUri;
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        final int match = uriMatcher.match(uri);
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        switch (match){
            case WEATHER:
                db.beginTransaction();
                int returnCount = 0;
                long _id;
                try{
                    for(ContentValues value : values){
                        _id = db.insert(WeatherContract.WeatherEntry.TABLE_NAME, null, value);
                        if(_id != -1){
                            returnCount++;
                        }
                    }
                    db.setTransactionSuccessful();
                }
                catch (IllegalStateException e){
                    Log.e("BulkInsert in " + WeatherProvider.class.getSimpleName(),"Illegal", e);
                }
                finally {
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri, null);
                return returnCount;

            default:
                return super.bulkInsert(uri, values);
        }

    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        final int match = uriMatcher.match(uri);
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int numOfRowsAffected = 0;
        switch (match){
            case WEATHER:
                numOfRowsAffected = db.delete(WeatherContract.WeatherEntry.TABLE_NAME, null, null);
                break;
            case WEATHER_WITH_LOCATION:
                final String startDate = WeatherContract.WeatherEntry.getStartDateFromUri(uri);
                final String locationSettings = WeatherContract.WeatherEntry.getLocationSettingFromUri(uri);
                if(startDate != null){
                    selection = WeatherContract.WeatherEntry.TABLE_NAME + "."
                            + WeatherContract.WeatherEntry.COLUMN_LOC_KEY + " = ? AND "
                            + WeatherContract.WeatherEntry.COLUMN_DATETEXT + " >= ?";

                    selectionArgs = new String []{locationSettings, startDate};
                }else{
                    selection = WeatherContract.WeatherEntry.TABLE_NAME + "."
                            + WeatherContract.WeatherEntry.COLUMN_LOC_KEY + " = ?";

                    selectionArgs = new String[]{locationSettings};
                }

                numOfRowsAffected = db.delete(WeatherContract.WeatherEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case WEATHER_WITH_LOCATION_AND_DATE:
                selection = WeatherContract.WeatherEntry.TABLE_NAME + "."
                        + WeatherContract.WeatherEntry.COLUMN_LOC_KEY + " = ? AND"
                        + WeatherContract.WeatherEntry.COLUMN_DATETEXT + " = ?";
                selectionArgs = new String[]{WeatherContract.WeatherEntry.getLocationSettingFromUri(uri),
                        WeatherContract.WeatherEntry.getDateFromUri(uri)};

                numOfRowsAffected = db.delete(WeatherContract.WeatherEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case LOCATION:
                numOfRowsAffected = db.delete(WeatherContract.LocationEntry.TABLE_NAME, null, null);
                break;
            case LOCATION_WITH_ID:
                final long id = ContentUris.parseId(uri);
                if(id != -1){
                    selection = WeatherContract.LocationEntry._ID + " = '" + id + "'";
                    numOfRowsAffected = db.delete(WeatherContract.LocationEntry.TABLE_NAME, selection, selectionArgs);
                }
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        if(numOfRowsAffected > 0)
            getContext().getContentResolver().notifyChange(uri, null);
        return 0;
    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String selection, String[] selectionArgs) {
        final int match = uriMatcher.match(uri);
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int numOfRowsAffected = 0;
        switch (match){
            case WEATHER:
                numOfRowsAffected = db.update(WeatherContract.WeatherEntry.TABLE_NAME, contentValues, selection, selectionArgs);
                break;
            case WEATHER_WITH_LOCATION:
                final String startDate = WeatherContract.WeatherEntry.getStartDateFromUri(uri);
                final String locationSettings = WeatherContract.WeatherEntry.getLocationSettingFromUri(uri);
                if(startDate != null){
                    selection = WeatherContract.WeatherEntry.TABLE_NAME + "."
                            + WeatherContract.WeatherEntry.COLUMN_LOC_KEY + " = ? AND "
                            + WeatherContract.WeatherEntry.COLUMN_DATETEXT + " >= ?";

                    selectionArgs = new String []{locationSettings, startDate};
                }else{
                    selection = WeatherContract.WeatherEntry.TABLE_NAME + "."
                            + WeatherContract.WeatherEntry.COLUMN_LOC_KEY + " = ?";

                    selectionArgs = new String[]{locationSettings};
                }

                numOfRowsAffected = db.update(WeatherContract.WeatherEntry.TABLE_NAME, contentValues, selection, selectionArgs);
                break;
            case WEATHER_WITH_LOCATION_AND_DATE:
                selection = WeatherContract.WeatherEntry.TABLE_NAME + "."
                        + WeatherContract.WeatherEntry.COLUMN_LOC_KEY + " = ? AND"
                        + WeatherContract.WeatherEntry.COLUMN_DATETEXT + " = ?";
                selectionArgs = new String[]{WeatherContract.WeatherEntry.getLocationSettingFromUri(uri),
                        WeatherContract.WeatherEntry.getDateFromUri(uri)};

                numOfRowsAffected = db.update(WeatherContract.WeatherEntry.TABLE_NAME, contentValues, selection, selectionArgs);
                break;
            case LOCATION:
                numOfRowsAffected = db.update(WeatherContract.LocationEntry.TABLE_NAME, contentValues, null, null);
                break;
            case LOCATION_WITH_ID:
                final long id = ContentUris.parseId(uri);
                if(id != -1){
                    selection = WeatherContract.LocationEntry._ID + " = '" + id + "'";
                    numOfRowsAffected = db.update(WeatherContract.LocationEntry.TABLE_NAME, contentValues, selection, selectionArgs);
                }

                break;
            default:
                throw new UnsupportedOperationException("Unknown uri : " + uri);

        }
        if(numOfRowsAffected > 0)
            getContext().getContentResolver().notifyChange(uri, null);
        return numOfRowsAffected;
    }

    private static UriMatcher buildUriMatcher(){

        final UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String weather = WeatherContract.PATH_WEATHER;
        final String location = WeatherContract.PATH_LOCATION;

        uriMatcher.addURI(WeatherContract.CONTENT_AUTHORITY, weather, WEATHER);
        uriMatcher.addURI(WeatherContract.CONTENT_AUTHORITY, weather + "/*", WEATHER_WITH_LOCATION);
        uriMatcher.addURI(WeatherContract.CONTENT_AUTHORITY, weather + "/*/*", WEATHER_WITH_LOCATION_AND_DATE);
        uriMatcher.addURI(WeatherContract.CONTENT_AUTHORITY, location, LOCATION);
        uriMatcher.addURI(WeatherContract.CONTENT_AUTHORITY, location + "/#", LOCATION_WITH_ID);

        return uriMatcher;
    }
}
