package de.uib.opsidatamodel.dbtable;

import java.sql.*;
import java.io.File;

import java.util.*;
import de.uib.utilities.logging.logging;

public class Table
{
	protected String highTimestampS = new Timestamp(0).toString();
	
	public final static String versionKey = "table version"; 
	
	//static String tableName = "HOST";
	protected String localTablePath;
	protected String localFileName;
	
	public Table(String localTablePath)
	{
		this.localTablePath  = localTablePath;
		new File(localTablePath).mkdirs();
	}
	
	
	public String getLocalFilePath()
	{
		return localTablePath;
	}
	
	public String getHighTimestamp()
	{
		/*
		try{
			highTimestamp = Timestamp.valueOf(highTimestampS);
		}
		catch(Exception ex)
		{
			logging.info(this, "getHighTimestamp  exception: " + ex);
		}
		*/
		return highTimestampS; 
	}
	
	public void resetHighTimeStamp()
	{
		 highTimestampS = new Timestamp(0).toString();
	}
	
	public void compareToHighTimestamp(String s) 
	{
		if (s.compareTo(highTimestampS) > 0)
			highTimestampS = s; 
	}
	
	
	protected String valueAssertion(String key, String value)
	{
		StringBuffer sb = new  StringBuffer("");
		sb.append(key);
		sb.append("=");
		sb.append("'");
		if (value == null)
			value = "";
		sb.append( value );
		sb.append("'");
		
		return sb.toString();
	}
	
	protected String conjunction(Map<String, String> rowMap,  String[] keys)
	{
		logging.debug(this, "conjunction keys " + Arrays.toString(keys));
		if (keys == null || keys.length == 0)
			return null;
		
		StringBuffer sb = new  StringBuffer("(");
		sb.append( valueAssertion( keys[0], rowMap.get( keys[0] ) ) );
		
		for (int i = 1; i < keys.length; i++)
		{	
			sb.append( " and ");
			sb.append( valueAssertion( keys[i], rowMap.get( keys[i] ) ) );
		}
		
		sb.append( ")" );
		
		logging.debug(this, "conjunction " + sb.toString());
		
		return sb.toString();
	}
	
	
}
