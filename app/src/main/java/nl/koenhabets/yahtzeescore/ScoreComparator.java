package nl.koenhabets.yahtzeescore;

import java.util.Comparator;

public class ScoreComparator implements Comparator<ScoreItem> {
    @Override
    public int compare(ScoreItem scoreItem, ScoreItem scoreItem1) {

        return scoreItem1.getScore() - scoreItem.getScore();
    }
}
