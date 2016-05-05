package com.elec9782.youhancheery.logintest1;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class signUp extends AppCompatActivity {
    String userName, userPass, userEmail, userConPass;
    Context ctx = this;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        //final EditText NAME = (EditText) findViewById(R.id.PTName);
        final EditText USER_NAME = (EditText) findViewById(R.id.PTUsername);
        final EditText USER_PASS = (EditText) findViewById(R.id.PTPassword);
        final EditText USER_EMAIL = (EditText) findViewById(R.id.PTEmail);
        final EditText USER_CONPASS = (EditText) findViewById(R.id.conPass);
        final Button submit = (Button) findViewById(R.id.submit);

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                userName = USER_NAME.getText().toString();
                userPass = USER_PASS.getText().toString();
                userEmail = USER_EMAIL.getText().toString();
                userConPass = USER_CONPASS.getText().toString();

                //should have a confirm password case here
                if (userPass.equals(userConPass)) {

                    //put everything into the database
                    DatabaseOperations DB = new DatabaseOperations(ctx);
                    DB.putInformation(DB, userName, userPass, userEmail);
                    Toast.makeText(getApplicationContext(), "REGISTRATION SUCCESS", Toast.LENGTH_LONG).show();
                    finish();
                } else {
                    //Toast failure
                    Toast.makeText(getApplicationContext(), "REGISTRATION FAILURE", Toast.LENGTH_LONG).show();
                    //empty the fields so the hint shows on next sign up
                    USER_NAME.setText("");
                    USER_PASS.setText("");
                    USER_EMAIL.setText("");
                    USER_CONPASS.setText("");

                }
            }
        });
    }
}
