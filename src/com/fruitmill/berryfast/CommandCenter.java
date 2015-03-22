package com.fruitmill.berryfast;

import android.util.Log;
public class CommandCenter extends Actor {

    private String TAG = "SpeedLight";
    private Actor bluetoothController= null;
    private Actor SpeedMapController = null;
    private boolean connected = false;

    @Override
	protected void dispatch(Object[] message) {
		try {
			switch (message[0].toString()) {
			case "init":
                Log.d(TAG, "cc init");
                send(bluetoothController, new Object[]{"init"});
                send(SpeedMapController, new Object[]{"init"});
                connected = false;
				break;
            case "bluetooth_up":
                Log.d(TAG, "cc bup");
                send(bluetoothController, new Object[]{"connect"});
                break;
			case "connect":
                Log.d(TAG, "cc connect");
                connected = false;
                send(SpeedMapController, new Object[]{"pause"});
				send(bluetoothController, new Object[]{"connect"});
				break;
			case "connected":
                Log.d(TAG, "cc connected");
                connected = true;
                send(SpeedMapController, new Object[]{"start"});
                break;
            case "send_command":
                if (connected) {
                    send(bluetoothController, message);
                }
                break;
            case "stop_connect":
                connected = false;
                send(SpeedMapController, new Object[]{"pause"});
                send(bluetoothController, message);
                break;
            case "break_connect":
                connected = false;
                send(SpeedMapController, new Object[]{"pause"});
                send(bluetoothController, message);
                break;
            case "die":
                connected = false;
                send(bluetoothController, message);
                send(SpeedMapController, message);
                break;
            }
        } catch (Exception e) {
			e.printStackTrace();
		}
	}

    public void RegisterControllers(Actor blueController, Actor SpeedMap) {
        bluetoothController = blueController;
        SpeedMapController = SpeedMap;
    }
}
