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
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class Loction21 {
    public Context mContext;
    public Result sourceResult;
    public App21 app21;

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
        Map<String, Object> data = new HashMap<String, Object>();
        try {
            for (Method m : location.getClass().getMethods()
            ) {
                try {
                    String n = m.getName();
                    if (n.startsWith("get")) {
                        Object v = m.invoke(location);
                        json.put(m.getName(), v);


                    }
                } catch (Exception ex) {
                    //
                }
            }


            json.put("lat", location.getLatitude());
            json.put("lng", location.getLongitude());

            String _d = json.toString();
            data = new Gson().fromJson(_d, data.getClass());


        } catch (Exception e) {
            //
        }
        if (sourceResult != null && app21 != null) {
            sourceResult.success = true;
            sourceResult.data = data;
            app21.App21Result(sourceResult);
        }


        //  String s = json.toString();
        //String script = jsCallbackName + "('BASE64:" + DownloadFilesTask.strBase64(s) + "')";
        // wv.evaluateJavascript(script, null);
        //MainActivity m = (MainActivity) mContext;
        //m.evalJs(script);
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
