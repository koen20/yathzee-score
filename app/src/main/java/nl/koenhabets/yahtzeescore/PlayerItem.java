package nl.koenhabets.yahtzeescore;

public class PlayerItem {
    private String name;
    private int score;
    private long lastUpdate;
    private boolean visible;

    public PlayerItem(String name, int score, long lastUpdate, boolean visible) {
        this.name = name;
        this.score = score;
        this.lastUpdate = lastUpdate;
        this.visible = visible;
    }

    public String getName() {
        return name;
    }

    public int getScore() {
        return score;
    }

    public long getLastUpdate() {
        return lastUpdate;
    }

    public boolean isVisible() {
        return visible;
    }
}
