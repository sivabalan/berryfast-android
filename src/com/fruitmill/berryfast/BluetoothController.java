package com.fruitmill.berryfast;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;
import android.widget.Toast;

import com.fruitmill.berryfast.common.Utilities;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Scanner;
import java.util.UUID;

public class BluetoothController extends Actor {

	private BluetoothAdapter myBluetoothAdapter;
    private BluetoothSocket socket = null;
    private String TAG = "SpeedLight";
    private UUID SL_UUID = UUID.fromString("059c01eb-feaa-0e13-ffc4-f5d6f3be76d9");
    private Actor commandCenter = null;
    private Scanner inputScanner = null; //God awful scanner
    private boolean connected = false;

    @Override
	protected void dispatch(Object[] message) {
		try {
			switch (message[0].toString()) {
			case "init":
				initBluetooth();
				break;
			case "connect":
				connectToPi();
				break;
			case "stop_connect":
				stopConnect();
				break;
            case "send_command":
                sendCommand((byte[])message[1]);
                break;
            case "break_connect":
                breakConnect();
                break;
            case "die":
                breakConnect();
                break;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	
	private void initBluetooth() {
		myBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if(myBluetoothAdapter == null) {
			Toast.makeText(MainActivity.getContext(),"Your device does not support Bluetooth",
					Toast.LENGTH_LONG).show();
		} else {
			switchOnBlueTooth();
            MainActivity.getContext().registerReceiver(bReceiver, new IntentFilter(BluetoothDevice.ACTION_FOUND));
            send(commandCenter, new Object[]{"bluetooth_up"});
		}
	}
	
	private void connectToPi() {
		myBluetoothAdapter.startDiscovery();
	}

    private void stopConnect() {
        myBluetoothAdapter.cancelDiscovery();
    }

    private void breakConnect() {
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        connected = false;
    }
    private void sendCommand(byte[] command) {
        try {
            socket.getOutputStream().write(command);
        } catch (IOException e) {
            Log.d(TAG, "failed to write out data, reconnecting");
            if (connected) {
                send(commandCenter, new Object[]{"connect"});
                connected = false;
            }
        }
    }

    private boolean handShake(BluetoothSocket socket) {
        try {
            String nonce = String.valueOf(System.currentTimeMillis());
            OutputStream tmpOut = socket.getOutputStream();
            tmpOut.write(nonce.getBytes());
            InputStream tmpIn = socket.getInputStream();
            nonce = Utilities.generateNonce(nonce);
            inputScanner = new Scanner(tmpIn);
            String serverNonce = inputScanner.useDelimiter("\0").hasNext() ? inputScanner.next() : "";
            Log.d(TAG, "Server Nonce : " + serverNonce);
            Log.d(TAG, "Server Nonce Length : " + serverNonce.length());
            if (serverNonce.equals(nonce)) {
                nonce = Utilities.generateNonce(serverNonce);
                tmpOut.write(nonce.getBytes());
                tmpOut.flush();
                Log.d(TAG, "Client Nonce : " + nonce);
                return true;
            }
        } catch(IOException | IllegalArgumentException e) {
            e.printStackTrace();
        }

        return false;
    }

    final BroadcastReceiver bReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            // When discovery finds a device
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Get the BluetoothDevice object from the Intent
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                Log.d(TAG, "Device Name : " + device.getAddress());

                if(device.getName() != null && device.getName().contains("fruitpie")) {
                    try {
                        if(socket == null || !socket.isConnected()) {
                            socket = device.createInsecureRfcommSocketToServiceRecord(SL_UUID);
                            socket.connect();
                            if (handShake(socket)) {
                                String slPiAddress = device.getAddress(); // TODO: Store this address for later use
                                myBluetoothAdapter.cancelDiscovery();
                                // send the command center connected status so that it can
                                // continue initializing the rest of the systems
                                connected = true;
                                send(commandCenter, new Object[]{"connected", slPiAddress});
                                stopConnect();
                            } else {
                                socket.close();
                                connectToPi();
                            }
                        }
                    } catch(IOException | IllegalArgumentException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    };

    private void switchOnBlueTooth(){
        if (!myBluetoothAdapter.isEnabled()) {
            Intent turnOnIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            MainActivity.getContext().startActivity(turnOnIntent);
            Toast.makeText(MainActivity.getContext(), "Bluetooth turned on",Toast.LENGTH_LONG).show();
        }
    }

    public void RegisterCommandCenter(Actor cc) {
        commandCenter = cc;
    }

}
