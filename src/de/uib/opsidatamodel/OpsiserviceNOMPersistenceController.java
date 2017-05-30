/**
 *   PersistenceController
 *   implementation for the New Object Model (opsi 4.0)
 *   description: instances of PersistenceController give  
 *   access to proxy objects which mediate access to remote objects (and buffer the data)
 * 
 * 
 * The PersistenceController retrieves its data from a server that is compatible with the  
 *  opsi data server resp. its stub (proxy)
 *  It has a Executioner component that transmits requests to the opsi server and receives the responses.
 *
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
 *  copyright:     Copyright (c) 2000-2016
 *  organization: uib.de
 * @author  R. Roeder 
 */

package de.uib.opsidatamodel;

import java.util.*;
import java.io.File;
import de.uib.utilities.logging.*;
import de.uib.opsicommand.*;
import de.uib.configed.*;
import de.uib.configed.type.*;
import de.uib.configed.type.licences.*;
import de.uib.utilities.datastructure.*;
//import de.uib.utilities.*;
import de.uib.utilities.ExtendedInteger;
import de.uib.utilities.ExtendedDate;
import de.uib.utilities.table.*;
import de.uib.utilities.observer.*;

import de.uib.opsidatamodel.productstate.*;
import de.uib.opsidatamodel.dbtable.*;
import de.uib.opsidatamodel.permission.*;


public class OpsiserviceNOMPersistenceController extends PersistenceController
{
	private final static String EMPTYFIELD = "-";
	private final static ArrayList NONE_LIST = new ArrayList(){
		        public int size(){
			        return -1;
		        }
	        };

	/* data for checking permissions */
	protected boolean globalReadOnly;

	protected boolean serverFullPermission;

	protected boolean depotsFullPermission;
	protected Set<String> depotsPermitted;
	/* ------------------------------------------ */

	public final static String nameRequirementTypeBefore = "before";
	public final static String nameRequirementTypeAfter = "after";
	public final static String nameRequirementTypeNeutral = "";
	public final static String  nameRequirementTypeOnDeinstall = "on_deinstall";

	public final static String[] LICENSE_TYPES = new String[]{"VOLUME","OEM", "RETAIL", "CONCURRENT"};

	//Executioner exec; in superclass
	//Executioner execBackground;
	private static PersistenceController staticPersistControl;

	//private String server;
	protected String connectionServer;
	private String user;
	private String password;

	protected Map<String, List> mapOfMethodSignatures;

	protected List<OpsiProductInfo> productInfos;
	protected Map<String, Map<String, Object>> productGlobalInfos;
	protected List productDependenciesCompleteList;
	protected Map productDependenciesMapForProducts;
	protected Map<String, Map<String,  ConfigName2ConfigValue>> productproperties;
	// (pcname -> (productname -> (propertyname -> propertyvalue))) NOM
	private Map<String, Map<String, ConfigName2ConfigValue>> depot2product2properties;
	protected Set<String> productsHavingSpecificProperties;
	protected Map<String, Boolean> productHavingClientSpecificProperties;

	//protected List pcList;

	protected Map<String, Map<String, ListCellOptions>> productPropertyDefinitions; //for depot

	//protected enum SelectionState{SELECTED, NOT_SELECTED};


	protected HostInfoCollections hostInfoCollections;

	private String theDepot = "";

	private List<Map<String, Object>> depotLocalbootProducts;
	private List<Map<String, Object>> depotNetbootProducts;

	private List<String> localbootProductNames;
	private List<String> netbootProductNames;

	private Map<String, java.util.List<String>> possibleActions; //product-->possibleActions
	private String saveDepotId;
	private HashMap actionsToBeChanged;

	protected String[] logtypes;

	protected List<Map<String, Object>> softwareAuditOnClients;

	//protected Map<String, SWAuditEntry> installedSoftwareInformation;
	//key --> rowmap  for auditSoftware

	//protected TreeMap<String, LicencePoolEntry> licencePools;

	//protected TreeMap<String, Map> rowsWindowsSoftwareId2LPool;

	//protected HashMap<String, String> lPoolFROMWindowsSoftwareId;

	//protected HashMap<String, List> windowsSoftwareIdsFROMLPool;

	protected List< Map<String, Object>> relations_auditHardwareOnHost;

	protected  AuditSoftwareXLicencePool relations_auditSoftwareToLicencePools;

	//protected  List< Map<String, Object>> relations_auditSoftwareToLicencePools;


	protected Map<String,  Map> rowmapAuditSoftware;

	protected Map<String, String> fSoftware2LicencePool; //function softwareIdent --> pool

	protected Map<String, List<String>> fLicencePool2SoftwareList; //function pool --> list of assigned software

	protected Map<String,LicenceUsageEntry > rowsLicencesUsage; //map key -> rowmap

	protected Map<String, java.util.List<LicenceUsageEntry>> fClient2LicencesUsageList; //function host -> list of used licences

	protected Map<String, Map<String, Object>> rowsLicencesReconciliation;

	//protected Map<String, Map<String, Object>> statistics4licensePools;
	protected TreeMap<String, LicenceStatisticsRow> rowsLicenceStatistics;

	protected Map<String, Set<String> > swId2clients;

	protected Map<String, List<Object> > hwAuditConf;

	//protected Map <String, Map> host2SWInformation;

	//protected Map <String, Map> host2OpsiLicenseStatistics;

	class HostGroups extends HashMap<String, Map<String, String>>
	{
		public HostGroups(Map source)
		{
			super(source);
		}

		HostGroups addSpecialGroups()
		{
			logging.debug(this, "addSpecialGroups check");
			Vector<StringValuedRelationElement> groups = new Vector<StringValuedRelationElement>();

			if (get(de.uib.configed.tree.ClientTree.	DIRECTORY_PERSISTENT_NAME) == null)
				//create
			{
				logging.debug(this, "addSpecialGroups");
				StringValuedRelationElement directoryGroup = new StringValuedRelationElement();

				directoryGroup.put("groupId", de.uib.configed.tree.ClientTree.DIRECTORY_PERSISTENT_NAME);
				directoryGroup.put("parentGroupId", null);
				directoryGroup.put("description", "root of directory");

				addGroup(directoryGroup, false);

				groups.add(directoryGroup);

				put(de.uib.configed.tree.ClientTree.	DIRECTORY_PERSISTENT_NAME, directoryGroup);

				logging.debug(this, "addSpecialGroups we have " + this);

			}

			return this;
		}

		void alterToWorkingVersion()
		{
			logging.debug(this, "alterToWorkingVersion we have " + this);

			for (String groupName :  keySet())
			{
				Map<String, String> groupInfo = get(groupName);
				if (de.uib.configed.tree.ClientTree.	DIRECTORY_PERSISTENT_NAME.
				        equals(groupInfo.get("parentGroupId"))
				   )
					groupInfo.put("parentGroupId", de.uib.configed.tree.ClientTree.	DIRECTORY_NAME);

			}

			Map<String, String> directoryGroup = get(de.uib.configed.tree.ClientTree.	DIRECTORY_PERSISTENT_NAME);
			if (directoryGroup != null)
				directoryGroup.put("groupId", de.uib.configed.tree.ClientTree.DIRECTORY_NAME);

			put(de.uib.configed.tree.ClientTree.DIRECTORY_NAME,
			    directoryGroup
			   );

			remove(de.uib.configed.tree.ClientTree.	DIRECTORY_PERSISTENT_NAME);
		}
	};

	protected	HostGroups hostGroups;

	//protected String[] clientsWithFailed;

	protected Map<String, Set<String>> fObject2Groups;

	protected Map<String, Set<String>> fGroup2Members;

	protected Map<String, Set<String>> fProductGroup2Members;

	protected Map<String, String> logfiles;

	private ArrayList updateProductOnClient_items;

	private ArrayList<LicenceUsageEntry> itemsDeletionLicenceUsage;

	protected Map<String, Object> opsiInformation;

	protected Date expiresDate;

	protected Map<String, ModulePermissionValue> opsiModulesPermissions;
	protected Map<String, Object> opsiModulesInfo; //the may as read in 
	protected Map<String, Object> opsiModulesDisplayInfo; //the infos that are displayed in the gui
	protected Map<String, Boolean> opsiModules;  //the resulting info about permission

	protected String opsiVersion;

	public boolean withLicenceManagement = false;
	protected boolean withLocalImaging = false;
	//protected boolean withScalability1 = false;
	protected boolean withMySQL = false;
	protected boolean withUEFI = false;
	protected boolean withWAN = false;
	protected boolean withLinuxAgent = false;
	protected boolean withUserRoles = false;


	protected Map<String, ConfigOption> configOptions; //for internal use, for external cast to:
	protected Map<String, ListCellOptions> configListCellOptions;
	protected Map<String, java.util.List<Object>>configDefaultValues;
	protected Map<String, Map<String, Object>> hostConfigs;

	protected RemoteControls remoteControls;
	protected SavedSearches savedSearches;

	protected LinkedHashMap<String, Boolean> productOnClients_displayFieldsNetbootProducts;
	protected LinkedHashMap<String, Boolean> productOnClients_displayFieldsLocalbootProducts;
	protected LinkedHashMap<String, Boolean> host_displayFields;


	protected List configStateCollection;
	protected List deleteConfigStateItems;
	protected List configCollection;

	protected List productPropertyStateUpdateCollection;
	protected List productPropertyStateDeleteCollection;

	protected Map<String, Map<String, Object>> hostUpdates;

	protected TreeSet<String> productIds;
	protected Map<String, Map<String, String>> productDefaultStates;


	protected java.util.List< /*JSON*/Object>licenceOnClientDeleteItems;

	DataStub dataStub;
	protected boolean sourceAccepted = false;


	protected boolean sourceAccept()
	{
		sourceAccepted = dataStub.test();
		return sourceAccepted;
	}

	@Override
	public HostInfoCollections getHostInfoCollections()
	{
		return hostInfoCollections;
	}

	protected class CheckingEntryMapOfMaps extends LinkedHashMap<String, Map<String, Object>>{}

	/*
{
		@Override
		public Map<String, Object> put(String s, Map<String, Object> m)
		{
			if (s.equals(configServer) )
			{
				logging.info(this, "put " + configServer " + ", map " + m);
			}
			else 
				logging.info(this, "put " + s);
		
			
			return super.put(s, m);
			
		}
		
		 @Override
		public Map<String, Object> get(Object s)
		{
			Map<String, Object> result = super.get(s);
			
			if (s.equals(configServer) )
			{
				logging.info(this, "get bonifax : " + result);
			}
			
			return result;
			
		}
}
	*/		

	protected class DefaultHostInfoCollections extends HostInfoCollections
	{
		protected String configServer;
		protected java.util.List<String> opsiHostNames;

		protected int countClients = 0;
		//protected java.util.List<Map<String, Object>> opsiHosts;
		protected Map<String, Map<String, Object>> masterDepots;
		protected Map<String, Map<String, Object>> allDepots;
		protected Map<String, Map<String, HostInfo>> depot2_host2hostInfo;
		protected java.util.LinkedList<String> depotNamesList;

		protected HashMap<String, Boolean> mapOfPCs;
		//protected HashMap<String,SelectionState> mapPC_selected;
		protected HashMap<String,  HostInfo> mapPC_Infomap; //for some depots
		protected HashMap<String,  HostInfo> host2hostInfo; //all hosts
		protected HashMap<String, String> mapPcBelongsToDepot;


		//PersistenceController persis;

		DefaultHostInfoCollections(PersistenceController persis)
		{
			//this.persis = persis;
		}

		//deliver data
		
		private Map<String, Object> hideOpsiHostKey(Map<String, Object> source)
		{
			Map<String, Object> result = new HashMap<	String, Object> (source);	
			result.put(HostInfo.hostKeyKEY, "****");
			return result;
		}
			

		public String getConfigServer()
		{
			return configServer;
		}

		protected void checkMapPcBelongsToDepot()
		{
			if (mapPcBelongsToDepot == null)
				mapPcBelongsToDepot = new HashMap<String, String>();
		}

		public Map<String, String> getMapPcBelongsToDepot()
		{
			checkMapPcBelongsToDepot();
			return mapPcBelongsToDepot;
		}

		protected Map<String, Boolean> getMapOfPCs()
		{
			return mapOfPCs;
		}

		public java.util.List<String> getOpsiHostNames()
		{
			retrieveOpsiHosts();
			return opsiHostNames;
			/*
			{
				opsiHostNames = exec.getStringListResult(
				new OpsiMethodCall(
					"host_getIdents", 
					new Object[] {}
					)
				);
		}
			*/
		}

		public int getCountClients()
		{
			return countClients;
		}

		public Map<String, Map<String, Object>> getDepots()
		{
			retrieveOpsiHosts();
			logging.debug(this, "getDepots masterDepots " + masterDepots);

			//logging.info(this, "retrieveOpsiHosts masterDepot configserver " + masterDepots.get(configServer));
			return masterDepots;
		}


		public LinkedList<String> getDepotNamesList()
		{
			retrieveOpsiHosts();
			return depotNamesList;
		}


		public Map<String, Map<String, Object>> getAllDepots()
		{
			retrieveOpsiHosts();
			return allDepots;
		}

		public Map<String, HostInfo> getMapOfPCInfoMaps()
		{
			logging.info(this, "getMapOfPCInfoMaps() size " + mapPC_Infomap.size() );
			return mapPC_Infomap;
		}

		public Map<String, HostInfo> getMapOfAllPCInfoMaps()
		{
			logging.info(this, "getMapOfAllPCInfoMaps() size " + host2hostInfo.size() );
			return host2hostInfo;
		}



		//request data refreshes
		public void opsiHostsRequestRefresh()
		{
			opsiHostNames = null;
		}

		private Boolean interpretAsBoolean(Object ob, boolean defaultValue)
		{
			if (ob == null)
				return defaultValue;

			if (ob instanceof Boolean)
				return (Boolean) ob;

			if (ob instanceof Integer)
				return ((Integer) ob) == 1;

			if (ob instanceof String)
				return ( (String) ob).equals("1");

			return null; //not foreseen value
		}


		//build data
		protected void retrieveOpsiHosts()
		{
			//logging.info(this, "retrieveOpsiHosts  " + (opsiHostNames == null));

			if (opsiHostNames == null)
			{
				java.util.List<Map<java.lang.String,java.lang.Object>> opsiHosts = HOST_read();
				HostInfo.resetInstancesCount();

				opsiHostNames = new ArrayList<String>();
				allDepots = new TreeMap<String, Map<String, Object>>();
				//masterDepots = new LinkedHashMap<String, Map<String, Object>>();
				masterDepots = new CheckingEntryMapOfMaps();
				depotNamesList = new LinkedList<String>();

				countClients = opsiHosts.size();

				host2hostInfo = new HashMap<String, HostInfo>();

				//logging.debug(this, "retrieveOpsiHosts countClients " + countClients);

				//find opsi configserver and give it the top position
				for (Map<String, Object> host : opsiHosts)
				{
					String name = (String) host.get( HostInfo.hostnameKEY );
					opsiHostNames.add(name);

					//Map<String, Object> hostMap = new HashMap<String, Object>();

					for (String key : host.keySet())
					{
						if (de.uib.opsicommand.JSONReMapper.isNull(host.get(key)))
							//host.put(key, org.json.JSONObject.NULL);
							host.put(key, de.uib.opsicommand.JSONReMapper.NullRepresenter);

						//hostMap.put(key, "" + host.get(key));
					}

					if (
					    host.get(HostInfo.hostTypeKEY ).equals( HostInfo.hostTypeVALUE_OpsiConfigserver )
					)

					{
						logging.info(this, "retrieveOpsiHosts  type opsiconfigserver host " + hideOpsiHostKey(host));

						configServer = name;

						depotNamesList.add(name);

						allDepots.put(name, host);
						countClients--;

						boolean isMasterDepot = interpretAsBoolean( host.get( HostInfo.isMasterDepotKEY ), true);
						//logging.info(this, "configserver map " + host);
						//System.exit(0);

						if ( isMasterDepot )
						{
							Map<String, Object> hostMap = new HashMap<String, Object>(host);
							masterDepots.put(name, hostMap);
						}
					}
				}

				logging.info(this, "retrieveOpsiHost found masterDepots " + masterDepots.size());

				//find other depotserver and build depot2_host2hostInfo;
				depot2_host2hostInfo = new TreeMap<String, Map<String, HostInfo>>();
				depot2_host2hostInfo.put(configServer, new TreeMap<String, HostInfo>());

				//find depots and build entries for them
				for (Map<String, Object> host : opsiHosts)
				{
					String name = (String) host.get(HostInfo.hostnameKEY);
					//opsiHostNames.add(name);

					if (name == null)
					{
						logging.info(this, "retrieveOpsiHosts, host  " + host);
						//System.exit(0);
					}

					if (
					    host.get( HostInfo.hostTypeKEY ).equals( HostInfo.hostTypeVALUE_OpsiDepotserver )
					)

					{
						//logging.info(this, "retrieveOpsiHosts case OpsiDepotserver name, host  " + name  + " , " + host);

						allDepots.put(name, host);
						countClients--;

						boolean isMasterDepot = interpretAsBoolean( host.get( HostInfo.isMasterDepotKEY ), false);

						if ( isMasterDepot)
						{
							Map<String, Object> hostMap = new HashMap<String, Object>(host);
							masterDepots.put(name, hostMap);

							depot2_host2hostInfo.put(name,  new TreeMap<String, HostInfo>() );
						}
					}
				}

				for (Map<String, Object> host : opsiHosts)
				{
					String name = (String) host.get(HostInfo.hostnameKEY);
					if (
					    ( (String) host.get( HostInfo.hostTypeKEY ) ).equals( HostInfo.hostTypeVALUE_OpsiClient )
					)
						/*
						{
							//test ignore clients
					}
						*/

					{
						//logging.info(this, "retrieveOpsiHosts client  " + name + " config " + getConfigs().get(name));

						boolean depotFound = false;
						String depotId  = null;

						if(
						    getConfigs().get(name) == null
						    ||
						    getConfigs().get(name).get(CONFIG_DEPOT_ID) == null
						    ||
						    ( (java.util.List) (getConfigs().get(name).get(CONFIG_DEPOT_ID) ) ) .size() == 0
						)
						{
							logging.debug(this, "retrieveOpsiHosts client  " + name + " has no config for " + CONFIG_DEPOT_ID);
						}
						else
						{
							depotId = (String)  ( (java.util.List) (getConfigs().get(name).get(CONFIG_DEPOT_ID) ) ).get(0);
						}

						if ( depotId != null &&  masterDepots.keySet().contains(depotId) )
						{
							depotFound = true;
						}
						else
						{
							if (depotId != null)
								logging.warning("Host " + name + " is in " + depotId + " which is not a master depot");
						}


						logging.debug(this, "getConfigs for " + name);
						//logging.info(this, "getConfigs() " + getConfigs());
						//logging.info(this, "getConfigs().get(name).size() " + getConfigs().get(name).size());
						//logging.info(this, "getConfigs().get(name).get(CONFIG_DHCPD_FILENAME)  " +  getConfigs().get(name).get(CONFIG_DHCPD_FILENAME));
						// Get UEFI Boot
						if (
						    getConfigs().get(name) != null
						    &&
						    getConfigs().get(name).get(CONFIG_DHCPD_FILENAME) != null
						    &&
						    ( (java.util.List) (getConfigs().get(name).get(CONFIG_DHCPD_FILENAME) )  ).size() > 0
						)
						{
							String configValue =  (String) ( (java.util.List) (getConfigs().get(name).get(CONFIG_DHCPD_FILENAME) ) ).get(0) ;

							//logging.info(this, "dhcpd filename for " + name + ": " + configValue );

							if ( configValue.indexOf ( ELILO_STRING ) >= 0  )
							{
								// something similar should work, but not this: getHostInfoCollections().getMapOfPCInfoMaps().get(host).setUefiBoot(true);
								host.put(HostInfo.clientUefiBootKEY, true);
							}
						}
						else
						{
							//logging.info(this, "dcpd filename for " + name + ": none" );

							//host.put(HostInfo.clientUefiBootKEY, false);
						}


						// GET WAN STANDARD CONFIG
						if (getConfig(name) != null )
						{

							boolean result = true;

							//logging.info(this, "host " + name);
							if (result) result = valueFromConfigStateAsExpected(getConfig(name), CONFIG_CLIENTD_EVENT_GUISTARTUP, CONFIG_CLIENTD_EVENT_GUISTARTUP_WAN_VALUE);
							if (result) result = valueFromConfigStateAsExpected(getConfig(name), CONFIG_CLIENTD_EVENT_GUISTARTUP_USERLOGGEDIN, CONFIG_CLIENTD_EVENT_GUISTARTUP_USERLOGGEDIN_WAN_VALUE);
							if (result) result = valueFromConfigStateAsExpected(getConfig(name), CONFIG_CLIENTD_EVENT_NET_CONNECTION, CONFIG_CLIENTD_EVENT_NET_CONNECTION_WAN_VALUE);
							if (result) result = valueFromConfigStateAsExpected(getConfig(name), CONFIG_CLIENTD_EVENT_TIMER, CONFIG_CLIENTD_EVENT_TIMER_WAN_VALUE);
							host.put(HostInfo.clientWanConfigKEY, result);

						}

						HostInfo hostInfo = null;

						String myDepot = null;

						if (depotFound)
						{
							host.put( HostInfo.depotOfClientKEY, depotId);
							hostInfo = new HostInfo(host);
							hostInfo.setInDepot(depotId);
							myDepot = depotId;
							//if (hostInfo.isClientInDepot("dummydepot12.uib.local")) //test for depot_restriction:
							//	depot2_host2hostInfo.get(depotId).put(name, hostInfo);
						}
						else
						{
							host.put( HostInfo.depotOfClientKEY, configServer);
							hostInfo = new HostInfo(host);
							hostInfo.setInDepot(configServer);
							myDepot = configServer;
							//if (hostInfo.isClientInDepot("dummydepot12.uib.local")) //test for depot_restriction:
							//	depot2_host2hostInfo.get(configServer).put(name, hostInfo);
						}

						host2hostInfo.put(name, hostInfo);
						depot2_host2hostInfo.get(myDepot).put(name, hostInfo);

						/*
						if (
						(name,  getHostInfoCollections()))
					{
							depot2_host2hostInfo.get(myDepot).remove(name);
					}
						*/



						/*
						for (OpsiPermission permission : hostPermissionsChain)
					{
							if (permission instanceof HostInDepotPermission)
							{
								
								((HostInDepotPermission) permission).setHost( name );
								
								if (permission.permit())
								{
									
									depot2_host2hostInfo.get(myDepot).put(name, hostInfo);
									break;
								}
									
							}
					}
						*/




						//logging.info(this, "retrieveOpsiHosts client  " + name + " opsiClients "  + opsiClients);
						//System.exit(0);

					}

				}

				for (String depot : masterDepots.keySet())
				{
					logging.info(this, "retrieveOpsiHosts clients in " + depot  + ": " + depot2_host2hostInfo .get(depot).size());
				}

				TreeSet<String> depotNamesSorted = new TreeSet<String>(masterDepots.keySet());
				depotNamesSorted.remove(configServer);

				for (String depot : depotNamesSorted)
				{
					depotNamesList.add(depot);
				}

				//test for depot_restriction:
				//if (hostInfo.isClientInDepot("dummydepot12.uib.local"))

				//System.exit(0);
				
				logging.info(this, "retrieveOpsiHosts  HostInfo instances counter " + HostInfo.getInstancesCount());
				logging.info(this, "retrieveOpsiHosts  hostnames " + opsiHostNames.size() );

			}
			
			
		}

		private Boolean valueFromConfigStateAsExpected(Map<String, Object> configs, String configKey,  boolean expectValue)
		{
			boolean result = false;

			if (
			    configs != null
			    &&
			    configs.get(configKey) != null
			    &&
			    ( (java.util.List) (configs.get(configKey) )  ).size() > 0
			)
			{

				// {
				// if (expectValue instanceof Boolean)

				Object value = ((java.util.List) (configs.get(configKey) ) ).get(0) ;


				if (value instanceof Boolean)
				{
					// boolean valueBool = (boolean) value ;
					if ( (Boolean) value == (Boolean) expectValue)
						result = true;
				}


				//if (result)
				//	logging.info(this, "valueFromConfigStateAsExpected configs " + configs );

				// else if (value instanceof String)
				// {
				// 	if ( ((String) value).equals( expectValue ) )
				// 		result = true;
				// }
				// else
				// {
				// 	logging.error(this, "it is no a boolean and not a string, how to handle it ?");
				// }
				// }
			}
			return result;
		}

		public Map<String, Boolean> getPrototypesForDepots(String[] depots)
		{
			return null;
		}

		public Map<String, Boolean> getPcListForDepots(String[] depots)
		{
			retrieveOpsiHosts();

			//if (mapOfPCs == null)
			logging.debug (this, " ------ building pcList");
			mapPcBelongsToDepot = new HashMap<String, String>();
			//mapPC_selected = new HashMap();
			mapOfPCs = new HashMap<String, Boolean>();
			mapPC_Infomap = new HashMap<String, HostInfo>();

			//Map mapForDepot = new HashMap();
			//Map mapPCs_inDepot = new HashMap<String, Boolean>();
			//checkMapPcBelongsToDepot();

			//logging.warning(this, "getClients_listOfHashes server, depots " + server + ",  " + Arrays.toString(depots));

			ArrayList<String> depotList = new ArrayList<String>();
			for (String depot : depots)
			{
				if ( getDepotPermission( depot ))
					depotList.add(depot);
			}

			for ( String depot : depotList )
			{

				//logging.info(this, "getPcListForDepots depot " + depot );

				if (depot2_host2hostInfo.get(depot) == null)
				{
					logging.info(this, "getPcListForDepots depot " + depot + " is null" );
				}
				else
				{
					/*
					logging.info(this, "getPcListForDepots depot " + depot + " has size " +
						depot2_host2hostInfo.get(depot).size()
						);
					*/

					for (String clientName : depot2_host2hostInfo.get(depot).keySet())
					{
						HostInfo hostInfo = depot2_host2hostInfo.get(depot).get(clientName);

						//mapPCs_inDepot.put(clientName, false);
						mapOfPCs.put(clientName, false);

						/* already in depot2_host2hostInfo
						boolean uefiboot =
							 getConfigs().get(clientName) != null
							&&
							getConfigs().get(clientName).get(CONFIG_DHCPD_FILENAME) != null
							&&
							((java.util.List) (getConfigs().get(clientName).get(CONFIG_DHCPD_FILENAME))).size() > 0;
							
						hostInfo.setUefiBoot(uefiboot);
						hostInfo.setInDepot(depot);
						*/

						mapPC_Infomap.put(clientName, hostInfo);
						mapPcBelongsToDepot.put(clientName, depot);
					}
				}
			}

			return mapOfPCs;

		}

		protected void setDepot(String clientName, String depotId)
		{
			//set config
			if( getConfigs().get(clientName) == null )
				getConfigs().put(clientName, new HashMap<String, Object>());
			ArrayList<String> depotList = new ArrayList<String>();
			depotList.add(depotId);
			getConfigs().get(clientName).put( CONFIG_DEPOT_ID , depotList);

			//set in mapPC_Infomap
			HostInfo hostInfo =  mapPC_Infomap.get(clientName);

			logging.info(this, "setDepot, hostinfo for client " + clientName +  " : "
			             + mapPC_Infomap.get(clientName));

			hostInfo.put(HostInfo.depotOfClientKEY, depotId);


			String oldDepot = mapPcBelongsToDepot.get(clientName);
			logging.info(this, "setDepot clientName, oldDepot " + clientName + ", " + oldDepot);
			//set in mapPcBelongsToDepot
			mapPcBelongsToDepot.put(clientName, depotId);

			depot2_host2hostInfo.get(oldDepot).remove(clientName);
			depot2_host2hostInfo.get(depotId).put(clientName, hostInfo);
		}

		@Override
		public void setDepotForClients(String[] clients, String depotId)
		{
			//Map<String, Object> values = new HashMap<String, Object>();
			if (!getDepotPermission(depotId))
				return;

			ArrayList<String> depots = new ArrayList<String>();
			//values.put(CONFIG_DEPOT_ID, depots);
			ConfigName2ConfigValue config = new ConfigName2ConfigValue(null);
			depots.add(depotId);


			config.put(CONFIG_DEPOT_ID, depots);
			for (int i = 0; i < clients.length; i++)
			{
				//collect data
				//logging.info(this, "setDepotForClients, client " + clients[i] +  ", configState " + config);
				setAdditionalConfiguration(clients[i], config);
			}
			//send data
			setAdditionalConfiguration(false);


			//change transitory data
			for (int i = 0; i < clients.length; i++)
			{
				setDepot(clients[i], depotId);
			}


			//we hope to have completely changed the internal data
			//opsiHostNamesRequestRefresh();


		}

		//update derived data (caution!), does not create a HostInfo
		public void addOpsiHostName(String newName)
		{
			opsiHostNames.add(newName);
		}


		public void updateLocalHostInfo(String hostId, String property, Object value)
		{
			if (mapPC_Infomap != null && mapPC_Infomap.get(hostId) != null)
			{
				mapPC_Infomap.get(hostId).put(property, value);
				logging.debug(this, "updateLocalHostInfo " + hostId + " - " + property + " : " + value);

				/*
				logging.info(this, "updateLocalHostInfo  value saved " +
					mapPC_Infomap.get(hostId) +  " // " +
					getMapOfPCInfoMaps().get(hostId));
				*/
			}



		}


		public void setLocalHostInfo(String hostId, String depotId, HostInfo hostInfo)
		{
			logging.debug(this, "setLocalHostInfo "  + " " + hostId + ", " + depotId + ", " + hostInfo);
			mapPC_Infomap.put(hostId, hostInfo);
			depot2_host2hostInfo.get(depotId).put(hostId, hostInfo);
		}



		/*
		public void intersectWithMapOfPCs(List<String> clientSelection)
		{
			if (clientSelection != null)
			{
				for (String client : clientSelection)
				{
					if (mapOfPCs.containsKey(client))//observe depots
						mapOfPCs.put(client,true);
					//mapPC_selected.put(item, SelectionState.SELECTED);
				}
			}
		}
		*/

	}


	//package visibility, the constructor is called by PersistenceControllerFactory
	OpsiserviceNOMPersistenceController (String server, String user, String password)
	{
		logging.info(this, "start construction, \nconnect to " + server + " as " + user
		             //+ " with " + password
		            );
		this.connectionServer = server;
		this.user = user;

		this.password = password;

		new de.uib.configed.type.user.OpsiUser(user);

		logging.debug(this, "create");

		hostInfoCollections = new DefaultHostInfoCollections(this);

		exec = new JSONthroughHTTPS(server,user,password);

		//exec.getConnectionState().waitForConnection(0);


		execs.put(server, exec);

		hwAuditConf = new HashMap<String, List<Object> >();

		initMembers();
	}

	protected void initMembers()
	{
		if (dataStub == null)
			dataStub = new DataStubNOM(this);
	}



	@Override
	final public void checkConfiguration()
	{
		retrieveOpsiModules();
		logging.info(this, "checkConfiguration, modules " + opsiModules);
		initMembers();

		Map<String, java.util.List<Object>> serverPropertyMap = getConfigDefaultValues();

		globalReadOnly = checkReadOnlyBySystemuser();

		serverFullPermission = !globalReadOnly;
		depotsFullPermission = true;

		if (applyUserConfiguration())
		{
			supplyAllPermissionEntries();
			checkPermissions();
		}
		else if (serverPropertyMap.get(KEY_USER_REGISTER) == null)
		{
			ArrayList<Object> readyObjects = new ArrayList<Object>();
			Map<String, Object> item  = createBoolConfig( KEY_USER_REGISTER, KEY_USER_REGISTER_VALUE, "the primary value setting is false");
			readyObjects.add( exec.jsonMap(item) );

			OpsiMethodCall omc = new OpsiMethodCall(
			                         "config_updateObjects",
			                         new Object[] {exec.jsonArray(readyObjects)}
			                     );

			exec.doCall(omc);
			checkPermissions();
		}

		if (serverFullPermission)
			checkStandardConfigs();

		/*
		logging.info(this, "checkConfiguration test exit");
		System.exit(0);
		*/
	}





	/*
	abstract class ServerDelegate
{
		public ServerExec(String server, Executioner exec)
		
		
		public Executioner getExec()
		
		
		abstract 
		public int delegateCounter()
		
		
}
	*/



	@Override
	public Executioner retrieveWorkingExec(String depot)
	{
		//logging.info(this, "retrieveWorkingExec for server " + depot);
		logging.debug(this, "retrieveWorkingExec , compare depotname " + depot + " to config server "
		              + hostInfoCollections.getConfigServer() + " ( named as " + connectionServer + ")");

		if ( depot.equals( hostInfoCollections.getConfigServer() )  )
			//connectionServer))
		{
			logging.debug(this,  "retrieveWorkingExec for config server");
			return exec;
		}

		String password =  (String) getHostInfoCollections().getDepots().get(depot).get(HostInfo.hostKeyKEY);

		Executioner exec1 =  new JSONthroughHTTPS(depot, depot, password);

		if (makeConnection (exec1))
		{
			logging.info(this,  "retrieveWorkingExec new for server " + depot);
			return exec1;
		}

		logging.info(this, "no connection to server " + depot);

		return Executioner.NONE;
	}



	@Override
	protected boolean makeConnection()
	{
		return makeConnection(exec);
	}


	protected boolean makeConnection(Executioner exec1)
	{
		//setConnectionState(new ConnectionState (ConnectionState.STARTED_CONNECTING));
		//set by executioner

		logging.info(this, "trying to make connection");
		boolean result = false;
		try
		{
			//logging.injectLogLevel(5);


			result = exec1.doCall(new OpsiMethodCall ("authenticated", new String[]{}));

			//logging.injectLogLevel(null);

			//System.exit(0);

			if (!result)
			{
				logging.info(this, "connection does not work");
				//System.exit(0);
			}

		}
		catch (java.lang.ClassCastException ex)
		{
			logging.info(this, "JSONthroughHTTPS failed to make connection");
			//logging.info(this, "JSONthroughHTTPS failed to make connection, trying JSONthroughHTTPSOldImpl"); //deprecated
			//exec = new JSONthroughHTTPSOldImpl(connectionServer, user, password);
			//exec.doCall(new OpsiMethodCall ("authenticated", new String[]{}));

		}

		result = result && (getConnectionState().equals(ConnectionState.CONNECTED));

		logging.info(this, "tried to make connection result " + result);

		return result;

	}



	/* error handling convenience methods
	public List getErrorList ()
{
		return logging.getErrorList ();
}


	public void clearErrorList ()
{
		logging.clearErrorList ();
}
	*/

	//we delegate method calls to the executioner
	public  ConnectionState getConnectionState()
	{
		return exec.getConnectionState();
	}

	public  void setConnectionState(ConnectionState state)
	{
		exec.setConnectionState(state);
	}

	public boolean isGlobalReadOnly()
	{
		//logging.info(this, "isreadonly " + globalReadOnly);

		return globalReadOnly;
	}

	protected boolean checkReadOnlyBySystemuser()
	{
		boolean result = false;

		logging.info(this, "checkReadOnly");
		if (exec.getBooleanResult(new OpsiMethodCall("accessControl_userIsReadOnlyUser", new String[]{})) )
		{
			result = true;
			logging.info(this, "checkReadOnly " + globalReadOnly);

		}

		return result;

	}
	
	@Override
	public LinkedHashMap<String, Map<String, Object>> getDepotPropertiesForPermittedDepots()
	{
		
		//persist.getHostInfoCollections().depotsRequestRefresh();
		Map<String, Map<String, Object>> depotProperties = getHostInfoCollections().getAllDepots();
		LinkedHashMap<String, Map<String, Object>> depotPropertiesForPermittedDepots = new LinkedHashMap<String, Map<String, Object>>();
		
		String configServer = getHostInfoCollections().getConfigServer();
		if ( getDepotPermission( configServer ) )
			depotPropertiesForPermittedDepots.put( configServer,  depotProperties.get( configServer ) );
		
		for (String depot :depotProperties.keySet())
		{
			if (!depot.equals(configServer) && getDepotPermission(depot))
				depotPropertiesForPermittedDepots.put(depot,  depotProperties.get(depot));
		}
		
		return depotPropertiesForPermittedDepots;
	}
				

	private String genericUserPart()
	{
		return KEY_USER_ROOT + ".{}.";
	}
	
	private String userPart()
	{
		return KEY_USER_ROOT + ".{" + user + "}.";
	}
	
	private void createUserPropertyClass()
	{
		String newpropertyclass =userPart().substring(0, userPart().length()-1);
		if (! PROPERTYCLASSES_SERVER.containsKey(newpropertyclass))
		{
			logging.info(this, "createUserPropertyClass found new user " + user + " [" + newpropertyclass +"]");
			PROPERTYCLASSES_SERVER.put(newpropertyclass, "");
		}
	};

