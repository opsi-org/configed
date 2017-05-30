package de.uib.configed;

import de.uib.messages.*;
import de.uib.utilities.logging.*;
import javax.swing.UIManager;
import javax.swing.*;
import java.util.*;
import java.awt.Toolkit;
import java.io.*;
import java.net.URL;
import java.nio.charset.Charset;
import java.awt.*;
import utils.*;
import de.uib.opsidatamodel.*;
import de.uib.opsicommand.ConnectionState;


public class configed
{
	public static boolean isApplet = false;
	public static boolean useHalt = false;

	public static de.uib.utilities.swing.FLoadingWaiter fProgress;

	public final static String usage = "\n" +
	                                   "\tconfiged [OPTIONS] \n" +
	                                   "\t\twhere an OPTION may be \n";
	/*
	"-l LOC, \t--locale LOC \t\t\t(Set locale LOC (format: <language>_<country>)) \n" +
	"-h HOST, \t--host HOST \t\t\t(Configuration server HOST to connect to, --help shows the command usage) \n" +
	"-u NAME, \t--user NAME \t\t\t(user for authentication) \n" +
	"-p PASSWORD, \t--password PASSWORD\t\t(password for authentication) \n" +
	"-c CLIENT, \t--client CLIENT \t\t(CLIENT to preselect) + \n" +
	"-t INDEX, \t--tab INDEX\t\t\t(Start with tab number INDEX, index counting starts with 0, works only if a CLIENT is preselected ) + \n" +
	"-d PATH, \t--logdirectory PATH \t\t(Directory for the log files) \n" +
	"-qs [SAVEDSEARCH_NAME], \t\t--querysavedsearch [SAVEDSEARCH_NAME]\t\t(On command line: tell saved host searches list resp. the search result for [SAVEDSEARCH_NAME])\n" +
	//"-qsj, \t--querysavedsearchjson \t(CLI: same as querysavedsearch, but output as jsonlist)\n" +
	"--gzip \t\t\t\t\t\t(Activate gzip transmission of data from opsi server) \n" +
	"--version \t\t\t\t\t(Tell configed version)\n" +
	"--help \t\t\t\t\t\t(Give this help)\n" +
	"--loglevel L \t\t\t\t\t(Set logging level L, L is a number >= " +  logging.LEVEL_FATAL + ", <= " + logging.LEVEL_DEBUG + ") \n" ;
	*/

	public final static String[][] usageLines = new String[][]{
	            new String[]{"-l LOC", 				"--locale LOC", 						"Set locale LOC (format: <language>_<country>)"},
	            new String[]{"-h HOST", 				"--host HOST", 						"Configuration server HOST to connect to"},
	            new String[]{"-u NAME", 			"--user NAME",						"user for authentication"},
	            new String[]{"-p PASSWORD", 		"--password PASSWORD",				"password for authentication"},
	            new String[]{"-c CLIENT", 			"--client CLIENT", 					"CLIENT to preselect"},
	            new String[]{"-g CLIENTGROUP", 		"--group CLIENTGROUP", 			"CLIENTGROUP to preselect"},
	            new String[]{"-t INDEX",				"--tab INDEX",						"Start with tab number INDEX, index counting starts with 0, works only if a CLIENT is preselected"},
	            new String[]{"-d PATH", 				"--logdirectory PATH", 				"Directory for the log files"},
	            new String[]{"-r REFRESHMINUTES", 	"--refreshminuites REFRESHMINUTES", 	"Refresh data every REFRESHMINUTES  (where this feature is implemented, 0 = never)"},
	            new String[]{"-qs [SAVEDSEARCH_NAME]", "--querysavedsearch [SAVEDSEARCH_NAME]","On command line: tell saved host searches list resp. the search result for [SAVEDSEARCH_NAME])"},
	             //"-qsj, \t--querysavedsearchjson \t(CLI: same as querysavedsearch, but output as jsonlist)\n" +
	            new String[]{"--gzip [y/n]", "", "Activate gzip transmission of data from opsi server yes/no"},
	            new String[]{"--sslversion PREFERRED_SSL_VERSION", "", "Try to use this SSL/ TLS version"},
	            new String[]{"--ssh-key SSHKEY",				"", "full path with filename from sshkey used for authentication on ssh server"},
	            new String[]{"--ssh-passphrase PASSPHRASE",		"", "passphrase for given sshkey used for authentication on ssh server"},
	            new String[]{"--version", "",  "Tell configed version"},
	            new String[]{"--collect_queries_until_no N", "", "Collect the first N queries; N = -1 (default, no collect) N = infinite "},
	            new String[]{"--help", "", "Give this help"},
	            new String[]{"--loglevel L", "",  "Set logging level L, L is a number >= " +  logging.LEVEL_FATAL + ", <= " + logging.LEVEL_DEBUG + ""},
	            new String[]{"--halt", "",  "Use  first occurring debug halt point that may be in the code"},
	            //new String[]{"--sqlgethashes", "", "Use sql statements with getHashes where implemented in PersistenceController "},
	            new String[]{"--sqlgetrows", "", "Force use sql statements by getRawData"},
	            new String[]{"--nosqlrawdata", "", "Avoid getRawData"},
	            // new String[]{"--synced", "", "Load table first and do only sync afterwards "},
	            //new String[]{"--dblocal", "", "Tries to make use of an embedded local database "},
	            //new String[]{"--dblocalnew", "", "Tries to make use of an embedded local database after renewing it"},
	            // new String[]{"--sqldirect", "", "Use direct sql access if possible in PersistenceController "}
	            new String[]{"--sqldirect-cleanup-auditsoftware", "", "Use direct sql access if possible in PersistenceController "}




	            //undocumented
	            //new String[]{"--me", "--testPersistenceControllerMethod", ""}




	        }
	        ;


