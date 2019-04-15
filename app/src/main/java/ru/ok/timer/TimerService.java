package ru.ok.timer;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;

import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;

import androidx.annotation.Nullable;

public class TimerService extends Service {

    public static final String NOTIFICATION_CHANNEL = "timer_notification";
    public static final int NOTIFICATION_ID = 0;

    private Timer timer;
    private long time = 0L;
    private boolean isActive = false;

    private NotificationManager notificationManager;
    private Notification.Builder notificationBuilder;

    private TimerBinder binder = new TimerBinder();
    private WeakReference<MainActivity> activityReference;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        Intent notificationIntent = new Intent(getApplicationContext(), MainActivity.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        notificationBuilder = new Notification.Builder(getApplicationContext())
                .setSmallIcon(R.drawable.ic_notification_timer)
                .setContentTitle("Timer title")
                .setContentText("Timer content text")
                .setContentIntent(PendingIntent.getActivity(getApplicationContext(), 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationBuilder.setChannelId(NOTIFICATION_CHANNEL);
        }
        createNotificationChannel();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Timer name";
            String description = "Timer description";
            int importance = NotificationManager.IMPORTANCE_LOW;
            NotificationChannel channel = new NotificationChannel(NOTIFICATION_CHANNEL, name, importance);
            channel.setDescription(description);
            notificationManager.createNotificationChannel(channel);
        }
    }

    class MyTimerTask extends TimerTask {

        final SimpleDateFormat millisFormatter = new SimpleDateFormat("mm:ss:SSS");
        final SimpleDateFormat secondsFormatter = new SimpleDateFormat("mm:ss");
        Date date;

        MyTimerTask() {
            millisFormatter.setTimeZone(TimeZone.getTimeZone("UTC"));
            secondsFormatter.setTimeZone(TimeZone.getTimeZone("UTC"));
        }

        @Override
        public void run() {
            time++;
            date = new Date(time);

            if (time % 1000L == 0) {
                String text = secondsFormatter.format(date);
                notificationBuilder.setContentText(text);
                notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build());
                MainActivity activity = activityReference.get();
                if (activity != null) {
                    activity.setTime(text);
                }
            }
        }
    }

    class TimerBinder extends Binder {

        void attachActivity(final MainActivity activity) {
            activityReference = new WeakReference<>(activity);
        }

        void startPause() {
            isActive = !isActive;
            if (isActive) {
                startForeground(NOTIFICATION_ID, notificationBuilder.build());
                timer = new Timer();
                timer.scheduleAtFixedRate(new MyTimerTask(), 0L, 1L);
            } else {
                timer.cancel();
            }
        }

        void stop() {
            timer.cancel();
            time = 0L;
            isActive = false;
            notificationManager.cancel(NOTIFICATION_ID);
            stopSelf();
        }
    }
}
