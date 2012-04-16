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
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.text.Html;
import android.text.Spannable;

import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.URLSpan;

import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.ContextThemeWrapper;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.DatePicker;
//import android.widget.TextSwitcher;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;


public class ESDailyActivity extends Activity {
	
	private GestureDetector m_GestureDetector;
	private ScaleGestureDetector m_sGestureDetector;
	private float mScaleFactor = 1.f;

	View.OnTouchListener gestureListener;
	Context ctx;
	private Animation slideLeftIn, slideLeftOut, slideRightIn, slideRightOut;
    //private TextSwitcher textSwitcher;
    private ViewFlipper flipper;
    public String mLang, mTheme;
    
	//private Context context;
	private int mYear, mMonth, mDay;

//	WebView wv1; 
//	WebView wv2;
	TextView tv1, tv2, tvVerse1, tvVerse2, tvDate1, tvDate2;
	boolean otherView;
	SimpleDateFormat dateFormat, displayFormat;
	String today, currentDate, nextDate, prevDate, currentScripture;
	static final int DATE_DIALOG_ID=0;
	
	SharedPreferences preferences;
	
	ViewConfiguration vc;
	int SWIPE_MIN_DISTANCE, SWIPE_MAX_OFF_PATH, SWIPE_THRESHOLD_VELOCITY;
	
	static ESDailyActivity ACTIVITY;
	static PendingIntent RESTART_INTENT;
	private static final int REQUEST_PICK_FILE = 1;
	private static final int REQUEST_PREFERENCES = 2;
    private static final String PREF_PREFIX_KEY = "lang_widget_";

	DBHelper dbHelper;
	ProgressDialog dialog;
	
