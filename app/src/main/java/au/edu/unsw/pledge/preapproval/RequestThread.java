package au.edu.unsw.pledge.preapproval;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.InvalidPropertiesFormatException;

import javax.net.ssl.HttpsURLConnection;

import au.edu.unsw.pledge.ClientActivity;

class RequestThread implements Runnable {

    private final static String TAG = "Adrian";

    public interface RequestListener {
        void preapprovalKeyObtained(String key);

        void preapprovalPaymentCompleted();

        void failed();
    }

    private RequestListener listener;
    private int action;
    private String confirmedPreapprovalKey;

    private String amount;

    RequestThread(RequestListener listener, Intent intent) throws InvalidPropertiesFormatException {
//        Log.i(TAG, "RequestThread constructor");
        this.listener = listener;

        // Set the action field to what is stored in the intent or default to NONE
        this.action = intent.getIntExtra(RequestService.ACTION, RequestService.GET_NONE);

        // Check that we have a preapproval key to for getting a preapproved payment
        if (action == RequestService.GET_PREAPPROVED_PAYMENT) {
            if (intent.getStringExtra(RequestService.PREAPPROVAL_KEY) != null) {
                confirmedPreapprovalKey = intent.getStringExtra(RequestService.PREAPPROVAL_KEY);
                amount = intent.getStringExtra(RequestService.CHARGE_AMOUNT);
                if (amount == null) {
                    Log.wtf("Adrian", "this is not supposed to be there");
                }
            } else
                throw new InvalidPropertiesFormatException("Missing Preapproval Key to get Payment");
        }

    }

    // New exception type to differentiate between missing key and parse error
    class JSONKeyException extends JSONException {
        JSONKeyException(String message) {
            super(message);
        }
    }

    /**
     * Makes a Preapproval API call to get a Preapproval Key
     *
     * @return the String containing the Preapproval Key
     * @throws IOException if an error occurs trying to open a connection
     */
    private String getPreapprovalKey() throws IOException {
        String preapprovalKey = null;
        String response = null;

        try {
            Log.i(TAG, "in getPreApprovalKey after");
            URL url = new URL("https://svcs.sandbox.paypal.com/AdaptivePayments/Preapproval");
            HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
            Log.i(TAG, "getPreApprovalKey: got connection");
            conn.setRequestMethod("POST");

            String request = generatePreapprovalData();
            addPaypalHeaders(conn);

            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            conn.setRequestProperty("Content-Length", "" + request.length());
            conn.setDoInput(true);
            conn.setDoOutput(true);

            // Send request to paypal endpoint
            OutputStream out = conn.getOutputStream();
            out.write(request.getBytes());
            out.close();

            // Read response from paypal endpoint
            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            response = in.readLine();
            Log.v(TAG, "response: "+response);
            in.close();

            // Extract preapproval key from json response
            JSONObject jsonObject = new JSONObject(new JSONTokener(response));
            try {
                preapprovalKey = jsonObject.getString("preapprovalKey");
            } catch (JSONException e) {
                // Error in response
                throw new JSONKeyException("Missing Preapproval Key");
            }

        } catch (MalformedURLException e) {
            // this should never happen since our sandbox url is hardcoded
        } catch (JSONKeyException e) {
            // possible error with request
            System.err.println("Missing Preapproval Key: " + response);
        } catch (JSONException e) {
            // response could not be parsed as a JSON string
            System.err.println("Not Parsable JSON: " + response);
        }

        return preapprovalKey;
    }

    private Boolean getPayment() throws IOException, JSONKeyException {
        String response = null;
        Boolean success = false;
        try {
            URL url = new URL("https://svcs.sandbox.paypal.com/AdaptivePayments/Pay");
            HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();

            conn.setRequestMethod("POST");

            String request = generatePaymentData();
            addPaypalHeaders(conn);

            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            conn.setRequestProperty("Content-Length", "" + request.length());
            conn.setDoInput(true);
            conn.setDoOutput(true);

            // Send request to paypal endpoint
            OutputStream out = conn.getOutputStream();
            out.write(request.getBytes());
            out.close();
            System.out.println(request);
            // Read response from paypal endpoint
            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            response = in.readLine();
            in.close();

            // Extract exec status from json response
            JSONObject jsonObject = new JSONObject(new JSONTokener(response));
            try {
                if (jsonObject.getString("paymentExecStatus").equals("COMPLETED"))
                    success = true; // We have succeeded in getting a payment!
            } catch (JSONException e) {
                // Error in response
                throw new JSONKeyException("Missing paymentExecStatus Key");
            }
        } catch (MalformedURLException e) {
            // this should never happen since our sandbox url is hardcoded
        } catch (JSONKeyException e) {
            // possible error with request
            System.err.println("Missing paymentExecStatus Key: " + response);
            throw new JSONKeyException("Could not complete payment");
        } catch (JSONException e) {
            // response could not be parsed as a JSON string
            System.err.println("Not Parsable JSON: " + response);
        }

        return success;
    }

