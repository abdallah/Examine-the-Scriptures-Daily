package in.grat.esd2;

import in.grat.esd2.R;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.ContextThemeWrapper;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.webkit.WebSettings;
import android.webkit.WebSettings.RenderPriority;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.DatePicker;
//import android.widget.TextSwitcher;
import android.widget.Toast;
import android.widget.ViewFlipper;


public class ESDailyActivity extends Activity {
	
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

	WebView wv1; 
	WebView wv2;
	boolean otherView;
	SimpleDateFormat dateFormat;
	SimpleDateFormat displayFormat;
	String today;
	String currentDate;
	String nextDate;
	String prevDate;
	String currentScripture;
	static final int DATE_DIALOG_ID=0;
	
	SharedPreferences preferences;
	
	ViewConfiguration vc;
	int SWIPE_MIN_DISTANCE;
	int SWIPE_MAX_OFF_PATH;
	int SWIPE_THRESHOLD_VELOCITY;
	
	static ESDailyActivity ACTIVITY;
	static PendingIntent RESTART_INTENT;
	private static final int REQUEST_PICK_FILE = 1;

	DBHelper dbHelper;		
	
	public void initUI() {
		ViewConfiguration vc = ViewConfiguration.get(getApplicationContext());
    	SWIPE_MIN_DISTANCE = vc.getScaledTouchSlop();
        SWIPE_MAX_OFF_PATH = vc.getScaledTouchSlop();
    	SWIPE_THRESHOLD_VELOCITY = vc.getScaledMinimumFlingVelocity();
        
        slideLeftIn = AnimationUtils.loadAnimation(this, R.anim.slide_left_in);
        slideLeftOut = AnimationUtils.loadAnimation(this, R.anim.slide_left_out);
        slideRightIn = AnimationUtils.loadAnimation(this, R.anim.slide_right_in);
        slideRightOut = AnimationUtils.loadAnimation(this, R.anim.slide_right_out);
        
        gestureDetector = new GestureDetector(getApplicationContext(), new MyGestureDetector());
        gestureListener = new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) { 
            	int action = event.getAction();
            	Log.d("ESD", "Action: "+action);
                return (gestureDetector.onTouchEvent(event) || v.onTouchEvent(event));
            }
        };
        
        dbHelper = new DBHelper(getApplicationContext());
        dbHelper.openDataBase();
    	
        dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        displayFormat = new SimpleDateFormat("EEEE, MMMM d");
        setContentView(R.layout.main);
        flipper = (ViewFlipper) findViewById(R.id.flipper);
        wv1 = (WebView) this.findViewById(R.id.webView1);
        wv2 = (WebView) this.findViewById(R.id.webView2);
        
        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        int zoomLevel = preferences.getInt("zoom", 150);
		wv1.setOnTouchListener(gestureListener);
		wv2.setOnTouchListener(gestureListener);
		wv1.getSettings().setRenderPriority(RenderPriority.HIGH);
		wv1.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
		wv1.getSettings().setBuiltInZoomControls(true);
		wv2.getSettings().setBuiltInZoomControls(true);
		wv1.setInitialScale(zoomLevel);
		wv2.setInitialScale(zoomLevel);
		
        otherView = false;
	}
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ACTIVITY = this;
        RESTART_INTENT = PendingIntent.getActivity(this.getBaseContext(), 0, new Intent(getIntent()), getIntent().getFlags());
        initUI();
        gotoToday();
    }
    
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
    	super.onConfigurationChanged(newConfig);
    	initUI();
    	setDates(currentDate);
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
        case R.id.mnuImport:
        	Intent intent = new Intent(ESDailyActivity.this, FilePickerActivity.class);
			ArrayList<String> extensions = new ArrayList<String>();
			extensions.add(".epub");
			intent.putExtra(FilePickerActivity.EXTRA_ACCEPTED_FILE_EXTENSIONS, extensions);
			intent.putExtra(FilePickerActivity.EXTRA_FILE_PATH, "/sdcard/download");
			startActivityForResult(intent, REQUEST_PICK_FILE);
			return true;
