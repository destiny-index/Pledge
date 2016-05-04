package au.edu.unsw.pledge;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Set;
import java.util.UUID;

public class BluetoothActivity extends AppCompatActivity {

    private static final String TAG = "BluetoothActivity";
    private static final int REQUEST_ENABLE_BT = 1;

    private final BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch(requestCode) {
            case REQUEST_ENABLE_BT:
                if (resultCode == RESULT_OK){
                    queryPairedDevices();
                } else {
                    TextView textView = (TextView) findViewById(R.id.blueToothText);
                    textView.setText("Must enable bluetooth");
                }
                break;
            default:
                Log.wtf(TAG, "Fatal Error");
                break;
        }
    }

    public void onScanBluetooth(View v) {
        TextView textView = (TextView) findViewById(R.id.blueToothText);

        if (bluetoothAdapter == null) {
            textView.setText("Device does not support bluetooth");
            return;
        }

        if (!bluetoothAdapter.isEnabled()) {
            Intent enableBluetoothIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBluetoothIntent, REQUEST_ENABLE_BT);
        } else {
            queryPairedDevices();
        }
    }

    private void queryPairedDevices() {
        TextView textView = (TextView) findViewById(R.id.blueToothText);
        textView.setText("Scanning for bluetooth devices");

        // Querying paired devices
        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);

        ListView listView = (ListView) findViewById(R.id.listView);
        listView.setAdapter(arrayAdapter);
        listView.setOnItemClickListener(new ListView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                // Get the Device MAC address, which is the last 17 chars in the view
                String text = (String) parent.getItemAtPosition(position);
                String MAC = text.substring(text.length() - 17);
                BluetoothDevice device = bluetoothAdapter.getRemoteDevice(MAC);

                Toast.makeText(BluetoothActivity.this, "MAC:" + MAC, Toast.LENGTH_SHORT).show();
                ConnectThread t = new ConnectThread(device);
                t.start();
            }
        });

        if (pairedDevices.size() > 0) {
            // Loop through paired devices
            for (BluetoothDevice device: pairedDevices) {
                // Add the name and address to an array adapter to show in a ListView
                arrayAdapter.add(device.getName() + "\n" + device.getAddress());
            }
            textView.setText("Found paired device(s)");
        } else {
            textView.setText("No paired device\nDiscover device?");
        }

    }

    private class ConnectThread extends Thread {
        private final BluetoothSocket socket;
//        private final BluetoothDevice device;

        public ConnectThread(BluetoothDevice device) {
            // Use a temporary object that is later assigned to socket,
            // because socket is final
            BluetoothSocket tmp = null;
//            this.device = device;

            // Get a BluetoothSocket to connect with the given BluetoothDevice
            try {
                // UUID is the app's UUID string, also used by the server code
                tmp = device.createRfcommSocketToServiceRecord(UUID.fromString(Constants.UUID));
            } catch (IOException e) {
                e.printStackTrace();
            }
            socket = tmp;
        }

        @Override
        public void run() {
            super.run();
            // Cancel discovery because it will slow down the connection
            bluetoothAdapter.cancelDiscovery();

            try {
                // Connect the device through the socket. This will block until it
                // succeeds or throws an exception
                socket.connect();
            } catch (IOException connectException) {
                // Unable to connect; close the socket and get out
                try {
                    socket.close();
                } catch (IOException closeException) {
                    return;
                }
            }
            // Do work to manage the connection (in a separate thread)
            try (
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

//                OutputStream out = socket.getOutputStream();

            ) {
                showToast("Writing Hello");
//                out.write("Hello From Adrian".getBytes());
                out.println("Helllo this is something else");
                showToast("Done Hello");

            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        // Will cancel an in-progress connection, and close the socket
        public void cancel() {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void showToast(final String toast) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(BluetoothActivity.this, toast, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
