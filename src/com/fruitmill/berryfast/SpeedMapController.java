package com.fruitmill.berryfast;

public class SpeedMapController extends Actor {

    private String TAG = "SpeedLight";
    private Actor commandcenter= null;
    private boolean pause = true;

    @Override
	protected void dispatch(Object[] message) {
		try {
			switch (message[0].toString()) {
			case "init":
                pause = true;
				break;
            case "start":
                pause = false;
                break;
			case "pause":
                pause = true;
                break;
			case "die":
                pause = true;
            }
        } catch (Exception e) {
			e.printStackTrace();
		}
	}

    public void RegisterCommandCenter(Actor cc) {
        commandcenter = cc;
    }
}
