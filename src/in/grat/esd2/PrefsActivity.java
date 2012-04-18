package in.grat.esd2;


import in.grat.esd2.R;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;


public class PrefsActivity extends Activity {
	DBHelper dbhelper; 
	Spinner mLang, mTheme;
	SharedPreferences preferences;
	
	// TimePreference alarmTime;
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.preferences_layout);
	    Context context = getApplicationContext();
	    preferences = PreferenceManager.getDefaultSharedPreferences(context);
	    String langId = preferences.getString("lang", "e");
	    String themeId = preferences.getString("theme", "normal");
	    
	    dbhelper = new DBHelper(getBaseContext()); 
//        try {
//        	dbhelper.openDataBase();
//	 	} catch(SQLException sqle){
//	 		throw sqle;
//	 	}
        Cursor cursor = dbhelper.fetchLanguages();
        startManagingCursor(cursor);
        
        mLang = (Spinner)findViewById(R.id.sp_language);
        SimpleCursorAdapter langAdapter = new SimpleCursorAdapter(this, R.layout.language_list_item, cursor, 
        		new String[]{"_id", "name"}, 
        		new int[]{R.id.language_id, R.id.language_name}
        		);
        mLang.setAdapter(langAdapter);
        for (int i=0; i<mLang.getCount(); i++) {
        	Cursor v = (Cursor) mLang.getItemAtPosition(i);
        	String lId = v.getString(v.getColumnIndex("_id"));
        	if (lId.equalsIgnoreCase(langId)) { 
        		mLang.setSelection(i); break;
        	}
        }
        mTheme = (Spinner) findViewById(R.id.sp_theme);
        ArrayAdapter<CharSequence> themeAdapter = ArrayAdapter.createFromResource(
                this, R.array.themes_array, android.R.layout.simple_spinner_item);
        themeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mTheme.setAdapter(themeAdapter);
        for (int i=0; i<mTheme.getCount(); i++) {
        	String tId = (String) mTheme.getItemAtPosition(i);
        	if (tId.equalsIgnoreCase(themeId)) {
        		mTheme.setSelection(i);
        	}
        }

        Button saveButton = (Button) findViewById(R.id.save_prefs);
        saveButton.setOnClickListener(new View.OnClickListener() {	
			@Override
			public void onClick(View v) {
				savePreferences();
				Intent extra = new Intent();
				setResult(RESULT_OK, extra);
				finish();
			}
		});

	}
	
	public void savePreferences() { 
		SharedPreferences.Editor prefs = preferences.edit();
		
		TextView tvId = (TextView) mLang.getSelectedView().findViewById(R.id.language_id);
		String lId = tvId.getText().toString();
		prefs.putString("lang", lId);
		
		String sTheme = (String) mTheme.getSelectedItem();
		prefs.putString("theme", sTheme);
		
		prefs.commit();
		Toast.makeText(this, "Saved", 1000);
	}
	
}
