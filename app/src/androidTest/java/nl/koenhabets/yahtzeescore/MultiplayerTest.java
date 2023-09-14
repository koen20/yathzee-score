package nl.koenhabets.yahtzeescore;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.runner.RunWith;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class MultiplayerTest {
    /*public MultiplayerOud createUser() {
        MultiplayerOud multiplayer = new MultiplayerOud(ApplicationProvider.getApplicationContext(), "test", 0, "123");
        multiplayer.proccessMessage("testUser1;10;1619465600875;1234", false, "1234");
        multiplayer.proccessMessage("testUser1;10;1619465600875;1234", true, "1234");
        return multiplayer;
    }

    @Test
    public void addUser() {
        MultiplayerOud multiplayer = createUser();
        Gson gson = new Gson();
        PlayerItem playerItem = new PlayerItem("testUser1", 10, 1619465600875L, true, false);
        playerItem.setId("1234");

        PlayerItem newPlayer = multiplayer.getPlayer("1234");

        assertEquals(gson.toJson(playerItem), gson.toJson(newPlayer));
    }

    @Test
    public void updateScore() {
        MultiplayerOud multiplayer = createUser();
        assertEquals(10, multiplayer.getPlayer("1234").getScore().intValue());
        multiplayer.proccessMessage("testUser1;20;1619465600890;1234", true, "1234");
        assertEquals(20, multiplayer.getPlayer("1234").getScore().intValue());
        multiplayer.proccessMessage("testUser1;30;1000;1234", true, "1234");
        assertEquals(20, multiplayer.getPlayer("1234").getScore().intValue());
    }*/
}
