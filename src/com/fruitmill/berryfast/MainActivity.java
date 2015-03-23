package com.fruitmill.berryfast;

import android.app.Activity;
import android.content.Context;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.fruitmill.berryfast.common.Commands;
import com.fruitmill.berryfast.common.Utilities;
import com.fruitmill.speedlight.R;

public class MainActivity extends Activity {
    private static Context context;
    private static LocationManager locationManager;

	private Button sendBtn = null;
	private Button stopBtn = null;
    private boolean dataSwitch = true;
    private String TAG = "SpeedLight";
    private CommandCenter commandCenter;

    @Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		context = getApplicationContext();
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
		// create command center
        // create bluetooth listener
        // create speedlimit checker
        // init command center
        // send connect to command center
        commandCenter = new CommandCenter();
        BluetoothController blcon = new BluetoothController();
        SpeedMapController spmcon = new SpeedMapController();
        blcon.RegisterCommandCenter(commandCenter);
        spmcon.RegisterCommandCenter(commandCenter);
        commandCenter.RegisterControllers(blcon, spmcon);
        Actor.send(commandCenter, new Object[]{"init"});

        sendBtn = (Button)findViewById(R.id.sdpSend);
        stopBtn = (Button)findViewById(R.id.sdpSendStop);
        sendBtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				dataSwitch = true;
                Thread thread = new Thread() {
                    public void run() {
                        double[] colorVal = null;
                        double[] prevColorVal = null;

                        while(dataSwitch) {
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
                            Actor.send(commandCenter,
                                    new Object[]{"send_command",
                                            cmds.getJSON().getBytes()});
                            if(colorVal[1] < 1) { break; }
                            prevColorVal = colorVal;
                        }
                    }
                };
                thread.start();
			}
		  });

        stopBtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Log.d(TAG, "Stopping SDP data");
				dataSwitch = false;
				final Commands cmds = new Commands();
				cmds.add("led_color", new int[]{0, 0, 0});
				Actor.send(commandCenter,
                        new Object[]{"send_command", cmds.getJSON().getBytes()});
			}
		});
	}

    public static Context getContext() {
        return context;
    }
    public static LocationManager getLocManager() {
        return locationManager;
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
