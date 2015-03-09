package com.fruitmill.berryfast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Scanner;
import java.util.UUID;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
//import android.view.Menu;
//import android.view.MenuItem;
import android.widget.Button;
import android.widget.Toast;

import com.fruitmill.berryfast.common.Commands;
import com.fruitmill.berryfast.common.Utilities;
import com.fruitmill.speedlight.R;

public class MainActivity extends Activity {

	private Button sendBtn;
	private Button stopBtn;
	private BluetoothAdapter myBluetoothAdapter;
	private BluetoothSocket socket = null;
	
	private String TAG = "SpeedLight";
	private int REQUEST_ENABLE_BT = 0;
	private UUID SL_UUID = UUID.fromString("059c01eb-feaa-0e13-ffc4-f5d6f3be76d9");
	private Scanner inputScanner = null; //God awful scanner
	private boolean dataSwitch = true;
	
	private String slPiAddress = "";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
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
			
		sendBtn = (Button)findViewById(R.id.sdpSend);
		sendBtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				dataSwitch = true;
				
				if (myBluetoothAdapter == null) 
				{
			          Log.d(TAG , "No bluetooth");
			    }
			
				// Get a BluetoothSocket to connect with the given BluetoothDevice
				try {
					if(socket != null && socket.isConnected())
					{
						final OutputStream tmpOut = socket.getOutputStream();
						
						Thread thread = new Thread() {
							public void run() {
								
								double[] colorVal = null;
								double[] prevColorVal = null;
								
								while(dataSwitch)
								{
									colorVal = Utilities.generateLEDColor(prevColorVal);
					
									final Commands cmds = new Commands();
									
									cmds.add("led_color", colorVal);
									
									Log.d(TAG, cmds.getJSON());
									
//									try {
//										Thread.sleep(100);
//									} catch (InterruptedException e) {
//										// TODO Auto-generated catch block
//										e.printStackTrace();
//									}
									try {
										tmpOut.write(cmds.getJSON().getBytes());
									} catch (IOException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
									if(colorVal[1] < 1) { break; }
									prevColorVal = colorVal;
								}
							}
						};
						thread.start();
						
						
						Toast.makeText(getApplicationContext(), socket.getRemoteDevice().getName(),
								Toast.LENGTH_LONG).show();
					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		  });
		
		stopBtn = (Button)findViewById(R.id.sdpSendStop);
		stopBtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Log.d(TAG, "Stopping SDP data");
				dataSwitch = false;
				
				if (myBluetoothAdapter == null) 
				{
			          Log.d(TAG , "No bluetooth");
			    }
			
				// Get a BluetoothSocket to connect with the given BluetoothDevice
				try {
					if(socket != null && socket.isConnected())
					{
						final OutputStream tmpOut = socket.getOutputStream();
						
						final Commands cmds = new Commands();
						cmds.add("led_color", new int[]{0, 0, 0});
						
						tmpOut.write(cmds.getJSON().getBytes());
						
						Toast.makeText(getApplicationContext(), socket.getRemoteDevice().getName(),
								Toast.LENGTH_LONG).show();
					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
	}

	final BroadcastReceiver blueToothStatusReceiver = new BroadcastReceiver() {
	    public void onReceive(Context context, Intent intent) {
	        String action = intent.getAction();
	    
	        if(BluetoothAdapter.ACTION_STATE_CHANGED.equals(action))
	        {
	        	int btState = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
	        	Log.d(TAG, "BT State" + String.valueOf(btState));
	        	if(btState == BluetoothAdapter.STATE_ON)
    			{
	        		myBluetoothAdapter.startDiscovery();
    			}
	        	else if(btState != BluetoothAdapter.STATE_OFF)
	        	{
	        		switchOnBlueTooth();
	        	}
	        }
	    }
	};
	
	final BroadcastReceiver bReceiver = new BroadcastReceiver() {
	    public void onReceive(Context context, Intent intent) {
	        String action = intent.getAction();
	        // When discovery finds a device
	        if (BluetoothDevice.ACTION_FOUND.equals(action)) {
	             // Get the BluetoothDevice object from the Intent
	        	 BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
	        	 Log.d(TAG , "Device Name : " + device.getAddress());
        		 
	        	 if(device.getName() != null && device.getName().contains("fruitpie"))
	        	 {
	        	 	 try {
		        		 if(socket == null || !socket.isConnected())
		        		 {
		        			 socket = device.createInsecureRfcommSocketToServiceRecord(SL_UUID);
		        			 socket.connect();
		        			 
		        			 String nonce = String.valueOf(System.currentTimeMillis());
		        			 
		        			 OutputStream tmpOut = socket.getOutputStream();
		        			 tmpOut.write(nonce.getBytes());
		        			 
		        			 InputStream tmpIn = socket.getInputStream();

		        			 nonce = Utilities.generateNonce(nonce);
		        			 
		        			 Scanner s = new Scanner(tmpIn); 
		        			 
		        			 String serverNonce = s.useDelimiter("\0").hasNext() ? s.next() : ""; 
		        			 
		        			 Log.d(TAG, "Server Nonce : " + serverNonce);

		        			 Log.d(TAG, "Server Nonce Length : " + serverNonce.length());
		        			 
		        			 if(serverNonce.equals(nonce))
		        			 {
		        				 nonce = Utilities.generateNonce(serverNonce);
		        				 
		        				 tmpOut.write(nonce.getBytes());
		        				 tmpOut.flush();
			        			 			        			 
			        			 slPiAddress = device.getAddress(); // TODO: Store this address for later use
			        			 myBluetoothAdapter.cancelDiscovery();
			        			 Log.d(TAG, "Client Nonce : " + nonce);
			        			 
		        			 }
		        			 else
		        			 {
		        				 socket.close();
		        				 myBluetoothAdapter.startDiscovery();
		        			 }
		        	
		        		 }
		        	 }
		        	 catch(IOException | IllegalArgumentException e) 
		        	 {
		        		 e.printStackTrace();
		        	 }
	        	 }
	        }
	    }
	};
	
	public void switchOnBlueTooth(){
	  if (!myBluetoothAdapter.isEnabled()) {
	     Intent turnOnIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
	     startActivityForResult(turnOnIntent, REQUEST_ENABLE_BT);
	
	     Toast.makeText(getApplicationContext(),"Bluetooth turned on",Toast.LENGTH_LONG).show();
	  }
	  else
	  {
		  myBluetoothAdapter.startDiscovery();
	  }
	}
	
//	@Override
//	public boolean onCreateOptionsMenu(Menu menu) {
//		// Inflate the menu; this adds items to the action bar if it is present.
//		getMenuInflater().inflate(R.menu.main, menu);
//		return true;
//	}
//
//	@Override
//	public boolean onOptionsItemSelected(MenuItem item) {
//		// Handle action bar item clicks here. The action bar will
//		// automatically handle clicks on the Home/Up button, so long
//		// as you specify a parent activity in AndroidManifest.xml.
//		int id = item.getItemId();
//		if (id == R.id.action_settings) {
//			return true;
//		}
//		return super.onOptionsItemSelected(item);
//	}
}
