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
import de.uib.utilities.logging.*;
import de.uib.opsicommand.*;
import de.uib.configed.*;
import de.uib.utilities.logging.*;
import de.uib.configed.type.*;
import de.uib.utilities.table.*;

import de.uib.opsidatamodel.productstate.*;
import de.uib.opsidatamodel.dbtable.*;


public class OpsiserviceSQLgetdataPersistenceController extends OpsiserviceNOMPersistenceController
{
	
	java.sql.Time PRODUCT_ON_CLIENT_last_read = null;
	
	OpsiserviceSQLgetdataPersistenceController (String server, String user, String password)
	{
		super(server, user, password);
	}
	
	@Override
	public java.util.List<Map<java.lang.String,java.lang.Object>> HOST_read()
	{
			
		logging.debug(this, "HOST_read ");
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
		
		for (Map<java.lang.String,java.lang.Object> entry : opsiHosts)
		{
			//logging.info(this, "HOST_read " + entry);
			Host.db2ServiceRowMap(entry);
			//logging.info(this, "HOST_read " + entry);
		}
		
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
		
	
	@Override
	protected  Map<String, java.util.List<Map<String, String>>> getLocalBootProductStatesNOM(String[] clientIds)
	{
		
		java.util.List clients = java.util.Arrays.asList(clientIds);
		
		String columns = Arrays.toString(	(ProductState.DB_COLUMN_NAMES).toArray( new String[]{} )  ) ;
		columns = columns.substring(1);
		columns = columns.substring(0, columns.length()-1);
		
		columns = "clientId, " + columns;
		
		
		String query = "select " + columns + " from PRODUCT_ON_CLIENT " 
			+ " where  productType = 'LocalbootProduct'"
			+ " AND \n" 
			+ " ( "  
			+ giveWhereOR("clientId", clientIds)
			+ ") ";
			
		//System.out.println(query);
		//System.exit(0);
		
		
		
		java.util.List<Map<java.lang.String,java.lang.String>> 
			productOnClients
			= exec.getListOfStringMaps(
				new OpsiMethodCall(
					"getData",
					new Object[]{query}
				)
			)
			;
			
		
		Map<String, java.util.List<Map<String, String>>> result = new HashMap<String, java.util.List<Map<String, String>>>(); 
		for (Map<String, String> m : productOnClients)
		{
			//logging.info(this, " getLocalBootProductStatesNOM " + m);
			
			String client = m.get("clientId");
			
			/*
			if (!clients.contains(client))
				continue;
			*/
			java.util.List<Map<String, String>>states1Client = result.get(client);
			if (states1Client == null)
			{
				states1Client = new ArrayList<Map<String, String>>();
				result.put(client, states1Client);
			}
			
			
			//states1Client.add(JSONReMapper.giveEmptyForNull(m));
			
			
			states1Client.add(
				new ProductState(
					JSONReMapper.giveEmptyForNullString(m),
					true
					)
				);
		}
		return result;
	}
	
	
	
}



