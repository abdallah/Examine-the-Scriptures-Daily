package in.grat.esd;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {
		 
	//The Android's default system path of your application database.
	private static final String DATABASE_NAME = "esd11";
    private static final String DATABASE_TABLE = "esd11";
    static final String KEY_DATE = "date";
    static final String KEY_VERSE = "verse";
    static final String KEY_COMMENT = "comment";
	
    private static final String DATABASE_PATH = "/data/data/in.grat.esd/databases/";
	    
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
    }	
 
    /**
     * Creates a empty database on the system and rewrites it with your own database.
     * */
    public void createDataBase() throws IOException{
    	boolean dbExist = checkDataBase();
    	if(dbExist){
    		//do nothing - database already exist
    	} else {
        	this.getReadableDatabase();
        	try {
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
 
	}
 
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
 
	}

	public String[] fetchText(String date) {
		Cursor mCursor = myDataBase.query(DATABASE_TABLE, new String[] {
				KEY_DATE, KEY_VERSE, KEY_COMMENT },
				KEY_DATE + "='"+date+"'", null, null, null, null, null);
		//mCursor = myDataBase.rawQuery("SELECT * FROM "+DATABASE_TABLE, new String[]{});
   	if (mCursor.getCount()<1) {
    		mCursor.close();
    		return null;
    	} else {
    		mCursor.moveToFirst();
    		String[] results = new String[] {
    				mCursor.getString(mCursor.getColumnIndex(DBHelper.KEY_VERSE)), 
    				mCursor.getString(mCursor.getColumnIndex(DBHelper.KEY_COMMENT))};
    		mCursor.close();
    		return results;
    	}
    	
	}
	
}
