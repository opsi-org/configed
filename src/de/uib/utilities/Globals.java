package de.uib.utilities;

import java.awt.Font;
import java.awt.Frame;
import java.awt.Color;
import java.text.*;
import java.awt.Image;
import java.awt.Dimension;
import java.awt.Container;
import java.text.Collator;
import java.util.*;
import java.sql.Timestamp;
import javax.swing.ImageIcon;
import de.uib.utilities.logging.logging;

/**
 *  This class contains global constants and functions for the library
 *  Copyright:     Copyright (c) uib 2001-2017
 */

public class Globals 
{	
	public static String APPNAME = "";
	public static Image mainIcon = null;
	public static String iconresourcename = "";
	
	public static final Font defaultFont = new java.awt.Font("SansSerif", 0, 11);
	public static final Font defaultFontStandardBold = new java.awt.Font("SansSerif", Font.BOLD, 11);
	public static final Font defaultFontSmall = new java.awt.Font("SansSerif", 0, 9);
	public static final Font defaultFontSmallBold = new java.awt.Font("SansSerif", Font.BOLD, 9);
	//public static final Font defaultFont12 = new java.awt.Font("SansSerif", 0, 12);
	public static final Font defaultFontBig = new java.awt.Font("SansSerif", 0, 12);
	public static final Font defaultFontBold = new java.awt.Font("SansSerif", Font.BOLD, 12);
	public static final Font defaultFontStandard = defaultFont;
	
			/*
		// Get all font family names
		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		String fontNames[] = ge.getAvailableFontFamilyNames();
		
		// Iterate the font family names
		for (int i=0; i<fontNames.length; i++) {
			System.out.println("FONT ==>>> " + fontNames[i]);
		}
		*/
	
	public static final Color backgroundWhite = new Color (245, 245, 245);
	public static final Color backgroundGrey = new Color (220,220,220);
	public static final Color backgroundLightGrey = new Color (230,230,230);
	public static final Color backLightBlue = new Color  (220,228,242);  //new Color (210,216,232); 
	//public static final Color backLight = new Color (220,230,255); 
	public static final Color backBlue = new Color (200,200,250); 
	public static final Color backNimbus = new Color (214,219,222);
	public static final Color backNimbusLight = new Color (224,229,235);
	
	public static final Color backYellow = new java.awt.Color (255, 255, 182);
	public static final Color backLightYellow = new java.awt.Color (250, 248, 221);
	public static final Color backLightGreen = new java.awt.Color (230,255,210);
	public static final Color backTabsColor = new java.awt.Color (206, 223, 247);
	public static final Color darkOrange = new java.awt.Color(218,180,4);
	public static final Color lightBlack = new Color (30,30,30);
	public static final Color textGrey = new Color (80,80,80);
	public static final Color blue = new Color (30,30,100);
	public static final Color blueGrey = new Color (180,190,190);
	public static final Color violett = new Color (160,170,200);
	public static final Color greyed = new Color (150,150,150);
	
	
	public static final Color opsiLogoBlue = new Color(106,128,174);
	public static final Color opsiLogoLightBlue = new Color(195,200,222);
	
	public static final Color defaultTableCellBgColor1 = new Color (255,255,255);
	public static final Color defaultTableCellBgColor2 = backLightYellow; //new Color (224,231,255);
	public static final Color backgroundColorEditFields = backLightYellow;
	public static final Color defaultTableHeaderBgColor = new Color (222,231,247); //new Color (206,223,247);
	public static final Color defaultTableCellSelectedBgColor = new Color (184,207,229);
	public static final Color defaultTableCellSelectedBgColorNotEditable = new Color (189,207,231);
	
	
	public static final int toolTipInitialDelayMs = 1000;
	public static final int toolTipDismissDelayMs = 20000;
	public static final int toolTipReshowDelayMs  = 0;
	
	
	public static final int dateFormatStylePattern = DateFormat.LONG;
	
	public static final int vGapSize = 10;
	public static final int hGapSize = 10;
	
