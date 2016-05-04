package au.edu.unsw.pledge;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.widget.Toast;

import java.io.IOException;
import java.util.UUID;

/**
 * Created by Adrian on 2016/05/04.
 */
public class AcceptThread extends Thread {

    private final BluetoothServerSocket serverSocket;

    private final Context context;

    public AcceptThread(Context context) {
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
        this.context = context;
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
                try {
                    serverSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Toast.makeText(context, "Accepted Connection", Toast.LENGTH_SHORT).show();
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
