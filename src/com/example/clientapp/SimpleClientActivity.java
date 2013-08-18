package com.example.clientapp;


import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;

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
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		 super.onCreate(savedInstanceState);
		  setContentView(R.layout.activity_simple_client);
		 
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
	
	private class ConnectThread extends AsyncTask <Void, Void, Void> {

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

}


