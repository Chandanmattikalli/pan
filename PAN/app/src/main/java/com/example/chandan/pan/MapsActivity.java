package com.example.chandan.pan;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;

import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DecimalFormat;


public class MapsActivity extends FragmentActivity implements LocationListener, GoogleMap.OnMarkerClickListener
{
    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    protected LocationManager locationManager;
    private boolean mapset;
    public TextView ScreenLog;
    private Marker marker;
    private int id;
    private TextView details;
    private int contactNumber;
    private Button buttonDelivery;
    private Button buttonCall;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.map);
        ScreenLog = (TextView) findViewById(R.id.log);
        details =  (TextView) findViewById(R.id.textViewDetail);
        buttonDelivery = (Button)findViewById(R.id.buttonDelivered);
        buttonCall = (Button)findViewById(R.id.buttonCall);

        buttonDelivery.setVisibility(View.INVISIBLE);
        buttonCall.setVisibility(View.INVISIBLE);
        details.setVisibility(View.INVISIBLE);

        setUpMapIfNeeded();
        new HttpAsyncTask().execute("http://panweb.co/vtigercrm/ums_integration/return_salesorder_data.php");
        mMap.setOnMarkerClickListener((GoogleMap.OnMarkerClickListener)this);
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        // TODO Auto-generated method stub
//        if(marker.equals(marker_1)){
//            Log.w("Click", "test");
//            return true;
//        }
        Log.w("Click","hey");
        buttonDelivery.setVisibility(View.VISIBLE);
        buttonCall.setVisibility(View.VISIBLE);
        details.setVisibility(View.VISIBLE);
        details.setText(marker.getTitle());
        return true;
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
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
    }

    /**
     * Sets up the map if it is possible to do so (i.e., the Google Play services APK is correctly
     * installed) and the map has not already been instantiated.. This will ensure that we only ever
     * call {@link #setUpMap()} once when {@link #mMap} is not null.
     * <p/>
     * If it isn't installed {@link SupportMapFragment} (and
     * {@link com.google.android.gms.maps.MapView MapView}) will show a prompt for the user to
     * install/update the Google Play services APK on their device.
     * <p/>
     * A user can return to this FragmentActivity after following the prompt and correctly
     * installing/updating/enabling the Google Play services. Since the FragmentActivity may not
     * have been completely destroyed during this process (it is likely that it would only be
     * stopped or paused), {@link #onCreate(Bundle)} may not be called again so we should call this
     * method in {@link #onResume()} to guarantee that it will be called.
     */
    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            mMap.setMyLocationEnabled(true);
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                setUpMap();
            }
        }
    }

    /**
     * This is where we can add markers or lines, add listeners or move the camera.
     * <p/>
     * This should only be called once and when we are sure that {@link #mMap} is not null.
     */
    private void setUpMap()
    {
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 100, 0, this);
    }

    @Override
    public void onLocationChanged(Location location)
    {
        double Latitude = location.getLatitude();
        double Longitude = location.getLongitude();
        Log.e("latitude",String.valueOf(Latitude));
        Log.e("longitude",String.valueOf(Longitude));

        DecimalFormat df = new DecimalFormat("##.####");
        Log.e("formatted latitude", df.format(Latitude));
        float[] results = new float[1];
        Location.distanceBetween(12.92127341, 77.55655992, Latitude, Longitude, results);
        Log.e("distance :", Float.toString(results[0]));

        if(results[0] < 20)
        {
            ScreenLog.setText("In :"+ Float.toString(results[0]));
        }
        else
        {
            ScreenLog.setText("Out :"+ Float.toString(results[0]));
        }
        // this can be done in the setUpMap function once we get the actual latitude and longitude
        if(this.mapset == false)
        {
            double latitude = location.getLatitude();
            double longitude = location.getLongitude();
            LatLng latLng = new LatLng(latitude, longitude);
            mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
            mMap.animateCamera(CameraUpdateFactory.zoomTo(15));
            this.mapset = true;
        }
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }

    //****************************************async call to get data*************************************//
    private class HttpAsyncTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {

            return GET(urls[0]);
        }
        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {
            updateMarkers(result);
        }
    }
    public void updateMarkers(String data)
    {
        try {
            JSONObject jsonRootObject = new JSONObject(data);
            JSONArray jsonArray = jsonRootObject.optJSONArray("salesorder");
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                Log.i("order no: ", jsonObject.optString("ordernumber").toString());
                mMap.addMarker(new MarkerOptions().
                                position(new LatLng(Double.parseDouble(jsonObject.optString("latitude").toString()), Double.parseDouble(jsonObject.optString("longitude").toString())))
                                .title("shop: " + jsonObject.optString("shopname").toString() + ", quantity: " + jsonObject.optString("quantity").toString())
                                .snippet(jsonObject.optString("shopnumber").toString())
                );
            }
        } catch (JSONException e) {e.printStackTrace();}
        //Log.e("Data", result);
    }

    public static String GET(String url){
        InputStream inputStream = null;
        String result = "";
        try {

            // create HttpClient
            HttpClient httpclient = new DefaultHttpClient();

            // make GET request to the given URL
            HttpResponse httpResponse = httpclient.execute(new HttpGet(url));

            // receive response as inputStream
            inputStream = httpResponse.getEntity().getContent();

            // convert inputstream to string
            if(inputStream != null)
                result = convertInputStreamToString(inputStream);
            else
                result = "Did not work!";

        } catch (Exception e) {
            Log.d("InputStream", e.getLocalizedMessage());
        }

        return result;
    }
    private static String convertInputStreamToString(InputStream inputStream) throws IOException {
        BufferedReader bufferedReader = new BufferedReader( new InputStreamReader(inputStream));
        String line = "";
        String result = "";
        while((line = bufferedReader.readLine()) != null)
            result += line;

        inputStream.close();
        return result;
    }

}