	protected void supplyAllPermissionEntries()
	{

		Map<String, java.util.List<Object>> serverPropertyMap = getConfigDefaultValues();

		createUserPropertyClass();

		ArrayList<Object> readyObjects = new ArrayList<Object>();
		Map<String, Object> item = null;


		String configKey = userPart() + PARTKEY_USER_PRIVILEGE_GLOBAL_READONLY ;
		logging.info(this, "supplyPermissionEntries  configKey " + configKey);


		if (serverPropertyMap.get( configKey ) == null)
		{

			logging.info(this, "supplyPermissionEntries. serverPropertyMap has no value for key " + configKey);

			item  = createBoolConfig( configKey, globalReadOnly, "the primary value setting is based on the user group");
			readyObjects.add( exec.jsonMap(item) );
			//logging.info(this, "supplyPermissionEntries, readyObjects " + readyObjects);
		}

		configKey =  userPart() + PARTKEY_USER_PRIVILEGE_SERVER_READWRITE;
		logging.info(this, "supplyPermissionEntries  configKey " + configKey);

		if (serverPropertyMap.get( configKey ) == null)
		{
			logging.info(this, "supplyPermissionEntries. serverPropertyMap has no value for key " + configKey);

			item  = createBoolConfig( configKey, true, "the primary value setting is true");
			readyObjects.add( exec.jsonMap(item) );
		}

		configKey =  userPart() +  PARTKEY_USER_PRIVILEGE_DEPOTACCESS_ONLY_AS_SPECIFIED;
		logging.info(this, "supplyPermissionEntries  configKey " + configKey);

		if (serverPropertyMap.get( configKey ) == null)
		{
			logging.info(this, "supplyPermissionEntries. serverPropertyMap has no value for key " + configKey);

			item  = createBoolConfig( configKey, false, "the primary value setting is false");
			readyObjects.add( exec.jsonMap(item) );
		}

		configKey =  userPart() +  PARTKEY_USER_PRIVILEGE_DEPOTS_ACCESSIBLE;
		logging.info(this, "supplyPermissionEntries  configKey " + configKey);

		ArrayList<String> depotsPermitted = new ArrayList<String>();
		if (serverPropertyMap.get( configKey ) == null)
		{
			logging.info(this, "supplyPermissionEntries. serverPropertyMap has no value for key " + configKey);
		}
		else
		{
			logging.info(this, "supplyPermissionEntries found depotsPermitted " + serverPropertyMap.get( configKey ));
			logging.info(this, "supplyPermissionEntries found depotsPermitted " + serverPropertyMap.get( configKey ).getClass().getName());

			for (Object val : serverPropertyMap.get( configKey ))
			{
				depotsPermitted.add( (String) val );
			}
		}

		Set<String> depotOptionsOld = new TreeSet<String>();
		Set<String> depotOptions = new TreeSet<String>();
		ArrayList<String> listDepotOptions = new ArrayList<String>();

		logging.info(this, "supplyPermissionEntries existing depots " + getHostInfoCollections().getDepots().keySet());

		if ( getConfigOptions().get( configKey ) == null )
		{
			logging.info(this, "supplyPermissionEntries. no configOptions for key " + configKey);
		}
		else
		{
			logging.info(this, "supplyPermissionEntries options until now " + getConfigOptions().get( configKey ).getPossibleValues());

			for (Object val : getConfigOptions().get( configKey ).getPossibleValues() )
			{
				String valS = null;
				if (val == null)
					valS = "--";
				else
					valS = val.toString();
				depotOptionsOld.add( valS );
				depotOptions.add( valS );
			}
			//options until now shall be kept even if the depot does not exist at the moment
			
			// because of type object we cannot use addAll
		}
		depotOptions.addAll( getHostInfoCollections().getDepotNamesList() ); // we add all existing depots to the options

		if (!depotOptions.equals( depotOptionsOld ))  //includes the case that there was no value for the configKey at all
		{
			LinkedHashSet<String> orderedDepotOptions = new LinkedHashSet<String>();
			orderedDepotOptions.add( getHostInfoCollections().getConfigServer() );
			for (String depot : depotOptions)
			{
				orderedDepotOptions.add( depot );
			}

			listDepotOptions = new ArrayList<String> (orderedDepotOptions);

			item =  createNOMitem( ConfigOption.UNICODE_TYPE );

			item.put("ident", configKey);
			item.put("editable", true);
			item.put("multiValue", true);

			item.put("description", "the primary value setting is a empty selection list, but all existing depots as option");
			item.put("defaultValues",  exec.jsonArray(depotsPermitted) ) ;
			item.put("possibleValues",  exec.jsonArray(listDepotOptions) );

			readyObjects.add( exec.jsonMap(item) );
		}



		if ( readyObjects.size() > 0 )
		{

			OpsiMethodCall omc = new OpsiMethodCall(
			                         "config_updateObjects",
			                         new Object[] {exec.jsonArray(readyObjects)}
			                     );

			exec.doCall(omc);
		}

	}


	@Override
	public void checkPermissions()
	{

		OpsiPermission.ActionPrivilege serverActionPermission;

		Map<String, java.util.List<Object>> serverPropertyMap = getConfigDefaultValues();

		String configKey = null; // variable for simplifying the use of the map


		if (!globalReadOnly) // already specified via systemuser group
		{
			// lookup if we have a config for it and set it though not set by group
			configKey = userPart() + PARTKEY_USER_PRIVILEGE_GLOBAL_READONLY;
			logging.info(this, "checkPermissions  configKey " + configKey);
			globalReadOnly = (serverPropertyMap.get( configKey ) != null) && (Boolean) (serverPropertyMap.get( configKey ).get(0));
		}

		logging.info(this, " checkPermissions globalReadOnly " + globalReadOnly);

		if (globalReadOnly)
		{
			serverActionPermission = OpsiPermission.ActionPrivilege.READ_ONLY;
		}
		else
		{
			Boolean mayWriteOnOpsiserver = true; // is default!!

			configKey =  userPart() + PARTKEY_USER_PRIVILEGE_SERVER_READWRITE;
			logging.info(this, "checkPermissions  configKey " + configKey);
			//logging.info(this, " checkPermissions ServerPropertyMap.get ... with configKey " + configKey);
			//logging.info(this, " checkPermissions ServerPropertyMap.get(... ");
			//logging.info(this, " checkPermissions  value list  " +  serverPropertyMap.get( configKey ));

			if ( serverPropertyMap.get( configKey )  != null
			   )
			{
				//logging.info(this, " checkPermissions  list  class " +  serverPropertyMap.get( configKey ).getClass().getName() );
				//logging.info(this, " checkPermissions  value  class " +  (serverPropertyMap.get( configKey ).get(0)).getClass().getName() );
				//logging.info(this, " checkPermissions  value  " +  (serverPropertyMap.get( configKey ).get(0)) );
				mayWriteOnOpsiserver =(Boolean) ( ( serverPropertyMap.get(configKey) ) .get(0) );
			}

			logging.info(this, " checkPermissions mayWriteOnOpsiserver " + mayWriteOnOpsiserver);
			if (mayWriteOnOpsiserver)
			{
				serverActionPermission = OpsiPermission.ActionPrivilege.READ_WRITE;
			}
			else
			{
				serverActionPermission = OpsiPermission.ActionPrivilege.READ_ONLY;
			}
		}

		//serverActionPermission = OpsiPermission.ActionPrivilege.READ_ONLY;
		serverFullPermission =
		    (serverActionPermission == OpsiPermission.ActionPrivilege.READ_WRITE);



		configKey =  userPart() +  PARTKEY_USER_PRIVILEGE_DEPOTACCESS_ONLY_AS_SPECIFIED;
		logging.info(this, "checkPermissions  configKey " + configKey);
		depotsFullPermission = true; //default !!
		if (serverPropertyMap.get( configKey ) != null)
		{
			depotsFullPermission = !(Boolean) (serverPropertyMap.get( configKey ).get(0));
		}

		configKey =  userPart() +  PARTKEY_USER_PRIVILEGE_DEPOTS_ACCESSIBLE;
		logging.info(this, "checkPermissions  configKey " + configKey);
		depotsPermitted = new HashSet<String>();

		if (!depotsFullPermission)
		{
			if ( serverPropertyMap.get( configKey ) == null )
				// we didn't configure anything, therefore we revoke the setting
			{
				depotsFullPermission = true;
			}
			else
			{
				for ( Object val : serverPropertyMap.get( configKey ) )
				{
					depotsPermitted.add( (String) val );
				}
			}
		}

		logging.info(this, "checkPermissions  depotsFullPermission " + depotsFullPermission);
		logging.info(this, "checkPermissions  depotsPermitted " + depotsPermitted);

		de.uib.configed.type.user.UserRole.determineRole(
		    globalReadOnly, !depotsFullPermission, serverFullPermission );

		logging.info(this, "checkPermissions role " + de.uib.configed.type.user.UserRole.getRole());
	}

	@Override
	public  boolean isServerFullPermission()
	{
		return serverFullPermission;
	}

	@Override
	public  boolean isDepotsFullPermission()
	{
		return depotsFullPermission;
	}

	@Override
	public boolean getDepotPermission(String depotId)
	{
		if (depotsFullPermission)
			return true;

		boolean result = false;

		if (depotsPermitted != null)
			result = depotsPermitted.contains(depotId);

		//logging.info(this, "getDepotPermission false for " + depotId);

		return result;
	}


	/*
	private boolean checkHostPermission(String hostname,  HostInfoCollections hostInfos)
	// true if either no host permission exist or 
	// at least one permission is given
{
		boolean hostAccepted = true;
		
		
		if (hostPermissionsChain.size() > 0)
		{
			hostAccepted = false;
			//at least one permission must be given
			for (HostRelatedPermission p : hostPermissionsChain)
			{
				if (p instanceof HostInDepotPermission)
				{
					((HostInDepotPermission) p).setHostInfoCollections( hostInfos );
					p.setHost( hostname );
					hostAccepted = p.permit();
					if (hostAccepted)
						break;
				}
			}
		}
		
		
		logging.info(this, "checkHostPermission hostname " + hostname + ": " + hostAccepted);  
			
		
		return hostAccepted;
}
	*/


	/*
	public  List getListResult (OpsiMethodCall omc)
{
		return exec.getListResult ( omc);
}
	public  Map getMapResult (OpsiMethodCall omc)
{
		return exec. getMapResult ( omc);
}
	public  String getStringResult (OpsiMethodCall omc)
{
		return exec.getStringResult ( omc);
}
	public  Map getMapFromItem (Object s)
{
		return exec.getMapFromItem (s);
}
	public  String getStringValueFromItem (Object s)
{
		return exec.getStringValueFromItem (s);
}

	*/





	public boolean installPackage(String filename)
	{
		return installPackage(filename, true, new Object[]{}, "");
	}

	public boolean installPackage(String filename, boolean force, Object propertyDefaultValues, String tempDir)
	{

		String method = "depot_installPackage";

		logging.check(this,  method);
		boolean result = exec.doCall(
		                     new OpsiMethodCall(
		                         method,
		                         new Object[]{
		                             filename,
		                             force
		                         }
		                     )
		                 )
		                 ;
		logging.info(this, "installPackage result " + result);

		return result;
	}


	public boolean setRights(String path)
	{
		String method = "setRights";
		logging.info(this,  "setRights for path " + path);

		String[] args = new String[]{path};

		//System.exit(0);

		if (path == null)
			args = new String[]{};

		boolean result = exec.getBooleanResult(
		                     new OpsiMethodCall(
		                         //"extend/configed",
		                         method,
		                         args)
		                 );

		return result;

	}

	@Override
	public java.util.List<Map<java.lang.String,java.lang.Object>> HOST_read()
	{
		String[] callAttributes = new String[] {};
		HashMap callFilter = new HashMap ();

		TimeCheck timer= new TimeCheck(this, "HOST_read").start();
		logging.check(this, "host_getObjects");
		java.util.List<Map<java.lang.String,java.lang.Object>> opsiHosts = exec.getListOfMaps(
		            new OpsiMethodCall(
		                "host_getObjects",
		                new Object[]{
		                    callAttributes,
		                    callFilter
		                }
		            )
		        )
		        ;
		timer.stop();
		for (Map<java.lang.String,java.lang.Object> entry : opsiHosts)
		{

			//Host.serviceKeyMapping
		}
		//System.exit(0);

		return opsiHosts;
	}


	public List<String> getClientsWithOtherProductVersion(String productId, String productVersion, String packageVersion)
	{

		String[] callAttributes = new String[]{};

		HashMap<String, String> callFilter = new HashMap<String, String>();
		callFilter.put(ProductOnClient.PRODUCTid, productId);
		callFilter.put(ProductOnClient.PRODUCTtype, ProductOnClient.LOCALBOOTid);


		//controller.notifyDataLoadingObservers(configed.getResourceValue("LoadingObserver.loadtable") + " product"));

		List<Map<String, Object>>  retrievedList  = retrieveListOfMapsNOM(
		            callAttributes,
		            callFilter,
		            "productOnClient_getObjects"
		        );

		List<String> result = new ArrayList<String>();


		for (Map<String, Object> m : retrievedList)
		{
			String client = (String) m.get( ProductOnClient.CLIENTid );
			//if (!client.equals("pcbirgit.uib.local"))
			//	continue;
			
			String clientProductVersion =  (String) m.get( OpsiPackage.SERVICEkeyPRODUCT_VERSION );
			String clientPackageVersion = (String) m.get( OpsiPackage.SERVICEkeyPACKAGE_VERSION );

			Object clientProductState = m.get( ProductState.KEY_installationStatus );
			//Object clientActionRequest = m.get (ProductState.KEY_actionRequest );


			//logging.info(this, "clientActionRequest " + clientActionRequest);


			if (
				//has wrong product version
			    InstallationStatus.getLabel(InstallationStatus.INSTALLED).equals( clientProductState )
			    &&
			    (
			        ( !JSONReMapper.equalsNull( clientProductVersion ) && !productVersion.equals( clientProductVersion  ) )
			        ||
			        ( !JSONReMapper.equalsNull( clientPackageVersion ) && !packageVersion.equals(  clientPackageVersion ) )
			    )
			    	//and is not 
			    	//already configured for renewing
			   // && 
			   // !(clientActionRequest != null  && ActionRequest.getLabel( ActionRequest.SETUP).equals( clientActionRequest ))
			)
			{
				logging.debug("getClientsWithOtherProductVersion hit " + m);
				result.add(client);
			}
		}


		logging.info(this, "getClientsWithOtherProductVersion globally " + result.size());

		//hostInfoCollections.intersectWithMapOfPCs(result);
		//should be done otherwere by preselection of depots

		//logging.info(this, "getClientsWithOtherProductVersion " + result.size());

		return result;
	}



	/*
	public synchronized List<String> getClientsWithOtherProductVersion(String productId, String productVersion, String packageVersion)
{
		List<String> clientSelection =

		    exec.getStringListResult(
		        new OpsiMethodCall(
		            //"extend/configed",
		            "getClientsWithOtherProductVersion_list",
		            new String[] {productId, productVersion, packageVersion}
		        )
		    );

		hostInfoCollections.intersectWithMapOfPCs(clientSelection);

		return clientSelection;
}
	*/

	/*
	public void clientsWithFailedRequestRefresh()
{
		clientsWithFailed = null;
}


	public String[] getClientsWithFailed()
{
		if (clientsWithFailed != null)
			return clientsWithFailed;
		
		Map<String, java.util.List> mapClientsWithFailedProducts=
		exec.getMapOfLists(
			new OpsiMethodCall(
				//"extend/configed",
				"getClientsWithFailed_hash",
				new String[]{}
			)
		);
		
		List<String> clientList = new ArrayList(new TreeSet( mapClientsWithFailedProducts.keySet()));
		intersectWithMapOfPCs(clientList);
		
		clientsWithFailed = clientList.toArray(new String[]{});
		
		return clientsWithFailed;
}
	*/

	/*
	public Map<String, String> getProductVersion(String productId, String depotId)
{
		String callInfo = "getProductVersion for product, depot " + productId + ", " + depotId;

		Map foundMap =

		    exec.getMapResult(
		        new OpsiMethodCall(
		            //"extend/configed",
		            "getProductVersion_hash",
		            new String[]{productId, depotId}
		        )
		    );

		Map<String, String> result = new HashMap();

		String error = "";

		if (foundMap.get("productVersion") != null && foundMap.get("productVersion") instanceof String)
			result.put("productVersion", (String) foundMap.get("productVersion"));
		else
			error = callInfo + "error: productVersion not found";

		if ((error.equals("")) &&  (foundMap.get("packageVersion") != null) && (foundMap.get("packageVersion") instanceof String))
			result.put("packageVersion", (String) foundMap.get("packageVersion"));
		else
			error = callInfo + "error: packageVersion not found";


		if (!error.equals(""))
			logging.info(this, error);

		logging.debug(this, callInfo + " result: " + result);

		return result;
}
	*/

	public boolean areDepotsSynchronous(Set depots)
	{
		OpsiMethodCall omc = new OpsiMethodCall(
		                         "areDepotsSynchronous",
		                         new Object[]{depots.toArray()} );

		boolean result = exec.getBooleanResult ( omc );
		//System.out.println ("------------ depots synchronous " + result);
		return result;
	}


	private List<Object> addWANConfigStates(String clientId, boolean wan,  List<Object> jsonObjects)
	{
		Map<String, java.util.List<Object>> wanConfig = new HashMap<String, java.util.List<Object>>();
		Map<String, java.util.List<Object>> notWanConfig = new HashMap<String, java.util.List<Object>>();

		java.util.List<Object> values;


		values = new ArrayList<Object>();
		values.add (!CONFIG_CLIENTD_EVENT_GUISTARTUP_WAN_VALUE);
		notWanConfig.put( CONFIG_CLIENTD_EVENT_GUISTARTUP, values );

		values = new ArrayList<Object>();
		values.add (CONFIG_CLIENTD_EVENT_GUISTARTUP_WAN_VALUE);
		wanConfig.put(CONFIG_CLIENTD_EVENT_GUISTARTUP, values );


		values = new ArrayList<Object>();
		values.add (!CONFIG_CLIENTD_EVENT_GUISTARTUP_USERLOGGEDIN_WAN_VALUE);
		notWanConfig.put( CONFIG_CLIENTD_EVENT_GUISTARTUP_USERLOGGEDIN, values );

		values = new ArrayList<Object>();
		values.add (CONFIG_CLIENTD_EVENT_GUISTARTUP_USERLOGGEDIN_WAN_VALUE);
		wanConfig.put( CONFIG_CLIENTD_EVENT_GUISTARTUP_USERLOGGEDIN, values );


		values = new ArrayList<Object>();
		values.add (!CONFIG_CLIENTD_EVENT_NET_CONNECTION_WAN_VALUE);
		notWanConfig.put(CONFIG_CLIENTD_EVENT_NET_CONNECTION, values );

		values = new ArrayList<Object>();
		values.add (CONFIG_CLIENTD_EVENT_NET_CONNECTION_WAN_VALUE);
		wanConfig.put(CONFIG_CLIENTD_EVENT_NET_CONNECTION, values );


		values = new ArrayList<Object>();
		values.add (!CONFIG_CLIENTD_EVENT_TIMER_WAN_VALUE);
		notWanConfig.put(CONFIG_CLIENTD_EVENT_TIMER, values );

		values = new ArrayList<Object>();
		values.add (CONFIG_CLIENTD_EVENT_TIMER_WAN_VALUE);
		wanConfig.put(CONFIG_CLIENTD_EVENT_TIMER, values );

		//logging.info(this, "addWANConfigStates wanConfig " + wanConfig);
		//logging.info(this, "addWANConfigStates notWanConfig " + notWanConfig);

		setConfig(notWanConfig); //create new configs
		setConfig(); //send to opsiserver



		Map<String, java.util.List<Object>> specifiedConfig;

		if (wan)
			specifiedConfig = wanConfig;
		else
			specifiedConfig = notWanConfig;

		if (jsonObjects == null)
			jsonObjects = new ArrayList<Object>();

		//logging.info(this, "addWANConfigState  specifiedConfig " + specifiedConfig);
		//logging.info(this, "addWANConfigState  specifiedConfig.keySet() " + specifiedConfig.keySet());

		for (String configId : specifiedConfig.keySet())
		{
			//logging.info(this, "addWANConfigState configId " + configId);
			Map<String, Object> item = createNOMitem(ConfigStateEntry.TYPE);

			item.put(ConfigStateEntry.CONFIG_ID, configId);

			//logging.info(this, "addWANConfigState values " + specifiedConfig.get(configId));

			item.put(ConfigStateEntry.VALUES, exec.jsonArray(specifiedConfig.get(configId)));

			//logging.info(this, "addWANConfigState values " + item.get(ConfigStateEntry.VALUES));
			item.put(ConfigStateEntry.OBJECT_ID, clientId);

			jsonObjects.add( exec.jsonMap( item) );
		}

		return jsonObjects;
	}

	public boolean setWANConfigs(String clientId , boolean wan)
	{
		boolean result = false;

		java.util.List<Object> jsonObjects = addWANConfigStates(clientId, wan, null);


		//logging.info(this, "setWANConfigs jsonObjects " + jsonObjects);


		OpsiMethodCall omc = new OpsiMethodCall(
		                         "configState_updateObjects",
		                         new Object[] {exec.jsonArray(jsonObjects)}
		                     );

		result = exec.doCall(omc);
		return result;
	}



	private Object createUefiEntry(String clientId)
	{
		Map<String, Object> item = createNOMitem("ConfigState");
		java.util.List values = new ArrayList();
		values.add(EFI_DHCPD_FILENAME);
		item.put("objectId", clientId);
		item.put("values", exec.jsonArray(values));
		item.put("configId", CONFIG_DHCPD_FILENAME);

		return exec.jsonMap(item);
	}


	public boolean configureUefiBoot(String clientId, boolean uefiBoot)
	{
		boolean result = true;


		if (uefiBoot)
		{
			List<Object> jsonObjects = new ArrayList<Object>();
			jsonObjects.add( createUefiEntry( clientId) );

			OpsiMethodCall omc = new OpsiMethodCall(
			                         "configState_updateObjects",
			                         new Object[] {exec.jsonArray(jsonObjects)}
			                     );
			result = exec.doCall(omc);
		}
		else
		{

			Map<String, Object> itemFilename = createNOMitem("ConfigState");
			itemFilename.put("configId", CONFIG_DHCPD_FILENAME);
			itemFilename.put("objectId", clientId);

			List<Object> jsonObjects = new ArrayList<Object>();
			jsonObjects.add( exec.jsonMap(itemFilename) );

			OpsiMethodCall omc = new OpsiMethodCall(
			                         "configState_deleteObjects",
			                         new Object[] { exec.jsonArray(jsonObjects) }
			                     );
			result = exec.doCall(omc);
		}



		/* in HostInfo
		if (result) 
		//update intermediate collections
		{
			hostInfoCollections.updateLocalHostInfo(clientId, HostInfo.clientUefiBootKEY, uefiBoot);
	}
		*/


		return result;
	}

	public boolean createClient (String hostname, String domainname,
	                             String depotId,
	                             String description, String inventorynumber,
	                             String notes, String ipaddress, String macaddress,
	                             // boolean shutdownInstall,
	                             boolean uefiBoot, boolean wanConfig,
	                             String group, String productNetboot, String productLocalboot)
	{
		if ( !getDepotPermission (depotId ) )
		{
			return false;
		}

		boolean result = false;

		if (inventorynumber == null) {
			inventorynumber = "";
		}

		if (description == null) {
			description = "";
		}

		if (notes == null) {
			notes = "";
		}

		if (ipaddress.equals("")){
			ipaddress = null;
			//null works, "" does not in the opsi call
		}

		if (group == null)
			group = "";

		String newClientId  = hostname + "." + domainname;

		Map<String, Object> hostItem = createNOMitem("OpsiClient");
		hostItem.put("id", newClientId);
		hostItem.put("description", description);
		hostItem.put("notes", notes);
		hostItem.put("hardwareAddress", macaddress);
		hostItem.put("ipAddress", ipaddress);
		hostItem.put("inventoryNumber", inventorynumber);

		OpsiMethodCall omc = new OpsiMethodCall(
		                         "host_createObjects",
		                         new Object[]{
		                             exec.jsonMap(hostItem)
		                         }
		                     );
		result =  exec.doCall(omc);

		HostInfo hostInfo = new HostInfo(hostItem);

		if (result)
		{
			List<Object> jsonObjects = new ArrayList<Object>();

			Map<String, Object> itemDepot = createNOMitem("ConfigState");
			java.util.List valuesDepot = new ArrayList();
			valuesDepot.add(depotId);
			itemDepot.put("objectId", newClientId);
			itemDepot.put("values", exec.jsonArray(valuesDepot));
			itemDepot.put("configId", CONFIG_DEPOT_ID);

			jsonObjects.add(exec.jsonMap(itemDepot));

			if (uefiBoot)
			{
				jsonObjects.add( createUefiEntry(newClientId) );
			}

			if (wanConfig)
			{
				jsonObjects = addWANConfigStates(newClientId, true, jsonObjects);
			}

			// if (shutdownInstall)
			// {

			// 	String product = "opsi-client-agent";
			// 	setCommonProductPropertyValue( new HashSet(Arrays.asList(newClientID)), product, "on_shutdown_install" , Arrays.asList("on") );
			// 	Map<String, String> productValues = new HashMap<String,String>();
			// 	productValues.put("actionRequest", "setup");

			// 	updateProductOnClient(
			// 			   			newClientID,
			// 			    		product,
			// 			    		OpsiPackage.TYPE_LOCALBOOT,
			// 			    		productValues
			// 			);

			// 	// persist.updateProductOnClients();

			// 	// persist.getHostInfoCollections().updateLocalHostInfo(newClientID, HostInfo.clientShutdownInstallKEY, shutdownInstall);
			// 	// persist.getHostInfoCollections().setLocalHostInfo(newClientID, depotID, hostInfo);
			// }

			omc = new OpsiMethodCall(
			          "configState_updateObjects",
			          new Object[] {exec.jsonArray(jsonObjects)}
			      );

			result = exec.doCall(omc);

			// if (wan) && isWithWAN()
			// {
			// 	setWANConfig(newClientId ,wan);
			// }
		}


		if ( (result) && ( (group!=null) && (!group.isEmpty())) )
		{
			logging.info(this,"createClient" + " group " + group);
			List<Object> jsonObjects = new ArrayList<Object>();
			Map<String, Object> itemGroup = createNOMitem(Object2GroupEntry.TYPE_NAME);
			itemGroup.put("groupType", Object2GroupEntry.GROUP_TYPE_HOSTGROUP);
			itemGroup.put( Object2GroupEntry.GROUP_ID_KEY, group);
			itemGroup.put(Object2GroupEntry.MEMBER_KEY, newClientId);
			jsonObjects.add(exec.jsonMap(itemGroup));
			omc = new OpsiMethodCall(
			          "objectToGroup_createObjects",
			          new Object[] {exec.jsonArray(jsonObjects)}
			      );
			result = exec.doCall(omc);
		}

		if ( (result) && ( (productNetboot!=null) && (!productNetboot.isEmpty())) )
		{
			logging.info(this,"createClient" + " productNetboot " + productNetboot);
			List<Object> jsonObjects = new ArrayList<Object>();
			Map<String, Object> itemProducts = createNOMitem("ProductOnClient");
			itemProducts.put(OpsiPackage.DBkeyPRODUCT_ID, productNetboot);
			itemProducts.put(OpsiPackage.SERVICEkeyPRODUCT_TYPE, OpsiPackage.NETBOOT_PRODUCT_SERVER_STRING);
			itemProducts.put("clientId", newClientId);
			itemProducts.put(ProductState.key2servicekey.get(ProductState.KEY_actionRequest), "setup");
			jsonObjects.add(exec.jsonMap(itemProducts));
			omc = new OpsiMethodCall(
			          "productOnClient_createObjects",
			          new Object[] {exec.jsonArray(jsonObjects)}
			      );
			result = exec.doCall(omc);
		}

		if ( (result) && ( (productLocalboot!=null) && (!productLocalboot.isEmpty())) )
		{
			logging.info(this,"createClient" + " productLocalboot " + productLocalboot);
			List<Object> jsonObjects = new ArrayList<Object>();
			Map<String, Object> itemProducts = createNOMitem("ProductOnClient");
			itemProducts.put(OpsiPackage.DBkeyPRODUCT_ID, productLocalboot);
			itemProducts.put(OpsiPackage.SERVICEkeyPRODUCT_TYPE, OpsiPackage.LOCALBOOT_PRODUCT_SERVER_STRING);
			itemProducts.put("clientId", newClientId);
			itemProducts.put(ProductState.key2servicekey.get(ProductState.KEY_actionRequest), "setup");
			jsonObjects.add(exec.jsonMap(itemProducts));
			omc = new OpsiMethodCall(
			          "productOnClient_createObjects",
			          new Object[] {exec.jsonArray(jsonObjects)}
			      );
			result = exec.doCall(omc);

		}


		if (result)
			//update intermediate collections
		{
			if (depotId == null || depotId.equals(""))
				depotId = getHostInfoCollections().getConfigServer();
			hostInfo.setInDepot(depotId);
			hostInfo.setUefiBoot(uefiBoot);
			hostInfo.setWanConfig(wanConfig);
			// hostInfo.setShutdownInstall(shutdownInstall);
			// hostInfo.setParentGroup - update Groups
			// update groupTable
			// update Product Tables
			hostInfoCollections.setLocalHostInfo(newClientId, depotId, hostInfo);

			logging.info(this, " createClient hostInfo " + hostInfo);
		}

		return result;

	}

	public boolean renameClient(String hostname, String newHostname)
	{
		if ( globalReadOnly )
			return false;

		OpsiMethodCall omc = new OpsiMethodCall(
		                         "host_renameOpsiClient",
		                         new String[]{ hostname, newHostname }
		                     );


		hostInfoCollections.opsiHostsRequestRefresh();

		return exec.doCall( omc );
	}


	public void deleteClient (String hostId)
	{
		if ( globalReadOnly )
			return;

		OpsiMethodCall omc = new OpsiMethodCall(
		                         "host_delete",
		                         new String[]{ hostId } );
		exec.doCall ( omc );
		hostInfoCollections.opsiHostsRequestRefresh();
	}


	/*
	public void wakeOnLan (String hostId)
	//opsi 3 comenuItemsOpsiclientdEventmpatibility
{
}
	*/

	//hostControl methods
	private java.util.List<String> collectErrorsFromResponsesByHost(Map responses, String callingMethodName)
	{
		ArrayList<String> errors = new ArrayList<String>();

		for (Object host : responses.keySet())
		{
			org.json.JSONObject jO = (org.json.JSONObject) responses.get(host);
			String error = //de.uib.opsicommand.JSONExecutioner.getErrorFromResponse(jO);
			    de.uib.opsicommand.JSONReMapper.getErrorFromResponse(jO);

			if (error != null)
			{
				error = host + ":\t" + error;
				logging.info(callingMethodName + ",  " + error );
				errors.add(error);
			}

			/*
			else
		{
				error = host + ":t" + "ok";
				errors.add(error);
		}
			*/
		}

		return errors;
	}

	public java.util.List<String>  deletePackageCaches (String[] hostIds)
	{


		OpsiMethodCall omc = new OpsiMethodCall(
		                         "hostControlSafe_opsiclientdRpc",
		                         new Object[]{
		                             "cacheService_deleteCache",
		                             new Object[]{},
		                             hostIds
		                         }
		                     );

		Map responses = exec.getMapResult(omc);

		return collectErrorsFromResponsesByHost(responses, "deleteCache");
	}

	@Override
	public Map<String, java.util.List<String>> getHostSeparationByDepots( String[] hostIds )
	{
		Map<String, java.util.Set<String>> hostSeparationByDepots = new HashMap<String, java.util.Set<String>>();


		for (String hostId : hostIds)
		{
			String depotId = getHostInfoCollections().getMapPcBelongsToDepot().get(hostId);
			//logging.info(this, "host " + hostId + " in depot " + depotId);

			if (hostSeparationByDepots.get(depotId) == null)
				hostSeparationByDepots.put(depotId, new HashSet<String>());

			hostSeparationByDepots.get(depotId).add(hostId);
		}


		Map<String, java.util.List<String>> result = new HashMap<String,  java.util.List<String>>();
		for (String depot : hostSeparationByDepots.keySet())
		{
			result.put(depot, new ArrayList<String>(hostSeparationByDepots.get(depot)));
		}

		return result;
	}

	@Override
	public java.util.List<String> wakeOnLan (String[] hostIds)
	{
		return wakeOnLan( hostIds,   getHostSeparationByDepots( hostIds ));
	}

	protected java.util.List<String>  wakeOnLan (String[] hostIds, Map<String, java.util.List<String>> hostSeparationByDepot)
	{
		Map responses = new HashMap();

		Map<String, Executioner> executionerForDepots = new HashMap<String, Executioner>();

		for (String depot : hostSeparationByDepot.keySet())
		{
			logging.info(this, "from depot " + depot + " we have hosts " + hostSeparationByDepot.get(depot));

			String hostkey =  (String) getHostInfoCollections().getDepots().get(depot).get(HostInfo.hostKeyKEY);

			//logging.info(this, "from depot " + depot + " key " + hostkey + " \n "
			//+ getHostInfoCollections().getMapOfAllPCInfoMaps().get(depot)
			//	 );

			Executioner exec1 = executionerForDepots.get(depot);

			logging.info(this, "working exec for depot "  + depot + " " + (exec1 != null ));

			if (exec1 == null)
			{
				exec1 = retrieveWorkingExec(depot);
				//if (exec1 == Executioner.None)

			}

			if (exec1 != null && exec1 != Executioner.NONE)
			{
				OpsiMethodCall omc = new OpsiMethodCall(
				                         "hostControl_start",
				                         new Object[]{
				                             hostSeparationByDepot.get(depot).toArray(new String[0] )
				                         }
				                     );

				Map responses1 = exec1.getMapResult(omc);
				responses.putAll(responses1);
			}


		}

		return collectErrorsFromResponsesByHost(responses, "wakeOnLan");

	}


	@Override
	public java.util.List<String>  wakeOnLan (
	    java.util.Set<String> hostIds,
	    Map<String, java.util.List<String>> hostSeparationByDepot,
	    Map<String, Executioner> execsByDepot)
	{
		Map responses = new HashMap();

		for (String depot : hostSeparationByDepot.keySet())
		{
			if ( hostSeparationByDepot.get(depot) != null &&  hostSeparationByDepot.get(depot).size() > 0)
			{
				//logging.info(this, "wakeOnLan from depot " + depot + " we have hosts " + hostSeparationByDepot.get(depot));
				java.util.Set<String> hostsToWake = new HashSet<String> (hostIds);
				hostsToWake.retainAll( hostSeparationByDepot.get(depot) );
				//logging.debug(this, "wakeOnLan execute for " +  hostsToWake);

				if (execsByDepot.get(depot) != null && execsByDepot.get(depot) != Executioner.NONE  && hostsToWake.size() > 0)
				{
					logging.debug(this, "wakeOnLan execute for " +  hostsToWake);
					OpsiMethodCall omc = new OpsiMethodCall(
					                         "hostControl_start",
					                         new Object[]{
					                             hostsToWake.toArray(new String[0] )
					                         }
					                     );

					Map responses1 = execsByDepot.get(depot).getMapResult(omc);
					responses.putAll(responses1);
				}
			}

		}

		return collectErrorsFromResponsesByHost(responses, "wakeOnLan");

	}

	public java.util.List<String> fireOpsiclientdEventOnClients (String event, String[] clientIds)
	{
		OpsiMethodCall omc = new OpsiMethodCall(
		                         "hostControl_fireEvent",
		                         new Object[]{ event, clientIds } );

		Map responses = exec.getMapResult(omc);

		return collectErrorsFromResponsesByHost(responses, "fireOpsiclientdEventOnClients");
	}

	public java.util.List<String> showPopupOnClients (String message, String[] clientIds)
	{
		OpsiMethodCall omc = new OpsiMethodCall(
		                         "hostControl_showPopup",
		                         new Object[]{ message, clientIds } );

		Map responses = exec.getMapResult(omc);

		return collectErrorsFromResponsesByHost(responses, "showPopupOnClients");

	}

	public  java.util.List<String> shutdownClients (String[] clientIds)
	{
		OpsiMethodCall omc = new OpsiMethodCall(
		                         "hostControl_shutdown",
		                         new Object[]{ clientIds } );

		Map responses = exec.getMapResult(omc);

		return collectErrorsFromResponsesByHost(responses, "shutdownClients");

	}

	public  java.util.List<String>  rebootClients (String[] clientIds)
	{
		OpsiMethodCall omc = new OpsiMethodCall(
		                         "hostControl_reboot",
		                         new Object[]{ clientIds } );

		Map responses = exec.getMapResult(omc);

		return collectErrorsFromResponsesByHost(responses, "rebootClients");
	}

	public Map<String, Object> reachableInfo(String[] clientIds)
	{
		Object[] callParameters = new Object[]{};
		if (clientIds != null)
		{
			callParameters = new Object[]{ clientIds } ;
		}
		Map<String, Object> result = exec/*Background*/.getMapResult(
		                                 new OpsiMethodCall(
		                                     "hostControl_reachable",
		                                     callParameters,
		                                     OpsiMethodCall.BACKGROUND //background call, do not show waiting info
		                                 )
		                             );

		return result;
	}

	public Map<String, String> sessionInfo(String[] clientIds)
	{
		Map result = new HashMap<String, String>();

		Object[] callParameters = new Object[]{};
		if (clientIds != null && clientIds.length > 0)
		{
			callParameters = new Object[]{ clientIds } ;
		}
		String methodname = "hostControl_getActiveSessions";

		Map<String, Object> result0 = de.uib.opsicommand.JSONReMapper.getResponses(
		                                  exec.retrieveJSONObject (
		                                      new OpsiMethodCall(
		                                          methodname,
		                                          callParameters
		                                      )
		                                  )
		                              );

		for (String key : result0.keySet())
		{
			String value = "";

			if (result0.get(key) instanceof String)
			{
				//error

				String errorStr = (String) result0.get(key);
				value = "no response";
				if (errorStr.indexOf("Opsi timeout") > -1)
				{
					int i = errorStr.indexOf("(");
					if (i > -1)
					{
						value = value + "   " + errorStr.substring(i+1, errorStr.length()-1);
					}
					else
						value = value + " (opsi timeout)";
				}

				else if (errorStr.indexOf(methodname) > - 1)
					value = value + "  (" + methodname + " not valid)";

				else if (errorStr.indexOf("Name or service not known") > -1)
					value = value + " (name or service not known)";
			}

			else if ( result0.get(key) instanceof java.util.List)
			{
				//should then hold

				java.util.List sessionlist = (java.util.List) (result0.get(key));
				for ( Object element: sessionlist)
				{
					Map<String, Object> session = JSONReMapper.getMap_Object((org.json.JSONObject)element);

					String username = "" + session.get("UserName");
					String logondomain = "" + session.get("LogonDomain");

					if (!value.equals(""))
						value = value + "; ";

					value = value + username + " (" + logondomain + "\\" + username + ")";
				}
			}

			result.put(key, value);
		}

		return result;
	}


	//executes all updates collected by setHostDescription ...
	public void updateHosts()
	{
		if ( globalReadOnly )
			return;

		// checkHostPermission is done in updateHost

		if (hostUpdates == null)
			return;

		List<Object> updates = new ArrayList<Object>();
		for (String hostId : hostUpdates.keySet() )
		{
			updates.add( exec.jsonMap( (Map) hostUpdates.get(hostId) ) );
		}


		OpsiMethodCall omc = new OpsiMethodCall(
		                         "host_updateObjects",
		                         new Object[]{updates.toArray()}
		                     );

		if (exec.doCall ( omc ))
			hostUpdates.clear();

	}


