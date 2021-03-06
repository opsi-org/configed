/*
 * configed - configuration editor for client work stations in opsi
 * (open pc server integration) www.opsi.org
 *
 * Copyright (C) 2000 - 2015 uib.de
 *
 *  @author  Rupert Roeder
 */

package de.uib.opsidatamodel;

import java.util.*;
import de.uib.opsicommand.*;
import de.uib.configed.*;
import de.uib.configed.type.*;


/**
 *   HostInfoCollections
 *   description: abstract methods for retrieving and setting host table data
 *    
 *  copyright:     Copyright (c) 2014-2015
 *  organization: uib.de
 * @author  Rupert Roeder 
 */
public abstract class HostInfoCollections
{
	public abstract int getCountClients();
	
	public abstract String getConfigServer();
	
	public abstract void addOpsiHostName(String newName);
	
	public abstract List<String> getOpsiHostNames();
	
	public abstract Map<String, Map<String, Object>> getDepots();//only master depots
	
	public abstract Map<String, Map<String, Object>> getAllDepots();
	
	public abstract LinkedList<String> getDepotNamesList(); //master depots in display order
	
	//protected abstract Map<String, Boolean> getMapOfPCs();
	
	public abstract Map<String, String> getMapPcBelongsToDepot();
	
	public abstract Map<String, HostInfo> getMapOfPCInfoMaps();
	
	public abstract Map<String, HostInfo> getMapOfAllPCInfoMaps();
	
	public abstract Map<String, Boolean> getPcListForDepots(String[] depots);
	
	public abstract void opsiHostsRequestRefresh();
	//includes all refreshes
	
	//public abstract void pclistRequestRefresh();
	
	public abstract void setDepotForClients(String[] clients, String depotId);
	
	public abstract void updateLocalHostInfo(String hostID, String property, Object value);
	
	public abstract void setLocalHostInfo(String hostId, String depotId, HostInfo hostInfo);
	
	//public abstract void intersectWithMapOfPCs(List<String> clientSelection);
	
	protected abstract void retrieveOpsiHosts();
	
}
		
