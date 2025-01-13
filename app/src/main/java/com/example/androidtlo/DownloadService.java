package com.example.androidtlo;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.IBinder;
import android.provider.MediaStore;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class DownloadService extends Service {

    private static final String CHANNEL_ID = "DownloadChannel";
    private static final String TAG = "DownloadService";

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String fileUrl = intent.getStringExtra("fileUrl"); // URL файла
        new Thread(() -> downloadFile(fileUrl)).start();
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Скачивание файла")
                .setContentText("Идет скачивание...")
                .setSmallIcon(android.R.drawable.stat_sys_download)
                .build();
        startForeground(1, notification);

        return START_NOT_STICKY;
    }

    private void sendProgressBroadcast(long pobranychBajtow, long rozmiar) {
        Intent intent = new Intent("com.example.androidtlo.PROGRESS_UPDATE");
        PostepInfo postepInfo = new PostepInfo(pobranychBajtow, rozmiar, pobranychBajtow + "/" + rozmiar + " байт");
        intent.putExtra("progress_info", postepInfo);
        sendBroadcast(intent);
    }


    private void downloadFile(String fileUrl) {
        try {
            URL url = new URL(fileUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.connect();

            long fileLength = connection.getContentLength();

            try (InputStream input = new BufferedInputStream(connection.getInputStream());
                 OutputStream output = new FileOutputStream(new File(getExternalFilesDir(null), "downloaded_file.txt"))) {

                byte[] data = new byte[1024];
                long total = 0;
                int count;
                while ((count = input.read(data)) != -1) {
                    total += count;
                    output.write(data, 0, count);
                    sendProgressBroadcast(total, fileLength);
                }
            }
        } catch (Exception e) {
            Log.e("DownloadService", "Ошибка загрузки", e);
        }
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "Служба остановлена");
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Канал скачивания",
                    NotificationManager.IMPORTANCE_LOW
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
    }
}
