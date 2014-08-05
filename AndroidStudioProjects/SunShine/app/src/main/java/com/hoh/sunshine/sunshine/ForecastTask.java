package com.hoh.sunshine.sunshine;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import com.hoh.sunshine.sunshine.data.WeatherContract;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by funso on 8/2/14.
 */
public class ForecastTask extends AsyncTask<String, Void, Void> {

    private static final String LOG_TAG = "ForecastFragmentLog";
    private final int numOfDays = 7;
    private Context context;



    public ForecastTask(Context context){
        this.context = context;
    }

    private long addLocation(String locationSetting, String cityName, double lat, double lon){
        final ContentResolver resolver = context.getContentResolver();
        long retId = 0;
        final Uri queryUri = WeatherContract.LocationEntry.CONTENT_URI;

        String selection = WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING + " = ? AND "
                + WeatherContract.LocationEntry.COLUMN_LOC_NAME + " = ?";
        String [] selectionArgs = {locationSetting, cityName};
        Cursor cursor = resolver.query(queryUri, null, selection, selectionArgs, null);
        if(!cursor.moveToFirst()){
            final ContentValues value = new ContentValues();
            value.put(WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING, locationSetting);
            value.put(WeatherContract.LocationEntry.COLUMN_LOC_NAME, cityName);
            value.put(WeatherContract.LocationEntry.COLUMN_COORD_LAT, lat);
            value.put(WeatherContract.LocationEntry.COLUMN_COORD_LONG, lon);

            Uri newRowUri = resolver.insert(queryUri, value);
            retId = ContentUris.parseId(newRowUri);
        }
        else {
            retId = cursor.getLong(cursor.getColumnIndex(WeatherContract.LocationEntry._ID));
        }
        return retId;
    }

