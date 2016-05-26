package au.edu.unsw.pledge;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import au.edu.unsw.pledge.preapproval.RequestService;

public class HostActivity extends AppCompatActivity {

    private static final String TAG = "BluetoothActivity";
    private static final int REQUEST_ENABLE_DISCOVERABLE = 1;

    private ArrayAdapter<String> preapprovalKeys = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_host);

        Log.v(TAG, "Starting Host");

        preapprovalKeys = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
        ListView lv = (ListView) findViewById(R.id.listView);
        lv.setAdapter(preapprovalKeys);

        Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);

        // value of 0 means the device is always discoverable
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 0);
        startActivityForResult(discoverableIntent, REQUEST_ENABLE_DISCOVERABLE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
        switch(requestCode) {
            case REQUEST_ENABLE_DISCOVERABLE:
                if (resultCode != RESULT_CANCELED) {
                    Log.v(TAG, "Got discoverable");
                    startServer();
                } else {
                    Toast.makeText(this, "Must allow discoverable", Toast.LENGTH_SHORT);
                }
                break;
            default:
                Log.wtf(TAG, "onActivityResult returned unknown requestCode");
        }
    }

    private void startServer() {
        Toast.makeText(this, "Starting BluetoothServer", Toast.LENGTH_LONG);

        (new AcceptThread()).start();
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
                tmp = bluetoothAdapter.listenUsingRfcommWithServiceRecord(Constants.APP_NAME, UUID.fromString(Constants.UUID));
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
                    showToast("Accept Thread Broken");
                }
                if (socket != null) {
                    // Do work to manage the connection (in a separate thread)
                    new ServerThread(socket).start();
                }
            }
        }
        // Will cancel the listening socket, and cause the thread to finish
        public void cancel() {
            try {
                serverSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private class ServerThread extends Thread {

            private BluetoothSocket socket = null;

            public ServerThread(BluetoothSocket socket) {
                this.socket = socket;
            }

            @Override
            public void run() {
                try (
                    PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                    BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                ) {
                    String inputLine;
                    String inputLine2;

                    inputLine = in.readLine();
                    Log.v(TAG, "Got from client: "+inputLine);
                    out.println("GOT PREAPPROVAL");
                    showToast(inputLine);

                    inputLine2 = in.readLine();
                    Log.v(TAG, "Got from client: "+inputLine2);
                    out.println("GOT PLEDGE ID");
                    showToast(inputLine2);
                    addToList(inputLine + "; " + inputLine2);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    private void showToast(final String toast) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(HostActivity.this, toast, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private synchronized void addToList(final String key) {
        runOnUiThread(new Runnable () {
            @Override
            public void run() {
                preapprovalKeys.add(key);
            }
        });
    }

    public void payEveryone(View v) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle("Payment Total");

//         Set up the input
        final EditText input = new EditText(this);
//         Specify the type of input expected
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        builder.setView(input);

        // Set up the buttons
        builder.setPositiveButton("Accept", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                BigDecimal amount = new BigDecimal(input.getText().toString());

                Log.i(TAG, "paying Everyone");
                for (int i = 0, max = preapprovalKeys.getCount(); i < max; ++i) {
                    // + 1 because of host itself
                    Log.v(TAG, "charging"+preapprovalKeys.getItem(i).split(";")[0] +" "+ amount.divide(new BigDecimal(max + 1), BigDecimal.ROUND_DOWN));
                    getPaymentFromKey(preapprovalKeys.getItem(i).split(";")[0], amount.divide(new BigDecimal(max + 1), BigDecimal.ROUND_DOWN));
                }
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.show();
    }
    private void getPaymentFromKey(String preapprovalKey, BigDecimal chargeAmount) {
        Intent intent = new Intent(getApplicationContext(), RequestService.class);
        intent.putExtra(RequestService.ACTION, RequestService.GET_PREAPPROVED_PAYMENT);
        intent.putExtra(RequestService.PREAPPROVAL_KEY, preapprovalKey);
        intent.putExtra(RequestService.CHARGE_AMOUNT, chargeAmount.toString());
        startService(intent);
    }
}
