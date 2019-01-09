package yxr.com.servicebestpractice.service;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Binder;
import android.os.Environment;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;

import java.io.File;

import yxr.com.servicebestpractice.DownloadTask;
import yxr.com.servicebestpractice.MainActivity;
import yxr.com.servicebestpractice.R;
import yxr.com.servicebestpractice.dao.DownloadListener;

public class DownloadService extends Service {
    private DownloadTask downloadTask;
    private String downloadUrl;

    private DownloadListener listener = new DownloadListener() {
        @Override
        public void onProgress(int progress) {
            getNotificationManager().notify(1, getNotification("Downloading...", progress));
        }

        @Override
        public void onSuccess() {
            downloadTask = null;
            stopForeground(true);
            getNotificationManager().notify(1, getNotification("Download Success", -1));
            Toast.makeText(DownloadService.this, "Download Success", Toast.LENGTH_SHORT).show();


        }

        @Override
        public void onFailed() {
            downloadTask = null;
            stopForeground(true);
            getNotificationManager().notify(1, getNotification("Download Failed", -1));
            Toast.makeText(DownloadService.this, "Download Failed", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onPaused() {
            downloadTask = null;
            stopForeground(true);
            Toast.makeText(DownloadService.this, "Download Paused", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onCanceled() {
            downloadTask = null;
            stopForeground(true);
            Toast.makeText(DownloadService.this, "Download Canceled", Toast.LENGTH_SHORT).show();
        }
    };

    public DownloadService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    private DownloadBinder mBinder = new DownloadBinder();

    public class DownloadBinder extends Binder {
        public void startDownload(String url) {
            if (downloadTask == null) {
                downloadUrl = url;
                downloadTask = new DownloadTask(listener);
                downloadTask.execute(downloadUrl);
                startForeground(1, getNotification("Downloading...", 0));
                Toast.makeText(DownloadService.this, "Downloading", Toast.LENGTH_SHORT).show();

            }
        }

        public void pauseDownload() {
            if (downloadTask != null) {
                downloadTask.pauseDownload();
            }
        }

        public void cancelDownload() {
            if (downloadTask != null) {
                downloadTask.cancelDownload();
            }
            if (downloadUrl != null) {
                String fileName = downloadUrl.substring(downloadUrl.lastIndexOf("/"));
                String path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath();
                File file = new File(path + fileName);
                if (file.exists()) {
                    file.delete();
                }
                getNotificationManager().cancel(1);
                stopForeground(true);
                Toast.makeText(DownloadService.this, "Canceled", Toast.LENGTH_SHORT).show();
            }
        }


    }

    private Notification getNotification(String s, int i) {
        NotificationManager systemService = getNotificationManager();
        NotificationChannel notificationChannel = null;
        NotificationCompat.Builder builder = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            notificationChannel = new NotificationChannel("channel1", "channelName", NotificationManager.IMPORTANCE_HIGH);
            notificationChannel.setLightColor(Color.RED);
            notificationChannel.setVibrationPattern(new long[]{0, 1000, 0, 1000});
            builder = new NotificationCompat.Builder(this, "channel1");
            systemService.createNotificationChannel(notificationChannel);
        }

        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pi = PendingIntent.getActivity(this, 0, intent, 0);
        Notification build = builder.
                setSmallIcon(R.mipmap.ic_launcher).
                setVisibility(111).
                setWhen(System.currentTimeMillis()).
                setContentIntent(pi).
                setLights(Color.RED, 1000, 1000).
                setAutoCancel(true)
                .setContentTitle(s).
                        build();
        systemService.notify(1, build);


        if (i >= 0
                ) {
            builder.setContentText(i + "%");
            builder.setProgress(100, i, false);

        }
        return builder.build();
    }

    private NotificationManager getNotificationManager() {
        return (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

    }
}
