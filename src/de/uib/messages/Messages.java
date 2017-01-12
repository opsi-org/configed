package de.uib.messages;

import java.util.*;
import java.io.*;
import java.net.URI;
import utils.*;
import de.uib.configed.*;
import  de.uib.utilities.logging.*;

public class Messages
{
	private static final String prefix = "configed_";
	private static final String BUNDLE_NAME = "de/uib/messages/configed";
	private static final String FILE_TYPE = "properties";
	private static final String LOCALISATIONS_CONF = "valid_localisations.conf";
	
	
	
	static java.util.List<LocaleRepresentation> existingLocales;
	static java.util.List<String> existingLocalesNames;
	static java.util.Map<String, String> localeInfo;
	static String selectedLocaleString;
	static Locale myLocale = null;
	static public ResourceBundle messages;
	static public ResourceBundle messagesEN;
	static List<String> myLocaleCharacteristics;
	static List<String> myLocaleCharacteristicsEN;
	
	
	private static String findSelectedLocale(String language, String country)
	{
		String result = null;
		myLocaleCharacteristics = new ArrayList<String>();
		String characteristics = language + "_" + country; 
		
		myLocaleCharacteristics.add(characteristics);
		if (existingLocalesNames.indexOf(characteristics) > -1)
			result = characteristics;
		
		characteristics = language;
		myLocaleCharacteristics.add(characteristics);
		if (result == null && (existingLocalesNames.indexOf(characteristics) > -1))
			result = characteristics;
		
		return result;
	}
		
	
	public static String getSelectedLocale()
	{
		selectedLocaleString = findSelectedLocale(myLocale.getLanguage(), myLocale.getCountry());
		
		if (selectedLocaleString == null)
		{
			//not found, now try again for default locale
			produceLocale(); 
			selectedLocaleString = findSelectedLocale(myLocale.getLanguage(), myLocale.getCountry());
			
			if (selectedLocaleString == null)
			{
				//default locale not found
				produceLocale_enUS();
				selectedLocaleString = findSelectedLocale(myLocale.getLanguage(), myLocale.getCountry());
			}
		}
			
		return selectedLocaleString;	
	}
	
	public static ResourceBundle getResource() throws MissingResourceException
	{
		try
		{
			logging.debug("Messages, getResource from " + BUNDLE_NAME);
			messages = ResourceBundleUtf8.getBundle(BUNDLE_NAME, myLocale);
			
			logging.debug("Messages messages " + messages);
			for (String key : messages.keySet())
			{
				logging.debug("key " + key + ", value " + messages.getString(key));
			}
			
		}
		catch (MissingResourceException ex)
		{
			messages = getResourceEN();
		}
		return messages;
	}
	
	public static ResourceBundle getResourceEN() throws MissingResourceException
	{
		messagesEN = ResourceBundleUtf8.getBundle(BUNDLE_NAME, new Locale("en", "US")  );
		myLocaleCharacteristicsEN = new ArrayList<String>();
		myLocaleCharacteristicsEN.add("en_US");
		myLocaleCharacteristicsEN.add("en");
		return messagesEN;
	}
	
	private static Locale giveLocale(String selection)
	{
		logging.debug("Messages: selected locale " + myLocale + " by " + selection);
		return myLocale;
	}
	
	
	private static Locale produceLocale()
	{
		myLocale  = Locale.getDefault();
		return giveLocale("default");
	}
	
	private static Locale produceLocale(String language)
	{
		myLocale  = new Locale(language);
		return giveLocale("language " + language);
	}
	
	private static Locale produceLocale(String language, String country)
	{
		myLocale  = new Locale(language, country);
		return giveLocale("language " + language + ", country " + country);
	}
	
	private static Locale produceLocale_enUS()
	{
		myLocale  = new Locale("en", "US");
		return giveLocale("fallback (en_US)");
	}
	
	public static Locale getLocale()
	{
		return myLocale;
	}
	
