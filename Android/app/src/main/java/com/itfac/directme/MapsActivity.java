package com.itfac.directme;

import android.app.Dialog;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    int i = 0;
    double pastLatitude;
    double pastLongitude;
    double retrieveRequestTime;
    int gpsSetTimeInterval = 3000; // milliseconds
    double notiRquestConstantInTimeBase = 5.0 ; // GPS refreshing time * request times is equals to time, it can be change please recount and change this constant (example : 4 * 2000ms = 8000 ms)
    double latitude;
    double longitude;
    int cameraSetTime = 20;   // camera reset every 20000 = 20 seconds
    String range = "0.04";
    double distanceDifferenceToRequest = 0.0014;  //Distance variation value to call GET request to DataBase

    GoogleMap mGoogleMap; // check whether public works(trick)
    GoogleApiClient mGoogleApiClient;

    private static final String TAG = "milanmessage";

    private String TAG2 = MapsActivity.class.getSimpleName();

    public ArrayList<HashMap<String, String>>  locationList = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (googleServicesAvailable()) {
            Toast.makeText(this, "googleService ok", Toast.LENGTH_LONG).show();
            setContentView(R.layout.activity_maps); // Obtain the SupportMapFragment and get notified when the map is ready to be used.
            initMap();
        } else {
            Toast.makeText(this, "No google service", Toast.LENGTH_LONG).show(); // No Google Maps Layout
        }

    }


    private void initMap() {
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        Log.i(TAG, "onCreate Map");
    }

    public boolean googleServicesAvailable() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int isAvailable = apiAvailability.isGooglePlayServicesAvailable(this);
        if (isAvailable == ConnectionResult.SUCCESS) {
            return true;
        } else if (apiAvailability.isUserResolvableError(isAvailable)) {
            Dialog dialog = apiAvailability.getErrorDialog(this, isAvailable, 0);
            dialog.show();
        } else {
            Toast.makeText(this, "Can't connect to play service", Toast.LENGTH_LONG).show();
        }
        return false;
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        mLocationRequest = LocationRequest.create();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(gpsSetTimeInterval);
        Log.i(TAG, "gps weda");

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
    }

    @Override
    public void onConnectionSuspended(int i) { }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) { }

    @Override
    public void onLocationChanged(Location location) {
        if (location == null) {
            Toast.makeText(this, "Cant get current location", Toast.LENGTH_LONG).show();
        } else {
            double currentLatitude = location.getLatitude();
            double currentLongitude = location.getLongitude();
            LatLng ll = new LatLng(currentLatitude, currentLongitude);
            CameraUpdate update = CameraUpdateFactory.newLatLngZoom(ll, 16);
            if (i==0){                                 // at the start move the camera
                mGoogleMap.animateCamera(update);
                i++;
            }else if(i>=cameraSetTime){                            // after start, move camera with 15second delay
                mGoogleMap.animateCamera(update);
                i = 1;
            }else {
                i += Double.parseDouble(String.valueOf(gpsSetTimeInterval / 1000));
            }


                if (pastLatitude == 0 && pastLongitude == 0) {   // data retrieve request decide
                    pastLatitude = currentLatitude;
                    pastLongitude = currentLongitude;
                    latitude = currentLatitude;
                    longitude = currentLongitude;
                    new SendPostRequest().execute();
                    try {
                        Thread.sleep(2000);
                        setMarkers(mGoogleMap);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                } else if (distanceDifferenceToRequest <= latlonDifferent(pastLatitude, pastLongitude, currentLatitude, currentLongitude) || notiRquestConstantInTimeBase <= retrieveRequestTime) {  // based on location change or based on time change
                    latitude = currentLatitude;
                    longitude = currentLongitude;

                    new SendPostRequest().execute();
                    Toast.makeText(this, "Data calling", Toast.LENGTH_LONG).show();
                    try {
                        Thread.sleep(2000);
                        setMarkers(mGoogleMap);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    pastLatitude = currentLatitude;
                    pastLongitude = currentLongitude;
                    retrieveRequestTime = 0.0;
                } else {

                    retrieveRequestTime += (gpsSetTimeInterval / 1000);
                }

            }
    }

    private double latlonDifferent(double pLa, double pLo, double cLa, double cLo){
        double diffLat;
        double diffLon;
        diffLat=Math.abs(cLa-pLa);
        diffLon=Math.abs(cLo-pLo);
        return Math.sqrt((diffLat*diffLat)+(diffLon*diffLon));
    }

    private class SendPostRequest extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Log.i(TAG, "Json Data is downloading");
        }

        protected Void doInBackground(Void... arg0) {
            HttpHandler sh = new HttpHandler();  // Making a request to url and getting response
            Uri.Builder builder = new Uri.Builder();
            builder .scheme("http")
                    .authority("ewizardz.projects.mrt.ac.lk") //  .encodedAuthority("http://192.168.123.1:3000"); for ip hosting server
                    .appendPath("locations")
                    .appendQueryParameter("lat", String.valueOf(latitude))
                    .appendQueryParameter("lon", String.valueOf(longitude))
                    .appendQueryParameter("range", range);
            String url = builder.build().toString();   //    example : String url = "http://192.168.123.1:3000/locations?lat=6.7935&lon=79.8999&range=0.6";
            String jsonStr = sh.makeServiceCall(url);
            Log.e(TAG2, "Response from url: " + jsonStr);
            if (jsonStr != null) {
                locationList.clear();
                try {

                    JSONArray jsonArray;
                    jsonArray = new JSONArray(jsonStr); // Getting JSON Array node

                    for (int i = 0; i < jsonArray.length(); i++) { // looping through All Contacts
                        JSONObject c = jsonArray.getJSONObject(i);
                        String _id = c.getString("_id");
                        String userMode = c.getString("userMode");
                        String damageType = c.getString("damageType");
                        String damageName = c.getString("damageName");
                        String damageTypeNumber = c.getString("damageTypeNumber");
                        String confirmStatus = c.getString("confirmStatus");
                        String repairing = c.getString("repairing");

                        // location sub object is JSON Object
                        JSONObject location = c.getJSONObject("location");
                        String locationName = location.getString("locationName");
                        String latitude = location.getString("latitude");
                        String longitude = location.getString("longitude");

                        //flag sub object is JSON Object
                        JSONObject flag = c.getJSONObject("flag");
                        String  foundItCount = flag.getString("foundItCount");
                        String  notFoundItCount = flag.getString("notFoundItCount");
                        String  solvedConfirmCount = flag.getString("solvedConfirmCount");
                        String  repairingCount = flag.getString("repairingCount");

                        //time sub object is JSON Object
                        JSONObject timeStamp = c.getJSONObject("timeStamp");
                        String date = timeStamp.getString("date");

                        //user sub object
                        JSONObject userContribution = c.getJSONObject("userContribution");
                        String notificationGenerateUserId = userContribution.getString("notificationGenerateUserId");

                        // tmp hash map for single contact
                        HashMap<String, String> locationn = new HashMap<>();

                        // adding each child node to HashMap key => value
                        locationn.put("markerId", _id);
                        locationn.put("markerUserMode", userMode);
                        locationn.put("markerDamageName", damageName);
                        locationn.put("markerDamageType", damageType);
                        locationn.put("markerDamageTypeNumber", damageTypeNumber);
                        locationn.put("markerConfirmStatus", confirmStatus);
                        locationn.put("markerRepairing", repairing);
                        locationn.put("markerLocationName", locationName);
                        locationn.put("markerLatitude", latitude);
                        locationn.put("markerLongitude", longitude);
                        locationn.put("markerFoundItCount", foundItCount);
                        locationn.put("markerNotFoundItCount", notFoundItCount);
                        locationn.put("markerSolvedConfirmCount", solvedConfirmCount);
                        locationn.put("markerRepairingCount", repairingCount);
                        locationn.put("markerDate", date);
                        locationn.put("markerNotificationGenerateUserId", notificationGenerateUserId);

                        locationList.add(locationn); // adding one notification object to  locationlist
                        Log.i(TAG, "Arraylist ok");
                    }
                } catch (final JSONException e) {
                    Log.e(TAG2, "Json parsing error: " + e.getMessage());
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(),
                                    "Json parsing error: " + e.getMessage(),
                                    Toast.LENGTH_LONG).show();
                        }
                    });

                }
            } else {
                Log.e(TAG2, "Couldn't get json from server.");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(),"Oh, Your area is well developed. We could found any damage!",Toast.LENGTH_LONG).show();
                    }
                });
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);


        }
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mGoogleMap = googleMap;
        mGoogleMap.setMyLocationEnabled(true);
        mGoogleMap.animateCamera(CameraUpdateFactory.zoomTo(3));
  //    mGoogleMap.setTrafficEnabled(true);
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        mGoogleApiClient.connect();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO:Consider Calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
        }
    }

    LocationRequest mLocationRequest;

    public void setMarkers(GoogleMap mGoogleMap){
        if(locationList.size()!=0 || locationList!=null){
            Log.i(TAG, "Array List Not empty");//Toast.makeText(MapsActivity.this, "Array List Not empty", Toast.LENGTH_LONG).show();
            Log.i(TAG, "For ekata kalin weda");
            Log.i(TAG, String.valueOf(locationList.size()));
            try{
                mGoogleMap.clear();
                for (int k=0;k<locationList.size();k++){
                    Log.i(TAG, "For for eka ethule ssdf weda");
                    String currnetMarkeruserMode = locationList.get(k).get("markerUserMode");
                    double currentMarkerLatitude = Double.parseDouble(locationList.get(k).get("markerLatitude"));
                    double currentMarkerLongitude = Double.parseDouble(locationList.get(k).get("markerLongitude"));
                    String currentMarkerDamageType = locationList.get(k).get("markerDamageName");
                    int currentMarkerDamageTypeNumber = Integer.parseInt(locationList.get(k).get("markerDamageTypeNumber"));
                    String currentMarkerLocationName = locationList.get(k).get("LocationName");

                    Log.i(TAG, "For eka weda");
                    switch (currentMarkerDamageTypeNumber) {
                        case 1:  mGoogleMap.addMarker(new MarkerOptions().position(new LatLng(currentMarkerLatitude,currentMarkerLongitude)).title(currentMarkerDamageType).icon(BitmapDescriptorFactory.fromResource(R.mipmap.map_marker_pit_small)));
                            break; // PitSmall
                        case 2:  mGoogleMap.addMarker(new MarkerOptions().position(new LatLng(currentMarkerLatitude,currentMarkerLongitude)).title(currentMarkerDamageType+" at latitude :" +currentMarkerLatitude+ "and longitude :" +currentMarkerLongitude).icon(BitmapDescriptorFactory.fromResource(R.mipmap.map_marker_pit_medium)));
                            break; // PitMedium
                        case 3:  mGoogleMap.addMarker(new MarkerOptions().position(new LatLng(currentMarkerLatitude,currentMarkerLongitude)).title(currentMarkerDamageType+" at latitude :" +currentMarkerLatitude+ "and longitude :" +currentMarkerLongitude).icon(BitmapDescriptorFactory.fromResource(R.mipmap.map_marker_pit_large)));
                            break; // PitLarge
                        case 4:  mGoogleMap.addMarker(new MarkerOptions().position(new LatLng(currentMarkerLatitude,currentMarkerLongitude)).title(currentMarkerDamageType+" at latitude :" +currentMarkerLatitude+ "and longitude :" +currentMarkerLongitude).icon(BitmapDescriptorFactory.fromResource(R.mipmap.map_marker_bump)));
                            break; // Bump
                        case 5:  mGoogleMap.addMarker(new MarkerOptions().position(new LatLng(currentMarkerLatitude,currentMarkerLongitude)).title(currentMarkerDamageType+" at latitude :" +currentMarkerLatitude+ "and longitude :" +currentMarkerLongitude).icon(BitmapDescriptorFactory.fromResource(R.mipmap.map_marker_broken_road_small)));
                            break; // BrokenRoadSmall
                        case 6:  mGoogleMap.addMarker(new MarkerOptions().position(new LatLng(currentMarkerLatitude,currentMarkerLongitude)).title(currentMarkerDamageType+" at latitude :" +currentMarkerLatitude+ "and longitude :" +currentMarkerLongitude).icon(BitmapDescriptorFactory.fromResource(R.mipmap.map_marker_broken_road_medium)));
                            break; // BrokenRoadMedium
                        case 7:  mGoogleMap.addMarker(new MarkerOptions().position(new LatLng(currentMarkerLatitude,currentMarkerLongitude)).title(currentMarkerDamageType+" at latitude :" +currentMarkerLatitude+ "and longitude :" +currentMarkerLongitude).icon(BitmapDescriptorFactory.fromResource(R.mipmap.map_marker_broken_road_large)));
                            break; // BrokenRoadLarge
                        case 8:  mGoogleMap.addMarker(new MarkerOptions().position(new LatLng(currentMarkerLatitude,currentMarkerLongitude)).title(currentMarkerDamageType+" at latitude :" +currentMarkerLatitude+ "and longitude :" +currentMarkerLongitude).icon(BitmapDescriptorFactory.fromResource(R.mipmap.map_marker_accident)));
                            break; // Accident
                        case 9:  mGoogleMap.addMarker(new MarkerOptions().position(new LatLng(currentMarkerLatitude,currentMarkerLongitude)).title(currentMarkerDamageType+" at latitude :" +currentMarkerLatitude+ "and longitude :" +currentMarkerLongitude).icon(BitmapDescriptorFactory.fromResource(R.mipmap.map_marker_accident_medical)));
                            break; // AccidentMedical
                        case 10:  mGoogleMap.addMarker(new MarkerOptions().position(new LatLng(currentMarkerLatitude,currentMarkerLongitude)).title(currentMarkerDamageType+" at latitude :" +currentMarkerLatitude+ "and longitude :" +currentMarkerLongitude).icon(BitmapDescriptorFactory.fromResource(R.mipmap.map_marker_accident_fire)));
                            break; // AccidentFire
                        case 11:  mGoogleMap.addMarker(new MarkerOptions().position(new LatLng(currentMarkerLatitude,currentMarkerLongitude)).title(currentMarkerDamageType+" at latitude :" +currentMarkerLatitude+ "and longitude :" +currentMarkerLongitude).icon(BitmapDescriptorFactory.fromResource(R.mipmap.map_marker_road_block_partially)));
                            break; // RoadBlockPartially
                        case 12:  mGoogleMap.addMarker(new MarkerOptions().position(new LatLng(currentMarkerLatitude,currentMarkerLongitude)).title(currentMarkerDamageType+" at latitude :" +currentMarkerLatitude+ "and longitude :" +currentMarkerLongitude).icon(BitmapDescriptorFactory.fromResource(R.mipmap.map_marker_road_block_fully)));
                            break; // RoadBlockFully
                        case 13:  mGoogleMap.addMarker(new MarkerOptions().position(new LatLng(currentMarkerLatitude,currentMarkerLongitude)).title(currentMarkerDamageType+" at latitude :" +currentMarkerLatitude+ "and longitude :" +currentMarkerLongitude).icon(BitmapDescriptorFactory.fromResource(R.mipmap.map_marker_road_closed)));
                            break; // RoadClosed
                        default: break;
                    }
                    Log.i(TAG, "One Iteration ok");
                }
            }catch(Exception e){
                Log.i(TAG, "There is an error");
                e.printStackTrace();
            }
        }else{
            Log.i(TAG, "ArrayList is Empty or null");
        }
    }
}