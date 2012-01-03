package in.grat.esd2;

import java.util.Calendar;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

public class AlarmReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		Log.d("ESD11", "Received alarm!");
		Toast.makeText(context, "ALARMA!", Toast.LENGTH_SHORT);
	}
	
	public static void setAlarm(Context context) {
		setAlarm(context, 6, 0); // default at 6AM!
	}
	public static void setAlarm(Context context, int hour, int minute) {
		Calendar updateTime = Calendar.getInstance();
		updateTime.set(Calendar.HOUR_OF_DAY, hour);
		updateTime.set(Calendar.MINUTE, minute);

		AlarmManager alarms = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		alarms.setInexactRepeating(AlarmManager.RTC_WAKEUP,
				updateTime.getTimeInMillis(), AlarmManager.INTERVAL_DAY,
				getPendingIntent(context));
	}
	public static void cancelAlarm(Context context) {
		AlarmManager mgr = (AlarmManager) context
				.getSystemService(Context.ALARM_SERVICE);

		mgr.cancel(getPendingIntent(context));
	}
	private static PendingIntent getPendingIntent(Context context) {
		Intent i = new Intent(context, AlarmReceiver.class);
		return (PendingIntent.getBroadcast(context, 0, i, 0));
	}
	
	
}
