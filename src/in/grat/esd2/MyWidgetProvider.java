package in.grat.esd2;

import in.grat.esd2.R;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;

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
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.SQLException;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.text.Html;
import android.text.Spanned;
import android.widget.RemoteViews;

public class MyWidgetProvider extends AppWidgetProvider {
	static DBHelper dbhelper;
	String lang;
	private static final String PREF_PREFIX_KEY = "lang_widget_";

	private static DBHelper getDBHelper(Context context) {
		DBHelper dbh = new DBHelper(context); 
        try {
        	dbh.openDataBase();
	 	} catch(SQLException sqle){
	 		throw sqle;
	 	}
        return dbh;	
	}
	
	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager,
			int[] appWidgetIds) {
				RemoteViews remoteViews = new RemoteViews(context.getPackageName(),
				R.layout.widget_layout);
		Intent intent;
		dbhelper = getDBHelper(context);
		Calendar calendar = Calendar.getInstance();
    	SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    	SimpleDateFormat displayFormat = new SimpleDateFormat("EEEE, MMMM d");
    	String today = dateFormat.format(calendar.getTime());
    	String todayDisplay = displayFormat.format(calendar.getTime());

    	for(int widgetId : appWidgetIds) {
    		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
    	    String lang = preferences.getString("lang_widget_"+widgetId, "e");
    	    intent = new Intent(context.getApplicationContext(), ESDailyActivity.class);
    	    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    	    intent.setData(Uri.withAppendedPath(Uri.parse("esdLang://widget/id/"), String.valueOf(widgetId)));
    	    intent.putExtra("lang", lang);
    	    
    	    String[] results = dbhelper.fetchText(today, "document", lang);
	    	Spanned text = Html.fromHtml(extractVerse(results[0]));
	    	StringBuilder sb = new StringBuilder(text.toString());
	    	Spanned dateDisplay = Html.fromHtml(extractDate(results[0]));
	    	todayDisplay = (dateDisplay!=null)? dateDisplay.toString().trim() : todayDisplay; 
	    	remoteViews.setTextViewText(R.id.tvTodaysText, sb);
	    	remoteViews.setTextViewText(R.id.tvTodaysDate, todayDisplay);
	    	
			remoteViews.setOnClickPendingIntent(R.id.tvTodaysText, PendingIntent.getActivity(context, widgetId, intent, 0));
			
			appWidgetManager.updateAppWidget(widgetId, remoteViews);
    	}

	}
	@Override
	public void onDeleted(Context context, int[] appWidgetIds) {
		for (int widgetId : appWidgetIds) {
			SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
			preferences.edit().remove(PREF_PREFIX_KEY + widgetId);
		}
	}
	
	@Override
	public void onDisabled(Context context) { 
		AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
		ComponentName thisWidget = new ComponentName(context,
				MyWidgetProvider.class);
		int[] allWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);
		for (int widgetId : allWidgetIds) {
			SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
			preferences.edit().remove(PREF_PREFIX_KEY + widgetId);
		}
	}
	
	public static String extractVerse(String doc) {
		DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
    	builderFactory.setNamespaceAware(true);
    	InputSource inStream = new InputSource();

    	DocumentBuilder builder;
		try {
			inStream.setCharacterStream(new StringReader(doc));
			builder = builderFactory.newDocumentBuilder();
			Document document = builder.parse(inStream);
			NodeList nodes = document.getElementsByTagName("p");
			for (int i=0; i<nodes.getLength(); i++) {
				Node cls = nodes.item(i).getAttributes().getNamedItem("class");
				if ( cls != null && cls.getNodeValue().equalsIgnoreCase("sa")) {
					TransformerFactory transFactory = TransformerFactory.newInstance();
					Transformer transformer = transFactory.newTransformer();
					StringWriter buffer = new StringWriter();
					transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
					transformer.transform(new DOMSource(nodes.item(i)), new StreamResult(buffer));
					return buffer.toString();
				}
			}
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (TransformerConfigurationException e) {
			e.printStackTrace();
		} catch (TransformerException e) {
			e.printStackTrace();
		}
		return null;
			
	}
	
	public static String extractDate(String doc) {
		DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
    	builderFactory.setNamespaceAware(true);
    	InputSource inStream = new InputSource();

    	DocumentBuilder builder;
		try {
			inStream.setCharacterStream(new StringReader(doc));
			builder = builderFactory.newDocumentBuilder();
			Document document = builder.parse(inStream);
			NodeList nodes = document.getElementsByTagName("p");
			for (int i=0; i<nodes.getLength(); i++) {
				Node cls = nodes.item(i).getAttributes().getNamedItem("class");
				if ( cls != null && cls.getNodeValue().equalsIgnoreCase("ss")) {
					TransformerFactory transFactory = TransformerFactory.newInstance();
					Transformer transformer = transFactory.newTransformer();
					StringWriter buffer = new StringWriter();
					transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
					transformer.transform(new DOMSource(nodes.item(i)), new StreamResult(buffer));
					return buffer.toString();
				}
			}
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (TransformerConfigurationException e) {
			e.printStackTrace();
		} catch (TransformerException e) {
			e.printStackTrace();
		}
		return "not found!";
			
	}


	public static void updateAppWidget(Context context,
			AppWidgetManager appWidgetManager, int mAppWidgetId, String langId) {
		Calendar calendar = Calendar.getInstance();
    	SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    	SimpleDateFormat displayFormat = new SimpleDateFormat("EEEE, MMMM d");
    	String today = dateFormat.format(calendar.getTime());
    	String todayDisplay = displayFormat.format(calendar.getTime());
		RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_layout);
		dbhelper = getDBHelper(context);
		String[] results = dbhelper.fetchText(today, "document", langId);
    	Spanned text = Html.fromHtml(extractVerse(results[0]));
    	StringBuilder sb = new StringBuilder(text.toString());
    	remoteViews.setTextViewText(R.id.tvTodaysText, sb);
    	remoteViews.setTextViewText(R.id.tvTodaysDate, todayDisplay);
    	Intent intent = new Intent(context.getApplicationContext(), ESDailyActivity.class);
	    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
	    intent.setData(Uri.withAppendedPath(Uri.parse("esdLang://widget/id/"), String.valueOf(mAppWidgetId)));
	    intent.putExtra("lang", langId);
		remoteViews.setOnClickPendingIntent(R.id.tvTodaysText, PendingIntent.getActivity(context, mAppWidgetId, intent, 0));

        // Tell the widget manager
        appWidgetManager.updateAppWidget(mAppWidgetId, remoteViews);

	}

	
}