	public final static Charset serverCharset = Charset.forName("UTF-8");
	public final static String javaVersion = System.getProperty("java.version");
	public final static String systemSSLversion = System.getProperty("https.protocols");
	public final static String STATEOFTHEART_SSL_VERSION = "TLSv1.2";
	public final static String JAVA_1_7_DEFAUTL_SSL_VERSION = "TLSv1";
	public static String PREFERRED_SSL_VERSION = STATEOFTHEART_SSL_VERSION;
	public static boolean sslversionChecked = false; //set only once
	protected static boolean serverCharset_equals_vm_charset = false;

	private static JApplet appletHost;

	private static ConfigedMain cm;


	private static String locale = null;
	private static String host  = null;
	private static String user = null;
	private static String password = null;
	public static String sshkey = null;
	public static String sshkeypassphrase = null;
	private static String client = null;
	private static String clientgroup = null;
	private static Integer tab = null;
	private static String logdirectory = "";
	private static boolean optionCLIQuerySearch = false;
	private static String savedSearch = null;

	private static boolean optionPersistenceControllerMethodCall = false;
	private static String methodCall = null;


	public static SavedStates savedStates;
	public static final String savedStatesFilename = "configedStates.prop";

	public static Integer refreshMinutes = 0;

	private static String paramLocale;
	private static String paramHost;
	private static String paramUser;
	private static String paramPassword;
	private static String paramClient;
	private static String paramClientgroup;
	private static Integer paramTab;

	//public static Locale LOCALE;

	//protected static ResourceBundle messages;
	//protected static ResourceBundle messagesEN;

	private static String tabs(int count)
	{
		StringBuffer buf = new StringBuffer("");
		for (int j = 0; j < count; j++)
			buf.append("\t");
		return buf.toString();
	}

	protected static void usage()
	{
		System.out.println(usage);

		final int tabWidth = 8;
		int length0 = 0;
		int length1 = 0;

		for (int i = 0; i<usageLines.length; i++)
		{
			//we find max of fillTabs0, fillTabs1
			int len = usageLines[i][0].length();

			if (len > length0)
				length0 = len;

			len = usageLines[i][1].length();


			if (len > length1)
				length1 = len;
		}


		int allTabs0 = 0;
		if ( length0 % tabWidth == 0)
			allTabs0 = length0 / tabWidth + 1;
		else
			allTabs0 = (int) (length0 / tabWidth) + 1;

		int allTabs1 = 0;
		if ( length1 % tabWidth == 0)
			allTabs1 = length1 / tabWidth + 1;
		else
			allTabs1 = (int) (length1 / tabWidth) + 1;

		for (int i = 0; i<usageLines.length; i++)
		{
			//System.out.println("usageLines " + i + ", " + 0 + usageLines[i][0]);

			int startedTabs0 = (int) (usageLines[i][0].length() / tabWidth);
			int startedTabs1 = (int) (usageLines[i][1].length() / tabWidth);


			System.out.println("\t" +
			                   usageLines[i][0] + tabs(allTabs0 - startedTabs0) +
			                   usageLines[i][1] + tabs(allTabs1 - startedTabs1) +
			                   usageLines[i][2]
			                  );
		}

	}

