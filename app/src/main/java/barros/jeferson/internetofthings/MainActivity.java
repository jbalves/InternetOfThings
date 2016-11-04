package barros.jeferson.internetofthings;
// -- https://github.com/eclipse/paho.mqtt.android

import android.Manifest;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttPersistenceException;

import java.io.UnsupportedEncodingException;

public class MainActivity extends AppCompatActivity {

    TextView aX, aY, aZ, gX, gY, gZ, temp, umid, lum, press;

    SensorManager mSensorManager;

    Sensor acelerometro, giroscopio, temperatura, humidade, luminosidade, pressao;

    SensorEventListener acelerometroListener, giroscopioListener, temperaturaListener,
            humidadeListener, luminosidadeListener, pressaoListener;

    public final static String TAG="Sensor";

    MqttAndroidClient client;

    String clientId;

    public final int QOS = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        aX = (TextView) findViewById(R.id.acel_x);
        aY = (TextView) findViewById(R.id.acel_y);
        aZ = (TextView) findViewById(R.id.acel_z);

        gX = (TextView) findViewById(R.id.gir_x);
        gY = (TextView) findViewById(R.id.gir_y);
        gZ = (TextView) findViewById(R.id.gir_z);

        temp = (TextView) findViewById(R.id.temp);
        umid = (TextView) findViewById(R.id.umid);
        lum = (TextView) findViewById(R.id.lum);
        press = (TextView) findViewById(R.id.press);

        clientId = MqttClient.generateClientId();

        client = new MqttAndroidClient(this.getApplicationContext(),"tcp://iot.eclipse.org:1883",clientId);

        try {
            IMqttToken token = client.connect();
            token.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    // We are connected
                    Log.d(TAG, "onSuccess");

                    String payload = "Sender " + clientId + " está online!";
                    byte[] encodedPayload;

                    try {
                        encodedPayload = payload.getBytes("UTF-8");

                        MqttMessage message = new MqttMessage(encodedPayload);
                        client.publish("/ocean/sensores/debug", message);

                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    } catch (MqttPersistenceException e) {
                        e.printStackTrace();
                    } catch (MqttException e) {
                        e.printStackTrace();
                    }

                    enviarDados();
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    // Something went wrong e.g. connection timeout or firewall problems
                    Log.d(TAG, "onFailure");

                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        if (mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null){
            acelerometro = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        }else {
            Log.i(TAG, "Não tem TYPE_ACCELEROMETER");
        }

        if (mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE) != null){
            giroscopio = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        }else {
            Log.i(TAG, "Não tem TYPE_GYROSCOPE");
        }