/* epub broken code
**

package in.grat.esd2;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import nl.siegmann.epublib.domain.Book;
import nl.siegmann.epublib.epub.EpubReader;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.preference.PreferenceManager;
import android.text.Html;
import android.widget.RemoteViews;

public class MyWidgetProvider extends AppWidgetProvider {
	EpubHelper epub;
	
	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager,
			int[] appWidgetIds) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        String filename = preferences.getString("ePubFile", "es12_E.epub");
        File ePubFile = new File(filename);
        Book book;

		AssetManager assetManager = context.getAssets();
		try {
			if (ePubFile.canRead()) { 
				book = (new EpubReader()).readEpub(new FileInputStream(filename));
			} else {
				book = (new EpubReader()).readEpub(assetManager.open("es12_E.epub")); // default
			}
			epub = new EpubHelper(book, context); 
		} catch (IOException e) {
			e.printStackTrace();
		}
		        
		RemoteViews remoteViews = new RemoteViews(context.getPackageName(),
				R.layout.widget_layout);
		Intent intent = new Intent(context.getApplicationContext(), ESDailyActivity.class);

		Calendar calendar = Calendar.getInstance();
    	SimpleDateFormat displayFormat = new SimpleDateFormat("EEEE, MMMM d");
    	String todayDisplay = displayFormat.format(calendar.getTime());
    	String data = epub.getVerseForDay(calendar.getTime());
    	remoteViews.setTextViewText(R.id.tvTodaysText, Html.fromHtml(data));
    	remoteViews.setTextViewText(R.id.tvTodaysDate, todayDisplay);
    	
    	PendingIntent pendingIntent = PendingIntent.getActivity(context, getResultCode(), intent, PendingIntent.FLAG_UPDATE_CURRENT);
		remoteViews.setOnClickPendingIntent(R.id.tvTodaysText, pendingIntent);
		appWidgetManager.updateAppWidget(appWidgetIds, remoteViews);

		// Update the widgets via the service
		//context.startService(intent);
	}
}

*/