package se.alexn.have;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.util.Log;

public abstract class HAServer {
	boolean internetConnection;
	Context context;
	String remoteAdr = "sturep.se";

	
	public HAServer(Context context){
		this.context = context;
		Log.d("HAServer", "new server init");

		internetConnection = hasInternetConnection();
		if(!internetConnection){
			// No internet! 
			Log.d("HAServer", "No internetConnection...");
		}
	}
	
	public void sendRequestToServerWithResponse(String request, String file){
		if(!internetConnection) return;

		AsyncTask<String, Void, String> doRequest = new requestServerClass();
		doRequest.execute(file, request);
	}
	
	public boolean hasInternetConnection(){
		ConnectivityManager connectivity = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		if(connectivity != null){
			NetworkInfo[] info = connectivity.getAllNetworkInfo();
			if(info != null)
				for(int n = 0; n < info.length; n++){
					if(info[n].getState() == NetworkInfo.State.CONNECTED){
						return true;
					}
				}
		}
		return false;
	}

	class requestServerClass extends AsyncTask<String, Void, String>{
		
		Exception exceptionOccured;

		@Override
		protected String doInBackground(String... params) {

				String adr = remoteAdr+"/"+params[0];
				String postData = params[1]+"&os=android";

				Log.d("HAServer", "post: "+postData);

				try {
					URL url = new URL(adr);
					HttpURLConnection connection = (HttpURLConnection) url.openConnection();

					// Setup conneciton
					connection.setDoOutput(true);
					connection.setDoInput(true);

					connection.setInstanceFollowRedirects(false);
					connection.setRequestMethod("POST");
					connection.setRequestProperty("Content-type", "application/x-www-form-urlencoded"); // Must not be "multipart/form-data" or server sided hashing will fail
					connection.setRequestProperty("charset", "UTF-8");
					//connection.setRequestProperty("Content-length", Integer.toString(postData.getBytes().length));
					connection.setUseCaches(false);

					DataOutputStream dos = new DataOutputStream(connection.getOutputStream());
					dos.writeBytes(postData);
					dos.flush();
					dos.close();

					InputStreamReader isr = new InputStreamReader((InputStream) connection.getContent());
					BufferedReader br = new BufferedReader(isr);

					StringBuilder response = new StringBuilder("");
					String line = null;
					while((line = br.readLine()) != null){
						response.append(line);
					}
					br.close();
					isr.close();
					connection.disconnect();

					Log.d("HAServer", "Server says: "+response.toString());
					return response.toString();

				} catch (MalformedURLException e) {
					e.printStackTrace();
					Log.d("HAServer", "Malformed URL!");
				} catch (IOException e) {
					e.printStackTrace();
					Log.d("HAServer", "IOException!");
				}
				return null;
		}
		
		@Override
		protected void onPostExecute(String result){
			// result is the value returned by the server, how to return it??. Maybe do login and show next blaha from here?
			// Use (int) callback to find out what to do at callback.
			
			Log.d("HAServer", "Server got response");

			// Normaly overridden by caller. 
			onServerResult(result);
		}
	}
	
	/*
	 * Method called when receiving server result.
	 * */
	abstract public void onServerResult(String result);
}
