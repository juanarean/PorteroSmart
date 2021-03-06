package com.basis.porterosmart;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.basis.porterosmart.Common.MyApp;

public class MyReceiver extends BroadcastReceiver {

    /**
     * Al reiniciar el sistema se lanza de nuevo el servicio de MQTT.
     * @param context
     * @param intent
     */

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)){
            Intent jobServiceIntent = new Intent(context, MyService.class);
            MyService.enqueueWork(context,jobServiceIntent);
        }
    }
}