	public void initUI() {
		ViewConfiguration vc = ViewConfiguration.get(getApplicationContext());
    	SWIPE_MIN_DISTANCE = vc.getScaledTouchSlop();
        SWIPE_MAX_OFF_PATH = vc.getScaledTouchSlop();
    	SWIPE_THRESHOLD_VELOCITY = vc.getScaledMinimumFlingVelocity();
        
        slideLeftIn = AnimationUtils.loadAnimation(this, R.anim.slide_left_in);
        slideLeftOut = AnimationUtils.loadAnimation(this, R.anim.slide_left_out);
        slideRightIn = AnimationUtils.loadAnimation(this, R.anim.slide_right_in);
        slideRightOut = AnimationUtils.loadAnimation(this, R.anim.slide_right_out);
        
        m_GestureDetector = new GestureDetector(getApplicationContext(), new MyGestureDetector());
        m_sGestureDetector = new ScaleGestureDetector(getApplicationContext(), new ScaleListener());

        gestureListener = new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) { 
            	int action = event.getAction();
            	Log.d("ESD", "Action: "+action);
                return (m_sGestureDetector.onTouchEvent(event) && m_GestureDetector.onTouchEvent(event) || v.onTouchEvent(event));
            }
        };
        
        
        dbHelper = new DBHelper(getApplicationContext());
    	
        dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        displayFormat = new SimpleDateFormat("EEEE, MMMM d");
        

        
        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        if (getIntent().hasExtra("lang")) {
        	String widgetId = getIntent().getDataString().replaceAll(".*/(\\d+)", "$1");
        	mLang = preferences.getString(PREF_PREFIX_KEY + widgetId, null);
        }
        if (mLang==null) { mLang = preferences.getString("lang", "e"); }
        mTheme = preferences.getString("theme", "normal");
        if (mTheme.equalsIgnoreCase("normal")) { 
        	//this.setTheme(android.R.style.Theme_NoTitleBar);
        } else if (mTheme.equalsIgnoreCase("inverted")) { 
        	this.setTheme(android.R.style.Theme_Black_NoTitleBar);
        }

        setContentView(R.layout.main);
        flipper = (ViewFlipper) findViewById(R.id.flipper);
        
        tvVerse1 = (TextView) this.findViewById(R.id.tvVerse1);
        tvVerse2 = (TextView) this.findViewById(R.id.tvVerse2);
        tvDate1 = (TextView) this.findViewById(R.id.tvDate1);
        tvDate2 = (TextView) this.findViewById(R.id.tvDate2);
        tv1 = (TextView) this.findViewById(R.id.tvDisplay1);
        tv2 = (TextView) this.findViewById(R.id.tvDisplay2);
        float zoomLevel = 16.0f;
        try {
        	 zoomLevel = preferences.getFloat("zoom", 16.0f);
        	 preferences.edit().putFloat("zoom", zoomLevel).commit();
        } catch (Exception e) {
        	Log.e("ESD11", e.getMessage());
        }
        TextView[] tvs = { tv1, tv2, tvVerse1, tvVerse2, tvDate1, tvDate2 };
        for (TextView tv : tvs) {
        	if (tv == tvDate1 || tv == tvDate2) {
        		tv.setTextSize(zoomLevel+2);
        	} else {
        		tv.setTextSize(zoomLevel);
        	}
        	tv.setMovementMethod(LinkMovementMethod.getInstance());
        	tv.setOnTouchListener(gestureListener);
        }
		otherView = false;
	}
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        ACTIVITY = this;
        ctx = this;
        RESTART_INTENT = PendingIntent.getActivity(this.getBaseContext(), 0, new Intent(getIntent()), getIntent().getFlags());
        initUI();
        super.onCreate(savedInstanceState);

        gotoToday();
    }
    
    @Override
    protected void onDestroy() {
    	super.onDestroy();
        if (dbHelper != null) {
        	dbHelper.close();
        }
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
        case R.id.mnuPrefs: 
        	Intent p = new Intent(ESDailyActivity.this, PrefsActivity.class);
			startActivityForResult(p, REQUEST_PREFERENCES);
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
					dialog = ProgressDialog.show(ctx, "Reading ePub file", "Please wait...", true);
					importEPub(f.getPath());
					
				}
				break;
			case REQUEST_PREFERENCES:
				preferences = PreferenceManager.getDefaultSharedPreferences(this);
		        String newTheme = preferences.getString("theme", "normal");
		        String newLang = preferences.getString("lang", mLang);
		        if (!newTheme.equalsIgnoreCase(mTheme) || !newLang.equalsIgnoreCase(mLang)) { 
		        	restartApp();
		        }
				break;
			}
		}
	}
    
    
	public void importEPub(final String path) {
		Runnable runnable = new Runnable() {
			@Override
			public void run() {
				EpubHelper epub = new EpubHelper(path, ctx);
				epub.exportToDB();
				handler.sendEmptyMessage(0);
			}
		};
		new Thread(runnable).start();
		
	}
    
    private Handler handler = new Handler() { 
    	@Override
    	public void handleMessage(Message msg) {
    		dialog.dismiss();
    	}
    };
    
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
   


	class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
	    @Override
	    public boolean onScale(ScaleGestureDetector detector) {
	        mScaleFactor *= detector.getScaleFactor();
	        
	        // Don't let the object get too small or too large.
	        mScaleFactor = Math.max(8.0f, Math.min(mScaleFactor, 25.0f));
//	        if (mScaleFactor > 1) {
//	        	Log.d("ESD2", "Zooming in, enlarge font");
//	        } else { 
//	        	Log.d("ESD2", "Zooming out, zagher");
//	        }
	        TextView[] tvs = { tv1, tv2, tvVerse1, tvVerse2, tvDate1, tvDate2 };
	        for (TextView tv : tvs) {
	        	if (tv == tvDate1 || tv == tvDate2) {
	        		tv.setTextSize(mScaleFactor+2);
	        	} else {
	        		tv.setTextSize(mScaleFactor);
	        	}
	        	tv.setMovementMethod(LinkMovementMethod.getInstance());
	        	tv.setOnTouchListener(gestureListener);
	        }
	        
	    	preferences = PreferenceManager.getDefaultSharedPreferences(ctx);
	    	float zoomLevel = mScaleFactor;
	    	Log.d("ESD2", "ZoomLevel: "+zoomLevel);
	        preferences.edit().putFloat("zoom", zoomLevel).commit();

	        return true;
	    }
	}
	
    @Override
    public boolean onTouchEvent(MotionEvent event) {
    	return m_sGestureDetector.onTouchEvent(event) && m_GestureDetector.onTouchEvent(event);
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
    	Intent intent = new Intent(Intent.ACTION_SEND);
    	intent.putExtra(Intent.EXTRA_SUBJECT, "Today's Daily Scripture");
    	intent.putExtra(Intent.EXTRA_TEXT, currentScripture);
    	
    	intent.setType("text/plain");
    	return intent;
    }
    
    public static void restartApp() {
    	AlarmManager mgr = (AlarmManager)ACTIVITY.getSystemService(Context.ALARM_SERVICE);
    	mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 1000, RESTART_INTENT);
    	System.exit(2);
    }
    
    public void fetchText(String date) {
//    	String[] data = dbHelper.fetchText(date, "document", this.mLang);
    	DBHelper.VerseParts vp = dbHelper.getVerseParts(date, "document", mLang);
    	
    	if (dbHelper.PartsExtractedOK) {
    		StringBuilder sb = new StringBuilder(Html.fromHtml(vp.verse).toString());
    		sb.append(Html.fromHtml(vp.text).toString());
    		currentScripture = sb.toString();
    		if (!otherView) {
    			tvDate1.setText(Html.fromHtml(vp.date).toString().trim());
    			tvVerse1.setText(rearrangeSpans(vp.verse));
    			tv1.setText(rearrangeSpans(vp.text));
    		} else {
    			tvDate2.setText(Html.fromHtml(vp.date).toString().trim());
    			tvVerse2.setText(rearrangeSpans(vp.verse));
    			tv2.setText(rearrangeSpans(vp.text));
    		}
    	} else { 
    		Toast.makeText(ctx, "Date not found!", 3000);
    		Log.d("ESD", "found nothing!" ); 
    	}
    }
    
    private Spannable rearrangeSpans(String text) {
    	Spannable sText = (Spannable) Html.fromHtml(text);
    	int foreColor = tv1.getTextColors().getDefaultColor();
    	
    	URLSpan[] spans = sText.getSpans(0, sText.length(), URLSpan.class);
    	for (URLSpan span : spans) {
    	    int x = sText.getSpanStart(span);
    	    int y = sText.getSpanEnd(span);
    	    int flags = sText.getSpanFlags(span);
    	    String url = span.getURL();
    	    sText.removeSpan(span);
    	    InternalURLSpan iurlSpan = new InternalURLSpan(url);
       	    sText.setSpan(iurlSpan, x, y, flags);
       	    sText.setSpan(new ForegroundColorSpan(foreColor), x, y, flags);
    	}
    	return sText;
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
    
    class InternalURLSpan extends ClickableSpan {  
        OnClickListener mListener;
        Uri mUri;
    	String mFragment;
    	

        public InternalURLSpan(String Url) {
        	this.mUri = Uri.parse(Url);
	    	this.mFragment = this.mUri.getFragment();

        }

		@Override  
        public void onClick(View widget) {  
			DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
	    	builderFactory.setNamespaceAware(true);
	    	String msg = "";
	    	
	    	DocumentBuilder builder;
			try {
				builder = builderFactory.newDocumentBuilder();
				String[] refDoc = dbHelper.fetchText(currentDate, "references", mLang);
				Document document = builder.parse(new ByteArrayInputStream(refDoc[0].getBytes()));
				TransformerFactory transFactory = TransformerFactory.newInstance();
				Transformer transformer = transFactory.newTransformer();
				StringWriter buffer = new StringWriter();
				transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
				
				Node node = document.getElementById(this.mFragment).getParentNode();
				if (this.mFragment.startsWith("pcitation")==true) {
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
        }  
    }  
    
/*    private class WVC extends WebViewClient {
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
				String[] refDoc = this.dbHelper.fetchText(currentDate, "references", lang);
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
*/
	
   
}