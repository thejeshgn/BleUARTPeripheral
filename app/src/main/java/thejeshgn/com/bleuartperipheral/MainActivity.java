package thejeshgn.com.bleuartperipheral;

import android.content.Intent;
import android.os.ParcelUuid;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattServer;
import android.bluetooth.BluetoothGattServerCallback;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

/**
 * It has everything that is needed by GattServer to respond
 * MIT Licnese
 * 2016 - Thejesh GN - https://hejeshgn.com
 */


public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothLeAdvertiser mBluetoothLeAdvertiser;
    private BluetoothGattServer mGattServer;

    private ArrayList<BluetoothDevice> mConnectedDevices;
    private ArrayAdapter<BluetoothDevice> mConnectedDevicesAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ListView list = new ListView(this);
        setContentView(list);

        mConnectedDevices = new ArrayList<BluetoothDevice>();
        mConnectedDevicesAdapter = new ArrayAdapter<BluetoothDevice>(this,
                android.R.layout.simple_list_item_1, mConnectedDevices);
        list.setAdapter(mConnectedDevicesAdapter);

        mBluetoothManager = (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);
        mBluetoothAdapter = mBluetoothManager.getAdapter();

    }


    protected void onResume() {
        super.onResume();
        /*
         * Make sure bluettoth is enabled
         */
        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            //Bluetooth is disabled
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivity(enableBtIntent);
            finish();
            return;
        }

        /*
         * Check for Bluetooth LE Support
         */
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, "No LE Support.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        /*
         * Check for advertising support.
         */
        if (!mBluetoothAdapter.isMultipleAdvertisementSupported()) {
            Toast.makeText(this, "No Advertising Support.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        mBluetoothLeAdvertiser = mBluetoothAdapter.getBluetoothLeAdvertiser();
        mGattServer = mBluetoothManager.openGattServer(this, mGattServerCallback);

        // If everything is okay then start
        initServer();
        startAdvertising();
    }

        /*
           * Callback handles events from the framework describing
           * if we were successful in starting the advertisement requests.
           */
        private AdvertiseCallback mAdvertiseCallback = new AdvertiseCallback() {
            @Override
            public void onStartSuccess(AdvertiseSettings settingsInEffect) {
                Log.i(TAG, "Peripheral Advertise Started.");
                postStatusMessage("GATT Server Ready");
            }

            @Override
            public void onStartFailure(int errorCode) {
                Log.w(TAG, "Peripheral Advertise Failed: "+errorCode);
                postStatusMessage("GATT Server Error "+errorCode);
            }
        };

        private Handler mHandler = new Handler();
        private void postStatusMessage(final String message) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    setTitle(message);
                }
            });
        }

        /*
     * Create the GATT server instance, attaching all services and
     * characteristics that should be exposed
     */
    private void initServer() {
        BluetoothGattService UART_SERVICE =new BluetoothGattService(UARTProfile.UART_SERVICE,
                BluetoothGattService.SERVICE_TYPE_PRIMARY);

        BluetoothGattCharacteristic TX_READ_CHAR =
                new BluetoothGattCharacteristic(UARTProfile.TX_READ_CHAR,
                        //Read-only characteristic, supports notifications
                        BluetoothGattCharacteristic.PROPERTY_READ | BluetoothGattCharacteristic.PROPERTY_NOTIFY,
                        BluetoothGattCharacteristic.PERMISSION_READ);

        //Descriptor for read notifications
        BluetoothGattDescriptor TX_READ_CHAR_DESC = new BluetoothGattDescriptor(UARTProfile.TX_READ_CHAR_DESC, UARTProfile.DESCRIPTOR_PERMISSION);
        TX_READ_CHAR.addDescriptor(TX_READ_CHAR_DESC);


        BluetoothGattCharacteristic RX_WRITE_CHAR =
                new BluetoothGattCharacteristic(UARTProfile.RX_WRITE_CHAR,
                        //write permissions
                        BluetoothGattCharacteristic.PROPERTY_WRITE , BluetoothGattCharacteristic.PERMISSION_WRITE);


        UART_SERVICE.addCharacteristic(TX_READ_CHAR);
        UART_SERVICE.addCharacteristic(RX_WRITE_CHAR);

        mGattServer.addService(UART_SERVICE);
    }


    /*
     * Initialize the advertiser
     */
    private void startAdvertising() {
        if (mBluetoothLeAdvertiser == null) return;

        AdvertiseSettings settings = new AdvertiseSettings.Builder()
                .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_BALANCED)
                .setConnectable(true)
                .setTimeout(0)
                .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_MEDIUM)
                .build();

        AdvertiseData data = new AdvertiseData.Builder()
                .setIncludeDeviceName(true)
                .addServiceUuid(new ParcelUuid(UARTProfile.UART_SERVICE))
                .build();

        mBluetoothLeAdvertiser.startAdvertising(settings, data, mAdvertiseCallback);
    }


    private void postDeviceChange(final BluetoothDevice device, final boolean toAdd) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                //This will add the item to our list and update the adapter at the same time.
                if (toAdd) {
                    if (mConnectedDevicesAdapter.getPosition(device) < 0){
                        mConnectedDevicesAdapter.add(device);
                    }

                } else {
                    mConnectedDevicesAdapter.remove(device);
                }

            }
        });
    }


        /*
     * Terminate the server and any running callbacks
     */
        private void shutdownServer() {
            //mHandler.removeCallbacks(mNotifyRunnable);

            if (mGattServer == null) return;

            mGattServer.close();
        }

