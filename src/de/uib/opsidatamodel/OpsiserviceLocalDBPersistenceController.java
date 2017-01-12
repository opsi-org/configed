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
import de.uib.utilities.logging.*;
import de.uib.configed.type.*;
import de.uib.utilities.table.*;

import de.uib.opsidatamodel.productstate.*;
import de.uib.opsidatamodel.dbtable.*;



public class OpsiserviceLocalDBPersistenceController extends OpsiserviceNOMPersistenceController
{
	boolean resync = false;
	
	protected String localTablePath = System.getProperty("user.home") + File.separator + ".configed" + File.separator;
	
	protected LocalbootProductOnClient tableLocalbootProductOnClient;
	protected Map<String, Map<String, Map<String, String>>>  localbootProductRowmaps;
	
	
	public OpsiserviceLocalDBPersistenceController (String server, String user, String password)
	{
		this(server, user, password, false);
	}
	
	public OpsiserviceLocalDBPersistenceController (String server, String user, String password, boolean resync)
	{
		super(server, user, password);
		
		logging.debug(this, "creation server, user, resync " + server + ", " + user + ", " + resync);
		
		this.resync = resync;
		
		if (System.getenv("LOCALAPPDATA") != null) 
		{
			localTablePath = System.getenv("LOCALAPPDATA") + File.separator + "configed" + File.separator + server.replace(':', '_') + File.separator + "tables" + File.separator; 
		
		else 
		{
			localTablePath = System.getProperty("user.home") + File.separator + ".configed" + File.separator + server.replace(':', '_') + File.separator + "tables" + File.separator; 
		
		}
		
		
		tableLocalbootProductOnClient = new LocalbootProductOnClient(localTablePath);
		
		syncLocalbootProductRowmaps();
		
		
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
	
	
	
	
	private void readLocalbootProductRowmaps()
	{
		logging.info(this, "readLocalbootProductRowmaps, local file " + tableLocalbootProductOnClient.getLocalFilePath());
		
		try
		{
			File localFile = new File( tableLocalbootProductOnClient.getLocalFilePath() ) ;
			
			if (!localFile.exists())
			{
				logging.info(this, "not existing: " + tableLocalbootProductOnClient.getLocalFilePath());
				return;
			}
			
			BufferedReader in = new BufferedReader(new FileReader(localFile));
			
			String versionLine = in.readLine();
			String versionLineCompare = Table.versionKey +":" +  LocalbootProductOnClient.version;
			if (!versionLineCompare.equals(versionLine))
			{
				in.close();
				logging.info(this, "readLocalbootProductRowmaps: cannot read data, version difference");
				return;
			}
			
			String line = in.readLine();
			
			while (line != null)
			{
				String[] parts = line.split(":");
				//System.out.println(Arrays.toString(parts));
				
				String client = parts[0];
				
				//logging.info(this, "reading client " + client);
				
				/*
				if (!client.equals("a-010-10.kbit.intern"))
				{
					line = in.readLine();
					continue;
				}
				*/
				
				String product = parts[1];
				String key = parts[2];
				String value = "";
				if (parts.length > 3)
					value = parts[3];
				
				if (key.equals("stateChange") && !value.equals(""))
					tableLocalbootProductOnClient.compareToHighTimestamp(value);
				//adjsut 
					
				
				Map<String, Map<String, String>> mClient = localbootProductRowmaps.get(client);
				if (mClient == null)
				{
					mClient = new HashMap<String, Map<String, String>>();
					localbootProductRowmaps.put(client, mClient);
				}
				Map<String, String> mProduct = mClient.get(product);
				if (mProduct == null)
				{
					mProduct = new HashMap<String, String>();
					mClient.put(product, mProduct);
				}
				mProduct.put(key, value);
				
				line = in.readLine();
			}
			
			
		}
		
		catch(Exception ex)
		{
			logging.info(this, "read error : " + ex);
		}
		
			
		
		
		
	}
	
	
	private void writeLocalbootProductRowmaps()
	{
		logging.info(this, "writeLocalbootProductRowmaps");
		try
		{
			if (localbootProductRowmaps == null)
				return;
		
			PrintWriter out = new PrintWriter( new BufferedWriter (new FileWriter(
				tableLocalbootProductOnClient.getLocalFilePath())));
			
			String versionLine  = Table.versionKey +":" +  LocalbootProductOnClient.version; 
			out.println( versionLine );
			
			for (String client : localbootProductRowmaps.keySet())
			{
				/*
				if (!client.equals("a-010-10.kbit.intern"))
					continue;
				*/
				
				
				for (String product : localbootProductRowmaps.get(client).keySet())
				{
					Map<String, String> m = localbootProductRowmaps.get(client).get(product); 
					for (String key : m.keySet())
					{
						out.print(client);
						out.print(":");
						out.print(product);
						out.print(":");
						out.print(key);
						out.print(":");
						out.print(m.get(key));
						out.println();
					}
				}
			}
			out.flush();
			
			out.close();
		}
		catch(Exception ex)
		{
			logging.error("write error : " + ex);
		}
		logging.info(this, "writeLocalbootProductRowmaps ready");
	}
	
	private void syncLocalbootProductRowmaps()
	{
		logging.debug(this, "syncLocalbootProductRowmaps");
		
		//boolean saveMode = false; // we save data locally on initiating
		
		localbootProductRowmaps = new HashMap<String, Map<String, Map<String, String>>>();
		tableLocalbootProductOnClient.resetHighTimeStamp();
		
		if (resync)
		{
			resync = false;
			//saveMode = true;
		}
		else
		{
			readLocalbootProductRowmaps();
		}
		
		updateLocalbootProductRowmaps();
		
		
		writeLocalbootProductRowmaps();
	}
	
	
	
	private void updateLocalbootProductRowmaps()
	{
		logging.debug(this, "updateLocalbootProductRowmaps");

		TimeCheck timer= new TimeCheck(this, "updateLocalbootProductRowmaps").start();		
		
		java.util.List<String> columns = LocalbootProductOnClient.columns;
		
		int clientIdCol = 10; // columns.indexOf("clientId");
		int productTypeCol = 11; //columns.indexOf("productType");
		int productIdCol = 0; //columns.indexOf("productId");
		int lastStateChangeCol = 9; //columns.indexOf("modificationTime");
		
		String columnsString = LocalbootProductOnClient.columnsString;
		
		String queryTable = "select " + columnsString + " from PRODUCT_ON_CLIENT "
			+ " \nwhere \n"
			+ " productType = 'LocalbootProduct' and "
			+ " modificationTime >= '" + tableLocalbootProductOnClient.getHighTimestamp() +"'"; 
		
		
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
			
			/*
			if (!client.equals("a-010-10.kbit.intern"))
					continue;
			*/	
				
			
			String product = row.get(productIdCol);
			
			//tableLocalbootProductOnClient.compareToHighTimestamp(row.get(lastStateChangeCol));
			
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
				/*
				logging.info(this, "columns.get(i) " + i + ", " + columns.get(i));
				logging.info(this, "serviceKeyMapping.getInverseMap().get(columns.get(i) " 
					+ i + ", " + LocalbootProductOnClient.serviceKeyMapping.getInverseMap().get(columns.get(i)));
				*/
				rowMap.put( 
					LocalbootProductOnClient.serviceKeyMapping.getInverseMap().get(columns.get(i)), 
					JSONReMapper.giveEmptyForNullString(row.get(i))
					);
			}
		}
		
		
		timer.stop();
		
		logging.debug(this, "updateLocalbootProductRowmaps localbootProductRowmaps:\n" + localbootProductRowmaps);
		
		logging.debug(this, "updateLocalbootProductRowmaps high timestamp " + tableLocalbootProductOnClient.getHighTimestamp());
		
		
		
		
	}
	
	
	/*
	private void updateLocalbootProductRowmapsJSON()
	{
		logging.debug(this, "updateLocalbootProductStates"); 
		
		java.util.List<String> columns = LocalbootProductOnClient.columns;
		String columnsString = LocalbootProductOnClient.columnsString;
		
		String queryTable = "select " + columnsString + " from PRODUCT_ON_CLIENT "
			+ " \nwhere \n"
			+ " productType = 'LocalbootProduct' and "
			+ " modificationTime >= '" + tableLocalbootProductOnClient.getHighTimestamp() +"'"; 
		
		org.json.JSONObject data = exec.retrieveJSONObject(
			new OpsiMethodCall(
					"getRawData",
					new Object[]{queryTable}
			)
		);
		
		
		
		//logging.debug(this, " data " + data);
		
		TimeCheck timer= new TimeCheck(this, "updateLocalbootProductStates, JSONReMapper").start();
		List<List<String>> dataList =  JSONReMapper.getListOfListsOfStrings(data, "result");
		timer.stop();
		//logging.debug(this, " datalist " + dataList);
		
		timer= new TimeCheck(this, "updateLocalbootProductStates,  build map").start();
		//Map<String, java.util.List<String>> map = new HashMap<String, java.util.List<String>>();
		Map<String,  Map<String, org.json.JSONArray>> map 
			= new HashMap<String, Map<String, org.json.JSONArray>>();
		
		int clientIdCol = 10; // LocalbootProductOnClient.columns.indexOf("clientId");
		int productTypeCol = 11; //LocalbootProductOnClient.columns.indexOf("productType");
		int productIdCol = 0; //LocalbootProductOnClient.columns.indexOf("productId");
		int lastStateChangeCol = 9; //LocalbootProductOnClient.columns.indexOf("modificationTime");
		
		
		for (int i=0; i < dataList.size(); i++)
		{
			//logging.debug(this, "datalist get i " + i + " : " + dataList.get(i));
			
			java.util.List<String> row = dataList.get(i);
			
			tableLocalbootProductOnClient.compareToHighTimestamp(row.get(lastStateChangeCol));
			
			Map<String, org.json.JSONArray> clientMap = map.get(row.get(clientIdCol));
			
			if (clientMap == null)
			{
				clientMap = new HashMap<String, org.json.JSONArray>();
				map.put(row.get(clientIdCol), clientMap);
			}
			clientMap.put(row.get(productIdCol), new org.json.JSONArray(row)); 
		}
		
		timer.stop();
		
		logging.debug(this, "" + map);
		
		//logging.debug(this, "" + new org.json.JSONObject(map));
		
		logging.debug(this, "updateLocalbootProductStates high timestamp " + tableLocalbootProductOnClient.getHighTimestamp());
		
		//System.exit(0);
		
	}
	*/
	
	@Override
	protected  Map<String, java.util.List<Map<String, String>>> getLocalBootProductStatesNOM(String[] clientIds)
	{
		
		updateLocalbootProductRowmaps();
		
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
		
		if (configed.useHalt)
		{
			logging.info(this, "getLocalBootProductStates, halt execution"); 
			System.exit(0);
		}
		

		//logging.debug(this, "getLocalBootProductStatesNOM for clients " + clients +  ": \n" + result);
		
		return result;
	}
	

	
}



