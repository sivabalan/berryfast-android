package com.fruitmill.speedlight.common;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

public class Commands {

	private JSONObject cmdObject;
	
	public Commands() {
		cmdObject = new JSONObject();
	}
	
	public void add(String cmd, Object value) {
		try {
			JSONArray ja = new JSONArray(value);
			Log.d("SpeedLight",ja.toString());
			cmdObject.put(cmd, ja);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	public String getJSON() {
		return cmdObject.toString()+";";
	}
}
