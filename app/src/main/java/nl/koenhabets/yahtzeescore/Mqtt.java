package nl.koenhabets.yahtzeescore;

import android.content.Context;
import android.util.Log;

import com.crashlytics.android.Crashlytics;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

class Mqtt {
    public static MqttAndroidClient mqttAndroidClient;
    private final static String serverUri = "tcp://yahtzee.koenhabets.nl:7829";

    private final static String clientId = "Yahtzee-" + ((int) (Math.random() * ((999999 - 1) + 1)) + 1) + "-";

    public static void connectMqtt(String name, Context context) throws MqttException {
        Crashlytics.setUserIdentifier(clientId);
        mqttAndroidClient = new MqttAndroidClient(context, serverUri, clientId + name);
        mqttAndroidClient.setCallback(new MqttCallbackExtended() {
            @Override
            public void connectComplete(boolean b, String s) {
                Log.w("mqtt", s);
                mqttSubscribe();
            }

            @Override
            public void connectionLost(Throwable throwable) {

            }

            @Override
            public void messageArrived(String topic, MqttMessage mqttMessage) {
                Log.w("Mqtt", mqttMessage.toString());
                MainActivity.proccessMessage(mqttMessage.toString(), true);
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {

            }
        });
        MqttConnectOptions mqttConnectOptions = new MqttConnectOptions();
        mqttConnectOptions.setUserName("Yahtzee");
        mqttConnectOptions.setPassword("ao&si4foi2eawefw".toCharArray());
        mqttConnectOptions.setAutomaticReconnect(true);
        mqttConnectOptions.setCleanSession(false);
        mqttAndroidClient.connect(mqttConnectOptions);
    }

    public static void disconnectMqtt() {
        try {
            mqttAndroidClient.disconnect();
        } catch (Exception e){

        }
    }

    private static void mqttSubscribe() {
        try {
            mqttAndroidClient.subscribe("score", 0, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Log.w("Mqtt", "Subscribed!");
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Log.w("Mqtt", "Subscribed fail!");
                }
            });

        } catch (Exception ex) {
            System.err.println("Exception whilst subscribing");
            ex.printStackTrace();
        }

    }

    public static void publish(String topic, String message) throws MqttException {
        mqttAndroidClient.publish(topic, new MqttMessage(message.getBytes()));

    }
}
