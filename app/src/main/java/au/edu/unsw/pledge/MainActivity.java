package au.edu.unsw.pledge;

import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "BluetoothActivity";
    private static final int REQUEST_ENABLE_DISCOVERABLE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Start Bluetooth activity
        final Button bluetoothButton = (Button) findViewById(R.id.bluetoothButton);
        bluetoothButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent newActivityIntent = new Intent(MainActivity.this, BluetoothActivity.class);
                startActivity(newActivityIntent);
            }
        });

        final Button discoverableButton = (Button) findViewById(R.id.discoverableButton);
        discoverableButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
//              Default discoverable duration is 120 seconds
                // discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
                startActivityForResult(discoverableIntent, REQUEST_ENABLE_DISCOVERABLE);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch(requestCode) {
            case REQUEST_ENABLE_DISCOVERABLE:
                if (resultCode == RESULT_OK){
                    startServer();
                } else {
                    TextView textView = (TextView) findViewById(R.id.textView);
                    textView.setText("Must allow discoverable");
                }
                break;
            default:
                Log.wtf(TAG, "Fatal Error");
                break;
        }
    }

    private void startServer() {
        TextView textView = (TextView) findViewById(R.id.textView);
        textView.setText("Starting BluetoothServer");
    }
}
