package com.procatdt.appbluetooth;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

// Unfortunately, for apps targeting api 26 or higher, manifest declared broadcast receivers don't work anymore.
// https://developer.android.com/guide/components/broadcast-exceptions
// https://developer.android.com/guide/topics/connectivity/bluetooth.html
// Note: Make sure all permissions have been allowed in order for bluetooth to work correctly

public class MainActivity extends AppCompatActivity {
    private RecyclerAdapter mRecyclerAdapter;
    private List<BluetoothDevice> mDevices = new ArrayList<>();
    private List<BluetoothModel> mPrinters = new ArrayList<>();
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothDevice bluetoothDevice;
    private BluetoothModel bluetoothModel;
    private String mMacList = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Timber.d("**************** Select Printer Activity *****************");
        bluetoothModel = BluetoothModel.getInstance();
        checkPermissions();
        connectBlueTooth();
    }

    private void connectBlueTooth() {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            Timber.d("Device does not support bluetooth");
        } else {
            Timber.d("Bluetooth is supported. Turn on bluetooth adapter.");
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(intent, 0);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 0 && resultCode == RESULT_OK) {
            setupRecycler();
            Timber.d("Successfully turned on Bluetooth");
            IntentFilter filter = new IntentFilter();
            filter.addAction(BluetoothDevice.ACTION_FOUND);
            filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
            filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
            registerReceiver(mReceiver, filter);
            bluetoothAdapter.startDiscovery();
        } else {
            Timber.d("Failed turning on Bluetooth");
        }
    }

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
                Timber.d("Bluetooth Device Discovery Started");
            }
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                bluetoothDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                Timber.d("Found " + bluetoothDevice.getName() + " at " + bluetoothDevice.getAddress());
                if (!mMacList.contains(bluetoothDevice.getAddress())) {
                    mMacList = mMacList + bluetoothDevice.getAddress() + " ";
                    BluetoothModel printer = new BluetoothModel();
                    printer.setName(bluetoothDevice.getName());
                    printer.setMacAddress(bluetoothDevice.getAddress());
                    printer.setPaired(false);
                    printer.setSelected(false);
                    mPrinters.add(printer);
                    mDevices.add(bluetoothDevice);
                    mRecyclerAdapter.notifyDataSetChanged();
                }
            }
            if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                Timber.d("Bluetooth Device Discovery Finished");
                if (mDevices.size() == 0) {
                    Timber.d("No Bluetooth Devices Found");
                }
            }
        }
    };

    @Override
    public void onDestroy() {
        unregisterReceiver(mReceiver);
        super.onDestroy();
    }

    private void setupRecycler() {
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        mRecyclerAdapter = new RecyclerAdapter(mPrinters);
        recyclerView.setAdapter(mRecyclerAdapter);
    }

    private class RecyclerAdapter extends RecyclerView.Adapter<RecyclerHolder> {
        private List<BluetoothModel> printers;

        public RecyclerAdapter(List<BluetoothModel> packList) {
            printers = packList;
        }

        @Override
        public RecyclerHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_select_printer, parent, false);
            return new RecyclerHolder(view);
        }

        @Override
        public void onBindViewHolder(RecyclerHolder recyclerholder, int position) {
            BluetoothModel printer = printers.get(position);
            if (printer.isSelected()) {
                recyclerholder.itemView.setBackgroundColor(getResources().getColor(R.color.colorYellow));
            } else {
                recyclerholder.itemView.setBackgroundColor(getResources().getColor(R.color.colorWhite));
            }
            recyclerholder.bindRecyclerData(printer);
        }

        @Override
        public int getItemCount() {
            return printers.size();
        }
    }

    private class RecyclerHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private TextView nameTextView;
        private TextView macTextView;
        private BluetoothModel printer;

        public RecyclerHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            nameTextView = (TextView) itemView.findViewById(R.id.name_label);
            macTextView = (TextView) itemView.findViewById(R.id.mac_label);
        }

        public void bindRecyclerData(BluetoothModel newItem) {
            printer = newItem;
            nameTextView.setText(printer.getName());
            macTextView.setText(printer.getMacAddress());
        }

        @Override
        public void onClick(View v) {
            for (int i = 0; i < mPrinters.size(); i++) {
                mPrinters.get(i).setSelected(false);
            }
            mPrinters.get(getAdapterPosition()).setSelected(true);
            bluetoothModel = mPrinters.get(getAdapterPosition());
            Timber.d("The selected printer is " + bluetoothModel.getMacAddress());
            mRecyclerAdapter.notifyDataSetChanged();
        }
    }

    public void onPairClick(View v) {
        for (int i = 0; i < mDevices.size(); i++) {
            if (mDevices.get(i).getAddress().equals(bluetoothModel.getMacAddress())) {
                bluetoothDevice = mDevices.get(i);
                break;
            }
        }
        pairDevice(bluetoothDevice);
        Toast.makeText(this, "Device has been paired to " + bluetoothModel.getMacAddress(), Toast.LENGTH_LONG).show();
    }

    private boolean pairDevice(BluetoothDevice bluetoothDevice) {
        try {
            Timber.d("Pairing with " + bluetoothDevice.getAddress());
            return bluetoothDevice.createBond();
        } catch (Exception e) {
            Timber.e("Could not pair device:" + e.getMessage());
            return false;
        }
    }

    public void onPrintClick(View v) {
        Intent intent = new Intent(this, PrintActivity.class);
        intent.putExtra("printer", bluetoothModel.getMacAddress());
        startActivity(intent);
    }

    public void onExitClick(View v) {
        finish();
    }


    public void checkPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            String[] permissions = {
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.CAMERA
            };
            if (!hasPermissions(permissions)) {
                ActivityCompat.requestPermissions(this, permissions, 1);
            }
        }
    }

    public boolean hasPermissions(String[] permissions) {
        if (permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

}
