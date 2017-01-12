/**
 *   PersistenceController
 *   implementation for the New Object Model (opsi 4.0) overwritten by direct sql
 *   description: instances of PersistenceController serve 
 *   as proxy objects which give access to remote objects (and buffer the data)
 * 
 *  A  PersistenceController retrieves its data from a server that is compatible with the  
 *  opsi data server.
 *  It has a Executioner component that transmits requests to the opsi server and receives the responses.
 *  There are several classes which implement the Executioner methods in different ways 
 *  dependent on the used means and protocols
 *
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *  
 *    
 *  copyright:     Copyright (c) 2013
 *  organization: uib.de
 * @author  R. Roeder 
 */

package de.uib.opsidatamodel;

import java.util.*;
import java.sql.*;
import java.io.File;
import de.uib.utilities.logging.*;
import de.uib.opsicommand.*;
import de.uib.configed.*;
import de.uib.utilities.logging.*;
import de.uib.configed.type.*;
import de.uib.utilities.table.*;

import de.uib.opsidatamodel.productstate.*;


public class OpsiserviceLocalSQLPersistenceController extends OpsiserviceNOMPersistenceController
{
	boolean resync = false;
	
	String localTablePath = System.getProperty("user.home") + File.separator + ".configed" + File.separator;
	java.sql.Connection derbyConn;
	
	TableProductOnClient tableProductOnClient;
	
	public OpsiserviceLocalSQLPersistenceController (String server, String user, String password)
	{
		this(server, user, password, false);
	}
	
	public OpsiserviceLocalSQLPersistenceController (String server, String user, String password, boolean resync)
	{
		super(server, user, password);
		
		this.resync = resync; 
		if (System.getenv("LOCALAPPDATA") != null) 
		{
			localTablePath = System.getenv("LOCALAPPDATA") + File.separator + "configed" + + File.separator;
		}
		
		derbyConn = DerbyConnect.getConnection(localTablePath, server, resync);
		
		tableProductOnClient = new TableProductOnClient();
		
		if (resync) 
			createProductStates();
		
		
	}
	
	@Override
	public java.util.List<Map<java.lang.String,java.lang.Object>> HOST_read()
	{
			
		logging.info(this, "HOST_read ");
		String query = "select *  from HOST";
		TimeCheck timer= new TimeCheck(this, "HOST_read").start();
		
		logging.check(this, "HOST_read");
		java.util.List<Map<java.lang.String,java.lang.Object>> opsiHosts = exec.getListOfMaps(
				new OpsiMethodCall(
					"getData",
					new Object[]{query}
				)
			)
			;
		timer.stop();
		
		return opsiHosts;
	}
	
	private String giveWhereOR(String colName, String[] values)
	{
		if (values == null || values.length == 0)
			return "true";
		
		StringBuffer result = new StringBuffer(colName + " = '" + values[0] + "'");
		
		int lineCount = 0;
		
		for (int i = 1; i < values.length; i++)
		{
			result.append(" OR ");
			result.append(colName);
			result.append(" = '");
			result.append(values[i]);
			result.append("'      ");
			lineCount++;
			if (lineCount == 100)
			{
				result.append("\n");
				lineCount = 0;
			}
		}
		
		return result.toString();
	}		
	

