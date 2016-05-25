package au.edu.unsw.pledge.fragments;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.UUID;

import au.edu.unsw.pledge.HostActivity;
import au.edu.unsw.pledge.R;


public class FragmentHome extends Fragment{

    public FragmentHome() {
        // Required empty public constructor
    }

    // Unique UUID for this application
    private static final UUID MY_UUID = UUID.fromString("12554550-110f-11e6-a148-3e1d05defe78");

    // Request Code
    int REQUEST_ENABLE_BT = 1;  // Request code for enabling bluetooth

    // Member field
    private BluetoothAdapter mBluetoothAdapter;
    private ArrayAdapter<String> mArrayAdapter;
    private ArrayList<String> noDupDevices; // Checks for duplicates

    // Intent Filter
    IntentFilter filterFound = new IntentFilter(BluetoothDevice.ACTION_FOUND);  // filter to find devices via bluetooth
    IntentFilter filterFinished = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED); // filter to know when discovery is finished

    // View
    Button clientButton;    //client button that find bluetooth devices
    ListView bluetoothList; //list of bluetooth devices found

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = null;
        rootView = inflater.inflate(R.layout.fragment_one, container, false);

        Button hostButton = (Button) rootView.findViewById(R.id.hostButton);
        hostButton.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent hostIntent = new Intent(getActivity(), HostActivity.class);
                startActivity(hostIntent);
            }
        });
        clientButton = (Button)rootView.findViewById(R.id.clientButton);
        mArrayAdapter = new ArrayAdapter<String>(getContext(), R.layout.bluetooth_list);
        bluetoothList = (ListView)rootView.findViewById(R.id.bluetoothList);
        bluetoothList.setAdapter(mArrayAdapter);
        noDupDevices = new ArrayList<String>();
        // Register the receiver to discover devices
        getActivity().registerReceiver(mReceiver, filterFound);
        // Register for broadcasts when discovery has finished
        getActivity().registerReceiver(mReceiver, filterFinished);

        // BluetoothAdaptor
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            // Phone doesn't support bluetooth... must be some ancient history device
            // Pop up dialog
        }

        clientButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (!mBluetoothAdapter.isEnabled()) {
                    // Bluetooth is not enabled, user needs to enable it
                    Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    // Request to enable Bluetooth
                    startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
                }
                // If we're already discovering, don't start another discovery
                if (mBluetoothAdapter.isDiscovering()) {
                //Ignore
                }
                else {
                    noDupDevices.clear();
                    mArrayAdapter.clear();
                    // Request discover from BluetoothAdapter
                    Toast.makeText(getContext(), "Searching devices", Toast.LENGTH_LONG).show();
                    mBluetoothAdapter.startDiscovery();
                }
            }
        });

        bluetoothList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Cancel discovery because it's costly and we're about to connect
                mBluetoothAdapter.cancelDiscovery();

                // Get the device MAC address, which is the last 17 chars in the View
                String itemValue = (String) bluetoothList.getItemAtPosition(position);
                String MAC = itemValue.substring(itemValue.length() - 17);

                // Initiate a connection request in a separate thread
                Toast.makeText(getContext(), MAC, Toast.LENGTH_LONG).show();
            }
        });



        // Inflate the layout for this fragment
        return rootView;
    }

    // If we get an activity result...
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Is the request to enable bluetooth?
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == Activity.RESULT_OK) {
                // The user enabled bluetooth
                Toast.makeText(getContext(), "Bluetooth enabled", Toast.LENGTH_LONG).show();
            }
            if (resultCode == Activity.RESULT_CANCELED) {
                // The user refused to enable bluetooth
                Toast.makeText(getContext(), "Please enable Bluetooth", Toast.LENGTH_LONG).show();
            }
        }
    }

    // Create a BroadcastReceiver to respond to devices found and when discovery is finished
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            // When discovery finds a device
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Get the BluetoothDevice object from the Intent
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                boolean flag = false; // flag to indicate that particular device is already in the list or not
                //check if we have dups
                for(String result : noDupDevices) {
                    if (result.equals(device.getName()+ " " + device.getAddress())){
                        flag = true;
                    }
                }
                if(!flag) {
                    noDupDevices.add(device.getName() + " " + device.getAddress());
                    mArrayAdapter.add(device.getName() + "\n" + device.getAddress());
                }
            }
            // When discovery is finished
            else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                Toast.makeText(context, "Search complete", Toast.LENGTH_LONG).show();
            }
        }
    };
}
