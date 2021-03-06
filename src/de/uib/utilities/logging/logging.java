package de.uib.utilities.logging;

import java.io.*;
import java.util.regex.*;
import java.util.*;
import java.util.GregorianCalendar;
import de.uib.configed.ConfigedMain;
import de.uib.configed.Globals;
import de.uib.configed.gui.FShowList;
import javax.swing.*;
import de.uib.utilities.thread.WaitCursor;


public class logging
			implements LogEventSubject

{
	public static String wantedDirectory= null;

	public static  String programSubDir = "";
	private static  String logfileDelimiter = "configed";
	private static  String extension = ".log";

	public static final int LEVEL_INFO = 3;
	public static final int LEVEL_CHECK = 4;
	public static final int LEVEL_DEBUG = 5;
	public static final int LEVEL_DONT_SHOW_IT = 6;
	public static final int LEVEL_FATAL = 0;
	public static final int LEVEL_ERROR = 1;
	public static final int LEVEL_WARNING = 2;

	public static final int LEVEL_SHOW_ALWAYS =  LEVEL_ERROR;

	private static final java.text.SimpleDateFormat loggingDateFormat = new java.text.SimpleDateFormat( "MMM dd  HH:mm:ss.SSS  yyyy");


	public static final String levelText(int level)
	{
		//StringBuffer buf =  new StringBuffer("");
		String result = "UNKNOWN_LEVEL  ";
		switch (level)
		{
		case LEVEL_DONT_SHOW_IT: 	result = "(HIDDEN)  ";  break;
		case LEVEL_DEBUG: 			result = "(DEBUG)   ";  break;
		case LEVEL_CHECK: 			result = "(CHECK)   ";  break;
		case LEVEL_INFO: 			result = "(INFO)    ";  break;
		case LEVEL_WARNING: 		result = "(WARNING) ";  break;
		case LEVEL_ERROR: 			result = "(ERROR)   ";  break;
		case LEVEL_FATAL: 			result = "(FATAL)   ";  break;
		}


		/*
		buf.append(result);
		buf.append("  ");
		//fill with spaces

		for (int i = result.length(); i < 20; i++)
			buf.append(' ');
		*/


		return result;


	}

	public static int AKT_DEBUG_LEVEL = LEVEL_INFO;
	static private int AKT_LEVEL_FOR_SHOWING_MESSAGES =  LEVEL_ERROR;

	static private int numberOfKeptLogFiles = 3;
	static private PrintWriter writer;
	static private boolean initialized = false;
	static public boolean noFileAccess = false;

	private final static int maxListedErrors = 20;
	private  static Vector errorList;

	static public FShowList  fErrors;

	private static Integer induceLevel;

	protected static Vector<LogEventObserver> logEventObservers;
	
	private static boolean suppressConsole = false;
	
	public static void setSuppressConsole(boolean b)
	{
		suppressConsole = b;
	}
	
	private static void writeToConsole(String s)
	{
		if (!suppressConsole) 
			System.out.println(s);
	}

	public static void setAktDebugLevel(int newLevel)
	{
		if( newLevel >= 0 )
			writeToConsole("debug level was " + AKT_DEBUG_LEVEL);
		AKT_DEBUG_LEVEL = newLevel;
		if( newLevel >= 0 )
			writeToConsole("debug level set to " + newLevel);
	}

	synchronized static final public void init(  )
	{
		//setAktDebugLevel (LEVEL_INFO);

		writeToConsole("logging init");

		errorList = new Vector (maxListedErrors);

		logEventObservers = new Vector<LogEventObserver>();

		try
		{
			String theFilename;

			File theFile;

			// File logDirectory = new File( System.getProperty("user.home") + File.separator +  programSubDir );
			File logDirectory;
			
			if (System.getenv("LOCALAPPDATA") != null)
				logDirectory = new File( System.getenv("LOCALAPPDATA") + File.separator +  programSubDir );
			else
				logDirectory = new File( System.getProperty("user.home") + File.separator +  programSubDir );

			writeToConsole("logging init logDirectory " + logDirectory);

			//general default directory

			boolean takeLogDirectory = false;

			String errorMessage = "";

			File directory = logDirectory;
			writeToConsole("logging directory is " + directory.toString());

			if (wantedDirectory != null && !wantedDirectory.equals(""))
			{
				directory = new File (wantedDirectory);
				try
				{
					if (!directory.isDirectory())
					{
						errorMessage = "\"" + wantedDirectory + "\" is no existing directory";
						takeLogDirectory = true;
					}
					else if (!directory.canRead() ||  !directory.canWrite())
					{
						errorMessage = "We have not the necessary privileges for writing in \"" + wantedDirectory + "\" ";
						takeLogDirectory = true;
					}
				}
				catch (SecurityException se)
				{
					errorMessage = "directory is not readable";
					takeLogDirectory = true;
				}
				if (takeLogDirectory)
					System.out.println (now() + ": " + errorMessage);
			}

			if (takeLogDirectory)
				directory = logDirectory;

			if( AKT_DEBUG_LEVEL >= 0 )
			{
				System.out.println ("logging directory is " + directory.toString());
				System.out.println ("log level is " +  AKT_DEBUG_LEVEL);
			}


			String completeDirectoryString = directory.toString();

			new File(completeDirectoryString).mkdirs();

			theFilename = completeDirectoryString  +  File.separator + logfileDelimiter +  extension;
			theFilename = new File (theFilename).getAbsolutePath();
			theFile = new File(theFilename);



			if (numberOfKeptLogFiles > 0)
			{
				String[] filenames = new String[numberOfKeptLogFiles];
				File[] logfiles = new File[numberOfKeptLogFiles];

				for (int i = 0; i < numberOfKeptLogFiles; i++)
				{
					filenames[i] = completeDirectoryString  +  File.separator + logfileDelimiter +  "_" + i + extension;
					logfiles[i] = new File (filenames[i]);
				}



				for (int i = numberOfKeptLogFiles - 1; i > 0; i--)
				{
					if (logfiles[i-1].exists())
					{
						logfiles[i-1].renameTo(logfiles[i]);
					}
				}

				if (theFile.exists())
					theFile.renameTo(logfiles[0]);

				if( AKT_DEBUG_LEVEL >= 0 )
					System.out.println ("logging: use " + theFilename);
			}



			writer = new PrintWriter (new FileOutputStream (theFilename));

			initialized = true;



		}
		catch (Exception ex)
		{
			System.out.println("file system logging could not be initialized " + ex.toString());
			//errorList.add (ex.toString());
			noFileAccess = true;
		}
		
	}


	static private boolean debug (int level)
	{
		return (level <= AKT_DEBUG_LEVEL);
	}


	static private  boolean showOnGUI (int level)
	{
		return  (level <= AKT_LEVEL_FOR_SHOWING_MESSAGES);
	}

	static private String now ()
	{
		//return   new GregorianCalendar().getTime().toString();
		return loggingDateFormat.format(new java.util.Date());// + "  ";
	}
	
	static private String shorten(String mesg)
	{
		int i =  mesg.indexOf("\0");
		if (i > -1)
		{
			return "<<< " + mesg + ">>>";
			
		}
		return mesg;
	}

	static private String makeDebugTextForTime (int level, String location, String mesg, String time)
	{
		return "[" + level + "] " +  levelText(level) + " [" +  time +  "]   "
		+ " [" + Thread.currentThread() + "]      "
		+ location +  "   " + shorten(mesg);
	}


	static private void addErrorToList (String mesg, String time)
	{
		while (errorList.size() >= maxListedErrors)
		{
			errorList.removeElementAt(0);
		}

		errorList.add (time + " -- " +shorten(mesg));

		for (int i = 0; i < logEventObservers.size(); i++)
		{
			logEventObservers.get(i).logEventOccurred(new LogEvent(null, "", -1, true));
		}
	}

	static private boolean checkInit( String mesg )
	{
		if (!initialized && !noFileAccess)
		{
			//System.out.println (now() +  ": wanted directory in checkInit " + wantedDirectory);
			if (wantedDirectory == null)
			{
				System.out.println ("logging directory not yet set");
				System.out.println (mesg);
				return false;
			}
			else
				init();
		}

		return true;

	}

	static private int modifyLevel(int level)
	{
		if (level <=  LEVEL_SHOW_ALWAYS)
			return level;

		if (induceLevel != null)
			return induceLevel.intValue();

		return level;
	}


	synchronized static public void injectLogLevel (Object ob, Integer level)
	{
		String source = "";
		
		if (ob != null)
			source = ob.getClass().getName() + " ";
		
		if( AKT_DEBUG_LEVEL >= 0 )
		{
			System.out.println(source + "induce loglevel " + level);
			induceLevel = level;
		}
		else
		{
			writeToConsole(source + "loglevel already < 0");
		}
	}
		
	
	synchronized static public void injectLogLevel (Integer level)
	{
		injectLogLevel(null, level);
	}
	
	
	synchronized static public Integer getInjectedLogLevel()
	{
		return induceLevel;
	}

	static public String getIntegers(int[] s)
	{
		if (s == null)
			return null;

		StringBuffer result = new StringBuffer("{");

		for (int j = 0; j < s.length; j++)
		{
			result.append("\"");
			result.append(""+s[j]);
			result.append("\"");
			if (j < s.length - 1) result.append(", ");
		}

		result.append("}");

		return result.toString();
	}

	static public String getStrings(int[] s)
	{
		if (s == null)
			return null;

		Integer[] t = new Integer[s.length];
		for (int i =0; i < s.length; i++)
			t[i] = s[i];

		return getStrings(t);
	}

	static public String getSize(Object[] a)
	{
		if (a == null)
			return null;
		
		return "" + a.length;
	}
	
	static public String getSize(Collection c)
	{
		if (c == null)
			return null;
		
		return "" + c.size();
	}
	

	static public String getStrings(Object[] s)
	{
		if (s == null)
			return null;

		StringBuffer result = new StringBuffer("{");

		for (int j = 0; j < s.length; j++)
		{
			result.append("\"");
			result.append(""+s[j]);
			result.append("\"");
			if (j < s.length - 1) result.append(", ");
		}

		result.append("}");

		return result.toString();
	}

	static public String getStack(Object[] s)
	{
		if (s == null)
			return null;

		StringBuffer result = new StringBuffer();
		result.append("STACK:\n");

		for (int j = 0; j < s.length; j++)
		{
			result.append("     "+s[j]);
			result.append("\n");
		}

		return result.toString();
	}




	synchronized static public void debugOut (int level, String mesg, Throwable ex)
	{
		if (!checkInit(mesg))
			return;

		int modLevel = modifyLevel(level);

		String nowTime = now();
		
		if ( debug( modLevel ) && modLevel <  LEVEL_DONT_SHOW_IT )
		{
			String myText = supplyXInfo( makeDebugTextForTime  (modifyLevel(level), "", shorten(mesg), nowTime) );
			if (AKT_DEBUG_LEVEL >= LEVEL_INFO || noFileAccess)
			{
				System.out.println(myText);
				ex.printStackTrace();
			}


			if (!noFileAccess)
			{
				writer.println(myText);
				ex.printStackTrace(writer);
				writer.flush();
			}
		}
		
		if ( showOnGUI( modLevel ) )
		{
			addErrorToList(shorten(mesg), nowTime);
		}

	}
	
	
	synchronized static public void debugOut (int level, String mesg)
	{
		debugOut(level, mesg, true);
	}

	synchronized static public void debugOut (int level, String mesg, boolean modifyingAllowed)
	{
		//System.out.println("debugOut " + level + ", " + mesg + " allow modifying " + modifyingAllowed);
		if (!checkInit(mesg))
			return;

		String nowTime = now();
		int modLevel = level;
		
		if (modifyingAllowed)
			modLevel = modifyLevel(level);
		
		if ( debug( modLevel ) && modLevel <  LEVEL_DONT_SHOW_IT)
		{
			String myText = supplyXInfo( makeDebugTextForTime  (modifyLevel(level), "", shorten(mesg), nowTime) );
			if (AKT_DEBUG_LEVEL >= LEVEL_INFO || noFileAccess) System.out.println(myText);

			if (!noFileAccess)
			{
				writer.println(myText);
				writer.flush();
			}
		}
		
		if ( showOnGUI ( modLevel ) )
		{
			addErrorToList(shorten(mesg), nowTime);
		}


	}


	synchronized static public void debugOut (String location, int level, String mesg)
	{
		if (!checkInit(mesg))
			return;

		int modLevel = modifyLevel(level);

		String nowTime = now();

		if ( debug( modLevel ) && modLevel <  LEVEL_DONT_SHOW_IT)
		{
			String myText = supplyXInfo( makeDebugTextForTime( modLevel, location, shorten(mesg), nowTime) );

			if (AKT_DEBUG_LEVEL >= LEVEL_INFO || noFileAccess ) System.out.println(myText);

			if (!noFileAccess)
			{
				writer.println(myText);
				writer.flush();
			}
		}
		
		if ( showOnGUI( modLevel ) )
		{
			addErrorToList(shorten(mesg), nowTime);
		}


	}

	private static String supplyXInfo(String s)
	{
		//return " [" + Thread.currentThread() + "] " + s;
		return s;
	}
	
	synchronized static public void debugOut (Object caller, int level, String mesg)
	{
		if (!checkInit(mesg))
			return;

		int modLevel = modifyLevel(level);
		String nowTime = now();

		if (showOnGUI (modLevel) )
		{
			addErrorToList(shorten(mesg), nowTime);
		}


		if ( debug( modLevel ) && modLevel <  LEVEL_DONT_SHOW_IT)
		{
			String myText = supplyXInfo( makeDebugTextForTime( modLevel, caller.getClass().getName() , shorten(mesg), nowTime));
			if (AKT_DEBUG_LEVEL >= LEVEL_INFO || noFileAccess )  System.out.println(myText);

			if (!noFileAccess)
			{
				writer.println(myText);
				writer.flush();
			}
		}
	}


	static public void hidden(Object caller, String mesg)
	{
		debugOut (caller, LEVEL_DONT_SHOW_IT, mesg);
	}

	static public void debug(Object caller, String mesg)
	{
		debugOut (caller, LEVEL_DEBUG, mesg);
	}

	static public void check(Object caller, String mesg)
	{
		debugOut (caller, LEVEL_CHECK, mesg);
	}

	static public void info(Object caller, String mesg)
	{
		debugOut (caller, LEVEL_INFO, mesg);
	}

	static public void warning(Object caller, String mesg)
	{
		debugOut (caller, LEVEL_WARNING, mesg);
	}

	static public void error(Object caller, String mesg, Exception ex)
	{
		logTrace(ex);
		error(caller, mesg);
	}

	static public void error(Object caller, String mesg)
	{
		debugOut (caller, LEVEL_ERROR, mesg);
	}

	static public void fatal(Object caller, String mesg)
	{
		debugOut (caller, LEVEL_FATAL, mesg);
	}



	static public void hidden(String mesg)
	{
		debugOut (LEVEL_DONT_SHOW_IT, mesg);
	}

	static public void debug(String mesg)
	{
		debugOut (LEVEL_DEBUG, mesg);
	}

	static public void check (String mesg)
	{
		debugOut (LEVEL_CHECK, mesg);
	}

	static public void info(String mesg)
	{
		debugOut (LEVEL_INFO, mesg);
	}

	static public void warning(String mesg)
	{
		debugOut (LEVEL_WARNING, mesg);
	}

	static public void error(String mesg, Exception ex)
	{
		logTrace(ex);
		error(mesg);
	}

	static public void error(String mesg)
	{
		debugOut (LEVEL_ERROR, mesg);
	}

	static public void fatal(String mesg)
	{
		debugOut (LEVEL_FATAL, mesg);
	}


	static public void clearErrorList()
	{
		errorList.clear();
	}

	static private List getErrorList ()
	{
		return errorList;
	}

	static private void checkErrorList()
	{
		checkErrorList(null);
	}

	static public void checkErrorList(JFrame parentFrame)
	// if errors Occurred show a window with the logged errors
	{
		//System.out.println("checkErrorList");
		final JFrame f;
		if ( parentFrame == null)
			f = Globals.mainFrame;
		else
			f = parentFrame;

		int errorCount = getErrorList().size();

		if (errorCount == 0)
			return;


		if (fErrors == null)
		{
			WaitCursor.stopAll();

			//System.out.println(" start fErrors");
			fErrors = new FShowList (f, Globals.APPNAME + ": problems Occurred", false,new String[]{"ok"}, 400, 300);
		}
		

		new Thread(){
			public void run()
			{
				fErrors.setMessage(logging.getErrorListAsLines());
				fErrors.setAlwaysOnTop(true);
				fErrors.setVisible(true);
			}
		}.start();

		//parentFrame.setVisible(true);

	}

	static public String getErrorListAsLines()
	{
		StringBuffer result = new StringBuffer("");
		if (errorList.size() > 0)
		{
			Object[] arr = errorList.toArray();
			for (int i = 0; i < errorList.size(); i++)
			{
				result.append ("\n");
				result.append (arr[i].toString());
			}
		}

		return result.toString();
	}


	public static void logTrace(Throwable ex)
	{
		//System.out.println("logTrace " +ex);
		String mesg = "Exception " + ex + " \n" + getStack( ex.getStackTrace() ); 
		debugOut (LEVEL_INFO, mesg, false);
	}

	public static void debugMap(Object caller, Map m)
	{
		if (m == null)
		{
			debug(caller, " is null");
			return;
		}

		Iterator iter = m.keySet().iterator();

		while (iter.hasNext())
		{
			Object key = iter.next();

			Object value = m.get(key);

			debug(caller, " key: " + key + ", class " + key.getClass().getName()
			      + ", value " + value + ", class " + value.getClass().getName());
		}
	}


	// used instead of interface LogEventSubject
	static public void registLogEventObserver( LogEventObserver o)
	{
		logEventObservers.add(o);
	}


	// interface LogEventSubject
	public void registerLogEventObserver( LogEventObserver o)
	{
		// not implemented since static method is needed
	}

}
