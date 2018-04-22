package com.example.hp.maskot2;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONObject;
import java.text.DateFormat;
import java.util.Date;

import cz.msebera.android.httpclient.Header;

import dmax.dialog.SpotsDialog;

public class MainActivity extends AppCompatActivity  implements
        LocationListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, OnMapReadyCallback {

    private static final String TAG = "LocationActivity";
    private static final long INTERVAL = 3000 * 10;
    private static final long FASTEST_INTERVAL = 1000 * 1;
    Button btnFusedLocation;
    LocationRequest mLocationRequest;
    GoogleApiClient mGoogleApiClient;
    Location mCurrentLocation;
    String mLastUpdateTime;
    GoogleMap myMap;

    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d(TAG, "onCreate ...............................");
        //show error dialog if GoolglePlayServices not available
        if (!isGooglePlayServicesAvailable()) {
            finish();
        }
        createLocationRequest();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        setContentView(R.layout.activity_main);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.mapView);

        mapFragment.getMapAsync(this);

        btnFusedLocation = (Button) findViewById(R.id.btnShowLocation);
        btnFusedLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {

                AlertDialog alertDialog = new AlertDialog.Builder(
                        MainActivity.this).create();

                // Setting Dialog Title
                alertDialog.setTitle("Save Location");

                // Setting Dialog Message
                alertDialog.setMessage("Do you really want to save this location?");


                // Setting OK Button
                alertDialog.setButton("Save", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        RequestParams requestParams = new RequestParams();
                        requestParams.put("latitude", mCurrentLocation.getLatitude());
                        requestParams.put("longitude", mCurrentLocation.getLongitude());
                        saveDataTask(requestParams);

                    }
                });

                // Showing Alert Message
                alertDialog.show();

            }
        });

    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d(TAG, "onStart fired ..............");
        mGoogleApiClient.connect();
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d(TAG, "onStop fired ..............");
        mGoogleApiClient.disconnect();
        Log.d(TAG, "isConnected ...............: " + mGoogleApiClient.isConnected());
    }

    private boolean isGooglePlayServicesAvailable() {
        int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (ConnectionResult.SUCCESS == status) {
            return true;
        } else {
            GooglePlayServicesUtil.getErrorDialog(status, this, 0).show();
            return false;
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.d(TAG, "onConnected - isConnected ...............: " + mGoogleApiClient.isConnected());
        startLocationUpdates();
    }

    protected void startLocationUpdates() {
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
        PendingResult<Status> pendingResult = LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, mLocationRequest, this);
        Log.d(TAG, "Location update started ..............: ");
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d(TAG, "Connection failed: " + connectionResult.toString());
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.d(TAG, "Firing onLocationChanged..............................................");
        mCurrentLocation = location;
        mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());
        updateUI();
    }

    private void updateUI() {
        Log.d(TAG, "UI update initiated .............");
        if (null != mCurrentLocation) {
            String lat = String.valueOf(mCurrentLocation.getLatitude());
            String lng = String.valueOf(mCurrentLocation.getLongitude());
            if(myMap != null) {
                myMap.clear();
            }
            myMap.addMarker(new MarkerOptions().position(new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude())).title(""));
            myMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude()), 14));


        } else {
            Log.d(TAG, "location is null ...............");
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopLocationUpdates();
    }

    protected void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(
                mGoogleApiClient, this);
        Log.d(TAG, "Location update stopped .......................");
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mGoogleApiClient.isConnected()) {
            startLocationUpdates();
            Log.d(TAG, "Location update resumed .....................");
        }
    }

    @Override
    public void onMapReady(GoogleMap map) {
        // DO WHATEVER YOU WANT WITH GOOGLEMAP
        map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        map.getUiSettings().setZoomControlsEnabled(true);

        myMap = map;

    }


    public void saveDataTask(RequestParams params) {

        final AlertDialog dialog = new SpotsDialog(this);
        dialog.show();

        AsyncHttpClient client = new AsyncHttpClient();
        client.setTimeout(30000);
        client.get(AppConstant.saveLocation, params, new AsyncHttpResponseHandler() {

            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {

                Log.e("responseBody", responseBody.toString() + "success" + "status code" + statusCode);
                JSONObject jsonResponse;

                try {
                    String response = new String(responseBody, "UTF-8");
                    Log.e("response", response);
                    jsonResponse = new JSONObject(response);
                    Log.e("Response from Server", jsonResponse.toString());


                    String result = jsonResponse.getString("result");


                    if (result.equalsIgnoreCase("100")) {

                        dialog.dismiss();
                        String id = jsonResponse.getString("id");

                        AlertDialog alertDialog = new AlertDialog.Builder(
                                MainActivity.this).create();

                        // Setting Dialog Title
                        alertDialog.setTitle("Location Saved");

                        // Setting Dialog Message
                        alertDialog.setTitle("DigiCode: " + id);


                        // Setting OK Button
                        alertDialog.setButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();

                            }
                        });

                        // Showing Alert Message
                        alertDialog.show();


                    } else {

                        dialog.dismiss();

                        String message = jsonResponse.getString("message");

                        AlertDialog alertDialog = new AlertDialog.Builder(
                                MainActivity.this).create();

                        // Setting Dialog Title
                        alertDialog.setTitle("Error");

                        // Setting Dialog Message
                        alertDialog.setMessage(message);


                        // Setting OK Button
                        alertDialog.setButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();

                            }
                        });

                        // Showing Alert Message
                        alertDialog.show();


                    }


                } catch (Exception e1) {

                    e1.printStackTrace();
                    dialog.dismiss();


                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                Log.e("responseBody", String.valueOf(responseBody) + "fail" + "status code" + statusCode);

                dialog.dismiss();


            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);//Menu Resource, Menu
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_search:

                openDialog();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void openDialog() {
        // Create custom dialog object
        final Dialog dialog = new Dialog(MainActivity.this);
        // Include dialog.xml file
        dialog.setContentView(R.layout.dialog);
        // Set dialog title
        dialog.setTitle("Search Location");


        dialog.show();

        Button search = (Button) dialog.findViewById(R.id.search);
        final EditText searchId = (EditText) dialog.findViewById(R.id.searchId);
        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Close dialog
                if (searchId.getText().toString().length() == 0) {
                    searchId.setError("Can't be empty");
                } else {

                    dialog.dismiss();
                    RequestParams requestParams = new RequestParams();
                    requestParams.put("id", searchId.getText().toString().trim());
                    searchDataTask(requestParams);
                }


            }
        });

    }


    public void searchDataTask(RequestParams params) {

        final AlertDialog dialog = new SpotsDialog(this);
        dialog.show();

        AsyncHttpClient client = new AsyncHttpClient();
        client.setTimeout(30000);
        client.get(AppConstant.getLocation, params, new AsyncHttpResponseHandler() {

            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {

                Log.e("responseBody", responseBody.toString() + "success" + "status code" + statusCode);
                JSONObject jsonResponse;

                try {
                    String response = new String(responseBody, "UTF-8");
                    Log.e("response", response);
                    jsonResponse = new JSONObject(response);
                    Log.e("Response from Server", jsonResponse.toString());





                    dialog.dismiss();
                    double latitude = jsonResponse.getDouble("latitude");
                    double longitude = jsonResponse.getDouble("longitude");

                    Intent intent=new Intent(MainActivity.this,Result.class);
                    Bundle bundle = new Bundle();
                    bundle.putDouble("latitude", latitude);
                    bundle.putDouble("longitude", longitude);
                    intent.putExtras(bundle);
                    startActivity(intent);




                } catch (Exception e1) {

                    e1.printStackTrace();
                    dialog.dismiss();


                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                Log.e("responseBody", String.valueOf(responseBody) + "fail" + "status code" + statusCode);

                dialog.dismiss();


            }
        });
    }

}

