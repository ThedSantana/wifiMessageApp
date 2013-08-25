package com.example.clientapp;


import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Random;
import java.util.UUID;

import com.getpebble.android.kit.PebbleKit;
import com.getpebble.android.kit.util.PebbleDictionary;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.app.Activity;
import android.app.IntentService;
import android.content.Context;
import android.content.Intent;

import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class SimpleClientActivity extends Activity {

	 private EditText textField;
	 private Button button;
	 private ConnectThread comThread;
	 private Socket client;
	 private PrintWriter printwriter;
	 private String messsage;
	
	// For pebble communication
    private PebbleKit.PebbleDataReceiver dataReceiver;
	private Handler mPebbleHandler;
	
    private final Random rand = new Random();
    private final static UUID PEBBLE_APP_UUID = UUID.fromString("EC7EE5C6-8DDF-4089-AA84-C3396A11CC95");
    private final static int CMD_KEY = 0x00;
    private final static int CMD_UP = 0x01;

	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		 super.onCreate(savedInstanceState);
		  setContentView(R.layout.activity_simple_client);
		  
		  mPebbleHandler = new Handler();
		  
	      Intent pebbleListener = new Intent(this, pebbleWifiTunnel.class);
	      startService(pebbleListener);
	      
	      System.out.println("service should be started");

		 
		  textField = (EditText) findViewById(R.id.editText1); //reference to the text field
		  setButton((Button) findViewById(R.id.button1));   //reference to the send button
		  getButton().setOnClickListener(new View.OnClickListener() {
			  
			   public void onClick(View v) {
			 
			    messsage = textField.getText().toString(); //get the text message on the text field
			    textField.setText("");      //Reset the text field to blank
		    	
			    System.out.println("In onClick");
				comThread = new ConnectThread();
				comThread.execute();
		    	System.out.println("Out onClick");

			   }
			  });
	}
	

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.simple_client, menu);
		
		return true;
	}
	
    @Override
    protected void onPause() {
        super.onPause();
        // Always deregister any Activity-scoped BroadcastReceivers when the Activity is paused
        if (dataReceiver != null) {
           unregisterReceiver(dataReceiver);
           dataReceiver = null;
       }
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        // In order to interact with the UI thread from a broadcast receiver, we need to perform any updates through
        // an Android handler. For more information, see: http://developer.android.com/reference/android/os/Handler.html

        // To receive data back from the app, android
        // applications must register a "DataReceiver" to operate on the
        // dictionaries received from the watch.
        //
        // In this example, we're registering a receiver to listen for
        // button presses sent from the watch
        
        dataReceiver = new PebbleKit.PebbleDataReceiver(PEBBLE_APP_UUID) {
            @Override
            public void receiveData(final Context context, final int transactionId, final PebbleDictionary data) {
                final int cmd = data.getUnsignedInteger(CMD_KEY).intValue();

                mPebbleHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        // All data received from the Pebble must be ACK'd, otherwise you'll hit time-outs in the
                        // watch-app which will cause the watch to feel "laggy" during periods of frequent
                        // communication.
                        PebbleKit.sendAckToPebble(context, transactionId);

                        switch (cmd) {
                            // send SMS when the up button is pressed
                            case CMD_UP:
                            	messsage = "Pebble Ping";
                			    System.out.println("Received CMD_UP from Pebble");
                				//comThread = new ConnectThread();
                				//comThread.execute();
                				
                				//vibrateWatch(getApplicationContext());
                				
                                break;
                            default:
                                break;
                        }
                    }
                });
            }
        };
        PebbleKit.registerReceivedDataHandler(this, dataReceiver);
        startWatchApp(null);
    }
	
	
	
	public class ConnectThread extends AsyncTask <Void, Void, Void> {

	    public ConnectThread() {
	    }

		@Override
		protected Void doInBackground(Void... params) {

	    		System.out.println("In inBackground");
		 
			    try {
			 
			     client = new Socket("192.168.0.22", 4444);  //connect to server
			     printwriter = new PrintWriter(client.getOutputStream(),true);
			     printwriter.write(messsage);  //write the message to output stream
			 
			     printwriter.flush();
			     printwriter.close();
			     client.close();   //closing the connection
			 
			    } catch (UnknownHostException e) {
			     e.printStackTrace();
			    } catch (IOException e) {
			     e.printStackTrace();
			    }
			    
	    		System.out.println("out inBackground");

				return null;
		}
	}
	

	public Button getButton() {
		return button;
	}

	public void setButton(Button button) {
		this.button = button;
	}
	
    // Send a broadcast to launch the specified application on the connected Pebble
    public void startWatchApp(View view) {
        PebbleKit.startAppOnPebble(getApplicationContext(), PEBBLE_APP_UUID);
    }
    
    // Send a broadcast to close the specified application on the connected Pebble
    public void stopWatchApp(View view) {
        PebbleKit.closeAppOnPebble(getApplicationContext(), PEBBLE_APP_UUID);
    }
    
    public static void vibrateWatch(Context c) {
        PebbleDictionary data = new PebbleDictionary();
        data.addUint8(CMD_KEY, (byte) CMD_UP);
        PebbleKit.sendDataToPebble(c, PEBBLE_APP_UUID, data);
    }

}




