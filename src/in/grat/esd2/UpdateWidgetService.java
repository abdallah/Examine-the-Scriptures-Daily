package in.grat.esd2;

import in.grat.esd2.R;

import java.util.Random;

import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import android.widget.RemoteViews;

public class UpdateWidgetService extends Service {
	@Override
	public void onStart(Intent intent, int startId) {
		Log.i("UpdateWidgetService", "Called");
		// Create some random data
		String fakeUpdate = null;
		Random random = new Random();

		AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this
				.getApplicationContext());

		int[] appWidgetIds = intent
				.getIntArrayExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS);
		if (appWidgetIds.length > 0) {
			for (int widgetId : appWidgetIds) {
				int nextInt = random.nextInt(100);
				fakeUpdate = "Random: " + String.valueOf(nextInt);
				RemoteViews remoteViews = new RemoteViews(getPackageName(),
						R.layout.widget_layout);
				remoteViews.setTextViewText(R.id.tvTodaysText, fakeUpdate);
				appWidgetManager.updateAppWidget(widgetId, remoteViews);
			}
			stopSelf();
		}
		super.onStart(intent, startId);
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
}