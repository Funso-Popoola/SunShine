package com.hoh.sunshine.sunshine;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.v4.view.ActionProvider;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBarActivity;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ShareActionProvider;
import android.widget.TextView;

import org.w3c.dom.Text;

public class DetailActivity extends ActionBarActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        setContentView(R.layout.activity_detail);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new DetailFragment())
                    .commit();

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
            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
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
    public static class DetailFragment extends Fragment {


        private final String FORECAST_SHARE_HASHTAG = "#SunShine";
        private final String LOG_TAG = "Detail Fragment";
        private String shareStr;

        TextView textView;
        String detailStr;

        public DetailFragment() {
            setHasOptionsMenu(true);
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_detail, container, false);

            Intent intent = getActivity().getIntent();

            textView = (TextView)rootView.findViewById(R.id.detailTextView);
            detailStr = intent.getStringExtra(Intent.EXTRA_TEXT);
            textView.setText(detailStr);

            //textView = (TextView)rootView.findViewById(R.id.detailTextView);
            return rootView;
        }

        @Override
        public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
            inflater.inflate(R.menu.share_menu, menu);

            MenuItem menuItem = menu.findItem(R.id.action_share);


            android.support.v7.widget.ShareActionProvider myShareActionProvider = (android.support.v7.widget.ShareActionProvider)MenuItemCompat.getActionProvider(menuItem);

            if(myShareActionProvider != null){
                myShareActionProvider.setShareIntent(createShareForecastIntent());
            }
            else{
                Log.v(LOG_TAG, "Share Action Provider not found");
            }
        }

        private Intent createShareForecastIntent(){
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_TEXT, detailStr + FORECAST_SHARE_HASHTAG);

            return shareIntent;
        }


    }
}
