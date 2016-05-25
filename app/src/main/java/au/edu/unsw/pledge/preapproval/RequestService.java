package au.edu.unsw.pledge.preapproval;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.widget.Toast;

import java.util.InvalidPropertiesFormatException;

public class RequestService extends Service implements RequestThread.RequestListener {
    // Used by an intent filter to determine that we have obtained a preapproval key
    public static final String PREAPPROVAL_KEY_OBTAINED =
            "au.edu.unsw.pledge.preapproval.RequestService.PREAPPROVAL_KEY_OBTAINED";

    // Used by an intent filter to determine that we have obtained a payment
    public static final String PAYMENT_COMPLETE =
            "au.edu.unsw.pledge.preapproval.RequestService.PAYMENT_COMPLETE";

    // Used to access the preapproval key stored in an intent
    public static final String PREAPPROVAL_KEY =
            "au.edu.unsw.pledge.preapproval.RequestService.PREAPPROVAL_KEY";

    // Used to access the action that should be taken by the request thread
    public static final String ACTION =
            "au.edu.unsw.pledge.preapproval.RequestService.ACTION";

    public static final String CHARGE_AMOUNT =
            "au.edu.unsw.pledge.preapproval.RequestService.CHARGE_AMOUNT";

    // Accepted ACTION values
    public static final int GET_NONE = 0;
    public static final int GET_PREAPPROVAL = 1;
    public static final int GET_PREAPPROVED_PAYMENT = 2;

    Thread thread; // worker thread

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Toast.makeText(this, "Service Started", Toast.LENGTH_SHORT).show();
        try {

            RequestThread rt = new RequestThread(this, intent);

            thread = new Thread(rt);
            thread.start();
        } catch (InvalidPropertiesFormatException e) {
            // Can't start a request thread
            this.stopSelf();
        }
        return START_STICKY;

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        thread.interrupt();
        try {
            thread.join();
        } catch (InterruptedException ignored) {
            // Main thread should not be interrupted
        }
        Toast.makeText(this, "Service Stopped", Toast.LENGTH_SHORT).show();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void preapprovalKeyObtained(String key) {
        Intent intent = new Intent(PREAPPROVAL_KEY_OBTAINED);
        intent.putExtra(PREAPPROVAL_KEY, key);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);

        // Obtained preapproval key so we can end the service
        this.stopSelf();
    }

    @Override
    public void preapprovalPaymentCompleted() {
        Intent intent = new Intent(PAYMENT_COMPLETE);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);

        this.stopSelf();
    }

    @Override
    public void failed() {
        this.stopSelf();
    }
}
