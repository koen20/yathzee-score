package nl.koenhabets.yahtzeescore;

import java.util.Comparator;

public class ScoreComparatorDate implements Comparator<ScoreItem> {
    @Override
    public int compare(ScoreItem scoreItem, ScoreItem scoreItem1) {

        return scoreItem1.getDate().compareTo(scoreItem.getDate());
    }
}
