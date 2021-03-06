package de.uib.opsidatamodel.dbtable;

import java.sql.*;
import java.io.File;

import java.util.*;
import de.uib.utilities.table.*;
import de.uib.utilities.logging.logging;

public class ProductProperty extends Table
{
	public final static String tableName = "PRODUCT_PROPERTY_VALUE";
	
	public final static String version 
		= class.getName() + " " +  de.uib.configed.Globals.VERSION;
	
	
	public static java.util.List<String> columns;
	static {
		columns = new ArrayList<String>();
		columns.add("product_property_id");
		columns.add("productId");
		columns.add("productVersion");
		columns.add("packageVersion");
		columns.add("propertyId");
		columns.add("value");
		columns.add("isDefault");
		
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
		primaryKey.add("product_property_state_id");
		
		StringBuffer sb = new StringBuffer("");
		for (String key : primaryKey)
		{
			sb.append(key);
			sb.append(";");
		}
		primaryKeyString = sb.toString();
	}
	
	
	public ProductProperty(String localTablePath) 
	{
		super(localTablePath);
		this.localTablePath = localTablePath + File.separator + localFilename;
	}
}
