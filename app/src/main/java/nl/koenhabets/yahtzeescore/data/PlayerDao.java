package nl.koenhabets.yahtzeescore.data;


import org.json.JSONArray;

public interface PlayerDao {
    JSONArray getAll();

    void add(String string);
}