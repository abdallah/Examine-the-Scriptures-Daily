package in.grat.esd2;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
//import android.util.Log;

public class DBHelper extends SQLiteOpenHelper {
		 
	//The Android's default system path of your application database.
	static final String DATABASE_NAME = "esd";
    static final String TABLE_NAME = "esd";
    static final String LANG_TABLE_NAME = "languages";
    static final String KEY_DATE = "date";
    static final String KEY_TEXT = "dailytext";
    static final String KEY_REFS = "refs";
    static final String KEY_LANG = "lang";
	
    private static final String DATABASE_PATH = "/data/data/in.grat.esd2/databases/";
	    
	private SQLiteDatabase myDataBase; 
	private final Context myContext;
	 
    /**
     * Constructor
     * Takes and keeps a reference of the passed context in order to access to the application assets and resources.
     * @param context
     */
    public DBHelper(Context context) {
    	super(context, DATABASE_NAME, null, 1);
        this.myContext = context;
        String myPath = DATABASE_PATH + DATABASE_NAME;
        try {
			createDataBase();
			myDataBase = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READONLY);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
    }
 
    /**
     * Creates a empty database on the system and rewrites it with your own database.
     * */
    public void createDataBase() throws IOException{
    	boolean dbExist = checkDataBase();
    	if(dbExist){
    		//do nothing - database already exist
    	} else {
           	try {
           		this.getReadableDatabase();
    			copyDataBase();
    		} catch (IOException e) {
        		throw new Error("Error copying database");
        	}
    	}
    }
 
    /**
     * Check if the database already exist to avoid re-copying the file each time you open the application.
     * @return true if it exists, false if it doesn't
     */
    private boolean checkDataBase(){
    	SQLiteDatabase checkDB = null;
    	try{
    		String myPath = DATABASE_PATH + DATABASE_NAME;
    		checkDB = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READONLY);
 
    	} catch (SQLiteException e){
    		//database does't exist yet. 
    	}
    	if(checkDB != null){
    		checkDB.close();
    	}
    	return checkDB != null ? true : false;
    }
 
    /**
     * Copies your database from your local assets-folder to the just created empty database in the
     * system folder, from where it can be accessed and handled.
     * This is done by transfering bytestream.
     * */
    private void copyDataBase() throws IOException{
    	//Open your local db as the input stream
    	InputStream myInput = myContext.getAssets().open(DATABASE_NAME);
    	// Path to the just created empty db
    	String outFileName = DATABASE_PATH + DATABASE_NAME;
    	//Open the empty db as the output stream
    	OutputStream myOutput = new FileOutputStream(outFileName);
    	//transfer bytes from the inputfile to the outputfile
    	byte[] buffer = new byte[1024];
    	int length;
    	while ((length = myInput.read(buffer))>0){
    		myOutput.write(buffer, 0, length);
    	}
    	//Close the streams
    	myOutput.flush();
    	myOutput.close();
    	myInput.close();
    }
 
    public void openDataBase() throws SQLException{
    	//Open the database
        String myPath = DATABASE_PATH + DATABASE_NAME;
    	myDataBase = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READONLY);
    }
 
    @Override
	public synchronized void close() {
    	if(myDataBase != null)
    		myDataBase.close();
   	    super.close();
	}
 
	@Override
	public void onCreate(SQLiteDatabase db) {
		try {
			createDataBase();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// db.execSQL("CREATE TABLE "+TABLE_NAME+" (_id integer primary key autoincrement, "+KEY_DATE+" TEXT, "+KEY_TEXT+" TEXT, "+KEY_REFS+" TEXT);");
	}
	public static List<String> GetColumns(SQLiteDatabase db, String tableName) {
	    List<String> ar = null;
	    Cursor c = null;
	    try {
	        c = db.rawQuery("select * from " + tableName + " limit 1", null);
	        if (c != null) {
	            ar = new ArrayList<String>(Arrays.asList(c.getColumnNames()));
	        }
	    } catch (Exception e) {
	        //Log.v(tableName, e.getMessage(), e);
	        e.printStackTrace();
	    } finally {
	        if (c != null)
	            c.close();
	    }
	    return ar;
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		List<String> columns = GetColumns(db, TABLE_NAME);
		if (! columns.contains(new String("lang"))) 
			db.execSQL("ALTER TABLE "+TABLE_NAME+" ADD COLUMN lang TEXT DEFAULT 'E';");
	}

	public String[] fetchText(String date, String type, String lang) {
		String doc = type.equalsIgnoreCase("document") ? KEY_TEXT : KEY_REFS;
		String[] results = null;
		try {
			Cursor mCursor = myDataBase.query(TABLE_NAME, 
				new String[] { KEY_DATE, doc },
				KEY_DATE + "='"+date+"' AND "+ KEY_LANG + "='"+lang.toLowerCase()+"'", 
				null, null, null, null, null);
			
			if (mCursor.getCount() < 1) {
				mCursor.close();
			} else {
				mCursor.moveToFirst();
				results = new String[] { mCursor.getString(mCursor
						.getColumnIndex(doc)) };
				mCursor.close();
			}
			
		} catch (NullPointerException e) {
			e.printStackTrace();
		}
		return results;

	}
	
	public Cursor fetchLanguages() throws SQLException {
		String q =  "SELECT DISTINCT id as _id, name FROM "+LANG_TABLE_NAME+" INNER JOIN "+TABLE_NAME+" on "+LANG_TABLE_NAME+".id = "+TABLE_NAME+".lang COLLATE NOCASE";
		Cursor mCursor = myDataBase.rawQuery(q, null);
		if (mCursor != null) {
			mCursor.moveToFirst();
		}
		return mCursor;
	}

	
}