	protected void updateHost(String hostId, String property, String value)
	{
		/*
		if (!checkHostPermission( hostId, getHostInfoCollections() ) )
			return;
		*/

		if (hostUpdates == null)
		{
			hostUpdates = new HashMap<String, Map<String, Object>>();
		}

		Map<String, Object> hostUpdateMap = hostUpdates.get(hostId);

		if (hostUpdateMap == null )
		{
			hostUpdateMap = new HashMap<String, Object>();
		}

		//logging.info(this, "updateHost map pre " + hostUpdateMap);

		String sValue = "";
		if (value != null)
			sValue = value;


		hostUpdateMap.put("ident", hostId);
		hostUpdateMap.put("type", "OpsiClient");
		hostUpdateMap.put(property, value);

		//logging.info(this, "updateHost map " + hostUpdateMap);

		hostUpdates.put(hostId, hostUpdateMap);
	}

	public void setHostDescription (String hostId, String description)
	{
		updateHost(hostId, "description", description);
	}

	public void setClientInventoryNumber (String hostId, String inventoryNumber)
	{
		updateHost(hostId, "inventoryNumber", inventoryNumber);
	}

	public void setClientOneTimePassword (String hostId, String oneTimePassword)
	{
		updateHost(hostId, "oneTimePassword", oneTimePassword);
	}

	public void setHostNotes (String hostId, String notes)
	{
		updateHost(hostId, "notes", notes);
	}

	public void setMacAddress(String hostId, String address)
	{
		updateHost(hostId, "hardwareAddress", address);
	}

	public String getMacAddress (String hostId)
	//opsi 3 compatibility
	{
		return "";
	}

	public Map<String, Map<String, String>>  getProductGroups()
	{
		String[] callAttributes = new String[] {};
		HashMap callFilter = new HashMap ();
		callFilter.put("type", Object2GroupEntry.GROUP_TYPE_PRODUCTGROUP);

		Map<String, Map<String, String>> result =
		    exec.getStringMappedObjectsByKey(
		        new OpsiMethodCall(
		            "group_getObjects",
		            new Object[]{
		                callAttributes,
		                callFilter
		            }
		        ),
		        "ident",
		        new String[]{"id", "parentGroupId", "description"},
		        new String[]{"groupId", "parentGroupId", "description"}
		    )
		    ;

		/*
		HashMap noGroup = new HashMap<String, String>();
		noGroup.put("description", "no Group");

		result.put("", noGroup);
		*/

		return result;
	}

	public Map<String, Map<String, String>>  getHostGroups()
	{
		if (hostGroups != null)
			return hostGroups;

		String[] callAttributes = new String[] {};
		HashMap callFilter = new HashMap ();
		callFilter.put("type", Object2GroupEntry.GROUP_TYPE_HOSTGROUP);

		hostGroups = new HostGroups(exec.getStringMappedObjectsByKey(
		                                new OpsiMethodCall(
		                                    "group_getObjects",
		                                    new Object[]{
		                                        callAttributes,
		                                        callFilter
		                                    }
		                                ),
		                                "ident",
		                                new String[]{"id", "parentGroupId", "description"},
		                                new String[]{"groupId", "parentGroupId", "description"}
		                            )
		                           );
		logging.debug(this, "getHostGroups " + hostGroups);

		hostGroups = hostGroups.addSpecialGroups();
		logging.debug(this, "getHostGroups " + hostGroups);
		hostGroups.alterToWorkingVersion();

		logging.debug(this, "getHostGroups rebuilt" + hostGroups);

		return hostGroups;
	}


	public void hostGroupsRequestRefresh()
	{
		hostGroups = null;
	}

	public void fGroup2MembersRequestRefresh()
	{
		fGroup2Members = null;
	}

	public void fProductGroup2MembersRequestRefresh()
	{
		fProductGroup2Members = null;
	}

	private  Map<String, Set<String>>  projectToFunction
	(Map <String, Map<String, String>>  mappedRelation,
	 String originVar,
	 String imageVar)
	{
		Map <String, Set<String>> result = new TreeMap<String, Set<String>>();

		Iterator iter = mappedRelation.keySet().iterator();

		while (iter.hasNext())
		{
			String key = (String) iter.next();
			//System.out.println( "--- key " + key);
			Map<String, String> relation = (Map<String, String>)  mappedRelation.get(key);
			String originValue = relation.get(originVar);
			String imageValue = relation.get(imageVar);
			if (imageValue != null)
			{
				Set<String> assignedSet = result.get(originValue);
				if (assignedSet == null) // no assignment yet
				{
					assignedSet = new TreeSet<String>();
				}
				//logging.debug(this, "projectToFunction: assignedSet, imageValue " + assignedSet + ", " + imageValue);
				assignedSet.add(imageValue);
				result.put(originValue, assignedSet);
				//logging.debug(this, "--- originValue " + originValue + " assigned " +assignedSet);
			}
		}

		return result;
	}

	public Map<String, Set<String>> getFGroup2Members()
	{
		if (fGroup2Members == null)
		{
			fGroup2Members = retrieveFGroup2Members(Object2GroupEntry.GROUP_TYPE_HOSTGROUP, "clientId");
		}

		return fGroup2Members;
	}

	public Map<String, Set<String>> getFProductGroup2Members()
	{
		if (fProductGroup2Members == null)
		{
			fProductGroup2Members = retrieveFGroup2Members(Object2GroupEntry.GROUP_TYPE_PRODUCTGROUP, "productId");
		}

		return fProductGroup2Members;

	}

	private Map<String, Set<String>> retrieveFGroup2Members(String groupType, String memberIdName)
	//returns the function that yields for a given groupId all objects which belong to the group
	{
		String[] callAttributes = new String[] {};
		HashMap callFilter = new HashMap ();
		callFilter.put("groupType", groupType);

		Map<String, Map<String, String>> mappedRelations =

		    exec.getStringMappedObjectsByKey(
		        new OpsiMethodCall(
		            "objectToGroup_getObjects",
		            new Object[]{
		                callAttributes,
		                callFilter
		            }
		        ),
		        "ident",
		        new String[]{"objectId", "groupId"},
		        new String[]{memberIdName, "groupId"}
		    );



		return projectToFunction(mappedRelations, "groupId", memberIdName);
	};


	public void fObject2ProductGroupsRequestRefresh()
	{
		fObject2Groups = null;
	}

	public void fObject2GroupsRequestRefresh()
	{
		fObject2Groups = null;
	}

	public Map<String, Set<String>> getFObject2Groups()
	//returns the function that yields for a given clientId all groups to which the client belongs
	{
		if (fObject2Groups == null)
		{

			Map<String, Map<String, String>> mappedRelations =

			    exec.getStringMappedObjectsByKey(
			        new OpsiMethodCall(
			            "objectToGroup_getObjects",
			            new String[]{}
			        ),
			        "ident",
			        new String[]{"objectId", "groupId"},
			        new String[]{"clientId", "groupId"},
			        de.uib.configed.tree.ClientTree.translationsFromPersistentNames
			    );



			fObject2Groups
			= projectToFunction(mappedRelations, "clientId", "groupId");

		}

		return fObject2Groups;
	}


	public boolean addObject2Group(String objectId, String groupId)
	{
		if ( globalReadOnly )
			return false;

		/*
		if (!checkHostPermission( objectId, getHostInfoCollections() ) )
			return false;
		*/

		String persistentGroupId = de.uib.configed.tree.ClientTree.translateToPersistentName(groupId);
		logging.debug(this, "addObject2Group persistentGroupId " + persistentGroupId);
		OpsiMethodCall omc = new OpsiMethodCall(
		                         "objectToGroup_create",
		                         new String[]{Object2GroupEntry.GROUP_TYPE_HOSTGROUP, persistentGroupId, objectId}
		                     );

		return exec.doCall( omc );
	}

	public boolean removeHostGroupElements(java.util.List<Object2GroupEntry> entries)
	{
		if ( globalReadOnly )
			return false;

		ArrayList<Object> deleteItems = new ArrayList<Object>();
		for (Object2GroupEntry entry : entries)
		{

			//if ( checkHostPermission( entry.getMember(), getHostInfoCollections() ) )
			{
				Map<String, Object>  deleteItem= createNOMitem(Object2GroupEntry.TYPE_NAME);
				deleteItem.put(Object2GroupEntry.GROUP_TYPE_KEY, Object2GroupEntry.GROUP_TYPE_HOSTGROUP);
				deleteItem.put(Object2GroupEntry.GROUP_ID_KEY,  entry.getGroupId());
				deleteItem.put(Object2GroupEntry.MEMBER_KEY, entry.getMember());

				deleteItems.add(exec.jsonMap(deleteItem));
			}
		}

		boolean result = true;
		if (deleteItems.size() > 0)
		{
			OpsiMethodCall omc = new OpsiMethodCall(
			                         "objectToGroup_deleteObjects",
			                         new Object[] {
			                             deleteItems.toArray()
			                         }
			                     )
			                     ;


			if (exec.doCall(omc))
			{
				deleteItems.clear();
			}
			else
				result = false;

		}


		return result;
	}



	public boolean removeObject2Group(String objectId, String groupId)
	{
		if ( globalReadOnly )
			return false;

		/*
		if (!checkHostPermission( objectId, getHostInfoCollections() ) )
			return false;
		*/

		String persistentGroupId = de.uib.configed.tree.ClientTree.translateToPersistentName(groupId);
		OpsiMethodCall omc = new OpsiMethodCall(
		                         "objectToGroup_delete",
		                         new String[]{Object2GroupEntry.GROUP_TYPE_HOSTGROUP, persistentGroupId , objectId}
		                     );

		return exec.doCall( omc );
	}

	public boolean addGroup(StringValuedRelationElement newgroup)
	{
		return addGroup(newgroup, true);
	}

	private boolean addGroup(StringValuedRelationElement newgroup, boolean requestRefresh)
	{
		if ( !serverFullPermission )
			return false;

		logging.debug(this, "addGroup : " + newgroup +  " requestRefresh " + requestRefresh);

		String  id = newgroup.get("groupId");
		String parentId = newgroup.get("parentGroupId");
		if (parentId == null
		        || parentId.equals(de.uib.configed.tree.ClientTree.GROUPS_NAME)
		   )
			parentId = null;

		parentId = de.uib.configed.tree.ClientTree.translateToPersistentName(parentId);


		if (id.equalsIgnoreCase(parentId))
		{
			logging.error(this, "Cannot add group as child to itself, group ID " + id);
			return false;
		}

		String description = newgroup.get("description");
		String notes = "";

		OpsiMethodCall omc = new OpsiMethodCall(
		                         "group_createHostGroup",
		                         new Object[]{id, description, notes, parentId}
		                     );
		boolean result = exec.doCall ( omc );
		if (result)
			hostGroupsRequestRefresh();

		return result;

	}


	public boolean deleteGroup (String groupId)
	{
		if ( !serverFullPermission )
			return false;


		if (groupId ==  null)
			return false;

		OpsiMethodCall omc = new OpsiMethodCall(
		                         "group_delete",
		                         new String[]{groupId}
		                     );
		boolean result =  exec.doCall ( omc );

		if (result)
			hostGroupsRequestRefresh();

		return result;
	}

	public boolean updateGroup (String groupId, Map<String, String> updateInfo)
	{
		if ( !serverFullPermission )
			return false;

		if (groupId == null)
			return false;

		if (updateInfo == null)
			updateInfo = new HashMap<String, String>();

		updateInfo.put("ident", groupId);
		updateInfo.put("type", Object2GroupEntry.GROUP_TYPE_HOSTGROUP);

		if ( updateInfo.get("parentGroupId").equals(de.uib.configed.tree.ClientTree.GROUPS_NAME) )
			updateInfo.put("parentGroupId", "null");

		String parentGroupId = updateInfo.get("parentGroupId");
		parentGroupId = de.uib.configed.tree.ClientTree.translateToPersistentName(parentGroupId);
		updateInfo.put("parentGroupId", parentGroupId);

		logging.debug(this, "updateGroup " + parentGroupId);


		OpsiMethodCall omc = new OpsiMethodCall(
		                         "group_updateObject",
		                         new Object[]{exec.jsonMap(updateInfo)}
		                     );
		return exec.doCall ( omc );
	}


	public boolean setProductGroup(String groupId, String description, Set<String> productSet)
	{
		if ( !serverFullPermission )
			return false;


		logging.debug(this, "setProductGroup: groupId " + groupId);
		if (groupId == null)
			return false;

		boolean result = true;

		HashMap map = new HashMap<String, String>();

		map.put("id", groupId);
		map.put("type", Object2GroupEntry.GROUP_TYPE_PRODUCTGROUP);

		if  (description != null)
			map.put("description", description);

		OpsiMethodCall omc = new OpsiMethodCall(
		                         "group_createObjects",
		                         new Object[]{
		                             new Object[]{exec.jsonMap(map)}
		                         }
		                     );
		result = exec.doCall ( omc );

		HashSet<String> inNewSetnotInOriSet = new HashSet<String>(productSet);
		HashSet<String> inOriSetnotInNewSet = new HashSet<String>();

		if (groupId != null && getFProductGroup2Members().get(groupId) != null)
		{

			Set oriSet =  getFProductGroup2Members().get(groupId);
			logging.debug(this, "setProductGroup: oriSet " + oriSet);
			inOriSetnotInNewSet = new HashSet<String>(oriSet);
			inOriSetnotInNewSet.removeAll(productSet);
			inNewSetnotInOriSet.removeAll(oriSet);
		}


		logging.debug(this, "setProductGroup: inOriSetnotInNewSet, inNewSetnotInOriSet. "
		              +  inOriSetnotInNewSet + ", " + inNewSetnotInOriSet);


		final Map typingObject = new HashMap();
		typingObject.put("groupType", Object2GroupEntry.GROUP_TYPE_PRODUCTGROUP);
		typingObject.put("type",  Object2GroupEntry.TYPE_NAME);


		List object2Groups = new ArrayList();
		for (String objectId : inOriSetnotInNewSet)
		{
			Map m = new HashMap(typingObject);
			m.put("groupId", groupId);
			m.put("objectId", objectId);
			object2Groups.add(exec.jsonMap(m));
		}

		logging.debug(this, "delete objects " + object2Groups);

		if (object2Groups.size() > 0)
		{
			Object jsonArray = exec.jsonArray(object2Groups);
			result = result && exec.doCall(
			             new OpsiMethodCall(
			                 "objectToGroup_deleteObjects",
			                 new Object[]{
			                     jsonArray
			                 }
			             )
			         )
			         ;
		}

		object2Groups.clear();
		for (String objectId : inNewSetnotInOriSet)
		{
			Map m = new HashMap(typingObject);
			m.put("groupId", groupId);
			m.put("objectId", objectId);
			object2Groups.add(exec.jsonMap(m));
		}

		logging.debug(this, "create new objects " + object2Groups);

		if (object2Groups.size() > 0)
		{
			Object jsonArray = exec.jsonArray(object2Groups);
			result = result && exec.doCall(
			             new OpsiMethodCall(
			                 "objectToGroup_createObjects",
			                 new Object[]{
			                     jsonArray
			                 }
			             )
			         )
			         ;
		}

		if (result)
		{
			getFProductGroup2Members().put(groupId, productSet);
		}


		return result;

	}



	public List<String> getHostGroupIds()
	{
		Set<String> groups = getHostGroups().keySet();
		groups.remove(de.uib.configed.tree.ClientTree.DIRECTORY_NAME);

		return new ArrayList<String> (new TreeSet<String> (groups) );


		/*
		return exec.getListResult ( new OpsiMethodCall(
						"getGroupIds_list", 
						new String[]{}) );
		*/
	}

	/*
	public boolean writeGroup (String groupname, String[] groupmembers)
{
		Object[] params = new Object[] {groupname, groupmembers};

		OpsiMethodCall omc = new OpsiMethodCall("createGroup", params);

		return exec.doCall ( omc );
}
	*/


	/*
	public String getPcInfo( String hostId )
	// getHostDescription
{
		return exec.getStringResult ( new OpsiMethodCall( "getHostDescription", new String[]{hostId} ) );
}
	*/

	/*
	public boolean existsEntry (String pcname)
	{
		//if (mapPC_selected == null) return false;
		if (hostInfoCollections.getMapOfPCs() == null) return false;

		return hostInfoCollections.getMapOfPCs().containsKey(pcname);
		//return  mapPC_selected.containsKey(pcname);
		// without casting to string we test equality by Object.equals() whereas String.equals() gives what we want
	}
	*/


	public void hwAuditConfRequestRefresh()
	{
		hwAuditConf.clear();
	}


	public List<Object> getOpsiHWAuditConf (String locale)
	{
		if (!hwAuditConf.containsKey(locale))
		{
			hwAuditConf.put(locale, exec.getListOfMapsOfListsOfMaps(new OpsiMethodCall(
			                    "getOpsiHWAuditConf",
			                    new String[]{ locale }) ) );
		}

		return hwAuditConf.get(locale);
	}


	public Map getSoftwareInfo (String clientId)
	{
		return null;
	}


	public void softwareAuditOnClientsRequestRefresh()
	{
		logging.info(this, "softwareAuditOnClientsRequestRefresh");
		dataStub.softwareAuditOnClientsRequestRefresh();
	}

	public void fillClient2Software(java.util.List<String> clients)
	{
		dataStub.fillClient2Software(clients);
	}


	public Map<String, java.util.List<SWAuditClientEntry>> getClient2Software()
	{
		return dataStub.getClient2Software();
	}

	/*
	public  java.util.List <Map<String, Object>> getSoftwareAuditOnClients()
{
		//logging.debug(this, "getSoftwareAuditOnClients");
		return dataStub.getSoftwareAuditOnClients();
}
	*/


	public DatedRowList getSoftwareAudit (String clientId)
	{
		dataStub.fillClient2Software(clientId);
		//logging.info(this, " " + dataStub.getClient2Software());
		java.util.List<SWAuditClientEntry> entries = dataStub.getClient2Software().get(clientId);
		//logging.info(this, " getSoftwareAudit for client " + clientId + "-- " + dataStub.getClient2Software().get(clientId));


		if (entries == null)
			return null;


		List<String[]> list = new ArrayList<String[]>();
		String dateS = null;

		if (entries.size() > 0)
			dateS = entries.get(0).getLastModification();


		for (SWAuditClientEntry entry : entries)
		{
			if (entry.getSWid() != null)
			{
				logging.debug(this, "getSoftwareAudit,  client entry " + entry); 
				list.add(entry.getExpandedData(getInstalledSoftwareInformation(), getSWident(entry.getSWid()) ));
			}
		}

		logging.info(this, "getSoftwareAudit for client " + clientId);
		return new DatedRowList(list, dateS);
	}

	public String getLastSoftwareAuditModification(String clientId)
	{
		String result = "";

		if (clientId  != null
		        &&!clientId.equals("")
		        &&dataStub.getClient2Software() != null
		        && dataStub.getClient2Software().get(clientId) != null
		        && dataStub.getClient2Software().get(clientId).size() > 0
		   )
		{
			result = dataStub.getClient2Software().get(clientId).get(0).getLastModification();
		}

		return result;
	}


	@Override
	public Map<String, Map/*<String, String>*/> retrieveSoftwareAuditData(String clientId)
	{
		Map<String, Map/*<String, String>*/> result = new TreeMap<String, Map/*<String, String>*/>();

		if (clientId == null || clientId.equals(""))
			return result;

		dataStub.fillClient2Software(clientId);
		//logging.info(this, " " + dataStub.getClient2Software());
		java.util.List<SWAuditClientEntry> entries = dataStub.getClient2Software().get(clientId);
		//logging.info(this, " getSoftwareAudit for client " + clientId + "-- " + dataStub.getClient2Software().get(clientId));


		if (entries == null)
			return result;




		for (SWAuditClientEntry entry : entries)
		{
			if (entry.getSWid() != null)
			{
				result.put(
				    "" + entry.getSWid(),
				    entry.getExpandedMap( getInstalledSoftwareInformation(), getSWident( entry.getSWid() ) )
				);
			}
		}

		return result;
	}





	/*
	public List<Map<String, String>> getSoftwareAudit (String clientId)
{
		java.util.List <Map<String, String>> result = new ArrayList<Map<String, String>>();
		
		for (SWAuditClientEntry entry : dataStub.getClient2Software().get(clientId))
		{
			String ident = dataStub.getSWident(entry.getSWid());
			
			Map<String, String> rowmap = dataStub.getInstalledSoftwareInformation().get(ident);
			rowmap.put("lastseen", entry.getLastModification());
			result.add(rowmap);
		}
			
		
		 
		return result;
}
	*/

	/*

	public List<Map<String, Object>> getSoftwareAudit (String clientId)
{
		//softwareAudit 
		logging.debug(this, "call getSoftwareAudit for " + clientId);
		//getSoftwareAuditOnClients();
		//logging.injectLogLevel(new Integer(logging.LEVEL_WARNING));
		List<Map<String, Object>> info = exec.getListOfMaps(new OpsiMethodCall(
						//"extend/configed",
						"getSoftwareAudit",
						new String[] { clientId } ) );
		//logging.injectLogLevel(null);
		return info;
}
	*/

	public Object getHardwareInfo (String clientId, boolean asHTMLtable)
	{
		if (clientId == null)
			return null;

		Map info = exec.getMapOfListsOfMaps(new OpsiMethodCall(
		                                        "getHardwareInformation_hash",
		                                        new String[] { clientId } ) );
		if (info.size() > 1) //the first element is a default scantime
			// new version of hardware info
		{
			return info;
		}

		return null;

	}

	public void auditHardwareOnHostRequestRefresh()
	{
		relations_auditHardwareOnHost =null;
	}

	public List< Map<String, Object> > getHardwareOnClient()
	{
		if (relations_auditHardwareOnHost == null)
		{
			Map<String, String> filterMap = new HashMap<String, String>();
			filterMap.put( "state", "1" );
			relations_auditHardwareOnHost
			= exec.getListOfMaps( new OpsiMethodCall(
			                          "auditHardwareOnHost_getHashes",
			                          new Object[] {new String[0], filterMap } ) );
		}

		//logging.info(this, "relations_auditHardwareOnHost " + relations_auditHardwareOnHost);

		return relations_auditHardwareOnHost;
	}

	public String[] getLogtypes()
	{
		if (logtypes == null)
			logtypes = Globals.logtypes;

		return logtypes;
	}

	public Map<String, String> getEmptyLogfiles()
	{
		logfiles = new HashMap<String, String>();
		String[] logtypes = getLogtypes();

		for (int i = 0; i < logtypes.length; i++)
		{
			logfiles.put(logtypes[i], "");
		}

		return logfiles;
	}



	public Map<String, String> getLogfiles(String clientId, String logtype)
	{
		String[] logtypes = getLogtypes();

		if (logfiles == null)
		{
			getEmptyLogfiles();
		}

		int i = Arrays.asList(Globals.logtypes).indexOf(logtype);
		if (i < 0)
		{
			logging.error("illegal logtype: " + logtype);
			return logfiles;
		}

		logging.debug(this, "------------- getLogfile logtye " + logtype);

		String s = "";
		try{

			logging.debug(this, "OpsiMethodCall readLog " + logtypes[i] + " max size " + Globals.maxLogSizes[i]);
			// since loglines becoming too long;
			//logging.injectLogLevel(new Integer(logging.LEVEL_WARNING));
			try{

				//if ( getMethodSignature("readLog").contains("maxSize") )
				{
					if (Globals.maxLogSizes[  i  ] == 0)
					{
						s = exec.getStringResult(
						        new OpsiMethodCall(
						            "readLog",
						            new String[]{ logtype, clientId }) );
					}
					else
					{
						s = exec.getStringResult(
						        new OpsiMethodCall(
						            "readLog",
						            new String[]{ logtype, clientId, String.valueOf(Globals.maxLogSizes[i]) }) );
					}
				}
				/*
				else

			{
					s = exec.getStringResult(
					        new OpsiMethodCall(
					            "readLog",
					            new String[]{ logtype, clientId }) );
			}
				*/
				//logging.injectLogLevel(null);

			}
			catch (java.lang.OutOfMemoryError e)
			{
				s = "--- file too big for showing, enlarge java memory  ---";
				System.gc();
			}
			logging.injectLogLevel(null);

		}
		catch (Exception ex)
		{
			s = "not found, " + ex;
		}

		logfiles.put(logtype, s);

		/*
		while (i < logtypes.length)
		{
			result.put(logtypes[i], "memory missing");
			i++;
			
	}
		*/

		return logfiles;
	}



	public Map<String, String> getLogfiles(String clientId)
	{
		logfiles = new HashMap<String, String>();

		//logging.debug(this, "------------- getLogfiles");

		String[] logtypes = getLogtypes();

		for (int i = 0; i < logtypes.length; i++)
		{
			getLogfiles(clientId, logtypes[i]);
		}

		return logfiles;
	}


	/*
	public Vector getInstallImages()
{
		return new Vector(
		           exec.getListResult(
		               new OpsiMethodCall(
		                   "getBootimages_list",
		                   new String[]{}) ) );
}
	*/

	/*
	public void  depotProductPropertiesRequestRefresh()
{
		depot2product2properties = null;
		dataStub.productPropertyStatesRequestRefresh();
}
	*/

	public void depotChange()
	{
		logging.info(this, "depotChange");
		productGlobalInfos = null;
		possibleActions = null;
		saveDepotId = null;
		productIds = null;
		netbootProductNames = null;
		localbootProductNames = null;
		retrieveProducts();
		retrieveProductPropertyDefinitions();
		getProductGlobalInfos(theDepot);

	}

	public void productDataRequestRefresh()
	{
		dataStub.productDataRequestRefresh();
		productpropertiesRequestRefresh();
		depot2product2properties = null;
		depotChange();
	}


	public List<String> getAllLocalbootProductNames (String depotId)
	{
		logging.debug(this, "getAllLocalbootProductNames for depot " + depotId);
		logging.info(this, "getAllLocalbootProductNames, producing " + (localbootProductNames == null));
		if (localbootProductNames == null)
		{
			//opsi 4.0
			//localbootProductNames = exec.getListResult(  new OpsiMethodCall("getLocalBootProductIds_list", new String[]{theDepot}) );

			//logging.injectLogLevel(logging.LEVEL_WARNING);
			Map productOrderingResult = (Map) exec.getMapOfLists(  new OpsiMethodCall(
			                                "getProductOrdering",
			                                //new String[]{depotId, "algorithm2"})
			                                new String[]{depotId })
			                                                    );

			//logging.injectLogLevel(null);

			List<String> sortedProducts = (List<String>) productOrderingResult.get("sorted");
			if (sortedProducts == null)
				sortedProducts = new ArrayList<String>();

			List<String> notSortedProducts = (List<String>) productOrderingResult.get("not_sorted");
			if (notSortedProducts == null)
				notSortedProducts = new ArrayList<String>();

			logging.info(this, "not ordered "
			             + (notSortedProducts.size() - sortedProducts.size()) + ""
			            );
			;

			notSortedProducts.removeAll(sortedProducts);
			logging.info(this, "missing: " +  notSortedProducts);

			localbootProductNames = sortedProducts;
			localbootProductNames.addAll(notSortedProducts);




			/*
			localbootProductNames = exec.getStringListResult(  new OpsiMethodCall(
				//"extend/configed",
				"getProductOrdering",
				new String[]{theDepot})
				)
			;
			*/

		}

		logging.info(this, "localbootProductNames sorted, size " + localbootProductNames.size());

		return localbootProductNames;
	}


	public List<String> getAllLocalbootProductNames ()
	{
		return  getAllLocalbootProductNames (theDepot);
	}

	public void retrieveProducts()
	{
		retrieveDepotProducts(theDepot);
	}

	protected void retrieveDepotProducts(String depotId)
	{
		logging.debug(this, "retrieveDepotProducts for " + depotId);
		/*
		if (netbootProductNames == null || localbootProductNames == null)
			dataStub.retrieveProductsAllDepots();
		*/


		if (dataStub.getDepot2NetbootProducts().get(depotId) != null)
			netbootProductNames = new ArrayList<String>( dataStub.getDepot2NetbootProducts().get(depotId).keySet() )  ;
		else
			netbootProductNames = new ArrayList<String>();

		localbootProductNames = getAllLocalbootProductNames(depotId);


		//= new ArrayList<String>( dataStub.getDepot2LocalbootProducts().get(depotId).keySet() );

	}


	public List<String> getAllDepotsWithIdenticalProductStock(String depot)
	{
		List<String> result = new ArrayList<String>();

		TreeSet<OpsiPackage> first =
		    dataStub.getDepot2Packages().get(depot);
		logging.info(this, "getAllDepotsWithIdenticalProductStock " + first);

		for (String testdepot : getHostInfoCollections().getAllDepots().keySet())
		{
			if (
			    depot.equals(testdepot)
			    ||
			    (first == null && dataStub.getDepot2Packages().get(testdepot) == null)
			    ||
			    (first != null && first.equals(dataStub.getDepot2Packages().get(testdepot)))
			)
				result.add(testdepot);
		}
		logging.info(this, "getAllDepotsWithIdenticalProductStock  result " + result);

		return result;
	}


	public List<String> getAllNetbootProductNames (String depotId)
	{
		if (netbootProductNames == null)
		{
			retrieveDepotProducts(depotId);
		}
		return netbootProductNames;
	}

	public List<String> getAllNetbootProductNames ()
	{
		return getAllNetbootProductNames(theDepot);
	}

	public Vector<String> getWinProducts(String depotId, String depotProductDirectory)
	{
		Vector<String> winProducts = new Vector<String>();
		if (depotProductDirectory == null)
			return winProducts;

		boolean smbMounted = new File(depotProductDirectory).exists();

		for (String product: new TreeSet<String>(getAllNetbootProductNames (depotId)))
		{
			//if (product.toLowerCase().startsWith("win"))
			if (
			    !smbMounted //probably not on Windows, take every product to correct path manually
			    ||
			    new File(depotProductDirectory + File.separator + product + File.separator
			             + de.uib.connectx.SmbConnect.directoryPE).exists() //win 6.x
			    ||
			    new File(depotProductDirectory + File.separator + product + File.separator
			             + de.uib.connectx.SmbConnect.directoryI386).exists() //XP
			)
				winProducts.add(product);
		}

		return winProducts;
	}

	@Override
	public Map<String, Map<String, OpsiProductInfo>> getProduct2versionInfo2infos()
	{
		return dataStub.getProduct2versionInfo2infos();
	}
	
	
	@Override
	public Object2Product2VersionList getDepot2LocalbootProducts()
	{
		return dataStub.getDepot2LocalbootProducts();
	}
	
	@Override
	public Object2Product2VersionList getDepot2NetbootProducts()
	{
		return dataStub.getDepot2NetbootProducts();
	}
	
	
	/*
	public void retrieveProductGlobalInfos()
{
		retrieveProductGlobalInfos(theDepot);
}
	*/


	private void retrieveProductGlobalInfos(String depotId)
	{
		//depotId = "tst-srv-001.uib.local";
		logging.info(this, "retrieveProductGlobalInfos , depot " + depotId);

		productGlobalInfos = new HashMap<String, Map<String, Object>>();
		possibleActions = new HashMap<String, List<String>>();

		for (String productId : dataStub.getProduct2versionInfo2infos().keySet())
		{
			if (dataStub.getProduct2versionInfo2infos().get(productId) == null)
				logging.warning(this, "retrieveProductGlobalInfos productId == null for product " + productId);
				
			if (dataStub.getProduct2versionInfo2infos().get(productId) != null)
			{
				String versionInfo = null;
				Map <String, OpsiProductInfo> productAllInfos = dataStub.getProduct2versionInfo2infos().get(productId);


				//look for associated product on depot info
				Product2VersionList product2VersionList = dataStub.getDepot2LocalbootProducts().get(depotId);
				if (product2VersionList != null &&  product2VersionList.get(productId) != null && product2VersionList.get(productId).size()>0)
				{
					versionInfo =  product2VersionList.get(productId).get(0);
				}

				if (versionInfo == null)
				{
					product2VersionList = dataStub.getDepot2NetbootProducts().get(depotId);

					if (product2VersionList != null &&  product2VersionList.get(productId) != null && product2VersionList.get(productId).size()>0)
					{
						versionInfo =  product2VersionList.get(productId).get(0);
					}
				}

				//if found go on

				if (versionInfo != null && productAllInfos.get(versionInfo) != null)
				{
					OpsiProductInfo productInfo = productAllInfos.get(versionInfo);

					//logging.info(this, "productInfo " + productInfo);
					
					//System.exit(0);

					possibleActions.put(productId, productInfo.getPossibleActions());

					Map aProductInfo = new HashMap<String, Object>();

					aProductInfo.put("actions", productInfo.getPossibleActions());


					aProductInfo.put(
					    de.uib.opsidatamodel.productstate.ProductState.KEY_productId,
					    productId
					    //productInfo.getProductId()
					);
					aProductInfo.put(
					    de.uib.opsidatamodel.productstate.ProductState.KEY_versionInfo,
					    Globals.ProductPackageVersionSeparator.formatKeyForDisplay(productInfo.getVersionInfo())
					);

					aProductInfo.put(
					    de.uib.opsidatamodel.productstate.ProductState.KEY_productPriority,
					    productInfo.getPriority()
					);

					aProductInfo.put(
					    de.uib.opsidatamodel.productstate.ProductState.KEY_productName,
					    //OpsiProductInfo.SERVICEkeyPRODUCT_NAME,
					    productInfo.getProductName()
					);

					aProductInfo.put(
					    OpsiProductInfo.SERVICEkeyPRODUCT_DESCRIPTION,
					    productInfo.getDescription()
					);

					aProductInfo.put(
					    OpsiProductInfo.SERVICEkeyPRODUCT_ADVICE,
					    productInfo.getAdvice()
					);


					aProductInfo.put(
					    de.uib.opsidatamodel.productstate.ProductState.KEY_productVersion,
					    productInfo.getProductVersion()
					);

					aProductInfo.put(
					    de.uib.opsidatamodel.productstate.ProductState.KEY_packageVersion,
					    productInfo.getPackageVersion()
					);

					logging.debug(this, "productInfo " + aProductInfo);
					//System.exit(0);

					productGlobalInfos.put(productId, aProductInfo);


					//counting++;

				}
			}
		}
		
		logging.info(this, "retrieveProductGlobalInfos  found number  " + productGlobalInfos.size());
		
	}



	private void checkProductGlobalInfos(String depotId)
	{
		logging.info(this, "checkProductGlobalInfos for Depot " + depotId);
		if (!(theDepot.equals(depotId))) logging.warning(this, "depot irregular, preset " + theDepot);
		if (depotId == null || depotId.equals(""))
		{
			logging.warning(this, "checkProductGlobalInfos called for no depot");
		}
		logging.debug(this, "checkProductGlobalInfos depotId " + depotId 	+ " productGlobaInfos  = null " +(productGlobalInfos  ==null) 	+ " possibleActions = null " + (possibleActions == null)	);
		if (possibleActions == null
		        || productGlobalInfos == null
		        || theDepot == null
		        || !theDepot.equals(depotId)
		   )
		{
			retrieveProductGlobalInfos(depotId);
		}
	}


	public Map<String, java.util.List<String>>  getPossibleActions(String depotId)
	//map with key productId
	{
		logging.debug(this, "getPossibleActions depot irregular " + !theDepot.equals(depotId));
		checkProductGlobalInfos(depotId);
		return possibleActions;
	}



	/*
	public Map getMapOfProductStates (String clientId)
{
		HashMap result = new HashMap();

		List productstates =  exec.getListResult( new OpsiMethodCall("getProductInstallationStatus_listOfHashes", new String[]{clientId}) );

		//System.out.println ("========== productstates " + productstates);

		if (productstates != null)
		{
			for (int i = 0; i < productstates.size(); i++)
			{
				Map productEntry = exec.getMapFromItem( productstates.get(i) );

				if (productEntry.get(OpsiProduct.KEY) != null)
				{
					result.put   (productEntry.get(OpsiProduct.KEY),  productEntry.get(InstallationStatus.KEY) );
				}
				// try bugfix
				{
					logging.debug (this, " --------------- null for " + OpsiProduct.KEY);
					//result.put   (productEntry.get(OpsiProduct.KEY),  productEntry.g );
				}
			}
		}
		return result;
}


	public Map getMapOfProductActions (String clientId)
{
		Map result = new HashMap();
		List productactions =  exec.getListResult( new OpsiMethodCall( "getProductActionRequests_listOfHashes", new String[]{clientId} ) );

		if  (productactions != null)
		{
			for (int i = 0; i < productactions.size(); i++)
			{
				Map actionEntry = exec.getMapFromItem(productactions.get(i));
				if  (actionEntry.get(OpsiProduct.KEY)  !=  null)
				{
					result.put (actionEntry.get(OpsiProduct.KEY), actionEntry.get(ActionRequest.KEY));
				}
			}
		}

		return result;
}
	*/



	public Map getMapOfProductStatesAndActions (String[] clientIds)
	{
		logging.debug(this, "getMapOfProductStatesAndActions for : " + logging.getStrings(clientIds));

		Map result = new HashMap();

		if (clientIds == null || clientIds.length == 0)
			return result;


		Map states = getProductStatesNOM(clientIds);

		//logging.debug(this, "getMapOProductStatesAndActions for : " + logging.getStrings(clientIds) + "\n" 	+ states);


		if  (states != null)
		{
			return states;
		}


		return result;

	}


	private Map<String, java.util.List<Map<String, String>>>  getLocalBootProductStates(
	    String[] clientIds,
	    Map<String, java.util.List<Map<String, String>>> currentMap)
	{

		Map<String, java.util.List<Map<String, String>>>
		result = getLocalBootProductStatesNOM(clientIds);

		return result;
	}



