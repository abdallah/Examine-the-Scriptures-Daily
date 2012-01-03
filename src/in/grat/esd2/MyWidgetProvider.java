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
import android.content.Context;
import android.content.Intent;
import android.database.SQLException;
import android.text.Html;
import android.text.Spanned;
import android.widget.RemoteViews;

public class MyWidgetProvider extends AppWidgetProvider {
	DBHelper dbhelper;
	
	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager,
			int[] appWidgetIds) {
		dbhelper = new DBHelper(context); 
        try {
        	dbhelper.openDataBase();
	 	} catch(SQLException sqle){
	 		throw sqle;
	 	}
		RemoteViews remoteViews = new RemoteViews(context.getPackageName(),
				R.layout.widget_layout);
		Intent intent = new Intent(context.getApplicationContext(), ESDailyActivity.class);

		Calendar calendar = Calendar.getInstance();
    	SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    	SimpleDateFormat displayFormat = new SimpleDateFormat("EEEE, MMMM d");
    	String today = dateFormat.format(calendar.getTime());
    	String todayDisplay = displayFormat.format(calendar.getTime());
    	String[] results = dbhelper.fetchText(today, "document");
    	Spanned text = Html.fromHtml(extractVerse(results[0]));
    	StringBuilder sb = new StringBuilder(text.toString());
    	remoteViews.setTextViewText(R.id.tvTodaysText, sb);
    	remoteViews.setTextViewText(R.id.tvTodaysDate, todayDisplay);
    	
    	PendingIntent pendingIntent = PendingIntent.getActivity(context, getResultCode(), intent, PendingIntent.FLAG_UPDATE_CURRENT);
		remoteViews.setOnClickPendingIntent(R.id.tvTodaysText, pendingIntent);
		appWidgetManager.updateAppWidget(appWidgetIds, remoteViews);

		// Update the widgets via the service
		//context.startService(intent);
	}
	
	public String extractVerse(String doc) {
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
		return "not found!";
			
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