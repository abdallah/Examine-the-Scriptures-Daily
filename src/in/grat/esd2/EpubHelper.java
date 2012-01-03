package in.grat.esd2;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringWriter;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;

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
import org.xml.sax.SAXException;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import nl.siegmann.epublib.domain.Book;
import nl.siegmann.epublib.domain.Resource;
import nl.siegmann.epublib.epub.EpubReader;

public class EpubHelper {
	private Book book; 
	private Context context;
	public String title = "";
	SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
	SimpleDateFormat hrefFormat = new SimpleDateFormat("MM_'ES12'_.MMM");
	SimpleDateFormat displayFormat = new SimpleDateFormat("EEEE, MMMM d");
	DecimalFormat dblzero = new DecimalFormat("00");

	public EpubHelper(Book book, Context context) {
		this.book = book;
		this.context = context;
		hrefFormat = new SimpleDateFormat("MM_'ES"+getBookYear()+"'_.MMM");
	}
	public EpubHelper(String filename, Context context) {
		try {
			this.book = (new EpubReader()).readEpub(new FileInputStream(filename));
			this.context = context;
			hrefFormat = new SimpleDateFormat("MM_'ES"+getBookYear()+"'_.MMM");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	private String getBookYear() {
		this.title = this.book.getMetadata().getFirstTitle();
		String year = title.substring(title.length()-2);
		return year;
	}
	public String getSpecificDate(String day, String type) {
		Date d;
		try {
			d = dateFormat.parse(day);
			return getSpecificDate(d, type);
		} catch (ParseException e) {
			e.printStackTrace();
		} 
		return null;
	}
	
	public String  getSpecificDate(Date day, String type) { 
		String hrefDate = getHrefForDay(day, type);
		Resource r = this.book.getResources().getByHref(hrefDate);
		return new String( r.getData() );
	}
	
	private String getHrefForDay(Date day, String type) {
		int dayOfMonth;
		int monthOfYear;
		Calendar cal = Calendar.getInstance();
		cal.setTime(day);
		String hrefDate = hrefFormat.format(day).toUpperCase();
		dayOfMonth=cal.get(Calendar.DAY_OF_MONTH);
		monthOfYear=cal.get(Calendar.MONTH);
		if (type.equalsIgnoreCase("document")) { 
			hrefDate += (dayOfMonth==1) ? ".xhtml" : "-split"+dayOfMonth+".xhtml";
		} else { 
			hrefDate += (dayOfMonth==1) ? "-extracted.xhtml" : "-extracted"+dayOfMonth+".xhtml";
		}
		hrefDate = dblzero.format(monthOfYear+4)+hrefDate.substring(2);
		return hrefDate;
	}
	
	public String getVerseForDay(Date day) {
		String hrefDate = getHrefForDay(day, "document");
		Resource r = this.book.getResources().getByHref(hrefDate);
		DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
    	builderFactory.setNamespaceAware(true);
    	DocumentBuilder builder;
		try {
			builder = builderFactory.newDocumentBuilder();
			Document document = builder.parse(r.getInputStream());
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
	
	public void exportToDB() {
		SQLiteDatabase db;
		DBHelper dbHelper;		
		dbHelper = new DBHelper(this.context);
		db = dbHelper.getWritableDatabase();
		String Year = "20"+this.getBookYear();
		
		Date first;
		try {
			first = dateFormat.parse(Year+"-01-01");
			Date last = dateFormat.parse(Year+"-12-31");

			Iterator<Date> i = new DateIterator(first, last);
	    	while(i.hasNext()) { 
	    		Date d = i.next();
	    		ContentValues cv=new ContentValues();
	    		cv.put(DBHelper.KEY_DATE, dateFormat.format(d));
	    		cv.put(DBHelper.KEY_TEXT, getSpecificDate(d, "document"));
	    		cv.put(DBHelper.KEY_REFS, getSpecificDate(d, "reference"));
	    		Log.d("ESD", "Exporting: "+displayFormat.format(d));
	    		db.insert(DBHelper.TABLE_NAME, null, cv);
	    	}
	    	db.close();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			
		}
		
		
	}
}
