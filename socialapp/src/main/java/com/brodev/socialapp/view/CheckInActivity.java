package com.brodev.socialapp.view;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.MenuItem;
import com.brodev.socialapp.android.GooglePlaces;
import com.brodev.socialapp.android.PhraseManager;
import com.brodev.socialapp.android.manager.AlertManager;
import com.brodev.socialapp.entity.CheckInPlace;
import com.brodev.socialapp.entity.Place;
import com.brodev.socialapp.entity.PlacesList;
import com.brodev.socialapp.entity.User;
import com.brodev.socialapp.handler.GPSTracker;
import com.brodev.socialapp.http.NetworkUntil;
import com.mypinkpal.app.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.LocationSource;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;

public class CheckInActivity extends SherlockFragmentActivity implements LocationListener, LocationSource {

    private User user;
    private PhraseManager phraseManager;
    private GooglePlaces googlePlaces;
    private PlacesList nearPlaces;
    private GPSTracker gpsTracker;
    private ListView actualListView;
    private AlertManager alertManager;
    private CheckInPlace checkInPlace;
    private GoogleMap googleMap;
    private LocationManager locationManager;
    private OnLocationChangedListener mListener;

    // ListItems data
    private CheckInAdapter placesListItems;

    private static final double radius = 1000;
    private boolean enabledGPS, networkIsEnabled;
    private String userId;

    private NetworkUntil networkUntil = new NetworkUntil();

