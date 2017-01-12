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
import java.io.*;
import de.uib.utilities.logging.*;
import de.uib.opsicommand.*;
import de.uib.configed.*;
import de.uib.utilities.swing.*;
import de.uib.configed.type.*;
import de.uib.utilities.table.*;

import de.uib.opsidatamodel.productstate.*;
import de.uib.opsidatamodel.dbtable.*;



public class OpsiserviceSyncedPersistenceController extends OpsiserviceNOMPersistenceController
{
	boolean resync = false;
	
	protected String localTablePath = System.getProperty("user.home") + File.separator + ".configed" + File.separator;
	
	protected ProductOnClient tableProductOnClient;
	protected Map<String, Map<String, Map<String, String>>>  localbootProductRowmaps;
	protected Map<String, Map<String, Map<String, String>>>  netbootProductRowmaps;
	protected Map<String, Map<String, Map<String, String>>>  allProductRowmaps;
	
	final java.util.List<String> columns = ProductOnClient.columns;
	
	
	public OpsiserviceSyncedPersistenceController (String server, String user, String password)
	{
		this(server, user, password, false);
	}
	
	public OpsiserviceSyncedPersistenceController (String server, String user, String password, boolean resync)
	{
		super(server, user, password);
		
		logging.debug(this, "creation server, user, resync " + server + ", " + user + ", " + resync);
		
		this.resync = resync;
		
		if (System.getenv("LOCALAPPDATA") != null) 
		{
			localTablePath = System.getenv("LOCALAPPDATA") + File.separator + "configed" + File.separator + server.replace(':', '_') + File.separator + "tables" + File.separator; 
		}
		else
		{
			localTablePath = System.getProperty("user.home") + File.separator + ".configed" + File.separator + server.replace(':', '_') + File.separator + "tables" + File.separator; 
		}
		
		
		tableProductOnClient = new ProductOnClient(localTablePath);
		
		
		
	}
	
	@Override
	public void syncTables()
	{
		syncProductRowmaps();
	}
	
	
	@Override
	public java.util.List<Map<java.lang.String,java.lang.Object>> HOST_read()
	{
			
		logging.debug(this, "HOST_read ");
		String query = "select " + Host.columnsString + "  from HOST";
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
		
		for (Map<java.lang.String,java.lang.Object> entry : opsiHosts)
		{
			//logging.info(this, "HOST_read " + entry);
			Host.db2ServiceRowMap(entry);
			//logging.info(this, "HOST_read " + entry);
		}
		//System.exit(0);
		
		return opsiHosts;
	}
	
	private void syncProductRowmaps()
	{
		logging.debug(this, "syncLocalbootProductRowmaps");
		
		//boolean saveMode = false; // we save data locally on initiating
		
		if (resync)
		{
			resync = false;
			tableProductOnClient.resetHighTimeStamp();
			//saveMode = true;
		}
		
		
		updateProductRowmaps();
	}
	
	
	private void addRow(  
		Map<String, Map<String, Map<String, String>>> rowmaps, 
		String client, 
		String product, 
		Map<String, String> rowMap)
	{
		Map<String, Map<String, String>> clientData = rowmaps.get(client);
			
		if (clientData == null)
		{
			clientData = new HashMap<String, Map<String, String>>();
			rowmaps.put(client, clientData);
		}
		
		clientData.put(product, rowMap);
	}
	
