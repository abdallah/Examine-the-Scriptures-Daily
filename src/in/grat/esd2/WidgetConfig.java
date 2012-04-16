package in.grat.esd2;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.SQLException;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.TextView;

public class WidgetConfig extends Activity {
	static final String TAG = "ESD2WidgetConfigure";
	DBHelper dbhelper;


    private static final String PREFS_NAME
            = "in.grat.esd2.MyWidgetProvider";
    private static final String PREF_PREFIX_KEY = "lang_widget_";

    int mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
    Spinner mLang;
    
    public WidgetConfig() {
        super();
    }

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setResult(RESULT_CANCELED);
        setContentView(R.layout.appwidget_config);
     
        // Find the widget id from the intent. 
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            mAppWidgetId = extras.getInt(
                    AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
        }
        // If they gave us an intent without the widget id, just bail.
        if (mAppWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish();
        }
        
        dbhelper = new DBHelper(getBaseContext()); 
//        try {
//        	dbhelper.openDataBase();
//	 	} catch(SQLException sqle){
//	 		throw sqle;
//	 	}
        Cursor cursor = dbhelper.fetchLanguages();
        startManagingCursor(cursor);
        mLang = (Spinner)findViewById(R.id.spin_lang);
        SimpleCursorAdapter adapter = new SimpleCursorAdapter(this, R.layout.language_list_item, cursor, 
        		new String[]{"_id", "name"}, 
        		new int[]{R.id.language_id, R.id.language_name}
        		);
        mLang.setAdapter(adapter);
        String langId = loadLangPref(getApplicationContext(), mAppWidgetId);
        for (int i=0; i<mLang.getCount(); i++) {
        	Cursor v = (Cursor) mLang.getItemAtPosition(i);
        	String lId = v.getString(v.getColumnIndex("_id"));
        	if (lId.equalsIgnoreCase(langId)) { 
        		mLang.setSelection(i);
        	}
        }
        
		// Bind the action for the save button.
        findViewById(R.id.save_button).setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				TextView sv = (TextView) mLang.getSelectedView().findViewById(R.id.language_id);
				String langId = sv.getText().toString();
				final Context context = getApplicationContext();

				saveLanguagePref(context, mAppWidgetId, langId);
				
				AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
	            MyWidgetProvider.updateAppWidget(context, appWidgetManager,
	                    mAppWidgetId, langId);

	            // Make sure we pass back the original appWidgetId
	            Intent resultValue = new Intent();
	            resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
	            setResult(RESULT_OK, resultValue);
	            finish();

			}
		});
       
    }
    
    static void saveLanguagePref(Context context, int appWidgetId, String lang) {
        SharedPreferences.Editor prefs = PreferenceManager.getDefaultSharedPreferences(context).edit();
        prefs.putString(PREF_PREFIX_KEY + appWidgetId, lang);
        prefs.commit();
    }

	static String loadLangPref(Context context, int appWidgetId) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		String prefix = prefs.getString(PREF_PREFIX_KEY + appWidgetId, null);
		if (prefix != null) {
			return prefix;
		} else {
			return "E";
		}
	}
	
	
}
