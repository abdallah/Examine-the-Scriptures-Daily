package in.grat.esd2;


import in.grat.esd2.R;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;


public class PrefsActivity extends PreferenceActivity {
	TimePreference alarmTime;
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    addPreferencesFromResource(R.xml.prefs);
	    
	    alarmTime = (TimePreference) findPreference("alarm_time");
	    alarmTime.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
			@Override
			public boolean onPreferenceChange(Preference pref, Object newValue) {
				if (pref.getKey().equalsIgnoreCase("alarm_time")) {
					AlarmReceiver.setAlarm(getApplicationContext(), alarmTime.getHour(), alarmTime.getMinute());
				} else if (pref.getKey().equalsIgnoreCase("reminder")) {
					if ((Boolean) newValue==true) {
						AlarmReceiver.setAlarm(getApplicationContext(), alarmTime.getHour(), alarmTime.getMinute());
					} else {
						AlarmReceiver.cancelAlarm(getApplicationContext());
					}
				} else if (pref.getKey().equalsIgnoreCase("ePubFile")) {
					ESDailyActivity.restartApp();
				}
				return true;
			}});
	}
	
}
