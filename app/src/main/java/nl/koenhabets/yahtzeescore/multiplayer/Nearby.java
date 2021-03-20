package nl.koenhabets.yahtzeescore.multiplayer;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.nearby.messages.Message;
import com.google.android.gms.nearby.messages.MessageListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.firebase.auth.FirebaseUser;

public class Nearby implements OnFailureListener {
    private MessageListener mMessageListener;
    private Message mMessage;
    private FirebaseUser firebaseUser;
    private Context context;
    private NearbyListener listener;

    public Nearby(Context context, FirebaseUser firebaseUser){
        this.context = context;
        this.firebaseUser = firebaseUser;
        initNearby();
    }

    public interface NearbyListener {
        void onMessage(String message);
    }

    public void setNearbyListener(NearbyListener listener) {this.listener = listener;}

    public void initNearby() {
        mMessageListener = new MessageListener() {
            @Override
            public void onFound(Message message) {
                Log.d("t", "Found message: " + new String(message.getContent()));
                listener.onMessage(new String(message.getContent()));
            }

            @Override
            public void onLost(Message message) {
                Log.d("d", "Lost sight of message: " + new String(message.getContent()));
            }
        };
        mMessage = new Message((firebaseUser.getUid()).getBytes());

        com.google.android.gms.nearby.Nearby.getMessagesClient(context).publish(mMessage).addOnFailureListener(this);
        com.google.android.gms.nearby.Nearby.getMessagesClient(context).subscribe(mMessageListener);
    }

    public void disconnect(){
        com.google.android.gms.nearby.Nearby.getMessagesClient(context).unpublish(mMessage);
        com.google.android.gms.nearby.Nearby.getMessagesClient(context).unsubscribe(mMessageListener);
    }

    public void updateScore(String text){
        mMessage = new Message((text).getBytes());
        com.google.android.gms.nearby.Nearby.getMessagesClient(context).publish(mMessage).addOnFailureListener(this);
    }

    @Override
    public void onFailure(@NonNull Exception e) {

    }
}
