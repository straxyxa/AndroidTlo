package com.example.androidtlo;

import android.annotation.SuppressLint;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.Toast;

public class FileDownloder {

    private final Context context;
    private final DownloadManager downloadManager;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private long downloadID;

    public FileDownloder(Context context) {
        this.context = context;
        this.downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
    }

    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    public void downloadFile(String url, ProgressBar progressBar) {
        // Настройка загрузки
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url))
                .setTitle("Downloading File")
                .setDescription("File is being downloaded...")
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                .setAllowedOverMetered(true)
                .setAllowedOverRoaming(true);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            request.setDestinationInExternalFilesDir(context, "Download", "downloaded_file.zip");
        } else {
            request.setDestinationInExternalPublicDir("/Download", "downloaded_file.zip");
        }

        // Запуск загрузки
        downloadID = downloadManager.enqueue(request);
        monitorDownloadProgress(progressBar);

        // Регистрация BroadcastReceiver
        context.registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                long id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
                if (id == downloadID) {
                    Toast.makeText(context, "Download Completed", Toast.LENGTH_SHORT).show();
                    progressBar.setProgress(100); // Устанавливаем прогресс на максимум
                }
            }
        }, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
    }

    private void monitorDownloadProgress(ProgressBar progressBar) {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                DownloadManager.Query query = new DownloadManager.Query();
                query.setFilterById(downloadID);
                Cursor cursor = downloadManager.query(query);

                if (cursor != null && cursor.moveToFirst()) {
                    // Проверяем наличие столбцов
                    int bytesDownloadedIndex = cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR);
                    int bytesTotalIndex = cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES);
                    int statusIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS);

                    if (bytesDownloadedIndex != -1 && bytesTotalIndex != -1) {
                        int bytesDownloaded = cursor.getInt(bytesDownloadedIndex);
                        int bytesTotal = cursor.getInt(bytesTotalIndex);

                        if (bytesTotal > 0) {
                            int progress = (int) ((bytesDownloaded * 100L) / bytesTotal);
                            progressBar.setProgress(progress);
                        }
                    } else {
                        Log.e("DownloadError", "Required columns not found");
                    }

                    if (statusIndex != -1) {
                        int status = cursor.getInt(statusIndex);
                        if (status == DownloadManager.STATUS_SUCCESSFUL || status == DownloadManager.STATUS_FAILED) {
                            cursor.close();
                            return; // Останавливаем мониторинг
                        }
                    }
                }

                if (cursor != null) {
                    cursor.close();
                }

                handler.postDelayed(this, 500); // Повторяем через 500 мс
            }
        }, 500);
    }
}