package barqsoft.footballscores.widget;

import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.support.v4.app.LoaderManager;
import android.util.Log;
import android.widget.RemoteViews;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.Vector;

import barqsoft.footballscores.DatabaseContract;
import barqsoft.footballscores.R;
import barqsoft.footballscores.service.myFetchService;


public class WidgetIntentReceiver extends BroadcastReceiver {

    public static final String LOG_TAG = "myFetchService";

    LoaderManager.LoaderCallbacks loaderManager;
    private static int clickCount = 0;
    public static final int SCORES_LOADER = 0;
    int COL_HOME = 3;

    Cursor cursor;
    //Match data
    String League = "";
    String mDate = "";
    String mTime = "";
    String Home = "";
    String Away = "";
    String Home_goals = "";
    String Away_goals = "";
    String match_id = "";
    String match_day = "";
    int i = 0;
    @Override
    public void onReceive(Context context, Intent intent) {


        boolean widgetUpdated = intent.getBooleanExtra("widgetUpdated", false);
        if(!widgetUpdated) {
            Intent service_start = new Intent(context, myFetchService.class);
            service_start.putExtra("widgetUpdate", true);
            context.startService(service_start);
        }else{

            String serverResponse = intent.getStringExtra("widgetUpdateResult");
            processJSONdata(serverResponse, context, true);
            updateWidgetPictureAndButtonListener(context);
        }
    }


    private void updateWidgetPictureAndButtonListener(Context context) {
        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_demo);
       remoteViews.setTextViewText(R.id.home_name_widget, Home);
        remoteViews.setTextViewText(R.id.away_name_widget, Away);
        remoteViews.setTextViewText(R.id.score_textview_widget, Home_goals + " : " + Away_goals);

        remoteViews.setOnClickPendingIntent(R.id.widget_button, WidgetProvider.buildButtonPendingIntent(context));

        WidgetProvider.pushWidgetUpdate(context.getApplicationContext(), remoteViews);


    }

    private int getImageToSet() {
        clickCount++;
        return clickCount % 2 == 0 ? R.drawable.west_ham : R.drawable.sunderland;
    }

    public void processJSONdata(String JSONdata, Context mContext, boolean isReal) {
        //JSON data
        final String SERIE_A = "401";

        final String PREMIER_LEGAUE = "398";

        final String CHAMPIONS_LEAGUE = "405";

        final String PRIMERA_DIVISION = "399";

        final String BUNDESLIGA = "394";

        final String SEASON_LINK = "http://api.football-data.org/alpha/soccerseasons/";
        final String MATCH_LINK = "http://api.football-data.org/alpha/fixtures/";
        final String FIXTURES = "fixtures";
        final String LINKS = "_links";
        final String SOCCER_SEASON = "soccerseason";
        final String SELF = "self";
        final String MATCH_DATE = "date";
        final String HOME_TEAM = "homeTeamName";
        final String AWAY_TEAM = "awayTeamName";
        final String RESULT = "result";
        final String HOME_GOALS = "goalsHomeTeam";
        final String AWAY_GOALS = "goalsAwayTeam";
        final String MATCH_DAY = "matchday";


        try {
            JSONArray matches = new JSONObject(JSONdata).getJSONArray(FIXTURES);


            //ContentValues to be inserted
            Vector<ContentValues> values = new Vector<ContentValues>(matches.length());
            for (int i = 0; i < matches.length(); i++) {
                JSONObject match_data = matches.getJSONObject(i);
                League = match_data.getJSONObject(LINKS).getJSONObject(SOCCER_SEASON).
                        getString("href");
                League = League.replace(SEASON_LINK, "");
                if (League.equals(PREMIER_LEGAUE) ||
                        League.equals(SERIE_A) ||
                        League.equals(CHAMPIONS_LEAGUE) ||
                        League.equals(BUNDESLIGA) ||
                        League.equals(PRIMERA_DIVISION)) {
                    match_id = match_data.getJSONObject(LINKS).getJSONObject(SELF).
                            getString("href");
                    match_id = match_id.replace(MATCH_LINK, "");
                    if (!isReal) {
                        //This if statement changes the match ID of the dummy data so that it all goes into the database
                        match_id = match_id + Integer.toString(i);
                    }

                    mDate = match_data.getString(MATCH_DATE);
                    mTime = mDate.substring(mDate.indexOf("T") + 1, mDate.indexOf("Z"));
                    mDate = mDate.substring(0, mDate.indexOf("T"));
                    SimpleDateFormat match_date = new SimpleDateFormat("yyyy-MM-ddHH:mm:ss");
                    match_date.setTimeZone(TimeZone.getTimeZone("UTC"));
                    try {
                        Date parseddate = match_date.parse(mDate + mTime);
                        SimpleDateFormat new_date = new SimpleDateFormat("yyyy-MM-dd:HH:mm");
                        new_date.setTimeZone(TimeZone.getDefault());
                        mDate = new_date.format(parseddate);
                        mTime = mDate.substring(mDate.indexOf(":") + 1);
                        mDate = mDate.substring(0, mDate.indexOf(":"));

                        if (!isReal) {
                            //This if statement changes the dummy data's date to match our current date range.
                            Date fragmentdate = new Date(System.currentTimeMillis() + ((i - 2) * 86400000));
                            SimpleDateFormat mformat = new SimpleDateFormat("yyyy-MM-dd");
                            mDate = mformat.format(fragmentdate);
                        }
                    } catch (Exception e) {
                        Log.d("Widget", "error here!");
                        Log.e("Widget", e.getMessage());
                    }
                    Home = match_data.getString(HOME_TEAM);
                    Away = match_data.getString(AWAY_TEAM);
                    Home_goals = match_data.getJSONObject(RESULT).getString(HOME_GOALS);
                    Away_goals = match_data.getJSONObject(RESULT).getString(AWAY_GOALS);
                    match_day = match_data.getString(MATCH_DAY);

                    ContentValues match_values = new ContentValues();
                    match_values.put(DatabaseContract.scores_table.MATCH_ID, match_id);
                    match_values.put(DatabaseContract.scores_table.DATE_COL, mDate);
                    match_values.put(DatabaseContract.scores_table.TIME_COL, mTime);
                    match_values.put(DatabaseContract.scores_table.HOME_COL, Home);
                    match_values.put(DatabaseContract.scores_table.AWAY_COL, Away);
                    match_values.put(DatabaseContract.scores_table.HOME_GOALS_COL, Home_goals);
                    match_values.put(DatabaseContract.scores_table.AWAY_GOALS_COL, Away_goals);
                    match_values.put(DatabaseContract.scores_table.LEAGUE_COL, League);
                    match_values.put(DatabaseContract.scores_table.MATCH_DAY, match_day);
                    //log spam


                    //Log.v(LOG_TAG,match_id);
                    //Log.v(LOG_TAG,mDate);
                    //Log.v(LOG_TAG,mTime);
                    //Log.v(LOG_TAG,Home);
                    //Log.v(LOG_TAG,Away);
                    //Log.v(LOG_TAG,Home_goals);
                    //Log.v(LOG_TAG,Away_goals);

                    values.add(match_values);
                }
            }
            int inserted_data = 0;
            ContentValues[] insert_data = new ContentValues[values.size()];
            values.toArray(insert_data);
            inserted_data = mContext.getContentResolver().bulkInsert(
                    DatabaseContract.BASE_CONTENT_URI, insert_data);
            //Log.v(LOG_TAG,"Succesfully Inserted : " + String.valueOf(inserted_data));
        } catch (JSONException e) {
            Log.e("Widget", e.getMessage());
        }

    }
}