    private void addPaypalHeaders(HttpsURLConnection connection) {
        connection.setRequestProperty("X-PAYPAL-SECURITY-USERID", "pledge.developer-facilitator_api1.gmail.com");
        connection.setRequestProperty("X-PAYPAL-SECURITY-PASSWORD", "CUAUPT9EZ25GX6WG");
        connection.setRequestProperty("X-PAYPAL-SECURITY-SIGNATURE", "A3uoy7Xv1QExVZR76H03aZp1fRE3AxBJMnz4EePy2d9RNCBTDiNBP.4q");
        connection.setRequestProperty("X-PAYPAL-REQUEST-DATA-FORMAT", "JSON");
        connection.setRequestProperty("X-PAYPAL-RESPONSE-DATA-FORMAT", "JSON");
        connection.setRequestProperty("X-PAYPAL-APPLICATION-ID", "APP-80W284485P519543T");
    }

    private String generatePaymentData() {
        JSONObject json = new JSONObject();
        try {
            // TODO: Generate these values based on user configuration/settings
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences((Context) listener);

            JSONObject receiverList = new JSONObject();
            JSONArray receiver = new JSONArray();
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("amount", amount);
            jsonObject.put("email", prefs.getString("pref_paymentAccount", null));
            receiver.put(jsonObject);
            receiverList.put("receiver", receiver);

            json.put("actionType", "PAY");
            json.put("preapprovalKey", confirmedPreapprovalKey);
            json.put("currencyCode", "USD");
            json.put("receiverList", receiverList);
            json.put("returnUrl", "http://www.example.com/success.html");
            json.put("cancelUrl", "http://www.example.com/failure.html");
            json.put("requestEnvelope", "{\"errorLanguage\": \"en_US\",\"detailLevel\": \"ReturnAll\"}");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return json.toString();
    }

    private String generatePreapprovalData() {
        HashMap<String, String> dataMap = new HashMap<String, String>();

        Calendar calendar = Calendar.getInstance();
        Date today = calendar.getTime();
        calendar.add(Calendar.DATE, 1); // preapproval lasts for 1 day
        Date tomorrow = calendar.getTime();

        String startingDate = (new SimpleDateFormat("yyyy-MM-dd")).format(today);
        String endingDate = (new SimpleDateFormat("yyyy-MM-dd")).format(tomorrow);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences((Context) listener);

        dataMap.put("startingDate", startingDate + "Z");
        dataMap.put("endingDate", endingDate + "Z");
        dataMap.put("currencyCode", "USD");
        dataMap.put("returnUrl", "http://www.example.com/success.html");
        dataMap.put("cancelUrl", "http://www.example.com/failure.html");
        dataMap.put("maxAmountPerPayment", prefs.getString("pref_paymentLimit", null));
        dataMap.put("maxNumberOfPayments", "1");
        dataMap.put("maxTotalAmountOfAllPayments", "800.00");
        dataMap.put("requestEnvelope", "{\"errorLanguage\": \"en_US\",\"detailLevel\": \"ReturnAll\"}");

        JSONObject json = new JSONObject(dataMap);
        return json.toString();
    }

    @Override
    public void run() {
        Log.i(TAG, "In RequestThread, running");
        while (!Thread.interrupted()) {
            try {
                if (action == RequestService.GET_PREAPPROVAL) {
                    Log.i(TAG, "Getting preapproval key");
                    String preapprovalKey = getPreapprovalKey();
                    Log.i(TAG, "Preapproval key is" + preapprovalKey);

                    // we get here if the above has not thrown an exception
                    listener.preapprovalKeyObtained(preapprovalKey);
                    break;
                } else if (action == RequestService.GET_PREAPPROVED_PAYMENT) {
                    if (getPayment()) {
                        // We have succeeded in getting a payment
                        listener.preapprovalPaymentCompleted();
                        break;
                    }
                }
            } catch (JSONKeyException e) {
                Log.i(TAG, "in JSONKeyException" + e);
                e.printStackTrace();
                // Stop trying to connect to PayPal API. Our data is malformed.
                Thread.currentThread().interrupt();
                listener.failed();
            } catch (IOException e) {
                Log.i(TAG, "in IOException" + e);
                // Connection error - Try again after a wait
                try {
                    Thread.sleep(8000);
                } catch (InterruptedException ignored) {
                    // Thread got interrupted so just fall through
                }
            }
        }
    }

}