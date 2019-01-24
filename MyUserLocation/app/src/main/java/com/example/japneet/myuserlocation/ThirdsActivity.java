package com.example.japneet.myuserlocation;


import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.japneet.myuserlocation.models.PlaceInfo;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.places.AutocompletePrediction;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationRequest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class ThirdsActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleApiClient.OnConnectionFailedListener
    {

        @Override
        public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

        }

    //vars
    private Boolean mLocationPermissionsGranted = false;
    private GoogleMap mMap;
    private static final float DEFAULT_ZOOM = 15f;
    private PlaceAutocompleteAdapter mPlaceAutocompleteAdapter;
    private static final LatLngBounds LAT_LNG_BOUNDS = new LatLngBounds(
            new LatLng(-40, -168), new LatLng(71, 136));
        private PlaceInfo mPlace;
        private Marker mMarker;
        private static final int PLACE_PICKER_REQUEST = 1;
        private static final CharSequence[] MAP_TYPE_ITEMS =
                {"Road Map", "Hybrid", "Satellite", "Terrain"};


    //Widgets
    private AutoCompleteTextView mSearchText;
    private ImageView mGps, mInfo, mPlacePicker , map_mode;



    static final String TAG = "MapActivity";
    private static final String FINE_LOCATION = android.Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COURSE_LOCATION = android.Manifest.permission.ACCESS_COARSE_LOCATION;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1234;

    private FusedLocationProviderClient mFusedLocationProviderClient;
    private GoogleApiClient mGoogleApiClient;






    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_thirds);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        getLocationPermission();
        mSearchText =  findViewById(R.id.input_search);
        mGps = findViewById(R.id.ic_gps);
        mInfo = findViewById(R.id.place_info);
        mPlacePicker= findViewById(R.id.place_picker);
        map_mode = findViewById(R.id.map_mode);














    }









        private void init(){
        Log.d(TAG, "init: initializing");

        mGoogleApiClient = new GoogleApiClient
                .Builder(this)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .enableAutoManage(this, this)
                .build();

        mSearchText.setOnItemClickListener(mAutocompleteClickListener);

        mPlaceAutocompleteAdapter = new PlaceAutocompleteAdapter(this, mGoogleApiClient,
                LAT_LNG_BOUNDS, null);


        mSearchText.setAdapter(mPlaceAutocompleteAdapter);

        mSearchText.setOnEditorActionListener(new TextView.OnEditorActionListener()


        {

            //this will help to handle the events occuring on search bar such as pressing of enter key will lead to search.
            // down arrow will lead to next event and so on..!

            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                if(actionId == EditorInfo.IME_ACTION_SEARCH
                        || actionId == EditorInfo.IME_ACTION_DONE
                        || keyEvent.getAction() == KeyEvent.ACTION_DOWN
                        || keyEvent.getAction() == KeyEvent.KEYCODE_ENTER){

                    //execute our method for searching
                    geoLocate();
                }

                return false;
            }
        });

        mGps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG, "onClick: clicked gps icon");
                getDeviceLocation();
            }
        });

        mInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG, "onClick: clicked place info");
                try{
                    if(mMarker.isInfoWindowShown()){
                        mMarker.hideInfoWindow();
                    }else{
                        Log.d(TAG, "onClick: place info: " + mPlace.toString());
                        //Toast.makeText(ThirdsActivity.this , "info is: " + mPlace.toString(), Toast.LENGTH_SHORT).show();
                        mMarker.showInfoWindow();
                    }
                }catch (NullPointerException e){
                    Log.e(TAG, "onClick: NullPointerException: " + e.getMessage() );
                }
            }
        });


        mPlacePicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {



                PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();

                try {
                    startActivityForResult(builder.build(ThirdsActivity.this), PLACE_PICKER_REQUEST);
                } catch (GooglePlayServicesRepairableException e) {
                    Log.e(TAG, "onClick: GooglePlayServicesRepairableException: " + e.getMessage() );
                } catch (GooglePlayServicesNotAvailableException e) {
                    Log.e(TAG, "onClick: GooglePlayServicesNotAvailableException: " + e.getMessage() );
                }
            }
        });


        map_mode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                Log.i(TAG, "onClick: Earth icon is pressed!!");

                showMapTypeSelectorDialog();



                }
        });


        hideSoftKeyboard();



    }



        protected void onActivityResult(int requestCode, int resultCode, Intent data) {
            if (requestCode == PLACE_PICKER_REQUEST) {
                if (resultCode == RESULT_OK) {
                    Place place = PlacePicker.getPlace(this, data);

                    PendingResult<PlaceBuffer> placeResult = Places.GeoDataApi
                            .getPlaceById(mGoogleApiClient, place.getId());
                    placeResult.setResultCallback(mUpdatePlaceDetailsCallback);
                }
            }
        }






