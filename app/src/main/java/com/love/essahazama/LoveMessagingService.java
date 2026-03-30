package com.love.essahazama;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

/**
 * يستقبل الإشعارات من Firebase Cloud Messaging
 * عندما يرسل أحدهم رسالة في الدردشة تصل إشعار للطرف الآخر
 */
public class LoveMessagingService extends FirebaseMessagingService {

    private static final String CHANNEL_ID   = "love_chat_channel";
    private static final String CHANNEL_NAME = "دردشة الحب 💜";
    private static final int    NOTIF_ID     = 1001;

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        String title = "💜 رسالة جديدة";
        String body  = "لديك رسالة حب جديدة!";

        if (remoteMessage.getNotification() != null) {
            if (remoteMessage.getNotification().getTitle() != null)
                title = remoteMessage.getNotification().getTitle();
            if (remoteMessage.getNotification().getBody() != null)
                body = remoteMessage.getNotification().getBody();
        }

        // فقط اسم المرسل — بدون نص الرسالة
        if (remoteMessage.getData().containsKey("sender")) {
            String sender = remoteMessage.getData().get("sender");
            String senderName = "essa".equals(sender) ? "Essa 💜" : "حزامه 🌸";
            title = "💜 رسالة جديدة";
            body  = "أرسل لك " + senderName + " رسالة";
        }

        showNotification(title, body);
    }

    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        // يمكن حفظ الـ token في Firebase لاحقاً
    }

    private void showNotification(String title, String body) {
        NotificationManager manager =
            (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Create channel for Android 8+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription("إشعارات دردشة Essa & حزامه");
            channel.enableVibration(true);
            if (manager != null) manager.createNotificationChannel(channel);
        }

        // Intent to open app when notification tapped
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_email)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(new NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setVibrate(new long[]{0, 250, 100, 250})
            .setContentIntent(pendingIntent);

        if (manager != null) manager.notify(NOTIF_ID, builder.build());
    }
}
