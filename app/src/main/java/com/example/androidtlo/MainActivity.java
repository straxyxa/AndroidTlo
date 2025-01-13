package com.example.androidtlo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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
    private TextView textViewGetByte;

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
        buttonGetFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, DownloadService.class);
                intent.putExtra("fileUrl", getString(R.string.example_address));
                startService(intent);
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



    private final BroadcastReceiver progressReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals("com.example.androidtlo.PROGRESS_UPDATE")) {
                PostepInfo postepInfo = intent.getParcelableExtra("progress_info");
                if (postepInfo != null) {
                    updateProgressUI(postepInfo);
                }
            }
        }
    };

    @Override
    protected void onStart() {
        super.onStart();
        IntentFilter filter = new IntentFilter("com.example.androidtlo.PROGRESS_UPDATE");
        registerReceiver(progressReceiver, filter);
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(progressReceiver);
    }

    private void updateProgressUI(PostepInfo postepInfo) {
        if (postepInfo.mRozmiar > 0) {
            int progress = (int) ((postepInfo.mPobranychBajtow * 100) / postepInfo.mRozmiar);
            progressBar.setProgress(progress);
        }
        textViewGetByte.setText(postepInfo.mPobranychBajtow + " / " + postepInfo.mRozmiar);
    }



    private void initViews() {
        editTextEnterAddress = findViewById(R.id.editTextEnterAddress);
        textViewSizeFile = findViewById(R.id.textViewSizeFile);
        textViewTypeFile = findViewById(R.id.textViewTypeFile);
        buttonGetInfo = findViewById(R.id.buttonGetInfo);
        buttonGetFile = findViewById(R.id.buttonGetFile);
        progressBar = findViewById(R.id.progressBar);
        textViewGetByte = findViewById(R.id.textViewGetByte);
    }

}