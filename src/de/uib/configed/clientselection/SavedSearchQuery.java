package de.uib.configed.clientselection;

import de.uib.opsidatamodel.*;
import de.uib.messages.Messages;
import de.uib.utilities.logging.logging;
import de.uib.opsicommand.ConnectionState;
import java.util.*;

/**
 * This class is a little command line tool which can execute saved searches.
 */
public class SavedSearchQuery
{
    private final String usage="\n" +
            "configed_savedsearch [OPTIONS] [NAME]\n\n" +
            "Runs the given search NAME and returns the matching clients. " +
            "If NAME is not set, list all available searches.\n\n" +
            "OPTIONS:\n" +
            "  -h\tConfiguration server to connect to\n" +
            "  -u\tUsername for authentication\n" +
            "  -p\tPassword for authentication\n";

    private String[] args;
    private String host;
    private String user;
    private String password;
    private String search;
    
     public SavedSearchQuery()
     {
     	 logging.wantedDirectory = "";
     	 logging.setAktDebugLevel(-1);
     }
    
     /* constructor for standalone call of this class
     */
	public SavedSearchQuery( String[] args )
	{
		this();
		this.args = args;
		if( !parseArgs() )
        {
            showUsage();
            System.exit(10);
        }
	}
	
	public boolean parseArgs()
    {
        String lastOption=null;
        search=null;
        for( int i=0; i<args.length; i++ )
        {
            if( args[i].equals("-h") || args[i].equals("-u") || args[i].equals("-p") )
            {
                if( lastOption != null )
                    return false;
                lastOption = args[i];
            }
            else
            {
                if( lastOption != null )
                {
                    addInfo( lastOption.trim(), args[i] );
                    lastOption=null;
                }
                else
                {
                    if( search != null )
                        return false;
                    search = args[i];
                }
            }
        }
        return true;
    }
	
	public void showUsage()
	{
		System.out.println(usage);
	}
    
    public void setArgs(String host,  String user,  String password,  String search)
    {
    		this.host = host;
    		this.user = user;
    		this.password = password;
    		this.search = search;
    }
    	
    
 
    
    public void addMissingArgs()
    {
        if( host == null )
            host = getInfo( "Host: ", false );
        if( user == null )
            user = getInfo( "User: ", false );
        if( password == null )
            password = getInfo( "Password: ", true );
    }
    
    public void runSearch()
    {
        Messages.setLocale("en");
        PersistenceController controller = PersistenceControllerFactory.getNewPersistenceController( host, user, password );
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
        
        Map<String, Map<java.lang.String,java.lang.Object>> depots = controller.getHostInfoCollections().getAllDepots();
        controller.getHostInfoCollections().getPcListForDepots( depots.keySet().toArray( new String[0] ) ); 
        
        SelectionManager manager = new SelectionManager(null);
        java.util.List<String> searches = manager.getSavedSearchesNames();
        if( search == null )
        {
            printResult( searches );
            return;
        }
        
        if( !searches.contains( search ) )
        {
            System.err.println( "Search not found." );
            System.exit(2);
        }
        
        manager.loadSearch( search );
        printResult( manager.selectClients() );
    }
    
    public static void main( String[] args )
    {
        SavedSearchQuery query = new SavedSearchQuery(args);
        if( !query.parseArgs() )
        {
            query.showUsage();
            System.exit(10);
        }
        query.addMissingArgs();
        query.runSearch();
    }
        
    
    private void addInfo( String option, String value )
    {
        if( option.equals("-h") )
            host=value;
        else if( option.equals("-u") )
            user=value;
        else if( option.equals("-p") )
            password=value;
        else
            throw new IllegalArgumentException( "Unknown option " + option );
    }
    
    private String getInfo( String question, boolean password )
    {
        if( System.console() == null )
            return "";
        System.out.print( question );
        if( password )
            return String.valueOf( System.console().readPassword() ).trim();
        return String.valueOf( System.console().readLine() ).trim();
    }
    
    private void printResult( List<String> result )
    {
        for( String line: result )
            System.out.println( line );
    }
}
