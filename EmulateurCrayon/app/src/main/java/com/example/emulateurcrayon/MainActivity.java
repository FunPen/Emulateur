package com.example.emulateurcrayon;


import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.support.v7.app.ActionBarActivity;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

public class MainActivity extends ActionBarActivity implements SensorEventListener {

	private SensorManager senSensorManager;
	private Sensor senAccelerometer;
	private Sensor senGyroscope;
	private long lastUpdate = 0;
	private float last_x, last_y, last_z;
	private static final int SHAKE_THRESHOLD = 100;
	private static final int REQUEST_ENABLE_BT = 1;
	
	private BroadcastReceiver mReceiver;
	private List<String> mArrayAdapter = new ArrayList<String>();

    private BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

    // MY_UUID is the app's UUID string, also used by the client code
    private static final UUID MY_UUID = UUID.fromString("00016875-0000-1000-8000-00805f9b34fb");
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		senSensorManager = (SensorManager) this.getSystemService(Context.SENSOR_SERVICE);

        List<Sensor> list = senSensorManager.getSensorList(Sensor.TYPE_ALL);
        for(Sensor sensor: list){
            Log.i("debug", "sensor : " + sensor.getName());
        }

	    senAccelerometer = senSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
	    senGyroscope = senSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
	    senSensorManager.registerListener(this, senAccelerometer , SensorManager.SENSOR_DELAY_NORMAL);
	    senSensorManager.registerListener(this, senGyroscope , SensorManager.SENSOR_DELAY_NORMAL);

        Log.i("Debug", "before AcceptThread");

        AcceptThread Server = new AcceptThread();

        Log.i("Debug", "after AcceptThread");
		
		if (mBluetoothAdapter == null) {
		    // Device does not support Bluetooth
            Log.i("Debug", "no bluetooth");
		}
		
		if (!mBluetoothAdapter.isEnabled()) {
		    Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
		    startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
		}
		
		/*Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
		// If there are paired devices
		if (pairedDevices.size() > 0) {
		    // Loop through paired devices
		    for (BluetoothDevice device : pairedDevices) {
				// Add the name and address to an array adapter to show in a ListView
		        mArrayAdapter.add(device.getName() + "\n" + device.getAddress());
		    }
		}
		
		// Create a BroadcastReceiver for ACTION_FOUND
			mReceiver = new BroadcastReceiver() {
		    public void onReceive(Context context, Intent intent) {
		        String action = intent.getAction();
		        // When discovery finds a device
		        if (BluetoothDevice.ACTION_FOUND.equals(action)) {
		            // Get the BluetoothDevice object from the Intent
		            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
		            // Add the name and address to an array adapter to show in a ListView
		            mArrayAdapter.add(device.getName() + "\n" + device.getAddress());
		        }
		    }
		};
		// Register the BroadcastReceiver
		IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
		registerReceiver(mReceiver, filter); // Don't forget to unregister during onDestroy
		*/

        Log.i("Debug", "pre_launch server");
        //Server.run();
        Log.i("Debug", "post_launch server");
	}

    private class AcceptThread extends Thread {
        private final BluetoothServerSocket mmServerSocket;

        public AcceptThread() {
            // Use a temporary object that is later assigned to mmServerSocket,
            // because mmServerSocket is final
            BluetoothServerSocket tmp = null;

            try {
                tmp = mBluetoothAdapter.listenUsingRfcommWithServiceRecord("Crayon3D", MY_UUID);
                Log.i("Debug", "init UUID");
            } catch (IOException e) { }
            mmServerSocket = tmp;
        }

        public void run() {
            BluetoothSocket socket = null;
            // Keep listening until exception occurs or a socket is returned
            while (true) {
                try {
                    socket = mmServerSocket.accept();
                } catch (IOException e) {
                    Log.i("Debug", "no accept");
                    break;
                }
                // If a connection was accepted
                if (socket != null) {
                    // Do work to manage the connection (in a separate thread)
                    manageConnectedSocket(socket);
                    Log.i("Debug", "socket accept");
                    try {
                        mmServerSocket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
                }
            }
        }

        /** Will cancel the listening socket, and cause the thread to finish */
        public void cancel() {
            try {
                mmServerSocket.close();
            } catch (IOException e) { }
        }
    }

    private void manageConnectedSocket(BluetoothSocket socket) {
    }

    @Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	protected void onPause() {
	    super.onPause();
	    senSensorManager.unregisterListener(this);
	}
	
	protected void onResume() {
	    super.onResume();
	}
	
	@Override
	public void onSensorChanged(SensorEvent sensorEvent) {
		Sensor mySensor = sensorEvent.sensor;

		final TextView textViewToChangeX = (TextView) findViewById(R.id.textViewX);
		final TextView textViewToChangeY = (TextView) findViewById(R.id.textViewY);
		final TextView textViewToChangeZ = (TextView) findViewById(R.id.textViewZ);
		
		final TextView textViewToChangeGyroX = (TextView) findViewById(R.id.TextViewGyroX);
		final TextView textViewToChangeGyroY = (TextView) findViewById(R.id.TextViewGyroY);
		final TextView textViewToChangeGyroZ = (TextView) findViewById(R.id.TextViewGyroZ);
		
		if (mySensor.getType() == Sensor.TYPE_ORIENTATION) {

            Log.i("Debug", "Gyro");

            float x = sensorEvent.values[0];
            float y = sensorEvent.values[1];
            float z = sensorEvent.values[2];

            textViewToChangeGyroX.setText("GyroX = " + Float.toString(x));
            textViewToChangeGyroY.setText("GyroY = " + Float.toString(y));
            textViewToChangeGyroZ.setText("GyroZ = " + Float.toString(z));

            }

	    else if (mySensor.getType() == Sensor.TYPE_ACCELEROMETER) {

	        float x = sensorEvent.values[0];
	        float y = sensorEvent.values[1];
	        float z = sensorEvent.values[2];
	 
	        long curTime = System.currentTimeMillis();
	 
	        if ((curTime - lastUpdate) > 50) {
	            long diffTime = (curTime - lastUpdate);
	            lastUpdate = curTime;
	 
	            float speed = Math.abs(x + y + z - last_x - last_y - last_z)/ diffTime * 10000;
	 
	            if (speed > SHAKE_THRESHOLD) {

                    Log.i("Debug", "Accelero");

	            	textViewToChangeX.setText("X = " + Float.toString(x));
	            	textViewToChangeY.setText("Y = " + Float.toString(y));
	            	textViewToChangeZ.setText("Z = " + Float.toString(z));
	            }
	            
	            last_x = x;
	            last_y = y;
	            last_z = z;
	        }
	    }
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO Auto-generated method stub
		
	}
}