	private void updateProductRowmaps()
	{
		logging.debug(this, "updateProductRowmaps");
		
		if (localbootProductRowmaps == null)
			localbootProductRowmaps = new HashMap<String, Map<String, Map<String, String>>>();
			
		if (netbootProductRowmaps == null)
			netbootProductRowmaps = new HashMap<String, Map<String, Map<String, String>>>();
		
		if (allProductRowmaps == null)
			allProductRowmaps = new HashMap<String, Map<String, Map<String, String>>>();
		

		TimeCheck timer= new TimeCheck(this, "updateProductRowmaps").start();		
		
		
		int clientIdCol = 10; // columns.indexOf("clientId");
		int productTypeCol = 11; //columns.indexOf("productType");
		int productIdCol = 0; //columns.indexOf("productId");
		int lastStateChangeCol = 9; //columns.indexOf("modificationTime");
		
		String columnsString = ProductOnClient.columnsString;
		
		String queryTable = "select " + columnsString + " from PRODUCT_ON_CLIENT "
			+ " \nwhere \n"
			//+ " productType = 'LocalbootProduct' and "
			+ " modificationTime >= '" + tableProductOnClient.getHighTimestamp() +"'"; 
		
		
		java.util.List<java.util.List<java.lang.String>> 
			data
			= exec.getListOfStringLists(
				new OpsiMethodCall(
					"getRawData",
					new Object[]{queryTable}
				)
			)
		;
		
		//logging.debug(this, " data " + data);
		
		for (java.util.List<String> row : data)
		{
			tableProductOnClient.compareToHighTimestamp(row.get(lastStateChangeCol));
			
			String client = row.get(clientIdCol);
			
			String product = row.get(productIdCol);
			String productType = row.get(productTypeCol);
			
			Map<String, String> rowMap = new HashMap<String, String>();
			for (int i = 0; i < columns.size(); i++)
			{
				/*
				logging.info(this, "columns.get(i) " + i + ", " + columns.get(i));
				logging.info(this, "serviceKeyMapping.getInverseMap().get(columns.get(i) " 
					+ i + ", " + tableProductOnClient.serviceKeyMapping.getInverseMap().get(columns.get(i)));
				*/
				rowMap.put( 
					ProductOnClient.serviceKeyMapping.getInverseMap().get(columns.get(i)), 
					JSONReMapper.giveEmptyForNullString(row.get(i))
					);
			}
			
			addRow(allProductRowmaps, client, product, rowMap);
			
			if (productType.equals(ProductOnClient.LOCALBOOTid))
				addRow(localbootProductRowmaps, client, product, rowMap);
			
			else if (productType.equals(ProductOnClient.NETBOOTid))
				addRow(netbootProductRowmaps, client, product, rowMap);
		}
		
		
		timer.stop();
		
		logging.debug(this, "updateProductRowmaps localbootProductRowmaps:\n" + allProductRowmaps);
		
		logging.debug(this, "updateProductRowmaps high timestamp " + tableProductOnClient.getHighTimestamp());
		
		
		
		
	}
	
	
	/*
	
	private void updateLocalbootProductRowmaps()
	{
		logging.debug(this, "updateLocalbootProductRowmaps");
		
		if (localbootProductRowmaps == null)
			localbootProductRowmaps = new HashMap<String, Map<String, Map<String, String>>>();
			
			

		TimeCheck timer= new TimeCheck(this, "updateLocalbootProductRowmaps").start();		
		
		java.util.List<String> columns = LocalbootProductOnClient.columns;
		
		int clientIdCol = 10; // columns.indexOf("clientId");
		int productTypeCol = 11; //columns.indexOf("productType");
		int productIdCol = 0; //columns.indexOf("productId");
		int lastStateChangeCol = 9; //columns.indexOf("modificationTime");
		
		String columnsString = LocalbootProductOnClient.columnsString;
		
		String queryTable = "select " + columnsString + " from PRODUCT_ON_CLIENT "
			+ " \nwhere \n"
			//+ " productType = 'LocalbootProduct' and "
			+ " modificationTime >= '" + tableProductOnClient.getHighTimestamp() +"'"; 
		
		
		java.util.List<java.util.List<java.lang.String>> 
			data
			= exec.getListOfStringLists(
				new OpsiMethodCall(
					"getRawData",
					new Object[]{queryTable}
				)
			)
		;
		
		//logging.debug(this, " data " + data);
		
		for (java.util.List<String> row : data)
		{
			String client = row.get(clientIdCol);
			
				
			
			String product = row.get(productIdCol);
			
			tableProductOnClient.compareToHighTimestamp(row.get(lastStateChangeCol));
			
			Map<String, Map<String, String>> clientData = localbootProductRowmaps.get(client);
			
			if (clientData == null)
			{
				clientData = new HashMap<String, Map<String, String>>();
				localbootProductRowmaps.put(client, clientData);
			}
			
			Map<String, String> rowMap = clientData.get(product);
			
			if (rowMap == null)
			{
				rowMap = new HashMap<String, String>();
				clientData.put(product, rowMap);
			}
			for (int i = 0; i < columns.size(); i++)
			{
				rowMap.put( 
					LocalbootProductOnClient.serviceKeyMapping.getInverseMap().get(columns.get(i)), 
					JSONReMapper.giveEmptyForNullString(row.get(i))
					);
			}
		}
		
		
		timer.stop();
		
		logging.debug(this, "updateLocalbootProductRowmaps localbootProductRowmaps:\n" + localbootProductRowmaps);
		
		logging.debug(this, "updateLocalbootProductRowmaps high timestamp " + tableProductOnClient.getHighTimestamp());
		
		
		
		
	}
	*/
	
	
	
