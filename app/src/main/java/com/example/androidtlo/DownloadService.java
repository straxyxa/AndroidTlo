package com.example.androidtlo;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.IBinder;
import android.provider.MediaStore;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
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

        // Запуск foreground notification
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Скачивание файла")
                .setContentText("Идет скачивание...")
                .setSmallIcon(android.R.drawable.stat_sys_download)
                .build();
        startForeground(1, notification);

        return START_NOT_STICKY;
    }

    private void downloadFile(String fileUrl) {
        try {

            URL url = new URL(fileUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.connect();

            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                Log.e(TAG, "Ошибка соединения: " + connection.getResponseCode());
                stopSelf();
                return;
            }

            BufferedInputStream input = new BufferedInputStream(connection.getInputStream());
            byte[] data = new byte[1024];
            int count;
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();

            while ((count = input.read(data)) != -1) {
                buffer.write(data, 0, count);
            }

            // Сохранение файла
            saveFile(this, buffer.toByteArray());

            input.close();
            Log.i(TAG, "Файл успешно скачан.");
        } catch (Exception e) {
            Log.e(TAG, "Ошибка скачивания: ", e);
        } finally {
            stopSelf();
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
    public void saveFile(Context context, byte[] data) {
        OutputStream outputStream = null;
        try {
            Uri fileUri;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ContentValues values = new ContentValues();
                values.put(MediaStore.MediaColumns.DISPLAY_NAME, "downloaded_file.txt"); // Имя файла
                values.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS); // Папка "Загрузки"
                values.put(MediaStore.MediaColumns.MIME_TYPE, "text/plain");

                fileUri = context.getContentResolver().insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values);
            } else {
                // Для устройств до Android 10
                File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "downloaded_file.txt");
                fileUri = Uri.fromFile(file);
            }

            if (fileUri != null) {
                outputStream = context.getContentResolver().openOutputStream(fileUri);
                outputStream.write(data);
                Log.i("DownloadService", "Файл успешно сохранен: " + fileUri.toString());
            }
        } catch (Exception e) {
            Log.e("DownloadService", "Ошибка сохранения файла", e);
        } finally {
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (Exception e) {
                    Log.e("DownloadService", "Ошибка при закрытии потока", e);
                }
            }
        }
    }
}