	protected static boolean isValue(String[] args, int i)
	{
		//System.out.println( "isValue " + args[i] + " length " + args.length + " i " + i  + " has - " + (args[i].indexOf('-') == 0) );

		if ( i >= args.length ||  args[i].indexOf('-') == 0)
			return false;

		return true;
	}



	protected static String getArg(String[] args, int i)
	{
		if ( args.length <= i+1 || args[i+1].indexOf('-') == 0 )
		{
			System.err.println("Missing value for option " + args[i]);
			usage();
			endApp(1);
		}
		i++;
		return args[i];
	}

	public static void startWithLocale()
	{
		logging.info("system information: ");
		logging.info(" configed version " + Globals.VERSION + " " + Globals.VERDATE + " " + Globals.VERHASHTAG);
		logging.info(" running by java version " + javaVersion);
		sslversionCheck(true);
		//logging.info("configed, start with Locale");
		cm = new ConfigedMain(appletHost, paramHost, paramUser, paramPassword);

		if (appletHost != null)
			((configedApplet) appletHost).setMainController(cm);

		SwingUtilities.invokeLater(new Runnable(){
			public void run()
			{
				cm.init();
			}
		});

		/*
		if (appletHost != null)
		{
			
			if (paramClient != null || paramClientgroup  != null)
			{
				if (paramClientgroup != null) cm.setGroup(paramClientgroup);
				if (paramClient != null)cm.setClient(paramClient);
				
				if (paramTab != null)
					cm.setVisualViewIndex(paramTab);
				
				
			}
	}
		else
		{
			if (paramClient != null || paramClientgroup  != null)
			{
				if (paramClientgroup != null) cm.setGroup(paramClientgroup);
				if (paramClient != null)cm.setClient(paramClient);
				
				logging.info("set client " + paramClient);
				
				
				
				if (paramTab != null)
					cm.setVisualViewIndex(paramTab);
				
				
			}
	}
		*/


		try
		{

			if (appletHost != null)
			{
				SwingUtilities.invokeLater(
				    new Runnable(){
					    public void run(){

						    if (paramClient != null || paramClientgroup  != null)
						    {
							    if (paramClientgroup != null) cm.setGroup(paramClientgroup);
							    if (paramClient != null)cm.setClient(paramClient);
							    logging.info("set client " + paramClient);

							    if (paramTab != null)
								    cm.setVisualViewIndex(paramTab);


						    }

					    }
				    }
				);
			}
			else


			{
				SwingUtilities.invokeAndWait(
				    new Runnable(){
					    public void run(){

						    if (paramClient != null || paramClientgroup  != null)
						    {
							    if (paramClientgroup != null) cm.setGroup(paramClientgroup);
							    if (paramClient != null)cm.setClient(paramClient);
							    logging.info("set client " + paramClient);

							    if (paramTab != null)
								    cm.setVisualViewIndex(paramTab);


						    }

					    }
				    }
				);
			}
		}
		catch(Exception ex)
		{
			logging.info(" run " + ex);
		}

	}



