package in.grat.esd;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Intent;
import android.database.SQLException;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.DatePicker;
//import android.widget.TextSwitcher;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;


public class ESDailyActivity extends Activity {
	private static final int SWIPE_MIN_DISTANCE = 120;
    private static final int SWIPE_MAX_OFF_PATH = 250;
	private static final int SWIPE_THRESHOLD_VELOCITY = 200;
	private GestureDetector gestureDetector;
	View.OnTouchListener gestureListener;
	private Animation slideLeftIn;
	private Animation slideLeftOut;
	private Animation slideRightIn;
    private Animation slideRightOut;
    //private TextSwitcher textSwitcher;
    private ViewFlipper flipper;
    
	//private Context context;
	private int mYear;
    private int mMonth;
    private int mDay;

	private DBHelper dbhelper;
	TextView tvVerse;
	TextView tvVerse2;
	TextView tvComment;
	TextView tvComment2;
	TextView tvTitle;
	TextView tvTitle2;
	boolean otherView;
	SimpleDateFormat dateFormat;
	SimpleDateFormat displayFormat;
	String today;
	String currentDate;
	String nextDate;
	String prevDate;
	String[] currentScripture;
	static final int DATE_DIALOG_ID=0;
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        slideLeftIn = AnimationUtils.loadAnimation(this, R.anim.slide_left_in);
        slideLeftOut = AnimationUtils.loadAnimation(this, R.anim.slide_left_out);
        slideRightIn = AnimationUtils.loadAnimation(this, R.anim.slide_right_in);
        slideRightOut = AnimationUtils.loadAnimation(this, R.anim.slide_right_out);
        
        gestureDetector = new GestureDetector(getApplicationContext(), new MyGestureDetector());
        gestureListener = new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                if (gestureDetector.onTouchEvent(event)) {
                    return true;
                }
                return false;
            }
        };
    
        dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        displayFormat = new SimpleDateFormat("EEEE, MMMM d");
        dbhelper = new DBHelper(this); 
        try {
        	dbhelper.createDataBase();
        	dbhelper.openDataBase();
	 	} catch (IOException ioe) {
	 		throw new Error("Unable to create database");
	 	} catch(SQLException sqle){
	 		throw sqle;
	 	}
        setContentView(R.layout.main);
        flipper = (ViewFlipper) findViewById(R.id.flipper);
        tvVerse = (TextView) findViewById(R.id.tvVerse);
        tvComment = (TextView) findViewById(R.id.tvComment);
        tvVerse2 = (TextView) findViewById(R.id.tvVerse2);
        tvComment2 = (TextView) findViewById(R.id.tvComment2);
        tvTitle = (TextView) findViewById(R.id.tvTitle);
        tvTitle2 = (TextView) findViewById(R.id.tvTitle2);
        tvComment.setMovementMethod(new ScrollingMovementMethod());
        tvComment2.setMovementMethod(new ScrollingMovementMethod());

        otherView = false;
        
        gotoToday();
        
 
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
        case R.id.mnuGoto:
        	showDialog(DATE_DIALOG_ID);
            return true;
        case R.id.mnuShare:
        	Intent i = createShareIntent();
        	startActivity(i);
            return true;
        case R.id.mnuToday:
        	gotoToday();
        	return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }
    class MyGestureDetector extends SimpleOnGestureListener {
        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            try {
                if (Math.abs(e1.getY() - e2.getY()) > SWIPE_MAX_OFF_PATH)
                    return false;
                // right to left swipe
                if(e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                	flipper.setInAnimation(slideLeftIn);
                	flipper.setOutAnimation(slideLeftOut);
                	otherView = !otherView;
                	setDates(nextDate);
                	flipper.showNext();

                }  else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                	flipper.setInAnimation(slideRightIn);
                	flipper.setOutAnimation(slideRightOut);
                	otherView = !otherView;
                	setDates(prevDate);
                	flipper.showPrevious();
                }
            } catch (Exception e) {
                Log.e("ESD11", e.getMessage());
            }
            return false;
        }
    }
    
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (gestureDetector.onTouchEvent(event))
	        return true;
	    else
	    	return false;
    }
    
    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
        case DATE_DIALOG_ID:
            return new DatePickerDialog(this,
                        mDateSetListener,
                        mYear, mMonth, mDay);
        }
        return null;
    }
    private DatePickerDialog.OnDateSetListener mDateSetListener =
        new DatePickerDialog.OnDateSetListener() {
			@Override
			public void onDateSet(DatePicker view, int year, 
                                  int monthOfYear, int dayOfMonth) {
				StringBuilder sb = new StringBuilder();
				sb.append(year).append("-")
					.append(monthOfYear+1)
					.append("-").append(dayOfMonth);
				try {
					String date = dateFormat.format(dateFormat.parse(sb.toString()));
					setDates(date);
				} catch (ParseException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
            }
        };

    public Intent createShareIntent() { 
    	StringBuilder sb = new StringBuilder(currentScripture[1]).append("\n--\nsent from my droid(tm)");
    	Intent intent = new Intent(Intent.ACTION_SEND);
    	intent.putExtra(Intent.EXTRA_SUBJECT, currentScripture[0]);
    	intent.putExtra(Intent.EXTRA_TEXT, sb.toString());
    	
    	intent.setType("text/plain");
    	return intent;
    }
    
    public void fetchText(String date) {
    	String displayDate = date;
    	try {
			Date d = dateFormat.parse(date);
			displayDate = displayFormat.format(d);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    	String[] results = dbhelper.fetchText(date);
    	if (results!=null) {
    		currentScripture = results;
    		if (!otherView) {
    			tvTitle.setText(displayDate);
		    	tvVerse.setText(results[0]);
		    	tvComment.setText(results[1]);
    		} else {
    			tvTitle2.setText(displayDate);
		    	tvVerse2.setText(results[0]);
		    	tvComment2.setText(results[1]);
    		}
    	} else { 
    		Toast.makeText(getApplicationContext(), "Date not found!", 3000);
    		Log.d("ESD", "found nothing!" ); 
    	}
    }
    
    private void setDates(String date) { 
    	currentDate = date;
    	Calendar cal = Calendar.getInstance();
    	try {
    		fetchText(date);
    		Date d = dateFormat.parse(date);
			cal.setTime(d);
			mYear = cal.get(Calendar.YEAR);
			mMonth = cal.get(Calendar.MONTH);
			mDay = cal.get(Calendar.DAY_OF_MONTH);;
			cal.add(Calendar.DATE, -1);
	    	prevDate = dateFormat.format(cal.getTime());
	    	cal.add(Calendar.DATE, +2);
	    	nextDate = dateFormat.format(cal.getTime());
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    private void gotoToday() {
    	Calendar calendar = Calendar.getInstance();
    	today = dateFormat.format(calendar.getTime());
    	setDates(today);
    }
    
   
}