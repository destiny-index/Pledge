package au.edu.unsw.pledge;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "BluetoothActivity";
    private static final int REQUEST_ENABLE_DISCOVERABLE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Start Bluetooth activity
        final Button bluetoothButton = (Button) findViewById(R.id.bluetoothButton);
        bluetoothButton.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent newActivityIntent = new Intent(MainActivity.this, BluetoothActivity.class);
                startActivity(newActivityIntent);
            }
        });

        final Button discoverableButton = (Button) findViewById(R.id.discoverableButton);
        discoverableButton.setOnClickListener(new Button.OnClickListener() {
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
//        super.onActivityResult(requestCode, resultCode, data);

        switch(requestCode) {
            case REQUEST_ENABLE_DISCOVERABLE:
                if (resultCode != RESULT_CANCELED){
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

        // Enable discoverability also enables bluetooth, so no need to check for bluetooth again

        // Start Accept Thread
        AcceptThread t = new AcceptThread();
        t.start();
    }

    private class AcceptThread extends Thread {

        private final BluetoothServerSocket serverSocket;

        public AcceptThread() {
            // Use a temporary object that is later assigned to serverSocket
            // because serverSocket is final
            BluetoothServerSocket tmp = null;
            try {
                BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

                // MY_UUID is the app's UUID string, also used by the client code
                tmp = bluetoothAdapter.listenUsingRfcommWithServiceRecord(Constants.APP_NAME,
                        UUID.fromString(Constants.UUID));
            } catch (IOException e) {
                e.printStackTrace();
            }
            serverSocket = tmp;
        }

        @Override
        public void run() {
            super.run();

            BluetoothSocket socket = null;
            // Keep listening until exception occurs or a socket is returned
            while (true) {
                try {
                    socket = serverSocket.accept(); // Blocking call
                } catch (IOException e) {
                    e.printStackTrace();
                    break;
                }
                // If a connection was accepted
                if (socket != null) {
                    // Do work to manage the connection (in a separate thread)
                    new ServerThread(socket).start();

                    // Just do 1 connection for now
                    try {
                        serverSocket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    showToast("Accepted Connection");
                    break;
                }
            }
        }

        /** Will cancel the listening socket, and cause the thread to finish */
        public void cancel() {
            try {
                serverSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    private class ServerThread extends Thread {

        private BluetoothSocket socket = null;

        public ServerThread(BluetoothSocket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            super.run();

            try (
//                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
//                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            InputStream in = socket.getInputStream();

            ) {
                String inputLine, outputLine;
                byte[] buffer = new byte[1024];
                int bytes;
                showToast("Reading input");
                bytes = in.read(buffer);
                showToast("Write to Toast");
                showToast(buffer.toString());


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
    }

    public void showToast(final String toast) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MainActivity.this, toast, Toast.LENGTH_SHORT).show();
            }
        });
    }

}
