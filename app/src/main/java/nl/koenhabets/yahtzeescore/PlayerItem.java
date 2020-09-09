package nl.koenhabets.yahtzeescore;

public class PlayerItem implements Comparable<PlayerItem> {
    private String name;
    private Integer score;
    private long lastUpdate;
    private boolean visible;

    public PlayerItem(String name, Integer score, long lastUpdate, boolean visible) {
        this.name = name;
        this.score = score;
        this.lastUpdate = lastUpdate;
        this.visible = visible;
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

    public void setVisible (boolean visible) {
        this.visible = visible;
    }

    @Override
    public int compareTo(PlayerItem p) {
        return p.getScore().compareTo(this.getScore());
    }
}
