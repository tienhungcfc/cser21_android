package vn.cservn;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import java.io.IOException;
import java.net.URL;
import java.util.Map;
//https://github.com/jirawatee/FirebaseCloudMessaging-Android/blob/master/app/src/main/java/com/example/fcm/MyFirebaseMessagingService.java
public class MyFirebaseMessagingService extends FirebaseMessagingService {

    /**
     * Called when message is received.
     *
     * @param remoteMessage Object representing the message received from Firebase Cloud Messaging.
     */
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        // TODO(developer): Handle FCM messages here.
        // If the application is in the foreground handle both data and notification messages here.
        // Also if you intend on generating your own notifications as a result of a received FCM
        // message, here is where that should be initiated. See sendNotification method below.
        RemoteMessage.Notification notification = remoteMessage.getNotification();
        Map<String, String> data = remoteMessage.getData();

        sendNotification(notification, data);

    }


    public static Bitmap getImageUrl(String _url){
        try{
            URL url = new URL(_url);
            return   BitmapFactory.decodeStream(url.openConnection().getInputStream());
        }catch (Exception e){
            return  null;
        }
    }
    /**
     * Create and show a custom notification containing the received FCM message.
     *
     * @param notification FCM notification payload received.
     * @param data FCM data payload received.
     */
    public void sendNotification(RemoteMessage.Notification notification, Map<String, String> data) {

        String content= notification.getBody();

        Bitmap icon = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher);

        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        //PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT);

        for(String key : data.keySet()){
            intent.putExtra(key, data.get(key));
        }



        PendingIntent pendingIntent =  PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, "channel_id")
                .setContentTitle(notification.getTitle())
                .setContentText(content)
                .setAutoCancel(true)
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .setContentIntent(pendingIntent)
                .setContentInfo(notification.getTitle())
                .setLargeIcon(icon)
                //.setColor(Color.RED)
                .setLights(Color.RED, 1000, 300)
                .setDefaults(Notification.DEFAULT_VIBRATE)
                .setSmallIcon(R.mipmap.ic_launcher);

        //data -> customize -> display noti
        //1
        String smallIcon = data.containsKey("smallIcon") ? data.get("smallIcon") : null;
        if(smallIcon!=null && !"".equals(smallIcon)){
           //It's not possible to set a custom small icon,
        }

        //2
        String largeIconUrl = data.containsKey("largeIcon") ? data.get("largeIcon") : null;
        if(largeIconUrl!=null && !"".equals(largeIconUrl) &&  !"ic_launcher".equals(largeIconUrl)){
            Bitmap b= getImageUrl(largeIconUrl);
            if(b!=null) notificationBuilder.setLargeIcon(b);
        }

        //3
        String color = data.containsKey("color") ? data.get("color") : null;
        int _Color=getResources().getColor(R.color.colorPrimary);
        if(color!=null && !"".equals(color)  ){
            _Color =Color.parseColor(color);
            notificationBuilder.setColor(_Color);
        }

        //4
        String light_color = data.containsKey("light_color") ? data.get("light_color") : null;
        int color_light =getResources().getColor(R.color.colorPrimary);
        if(light_color!=null && !"".equals(light_color) ){
            color_light=  Color.parseColor(light_color);
        }

        //5
        String light_onMs = data.containsKey("light_onMs") ? data.get("light_onMs") : null;
        int onMs= 1000;
        if(light_onMs!=null && !"".equals(light_onMs) ){
           onMs = Integer.parseInt(light_onMs);
        }

        //6
        int offMs=300;
        String light_offMs = data.containsKey("light_offMs") ? data.get("light_offMs") : null;
        if(light_offMs!=null && !"".equals(light_offMs) ){
            offMs = Integer.parseInt(light_offMs);
        }

        notificationBuilder.setLights(color_light, onMs, offMs);

        //7
        String vibrate = data.containsKey("vibrate") ? data.get("vibrate") : null;
        int _vibrate= Notification.DEFAULT_VIBRATE;
        if(vibrate!=null && !"".equals(vibrate) && !"DEFAULT_VIBRATE".equals(vibrate) ){
            _vibrate = Integer.parseInt(vibrate);
            notificationBuilder.setDefaults(_vibrate);
        }


        //8
        String sound = data.containsKey("sound") ? data.get("sound") : null;
        Uri _sound= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        if(sound!=null && !"".equals(sound) && sound !="TYPE_NOTIFICATION" ){
            _sound=  Uri.parse(sound);
        }


        try {
            String picture_url = data.get("picture_url");
            if (picture_url != null && !"".equals(picture_url)) {
                URL url = new URL(picture_url);
                Bitmap bigPicture = BitmapFactory.decodeStream(url.openConnection().getInputStream());
                notificationBuilder.setStyle(
                        new NotificationCompat.BigPictureStyle().bigPicture(bigPicture).setSummaryText(notification.getBody())
                );
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

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

        notificationManager.notify(001, notificationBuilder.build());
    }
}