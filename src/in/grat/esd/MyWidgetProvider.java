package in.grat.esd;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.database.SQLException;
import android.widget.RemoteViews;

public class MyWidgetProvider extends AppWidgetProvider {
	DBHelper dbhelper;
	
	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager,
			int[] appWidgetIds) {
		dbhelper = new DBHelper(context); 
        try {
        	dbhelper.openDataBase();
	 	} catch(SQLException sqle){
	 		throw sqle;
	 	}
		RemoteViews remoteViews = new RemoteViews(context.getPackageName(),
				R.layout.widget_layout);
		Intent intent = new Intent(context.getApplicationContext(), ESDailyActivity.class);

		Calendar calendar = Calendar.getInstance();
    	SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    	SimpleDateFormat displayFormat = new SimpleDateFormat("EE d, yyyy");
    	String today = dateFormat.format(calendar.getTime());
    	String todayDisplay = displayFormat.format(calendar.getTime());
    	String[] results = dbhelper.fetchText(today);
    	remoteViews.setTextViewText(R.id.tvTodaysText, results[0]);
    	remoteViews.setTextViewText(R.id.tvTodaysDate, todayDisplay);
    	
    	PendingIntent pendingIntent = PendingIntent.getActivity(context, getResultCode(), intent, PendingIntent.FLAG_UPDATE_CURRENT);
		remoteViews.setOnClickPendingIntent(R.id.tvTodaysText, pendingIntent);
		appWidgetManager.updateAppWidget(appWidgetIds, remoteViews);

		// Update the widgets via the service
		//context.startService(intent);
	}
}