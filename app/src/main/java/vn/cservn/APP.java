package vn.cservn;

import android.app.Application;
import android.content.Context;

public class APP extends Application {

    private static Context context;

    public void onCreate() {
        super.onCreate();
        APP.context = getApplicationContext();
    }

    public static Context getAppContext() {
        return APP.context;
    }
}
