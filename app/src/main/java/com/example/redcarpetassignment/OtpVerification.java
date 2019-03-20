package com.example.redcarpetassignment;

import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

public class OtpVerification extends AppCompatActivity {

    private LinearLayout rootLayout;
    private EditText otpEditText;
    private Button confirmOtpBtn;
    private String otp = "";
    private String number = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otp_verification);
        rootLayout = findViewById(R.id.rootLinearLayout);
        otpEditText = findViewById(R.id.otpEditText);
        confirmOtpBtn = findViewById(R.id.confirmOtpBtn);
        confirmOtpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onConfirmClick();
            }
        });
        number = getIntent().getStringExtra("number");
        FirebaseHelper.getInstance(this).findUser(number);

//        User user = FirebaseHelper.getInstance(this).userExists(number);
//        if(!user.getNumber().equalsIgnoreCase("")){
//            //user already exists Point 4
//            Toast.makeText(this, "User Already Exists", Toast.LENGTH_SHORT).show();
//            FirebaseHelper.getInstance(this).updateVisitCount(user);
//            Toast.makeText(this, "welcome back for "+user.getVisit_count()+1+" time", Toast.LENGTH_SHORT).show();
//            //TODO Move back to MainActivity
//        }else{
//            //sending OTP flow followed Point 3
//            Toast.makeText(this, "New User", Toast.LENGTH_SHORT).show();
//            FirebaseHelper.getInstance(this).sendOtp(number);
//        }

    }

    private void onConfirmClick() {
        otp = otpEditText.getText().toString();
        if (!otp.equalsIgnoreCase("")) {
            FirebaseHelper.getInstance(this).confirmOtp(otp);
        }
    }
}
