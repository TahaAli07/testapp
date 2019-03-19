package com.example.redcarpetassignment;

import android.app.Activity;
import android.content.Context;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

import static android.content.ContentValues.TAG;

public class FirebaseHelper {
    private FirebaseAuth firebaseAuth;
    private Context context;
    private static FirebaseHelper firebaseHelper;
    private String verId;
    private PhoneAuthProvider.ForceResendingToken resendToken;

    public static FirebaseHelper getInstance(Context context) {
        if (firebaseHelper == null) { //if there is no instance available... create new one
            firebaseHelper = new FirebaseHelper(context);
        }
        return firebaseHelper;
    }

    private FirebaseHelper(Context context) {
        FirebaseApp.initializeApp(context);
        this.context = context;
    }

    public void sendOtp(String phoneNumber) {
        PhoneAuthProvider.getInstance().verifyPhoneNumber("+91" + phoneNumber, 20, TimeUnit.SECONDS, (Activity) context
                , new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                    @Override
                    public void onVerificationCompleted(PhoneAuthCredential credential) {
                        // This callback will be invoked in two situations:
                        // 1 - Instant verification. In some cases the phone number can be instantly
                        //     verified without needing to send or enter a verification code.
                        // 2 - Auto-retrieval. On some devices Google Play services can automatically
                        //     detect the incoming verification SMS and perform verification without
                        //     user action.
                        Log.d(TAG, "onVerificationCompleted:" + credential);
                        Toast.makeText(context, "onVerificationCompleted", Toast.LENGTH_SHORT).show();
                        //signInWithPhoneAuthCredential(credential);
                    }

                    @Override
                    public void onVerificationFailed(FirebaseException e) {
                        // This callback is invoked in an invalid request for verification is made,
                        // for instance if the the phone number format is not valid.
                        Log.w(TAG, "onVerificationFailed", e);
                        Toast.makeText(context, "onVerificationFailed", Toast.LENGTH_SHORT).show();
                        if (e instanceof FirebaseAuthInvalidCredentialsException) {
                            // Invalid request
                            // ...
                        } else if (e instanceof FirebaseTooManyRequestsException) {
                            // The SMS quota for the project has been exceeded
                            // ...
                        }

                        // Show a message and update the UI
                        // ...
                    }

                    @Override
                    public void onCodeSent(String verificationId,
                                           PhoneAuthProvider.ForceResendingToken token) {
                        // The SMS verification code has been sent to the provided phone number, we
                        // now need to ask the user to enter the code and then construct a credential
                        // by combining the code with a verification ID.
                        Log.d(TAG, "onCodeSent:" + verificationId);
                        Toast.makeText(context, "OTP SENT", Toast.LENGTH_SHORT).show();
                        // Save verification ID and resending token so we can use them later
                        verId = verificationId;
                        resendToken = token;

                        // ...
                    }
                });
    }

    public void confirmOtp(String code) {
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verId, code);
        Toast.makeText(context, "Credentials received are - "+credential , Toast.LENGTH_SHORT).show();
    }
}
