package vn.cservn;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.Instrumentation;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Looper;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.util.Base64;
import android.util.Log;
import android.webkit.ValueCallback;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.gson.Gson;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class App21 {
    Context mContext;

    App21(Context _mContext) {
        mContext = _mContext;

    }

    public void App21Result(Result result) {

        try {
           /* JSONObject json = new JSONObject();
            json.put("success", result.success);
            json.put("error", result.error);
            json.put("data", result.data);
            json.put("sub_cmd_id", result.sub_cmd_id);
            json.put("sub_cmd", result.sub_cmd);*/

            Gson gson = new Gson();
            String params = result.params;//có thể params= jsonString => lỗi
            result.params = "";

            String s = gson.toJson(result);

            s = s.replace("'", "\\'");
            if (params != null) params = params.replace("'", "\\'");
            String script = "App21Result('" + s + "', '" + params + "')";
            // wv.evaluateJavascript(script, null);

            MainActivity m = (MainActivity) mContext;
            m.evalJs(script);
            ;

        } catch (Throwable tx) {
            Log.wtf("e", "loi", tx);
        }
    }

    /**
     * @param json {sub_cmd, params, sub_cmd_id}
     */
    public void call(String json) {
        Result rs = new Result();
        rs.sub_cmd = "";
        rs.sub_cmd_id = 0;
        rs.params = "";
        try {
            JSONObject c = new JSONObject(json);
            rs.sub_cmd = c.getString("sub_cmd");
            rs.sub_cmd_id = c.getInt("sub_cmd_id");

            if (c.has("params"))
                rs.params = c.getString("params");


            Method method = App21.class.getDeclaredMethod(rs.sub_cmd, Result.class);
            method.setAccessible(true);

            if (method == null) throw new Throwable("NO_" + rs.sub_cmd);
            method.invoke(this, rs);

        } catch (Throwable tx) {
            //nothing to do

            rs.success = false;
            rs.error = tx.toString();
            rs.data = "";
            App21Result(rs);
        }
    }

    void REBOOT(Result result) {

        int miliSecond = Integer.parseInt(result.params);

        Result rs = result.copy();
        rs.success = true;
        rs.data = "REBOOT AFTER " + miliSecond + "(MS)";

        App21Result(rs);


        Async21.run(miliSecond, new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(mContext, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                PendingIntent pendingIntent = PendingIntent.getActivity(mContext, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
                AlarmManager mgr = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
                mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 100, pendingIntent);
                System.exit(0);
            }
        });


    }

    void _PERMISSION(final Result result, final String PermissionName, final Runnable granted) {
        final MainActivity m = (MainActivity) mContext;

        m.checkPermission(PermissionName, new Callback21() {
            @Override
            public void ok() {
                granted.run();
            }

            @Override
            public void no() {
                m.requirePermission(PermissionName, new Callback21() {
                    @Override
                    public void ok() {
                        granted.run();
                    }

                    @Override
                    public void no() {
                        Result rs = result.copy();
                        rs.success = false;
                        rs.error = "PERMISSION/" + PermissionName + "/DENIED";
                        App21Result(rs);
                    }
                });
            }
        });


    }

    ActivityResultIDManager activityResultIDManager = new ActivityResultIDManager();
    boolean IsMe = false;

    File save(Bitmap bmp, String filename) {
        ContextWrapper cw = new ContextWrapper(mContext.getApplicationContext());
        File directory = cw.getDir("profile", Context.MODE_PRIVATE);
        if (!directory.exists()) {
            directory.mkdir();
        }
        File mypath = new File(directory, filename);

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(mypath);
            bmp.compress(filename.endsWith(".png") ? Bitmap.CompressFormat.PNG : Bitmap.CompressFormat.JPEG, 100, fos);
            fos.close();
        } catch (Exception e) {
            Log.e("SAVE_IMAGE", e.getMessage(), e);
        }
        return mypath;
    }

    String bitmapToBase64(Bitmap bitmap) {


        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();


        return Base64.encodeToString(byteArray, Base64.URL_SAFE);
    }

    Map<String, String> mapParams(String params) {


        Map<String, String> map = new HashMap<String, String>();
        try {
            if (params == null || "".equals(params)) return map;
            for (String seg : params.split(",")) {
                String[] arr = seg.split(":");
                map.putIfAbsent(arr[0], arr.length > 1 ? arr[1] : null);
            }
        } catch (Exception e) {
            //
        }
        return map;
    }

    String now() {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd-HHmmss");
        Date date = new Date(System.currentTimeMillis());
        return formatter.format(date);
    }

    void BASE64(final Result result) {

        new Runnable() {
            @Override
            public void run() {
                Result rs = result.copy();
                MainActivity m = (MainActivity) mContext;
                DownloadFilesTask downloadFilesTask = new DownloadFilesTask();
                String base64 = downloadFilesTask.toBase64(result.params);
                m.evalJs("__BASE64('" + base64 + "')");
            }
        }.run();
    }

    void CAMERA(final Result result) {
        final String CAMERA = Manifest.permission.CAMERA;
        PackageManager packageManager = mContext.getPackageManager();
        if (packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)) {
            // this device has a camera
            _PERMISSION(result.copy(), CAMERA, new Runnable() {
                @Override
                public void run() {

                    Async21.run(0, new Runnable() {
                        @Override
                        public void run() {
                            final MainActivity m = (MainActivity) mContext;

                            Intent cInt = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

                            if (cInt.resolveActivity(m.getPackageManager()) == null) {
                                Result rs = result.copy();
                                rs.success = false;
                                rs.error = "resolveActivity()==null";
                                App21Result(rs);
                                return;
                            }


                            IsMe = true;
                            m.startActivityForResult(cInt, activityResultIDManager.put(new ActivityResultID() {
                                @Override
                                public void run() {
                                    if (this.resultCode == Activity.RESULT_OK) {
                                        Bitmap bp = (Bitmap) this.intent.getExtras().get("data");

                                        //imgCapture.setImageBitmap(bp);

                                        Result rs = result.copy();
                                        Map<String, String> map = mapParams(rs.params);
                                        String ext = map.containsKey("ext") ? map.get("ext") : "png";
                                        String pref = map.containsKey("pref") ? map.get("pref") : "IMG";
                                        rs.success = true;
                                        rs.data = "OK";

                                        File f = save(bp, pref + now() + "." + ext);
                                        rs.data = "file://" + f.getAbsolutePath();
                                        App21Result(rs);
                                    } else if (resultCode == Activity.RESULT_CANCELED) {
                                        Result rs = result.copy();
                                        rs.success = false;
                                        rs.error = "resultCode=" + resultCode;
                                        App21Result(rs);
                                    }
                                }
                            }), cInt.getExtras());
                        }
                    });
                }
            });


        } else {
            // no camera on this device
            Result rs = result.copy();

            rs.success = false;
            rs.error = "no camera on this device";
            App21Result(rs);
        }
    }

    void FILE(final Result result) {
        Result rs = result.copy();
        rs.success = true;
        rs.data = "file OK";

        App21Result(rs);
    }

    void DELETE_FILE(final Result result) {
        Result rs = result.copy();

        try {
            String pre = "file://";
            String path = result.params;
            if (path != null && !"".equals(path)) {
                //path = path.toLowerCase();
                if (path.startsWith(pre)) path = path.replace(pre, "");
                File f = new File(path);
                if (!f.exists()) throw new Exception("FILE NOT EXIST");
                f.delete();
            }
            rs.success = true;
            rs.data = "deleted";
        } catch (Exception e) {
            rs.success = false;
            rs.error = e.getMessage();
        }
        App21Result(rs);
    }

    void REQUIRE_PERMISSIONS(final Result result) {
        final MainActivity m = (MainActivity) mContext;
        m.requirePermission(result.params, new Callback21() {
            @Override
            public void ok() {
                Result rs = result.copy();
                rs.success = true;
                App21Result(rs);
                ;
            }

            @Override
            public void no() {
                Result rs = result.copy();
                rs.success = false;
                App21Result(rs);
                ;
            }
        });
    }

    Loction21 loction21 = null;

    void LOCATION(final Result result) {
        final MainActivity m = (MainActivity) mContext;
        final String COARSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
        final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
        _PERMISSION(result, COARSE_LOCATION + "," + FINE_LOCATION, new Runnable() {
            @Override
            public void run() {

                if (loction21 == null) {
                    loction21 = new Loction21(mContext);
                    //loction21.mContext = mContext;
                }
                if (loction21.isLocationEnabled()) {
                    Result rs = result.copy();
                    rs.success = true;
                    App21Result(rs);
                    ;
                    loction21.run(rs.params);

                } else {
                    Result rs = result.copy();
                    rs.success = false;
                    rs.error = "Turn off location";
                    App21Result(rs);
                    ;
                }
            }
        });
    }

    void DOWNLOAD(final Result result) {
        DownloadFilesTask downloadFilesTask = new DownloadFilesTask() {
            @Override
            protected void onPostExecute(String localPath) {
                Result rs = result.copy();
                rs.success = true;
                rs.data = localPath;
                app21.App21Result(rs);
            }
        };
        downloadFilesTask.app21 = this;
        downloadFilesTask.execute(result.params);
    }

    void GET_DOWNLOADED(final Result result) {
        DownloadFilesTask downloadFilesTask = new DownloadFilesTask() {
        };
        downloadFilesTask.app21 = this;
        Result rs = result.copy();
        rs.data = downloadFilesTask.getlist();
        rs.success = true;
        App21Result(rs);
        ;
    }

    void CLEAR_DOWNLOAD(final Result result) {
        DownloadFilesTask downloadFilesTask = new DownloadFilesTask();
        downloadFilesTask.app21 = this;
        downloadFilesTask.clear(result.params, new Runnable() {
            @Override
            public void run() {
                Result rs = result.copy();
                rs.success = true;
                rs.data = "";
                App21Result(rs);
            }
        });
    }

    void NOTI(final Result result) {

        try {
            Gson gson = new Gson();
            Noti21 noti21 = gson.fromJson(result.params, Noti21.class);
            // Noti21 noti21 = new Noti21();

            // noti21.notification = new Notification21();
            // noti21.notification.title = "test";


            final MainActivity m = (MainActivity) mContext;


            AlarmReceiver21.noti(noti21, mContext, m.getIntent());

            result.success = true;
            App21Result(result);
        } catch (Exception ex) {
            result.success = false;
            result.error = ex.getMessage();
            App21Result(result);
        }
    }

    void ALARM_NOTI(final Result result) {

        final String WAKE_LOCK = Manifest.permission.WAKE_LOCK;

        _PERMISSION(result, WAKE_LOCK, new Runnable() {
            @Override
            public void run() {
                AlarmReceiver21.setConfig(result.params, mContext);
                new AlarmReceiver21().setAlarm(mContext);
                ;
                Result rs = result.copy();
                rs.success = true;
                App21Result(rs);

            }
        });
    }

    void GET_PHONE(final Result result) {
        final String READ_PHONE_STATE = Manifest.permission.READ_PHONE_STATE;
        _PERMISSION(result, READ_PHONE_STATE, new Runnable() {
            @Override
            public void run() {
                TelephonyManager tMgr = (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);
                @SuppressLint("MissingPermission")
                String number = tMgr.getLine1Number();
                Result rs = result.copy();
                rs.success = true;
                rs.data = number;
                App21Result(rs);
                ;
            }
        });
    }

    void SEND_SMS(final Result result) {
        final String SEND_SMS = Manifest.permission.SEND_SMS;
        _PERMISSION(result, SEND_SMS, new Runnable() {
            @Override
            public void run() {

                try {

                    SMS sms= new Gson().fromJson(result.params, SMS.class);

                    SmsManager.getDefault().sendTextMessage(sms.number, null, sms.smsText, null, null);
                    Result rs = result.copy();
                    rs.success = true;

                    App21Result(rs);
                    ;
                } catch (Exception ex) {
                    Result rs = result.copy();
                    rs.success = false;
                    rs.error = ex.getMessage();
                    App21Result(rs);
                }

            }
        });
    }

    public boolean onActivityResult(int requestCode, int resultCode, Intent intent, Activity activity) {
        // Activity act = activity.getCallingActivity().;


        if (IsMe) {
            activityResultIDManager.run(requestCode, resultCode, intent);
        }
        boolean t = IsMe;
        IsMe = false;
        return t; //true -> xuwr lys trong app21
    }
}

