package in.grat.esd2;


import in.grat.esd2.R;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.SQLException;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;


public class PrefsActivity extends Activity {
	DBHelper dbhelper; 
	Spinner mLang;
	SharedPreferences preferences;
	
	// TimePreference alarmTime;
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.preferences_layout);
	    Context context = getApplicationContext();
	    preferences = PreferenceManager.getDefaultSharedPreferences(context);
	    String langId = preferences.getString("lang", "e");
	    
	    dbhelper = new DBHelper(getBaseContext()); 
        try {
        	dbhelper.openDataBase();
	 	} catch(SQLException sqle){
	 		throw sqle;
	 	}
        Cursor cursor = dbhelper.fetchLanguages();
        startManagingCursor(cursor);
        
        mLang = (Spinner)findViewById(R.id.sp_language);
        SimpleCursorAdapter adapter = new SimpleCursorAdapter(this, R.layout.language_list_item, cursor, 
        		new String[]{"_id", "name"}, 
        		new int[]{R.id.language_id, R.id.language_name}
        		);
        mLang.setAdapter(adapter);
        for (int i=0; i<mLang.getCount(); i++) {
        	Cursor v = (Cursor) mLang.getItemAtPosition(i);
        	String lId = v.getString(v.getColumnIndex("_id"));
        	if (lId.equalsIgnoreCase(langId)) { 
        		mLang.setSelection(i); break;
        	}
        }

        Button saveButton = (Button) findViewById(R.id.save_prefs);
        saveButton.setOnClickListener(new View.OnClickListener() {	
			@Override
			public void onClick(View v) {
				savePreferences();
				finish();
			}
		});

	}
	
	public void savePreferences() { 
		SharedPreferences.Editor prefs = preferences.edit();
		TextView tvId = (TextView) mLang.getSelectedView().findViewById(R.id.language_id);
		String lId = tvId.getText().toString();
		prefs.putString("lang", lId);
		prefs.commit();
		Toast.makeText(this, "Saved", 1000);
	}
	
}
