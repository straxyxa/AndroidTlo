package com.example.androidtlo;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.lifecycle.ViewModelProvider;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends AppCompatActivity {
    private EditText editTextEnterAddress;
    private TextView textViewSizeFile;
    private TextView textViewTypeFile;
    private Button buttonGetInfo;
    private Button buttonGetFile;
    private Handler handler = new Handler(Looper.getMainLooper());
    private ProgressBar progressBar;
    private TextView downloadedBytesView;

    private final String  CHANNEL_ID = "file_download_channel";
    private final  Byte  NOTIFICATION_ID = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initViews();
        progressBar = findViewById(R.id.progressBar);
        buttonGetInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getFileInfo(getUrl());
            }
        });
        FileDownloder fileDownloder = new FileDownloder(MainActivity.this);
        buttonGetFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = getUrl();
                fileDownloder.downloadFile(url, progressBar, downloadedBytesView);
                sendNotification("Download Started", "Downloading", 0);
            }
        });
    }

    private String getUrl() {
        return editTextEnterAddress.getText().toString().trim();
    }

    private void getFileInfo(String url1){
        if(urlValidation()){
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        URL url = new URL(url1);
                        HttpURLConnection connection = (HttpURLConnection)url.openConnection();
                        connection.setRequestMethod("HEAD");
                        connection.connect();
                        String fileType = connection.getContentType();
                        int fileLength = connection.getContentLength();
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                if(fileType == null){
                                    Toast.makeText(
                                            MainActivity.this,
                                            R.string.get_type_file_error,
                                            Toast.LENGTH_SHORT
                                    ).show();
                                }else {
                                    textViewTypeFile.setText(fileType);
                                }
                                if(fileLength == -1){
                                    Toast.makeText(
                                            MainActivity.this,
                                            R.string.get_size_file_error,
                                            Toast.LENGTH_SHORT
                                    ).show();
                                }else{
                                    textViewSizeFile.setText(String.valueOf(fileLength));
                                }
                            }
                        });
                        connection.disconnect();
                    } catch (final IOException e) {
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                System.err.println("error to get file info!");
                            }
                        });
                    }
                }
            });
            thread.start();

        }
    }

    private boolean urlValidation() {
        String url = getUrl();
        if (url.isEmpty()) {
            editTextEnterAddress.setError(getString(R.string.error_field_address));
            return false;
        } else if (url.startsWith(getString(R.string.urlValidation1)) ||
                url.startsWith(getString(R.string.urlValidation2)) ||
                url.startsWith(getString(R.string.urlValidation3))
        ) {
            return true;
        }else{
            editTextEnterAddress.setError(getString(R.string.error_field_address1));
            return false;
        }
    }

    private void initViews() {
        editTextEnterAddress = findViewById(R.id.editTextEnterAddress);
        textViewSizeFile = findViewById(R.id.textViewSizeFile);
        textViewTypeFile = findViewById(R.id.textViewTypeFile);
        buttonGetInfo = findViewById(R.id.buttonGetInfo);
        buttonGetFile = findViewById(R.id.buttonGetFile);
    }
   // Create channel
    private  void createNotificationChannel() {
        String name = "File Download Channel";
        String descriptionText = "Channel for file download notifications";
        int importance = 0;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            importance = NotificationManager.IMPORTANCE_HIGH;
        }
        NotificationChannel channel = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(descriptionText);
        }
        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationManager.createNotificationChannel(channel);
        }
    }
        //resend/update message
    private  void updateNotification(String title, String message, int progress) {
        sendNotification(title, message, progress);
    }
//send message
    private void sendNotification(String title, String message, int progress) {
        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setOngoing(true)
                .setProgress(100, progress, false);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(NOTIFICATION_ID, builder.build());
    }

}