package barqsoft.footballscores;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by GentBerani on 7/16/15.
 */
public class PreferencesIn {
    public static Context _ctx;

    public static void initialize(Context ctx_) {
        _ctx = ctx_;
    }

    public static SharedPreferences prefs = _ctx.getSharedPreferences(_ctx.getPackageName(), Context.MODE_PRIVATE);


    public static String getHomeScore(String home) {
        return prefs.getString(home, "");
    }

    public static void Save(String key, String description) {
        prefs.edit().putString(key, description).commit();
    }

    public static void SaveInt(String key, int description) {
        prefs.edit().putInt(key, description).commit();
    }
}
