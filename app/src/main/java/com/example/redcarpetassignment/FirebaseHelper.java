package com.example.redcarpetassignment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;

import androidx.annotation.NonNull;

import com.google.android.material.snackbar.Snackbar;

import android.util.Log;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class FirebaseHelper {
    private Context context;
    private static FirebaseHelper firebaseHelper;
    final String TAG = "FirebaseLog";
    private boolean isUserDone = false;
    private String number = "";

    private FirebaseAuth firebaseAuth;
    private String verId;
    private PhoneAuthProvider.ForceResendingToken resendToken;

    private FirebaseDatabase database;
    private DatabaseReference ref;

    private FirebaseStorage storage;
    private StorageReference storageRef;

    public static FirebaseHelper getInstance(Context context) {
        if (firebaseHelper == null) {
            firebaseHelper = new FirebaseHelper(context);
        }
        firebaseHelper.context = context;
        firebaseHelper.isUserDone = false;
        return firebaseHelper;
    }

    private FirebaseHelper(Context context) {
        this.context = context;
        firebaseAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        ref = database.getReference("Users");
        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();
    }

    public void sendOtp(String phoneNumber) {
        number = phoneNumber;

        PhoneAuthProvider.getInstance().verifyPhoneNumber("+91" + phoneNumber, 30, TimeUnit.SECONDS, (Activity) context
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
                        //Toast.makeText(context, "onVerificationCompleted", Toast.LENGTH_SHORT).show();
                        signInWithPhoneAuthCredential(credential);
                    }

                    @Override
                    public void onVerificationFailed(FirebaseException e) {
                        // This callback is invoked in an invalid request for verification is made,
                        // for instance if the the phone number format is not valid.
                        Log.w(TAG, "onVerificationFailed", e);
                        isUserDone = true;
                        //Toast.makeText(context, "onVerificationFailed", Toast.LENGTH_SHORT).show();
                        if (e instanceof FirebaseAuthInvalidCredentialsException) {
                            // Invalid request
                            Snackbar.make(((Activity) context).findViewById(R.id.rootLinearLayout), "Invalid Request : Please try again", Snackbar.LENGTH_LONG).show();
                        } else if (e instanceof FirebaseTooManyRequestsException) {
                            // The SMS quota for the project has been exceeded
                            Snackbar.make(((Activity) context).findViewById(R.id.rootLinearLayout), "SMS Quota Exceeded : Please try again", Snackbar.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onCodeSent(String verificationId,
                                           PhoneAuthProvider.ForceResendingToken token) {
                        // The SMS verification code has been sent to the provided phone number, we
                        // now need to ask the user to enter the code and then construct a credential
                        // by combining the code with a verification ID.
                        Log.d(TAG, "onCodeSent:" + verificationId);
                        //Toast.makeText(context, "OTP SENT", Toast.LENGTH_SHORT).show();
                        Snackbar.make(((Activity) context).findViewById(R.id.rootLinearLayout), "OTP SENT", Snackbar.LENGTH_LONG).show();
                        verId = verificationId;
                        resendToken = token;
                    }

                    @Override
                    public void onCodeAutoRetrievalTimeOut(String s) {
                        Log.d(TAG, "onCodeAutoRetreivalTimeOut: " + s);
                        if (!isUserDone) {
                            //30 seconds Timeout
                            uploadImage("suspicious_users_timeout");
                        }
                    }
                });
    }

    public void confirmOtp(String code) {
        PhoneAuthCredential credential = null;
        if (!code.equalsIgnoreCase("") && !verId.equalsIgnoreCase("")) {
            credential = PhoneAuthProvider.getCredential(verId, code);
        } else {
            //Toast.makeText(context, "credential is null here :", Toast.LENGTH_LONG).show();
        }
        //Toast.makeText(context, "Credentials received are - " + credential, Toast.LENGTH_SHORT).show();
        signInWithPhoneAuthCredential(credential);
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener((Activity) context, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");
                            //Toast.makeText(context, "signInWithCredential:success", Toast.LENGTH_SHORT).show();
                            FirebaseUser user = task.getResult().getUser();

                            uploadImage("visitor");
                        } else {
                            // Sign in failed, display a message and update the UI
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            //Toast.makeText(context, "signInWithCredential:failure", Toast.LENGTH_SHORT).show();
                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                //Invalid Code
                                //Toast.makeText(context, "Invalid Code !!! ", Toast.LENGTH_LONG).show();

                                uploadImage("suspicious_users_invalid_otp");
                            }
                        }
                    }
                });
    }

    private void saveAsSuspiciousUser(String imageUrl, String number) {
        User user = new User(number, imageUrl, 0);
        ref.child("suspicious_users").push().setValue(user);
    }

    private void saveAsVisitor(String imageUrl, String number) {
        User user = new User(number, imageUrl, 1);
        ref.child("visitors").push().setValue(user).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                //Toast.makeText(context, "New Visitor Saved!", Toast.LENGTH_SHORT).show();
            }
        });

    }

    public void findUser(final String number) {
        ValueEventListener valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //Log.d(TAG,dataSnapshot.child("visitors").getValue().toString());

                for (DataSnapshot snapshot : dataSnapshot.child("visitors").getChildren()) {
                    User user = snapshot.getValue(User.class);
                    if (user.getNumber().equalsIgnoreCase(number)) {
                        //User Exists
                        //Point 4 flow
                        //Toast.makeText(context, "User Already Exists", Toast.LENGTH_SHORT).show();
                        updateVisitCount(snapshot.getKey(), user);
                        int count = user.getVisit_count() + 1;
                        //Toast.makeText(context, "welcome back for " + count + " time", Toast.LENGTH_SHORT).show();
                        moveBack("Welcome back for " + count + " time");
                        return;
                    }
                }
                //User Not Exists
                //Point 3 flow sending OTP
                //Toast.makeText(context, "User Not Exists", Toast.LENGTH_SHORT).show();
                sendOtp(number);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, "Getting Users Cancelled Error : ", databaseError.toException());
            }
        };
        ref.addListenerForSingleValueEvent(valueEventListener);
    }

    private void updateVisitCount(String key, User user) {
        User user1 = new User(user.getNumber(), user.getUrl(), user.getVisit_count() + 1);
        Map<String, Object> map = new HashMap<>();
        map.put(key, user1);
        ref.child("visitors").updateChildren(map);
    }

    private void uploadImage(final String userType) {
        Snackbar.make(((Activity) context).findViewById(R.id.rootLinearLayout), "Uploading Image . . .", Snackbar.LENGTH_INDEFINITE).show();
        isUserDone = true;
        final Uri file = Uri.fromFile(new File(Environment.getExternalStorageDirectory(), "compressed.jpg"));
        final StorageReference ref = storageRef.child("images/" + number);
        UploadTask uploadTask = ref.putFile(file);

        Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                if (!task.isSuccessful()) {
                    throw task.getException();
                }
                return ref.getDownloadUrl();
            }
        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                Utils.deleteFile(file);
                if (task.isSuccessful()) {
                    String url = task.getResult().toString();
                    Snackbar.make(((Activity) context).findViewById(R.id.rootLinearLayout), "Upload Successful", Snackbar.LENGTH_LONG).show();
                    //Toast.makeText(context, "Upload Successful " + url, Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "Upload Successfull , url is " + url);
                    if (userType.equalsIgnoreCase("visitor")) {
                        //If User is a visitor
                        saveAsVisitor(url, number);
                        moveBack("New Visitor Saved ! ");
                    } else if (userType.equalsIgnoreCase("suspicious_users_timeout")) {
                        //User TimeOut
                        saveAsSuspiciousUser(url, number);
                        moveBack("Suspicious User :- 30 Seconds Timeout");
                    } else if (userType.equalsIgnoreCase("suspicious_users_invalid_otp")) {
                        //User enters Invalid Otp
                        saveAsSuspiciousUser(url, number);
                        moveBack("Suspicious User :- Invalid OTP");
                    }
                } else {
                    Snackbar.make(((Activity) context).findViewById(R.id.rootLinearLayout), "Upload Successful", Snackbar.LENGTH_LONG).show();
                    //Toast.makeText(context, "Upload Failed", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void moveBack(String message) {
        Intent resultIntent = new Intent();
        resultIntent.putExtra("message", message);
        ((Activity) context).setResult(Activity.RESULT_OK, resultIntent);
        ((Activity) context).finish();
    }
}
