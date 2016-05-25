package au.edu.unsw.pledge;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Bundle;

import java.io.IOException;
import java.util.UUID;

import au.edu.unsw.pledge.preapproval.InterfaceActivity;

public class ClientActivity extends InterfaceActivity {

    private final BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client);
        finish();
    }

    private class ConnectThread extends Thread {
        private final BluetoothSocket socket;

        public ConnectThread(BluetoothDevice device) {
            // Use a temporary object that is later assigned to the socket,
            // because socket is final
            BluetoothSocket tmp = null;

            // Get a BluetoothSocket to connect with the given BluetoothDevice
            try {
                // UUID is the app's UUIDstring, also used by the server code
                tmp = device.createRfcommSocketToServiceRecord(UUID.fromString(Constants.UUID));
            } catch (IOException e) {
                e.printStackTrace();
            }
            socket = tmp;
        }

        @Override
        public void run() {
            super.run();
            bluetoothAdapter.cancelDiscovery();
        }

        // Will cancel an in-progress connection, and close the socket
        public void closeSocket() {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