//        case R.id.mnuPrefs:
//        	Intent p = new Intent(ESDailyActivity.this, PrefsActivity.class);
//			startActivity(p);
        default:
            return super.onOptionsItemSelected(item);
        }
    }
    
    @Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == RESULT_OK) {
			switch (requestCode) {
			case REQUEST_PICK_FILE:
				if (data.hasExtra(FilePickerActivity.EXTRA_FILE_PATH)) {
					// Get the file path
					File f = new File(
							data.getStringExtra(FilePickerActivity.EXTRA_FILE_PATH));
					Toast.makeText(getApplicationContext(), "Please hold while I import the epub file", Toast.LENGTH_SHORT);
					EpubHelper epub = new EpubHelper(f.getPath(), getApplicationContext());
					epub.exportToDB();
					Toast.makeText(getApplicationContext(), "Import complete", Toast.LENGTH_SHORT);
				}
			}
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
                	return false; //true;
                }  else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                	flipper.setInAnimation(slideRightIn);
                	flipper.setOutAnimation(slideRightOut);
                	otherView = !otherView;
                	setDates(prevDate);
                	flipper.showPrevious();
                	return false; //true;
                }
            } catch (Exception e) {
                Log.e("ESD11", e.getMessage());
            }
            return false;
        }

		public boolean onDown(MotionEvent event) {
			return false;
		}

    }
    
    @Override
    public boolean onTouchEvent(MotionEvent event) {
    	preferences = PreferenceManager.getDefaultSharedPreferences(this);
        float zoomLevel = (otherView)? wv2.getScale()*100 : wv1.getScale()*100;
        preferences.edit().putInt("zoom", (int) zoomLevel).commit();
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
					e1.printStackTrace();
				}
            }
        };

    public Intent createShareIntent() { 
    	Spanned text = Html.fromHtml(currentScripture);
    	StringBuilder sb = new StringBuilder(text.toString());
    	Intent intent = new Intent(Intent.ACTION_SEND);
    	intent.putExtra(Intent.EXTRA_SUBJECT, "Today's Daily Scripture");
    	intent.putExtra(Intent.EXTRA_TEXT, sb.toString());
    	
    	intent.setType("text/plain");
    	return intent;
    }
    
    public static void restartApp() {
    	AlarmManager mgr = (AlarmManager)ACTIVITY.getSystemService(Context.ALARM_SERVICE);
    	mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 1000, RESTART_INTENT);
    	System.exit(2);
    }
    
    public void fetchText(String date) {
    	String[] data = dbHelper.fetchText(date, "document");
    	if (data!=null) {
    		currentScripture = data[0];
    		if (!otherView) {
    			wv1.loadDataWithBaseURL("file:///android_asset/", data[0], "text/html", "utf-8", null);
    			wv1.setWebViewClient(new WVC(currentDate, dbHelper));
    		} else {
    			wv2.loadDataWithBaseURL("file:///android_asset/", data[0], "text/html", "utf-8", null);
    			wv2.setWebViewClient(new WVC(currentDate, dbHelper));
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
			e.printStackTrace();
		}
    }
    private void gotoToday() {
    	Calendar calendar = Calendar.getInstance();
    	today = dateFormat.format(calendar.getTime());
    	setDates(today);
    }
    
    private class WVC extends WebViewClient {
    	String currentDate;
    	DBHelper dbHelper;
    	
		public WVC(String currentDate, DBHelper dbHelper) { 
			this.currentDate = currentDate;
			this.dbHelper = dbHelper;
		}
	    @Override
	    public boolean shouldOverrideUrlLoading(WebView view, String url) {
	    	Uri uri = Uri.parse(url);
	    	//String refLocation = uri.getPath().substring(15); 
	    	String fragment = uri.getFragment();
	    	
	    	DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
	    	builderFactory.setNamespaceAware(true);
	    	String msg = "";
	    	
	    	DocumentBuilder builder;
			try {
				builder = builderFactory.newDocumentBuilder();
				String[] refDoc = this.dbHelper.fetchText(currentDate, "references");
				Document document = builder.parse(new ByteArrayInputStream(refDoc[0].getBytes()));
				TransformerFactory transFactory = TransformerFactory.newInstance();
				Transformer transformer = transFactory.newTransformer();
				StringWriter buffer = new StringWriter();
				transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
				
				Node node = document.getElementById(fragment).getParentNode();
				if (fragment.startsWith("pcitation")==true) {
					for(Node cNode=node.getParentNode();
						    cNode!=null; cNode=cNode.getNextSibling()){
						transformer.transform(new DOMSource(cNode), new StreamResult(buffer));
					}
					msg += buffer.toString();
		
				} else { 
					transformer.transform(new DOMSource(node), new StreamResult(buffer));
					msg = buffer.toString();
				}

				Log.d("epublib", "Element: "+msg);
				msg = msg.replace("^", "")
						.replace("***", "");
				
				AlertDialog.Builder dbld = new AlertDialog.Builder(
						new ContextThemeWrapper(ESDailyActivity.this, R.style.AlertDialogCustom));
				dbld.setMessage(Html.fromHtml(msg))
					.setCancelable(true);
				AlertDialog alert = dbld.create();
				
				alert.show();
				//Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
			} catch (ParserConfigurationException e) {
				e.printStackTrace();
			} catch (SAXException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}  catch (NullPointerException e) {
				e.printStackTrace();
			} catch (TransformerConfigurationException e) {
				e.printStackTrace();
			} catch (TransformerException e) {
				e.printStackTrace();
			}
  
	        return true;
	    }
	}
   
}