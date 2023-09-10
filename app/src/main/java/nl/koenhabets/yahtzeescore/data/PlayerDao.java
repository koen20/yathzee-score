package nl.koenhabets.yahtzeescore.data;


import java.util.List;

import nl.koenhabets.yahtzeescore.model.PlayerItem;

public interface PlayerDao {
    List<PlayerItem> getAll();

    void add(PlayerItem item);
}