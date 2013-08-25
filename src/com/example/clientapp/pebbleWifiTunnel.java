package com.example.clientapp;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Random;
import java.util.UUID;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.widget.Toast;

import com.getpebble.android.kit.PebbleKit;
import com.getpebble.android.kit.util.PebbleDictionary;

public class pebbleWifiTunnel extends Service {
	
	// For pebble communication
	private PebbleKit.PebbleDataReceiver dataReceiver;
	private Handler mPebbleHandler;
	private Socket client;
	private PrintWriter printwriter;
	private String messsage;

	private final static UUID PEBBLE_APP_UUID = UUID.fromString("EC7EE5C6-8DDF-4089-AA84-C3396A11CC95");
	private final static int CMD_KEY = 0x00;
	private final static int CMD_UP = 0x01;

	public pebbleWifiTunnel() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void onCreate() {
		System.out.println("Creating Service");

		mPebbleHandler = new Handler();
		
		dataReceiver = new PebbleKit.PebbleDataReceiver(PEBBLE_APP_UUID) {
			@Override
			public void receiveData(final Context context,
				final int transactionId, final PebbleDictionary data) {
				final int cmd = data.getUnsignedInteger(CMD_KEY).intValue();

				mPebbleHandler.post(new Runnable() {
					@Override
					public void run() {
						// All data received from the Pebble must be ACK'd,
						// otherwise you'll hit time-outs in the
						// watch-app which will cause the watch to feel
						// "laggy" during periods of frequent
						// communication.
						PebbleKit.sendAckToPebble(context, transactionId);

						switch (cmd) {
						// send SMS when the up button is pressed
						case CMD_UP:
							messsage = "Pebble Ping";
							System.out.println("Received CMD_UP from Pebble");
				    		System.out.println("In Service Background");
				   		 
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
						    
				    		System.out.println("Out Service Background");

							break;
						default:
							break;
						}
					}
				});
			}
		};
		PebbleKit.registerReceivedDataHandler(this, dataReceiver);

	}

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Toast.makeText(this, "service starting", Toast.LENGTH_SHORT).show();
		
		System.out.println("Starting Service");

		// If we get killed, after returning from here, restart
		return START_STICKY;
	}

	@Override
	public void onDestroy() {

		if (dataReceiver != null) {
			unregisterReceiver(dataReceiver);
			dataReceiver = null;
		}
		Toast.makeText(this, "service done", Toast.LENGTH_SHORT).show();
	}
}
