package com.example.redcarpetassignment;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.support.design.widget.Snackbar;

import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.camerakit.CameraKitView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import id.zelory.compressor.Compressor;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {

    private LinearLayout rootLayout;
    private EditText phoneEditText;
    private Button submitBtn;
    private String number = "";
    private CameraKitView cameraKitView;
    private Button captureBtn;
    private File compressedImage;
    private Disposable disposable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        rootLayout = findViewById(R.id.rootLayout);
        phoneEditText = findViewById(R.id.phoneEditText);
        cameraKitView = findViewById(R.id.cameraKitView);
        submitBtn = findViewById(R.id.submitBtn);
        captureBtn = findViewById(R.id.captureBtn);
        captureBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onCaptureClick();
            }
        });
        submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onSubmitClick();
            }
        });
        compressedImage = new File(Environment.getExternalStorageDirectory(), "compressed.jpg");
    }

    @Override
    protected void onStart() {
        super.onStart();
        cameraKitView.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        cameraKitView.onResume();
    }

    @Override
    protected void onPause() {
        cameraKitView.onPause();
        super.onPause();
    }

    @Override
    protected void onStop() {
        cameraKitView.onStop();
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        disposable.dispose();
        super.onDestroy();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 100) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            } else {
                Snackbar.make(rootLayout, "Permission is required to save your picture", Snackbar.LENGTH_LONG);
            }
        } else {
            cameraKitView.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
        getStoragePermission();
    }

    private void onCaptureClick() {
        cameraKitView.captureImage(new CameraKitView.ImageCallback() {
            @Override
            public void onImage(CameraKitView cameraKitView, final byte[] capturedImage) {
                File savedPhoto = new File(Environment.getExternalStorageDirectory(), "photo.jpg");
                try {
                    FileOutputStream outputStream = new FileOutputStream(savedPhoto.getPath());
                    outputStream.write(capturedImage);
                    outputStream.close();
                    Log.d("Debug", savedPhoto.exists() ? "true" : "false");
                    compressImage(savedPhoto);
                } catch (java.io.IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void onSubmitClick() {
        number = phoneEditText.getText().toString();
        if (!(number.length() == 10 && (number.charAt(0) == '7' || number.charAt(0) == '8' || number.charAt(0) == '9'))) {
            Snackbar.make(rootLayout, "Invalid Phone number", Snackbar.LENGTH_LONG).show();
            return;
        }
        if (!compressedImage.exists()) {
            Snackbar.make(rootLayout, "Take a selfie please", Snackbar.LENGTH_LONG).show();
            return;
        }
        FirebaseHelper.getInstance(MainActivity.this).sendOtp(number);
        Intent intent = new Intent(MainActivity.this, OtpVerification.class);
        startActivity(intent);
    }

    private void getStoragePermission() {
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 100);
        }
    }

    private void compressImage(File savedPhoto) {
        disposable = new Compressor(MainActivity.this)
                .setQuality(20)
                .compressToFileAsFlowable(savedPhoto)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<File>() {
                    @Override
                    public void accept(File file) {
                        Toast.makeText(MainActivity.this, "inisde accept " + file.getPath(), Toast.LENGTH_SHORT).show();
                        try {
                            Utils.copy(file, compressedImage);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) {
                        throwable.printStackTrace();
                        Log.d("Debug", throwable.getMessage());
                    }
                });
    }
}