//        private Runnable mNotifyRunnable = new Runnable() {
//            @Override
//            public void run() {
//                mHandler.postDelayed(this, 2000);
//            }
//        };


    /* Callback handles all incoming requests from GATT clients.
     * From connections to read/write requests.
     */
    private BluetoothGattServerCallback mGattServerCallback = new BluetoothGattServerCallback() {
        @Override
        public void onConnectionStateChange(BluetoothDevice device, int status, int newState) {
            super.onConnectionStateChange(device, status, newState);
            Log.i(TAG, "onConnectionStateChange "
                    +UARTProfile.getStatusDescription(status)+" "
                    +UARTProfile.getStateDescription(newState));

            if (newState == BluetoothProfile.STATE_CONNECTED) {
                postDeviceChange(device, true);

            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                postDeviceChange(device, false);
            }
        }

        @Override
        public void onServiceAdded(int status, BluetoothGattService service) {
            Log.d("Start", "Our gatt server service was added.");
            super.onServiceAdded(status, service);
        }


        @Override
        public void onCharacteristicReadRequest(BluetoothDevice device,
                                                int requestId,
                                                int offset,
                                                BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicReadRequest(device, requestId, offset, characteristic);
            Log.d(TAG, "READ called onCharacteristicReadRequest " + characteristic.getUuid().toString());

            if (UARTProfile.TX_READ_CHAR.equals(characteristic.getUuid())) {
                mGattServer.sendResponse(device,
                        requestId,
                        BluetoothGatt.GATT_SUCCESS,
                        0,
                        storage);
            }

        }



        @Override
        public void onCharacteristicWriteRequest(BluetoothDevice device,
                                                 int requestId,
                                                 BluetoothGattCharacteristic characteristic,
                                                 boolean preparedWrite,
                                                 boolean responseNeeded,
                                                 int offset,
                                                 byte[] value) {
            super.onCharacteristicWriteRequest(device, requestId, characteristic, preparedWrite, responseNeeded, offset, value);
            Log.i(TAG, "onCharacteristicWriteRequest "+characteristic.getUuid().toString());

            if (UARTProfile.RX_WRITE_CHAR.equals(characteristic.getUuid())) {

                //IMP: Copy the received value to storage
                storage = value;
                if (responseNeeded) {
                    mGattServer.sendResponse(device,
                            requestId,
                            BluetoothGatt.GATT_SUCCESS,
                            0,
                            value);
                    Log.d(TAG, "Received  data on "+characteristic.getUuid().toString());
                    Log.d(TAG, "Received data"+ bytesToHex(value));

                }

                //IMP: Respond
                sendOurResponse();

                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this, "We received data", Toast.LENGTH_SHORT).show();
                    }
                });




            }
        }



        @Override
        public void onNotificationSent(BluetoothDevice device, int status)
        {
            Log.d("GattServer", "onNotificationSent");
            super.onNotificationSent(device, status);
        }


        @Override
        public void onDescriptorReadRequest(BluetoothDevice device, int requestId, int offset, BluetoothGattDescriptor descriptor) {
            Log.d("HELLO", "Our gatt server descriptor was read.");
            super.onDescriptorReadRequest(device, requestId, offset, descriptor);
            Log.d("DONE", "Our gatt server descriptor was read.");
        }

        @Override
        public void onDescriptorWriteRequest(BluetoothDevice device, int requestId, BluetoothGattDescriptor descriptor, boolean preparedWrite, boolean responseNeeded, int offset, byte[] value) {
            Log.d("HELLO", "Our gatt server descriptor was written.");
            super.onDescriptorWriteRequest(device, requestId, descriptor, preparedWrite, responseNeeded, offset, value);
            Log.d("DONE", "Our gatt server descriptor was written.");

            //NOTE: Its important to send response. It expects response else it will disconnect
            if (responseNeeded) {
                mGattServer.sendResponse(device,
                        requestId,
                        BluetoothGatt.GATT_SUCCESS,
                        0,
                        value);

            }

        }

        //end of gatt server
    };







    //Send notification to all the devices once you write
    private void sendOurResponse() {
        for (BluetoothDevice device : mConnectedDevices) {
            BluetoothGattCharacteristic readCharacteristic = mGattServer.getService(UARTProfile.UART_SERVICE)
                    .getCharacteristic(UARTProfile.TX_READ_CHAR);

            byte[] notify_msg = storage;
            String hexStorage =  bytesToHex(storage);
            Log.d(TAG, "received string = "+bytesToHex(storage));


            if(hexStorage.equals("77686F616D69")){

                notify_msg = "I am echo an machine".getBytes();

            }else if(bytesToHex(storage).equals("64617465")){
                DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
                Date date = new Date();
                notify_msg = dateFormat.format(date).getBytes();

            }else{
                //TODO: Do nothing send what you received. Basically echo
            }
            readCharacteristic.setValue(notify_msg);
            Log.d(TAG, "Sending Notifications"+notify_msg);
           boolean is_notified =  mGattServer.notifyCharacteristicChanged(device, readCharacteristic, false);
            Log.d(TAG, "Notifications ="+is_notified);
        }
    }


    private byte[] storage = hexStringToByteArray("1111");

    final protected static char[] hexArray = "0123456789ABCDEF".toCharArray();

    //Helper function converts byte array to hex string
    //for priting
    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for ( int j = 0; j < bytes.length; j++ ) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }


    //Helper function converts hex string into
    //byte array
    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i+1), 16));
        }
        return data;
    }


}