	/** construct the application */
	public configed(JApplet appletHost, String paramLocale, String paramHost, String paramUser, String paramPassword, final String paramClient, final String paramClientgroup, final Integer paramTab, String paramLogdirectory)
	{

		UncaughtExceptionHandler errorHandler = new UncaughtExceptionHandlerLocalized();
		Thread.setDefaultUncaughtExceptionHandler(errorHandler);


		System.out.println ("starting " + getClass().getName());
		System.out.println ("default charset is " + Charset.defaultCharset().displayName());
		System.out.println ("server charset is configured as "  + serverCharset);
		System.out.println(" applet " + appletHost);

		if (serverCharset.equals(  Charset.defaultCharset() ) )
		{
			serverCharset_equals_vm_charset = true;
			System.out.println ("they are equal");
		}

		configureUI();

		String imageHandled = "(we start image retrieving)";
		//System.out.println (imageHandled);
		try
		{
			String resourceS = "opsi.gif";
			URL resource = Globals.class.getResource(resourceS);
			if (resource == null)
			{
				System.out.println ("image resource " + resourceS + "  not found");
			}
			else
			{
				Globals.mainIcon = Toolkit.getDefaultToolkit().createImage(resource);
				imageHandled = "setIconImage";
			}
		}
		catch(Exception ex)
		{
			System.out.println ("imageHandled failed: " + ex.toString());
		}


		System.out.println("--  wantedDirectory " + logging.wantedDirectory);


		// set wanted directory for logging
		if (logdirectory != null)
			logging.wantedDirectory = logdirectory;
		else
			logging.wantedDirectory = "";

		System.out.println(" --  wantedDirectory " + logging.wantedDirectory);


		String[] nameParts = getClass().getName().split("\\.");
		if ( (System.getenv("LOCALAPPDATA") != null) && (nameParts.length>0) )
		{
			logging.programSubDir = "" + nameParts[nameParts.length-1];
		}
		else if (nameParts.length>0)
		{
			logging.programSubDir = "." + nameParts[nameParts.length-1];
		}



		// initialized in ConfigedMain
		/*
		File savedStatesDir = new File
			
			(System.getProperty("user.home") + File.separator + logging.programSubDir + 
			File.separator
			+  paramHost.replace(":", "_")
			);

		savedStatesDir.mkdirs();

		savedStates = new SavedStates(new File(savedStatesDir.toString()
			+  File.separator + savedStatesFilename));

		try{
			savedStates.load();
	}
		catch(IOException iox)
		{
			logging.info(this, "saved states file could not be loades");
	}
		*/



		// set locale
		java.util.List existingLocales = Messages.getLocaleNames();
		Messages.setLocale(paramLocale);


		logging.info("getLocales: " + existingLocales);
		logging.info("selected locale characteristic " + Messages.getSelectedLocale());


		configed.appletHost = appletHost;

		configed.paramHost = paramHost;
		configed.paramUser = paramUser;
		configed.paramPassword = paramPassword;
		configed.paramTab = paramTab;
		configed.paramClient = paramClient;
		configed.paramClientgroup = paramClientgroup;


		startWithLocale();
	}

	protected void revalidate()
	{
		cm.initialTreeActivation();
	}