	protected  Map<String, java.util.List<Map<String, String>>> getProductStatesNOM(String[] clientIds)
	{
		//logging.info(this, "getProductStatesNOM for " + Arrays.toString(clientIds));
		String[] callAttributes =  new String[] {};
		HashMap callFilter = new HashMap();
		callFilter.put("type", "ProductOnClient");
		callFilter.put("clientId",  exec.jsonArray(java.util.Arrays.asList(clientIds)));
		//callFilter.put("productType", LOCALBOOT_PRODUCT_SERVER_STRING);

		java.util.List<Map<java.lang.String,java.lang.Object>>
		productOnClients
		= exec.getListOfMaps(
		      new OpsiMethodCall(
		          "productOnClient_getHashes",
		          new Object[]{
		              callAttributes,
		              callFilter
		          }
		      )
		  )
		  ;

		Map<String, java.util.List<Map<String, String>>> result = new HashMap<String, java.util.List<Map<String, String>>>();
		for (Map m : productOnClients)
		{
			String client = (String) m.get("clientId");
			java.util.List<Map<String, String>>states1Client = result.get(client);
			if (states1Client == null)
			{
				states1Client = new ArrayList<Map<String, String>>();
				result.put(client, states1Client);
			}


			//states1Client.add(JSONReMapper.giveEmptyForNull(m));


			states1Client.add(
			    new ProductState(
			        JSONReMapper.giveEmptyForNull(m),
			        true
			    )
			);
		}
		return result;
	}


	protected  Map<String, java.util.List<Map<String, String>>> getLocalBootProductStatesNOM(String[] clientIds)
	{
		String[] callAttributes =  new String[] {};
		HashMap callFilter = new HashMap();
		callFilter.put("type", "ProductOnClient");
		callFilter.put("clientId",  exec.jsonArray(java.util.Arrays.asList(clientIds)));
		callFilter.put("productType", OpsiPackage.LOCALBOOT_PRODUCT_SERVER_STRING);

		java.util.List<Map<java.lang.String,java.lang.Object>>
		productOnClients
		= exec.getListOfMaps(
		      new OpsiMethodCall(
		          "productOnClient_getHashes",
		          new Object[]{
		              callAttributes,
		              callFilter
		          }
		      )
		  )
		  ;

		Map<String, java.util.List<Map<String, String>>> result = new HashMap<String, java.util.List<Map<String, String>>>();

		for (Map m : productOnClients)
		{
			//logging.info(this, " getLocalBootProductStatesNOM " + m);
			String client = (String) m.get( ProductOnClient.CLIENTid );
			java.util.List<Map<String, String>>states1Client = result.get(client);
			if (states1Client == null)
			{
				states1Client = new ArrayList<Map<String, String>>();
				result.put(client, states1Client);
			}

			Map<String, String> aState = new ProductState(
			                                 JSONReMapper.giveEmptyForNull(m),
			                                 true
			                             ) ;

			states1Client.add(aState);

			//logging.info(this,  "getLocalBootProductStatesNOM aState  " + aState);

			//logging.info(this, " getLocalBootProductStatesNOM " + rebuiltEntry);

		}


		return result;
	}


	public Map getMapOfLocalbootProductStatesAndActions (String[] clientIds)
	{
		return getMapOfLocalbootProductStatesAndActions (clientIds, null);
	}

	public Map getMapOfLocalbootProductStatesAndActions (String[] clientIds,
	        Map currentMap)
	{
		logging.debug(this, "getMapOfLocalbootProductStatesAndActions for : " + logging.getStrings(clientIds));

		if (clientIds == null || clientIds.length == 0)
			return null;

		Map result = new HashMap();
		Map states = null;

		states = getLocalBootProductStates(clientIds, currentMap);

		//logging.debug(this, "getMapOfLocalbootProductStatesAndActions for : " + logging.getStrings(clientIds) + "\n"
		//	+ states);


		if  (states != null)
		{
			return states;
		}


		return result;
	}
	//test ende*/




	protected  Map<String, java.util.List<Map<String, String>>> getNetBootProductStatesNOM(String[] clientIds)
	{
		String[] callAttributes =  new String[] {};
		HashMap callFilter = new HashMap();
		callFilter.put("type", "ProductOnClient");
		callFilter.put("clientId",  exec.jsonArray(java.util.Arrays.asList(clientIds)));
		callFilter.put("productType", OpsiPackage.NETBOOT_PRODUCT_SERVER_STRING);

		java.util.List<Map<java.lang.String,java.lang.Object>>
		productOnClients
		= exec.getListOfMaps(
		      new OpsiMethodCall(
		          "productOnClient_getHashes",
		          new Object[]{
		              callAttributes,
		              callFilter
		          }
		      )
		  )
		  ;

		Map<String, java.util.List<Map<String, String>>> result = new HashMap<String, java.util.List<Map<String, String>>>();
		for (Map m : productOnClients)
		{
			//logging.info(this, " getNetBootProductStatesNOM " + m);
			String client = (String) m.get("clientId");
			java.util.List<Map<String, String>>states1Client = result.get(client);
			if (states1Client == null)
			{
				states1Client = new ArrayList<Map<String, String>>();
				result.put(client, states1Client);
			}

			states1Client.add(
			    new ProductState(
			        JSONReMapper.giveEmptyForNull(m),
			        true
			    )
			);
		}
		return result;
	}



	public Map getMapOfNetbootProductStatesAndActions (String[] clientIds)
	{
		logging.debug(this, "getMapOfNetbootProductStatesAndActions for : " + logging.getStrings(clientIds));

		if (clientIds == null || clientIds.length == 0)
			return null;

		Map result = new HashMap();

		Map states = getNetBootProductStatesNOM(clientIds);
		/*
		exec.getMapOfListsOfMaps( new OpsiMethodCall(
			//"extend/configed", 
			"getNetBootProductStates_hash", 
			new Object[] { clientIds } ) );
		*/

		if  (states != null)
		{

			return states;
		}
		return result;
	}



	protected boolean updateProductOnClient(String pcname, String productname, int producttype, Map updateValues,
	                                        java.util.List updateItems
	                                       )
	{
		HashMap values = new HashMap();

		values.put("productType", OpsiPackage.giveProductType(producttype));
		values.put("type", "ProductOnClient");
		values.put("clientId", pcname);
		values.put("productId", productname);
		values.putAll(updateValues);

		logging.debug(this, "updateProductOnClient, values " + values);
		updateItems.add(exec.jsonMap(values));

		return true;
	}


	public boolean updateProductOnClient(String pcname, String productname, int producttype, Map updateValues)
	{
		if (updateProductOnClient_items == null)
			updateProductOnClient_items = new ArrayList();

		return updateProductOnClient(pcname, productname, producttype, updateValues, updateProductOnClient_items);
	}

	public boolean updateProductOnClients( java.util.List updateItems )
	//hopefully we get only updateItems for allowed clients
	{
		logging.info(this,"updateProductOnClients ");

		if ( globalReadOnly )
			return false;


		boolean result = false;

		if (
		    updateItems != null
		    && updateItems.size() > 0
		)
		{
			logging.info(this,"updateProductOnClients  updateItems.size " + updateItems.size() );

			OpsiMethodCall omc
			= new OpsiMethodCall(
			      "productOnClient_updateObjects",
			      new Object[]{exec.jsonArray(updateItems)}
			  );

			result = exec.doCall(omc);

			if (result) updateItems.clear();
		}

		return result;
	}


	public boolean updateProductOnClients()
	{
		return updateProductOnClients( updateProductOnClient_items );
	}



	public boolean updateProductOnClients(
	    Set<String> clients,
	    String productName,
	    int productType, Map<String, String> changedValues)
	{
		java.util.List updateCollection = new ArrayList();

		boolean result = true;

		//collect updates for all clients
		for (String client : clients)
		{
			result = result
			         && updateProductOnClient(
			             client, productName, productType,
			             changedValues, updateCollection);
		}

		//execute
		return result && updateProductOnClients(updateCollection);
	}


	public boolean freeAllPossibleLicences(String[] selectedClients)
	//hopefully we get only updateItems for allowed clients
	{
		if ( !serverFullPermission )
			return false;

		boolean result = true;


		//get bound licenses;
		//get all licenses

		for (int i = 0; i < selectedClients.length; i++)
		{
			Map<String, Object> licenceOnClientItem = createNOMitem("LicenseOnClient");
			//productOnClientItem.put("productType", OpsiPackage.LOCALBOOT_PRODUCT_SERVER_STRING);
			//productOnClientItem.put("clientId", selectedClients[i]);
			//productOnClientItem.put("productId", product);

			//deleteProductItems.add(exec.jsonMap(productOnClientItem));

		}

		return result;
	}

	public boolean resetLocalbootProducts(String[] selectedClients)
	//hopefully we get only updateItems for allowed clients
	{
		if ( globalReadOnly )
			return false;

		boolean result = true;

		ArrayList<Object> deleteProductItems = new ArrayList<Object>();
		ArrayList<Object> deletePropertyItems = new ArrayList<Object>();

		for (String product: localbootProductNames)
		{
			for (int i = 0; i < selectedClients.length; i++)
			{
				Map<String, Object> productOnClientItem = createNOMitem("ProductOnClient");
				productOnClientItem.put("productType", OpsiPackage.LOCALBOOT_PRODUCT_SERVER_STRING);
				productOnClientItem.put("clientId", selectedClients[i]);
				productOnClientItem.put("productId", product);

				deleteProductItems.add(exec.jsonMap(productOnClientItem));

				Map<String, Object> propertyStateItem = createNOMitem("ProductPropertyState");

				if (getProductproperties(selectedClients[i], product) != null)
				{
					for (String propertyId : getProductproperties(selectedClients[i], product).keySet())
					{
						propertyStateItem.put("objectId", selectedClients[i]);
						propertyStateItem.put("productId", product);
						propertyStateItem.put("propertyId", propertyId);

						deletePropertyItems.add(exec.jsonMap(propertyStateItem));
					}
				}

			}
		}

		//logging.debug(this, "resetLocalbootProducts deleteProductItems " + deleteProductItems);
		logging.debug(this, "resetLocalbootProducts deleteProductItems.size " + deleteProductItems.size());

		if (deleteProductItems.size() > 0)
		{
			logging.injectLogLevel(logging.LEVEL_DONT_SHOW_IT);
			OpsiMethodCall omc = new OpsiMethodCall(
			                         "productOnClient_deleteObjects",
			                         new Object[] {
			                             deleteProductItems.toArray()
			                         }
			                     )
			                     ;

			result = exec.doCall(omc);

			logging.injectLogLevel(null);
		}

		logging.debug(this, "resetLocalbootProducts result " + result);

		logging.debug(this, "resetLocalbootProducts deletePropertyItems.size " + deletePropertyItems.size());
		if (result && deletePropertyItems.size() > 0)
		{
			logging.injectLogLevel(logging.LEVEL_DONT_SHOW_IT);
			OpsiMethodCall omc = new OpsiMethodCall(
			                         "productPropertyState_deleteObjects",
			                         new Object[] {
			                             deletePropertyItems.toArray()
			                         }
			                     )
			                     ;

			result = exec.doCall(omc);

			logging.injectLogLevel(null);
		}

		logging.debug(this, "resetLocalbootProducts result " + result);

		return result;
	}

	/*
	void checkProductInfos ()
{
		if (productInfos == null)
		{
			productInfos = new HashMap();
		}
}
	*/



	public void retrieveProductDependencies()
	{
		dataStub.getDepot2product2dependencyInfos();
	}



	public Map<String, Map<String, Object>> getProductGlobalInfos(String depotId)
	{
		checkProductGlobalInfos(depotId);
		return productGlobalInfos;
	}

	public Map<String, Object> getProductInfos( String productname )
	{
		checkProductGlobalInfos(theDepot);
		return (Map) productGlobalInfos.get(productname);
		/*
		Map infos = getProductInfos();

		if (infos.get(productname) == null) {
			infos.put( productname, new HashMap() );
	}

		return (Map) infos.get(productname);
		*/
	}


	public Map<String, Map<String, String>> getProductDefaultStates()
	{
		if (productIds == null)
			getProductIds();



		logging.debug(this, "getProductDefaultStates, count " + productDefaultStates.size());
		return productDefaultStates;
	}

	@Override
	public  Vector<Vector<Object>> getProductRows()
	{
		return dataStub.getProductRows();
	}

	@Override
	public Map<String, Map<String, java.util.List<String>>> getProduct2VersionInfo2Depots()
	{
		return dataStub.getProduct2VersionInfo2Depots();
	}


	public TreeSet<String> getProductIds()
	{
		dataStub.getProduct2versionInfo2infos();

		if (productIds == null)
		{
			/*
			List<Map<String, Object>> maps  = exec.getListOfMaps(
				new OpsiMethodCall(
					"product_getHashes", new Object[]{}
					)
				);

			productIds = new TreeSet<String>();
			productDefaultStates = new TreeMap<String, Map<String, String>>();

			for (Map<String, Object> map : maps )
		{
				if (map.get("id") != null)
				{
					String product = (String) map.get("id"); 
					productIds.add(product);
					
					ProductState productDefault = new ProductState(null);
					productDefault.put("productId", product);
					productDefaultStates.put(product, productDefault);
				}
				//since we have a set double ids will appear only once
		}

			*/

			productIds = new TreeSet<String>();
			productDefaultStates = new TreeMap<String, Map<String, String>>();

			for (String  productId : dataStub.getProduct2versionInfo2infos().keySet() )
			{
				productIds.add(productId);
				ProductState productDefault = new ProductState(null);
				productDefault.put("productId", productId);
				productDefaultStates.put(productId, productDefault);
			}

			logging.info(this, "getProductIds size / names " + productIds.size() + " / " + productIds);
			
		
		

		}
		/*


		new TreeSet( exec.getListResult (
					new OpsiMethodCall(
						"getProductIds_list", new Object[]{} 
					)
		);
		*/


		/*
		checkProductGlobalInfos(theDepot);
		Map infos = getProductGlobalInfos(theDepot);
		TreeSet result = new TreeSet(infos.keySet());
		*/

		return productIds;
	}


	private List<Map<String, String>> getProductDependencies ( String depotId, String  productId)
	{
		java.util.List<Map<String, String>> result = null;

		if (
		    dataStub.getDepot2product2dependencyInfos().get(depotId) != null
		)
		{
			result = dataStub.getDepot2product2dependencyInfos().get(depotId).get(productId);
		}

		if (result == null)
		{
			result = 	new ArrayList<Map<String, String>>();
		}



		logging.debug(this, "getProductDependencies for depot, product " + depotId + ", " + productId + " , result " + result);
		return  result;
	}


	/*
	private List getProductDependencies ( String  productname)
{
		java.util.List<Map<String, String>> result = dataStub.getDepot2product2dependencyInfos().get(theDepot).get(productname);
		logging.debug(this, "getProductDependencies for product " + productname + " , result " + result);
		return  result;

		
}
	*/


	private boolean generateIfNull3(Map<String, Map<String, Map<String, Object>>> ob)
	{
		if (ob == null)
		{
			ob = new HashMap<String, Map<String,Map<String, Object>>>();
			return true;
		}
		return false;
	}

	private boolean generateIfNull2(Map<String, Map<String, Object>>  ob)
	{
		if (ob == null)
		{
			ob = new HashMap<String,Map<String, Object>>();
			return true;
		}
		return false;
	}

	private boolean generateIfNull1(Map<String, Object>  ob)
	{
		if (ob == null)
		{
			ob = new HashMap<String, Object>();
			return true;
		}
		return false;
	}


	/**
	returns a set which depends on the momentarily selected hosts as specified by a call to 
	retrieveProductProperties
	*/
	protected Set<String> getProductsHavingSpecificProperties()
	{
		return productsHavingSpecificProperties;
	}

	public Boolean hasClientSpecificProperties(String productname)
	{
		return  productHavingClientSpecificProperties.get(productname);
	}

	public Map<String, Boolean> getProductHavingClientSpecificProperties()
	{
		return productHavingClientSpecificProperties;
	}

	List<Map<String, Object>> retrieveListOfMapsNOM(
	    String methodName
	)
	{
		String[] callAttributes = new String[]{};
		HashMap callFilter = new HashMap();
		return
		    retrieveListOfMapsNOM(
		        callAttributes,
		        callFilter,
		        methodName
		    );
	}


	//public in package
	List<Map<String, Object>> retrieveListOfMapsNOM(
	    String[] callAttributes,
	    HashMap callFilter,
	    String methodName
	)
	{
		List<Map<String, Object>> retrieved
		=
		    exec.getListOfMaps(
		        new OpsiMethodCall(
		            methodName,
		            new Object[]{callAttributes, callFilter}
		        )
		    );
		logging.injectLogLevel(logging.LEVEL_DONT_SHOW_IT);
		logging.debug(this, "retrieveListOfMapsNOM " + retrieved);
		logging.injectLogLevel(null);

		return retrieved;
	}


	/**
	Collects the common property values of some product for a client collection,<br \>
	needed for local imaging handling
	<br \>
	@param ArrayList<String> clients -
	@param String product
	@param String property
	*/
	public List<String> getCommonProductPropertyValues(java.util.List<String> clients, String product, String property)
	{
		logging.info(this, "getCommonProductPropertyValues for product, property, clients " + product + ", " + property + "  -- " + clients);
		String[] callAttributes = new String[]{};//"objectId","productId","propertyId", "values"};
		HashMap callFilter = new HashMap();
		callFilter.put("objectId", exec.jsonArray(clients));
		callFilter.put("productId", product);
		callFilter.put("propertyId", property);
		List<Map<String, Object>> properties
		= retrieveListOfMapsNOM(
		      callAttributes,
		      callFilter,
		      "productPropertyState_getObjects"
		  );

		Set<String> resultSet = new HashSet<String>();

		boolean starting = true;

		for (Map<String,Object> map : properties)
		{
			Object client = map.get("objectId");
			Object retrievedValues = ((org.json.JSONArray) map.get("values")).toList();


			//logging.debug(this, "getCommonProductPropertyValues client :: value "
			//	+ client + " :: " + retrievedValues + " , class " + retrievedValues.getClass());

			List<Object> valueList = (java.util.List<Object>) retrievedValues;

			Set<String> values = new HashSet<String>();

			for (int i = 0; i < valueList.size(); i++)
			{
				values.add((String) valueList.get(i));
			}

			if (starting)
			{
				resultSet = values;
				starting = false;
			}
			else
				resultSet.retainAll(values);

			//logging.debug(this, "getCommonProductPropertyValues client :: value "
			//	+ client + " resulting " + resultSet);
		}

		logging.info(this, "getCommonProductPropertyValues "  + resultSet);

		return new ArrayList<String> (resultSet);

	}


	//used by retrieveProductproperties(final Set<String> clientNames)
	/*
	private List<Map<String, Object>> getProductProperties(
		ArrayList<String> clients
		)
{
		logging.info(this, "getProductProperties clients " + clients);
		String[] callAttributes = new String[]{};//"objectId","productId","propertyId", "values"};
		HashMap callFilter = new HashMap();
		callFilter.put("objectId", exec.jsonArray(clients));
		
		return retrieveListOfMapsNOM(
				callAttributes,
				callFilter,
				"productPropertyState_getObjects"
				);
}
	*/


	/**
	This method collects properties for all selected clients and all products,<br \>
	as a sideeffect, it produces the depot specific default values
	<br \>
	@param clientNames -
	*/
	public void retrieveProductproperties(List<String> clientNames)
	{
		retrieveProductproperties(new HashSet<String> (clientNames));
	}


	public Map<String, Map<String, ConfigName2ConfigValue>> getDepot2product2properties()
	{
		retrieveDepotProductProperties();
		return depot2product2properties;
	}


	public  Map<String, ConfigName2ConfigValue> getDefaultProductProperties(String depotId)
	{
		logging.debug(this, "getDefaultProductProperties for depot " + depotId);
		retrieveDepotProductProperties();
		if (depot2product2properties == null)
		{
			logging.error("no product properties ");
			return null;
		}
		else
		{
			//logging.debug(this, "getDefaultProductProperties for depotId " + depotId + ": " 	+ depot2product2properties.get(depotId) );

			if (depot2product2properties.get(depotId) == null)
				//initializing state
				return new HashMap<String, ConfigName2ConfigValue>();

			if (depot2product2properties.get(depotId).size() > 0)
			{
				logging.info(this, "getDefaultProductProperties for depotId " + depotId + " starts with "
				             +
				             new ArrayList<String>(
				                 depot2product2properties.get(depotId).keySet()
				             ).get(0) );
			}

			return depot2product2properties.get(depotId);
		}
	}


	public void retrieveDepotProductProperties()
	{
		if (depot2product2properties != null)
			return;

		logging.info(this, "retrieveDepotProductProperties, build depot2product2properties");

		depot2product2properties = new HashMap<String, Map<String, ConfigName2ConfigValue>>();

		//dataStub.fillProductPropertyStates(getDepots().keySet());

		//depot missing ??

		List<Map<String, Object>> retrieved = dataStub.getProductPropertyDepotStates(hostInfoCollections.getDepots().keySet());

		for (Map<String, Object> map : retrieved)
		{
			String host = (String) map.get(ProductPropertyState.OBJECT_ID);


			if (!hostInfoCollections.getDepots().keySet().contains(host))
			{
				logging.warning(this, "should be a productPropertyState for a depot, but host " + host);
				continue;
			}

			//logging.info(this, "retrieveDepotProductProperties depot " + host);

			Map<String, ConfigName2ConfigValue> productproperties1Host = depot2product2properties.get(host);

			if (productproperties1Host == null)
			{
				productproperties1Host = new HashMap<String, ConfigName2ConfigValue>();
				depot2product2properties.put(host, productproperties1Host);
			}


			ConfigName2ConfigValue properties = productproperties1Host.get(map.get(ProductPropertyState.PRODUCT_ID));
			if ( properties == null )
			{
				properties = new ConfigName2ConfigValue(new HashMap<String, Object>());
				productproperties1Host.put((String) map.get(ProductPropertyState.PRODUCT_ID), properties);
			}

			properties.put((String) map.get(ProductPropertyState.PROPERTY_ID), ( (org.json.JSONArray) map.get(ProductPropertyState.VALUES)).toList());
			properties.getRetrieved().put((String) map.get(ProductPropertyState.PROPERTY_ID), ((org.json.JSONArray) map.get(ProductPropertyState.VALUES)).toList());

			logging.debug(this, "retrieveDepotProductProperties product properties " + map.get(ProductPropertyState.PRODUCT_ID) );// + " :::  " +  properties);
		}

		/*

		depot2product2propertiesSafe = new HashMap<String, Map<String, ConfigName2ConfigValue>>();

		for (String depot : hostInfoCollections.getDepots().keySet())
		{
			depot2product2propertiesSafe.put(depot. depot2product2properties.get(depot));
	}
		*/



		//System.exit(0);
	}



	/**
	This method collects properties for all selected clients and all products,<br \>
	as a sideeffect, it produces the depot specific default values
	<br \>
	@param clientNames -
	*/
	public void retrieveProductproperties(final Set<String> clientNames)
	{

		/*
		boolean existing 
			= (
				productproperties != null 
				&& clientNames.removeAll(productproperties.keySet()).size() == 0
			)
		its clearer with a manual search:
		*/

		boolean existing = true;


		//logging.info(this, "retrieveProductproperties productproperties ");// + productproperties);

		if (productproperties == null)
			existing = false;
		else
		{
			for (String client : clientNames)
			{
				if (productproperties.get(client) == null)
				{
					existing = false;
					break;
				}
			}
		}

		//logging.info(this, "retrieveProductproperties for hosts " + clientNames + "  existing " + existing);

		if (existing)
			return;

		//dataStub.productPropertyStatesRequestRefresh();

		//clientNames.add(theDepot);
		//we get the default values from the depot, but in newer version we have the depot values already



		/*
		String[] callAttributes = new String[]{};//"objectId","productId","propertyId", "values"};
		HashMap callFilter = new HashMap();
		callFilter.put("objectId", exec.jsonArray(new ArrayList(clientNames)));

		List<Map<String, Object>> retrieved
		= retrieveListOfMapsNOM(
			callAttributes,
			callFilter,
			"productPropertyState_getObjects"
		);
		*/



		//String testhost = "";

		productproperties = new HashMap<String, Map <String,  ConfigName2ConfigValue>>();
		Map<String, Map<String, Map <String, Object>>> productproperties_retrieved = new HashMap<String, Map<String, Map <String, Object>>>();

		dataStub.fillProductPropertyStates(clientNames);
		List<Map<String, Object>> retrieved = dataStub.getProductPropertyStates();

		Set<String> productsWithProductPropertyStates = new HashSet<String>();


		for (Map<String, Object> map : retrieved)
		{
			String host = (String) map.get("objectId");

			productsWithProductPropertyStates.add( (String) map.get("productId") );

			Map<String, Map<String, Object>> productproperties1Client = productproperties_retrieved.get(host);


			if (productproperties1Client == null)
			{
				productproperties1Client = new HashMap<String, Map<String, Object>>();
				productproperties_retrieved.put(host, productproperties1Client);
			}

			Map<String, Object> properties = productproperties1Client.get(map.get("productId"));
			if ( properties == null )
			{
				properties = new HashMap<String, Object>();
				productproperties1Client.put((String) map.get("productId"), properties);
			}


			properties.put((String) map.get("propertyId"), ((org.json.JSONArray) map.get("values")).toList());

			//logging.info(this, " retrieveProductproperties for product " +  map.get("productId") + ":  " + properties);

		}

		logging.info(this, " retrieveProductproperties  productsWithProductPropertyStates " + productsWithProductPropertyStates);


		//logging.debug(this, " retrieveProductproperties B productproperties_retrieved " + productproperties_retrieved);

		//havingProductProperties = new TreeSet<String>(productproperties_retrieved.keySet());


		Map<String, ConfigName2ConfigValue> defaultProperties  = getDefaultProductProperties(theDepot);
		Map<String, Map<String, Object>> defaultProperties_retrieved =  new HashMap<String, Map<String, Object>>();
		if (defaultProperties == null)
		{
			//should not occur
		}
		else
		{
			for (String depot : defaultProperties.keySet())
			{
				defaultProperties_retrieved.put(depot, defaultProperties.get(depot));
			}
		}

		Set<String> products = defaultProperties_retrieved.keySet();

		/*
		Map<String, Map<String, Object>> defaultProperties_retrieved  = null;
		Set<String> products  = null;

		if (productproperties_retrieved.containsKey(
		
))
		{
			defaultProperties_retrieved = productproperties_retrieved.get(theDepot);
			products = defaultProperties_retrieved.keySet();
	}
		else
		{
			defaultProperties_retrieved  = new HashMap<String, Map<String, Object>>();
			products = new HashSet<String>();
	}
		*/

		productsHavingSpecificProperties = new TreeSet<String>(products);

		for (String host: clientNames)
		{
			HashMap<String, ConfigName2ConfigValue> productproperties1Client = new HashMap<String, ConfigName2ConfigValue>();
			productproperties.put(host, productproperties1Client);

			Map<String, Map<String, Object>> retrievedProperties = productproperties_retrieved.get(host);
			if (retrievedProperties == null)
			{
				retrievedProperties = defaultProperties_retrieved;
				productsHavingSpecificProperties.clear();
			}

			for (String product : products)
			{
				Map<String, Object> retrievedProperties1Product = retrievedProperties.get(product);
				Map<String, Object> properties1Product = new HashMap<String, Object>(defaultProperties_retrieved.get(product)); //complete set of default values


				if (retrievedProperties1Product == null)
				{
					productsHavingSpecificProperties.remove(product);
				}
				else
				{
					for (String property: retrievedProperties1Product.keySet())
					{
						properties1Product.put(property, retrievedProperties1Product.get(property));
					}
				}

				ConfigName2ConfigValue state = new ConfigName2ConfigValue(properties1Product, null);
				productproperties1Client.put(product, state);
			}
		}

		logging.info(this, " retrieveProductproperties productsHavingSpecificProperties " +productsHavingSpecificProperties );

		Map<String, ConfigName2ConfigValue> depotValues =  getDefaultProductProperties(theDepot);
		//productproperties.get(theDepot);
		//logging.debug(this, "retrieveProductproperties " + productproperties);
		//logging.info(this, "retrieveProductproperties, depotValues " + depotValues);
		//logging.info(this, "retrieveProductproperties, depotValues ============= ");

		for (String product : products)
		{
			if (productPropertyDefinitions != null && productPropertyDefinitions.get(product)!= null)
			{
				ConfigName2ConfigValue productPropertyConfig = (ConfigName2ConfigValue) depotValues.get(product);
				//logging.info(this, "retrieveProductproperties, depotvalues for product "  + product + " :  " + productPropertyConfig);

				Iterator iterProperties = productPropertyDefinitions.get(product).keySet().iterator();
				while (iterProperties.hasNext())
				{
					String property = (String) iterProperties.next();

					if (productPropertyConfig == null ||  productPropertyConfig.get(property) == null)
					{
						((ListCellOptions) (productPropertyDefinitions.get(product).get(property))).setDefaultValues(
						    new ArrayList());
					}
					else
					{
						((ListCellOptions) (productPropertyDefinitions.get(product).get(property))).setDefaultValues(
						    (java.util.List) productPropertyConfig.get(property)
						);
					}
				}
			}
		}

		productHavingClientSpecificProperties = new HashMap<String, Boolean>();
		for (String product : products)
		{
			productHavingClientSpecificProperties.put(product,
			        (Boolean) productsHavingSpecificProperties.contains(product)
			                                         );
		}



		/*
		logging.injectLogLevel(5);
		logging.debug(this, "retrieveProductproperties, specified default values in productPropertyDefinitions: " 
			+ productPropertyDefinitions);
		logging.injectLogLevel(null);
		*/
	}




	/**
	<br>
	@param pcname - if it changes productproperties should have been set to null.
	@param  productname   
	*/
	public Map<String, Object>  getProductproperties (String pcname, String  productname)
	{
		logging.debug(this, "getProductProperties for product, host " + productname + ", " + pcname);
		/*
		if (!checkProductproperties(pcname, productname))
		{
			//should not be, but lets try again:
			logging.debug("getProductproperties with additional client " + pcname + " for " + productname);
			Set<String> pcs = new TreeSet<String>();
			pcs.add(pcname);
			retrieveProductproperties(pcs);
			checkProductproperties(pcname, productname);
	}
		*/


		Set<String> pcs = new TreeSet<String>();
		pcs.add(pcname);
		retrieveProductproperties(pcs);

		if (
		    productproperties.get(pcname) == null
		    ||
		    productproperties.get(pcname).get(productname) == null
		)
			return new HashMap<String, Object>();

		logging.info(this, "getProductProperties for product, host " + productname + ", " + pcname);
		// + productproperties);

		return productproperties.get(pcname).get(productname);
	}


	//collect productPropertyState updates and deletions
	public void setProductproperties(String pcname, String productname, Map properties,
	                                 java.util.List updateCollection, java.util.List deleteCollection)
	{
		if (! (properties instanceof de.uib.configed.type.ConfigName2ConfigValue) )
		{
			logging.warning(this, "! properties instanceof de.uib.configed.type.ConfigName2ConfigValue ");
			return;
		}

		Iterator propertiesKeyIterator = properties.keySet().iterator();

		Map state = new HashMap();

		while (propertiesKeyIterator.hasNext())
		{
			String key = (String) propertiesKeyIterator.next();


			state.put("type","ProductPropertyState");
			state.put("objectId", pcname);
			state.put("productId", productname);
			state.put("propertyId", key);

			java.util.List newValue = (java.util.List) properties.get(key);



			//logging.debug(this, "setAdditionalConfiguration,  properties.get(key) " + properties.get(key));

			Map retrievedConfig = ((RetrievedMap) properties).getRetrieved();
			Object oldValue = null;

			if (retrievedConfig != null)
				oldValue = retrievedConfig.get(key);


			//logging.debug(this, "setProductProperties,  " +
			//	" ((RetrievedMap) properties).getRetrieved().get..., + key  " + key  + ": " +
			//	 oldValue);

			if ( newValue !=  oldValue )
			{
				if (newValue == de.uib.utilities.datapanel.MapTableModel.nullLIST)
				{
					logging.debug(this, "setProductProperties,  requested deletion " + (java.util.List) properties.get(key));
					deleteCollection.add(exec.jsonMap(state));

					//we hope that the update works and directly update the retrievedConfig
					if (retrievedConfig != null)
						retrievedConfig.remove(key);
				}

				else
				{
					state.put("values",  exec.jsonArray( newValue ) );
					logging.debug(this, "setProductProperties,  requested update " + (java.util.List) properties.get(key)
					              + " for oldValue " + oldValue);
					logging.debug(this, "setProductProperties,  we have new state " + state);
					updateCollection.add(exec.jsonMap(state));

					//we hope that the update works and directly update the retrievedConfig
					if (retrievedConfig != null)
						retrievedConfig.put(key,  (java.util.List) properties.get(key));
				}

			}

			/* postponed to setProductProperties()
			exec.doCall (new OpsiMethodCall (
				"configState_updateObjects", new Object[] {exec.jsonArray(productPropertyStateUpdateCollection) }
				)
			);
			*/
		}

	}

	//collect productPropertyState updates and deletions in standard lists
	public void setProductproperties(String pcname, String productname, Map properties)
	{
		//old version
		//logging.info(this, "setProductproperties (not called), productname " + productname + ", properties " + properties );
		//exec.doCall (new OpsiMethodCall ("setProductProperties",  params));


		if (productPropertyStateUpdateCollection == null)
			productPropertyStateUpdateCollection = new ArrayList();

		if (productPropertyStateDeleteCollection == null)
			productPropertyStateDeleteCollection = new ArrayList();


		setProductproperties(pcname, productname, properties,
		                     productPropertyStateUpdateCollection,
		                     productPropertyStateDeleteCollection
		                    );

	}


	//send productPropertyState updates and clear the collections for standard collections
	public  void setProductproperties()
	{
		setProductproperties(
		    productPropertyStateUpdateCollection,
		    productPropertyStateDeleteCollection
		);
	}


	//send productPropertyState updates and clear the collections
	public  void setProductproperties(
	    java.util.List updateCollection,
	    java.util.List deleteCollection
	)
	{
		logging.debug(this, "setProductproperties() ");

		if ( globalReadOnly )
			return;

		//,  updateCollection: " + updateCollection );

		if (updateCollection != null && updateCollection.size() > 0)
		{
			if (
			    exec.doCall (new OpsiMethodCall (
			                     "productPropertyState_updateObjects",
			                     new Object[]{
			                         exec.jsonArray(updateCollection)
			                     }
			                 )
			                )
			)
			{
				updateCollection.clear();
			}
		}

		if (deleteCollection != null && deleteCollection.size() > 0)
		{
			if (
			    exec.doCall (new OpsiMethodCall (
			                     "productPropertyState_deleteObjects",
			                     new Object[]{
			                         exec.jsonArray(deleteCollection)
			                     }
			                 )
			                )
			)
			{
				deleteCollection.clear();
			}
		}
	}


	public void setCommonProductPropertyValue(
	    Set<String> clientNames, String productName, String propertyName,
	    java.util.List<String> values
	)
	{
		java.util.List updateCollection = new ArrayList();
		java.util.List deleteCollection = new ArrayList();

		//collect updates for all clients
		for (String client : clientNames)
		{
			Map newdata = new de.uib.configed.type.ConfigName2ConfigValue(null);

			newdata.put(propertyName, values);

			//collect the updates
			setProductproperties(
			    client,
			    productName,
			    newdata,
			    updateCollection, deleteCollection);
		}
		//execute updates
		setProductproperties(updateCollection, deleteCollection);
	}


	/*
	public Map getProductPropertyValuesMap (String productname)
	//not used in opsi 4
{
		retrieveProductPropertyDefinitions();
		//System.out.println (" --------  values for " + productname);
		//System.out.println (" --------  values map is  " + (Map) productPropertyValues.get(productname));

		return null;// (Map) productPropertyValues.get(productname);
}


	public Map getProductPropertyDefaultsMap (String productname)
	//not used in opsi 4
{
		retrieveProductPropertyDefinitions();

		return null; //(Map) productPropertyDefaults.get(productname);
}


	public Map getProductPropertyDescriptionsMap (String productname)
	//not used in opsi 4
{
		retrieveProductPropertyDefinitions();
		return null;//(Map) productPropertyDescriptions.get(productname);
}
	*/

	public  Map<String, ListCellOptions> getProductPropertyOptionsMap(
	    String depotId, String productId)
	{

		Map<String, ListCellOptions> result = null;

		if (
		    dataStub.getDepot2Product2PropertyDefinitions().get(depotId) == null
		)
		{
			result = new HashMap<String, ListCellOptions>();
			logging.info("getProductPropertyOptionsMap: no productproperty definitions for depot " + depotId);
		}
		else
			result = dataStub.getDepot2Product2PropertyDefinitions().get(depotId).get(productId);

		if (result == null)
		{
			logging.info("getProductPropertyOptionsMap: no productproperty definitions  for depot, product " + depotId  + ", " + productId);
			result = new HashMap<String, ListCellOptions>();
		}

		return result;

	}

	public  Map<String, ListCellOptions> getProductPropertyOptionsMap(String productId)
	{
		retrieveProductPropertyDefinitions();
		Map<String, ListCellOptions> result;
		//logging.debug(this, "getProductPropertyOptionsMap, productId " + productId + ": " +  productPropertyDefinitions.get(productId));
		if (productPropertyDefinitions == null)
			result = new HashMap<String, ListCellOptions>();
		else
		{
			result = productPropertyDefinitions.get(productId);
			if (result == null)
				result = new HashMap<String, ListCellOptions>();
		}

		return result;
	}


	public void productPropertyDefinitionsRequestRefresh()
	{
		dataStub.productPropertyDefinitionsRequestRefresh();
		productPropertyDefinitions = null;
	}

	public void retrieveProductPropertyDefinitions()
	{
		//dataStub.retrieveAllProductPropertyDefinitions();

		productPropertyDefinitions = dataStub.getDepot2Product2PropertyDefinitions().get(theDepot);



	}

	public String getProductTitle(String product)
	{
		//checkProductGlobalInfos(theDepot);
		//System.out.println ("productinfo " + getProductInfos(product).get ("opsiProductName") );
		logging.info(this, "getProductTitle for product " + product + " on depot " + theDepot);
		logging.info(this, "getProductTitle for productGlobalsInfos found number " + productGlobalInfos.size() );
		logging.info(this, "getProductTitle, productInfos " +  productGlobalInfos.get(product));
		Object result = productGlobalInfos.get(product).get(ProductState.KEY_productName);
		logging.info(this, "getProductTitle for product " + result);

		/*
		String result = exec.getStringValueFromItem ( ((Map) getProductInfos(product)).get (
		                    //"name"
		                    de.uib.opsidatamodel.productstate.ProductState.KEY_productName
		                ));
		*/
		String resultS = null;
		if (result == null)
			resultS = EMPTYFIELD;
		else
			resultS = "" + result;
		return resultS;
	}

