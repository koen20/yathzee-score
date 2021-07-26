package nl.koenhabets.yahtzeescore;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageInfo;

import com.google.android.play.core.appupdate.AppUpdateInfo;
import com.google.android.play.core.appupdate.AppUpdateManager;
import com.google.android.play.core.appupdate.AppUpdateManagerFactory;
import com.google.android.play.core.install.model.AppUpdateType;
import com.google.android.play.core.tasks.Task;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class AppUpdates implements Runnable {
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

    @Override
    public void run() {
        try {
            AppUpdateManager appUpdateManager = AppUpdateManagerFactory.create(context);

            Task<AppUpdateInfo> appUpdateInfoTask = appUpdateManager.getAppUpdateInfo();
            try {
                PackageInfo pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
                int verCode = pInfo.versionCode;

                appUpdateInfoTask.addOnSuccessListener(appUpdateInfo -> {
                    try {
                        //if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE) {
                        JSONObject jsonObject = getVersionInfo();
                        if (jsonObject.has("flexibleVersion")) {
                            if (jsonObject.getInt("flexibleVersion") > verCode) {
                                appUpdateManager.startUpdateFlowForResult(appUpdateInfo, AppUpdateType.FLEXIBLE, activity, 2);
                            }
                        }
                        if (jsonObject.has("immediateVersion")) {
                            if (jsonObject.getInt("immediateVersion") > verCode) {
                                appUpdateManager.startUpdateFlowForResult(appUpdateInfo, AppUpdateType.IMMEDIATE, activity, 2);
                            }
                        }
                        //}
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