	private  Map<String, java.util.List<Map<String, String>>> getLocalProductStatesFromDB( String query )
	{
		Map<String, java.util.List<Map<String, String>>> result = new HashMap<String, java.util.List<Map<String, String>>>(); 
		
		try
		{
			
			java.sql.Statement stat = derbyConn.createStatement(
								ResultSet.TYPE_SCROLL_INSENSITIVE,
								ResultSet.CONCUR_READ_ONLY
								);
		
			ResultSet rs = stat.executeQuery(query);
			
			
			while (rs.next())
			{
				String client = rs.getString("clientId");
				
				java.util.List<Map<String, String>>states1Client = result.get(client);
				if (states1Client == null)
				{
					states1Client = new ArrayList<Map<String, String>>();
					result.put(client, states1Client);
				}
				
				Map<String, String> rowMap = new HashMap<String, String>();
				
				
				for (String col : ProductState.DB_COLUMNS.keySet())
				{
					if (rs.getString(col) == null)
						rowMap.put(col, "");
					
					else
						rowMap.put(col, rs.getString(col));
				}
				
				states1Client.add(new ProductState(rowMap,true));
				
				
				if (rs.getObject(ProductState.columnIndexLastStateChange + 1) != null)
				{
					Timestamp lastModification = rs.getTimestamp(ProductState.columnIndexLastStateChange + 1);
					
					if (lastModification.after( tableProductOnClient.highestTimestamp ))
						tableProductOnClient.highestTimestamp = lastModification;
				}
				
			}
		}
		catch( SQLException e )
		{
			logging.info(this,"getLocalBootProductStatesNOM sql Error  in:\n" +query);
			logging.error("getLocalBootProductStatesNOM sql Error " +e.toString());
		}
		
		//timer.stop();
	
		return result;
		
	}
	
	
	private void createProductStates()
	{
		logging.debug(this, "createProductStates"); 
		
		java.util.List<String> columns = TableProductOnClient.columns;
		String columnsString = TableProductOnClient.columnsString;
		
		String queryTable = "select " + columnsString + " from PRODUCT_ON_CLIENT "; 
		
			
		java.util.List<java.util.List<java.lang.String>> 
			productOnClients
			= exec.getListOfStringLists(
				new OpsiMethodCall(
					"getRawData",
					new Object[]{queryTable}
				)
			)
			;
		
		int rowCount = 0;
		for (java.util.List<String> row : productOnClients)
		{
			rowCount++;
			
			if (rowCount % 1000 == 0)
				logging.debug(this, "getLocalBootProductStatesNOM  row " + rowCount);
			//logging.debug(this, "getLocalBootProductStatesNOM row " + rowCount  + ": " + row);
			tableProductOnClient.insertRow(row);
		}
		
	}
	
		
		
	
	private void updateProductStates()
	{
		
		Timestamp lastEntryTime = tableProductOnClient.retrieveHighestTimestamp();
		
		java.util.List<String> columns = TableProductOnClient.columns;
		String columnsString = TableProductOnClient.columnsString;
		
		String queryUpdateDB = "select " + columnsString + " from PRODUCT_ON_CLIENT " 
			+ " where "
			+ ProductState.DB_COLUMN_NAMES.get( ProductState.columnIndexLastStateChange ) + " >= '" 
			+ lastEntryTime + "'";   
		
			
		java.util.List<java.util.List<java.lang.String>> 
			productOnClients
			= exec.getListOfStringLists(
				new OpsiMethodCall(
					"getRawData",
					new Object[]{queryUpdateDB}
				)
			)
			;
		
		int rowCount = 0;
		for (java.util.List<String> row : productOnClients)
		{
			rowCount++;
			logging.debug(this, "getLocalBootProductStatesNOM row " + rowCount  + ": " + row);
			//tableProductOnClient.writeRow(row);
		}
	}
		
	
	
	@Override
	protected  Map<String, java.util.List<Map<String, String>>> getLocalBootProductStatesNOM(String[] clientIds)
	{
		
		//updateProductStates();
		
		java.util.List clients = java.util.Arrays.asList(clientIds);
		
		java.util.List<String> columns = new ArrayList<String>(ProductState.DB_COLUMN_NAMES);
		columns.add("clientId");
		columns.add("productType");
		
		String columnsString = Arrays.toString( columns.toArray( new String[]{} )  ) ;
		columnsString = columnsString.substring(1);
		columnsString = columnsString.substring(0, columnsString.length()-1);
		
		
		String query = "select " + columnsString + " from PRODUCT_ON_CLIENT " 
			+ " where "  
			+ "productType = 'LocalbootProduct'"
			+ " AND \n" 
			+ " ( "  
			+ giveWhereOR("clientId", clientIds)
			+ ") ";
			
			
		logging.info(this, "getLocalBootProductStatesNOM query " + query);
		
		
		Map<String, java.util.List<Map<String, String>>> result = getLocalProductStatesFromDB( query );
		
		
		/*
		query = query 
			+ "AND  \n"
			+ ProductState.DB_COLUMN_NAMES.get( ProductState.columnIndexLastStateChange ) + " > '" 
			+ tableProductOnClient.highestTimestamp + "'";   
		
			
		logging.info(this, "getLocalBootProductStatesNOM query " + query);
		
		int rowCount = 0;
		
		java.util.List<java.util.List<java.lang.String>> 
			productOnClients
			= exec.getListOfStringLists(
				new OpsiMethodCall(
					"getRawData",
					new Object[]{query}
				)
			)
			;
			
			for (java.util.List<String> row : productOnClients)
			{
				rowCount++;
				
				//logging.debug(this, "getLocalBootProductStatesNOM row " + rowCount  + ": " + row);
				
				String client = row.get(0);
				
				java.util.List<Map<String, String>>states1Client = result.get(client);
				
				if (states1Client == null)
				{
					states1Client = new ArrayList<Map<String, String>>();
					result.put(client, states1Client);
				}
				
				Map<String, String> rowMap = new LinkedHashMap<String, String>(); 
				
				//logging.debug(this,  " getLocalBootProductStatesNOM columns " + columns);
				
				for (int i = 0; i < columns.size(); i++)
				{
					//logging.debug(this,  " getLocalBootProductStatesNOM  " + i + ": "  + columns.get(i) + " = " +  row.get(i));
					rowMap.put(columns.get(i), row.get(i));
				}
				
				tableProductOnClient.writeRow(rowMap);
				
				states1Client.add(new ProductState(rowMap,true));
				
				
				
				
			}
				
		*/
		//System.exit(0);
		
		return result;
	}
	
	
	
}



