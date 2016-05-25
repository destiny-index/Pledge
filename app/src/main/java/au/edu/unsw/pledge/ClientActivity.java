package au.edu.unsw.pledge;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.UUID;

import au.edu.unsw.pledge.preapproval.InterfaceActivity;
import au.edu.unsw.pledge.preapproval.RequestService;

public class ClientActivity extends InterfaceActivity {

    public final static String EXTRA_MAC = "au.edu.unsw.pledge.MAC";
    public final static String EXTRA_AMOUNT = "au.edu.unsw.pledge.AMOUNT";

    private final static String TAG = "ClientActivity";
    private final BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

    private String MAC = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client);

        MAC = getIntent().getStringExtra(EXTRA_MAC);
        if (MAC == null) {
            Log.wtf(TAG, "Got MAC is null");
            finish();
        }

        Intent intent = new Intent(this, RequestService.class);
        intent.putExtra(RequestService.ACTION, RequestService.GET_PREAPPROVAL);
        startService(intent);
        /**
         * will call onActivityResult with requestCode PREAPPROVAL_REQUEST
         */
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PREAPPROVAL_REQUEST && resultCode == RESULT_OK) {
            if (ppObj == null) {
                Log.wtf(TAG, "This is not suppposed to happen");
            }
            String preApprovalKey = ppObj.getPreapprovalKey();
            Toast.makeText(this, ""+preApprovalKey, Toast.LENGTH_LONG).show();
            // Pass BluetoothSocket and preApprovalKey to thread
            BluetoothDevice device = bluetoothAdapter.getRemoteDevice(MAC);

            (new ConnectThread(device, preApprovalKey)).start();
        }
    }

    private class ConnectThread extends Thread {
        private final BluetoothSocket socket;
        private final String payLoad;

        public ConnectThread(BluetoothDevice device, String payLoad) {
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
            if (payLoad == null) {
                Log.wtf(TAG, "payLoad is NULL");
            }
            this.payLoad = payLoad;
        }

        @Override
        public void run() {
            super.run();
            // This is safe to call without checking if it's running first or not
            bluetoothAdapter.cancelDiscovery();

            try {
                // Connect the device through the socket. This will block until
                // it succeeds or throws an exception
                socket.connect();
            } catch (IOException closeException) {
                showToast("Failed to connect to Host");
                try {
                    socket.close();
                } catch (IOException e) {}
                return;
            }

            // Do work to manage the connection (in a separate thread)
            try (
                    // true for auto-flush
                    PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                    BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            ) {
                showToast("I'm sending stuff");

                out.println(payLoad);
                while (!in.readLine().equals("GOT PREAPPROVAL")) {
                    out.println(payLoad);
                }

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
        public void closeSocket() {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    private void showToast(final String toast) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(ClientActivity.this, toast, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
