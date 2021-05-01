
package nl.koenhabets.yahtzeescore.multiplayer;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import nl.koenhabets.yahtzeescore.data.PlayerDao;
import nl.koenhabets.yahtzeescore.data.PlayerDaoImpl;

public class Multiplayer {
    private MultiplayerListener listener;
    private Boolean realtimeDatabaseEnabled = true;
    private String firebaseUserUid;
    private DatabaseReference database;
    private ChildEventListener childEventListener;

    private List<PlayerItem> players = new ArrayList<>();
    private Timer updateTimer;
    private Timer autoRemoveTimer;
    private String name;
    private int score;
    private Mqtt mqtt;
    private Nearby nearby;
    private PlayerDao playerDao;

    public Multiplayer(Context context, String name, int score, String firebaseUserUid) {
        database = FirebaseDatabase.getInstance().getReference();
        this.name = name;
        this.listener = null;
        this.score = score;
        this.firebaseUserUid = firebaseUserUid;
        playerDao = new PlayerDaoImpl(context);
        initMultiplayer(context, name);
    }

    public interface MultiplayerListener {
        void onChange(List<PlayerItem> players);

        void onChangeFullScore(List<PlayerItem> players);
    }

    public void setMultiplayerListener(MultiplayerListener listener) {
        this.listener = listener;
    }

