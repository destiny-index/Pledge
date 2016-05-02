package au.edu.unsw.pledge;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import au.edu.unsw.pledge.preapproval.InterfaceActivity;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.startActivity(new Intent(this, InterfaceActivity.class));
    }
}
