package nl.koenhabets.yahtzeescore.data;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class Player {
    @PrimaryKey
    public String id;
    public String name;
}
