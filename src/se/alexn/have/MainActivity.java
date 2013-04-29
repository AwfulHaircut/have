package se.alexn.have;

import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class MainActivity extends Activity {
	EditText textBox;
	TextView tv;
	Button checkIn;
	LocationManager lm;
	
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		// Get reffs
		checkIn = (Button) findViewById(R.id.checkinButton);
		textBox = (EditText) findViewById(R.id.commentBox);

		// Set listeners
		checkIn.setOnClickListener(click);
		
		lm = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
	}
	
	View.OnClickListener click = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			String message = textBox.getText().toString();
			lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, locationListener);
		}
	};
	
	/*
	 * Upload data to server and stop listening for GPS!
	 * */
	private void uploadAndClose(){
		// Remove 
		lm.removeUpdates(locationListener);
		
		HAServer server = new HAServer(this){
			@Override
			public void onServerResult(String result) {
				// Get server data!
			}
		};
		server.sendRequestToServerWithResponse("", "");
	}
	
	LocationListener locationListener = new LocationListener(){

		@Override
		public void onLocationChanged(Location location) {
			// New Location!
			Log.d("location", "Got new location at: lat:"+location.getLatitude()+" long:"+location.getLongitude());
			uploadAndClose();
		}
		
		@Override
		public void onProviderDisabled(String provider) {}
		@Override
		public void onProviderEnabled(String provider) {}
		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {}
		
	};
	
}