	public String getProductInfo(String product)
	{
		//checkProductGlobalInfos(theDepot);
		String result = "" + productGlobalInfos.get(product).get(OpsiProductInfo.SERVICEkeyPRODUCT_DESCRIPTION);
		logging.debug(this, " getProductInfo for product " + product + ": " + result);

		return result;

		/*
		//System.out.println ("productinfo " + getProductInfos(product).get ("description"));
		String result = exec.getStringValueFromItem ( ((Map) getProductInfos(product)).get("description") ); 
		if (result == null)
			result = EMPTYFIELD;
		return result;
		*/
	}

	public String getProductHint(String product)
	{
		//checkProductGlobalInfos(theDepot);
		//logging.info(this, "getProductHint product " + product + ": " + productGlobalInfos.get(product));
		return (String) productGlobalInfos.get(product).get(OpsiProductInfo.SERVICEkeyPRODUCT_ADVICE);

		/*
		//System.out.println ("productinfo " + getProductInfos(product).get ("opsiProductAdvice"));
		String result = exec.getStringValueFromItem ( ((Map) getProductInfos(product)).get ("advice") );

		if (result == null)
			result = EMPTYFIELD;
		return result;
		*/


	}

	public String getProductVersion(String product)
	{
		//checkProductGlobalInfos(theDepot);
		productGlobalInfos.get(product).get("productVersion");



		//System.out.println ("##########  getProductInfos of product " + product);
		String result = exec.getStringValueFromItem ( ((Map) getProductInfos(product)).get ("productVersion") );

		if (result == null)
			result = EMPTYFIELD;


		logging.debug(this, "getProductVersion which? "
		              + productGlobalInfos.get(product).get("productVersion")
		              + " //or//   " + result);

		return result;
	}

	public String getProductPackageVersion(String product)
	{
		//checkProductGlobalInfos(theDepot);
		return (String) productGlobalInfos.get(product).get("packageVersion");
		/*
		String result = exec.getStringValueFromItem ( ((Map) getProductInfos(product)).get ("packageVersion") );

		if (result == null)
			result = EMPTYFIELD;
		return result;
		*/
	}



	public String getProductTimestamp(String product)
	{

		//String result = exec.getStringValueFromItem ( ((Map) getProductInfos(product)).get ("creationTimestamp") );
		String result = null;
		if (result == null)
			result = EMPTYFIELD;
		return result;
	}



	private  Map<String, String> getProductRequirements ( String depotId, String productname, String requirementType)
	{
		Map<String, String>  result = new HashMap<String, String>();
		//checkProductDependencies();

		String depot = null;
		if (depotId == null)
			depot = theDepot;
		else
			depot = depotId;


		logging.debug(this, "getProductRequirements productname, requirementType  " + productname + ", " + requirementType);

		java.util.List<Map<String, String>> dependenciesFor1product
		=
		    getProductDependencies(depot, productname);
		//dataStub.getDepot2product2dependencyInfos().get(theDepot).get(productname);

		//List dependenciesFor1product = getProductDependencies(productname);
		//for (int i = 0; i < dependenciesFor1product.size(); i++)
		if (dependenciesFor1product == null)
			return result;

		for( Map<String, String> aDependency : dependenciesFor1product)
		{
			//Object aDependencyObject = dependenciesFor1product.get(i);
			//Map aDependency = exec.getMapFromItem(aDependencyObject );


			logging.debug(this, " dependency map : " + aDependency);

			if (
			    requirementType.equals (nameRequirementTypeOnDeinstall)
			    //we demand information for this type,
			    //this is not specified by type in the dependency map
			    //but only by the action value
			    &&
			    ( (String)  (aDependency.get("action") ))  .equals(ActionRequest.getLabel(ActionRequest.UNINSTALL))

			)

			{
				result.put(	(String) (aDependency.get("requiredProductId")),
				            (String) (aDependency.get("requiredInstallationStatus")) + ":"
				            + (String) (aDependency.get("requiredAction"))
				          );
			}


			else
			{

				logging.debug(this, " dependency map : " );

				if (

				    (
				        requirementType.equals (nameRequirementTypeNeutral)
				        ||requirementType.equals (nameRequirementTypeBefore)
				        ||requirementType.equals (nameRequirementTypeAfter)
				    )

				    &&
				    (
				        ((String)  (aDependency.get("action") ))  .equals(ActionRequest.getLabel(ActionRequest.SETUP))
				        ||
				        ((String)  (aDependency.get("action") ))  .equals(ActionRequest.getLabel(ActionRequest.ONCE))
				        ||
				        ((String)  (aDependency.get("action") ))  .equals(ActionRequest.getLabel(ActionRequest.ALWAYS))
				        ||
				        ((String)  (aDependency.get("action") ))  .equals(ActionRequest.getLabel(ActionRequest.CUSTOM))
				    )

				    &&

				    ((String)  (aDependency.get("requirementType") ))  .equals (requirementType)


				)
				{
					result.put(	(String) (aDependency.get("requiredProductId")),
					            (String) (aDependency.get("requiredInstallationStatus")) + ":"
					            + (String) (aDependency.get("requiredAction"))
					          );
				}
			}

		}

		logging.debug(this, "getProductRequirements " + result);

		return result;
	}



	public Map<String, String> getProductPreRequirements( String depotId, String productname )
	{
		return getProductRequirements (depotId, productname, nameRequirementTypeBefore);
	}

	public Map<String, String> getProductRequirements(  String depotId,  String productname )
	{
		return getProductRequirements (depotId, productname, nameRequirementTypeNeutral);
	}

	public Map<String, String> getProductPostRequirements(  String depotId, String productname )
	{
		return getProductRequirements (depotId, productname, nameRequirementTypeAfter);
	}

	public Map<String, String> getProductDeinstallRequirements(  String depotId, String productname )
	{
		return getProductRequirements (depotId, productname, nameRequirementTypeOnDeinstall);
	}


	public void productpropertiesRequestRefresh()
	{
		dataStub.productPropertyStatesRequestRefresh();
		productproperties = null;
	}

	/*
	public void mapOfMethodSignaturesRequestRefresh()
{
		mapOfMethodSignatures = null;
}
	*/

	@Override
	public List getMethodSignature(String methodname)
	//lazy initializing
	{
		if (mapOfMethodSignatures == null)
		{
			List methodsList = exec.getListResult (
			                       new OpsiMethodCall(
			                           //"extend/configed",
			                           "getPossibleMethods_listOfHashes",
			                           new Object[]{  } )
			                   );

			if (methodsList != null)
			{
				mapOfMethodSignatures = new HashMap();

				Iterator iter = methodsList.iterator();
				while (iter.hasNext())
				{
					Map listEntry = exec.getMapFromItem(iter.next());

					String name = (String) listEntry.get("name");
					List signature = exec.getListFromItem(listEntry.get("params").toString()); // should never result to null
					for (int i=0; i<signature.size(); i++)
					{
						if ( ((String)signature.get(i)).charAt(0) == '*')
						{
							signature.set(i, ((String)signature.get(i)).substring(1));
						}
					}
					mapOfMethodSignatures.put (name, signature);
				}
			}
		}


		if (mapOfMethodSignatures.get(methodname) == null)
			return NONE_LIST;


		return (List) mapOfMethodSignatures.get(methodname);
	}


	public String getBackendInfos()
	{
		String bgColor0 = "#dedeff";
		String bgColor1 = "#ffffff";
		String bgColor ="";

		String titleSize = "14px";
		String fontSizeBig = "10px";
		String fontSizeSmall = "8px";
		// are not evaluated at this moment


		OpsiMethodCall omc = new OpsiMethodCall(
		                         "getBackendInfos_listOfHashes",
		                         new String[]{  } );

		List list = exec.getListResult ( omc );

		StringBuffer buf = new StringBuffer("");

		HashMap backends = new HashMap();


		for (int i = 0; i < list.size(); i++)
		{
			Map listEntry = exec.getMapFromItem(list.get(i));


			String backendName = "UNKNOWN";

			if (listEntry.containsKey("name"))
			{
				backendName = (String)listEntry.get("name");
			}

			if ( !backends.containsKey(backendName) )
			{
				backends.put(backendName, new ArrayList());
			}

			((ArrayList) backends.get(backendName)).add(listEntry);
		}

		buf.append ("<table border='0' cellspacing='0' cellpadding='0'>\n");

		Iterator backendIterator = backends.keySet().iterator();
		while (backendIterator.hasNext())
		{
			String backendName = (String) backendIterator.next();

			buf.append ("<tr><td bgcolor='#fbeca5' color='#000000'  width='100%'  colspan='3'  align='left'>");
			buf.append ("<font size='" + titleSize + "'><b>" + backendName + "</b></font></td></tr>");

			ArrayList backendEntries = (ArrayList) backends.get(backendName);

			for (int i = 0; i < backendEntries.size(); i++)
			{
				Map listEntry = (Map) backendEntries.get(i);

				Iterator eIt = listEntry.keySet().iterator();

				boolean entryIsEven = false;

				while (eIt.hasNext())
				{
					String key = (String) eIt.next();
					if ( key.equals("name"))
						continue;

					entryIsEven = !entryIsEven;
					if (entryIsEven)  bgColor = bgColor0; else  bgColor = bgColor1;

					Object value = listEntry.get(key);
					buf.append ("<tr height='8px'>");
					buf.append ("<td width='200px'  bgcolor='" + bgColor + "' align='left' valign='top'><font size='" + fontSizeBig + "'>" + key + "</font></td>");

					if (key.equals("config"))
					{
						buf.append ("<td colspan='2'  bgcolor='" + bgColor + "' align='left' valign='top'><font size='" + fontSizeBig + "'>&nbsp;</font></td>");
						buf.append ("</tr>");

						//System.out.println ("---------- get map from item " + value);
						Map configItems = exec.getMapFromItem(value);


						if (configItems == null) //does not occur since getMapFromItem produces at least an empty map
						{
							logging.debug(this, "------------------ key " + key + "  config  null");
						}
						else
						{
							Iterator configItemsIterator = configItems.keySet().iterator();

							while (configItemsIterator.hasNext())
							{
								String configKey = (String) configItemsIterator.next();

								Object jO  = configItems.get(configKey);

								String configVal = "";

								try{
									configVal = jO.toString();
								}
								catch (Exception jsonEx){
									logging.debug(this,  jsonEx.toString());
								}
								buf.append ("<td bgcolor='" + bgColor + "'>&nbsp;</td>");
								buf.append ("<td width='200px'  bgcolor='" + bgColor + "' align='left' valign='top'><font size='" + fontSizeSmall + "'>" + configKey + "</font></td>");
								buf.append ("<td width='200px'  bgcolor='" + bgColor + "' align='left' valign='top'><font size='" + fontSizeSmall + "'>" + configVal + "</font></td>");
								buf.append ("</tr>");
							}
						}

					}
					else
					{
						buf.append ("<td width='300px'  bgcolor='" + bgColor + "' align='left' valign='top'><font size='" + fontSizeBig + "'>" + value + "</font></td>");
						buf.append ("</tr>");
					}
				}
				buf.append ("<tr height='10px'><td bgcolor='" + bgColor + "' colspan='3'></td></tr>");
			}

			buf.append ("<tr><td bgcolor='#ffffff' color='#000000' width='100%' height='30px' colspan='3'>&nbsp;</td></tr>");
		}

		buf.append ("</table>\n");

		return buf.toString();

	}


	/* network and additional settings, for network objects */

	/*
	public List getServers()
{
		List res = exec.getListResult(
		               new OpsiMethodCall(
		                   "getServerIds_list",
		                   new String[]{})
		           );
		logging.debug(this,"getServers: " + res);
		return res;
}
	*/

	/*
	public  Map getNetworkConfiguration (String objectId)
{
		return exec.getMapResult(
		           new OpsiMethodCall(
		               "getNetworkConfig_hash",
		               new String[]{
		                   objectId}));
}
	*/



	public  Map<String, ListCellOptions> getConfigOptions()
	{
		//logging.info(this, "getConfigOptions() start");


		if (configListCellOptions == null || configOptions == null || configDefaultValues == null)
		{
			logging.debug(this, "getConfigOptions() work");

			boolean tryIt = true;

			int tryOnceMoreCounter = 0;
			final int stopRepeatingAtThis = 1;

			while ( tryIt )
			{
				tryIt = false;
				tryOnceMoreCounter++;

				configOptions = new HashMap<String, ConfigOption>();
				configListCellOptions = new HashMap<String, ListCellOptions>();
				configDefaultValues = new HashMap<String, java.util.List<Object>>();

				remoteControls = new RemoteControls();
				savedSearches = new SavedSearches();


				List<Map<String, Object>> retrievedList
				= retrieveListOfMapsNOM("config_getObjects");


				logging.info(this, "configOptions retrieved "); //: " + retrieved);


				for (Map<String, Object> configItem : retrievedList)
				{
					//map to java type
					for (String  var : configItem.keySet())
					{
						if (configItem.get(var) instanceof org.json.JSONArray)
						{
							configItem.put(var, ((org.json.JSONArray)configItem.get(var)).toList());
						}
					}

					String key = (String) configItem.get("ident");

					//build a ConfigOption from the retrieved item
					ConfigOption configOption = new ConfigOption( configItem ) ;

					configOptions.put(key, configOption);


					configListCellOptions.put(key, (ListCellOptions) configOption);

					if (configOption.getDefaultValues() == null)
					{
						logging.warning(this, "default values missing for config  " + key );

						if (tryOnceMoreCounter <= stopRepeatingAtThis)
						{
							tryIt = true;
							logging.warning(this, "repeat loading the values , we repeated  " + tryOnceMoreCounter + " times");

							try{
								Thread.sleep(1000);
							}
							catch (InterruptedException iex)
							{}
							break;
						}
					}


					configDefaultValues.put(key, configOption.getDefaultValues());

					if (configOption.getDefaultValues() != null
					        && configOption.getDefaultValues().size() > 0)
					{
						remoteControls.checkIn(key, "" + configOption.getDefaultValues().get(0));
						savedSearches.checkIn(key, "" + configOption.getDefaultValues().get(0));
					}

				}
			}

			logging.debug(this, "getConfigOptions() work finished");
		}

		//logging.info(this, "getConfigOptions() ready ");
		//+ configListCellOptions);

		return (Map<String, ListCellOptions>) configListCellOptions;

	}

	/*
	public  Map<String, ListCellOptions> getConfigOptions()
{
		if (configListCellOptions == null || configOptions == null || configDefaultValues == null)
		{
			configOptions = new HashMap<String, ConfigOption>();
			configListCellOptions = new HashMap<String, ListCellOptions>();
			configDefaultValues = new HashMap<String, java.util.List>();

			remoteControls = new RemoteControls();
			savedSearches = new SavedSearches();

			Map<String, Map<String, Object>> retrieved =  exec.getMap2_Object(
			            new OpsiMethodCall(
			                //"extend/configed",
			                "getConfigOptions_hash",
			                new String[]{}
			            )
			        );

			if (retrieved == nullconfig				return null;


			logging.info(this, "configOptions retrieved "); //: " + retrieved);

			Iterator iter = retrieved.keySet().iterator();

			while (iter.hasNext())
			{
				String key = (String) iter.next();
				ConfigOption configOption = new ConfigOption( retrieved.get(key) ) ;
				//logging.debug(this, "key " + key + " configOption : " + configOption);
				configOptions.put(key, configOption);
				configListCellOptions.put(key, (ListCellOptions) configOption);
				configDefaultValues.put(key, configOption.getDefaultValues());

				if (configOption.getDefaultValues() != null
				        && configOption.getDefaultValues().size() > 0)
				{
					remoteControls.checkIn(key, "" + configOption.getDefaultValues().get(0));
					savedSearches.checkIn(key, "" + configOption.getDefaultValues().get(0));
				}				
			
			}
		}

		logging.info(this, "getConfigOptions() ready ");
		//+ configListCellOptions);

		return (Map<String, ListCellOptions>) configListCellOptions;

}
	*/

	private Object getLocalbootDisplayFieldsConfig()
	{
		//getConfigs();
		return null;
	}

	public Map<String, RemoteControl> getRemoteControls()
	{
		getConfigOptions();
		return remoteControls;
	}

	public SavedSearches getSavedSearches()
	{
		getConfigOptions();
		return savedSearches;
	}

	public Map<String, java.util.List<Object>> getConfigDefaultValues()
	{
		getConfigOptions();
		return configDefaultValues;
	}


	public void configOptionsRequestRefresh()
	{
		configOptions = null;
	}

	public void hostConfigsRequestRefresh()
	{
		dataStub.hostConfigsRequestRefresh();
	}

	/*
	public void hostConfigsRequestRefresh(String[] clients)
{
}

	public void hostConfigsCheck(String[] clients)
{
		//dataStub.hostConfigsRequestRefresh(someClients);
}
	*/


	public Map<String, Map<String, Object>> getConfigs()
	{
		return dataStub.getConfigs();
	}

	/*
	public Map<String, Map<String, Object>> getConfigs(String[] objectIds)
{
		getConfigOptions();

		//if (mapOfMethodSignatures.containsKey("getConfigs"))
		{
			hostConfigs =  exec.getMap2_Object(
			                   new OpsiMethodCall(
			                       //"extend/configed",
			                       "getConfigs",
			                       new Object[]{objectIds}
			                   )
			               );
		}
		logging.debug(this, "getConfigs: " + hostConfigs);
		return hostConfigs;
}
	*/


	public Map<String, Object> getConfig(String objectId)
	{
		getConfigOptions();

		Map<String, Object> retrieved = dataStub.getConfigs().get(objectId);
		/*

		Map<String, Object> retrieved = null;
		//logging.debug(this, "hostConfigs " + hostConfigs);
		if (hostConfigs == null || hostConfigs.isEmpty())
		{
			//compatibility
			retrieved 	= exec.getMap_Object(
			                 new OpsiMethodCall(
			                     //"extend/configed",
			                     "getConfig_hash",
			                     new String[]{objectId}
			                 )
			             );
	}
		else
			retrieved = hostConfigs.get(objectId);

		logging.debug(this, "retrieved: " + retrieved);
		logging.debug(this, "hostConfigs: " + hostConfigs);
		*/

		return new ConfigName2ConfigValue(retrieved, configOptions);
	}


	/*
	public  Map getAdditionalConfiguration (String objectId)
{
		return exec.getMapResult(
		           new OpsiMethodCall(
		               "getGeneralConfig_hash",
		               new String[]{
		                   objectId}));
}
	*/

	/*
	public  void setNetworkConfiguration (String objectId, Map settings)
	//not needed in opsi4.0
{
		//System.out.println ("--- persistence controller shall realize the network configuration update ");


		Object[] params = new Object[]   {exec.jsonMap(settings), objectId};
		exec.doCall (new OpsiMethodCall ("setNetworkConfig",  params));
}
	*/

	private Map<String, Object> createNOMitem (String type)
	{
		Map<String, Object> item = new HashMap<String, Object>();
		item.put("type", type);

		return item;
	}



	private Map<String, Object> createBoolConfig( String key, Boolean value, String description)
	{
		Map<String, Object> item = createNOMitem( ConfigOption.BOOL_TYPE );

		item.put("ident", key);
		item.put("description", description);
		item.put("editable", false);
		item.put("multiValue", false);

		java.util.List<Object> possibleValues = new ArrayList<Object>();
		possibleValues.add(true);
		possibleValues.add(false);
		item.put("possibleValues", exec.jsonArray(possibleValues));

		//defaultValues
		java.util.List<Object> defaultValues = new ArrayList<Object> ();
		defaultValues.add(value);
		item.put("defaultValues", exec.jsonArray(defaultValues));

		return item;
	}



	public void setHostValues(Map settings)
	{
		if ( globalReadOnly )
			return ;


		List hostMaps = new ArrayList();


		Map corrected = new HashMap();
		for (Object key : settings.keySet())
		{
			if (settings.get(key) instanceof String && ((String)settings.get(key)).trim().equals(
			            de.uib.opsicommand.JSONReMapper.NullRepresenter
			        ))
				corrected.put(key, org.json.JSONObject.NULL);
			else
				corrected.put(key, settings.get(key));
		}

		hostMaps.add(exec.jsonMap(corrected));



		exec.doCall(
		    new OpsiMethodCall(
		        "host_createObjects",
		        new Object[]{
		            exec.jsonArray(hostMaps)
		        }
		    )
		);
	}


	//collect config state updates
	public  void setAdditionalConfiguration (String objectId, ConfigName2ConfigValue settings)
	{
		if (configStateCollection == null)
			configStateCollection = new ArrayList();

		Set<String> currentKeys = settings.keySet();
		logging.info(this, "setAdditionalConfigurations current keySet size: " + currentKeys.size() );
		if ( settings.getRetrieved() != null )
		{
			Set<String> retrievedKeys = settings.getRetrieved().keySet();

			logging.info(this, "setAdditionalConfigurations retrieved keys size  " + retrievedKeys.size());
			
			/*
			for (String key : ((RetrievedMap)settings).keySet())
			{
				logging.info(this, "setAdditionalConfiguration key, value " + key + ", " + settings.get(key));
			}
			*/

			Set removedKeys = new HashSet(retrievedKeys);
			removedKeys.removeAll(currentKeys);
			logging.info(this, "setAdditionalConfigurations removed " + removedKeys);

			if (removedKeys.size() > 0)
			{
				if (deleteConfigStateItems == null)
					deleteConfigStateItems = new ArrayList();

				for (Object key : removedKeys)
				{
					String ident = "" + key + ";" + objectId;

					Map<String, Object> item =
					    createNOMitem("ConfigState");
					item.put("ident", ident);
					deleteConfigStateItems.add(exec.jsonMap(item));
				}
			}
		}
		Iterator iter = settings.keySet().iterator();
		while (iter.hasNext())
		{
			String key = (String) iter.next();

			Map state = new HashMap();

			state.put("type", "ConfigState");
			state.put("objectId", objectId);
			state.put("configId", key);
			state.put("values",  (java.util.List) (settings.get(key)) );



			//logging.debug(this, "setAdditionalConfiguration,  settings.get(key) " + settings.get(key));

			Map retrievedConfig = ((RetrievedMap) settings).getRetrieved();
			Object oldValue = null;

			if (retrievedConfig != null)
				oldValue = retrievedConfig.get(key);

			/*
			logging.debug(this, "setAdditionalConfiguration,  " +
				" ((RetrievedMap) settings).getRetrieved().get..., + key  " + key  + ": " +
				 oldValue);
			*/

			if ((java.util.List) settings.get(key) !=  oldValue )
			{
				//configStateCollection.add(exec.jsonMap(state));
				configStateCollection.add(state);

				//we hope that the update works and directly update the retrievedConfig
				if (retrievedConfig != null) retrievedConfig.put(key,  (java.util.List) settings.get(key));
			}


			/* postponed to setAdditionalConfiguration()
			exec.doCall (new OpsiMethodCall (
				"configState_updateObjects", new Object[] {exec.jsonArray(configStateCollection) }
				)
			);
			*/
		}
	}


	//send config updates and clear the collection
	public  void setAdditionalConfiguration(boolean determineConfigOptions)
	{
		if ( globalReadOnly )
			return;

		if (
			Globals.checkCollection(this, "setAdditionalConfiguration", "configStateCollection", configStateCollection )
			&&
			configStateCollection.size() > 0
		)
		{
			boolean configsChanged = false;

			if (deleteConfigStateItems == null)
				deleteConfigStateItems = new ArrayList();

			// add configId where necessary
			Set<String> usedConfigIds = new HashSet<String>();
			Map<String, String> typesOfUsedConfigIds = new HashMap<String, String>();

			List doneList = new ArrayList();

			for (Object configState : configStateCollection)
			{
				String ident = (String) ( ((Map) configState).get("configId") );
				usedConfigIds.add( ident );

				java.util.List valueList =  (java.util.List) ( ((Map) configState).get("values") );


				if (   valueList.size() > 0 && valueList.get(0) instanceof Boolean  )
					typesOfUsedConfigIds.put( ident, "BoolConfig" );
				else
					typesOfUsedConfigIds.put( ident, "UnicodeConfig" );

				if (valueList  ==  de.uib.utilities.datapanel.MapTableModel.nullLIST)
				{

					Map<String, Object> item = createNOMitem(
					                               "ConfigState"
					                           );
					item.put("objectId", ((Map)configState).get("objectId"));
					item.put("configId", ((Map)configState).get("configId"));

					deleteConfigStateItems.add(exec.jsonMap(item));

					doneList.add(configState);
				}
			}
			logging.debug(this, "setAdditionalConfiguration(), usedConfigIds: " + usedConfigIds);


			logging.debug(this, "setAdditionalConfiguration(), deleteConfigStateItems  " +  deleteConfigStateItems);
			// not used
			if (deleteConfigStateItems.size() > 0)
			{

				OpsiMethodCall omc = new OpsiMethodCall(
				                         "configState_deleteObjects",
				                         new Object[] {
				                             deleteConfigStateItems.toArray()
				                         }
				                     )
				                     ;


				if (exec.doCall(omc))
				{
					deleteConfigStateItems.clear();
					configStateCollection.removeAll(doneList);
				}

			}


			List existingConfigIds =
			    exec.getListResult(
			        new OpsiMethodCall(
			            "config_getIdents",
			            new Object[]{}
			        )
			    )
			    ;
			logging.debug(this, "setAdditionalConfiguration(), existingConfigIds: " + existingConfigIds.size());

			Set<String> missingConfigIds = new HashSet<String>(usedConfigIds);
			for (Object configId : existingConfigIds)
			{
				missingConfigIds.remove(configId);
			}
			logging.debug(this, "setAdditionalConfiguration(), missingConfigIds: " + missingConfigIds);
			ArrayList createItems = new ArrayList();
			for (String missingId : missingConfigIds)
			{
				Map<String, Object> item = createNOMitem(
				                               typesOfUsedConfigIds.get(missingId)
				                           );
				item.put("ident", missingId);
				createItems.add(exec.jsonMap(item));
			}

			if (createItems.size() > 0)
			{
				//logging.debug(this, "should call config_createObjects");

				OpsiMethodCall omc = new OpsiMethodCall(
				                         "config_createObjects",
				                         new Object[] {
				                             createItems.toArray()
				                         }
				                     );
				exec.doCall(omc);
				configsChanged = true;

			}

			if (configsChanged)
			{
				configOptionsRequestRefresh();
				getConfigOptions();
			}

			//build calls

			List callsConfigName2ConfigValueCollection = new ArrayList();
			List callsConfigCollection = new ArrayList();

			for (Object stateO : configStateCollection)
			{
				Map state = (Map) stateO;

				if (determineConfigOptions)
				{
					ConfigOption configOption = configOptions.get(state.get("configId"));

					Map<String, Object> config_forUpdate = new HashMap<String, Object>();

					config_forUpdate.put("ident", state.get("configId"));
					config_forUpdate.put("type", configOption.getRetrieved().get("type"));
					config_forUpdate.put("defaultValues", exec.jsonArray((java.util.List) state.get("values")));

					List possibleValues = (List) configOption.get("possibleValues") ;
					for (Object item : (List) state.get("values"))
					{
						if (possibleValues.indexOf(item) == -1)
							possibleValues.add(item);
					}
					config_forUpdate.put("possibleValues",
					                     exec.jsonArray(possibleValues));

					//mapping to JSON
					logging.debug(this, "setAdditionalConfiguation " + config_forUpdate);
					callsConfigCollection.add(exec.jsonMap(config_forUpdate));
				}


				//mapping to JSON
				//logging.info(this, "setAdditionalConfiguation, put into calls collection " + state );
				//logging.info(this, "setAdditionalConfiguation, put into calls collection value " + (java.util.List) state.get("values"));

				//logging.info(this, "setAdditionalConfiguation, put into calls collection. old value  "  + getConfigs().get(state.get("objectId")).get(state.get("configId")));

				//if (getConfigs().get(state.get("objectId")).get(state.get("configId").equals(

				state.put("values",  exec.jsonArray((java.util.List) state.get("values")));

				//logging.info(this, "setAdditionalConfiguation, put into calls collection with json value " + state.get("values") );

				callsConfigName2ConfigValueCollection.add(exec.jsonMap((Map) state));
			}



			logging.debug(this, "callsConfigCollection " + callsConfigCollection);
			if (callsConfigCollection.size() > 0)
			{
				exec.doCall(
				    new OpsiMethodCall(
				        "config_updateObjects",
				        new Object[]{
				            exec.jsonArray(callsConfigCollection)
				        }
				    )
				);
			}

			//do call
			//configOptionsRequestRefresh();

			// now we can set the values and clear the collected update items
			boolean result = exec.doCall (
			                     new OpsiMethodCall(
			                         "configState_updateObjects",
			                         new Object[]{
			                             exec.jsonArray(callsConfigName2ConfigValueCollection)
			                         }
			                     )
			                 );

			/*
			if (result)
				getConfigs().get
			*/


			// at any rate:
			configStateCollection.clear();


		}
	}

	//collect config updates
	public void setConfig(Map<String, java.util.List<Object>> settings)
	{
		logging.debug(this, "setConfig settings " + settings);
		if (configCollection == null )
			configCollection = new ArrayList();

		for (String key : settings.keySet())
		{
			logging.debug(this, "setConfig,  key, settings.get(key): " + key +", " + settings.get(key));

			if (settings.get(key) != null)
			{
				logging.debug(this, "setConfig,  settings.get(key), settings.get(key).getClass().getName(): "  + settings.get(key) +" , " + settings.get(key).getClass().getName());

				/* historically
				if (settings.get(key) instanceof Map)
			{
					Map<String,Object> newConfig = null;			

					newConfig = (HashMap<String,Object>) settings.get(key);
					
					if (configOptions.get(key) == null)
					{
						Map<String, Object> config = new HashMap<String, Object>();
						config.put("ident", key);
						config.put("type", newConfig.get("type"));
						config.put("defaultValues", newConfig.get("defaultValues"));
						config.put("possibleValues", newConfig.get("possibleValues"));
						config.put("description", newConfig.get("description"));
						configCollection.add(config);
					}
			}
				else 
				*/
				if (settings.get(key) instanceof List)
				{
					java.util.List oldValue = null;

					if (configOptions.get(key) != null)
						oldValue =  configOptions.get(key).getDefaultValues();

					logging.debug(this, "setConfig, key, oldValue: " + key + ", " + oldValue);

					java.util.List valueList = (List) settings.get(key);

					if (valueList != null && (!valueList.equals( oldValue ) ) )
					{
						Map<String, Object> config = new HashMap<String, Object>();
						//config.put(key, (java.util.List) settings.get(key));
						config.put("ident", key);

						String type = "UnicodeConfig";

						logging.debug(this, "setConfig, key,  configOptions.get(key):  " + key + ", "  + configOptions.get(key));
						if (configOptions.get(key) != null)
							type = (String) configOptions.get(key).get("type");

						else
						{
							if (valueList.size() > 0)
							{
								if (valueList.get(0) instanceof java.lang.Boolean)
									type = "BoolConfig";
							}
						}

						config.put("type", type);

						//config.put("defaultValues", exec.jsonArray(valueList));
						config.put("defaultValues", valueList);

						List possibleValues = null;
						if (configOptions.get(key) == null)
						{
							possibleValues = new ArrayList();
							if (type.equals(ConfigOption.BOOL_TYPE))
							{
								possibleValues.add(true);
								possibleValues.add(false);
							}
						}
						else
							possibleValues = (List) configOptions.get(key).getPossibleValues() ;

						for (Object item : valueList)
						{
							if (possibleValues.indexOf(item) == -1)
								possibleValues.add(item);
						}

						config.put("possibleValues", possibleValues);

						//configCollection.add(exec.jsonMap(config));
						configCollection.add(config);
					}
				}

				else
				{
					logging.error("setConfig,  key, settings.get(key): " + key +", " + settings.get(key) + " \nUnexpected type");
				}

			}
		}
	}

	//send config updates and clear the collection
	public void setConfig()
	{
		logging.info(this, "setConfig(),  configCollection null " + (configCollection == null) );
		if (configCollection != null)
			logging.info(this, "setConfig(),  configCollection size  " + configCollection.size() );

		if ( globalReadOnly )
			return;

		if (configCollection != null && configCollection.size() > 0)
		{
			boolean configsChanged = false;
			// add configId where necessary
			List<String> usedConfigIds = new ArrayList<String>();
			Map<String, String> typesOfUsedConfigIds = new HashMap<String, String>();
			for (Object config : configCollection)
			{
				String ident = (String) ( ((Map) config).get("ident") );
				usedConfigIds.add( ident );
				typesOfUsedConfigIds.put( ident, (String) ((Map) config).get("type") );
			}

			logging.debug(this, "setConfig(), usedConfigIds: " + usedConfigIds);

			List existingConfigIds =
			    exec.getListResult(
			        new OpsiMethodCall(
			            "config_getIdents",
			            new Object[]{}
			        )
			    )
			    ;

			logging.info(this, "setConfig(), existingConfigIds: " + existingConfigIds.size());

			//System.exit(0);

			List<String> missingConfigIds = new ArrayList<String>(usedConfigIds);
			for (Object configId : existingConfigIds)
			{
				missingConfigIds.remove(configId);
			}
			logging.info(this, "setConfig(), missingConfigIds: " + missingConfigIds);
			ArrayList createItems = new ArrayList();
			for (String missingId : missingConfigIds)
			{
				Map<String, Object> item = createNOMitem(
				                               typesOfUsedConfigIds.get(missingId)
				                           );
				item.put("ident", missingId);
				createItems.add(exec.jsonMap(item));
			}


			if (createItems.size() > 0)
			{
				OpsiMethodCall omc = new OpsiMethodCall(
				                         "config_createObjects",
				                         new Object[] {
				                             createItems.toArray()
				                         }
				                     );
				exec.doCall(omc);
			}


			//remap to JSON types
			List<org.json.JSONObject> callsConfigUpdateCollection = new ArrayList<org.json.JSONObject>();
			List callsConfigDeleteCollection = new ArrayList();

			for (Object config : configCollection)
			{
				Map callConfig = (Map) config;

				if ( ((Map) config).get("defaultValues")  ==  de.uib.utilities.datapanel.MapTableModel.nullLIST)
				{
					callsConfigDeleteCollection.add(exec.jsonMap((Map) config));
				}

				else
				{
					callConfig.put("defaultValues", exec.jsonArray((List) ((Map) config).get("defaultValues")));
					callConfig.put("possibleValues", exec.jsonArray((List) ((Map) config).get("possibleValues")));
					callsConfigUpdateCollection.add(exec.jsonMap((Map) config));
				}
			}


			if (callsConfigDeleteCollection.size() > 0)
			{
				exec.doCall (
				    new OpsiMethodCall(
				        "config_deleteObjects",
				        new Object[]{
				            exec.jsonArray(callsConfigDeleteCollection)
				        }
				    )
				)
				;
				configOptionsRequestRefresh();
				hostConfigsRequestRefresh(); //because of referential integrity
			}


			if (callsConfigUpdateCollection.size() > 0)
			{
				exec.doCall (
				    new OpsiMethodCall(
				        "config_updateObjects",
				        new Object[]{
				            exec.jsonArray(callsConfigUpdateCollection)
				        }
				    )
				)
				;
				configOptionsRequestRefresh();
			}

			getConfigOptions();
			configCollection.clear();

			logging.info(this, "setConfig(),  configCollection result: " + configCollection );

		}
	}



	/*
	public  void setAdditionalConfiguration (String objectId, Map settings)
{
		//System.out.println ("--- persistence controller shall realize the additional configuration update ");
		ArrayList listOfStates = new ArrayList(); 
		
		Iterator iter = settings.keySet().iterator();
		
		while (iter.hasNext())
		{
			String key = (String) iter.next();
			
			Map state = new HashMap();
		
			state.put("type", "configState");
			state.put("objectId", objectId);
			state.put("configId", key);
			state.put("values", settings.get(key));
			
			listOfStates.add(state);
		}
		
		exec.doCall (new OpsiMethodCall (
			"configegState_updateObjects",  
			new Object[]{listOfStates}
			)
		);
		
		
		Object[] params = new Object[]   {exec.jsonMap(settings), objectId};
		exec.doCall (new OpsiMethodCall ("setGeneralConfig",  params));
}
	*/

	public String getDomain(String objectId)
	{
		String result;
		Object[] params;
		if (objectId.equals(""))
			params = new Object[]{};
		else
			params = new Object[] {objectId};

		result = exec.getStringResult(new OpsiMethodCall("getDomain",params));
		return result;
	}

	public void setDepot(String depotId)
	{
		logging.info(this, "setDepot =========== " + depotId);
		theDepot = depotId;
	}
	
	public String getDepot()
	{
		return theDepot;
	}
	
	public Map<String, SWAuditEntry> getInstalledSoftwareInformation()
	{
		return dataStub.getInstalledSoftwareInformation();
	}

	public void installedSoftwareInformationRequestRefresh()
	{
		logging.info(this," call installedSoftwareInformationRequestRefresh()");
		dataStub.installedSoftwareInformationRequestRefresh();
	}

	public String getSWident(Integer i)
	{
		return dataStub.getSWident(i);
	}

	public LinkedList<String> getSoftwareList()
	{
		return dataStub.getSoftwareList();
	}



	/* licences */
	/*
		service methods used:
		
		createLicenseContract
		deleteLicenseContract
		createLicensePool
		deleteLicensePool
		getSoftwareLicenses_listOfHashes
		createSoftwareLicense
		deleteSoftwareLicense
		getSoftwareLicenses_listOfHashes
		addSoftwareLicenseToLicensePool
		removeSoftwareLicenseFromLicensePool #deleteRelationSoftwareLicenseToLicensePool
		addProductIdsToLicensePool
		removeProductIdsFromLicensePool #deleteRelationProductId2LPool
		setWindowsSoftwareIdsToLicensePool
		getOrCreateSoftwareLicenseUsage_hash
		setSoftwareLicenseUsage
		deleteSoftwareLicenseUsage
		
		
	*/



