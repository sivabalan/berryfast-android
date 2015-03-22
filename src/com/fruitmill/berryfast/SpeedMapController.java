package com.fruitmill.berryfast;

import com.fruitmill.berryfast.common.Commands;

public class SpeedMapController extends Actor {

    private String TAG = "SpeedLight";
    private Actor commandCenter = null;
    private boolean pause = true;
    private boolean stop = false;

    @Override
	protected void dispatch(Object[] message) {
		try {
			switch (message[0].toString()) {
			case "init":
                pause = true;
                stop = false;
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
        return 0.0;
    }

    private double requiredSpeed() {
        return 0.0;
    }

    private void setUpSpeedMap() {
        return;
    }

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