    public void initMultiplayer(Context context, String name) {
        nearby = new Nearby(context, firebaseUserUid);
        nearby.setNearbyListener(message -> proccessMessage(message, false, ""));

        try {
            mqtt = new Mqtt(context, name);
            mqtt.setMqttListener(message -> proccessMessage(message, true, ""));
        } catch (Exception e) {
            e.printStackTrace();
        }
        updateTimer = new Timer();
        updateTimer.scheduleAtFixedRate(new updateTask(), 1000, 10000);

        autoRemoveTimer = new Timer();
        autoRemoveTimer.scheduleAtFixedRate(new autoRemove(), 60000, 60000);

        // read all discovered players and add them to the players list
        List<PlayerItem> playersRead = playerDao.getAll();
        for (int i = 0; i < playersRead.size(); i++) {
            try {
                PlayerItem player = playersRead.get(i);
                if (getPlayer(player.getId()) == null) {
                    player.setValueEventListenerFull(addDatabaseListener(player.getId()));
                    player.setVisible(false);
                    players.add(player);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        Log.i("players", players.toString() + "");

        initDatabase();
    }

    public void initDatabase() {
        if (realtimeDatabaseEnabled) {//todo only listen to players nearby
            childEventListener = database.child("score").addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                }

                @Override
                public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                    Log.i("Firebase Received", dataSnapshot.getValue().toString());
                    try {
                        proccessMessage(dataSnapshot.getValue().toString(), true, dataSnapshot.getKey());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

                }

                @Override
                public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                }

                @Override
                public void onCancelled(DatabaseError error) {
                    Log.w("EditTagsActivity", "Failed to read scores.", error.toException());
                }
            });
        }
    }

    public ValueEventListener addDatabaseListener(String id) {
        ValueEventListener valueEventListener = null;
        if (!id.equals("")) {//if id is empty all messages are received
            Log.i("Firebase", "Adding listener for " + id);
            valueEventListener = database.child("scoreFull").child(id).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    try {
                        if (snapshot.exists()) {
                            Log.i("Firebase Received", snapshot.getValue().toString());
                            try {
                                proccessFullScore(snapshot.getValue().toString(), snapshot.getKey());
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        } else {
                            Log.i("Firebase null", id);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onCancelled(DatabaseError error) {
                    Log.w("EditTagsActivity", "Failed to read scores.", error.toException());
                }
            });
        }

        return valueEventListener;
    }

    public void setScore(int score) {
        this.score = score;
        listener.onChange(players);
        updateNearbyScore();
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setFullScore(JSONObject jsonObject) {
        if (realtimeDatabaseEnabled) {
            try {
                database.child("scoreFull").child(firebaseUserUid).setValue(jsonObject.toString());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public int getPlayerAmount() {
        int count = 0;
        for (int i = 0; i < players.size(); i++) {
            if (players.get(i).isVisible() && !players.get(i).isLocal()) {
                count++;
            }
        }
        return count;
    }

    public PlayerItem getPlayer(String id) {
        PlayerItem playerItem = null;
        for (int i = 0; i < getPlayers().size(); i++) {
            if (getPlayers().get(i).getName().equals(name)) {
                playerItem = getPlayers().get(i);
            }
            if (getPlayers().get(i).getId() != null) {
                if (getPlayers().get(i).getId().equals(id)) {
                    playerItem = getPlayers().get(i);
                }
            }
        }

        return playerItem;
    }

    public void addPlayer(PlayerItem playerItem) {
        players.add(playerItem);
    }

    public List<PlayerItem> getPlayers() {
        return players;
    }

    public void stopMultiplayer() {
        nearby.disconnect();
        try {
            mqtt.disconnectMqtt();
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            updateTimer.cancel();
            updateTimer.purge();
            autoRemoveTimer.cancel();
            autoRemoveTimer.purge();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (realtimeDatabaseEnabled) {
            try {
                database.child("score").child(firebaseUserUid).removeValue();
                database.child("scoreFull").child(firebaseUserUid).removeValue();
                for (int i = 0; i < players.size(); i++) {
                    ValueEventListener valueEventListener = players.get(i).getValueEventListenerFull();
                    if (valueEventListener != null) {
                        database.removeEventListener(valueEventListener);
                    }
                }
                database.removeEventListener(childEventListener);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private class updateTask extends TimerTask {
        @Override
        public void run() {
            updateNearbyScore();
        }
    }

    //make player invisible if there are no messages for two minutes
    private class autoRemove extends TimerTask {
        @Override
        public void run() {
            Date date = new Date();
            for (int i = 0; i < players.size(); i++) {
                if (date.getTime() - players.get(i).getLastUpdate() > 120000 && !players.get(i).getName().equals(name)) {
                    players.get(i).setVisible(false);
                    Log.i("players", "Making player invisible: " + players.get(i).getName());
                }
            }
        }
    }

    public void proccessMessage(String message, boolean mqtt, String id) {
        try {
            if (!message.equals("new player")) {
                String[] messageSplit = message.split(";");
                boolean exists = false;
                if (!messageSplit[0].equals(name) && !messageSplit[0].equals("")) {
                    for (int i = 0; i < players.size(); i++) {
                        PlayerItem playerItem = players.get(i);
                        boolean match = false;
                        if (playerItem.getId() == null || playerItem.getId().equals("") || messageSplit.length < 4) {
                            Log.i("match", "trying to match with name");
                            if (playerItem.getName().equals(messageSplit[0])) {
                                match = true;
                                Log.i("Multiplayer", "match with name");
                            }
                        } else {
                            try {
                                Log.i("match", "trying to match with id");
                                if (playerItem.getId().equals(messageSplit[3])) {
                                    match = true;
                                    Log.i("Multiplayer", "match with id");
                                }
                            } catch (ArrayIndexOutOfBoundsException ignored) {

                            }
                        }

                        if (match) {
                            exists = true;
                            //check if id of saved player is empty and add the player id from the firebase key (<= v1.10)
                            if (playerItem.getId() == null || playerItem.getId().equals("")) {
                                players.get(i).setId(id);
                                Log.i("received", "Setting value event listener for " + id);
                                players.get(i).setValueEventListenerFull(addDatabaseListener(id));
                                playerDao.add(playerItem);
                            }
                            if (playerItem.getLastUpdate() < Long.parseLong(messageSplit[2]) && mqtt) {
                                Log.i("message", "newer message");
                                players.get(i).setLastUpdate(Long.parseLong(messageSplit[2]));
                                players.get(i).setScore(Integer.parseInt(messageSplit[1]));
                                players.get(i).setVisible(true);
                                players.get(i).setName(messageSplit[0]);
                                listener.onChange(players);
                                break;
                            }
                        }
                    }
                    if (!exists && !mqtt) {
                        Log.i("New player", messageSplit[0]);
                        PlayerItem item = new PlayerItem(messageSplit[0], Integer.parseInt(messageSplit[1]), Long.parseLong(messageSplit[2]), true, false);
                        try {
                            item.setId(messageSplit[3]);
                            item.setValueEventListenerFull(addDatabaseListener(messageSplit[3]));
                            PlayerItem item2 = new PlayerItem(messageSplit[0], 0, 0, false, false);
                            item2.setId(messageSplit[3]);
                            playerDao.add(item2);
                            Log.i("received", "Setting value event listener for " + id);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        players.add(item);
                        listener.onChange(players);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void proccessFullScore(String score, String id) {
        for (int i = 0; i < players.size(); i++) {
            if (players.get(i).getId() != null) {
                if (players.get(i).getId().equals(id)) {
                    try {
                        players.get(i).setFullScore(new JSONObject(score));
                        break;
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        listener.onChangeFullScore(players);
    }

    public void updateNearbyScore() {
        Date date = new Date();
        if (!name.equals("")) {
            String text = name + ";" + (score) + ";" + date.getTime() + ";" + firebaseUserUid;
            try {
                mqtt.publish("score", text);
            } catch (Exception e) {
                e.printStackTrace();
            }
            nearby.updateScore(text);
            if (realtimeDatabaseEnabled) {
                try {
                    database.child("score").child(firebaseUserUid).setValue(text);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

    }
}
