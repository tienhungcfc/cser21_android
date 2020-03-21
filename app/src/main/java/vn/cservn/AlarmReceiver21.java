package vn.cservn;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.NotificationCompat;

import com.google.gson.Gson;

import java.io.IOException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class AlarmReceiver21 extends BroadcastReceiver {

    //https://stackoverflow.com/questions/4459058/alarm-manager-example
    private AlarmManager alarmMgr;
    private PendingIntent alarmIntent;

    private static String shareName = "alert_config";


    public String Now() {
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Date date = new Date();
        return (dateFormat.format(date)); //2016/11/16 12:08:43
    }


   /* public void setAlarm(Context context)
    {
        AlarmManager am =( AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        Intent i = new Intent(context, Alarm.class);
        PendingIntent pi = PendingIntent.getBroadcast(context, 0, i, 0);
        am.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), 1000 * 60 * 10, pi); // Millisec * Second * Minute
    }*/

    public static void setConfig(String jsonConfig, Context context) {
        try {
            //Gson gson = new Gson();
            SharedPreferences sharedPreferences = context.getSharedPreferences(shareName, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("config", jsonConfig);
            editor.apply();


        } catch (Exception e) {

        }
    }

    Config getConfig(Context context){
        Config config = new Config();
        try {
            SharedPreferences sharedPreferences = context.getSharedPreferences(shareName, Context.MODE_PRIVATE);
            Gson gson = new Gson();
            config = gson.fromJson(sharedPreferences.getString("config", null), Config.class);
        } catch (Exception ex) {

        }

         return config;
    }

    public void setAlarm(Context context) {
         Config config = getConfig(context);
         if(config == null) return;;
        ;

        if (!config.enable) {
            cancelAlarm(context);
            return;
        }

        if (alarmMgr != null) {
            alarmMgr.cancel(alarmIntent);
        }
        alarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, AlarmReceiver21.class);
        alarmIntent = PendingIntent.getBroadcast(context, 0, intent, 0);

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());


        long intervalMillis = config.intervalMillis;
        alarmMgr.setInexactRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), intervalMillis, alarmIntent);

        //
        ComponentName receiver = new ComponentName(context, BroadcastReceiver21.class);
        PackageManager pm = context.getPackageManager();

        pm.setComponentEnabledSetting(receiver,
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP);
    }

    public void cancelAlarm(Context context) {
        Intent intent = new Intent(context, AlarmReceiver21.class);
        PendingIntent sender = PendingIntent.getBroadcast(context, 0, intent, 0);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(sender);
        //
        ComponentName receiver = new ComponentName(context, BroadcastReceiver21.class);
        PackageManager pm = context.getPackageManager();

        pm.setComponentEnabledSetting(receiver,
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP);
    }

    private static int increId = 1;

    @Override
    public void onReceive(Context context, Intent intent) {

        final Config config = getConfig(context);
        if(config == null) return;;
        if (!config.enable) {
            return;
        }
        if(WebControl.IsNullEmpty(config.server) ) return;

        final  Context c= context;
        final  Intent i = intent;

        new Runnable(){
            @Override
            public void run() {
                String s= "";
                Noti21 noti21 = new Noti21();
                noti21.notification = new Notification21();
                noti21.notification.title = "Alert-" + Now();

                noti21.data = new HashMap<String, String>();

                if (increId >= Integer.MAX_VALUE) increId = 0;
                int noti_id= increId++;
                noti21.data.put("noti_id", "" + noti_id);
                noti21.notification.body = "server -> " + s;//new Date()
                noti(noti21, c, i);
            }
        }.run();
       // demoNoti();
    }

    void demoNoti(Context context, Intent intent){
        Noti21 noti21 = new Noti21();
        noti21.notification = new Notification21();
        noti21.notification.title = "Alert-" + Now();

        noti21.data = new HashMap<String, String>();

        if (increId >= Integer.MAX_VALUE) increId = 0;
        int noti_id= increId++;
        noti21.data.put("noti_id", "" + noti_id);
        noti21.notification.body = "ok -> " + noti_id;//new Date()
        noti(noti21, context, intent);
    }

    public static void noti(Noti21 noti21, Context context, Intent sourceIntent) {
        String content = noti21.notification.body;
        Bitmap icon = BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher);
        Map<String, String> data = noti21.data;

        int noti_id = 001;

        if (data == null) data = new HashMap<String, String>();

        if (data.containsKey("noti_id")) noti_id = Integer.parseInt(data.get("noti_id"));
        //PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT);

        Intent intent = new Intent(APP.getAppContext(), MainActivity.class);

        if (data != null)
            for (String key : data.keySet()) {
                intent.putExtra(key, data.get(key));
            }



        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);


        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context, "channel_id")
                .setContentTitle(noti21.notification.title)
                .setContentText(content)
                .setAutoCancel(true)
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .setContentIntent(pendingIntent)
                .setContentInfo(noti21.notification.title)
                .setLargeIcon(icon)
                //.setColor(Color.RED)
                .setLights(Color.RED, 1000, 300)
                .setDefaults(Notification.DEFAULT_VIBRATE)
                .setSmallIcon(R.mipmap.ic_launcher);

        //data -> customize -> display noti
        //1
        String smallIcon = data.containsKey("smallIcon") ? data.get("smallIcon") : null;
        if (smallIcon != null && !"".equals(smallIcon)) {
            //It's not possible to set a custom small icon,
        }

        //2
        String largeIconUrl = data.containsKey("largeIcon") ? data.get("largeIcon") : null;
        if (largeIconUrl != null && !"".equals(largeIconUrl) && !"ic_launcher".equals(largeIconUrl)) {
            largeIconUrl = DownloadFilesTask.tryDecodeUrl(largeIconUrl);
            Bitmap b = MyFirebaseMessagingService.getImageUrl(largeIconUrl);
            if (b != null) notificationBuilder.setLargeIcon(b);
        }

        //3
        String color = data.containsKey("color") ? data.get("color") : null;
        int _Color = context.getResources().getColor(R.color.colorPrimary);
        if (color != null && !"".equals(color)) {
            _Color = Color.parseColor(color);
            notificationBuilder.setColor(_Color);
        }

        //4
        String light_color = data.containsKey("light_color") ? data.get("light_color") : null;
        int color_light = context.getResources().getColor(R.color.colorPrimary);
        if (light_color != null && !"".equals(light_color)) {
            color_light = Color.parseColor(light_color);
        }

        //5
        String light_onMs = data.containsKey("light_onMs") ? data.get("light_onMs") : null;
        int onMs = 1000;
        if (light_onMs != null && !"".equals(light_onMs)) {
            onMs = Integer.parseInt(light_onMs);
        }

        //6
        int offMs = 300;
        String light_offMs = data.containsKey("light_offMs") ? data.get("light_offMs") : null;
        if (light_offMs != null && !"".equals(light_offMs)) {
            offMs = Integer.parseInt(light_offMs);
        }

        notificationBuilder.setLights(color_light, onMs, offMs);

        //7
        String vibrate = data.containsKey("vibrate") ? data.get("vibrate") : null;
        int _vibrate = Notification.DEFAULT_VIBRATE;
        if (vibrate != null && !"".equals(vibrate) && !"DEFAULT_VIBRATE".equals(vibrate)) {
            _vibrate = Integer.parseInt(vibrate);
            notificationBuilder.setDefaults(_vibrate);
        }


        //8
        String sound = data.containsKey("sound") ? data.get("sound") : null;
        Uri _sound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        if (sound != null && !"".equals(sound) && sound != "TYPE_NOTIFICATION") {
            _sound = Uri.parse(sound);
        }


        try {
            String picture_url = data.get("picture_url");
            if (picture_url != null && !"".equals(picture_url)) {
                picture_url = DownloadFilesTask.tryDecodeUrl(picture_url);
                URL url = new URL(picture_url);
                Bitmap bigPicture = BitmapFactory.decodeStream(url.openConnection().getInputStream());
                notificationBuilder.setStyle(
                        new NotificationCompat.BigPictureStyle().bigPicture(bigPicture).setSummaryText(content)
                );
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        // Notification Channel is required for Android O and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    "channel_id", "channel_name", NotificationManager.IMPORTANCE_DEFAULT
            );
            channel.setDescription("channel description");
            channel.setShowBadge(true);
            channel.canShowBadge();
            channel.enableLights(true);
            channel.setLightColor(_Color);
            channel.enableVibration(true);
            channel.setVibrationPattern(new long[]{100, 200, 300, 400, 500});
            notificationManager.createNotificationChannel(channel);
        }

        notificationManager.notify(noti_id, notificationBuilder.build());
    }

    class Config {
        public boolean enable;
        public int intervalMillis = 1000 * 60 * 15;
        public String server;
        public boolean syncLocaction;
    }
    class ServerData{

    }
}