package vn.cservn;

import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.os.Looper;
import android.support.annotation.NonNull;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import org.json.JSONException;
import org.json.JSONObject;

public class Loction21 {
    public Context mContext;

    public boolean isLocationEnabled() {
        LocationManager locationManager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
                LocationManager.NETWORK_PROVIDER
        );
    }

    FusedLocationProviderClient mFusedLocationClient = null;

    public Loction21(Context mContext) {
        this.mContext = mContext;
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(mContext);
    }

    @SuppressLint("MissingPermission")
    private void requestNewLocationData() {

        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(0);
        mLocationRequest.setFastestInterval(0);
        mLocationRequest.setNumUpdates(1);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(mContext);
        mFusedLocationClient.requestLocationUpdates(
                mLocationRequest, mLocationCallback,
                Looper.myLooper()
        );

    }

    private void updateClient(Location location) {
        JSONObject json = new JSONObject();
        try {
            json.put("lat", location.getLatitude());
            json.put("lng", location.getLongitude());
        } catch (Exception e) {
            //
        }
        String s = json.toString();
        String script = jsCallbackName + "('" + s + "')";
        // wv.evaluateJavascript(script, null);
        MainActivity m = (MainActivity) mContext;
        m.evalJs(script);
    }

    private LocationCallback mLocationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            Location mLastLocation = locationResult.getLastLocation();
           /* latTextView.setText(mLastLocation.getLatitude()+"");
            lonTextView.setText(mLastLocation.getLongitude()+"");*/

            updateClient(mLastLocation);
        }
    };

    String jsCallbackName = null;

    public void run(String jsCallbackName) {
        this.jsCallbackName = jsCallbackName;
        mFusedLocationClient.getLastLocation().addOnCompleteListener(
                new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        Location location = task.getResult();
                        if (location == null) {
                            requestNewLocationData();
                        } else {
                            updateClient(location);
                        }
                    }
                }
        );
    }

}