    @Override
    protected Void doInBackground(String ...postCode) {

        HttpURLConnection urlConnection = null;
        BufferedReader myBufReader = null;

        String forecastJson = null;

        String code = postCode[0];
        String mode = "json";
        String units = "metric";
        String cnt = Integer.toString(numOfDays);

        try {
            //Constructing the url for the OpenWeatherMap query

            final String BASE_PATH = "api.openweathermap.org/data/2.5/forecast/daily";
            final String QUERY_POSTCODE = "q";
            final String QUERY_MODE = "mode";
            final String QUERY_UNITS = "units";
            final String QUERY_CNT = "cnt";

            Uri.Builder builder = new Uri.Builder();
            builder.path(BASE_PATH);
            builder.appendQueryParameter(QUERY_POSTCODE, code)
                    .appendQueryParameter(QUERY_MODE, mode)
                    .appendQueryParameter(QUERY_UNITS, units)
                    .appendQueryParameter(QUERY_CNT, cnt)
                    .build();


            //URL url = new URL("http://api.openweathermap.org/data/2.5/forecast/daily?q=94043&mode=json&units=metric&cnt=7");

            URL url = new URL("http://" + builder.toString());
            //create the request and open the connection
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            //Read the input stream into a string
            InputStream inputStream = urlConnection.getInputStream();
            StringBuffer buffer = new StringBuffer();
            if(inputStream == null){
                //do nothing
                forecastJson = null;
            }
            myBufReader = new BufferedReader(new InputStreamReader(inputStream));

            String line = myBufReader.readLine();
            while (line  != null){
                buffer.append(line + "\n");

                line = myBufReader.readLine();
            }

            if(buffer.length() == 0){
                forecastJson = null;
            }

            forecastJson = buffer.toString();

        }
        catch (IOException e){
            Log.v(LOG_TAG, "IOException", e);

            forecastJson = null;
        }

        finally {
            if(urlConnection == null){
                urlConnection.disconnect();
            }

            if(myBufReader != null){
                try {
                    myBufReader.close();
                }
                catch (IOException e){
                    Log.e(LOG_TAG,"Unable to close reader", e);
                }
            }
        }
        if(forecastJson == null){
            return null;
        }

        final String OWM_CITY = "city";
        final String  OWM_CITY_NAME = "name";
        final String OWM_CITY_COORD = "coord";
        final String OWM_CITY_COORD_LAT = "lat";
        final String OWM_CITY_COORD_LON = "lon";

        final String OWM_FORECAST_LIST = "list";
        final String OWM_DATE_TIME = "dt";
        final String OWM_TEMP = "temp";
        final String OWM_TEMP_MIN = "min";
        final String OWM_TEMP_MAX = "max";
        final String OWM_PRESSURE = "pressure";
        final String OWM_HUMIDITY = "humidity";
        final String OWM_WEATHER = "weather";
        final String OWM_WEATHER_ID = "id";
        final String OWM_WEATHER_MAIN = "main";

        final String OWM_SPEED = "speed";
        final String OWM_DEGREE = "deg";

        try {
            //create the Json instance of the weatherJsonStr
            JSONObject weatherJson = new JSONObject(forecastJson);

            JSONObject cityJSONObject = weatherJson.getJSONObject(OWM_CITY);
            String cityName = cityJSONObject.getString(OWM_CITY_NAME);
            JSONObject coordJSONObject = cityJSONObject.getJSONObject(OWM_CITY_COORD);
            double cityLat = coordJSONObject.getDouble(OWM_CITY_COORD_LAT);
            double cityLon = coordJSONObject.getDouble(OWM_CITY_COORD_LON);

            String locationSetting = code;

            long locRowId = addLocation(locationSetting, cityName, cityLat, cityLon);

            //get the JSONArray list
            JSONArray weekJSONArray = weatherJson.getJSONArray(OWM_FORECAST_LIST);

            JSONObject dayForecast;
            long dateTime;
            JSONObject tempJSONObj;
            double minTemp;
            double maxTemp;
            double windSpeed;
            double pressure;
            double humidity;
            double degree;
            long weather_id;

            JSONArray weatherJSONArray;
            JSONObject weatherObj;


            String description;

            String prefUnits = Utility.getPreferredUnits(context);

            int daysInJson = weekJSONArray.length();

            if(daysInJson != numOfDays){
                Log.w("INCOMPLETE OR EXCESSIVE DATA", "The data received from cloud is not exact");
            }

            String[] weatherString = new String[numOfDays];

            ContentValues [] weatherValues = new ContentValues[numOfDays];


            for (int i = 0; i < daysInJson; i++) {
                dayForecast = weekJSONArray.getJSONObject(i);
                dateTime = dayForecast.getLong(OWM_DATE_TIME);
                pressure = dayForecast.getDouble(OWM_PRESSURE);
                humidity = dayForecast.getDouble(OWM_HUMIDITY);
                windSpeed = dayForecast.getDouble(OWM_SPEED);
                degree = dayForecast.getDouble(OWM_DEGREE);
                tempJSONObj = dayForecast.getJSONObject(OWM_TEMP);
                minTemp = tempJSONObj.getDouble(OWM_TEMP_MIN);
                maxTemp = tempJSONObj.getDouble(OWM_TEMP_MAX);
                if ("imperial".equalsIgnoreCase(prefUnits)) {
                    minTemp = ((9.0 / 5.0) * minTemp) + 32;
                    maxTemp = ((9.0 / 5.0) * maxTemp) + 32;
                }
                weatherJSONArray = dayForecast.getJSONArray(OWM_WEATHER);
                weatherObj = weatherJSONArray.getJSONObject(0);
                weather_id = weatherObj.getLong(OWM_WEATHER_ID);
                description = weatherObj.getString(OWM_WEATHER_MAIN);

                //weatherString[i] = getReadableDateTimeString(dateTime) + "-" + description + "-" + formatHighLow(maxTemp, minTemp);

                weatherValues[i] = new ContentValues();
                weatherValues[i].put(WeatherContract.WeatherEntry.COLUMN_LOC_KEY, ((Long)locRowId).toString());
                weatherValues[i].put(WeatherContract.WeatherEntry.COLUMN_PRESSURE, pressure);
                weatherValues[i].put(WeatherContract.WeatherEntry.COLUMN_HUMIDITY, humidity);
                weatherValues[i].put(WeatherContract.WeatherEntry.COLUMN_MAX_TEMP, maxTemp);
                weatherValues[i].put(WeatherContract.WeatherEntry.COLUMN_MIN_TEMP, minTemp);
                weatherValues[i].put(WeatherContract.WeatherEntry.COLUMN_DEGREES, degree);
                weatherValues[i].put(WeatherContract.WeatherEntry.COLUMN_DATETEXT,
                        WeatherContract.WeatherEntry.getDbDateString(new Date(dateTime * 1000)));
                weatherValues[i].put(WeatherContract.WeatherEntry.COLUMN_WEATHER_ID, ((Long)weather_id).toString());
                weatherValues[i].put(WeatherContract.WeatherEntry.COLUMN_SHORT_DESC, description);
                weatherValues[i].put(WeatherContract.WeatherEntry.COLUMN_WIND_SPEED, windSpeed);

            }
            Uri uri = WeatherContract.WeatherEntry.CONTENT_URI;
            int numAffected = context.getContentResolver().bulkInsert(uri, weatherValues);
        }
        catch (JSONException e){
            Log.e("", "Could not parse JSON ", e);
        }


        return null;
    }

    private String getReadableDateTimeString(long dt){
        //convert the dt to human-readable format
        //convert dt first to milliseconds from seconds
        Date readable = new Date(dt * 1000);
        SimpleDateFormat format = new SimpleDateFormat("E, MMM d");
        format.format(readable);
        return readable.toString();
    }



    /*
    @Override
    protected void onPostExecute(String s) {
        //super.onPostExecute(s);
        if(s != null){
            ForecastFragment frag = new ForecastFragment();
            frag.receiveFromAsyncTask(s);
        }

        //Log.v("JsonString", s);

    }
    */
}