	protected static void processArgs ( String[] args )
	{
		/*
		System.out.println("args:");
		for (int i = 0; i < args.length; i++)
		{
			System.out.println(args[i]);
	}
		*/

		de.uib.opsicommand.JSONthroughHTTP.gzipTransmission = true;
		
		
		if (args.length == 2 && args[0].equals("--args"))
		{
			args = args[1].split(";;");
		}


		for (int i = 0; i < args.length; i++)
		{

			if ( args[i].equals("--help") )
			{
				usage();
				endApp(0);
			}
		}


		int firstPossibleNonOptionIndex = args.length -1;
		int loglevel = logging.AKT_DEBUG_LEVEL;

		int i = 0;
		while (i < args.length)
		{
			String arg = args[i];
			//System.out.println("treat  i, arg " + i + ", " + arg);
			if (args[i].charAt(0) != '-') //no option
			{
				if (i < firstPossibleNonOptionIndex)
				{
					usage();
					endApp(0);
				}
				else
				{
					savedSearch = args[i];
					//methodCall = args[i];
					//only one of them can be used
				}
				i++;
			}
			else // options
			{
				//System.out.println(" option  " + arg);
				if ( args[i].equals("-l") || args[i].equals("--locale") )
				{
					locale = getArg(args, i);
					i = i+2;
				}
				else if ( args[i].equals("-h") || args[i].equals("--host") )
				{
					host = getArg(args, i);
					i = i+2;
				}
				else if ( args[i].equals("-u") || args[i].equals("--user") )
				{
					user = getArg(args, i);
					i = i+2;
				}
				else if ( args[i].equals("-p") || args[i].equals("--password") )
				{
					password = getArg(args, i);
					i = i+2;
				}
				else if ( args[i].equals("-c") || args[i].equals("--client") )
				{
					client = getArg(args, i);
					i = i+2;
				}

				else if ( args[i].equals("-g") || args[i].equals("--group") )
				{
					clientgroup = getArg(args, i);
					i = i+2;
				}

				else if ( args[i].equals("-t") || args[i].equals("--tab") )
				{
					String tabS = getArg(args, i);
					try
					{
						tab =  Integer.parseInt (tabS);
					}
					catch(NumberFormatException ex)
					{
						System.out.println ("  \n\nArgument >" + tabS + "< has no integer format");
						usage();
						endApp(1);
					}
					i = i+2;
				}
				else if ( args[i].equals("-d") || args[i].equals("--logdirectory") )
				{
					logdirectory = getArg(args, i);

					i = i+2;
				}

				else if ( args[i].equals("-r") || args[i].equals("--refreshminutes") )
				{
					String test = getArg(args, i);

					try{
						refreshMinutes = Integer.valueOf(test);
					}
					catch(NumberFormatException ex)
					{
						System.out.println ("  \n\nArgument >" + test + "< has no integer format");
						usage();
						endApp(1);
					}

					i = i+2;
				}

				else if ( args[i].equals("--ssh-key") )
				{
					sshkey = getArg(args, i);
					i = i+2;
				}
				else if ( args[i].equals("--ssh-passphrase") )
				{
					sshkeypassphrase = getArg(args, i);
					i = i+2;
				}

				else if (args[i].equals("--gzip") )
				{
					de.uib.opsicommand.JSONthroughHTTP.gzipTransmission = true;
					i = i+1;
					//System.out.println ("gzip");

					if (isValue(args, i))
					{
						//System.out.println (args[i]);
						if (args[i].toUpperCase().equals("Y"))
							de.uib.opsicommand.JSONthroughHTTP.gzipTransmission = true;
						else if (args[i].toUpperCase().equals("N"))
							de.uib.opsicommand.JSONthroughHTTP.gzipTransmission = false;
						else
						{
							usage();
							endApp(1);
						}
						i = i+1;
					}
				}
				
				else if ( args[i].equals("--sslversion") )
				{
					PREFERRED_SSL_VERSION = getArg(args, i);
					if ( !args[i+1].equalsIgnoreCase( STATEOFTHEART_SSL_VERSION ) )
					{
						PREFERRED_SSL_VERSION = args[i+1];
					}
					sslversionCheck( false ); //keep sslversion as set by this parameter
					
					i = i+2;
				}

				else if (args[i].equals("-qs") || args[i].equals("--querysavedsearch") )
				{
					optionCLIQuerySearch = true;
					i=i+1;
				}
				
				else if (args[i].equals("-me") || args[i].equals("--testPersistenceControllerMethod") )
				{
					optionPersistenceControllerMethodCall = true;
					i=i+1;
				}

				else if (args[i].equals("--sqlgethashes"))
				{
					de.uib.opsidatamodel.PersistenceControllerFactory.sqlAndGetHashes = true;
					i=i+1;
				}

				else if (args[i].equals("--sqlgetrows"))
				{
					de.uib.opsidatamodel.PersistenceControllerFactory.sqlAndGetRows = true;
					i=i+1;
				}
				
				else if (args[i].equals("--nosqlrawdata"))
				{
					de.uib.opsidatamodel.PersistenceControllerFactory.avoidSqlRawData = true;
					i=i+1;
				}

				/*
				else if (args[i].equals("--dblocal"))
				{
					de.uib.opsidatamodel.PersistenceControllerFactory.localDB= true;
					i=i+1;
				}

				else if (args[i].equals("--dblocalnew"))
				{
					de.uib.opsidatamodel.PersistenceControllerFactory.localDBResync= true;
					i=i+1;
				}

				else if (args[i].equals("--synced"))
				{
					de.uib.opsidatamodel.PersistenceControllerFactory.synced= true;
					i=i+1;
				}

				else if (args[i].equals("--sqldirect"))
				{
					de.uib.opsidatamodel.PersistenceControllerFactory.sqlDirect = true;
					i=i+1;
				}
				*/
				
				else if (args[i].equals("--sqldirect-cleanup-auditsoftware"))
				{
					de.uib.opsidatamodel.PersistenceControllerFactory.sqlDirect = true;
					de.uib.opsidatamodel.PersistenceControllerFactory.directmethodcall 
					=   de.uib.opsidatamodel.PersistenceControllerFactory.directmethodcall_cleanupAuditsoftware;
					i=i+1;
				}

				else if (args[i].equals("--version") )
				{
					System.out.println("configed version: " + Globals.VERSION + " (" + Globals.VERDATE + ") ");
					System.exit(0);
				}

				else if (args[i].equals("--help") )
				{
					usage();
					System.exit(0);
				}

				
				else if ( args[i].equals("--collect_queries_until_no") )
				{
					String no = getArg(args, i);
					try
					{
						de.uib.opsicommand.OpsiMethodCall.maxCollectSize  =  Integer.parseInt (no);
					}
					catch(NumberFormatException ex)
					{
						System.out.println ("  \n\nArgument >" + no+ "< has no integer format");
						usage();
						endApp(1);
					}
					i = i+2;
				}

				else if (args[i].equals("--loglevel") )
				{
					try
					{
						loglevel =  Integer.parseInt (getArg(args, i));
					}
					catch(NumberFormatException ex)
					{
						System.out.println (" \n\nArgument >" + getArg(args,i) + "< has no integer format");
					}
					i = i+2;

				}

				else if (args[i].equals("--halt") )
				{
					useHalt = true;
					i = i+1;
				}


				else
				{
					System.out.println("an option is not valid: " + arg);
					usage();
					endApp(0);
				}
			}
		}
		
		if (loglevel != logging.AKT_DEBUG_LEVEL)
		{
			if (optionCLIQuerySearch)
				logging.setSuppressConsole(true);
			
			// ? is setting allowed
			if (      loglevel <= logging.LEVEL_DEBUG
					  &&  loglevel >= logging.LEVEL_FATAL )

				logging.setAktDebugLevel(loglevel);
			else
				logging.info(" valid log levels between " + logging.LEVEL_FATAL + " and " + logging.LEVEL_DEBUG);
			
			loglevel = logging.AKT_DEBUG_LEVEL;

		}
	}

