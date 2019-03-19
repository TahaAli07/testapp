package com.example.redcarpetassignment;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class OtpVerification extends AppCompatActivity {

    private EditText otpEditText;
    private Button confirmOtpBtn;
    private String otp = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otp_verification);
        otpEditText = findViewById(R.id.otpEditText);
        confirmOtpBtn = findViewById(R.id.confirmOtpBtn);
        confirmOtpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onConfirmClick();
            }
        });
    }

    private void onConfirmClick() {
        otp = otpEditText.getText().toString();
        if (!otp.equalsIgnoreCase("")) {
            FirebaseHelper.getInstance(this).confirmOtp(otp);
        }
    }
}
