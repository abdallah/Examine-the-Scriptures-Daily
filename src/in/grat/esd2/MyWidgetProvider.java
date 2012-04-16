package in.grat.esd2;

import in.grat.esd2.R;

import java.text.SimpleDateFormat;
import java.util.Calendar;



import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.text.Html;
import android.text.Spanned;
import android.widget.RemoteViews;

public class MyWidgetProvider extends AppWidgetProvider {
	static DBHelper dbhelper;
	String lang;
	private static final String PREF_PREFIX_KEY = "lang_widget_";

	private static DBHelper getDBHelper(Context context) {
		DBHelper dbh = new DBHelper(context); 
//        try {
//        	dbh.openDataBase();
//	 	} catch(SQLException sqle){
//	 		throw sqle;
//	 	}
        return dbh;	
	}
	
	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager,
			int[] appWidgetIds) {
				RemoteViews remoteViews = new RemoteViews(context.getPackageName(),
				R.layout.widget_layout);
		Intent intent;
		dbhelper = getDBHelper(context);
		Calendar calendar = Calendar.getInstance();
    	SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    	SimpleDateFormat displayFormat = new SimpleDateFormat("EEEE, MMMM d");
    	String today = dateFormat.format(calendar.getTime());
    	String todayDisplay = displayFormat.format(calendar.getTime());

    	for(int widgetId : appWidgetIds) {
    		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
    	    String lang = preferences.getString("lang_widget_"+widgetId, "e");
    	    intent = new Intent(context.getApplicationContext(), ESDailyActivity.class);
    	    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    	    intent.setData(Uri.withAppendedPath(Uri.parse("esdLang://widget/id/"), String.valueOf(widgetId)));
    	    intent.putExtra("lang", lang);
    	    
//    	    String[] results = dbhelper.fetchText(today, "document", lang);
//	    	Spanned text = Html.fromHtml(extractVerse(results[0]));
//	    	StringBuilder sb = new StringBuilder(text.toString());
//	    	Spanned dateDisplay = Html.fromHtml(extractDate(results[0]));
//	    	todayDisplay = (dateDisplay!=null)? dateDisplay.toString().trim() : todayDisplay; 
//	    	remoteViews.setTextViewText(R.id.tvTodaysText, sb);
//	    	remoteViews.setTextViewText(R.id.tvTodaysDate, todayDisplay);
	    	
	    	DBHelper.VerseParts vp = dbhelper.getVerseParts(today, "document", lang);
	    	Spanned dateDisplay = Html.fromHtml(vp.date);
	    	todayDisplay = (dateDisplay!=null)? dateDisplay.toString().trim() : todayDisplay;
	    	StringBuilder text = new StringBuilder(Html.fromHtml(vp.verse).toString());
	    	remoteViews.setTextViewText(R.id.tvTodaysDate, todayDisplay);
	    	remoteViews.setTextViewText(R.id.tvTodaysText, text);
	    	
			remoteViews.setOnClickPendingIntent(R.id.tvTodaysText, PendingIntent.getActivity(context, widgetId, intent, 0));
			
			appWidgetManager.updateAppWidget(widgetId, remoteViews);
    	}

	}
	@Override
	public void onDeleted(Context context, int[] appWidgetIds) {
		for (int widgetId : appWidgetIds) {
			SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
			preferences.edit().remove(PREF_PREFIX_KEY + widgetId);
		}
	}
	
	@Override
	public void onDisabled(Context context) { 
		AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
		ComponentName thisWidget = new ComponentName(context,
				MyWidgetProvider.class);
		int[] allWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);
		for (int widgetId : allWidgetIds) {
			SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
			preferences.edit().remove(PREF_PREFIX_KEY + widgetId);
		}
	}
	



	public static void updateAppWidget(Context context,
			AppWidgetManager appWidgetManager, int mAppWidgetId, String langId) {
		Calendar calendar = Calendar.getInstance();
    	SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    	SimpleDateFormat displayFormat = new SimpleDateFormat("EEEE, MMMM d");
    	String today = dateFormat.format(calendar.getTime());
    	String todayDisplay = displayFormat.format(calendar.getTime());
		RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_layout);
		dbhelper = getDBHelper(context);

		DBHelper.VerseParts vp = dbhelper.getVerseParts(today, "document", langId);
		Spanned text = Html.fromHtml(vp.verse);
    	StringBuilder sb = new StringBuilder(text.toString());
    	remoteViews.setTextViewText(R.id.tvTodaysText, sb);
    	remoteViews.setTextViewText(R.id.tvTodaysDate, todayDisplay);
    	Intent intent = new Intent(context.getApplicationContext(), ESDailyActivity.class);
	    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
	    intent.setData(Uri.withAppendedPath(Uri.parse("esdLang://widget/id/"), String.valueOf(mAppWidgetId)));
	    intent.putExtra("lang", langId);
		remoteViews.setOnClickPendingIntent(R.id.tvTodaysText, PendingIntent.getActivity(context, mAppWidgetId, intent, 0));

        // Tell the widget manager
        appWidgetManager.updateAppWidget(mAppWidgetId, remoteViews);

	}

	
}

