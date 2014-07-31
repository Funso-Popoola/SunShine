package com.hoh.sunshine.sunshine;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class MainActivity extends ActionBarActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (savedInstanceState == null) {
            ForecastFragment frag = new ForecastFragment();
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, frag)
                    .commit();
            frag.setHasOptionsMenu(true);
        }

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            Intent settingsIntent = new Intent(this, SettingsActivity.class);
            startActivity(settingsIntent);
        }
        else if(id == R.id.action_showMap){
            Intent mapIntent = new Intent(Intent.ACTION_VIEW);

            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            String location = sp.getString(getString(R.string.pref_location_key), getString(R.string.pref_location_default_value));

            Uri mapUri = Uri.parse("geo:0,0?q=" + location);
            mapIntent.setData(mapUri);

            if(mapIntent.resolveActivity(getPackageManager()) != null){
                startActivity(mapIntent);
            }

        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */

}
