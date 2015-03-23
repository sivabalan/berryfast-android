package com.fruitmill.berryfast;

import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.widget.Toast;

import com.fruitmill.berryfast.common.Commands;

public class SpeedMapController extends Actor {

    private String TAG = "SpeedLight";
    private Actor commandCenter = null;
    private boolean pause = true;
    private boolean stop = false;
    private volatile boolean hasSpeed = false;
    private volatile double speed;
    private volatile double lat;
    private volatile double lon;
    private volatile boolean hasLocation = false;

    @Override
	protected void dispatch(Object[] message) {
		try {
			switch (message[0].toString()) {
			case "init":
                pause = true;
                stop = false;
                initThread();
				break;
            case "start":
                pause = false;
                break;
			case "pause":
                pause = true;
                break;
			case "die":
                pause = true;
                stop = true;
            }
        } catch (Exception e) {
			e.printStackTrace();
		}
	}

    private Commands generateCommands(double speedDiff) {
        return new Commands();
    }

    private double currentSpeed() {
        return speed;
    }

    private double requiredSpeed() {
        return 0.0;
    }

    private void setUpSpeedMap() {
        MainActivity.getLocManager().requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                0, // time of refresh, keeping it 0 because we sadly need very close to real time
                0, // min distance, this could be more than 0 maybe? (in meters)
                locationListener);
        return;
    }

    private LocationListener locationListener = new LocationListener() {
        public void onLocationChanged(Location location) {
            lat = location.getLatitude();
            lon = location.getLongitude();
            speed = location.getSpeed();
            hasSpeed = location.hasSpeed();

            hasLocation = true;
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {}

        public void onProviderEnabled(String provider) {}

        public void onProviderDisabled(String provider) {}
    };

    private void initThread() {
        setUpSpeedMap();
        Thread thread = new Thread() {
            public void run() {
                while(!stop) {
                    if (!pause) {
                        Actor.send(
                            commandCenter,
                            new Object[]{
                                "send_command",
                                    generateCommands(
                                        currentSpeed() - requiredSpeed()).getJSON().getBytes()});
                    } else {
                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException e) {
                            continue;
                        }
                    }
                }
            }
        };
        thread.start();
    }

    public void RegisterCommandCenter(Actor cc) {
        commandCenter = cc;
    }
}