//this will help us to find out the item entered in the search field by geo-locating it!!
    //for this method api key entered should have geo-location api enabled!


    private void geoLocate(){
        Log.i(TAG, "geoLocate: geolocating");

        String searchString = mSearchText.getText().toString(); // this helps to get the item entered in the search!

        Geocoder geocoder = new Geocoder(ThirdsActivity.this);
        List<Address> list = new ArrayList<>();
        try{
            list = geocoder.getFromLocationName(searchString, 1); //this help us to actually find the item and provide appropriate results.
        }catch (IOException e){
            Log.i(TAG, "geoLocate: IOException: " + e.getMessage() );
        }

        if(list.size() > 0){
            Address address = list.get(0);

            Log.i(TAG, "geoLocate: found a location: " + address.toString());
            Toast.makeText(ThirdsActivity.this, address.toString(), Toast.LENGTH_SHORT).show();
            moveCamera(new LatLng(address.getLatitude(), address.getLongitude()), DEFAULT_ZOOM,
                    address.getAddressLine(0));

        }
    }






//This will help us to check whether permission is present or we can ask for the permission from the user!
    // mlocationpermissionGranted variable is used for checking permission
    //in else case we are asking for the permission from user.


    private void getLocationPermission(){
        Log.i(TAG, "getLocationPermission: getting location permissions");
        String[] permissions = {android.Manifest.permission.ACCESS_FINE_LOCATION,
                android.Manifest.permission.ACCESS_COARSE_LOCATION};

        if(ContextCompat.checkSelfPermission(this.getApplicationContext(),
                FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            if(ContextCompat.checkSelfPermission(this.getApplicationContext(),
                    COURSE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                mLocationPermissionsGranted = true;

                //if permissions are already guranteed then also we want to initialize the init()



            }else{
                ActivityCompat.requestPermissions(ThirdsActivity.this,
                        permissions,
                        LOCATION_PERMISSION_REQUEST_CODE);
            }
        }else{
            ActivityCompat.requestPermissions(ThirdsActivity.this,
                    permissions,
                    LOCATION_PERMISSION_REQUEST_CODE);
        }
    }


    private void initMap(){
        Log.i(TAG, "initMap: initializing map");

        //

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);

        mapFragment.getMapAsync(ThirdsActivity.this);
    }



    //For getting the current location of the device we have created the function GetDeviceLocation ()

    private void getDeviceLocation(){
        Log.i(TAG, "getDeviceLocation: getting the devices current location");


        //Use the fused location provider to find the device's last-known location, then use that location to position the map.

        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        try{

            // if is used to check whether location is granted or not if it is granted then we can proceed further.
            if(mLocationPermissionsGranted){

                final Task<Location> location = mFusedLocationProviderClient.getLastLocation();
                location.addOnCompleteListener(ThirdsActivity.this, new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        //if location found successfully then we can proceed further.
                        if(task.isSuccessful()){
                            Log.i(TAG, "onComplete: found location!");
                            Toast.makeText(ThirdsActivity.this, "Location Found", Toast.LENGTH_SHORT).show();
                            Location currentLocation = (Location) task.getResult();

                            // moveCamera here is getting the longitude and latitude of current location.

                            moveCamera(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()),
                                    DEFAULT_ZOOM,
                                    "My Location" );

                        }else{
                            // location not found!
                            Log.i(TAG, "onComplete: current location is null");
                            Toast.makeText(ThirdsActivity.this, "unable to get current location", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        }catch (SecurityException e){
            Log.i(TAG, "getDeviceLocation: SecurityException: " + e.getMessage() );
        }
    }
                               


    // this method is created because we need to move the camera again and again in this app therefore we are reducing the time creating a function for this!
    private void moveCamera(LatLng latLng,float zoom , PlaceInfo placeInfo){
        Log.i(TAG, "moveCamera: moving the camera to: lat: " + latLng.latitude + ", lng: " + latLng.longitude );
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng,zoom));



        mMap.setInfoWindowAdapter(new CustomInfoWindowAdapter(ThirdsActivity.this));

        mMap.clear();

        if(placeInfo != null){
            try{
                String snippet = "Address: " + placeInfo.getAddress() + "\n" +
                        "Phone Number: " + placeInfo.getPhoneNumber() + "\n" +
                        "Website: " + placeInfo.getWebsiteUri() + "\n" +
                        "Price Rating: " + placeInfo.getRating() + "\n";

                MarkerOptions options = new MarkerOptions()
                        .position(latLng)
                        .title(placeInfo.getName())
                        .snippet(snippet);
                mMarker = mMap.addMarker(options);

            }catch (NullPointerException e){
                Log.e(TAG, "moveCamera: NullPointerException: " + e.getMessage() );
            }
        }else{
            mMap.addMarker(new MarkerOptions().position(latLng));
        }
        hideSoftKeyboard();


    }
    private void hideSoftKeyboard(){
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }



    private void moveCamera(LatLng latLng, float zoom, String title){
            Log.d(TAG, "moveCamera: moving the camera to: lat: " + latLng.latitude + ", lng: " + latLng.longitude );
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));

            if(!title.equals("My Location")){
                MarkerOptions options = new MarkerOptions()
                        .position(latLng)
                        .title(title);
                mMap.addMarker(options);
            }

            hideSoftKeyboard();
        }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.i(TAG, "onRequestPermissionsResult: called.");
        mLocationPermissionsGranted = false;

        switch(requestCode){
            case LOCATION_PERMISSION_REQUEST_CODE:{
                //kuch result hume mila hai agr mila hai toh variable grantResult ki length will be greater then '0' then we will proceed further!

                if(grantResults.length > 0){

                    //for loop is used to loop through all the results because results can be more then "1"

                    for(int i = 0; i < grantResults.length; i++){

//this "if" will check if the permission is granted is not granted then variable will return "false" otherwise "true"
                        if(grantResults[i] != PackageManager.PERMISSION_GRANTED){
                            mLocationPermissionsGranted = false;
                            Log.i(TAG, "onRequestPermissionsResult: permission failed");
                            return;
                        }
                    }

                    // this will display permission is granted and we can proceed further!!

                    Log.i(TAG, "onRequestPermissionsResult: permission granted");
                    mLocationPermissionsGranted = true;
                    //initialize our map



                    initMap();
                }
            }
        }


        }












    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        Toast.makeText(this, "Map is Ready", Toast.LENGTH_SHORT).show();
        Log.i(TAG, "onMapReady: map is ready");

        // if location is granted then we can proceed further.

        if (mLocationPermissionsGranted) {

            // get device locatoin will get the current position of the device as we have created.

            getDeviceLocation();

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return; //This is added automatically to check for explicit location of the device.
            }
            mMap.setMyLocationEnabled(true); // This will mark my location on the map!!

          mMap.getUiSettings().setMyLocationButtonEnabled(false); // This will disable the location button.
            //because we need to add the search bar so we have to add it manually.
            mMap.getUiSettings().setMapToolbarEnabled(true);
            mMap.getUiSettings().setZoomGesturesEnabled(true);

            init();

        }

    }



    /*
    GOOGLE API FOR GOOGLE PLACES!!
     */


        private AdapterView.OnItemClickListener mAutocompleteClickListener = new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                hideSoftKeyboard();

                final AutocompletePrediction item = mPlaceAutocompleteAdapter.getItem(i);
                final String placeId = item.getPlaceId();

                PendingResult<PlaceBuffer> placeResult = Places.GeoDataApi
                        .getPlaceById(mGoogleApiClient, placeId);
                placeResult.setResultCallback(mUpdatePlaceDetailsCallback);
            }
        };

        private  ResultCallback<PlaceBuffer> mUpdatePlaceDetailsCallback = new ResultCallback<PlaceBuffer>() {
            @Override
            public void onResult(@NonNull PlaceBuffer places) {
                if(!places.getStatus().isSuccess()){
                    Log.i(TAG, "onResult: Place query did not complete successfully: " + places.getStatus().toString());
                    places.release(); //this will prevent the memory leak.
                    return;
                }
                final Place place = places.get(0);

                try{

                    // try catch is used because it is possible that some of these
                    // parameters maybe null therefore we have to catch the null pointer exception.


                    mPlace = new PlaceInfo();
                    mPlace.setName(place.getName().toString());
                    Log.i(TAG, "onResult: name: " + place.getName());
                    mPlace.setAddress(place.getAddress().toString());
                    Log.i(TAG, "onResult: address: " + place.getAddress());
//                mPlace.setAttributions(place.getAttributions().toString());
//                Log.i(TAG, "onResult: attributions: " + place.getAttributions());
                    mPlace.setId(place.getId());
                    Log.i(TAG, "onResult: id:" + place.getId());
                    mPlace.setLatlng(place.getLatLng());
                    Log.i(TAG, "onResult: latlng: " + place.getLatLng());
                    mPlace.setRating(place.getRating());
                    Log.i(TAG, "onResult: rating: " + place.getRating());
                    mPlace.setPhoneNumber(place.getPhoneNumber().toString());
                    Log.i(TAG, "onResult: phone number: " + place.getPhoneNumber());
                    mPlace.setWebsiteUri(place.getWebsiteUri());
                    Log.i(TAG, "onResult: website uri: " + place.getWebsiteUri());

                    Log.i(TAG, "onResult: place: " + mPlace.toString());
                }catch (NullPointerException e){
                    Log.i(TAG, "onResult: NullPointerException: " + e.getMessage() );
                }

                moveCamera(new LatLng(place.getViewport().getCenter().latitude,
                        place.getViewport().getCenter().longitude), DEFAULT_ZOOM , mPlace);  // this will name our current location and implement the our default zoom!

                places.release();
            }
        };




        // TO ADD MENU IN THE APP FOR SWITCHING MODES OF MAPS INTO DIFFERENT LIKE SATELLITE , TERRAIN ETC.






        private void showMapTypeSelectorDialog() {
            // Prepare the dialog by setting up a Builder.
            final String fDialogTitle = "Select Map Type";
            AlertDialog.Builder builder = new AlertDialog.Builder(ThirdsActivity.this);
            builder.setTitle(fDialogTitle);

            // Find the current map type to pre-check the item representing the current state.
            int checkItem = mMap.getMapType() - 1;

            // Add an OnClickListener to the dialog, so that the selection will be handled.
            builder.setSingleChoiceItems(
                    MAP_TYPE_ITEMS,
                    checkItem,
                    new DialogInterface.OnClickListener() {

                        public void onClick(DialogInterface dialog, int item) {
                            // Locally create a finalised object.

                            // Perform an action depending on which item was selected.
                            switch (item) {
                                case 1:
                                    mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                                    break;
                                case 2:
                                    mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
                                    break;
                                case 3:
                                    mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                                    break;
                                default:
                                    mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                            }
                            dialog.dismiss();
                        }
                    }
            );

            // Build the dialog and show it.
            AlertDialog fMapTypeDialog = builder.create();
            fMapTypeDialog.setCanceledOnTouchOutside(true);
            fMapTypeDialog.show();
        }



}