	public static String encodeStringFromService( String s )
	{
		//logging.debug("configed: to encode " + s);

		return s;

		// change of encoding seems now not be necessary any more
		/*
		if  (serverCharset_equals_vm_charset)
			  return s;
		  
		 
		if (s == null || s.equals(""))
			return s;




		String result = new String ( s.getBytes( Charset.defaultCharset()), serverCharset );

		logging.debug("configed: new encoding " + result);

		return result;
		*/


	}


	public static String encodeStringForService( String s )
	{
		return s;
		// change of encoding seems now not be necessary any more
		/*
		if  (serverCharset_equals_vm_charset)
			  return s;
		  
		return   new String ( s.getBytes( serverCharset), Charset.defaultCharset()) ;
		*/
	}

	public static boolean get_serverCharset_equals_vm_charset()
	{
		boolean b = serverCharset_equals_vm_charset;
		return b;
	}
	
	public static void sslversionCheck(boolean correctingVersion)
	{
		//do it only once and keep checked info globally
	
		if (correctingVersion)
		{
			if ( javaVersion.startsWith("1.7") && !PREFERRED_SSL_VERSION.equals( JAVA_1_7_DEFAUTL_SSL_VERSION ) )
				PREFERRED_SSL_VERSION = JAVA_1_7_DEFAUTL_SSL_VERSION;
		}
		
		if ( !PREFERRED_SSL_VERSION.equals ( STATEOFTHEART_SSL_VERSION ) ) 
			logging.warning("call for a SSL Version "  + PREFERRED_SSL_VERSION + "  different from the recommended " + STATEOFTHEART_SSL_VERSION);
		
		sslversionChecked = true;
	}

	public static void endApp(int exitcode)
	{
		if (savedStates != null)
		{
			try{
				savedStates.store("states on finishing configed");
			}
			catch(IOException iox)
			{
				System.out.println("could not store saved states, " + iox);
			}
		}

		if (!isApplet)
		{
			de.uib.opsicommand.OpsiMethodCall.report();
			logging.info("regularly exiting app with code " + exitcode); 			
			System.exit(exitcode);
		}
	}


