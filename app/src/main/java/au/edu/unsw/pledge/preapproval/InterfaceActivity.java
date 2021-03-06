package au.edu.unsw.pledge.preapproval;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.paypal.android.MEP.PayPal;
import com.paypal.android.MEP.PayPalPreapproval;

import java.math.BigDecimal;

import au.edu.unsw.pledge.R;

public class InterfaceActivity extends AppCompatActivity {
    protected static final int PREAPPROVAL_REQUEST = 1;
    protected PayPal ppObj;

    /* This broadcast receiver will detect when the unconfirmed preapproval key is generated and
     * use the mobile payments library (MPL) to let the user confirm (or decline). */
    private BroadcastReceiver paypalRequestListener = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {

            Log.v("Adrian", "onReceive");

            if (intent.getAction() == RequestService.PREAPPROVAL_KEY_OBTAINED) {
                // Update the display with the preapproval key
                String preapprovalKey = intent.getStringExtra(RequestService.PREAPPROVAL_KEY);
                Log.v("Adrian", "preapprovalKey" + preapprovalKey);
                // Initialise the paypal mobile payments library (MPL)
                ppObj = PayPal.initWithAppID(context, "APP-80W284485P519543T", PayPal.ENV_SANDBOX);
                if (ppObj != null) {
                /* We have an initialized paypal MPL library so can get confirmation for the
                 * preapproval key */
                    ppObj.setPreapprovalKey(preapprovalKey);
                    PayPalPreapproval preapproval = new PayPalPreapproval();
                    preapproval.setMerchantName("Pledge");

                    Intent preapprovalIntent = ppObj.preapprove(preapproval, context);
                    startActivityForResult(preapprovalIntent, PREAPPROVAL_REQUEST);
                }
            } else if (intent.getAction() == RequestService.PAYMENT_COMPLETE) {
            }
        }
    };

    /**
     * This method will be called after the user confirms (or declines) a preapproved payment
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PREAPPROVAL_REQUEST && resultCode == RESULT_OK) {
            // The user confirmed
            if (ppObj != null) {
                ((TextView) findViewById(R.id.paypal_receiver)).append("\nApproved " + ppObj.getPreapprovalKey());
            } else {
                ((TextView) findViewById(R.id.paypal_receiver)).append("\nApproved ");
            }
            /* TODO:
             * At this point, we should send the preapproval key to the 'event' host via bluetooth
             * and let the host charge the contributers when he makes the final payment.
             */

        } else {
            // The user declined
            ((TextView) findViewById(R.id.paypal_receiver)).append("\nNot Approved");
            /* TODO:
             * At this point, we should tell the host that this user canceled/declined the
             * invitation to pledge funds
             */
        }

    }

    @Override
    protected void onPause() {
        super.onPause();

        LocalBroadcastManager.getInstance(this).unregisterReceiver(paypalRequestListener);
    }

    @Override
    protected void onResume() {
        super.onResume();

        IntentFilter intentFilter = new IntentFilter(RequestService.PREAPPROVAL_KEY_OBTAINED);
        intentFilter.addAction(RequestService.PAYMENT_COMPLETE);
        LocalBroadcastManager.getInstance(this).registerReceiver(paypalRequestListener, intentFilter);
    }
}
