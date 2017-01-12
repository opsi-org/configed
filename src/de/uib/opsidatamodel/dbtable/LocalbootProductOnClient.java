package de.uib.opsidatamodel.dbtable;

import java.sql.*;
import java.io.File;

import java.util.*;
import de.uib.utilities.table.*;
import de.uib.opsidatamodel.productstate.ProductState;
import de.uib.utilities.logging.logging;

public class LocalbootProductOnClient extends ProductOnClient
{
	
	private static String tableName = "PRODUCT_ON_CLIENT";
	private final static String localFilename = "localbootproductstates.configed";
	public final static String version 
		//= new LocalbootProductOnClient("").getClass().getName() + " " +  de.uib.configed.Globals.VERSION;
		= LocalbootProductOnClient.class.getName() + " " +  de.uib.configed.Globals.VERSION;
	
	
	public static java.util.List<String> columns;
	static {
		columns = new ArrayList<String>(ProductState.DB_COLUMN_NAMES);
		columns.add("clientId");
		//columns.add("productType");
	}
	
	public static String columnsString;
	static{
		columnsString = Arrays.toString( columns.toArray( new String[]{} )  ) ;
		columnsString = columnsString.substring(1);
		columnsString = columnsString.substring(0, columnsString.length()-1);
	}
	
	public static java.util.List<String> primaryKey;
	public static String primaryKeyString;
	static{
		primaryKey = new ArrayList<String>();
		primaryKey.add("clientId");
		primaryKey.add("productId");
		primaryKey.add("productType");
		
		StringBuffer sb = new StringBuffer("");
		for (String key : primaryKey)
		{
			sb.append(key);
			sb.append(";");
		}
		primaryKeyString = sb.toString();
	}
	
	private static Map<String, String> key2servicekeyX = new HashMap<String, String> (ProductState.key2servicekey);
	static{
		key2servicekeyX.put("clientId", "clientId");
	}
	public static de.uib.utilities.Mapping<String, String> serviceKeyMapping 
	= new de.uib.utilities.Mapping(key2servicekeyX);
	

	
	public LocalbootProductOnClient(String localTablePath) 
	{
		super(localTablePath);
		this.localTablePath = localTablePath + File.separator + localFilename;
		logging.debug(this, "created, localTablePath " + localTablePath);
		logging.info(this, "LocalBootProductOnClient version " + version);
	}
	
	
	
}
	
