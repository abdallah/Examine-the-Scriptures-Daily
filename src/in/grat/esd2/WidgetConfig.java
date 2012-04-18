package in.grat.esd2;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.TextView;

public class WidgetConfig extends Activity {
	static final String TAG = "ESD2WidgetConfigure";
	DBHelper dbhelper;


//    private static final String PREFS_NAME
//            = "in.grat.esd2.MyWidgetProvider";
    private static final String LANG_PREFIX_KEY = "lang_widget_";
    private static final String THEME_PREFIX_KEY = "theme_widget_";
    private static final String SIZE_PREFIX_KEY = "size_widget_";
    private static final String FONT_SIZE_PREFIX_KEY = "font_size_widget_";

    int mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
    Spinner mLang, mTheme;
    SeekBar sbFontSize;
    
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
        mTheme = (Spinner) findViewById(R.id.sp_theme);
        ArrayAdapter<CharSequence> themeAdapter = ArrayAdapter.createFromResource(
                this, R.array.themes_array, android.R.layout.simple_spinner_item);
        themeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mTheme.setAdapter(themeAdapter);
        
        sbFontSize = (SeekBar) findViewById(R.id.sbFontSize);
        sbFontSize.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
        	TextView lblFontSize = (TextView) findViewById(R.id.lblFontSize);

			@Override
			public void onProgressChanged(SeekBar sb, int size, boolean arg2) {
				lblFontSize.setText("Choose font size: " + (size + 10));
			}

			@Override
			public void onStartTrackingTouch(SeekBar arg0) {
				// TODO Auto-generated method stub		
			}

			@Override
			public void onStopTrackingTouch(SeekBar arg0) {
				// TODO Auto-generated method stub	
			}});
        
		// Bind the action for the save button.
        findViewById(R.id.save_button).setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				TextView sv = (TextView) mLang.getSelectedView().findViewById(R.id.language_id);
				String langId = sv.getText().toString();
				final Context context = getApplicationContext();
				String sTheme = (String) mTheme.getSelectedItem();
				int iFontSize = sbFontSize.getProgress() + 10;
				savePrefs(context, mAppWidgetId, langId, sTheme, iFontSize);
				
				AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
				SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
				String size = prefs.getString(SIZE_PREFIX_KEY + mAppWidgetId, "normal");
				if (size.equalsIgnoreCase("large")) {
					MyLargeWidgetProvider.updateAppWidget(context, appWidgetManager,
		                    mAppWidgetId, langId, sTheme);
				} else {
	            MyWidgetProvider.updateAppWidget(context, appWidgetManager,
	                    mAppWidgetId, langId, sTheme);
				}
	            // Make sure we pass back the original appWidgetId
	            Intent resultValue = new Intent();
	            resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
	            setResult(RESULT_OK, resultValue);
	            finish();

			}
		});
       
    }
    
    static void savePrefs(Context context, int appWidgetId, String lang, String theme, int fontSize) {
        SharedPreferences.Editor prefs = PreferenceManager.getDefaultSharedPreferences(context).edit();
        prefs.putString(LANG_PREFIX_KEY + appWidgetId, lang);
        prefs.putString(THEME_PREFIX_KEY + appWidgetId, theme);
        prefs.putInt(FONT_SIZE_PREFIX_KEY + appWidgetId, fontSize);
        prefs.commit();
    }

	static String loadLangPref(Context context, int appWidgetId) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		String prefix = prefs.getString(LANG_PREFIX_KEY + appWidgetId, null);
		if (prefix != null) {
			return prefix;
		} else {
			return "E";
		}
	}
	
	
}
