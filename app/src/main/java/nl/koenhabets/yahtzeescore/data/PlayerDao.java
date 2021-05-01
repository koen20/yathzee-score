package nl.koenhabets.yahtzeescore.data;


import java.util.List;

import nl.koenhabets.yahtzeescore.multiplayer.PlayerItem;

public interface PlayerDao {
    List<PlayerItem> getAll();

    void add(PlayerItem item);
}