	public static String getResourceValue( String key )
	{
		String result = key;
		try
		{
			result = Messages.messages.getString(key);
		}
		catch ( MissingResourceException mre)
		{
			// we return the key and log the problem:
			logging.debug("Problem: " + mre.toString());
			//System.out.println (" ----------- " + mre.toString());

			try
			{
				result = Messages.messagesEN.getString(key);
			}
			catch ( MissingResourceException mre2)
			{
				logging.debug("Problem: " + mre2.toString());
				//System.out.println (" ----------- " + mre2.toString());
			}
		}
		catch (Exception ex)
		{
			logging.warning("messages not there");
		}

		return result;
	}

	// from the JGoodies Library, we take the following function, observing

	/*
	 * Copyright (c) 2001-2008 JGoodies Karsten Lentzsch. All Rights Reserved.
	 *
	 * Redistribution and use in source and binary forms, with or without
	 * modification, are permitted provided that the following conditions are met:
	 *
	 *  o Redistributions of source code must retain the above copyright notice,
	 *    this list of conditions and the following disclaimer.
	 *
	 *  o Redistributions in binary form must reproduce the above copyright notice,
	 *    this list of conditions and the following disclaimer in the documentation
	 *    and/or other materials provided with the distribution.
	 *
	 *  o Neither the name of JGoodies Karsten Lentzsch nor the names of
	 *    its contributors may be used to endorse or promote products derived
	 *    from this software without specific prior written permission.
	 *
	 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
	 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
	 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
	 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
	 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
	 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
	 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
	 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
	 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
	 * OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
	 * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
	 */


	/**
	    
	   private void configureUI() {
	       UIManager.put(Options.USE_SYSTEM_FONTS_APP_KEY, Boolean.TRUE);
	       Options.setDefaultIconSize(new Dimension(18, 18));

	       String lafName =
	           LookUtils.IS_OS_WINDOWS_XP
	               ? Options.getCrossPlatformLookAndFeelClassName()
	               : Options.getSystemLookAndFeelClassName();

	       try {
	           UIManager.setLookAndFeel(lafName);
	       } catch (Exception e) {
	           System.err.println("Can't set look & feel:" + e);
	       }
	   }

	*/

	public static void configureUI()
	{
		boolean trynimbus = true;
		boolean found = false;

		if (trynimbus)
		{
			try {
				for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
					if ("Nimbus".equals(info.getName())) {
						logging.info("setting Nimbus look&feel");
						UIManager.setLookAndFeel(info.getClassName());
						logging.info("Nimbus look&feel set");

						//System.out.println(UIManager.getDefaults());

						Color defaultNimbusSelectionBackground = (Color) UIManager.get("nimbusSelectionBackground");
						//UIManager.put("nimbusSelectionBackground", UIManager.get("nimbusLightBackground"));


						UIManager.put("Tree.selectionBackground", UIManager.get("controlHighlight"));
						//was chosen: UIManager.put("nimbusSelectionBackground", UIManager.get("controlHighlight"));
						//UIManager.put("Tree[Enabled+Selected].collapsedIconPainter",  new javax.swing.text.DefaultHighlighter.DefaultHighlightPainter(java.awt.Color.yellow));
						//UIManager.put("Tree.rendererMargins", new Insets(0,0,0,0));

						//UIManager.put("Tree.drawHorizontalLines", true);
						//UIManager.put("Tree.drawVerticalLines", true);

						UIManager.put("TreeUI", de.uib.configed.tree.ClientTreeUI.class.getName());


						found = true;
						break;
					}
				}
			} catch (javax.swing.UnsupportedLookAndFeelException e) {
				// handle exception
				System.out.println (e);
			} catch (ClassNotFoundException e) {
				// handle exception
				System.out.println (e);
			} catch (InstantiationException e) {
				// handle exception
				System.out.println (e);
			} catch (IllegalAccessException e) {
				// handle exception
				System.out.println (e);
			}
		}

		if (!found)
			trynimbus = false;

		if (!trynimbus)
		{
			try
			{
				UIManager.setLookAndFeel("javax.swing.plaf.metal.MetalLookAndFeel");
			}
			catch(Exception ex)
			{
				System.out.println ("UIManager.setLookAndFeel('javax.swing.plaf.metal.MetalLookAndFeel')," +  ex);
			}
		}


