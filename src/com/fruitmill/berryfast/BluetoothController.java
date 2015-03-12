package com.fruitmill.berryfast;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.IntentFilter;
import android.widget.Toast;

public class BluetoothController extends Actor {

	private BluetoothAdapter myBluetoothAdapter;
	
	public BluetoothController() {
		
	}
	
	@Override
	protected void dispatch(Object[] message) {
		try {
			switch (message[0].toString()) {
			case "init":
				initBluetooth();
				break;
			case "connect":
				connectToPi(message);
				break;
			case "success_connect":
				processWords(message);
				break;
			default:
				// Forward
				send(stopWordManager, message);
				break;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	
	private void initBluetooth() {
		myBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

		if(myBluetoothAdapter == null) 
		{
			sendBtn.setEnabled(false);

			Toast.makeText(getApplicationContext(),"Your device does not support Bluetooth",
					Toast.LENGTH_LONG).show();
		} 
		else
		{
			switchOnBlueTooth();

			//registerReceiver(blueToothStatusReceiver, new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));

			registerReceiver(bReceiver, new IntentFilter(BluetoothDevice.ACTION_FOUND));
		}
	}
	
	private void connectToPi(String message) {
		
	}

}