	@Override
	protected  Map<String, java.util.List<Map<String, String>>> getNetBootProductStatesNOM(String[] clientIds)
	{
		updateProductRowmaps();
		
		java.util.List<String> clients = java.util.Arrays.asList(clientIds);
		
		Map<String, java.util.List<Map<String, String>>> result = new HashMap<String, java.util.List<Map<String, String>>>();
		
		for (String client : netbootProductRowmaps.keySet())
		{
			logging.debug(this, "getNetBootProductStatesNOM for client " + client);
			
			if (clients.contains(client))
			{
				for (String product : netbootProductRowmaps.get(client).keySet())
				{
					java.util.List<Map<String, String>> clientEntries = result.get(client);
					if (clientEntries == null)
					{
						clientEntries = new ArrayList<Map<String, String>>();
						result.put(client, clientEntries);
					}
					//Map<String, String> state =  netbootProductRowmaps.get(client).get(product); //not null since default were generated
					
					Map<String, String> state =  
						new ProductState(
							netbootProductRowmaps.get(client).get(product),
							true
						)
					;
					clientEntries.add(state);
					
					//logging.info(this,  "getNetBootProductStatesNOM state  " + state);
			
					
				}
			}
		}
		

		//logging.debug(this, "getNetBootProductStatesNOM for clients " + clients +  ": \n" + result);
		
		return result;
	}
	
	
	
	
	@Override
	protected  Map<String, java.util.List<Map<String, String>>> getProductStatesNOM(String[] clientIds)
	{
		updateProductRowmaps();
		
		java.util.List<String> clients = java.util.Arrays.asList(clientIds);
		
		Map<String, java.util.List<Map<String, String>>> result = new HashMap<String, java.util.List<Map<String, String>>>();
		
		for (String client : allProductRowmaps.keySet())
		{
			//logging.debug(this, "getProductStatesNOM for client " + client);
			
			if (clients.contains(client))
			{
				for (String product : allProductRowmaps.get(client).keySet())
				{
					java.util.List<Map<String, String>> clientEntries = result.get(client);
					if (clientEntries == null)
					{
						clientEntries = new ArrayList<Map<String, String>>();
						result.put(client, clientEntries);
					}
					//Map<String, String> state =  allProductRowmaps.get(client).get(product); //not null since default were generated
					
					Map<String, String> state =  
						new ProductState(
							allProductRowmaps.get(client).get(product),
							true
						)
					;
					clientEntries.add(state);
					
					//logging.info(this,  "getProductStatesNOM state  " + state);
			
				}
			}
		}
		
		
		return result;
	}
	
	
	@Override
	protected  Map<String, java.util.List<Map<String, String>>> getLocalBootProductStatesNOM(String[] clientIds)
	{
		
		updateProductRowmaps();
		
		java.util.List<String> clients = java.util.Arrays.asList(clientIds);
		
		Map<String, java.util.List<Map<String, String>>> result = new HashMap<String, java.util.List<Map<String, String>>>();
		
		for (String client : localbootProductRowmaps.keySet())
		{
			logging.debug(this, "getLocalBootProductStatesNOM for client " + client);
			
			if (clients.contains(client))
			{
				for (String product : localbootProductRowmaps.get(client).keySet())
				{
					java.util.List<Map<String, String>> clientEntries = result.get(client);
					if (clientEntries == null)
					{
						clientEntries = new ArrayList<Map<String, String>>();
						result.put(client, clientEntries);
					}
					//Map<String, String> state =  localbootProductRowmaps.get(client).get(product); //not null since default were generated
					
					Map<String, String> state =  
						new ProductState(
							localbootProductRowmaps.get(client).get(product),
							true
						)
					;
					clientEntries.add(state);
					
					//logging.info(this,  "getLocalBootProductStatesNOM state  " + state);
			
					
				}
			}
		}
		
		/*
		if (configed.useHalt)
		{
			logging.info(this, "getLocalBootProductStates, halt execution"); 
			System.exit(0);
		}
		*/

		//logging.debug(this, "getLocalBootProductStatesNOM for clients " + clients +  ": \n" + result);
		
		return result;
	}
	

	
}