class Result {
    public boolean success;
    public String error;
    public Object data;

    public String sub_cmd;
    public int sub_cmd_id;
    public String params;

    /**
     * copy
     */
    public Result copy() {
        Result _r = new Result();

        for (Field f : Result.class.getFields()) {
            try {
                f.set(_r, f.get(this));
            } catch (Exception ex) {

            }
        }

        return _r;
    }
}

class Async21 {
    public final static void run(final int miliSeconds, final Runnable fn) {
        new Thread() {
            public void run() {
                try {
                    Thread.sleep(miliSeconds);
                    fn.run();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }
        }.start();
    }
}

class Callback21 {
    public Exception lastExp;
    public Object lastResult;

    public void ok() {
    }

    ;

    public void no() {
    }

    ;
}

abstract class ActivityResultID implements Runnable {

    public int requestCode;
    public int resultCode;
    public Intent intent;

}

class ActivityResultIDManager {


    List<ActivityResultID> items = new ArrayList<>();
    int IncreId = 1;

    public int put(ActivityResultID activityResultID) {
        activityResultID.requestCode = IncreId++;
        items.add(activityResultID);
        return activityResultID.requestCode;
    }

    public int run(int requestCode, int resultCode, Intent intent) {
        ActivityResultID _r = null;
        for (ActivityResultID r : items) {
            if (r.requestCode == requestCode) {
                r.resultCode = resultCode;
                r.intent = intent;
                r.run();
                _r = r;
            }
        }
        if (_r != null) {
            items.remove(_r);
            return _r.requestCode;
        }
        return -1;
    }
}

class SMS {
    public String number;
    public String smsText;
}