	public static final int buttonHeight = 24;
	public static final int lineHeight = 28;
	public static final int progressBarHeight = 10;
	public static final int tableRowHeight = 21;
	public static final int buttonWidth = 140;
	public static final int iconWidth = 60;
	public static final int squareButtonWidth = 24;
	
	public static final Dimension buttonDimension = new Dimension(buttonWidth, buttonHeight);
	public static final Dimension lowerButtonDimension = new Dimension(buttonWidth, buttonHeight-4);
	public static final Dimension smallButtonDimension = new Dimension(buttonWidth/2, buttonHeight+4);
	public static final Dimension textfieldDimension = new Dimension(buttonWidth, lineHeight);
	public static final Dimension labelDimension = new Dimension(80, lineHeight);
	public static final Dimension shortlabelDimension = new Dimension(60, lineHeight);
	public static final Dimension counterfieldDimension = new Dimension(60, lineHeight);
	public static final Dimension newSmallButton = new Dimension(30,30);
	
	private static java.text.Collator alphaCollator = null;
	
	public static java.text.Collator getCollator()
	{
		if (alphaCollator == null)
		{
			alphaCollator = java.text.Collator.getInstance();
			//alphaCollator.setStrength(java.text.Collator.PRIMARY);
			alphaCollator.setStrength(java.text.Collator.IDENTICAL);
			
		}
		return alphaCollator;
	}
	
	
	
	public static Container masterFrame;
	
	     //mainIcon = Toolkit.getDefaultToolkit().createImage(ConfigedGlobals.class.getResource("opsi.gif"));  
	
	public static final String imageBase = "images";
	
	public static String imageBaseAbsolute;
	
	public static String getImagesBaseAbsolute()
	{
		if (imageBaseAbsolute == null)
			imageBaseAbsolute = Globals.class.getResource(imageBase).toString();
		return imageBaseAbsolute;
	}
	
	public final static String fileseparator = "/";
	
	public static boolean isWindows()
	{
		Runtime rt = Runtime.getRuntime();
		String osName = System.getProperty("os.name" );
		return osName.toLowerCase().startsWith("windows");
	}
	
	public static java.net.URL getImageResourceURL(String relPath)
	{
		String resourceS = imageBase + fileseparator + relPath;
		java.net.URL imgURL = Globals.class.getResource(resourceS);
		//System.out.println ( " ---- imgURL " + imgURL );
		logging.info("getImageResourceURL  found for " + resourceS + " url: " + imgURL);
		if (imgURL != null) 
		{
				return imgURL;
		} 
		else 
		{
				logging.warning("Couldn't find file  " + relPath);
				return null;
		}
	}
		
	
	public static javax.swing.ImageIcon createImageIcon(String path, String description) 
	{
		//System.out.println ( " ---- image path: " + imageBase + fileseparator + path );
		java.net.URL imgURL = Globals.class.getResource(imageBase + fileseparator + path);
		//System.out.println ( " ---- imgURL " + imgURL );
		if (imgURL != null) 
		{
				return new javax.swing.ImageIcon(imgURL, description);
		} 
		else 
		{
				System.out.println("Couldn't find file: " + path);
				return null;
		}
	}
	
	private static Map objects;
	
	public static Map getMap()
	{
		if (objects == null)
		{
			objects = new HashMap();
			
			objects.put("mainIcon", mainIcon);
			objects.put("defaultFont", defaultFont);
			objects.put("APPNAME", APPNAME);
		}
		
		return objects;
	}
	
	
	public static String driverType = "";
	
	public static String formT (String timeExpression)
	{
		if (driverType.equals("MSSQL"))
			return " convert(datetime, " + timeExpression + ", 121) ";  //MSSQL
		else
			return timeExpression; // standard
		//return " convert(char(19, convert(datetime, " + timeExpression + ", 121), 121) ";  //MSSQL, convert back to string
		
	}
	
