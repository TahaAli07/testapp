package com.example.redcarpetassignment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.widget.Toast;

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
    private FirebaseAuth firebaseAuth;
    private Context context;
    private static FirebaseHelper firebaseHelper;
    private String verId;
    private PhoneAuthProvider.ForceResendingToken resendToken;
    final String TAG = "FirebaseLog";
    private boolean isUserDone = false;

    private String number = "";
    private FirebaseDatabase database;
    private DatabaseReference ref;

    private FirebaseStorage storage;
    private StorageReference storageRef;


    public static FirebaseHelper getInstance(Context context) {
        if (firebaseHelper == null) {
            firebaseHelper = new FirebaseHelper(context);
        }
        firebaseHelper.context = context;
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
                        Toast.makeText(context, "onVerificationCompleted", Toast.LENGTH_SHORT).show();
                        signInWithPhoneAuthCredential(credential);
                    }

                    @Override
                    public void onVerificationFailed(FirebaseException e) {
                        // This callback is invoked in an invalid request for verification is made,
                        // for instance if the the phone number format is not valid.
                        Log.w(TAG, "onVerificationFailed", e);
                        isUserDone = true;
                        Toast.makeText(context, "onVerificationFailed", Toast.LENGTH_SHORT).show();
                        if (e instanceof FirebaseAuthInvalidCredentialsException) {
                            // Invalid request
                            Toast.makeText(context, "Invalid Request", Toast.LENGTH_SHORT).show();
                        } else if (e instanceof FirebaseTooManyRequestsException) {
                            // The SMS quota for the project has been exceeded
                            Toast.makeText(context, "SMS Quota Exceeded", Toast.LENGTH_SHORT).show();
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

                    @Override
                    public void onCodeAutoRetrievalTimeOut(String s) {
                        Log.d(TAG, "onCodeAutoRetreivalTimeOut: " + s);
                        if (!isUserDone) {
                            //User was not recognised as a visitor or a suspicious user
                            //30 seconds Timeout
                            Toast.makeText(context, "30 Seconds TimeOut: " + "s" + "i.e 30 seconds have passed", Toast.LENGTH_SHORT).show();
                            uploadImage("suspicious_users");
                        }
                    }
                });
    }

    public void confirmOtp(String code) {
        PhoneAuthCredential credential = null;
        if (!code.equalsIgnoreCase("") && !verId.equalsIgnoreCase("")) {
            credential = PhoneAuthProvider.getCredential(verId, code);
        } else {
            Toast.makeText(context, "credential is null here :", Toast.LENGTH_LONG).show();
        }
        Toast.makeText(context, "Credentials received are - " + credential, Toast.LENGTH_SHORT).show();
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
                            Toast.makeText(context, "signInWithCredential:success", Toast.LENGTH_SHORT).show();
                            FirebaseUser user = task.getResult().getUser();

                            uploadImage("visitor");
                        } else {
                            // Sign in failed, display a message and update the UI
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            Toast.makeText(context, "signInWithCredential:failure", Toast.LENGTH_SHORT).show();
                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                //Invalid Code
                                Toast.makeText(context, "Invalid Code !!! ", Toast.LENGTH_LONG).show();

                                uploadImage("suspicious_users");
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
        //remember to save visit_count initially as 1
        //show snackbar "New Visitor Saved!"
        User user = new User(number, imageUrl, 1);
        ref.child("visitors").push().setValue(user).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                Toast.makeText(context, "New Visitor Saved!", Toast.LENGTH_SHORT).show();
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
                        //User Exists "user"
                        //Point 4 flow
                        Toast.makeText(context, "User Already Exists", Toast.LENGTH_SHORT).show();
                        updateVisitCount(snapshot.getKey(), user);
                        int count = user.getVisit_count() + 1;
                        //Toast.makeText(context, "welcome back for " + count + " time", Toast.LENGTH_SHORT).show();
                        Snackbar.make(((Activity) context).getWindow().getDecorView().getRootView()
                                , "welcome back for " + count + " time"
                                , Snackbar.LENGTH_LONG).show();
                        moveBack();
                        return;
                    }
                }
                //User Not Exists
                //sending OTP flow followed Point 3
                Toast.makeText(context, "User Not Exists", Toast.LENGTH_SHORT).show();
                sendOtp(number);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "Getting Users Cancelled Error : ", databaseError.toException());
                // ...
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
        isUserDone = true;
        Uri file = Uri.fromFile(new File(Environment.getExternalStorageDirectory(), "compressed.jpg"));
        final StorageReference ref = storageRef.child("images/" + number);
        UploadTask uploadTask = ref.putFile(file);

        Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                if (!task.isSuccessful()) {
                    throw task.getException();
                }

                // Continue with the task to get the download URL
                return ref.getDownloadUrl();
            }
        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if (task.isSuccessful()) {
                    //Upload Successful
                    String url = task.getResult().toString();
                    Toast.makeText(context, "Upload Successful " + url, Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "Upload Successfull , url is " + url);
                    if (userType.equalsIgnoreCase("visitor")) {
                        //If User is a visitor
                        saveAsVisitor(url, number);
                    } else if (userType.equalsIgnoreCase("suspicious_users")) {
                        //If User is a suspicious user
                        saveAsSuspiciousUser(url, number);
                        moveBack();
                    }
                } else {
                    // Handle failures
                    // ...
                    Toast.makeText(context, "Upload Failed", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void moveBack() {
        ((Activity) context).finish();
    }
}
