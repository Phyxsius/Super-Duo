package barqsoft.footballscores;

import android.app.IntentService;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.database.Cursor;
import android.os.Build;
import android.widget.RemoteViews;

import barqsoft.footballscores.service.myFetchService;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p/>
 */
public class ScoreWidgetService extends IntentService {

    public ScoreWidgetService() {
        super("ScoreWidgetService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Intent service_start = new Intent(getApplicationContext(), myFetchService.class);
        getApplicationContext().startService(service_start);

        Cursor scoreCursor = getApplicationContext().getContentResolver()
                .query(DatabaseContract.scores_table.buildScores(),
                        null, null, null, null);

        // Assure we got a result from the call before we set information
        if (scoreCursor.getCount() > 0) {
            scoreCursor.moveToFirst();
            String home = scoreCursor.getString(scoresAdapter.COL_HOME);
            String away = scoreCursor.getString(scoresAdapter.COL_AWAY);
            Integer homeGoals = scoreCursor.getInt(scoresAdapter.COL_HOME_GOALS);
            Integer awayGoals = scoreCursor.getInt(scoresAdapter.COL_AWAY_GOALS);
            String matchTime = scoreCursor.getString(scoresAdapter.COL_MATCHTIME);


            // Retrieve all of the Today widget ids: these are the widgets we need to update
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
            int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(this,
                    ScoreWidget.class));

            for (int appWidgetId : appWidgetIds) {
                int layoutId = R.layout.score_widget;
                RemoteViews views = new RemoteViews(getPackageName(), layoutId);

                views.setTextViewText(R.id.widget_home_name, home);
                views.setTextViewText(R.id.widget_away_name, away);
                views.setImageViewResource(R.id.widget_home_crest,
                        Utilies.getTeamCrestByTeamName(home));
                views.setImageViewResource(R.id.widget_away_crest,
                        Utilies.getTeamCrestByTeamName(away));
                views.setTextViewText(R.id.widget_score_textview,
                        Utilies.getScores(homeGoals, awayGoals));
                views.setTextViewText(R.id.widget_data_textview, matchTime);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
                    views.setContentDescription(R.id.widget_score_textview,
                            "Score is " + Utilies.getScores(homeGoals, awayGoals));
                    views.setContentDescription(R.id.widget_data_textview, "Time is " + matchTime);
                }

                // Launch the app on click
                Intent launchIntent = new Intent(this, MainActivity.class);
                PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, launchIntent, 0);
                views.setOnClickPendingIntent(R.id.widget_container, pendingIntent);

                // Update the widget with the new information
                appWidgetManager.updateAppWidget(appWidgetId, views);
            }
        }
    }
}
