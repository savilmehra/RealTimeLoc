package com.easy_locater.realtimeloc;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import java.util.concurrent.TimeUnit;

public class SignUpAcitvity extends AppCompatActivity {
    Button btnGenerateOTP, btnSignIn;
    EditText etPhoneNumber, etOTP, countryCode;
    String phoneNumber, otp, phoneComplete;
    FirebaseAuth auth;
    PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallback;
    private String verificationCode;
    private String RegiId;
    private TextView policy;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (new TinyDB(this).getBoolean("loggedin")) {
            startActivity(new Intent(SignUpAcitvity.this, SplaceScreenActivity.class));
            finish();
        } else {
            setContentView(R.layout.sign_up);
            findViews();
            policy.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent34 = new Intent(SignUpAcitvity.this, Webview.class);
                    startActivity(intent34);
                }
            });
            StartFirebaseLogin();
            btnGenerateOTP.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (countryCode.getText().toString().trim() != null && !countryCode.getText().toString().trim().equalsIgnoreCase("")) {
                        if (etPhoneNumber.getText().toString().trim() != null && !etPhoneNumber.getText().toString().trim().equalsIgnoreCase("")) {
                            phoneComplete = countryCode.getText().toString() + etPhoneNumber.getText().toString();
                            phoneNumber = etPhoneNumber.getText().toString();
                            PhoneAuthProvider.getInstance().verifyPhoneNumber(
                                    phoneComplete,                     // Phone number to verify
                                    60,                           // Timeout duration
                                    TimeUnit.SECONDS,                // Unit of timeout
                                    SignUpAcitvity.this,        // Activity (for callback binding)
                                    mCallback);                      // OnVerificationStateChangedCallbacks
                            btnSignIn.setVisibility(View.VISIBLE);
                        } else
                            Toast.makeText(SignUpAcitvity.this, "Please Enter Phone Number", Toast.LENGTH_SHORT).show();

                    } else
                        Toast.makeText(SignUpAcitvity.this, "Please Enter the Country Code", Toast.LENGTH_SHORT).show();


                }
            });
            btnSignIn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (etOTP.getText().toString().trim() != null && !etOTP.getText().toString().trim().equalsIgnoreCase("")) {
                        otp = etOTP.getText().toString();
                        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationCode, otp);
                        SigninWithPhone(credential);
                    } else {
                        Toast.makeText(SignUpAcitvity.this, "Please Enter the OTP", Toast.LENGTH_SHORT).show();

                    }

                }
            });
        }

    }

    private void SigninWithPhone(PhoneAuthCredential credential) {
        auth.signInWithCredential(credential)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            new TinyDB(SignUpAcitvity.this).putBoolean("loggedin", true);
                            new TinyDB(SignUpAcitvity.this).putString("refreshedToken", FirebaseInstanceId.getInstance().getToken());
                            new TinyDB(SignUpAcitvity.this).putString("loginNumber", phoneNumber);
                            DatabaseReference databaseArtists = FirebaseDatabase.getInstance().getReference("User");


                            //checking if the value is provided
                            if (!TextUtils.isEmpty(phoneNumber)) {

                                //getting a unique id using push().getKey() method
                                //it will create a unique id and we will use it as the Primary Key for our Artist
                                String id = phoneNumber;
                                RegiId = FirebaseInstanceId.getInstance().getToken();
                                if (RegiId == null) {
                                    FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener(new OnSuccessListener<InstanceIdResult>() {
                                        @Override
                                        public void onSuccess(InstanceIdResult instanceIdResult) {
                                            RegiId = instanceIdResult.getToken();

                                        }
                                    });
                                }


                                //Saving the Artist
                                databaseArtists.child(id).setValue(RegiId);
                                //setting edittext to blank again
                                //displaying a success toast

                            } else {
                                //if the value is not given displaying a toast

                            }


                            startActivity(new Intent(SignUpAcitvity.this, SplaceScreenActivity.class));
                            finish();
                        } else {
                            Toast.makeText(SignUpAcitvity.this, "Incorrect OTP", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void findViews() {
        btnGenerateOTP = findViewById(R.id.btn_generate_otp);
        btnSignIn = findViewById(R.id.btn_sign_in);
        etPhoneNumber = findViewById(R.id.et_phone_number);
        countryCode = findViewById(R.id.countryCode);
        etOTP = findViewById(R.id.et_otp);
        policy = findViewById(R.id.policy);
    }

    private void StartFirebaseLogin() {
        auth = FirebaseAuth.getInstance();
        mCallback = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {
                Toast.makeText(SignUpAcitvity.this, "verification Successful", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onVerificationFailed(FirebaseException e) {
                Toast.makeText(SignUpAcitvity.this, e + "--------verification failed : Please Enter correct Country code or Phone Number", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCodeSent(String s, PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                super.onCodeSent(s, forceResendingToken);
                verificationCode = s;
                Toast.makeText(SignUpAcitvity.this, "We Have Sent You An OTP ", Toast.LENGTH_SHORT).show();
            }
        };
    }
}
