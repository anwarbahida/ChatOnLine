package com.projet.chatonline.notifications;

import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        // Vérifier si le message contient des données
        if (remoteMessage.getData().size() > 0) {
            String title = remoteMessage.getData().get("title");
            String body = remoteMessage.getData().get("body");

            NotificationHelper.showNotification(
                    getApplicationContext(),
                    title,
                    body
            );
        }

        // Notification envoyée via "notification" payload
        if (remoteMessage.getNotification() != null) {
            NotificationHelper.showNotification(
                    getApplicationContext(),
                    remoteMessage.getNotification().getTitle(),
                    remoteMessage.getNotification().getBody()
            );
        }
    }

    @Override
    public void onNewToken(String token) {
        super.onNewToken(token);
        Log.d("FCM_TOKEN", token);
        // Sauvegarder le token dans Firestore pour l'utilisateur connecté
    }
}
