package nl.koenhabets.yahtzeescore;

import android.content.Context;

import androidx.test.InstrumentationRegistry;
import androidx.test.runner.AndroidJUnit4;

import com.google.gson.Gson;

import org.junit.Test;
import org.junit.runner.RunWith;

import nl.koenhabets.yahtzeescore.multiplayer.Multiplayer;
import nl.koenhabets.yahtzeescore.multiplayer.PlayerItem;

import static org.junit.Assert.assertEquals;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class MultiplayerTest {
    public Multiplayer createUser() {
        Multiplayer multiplayer = new Multiplayer(InstrumentationRegistry.getTargetContext(), "test", 0, "123");
        multiplayer.proccessMessage("testUser1;10;1619465600875;1234", false, "1234");
        multiplayer.proccessMessage("testUser1;10;1619465600875;1234", true, "1234");
        return multiplayer;
    }

    @Test
    public void addUser() {
        Context appContext = InstrumentationRegistry.getTargetContext();
        Multiplayer multiplayer = createUser();
        Gson gson = new Gson();
        PlayerItem playerItem = new PlayerItem("testUser1", 10, 1619465600875L, true, false);
        playerItem.setId("1234");

        PlayerItem newPlayer = multiplayer.getPlayer("1234");

        assertEquals(gson.toJson(playerItem), gson.toJson(newPlayer));
    }

    @Test
    public void updateScore() {
        Multiplayer multiplayer = createUser();
        assertEquals(10, multiplayer.getPlayer("1234").getScore().intValue());
        multiplayer.proccessMessage("testUser1;20;1619465600890;1234", true, "1234");
        assertEquals(20, multiplayer.getPlayer("1234").getScore().intValue());
        multiplayer.proccessMessage("testUser1;30;1000;1234", true, "1234");
        assertEquals(20, multiplayer.getPlayer("1234").getScore().intValue());
    }
}
