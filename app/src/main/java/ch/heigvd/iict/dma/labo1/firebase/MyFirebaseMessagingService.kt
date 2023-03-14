package ch.heigvd.iict.dma.labo1.firebase

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        Log.d(TAG, "Message reçu: ${remoteMessage.data}")
    }

    override fun onNewToken(token: String) {
        Log.d(TAG, "Nouveau token: $token")
    }

    companion object {
        private const val TAG = "MyFirebaseMsgService"
    }
}
