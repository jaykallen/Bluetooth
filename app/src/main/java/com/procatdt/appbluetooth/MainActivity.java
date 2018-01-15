package com.procatdt.appbluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
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

public class MainActivity extends AppCompatActivity {
    private RecyclerAdapter mRecyclerAdapter;
    private List<BluetoothDevice> mDevices = new ArrayList<>();
    private BluetoothAdapter btAdapter;
    private BluetoothDevice mDevice;
    private List<BluetoothModel> mPrinters = new ArrayList<>();
    private BluetoothModel mPrinter;
    private String mMacList = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Timber.d("**************** Select Printer Activity *****************");
        mPrinter = BluetoothModel.getInstance();
        setupRecycler();
        btAdapter = BluetoothAdapter.getDefaultAdapter();
        if(btAdapter == null) {
            Timber.d("Device does not support bluetooth");
        } else {
            Timber.d("Bluetooth is supported. Turn on bluetooth adapter.");
            Intent turnOn = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(turnOn, 0);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode==0 && resultCode==RESULT_OK) {
            Timber.d("Successfully turned on Bluetooth. Scanning for devices.");
            IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
            registerReceiver(mReceiver, filter);
            btAdapter.startDiscovery();
        } else {
            Timber.d("Failed turning on Bluetooth");
        }
    }

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                mDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                Timber.d("Found " + mDevice.getName() + " at " + mDevice.getAddress());
                if (!mMacList.contains(mDevice.getAddress())) {
                    mMacList = mMacList + mDevice.getAddress() + " ";
                    BluetoothModel printer = new BluetoothModel();
                    printer.setName(mDevice.getName());
                    printer.setMacAddress(mDevice.getAddress());
                    printer.setPaired(false);
                    printer.setSelected(false);
                    mPrinters.add(printer);
                    mDevices.add(mDevice);
                    mRecyclerAdapter.notifyDataSetChanged();
                }
            }
        }
    };

    private void setupRecycler() {
        RecyclerView recyclerView = (RecyclerView)findViewById(R.id.recycler_view);
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
        public int getItemCount(){
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
            nameTextView = (TextView)itemView.findViewById(R.id.name_label);
            macTextView = (TextView)itemView.findViewById(R.id.mac_label);
        }
        public void bindRecyclerData(BluetoothModel newItem) {
            printer = newItem;
            nameTextView.setText(printer.getName());
            macTextView.setText(printer.getMacAddress());
        }
        @Override
        public void onClick(View v) {
            for (int i=0; i<mPrinters.size(); i++) {
                mPrinters.get(i).setSelected(false);
            }
            mPrinters.get(getAdapterPosition()).setSelected(true);
            mPrinter = mPrinters.get(getAdapterPosition());
            Timber.d("The selected printer is " + mPrinter.getMacAddress());
            mRecyclerAdapter.notifyDataSetChanged();
        }
    }

    public void onPairClick(View v) {
        for (int i=0; i<mDevices.size(); i++) {
            if (mDevices.get(i).getAddress().equals(mPrinter.getMacAddress())) {
                mDevice = mDevices.get(i);
                break;
            }
        }
        pairDevice(mDevice);
        Toast.makeText(this, "Device has been paired to " + mPrinter.getMacAddress(), Toast.LENGTH_LONG).show();
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
        intent.putExtra("printer", mPrinter.getMacAddress());
        startActivity(intent);
    }

    public void onExitClick(View v) {
        finish();
    }

}
