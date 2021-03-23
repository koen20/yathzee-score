package nl.koenhabets.yahtzeescore.multiplayer;

import com.google.firebase.database.ValueEventListener;

import org.json.JSONObject;

public class PlayerItem implements Comparable<PlayerItem> {
    private String name;
    private Integer score;
    private long lastUpdate;
    private boolean visible;
    private boolean local;
    private JSONObject fullScore = new JSONObject();
    private String id;
    private ValueEventListener valueEventListenerFull;

    public PlayerItem(String name, Integer score, long lastUpdate, boolean visible, boolean local) {
        this.name = name;
        this.score = score;
        this.lastUpdate = lastUpdate;
        this.visible = visible;
        this.local = local;
    }

    public String getName() {
        return name;
    }

    public Integer getScore() {
        return score;
    }

    public long getLastUpdate() {
        return lastUpdate;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public boolean isLocal() {
        return local;
    }

    public void setFullScore(JSONObject fullScore) {
        this.fullScore = fullScore;
    }

    public void setLastUpdate(Long lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public JSONObject getFullScore() {
        return fullScore;
    }

    @Override
    public int compareTo(PlayerItem p) {
        return p.getScore().compareTo(this.getScore());
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ValueEventListener getValueEventListenerFull() {
        return valueEventListenerFull;
    }

    public void setValueEventListenerFull(ValueEventListener valueEventListenerFull) {
        this.valueEventListenerFull = valueEventListenerFull;
    }
}