	public static String getMinutes()
	{
		String sqlNow 
				=  new java.sql.Timestamp (new java.util.GregorianCalendar().getTimeInMillis()).toString();
		sqlNow = sqlNow.substring(0, sqlNow.lastIndexOf(':'));
		sqlNow = sqlNow.replace(' ', '-');
		//sqlNow = sqlNow.replace(':', '-');
		
		return sqlNow;
	}
	
	public static String getSeconds()
	{
		String sqlNow 
				=  new java.sql.Timestamp (new java.util.GregorianCalendar().getTimeInMillis()).toString();
		
		//System.out.println(" sqlNow " + sqlNow);
		int i = sqlNow.lastIndexOf(' ');
		String date = sqlNow.substring(0, i);
		date = date.replace(' ', '-');
		String time = sqlNow.substring(i+1);
		time = time.substring(0, time.indexOf('.'));
		
		return date + "_" + time; 
		
		/*
		sqlNow = sqlNow.substring(0, sqlNow.indexOf('.'));
		sqlNow = sqlNow.replace(' ', '-');
		sqlNow.replace, sqlNow.lastIndexOf('_'));
		sqlNow = sqlNow.replace(':', '-');
		
		return sqlNow;
		*/
	}
	
	public static String getDate(boolean justNumbers)
	{
		String sqlNow 
				=  new java.sql.Timestamp (new java.util.GregorianCalendar().getTimeInMillis()).toString();
		sqlNow = sqlNow.substring(0, sqlNow.lastIndexOf(' '));
		
		if (justNumbers)
			sqlNow = sqlNow.replaceAll("-", "");
		
		return sqlNow;
	}
	
	public static Date getToday()
	{
		return new java.sql.Timestamp (new java.util.GregorianCalendar().getTimeInMillis());
	}
		
	private static String formatlNumberUpTo99(long n)
	{
		if (n < 10)
			return "0" + n;
		else
			return "" + n;
	}
	
	public static String giveTimeSpan(final long millis)
	{
		long seconds;
		long remseconds;
		String remSecondsS;
		long minutes;
		long remminutes;
		String remMinutesS;
		long hours;
		String hoursS;
		
		seconds = millis/1000;
		minutes = seconds/60;
		remseconds = seconds % 60;
		
		hours = minutes/60;
		remminutes = minutes % 60;
		
		remSecondsS = formatlNumberUpTo99(remseconds);
		remMinutesS = formatlNumberUpTo99(remminutes);
		hoursS = formatlNumberUpTo99(hours);
		
		String result = "" + hoursS + ":" + remMinutesS + ":" + remSecondsS;
		//logging.info(this, "giveTimeSpan for millis " + millis + " " + result); 
			
	
		return result;
	}
	
	
	public static String getStringValue(Object s)
	{
		if (s == null)
			return "";
		/*
		if (s instanceof String)
			return s;
		*/
		
		return s.toString();
	}
	
	
	public static java.util.ArrayList<String> takeAsStringList( java.util.List<Object> list )
	{
		java.util.ArrayList<String> result = new java.util.ArrayList<String>();
		
		if (list  == null)
			return result;
		
		for (Object val : list)
		{
			result.add( (String) val);
		}
		
		return result;
	}
	
	public static String pseudokey(String[] partialvalues)
	{
		StringBuffer resultBuffer = new StringBuffer("");
		
		if (partialvalues.length > 0)
		{
			resultBuffer.append(partialvalues[0]);
			
			for (int i = 1; i < partialvalues.length; i++)
			{
				resultBuffer.append(";");
				resultBuffer.append(partialvalues[i]);
			}
		}
		
		return resultBuffer.toString();
	}
	
	
	final static int tooltipLineLength = 50;
	final static int uncertainty = 20;
	
