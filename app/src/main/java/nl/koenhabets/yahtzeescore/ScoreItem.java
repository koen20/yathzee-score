package nl.koenhabets.yahtzeescore;

public class ScoreItem {
    private int score;
    private long date;
    private String id;

    public ScoreItem(int score, long date, String id) {
        this.score = score;
        this.date = date;
        this.id = id;
    }

    public int getScore() {
        return score;
    }

    public long getDate() {
        return date;
    }

    public String getId() {
        return id;
    }
}
