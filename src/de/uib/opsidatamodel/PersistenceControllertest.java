/*
 * configed - configuration editor for client work stations in opsi
 * (open pc server integration) www.opsi.org
 *
 * Copyright (C) 2000-2015 uib.de
 *
 *
 */

package de.uib.opsidatamodel;

import java.util.*;
import de.uib.opsicommand.*;
import de.uib.configed.*;
import de.uib.configed.type.*;
import de.uib.configed.type.licences.*;
import de.uib.utilities.observer.*;
import de.uib.opsidatamodel.dbtable.*;
import de.uib.opsidatamodel.permission.*;
import de.uib.utilities.datastructure.*;
import de.uib.utilities.logging.*;


/**
 *   PersistenceController
 *   description: abstract methods for retrieving and setting data
 *    
 *  copyright:     Copyright (c) 2000-2016
 *  organization: uib.de
 * @author  R. Roeder 
 */
public abstract class PersistenceController
	implements DataRefreshedObservable, DataLoadingObservable
{
	public final static String CLIENT_GLOBAL_SEPARATOR = "/";
	
	public final static Set<String> KEYS_OF_HOST_PROPERTIES_NOT_TO_EDIT = new HashSet();
	{
		KEYS_OF_HOST_PROPERTIES_NOT_TO_EDIT.add("type");
		KEYS_OF_HOST_PROPERTIES_NOT_TO_EDIT.add("id");
	}
	
	public final static String KEY_PRODUCTONCLIENT_DISPLAYFIELDS_LOCALBOOT = "configed.productonclient_displayfields_localboot";
	public final static String KEY_PRODUCTONCLIENT_DISPLAYFIELDS_NETBOOT = "configed.productonclient_displayfields_netboot";
	public final static String KEY_HOST_DISPLAYFIELDS = "configed.host_displayfields";
	public final static String KEY_HOST_EXTRA_DISPLAYFIELDS_IN_PanelLicencesReconciliation= "configed.license_inventory_extradisplayfields";
	
	public final static String KEY_DISABLED_CLIENT_ACTIONS = "configed.host_actions_disabled";
	
	public final static String KEY_OPSICLIENTD_EXTRA_EVENTS = "configed.opsiclientd_events";
	public final static String OPSI_CLIENTD_EVENT_on_demand = "on_demand";
	public final static String OPSI_CLIENTD_EVENT_silent_install = "silent_install";
	
	public final static String KEY_PRODUCT_SORT_ALGORITHM = "product_sort_algorithm";
	
	public final static String KEY_CHOICES_FOR_WOL_DELAY = "wol_delays_sec";
	
	public final static String localImageRestoreProductKey = "opsi-local-image-restore";
	public final static String localImagesListPropertyKey = "imagefiles_list";
	public final static String localImageToRestorePropertyKey = "imagefile";
	
	public final static String CONFIG_DEPOT_ID = "clientconfig.depot.id";

	public final static String KEY_SSH_DEFAULTWINUSER = "configed.ssh.deploy-client-agent.default.user";
	public final static String KEY_SSH_DEFAULTWINUSER_defaultvalue = "Adminuser";
	public final static String KEY_SSH_DEFAULTWINPW = "configed.ssh.deploy-client-agent.default.password";
	public final static String KEY_SSH_DEFAULTWINPW_defaultvalue = "nt123";


	public final static String CONFIG_CLIENTD_EVENT_GUISTARTUP = "opsiclientd.event_gui_startup.active";
	public final static Boolean CONFIG_CLIENTD_EVENT_GUISTARTUP_WAN_VALUE = false;
	
	public final static String CONFIG_CLIENTD_EVENT_GUISTARTUP_USERLOGGEDIN = "opsiclientd.event_gui_startup{user_logged_in}.active";
	public final static Boolean CONFIG_CLIENTD_EVENT_GUISTARTUP_USERLOGGEDIN_WAN_VALUE = false;
	
	public final static String CONFIG_CLIENTD_EVENT_NET_CONNECTION = "opsiclientd.event_net_connection.active";
	public final static Boolean CONFIG_CLIENTD_EVENT_NET_CONNECTION_WAN_VALUE = true;

	public final static String CONFIG_CLIENTD_EVENT_TIMER = "opsiclientd.event_timer.active";
	public final static Boolean CONFIG_CLIENTD_EVENT_TIMER_WAN_VALUE = true;


	public final static String CONFIG_DHCPD_FILENAME =  "clientconfig.dhcpd.filename";
	public final static String EFI_DHCPD_FILENAME = "linux/pxelinux.cfg/elilo.efi";
	public final static String EFI_DHCPD_FILENAME_X86 = "linux/pxelinux.cfg/elilo-x86.efi";
	//public final static String HOST_KEY_UEFI_BOOT = "uefi";
	public final static String ELILO_STRING = "elilo";
	
	public final static String KEY_USER_ROOT = OpsiPermission.CONFIGKEY_STR_USER;
	public final static String PARTKEY_USER_PRIVILEGE_GLOBAL_READONLY = 
		 OpsiPermission.CONFIGKEY_STR_PRIVILEGE + "." +  OpsiPermission.CONFIGKEY_STR_HOST + "."  +  OpsiPermission.CONFIGKEY_STR_ALLHOSTS + "."  +  OpsiPermission.CONFIGKEY_STR_READONLY;
		 //privilege.host.all.readonly : boolean
	public final static String PARTKEY_USER_PRIVILEGE_SERVER_READWRITE = 
		OpsiPermission.CONFIGKEY_STR_PRIVILEGE + "." +   OpsiPermission.CONFIGKEY_STR_HOST + "."  + OpsiPermission.CONFIGKEY_STR_SERVER + "." +  OpsiPermission.CONFIGKEY_STR_READWRITE;
		//privilege.host.opsiserver.readwrite boolean
	public final static String PARTKEY_USER_PRIVILEGE_DEPOTACCESS_ONLY_AS_SPECIFIED = 
		OpsiPermission.CONFIGKEY_STR_PRIVILEGE + "." +    OpsiPermission.CONFIGKEY_STR_HOST + "."  + OpsiPermission.CONFIGKEY_STR_DEPOT + "." +  OpsiPermission.CONFIGKEY_STR_DEPOTACCESSCONTROLLED;
		//privilege.host.depotaccess.configured; //boolean
	public final static String PARTKEY_USER_PRIVILEGE_DEPOTS_ACCESSIBLE =
		OpsiPermission.CONFIGKEY_STR_PRIVILEGE + "." +    OpsiPermission.CONFIGKEY_STR_HOST + "." +  OpsiPermission.CONFIGKEY_STR_DEPOT + "." +  OpsiPermission.CONFIGKEY_STR_DEPOTLIST;
		//privilege.host.depotaccess.depots : multivalue
	public final static String KEY_USER_REGISTER =  KEY_USER_ROOT + ".{}.register"; //boolean
	public static Boolean KEY_USER_REGISTER_VALUE =  null;
	
	// public final static String KEY_USER_SSH_REGISTER = "register.ssh";
	// public final static String KEY_USER_SSH_REGISTER_description = "if false: do not create new user specific ssh configs (use defaults)";
	// public static boolean KEY_USER_SSH_REGISTER_defaultvalue = false;

	public final static String KEY_SSH_MENU_ACTIVE = "ssh.menu_main.active";
	public final static String KEY_SSH_MENU_ACTIVE_description =  "de-activate menu server-console" ;
	public static boolean KEY_SSH_MENU_ACTIVE_defaultvalue = true;

	public final static String KEY_SSH_CONFIG_ACTIVE = "ssh.menu_config.active";
	public final static String KEY_SSH_CONFIG_ACTIVE_description = "de-activate menu to configure ssh connection" ;
	public static boolean KEY_SSH_CONFIG_ACTIVE_defaultvalue = false;

	public final static String KEY_SSH_CONTROL_ACTIVE = "ssh.menu_control.active";
	public final static String KEY_SSH_CONTROL_ACTIVE_description = "de-activate the command control";
	public static boolean KEY_SSH_CONTROL_ACTIVE_defaultvalue = false;
	
	public final static String KEY_SSH_SHELL_ACTIVE = "ssh.menu_terminal.active";
	public final static String KEY_SSH_SHELL_ACTIVE_description = "de-activate terminal to ssh server";
	public static boolean KEY_SSH_SHELL_ACTIVE_defaultvalue = false;

	public final static String KEY_SSH_COMMANDS_ACTIVE = "ssh.menus_commands.active";
	public final static String KEY_SSH_COMMANDS_ACTIVE_description = "de-activate all executable commands in ssh menu";
	public static boolean KEY_SSH_COMMANDS_ACTIVE_defaultvalue = false;
	/* 
	
	model
	
		cf.
		de.uib.configed.type.user.UserRole.getRole()
	
	if GLOBAL_READONLY (can be specified by user group or in config)
		everything readonly (ignoring a config  SERVER_READWRITE)
		
		if DEPOTACCESS_ONLY_AS_SPECIFIED: (default general access!)
			access only to specified depots
			
	else 
		if SERVER_READWRITE false or not set:
			write access to server
		
		if DEPOTACCESS_AS_SPECIFIED (default general access!)
			access only to specified depots
			(of course, the user could change this in case of server_readwrite)
			
	
			
	*/
			
		
	
	
	
	public final static java.util.List BOOLEAN_VALUES = new ArrayList<Boolean>();
	static{
		BOOLEAN_VALUES.add(true);
		BOOLEAN_VALUES.add(false);
	}
	
	public static TreeMap<String, String> PROPERTYCLASSES_SERVER;
	static{
		PROPERTYCLASSES_SERVER = new TreeMap<String, String>();
		PROPERTYCLASSES_SERVER.put( "", "general configuration items");
		PROPERTYCLASSES_SERVER.put( "clientconfig", "network configuration");
		PROPERTYCLASSES_SERVER.put( SavedSearch.CONFIG_KEY, "<html></p>saved search configurations ,<br />do not edit!</p></html>" );
		PROPERTYCLASSES_SERVER.put( RemoteControl.CONFIG_KEY, "<html><p>remote control call,<br />not client specific</p></html>" );
		PROPERTYCLASSES_SERVER.put( "opsiclientd", "<html>entries for the opsiclientd.conf</html>" );
		//PROPERTYCLASSES_SERVER.put( "opsi-local-image", "" );
		PROPERTYCLASSES_SERVER.put( "software-on-demand", "<html>software on demand configuration,<br />not client specific</html>");
		PROPERTYCLASSES_SERVER.put( OpsiPermission.CONFIGKEY_STR_USER, "<html>user privileges configuration,<br />not client specific</html>");
	}
	
	public static TreeMap<String, String> PROPERTYCLASSES_CLIENT;
	static{
		PROPERTYCLASSES_CLIENT = new TreeMap<String, String>();
		PROPERTYCLASSES_CLIENT.put( "", "general configuration items");
		PROPERTYCLASSES_CLIENT.put( "clientconfig", "network configuration");
		//PROPERTYCLASSES_CLIENT.put( SavedSearch.CONFIG_KEY, "<html></p>saved search configurations ,<br />do not edit!</p></html>" );
		//PROPERTYCLASSES_CLIENT.put( RemoteControl.CONFIG_KEY, "<html><p>remote control call,<br />not client specific</p></html>" );
		PROPERTYCLASSES_CLIENT.put( "opsiclientd", "<html>entries for the opsiclientd.conf</html>" );
		//PROPERTYCLASSES_CLIENT.put( "opsi-local-image", "" );
		PROPERTYCLASSES_CLIENT.put( "software-on-demand", "<html>software on demand configuration,<br />not client specific</html>");
		//PROPERTYCLASSES_CLIENT.put( OpsiPermission.CONFIGKEY_STR_USER, "<html>user privileges configuration,<br />not client specific</html>");
	}
	
	public static TreeMap<String, String> getPropertyClasses( ConfigedMain.EditingTarget target)
	{
		if (target.equals(ConfigedMain.EditingTarget.CLIENTS))
			return PROPERTYCLASSES_CLIENT;
		else
			return PROPERTYCLASSES_SERVER;
	}
	
	
	/** This creation method constructs a new Controller instance and lets a static variable point to it
	When next time we need a Controller we can choose if we take the already constructed one - returned from the static method 
	getPersistenceController - or construct a new one   
	
	public static PersistenceController getNewPersistenceController (String server, String user, String password)
	{
		return null;
	}  
	
	
	public static PersistenceController getPersistenceController () 
	{
		return  null;  
	}
	*/
	
	
	protected Executioner exec;
	
	protected final Map<String, Executioner> execs = new HashMap<String, Executioner>();
	
	public abstract void userConfigurationRequestReload();
	
	public abstract void checkConfiguration();
	
	protected abstract boolean sourceAccept(); 
	
	/* error handling convenience methods */ 
	//public abstract List getErrorList ();
	
	//public abstract void clearErrorList ();
	
	
	/* ============================*/
	public abstract Executioner retrieveWorkingExec(String depot);
	
	protected abstract boolean makeConnection();
	
	/* connection state handling */
	public abstract ConnectionState getConnectionState();
	
	public abstract void setConnectionState(ConnectionState state);
	
	//public abstract void checkReadOnly();
	
	
	
	public boolean hasUserPrivilegesData()
	{
		//user has roles
		//a role has privileges
		//a privilege is implemented by conditions referring to targets
		return false;
	}
	
	
	public abstract void checkPermissions();
	
	public abstract boolean isGlobalReadOnly();
	
	public abstract boolean isServerFullPermission();
	
	public abstract boolean isDepotsFullPermission();
	
	public abstract boolean getDepotPermission(String depotId);
	
	/* ============================*/
	/* data retrieving and setting */
	
	public void syncTables()
	{
	}
	
	
	
	public void cleanUpAuditSoftware()
	{
		logging.error(this, "cleanUpAuditSoftware not implemented");
	}
	
	
	//---------------------------------------------------------------
	//implementation of observer patterns
	// offer observing of data refreshed announcements
	protected java.util.List<DataRefreshedObserver> dataRefreshedObservers;
	
	public void registerDataRefreshedObserver(DataRefreshedObserver ob)
	{
		if (dataRefreshedObservers == null)
			dataRefreshedObservers = new ArrayList<DataRefreshedObserver>();
		dataRefreshedObservers.add(ob);
	}
	
	public void unregisterDataRefreshedObserver(DataRefreshedObserver ob)
	{
		if (dataRefreshedObservers != null)
			dataRefreshedObservers.remove(ob);
	}
	
	public void notifyDataRefreshedObservers(Object mesg)
	{
		if (dataRefreshedObservers == null)
			return;
		
		for (DataRefreshedObserver ob : dataRefreshedObservers)
		{
			ob.gotNotification(mesg);
		}
	}
	
	// offer observing of data loading 
	protected java.util.List<DataLoadingObserver> dataLoadingObservers;
	
	public void registerDataLoadingObserver(DataLoadingObserver ob)
	{
		if (dataLoadingObservers == null)
			dataLoadingObservers = new ArrayList<DataLoadingObserver>();
		dataLoadingObservers.add(ob);
	}
	
	public void unregisterDataLoadingObserver(DataLoadingObserver ob)
	{
		if (dataLoadingObservers != null)
			dataLoadingObservers.remove(ob);
	}
	
	public void notifyDataLoadingObservers(Object mesg)
	{
		if (dataLoadingObservers == null)
			return;
		
		for (DataLoadingObserver ob : dataLoadingObservers)
		{
			ob.gotNotification(mesg);
		}
	}
	
	//---------------------------------------------------------------
	
	
	
	/* server related */
	public abstract boolean installPackage(String filename);
	
	public abstract boolean setRights(String path);
	
	/* relating to the PC list */ 
	
	public abstract java.util.List<Map<java.lang.String,java.lang.Object>> HOST_read();
	
	public abstract HostInfoCollections getHostInfoCollections();
		
	public abstract java.util.List<String> getClientsWithOtherProductVersion(String productId, String productVersion, String packageVersion);
	
	//public abstract  String[] getClientsWithFailed();
	
	//public abstract Map<String, String> getProductVersion(String productId, String depotID);
		
	public abstract boolean areDepotsSynchronous(Set depots);
	
	public abstract boolean createClient (String hostname, String domainname, 
		String depotId,
		String description, String inventorynumber, String notes,  String ipaddress, String macaddress,
		boolean uefiBoot, boolean wan, String group, String productNetboot, String productLocalboot);
	
	public abstract boolean configureUefiBoot(String clientId, boolean uefiBoot);

	public abstract boolean setWANConfigs(String clientId, boolean wan);
	
	public abstract boolean renameClient(String hostname, String newHostname);
	
	public abstract void deleteClient (String hostId);
	
	public abstract java.util.List<String>  deletePackageCaches (String[] hostIds);
	
	//public abstract void wakeOnLan (String hostId);
	
	public abstract java.util.List<String> wakeOnLan (String[] hostIds);
	
	public abstract java.util.List<String> wakeOnLan (
		java.util.Set<String> hostIds, 
		Map<String, java.util.List<String>> hostSeparationByDepot,
		Map<String, Executioner> execsByDepot);

	
	public abstract java.util.List<String> fireOpsiclientdEventOnClients (String event, String[] clientIds);
	
	public abstract java.util.List<String> showPopupOnClients (String message, String[] clientIds);
	
	public abstract java.util.List<String> shutdownClients (String[] clientIds);
	
	public abstract java.util.List<String> rebootClients (String[] clientIds);
	
	public abstract Map<String, Object> reachableInfo(String[] clientIds);
	
	public abstract Map<String, String> sessionInfo(String[] clientIds);
	
	
	//executes all updates collected by setHostDescription ...
	public abstract void updateHosts();
	
	public abstract void setHostDescription (String hostId, String description);
	
	public abstract void setClientInventoryNumber (String hostId, String inventoryNumber);
	
	public abstract void setClientOneTimePassword (String hostId, String oneTimePassword);
	
	public abstract void setHostNotes (String hostId, String notes);
	
	public abstract String getMacAddress (String hostId);
	
	public abstract void setMacAddress (String hostId, String address);
	
	// group handling
	public abstract Map<String, Map<String, String>>  getProductGroups();
	
	public abstract  Map<String, Map<String, String>>  getHostGroups();
	
	public abstract void hostGroupsRequestRefresh();
	
	//public abstract void clientsWithFailedRequestRefresh();
	
	public abstract void fObject2GroupsRequestRefresh();
	
	public abstract Map<String, Set<String>> getFObject2Groups();
	
	public abstract void fGroup2MembersRequestRefresh();
	
	public abstract void fProductGroup2MembersRequestRefresh();
	
	public abstract Map<String, Set<String>> getFGroup2Members();
	
	public abstract Map<String, Set<String>> getFProductGroup2Members();
	
	public abstract boolean addObject2Group(String objectId, String groupId);
	
	public abstract boolean removeObject2Group(String objectId, String groupId);
	
	public abstract boolean removeHostGroupElements(java.util.List<Object2GroupEntry> entries);
	
	public abstract boolean addGroup(StringValuedRelationElement newgroup);
	
	public abstract boolean deleteGroup (String groupId);
	
	public abstract boolean updateGroup (String groupId, Map<String, String> updateInfo) ;
	
	public abstract boolean setProductGroup(String groupId, String description, Set<String> products) ;
	
	public abstract List<String> getHostGroupIds();
	
	
	public abstract Map<String, java.util.List<String> > getHostSeparationByDepots( String[] hostIds );
	
	// deprecated
	//public abstract boolean writeGroup (String groupname, String[] groupmembers);
	
	//public abstract String getPcInfo( String hostId );
	
	public abstract boolean existsEntry (String pcname);
	
	
	/* software info */
	
	public abstract LinkedList<String> getSoftwareList();
	
	public abstract Map getSoftwareInfo (String clientId);
	
	public abstract void fillClient2Software(java.util.List<String> clients);
	
	public abstract void softwareAuditOnClientsRequestRefresh();
	
	//public abstract List<Map<String, Object>> getSoftwareAuditOnClients();
	
	public abstract Map<String, java.util.List<SWAuditClientEntry>> getClient2Software();
	
	public abstract DatedRowList getSoftwareAudit (String clientId);
	
	public abstract String getLastSoftwareAuditModification(String clientId);
	
	public abstract Map<String, Map/*<String, String>*/> retrieveSoftwareAuditData(String clientId);
	
	/* hardware info */
	public abstract List getOpsiHWAuditConf (String locale);
	
	public abstract void hwAuditConfRequestRefresh();
	
	public abstract Object getHardwareInfo (String clientId, boolean asHTMLtable);
	
	public abstract void auditHardwareOnHostRequestRefresh();
	
	public abstract List< Map<String, Object> > getHardwareOnClient();
	
	/* log files */
	public abstract String[] getLogtypes();
	
	public abstract Map<String, String> getEmptyLogfiles();
	
	public abstract Map<String, String> getLogfiles(String clientId, String logtype);
	
	public abstract Map<String, String>  getLogfiles (String clientId);
	
	/* list of boot images */
	//public abstract Vector getInstallImages();
	
	
	// product related
	
	//public abstract void  depotProductPropertiesRequestRefresh();
	
	public abstract void depotChange();
	
	public abstract void productDataRequestRefresh();
	
	/* listings of all products and their properties */
	
	public abstract List<String> getAllLocalbootProductNames (String depotId);
	
	public abstract List<String> getAllLocalbootProductNames ();
	
	//public abstract void localbootProductNamesRequestRefresh();
	
	public abstract List<String> getAllDepotsWithIdenticalProductStock(String depot);
	
	//deprecated
	public abstract List<String> getAllNetbootProductNames ();
	
	public abstract List<String> getAllNetbootProductNames (String depotId);
	
	public abstract Vector<String> getWinProducts(String depotId, String depotProductDirectory);
	
	//public abstract void retrieveProductsAllDepots();
	
	public abstract void retrieveProducts();
	
	public abstract Map<String, java.util.List<String>>  getPossibleActions(String depotId);
	
	public abstract Map<String, Map<String, OpsiProductInfo>> getProduct2versionInfo2infos();
	
	//public abstract void retrieveProductGlobalInfos();
	
	public abstract Map<String, Map<String, Object>> getProductGlobalInfos(String depotId); //(productId -> (infoKey -> info))  
	
	public abstract  Vector<Vector<Object>> getProductRows();
	
	public abstract Map<String, Map<String, java.util.List<String>>> getProduct2VersionInfo2Depots();
	
	public abstract TreeSet<String> getProductIds();
	
	public abstract Map<String, Map<String, String>> getProductDefaultStates();
	
	//public abstract List getProductDependencies ( String  productname);
	
	public abstract void retrieveProductDependencies();
	
	//intersection of the values of the clients
	public abstract List<String> getCommonProductPropertyValues(java.util.List<String> clients, String product, String property);
	
	public abstract void productPropertyDefinitionsRequestRefresh();
	
	public abstract void retrieveProductPropertyDefinitions();
	
	public abstract Map<String, de.uib.utilities.table.ListCellOptions> getProductPropertyOptionsMap(String productId);
	
	public abstract Map<String, de.uib.utilities.table.ListCellOptions> getProductPropertyOptionsMap(String depotId, String productId);
	
	//public abstract Map getProductPropertyValuesMap (String productname);
	
	//public abstract Map getProductPropertyDescriptionsMap (String productname);
	
	//public abstract Map getProductPropertyDefaultsMap (String productname);
	
	public abstract String getProductTitle(String product);
	
	public abstract String getProductInfo(String product);
	
	public abstract String getProductHint(String product);
	
	public abstract String getProductVersion(String product);
	
	public abstract String getProductPackageVersion(String product);
	
	public abstract String getProductTimestamp(String product);
	
	/* PC specific listings of products and their states and updatings */
	
	//public abstract List[] getClientsLocalbootProductNames(String[] clientIds);
	
	//public abstract List[] getClientsNetbootProductNames(String[] clientIds);

	// methods requires java 8:
	// public abstract Map getProductStatesNOMSortedByClientId();
	// public abstract Map getProductStatesNOMSorted(String sortKey);
	
	//public abstract Map getMapOfProductStates (String clientId);
	
	//public abstract Map getMapOfProductActions (String clientId);
	
	public abstract Map getMapOfProductStatesAndActions (String[] clientIds);
	
	public abstract Map getMapOfLocalbootProductStatesAndActions (String[] clientIds, 
			Map currentMap);
	
	public abstract Map getMapOfLocalbootProductStatesAndActions (String[] clientIds);
	
	public abstract Map getMapOfNetbootProductStatesAndActions (String[] clientIds);
	
	//collecting update items
	public abstract boolean updateProductOnClient(String pcname, String productname, int producttype, Map updateValues);
	
	//send the collected items
	public abstract boolean updateProductOnClients();
	
	//update for the whole set of clients
	public abstract boolean updateProductOnClients( 
			Set<String> clients, 
			String productName, 
			int productType, 
			Map<String, String> changedValues);
	
	public abstract boolean resetLocalbootProducts(String[] selectedClients);
	
	public abstract Map<String, String> getProductPreRequirements( String depotId, String productname ); 
	
	public abstract Map<String, String> getProductRequirements( String depotId, String productname );  
	
	public abstract Map<String, String> getProductPostRequirements( String depotId, String productname ); 
	
	public abstract Map<String, String> getProductDeinstallRequirements( String depotId, String productname ); 
	
	
	
	/* pc and product specific */
	//public abstract void retrieveProductproperties (List clientNames);
	public abstract void productpropertiesRequestRefresh();
	
	public abstract void retrieveProductproperties (List<String> clientNames);
	
	public abstract Boolean hasClientSpecificProperties(String productname);
	
	public  abstract Map<String, Boolean> getProductHavingClientSpecificProperties();
	
	public abstract Map<String, Map<String, ConfigName2ConfigValue>> getDepot2product2properties();
	
	public abstract Map<String, ConfigName2ConfigValue> getDefaultProductProperties(String depotId);
	
	public abstract void retrieveDepotProductProperties();
	
	public abstract Map<String, Object> getProductproperties (String pcname, String  productname);
	
	//public abstract void setProductproperties(String pcname, String productname, Map properties, 
	//		java.util.List updateCollection, java.util.List deleteCollection);
	public abstract void setProductproperties(String pcname, String productname, Map properties);
	
	//public abstract void setProductproperties( java.util.List updateCollection, java.util.List deleteCollection );
	public abstract void setProductproperties();
	
	public abstract void setCommonProductPropertyValue(
		Set<String> clientNames, String productName, String propertyName,
		java.util.List<String> values);
	
	
   /* information about the service  */
    	//public abstract void mapOfMethodSignaturesRequestRefresh(); we dont need update this
	
	public abstract List getMethodSignature(String methodname);
 	
	
	public abstract String getBackendInfos();

	
	/* network and additional settings, for network objects */
	
	//public abstract java.util.List getServers();
	
	//public abstract Map getNetworkConfiguration (String objectId);
	
	public abstract void hostConfigsRequestRefresh();
	
	//public abstract void hostConfigsRequestRefresh(String[] clients);
	
	//public abstract void hostConfigsCheck(String[] clients);
	//retrieve host configs if not existing
	
	public abstract Map<String, de.uib.utilities.table.ListCellOptions> getConfigOptions();
	
	public abstract Map<String, Map<String, Object>> getConfigs();
	
	public abstract Map<String, Object> getConfig(String objectId);
	
	//public abstract Map getAdditionalConfiguration (String objectId);
	
	//public abstract void setNetworkConfiguration (String objectId, Map settings);
	
	public abstract void setHostValues(Map settings);
	
	public abstract void setAdditionalConfiguration (String objectId, Map settings);
	
	public abstract void setAdditionalConfiguration (boolean determineConfigOptions);
	
	public abstract void setConfig(Map<String,java.util.List<Object>> settings);
	
	public abstract void setConfig();
	
	public abstract void configOptionsRequestRefresh();
	
	public abstract Map<String, RemoteControl> getRemoteControls();
	
	public abstract SavedSearches getSavedSearches();
	
	public abstract void deleteSavedSearch(String name);
	
	public abstract void saveSearch(SavedSearch ob);
	
	public abstract Map<String, java.util.List<Object>> getConfigDefaultValues();
	
	public abstract java.util.List<String> getServerConfigStrings(String key);
	
	public abstract String getDomain(String objectId);
	
	public abstract void setDepot(String depotId);
	
	
	
	//public abstract String getDepot();
	
	public abstract Map<String, SWAuditEntry> getInstalledSoftwareInformation();
	
	public abstract void installedSoftwareInformationRequestRefresh();
	
	public abstract String getSWident(Integer i);
	
	
	/* licences */
	public abstract Map<String, LicenceContractEntry> getLicenceContracts();
	
	// returns the ID of the edited data record
	public abstract String editLicenceContract(
		String licenseContractId, 
		String partner, 
		String conclusionDate, 
		String notificationDate, 
		String expirationDate,
		String notes
		);
	
	public abstract boolean deleteLicenceContract(String licenseContractId);
	
	public abstract Map<String, LicencepoolEntry> getLicencepools();
	
	// returns the ID of the edited data record
	public abstract String editLicencePool(
		String licensePoolId,  
		String description
		);
	
	public abstract boolean deleteLicencePool(String licensePoolId);
	
	public abstract Map<String, LicenceEntry> getSoftwareLicences();
	
	// returns the ID of the edited data record
	public abstract String editSoftwareLicence(
		String softwareLicenseId, 
		String licenceContractId, 
		String licenceType, 
		String maxInstallations,
		String boundToHost,
		String expirationDate
		);
	
	public abstract boolean deleteSoftwareLicence(
		String softwareLicenseId
		);
	
	public abstract Map<String, Map> getRelationsSoftwareL2LPool();
	
	// returns the ID of the edited data record
	public abstract String editRelationSoftwareL2LPool(
		String softwareLicenseId,
		String licensePoolId, 
		String licenseKey
		);
	
		
	public abstract boolean deleteRelationSoftwareL2LPool(
		String softwareLicenseId, 
		String licensePoolId
		);
		
	public abstract Map<String, Map<String, String>> getRelationsProductId2LPool();
	
	// returns an ID of the edited data record
	public abstract String editRelationProductId2LPool(
		String productId,  
		String licensePoolId
		);
	
	public abstract boolean deleteRelationProductId2LPool(
		String productId, 
		String licensePoolId
		);
	
	public abstract void relations_windowsSoftwareId2LPool_requestRefresh();
	
	public abstract void relations_auditSoftwareToLicencePools_requestRefresh();
	
	public abstract List getSoftwareListByLicencePool(String licencePoolId);
	
	
	public abstract String getLicencePoolBySoftwareId(String softwareIdent);;
	
	
	//public abstract List getLicencePool2WindowsSoftwareIDs(String licensePoolId);
	
	public abstract Map<String, Map> getRelationsWindowsSoftwareId2LPool();
	
	public abstract boolean setWindowsSoftwareIds2LPool(
		String licensePoolId,
		List windowsSoftwareIds
		);
	
	/* returns the ID of the edited data record
	public abstract String editRelationWindowsSoftwareId2LPool(
		String windowsSoftwareId,  
		String licensePoolId			{

		);

	*/
	
	public abstract Map<String, LicenceStatisticsRow> getLicenceStatistics();
	
	public abstract void licencesUsageRequestRefresh();
	
	public abstract Map<String, java.util.List<LicenceUsageEntry>> getFClient2LicencesUsageList();
	
	public abstract Map<String, LicenceUsageEntry> getLicencesUsage();
	
	public abstract String getLicenceUsage(String hostId, String licensePoolId);
	
	public abstract String editLicenceUsage(String hostId, String softwareLicenseId, String licensePoolId, String licenseKey, String notes);
	
	public abstract boolean deleteLicenceUsage(String hostId, String softwareLicenseId, String licensePoolId);
	
	//collecting deletion items
	public abstract void addDeletionLicenceUsage(String hostId, String softwareLicenseId, String licensePoolId);
	
	//send the collected items
	public abstract boolean executeCollectedDeletionsLicenceUsage();
	
	public abstract void reconciliationInfoRequestRefresh();
	
	public abstract Map<String, Map<String, Object>> getLicencesReconciliation();
	
	public abstract String editLicencesReconciliation(String clientId, String licensePoolId);
	
	public abstract boolean deleteLicencesReconciliation(String clientId, String licensePoolId);
	
	//configurations and algorithms
	public abstract LinkedHashMap<String, Boolean> getProductOnClients_displayFieldsLocalbootProducts();
	public abstract LinkedHashMap<String, Boolean> getProductOnClients_displayFieldsNetbootProducts();
	public abstract LinkedHashMap<String, Boolean> getHost_displayFields();
	
	
	//menu configuration
	public abstract java.util.List<String> getDisabledClientMenuEntries();
	public abstract java.util.List<String> getOpsiclientdExtraEvents();
	
	
	//table sources
	//public abstract class AllProductsTableSource implements de.uib.utilities.table.provider.TableSource;
	
	//opsi module information
	public abstract void opsiInformationRequestRefresh();
	
	public abstract Date getOpsiExpiresDate();
	
	public abstract void retrieveOpsiModules();
	
	public abstract Map<String, Object> getOpsiModulesInfos(); 
	
	public abstract boolean isWithLocalImaging();
	
	//public abstract boolean isWithScalability1();
	
	public abstract boolean isWithLicenceManagement();
	
	public abstract boolean isWithMySQL();
	
	public abstract boolean isWithUEFI();

	public abstract boolean isWithWAN();
	
	public abstract boolean isWithLinuxAgent();
	
	public abstract boolean isWithUserRoles();
		
	public abstract String getOpsiVersion();
	

	public abstract java.util.List<Map<java.lang.String,java.lang.Object>> retrieveCommandList();
	public abstract boolean doActionSSHCommand(String method, List<Object> jsonObjects);
	public abstract boolean createSSHCommand(List<Object> jsonObjects);
	public abstract boolean updateSSHCommand(List<Object> jsonObjects);
	public abstract boolean deleteSSHCommand(List<String> jsonObjects);
	public abstract boolean checkSSHCommandMethod(String method);
}	

