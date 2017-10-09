package org.example.llamadaentrante;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;

/**
 * Created by esteban on 21/03/2016.
 */
public class RecepetorLlamadas extends BroadcastReceiver {

    String estado = "", numero = "";
    String info;

    public void onReceive(Context context, Intent intent) {

        // Sacamos información del intent



        Bundle extras = intent.getExtras();

        if(extras != null) {

            estado = extras.getString(TelephonyManager.EXTRA_STATE);

            if(estado.equals(TelephonyManager.EXTRA_STATE_RINGING)) {

                numero = extras.getString( TelephonyManager.EXTRA_INCOMING_NUMBER);

            }

        }

        info = estado + " "+ numero;

        Log.i("ReceptorAnuncio", info + " intent=" + intent);

        // Creamos Notificación

     /*   NotificationManager nm = (NotificationManager) context.getSystemService( Context.NOTIFICATION_SERVICE);

       // Notification notificacion = new Notification( R.drawable.ic_launcher,"Llamada entrante", System.currentTimeMillis());

        PendingIntent intencionPendiente = PendingIntent.getActivity( context, 0, new Intent(context, MainActivity.class), 0);
        //*****************************************
        Notification.Builder builder = new Notification.Builder(context);


        builder.setSmallIcon(R.drawable.notification_template_icon_bg);

        builder.setContentTitle("Llamda entrante");
        builder.setContentIntent(intencionPendiente);

        Notification notification = builder.getNotification();
//        notificationManager.notify(R.drawable.notification_template_icon_bg, notification);
        //notificacion.setLatestEventInfo(context, "Llamada entrante", info, intencionPendiente);

       // nm.notify(R.drawable.notification_template_icon_bg,     notification);
        notification = builder.build();*/
        NotificationManager  manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, new Intent(context, MainActivity.class), 0);

        Notification.Builder builder = new Notification.Builder(context);

        String texto= numero;
        builder.setAutoCancel(false);
        builder.setTicker("Recibien Llamadaaaa!!!!!!!");
        builder.setContentTitle("Llamada entrante");
        builder.setContentText("chaa");
        builder.setContentInfo(texto);
        builder.setSmallIcon(R.drawable.play);
        builder.setContentIntent(pendingIntent);
        builder.setOngoing(true);
        builder.setNumber(100);
        builder.build();

        Notification  myNotication = builder.getNotification();
        manager.notify(11, myNotication);
    }

}