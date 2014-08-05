package com.hoh.sunshine.sunshine;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
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
import android.widget.TextView;
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

import com.hoh.sunshine.sunshine.data.WeatherContract;
import com.hoh.sunshine.sunshine.data.WeatherContract.WeatherEntry;

/**
 * Created by funso on 7/26/14.
 */
public class ForecastFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>{

    public ForecastFragment() {

    }

    ForecastTask task;
    String postCode = "94043";
    SimpleCursorAdapter adapter;

    ListView lstView;
    SharedPreferences sp;

    private String mLocation;


    private static final int LOADER_ID = 0;

    private final String [] FORECAST_COLUMNS = {
        WeatherEntry.TABLE_NAME + "." + WeatherEntry._ID,
        WeatherEntry.COLUMN_DATETEXT,
        WeatherEntry.COLUMN_SHORT_DESC,
        WeatherEntry.COLUMN_MAX_TEMP,
        WeatherEntry.COLUMN_MIN_TEMP,
        WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING};

    //indices tied to the column
    public final int COL_WEATHER_ID = 0;
    public final int COL_DATETEXT = 1;
    public final int COL_SHORT_DESC = 2;
    public final int COL_MAX_TEMP = 3;
    public final int COL_MIN_TEMP = 4;
    public final int COL_LOCATION_SETTING = 5;
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
        ForecastTask newTask = new ForecastTask(getActivity().getApplicationContext());
        newTask.execute(postCode);
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        if(mLocation != null && !mLocation.equals(Utility.getPreferredLocation(getActivity()))){
            getLoaderManager().restartLoader(LOADER_ID, null, this);
        }
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
        //adapter = new ArrayAdapter<String>(getActivity(),R.layout.list_item_forecast, R.id.list_item_forecast_textView, weekForecast);

        adapter = new SimpleCursorAdapter(getActivity(),
                R.layout.weather_list_item,
                null,
                new String[]{WeatherEntry.COLUMN_DATETEXT,
                        WeatherEntry.COLUMN_SHORT_DESC,
                        WeatherEntry.COLUMN_MAX_TEMP,
                        WeatherEntry.COLUMN_MIN_TEMP},
                new int[]{R.id.list_item_date_textView,
                            R.id.list_item_forecast_textview,
                            R.id.list_item_high_textView,
                            R.id.list_item_low_textView}, 0);

        adapter.setViewBinder(new SimpleCursorAdapter.ViewBinder() {
            @Override
            public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
                switch (columnIndex){
                    case COL_MIN_TEMP:
                    case COL_MAX_TEMP:{
                        ((TextView) view).setText(
                                Utility.formatTemperature(cursor.getDouble(columnIndex),
                                        getActivity()));
                        return true;
                    }
                    case COL_DATETEXT:{
                        String dateText = cursor.getString(columnIndex);
                        TextView dateTextView = (TextView)view;
                        dateTextView.setText(Utility.formatDate(dateText));

                        return true;
                    }
                }
                return false;
            }
        });
        //finding the ListView

        lstView = (ListView)rootView.findViewById(R.id.listView_forecast);

        //setting ArrayAdapter on the ListView
        lstView.setAdapter(adapter);

        postCode = Utility.getPreferredLocation(getActivity());

        task = new ForecastTask(getActivity().getApplicationContext());

        task.execute(postCode);

        lstView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String message;
                SimpleCursorAdapter childAdapter = (SimpleCursorAdapter)adapterView.getAdapter();
                Cursor cursor = childAdapter.getCursor();
                String dateString = cursor.getString(cursor.getColumnIndex(WeatherEntry.COLUMN_DATETEXT));
                dateString = Utility.formatDate(dateString);
                String shortDesc = cursor.getString(cursor.getColumnIndex(WeatherEntry.COLUMN_SHORT_DESC));
                String highTemp = cursor.getString(cursor.getColumnIndex(WeatherEntry.COLUMN_MAX_TEMP));
                String lowTemp = cursor.getString(cursor.getColumnIndex(WeatherEntry.COLUMN_MIN_TEMP));
                message = dateString + "-" + shortDesc + "-" + highTemp + "/" + lowTemp;
                //message = adapter.getItem(i);
                Toast forecastToast = Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT);
                forecastToast.show();
                Intent intent = new Intent(getActivity(), DetailActivity.class);
                intent.setAction("android.intent.action.DETAIL");
                intent.putExtra(Intent.EXTRA_TEXT, message);
                startActivity(intent);
            }
        });

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(LOADER_ID, null, this);
        super.onActivityCreated(savedInstanceState);
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

            //adapter.clear();
            //for(int j = 0; j < weatherStr.length; j++){
            //    adapter.add(weatherStr[j]);
            //}

        }
        catch (JSONException e){
            Log.e("JSONERROR", "The parsing is unsuccessful", e);
        }
    }



    private String formatHighLow(double high, double low){
        //format the
        long roundedHigh = Math.round(high);
        long roundedLow = Math.round(low);
        return roundedHigh + "/" + roundedLow;
    }

    private String[] getWeatherStringFromJson(String weatherJsonStr, int numDays) throws JSONException{
        //create constants for key in open weather map JSON



        return null;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // This is called when a new Loader needs to be created.  This
        // fragment only uses one loader, so we don't care about checking the id.

        // To only show current and future dates, get the String representation for today,
        // and filter the query to return weather only for dates after or including today.
        // Only return data after today.

        String startDate = WeatherEntry.getDbDateString(new Date());

        String sortOrder = WeatherEntry.COLUMN_DATETEXT + " ASC";

        mLocation = Utility.getPreferredLocation(getActivity());
        Uri weatherForLocationUri = WeatherEntry.
                buildWeatherLocationWithStartDate(mLocation, startDate);

        // Now create and return a CursorLoader that will take care of
        // creating a Cursor for the data being displayed.
        return new CursorLoader(
                getActivity(),
                weatherForLocationUri,
                FORECAST_COLUMNS,
                null,
                null,
                sortOrder
        );

    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

        adapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        adapter.swapCursor(null);
    }
}