		//UIManager.put("SplitPane.dividerFocusColor", Globals.backBlue);
		//UIManager.put("SplitPane.darkShadow", Globals.backBlue);

		/*
		UIManager.put("ProgressBar.background", Globals.backLightBlue); 
			UIManager.put("ProgressBar.foreground", Globals.backLightBlue);
			UIManager.put("ProgressBar.selectionBackground", Color.red);
			UIManager.put("ProgressBar.selectionForeground", Globals.backLightBlue);
			 */
			 
		//JPopupMenu.setDefaultLightWeightPopupEnabled(false);
		//destroys some popups, saves others

	}

	private static void addMissingArgs()
	{
		if( host == null )
			host = getInfo( "Host: ", false );
		if( user == null )
			user = getInfo( "User: ", false );
		if( password == null )
			password = getInfo( "Password: ", true );

	}


	public static String getInfo( String question, boolean password )
	{
		if( System.console() == null )
		{
			return "";
		}
		System.out.print( question );

		if( password )
		{
			return String.valueOf( System.console().readPassword() ).trim();
		}

		return String.valueOf( System.console().readLine() ).trim();
	}



	public static de.uib.opsidatamodel.PersistenceController connect()
	{
		Messages.setLocale("en");
		de.uib.opsidatamodel.PersistenceController controller
		= de.uib.opsidatamodel.PersistenceControllerFactory.getNewPersistenceController( host, user, password );
		if( controller == null )
		{
			System.err.println("Authentication error.");
			System.exit(1);
		}
		
		if( controller.getConnectionState().getState() != ConnectionState.CONNECTED )
		{
			System.err.println("Authentication error.");
			System.exit(1);
		}

		return controller;
	}


	/** main-Methode
	*/
	public static void main(String[] args)
	{

		de.uib.utilities.Globals.APPNAME = Globals.APPNAME;
		de.uib.utilities.Globals.iconresourcename = Globals.iconresourcename;


		//logging.init(); too early, variables not set

		processArgs(args);


		if (optionCLIQuerySearch)
		{
			//System.out.println("optionCLIQuerySearch");
			de.uib.configed.clientselection.SavedSearchQuery query = new de.uib.configed.clientselection.SavedSearchQuery();
			query.setArgs( host, user, password, savedSearch);
			query.addMissingArgs();
			query.runSearch();
			System.exit(0);
			
		}

		if (optionPersistenceControllerMethodCall)
		{

			addMissingArgs();

			PersistenceController controller = connect();
			java.util.List<Map<String, Object>> opsiHosts= controller.HOST_read();

			//System.out.println( "" + controller.getOpsiHostNames());
			System.exit(0);

			//System.out.println(" called me with " + host + ", " + user + ",  " + methodCall);
		}
		
		if (de.uib.opsidatamodel.PersistenceControllerFactory.sqlDirect)
		{
			if (logdirectory != null)
				logging.wantedDirectory = logdirectory;
			else
				logging.wantedDirectory = "";
			
			addMissingArgs();
			
			PersistenceController controller = connect();
			System.exit(0);

		}
				
			
			
			
		



		String imageHandled = "(we start image retrieving)";


		//System.out.println (imageHandled);
		try
		{
			String resourceS =de.uib.utilities.Globals.iconresourcename;
			URL resource =de.uib.configed.Globals.class.getResource(resourceS);
			if (resource == null)
			{
				System.out.println ("image resource " + resourceS + "  not found");
			}
			else
			{
				de.uib.utilities.Globals.mainIcon = Toolkit.getDefaultToolkit().createImage(resource);
				imageHandled = "setIconImage";
			}
		}
		catch(Exception ex)
		{
			System.out.println ("imageHandled failed: " + ex.toString());
		}

		// Turn on antialiasing for text (not for applets)
		try
		{
			System.setProperty("swing.aatext", "true");
		}
		catch(Exception ex)
		{
			logging.info(" setting property swing.aatext" + ex);
		}



		 

		new configed(null, // we dont construct it for an applet
		             locale, host, user, password, client, clientgroup, tab, logdirectory);
	}

}
