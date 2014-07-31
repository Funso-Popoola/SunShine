package com.hoh.sunshine.sunshine;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * Created by funso on 7/26/14.
 */
public class ForecastFragment extends Fragment{

    public ForecastFragment() {

    }

    ForecastTask task;
    String postCode = "94043";
    ArrayAdapter<String> adapter;
    ListView lstView;
    SharedPreferences sp;
    @Override
    public void setHasOptionsMenu(boolean hasMenu) {
        super.setHasOptionsMenu(hasMenu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if(id == R.id.action_refresh){
            updateForecast();
        }
        return super.onOptionsItemSelected(item);
    }

    private void updateForecast(){
        ForecastTask newTask = new ForecastTask();
        newTask.execute(postCode);
    }

    @Override
    public void onStart() {
        super.onStart();
        updateForecast();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        /*String [] forecast = {"Today - Sunny - 88/13",
                "Tomorrow - Foggy - 70/40",
                "Weds - Cloudy - 72/63",
                "Thurs - Asteroids - 75/65",
                "Fri - Heavy Rain - 65/56",
                "Sat - HELP TRAPPED IN WEATHER STATION - 60/51",
                "Sun - Sunny - 80/68"};*/
        String [] forecast = {};

        List<String> weekForecast = new ArrayList<String>(Arrays.asList(forecast));

        //initializing the adapter
        adapter = new ArrayAdapter<String>(getActivity(),R.layout.list_item_forecast, R.id.list_item_forecast_textView, weekForecast);

        //finding the ListView

        lstView = (ListView)rootView.findViewById(R.id.listView_forecast);

        //setting ArrayAdapter on the ListView
        lstView.setAdapter(adapter);

        sp = PreferenceManager.getDefaultSharedPreferences(getActivity());

        postCode = sp.getString(getString(R.string.pref_location_key), getString(R.string.pref_location_default_value));

        task = new ForecastTask();

        task.execute(postCode);

        lstView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String message;
                message = adapter.getItem(i);
                Toast forecastToast = Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT);
                forecastToast.show();
                Intent intent = new Intent(getActivity(), DetailActivity.class);
                //intent.setAction("android.intent.action.DETAIL");
                intent.putExtra(Intent.EXTRA_TEXT, message);
                startActivity(intent);
            }
        });

        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.forecast_menu, menu);
    }

    public void receiveFromAsyncTask(String jsonStr){
        String [] weatherStr = new String[7];
        try{
            if(jsonStr != null){
                weatherStr = getWeatherStringFromJson(jsonStr, 7);
            }


            //update the adapter
            //Log.v("WEATHER_STRING", weatherStr[0] + weatherStr[6]);

            adapter.clear();
            for(int j = 0; j < weatherStr.length; j++){
                adapter.add(weatherStr[j]);
            }

        }
        catch (JSONException e){
            Log.e("JSONERROR", "The parsing is unsuccessful", e);
        }
    }

    private String getReadableDateTimeString(long dt){
        //convert the dt to human-readable format
        //convert dt first to milliseconds from seconds
        Date readable = new Date(dt * 1000);
        SimpleDateFormat format = new SimpleDateFormat("E, MMM d");
        format.format(readable);
        return readable.toString();
    }

    private String formatHighLow(double high, double low){
        //format the
        long roundedHigh = Math.round(high);
        long roundedLow = Math.round(low);
        return roundedHigh + "/" + roundedLow;
    }

    private String[] getWeatherStringFromJson(String weatherJsonStr, int numDays) throws JSONException{
        //create constants for key in open weather map JSON
        final String OWM_FORECAST_LIST = "list";
        final String OWM_DATE_TIME = "dt";
        final String OWM_TEMP = "temp";
        final String OWM_TEMP_MIN = "min";
        final String OWM_TEMP_MAX = "max";
        final String OWM_WEATHER = "weather";
        final String OWM_WEATHER_MAIN = "main";

        //create the Json instance of the weatherJsonStr
        JSONObject weatherJson = new JSONObject(weatherJsonStr);

        //get the JSONArray list
        JSONArray weekJSONArray = weatherJson.getJSONArray(OWM_FORECAST_LIST);

        JSONObject dayForecast;
        long dateTime;
        JSONObject tempJSONObj;
        double minTemp;
        double maxTemp;
        JSONArray weatherJSONArray;
        JSONObject weatherObj;
        String description;

        String units = sp.getString(getString(R.string.pref_units_key), getString(R.string.pref_units_default_value));


        String [] weatherString = new String[numDays];

        for(int i = 0; i < weekJSONArray.length(); i++){
            dayForecast = weekJSONArray.getJSONObject(i);
            dateTime = dayForecast.getLong(OWM_DATE_TIME);
            tempJSONObj = dayForecast.getJSONObject(OWM_TEMP);
            minTemp = tempJSONObj.getDouble(OWM_TEMP_MIN);
            maxTemp = tempJSONObj.getDouble(OWM_TEMP_MAX);
            if("imperial".equalsIgnoreCase(units)){
                minTemp = ((9.0 / 5.0) * minTemp) + 32;
                maxTemp = ((9.0 / 5.0) * maxTemp) + 32;
            }
            weatherJSONArray = dayForecast.getJSONArray(OWM_WEATHER);
            weatherObj = weatherJSONArray.getJSONObject(0);
            description = weatherObj.getString(OWM_WEATHER_MAIN);

            weatherString[i] = getReadableDateTimeString(dateTime) + "-" + description + "-" + formatHighLow(maxTemp, minTemp);
        }



        return weatherString;
    }

    private class ForecastTask extends AsyncTask<String, Void, String>{

        private static final String LOG_TAG = "ForecastFragmentLog";
        private final int numOfDays = 7;

        @Override
        protected String doInBackground(String ...postCode) {

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


                //Uri builtUrl = builder.build()
                //Log.v("URL", "BUILT URI" + builder.toString());



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

                String line;
                while ((line = myBufReader.readLine()) != null){
                    buffer.append(line + "\n");
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
            return forecastJson;
        }

        @Override
        protected void onPostExecute(String s) {
            //super.onPostExecute(s);
            if(s != null){
                receiveFromAsyncTask(s);
            }

            //Log.v("JsonString", s);

        }
    }
}

