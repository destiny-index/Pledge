package com.elec9782.youhancheery.logintestfirebase;

import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.firebase.client.Firebase;
import com.firebase.client.AuthData;
import com.firebase.client.*;

import java.util.Map;
import java.util.HashMap;
public class LoginActivity extends AppCompatActivity {
    protected EditText emailEditText;
    protected EditText passwordEditText;
    protected Button loginButton;
    protected TextView signUpTextView;
    protected LoginButton fbButton;
    protected TextView info;

    private CallbackManager callbackManager; //FB code, handles login requests
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //facebook SDK
        FacebookSdk.sdkInitialize(getApplicationContext());
        setContentView(R.layout.activity_login);
        //AppEventsLogger.activateApp(this);
        callbackManager = CallbackManager.Factory.create();
        //initialise widgets, including FB login button
        info = (TextView) findViewById(R.id.info);
        fbButton = (LoginButton) findViewById(R.id.fb_login_button);
        signUpTextView = (TextView)findViewById(R.id.signUpText);
        emailEditText = (EditText)findViewById(R.id.emailField);
        passwordEditText = (EditText)findViewById(R.id.passwordField);
        loginButton = (Button)findViewById(R.id.loginButton);
        final Firebase ref = new Firebase(Constants.FIREBASE_URL);
        //List<String> permissionNeeds = Arrays.asList("user_photos", "email", "public_profile");
        //loginButton.setReadPermissions(permissionNeeds);

        //for facebook logins
        fbButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {

                Intent intent = new Intent(LoginActivity.this, FB_LoggedIn.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);

            }

            @Override
            public void onCancel() {
            }
            @Override
            public void onError(FacebookException e) {
                Log.d("LoginActivity", e.getCause().toString());
            }
        });


        //for signups
        signUpTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, SignUpActivity.class);
                startActivity(intent);
            }
        });
        //for regular logins
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = emailEditText.getText().toString();
                String password = passwordEditText.getText().toString();

                if (email.isEmpty() || password.isEmpty()) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
                    builder.setMessage(R.string.login_error_message)
                            .setTitle(R.string.login_error_title)
                            .setPositiveButton(android.R.string.ok, null);
                    AlertDialog dialog = builder.create();
                    dialog.show();
                } else {
                    final String emailAddress = email;

                    //Login with an email/password combination
                    ref.authWithPassword(email, password, new Firebase.AuthResultHandler() {
                        @Override
                        public void onAuthenticated(AuthData authData) {
                            // Authenticated successfully with payload authData
                            Map<String, Object> map = new HashMap<String, Object>();
                            map.put("email", emailAddress);
                            ref.child("users").child(authData.getUid()).setValue(map);

                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                        }

                        @Override
                        public void onAuthenticationError(FirebaseError firebaseError) {
                            // Authenticated failed with error firebaseError
                            AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
                            builder.setMessage(firebaseError.getMessage())
                                    .setTitle(R.string.login_error_title)
                                    .setPositiveButton(android.R.string.ok, null);
                            AlertDialog dialog = builder.create();
                            dialog.show();
                        }
                    });
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }
}
