package com.hoh.sunshine.sunshine;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.hoh.sunshine.sunshine.data.WeatherContract;

import java.text.DateFormat;
import java.util.Date;

/**
 * Created by funso on 8/2/14.
 */
public class Utility {
    public static String getPreferredLocation(Context context){
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getString(context.getString(R.string.pref_location_key),
                context.getString(R.string.pref_location_default_value));
    }

    public static String getPreferredUnits(Context context){
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getString(context.getString(R.string.pref_units_key),
                context.getString(R.string.pref_units_default_value));
    }

    public static String formatTemperature(double temperature, Context context){
        double temp;
        if("imperial".equalsIgnoreCase(getPreferredUnits(context))){
            temp = (9.0 / 5.0) * temperature + 32;
        }else{
            temp = temperature;
        }
        return String.format("%.0f", temp);
    }

    static String formatDate(String dateString) {
        Date date = WeatherContract.WeatherEntry.getDateFromDb(dateString);
        return DateFormat.getDateInstance().format(date);
    }

}
