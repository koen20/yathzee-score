package nl.koenhabets.yahtzeescore;

import org.json.JSONObject;

public class ScoreItem {
    private int score;
    private long date;
    private String id;
    private JSONObject allScores;

    public ScoreItem(int score, long date, String id, JSONObject allScores) {
        this.score = score;
        this.date = date;
        this.id = id;
        this.allScores = allScores;
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

    public JSONObject getAllScores() {
        return allScores;
    }
}