	public Map<String, LicenceContractEntry> getLicenceContracts()
	// without internal caching
	{
		dataStub.licenceContractsRequestRefresh();
		return dataStub.getLicenceContracts();

		/*
		Map<String, Map> licenceContracts = new HashMap<String, Map>();

		if (withLicenceManagement)
		{
			List li
			= exec.getListResult(new OpsiMethodCall("getLicenseContracts_listOfHashes", new String[]{}));

			//System.out.println("" +li);

			Iterator iter = li.iterator();

			while (iter.hasNext())
			{
				Map listEntry = exec.getMapFromItem(iter.next());
				//System.out.println("" +listEntry.get("licenseContractId"));
				licenceContracts.put(""+listEntry.get("licenseContractId"), listEntry);
			}
	}

		logging.info(this, "getLicenceContracts()  classic  size " + licenceContracts.size() );
		logging.info(this, "getLicenceContracts()  compa size "  + dataStub.getLicenceContracts().size() );

		return licenceContracts;

		*/
	}

	// returns the ID of the edited data record
	public String editLicenceContract(
	    String licenseContractId,
	    String partner,
	    String conclusionDate,
	    String notificationDate,
	    String expirationDate,
	    String notes)
	{

		if (!serverFullPermission )
			return "";
		String result = "";

		logging.debug(this, "editLicenceContract " + licenseContractId);

		if (withLicenceManagement)
		{
			OpsiMethodCall omc = new OpsiMethodCall(
			                         "createLicenseContract",
			                         new String[]{ licenseContractId, partner, conclusionDate, notificationDate, expirationDate, notes} );
			//result = exec.getStringResult ( omc );
			//the method gives the first letter instead of the complete string as return value, therefore we set it in a shortcut:

			exec.getStringResult( omc );
			result = licenseContractId;
		}

		logging.debug(this, "editLicenceContract result " + result);

		return result;
	}

	public boolean deleteLicenceContract(String licenseContractId)
	{
		if (!serverFullPermission)
			return false;


		boolean result = false;

		//logging.injectLogLevel(logging.LEVEL_WARNING);

		if (withLicenceManagement)
		{
			OpsiMethodCall omc = new OpsiMethodCall(
			                         "deleteLicenseContract",
			                         new String[]{
			                             licenseContractId
			                         });

			result = exec.doCall ( omc );
		}
		//logging.injectLogLevel(null);

		return result;
	}



	// returns the ID of the edited data record
	public String editLicencePool(
	    String licensePoolId,  String description
	)
	{
		if (!serverFullPermission)
			return "";

		String result = "";

		if (withLicenceManagement)
		{
			//if ( getLicencepools().containsKey(licensePoolId) )
			
			OpsiMethodCall omc = new OpsiMethodCall(
			                         "createLicensePool",
			                         new String[]{ licensePoolId, description });
			result = exec.getStringResult ( omc );
		}

		return result;
	}


	public boolean deleteLicencePool(String licensePoolId)
	{
		logging.info(this, "deleteLicencePool " + licensePoolId);

		if (!serverFullPermission)
			return false;

		boolean result = false;

		//logging.injectLogLevel(logging.LEVEL_WARNING);

		if (withLicenceManagement)
		{
			//dataStub.getLicencepools().remove(licensePoolId);
			//does not get reach into the crucial data structures


			OpsiMethodCall omc = new OpsiMethodCall(
			                         "deleteLicensePool",
			                         new Object[]{
			                             licensePoolId,
			                             false
			                         });

			result = exec.doCall ( omc );


			//if (result)
			//	dataStub.licencepoolsRequestRefresh();
			//comes too late

		}
		//logging.injectLogLevel(null);

		return result;
	}

	public Map<String, LicenceEntry> getSoftwareLicences()
	// without internal caching
	{
		//dataStub.licencesRequestRefresh();

		Map<String, LicenceEntry> softwareLicences = new HashMap<String, LicenceEntry>();

		if (withLicenceManagement)
		{
			dataStub.licencesRequestRefresh();
			softwareLicences = dataStub.getLicences();
		}
		return softwareLicences;

	}

	// returns the ID of the edited data record
	public String editSoftwareLicence(
	    String softwareLicenseId,
	    String licenceContractId,
	    String licenceType,
	    String maxInstallations,
	    String boundToHost,
	    String expirationDate
	)
	{
		if (!serverFullPermission)
			return "";

		String result = "";

		if (withLicenceManagement)
		{
			OpsiMethodCall omc = new OpsiMethodCall(
			                         "createSoftwareLicense",
			                         new String[]{ softwareLicenseId, licenceContractId, licenceType, maxInstallations, boundToHost, expirationDate});
			result = exec.getStringResult ( omc );
			//System.out.println( " result of createSoftwareLicense " + result);
		}

		return result;
	}

	public boolean deleteSoftwareLicence(
	    String softwareLicenseId
	)
	{
		if (!serverFullPermission)
			return false;

		boolean result = false;

		//logging.injectLogLevel(logging.LEVEL_WARNING);

		if (withLicenceManagement)
		{
			OpsiMethodCall omc = new OpsiMethodCall(
			                         "deleteSoftwareLicense",
			                         new Object[]{
			                             softwareLicenseId,
			                             false
			                         });

			result = exec.doCall ( omc );
		}

		//logging.injectLogLevel(null);

		return result;
	}


	/*
	public Map<String, Map> getLicencekeys()
	// without internal caching 
{
		TreeMap<String, Map> licenceKeys = new TreeMap<String, Map>();
		
		if (withLicenceManagement)
		{
			List li
			= exec.getListResult(new OpsiMethodCall("getSoftwareLicenseKeys_listOfHashes", new String[]{}));
			
			Iterator iter = li.iterator();
			
			while (iter.hasNext())
			{
				Map listEntry = exec.getMapFromItem(iter.next());
				
				licenceKeys.put(""+listEntry.get("licenseKeyId"), listEntry);
			}
		}
		
		//System.out.println ( " ------------ licenceKeys: " + licenceKeys);
		return licenceKeys;

}
	*/

	public Map<String, Map> getRelationsSoftwareL2LPool()
	// without internal caching 
	// legacy license method
	{
		HashMap<String, Map> rowsSoftwareL2LPool = new HashMap<String, Map>();

		if (withLicenceManagement)
		{
			List li0
			= exec.getListResult(new OpsiMethodCall("getSoftwareLicenses_listOfHashes", new String[]{}));

			Iterator iter0 = li0.iterator();

			while (iter0.hasNext())
			{
				Object ob = iter0.next();

				//logging.debug(this, "------- ob " + ob);
				Map m0 = exec.getMapFromItem(ob);
				String softwareLicenseId = (String) m0.get("softwareLicenseId");
				//System.out.println (" ---------- " +  m0.get("licensePoolIds"));
				//System.out.println (" ---------- " +  exec.getListFromItem("" + m0.get("licensePoolIds")));
				List li1 = exec.getListFromItem( "" + m0.get("licensePoolIds")  );
				Map m1 = exec.getMapFromItem( "" + m0.get("licenseKeys") ) ;

				Iterator iter1 = li1.iterator();

				while (iter1.hasNext())
				{
					Map m = new HashMap();
					String licensePoolId = (String) iter1.next();
					m.put("licensePoolId", licensePoolId);

					String licenseKey = null;
					if (m1 != null)
						licenseKey = (String) m1.get(licensePoolId);
					if (licenseKey == null) licenseKey = "";
					m.put("licenseKey", licenseKey);

					m.put("softwareLicenseId", softwareLicenseId);

					rowsSoftwareL2LPool.put(
					    Globals.pseudokey(new String[]{softwareLicenseId, licensePoolId}),
					    m);

				}


				/*m.put("softwareLicenseId",  listEntry.get("softwareLicenseId"));
				m.put("licensePoolId", (String) listEntry.get("licensePoolId"));
				m.put("licenseKey",  (String) listEntry.get("licenseKey"));


				String pseudokey = ""+listEntry.get("softwareLicenseId") + "," + listEntry.get("licensePoolId"); 
				//logging.debug(this, "entry for " + pseudokey +  ": " + listEntry);

				rowsSoftwareL2LPool.put(
					pseudokey, 
					m);
				*/


			}

		}

		//System.out.println ( " ------------ licenceKeys: " + licenceKeys);
		return rowsSoftwareL2LPool;

	}

	//
	public String editRelationSoftwareL2LPool
	(
	    String softwareLicenseId,
	    String licensePoolId,
	    String licenseKey
	)
	{
		if (!serverFullPermission)
			return "";

		String result = "";

		if (withLicenceManagement)
		{
			//logging.debug(this, "--- licenseKey " + licenseKey + ", licensePoolId " + licensePoolId);
			OpsiMethodCall omc = new OpsiMethodCall(
			                         "addSoftwareLicenseToLicensePool",
			                         new String[]{ softwareLicenseId, licensePoolId,  licenseKey });

			//result = exec.doCall ( omc );
			result = exec.getStringResult ( omc );

			//logging.debug(this, "--- result for editRelationSoftwareL2LPool" + result);
		}

		//if (result == null)
		// return null;

		String pseudokey = Globals.pseudokey(new String[]{softwareLicenseId, licensePoolId});

		//System.out.println( " pseudokey " + pseudokey);

		return pseudokey;
	}


	public boolean deleteRelationSoftwareL2LPool(String softwareLicenseId, String licensePoolId)
	{
		if (!serverFullPermission)
			return false;

		boolean result = false;

		//logging.injectLogLevel(logging.LEVEL_WARNING);


		if (withLicenceManagement)
		{
			OpsiMethodCall omc = new OpsiMethodCall(
			                         "removeSoftwareLicenseFromLicensePool",
			                         new String[]{
			                             softwareLicenseId, licensePoolId
			                         });

			result = exec.doCall ( omc );

			//logging.injectLogLevel(null);

			//if (result)
		}

		return result;
	}

	public Map<String, Map<String, String>> getRelationsProductId2LPool()
	// without internal caching
	{


		HashMap<String, Map<String, String>> rowsLicencePoolXOpsiProduct = new HashMap<String, Map<String, String>>();

		if (withLicenceManagement)
		{
			dataStub.licencePoolXOpsiProductRequestRefresh();
			dataStub.getLicencePoolXOpsiProduct();
			logging.info(this, "licencePoolXOpsiProduct size " + dataStub.getLicencePoolXOpsiProduct().size());

			for (StringValuedRelationElement element : dataStub.getLicencePoolXOpsiProduct())
			{
				rowsLicencePoolXOpsiProduct.put(
				    de.uib.utilities.Globals.pseudokey(new String[]{
				                                           element.get(LicencePoolXOpsiProduct.licencepoolKEY),
				                                           element.get(LicencePoolXOpsiProduct.productIdKEY)
				                                       }),
				    element);
			}

			/*
			List li0
			//= exec.getListResult(new OpsiMethodCall("getRelationsProductIdToLicensePool_listOfHashes", new String[]{}));
			= exec.getListResult(new OpsiMethodCall("getLicensePools_listOfHashes", new String[]{}));

			Iterator iter0 = li0.iterator();

			while (iter0.hasNext())
		{
				Object ob = iter0.next();
				Map m0 = exec.getMapFromItem(ob);
				String licensePoolId = (String) m0.get("licensePoolId");

				List li1 = exec.getListFromItem( "" + m0.get("productIds") );

				Iterator iter1 = li1.iterator();

				while (iter1.hasNext())
				{
					Map m = new HashMap();
					String productId = (String) iter1.next();
					m.put("productId", productId);

					m.put("licensePoolId", licensePoolId);

					String pseudokey = Globals.pseudokey(new String[]{licensePoolId, productId});

					//System.out.println( " pseudokey " + pseudokey);

					rowsLicencePoolXOpsiProduct.put(
					    pseudokey,
					    m);
				}
		}
			*/
		}

		logging.info(this, "rowsLicencePoolXOpsiProduct size " +  rowsLicencePoolXOpsiProduct.size());

		return rowsLicencePoolXOpsiProduct;

	}


	public String editRelationProductId2LPool(
	    String productId,
	    String licensePoolId
	)
	{
		if (!serverFullPermission)
			return "";


		String result = "";

		if (withLicenceManagement)
		{
			OpsiMethodCall omc = new OpsiMethodCall(
			                         "addProductIdsToLicensePool",
			                         new String[]{ productId, licensePoolId });

			exec.doCall ( omc );
			//result = exec.getStringResult ( omc );

			//logging.debug(this, "--- result for editRelationProductId2LPool " + result);

			result = licensePoolId;

		}

		return result;
	}


	public boolean deleteRelationProductId2LPool(
	    String productId,
	    String licensePoolId)
	{
		if (!serverFullPermission)
			return false;

		boolean result = false;

		//logging.injectLogLevel(logging.LEVEL_WARNING);

		if (withLicenceManagement)
		{
			OpsiMethodCall omc = new OpsiMethodCall(
			                         "removeProductIdsFromLicensePool",
			                         new String[]{
			                             productId, licensePoolId
			                         });

			result = exec.doCall ( omc );

		}

		//logging.injectLogLevel(null);

		return result;
	}

	//new
	private void retrieveRelationsAuditSoftwareToLicencePools()
	{

		logging.info(this, "retrieveRelationsAuditSoftwareToLicencePools start "
		             + (relations_auditSoftwareToLicencePools != null));

		if (relations_auditSoftwareToLicencePools == null)
			dataStub.auditSoftwareXLicencePoolRequestRefresh();
		else
			return;

		if (!withLicenceManagement)
			return;


		/*
		String[] attributes = new String[]{
		                          "name",
		                          "version",
		                          "subVersion",
		                          "language",
		                          "architecture",
		                          "licensePoolId"
		                      };

		List< Map<String, Object>> relations_auditSoftwareToLicencePools1 =
		    exec.getListOfMaps(
		        new OpsiMethodCall(
		            "auditSoftwareToLicensePool_getObjects",
		            new Object[]{attributes})
		    )
		    ;
		 

		// derived maps
		rowmapAuditSoftware = new TreeMap<String,  Map>();
		fSoftware2LicencePool = new HashMap<String, String>(); //function softwareIdent --> pool
		fLicencePool2SoftwareList = new HashMap<String, List<String>>(); //function pool --> list of assigned software


		Iterator iter = relations_auditSoftwareToLicencePools1.iterator();

		//int i = 0;

		while (iter.hasNext())
		{
			Map retrieved = (Map) iter.next();
			SWAuditEntry entry = new SWAuditEntry(retrieved);
			String licencePoolKEY = "" + retrieved.get("licensePoolId");
			String swKEY = entry.getIdent();
			

			// build row for software table
			LinkedHashMap<String, String> row = new LinkedHashMap<String, String>();

			for (String colName : SWAuditEntry.getDisplayKeys())
			{
				row.put(colName, entry.get(colName));
				//if (i < 3) logging.info(this, "build row colName value " + colName + " , "  + entry.get(colName));
			}
			rowmapAuditSoftware.put(swKEY, row);

			//if (i < 3) logging.info(this, "build rowmap swKEY -- row " + swKEY + " -- " + row);

			//i++;




			//build fSoftware2LicencePool
			if (fSoftware2LicencePool.get(swKEY) != null
			        && !fSoftware2LicencePool.get(swKEY).equals(licencePoolKEY)
			   )
			{
				logging.error("software with ident \"" + swKEY + "\" has assigned license pool "
				              + fSoftware2LicencePool.get(swKEY) + " as well as "
				              + licencePoolKEY);
			}
			fSoftware2LicencePool.put(swKEY, licencePoolKEY);

			//build fLicencePool2SoftwareList
			if (fLicencePool2SoftwareList.get(licencePoolKEY) == null)
				fLicencePool2SoftwareList.put(licencePoolKEY, new ArrayList<String>());

			List softwareIds = fLicencePool2SoftwareList.get(licencePoolKEY);
			if (softwareIds.indexOf(swKEY) == -1)
				softwareIds.add(swKEY);
			
			logging.info(this, "A retrieveRelationsAuditSoftwareToLicencePools  licencePoolKEY "  + licencePoolKEY + ", softwareIds " + softwareIds); 
					 


	}
		*/

		relations_auditSoftwareToLicencePools = dataStub.getAuditSoftwareXLicencePool();

		rowmapAuditSoftware = new TreeMap<String,  Map>();
		fSoftware2LicencePool = new HashMap<String, String>(); //function softwareIdent --> pool
		fLicencePool2SoftwareList = new HashMap<String, List<String>>(); //function pool --> list of assigned software


		for (StringValuedRelationElement  retrieved :  relations_auditSoftwareToLicencePools)
		{
			//logging.info(this, "retrieveRelationsAuditSoftwareToLicencePools  retrieved " + retrieved);
			SWAuditEntry entry = new SWAuditEntry(retrieved, relations_auditSoftwareToLicencePools);
			String licencePoolKEY = retrieved.get(LicencepoolEntry.idSERVICEKEY);
			String swKEY = entry.getIdent();

			//logging.info(this, "retrieveRelationsAuditSoftwareToLicencePools  licencePool, swKEY  " + licencePoolKEY + ", " + swKEY);

			// build row for software table
			LinkedHashMap<String, String> row = new LinkedHashMap<String, String>();

			for (String colName : SWAuditEntry.getDisplayKeys())
			{
				row.put(colName, entry.get(colName));
				//if (i < 3) logging.info(this, "build row colName value " + colName + " , "  + entry.get(colName));
			}
			rowmapAuditSoftware.put(swKEY, row);

			//if (i < 3) logging.info(this, "build rowmap swKEY -- row " + swKEY + " -- " + row);

			//i++;




			//build fSoftware2LicencePool
			if (fSoftware2LicencePool.get(swKEY) != null
			        && !fSoftware2LicencePool.get(swKEY).equals(licencePoolKEY)
			   )
			{
				logging.error("software with ident \"" + swKEY + "\" has assigned license pool "
				              + fSoftware2LicencePool.get(swKEY) + " as well as "
				              + licencePoolKEY);
			}
			fSoftware2LicencePool.put(swKEY, licencePoolKEY);

			//build fLicencePool2SoftwareList
			if (fLicencePool2SoftwareList.get(licencePoolKEY) == null)
				fLicencePool2SoftwareList.put(licencePoolKEY, new ArrayList<String>());

			List softwareIds = fLicencePool2SoftwareList.get(licencePoolKEY);
			if (softwareIds.indexOf(swKEY) == -1)
				softwareIds.add(swKEY);


			//logging.info(this, "retrieveRelationsAuditSoftwareToLicencePools  licencePoolKEY "  + licencePoolKEY + ", softwareIds " + softwareIds);

		}


	}


	public void relations_auditSoftwareToLicencePools_requestRefresh()
	{
		//rowsWindowsSoftwareId2LPool = null;
		relations_auditSoftwareToLicencePools = null;
	}

	public List getSoftwareListByLicencePool(String licencePoolId)
	{
		return fLicencePool2SoftwareList.get(licencePoolId);
	}

	public String getLicencePoolBySoftwareId(String softwareIdent)
	{
		return fSoftware2LicencePool.get(softwareIdent);
	}

	//old
	public void relations_windowsSoftwareId2LPool_requestRefresh()
	{
		//rowsWindowsSoftwareId2LPool = null;
	}

	public TreeMap<String, Map> getRelationsWindowsSoftwareId2LPool()
	// without internal caching
	{
		/*
		rowsWindowsSoftwareId2LPool = new TreeMap<String, Map>();

		// maps that are additionally built for use by other methods 
		lPoolFROMWindowsSoftwareId = new HashMap<String, String>(); //function softwareId --> pool
		windowsSoftwareIdsFROMLPool = new HashMap<String, List>(); //function pool --> list of assigned windows software IDs
		   

		if (withLicenceManagement)
		{
			List li0
			//= exec.getListResult(new OpsiMethodCall("getRelationsWindowsSoftwareIdToLicensePool_listOfHashes", new String[]{}));
			= exec.getListResult(new OpsiMethodCall("getLicensePools_listOfHashes", new String[]{}));
				
			Iterator iter0 = li0.iterator();
			
			while (iter0.hasNext())
			{
				Object ob = iter0.next();
				Map m0 = exec.getMapFromItem(ob);
				String licensePoolId = (String) m0.get("licensePoolId");
				List li1 = exec.getListFromItem( "" + m0.get("windowsSoftwareIds") );
				
				windowsSoftwareIdsFROMLPool.put(licensePoolId, li1);
				
				
				Iterator iter1 = li1.iterator();
				
				while (iter1.hasNext())
				{
					Map m = new HashMap();
				    String windowsSoftwareId = (String) iter1.next();
					m.put("windowsSoftwareId", windowsSoftwareId);
					
					m.put("licensePoolId", licensePoolId);
						
					String pseudokey = Globals.pseudokey(new String[]{licensePoolId, windowsSoftwareId});
					
					logging.debug(this, "row rowsWindowsSoftwareId2LPool, key " + pseudokey + "; row " + m);
					
					rowsWindowsSoftwareId2LPool.put(
					pseudokey, 
					m);
					
					lPoolFROMWindowsSoftwareId.put(windowsSoftwareId, licensePoolId);
				}
			}
	}

		return rowsWindowsSoftwareId2LPool;
		*/
		return null;
	}

	public boolean setWindowsSoftwareIds2LPool(
	    String licensePoolId,
	    List softwareToAssign
	)
	{
		logging.debug(this, "setWindowsSoftwareIds2LPool  licensePoolId,  softwareToAssign:"
		              + licensePoolId + " , " + softwareToAssign);

		if ( !serverFullPermission )
			return false;


		boolean result = false;

		if (withLicenceManagement)
		{
			getInstalledSoftwareInformation();

			List oldEntries = fLicencePool2SoftwareList.get(licensePoolId);

			Set entriesToRemove = new HashSet();

			if (oldEntries != null)
			{
				ArrayList deleteItems = new ArrayList();
				for (Object swIdent : oldEntries)
				{
					//logging.debug(this, "installedSoftwareInformation.get(swIdent),  " + swIdent + ", " + installedSoftwareInformation.get(swIdent) );
					if (getInstalledSoftwareInformation().get(swIdent) != null)
						// software exists in audit software
					{
						entriesToRemove.add(swIdent);
						Map<String, String> item = new  HashMap<String, String>();
						item.put("ident", swIdent+ ";" + licensePoolId);
						item.put("type", "AuditSoftwareToLicensePool");
						deleteItems.add(exec.jsonMap(item));
					}
				}
				//logging.debug(this, "entriesToRemove " + entriesToRemove);

				OpsiMethodCall omc = new OpsiMethodCall(
				                         "auditSoftwareToLicensePool_deleteObjects",
				                         new Object[]{ deleteItems.toArray() }
				                     );
				result = exec.doCall ( omc );

				if (!result)
					return false;

			}
			else
				fLicencePool2SoftwareList.put(licensePoolId, new ArrayList());


			ArrayList createItems = new ArrayList();

			/*
			String typeInfo = "\"type\" : \"AuditSoftwareToLicensePool\"";
			for (Object swIdent : softwareToAssign)
		{
				String identInfo = " \"ident\" :  \""+ swIdent+ ";" + licensePoolId + "\"" ;
				String item = "{" + identInfo + ", " + typeInfo + "}"; 
				createItems.add(item);
		}
			*/

			for (Object swIdent : softwareToAssign)
			{
				Map<String, String> item = new  HashMap<String, String>();
				item.put("ident", swIdent+ ";" + licensePoolId);
				item.put("type", "AuditSoftwareToLicensePool");
				createItems.add(exec.jsonMap(item));
			}


			logging.debug(this, "setWindowsSoftwareIds2LPool, createItems " + createItems);


			OpsiMethodCall omc = new OpsiMethodCall(
			                         "auditSoftwareToLicensePool_createObjects",

			                         new Object[]{ createItems.toArray()  }
			                     );

			result = exec.doCall ( omc );
			//result = exec.getStringResult ( omc );

			if (result)
				//we build the correct data locally
			{
				HashSet intermediateSet
				= new HashSet(fLicencePool2SoftwareList.get(licensePoolId));
				intermediateSet.removeAll(entriesToRemove);
				intermediateSet.addAll(softwareToAssign);
				//dont delete old entries but avoid double entries
				java.util.List newList = new ArrayList(intermediateSet);
				fLicencePool2SoftwareList.put(licensePoolId, newList);
			}

		}

		return result;

	}

	/* returns an ID of the edited data record
	public String editRelationWindowsSoftwareId2LPool(
		String windowsSoftwareId, 
		String licensePoolId
		)
{
		
		String result = "";
		
		if (withLicenceManagement)
		{
			OpsiMethodCall omc = new OpsiMethodCall(
						//"createRelationWindowsSoftwareIdToLicencePool",
						"addWindowsSoftwareIdsToLicensePool",
						new String[]{ windowsSoftwareId, licensePoolId });
			
			exec.doCall ( omc );
			//result = exec.getStringResult ( omc );
			
			//logging.debug(this, "--- result for createRelationWindowsSoftwareId2LPool " + result);
			
		}			
		
		
		return result;
}
	*/




	/*

	protected Map<String, Map<String, String>> retrieveClients(java.util.List<String> requestedFields)
{
		if (requestedFields == null || requestedFields.size() == 0)
		{

		}

		if (!requestedFields.contains("id"))
			requestedFields.add(0, "id");

		String[] callAttributes = requestedFields.toArray(new String[0]);
		HashMap callFilter = new HashMap();
		callFilter.put("type", "OpsiClient");
		//callFilter.put("id", "asrock1.uib.local");

		//replace by a call of retrieveOpsiHosts?
		logging.info(this, "retrieveClients");

		Map<String, Map<String, String>> result =
		    exec.getStringMappedObjectsByKey(
		        new OpsiMethodCall(
		            "host_getObjects",
		            new Object[]{
		                callAttributes,
		                callFilter
		            }
		        ),
		        "ident"
		    )
		    ;

		//logging.debug(this, "retrieveClients " + result);
		return result;
}
	*/



	public java.util.List<String> getServerConfigStrings(String key)
	{
		getConfigOptions();

		java.util.ArrayList<String> result = de.uib.utilities.Globals.takeAsStringList( configDefaultValues.get(key) );
		return result;
	}

	@Override
	public Map<String, LicencepoolEntry> getLicencepools()
	{
		dataStub.licencepoolsRequestRefresh();
		return dataStub.getLicencepools();
	}

	private Integer getIntValue(String s)
	{
		if ( s == null )
			return -1;

		Integer result = -1;
		try
		{
			result = Integer.valueOf( s );
		}
		catch(Exception ex)
		{
			logging.info(this, "getIntValue exception " + ex);
		}

		return result;

	}

	private String testOutRow(String licencePoolId, Map<String, String> rowmap)
	{
		// Send all output to the Appendable object sb


		String result =
		    String.format("%50s", rowmap.get("licensePoolId")) + ": "
		    + " licence_options " + String.format("%5s",  rowmap.get("licence_options"))
		    + " used_by_opsi " + String.format("%5s", rowmap.get("used_by_opsi"))
		    + " remaining_opsi " + String.format("%5s",  rowmap.get("remaining_opsi"))
		    + " SWinventory_used " + String.format("%5s",  rowmap.get("SWinventory_used"))
		    + " SWinventory_remaining " + String.format("%5s", rowmap.get("SWinventory_remaining"))
		    ;

assert rowmap.get("licence_options") != null :  " rowmap.get(licence_options) is null";
assert rowsLicenceStatistics.get(licencePoolId) != null :  " rowsLicenceStatistics.get(licencePoolId)  is null";
assert rowsLicenceStatistics.get(licencePoolId).get("licence_options") != null :  "rowsLicenceStatistics.get(licencePoolId).get.get(licence_options) is null";
assert rowmap.get("used_by_opsi") != null :  " rowmap.get(used_by_opsi) is null";
assert rowsLicenceStatistics.get(licencePoolId).get("used_by_opsi") != null :  "rowsLicenceStatistics.get(licencePoolId).get(used_by_opsi)  is null";
assert  rowmap.get("SWinventory_used")!= null :  "rowsLicenceStatistics.get(licencePoolId).get(SWinventory_used ) is null";


		//comparing to service method
		boolean ok =

		    (rowmap.get("licence_options")).equals(
		        rowsLicenceStatistics.get(licencePoolId).get("licence_options")
		    )

		    &&

		    (rowmap.get("used_by_opsi")).equals(
		        rowsLicenceStatistics.get(licencePoolId).get("used_by_opsi")
		    )

		    &&

		    (rowmap.get("remaining_opsi")).equals(
		        rowsLicenceStatistics.get(licencePoolId).get("remaining_opsi")
		    )

		    &&

		    /*
		    (rowmap.get("SWinventory_remaining")).equals(
		    	(rowmap.get("licence_options"))
		    )
		    */



		    (rowmap.get("SWinventory_used")).equals(
		        rowsLicenceStatistics.get(licencePoolId).get("SWinventory_used")
		    )
		    ;



		if (!ok)
		{
			String result1
			=
			    String.format("%50s", rowmap.get("licensePoolId")) + ": "
			    + " licence_options " + String.format("%5s",
			                                          rowsLicenceStatistics.get(licencePoolId).get("licence_options"))
			    + " used_by_opsi " + String.format("%5s",
			                                       rowsLicenceStatistics.get(licencePoolId).get("used_by_opsi"))
			    + " remaining_opsi " + String.format("%5s",
			                                         rowsLicenceStatistics.get(licencePoolId).get("remaining_opsi"))
			    + " SWinventory_used " + String.format("%5s",
			                                           rowsLicenceStatistics.get(licencePoolId).get("SWinventory_used"))
			    + " SWinventory_remaining " + String.format("%5s",
			            rowsLicenceStatistics.get(licencePoolId).get("SWinventory_remaining"))
			    ;


			logging.warning(this,
			                "statistic results differ new result " + result );

			logging.warning(this,
			                "statistic results differ old  result " + result1 );
		}

		return result;


	}


	protected Map<String, LicenceStatisticsRow> produceLicenceStatistics()
	{
		//side effects of this method: rowsLicencesReconciliation
		logging.info(this, "produceLicenceStatistics === ");

		Map<String, List<String>> licencePool2listOfUsingClients_SWInvent = new HashMap<String, List<String>>();

		//countOnlyOnePoolusagePerClient = true
		Map<String, Set<String>> licencePool2setOfUsingClients_SWInvent = new HashMap<String, Set<String>>();

		//result
		Map<String, Integer> licencePoolUsagecount_SWInvent = new HashMap<String, Integer>();



		fillClient2Software(getHostInfoCollections().getOpsiHostNames()); //now we have audit software on client data for all clients
		AuditSoftwareXLicencePool auditSoftwareXLicencePool = dataStub.getAuditSoftwareXLicencePool();
		//Map<String, Relation> licencepool2Software = auditSoftwareXLicencePool.getFunctionBy(LicencepoolEntry.idSERVICEKEY);

		Map<String, java.util.Set<String>> swId2clients = dataStub.getSoftwareIdent2clients();


		if (withLicenceManagement)
		{
			Map<String, LicencepoolEntry> licencePools = dataStub.getLicencepools();

			if (rowsLicencesReconciliation == null)
			{
				rowsLicencesReconciliation = new HashMap<String, Map<String, Object>>();

				java.util.List<String> extraHostFields = getServerConfigStrings( KEY_HOST_EXTRA_DISPLAYFIELDS_IN_PanelLicencesReconciliation);
				//Map<String, Map<String, String>> clientMap = retrieveClients(extraHostFields);
				Map<String, HostInfo> clientMap = hostInfoCollections.getMapOfAllPCInfoMaps();


				for (String client : clientMap.keySet())
				{
					for (String pool : licencePools.keySet())
					{
						HashMap<String, Object> rowMap = new HashMap<String, Object>();

						rowMap.put("hostId", client);

						//rowMap.put("info", client + " text");

						for (String fieldName : extraHostFields)
						{
							rowMap.put(fieldName, clientMap.get(client).getMap().get(fieldName));
						}

						rowMap.put("licensePoolId", pool);
						rowMap.put("used_by_opsi", false);
						rowMap.put("SWinventory_used", false);
						String pseudokey = Globals.pseudokey(new String[]{client, pool});
						rowsLicencesReconciliation.put(pseudokey, rowMap);
					}
				}
			}

			getInstalledSoftwareInformation();

			retrieveRelationsAuditSoftwareToLicencePools();


			Iterator iter0 = getInstalledSoftwareInformation().keySet().iterator();

			// idents
			while (iter0.hasNext())
			{
				String softwareIdent = (String) iter0.next();

				String licencePoolId = (String) fSoftware2LicencePool.get(softwareIdent);

				if (licencePoolId != null)
				{

					List<String> listOfUsingClients = licencePool2listOfUsingClients_SWInvent.get(licencePoolId);
					Set<String> setOfUsingClients = licencePool2setOfUsingClients_SWInvent.get(licencePoolId);

					if (listOfUsingClients == null)
					{
						listOfUsingClients = new ArrayList<String>();
						licencePool2listOfUsingClients_SWInvent.put(licencePoolId, listOfUsingClients);
					}

					if (setOfUsingClients == null)
					{
						setOfUsingClients = new HashSet<String>();
						licencePool2setOfUsingClients_SWInvent.put(licencePoolId, setOfUsingClients);
					}

					logging.debug(this, "software " + softwareIdent + " installed on "
					              + swId2clients.get(softwareIdent));

					if (swId2clients.get(softwareIdent) == null)
						continue;


					try
					{
						for (Object client : swId2clients.get(softwareIdent))
						{
							listOfUsingClients.add((String) client);
							setOfUsingClients.add((String) client);
						}
					}
					catch(Exception ex)
					{
						logging.warning(" swId2clients.get(softwareIdent) -" + ex);
					}

					licencePoolUsagecount_SWInvent.put(licencePoolId, setOfUsingClients.size());

					for (String client : swId2clients.get(softwareIdent))
					{
						String pseudokey = Globals.pseudokey(new String[]{client, licencePoolId});

						if (rowsLicencesReconciliation.get(pseudokey)  == null )

							logging.warning("client " + client + " or license pool ID " + licencePoolId  + " do not exist");

						else
							rowsLicencesReconciliation.get(pseudokey).put("SWinventory_used", true);
					}
				}
			}
		}



		//------------------ retrieve data for statistics

		//table  LICENSE_POOL
		Map<String, LicencepoolEntry> licencePools = dataStub.getLicencepools();

		//table SOFTWARE_LICENSE
		logging.info(this, " licences ");
		//logging.info(this, " licences " + dataStub.getLicences());

		//table SOFTWARE_LICENSE_TO_LICENSE_POOL
		logging.info(this, " licence usabilities ");
		java.util.List<LicenceUsableForEntry> licenceUsabilities = dataStub.getLicenceUsabilities();
		//logging.info(this, " licence usabilities " + licenceUsabilities);

		//table LICENSE_ON_CLIENT
		logging.info(this, " licence usages ");
		java.util.List<LicenceUsageEntry> licenceUsages =  dataStub.getLicenceUsages();


		//software usage according to audit
		//tables
		// AUDIT_SOFTWARE_TO_LICENSE_POOL
		// SOFTWARE_CONFIG
		//leads to getSoftwareAuditOnClients()





		//----------------- set up data structure

		TreeMap<String, ExtendedInteger> pool2allowedUsagesCount = new TreeMap<String, ExtendedInteger>();

		for ( LicenceUsableForEntry licenceUsability : licenceUsabilities )
		{
			String pool = licenceUsability.getLicencePoolId();
			String licenceId =  licenceUsability.getLicenceId();

			ExtendedInteger count  = pool2allowedUsagesCount.get( pool );

			/*
			if (pool.equals("windows_ultimate_oem"))
				logging.info(this, " pool  windows_ultimate_oem count " + count);

			if (pool.equals("windows_ultimate_oem"))
				logging.info(this, licenceUsability.getLicenceId() );
			*/

			/*
			if (pool.equals("office2010"))
				logging.info(this, "getting count for office2010 " + dataStub.getLicences().get( licenceId ));
			*/

			if ( count == null )
			{
				//logging.info(this, String.format("count == null for  pool %s,  licenceId %s", pool, licenceId));
				//logging.info(this, " dataStub.getLicences() == null " + (dataStub.getLicences() == null));
				//logging.info(this, " dataStub.getLicences().get(licenceId) == null " + (dataStub.getLicences().get(licenceId) == null));
				count  =
				    dataStub.getLicences().get( licenceId )
				    .getMaxInstallations();

				pool2allowedUsagesCount.put( pool, count );

				/*
				if (pool.equals("office2010"))
				logging.info(this, "initialize count for office2010 " + count);
				*/
			}
			else
			{
				//logging.info(this, String.format("count != null for  pool %s,  licenceId %s", pool, licenceId));
				//logging.info(this, " dataStub.getLicences() == null " + (dataStub.getLicences() == null));
				//logging.info(this, " dataStub.getLicences().get(licenceId) == null " + (dataStub.getLicences().get(licenceId) == null));

				/*
				if (pool.equals("windows_ultimate_oem"))
					logging.info(this, " pool  windows_ultimate_oem adding  " + count + " to "  +
					dataStub.getLicences().get( licenceId )
					.getMaxInstallations()
					);	
				*/


				ExtendedInteger result = count.add(
				                             dataStub.getLicences().get( licenceId )
				                             .getMaxInstallations()
				                         );

				/*
				if (pool.equals("office2010"))
					logging.info(this, "continuing count for office2010 " + count);
				*/

				pool2allowedUsagesCount.put( pool, result );
			}


			if (pool.equals("bacula"))
				logging.info(this, "count for bacula " + count);


		}

		logging.info(this, " pool2allowedUsagesCount " +  pool2allowedUsagesCount);

		TreeMap<String, Integer> pool2opsiUsagesCount = new TreeMap<String, Integer>();

		Map<String, Set<String>> pool2opsiUsages = new TreeMap<String, Set<String>>();


		for (LicenceUsageEntry licenceUsage : licenceUsages )
		{
			String lic4pool = licenceUsage.getLic4pool();
			//logging.info(this, " pool with usage "  + lic4pool);

			String pool = licenceUsage.getLicencepool();
			Integer usageCount = pool2opsiUsagesCount.get(pool);
			Set<String> usingClients = pool2opsiUsages.get(pool);

			if (usingClients == null)
			{
				usingClients = new TreeSet<String>();
				pool2opsiUsages.put(pool, usingClients);
			}

			if (usageCount == null)
			{
				usageCount = new Integer(0);
				pool2opsiUsagesCount.put(pool, usageCount);
			}

			//logging.info(this, " pool with usage "  + licenceUsage.getClientId() );

			String clientId = licenceUsage.getClientId();

			if ( clientId != null)
			{
				usageCount = usageCount + 1;
				//logging.info(this, " usageCount "  + usageCount );
				pool2opsiUsagesCount.put(pool, usageCount);

				usingClients.add(clientId);

			}



		}

		//logging.info(this, " pool2opsiUsagesCount " +  pool2opsiUsagesCount);


		//all used licences for pools

		logging.info(this, "  retrieveStatistics  collect pool2installationsCount");

		TreeMap<String, TreeSet<String>> pool2clients = new TreeMap<String,  TreeSet<String>>();
		//we take Set since we count only one usage per client

		TreeMap<String, Integer> pool2installationsCount = new TreeMap<String, Integer>();

		//iterate throught the licence pools; get the software combined ids which require this licencepool
		//add the clients which have this software installed


		for (StringValuedRelationElement swXpool : auditSoftwareXLicencePool)
		{

			logging.debug(this, " retrieveStatistics1 relationElement  " + swXpool);

			String pool = swXpool.get(LicencepoolEntry.idSERVICEKEY);

			TreeSet<String> clientsServedByPool = pool2clients.get(pool);

			if (clientsServedByPool == null)
			{
				clientsServedByPool = new TreeSet<String>();
				pool2clients.put(pool, clientsServedByPool);
			}


			String swIdent = swXpool.get(AuditSoftwareXLicencePool.SwID);

			logging.debug(this, " retrieveStatistics1 swIdent " + swIdent);

			/*
			if (swIdent.startsWith("firefox"))
		{
				logging.info(this, " retrieveStatistics1 softwareIdent2clients.get(swIdent) " +
					swId2clients.get(swIdent));
				
				//firefox-locale-de;27.0+build1-0ubuntu0.12.04.
		}
			*/


			if (swId2clients.get(swIdent) != null)
			{
				logging.debug(this, "pool " + pool + " serves clients " +  swId2clients.get(swIdent));
				clientsServedByPool.addAll( swId2clients.get(swIdent) );

			}
		}

		for (String pool : pool2clients.keySet())
		{
			pool2installationsCount.put(pool, pool2clients.get(pool).size());
		}

		//logging.info(this, "pool2installationsCount " + pool2installationsCount);


		rowsLicenceStatistics = new TreeMap<String, LicenceStatisticsRow>();

		if (withLicenceManagement)
		{

			for( String licencePoolId : licencePools.keySet() )
			{
				LicenceStatisticsRow rowMap = new LicenceStatisticsRow( licencePoolId );
				rowsLicenceStatistics.put( licencePoolId, rowMap );

				rowMap.setAllowedUsagesCount( pool2allowedUsagesCount.get( licencePoolId) );

				rowMap.setOpsiUsagesCount( pool2opsiUsagesCount.get(licencePoolId) );

				rowMap.setSWauditUsagesCount( pool2installationsCount.get( licencePoolId) );

				//if (licencePoolId.equals("p_winxppro")) logging.info(this, "rowsLicenceStatistics1 " + licencePoolId + ":: "  + rowMap);

				//logging.info(this, "rowsLicenceStatistics1 " + testOutRow(licencePoolId, rowMap));



				Set<String> listOfUsingClients = pool2opsiUsages.get(licencePoolId);

				logging.info(this, "pool  " + licencePoolId + " used_by_opsi on clients : "  + listOfUsingClients );

				if (listOfUsingClients != null)
				{
					for (String client : listOfUsingClients)
					{
						String pseudokey = Globals.pseudokey(new String[]{client, licencePoolId});


						if (rowsLicencesReconciliation.get(pseudokey)  == null )
							logging.warning("client " + client + " or license pool ID " + licencePoolId  + " do not exist");
						else
							rowsLicencesReconciliation.get(pseudokey).put("used_by_opsi", true);
					}
				}

			}

		}

		logging.debug(this, "rowsLicenceStatistics " +  rowsLicenceStatistics);




		return rowsLicenceStatistics;

	}




