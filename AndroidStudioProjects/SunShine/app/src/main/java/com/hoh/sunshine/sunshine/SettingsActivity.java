package com.hoh.sunshine.sunshine;

import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.os.Bundle;

import com.hoh.sunshine.sunshine.data.WeatherContract;

public class SettingsActivity extends PreferenceActivity implements Preference.OnPreferenceChangeListener{

    private boolean mBindingPreference;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.pref_general);
        //setContentView(R.layout.activity_settings);

        bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_location_key)));
        bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_units_key)));


    }


    /*
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.settings, menu);
        return true;
    }
    */


    /*

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    */

    /**
     * Attaches a listener so the summary is always updated with the preference value.
     * Also fires the listener once, to initialize the summary (so it shows up before the value
     * is changed.)
     */
    private void bindPreferenceSummaryToValue(Preference preference){
        //set listener to watch for preference value changes
        mBindingPreference = true;
        preference.setOnPreferenceChangeListener(this);

        //Trigger the listener with the preference's current values
        onPreferenceChange(preference, PreferenceManager.getDefaultSharedPreferences(
                                                            preference.getContext())
                                                        .getString(preference.getKey(), ""));

        mBindingPreference = false;
    }

    /*
    @Override
    public boolean onPreferenceChange(Preference preference, Object o) {
        String stringValue = o.toString();

        if (preference instanceof ListPreference) {
            // For list pref_general, look up the correct display value in
            // the preference's 'entries' list (since they have separate labels/values).
            ListPreference listPreference = (ListPreference) preference;
            int prefIndex = listPreference.findIndexOfValue(stringValue);
            if (prefIndex >= 0) {
                preference.setSummary(listPreference.getEntries()[prefIndex]);
            }
        } else {
            // For other pref_general, set the summary to the value's simple string representation.
            preference.setSummary(stringValue);
        }
        return true;

    }
    */

    @Override
    public boolean onPreferenceChange(Preference preference, Object value) {
        String stringValue = value.toString();

        if(!mBindingPreference){
            if(preference.getKey().equals(getString(R.string.pref_location_key))){
                ForecastTask forecastTask = new ForecastTask(getApplicationContext());
                String location = value.toString();
                forecastTask.execute(location);
            }else {
                getContentResolver().notifyChange(WeatherContract.WeatherEntry.CONTENT_URI, null);
            }
        }
        return false;
    }
}
