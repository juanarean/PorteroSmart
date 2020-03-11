package com.basis.porterosmart;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.JobIntentService;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobile.client.Callback;
import com.amazonaws.mobile.client.UserStateDetails;
import com.amazonaws.mobileconnectors.iot.AWSIotMqttClientStatusCallback;
import com.amazonaws.mobileconnectors.iot.AWSIotMqttManager;
import com.amazonaws.mobileconnectors.iot.AWSIotMqttNewMessageCallback;
import com.amazonaws.mobileconnectors.iot.AWSIotMqttQos;
import com.basis.porterosmart.Common.MyApp;

import java.util.UUID;
import java.util.concurrent.CountDownLatch;

public class MyService extends JobIntentService {
    /**
     * Identifico el JOB para que no existan varias instancias.
     */
    static final int JOB_ID = 1000;

    /**
     * Aca se pone en la cola la petición del sericio.
     */
    static void enqueueWork(Context context, Intent work) {
        enqueueWork(context, MyService.class, JOB_ID, work);
    }

    AWSIotMqttManager mqttManager;
    String clientId;
    private static final String CUSTOMER_SPECIFIC_IOT_ENDPOINT = "aklvwl0ribi6h-ats.iot.us-west-2.amazonaws.com";
    static final String LOG_TAG = "Servicio MQTT";
    public String estado;
    public String topico;

    private static final String CHANNEL_ID = "Psmart";
    private NotificationManagerCompat notificationManager;


    @Override
    protected void onHandleWork(Intent intent){
        myTask();
    }



    @Override
    public void onDestroy() {
        super.onDestroy();
        //toast("Servicio Psmart Cerrado");
    }

    // Helper for showing tests
    final Handler mHandler = new Handler();
    void toast(final CharSequence text) {
        mHandler.post(new Runnable() {
            @Override public void run() {
                Toast.makeText(MyService.this, text, Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * La tarea del servicio conecta al servidor de amazon utilizando la configuracion en raw/awsconfiguration.json
     * para tener acceso al MQTT se debe configurar el AWS Cognito (es el metodo de validación de amazon para conectarse por medio de estas peticiones.
     * Se declara un AWSIotMqttManager conectandose al MQTT y por medio connect() se conecta y subscribe() se subscribe al tópico
     *
     */
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
        topico = sharedPreferences.getString("topico","default");
        if(topico.equals("default")){
            Log.d(LOG_TAG, "Error en el tópico");
        }

        final CountDownLatch latchInfinito = new CountDownLatch(1);
        try {
            mqttManager.connect(AWSMobileClient.getInstance(), new AWSIotMqttClientStatusCallback() {
                @Override
                public void onStatusChanged(final AWSIotMqttClientStatus status, final Throwable throwable) {
                    estado = String.valueOf(status);
                    Log.d(LOG_TAG, "Status = " + estado);
                    if (estado.equals("Connected")) {
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

                }
            });
        } catch (final Exception e) {
            Log.e(LOG_TAG, "Connection error 2.", e);

        }

        try {
            latchInfinito.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
            toast("Se rompio el latch infinito");
        }

    }

    /**
     * Funcion para lanzar una push notification en el sistema.
     *
     */
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
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true);

            notificationManager.notify(1, builder.build());
        }

        private void createNotificationChannel() {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                CharSequence name = "NoficacionPsmart";
                String description = "Notificacion de timbre";
                int importance = NotificationManager.IMPORTANCE_HIGH;
                NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
                channel.setDescription(description);
                NotificationManager notificationManager = getApplicationContext().getSystemService(NotificationManager.class);
                notificationManager.createNotificationChannel(channel);
            }
        }
}