    private ConnectivityManager connMgr;
    private NetworkInfo networkInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check_in);

        user = (User) getApplicationContext();
        phraseManager = new PhraseManager(getApplicationContext());
        gpsTracker = new GPSTracker(getApplicationContext());
        alertManager = new AlertManager();
        placesListItems = new CheckInAdapter(getApplicationContext());
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        enabledGPS = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        networkIsEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        userId = null;

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(phraseManager.getPhrase(getApplicationContext(), "accountapi.check_in"));
        Bundle extras = getIntent().getExtras();

        try {
            connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            networkInfo = connMgr.getActiveNetworkInfo();

            if (networkInfo == null || !networkInfo.isConnected())
            {
                alertManager.showAlertDialog(CheckInActivity.this, phraseManager.getPhrase(getApplicationContext(), "accountapi.no_internet_title"),
                        phraseManager.getPhrase(getApplicationContext(), "accountapi.no_internet_content"), false);
                return;
            }
            // Getting list view
            actualListView = (ListView) findViewById(R.id.checkin_place_fragment_list);

            //load places
            new LoadPlaces().execute();

            if (extras != null) {
                if (getIntent().hasExtra("user_id")) {
                    userId = extras.getString("user_id");
                }
            }

            if (locationManager != null)
            {
                //auto turn on gps
                turnGPSOn();

                if (enabledGPS)
                {
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 50000L, 0, this);
                }
                else if (networkIsEnabled)
                {
                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 50000L, 0, this);
                }
                else
                {
                    //Show an error dialog that GPS is disabled...
                    Toast.makeText(this, "GPS signal not found", Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivity(intent);
                }
            }
            setUpMapIfNeeded();

            //action click place list
            actualListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    checkInPlace = (CheckInPlace) actualListView.getAdapter().getItem(i);

                    if (checkInPlace != null)
                    {
                        Intent intent = new Intent(CheckInActivity.this, ImageUpload.class);
                        if (userId != null)
                        {
                            intent.putExtra("user_id", userId);
                        }

                        intent.putExtra("location_lat", String.valueOf(checkInPlace.getLatLocation()));
                        intent.putExtra("location_lng", String.valueOf(checkInPlace.getLngLocation()));
                        intent.putExtra("location_name", checkInPlace.getName());

                        startActivity(intent);
                        finish();
                    }
                }
            });
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Turn on gps
     */
    private void turnGPSOn(){
        String provider = Settings.Secure.getString(getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);

        if(!provider.contains("gps")){ //if gps is disabled
            final Intent poke = new Intent();
            poke.setClassName("com.android.settings", "com.android.settings.widget.SettingsAppWidgetProvider");
            poke.addCategory(Intent.CATEGORY_ALTERNATIVE);
            poke.setData(Uri.parse("3"));
            sendBroadcast(poke);
        }
    }

    @Override
    public void onPause()
    {
        if(locationManager != null)
        {
            locationManager.removeUpdates(this);
        }

        super.onPause();
    }

    @Override
    public void onResume()
    {
        super.onResume();

        setUpMapIfNeeded();

        if(locationManager != null)
        {
            googleMap.setMyLocationEnabled(true);
        }
    }

    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (googleMap == null)
        {
            // Try to obtain the map from the SupportMapFragment.
            googleMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapView)).getMap();
            // Check if we were successful in obtaining the map.

            if (googleMap != null)
            {
                setUpMap();
            }

            //This is how you register the LocationSource
            googleMap.setLocationSource(this);
        }
    }

    private void setUpMap()
    {
        googleMap.setMyLocationEnabled(true);
    }
    @Override
    public void activate(OnLocationChangedListener listener)
    {
        mListener = listener;
    }

    @Override
    public void deactivate()
    {
        mListener = null;
    }

    @Override
    public void onLocationChanged(Location location)
    {
        if( mListener != null )
        {
            mListener.onLocationChanged( location );

            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 14.0f));
        }
    }

    @Override
    public void onProviderDisabled(String provider) {
    }

    @Override
    public void onProviderEnabled(String provider) {
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Class load places
     */
    public class LoadPlaces extends AsyncTask<String, String, String>
    {
        @Override
        protected String doInBackground(String... strings) {
            googlePlaces = new GooglePlaces();

            try {
                nearPlaces = googlePlaces.search(gpsTracker.getLatitude(), gpsTracker.getLongitude(), radius, null, user.getGoogleKey());
            } catch (Exception ex) {
                ex.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            // Get json response status
            String status = nearPlaces.status;

            // Check for all possible status
            if (status.equals("OK"))
            {
                if (nearPlaces.results != null)
                {
                    // loop through each place
                    for (Place p : nearPlaces.results) {
                        checkInPlace = new CheckInPlace();

                        // Place reference won't display in listview - it will be hidden
                        // Place reference is used to get "place full details"
                        checkInPlace.setReference(p.reference);

                        // Place name
                        checkInPlace.setName(p.name);
                        checkInPlace.setIcon(p.icon);
                        checkInPlace.setLatLocation(p.geometry.location.lat);
                        checkInPlace.setLngLocation(p.geometry.location.lng);
                        checkInPlace.setVicinity(p.vicinity);

                        // adding HashMap to ArrayList
                        placesListItems.add(checkInPlace);
                    }

                    // Adding data into list view
                    actualListView.setAdapter(placesListItems);
                }
            }
            else if(status.equals("ZERO_RESULTS")){
                // Zero results found
                alertManager.showAlertDialog(CheckInActivity.this, "Near Places", "Sorry no places found. Try to change the types of places", false);
            }
            else if(status.equals("UNKNOWN_ERROR"))
            {
                alertManager.showAlertDialog(CheckInActivity.this, "Places Error", "Sorry unknown error occured.", false);
            }
            else if(status.equals("OVER_QUERY_LIMIT"))
            {
                alertManager.showAlertDialog(CheckInActivity.this, "Places Error", "Sorry query limit to google places is reached", false);
            }
            else if(status.equals("REQUEST_DENIED"))
            {
                alertManager.showAlertDialog(CheckInActivity.this, "Places Error", "Sorry error occured. Request is denied", false);
            }
            else if(status.equals("INVALID_REQUEST"))
            {
                alertManager.showAlertDialog(CheckInActivity.this, "Places Error", "Sorry error occured. Invalid Request", false);
            }
            else
            {
                alertManager.showAlertDialog(CheckInActivity.this, "Places Error", "Sorry error occured.", false);
            }
        }
    }

    /**
     * Create checkin browse adapter
     */
    public class CheckInAdapter extends ArrayAdapter<CheckInPlace>
    {
        public CheckInAdapter(Context context) {
            super(context, 0);
        }

        public View getView(int position, View convertView, ViewGroup parent)
        {
            View view = convertView;
            CheckInPlace item = getItem(position);
            CheckInViewHolder holder = null;

            if (view == null)
            {
                int layout = R.layout.checkin_list_item;

                view = LayoutInflater.from(getContext()).inflate(layout, null);
                //call element from xml
                ImageView icon = (ImageView) view.findViewById(R.id.check_in_icon);
                TextView reference = (TextView) view.findViewById(R.id.check_in_reference);
                TextView name = (TextView) view.findViewById(R.id.check_in_name);
                TextView vicinity = (TextView) view.findViewById(R.id.check_in_vicinity);

                view.setTag(new CheckInViewHolder(icon, reference, name, vicinity));
            }

            if (holder == null && view != null)
            {
                Object tag = view.getTag();
                if (tag instanceof CheckInViewHolder) {
                    holder = (CheckInViewHolder) tag;
                }
            }

            if (item != null && holder != null)
            {
                //set image friend;
                if (holder.imageHolder != null)
                {
                    if (!"".equals(item.getIcon()))
                    {
                        networkUntil.drawImageUrl(holder.imageHolder, item.getIcon(), R.drawable.loading);
                    }
                }

                if (holder.name != null && item.getName() != null)
                {
                    holder.name.setText(Html.fromHtml(item.getName()).toString());
                }

                if (holder.vicinity != null && item.getVicinity() != null)
                {
                    holder.vicinity.setText(Html.fromHtml(item.getVicinity()).toString());
                }
            }

            return view;
        }
    }

    /**
     * Class check in view holder
     */
    public class CheckInViewHolder
    {
        public final ImageView imageHolder;
        public final TextView reference;
        public final TextView name;
        public final TextView vicinity;

        public CheckInViewHolder(ImageView imageHolder, TextView reference, TextView name, TextView vicinity) {
            this.imageHolder = imageHolder;
            this.reference = reference;
            this.name = name;
            this.vicinity = vicinity;
        }
    }
}
