package com.example.falldetectiondnn;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetFileDescriptor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.StrictMode;
import android.provider.BaseColumns;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import org.tensorflow.lite.Interpreter;


public class MainActivity extends AppCompatActivity {

    Interpreter tflite;
    private static final int MY_PERMISSIONS_REQUEST_SEND_SMS =0 ;

    private SensorManager sensorManager;
    double ax, ay, az;   // these are the acceleration in x,y and z axis
    double gx, gy, gz;   // these are the gyroscope values in x,y and z axis
    private Sensor sensorAcc, sensorGyro;

    private LocationManager locationManager;
    public String latitude, longitude;

    EditText editTextAccX;
    EditText editTextAccY;
    EditText editTextAccZ;

    EditText editTextGyroX;
    EditText editTextGyroY;
    EditText editTextGyroZ;

    EditText editTextLatitude;
    EditText editTextLongitude;

    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ax=0.0;
        ay=0.0;
        az=0.0;
        gx=0.0;
        gy=0.0;
        gz=0.0;

        latitude = "";
        longitude = "";

        dbHelper = new DatabaseHelper(this);

        // Get sensor manager
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        // Get the default sensor of specified type
        sensorAcc = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        editTextAccX = findViewById(R.id.AccX);
        editTextAccY = findViewById(R.id.AccY);
        editTextAccZ = findViewById(R.id.AccZ);

        sensorGyro = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        editTextGyroX = findViewById(R.id.GyroX);
        editTextGyroY = findViewById(R.id.GyroY);
        editTextGyroZ = findViewById(R.id.GyroZ);

        editTextLatitude = (EditText) findViewById(R.id.Latitude);
        editTextLongitude = (EditText) findViewById(R.id.Longitude);

        try {
            tflite = new Interpreter(loadModelFile());
        }catch (Exception ex){
            ex.printStackTrace();
        }

        getLocation();
    }

    private final SensorEventListener mAccSensorListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {

            if (event.sensor.getType()==Sensor.TYPE_ACCELEROMETER){
                ax=event.values[0];
                editTextAccX.setText("X:" + String.valueOf(ax));
                ay=event.values[1];
                editTextAccY.setText("Y:" + String.valueOf(ay));
                az=event.values[2];
                editTextAccZ.setText("Z:" + String.valueOf(az));

                SaveToDB();

                try {
                    float result = doInference();
                    if (0.5 < result) {
                        SendToServer();
                        sendSMSMessage();
                    }
                } catch (Exception ex){
                    ex.printStackTrace();
                }
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
            Log.d("MY_APP", sensor.toString() + " - " + accuracy);
        }
    };

    private final SensorEventListener mGyroSensorListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {

            if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
                gx = event.values[0];
                editTextGyroX.setText("X:" + String.valueOf(gx));
                gy = event.values[1];
                editTextGyroY.setText("Y:" + String.valueOf(gy));
                gz = event.values[2];
                editTextGyroZ.setText("Z:" + String.valueOf(gz));

                SaveToDB();

                try {
                    float result = doInference();
                    if (0.5 < result) {
                        SendToServer();
                        sendSMSMessage();
                    }
                } catch (Exception ex){
                    ex.printStackTrace();
                }
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
            Log.d("MY_APP", sensor.toString() + " - " + accuracy);
        }
    };

    private MappedByteBuffer loadModelFile() throws IOException {
        AssetFileDescriptor fileDescriptor=this.getAssets().openFd("LogisticRegression.tflite");
        FileInputStream inputStream=new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel=inputStream.getChannel();
        long startOffset=fileDescriptor.getStartOffset();
        long declareLength=fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY,startOffset,declareLength);
    }

    private float doInference() {
        float[] inputVal=new float[6];
        inputVal[0] = (float) ax;
        inputVal[1] = (float) ay;
        inputVal[2] = (float) az;
        inputVal[3] = (float) gx;
        inputVal[4] = (float) gy;
        inputVal[5] = (float) gz;
        float[][] output=new float[1][1];
        if (tflite == null) {
            return 0.0f;
        }
        tflite.run(inputVal,output);
        return output[0][0];
    }

    void getLocation() {

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(MainActivity.this,
                        Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new
                    String[]{Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, new
                LocationListener() {
                    @Override
                    public void onLocationChanged(@NonNull Location location) {
                        latitude = String.valueOf(location.getLatitude());
                        longitude = String.valueOf(location.getLongitude());
                        editTextLatitude.setText(latitude);
                        editTextLongitude.setText(longitude);
                    }
                });
    }

    protected void sendSMSMessage() {
         if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.SEND_SMS)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.SEND_SMS)) {
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.SEND_SMS},
                        MY_PERMISSIONS_REQUEST_SEND_SMS);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,String permissions[], int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        String phoneNo = "8802177690";
        String message = "Patient Fell: latitude: " + latitude + ", longitude: " + longitude;
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_SEND_SMS: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    SmsManager smsManager = SmsManager.getDefault();
                    smsManager.sendTextMessage(phoneNo, null, message, null, null);
                    Toast.makeText(getApplicationContext(), "SMS sent.",
                            Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(getApplicationContext(),
                            "SMS failed, please try again.", Toast.LENGTH_LONG).show();
                    return;
                }
            }
        }
    }

    void SendToServer() {
        String message = "Patient Fell: latitude: " + latitude + ", longitude: " + longitude;
        StrictMode.ThreadPolicy policy = new
                StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        Socket socket;
        DataOutputStream dataOutputStream;
        DataInputStream dataInputStream;
        try {
            socket = new Socket("192.168.0.16", 5050);
            dataOutputStream = new DataOutputStream(socket.getOutputStream());
            dataOutputStream.writeUTF(message);
            dataInputStream = new DataInputStream(socket.getInputStream());
            Toast.makeText(MainActivity.this, dataInputStream.readUTF(), Toast.LENGTH_LONG).show();
            dataOutputStream.close();
            dataOutputStream.flush();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private  void SaveToDB() {
        float[] inputVal = new float[6];
        inputVal[0] = (float) ax;
        inputVal[1] = (float) ay;
        inputVal[2] = (float) az;
        inputVal[3] = (float) gx;
        inputVal[4] = (float) gy;
        inputVal[5] = (float) gz;

        if (dbHelper.insert(inputVal)) {
            Toast.makeText(MainActivity.this, "Inserted", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(MainActivity.this, "NOT Inserted", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (sensorAcc != null) {
            sensorManager.registerListener(mAccSensorListener, sensorAcc,
                    SensorManager.SENSOR_DELAY_NORMAL);
        }
        if (sensorGyro != null) {
            sensorManager.registerListener(mGyroSensorListener, sensorGyro,
                    SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (sensorAcc != null) {
            sensorManager.unregisterListener(mAccSensorListener);
        }
        if (sensorGyro != null) {
            sensorManager.unregisterListener(mGyroSensorListener);
        }
    }
}