package nl.koenhabets.yahtzeescore;

import org.json.JSONObject;

public class ScoreItem {
    private final int score;
    private final Long date;
    private final String id;
    private final JSONObject allScores;

    public ScoreItem(int score, Long date, String id, JSONObject allScores) {
        this.score = score;
        this.date = date;
        this.id = id;
        this.allScores = allScores;
    }

    public int getScore() {
        return score;
    }

    public Long getDate() {
        return date;
    }

    public String getId() {
        return id;
    }

    public JSONObject getAllScores() {
        return allScores;
    }
}
