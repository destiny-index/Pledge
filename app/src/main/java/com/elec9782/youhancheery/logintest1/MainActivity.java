package com.elec9782.youhancheery.logintest1;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    Button loginButton, signupButton;
    EditText username, password;
    Context ctx = this;
    boolean loginStatus = false;
    //String getUser, getPass;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        loginButton = (Button) findViewById(R.id.button1);
        signupButton = (Button) findViewById(R.id.button2);
        username = (EditText) findViewById(R.id.editText);
        password = (EditText) findViewById(R.id.editText2);

        loginButton.setOnClickListener(this);
        signupButton.setOnClickListener(this);
    }
    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.button1: //loginButton
                DatabaseOperations DOP = new DatabaseOperations(ctx);
                Cursor CR = DOP.getInformation(DOP);
                CR.moveToFirst(); //moves pointer to the first row, so we can iterate
                do {
                    if (username.getText().toString().equals(CR.getString(0)) &&
                        password.getText().toString().equals(CR.getString(1))) {
                        loginStatus = true;
                        Toast.makeText(getApplicationContext(), "SUCCESS", Toast.LENGTH_LONG).show();
                        Intent intentLoggedIn = new Intent(MainActivity.this, loggedIn.class);
                        startActivity(intentLoggedIn);
                    } else {
                        Toast.makeText(getApplicationContext(), "FAILURE", Toast.LENGTH_LONG).show();
                    }
                } while (CR.moveToNext()); //true if next row exists, else returns false & breaks
                break;
            case R.id.button2: //signupButton
                Intent intentSignUp = new Intent(MainActivity.this, signUp.class);
                startActivity(intentSignUp);
                break;
            default:
                break;
        }
    }
}