	@Override
	public Map<String, LicenceStatisticsRow> getLicenceStatistics()
	{

		return produceLicenceStatistics();


		/*


		logging.info(this, "getLicenceStatistics()");

		rowsLicenceStatistics = new TreeMap<String, LicenceStatisticsRow>();

		boolean countOnlyOnePoolusagePerClient = true;

		//countOnlyOnePoolusagePerClient = false
		Map<String, List<String>> licencePool2listOfUsingClients_SWInvent = new HashMap<String, List<String>>();

		//countOnlyOnePoolusagePerClient = true
		Map<String, Set<String>> licencePool2setOfUsingClients_SWInvent = new HashMap<String, Set<String>>();

		//result
		Map<String, Integer> licencePoolUsagecount_SWInvent = new HashMap<String, Integer>();


		if (withLicenceManagement)
		{
			Map<String, LicencepoolEntry> licencePools = dataStub.getLicencepools();

			if (rowsLicencesReconciliation == null)
			{
				rowsLicencesReconciliation = new HashMap<String, Map<String, Object>>();

				java.util.List<String> extraHostFields = getServerConfigStrings( KEY_HOST_EXTRA_DISPLAYFIELDS_IN_PanelLicencesReconciliation);
				Map<String, Map<String, String>> clientMap = retrieveClients(extraHostFields);

				for (String client : clientMap.keySet())
				{
					for (String pool : licencePools.keySet())
					{
						HashMap<String, Object> rowMap = new HashMap<String, Object>();

						rowMap.put("hostId", client);

						//rowMap.put("info", client + " text");

						for (String fieldName : extraHostFields)
						{
							rowMap.put(fieldName, clientMap.get(client).get(fieldName));
						}

						rowMap.put("licensePoolId", pool);
						rowMap.put("used_by_opsi", false);
						rowMap.put("SWinventory_used", false);
						String pseudokey = Globals.pseudokey(new String[]{client, pool});
						rowsLicencesReconciliation.put(pseudokey, rowMap);
					}
				}
			}

			getInstalledSoftwareInformation();

			retrieveRelationsAuditSoftwareToLicencePools();

			if (auditClientsUsingSoftware == null)
				auditClientsUsingSoftware =
				    exec.getMapOfLists(new OpsiMethodCall(
				                           //"extend/configed",
				                           "getAuditSoftwareUsage",
				                           new Object[]{}
				                       )
				                      );


			Iterator iter0 = getInstalledSoftwareInformation().keySet().iterator();

			// idents
			while (iter0.hasNext())
			{
				String softwareIdent = (String) iter0.next();

				String licencePoolId = (String) fSoftware2LicencePool.get(softwareIdent);

				if (licencePoolId != null)
				{

					List<String> listOfUsingClients = licencePool2listOfUsingClients_SWInvent.get(licencePoolId);
					Set<String> setOfUsingClients = licencePool2setOfUsingClients_SWInvent.get(licencePoolId);

					if (listOfUsingClients == null)
					{
						listOfUsingClients = new ArrayList<String>();
						licencePool2listOfUsingClients_SWInvent.put(licencePoolId, listOfUsingClients);
					}

					if (setOfUsingClients == null)
					{
						setOfUsingClients = new HashSet<String>();
						licencePool2setOfUsingClients_SWInvent.put(licencePoolId, setOfUsingClients);
					}



					logging.debug(this, "software " + softwareIdent + " installed on "
					              + auditClientsUsingSoftware.get(softwareIdent));

					if (auditClientsUsingSoftware.get(softwareIdent) == null)
						continue;


					try
					{
						for (Object client : auditClientsUsingSoftware.get(softwareIdent))
						{
							listOfUsingClients.add((String) client);
							setOfUsingClients.add((String) client);
						}
					}
					catch(Exception ex)
					{
						logging.warning(" auditClientsUsingSoftware.get(softwareIdent) -" + ex);
					}

					if (countOnlyOnePoolusagePerClient)
						licencePoolUsagecount_SWInvent.put(licencePoolId, setOfUsingClients.size());
					else
						licencePoolUsagecount_SWInvent.put(licencePoolId, listOfUsingClients.size());

					Iterator iter = auditClientsUsingSoftware.get(softwareIdent).iterator();
					while (iter.hasNext())
					{
						String client = (String) iter.next();

						String pseudokey = Globals.pseudokey(new String[]{client, licencePoolId});

						if (rowsLicencesReconciliation.get(pseudokey)  == null )

							logging.warning("client " + client + " or license pool ID " + licencePoolId  + " do not exist");

						else
							rowsLicencesReconciliation.get(pseudokey).put("SWinventory_used", true);
					}
				}
			}


			if (statistics4licensePools == null)
				statistics4licensePools = exec.getMapResult(
				                              new OpsiMethodCall(
				                                  "getLicenseStatistics_hash",
				                                  new String[]{})
				                          );


			for( String licencePoolId : licencePools.keySet() )
			{

				if (statistics4licensePools.get(licencePoolId) != null)
				{

					LicenceStatisticsRow rowMap = new LicenceStatisticsRow(licencePoolId);


					Map infoMap = exec.getMapFromItem(statistics4licensePools.get(licencePoolId));

					List listOfUsingClients = exec.getListFromItem("" + infoMap.get("usedBy"));

					Object maxInstallations = infoMap.get("maxInstallations");
					ExtendedInteger extIntMaxInstallations = new ExtendedInteger("" + maxInstallations);

					int usedAccordingToSWInvent = 0;
					if (licencePoolUsagecount_SWInvent.get(licencePoolId) != null
					   )
						usedAccordingToSWInvent = (Integer) licencePoolUsagecount_SWInvent.get(licencePoolId);
						
					ExtendedInteger extIntRemainingAccordingToSWInvent = extIntMaxInstallations.add(-usedAccordingToSWInvent);

					
					//rowMap.put("licence_items_count", "" + statistic4licensePool.get("licences"));
					rowMap.put("licence_options", extIntMaxInstallations.getDisplay());
					rowMap.put("used_by_opsi", "" + infoMap.get("usageCount"));

					Object remainingInstallations = infoMap.get("remainingInstallations");
					rowMap.put("remaining_opsi",  new ExtendedInteger("" + remainingInstallations).getDisplay() );
					rowMap.put("SWinventory_used", "" + usedAccordingToSWInvent);
					rowMap.put("SWinventory_remaining", extIntRemainingAccordingToSWInvent.getDisplay());

					rowsLicenceStatistics.put(licencePoolId, rowMap);


					Iterator iterUsingClients = listOfUsingClients.iterator();
					while (iterUsingClients.hasNext())
					{
						String client = (String) iterUsingClients.next();

						String pseudokey = Globals.pseudokey(new String[]{client, licencePoolId});

						if (rowsLicencesReconciliation.get(pseudokey)  == null )
							logging.warning("client " + client + " or license pool ID " + licencePoolId  + " do not exist");

						else
							rowsLicencesReconciliation.get(pseudokey).put("used_by_opsi", true);
					}
				}
			}
	}



		return rowsLicenceStatistics;


		*/

	}

	@Override
	public void licencesUsageRequestRefresh()
	{
		rowsLicencesUsage = null;

		fClient2LicencesUsageList = null;
	}

	@Override
	public Map<String, LicenceUsageEntry> getLicencesUsage()
	{
		retrieveLicencesUsage();
		return rowsLicencesUsage;
	}

	@Override
	public  Map<String, java.util.List<LicenceUsageEntry>> getFClient2LicencesUsageList()
	{
		retrieveLicencesUsage();
		return fClient2LicencesUsageList;
	}


	protected void retrieveLicencesUsage()
	{
		logging.info(this, "retrieveLicencesUsage with refresh " + (rowsLicencesUsage == null));

		if (rowsLicencesUsage == null)
			dataStub.licenceUsagesRequestRefresh();

		else
			return;

		if (!withLicenceManagement)
			return;

		rowsLicencesUsage = new HashMap<String, LicenceUsageEntry>();
		fClient2LicencesUsageList = new HashMap<String, java.util.List<LicenceUsageEntry>>();

		for (LicenceUsageEntry m : dataStub.getLicenceUsages())
		{
			rowsLicencesUsage.put(m.getPseudoKey(), m);

			java.util.List<LicenceUsageEntry> licencesUsagesForClient =
			    fClient2LicencesUsageList.get(m.getClientId());

			if (licencesUsagesForClient == null)
			{
				licencesUsagesForClient = new ArrayList<LicenceUsageEntry>();
				fClient2LicencesUsageList.put(m.getClientId(), licencesUsagesForClient);
			}
			licencesUsagesForClient.add(m);


		}
	}



	public String getLicenceUsage(String hostId, String licensePoolId)
	// retrieves the used software licence - or tries to reserve one - for the given host and licence pool
	{
		String result = null;
		Map resultMap = null;

		if (withLicenceManagement)
		{
			OpsiMethodCall omc0 = new OpsiMethodCall(
			                          "getOrCreateSoftwareLicenseUsage_hash",
			                          new String[]{
			                              hostId, licensePoolId
			                          });
			
			/*
			def getOrCreateSoftwareLicenseUsage_hash(self, hostId, licensePoolId="", productId="", windowsSoftwareId=""):
				return self.licenseOnClient_getOrCreateObject(
					clientId          = hostId,
					licensePoolId     = licensePoolId,
					productId         = productId,
					windowsSoftwareId = windowsSoftwareId).toHash()
			*/

			

			resultMap =  exec.getMapResult ( omc0 );


		}

		if (resultMap != null && !resultMap.isEmpty())
			result = Globals.pseudokey(new String[]{""+resultMap.get("hostId"), ""+resultMap.get("softwareLicenseId"), ""+resultMap.get("licensePoolId")});


		return result;

	}

	public String editLicenceUsage(String hostId, String softwareLicenseId, String licensePoolId, String licenseKey, String notes)
	{
		if (!serverFullPermission)
			return null;

		String result = null;
		Map resultMap = null;

		//logging.injectLogLevel(logging.LEVEL_WARNING);

		if (withLicenceManagement)
		{
			OpsiMethodCall omc = new OpsiMethodCall(
			                         "setSoftwareLicenseUsage",
			                         new String[]{
			                             hostId, licensePoolId, softwareLicenseId, licenseKey, notes
			                         });

			resultMap = exec.getMapResult ( omc );
		}

		//logging.injectLogLevel(null);

		if (!resultMap.isEmpty())
			result = Globals.pseudokey(new String[]{""+resultMap.get("hostId"), ""+resultMap.get("softwareLicenseId"), ""+resultMap.get("licensePoolId")});

		//System.out.println("--------- result getLicenceUsage " + result);
		return result;
	}


	@Override
	public void addDeletionLicenceUsage(String hostId, String softwareLicenseId, String licensePoolId)
	{
		//logging.info(this, "addDeletionLicenceUsage hostId, softwareLicenseId, licensePoolId " + hostId + ", " + softwareLicenseId + ", " + licensePoolId);
		if (itemsDeletionLicenceUsage == null)
			itemsDeletionLicenceUsage = new ArrayList<LicenceUsageEntry>();

		addDeletionLicenceUsage(hostId, softwareLicenseId, licensePoolId, itemsDeletionLicenceUsage);
		//logging.info(this, "addDeletionLicenceUsage, size of collectiion now  " + itemsDeletionLicenceUsage.size());

	}

	protected void addDeletionLicenceUsage(String hostId, String softwareLicenseId, String licensePoolId,
	                                       java.util.List<LicenceUsageEntry> deletionItems)
	{
		if (deletionItems == null)
			return;

		if ( !serverFullPermission )
			return;

		if (!withLicenceManagement)
			return;

		LicenceUsageEntry deletionItem = new LicenceUsageEntry(
		                                     hostId, softwareLicenseId, licensePoolId, "", "");

		//logging.info(this, " addDeletionLicenceUsage " + deletionItem);

		deletionItems.add( deletionItem );
		//logging.info(this, "addDeletionLicenceUsage deletionItems.count " + deletionItems.size());
	}

	@Override
	public boolean executeCollectedDeletionsLicenceUsage()
	{
		logging.info(this, "executeCollectedDeletionsLicenceUsage itemsDeletionLicenceUsage == null "
		             +  (itemsDeletionLicenceUsage == null));
		if (itemsDeletionLicenceUsage == null)
			return true;

		if ( !serverFullPermission )
			return false;

		if (!withLicenceManagement)
			return false;


		boolean result = false;

		//logging.injectLogLevel(logging.LEVEL_WARNING);

		java.util.List<Object> jsonPreparedList =  new ArrayList<Object>();
		for (LicenceUsageEntry item : itemsDeletionLicenceUsage)
		{
			jsonPreparedList.add(exec.jsonMap(item.getNOMobject()));
		}

		//logging.info(this, "executeCollectedDeletionsLicenceUsage " + jsonPreparedList);

		OpsiMethodCall omc = new OpsiMethodCall(
		                         "licenseOnClient_deleteObjects",
		                         new Object[]{
		                             exec.jsonArray(jsonPreparedList)
		                         });

		result = exec.doCall ( omc );

		if (result)
		{
			for (LicenceUsageEntry item : itemsDeletionLicenceUsage)
			{
				String key = item.getPseudoKey();
				String hostX = item.getClientId();

				LicenceUsageEntry rowmap = rowsLicencesUsage.get(key);
				rowsLicencesUsage.remove(key);
				fClient2LicencesUsageList.get(hostX).remove(rowmap);

				logging.debug(this, "deleteLicenceUsage check fClient2LicencesUsageList " +
				              fClient2LicencesUsageList.get(hostX));

			}
		}

		itemsDeletionLicenceUsage.clear();

		//logging.injectLogLevel(null);

		return result;
	}




	public boolean deleteLicenceUsage(String hostId, String softwareLicenseId, String licensePoolId)
	{
		if ( !serverFullPermission )
			return false;

		boolean result = false;

		//logging.injectLogLevel(logging.LEVEL_WARNING);

		if (withLicenceManagement)
		{
			OpsiMethodCall omc = new OpsiMethodCall(
			                         "deleteSoftwareLicenseUsage",
			                         new String[]{
			                             hostId, softwareLicenseId, licensePoolId
			                         });

			result = exec.doCall ( omc );


			if (result)
			{
				String key = LicenceUsageEntry.produceKey(hostId, licensePoolId, softwareLicenseId);
				LicenceUsageEntry rowmap = rowsLicencesUsage.get(key);
				rowsLicencesUsage.remove(key);
				fClient2LicencesUsageList.get(hostId).remove(rowmap);
			}

			logging.info(this, "deleteLicenceUsage check fClient2LicencesUsageList " + fClient2LicencesUsageList.get(hostId));

		}

		//logging.injectLogLevel(null);

		return result;
	}

	public void reconciliationInfoRequestRefresh()
	{
		logging.info(this, "reconciliationInfoRequestRefresh");
		rowsLicencesReconciliation = null;
		logging.info(this,"reconciliationInfoRequestRefresh installedSoftwareInformationRequestRefresh()");
		dataStub.installedSoftwareInformationRequestRefresh();
		//rowsWindowsSoftwareId2LPool = null;
		relations_auditSoftwareToLicencePools = null;
		//statistics4licensePools = null;
		//auditClientsUsingSoftware = null;
		dataStub.softwareAuditOnClientsRequestRefresh();
		dataStub.licencepoolsRequestRefresh();
		dataStub.licencesRequestRefresh();
		dataStub.licenceUsabilitiesRequestRefresh();
		dataStub.licenceUsagesRequestRefresh();
		hostInfoCollections.opsiHostsRequestRefresh();
	}

	public Map<String, Map<String, Object>> getLicencesReconciliation()
	{
		getLicenceStatistics(); // there we produce all infos needed

		/* test:

		rowsLicencesReconciliation = new HashMap<String, Map>();

						HashMap<String, Object> rowMap = new HashMap<String, Object>();
					
						rowMap.put("hostId", "wintestr.uib.local");
						rowMap.put("licensePoolId", "acrowrite");
						rowMap.put("used_by_opsi", false);
						rowMap.put("SWinventory_used", true);
						String pseudokey = Globals.pseudokey(new String[]{"wintestr.uib.local","acrowrite"});
						rowsLicencesReconciliation.put(pseudokey, rowMap);

			// test
		*/
		return rowsLicencesReconciliation;
	}

	public String editLicencesReconciliation(String clientId, String licensePoolId)
	{
		return "";
	}

	public boolean deleteLicencesReconciliation(String clientId, String licensePoolId)
	{
		return false;
	}

	private List<String> getPossibleValuesProductOnClientDisplayfields_localboot()
	{
		ArrayList<String> possibleValues = new ArrayList<String>();
		possibleValues.add("productId");
		possibleValues.add(ProductState.KEY_productName);
		possibleValues.add(ProductState.KEY_installationStatus);
		possibleValues.add(ProductState.KEY_installationInfo);
		//combines
		//ProductState.KEY_actionProgress
		//ProductState.KEY_actionResult
		//ProductState.KEY_lastAction
		possibleValues.add(ProductState.KEY_actionRequest);
		possibleValues.add(ProductState.KEY_productPriority);
		possibleValues.add(ProductState.KEY_position);
		possibleValues.add(ProductState.KEY_lastStateChange);
		//ProductState.KEY_actionSequence
		possibleValues.add(ProductState.KEY_targetConfiguration);
		possibleValues.add(ProductState.KEY_versionInfo);
		//combines
		//ProductState.KEY_productVersion
		//ProductState.KEY_packageVersion

		return possibleValues;
	}

	private List<String> getDefaultValuesProductOnClientDisplayfields_localboot()
	{
		List<String> result = new ArrayList<String>();

		result.add("productId");
		//result.add(ProductState.KEY_productName);
		result.add(ProductState.KEY_installationStatus);
		result.add(ProductState.KEY_installationInfo);
		result.add(ProductState.KEY_actionRequest);
		result.add(ProductState.KEY_versionInfo);

		return result;

	}



	private List<String> produceProductOnClientDisplayfields_localboot()
	{
		if ( globalReadOnly )
			return null;


		List<String> result  = getDefaultValuesProductOnClientDisplayfields_localboot();

		List<String> possibleValues = getPossibleValuesProductOnClientDisplayfields_localboot();

		//create config for service
		Map<String, Object> item = createNOMitem("UnicodeConfig");
		item.put("ident", KEY_PRODUCTONCLIENT_DISPLAYFIELDS_LOCALBOOT);
		item.put("description", "");
		item.put("defaultValues", exec.jsonArray(result));
		item.put("possibleValues", exec.jsonArray(possibleValues));
		item.put("editable", false);
		item.put("multiValue", true);

		logging.info(this, "produceProductOnClientDisplayfields_localboot");

		OpsiMethodCall omc = new OpsiMethodCall(
		                         "config_updateObjects",
		                         new Object[] {
		                             exec.jsonMap(item)
		                         }
		                     );

		exec.doCall(omc);

		return result;
	}


	@Override
	public void userConfigurationRequestReload()
	{
		logging.info(this, "userConfigurationRequestReload");
		KEY_USER_REGISTER_VALUE = null;
	}
	
	
	final private boolean isUserRegisterActivated()
	{
		boolean result = false;
		
		Map<String, java.util.List<Object>> serverPropertyMap = getConfigDefaultValues();
		if (serverPropertyMap.get(KEY_USER_REGISTER) != null || serverPropertyMap.get(KEY_USER_REGISTER).size() > 0)
				//dont do anything if we have not got the config
			{
				result = (Boolean) ((java.util.List)(serverPropertyMap.get(KEY_USER_REGISTER))).get(0);
			}
		return result;
	}
		
	//configurations and algorithms
	final protected boolean applyUserConfiguration()
	//combines KEY_USER_REGISTER_VALUE and the modules feature withUserRoles
	//should not be overwritten to avoid privileges confusion
	{
		// do it only once
		
		
		if (KEY_USER_REGISTER_VALUE == null)
		{
			KEY_USER_REGISTER_VALUE = false;
			
			Map<String, java.util.List<Object>> serverPropertyMap = getConfigDefaultValues();
			logging.info(this, "applyUserConfiguration config " + serverPropertyMap.get(KEY_USER_REGISTER) );
			
			if (serverPropertyMap.get(KEY_USER_REGISTER) != null && serverPropertyMap.get(KEY_USER_REGISTER).size() > 0)
				//dont do anything if we have not got the config
			{
				KEY_USER_REGISTER_VALUE = (Boolean) ((java.util.List)(serverPropertyMap.get(KEY_USER_REGISTER))).get(0);
				
				if (KEY_USER_REGISTER_VALUE && !withUserRoles)
				{
					KEY_USER_REGISTER_VALUE = false;
					
					//configed.endApp(2);
					javax.swing.SwingUtilities.invokeLater(new Thread(){
										public void run()
										{
											StringBuffer info = new StringBuffer();
											info.append(configed.getResourceValue("Permission.modules.missing_user_roles") + "\n\n\n");
											info.append(configed.getResourceValue("Permission.modules.missing_user_roles.1")  + "\n");
											info.append(configed.getResourceValue("Permission.modules.missing_user_roles.2")  + "\n");
											info.append(KEY_USER_REGISTER + " " + configed.getResourceValue("Permission.modules.missing_user_roles.3"));
											info.append("\n");
		
											logging.warning(this, " user role administration configured but not permitted by the modules file " + info);
		
											javax.swing.JOptionPane.showMessageDialog(	de.uib.configed.Globals.mainContainer,
													info.toString(),
													configed.getResourceValue("Permission.modules.title"),
													javax.swing.JOptionPane.OK_OPTION);
		
											//configed.endApp(2);
		
										}
									}
							   );
				}
				
				
				//logging.debug(this, "applyUserConfiguration type  " +  ((Object)(serverPropertyMap.get(KEY_USER_REGISTER).get(0))).getClass().getName());

				
			}
		}
		
		logging.info(this, "applyUserConfiguration result " +  KEY_USER_REGISTER_VALUE);
			
		return KEY_USER_REGISTER_VALUE;
	}

	/*public  java.util.Set<String> getAccessedDepots()
	{
		Map<String, java.util.List<Object>> serverPropertyMap = getConfigDefaultValues();
		return null;
	}
	*/

	public LinkedHashMap<String, Boolean> getProductOnClients_displayFieldsLocalbootProducts()
	{
		//getConfigOptions(); //necessary?
		if (productOnClients_displayFieldsLocalbootProducts == null)
		{
			Map<String, java.util.List<Object>> serverPropertyMap = getConfigDefaultValues();

			logging.debug(this, "getProductOnClients_displayFieldsLocalbootProducts()  configOptions.get(KEY_PRODUCTONCLIENT_DISPLAYFIELDS_LOCALBOOT " +  configOptions.get(KEY_PRODUCTONCLIENT_DISPLAYFIELDS_LOCALBOOT));


			List<String> configuredByService = de.uib.utilities.Globals.takeAsStringList( serverPropertyMap.get(KEY_PRODUCTONCLIENT_DISPLAYFIELDS_LOCALBOOT) );

			List<String> possibleValuesAccordingToService =  new ArrayList<String>();

			if (configOptions.get(KEY_PRODUCTONCLIENT_DISPLAYFIELDS_LOCALBOOT) != null)
				possibleValuesAccordingToService = (List<String>) configOptions.get(KEY_PRODUCTONCLIENT_DISPLAYFIELDS_LOCALBOOT).get("possibleValues");


			logging.debug(this, "getProductOnClients_displayFieldsLocalbootProducts() possibleValuesAccordingToService " + possibleValuesAccordingToService);
			//logging.debug(this, "getProductOnClients_displayFieldsLocalbootProducts() possibleValuesCurrentDefault " + getPossibleValuesProductOnClientDisplayfields_localboot());

			/*
			logging.debug(this, "getProductOnClients_displayFieldsLocalbootProducts() possibleValues " 
				+ configOptions.get(KEY_PRODUCTONCLIENT_DISPLAYFIELDS_LOCALBOOT).get("possibleValues") 
				+ ", " + configOptions.get(KEY_PRODUCTONCLIENT_DISPLAYFIELDS_LOCALBOOT).get("possibleValues").getClass());

			logging.debug(this, "getProductOnClients_displayFieldsLocalbootProducts() " + configuredByService);
			*/


			if (configuredByService.size() == 0
			        || !(
			            (new HashSet<String>(getPossibleValuesProductOnClientDisplayfields_localboot()))
			            .equals(
			                new HashSet<String>(possibleValuesAccordingToService)
			            )
			        )
			   )
			{
				//we did not initialize server property
				configuredByService = produceProductOnClientDisplayfields_localboot();
			}

			productOnClients_displayFieldsLocalbootProducts = new LinkedHashMap<String, Boolean>();

			//key names from de.uib.opsidatamodel.productstate.ProductState
			productOnClients_displayFieldsLocalbootProducts.put("productId", true);

			productOnClients_displayFieldsLocalbootProducts.put(ProductState.KEY_productName,
			        (configuredByService.indexOf(ProductState.KEY_productName) > -1)
			                                                   );

			productOnClients_displayFieldsLocalbootProducts.put(ProductState.KEY_targetConfiguration,
			        (configuredByService.indexOf(ProductState.KEY_targetConfiguration) > -1)
			                                                   );

			productOnClients_displayFieldsLocalbootProducts.put(ProductState.KEY_installationStatus, true);

			productOnClients_displayFieldsLocalbootProducts.put(ProductState.KEY_installationInfo,
			        (configuredByService.indexOf(ProductState.KEY_installationInfo) > -1)
			                                                   );

			productOnClients_displayFieldsLocalbootProducts.put(ProductState.KEY_actionRequest, true);

			productOnClients_displayFieldsLocalbootProducts.put(ProductState.KEY_productPriority,
			        (configuredByService.indexOf(ProductState.KEY_productPriority) > -1)
			                                                   );
			productOnClients_displayFieldsLocalbootProducts.put(ProductState.KEY_position,
			        (configuredByService.indexOf(ProductState.KEY_position) > -1)
			                                                   );


			productOnClients_displayFieldsLocalbootProducts.put(ProductState.KEY_lastStateChange,
			        (configuredByService.indexOf(ProductState.KEY_lastStateChange) > -1)
			                                                   );

			productOnClients_displayFieldsLocalbootProducts.put(ProductState.KEY_versionInfo, true);


		}

		return productOnClients_displayFieldsLocalbootProducts;
	}

	public void deleteSavedSearch(String name)
	{
		logging.debug(this, "deleteSavedSearch " + name);

		ArrayList<Object> readyObjects = new ArrayList<Object>();

		Map<String, Object> item;

		item = createNOMitem("UnicodeConfig");
		item.put("id", SavedSearch.CONFIG_KEY + "." + name);
		readyObjects.add(exec.jsonMap(item));

		item = createNOMitem("UnicodeConfig");
		item.put("id", SavedSearch.CONFIG_KEY + "." + name
		         + "." + SavedSearch.DESCRIPTION_KEY);
		readyObjects.add(exec.jsonMap(item));

		OpsiMethodCall omc = new OpsiMethodCall(
		                         "config_deleteObjects",
		                         new Object[] {exec.jsonArray(readyObjects)}
		                     );

		exec.doCall(omc);
		savedSearches.remove(name);


	}



	public void saveSearch(SavedSearch ob)
	{
		logging.debug(this, "saveSearch " + ob);

		ArrayList<Object> readyObjects = new ArrayList<Object>();
		//entry of serialization string
		readyObjects.add( produceConfigEntry("UnicodeConfig",
		                                     SavedSearch.CONFIG_KEY + "." + ob.getName(),
		                                     ob.getSerialization(),
		                                     ob.getDescription(),
		                                     false
		                                    )
		                );
		//description entry
		readyObjects.add( produceConfigEntry("UnicodeConfig",
		                                     SavedSearch.CONFIG_KEY + "." +  ob.getName()
		                                     + "." + SavedSearch.DESCRIPTION_KEY,
		                                     ob.getDescription(),
		                                     "",
		                                     true
		                                    )
		                );

		OpsiMethodCall omc = new OpsiMethodCall(
		                         "config_updateObjects",
		                         new Object[] {exec.jsonArray(readyObjects)}
		                     );

		exec.doCall(omc);
	}


	private List<String> getPossibleValuesProductOnClientDisplayfields_netboot()
	{
		ArrayList<String> possibleValues = new ArrayList<String>();
		possibleValues.add("productId");
		possibleValues.add(ProductState.KEY_productName);
		possibleValues.add(ProductState.KEY_installationStatus);
		possibleValues.add(ProductState.KEY_installationInfo);
		//combines
		//ProductState.KEY_actionProgress
		//ProductState.KEY_actionResult
		//ProductState.KEY_lastAction
		possibleValues.add(ProductState.KEY_actionRequest);
		possibleValues.add(ProductState.KEY_productPriority);
		possibleValues.add(ProductState.KEY_position);
		possibleValues.add(ProductState.KEY_lastStateChange);
		//ProductState.KEY_actionSequence
		//ProductState.KEY_actionSequence
		possibleValues.add(ProductState.KEY_targetConfiguration);
		possibleValues.add(ProductState.KEY_versionInfo);
		//combines
		//ProductState.KEY_productVersion
		//ProductState.KEY_packageVersion

		return possibleValues;
	}

	private List<String> getDefaultValuesProductOnClientDisplayfields_netboot()
	{
		List<String> result = new ArrayList<String>();

		result.add("productId");
		//result.add(ProductState.KEY_productName);
		result.add(ProductState.KEY_installationStatus);
		result.add(ProductState.KEY_installationInfo);
		result.add(ProductState.KEY_actionRequest);
		result.add(ProductState.KEY_versionInfo);

		return result;

	}



	private List<String> produceProductOnClientDisplayfields_netboot()
	{
		List<String> result  = getDefaultValuesProductOnClientDisplayfields_netboot();

		List<String> possibleValues = getPossibleValuesProductOnClientDisplayfields_netboot();

		//create config for service
		Map<String, Object> item = createNOMitem("UnicodeConfig");
		item.put("ident", KEY_PRODUCTONCLIENT_DISPLAYFIELDS_NETBOOT);
		item.put("description", "");
		item.put("defaultValues", exec.jsonArray(result));
		item.put("possibleValues", exec.jsonArray(possibleValues));
		item.put("editable", false);
		item.put("multiValue", true);

		logging.info(this, "produceProductOnClientDisplayfields_netboot");

		OpsiMethodCall omc = new OpsiMethodCall(
		                         "config_updateObjects",
		                         new Object[] {
		                             exec.jsonMap(item)
		                         }
		                     );

		exec.doCall(omc);

		return result;
	}



	public LinkedHashMap<String, Boolean> getProductOnClients_displayFieldsNetbootProducts()
	{
		if (productOnClients_displayFieldsNetbootProducts == null)
		{
			Map<String, java.util.List<Object>> serverPropertyMap = getConfigDefaultValues();

			List<String> configuredByService = de.uib.utilities.Globals.takeAsStringList( serverPropertyMap.get(KEY_PRODUCTONCLIENT_DISPLAYFIELDS_NETBOOT) );

			List<String> possibleValuesAccordingToService =  new ArrayList<String>();

			if (configOptions.get(KEY_PRODUCTONCLIENT_DISPLAYFIELDS_NETBOOT) != null)
				possibleValuesAccordingToService = (List<String>) configOptions.get(KEY_PRODUCTONCLIENT_DISPLAYFIELDS_NETBOOT).get("possibleValues");

			//logging.debug(this, "getProductOnClients_displayFieldsNetbootProducts() possibleValuesAccordingToService " + possibleValuesAccordingToService);
			//logging.debug(this, "getProductOnClients_displayFieldsNetbootProducts() possibleValuesCurrentDefault " + getPossibleValuesProductOnClientDisplayfields_netboot());

			/*
			logging.debug(this, "getProductOnClients_displayFieldsNetbootProducts() possibleValues " 
				+ configOptions.get(KEY_PRODUCTONCLIENT_DISPLAYFIELDS_NETBOOT).get("possibleValues") 
				+ ", " + configOptions.get(KEY_PRODUCTONCLIENT_DISPLAYFIELDS_NEtBOOT).get("possibleValues").getClass());

			logging.debug(this, "getProductOnClients_displayFieldsNetbootProducts() " + configuredByService);
			*/


			if (configuredByService.size() == 0
			        || !(
			            (new HashSet<String>(getPossibleValuesProductOnClientDisplayfields_netboot()))
			            .equals(
			                new HashSet<String>(possibleValuesAccordingToService)
			            )
			        )

			   )
			{
				//we did not initialize server property
				configuredByService = produceProductOnClientDisplayfields_netboot();
			}

			productOnClients_displayFieldsNetbootProducts = new LinkedHashMap<String, Boolean>();

			//key names from de.uib.opsidatamodel.productstate.ProductState
			productOnClients_displayFieldsNetbootProducts.put("productId", true);

			productOnClients_displayFieldsNetbootProducts.put(ProductState.KEY_productName,
			        (configuredByService.indexOf(ProductState.KEY_productName) > -1)
			                                                 );

			productOnClients_displayFieldsNetbootProducts.put(ProductState.KEY_targetConfiguration, false);
			productOnClients_displayFieldsNetbootProducts.put(ProductState.KEY_installationStatus, true);

			productOnClients_displayFieldsNetbootProducts.put(ProductState.KEY_installationInfo,
			        (configuredByService.indexOf(ProductState.KEY_installationInfo) > -1)
			                                                 );


			productOnClients_displayFieldsNetbootProducts.put(ProductState.KEY_actionRequest, true);


			productOnClients_displayFieldsNetbootProducts.put(ProductState.KEY_lastStateChange,
			        (configuredByService.indexOf(ProductState.KEY_lastStateChange) > -1)
			                                                 );

			productOnClients_displayFieldsNetbootProducts.put(ProductState.KEY_versionInfo, true);


		}

		return productOnClients_displayFieldsNetbootProducts;
	}

