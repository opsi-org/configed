/**
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *  
 *    
 *  copyright:     Copyright (c) 2014
 *  organization: uib.de
 * @author  R. Roeder 
 */


package de.uib.opsidatamodel;

import java.util.*;
import de.uib.utilities.logging.*;
import de.uib.opsicommand.*;
import de.uib.configed.*;
import de.uib.configed.type.*;
import de.uib.opsidatamodel.productstate.*;
import de.uib.opsidatamodel.dbtable.*;
import de.uib.utilities.table.*;


public class DataStubRawData extends DataStubNOM
{
	public DataStubRawData(OpsiserviceNOMPersistenceController controller)
	{
		super(controller);
	}
	
	
	//can be used if we do not need table specific translations of key names and value types
	protected List<Map<String, Object>> retrieveListOfMapsBySQLselect(
		java.util.List<String> columns,
		String tables,
		String condition
		)
	{
		StringBuilder sb = new StringBuilder("select");
		
		if (columns == null|| columns.size() ==0)
		{
			sb.append(" * ");
		}
		else
		{
			sb.append(" ");
			sb.append(columns.get(0));
			for (int i = 1; i < columns.size(); i++)
			{
				sb.append(", ");
				sb.append(columns.get(i));
			}
		}
		sb.append("\n from ");
		sb.append(tables);
		sb.append("where \n");
		sb.append(condition);
		
		
		String query = sb.toString();
		
		java.util.List<java.util.List<java.lang.String>> 
			rows
				= controller.exec.getListOfStringLists(
					new OpsiMethodCall(
						"getRawData",
						new Object[]{query}
						)
					);
		for (List<String> row : rows)
		{
			logging.info(this, "sql produced row " + row);
		}
		
		return null;
		
	}
		
		
		
	
	@Override
	public boolean test()
	{
		if (!super.test())
			return false;
		
		//test if we can access any table
		
		String query = "select  *  from " +  SWAuditClientEntry.DB_TABLE_NAME  
					+ " LIMIT 1 "; 
		
		logging.info(this, "test, query " + query);
		
		boolean result
					= controller.exec.doCall(
						new OpsiMethodCall(
							"getRawData",
							new Object[]{query}
							)
						);
					
		logging.info(this, "test result " + result);
		return result;	
	}
	