	public static Locale setLocale(String characteristics)
	{
		logging.debug("Messages, setLocale");
		Locale loc = null;
		if ( characteristics != null && !characteristics.equals(""))
		{
			//logging.info("Locale is: " + characteristics + ">");
			if (characteristics.length() == 5 && characteristics.indexOf('_') == 2)
			{
				try
				{
					loc = produceLocale(characteristics.substring(0,2), characteristics.substring(3,5) );
					logging.info("Locale " + loc.getLanguage() + "_" + loc.getCountry() + " set by param");
				}
				catch (Exception e)
				{
					logging.info("Failed to set locale '" + characteristics + "': " + e);
				}
			}
			
			else if (characteristics.length() == 2)
			{
				try
				{
					loc = produceLocale( characteristics );
					logging.info("Locale " + loc + " set by param");
				}
				catch (Exception e)
				{
					logging.info("Failed to set locale '" + characteristics + "': " + e);
				}
				
			}
			else
			{
				logging.info("Bad format for locale, use <language>_<country> or <language>, each component consisting of two chars, or just a two char <language>");
			}
			
		}
		
		if (loc == null)
			loc = produceLocale();
		
		
		try
		{
			messages = getResource();
			messagesEN = getResourceEN();
		}
		catch (MissingResourceException e) 
		{
			logging.info("Missing messages for locale EN");
		}
		
		
		return loc;
	}
	
	public static java.util.List<String> getLocaleNames()
	{
		if (existingLocalesNames == null)
				getLocaleRepresentations();
		
		return existingLocalesNames;
	}
	
	public static Map<String, String> getLocaleInfo()
	{
		if (localeInfo == null)
			getLocaleRepresentations();
		
		logging.debug("Messages, getLocaleInfo " + localeInfo);
		
		return localeInfo;
	}		
	
	private static java.util.List<LocaleRepresentation> getLocaleRepresentations()
	{
		if (existingLocales != null)
			return existingLocales;
		
		
		ArrayList<LocaleRepresentation> existingLocales = new ArrayList();
		localeInfo = new TreeMap<String, String>();
		
		InputStream stream = de.uib.messages.Messages.class.getResourceAsStream(LOCALISATIONS_CONF);
		BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
		try
		{
			String line = reader.readLine();
			while  (line != null)
			{
				line = line.trim();
				if (line.length() > 0 && line.charAt(0) != '#')
					existingLocales.add(new LocaleRepresentation(line));
				line = reader.readLine();
			}
		}
		catch(Exception ex)
		{
			logging.warning("Messages exception on reading: " + ex);
		}
		
		TreeSet<String> names = new TreeSet<String>();
		for (LocaleRepresentation representer: existingLocales)
		{
			names.add(representer.getName());
			localeInfo.put(representer.getName(), representer.getIconName());
		}
		logging.debug("Messages, existing names " + names);
		existingLocalesNames = new ArrayList(names);
		logging.debug("Messages, existing locales " + existingLocales);
		logging.debug("Messages, localeInfo  " +localeInfo);
		return existingLocales; 
	}
	
	/* does not work in applet context
	public static java.util.List<String> getLocales()
	{
		if (existingLocales != null)
			return existingLocales;
		
		existingLocales = new ArrayList<String>();
		
		try
		{
			URI uri  = de.uib.messages.Messages.class.getResource(".").toURI();
			File messagesDir = new File( uri );
			logging.debug("Messages:, dir "  + messagesDir);
			//logging.debug("Messages:, messagesDir isDirectory " + messagesDir.isDirectory());
			
			final class PropertiesFilenameFilter implements FilenameFilter{
				public boolean accept(File dir, String name)
				{
					return name.endsWith("." + FILE_TYPE);
				}
			};
			PropertiesFilenameFilter filter = new PropertiesFilenameFilter();
			
			
		
			//logging.debug("Messages: filter " + filter);
				
			if  (messagesDir.isDirectory())
			{
				String[] filenames = messagesDir.list(filter);
				for (int i = 0; i < filenames.length; i++)
				{
					String s = filenames[i];
					s = s.substring(prefix.length());
					s = s.substring(0, s.lastIndexOf("."));
					existingLocales.add(s);
				}
			}
		}
		catch(Exception ex)
		{
			logging.warning("Messages:, getLocales error: " + ex);
			logging.logTrace(ex);
		}
		
		
		existingLocales = new ArrayList(new TreeSet(existingLocales));
		//logging.info("Messages:, getLocales: " + existingLocales); 
		
		return existingLocales; 
	}
	*/
}