        if (mSensorManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE) != null){
            temperatura = mSensorManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE);
        }else {
            Log.i(TAG, "Não tem TYPE_AMBIENT_TEMPERATURE");
        }

        if (mSensorManager.getDefaultSensor(Sensor.TYPE_RELATIVE_HUMIDITY) != null){
            humidade = mSensorManager.getDefaultSensor(Sensor.TYPE_RELATIVE_HUMIDITY);
        }else {
            Log.i(TAG, "Não tem TYPE_RELATIVE_HUMIDITY");
        }

        if (mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT) != null){
            luminosidade = mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
        }else {
            Log.i(TAG, "Não tem TYPE_LIGHT");
        }

        if (mSensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE) != null){
            pressao = mSensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE);
        }else {
            Log.i(TAG, "Não tem TYPE_PRESSURE");
        }

    }

    protected void enviarDados() {
        if (luminosidade != null) {
            mSensorManager.registerListener(luminosidadeListener = new SensorEventListener() {
                @Override
                public void onSensorChanged(SensorEvent event) {
                    lum.setText("" + event.values[0]);

                    MqttMessage message = new MqttMessage();
                    message.setQos(QOS);
                    message.setPayload(("" + event.values[0]).getBytes());

                    try {
                        client.publish("/ocean/sensores/" + clientId + "/luminosidade", message);
                    } catch (MqttException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onAccuracyChanged(Sensor sensor, int accuracy) {

                }
            }, luminosidade, SensorManager.SENSOR_DELAY_NORMAL);
        }

        if (acelerometro != null) {
            mSensorManager.registerListener(acelerometroListener = new SensorEventListener() {
                @Override
                public void onSensorChanged(SensorEvent event) {
                    aX.setText("" + event.values[0]);
                    aY.setText("" + event.values[1]);
                    aZ.setText("" + event.values[2]);

                    MqttMessage messageX = new MqttMessage();
                    MqttMessage messageY = new MqttMessage();
                    MqttMessage messageZ = new MqttMessage();

                    messageX.setQos(QOS);
                    messageX.setPayload(("" + event.values[0]).getBytes());
                    messageY.setQos(QOS);
                    messageY.setPayload(("" + event.values[1]).getBytes());
                    messageZ.setQos(QOS);
                    messageZ.setPayload(("" + event.values[2]).getBytes());

                    try {
                        client.publish("/ocean/sensores/" + clientId + "/acelerometro/x", messageX);
                        client.publish("/ocean/sensores/" + clientId + "/acelerometro/y", messageY);
                        client.publish("/ocean/sensores/" + clientId + "/acelerometro/z", messageZ);
                    } catch (MqttException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onAccuracyChanged(Sensor sensor, int accuracy) {

                }
            }, acelerometro, SensorManager.SENSOR_DELAY_NORMAL);
        }

        if (giroscopio != null) {
            mSensorManager.registerListener(giroscopioListener = new SensorEventListener() {
                @Override
                public void onSensorChanged(SensorEvent event) {
                    gX.setText("" + event.values[0]);
                    gY.setText("" + event.values[1]);
                    gZ.setText("" + event.values[2]);

                    MqttMessage messageX = new MqttMessage();
                    MqttMessage messageY = new MqttMessage();
                    MqttMessage messageZ = new MqttMessage();

                    messageX.setQos(QOS);
                    messageX.setPayload(("" + event.values[0]).getBytes());
                    messageY.setQos(QOS);
                    messageY.setPayload(("" + event.values[1]).getBytes());
                    messageZ.setQos(QOS);
                    messageZ.setPayload(("" + event.values[2]).getBytes());


                    try {
                        client.publish("/ocean/sensores/" + clientId + "/giroscopio/x", messageX);
                        client.publish("/ocean/sensores/" + clientId + "/giroscopio/y", messageY);
                        client.publish("/ocean/sensores/" + clientId + "/giroscopio/z", messageZ);
                    } catch (MqttException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onAccuracyChanged(Sensor sensor, int accuracy) {

                }
            }, giroscopio, SensorManager.SENSOR_DELAY_NORMAL);
        }

        if (humidade != null) {
            mSensorManager.registerListener(humidadeListener = new SensorEventListener() {
                @Override
                public void onSensorChanged(SensorEvent event) {
                    umid.setText("" + event.values[0]);


                    MqttMessage message = new MqttMessage();

                    message.setQos(QOS);
                    message.setPayload(("" + event.values[0]).getBytes());


                    try {
                        client.publish("/ocean/sensores/" + clientId + "/umidade", message);
                    } catch (MqttException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onAccuracyChanged(Sensor sensor, int accuracy) {

                }
            }, humidade, SensorManager.SENSOR_DELAY_NORMAL);
        }

        if (pressao != null) {
            mSensorManager.registerListener(pressaoListener = new SensorEventListener() {
                @Override
                public void onSensorChanged(SensorEvent event) {
                    press.setText("" + event.values[0]);

                    MqttMessage message = new MqttMessage();

                    message.setQos(QOS);
                    message.setPayload(("" + event.values[0]).getBytes());


                    try {
                        client.publish("/ocean/sensores/" + clientId + "/pressao", message);
                    } catch (MqttException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onAccuracyChanged(Sensor sensor, int accuracy) {

                }
            }, pressao, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        enviarDados();
    }
}