	private List<String> produceHost_displayFields(List<String> givenList)
	{
		boolean createOnServer = true;
		List<String> result = null;
		logging.info(this, "produceHost_displayFields configOptions.get(key) " + configOptions.get(KEY_HOST_DISPLAYFIELDS));

		List givenPossibleValues = null;
		List givenDefaultValues = null;

		if (configOptions.get(KEY_HOST_DISPLAYFIELDS) != null)
		{
			givenPossibleValues =  configOptions.get(KEY_HOST_DISPLAYFIELDS).getPossibleValues();
			givenDefaultValues = configOptions.get(KEY_HOST_DISPLAYFIELDS).getDefaultValues();
		}

		List<String> possibleValues = new ArrayList<String>();
		possibleValues.add( HostInfo.hostname_DISPLAY_FIELD_LABEL );
		possibleValues.add( HostInfo.clientDescription_DISPLAY_FIELD_LABEL );
		possibleValues.add( HostInfo.clientSessionInfo_DISPLAY_FIELD_LABEL );
		possibleValues.add( HostInfo.clientConnected_DISPLAY_FIELD_LABEL );
		possibleValues.add( HostInfo.lastSeen_DISPLAY_FIELD_LABEL );
		possibleValues.add( HostInfo.clientWanConfig_DISPLAY_FIELD_LABEL) ;
		possibleValues.add( HostInfo.clientIpAddress_DISPLAY_FIELD_LABEL ) ;
		possibleValues.add( HostInfo.clientMacAddress_DISPLAY_FIELD_LABEL );
		possibleValues.add( HostInfo.clientInventoryNumber_DISPLAY_FIELD_LABEL );
		possibleValues.add( HostInfo.clientUefiBoot_DISPLAY_FIELD_LABEL) ;
		possibleValues.add( HostInfo.created_DISPLAY_FIELD_LABEL );
		possibleValues.add( HostInfo.depotOfClient_DISPLAY_FIELD_LABEL);

		if (givenPossibleValues == null || !givenPossibleValues.equals(possibleValues))
		{
			createOnServer = true;
		}

		List<String> defaultValues = new ArrayList<String>();
		defaultValues.add( HostInfo.hostname_DISPLAY_FIELD_LABEL );
		defaultValues.add( HostInfo.clientDescription_DISPLAY_FIELD_LABEL );
		defaultValues.add( HostInfo.clientConnected_DISPLAY_FIELD_LABEL );
		defaultValues.add( HostInfo.lastSeen_DISPLAY_FIELD_LABEL );
		defaultValues.add( HostInfo.clientIpAddress_DISPLAY_FIELD_LABEL ) ;

		if (
		    givenList == null //no service property
		    ||
		    givenList.size() == 0 //bad configuration
		)
		{
			result = defaultValues;
			createOnServer = true;
		}
		else
		{
			result = givenList;
			// but not if we want to change the default values:

			/*
			List<String> diffList = new ArrayList(givenList);
			diffList.removeAll(possibleValues);
			if ( !diffList.isEmpty() ) //service property contains elements that are not known to this configed version, we remove them:
		{
				result = defaultValues.removeAll(diffList);
				createOnServer = true;
		}
			*/

		}

		if (createOnServer)
		{
			//create config for service
			Map<String, Object> item = createNOMitem("UnicodeConfig");
			item.put("ident", KEY_HOST_DISPLAYFIELDS);
			item.put("description", "");
			item.put("defaultValues", exec.jsonArray(result));
			item.put("possibleValues", exec.jsonArray(possibleValues));
			item.put("editable", false);
			item.put("multiValue", true);


			OpsiMethodCall omc = new OpsiMethodCall(
			                         "config_updateObjects",
			                         new Object[] {
			                             exec.jsonMap(item)
			                         }
			                     );

			exec.doCall(omc);
		}

		return result;
	}



	public LinkedHashMap<String, Boolean> getHost_displayFields()
	{
		if (host_displayFields == null)
		{
			Map<String, java.util.List<Object>> serverPropertyMap = getConfigDefaultValues();

			List<String> configuredByService = de.uib.utilities.Globals.takeAsStringList( serverPropertyMap.get(KEY_HOST_DISPLAYFIELDS) );

			//check if have to initialize the server property
			configuredByService = produceHost_displayFields(configuredByService);

			host_displayFields = new LinkedHashMap<String, Boolean>();
			host_displayFields.put( HostInfo.hostname_DISPLAY_FIELD_LABEL, true);
			//always shown, we put it here because of ordering and repeat the statement after the loop if it has been set to false

			for (String field : HostInfo.ORDERING_DISPLAY_FIELDS)
			{
				host_displayFields.put(  field, (configuredByService.indexOf( field  ) > -1 ) );
			}

			host_displayFields.put( HostInfo.hostname_DISPLAY_FIELD_LABEL, true );


		}

		return host_displayFields;
	}

	public java.util.List<String> getDisabledClientMenuEntries()
	{
		getConfigOptions();

		return de.uib.utilities.Globals.takeAsStringList( configDefaultValues.get( KEY_DISABLED_CLIENT_ACTIONS ) );
	}

	public java.util.List<String> getOpsiclientdExtraEvents()
	{
		logging.debug(this, "getOpsiclientdExtraEvents");
		getConfigOptions();
		if ( configDefaultValues.get( KEY_OPSICLIENTD_EXTRA_EVENTS ) == null)
		{
			logging.warning(this, "checkStandardConfigs:  since no values found setting values for  " + KEY_OPSICLIENTD_EXTRA_EVENTS);
		}

		java.util.List<String> result  = de.uib.utilities.Globals.takeAsStringList( configDefaultValues.get( KEY_OPSICLIENTD_EXTRA_EVENTS ) );

		logging.debug(this, "getOpsiclientdExtraEvents() " + result);

		return result;
	}

	private Object produceConfigEntry(String NOMtype, String key, Object value, String description)
	{
		return produceConfigEntry(NOMtype, key, value, description, true);
	}

	private Object produceConfigEntry(String NOMtype, String key, Object value, String description, boolean editable)
	{
		java.util.List possibleValues = new ArrayList<String>();
		possibleValues.add(value);

		//defaultValues
		java.util.List defaultValues = new ArrayList<String>();
		defaultValues.add(value);

		//create config for service
		Map<String, Object> item;// = createNOMitem("UnicodeConfig");

		item = createNOMitem(NOMtype);
		item.put("ident", key);
		item.put("description", description);
		item.put("defaultValues", exec.jsonArray(defaultValues));
		item.put("possibleValues", exec.jsonArray(possibleValues));
		item.put("editable", editable);
		item.put("multiValue", false);


		return exec.jsonMap(item);
	}



	private boolean  checkStandardConfigs()
	{
		boolean result = (getConfigOptions() != null);
		
		logging.info(this, "checkStandardConfigs, already there " + result);
		
		if (!result) // (getConfigOptions() == null);
			return false;

		//product_sort_algorithm
		java.util.List possibleValues = new ArrayList();
		possibleValues.add("algorithm1");
		possibleValues.add("algorithm2");

		//defaultValues
		java.util.List defaultValues = new ArrayList ();
		defaultValues =  configDefaultValues.get(KEY_PRODUCT_SORT_ALGORITHM);
		logging.info(this, "checkStandardConfigs:  from server product_sort_algorithm " + defaultValues);

		//create config for service
		Map<String, Object> item = createNOMitem("UnicodeConfig");
		item.put("ident", KEY_PRODUCT_SORT_ALGORITHM);
		item.put("description", "");
		item.put("defaultValues", exec.jsonArray(defaultValues));

		item.put("possibleValues", exec.jsonArray(possibleValues));
		item.put("editable", false);
		item.put("multiValue", false);

		ArrayList<Object> readyObjects = new ArrayList<Object>();
		readyObjects.add(exec.jsonMap(item));


		//extra columns for licence management, page licences reconciliation
		possibleValues = new ArrayList();
		possibleValues.add("description");
		possibleValues.add("inventoryNumber");
		possibleValues.add("notes");
		possibleValues.add("ipAddress");
		possibleValues.add("lastSeen");

		//defaultValues

		defaultValues =  configDefaultValues.get(KEY_HOST_EXTRA_DISPLAYFIELDS_IN_PanelLicencesReconciliation);
		if (defaultValues == null)
		{
			logging.warning(this, "checkStandardConfigs:  since no values found setting values for  "  + KEY_HOST_EXTRA_DISPLAYFIELDS_IN_PanelLicencesReconciliation );
			//key not yet configured
			defaultValues = new ArrayList ();
			//example for standard configuration other than empty
			//defaultValues.add("description");
			//defaultValues.add("notes");
		}
		//logging.debug(this, "defaultValues  KEY_HOST_EXTRA_DISPLAYFIELDS_IN_PanelLicencesReconciliation " + defaultValues);

		//create config for service
		item = createNOMitem("UnicodeConfig");
		item.put("ident",KEY_HOST_EXTRA_DISPLAYFIELDS_IN_PanelLicencesReconciliation );
		item.put("description", configed.getResourceValue("ConfigedMain.Licences.TabLicenceReconciliation.ExtraHostFields"));
		item.put("defaultValues", exec.jsonArray(defaultValues));

		item.put("possibleValues", exec.jsonArray(possibleValues));
		item.put("editable", false);
		item.put("multiValue", true);


		readyObjects.add(exec.jsonMap(item));


		//remote controls
		String key;
		String command;
		String description;


		//ping_kde
		key = "ping_linux";
		command = "xterm +hold -e ping %host%";
		description = "ping, started in a Linux environment";

		readyObjects.add( produceConfigEntry("UnicodeConfig",
		                                     RemoteControl.CONFIG_KEY + "." + key,
		                                     command,
		                                     description)
		                );
		readyObjects.add( produceConfigEntry("BoolConfig",
		                                     RemoteControl.CONFIG_KEY + "." + key
		                                     + "." + RemoteControl.EDITABLE_KEY,
		                                     true,
		                                     "(command may be edited)")
		                );
		//description entry
		readyObjects.add( produceConfigEntry("UnicodeConfig",
		                                     RemoteControl.CONFIG_KEY + "." + key
		                                     + "." + RemoteControl.DESCRIPTION_KEY,
		                                     description,
		                                     "")
		                );


		//ping_windows
		key = "ping_windows";
		command = "cmd.exe /c start ping %host%";
		description = "ping, started in a Windows terminal";

		readyObjects.add( produceConfigEntry("UnicodeConfig",
		                                     RemoteControl.CONFIG_KEY + "." + key,
		                                     command,
		                                     description)
		                );
		readyObjects.add( produceConfigEntry("BoolConfig",
		                                     RemoteControl.CONFIG_KEY + "." + key
		                                     + "." + RemoteControl.EDITABLE_KEY,
		                                     true,
		                                     "(command may be edited)")
		                );
		//description entry
		readyObjects.add( produceConfigEntry("UnicodeConfig",
		                                     RemoteControl.CONFIG_KEY + "." + key
		                                     + "." + RemoteControl.DESCRIPTION_KEY,
		                                     description,
		                                     "")
		                );


		//configuration of host menus

		key = KEY_DISABLED_CLIENT_ACTIONS;

		defaultValues =  configDefaultValues.get(key);
		if (defaultValues == null)
		{
			logging.warning(this, "checkStandardConfigs:  since no values found setting values for  " + KEY_DISABLED_CLIENT_ACTIONS  );
			//key not yet configured
			defaultValues = new ArrayList ();
			configDefaultValues.put(key, defaultValues);
		}


		possibleValues = new ArrayList();
		possibleValues.add( de.uib.configed.gui.MainFrame.ITEM_ADD_CLIENT );
		possibleValues.add( de.uib.configed.gui.MainFrame.ITEM_DELETE_CLIENT );
		possibleValues.add( de.uib.configed.gui.MainFrame.ITEM_FREE_LICENCES);


		item = createNOMitem("UnicodeConfig");
		item.put("id", key);
		item.put("description", "");
		item.put("defaultValues", exec.jsonArray(defaultValues));

		item.put("possibleValues", exec.jsonArray(possibleValues));
		item.put("editable", false);
		item.put("multiValue", true);


		readyObjects.add(exec.jsonMap(item));



		//saved searches

		key = "product_failed";

		StringBuffer val = new StringBuffer();
		val.append("{ \"version\" : \"2\", ");
		val.append("\"data\" : {");
		val.append(" \"element\" : null, ");
		val.append(" \"elementPath\" : null,");
		val.append(" \"operation\" : \"SoftwareOperation\", \"dataType\" : null, \"data\" : null, ");
		val.append(" \"children\" : [ { \"element\" : \"SoftwareActionResultElement\", \"elementPath\" : [ \"Product\", \"Action Result\" ], \"operation\" : \"StringEqualsOperation\", \"dataType\" : TextType, \"data\" : \"failed\", \"children\" : null } ] ");
		val.append("} }");

		String value = val.toString();

		description = "any product failed";

		readyObjects.add( produceConfigEntry("UnicodeConfig",
		                                     SavedSearch.CONFIG_KEY + "." + key,
		                                     value,
		                                     description)
		                );

		//description entry
		readyObjects.add( produceConfigEntry("UnicodeConfig",
		                                     SavedSearch.CONFIG_KEY + "." + key
		                                     + "." + SavedSearch.DESCRIPTION_KEY,
		                                     description,
		                                     "")
		                );


		//ping_windows
		key = "ping_windows";
		command = "cmd.exe /c start ping %host%";
		description = "ping, started in a Windows terminal";

		readyObjects.add( produceConfigEntry("UnicodeConfig",
		                                     RemoteControl.CONFIG_KEY + "." + key,
		                                     command,
		                                     description)
		                );
		readyObjects.add( produceConfigEntry("BoolConfig",
		                                     RemoteControl.CONFIG_KEY + "." + key
		                                     + "." + RemoteControl.EDITABLE_KEY,
		                                     true,
		                                     "(command may be edited)")
		                );
		//description entry
		readyObjects.add( produceConfigEntry("UnicodeConfig",
		                                     RemoteControl.CONFIG_KEY + "." + key
		                                     + "." + RemoteControl.DESCRIPTION_KEY,
		                                     description,
		                                     "")
		                );



		//configuration of host menus

		key = KEY_DISABLED_CLIENT_ACTIONS;

		defaultValues =  configDefaultValues.get(key);
		if (defaultValues == null)
		{
			logging.warning(this, "checkStandardConfigs:  since no values found setting values for  " +   KEY_DISABLED_CLIENT_ACTIONS);
			//key not yet configured
			defaultValues = new ArrayList ();
			configDefaultValues.put(key, defaultValues);
		}
		/*
		else
		{
			try
			{
				java.util.List defaultValuesOLD = configed.savedStates.saveServerConfigs.get(...).desalize()
			}
			
			
		*/	


		possibleValues = new ArrayList();
		possibleValues.add( de.uib.configed.gui.MainFrame.ITEM_ADD_CLIENT );
		possibleValues.add( de.uib.configed.gui.MainFrame.ITEM_DELETE_CLIENT );


		item = createNOMitem("UnicodeConfig");
		item.put("id", key);
		item.put("description", "");
		item.put("defaultValues", exec.jsonArray(defaultValues));

		item.put("possibleValues", exec.jsonArray(possibleValues));
		item.put("editable", false);
		item.put("multiValue", true);

		readyObjects.add(exec.jsonMap(item));

		key = KEY_SSH_DEFAULTWINUSER;
		defaultValues =  configDefaultValues.get(key);
		if (defaultValues == null)
		{
			logging.warning(this, "checkStandardConfigs:  since no values found setting values for  " + KEY_SSH_DEFAULTWINUSER);
			readyObjects.add( produceConfigEntry("UnicodeConfig",
			                                     key,
			                                     KEY_SSH_DEFAULTWINUSER_defaultvalue,
			                                     "default windows username for deploy-client-agent-script"
			                                    )
			                );
		}

		key = KEY_SSH_DEFAULTWINPW;
		defaultValues =  configDefaultValues.get(key);
		if (defaultValues == null)
		{
			logging.warning(this, "checkStandardConfigs:  since no values found setting values for  " + KEY_SSH_DEFAULTWINPW);
			readyObjects.add( produceConfigEntry("UnicodeConfig",
			                                     key,
			                                     KEY_SSH_DEFAULTWINPW_defaultvalue,
			                                     "default windows password for deploy-client-agent-script"
			                                    )
			                );
		}

		key = WORKBENCH;
		defaultValues =  configDefaultValues.get(key);
		if (defaultValues == null)
		{
			logging.warning(this, "checkStandardConfigs:  since no values found setting values for  " + WORKBENCH);
			readyObjects.add( produceConfigEntry("UnicodeConfig",
			                                     key,
			                                     WORKBENCH_defaultvalue,
			                                     "default path to opsiproducts"
			                                    )
			                );
		}
		else
		{
			logging.info(this, "checkStandardConfigs set WORKBENCH_defaultvalue to " +  (String) defaultValues.get(0));
			WORKBENCH_defaultvalue = (String) defaultValues.get(0);
		}
		
		// create and get default values for ssh =============================================================
		key = genericUserPart() + KEY_SSH_MENU_ACTIVE;
		defaultValues =  configDefaultValues.get(key);
		if (defaultValues == null)
		{
			logging.warning(this, "checkStandardConfigs:  since no values found setting values for  " + key);
			readyObjects.add( produceConfigEntry("BoolConfig",	key, KEY_SSH_MENU_ACTIVE_defaultvalue, KEY_SSH_MENU_ACTIVE_description) );
		}
		else
		{
			logging.info(this, "checkStandardConfigs set KEY_SSH_MENU_ACTIVE_defaultvalue to " +  (Boolean) defaultValues.get(0));
			KEY_SSH_MENU_ACTIVE_defaultvalue = (Boolean) defaultValues.get(0);
		}
		
		key = genericUserPart() + KEY_SSH_SHELL_ACTIVE;
		defaultValues =  configDefaultValues.get(key);
		if (defaultValues == null)
		{
			logging.warning(this, "checkStandardConfigs:  since no values found setting values for  " + key);
			readyObjects.add( produceConfigEntry("BoolConfig", key, KEY_SSH_SHELL_ACTIVE_defaultvalue, KEY_SSH_SHELL_ACTIVE_description) );
		}
		else
		{
			logging.info(this, "checkStandardConfigs set KEY_SSH_SHELL_ACTIVE_defaultvalue to " +  (Boolean) defaultValues.get(0));
			KEY_SSH_SHELL_ACTIVE_defaultvalue = (Boolean) defaultValues.get(0);
		}
		
		key = genericUserPart() + KEY_SSH_CONFIG_ACTIVE;
		defaultValues =  configDefaultValues.get(key);
		if (defaultValues == null)
		{
			logging.warning(this, "checkStandardConfigs:  since no values found setting values for  " + key);
			readyObjects.add( produceConfigEntry("BoolConfig", key, KEY_SSH_CONFIG_ACTIVE_defaultvalue, KEY_SSH_CONFIG_ACTIVE_description) );
		}
		else
		{
			logging.info(this, "checkStandardConfigs set KEY_SSH_CONFIG_ACTIVE_defaultvalue to " +  (Boolean) defaultValues.get(0));
			KEY_SSH_CONFIG_ACTIVE_defaultvalue = (Boolean) defaultValues.get(0);
		}
		
		key = genericUserPart() + KEY_SSH_CONTROL_ACTIVE;
		defaultValues =  configDefaultValues.get(key);
		if (defaultValues == null)
		{
			logging.warning(this, "checkStandardConfigs:  since no values found setting values for  " + key);
			readyObjects.add( produceConfigEntry("BoolConfig", key, KEY_SSH_CONTROL_ACTIVE_defaultvalue, KEY_SSH_CONTROL_ACTIVE_description) );
		}
		else
		{
			logging.info(this, "checkStandardConfigs set KEY_SSH_CONTROL_ACTIVE_defaultvalue to " +  (Boolean) defaultValues.get(0));
			KEY_SSH_CONTROL_ACTIVE_defaultvalue = (Boolean) defaultValues.get(0);
		}
		
		key = genericUserPart() + KEY_SSH_COMMANDS_ACTIVE;
		defaultValues =  configDefaultValues.get(key);
		if (defaultValues == null)
		{
			logging.warning(this, "checkStandardConfigs:  since no values found setting values for  " + key);
			readyObjects.add( produceConfigEntry("BoolConfig", key, KEY_SSH_COMMANDS_ACTIVE_defaultvalue, KEY_SSH_COMMANDS_ACTIVE_description) );
		}
		else
		{
			logging.info(this, "checkStandardConfigs set KEY_SSH_COMMANDS_ACTIVE_defaultvalue to " +  (Boolean) defaultValues.get(0));
			KEY_SSH_COMMANDS_ACTIVE_defaultvalue = (Boolean) defaultValues.get(0);
		}
		
		if (KEY_USER_REGISTER_VALUE)
		{
			// user configs =========================
			key = userPart() + KEY_SSH_MENU_ACTIVE;
			defaultValues =  configDefaultValues.get(key);
			if (defaultValues == null)
			{
				logging.warning(this, "checkStandardConfigs:  since no values found setting values for  " + key);
				readyObjects.add( produceConfigEntry("BoolConfig",	key, KEY_SSH_MENU_ACTIVE_defaultvalue, KEY_SSH_MENU_ACTIVE_description) );
			}

			key = userPart() + KEY_SSH_SHELL_ACTIVE;
			defaultValues =  configDefaultValues.get(key);
			if (defaultValues == null)
			{
				logging.warning(this, "checkStandardConfigs:  since no values found setting values for  " + key);
				readyObjects.add( produceConfigEntry("BoolConfig", key, KEY_SSH_SHELL_ACTIVE_defaultvalue, KEY_SSH_SHELL_ACTIVE_description) );
			}

			key = userPart() + KEY_SSH_CONFIG_ACTIVE;
			defaultValues =  configDefaultValues.get(key);
			if (defaultValues == null)
			{
				logging.warning(this, "checkStandardConfigs:  since no values found setting values for  " + key);
				readyObjects.add( produceConfigEntry("BoolConfig", key, KEY_SSH_CONFIG_ACTIVE_defaultvalue, KEY_SSH_CONFIG_ACTIVE_description) );
			}

			key = userPart() + KEY_SSH_CONTROL_ACTIVE;
			defaultValues =  configDefaultValues.get(key);
			if (defaultValues == null)
			{
				logging.warning(this, "checkStandardConfigs:  since no values found setting values for  " + key);
				readyObjects.add( produceConfigEntry("BoolConfig", key, KEY_SSH_CONTROL_ACTIVE_defaultvalue, KEY_SSH_CONTROL_ACTIVE_description) );
			}

			key = userPart() + KEY_SSH_COMMANDS_ACTIVE;
			defaultValues =  configDefaultValues.get(key);
			if (defaultValues == null)
			{
				logging.warning(this, "checkStandardConfigs:  since no values found setting values for  " + key);
				readyObjects.add( produceConfigEntry("BoolConfig", key, KEY_SSH_COMMANDS_ACTIVE_defaultvalue, KEY_SSH_COMMANDS_ACTIVE_description) );
			}
		}

		// configuration of opsiclientd extra events

		key = KEY_OPSICLIENTD_EXTRA_EVENTS;

		defaultValues =  configDefaultValues.get(key);
		if (defaultValues == null)
		{
			logging.warning(this, "checkStandardConfigs:  since no values found setting values for  " + KEY_OPSICLIENTD_EXTRA_EVENTS);
			//key not yet configured
			defaultValues  = new ArrayList();


			defaultValues.add(  OPSI_CLIENTD_EVENT_on_demand  );
			//defaultValues.add( OPSI_CLIENTD_EVENT_silent_install );

			configDefaultValues.put(key, defaultValues);


			possibleValues = new ArrayList();

			possibleValues.add(  OPSI_CLIENTD_EVENT_on_demand  );
			possibleValues.add(  OPSI_CLIENTD_EVENT_silent_install );


			item = createNOMitem("UnicodeConfig");

			item = createNOMitem("UnicodeConfig");
			item.put("id", key);
			item.put("description", "");
			item.put("defaultValues", exec.jsonArray(defaultValues));

			item.put("possibleValues", exec.jsonArray(possibleValues));
			item.put("editable", true);
			item.put("multiValue", true);


			readyObjects.add(exec.jsonMap(item));
		}

		//do update
		OpsiMethodCall omc = new OpsiMethodCall(
		                         "config_updateObjects",
		                         new Object[] {exec.jsonArray(readyObjects)}
		                     );

		exec.doCall(omc);

		return true;
	}

	//opsi module information
	@Override
	public void opsiInformationRequestRefresh()
	{
		opsiInformation = null;
	}

	@Override
	public Date getOpsiExpiresDate()
	{
		retrieveOpsiModules();
		return expiresDate;
	}

	@Override
	public Map<String, Object> getOpsiModulesInfos()
	{
		retrieveOpsiModules();
		return opsiModulesDisplayInfo;
	}


	@Override
	final public void retrieveOpsiModules()
	{
		logging.info(this, "retrieveOpsiModules ");

		if (opsiInformation == null)
		{
			OpsiMethodCall omc = new OpsiMethodCall(
			                         "getOpsiInformation_hash",
			                         new String[]{});
			opsiInformation = new HashMap<String, Object>();
			opsiModulesInfo = new HashMap<String, Object>();
			opsiModulesDisplayInfo = new HashMap<String, Object>();
			opsiVersion = "";
			opsiModulesPermissions = new HashMap<String, ModulePermissionValue>();
			opsiModules = new HashMap<String, Boolean>(); //has the actual signal if a module is active
			
			Map<String, Object> opsiCountModules = new HashMap<String, Object>(); 
			String expiresKey = de.uib.opsidatamodel.permission.ModulePermissionValue.keyExpires;

			try
			{
				logging.info(this, "getOpsiInformation_hash, method signature: " + getMethodSignature("getOpsiInformation_hash"));
				if (getMethodSignature("getOpsiInformation_hash") != NONE_LIST) //method does not exist before opsi 3.4
				{
					opsiInformation = exec.getMapResult ( omc );
				}

				opsiVersion = (String) opsiInformation.get("opsiVersion");
				logging.info(this, "opsi version information " + opsiVersion);

				final ArrayList<String> missingModulesPermissionInfo = new ArrayList<String>();

				opsiModulesInfo = exec.getMapFromItem(opsiInformation.get("modules"));
				
				opsiModulesInfo.remove("signature");
				logging.info(this, "opsi module information " + opsiModulesInfo);
				opsiModulesInfo.remove("valid");
				
				opsiModulesDisplayInfo = new HashMap<String, Object>( opsiModulesInfo );
				//opsiModulesDisplayInfo.remove("signature");
				//opsiModulesDisplayInfo.remove("valid");
				

				ExtendedDate validUntil = ExtendedDate.INFINITE;
				if (opsiModulesInfo.get(expiresKey) != null)
					validUntil = new ExtendedDate( opsiModulesInfo.get(expiresKey) );


				opsiCountModules  = exec.getMapFromItem(opsiInformation.get("realmodules"));
				getHostInfoCollections().retrieveOpsiHosts();

				
				ExtendedInteger globalMaxClients = ExtendedInteger.ZERO;
				ExtendedInteger countClients = new ExtendedInteger( hostInfoCollections.getCountClients() );
				
			
				Date today = new Date();
				Calendar nowCal = Calendar.getInstance();
				nowCal.setTime( today );


				//read in modules
				for (String key : opsiModulesInfo.keySet())
				{
					ModulePermissionValue modulePermission  = new ModulePermissionValue( exec, opsiModulesInfo.get(key), validUntil); 
					// the default validUntil is historically assumed if nothing is specified);
					logging.info(this, "handle modules key, modulePermission  " + modulePermission);
					Boolean permissionCheck = modulePermission.getBoolean();
					opsiModulesPermissions.put(key, modulePermission); // first try
					if (permissionCheck != null)
						opsiModules.put(key, permissionCheck);
				}
					
				logging.info(this, "modules resulting step 0  " + opsiModules);
				
				//read in opsi count modules ("realmodules")
				for (String key : opsiCountModules.keySet())
				{
					ModulePermissionValue modulePermission  = new ModulePermissionValue( exec, opsiCountModules.get(key), validUntil); 
					// the default validUntil is historically assumed if nothing is specified);
					logging.info(this, "handle modules key, modulePermission  " + modulePermission);
					Boolean permissionCheck = modulePermission.getBoolean();
					opsiModulesPermissions.put( key, modulePermission ); //replace value got from modulesInfo
					
					if ( opsiCountModules.get(key) != null )
						opsiModulesDisplayInfo.put(key, opsiCountModules.get(key)); //overwrite value from modules by the opsiCountModules value if it exists)
				}
				
				logging.info(this, "modules resulting step 1 " + opsiModules);
				
				//set values for modules checked by configed
				for (String key : ModulePermissionValue.MODULE_CHECKED.keySet())
				{
					
					ModulePermissionValue modulePermission = opsiModulesPermissions.get(key);
					ExtendedInteger maxClientsForThisModule = modulePermission.getMaxClients();
					ExtendedDate expiresForThisModule = modulePermission.getExpires();
					
					if ( modulePermission.getBoolean() != null) 
					{
						opsiModules.put( key, modulePermission.getBoolean() );
						logging.info(this, " retrieveOpsiModules, set opsiModules for key " + key + ": " + modulePermission.getBoolean());
					}
					else
					{
						opsiModules.put(key, true);
						
						if ( maxClientsForThisModule.equals( ExtendedInteger.ZERO ) )
						{
							opsiModules.put(key, false);
						}
						else
						{
							int limit = (int) (maxClientsForThisModule.getNumber() * 1.05);
				
							if ( hostInfoCollections.getCountClients()  > limit )
							{
								opsiModules.put(key, false);
							}
	
							else 
							{
								if (!expiresForThisModule.equals ( ExtendedDate.INFINITE ) )
								{
									Date expiresDate = expiresForThisModule.getDate();
			
									if (today.after( expiresDate ) )
									{
										opsiModules.put(key, false);
									}
								}
							}
						}
					}
				}
					
				logging.info(this, "modules resulting step 2  " + opsiModules);
				
				
				
				for (String key : ModulePermissionValue.MODULE_CHECKED.keySet())
				{
					boolean problemToIndicateOnStart = !(opsiModules.get(key));
					ModulePermissionValue modulePermission = opsiModulesPermissions.get(key);
					ExtendedInteger maxClientsForThisModule = modulePermission.getMaxClients();
					ExtendedDate expiresForThisModule = modulePermission.getExpires();
					
					
					if ( maxClientsForThisModule.equals( ExtendedInteger.ZERO) )
						problemToIndicateOnStart = false;
					
					if (problemToIndicateOnStart)
					{
						if( key.equals("linux_agent") )
							problemToIndicateOnStart = false;
						
						else if (key.equals("userroles") && !isUserRegisterActivated())
							problemToIndicateOnStart = false;
					}
					
					if (problemToIndicateOnStart)
					{
						logging.info(this, "retrieveOpsiModules " + key + " , maxClients " + maxClientsForThisModule + " count " + hostInfoCollections.getCountClients());
		
						missingModulesPermissionInfo.add("Module " + key + ", maximum clients: " +maxClientsForThisModule);
			
					
						if (!expiresForThisModule.equals ( ExtendedDate.INFINITE ) )
						{
							Date expiresDate = expiresForThisModule.getDate();
							Calendar noticeCal = Calendar.getInstance();
							noticeCal.setTime( expiresDate );
							noticeCal.add(Calendar.DAY_OF_MONTH, -14);
	
							if (nowCal.after(noticeCal))
							{
								missingModulesPermissionInfo.add("Module " + key + ", expires: " +  expiresForThisModule);
							}
	
						}
					}
				}
						
				logging.info(this, "modules resulting  " + opsiModules);
				
				
				logging.info(this, " retrieveOpsiModules missingModulesPermissionInfos " +   missingModulesPermissionInfo);
					 
				//if (missingModulesPermissionInfo.size() > 0)
				//configed.endApp(2);

				if (missingModulesPermissionInfo.size() > 0)
				{	javax.swing.SwingUtilities.invokeLater(new Thread(){
						        public void run()
						        {
							        StringBuffer info = new StringBuffer(configed.getResourceValue("Permission.modules.clientcount.1"));
							        info.append(" (" + hostInfoCollections.getCountClients() + ") ");
							        info.append(configed.getResourceValue("Permission.modules.clientcount.2"));
							        info.append(":\n");
							        for (String moduleInfo : missingModulesPermissionInfo)
							        {
								        info.append(moduleInfo);
								        info.append("\n");
							        }

							        logging.info(this, "missingModules " + info);

							        javax.swing.JOptionPane.showMessageDialog(	de.uib.configed.Globals.mainContainer,
							                info.toString(),
							                configed.getResourceValue("Permission.modules.title"),
							                javax.swing.JOptionPane.OK_OPTION);

							        //configed.endApp(2);

						        }
					        }
					   );
				}

			}
			catch(Exception ex)
			{
				logging.logTrace(ex);
				logging.warning("opsi module information problem: " + ex);
			}



		

			//if (missingModulesPermissionInfo.size() == 0)
			{
				withLinuxAgent =  (opsiModules.get("linux_agent") != null)&& ((Boolean) opsiModules.get("linux_agent"));
				withLicenceManagement = (opsiModules.get("license_management") != null) && ((Boolean) opsiModules.get("license_management"));
				withLocalImaging =  (opsiModules.get("local_imaging") != null) && ((Boolean) opsiModules.get("local_imaging"));
				//logging.info(this, "withLocalImaging  "  +  withLocalImaging);
				//withScalability1 = ((Boolean) opsiModules.get("scalability1") == true);
				withMySQL = (opsiModules.get("mysql_backend") != null) && ((Boolean) opsiModules.get("mysql_backend"));
				withUEFI =  (opsiModules.get("uefi") != null) && ((Boolean) opsiModules.get("uefi"));
				withWAN =  (opsiModules.get("vpn") != null) && ((Boolean) opsiModules.get("vpn"));
				withUserRoles  =  (opsiModules.get("userroles") != null) && ((Boolean) opsiModules.get("userroles"));
			}

			//System.out.println(" opsiModules " + opsiModules);
			//System.exit(0);


			//withTreeView = ((Boolean) opsiModules.get("treeview") == true);
			
			logging.info(this, "retrieveOpsiModules opsiCountModules " + opsiCountModules);
			logging.info(this, "retrieveOpsiModules opsiModulesPermissions " + opsiModulesPermissions);
			logging.info(this, "retrieveOpsiModules opsiModules " + opsiModules);
			
			//configed.endApp(2);

		}

		logging.info(this, " withMySQL " + withMySQL);
		logging.info(this, " withLinuxAgent " + withLinuxAgent);
		logging.info(this, " withUserRoles " + withUserRoles);

		//logging.info(this, " with license management  " + withLicenceManagement);
		//System.exit(0);

	}

	@Override
	public boolean isWithLocalImaging()
	{
		retrieveOpsiModules();
		return withLocalImaging;
	}

	@Override
	public boolean isWithMySQL()
	{
		return withMySQL;
		/*
		logging.info(this, "isWithMySQL ( " 
			+ withMySQL + " ) " + opsiModules.get("mysql_backend") ); 
		return  (opsiModules.get("mysql_backend") != null) && ((Boolean) opsiModules.get("mysql_backend"));
		*/
	}

	@Override
	public boolean isWithUEFI()
	{
		return withUEFI;
	}

	@Override
	public boolean isWithWAN()
	{
		return withWAN;
	}

	@Override
	public boolean isWithLinuxAgent()
	{
		return withLinuxAgent;
	}

	@Override
	public boolean isWithLicenceManagement()
	{
		return withLicenceManagement;
	}

	@Override
	public boolean isWithUserRoles()
	{
		return withUserRoles;
	}


	/*
	public boolean isWithScalability1()
{
		return withScalability1;
}
	*/


	public String getOpsiVersion()
	{
		retrieveOpsiModules();
		if (opsiVersion == null || opsiVersion.equals("") )
			opsiVersion = "< 3.4";

		return opsiVersion;
	}


	/**
	* Test if sshcommand methods exists
	* @param method name
	* @return True if exists
	*/
	public boolean checkSSHCommandMethod(String method )
	{
		if (getMethodSignature(method) != NONE_LIST) //method does not exist before opsi 3.4
		{
			logging.info(this, "checkSSHCommandMethod " + method + " exists");
			return true;
			// opsiInformation = exec.getMapResult ( omc );
		}
		logging.info(this, "checkSSHCommandMethod " + method + " does not exists");
		return false;
	}
	/**
	* Exec the python-opsi command "SSHCommand_getObjects"
	* @return command objects 
	*/
	public java.util.List<Map<java.lang.String,java.lang.Object>> retrieveCommandList()
	{
		logging.info(this, "retrieveCommandList ");

		java.util.List<Map<java.lang.String,java.lang.Object>> sshCommands = exec.getListOfMaps(
		            new OpsiMethodCall(
		                "SSHCommand_getObjects",
		                new Object[]{/* callAttributes, callFilter*/}
		            )
		        );
		logging.debug(this, "retrieveCommandList commands " + sshCommands);
		return sshCommands;
	}
	/**
	* Exec a python-opsi command 
	* @param method name
	* @param jsonObjects to do sth
	* @return result True if everything is ok
	*/
	public boolean doActionSSHCommand(String method, List<Object> jsonObjects)
	{
		logging.info(this, "doActionSSHCommand method " + method);
		if (isGlobalReadOnly())
			return false;
		OpsiMethodCall omc = new OpsiMethodCall(
		                         method,
		                         new Object[] {exec.jsonArray(jsonObjects)}
		                     );
		boolean result = exec.doCall(omc);
		logging.info(this, "doActionSSHCommand method " + method + " result " + result);
		return result;
	}
	/**
	* Exec the python-opsi command "SSHCommand_deleteObjects"
	* @param jsonObjects to remove
	* @return result True if successfull
	*/
	public boolean deleteSSHCommand(List<String> jsonObjects)
	{
		// return doActionSSHCommand("SSHCommand_deleteObjects", jsonObjects); // Strings not object!
		logging.info(this, "deleteSSHCommand ");
		if (isGlobalReadOnly())
			return false;
		OpsiMethodCall omc = new OpsiMethodCall(
		                         "SSHCommand_deleteObjects",
		                         new Object[] {exec.jsonArray(jsonObjects)}
		                     );
		boolean result = exec.doCall(omc);
		logging.info(this, "deleteSSHCommand result " + result);
		return result;
	}
	/**
	* Exec the python-opsi command "SSHCommand_createObjects"
	* @param jsonObjects to create
	* @return result True if successfull
	*/
	public boolean createSSHCommand(List<Object> jsonObjects)
	{
		return doActionSSHCommand("SSHCommand_createObjects", jsonObjects);
	}

	/**
	* Exec the python-opsi command "SSHCommand_updateObjects"
	* @param jsonObjects to update
	* @return result True if successfull
	*/
	public boolean updateSSHCommand(List<Object> jsonObjects)
	{
		return doActionSSHCommand("SSHCommand_updateObjects", jsonObjects);
	}
}



