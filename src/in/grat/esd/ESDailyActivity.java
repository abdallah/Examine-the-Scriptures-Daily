package in.grat.esd;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import com.markupartist.android.widget.ActionBar;
import com.markupartist.android.widget.ActionBar.Action;
import com.markupartist.android.widget.ActionBar.IntentAction;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Intent;
import android.database.SQLException;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.DatePicker;
import android.widget.TextSwitcher;
import android.widget.TextView;
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
    private TextSwitcher textSwitcher;
    
	//private Context context;
	private int mYear;
    private int mMonth;
    private int mDay;

	private DBHelper dbhelper;
	TextView tvVerse;
	TextView tvComment;
	ActionBar actionBar;
	SimpleDateFormat dateFormat;
	String today;
	String yesterday;
	String tomorrow;
	String[] currentScripture;
	static final int DATE_DIALOG_ID=0;
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        textSwitcher = (TextSwitcher)findViewById(R.id.tswitch);
        slideLeftIn = AnimationUtils.loadAnimation(this, R.anim.slide_left_in);
        slideLeftOut = AnimationUtils.loadAnimation(this, R.anim.slide_left_out);
        slideRightIn = AnimationUtils.loadAnimation(this, R.anim.slide_right_in);
        slideRightOut = AnimationUtils.loadAnimation(this, R.anim.slide_right_out);
        
        gestureDetector = new GestureDetector(new MyGestureDetector());
        gestureListener = new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                if (gestureDetector.onTouchEvent(event)) {
                    return true;
                }
                return false;
            }
        };
    
        dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        gotoToday();
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
        tvVerse = (TextView) findViewById(R.id.tvVerse);
        tvComment = (TextView) findViewById(R.id.tvComment);
        
        actionBar = (ActionBar) findViewById(R.id.actionbar);
        fetchTodaysText();
        actionBar.setHomeAction(new UpdateAction(today, R.drawable.ic_title_home_default));
        actionBar.setTitle(today);
        //actionBar.addAction(new UpdateAction(yesterday, R.drawable.actionbar_back_indicator));
        actionBar.setOnTitleClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				showDialog(DATE_DIALOG_ID);
			}
    	});
        actionBar.addAction(new IntentAction(this, createShareIntent(), R.drawable.ic_title_share_default));
    }
    
    class MyGestureDetector extends SimpleOnGestureListener {
        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            try {
                if (Math.abs(e1.getY() - e2.getY()) > SWIPE_MAX_OFF_PATH)
                    return false;
                // right to left swipe
                if(e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                	textSwitcher.setInAnimation(slideLeftIn);
                	textSwitcher.setOutAnimation(slideLeftOut);
                	textSwitcher.showNext();
                }  else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                	textSwitcher.setInAnimation(slideRightIn);
                	textSwitcher.setOutAnimation(slideRightOut);
                	fetchText(tomorrow); 
                	updateDates(tomorrow);
                	textSwitcher.setText(currentScripture[1]);
                }
            } catch (Exception e) {
                // nothing
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
					updateDates(date);
	                fetchText(date);
				} catch (ParseException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
            }
        };

    public Intent createShareIntent() { 
    	Intent intent = new Intent(Intent.ACTION_SEND);
    	intent.putExtra(Intent.EXTRA_SUBJECT, currentScripture[0]);
    	intent.putExtra(Intent.EXTRA_TEXT, currentScripture[1]);
    	
    	intent.setType("text/plain");
    	return intent;
    }
    public void fetchText(String date) {
    	String[] results = dbhelper.fetchText(date);
    	if (results!=null) {
    		//updateDates(date);
    		currentScripture = results;
    		actionBar.setTitle(today);
	    	tvVerse.setText(results[0]);
	    	tvComment.setText(results[1]);
	    	
    	} else { 
    		Log.d("ESD", "found nothing!" ); 
    	}
    }
    private void updateDates(String date) { 
    	today = date;
    	Calendar cal = Calendar.getInstance();
    	try {
    		Date d = dateFormat.parse(date);
			cal.setTime(d);
			mYear = cal.get(Calendar.YEAR);
			mMonth = cal.get(Calendar.MONTH);
			mDay = cal.get(Calendar.DAY_OF_MONTH);;
			cal.add(Calendar.DATE, -1);
	    	yesterday = dateFormat.format(cal.getTime());
	    	cal.add(Calendar.DATE, +2);
	    	tomorrow = dateFormat.format(cal.getTime());
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    private void gotoToday() {
    	Calendar calendar = Calendar.getInstance();
    	today = dateFormat.format(calendar.getTime());
    	updateDates(today);
    }
    
    public void fetchTodaysText() {
    	fetchText(today);
    }
    
    private class UpdateAction implements Action {
    	private int mDrawable;
    	private String mDate;
    	public UpdateAction(String date, int drawable) {
            mDrawable = drawable;
            mDate = date;
        }

		@Override
		public int getDrawable() {
			return mDrawable;
		}
		@Override
		public void performAction(View view) {
			fetchText(mDate);
		}
    	
    }
    
   
}