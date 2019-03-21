package com.example.redcarpetassignment;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

public class OtpVerification extends AppCompatActivity {

    private LinearLayout rootLayout;
    private TextView numberTextView;
    private EditText otpEditText;
    private Button confirmOtpBtn;
    private String otp = "";
    private String number = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otp_verification);
        rootLayout = findViewById(R.id.rootLinearLayout);
        numberTextView = findViewById(R.id.numberTextView);
        otpEditText = findViewById(R.id.otpEditText);
        confirmOtpBtn = findViewById(R.id.confirmOtpBtn);
        confirmOtpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onConfirmClick();
            }
        });
        number = getIntent().getStringExtra("number");
        numberTextView.setText(number);
        FirebaseHelper.getInstance(this).findUser(number);

    }

    private void onConfirmClick() {
        otp = otpEditText.getText().toString();
        if (!otp.equalsIgnoreCase("")) {
            FirebaseHelper.getInstance(this).confirmOtp(otp);
        }
    }
}