	protected String giveWhereOR(String colName, java.util.List<String> values)
	{
		if (values == null || values.size() == 0)
			return "true";
		
		StringBuffer result = new StringBuffer(colName + " = '" + values.get(0) + "'");
		
		int lineCount = 0;
		
		for (int i = 1; i < values.size(); i++)
		{
			result.append(" OR ");
			result.append(colName);
			result.append(" = '");
			result.append(values.get(i));
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
		
	
	//===================================================
	
	//netbootStatesAndActions
	//localbootStatesAndActions
	
	
	//===================================================
	
	/* in superclass
	//protected  java.util.List <Map<String, Object>> productPropertyStates;
	//protected  java.util.List <Map<String, Object>> productPropertyDepotStates; //will only be refreshed when all product data are refreshed
	
	//protected java.util.Set<String> hostsWithProductProperties;
	*/
	
	// client is a set of added hosts, host represents the totality and will be update as a side effect
	
	
	@Override
	protected java.util.List <Map<String, Object>> produceProductPropertyStates(
	//protected java.util.List <Map<String, Object>> ENTWURFproduceProductPropertyStates(
			final Collection<String> clients,
			java.util.Set<String> hosts)
	{
		logging.debug(this, "produceProductPropertyStates new hosts " + clients  +  " old hosts " + hosts);
		
		//java.util.List <Map<String, Object>> compareList 	= super.produceProductPropertyStates(clients, hosts);
		//logging.info(this, "produceProductPropertyStates got  " + compareList.size());
		
		java.util.List <Map<String, Object>> result = new ArrayList<Map<String, Object>>();
		
		
		java.util.List<String> newClients = null;
		if (clients == null)
			newClients =  new ArrayList<String>();
		else 
			newClients = new ArrayList<String>(clients);
			
		if (hosts == null)
		{
			hosts = new HashSet<String>();
		}
		else
		{
			newClients.removeAll(hosts);
		}
		
		logging.debug(this, "produceProductPropertyStates, new hosts " + clients);
		
		
		if (newClients.size() == 0)
		{
		}	
		else 
		{
			hosts.addAll(newClients);
			
			//logging.info(this, "produceHostsWithProductPropertyStates, all hosts " + hosts);
			
			controller.notifyDataLoadingObservers(configed.getResourceValue("LoadingObserver.loadtable") + " product property state");
			
			
			StringBuilder cols = new StringBuilder("");
			cols.append( ProductPropertyState.tableName + "." + 
				ProductPropertyState.PRODUCT_ID );
			cols.append(", ");
			cols.append( ProductPropertyState.tableName + "." +
				ProductPropertyState.PROPERTY_ID );
			cols.append(", ");
			cols.append( ProductPropertyState.tableName + "." +
				ProductPropertyState.OBJECT_ID );
			cols.append(", ");
			cols.append( ProductPropertyState.tableName + "." +
				ProductPropertyState.VALUES );
			
				
			String query = "select \n"+ cols.toString()  
			+ "\n from " +  ProductPropertyState.tableName 
			+ "\n where " + giveWhereOR( ProductPropertyState.tableName + ".objectId", newClients);
			
			logging.info(this, "produceProductPropertyStates query " + query);
			
			java.util.List<java.util.List<java.lang.String>> 
				rows
					= controller.exec.getListOfStringLists(
						new OpsiMethodCall(
							"getRawData",
							new Object[]{query}
							)
						);
					
			logging.info(this, "produceProductPropertyStates got rows " + rows.size());
			int counter = 0;
			
			
			
			
			for (List<String> row : rows)
			{
				Map<String, Object> m = new HashMap<String, Object>();
				
				m.put( ProductPropertyState.PRODUCT_ID, row.get(0) ); 
				m.put( ProductPropertyState.PROPERTY_ID, row.get(1) );
				m.put( ProductPropertyState.OBJECT_ID, row.get(2) );
				
				//parse String and produce json list
				//ArrayList values = null;
				org.json.JSONArray values = null;
				try
				{
					values = new org.json.JSONArray(row.get(3));
				}
				catch(Exception ex)
				{
					logging.warning(this, "produceProductPropertyStates, error when json parsing database string \n" 
						+ row.get(3) + " for propertyId " + row.get(1) );
				}
				
				
				m.put( ProductPropertyState.VALUES, values);
				result.add(m);
				counter++;
			}
			
			
			logging.info(this, "produceProductPropertyStates produced  items " + counter);
			//logging.debug(this, "produceProductPropertyStates produced   " + result);
			//logging.info(this, "produceProductPropertyStates compare " + compareList);
			
			
			
			/*
			
			counter = 0;
			for (List<String> row : rows)
			{
				if (row.get(0).equals("firefox") && row.get(1).equals("profilemigrator"))
				{
					logging.info(this, "sql row  " + counter + ": " + row);
					for (int i = 0; i < row.size(); i++)
					{
						logging.info(this, "sql " + row.get(i));
					}
				
					Map<String, Object> m = new HashMap<String, Object>();
				
					m.put( ProductPropertyState.PRODUCT_ID, row.get(0) ); 
					m.put( ProductPropertyState.PROPERTY_ID, row.get(1) );
					m.put( ProductPropertyState.OBJECT_ID, row.get(2) );
					
					//parse String and produce json list
					org.json.JSONArray values = null;
					try
					{
						values = new org.json.JSONArray(row.get(3));
					}
					catch(Exception ex)
					{
						logging.warning(this, "produceProductPropertyStates, error when json parsing database string \n" 
							+ row.get(3) + " for propertyId " + row.get(1) );
					}
					
					
					m.put( ProductPropertyState.VALUES, values);
					
					logging.info(this, " values " + values);
					
					result.add(m);
					
					counter++;
				}
					
			}
			*/
			
			/*
			counter = 0;
			logging.info(this, "compare to ");
			for (Map<String, Object> m : compareList)
			{
				if (
					m.get("productId").equals("firefox")
					&&
					m.get("propertyId").equals("profilemigrator")
				)
					
				{
					logging.info(this, " .. " + counter);
					for (String key : m.keySet())
					{
						logging.info(this, " key " + key + " value of class "
							+ m.get(key).getClass().getName() + " : " 
							+ m.get(key) );
						
					}
					
							
						
					counter++;
				}
			}
			System.exit(0);
			*/
			
			//logging.info(this, "propstates: " + propstates);
		}
		
		return result;
	}
	

	
	
	//===================================================

	/* in superclass
	
	protected  java.util.List <Map<String, Object>> softwareAuditOnClients;
	protected  Map<String, java.util.List <SWAuditClientEntry>> client2software;
	
	protected java.sql.Time SOFTWARE_CONFIG_last_entry = null;
	

	@Override
	public void softwareAuditOnClientsRequestRefresh()
	{
		softwareAuditOnClients = null;
		client2software = null;
	}
	
	
	
	@Override
	public void fillClient2Software(String client)
	{
		logging.info(this, "fillClient2Software " + client);
		if (client2software == null)
		{
			retrieveSoftwareAuditOnClients(client);
			//logging.info(this, "fillClient2Software " + client2software);
			return;
		}
		
		if (client2software.get(client) == null)
			retrieveSoftwareAuditOnClients(client);
		
	}
	
	@Override
	public void fillClient2Software(java.util.List<String> clients)
	{
		retrieveSoftwareAuditOnClients(clients);
	}
	
	@Override
	public  Map<String, java.util.List<SWAuditClientEntry>> getClient2Software()
	//fill the clientlist by fill ...
	{
		logging.info(this, "getClient2Software  ============= ");
		return client2software;
	}
	
	@Override
	public  Map<String, java.util.Set<String>> getSoftwareIdent2clients()
	//fill the clientlist by fill ...
	{
		logging.info(this, "getSoftwareIdent2clients ============= "
			//);
			+  softwareIdent2clients );
		return softwareIdent2clients;
	}
	
		
	protected void retrieveSoftwareAuditOnClients()
	{
		retrieveSoftwareAuditOnClients(new ArrayList<String>());
	}
		
	protected void retrieveSoftwareAuditOnClients(String client)
	{
		java.util.List<String> clients = new ArrayList<String>();
		clients.add(client);
		retrieveSoftwareAuditOnClients(clients);
	}
	*/
	
	protected void retrieveSoftwareAuditOnClients(final java.util.List<String> clients)
	{
		logging.info(this,  "retrieveSoftwareAuditOnClients used memory on start " + de.uib.utilities.Globals.usedMemory());

		
		retrieveInstalledSoftwareInformation();
		logging.info(this, "retrieveSoftwareAuditOnClients client2Software null " 
			+ (client2software == null) 
			+ "  clients count ======  " + clients.size());
		
		java.util.List<String> newClients = new ArrayList<String>(clients);
		
		if (client2software!= null)
		{
			logging.info(this, "retrieveSoftwareAuditOnClients client2Software.keySet size " 
			+ "   +++  " + client2software.keySet().size());
			
			newClients.removeAll(client2software.keySet());
		}
		
		logging.info(this, "retrieveSoftwareAuditOnClients client2Software null " 
			+ (client2software == null) 
			+ "  new clients count  ====== " + newClients.size());
		
		int stepSize = 100;
		
		int missingEntries = 0;
		
		boolean fetchAll = true;
		//if (client2software == null || softwareId2clients == null ||  newClients.size() > 0)
		if (client2software == null || softwareIdent2clients == null ||  newClients.size() > 0)
		{
				String clientSelection = null;
				
				if (newClients.size() >= 50)
				{
					clientSelection = "";
				}
				else
				{
					clientSelection =
					 " AND ( "  
					+ giveWhereOR("clientId", newClients)
					+ ") "; 
					fetchAll = false;
					
				}
				
			
				//logging.info(this, "retrieveSoftwareAuditOnClients for " + clientListForCall.size()  + " clients " + clientListForCall);
				
				//client2software = new HashMap<String, java.util.List<String>>();
				if (client2software == null) client2software = new HashMap<String, java.util.List<SWAuditClientEntry>>();
				//if (softwareId2clients == null) softwareId2clients = new HashMap<Integer, java.util.Set<String>>();
				if (softwareIdent2clients == null) softwareIdent2clients = new HashMap<String, java.util.Set<String>>();
			
				
				controller.notifyDataLoadingObservers(configed.getResourceValue("LoadingObserver.loadtable") + " softwareConfig ");
					//, step " + step);
				
				logging.info(this, "retrieveSoftwareAuditOnClients/ SOFTWARE_CONFIG, start a request");
				
				String columns = SWAuditClientEntry.DB_COLUMN_NAMES.toString(); 
				columns = columns.substring(1);
				columns = columns.substring(0, columns.length()-1);
				
				/*
				String query = "select " + columns + " from " +  "HOST, " + SWAuditClientEntry.DB_TABLE_NAME + " \n" 
					+ " where  state = 1 "
					+ " and HOST.hostID = " + SWAuditClientEntry.DB_TABLE_NAME + ".clientId "
					+ " and HOST.type='OpsiClient' "
				*/
				String query = "select " + columns + " from " +  SWAuditClientEntry.DB_TABLE_NAME + " \n" 
					+ " where  state = 1 " 
					+ clientSelection
					+ " order by clientId " ;
					;
						
				logging.info(this, "retrieveSoftwareAuditOnClients, query " + query);
				
				java.util.List<java.util.List<java.lang.String>> 
				rows
							= controller.exec.getListOfStringLists(
								new OpsiMethodCall(
									"getRawData",
									new Object[]{query}
									)
								);
							
				
				logging.info(this, "retrieveSoftwareAuditOnClients, finished a request"); 
				
				if (rows == null || rows.size() == 0)
				{
					logging.warning(this, "no auditSoftwareOnClient");
				}
				else
				{
				
					logging.info(this, "retrieveSoftwareAuditOnClients rows size " + rows.size());
				
					
					/*
					for (String clientId : clientListForCall)
					{
						client2software.put(clientId, new LinkedList<SWAuditClientEntry>());
					}
					*/
					
					if (fetchAll) client2software.clear();
					
					for (java.util.List<String> row : rows)
					{
						String clientId = row.get(0);
						
						String swIdent = null;
						
						java.util.List<SWAuditClientEntry> entries = client2software.get(clientId);
						if (entries == null)
						{
							entries = new LinkedList<SWAuditClientEntry>();
							client2software.put(clientId, entries);
						}
						//logging.info(this, "adding client entry among " + newClients.size() + " for client " + clientId);
						
						SWAuditClientEntry clientEntry = 
						new SWAuditClientEntry( SWAuditClientEntry.DB_COLUMN_NAMES, row, controller);
						
						//String clientId = clientEntry.getClientId();
						swIdent = clientEntry.getSWident();
						
						/*
						if (swIdent.indexOf("55375-337") > -1 || swIdent.indexOf("55375-440") > -1)  
						{
							logging.info(this, " retrieveSoftwareAuditOnClient clientId : swIdent " + clientId + " : "  + swIdent);
						}
						*/
						
						
						/*
						if (clientEntry.getSWid() == -1)
						{
							missingEntries++;
							logging.info("Missing auditSoftware entry for swIdent " + SWAuditClientEntry.DB_COLUMN_NAMES + " for values: " +
								SWAuditClientEntry.produceSWident(SWAuditClientEntry.DB_COLUMN_NAMES, row)
								);
							
							
							//item.put(SWAuditEntry.WINDOWSsOFTWAREid, "MISSING");
						}
						else
						*/
						{
							java.util.Set<String>clientsWithThisSW = softwareIdent2clients.get(swIdent);
							if (clientsWithThisSW == null)
							{
								clientsWithThisSW = new HashSet<String>();
								
								softwareIdent2clients.put(swIdent, clientsWithThisSW);
							}
							
							/*
							
							if (swIdent.indexOf("55375-337") > -1 || swIdent.indexOf("55375-640") > -1)  
							{
								logging.info(this, "having this subversion " + clientId);
							}
							
							*/
							
							clientsWithThisSW.add(clientId);
							
							entries.add(clientEntry);
						}
						
						/*
						
						if (swIdent.indexOf("55375-337") > -1 || swIdent.indexOf("55375-640") > -1)
						{
							logging.info(this, " retrieveSoftwareAuditOnClient softwareIdent2clients.get(swIdent) " +
								" size " + softwareIdent2clients.get(swIdent).size() + " :: " + 
								softwareIdent2clients.get(swIdent) );
							
							//firefox-locale-de;27.0+build1-0ubuntu0.12.04.
						}
						*/
						
					}
					
					
					newClients.removeAll(client2software.keySet());
					//the remaining clients are without software entry
					
					for (String  clientId : newClients)
					{
						client2software.put(clientId, new LinkedList<SWAuditClientEntry>() );
					}
						
					
				}
				
				
				//logging.info(this, "retrieveSoftwareAuditOnClients client2software " + client2software);
				
				
				logging.info(this, "retrieveSoftwareAuditOnClients memory on end "
					+ Runtime.getRuntime().totalMemory() / 1000000 + " MB");
				//System.gc();
				//step++;
			
			
			
			controller.notifyDataRefreshedObservers("softwareConfig");
		}
		
		logging.info(this, " retrieveSoftwareAuditOnClients reports missingEntries " + missingEntries + " whereas softwareList has entries "  + softwareList.size());
	}
	
	//===================================================
	
	
	//===================================================
	
	/*
	getAuditSoftwareUsage
	
	select   count(*) as Anzahl, name, version, subversion, language, architecture from SOFTWARE_CONFIG  group by  name, version, subversion, language, architecture order by name, version, subversion, language, architecture ;
	*/

	//===================================================

	/* in superclass
	protected  Map<String, Map<String, Object>> hostConfigs;
	protected java.sql.Time CONFIG_STATE_last_entry = null;

	@Override
	public void hostConfigsRequestRefresh()
	{
		logging.info(this, "hostConfigsRequestRefresh");
		hostConfigs= null;
	}
	
	@Override
	public Map<String, Map<String, Object>> getConfigs()
	{
		retrieveHostConfigs();
		return hostConfigs;
	}
	*/
	
	
	@Override
	protected void retrieveHostConfigs()
	{
		//logging.info(this, "retrieveHostConfigs (hostConfigs == null) " + (hostConfigs == null) );
		//logging.info(this, "retrieveHostConfigs classCounter: (hostConfigs == null) " + classCounter + ": " + (hostConfigs == null) );
		
		if (hostConfigs != null)
			return;
		
		logging.info(this, "retrieveHostConfigs classCounter:" + classCounter);
		
		
		controller.notifyDataLoadingObservers(configed.getResourceValue("LoadingObserver.loadtable") + " config state");
			
		
		TimeCheck timeCheck = new TimeCheck(this, " retrieveHostConfigs");
		timeCheck.start();
		logging.info(this, "  retrieveHostConfigs ( CONFIG_STATE )  start a request");
		
		
		
		//String columns = ConfigStateEntry.DB_COLUMN_NAMES.toString(); 
		//columns = columns.substring(1);
		//columns = columns.substring(0, columns.length()-1);
		//json parsing for integer value false thereforw we omit the ID column
		
		
		String columns = 
			ConfigStateEntry.DB_TABLE_NAME + "." + ConfigStateEntry.OBJECT_ID + ", " + 
			ConfigStateEntry.DB_TABLE_NAME + "." + ConfigStateEntry.CONFIG_ID + ", "  + 
			ConfigStateEntry.DB_TABLE_NAME + "." + ConfigStateEntry.VALUES;
		
		String query = "select " + columns + " from " + ConfigStateEntry.DB_TABLE_NAME + " "; 
			//+ " where  state = 1 ";
			
				
		logging.info(this, "retrieveHostConfigs, query " + query);
		
		java.util.List<java.util.List<java.lang.String>> 
		rows
					= controller.exec.getListOfStringLists(
						new OpsiMethodCall(
							"getRawData",
							new Object[]{query}
							)
				
						);
		
		logging.info(this, "retrieveHostConfigs, finished a request");
		
		hostConfigs = new HashMap<String, Map<String, Object>>();
			
		if (rows == null || rows.size() == 0)
		{
			logging.warning(this, "no host config rows "  + rows);
		}
		else
		{
			logging.info(this, "retrieveHostConfigs rows size " + rows.size());
			
			for (java.util.List<String> row : rows)
			{
				String hostId = row.get(0);
					//ConfigStateEntry.DB_COLUMN_NAMES.indexOf(ConfigStateEntry.OBJECT_ID));
				
				
				Map<String, Object> configs1Host = hostConfigs.get(hostId);
				if (configs1Host == null)
				{
					configs1Host = new HashMap<String, Object>();
					hostConfigs.put(hostId, configs1Host);
				}
				
				String configId = row.get(1);
					
					//ConfigStateEntry.DB_COLUMN_NAMES.indexOf(ConfigStateEntry.CONFIG_ID) + 1);
				
				//get values as String
				String valueString = row.get(2);
					// ConfigStateEntry.DB_COLUMN_NAMES.indexOf( ConfigStateEntry.VALUES) );
				
			
				//parse String and produce list
				//ArrayList values = null;
				ArrayList values = new ArrayList();
				try
				{
					values = (new org.json.JSONArray(valueString)).toList();
					
				}
				catch(Exception ex)
				{
					logging.warning(this, "retrieveHostConfigs, error when json parsing database string \n" 
						+ valueString + " for configId " + configId);
				}
				
				//put into host configs
				configs1Host.put(configId, values);
			}
		}
		
		timeCheck.stop();
		logging.info(this, "retrieveHostConfigs retrieved ");
			//hostConfigs.keySet()
		controller.notifyDataRefreshedObservers("configState");
		//System.exit(0);
	}
	
	//===================================================
	/*
	String query = "select * from user";
						
		logging.info(this, "test, query " + query);
		
		boolean result
					= controller.exec.doCall(
						new OpsiMethodCall(
							"getRawData",
							new Object[]{query}
							)
						);
	
	test user table
	Opsi service error:  [ProgrammingError] (1146, "Table 'opsi.user' doesn't exist")
	*/
}
