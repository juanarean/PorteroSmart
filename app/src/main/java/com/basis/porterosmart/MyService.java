package com.basis.porterosmart;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobile.client.Callback;
import com.amazonaws.mobile.client.UserStateDetails;
import com.amazonaws.mobileconnectors.iot.AWSIotMqttClientStatusCallback;
import com.amazonaws.mobileconnectors.iot.AWSIotMqttManager;
import com.amazonaws.mobileconnectors.iot.AWSIotMqttNewMessageCallback;
import com.amazonaws.mobileconnectors.iot.AWSIotMqttQos;

import java.util.UUID;
import java.util.concurrent.CountDownLatch;

public class MyService extends Service {

    AWSIotMqttManager mqttManager;
    String clientId;
    private static final String CUSTOMER_SPECIFIC_IOT_ENDPOINT = "aklvwl0ribi6h-ats.iot.us-west-2.amazonaws.com";
    static final String LOG_TAG = "AsyncTask";
    public Runnable runnable = null;
    public String topico;

    private static final String CHANNEL_ID = "Psmart";
    private NotificationManagerCompat notificationManager;

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("En servicio","Entrando al onCreate");
        //myTask = new MyTask();
        myTask();
        Log.d("En servicio","Saliendo de onCreate");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("En servicio", "Entrando al onStartCommand");
        //onTaskRemoved(intent);
        //myTask();
        return START_STICKY;
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        Intent restartServiceIntent = new Intent(getApplicationContext(),this.getClass());
        restartServiceIntent.setPackage(getPackageName());
        startService(restartServiceIntent);
        super.onTaskRemoved(rootIntent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Intent restartServiceIntent = new Intent(getApplicationContext(),this.getClass());
        restartServiceIntent.setPackage(getPackageName());
        startService(restartServiceIntent);
    }

    public void myTask() {
        clientId = UUID.randomUUID().toString();
        final CountDownLatch latch = new CountDownLatch(1);
        AWSMobileClient.getInstance().initialize(getApplicationContext(),
                new Callback<UserStateDetails>() {
                    @Override
                    public void onResult(UserStateDetails result) {
                        latch.countDown();
                        Log.d(LOG_TAG, "AWSMobileClient on Result");
                    }

                    @Override
                    public void onError(Exception e) {
                        latch.countDown();
                        Log.e(LOG_TAG, "AWSMobileClient onError: ", e);
                    }
                }
        );

        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // MQTT Client
        mqttManager = new AWSIotMqttManager(clientId, CUSTOMER_SPECIFIC_IOT_ENDPOINT);
        Log.d(LOG_TAG, "AWSMqttManager");
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        topico = "Proyecto2019"; //sharedPreferences.getString("topico","default");
        /*if(topico.equals("default")){
            Log.d(LOG_TAG, "Error en el tópico");
        }*/


        try {
            mqttManager.connect(AWSMobileClient.getInstance(), new AWSIotMqttClientStatusCallback() {
                @Override
                public void onStatusChanged(final AWSIotMqttClientStatus status, final Throwable throwable) {
                    Log.d(LOG_TAG, "Status = " + String.valueOf(status));
                    if (String.valueOf(status).equals("Connected")) {
                        try {
                            mqttManager.subscribeToTopic(topico, AWSIotMqttQos.QOS0,
                                    new AWSIotMqttNewMessageCallback() {
                                        @Override
                                        public void onMessageArrived(final String topic, final byte[] data) {
                                            Log.d(LOG_TAG, "llegada de mensaje");
                                            Notificacion();
                                        }
                                    });
                        } catch (Exception e) {
                            Log.e(LOG_TAG, "Subscription error.", e);
                        }
                    }
                    /*runnable = new Runnable() {
                        @Override
                        public void run() {
                            if (throwable != null) {
                                Log.e(LOG_TAG, "Connection error 1.", throwable);
                            }
                        }
                    };*/
                }
            });
        } catch (final Exception e) {
            Log.e(LOG_TAG, "Connection error 2.", e);
        }
    }


        private void Notificacion() {
            createNotificationChannel();

            notificationManager = NotificationManagerCompat.from(getApplicationContext());
            Intent intentService = new Intent(getApplicationContext(), VideoActivity.class);
            intentService.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, intentService, 0);

            NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID)
                    .setSmallIcon(R.drawable.logo)
                    .setLargeIcon(BitmapFactory.decodeResource(getApplicationContext().getResources(), R.drawable.logo))
                    .setContentTitle("Portero Smart")
                    .setContentText("Estan tocando el timbre")
                    .setDefaults(Notification.DEFAULT_ALL)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                    // Set the intent that will fire when the user taps the notification
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true);

            notificationManager.notify(1, builder.build());
        }

        private void createNotificationChannel() {
            // Create the NotificationChannel, but only on API 26+ because
            // the NotificationChannel class is new and not in the support library
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                CharSequence name = "NoficacionPsmart";
                String description = "Notificacion de timbre";
                int importance = NotificationManager.IMPORTANCE_HIGH;
                NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
                channel.setDescription(description);
                // Register the channel with the system; you can't change the importance
                // or other notification behaviors after this
                NotificationManager notificationManager = getApplicationContext().getSystemService(NotificationManager.class);
                notificationManager.createNotificationChannel(channel);
            }
        }

            /*try {
                mqttManager.disconnect();
            } catch (Exception e) {
                Log.e(LOG_TAG, "Disconnect error.", e);
            }
            Log.d(LOG_TAG, "Cerrando asyncTask");
            //startService(new Intent(getApplicationContext(), MyIntentService.class));

            try {
                mqttManager.disconnect();
            } catch (Exception e) {
                Log.e(LOG_TAG, "Disconnect error.", e);
            }
            Log.d(LOG_TAG, "Cancelando AsyncTask");*/


}

