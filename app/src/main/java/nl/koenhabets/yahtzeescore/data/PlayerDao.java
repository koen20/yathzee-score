package nl.koenhabets.yahtzeescore.data;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface PlayerDao {
    @Query("SELECT * FROM player")
    List<Player> getAll();

    @Insert
    void insertAll(Player... players);

    @Delete
    void delete(Player player);
}