	public static String wrapToHTML(String s)
	{
		StringBuffer result = new StringBuffer("<html>");
		String remainder = s;
		while (remainder.length() > 0)
		{
			de.uib.utilities.logging.logging.debug("Globals, remainder " + remainder);
			if (remainder.length() <= tooltipLineLength)
			{
				result.append(remainder.replace("\\n", "<br />"));
				remainder = "";
				break;
			}
			result.append(remainder.substring(0, tooltipLineLength).replace("\\n", "<br />"));
			
			int testspan = min(remainder.length() - tooltipLineLength, uncertainty);
			
			String separationString
			= remainder.substring(tooltipLineLength, tooltipLineLength + testspan);
			
			
			boolean found = false;
			int i = 0;
			de.uib.utilities.logging.logging.debug("Globals, separationString " + separationString);
			
			while (!found && i < testspan) 
			{
				if (separationString.charAt(i) == ' ' 
					|| separationString.charAt(i) == '\n'
					|| separationString.charAt(i) == '\t')
				{
					found = true;
					if (separationString.charAt(i) == '\n')
						result.append("<br />");
				}
				else
					i++;
			}
			
			result.append(separationString.substring(0, i));
			result.append("<br />");
			int end = max(remainder.length(), tooltipLineLength); 
			remainder = remainder.substring(tooltipLineLength + i, end);
		}
		
		result.append("</html>");
		return result.toString();
	}
	
	public static int max(int a, int b)
	{
		int m = a;
		if (b > a)
			m = b;
		return m;
	}
	
	public static int min(int a, int b)
	{
		int m = a;
		if (b < a)
			m = b;
		return m;
	}
	
	public static boolean checkCollection( Object source, String location, String cName, Collection c )
	{
		boolean result = (c != null);
		if (result)
		{
			if (c instanceof Collection)
			{
				logging.info(source.getClass().getName() + " " + cName + " has size  " + ((Collection)c).size() );
			}
			else if (c instanceof Map)
			{
				logging.info(source.getClass().getName() + " " + cName + " has size  " + ((Map)c).size() );
			} 
			else	
			{
				logging.info(source.getClass().getName() + " " + cName + " is neither a Collection nor a Map  ");
				result = false;
			}
		}
		else 
			logging.info(source.getClass().getName() + " " + cName + " is null");
		
		return result;
	}
	
	public static String makeHTMLlines(String s)
	{
		if (s == null || s.trim().startsWith("<"))
			return s;
		
		final int maxLineLength = 80;
		
		StringBuffer b = new StringBuffer("<html>");
		int charsInLine = 0;
		boolean indentDone = false;
		int lineIndent = 0;
		for (int c = 0; c < s.length(); c++)
		{
			charsInLine++;
			switch (s.charAt(c))
			{
				case ' ':
					b.append("&nbsp;");
					if (!indentDone) lineIndent = lineIndent+1;
					break;
				case '\t':
					b.append("&nbsp;&nbsp;&nbsp;");
					if (!indentDone) lineIndent = lineIndent+3;
					break;
				case '\n':
					b.append("<br/>");
					indentDone = false;
					charsInLine = 0;
					lineIndent = 0;
					break;
				default:
					indentDone = true;
					b.append(s.charAt(c));
			}
			if (charsInLine >= maxLineLength)
			{
				if (c+1 < s.length())
				{
					if ((s.charAt(c+1) == ' ') || (s.charAt(c+1) == '\t') || (s.charAt(c+1) == '\n'))
					{
						c++;
						b.append("<br/>");
						if (s.charAt(c) != '\n')
						{
							while (lineIndent > 0)
							{
								lineIndent--;
								charsInLine++;
								b.append("&nbsp;");
							}
						}
						charsInLine = 0;
						indentDone = false;
						lineIndent = 0;
					}
				}
			}
		}
		
		b.append("</html>");
		
		return b.toString();
	}
	
		 
	public static String usedMemory()
	{
		long total = Runtime.getRuntime().totalMemory();
		long free = Runtime.getRuntime().freeMemory();
		
		return " " + ( ( (total- free)/1024 ) / 1024 )  + " MB ";
	}
		
			
	public static void main(String[] args)
	{
		//tests
		//System.out.println(wrapToHTML(args[0]));
	}

}
