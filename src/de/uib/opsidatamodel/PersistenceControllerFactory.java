/*
 * configed - configuration editor for client work stations in opsi
 * (open pc server integration) www.opsi.org
 *
 * Copyright (C) 2000-2014 uib.de
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 */

package de.uib.opsidatamodel;

import de.uib.utilities.logging.*;
import de.uib.configed.*;
import de.uib.opsicommand.ConnectionState;


public class PersistenceControllerFactory 
{
	private static PersistenceController staticPersistControl;
	//public static boolean sqlLocally = false;
	//public static boolean sqlLocallyResync = false;
	public static boolean localDB = false;
	public static boolean localDBResync = false;
	
	public static boolean synced = false;
	
	public static boolean sqlAndGetRows = false;
	public static boolean avoidSqlRawData = false;
	public static boolean sqlAndGetHashes = false;
	public static boolean sqlDirect = false;
	
	public static String directmethodcall = "";
	public final static String  directmethodcall_cleanupAuditsoftware = "cleanupAuditsoftware";
		
	/** This creation method constructs a new Controller instance and lets a static variable point to it
	When next time we need a Controller we can choose if we take the already constructed one - returned from the static method 
	getPersistenceController - or construct a new one   
	*/
	public static PersistenceController getNewPersistenceController (String server, String user, String password)
	{
		logging.info("getNewPersistenceController");
		if (staticPersistControl != null && staticPersistControl.getConnectionState().equals(ConnectionState.CONNECTED))
		{
			logging.info("a PersistenceController exists and we are connected, the existing one will be returned");
			return staticPersistControl;
		}
		
		PersistenceController persistControl;
		//PersistenceController persistControl1;
		
		if (sqlAndGetRows)
		{
			//have a try
			//persistControl = new OpsiserviceSQLgetrowsPersistenceController (server, user, password);
			persistControl = new OpsiserviceRawDataPersistenceController (server, user, password);
			logging.info("a PersistenceController initiated by option sqlAndGetRows got " + (persistControl == null));
		}
		else if (avoidSqlRawData)
		{
			sqlAndGetRows = false;
			persistControl = new OpsiserviceNOMPersistenceController (server, user, password);
			logging.info("a PersistenceController initiated by option avoidSqlRawData got " + (persistControl == null));
		}
		
		/*
		else if (localDB)
			persistControl = new OpsiserviceLocalDBPersistenceController (server, user, password);
		
		else if (localDBResync)
		{
			persistControl = new OpsiserviceLocalDBPersistenceController (server, user, password, true);
		}
		else if (synced)
			persistControl = new OpsiserviceSyncedPersistenceController (server, user, password);
		
		else if (sqlAndGetHashes)
			persistControl = new OpsiserviceSQLgetdataPersistenceController (server, user, password);
		*/
		else if (sqlDirect)
		{
			persistControl = new OpsiDirectSQLPersistenceController (server, user, password);
			if (directmethodcall.equals(directmethodcall_cleanupAuditsoftware))
			{
				persistControl.cleanUpAuditSoftware();
			}
			logging.info("a PersistenceController initiated by option sqlDirect got " + (persistControl == null));
			System.exit(0);
		}
				
			
		else
		{
			persistControl = new OpsiserviceRawDataPersistenceController (server, user, password);
			sqlAndGetRows = true;
			logging.info("a PersistenceController initiated by default, try RawData " + (persistControl == null));
		}
		
		
		
		boolean connected = persistControl.makeConnection(); 
		
		try
		{
			if (connected)
			{
				boolean sourceAccepted = persistControl.sourceAccept();
				
				
				if (sqlAndGetRows && !sourceAccepted)
				{
					sqlAndGetRows = false;
					persistControl = null;
					persistControl = new OpsiserviceNOMPersistenceController (server, user, password);
				}
				
				//String savePath = de.uib.opsicommand.OpsiMethodCall.standardRpcPath;
				//de.uib.opsicommand.OpsiMethodCall.standardRpcPath = ""; //for compatibility with older versions;
				
				
				
				
				if (persistControl.getOpsiVersion().compareTo( Globals.REQUIRED_SERVICE_VERSION ) < 0)
				{
					String errorInfo =
					configed.getResourceValue("PersistenceControllerFactory.requiredServiceVersion")
					 + " " +  Globals.REQUIRED_SERVICE_VERSION + ", "
					+ "\n( " + configed.getResourceValue("PersistenceControllerFactory.foundServiceVersion") 
					+ " " + persistControl.getOpsiVersion() + " ) "; 
					
					javax.swing.JOptionPane.showMessageDialog(	Globals.mainContainer, 
											errorInfo,
											Globals.APPNAME,
											javax.swing.JOptionPane.OK_OPTION);
					
					configed.endApp(1);
					
					return null;
					
					//persistControl = new OpsiservicePersistenceController (server, user, password);
				}
				
			
				
				persistControl.makeConnection();
				persistControl.checkConfiguration();
				persistControl.retrieveOpsiModules();
				//retrieves host infos because of client counting
				
				//de.uib.opsicommand.OpsiMethodCall.standardRpcPath = savePath;
				
				//persistControl.retrieveOpsiModules();
				//retrieves host infos because of client counting
				
				if (sqlAndGetRows && !persistControl.isWithMySQL())   
				{
					logging.info(" fall back to  " + OpsiserviceNOMPersistenceController.class);
					sqlAndGetRows = false;
					persistControl = null;
					persistControl = new OpsiserviceNOMPersistenceController (server, user, password);
					
					persistControl.makeConnection();
					persistControl.checkConfiguration();
					persistControl.retrieveOpsiModules();
					
				}
			}
		}
		
		
		catch(Exception ex)
		{
			logging.logTrace(ex);
			logging.error(ex.toString());
			
			String errorInfo = ex.toString();
			
			javax.swing.JOptionPane.showMessageDialog(	Globals.mainContainer, 
									errorInfo,
									Globals.APPNAME,
									javax.swing.JOptionPane.OK_OPTION);
			
			configed.endApp(2);
			
			return null;
		}
			
		
		staticPersistControl = persistControl;
		

		
		return staticPersistControl;
	}  
	
	public static PersistenceController getPersistenceController () 
	{
		return  staticPersistControl;  
	}
	
	public static ConnectionState getConnectionState()
	{
		if (staticPersistControl == null)
		{
			logging.info("PersistenceControllerFactory getConnectionState, " 
				+ " staticPersistControl null");
				
			return ConnectionState.ConnectionUndefined;
		}
		
		ConnectionState result=  staticPersistControl.getConnectionState();
		logging.info("PersistenceControllerFactory getConnectionState " + result); 
		
		return staticPersistControl.getConnectionState();
	}
			
}
	
	


	

