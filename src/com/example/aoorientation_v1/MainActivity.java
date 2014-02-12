package com.example.aoorientation_v1;
		
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.TextView;
		
public class MainActivity extends Activity implements SensorEventListener{
			
	public int overallCount = 0;
	public int localOverallCount = 0;	
	public TextView overallCountText;

	Handler handler = new Handler(){
	    @Override
	    public void handleMessage(Message msg) {
	        super.handleMessage(msg);
	        Bundle data = msg.getData();
	        String val = data.getString("value");
	        Log.e("houpan","请求结果-->" + val);
	    }
	};
	
	Runnable runnable = new Runnable(){
		
	    @Override
	    public void run() {
	        //
	        // TODO: http request.
	        //

	    	while(true){
		    	if(isDataAvalible){
		    		overallCount +=1;
		    		try {
						Thread.sleep(5);
					} catch (InterruptedException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					
			        JSONObject requestJSON = new JSONObject();
			        JSONObject argumentJSON = new JSONObject();
			        try {
			            requestJSON.put("command", "ACCELEROMETER_DATA");
			        	requestJSON.put("roll", String.valueOf(rollParsed));
			        	requestJSON.put("pitch", String.valueOf(pitchParsed));
			        	requestJSON.put("yaw", String.valueOf(azimuthParsed));
			        	requestJSON.put("overallCount", String.valueOf(overallCount));
			        } catch (JSONException e) {
			            e.printStackTrace();
			        }
			        
			        Log.e("houpan",requestJSON.toString());
		    		
					final String CODEPAGE = "UTF-8";
					HttpPost httpPostRequest = new HttpPost("http://54.250.127.255:5566/COMMAND_FROM_ANDROID");

			        
					try {
				        httpPostRequest.setHeader("Accept", "application/json");            
				        httpPostRequest.setHeader("Content-type", "application/json");
//						httpPostRequest.setEntity(new StringEntity(requestJSON.toString(), CODEPAGE));
						httpPostRequest.setEntity(new StringEntity("01234567890", CODEPAGE));
					} catch (UnsupportedEncodingException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					HttpResponse resp = null;
					HttpClient httpclient = new DefaultHttpClient();
					try {
						resp = httpclient.execute(httpPostRequest);
					} catch (ClientProtocolException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
			        Message msg = new Message();
			        Bundle data = new Bundle();
			        data.putString("value","ｒｅｓｕｌｔ");
			        msg.setData(data);
//			        handler.sendMessage(msg);
		    	}	    		
	    	}

	    }
	};
	Runnable runnableCount = new Runnable(){
		
	    @Override
	    public void run() {
	        //
	        // TODO: http request.
	        //

	    	while(true){
	    			localOverallCount +=1;
		    		
		    		try {
						Thread.sleep(5);
					} catch (InterruptedException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					
	    	}	    		
    	}
	};
	

	SensorManager sensorManager;
	private Sensor sensorAccelerometer;
	private Sensor sensorMagneticField;
			
	private float[] valuesAccelerometer;
	private float[] valuesMagneticField;
			
	private float[] matrixR;
	private float[] matrixI;
	private float[] matrixValues;
			
	TextView readingAzimuth, readingPitch, readingRoll;
			
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_main);
		
		isDataAvalible = false;
	    new Thread(runnable).start();
	    new Thread(runnableCount).start();
	    


		readingAzimuth = (TextView)findViewById(R.id.azimuth);
		readingPitch = (TextView)findViewById(R.id.pitch);
		readingRoll = (TextView)findViewById(R.id.roll);
		overallCountText = (TextView)findViewById(R.id.count);
		
		sensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
		sensorAccelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		sensorMagneticField = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
					
		valuesAccelerometer = new float[3];
		valuesMagneticField = new float[3];
	
		matrixR = new float[9];
		matrixI = new float[9];
		matrixValues = new float[3];
		

		
		
	}
		
	@Override
	protected void onResume() {
		
		sensorManager.registerListener(this,
				sensorAccelerometer,
				SensorManager.SENSOR_DELAY_NORMAL);
		sensorManager.registerListener(this,
				sensorMagneticField,
				SensorManager.SENSOR_DELAY_NORMAL);
		super.onResume();
	}
		
	@Override
	protected void onPause() {
		
		sensorManager.unregisterListener(this,
				sensorAccelerometer);
		sensorManager.unregisterListener(this,
				sensorMagneticField);
		super.onPause();
	}
		
	@Override
	public void onAccuracyChanged(Sensor arg0, int arg1) {
		// TODO Auto-generated method stub
				
	}
	
	static double azimuthParsed;
	static double pitchParsed;
	static double rollParsed;
	static boolean isDataAvalible;
	
		
	@Override
	public void onSensorChanged(SensorEvent event) {
		// TODO Auto-generated method stub
		

				
		switch(event.sensor.getType()){
		case Sensor.TYPE_ACCELEROMETER:
			for(int i =0; i < 3; i++){
				valuesAccelerometer[i] = event.values[i];
			}
			break;
		case Sensor.TYPE_MAGNETIC_FIELD:
			for(int i =0; i < 3; i++){
				valuesMagneticField[i] = event.values[i];
			}
			break;
		}
				
		boolean success = SensorManager.getRotationMatrix(
							matrixR,
							matrixI,
							valuesAccelerometer,
							valuesMagneticField);
				
		if(success){
			
			double lowpassAlpha = 0.5f;
			SensorManager.getOrientation(matrixR, matrixValues);
					
			azimuthParsed = Math.toDegrees(matrixValues[0]);
			pitchParsed = Math.toDegrees(matrixValues[1]);
			rollParsed = Math.toDegrees(matrixValues[2]);

			azimuthParsed = azimuthParsed * lowpassAlpha + (azimuthParsed * (1.0 - lowpassAlpha));
			pitchParsed = pitchParsed * lowpassAlpha + (pitchParsed * (1.0 - lowpassAlpha));
			rollParsed = rollParsed * lowpassAlpha + (rollParsed * (1.0 - lowpassAlpha));
						

			readingAzimuth.setText("Azimuth: " + String.valueOf(azimuthParsed));
			readingPitch.setText("Pitch: " + String.valueOf(pitchParsed));
			readingRoll.setText("Roll: " + String.valueOf(rollParsed));
	    	overallCountText.setText("Count: " + String.valueOf(overallCount)+",   local:"+localOverallCount);

					
			isDataAvalible = true; 			
		}				
	}
}