package com.example.panut.presencereceiver;

import android.app.Activity;
import android.arch.lifecycle.ViewModelProviders;
import android.bluetooth.BluetoothDevice;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;

import no.nordicsemi.android.nrftoolbox.profile.BleManager;
import no.nordicsemi.android.nrftoolbox.profile.BleManagerCallbacks;
import no.nordicsemi.android.nrftoolbox.profile.BleProfile;

/**
 * Created by Panut on 15-Jan-18.
 */


public class SensorConnection extends BleProfile implements SignalManagerCallbacks {

//    static public class ConnectionViews {
//        public TextView deviceNameView;
//        public TextView dataView;
//        public TextView fpsView;
//        public Button disconnectButton;
//    }

    public interface SensorConnectionListener {
        void onSensorConnected(SensorConnection connection);
        void onSensorDisconnected(SensorConnection connection);
    }

    private static int uniqueID = 1;

    private int count = -1;
    private long previousCountResetTime = 0;
    private long previousFrameTime = 0;

    private final Handler timeHandler = new Handler();
//    private ConnectionViews mConnectionUI;
    private View mRootView;
    private TextView mDeviceNameView;
    private TextView mDataView;
    private TextView mFpsView;
    private Button mDisconnectButton;

    private AudioViewModel mAudioViewModel;

    private SensorConnectionListener mConnectionListener;

    public int id;


    public static SensorConnection createConnection(final BluetoothDevice device, final Activity context) {
        SensorConnection connection = new SensorConnection(context);

        connection.connect(device);

        return connection;
    }

    private void createView(){
        mRootView = View.inflate(mContext, R.layout.device_connection_list_item, null);
        mDeviceNameView = mRootView.findViewById(R.id.device_name);
        mDataView = mRootView.findViewById(R.id.device_data);
        mFpsView = mRootView.findViewById(R.id.fps);
        mDisconnectButton = mRootView.findViewById(R.id.disconnect_button);

        mDisconnectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                disconnect();
            }
        });
    }

    public void setConnectionListener(SensorConnectionListener connectionListener) {
        this.mConnectionListener = connectionListener;
    }

    public SensorConnection(Activity context) {
        super(context);

        id = uniqueID++;

        createView();
        mAudioViewModel = ViewModelProviders.of((FragmentActivity)mContext).get(AudioViewModel.class);
    }

    public View getView() {
        return mRootView;
    }

    @Override
    public void onDeviceDisconnected(BluetoothDevice device) {
        super.onDeviceDisconnected(device);

        Log.d("Monitor", "Device Disconnected : " + device.getName());

        timeHandler.removeCallbacksAndMessages(null);
        mConnectionListener.onSensorDisconnected(this);
        mAudioViewModel.removeBuffer(this);
    }

    public void keepMonitorLog(){

        timeHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                SimpleDateFormat timeFormat =  new SimpleDateFormat("hh:mm:ss");

                Log.d("Monitor", timeFormat.format(new Date()));
                keepMonitorLog();
            }
        }, 3000);
    }

    @Override
    public void onDeviceConnected(BluetoothDevice device) {
        super.onDeviceConnected(device);

        keepMonitorLog();
        Log.d("Monitor", "Device Connected : " + device.getName());

        mContext.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mDeviceNameView.setText(device.getName());
            }
        });

        mAudioViewModel.initializeBuffer(this);
        mConnectionListener.onSensorConnected(this);
    }

//    @Nullable
//    @Override
//    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
//        View v = inflater.inflate(R.layout.fragment_sensor_connection, container);
//
//        _accelTextview = v.findViewById(R.id.accelTextView);
//        _fpsView = v.findViewById(R.id.fps_view);
//
//        return v;
//    }

    @Override
    protected BleManager<? extends BleManagerCallbacks> initializeManager()
    {
        final SignalManager signalManager = SignalManager.getSignalManager(mContext.getApplicationContext());
        signalManager.setGattCallbacks(this);
////        signalManager.SetAccelTextView(_accelTextview);

        return signalManager;
    }


    private short dataCount = 0;
    @Override
    public void onAccelDataRead(AccelData data) {
        long currentTime = System.nanoTime();
        float timePeriod = (currentTime - previousFrameTime) / 1000000000.f; // in second

        previousFrameTime = currentTime;

        String accel_str;
        accel_str = data.value[0] + " - " + data.value[data.value.length - 1];
//        accel_str = pData[0] + " - " + pData[pData.length - 1] + " : (" + minValue + " - " + maxValue + ")";


        mAudioViewModel.writeAudioBuffer(data.value, this);

        float fps = (float)count/(currentTime - previousCountResetTime) * 1000000000;
        // fps purpose
        if(count > 10 && mRootView != null){
            mContext.runOnUiThread(() -> {
                mDataView.setText(accel_str);
                mFpsView.setText(fps + "");
            });
        }

        if(count < 0 || count > 100) {
            count = 0;
            previousCountResetTime = System.nanoTime();
        }
        count++;
    }
}
