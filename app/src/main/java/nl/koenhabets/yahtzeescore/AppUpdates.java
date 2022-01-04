package nl.koenhabets.yahtzeescore;

import android.app.Activity;
import android.content.Context;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class AppUpdates {
    Context context;
    Activity activity;

    public AppUpdates(Context context, Activity activity) {
        this.context = context;
        this.activity = activity;
    }

    public static JSONObject getVersionInfo() {
        JSONObject jsonObject = new JSONObject();
        try {
            URL url = new URL("https://koenhabets.nl/yahtzee-update-info.json");
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuilder content = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                content.append(inputLine);
            }
            in.close();
            con.disconnect();
            jsonObject = new JSONObject(content.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }

        return jsonObject;
    }
}
