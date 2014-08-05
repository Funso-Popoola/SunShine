package com.hoh.sunshine.sunshine.data;

import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by funso on 7/30/14.
 * In this class declaration,
 * Specific constants were established to help encode and decode uris needed by the ContentProvider Class
 * Two Inner Classes;
 * The first was to capture all the attributes of the weather information gotten form the cloud,
 *      establish important constants as the table name and database creation columns
 *      implement methods for building proper uris to request from the ContentProvider and Resolver
 * The second is just like it; but for Location instead of weather forecast
 */
public class WeatherContract {

    //the scheme being the beginning part of every content uri as required by the content provider
    public static final String URI_SCHEME = "content://";

    //use the package name as the content authority of the database implementation
    public static final String CONTENT_AUTHORITY = "com.hoh.sunshine.sunshine";

    //the basic uri upon which others are addendum
    public static final Uri BASE_CONTENT_URI = Uri.parse(URI_SCHEME + CONTENT_AUTHORITY);

    //path added to uri to specify Table `location` as the database target
    public static final String PATH_LOCATION = "location";

    //path added to uri to specify Table `weather` as the db target
    public static final String PATH_WEATHER = "weather";


    /*
    The following is the class that prepares the basic properties, structures and function
    of the Weather Entry table in the Database
     */

    public static final class WeatherEntry implements BaseColumns{

        //the base uri with the application package as signature and the path to the weather Entry Table.
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_WEATHER).build();

        //the content-type for the content provider to return a list of item that matches the specified uri
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/" + CONTENT_AUTHORITY + "/" + PATH_WEATHER;

        //content-type for the content provider to return a single item as match the uri specified
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/" + CONTENT_AUTHORITY + "/" + PATH_WEATHER;

        public static final String TABLE_NAME = "weather";

        // Column with the foreign key into the location table.
        public static final String COLUMN_LOC_KEY = "location_id";
        // Date, stored as Text with format yyyy-MM-dd
        public static final String COLUMN_DATETEXT = "date";
        // Weather id as returned by API, to identify the icon to be used
        public static final String COLUMN_WEATHER_ID = "weather_id";

        // Short description and long description of the weather, as provided by API.
        // e.g "clear" vs "sky is clear".
        public static final String COLUMN_SHORT_DESC = "short_desc";

        // Min and max temperatures for the day (stored as floats)
        public static final String COLUMN_MIN_TEMP = "min";
        public static final String COLUMN_MAX_TEMP = "max";

        // Humidity is stored as a float representing percentage
        public static final String COLUMN_HUMIDITY = "humidity";

        // Humidity is stored as a float representing percentage
        public static final String COLUMN_PRESSURE = "pressure";

        // Wind speed is stored as a float representing wind speed  mph
        public static final String COLUMN_WIND_SPEED = "wind";

        // Degrees are meteorological degrees (e.g, 0 is north, 180 is south).  Stored as floats.
        public static final String COLUMN_DEGREES = "degrees";

        private static final String DATE_FORMAT = "yyyyMMdd";

        /*
        The following are all helper functions to help build suitable uri for
        groups of request for the Content Provider to handle
         */

        public static Uri buildWeatherUri(long id) {
            //appends an id as pert of the Uri
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static Uri buildWeatherLocation(String locationSetting) {
            //makes the Uri in the form CONTENT_URI/locationSetting
            //e.g content://com.hoh.sunshine.sunshine/weather/94043
            return CONTENT_URI.buildUpon().appendPath(locationSetting).build();
        }

        public static Uri buildWeatherLocationWithStartDate(
                String locationSetting, String startDate) {

            //makes Uri in the form CONTENT_URI/locationSetting?dt=startDate
            return CONTENT_URI.buildUpon().appendPath(locationSetting)
                    .appendQueryParameter(COLUMN_DATETEXT, startDate).build();
        }

        public static Uri buildWeatherLocationWithDate(String locationSetting, String date) {
            //makes the Uri in the form: CONTENT_URI/locationSetting/date
            return CONTENT_URI.buildUpon().appendPath(locationSetting).appendPath(date).build();
        }


        /*
        The following are helper functions to help get specific informations
        encoded into the uri as parameter unto the content provider
         */
        public static String getLocationSettingFromUri(Uri uri) {
            //retrieves the locationSetting form the given Uri
            //we use index 1 since locationSetting is always the first appended path in our Uris
            return uri.getPathSegments().get(1);
        }

        public static String getDateFromUri(Uri uri) {
            //retrieves the date form the given Uri
            //we use index 2 since date is always the second appended path in our Uris
            return uri.getPathSegments().get(2);
        }

        public static String getStartDateFromUri(Uri uri) {
            //retrieves the StartDate whenever it's encoded in uri as a query parameter
            //such uris are used to get weather forecast starting form a particular date
            return uri.getQueryParameter(COLUMN_DATETEXT);
        }

        public static String getDbDateString(Date date){
            SimpleDateFormat format = new SimpleDateFormat(DATE_FORMAT);
            return format.format(date);
        }

        /**
         * Converts a dateText to a long Unix time representation
         * @param dateText the input date string
         * @return the Date object
         */
        public static Date getDateFromDb(String dateText) {
            SimpleDateFormat dbDateFormat = new SimpleDateFormat(DATE_FORMAT);
            try {
                return dbDateFormat.parse(dateText);
            } catch ( ParseException e ) {
                e.printStackTrace();
                return null;
            }
        }
    }

    public static final class LocationEntry implements BaseColumns{

        //content uri => the_base_uri/location
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_LOCATION).build();

        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/" + CONTENT_AUTHORITY + "/" + PATH_LOCATION;

        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/" + CONTENT_AUTHORITY + "/" + PATH_LOCATION;

        public static final String TABLE_NAME = "location";

        public static final String COLUMN_LOCATION_SETTING = "location_setting";

        public static final String COLUMN_COORD_LAT = "coord_lat";

        public static final String COLUMN_COORD_LONG = "coord_long";

        public static final String COLUMN_LOC_NAME = "location_name";

        public static Uri buildLocationUri(long id){
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }
    }
}
