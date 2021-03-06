 /*
 * configed - configuration editor for client work stations in opsi
 * (open pc server integration) www.opsi.org
 *
 * Copyright (C) 2000-2016 uib.de
 *
 * This program is free software; you may redistribute it and/or
 * modify it under the terms of the GNU General Public
 * License, version AGPLv3, as published by the Free Software Foundation
 *
 */

package de.uib.configed;

import java.util.*;
import utils.*;
import de.uib.messages.Messages;
import de.uib.configed.guidata.*;
import de.uib.configed.gui.*;
import de.uib.configed.gui.ssh.*;
import de.uib.configed.gui.productpage.*;
import de.uib.configed.gui.licences.*;
import de.uib.configed.groupaction.*;
import de.uib.configed.productaction.*;
import de.uib.opsidatamodel.dbtable.*;
import de.uib.utilities.*;
import de.uib.utilities.datastructure.*;
import de.uib.utilities.logging.*;
import de.uib.utilities.swing.*;
import de.uib.utilities.swing.list.*;
import de.uib.utilities.selectionpanel.*;
import de.uib.utilities.tree.*;
import de.uib.utilities.swing.tabbedpane.*;
import de.uib.utilities.table.*;
import de.uib.utilities.table.gui.*;
import de.uib.utilities.table.provider.*;
import de.uib.utilities.table.updates.*;
import de.uib.utilities.thread.WaitCursor;
import de.uib.opsidatamodel.*;
import de.uib.opsidatamodel.datachanges.*;
import de.uib.opsicommand.ConnectionState;
import de.uib.configed.Globals;
import de.uib.configed.type.*;
import de.uib.configed.type.licences.*;
import de.uib.configed.tree.*;
import de.uib.opsicommand.Executioner;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import de.uib.utilities.table.*;
import javax.swing.table.TableModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.ListSelectionModel;
import org.jdesktop.swingx.autocomplete.*;
import java.text.MessageFormat;
import javax.swing.event.*;
import javax.swing.plaf.metal.*;
import javax.swing.tree.*;
import java.net.URL;
import java.io.*;

import de.uib.opsicommand.sshcommand.*;


/**
 * ConfigedMain
 * description: The main controller of the program
 * copyright:     Copyright (c) 2000-2015
 * organization: uib.de
 * @author  D. Oertel, R. Roeder, J. Schneider, A. Sucher
 */
public class ConfigedMain
			implements ListSelectionListener, TabController, LogEventObserver
{

	public final static int viewClients = 0;
	public final static int viewLocalbootProducts = 1;
	public final static int viewNetbootProducts = 2;
	public final static int viewNetworkconfiguration = 3;
	public final static int viewHardwareInfo = 4;
	public final static int viewSoftwareInfo = 5;
	public final static int viewLog = 6;
	public final static int viewProductProperties = 7;
	public final static int viewHostProperties = 8;




	final static String TEMPGROUPNAME = "";
	private int rebuiltCounter = 0;

	PersistenceController persist;

	//global table providers for licence management
	TableProvider licencePoolTableProvider;
	TableProvider licenceOptionsTableProvider;
	TableProvider licenceContractsTableProvider;
	TableProvider softwarelicencesTableProvider;


	public TableProvider globalProductsTableProvider;


	protected GeneralDataChangedKeeper generalDataChangedKeeper;
	protected ClientInfoDataChangedKeeper clientInfoDataChangedKeeper;
	//protected NetworkConfigDataChangedKeeper networkConfigDataChangedKeeper;
	//protected AdditionalConfigDataChangedKeeper additionalConfigDataChangedKeeper;
	protected RequirementsTableModel requirementsModel;
	protected IFInstallationStateTableModel  istmForSelectedClientsLocalboot;
	protected IFInstallationStateTableModel  istmForSelectedClientsNetboot;
	protected String pcName = null;
	private String[] selectedClients = new String[]{};
	java.util.List<String> saveSelectedClients = null;
	java.util.List<String> preSaveSelectedClients = null;

	//protected TreePath[] selectedTreePaths = new TreePath[]{}; // not used since we do not work with selection

	protected Map<String, TreePath> activeTreeNodes ;
	protected ArrayList<TreePath> activePaths;
	// is nearly activeTreeNodes.values() , but there may be multiple paths where only one entry to activeTreeNode remains ----
	//protected HashSet<String> activeParents; moved to ClientTree



	protected Set<String> clientsFilteredByTree;
	protected TreePath groupPathActivatedByTree;
	protected ActivatedGroupModel activatedGroupModel;

	protected String[] objectIds = new String[]{};
	protected String[] selectedDepots = new String[]{};
	protected String[] oldSelectedDepots;
	protected Vector<String> selectedDepotsV = new Vector<String>();


	protected boolean anyDataChanged = false;

	protected String clientInDepot;
	protected HostInfo hostInfo = new HostInfo();

	protected boolean groupLoading = false; // tells if a group of client is loaded via GroupManager (and not by direct selection)
	protected boolean changeListByToggleShowSelection = false;
	protected boolean hostgroupChanged = false;
	protected String groupname = TEMPGROUPNAME;
	private String appTitle = Globals.APPNAME;

	protected FTextArea fAskSaveChangedText;
	protected FTextArea fAskSaveProductConfiguration;

	private SavedSearchesDialog savedSearchesDialog;
	private ClientSelectionDialog clientSelectionDialog;
	private FShowList fShowReachableInfo;

	protected String productEdited = null;  // null serves als marker that we were not editing products
	protected Collection productProperties;  // the properties for one product and all selected clients
	protected de.uib.opsidatamodel.datachanges.UpdateCollection updateCollection = new UpdateCollection(new Vector());
	protected HashMap clientProductpropertiesUpdateCollections;
	/* for each product:
	a collection of all clients
	that contains name value pairs with changed data*/

	protected ProductpropertiesUpdateCollection clientProductpropertiesUpdateCollection;
	protected ProductpropertiesUpdateCollection depotProductpropertiesUpdateCollection;

	//protected NetworkconfigurationUpdateCollection networkconfigurationUpdateCollection;
	protected AdditionalconfigurationUpdateCollection additionalconfigurationUpdateCollection;

	protected HostUpdateCollection hostUpdateCollection;

	protected Map collectChangedStates = new HashMap();  // a map of clients, for each occurring client there is a map of products, where for each occurring product there may be an entry stateTypeKey - value

	protected Map<String /* client */, Map<String /* product */, Map<String /*propertykey*/, String/*propertyvalue*/>>>
	collectChangedLocalbootStates = new HashMap<String, Map<String, Map<String, String>>>();

	protected Map<String /* client */, Map<String /* product */, Map<String /*propertykey*/, String/*propertyvalue*/>>>
	collectChangedNetbootStates = new HashMap<String, Map<String, Map<String, String>>>();

	protected Map possibleActions = new HashMap(); // a map of products, product --> list of actions; used as an indicator that a product is in the depot

	protected Map mergedProductProperties = null;

	public LinkedHashMap<String, Boolean> displayFieldsLocalbootProducts;
	public LinkedHashMap<String, Boolean> displayFieldsNetbootProducts;

	protected Set depotsOfSelectedClients = null;

	//protected Vector pclist = new Vector();

	protected java.util.List<String> localbootProductnames;
	protected java.util.List<String> netbootProductnames;
	protected java.util.List hwAuditConfig;

	//marker variables for requests for reload when clientlist changes
	private Map localbootStatesAndActions = null;
	private boolean localbootStatesAndActionsUPDATE = false;
	private Map netbootStatesAndActions = null;
	private Map<String, Map<String, Object>> hostConfigs = null;

	//collection of retrieved software audit and hardware maps
	//private Map<String,  java.util.List<Map<String, Object>>> swInfoClientmap;
	private Map<String, Object> hwInfoClientmap;


	//public final  static ActionRequestDisplay ardDefault = new ActionRequestDisplay();
	//public final  static ActionRequestDisplay ardNetboot = new ActionRequestDisplay();


	protected String myServer;
	protected String myDomain;
	protected boolean multiDepot = false;


	private WaitCursor waitReachableCursor;
	private WaitCursor waitCursorInitGui;


	JTableSelectionPanel selectionPanel;

	de.uib.configed.tree.ClientTree treeClients;

	Map<String, Map<String, String>> productGroups;
	Map<String, Set<String>> productGroupMembers;

	DepotsList depotsList;
	Map<String, Map<String, Object>> depots;
	LinkedList<String> depotNamesLinked ;
	int[] depotsList_selectedIndices_lastFetched;
	boolean depotsList_selectionChanged;
	private String depotRepresentative;
	ListSelectionListener depotsListSelectionListener;


	private MainFrame mainFrame;


	private ReachableUpdater reachableUpdater  = new ReachableUpdater(0);

	private ArrayList<JFrame> allFrames;

	public TabbedFrame licencesFrame;

	public FGroupActions groupActionFrame;
	public FProductActions productActionFrame;

	ControlPanelEnterLicence controlPanelEnterLicence;
	ControlPanelNewLicence controlPanelNewLicence;
	ControlMultiTablePanel controlPanelAssignToLPools;
	ControlPanelLicencesStatistics controlPanelLicencesStatistics;
	ControlPanelLicencesUsage controlPanelLicencesUsage;
	ControlPanelLicencesReconciliation controlPanelLicencesReconciliation;


	Vector<ControlMultiTablePanel> allControlMultiTablePanels;

	DPassword dpass;
	MyDialogRemoteControl dialogRemoteControl;
	Map<String, RemoteControl> remoteControls;

	private int pcCount = 0;
	private boolean firstDepotListChange = true;

	final public Dimension licencesInitDimension = new Dimension(1000, 700);

	protected int viewIndex = viewClients;
	protected int saveClientsViewIndex = viewClients;
	protected int saveDepotsViewIndex = viewProductProperties;
	protected int saveServerViewIndex = viewNetworkconfiguration;


	//public enum MainTabState {CLIENT_SELECTION, LOCALBOOT_PRODUCTS, NETBOOT_PRODUCTS, NETWORK_CONFIG, HARDWARE_INVENT, SOFTWARE_INVENT};
	//protected MainTabState viewIndex;

	protected Map<String, Object>reachableInfo = new HashMap();
	protected Map<String, String>sessionInfo = new HashMap();

	protected Map<String,String> logfiles;

	public LinkedHashMap<String, Boolean> host_displayFields;

	public enum LicencesTabStatus { LICENCEPOOL, ENTER_LICENCE, EDIT_LICENCE, USAGE, RECONCILIATION, STATISTICS };
	protected Map<LicencesTabStatus, TabClient> licencesPanels = new HashMap<LicencesTabStatus, TabClient>();
	protected LicencesTabStatus licencesStatus;



	protected Map<LicencesTabStatus, String> licencesPanelsTabNames = new HashMap<LicencesTabStatus, String>();


	// ==================================================================
	// TabController Interface
	public Enum getStartTabState()
	{
		return LicencesTabStatus.LICENCEPOOL;
	}

	public TabClient getClient(Enum state)
	{
		return licencesPanels.get(state);
	}

	public void addClient(Enum status, TabClient panel)
	{
		licencesPanels.put((LicencesTabStatus) status, panel);
		licencesFrame.addTab(status, licencesPanelsTabNames.get(status), (JComponent) panel);
	}

	public void removeClient(Enum status)
	{
		licencesFrame.removeTab(status);
	}

	public Enum reactToStateChangeRequest(Enum newState )
	{
		logging.debug(this,"reactToStateChangeRequest( newState: " + (LicencesTabStatus)newState + "), current state "  + licencesStatus);
		if (newState != licencesStatus)
		{
			if (getClient(licencesStatus).mayLeave())
			{
				licencesStatus = (LicencesTabStatus)newState;

				//logging.debug(this, "licencesPanels.get(mainStatus) " + licencesPanels.get(mainStatus));

				if (getClient(licencesStatus)!= null)
				{
					licencesPanels.get(licencesStatus).reset();
				}
			}

			// otherwise we return the old status

		}

		return licencesStatus;
	}

	public boolean exit()
	{
		if (licencesFrame != null)
		{
			licencesFrame.setVisible(false);
			mainFrame.visualizeLicencesFramesActive(false);
		}
		return true;
	}

	// ==================================================================


	private boolean dataReady = false;
	private boolean startMode = true;



	private boolean filterClientList = false;

	public static String HOST = null;
	public static String USER = null;
	public static  String PASSWORD = null;
	public static  String SSHKEY = null;
	public static  String SSHKEYPASS = null;

	private JApplet appletHost = null;

	public enum EditingTarget {CLIENTS, DEPOTS, SERVER}
	// with this enum type we build a state model, which target shall be edited

	//protected EditingTarget target = EditingTarget.CLIENTS;
	public static EditingTarget editingTarget = EditingTarget.CLIENTS;




	public ConfigedMain(JApplet appletHost, String host, String user, String password)
	{
		this.appletHost  = appletHost;
		if (HOST == null)
			HOST= host;
		if (USER == null)
			USER = user;
		if (PASSWORD == null)
			PASSWORD = password;
		SSHKEY = configed.sshkey;
		SSHKEYPASS = configed.sshkeypassphrase;
		logging.registLogEventObserver(this);

	}

	public static ConfigedMain.EditingTarget getEditingTarget()
	{
		return editingTarget;
	}

	protected void initGui()
	{
		logging.info(this, "initGui");
		//logging.debug(this, "display fields " + persist.getProductOnClients_displayFieldsLocalbootProducts());
		displayFieldsLocalbootProducts = new LinkedHashMap<String, Boolean>(persist.getProductOnClients_displayFieldsLocalbootProducts());
		displayFieldsNetbootProducts = new LinkedHashMap<String, Boolean>(persist.getProductOnClients_displayFieldsNetbootProducts());
		//initialization by defaults, it can be edited afterwards


		initTree();

		allFrames = new ArrayList<JFrame>();

		initSpecialTableProviders();

		initMainFrame( );

		activatedGroupModel = new ActivatedGroupModel(mainFrame.getHostsStatusInfo());


		//initLicencesFrame();

		setEditingTarget (EditingTarget.CLIENTS);

		anyDataChanged = false;

		waitCursorInitGui = new WaitCursor( mainFrame.retrieveBasePane(), mainFrame.getCursor(), "initGui");
		
		

		preloadData();

		logging.debug(this, "initialTreeActivation	");

		SwingUtilities.invokeLater(new Runnable(){

			                           public void run()
			                           {
				                           initialTreeActivation();
				                           if (configed.fProgress != null)
				                           	   configed.fProgress.actAfterWaiting();
			                           }
		                           }
		                          );;


		reachableUpdater.setInterval(configed.refreshMinutes);

	}


	private String getSavedStatesLocation()
	{
		String result = "";
		
		if (System.getenv("LOCALAPPDATA") != null)
		{
			result = System.getenv("LOCALAPPDATA") + File.separator + logging.programSubDir;
		}
		else
		{
			result = System.getProperty("user.home") + File.separator + logging.programSubDir;
               }
               
               return result;
        }
	
	private String getSavedStatesDirectoryName()
	{
		String result = getSavedStatesLocation()
			                  + File.separator +  HOST.replace(":", "_");
               return result;
    	}
    	
    	//private java.util.List<String> getAll
			                 		
	
	private void initSavedStates()
	{
		File savedStatesDir  = new File(getSavedStatesDirectoryName());

		savedStatesDir.mkdirs();

		configed.savedStates = new SavedStates(new File(savedStatesDir.toString()
		                                       +  File.separator + configed.savedStatesFilename));

		try{
			configed.savedStates.load();
		}
		catch(IOException iox)
		{
			logging.info(this, "saved states file could not be loaded");
		}
		
		Integer oldUsageCount = Integer.valueOf(configed.savedStates.saveUsageCount.deserialize());
		
		configed.savedStates.saveUsageCount.serialize( oldUsageCount + 1 );
		
		
	}
	
	
	private Vector<String>  readLocallySavedServerNames()
	{
		Vector<String> result = new Vector<String>();
		
		TreeMap<java.sql.Timestamp, String> sortingmap = new TreeMap<java.sql.Timestamp, String>();
		
		File savedStatesLocation  = new File(getSavedStatesLocation());

		savedStatesLocation.mkdirs();
		
		File[] subdirs = null;
		
		try{
			subdirs = savedStatesLocation.listFiles(
				new FileFilter(){
					public boolean accept(File f)
					{
						return f.isDirectory();
					}
				}
			);
			
			
			
			for (File folder : subdirs)
			{
				//logging.info(this, "readLocallySavedServerNames savedStates for folder " + folder);
				
				File checkFile = new File( folder +   File.separator + configed.savedStatesFilename);
				String folderPath = folder.getPath();
				String elementname = folderPath.substring(folderPath.lastIndexOf(File.separator)+1);
				String hostname = elementname;
				Integer port = null;
				
				//revert encryption of ":"
				if (elementname.lastIndexOf("_") > -1)
				{
					try
					{
						port = Integer.valueOf( (elementname.substring(elementname.lastIndexOf("_")+1)).trim() );
						hostname = elementname.substring(0, elementname.lastIndexOf("_"));
					}
					catch(Exception ex)
					{
						logging.info(this,"no port found, should be NumberFormatException: " + ex);
					}
					
					if (port != null)
						elementname = hostname + ":" + port;
				}
					
					
					
				
				/*
				SavedStates testSavedStates = new SavedStates(checkFile);
				
				
				try{
					testSavedStates.load();
				}
				catch(IOException iox)
				{
					logging.info(this, "saved states file could not be loades");
				}
				
				logging.info(this, "readLocallySavedServerNames savedStates for " + elementname + " : " +testSavedStates);
				*/
					
				sortingmap.put (
					new java.sql.Timestamp( checkFile.lastModified() ), 
					elementname
					//Integer.valueOf (testSavedStates.saveUsageCount.deserialize())
					);
			}
				
		}
		catch(SecurityException ex)
		{
			logging.warning("could not read file: " + ex);
		}
		
		//logging.info(this, "readLocallySavedServerNames " + Arrays.toString(subdirs));
		//logging.info(this, "readLocallySavedServerNames  sortingmap " + sortingmap);
	
		for (Date date : sortingmap.descendingKeySet())
		{
			result.add(sortingmap.get(date));
		}
		
		logging.info(this, "readLocallySavedServerNames  result " + result);
		
		return result;
	}

	private void setSSHallowedHosts()
	{
		Set<String> sshAllowedHosts = new HashSet<String>();
		
		if (persist.isDepotsFullPermission())
		{
			logging.info(this, "set ssh allowed hosts " + HOST);
			sshAllowedHosts.add( HOST );
			sshAllowedHosts.addAll( persist.getHostInfoCollections().getDepots().keySet() );
		}
		else
		{
			sshAllowedHosts.addAll( persist.getDepotPropertiesForPermittedDepots().keySet() );
		}
		
		SSHCommandFactory.getInstance(this).setAllowedHosts( sshAllowedHosts );
	}
		

	public void loadDataAndGo()
	{
		dpass.setVisible(false);
		logging.clearErrorList();

		//errors are already handled in login
		logging.info(this, " we got persist " + persist);

		setSSHallowedHosts();
		
		logging.info(this, "call initData");
		initData();
		initSavedStates();
		oldSelectedDepots = configed.savedStates.saveDepotSelection.deserialize();

		Date opsiExpiresDate = persist.getOpsiExpiresDate();
		
		logging.info(this, " opsi modules file expires "  + opsiExpiresDate);

		persist.syncTables();
		

		configed.fProgress = new FLoadingWaiter(Globals.APPNAME + " " + configed.getResourceValue("FWaitProgress.title"));
		((de.uib.utilities.observer.DataLoadingObservable) persist).registerDataLoadingObserver(configed.fProgress);


		if (opsiExpiresDate != null)
		{
			Calendar nowCal = Calendar.getInstance();
			nowCal.setTime(new Date());

			Calendar noticeCal = Calendar.getInstance();
			noticeCal.setTime(opsiExpiresDate);

			noticeCal.add(Calendar.DAY_OF_MONTH, -14);

			if (nowCal.after(noticeCal))
			{
				logging.info(this, "show notice of expiring module");
				FTextArea fMessage = new FTextArea(mainFrame, 
					configed.getResourceValue("Permission.modules.title"),
					false,
					new String[]{"ok"}, 
					350, 150);
				fMessage.setMessage(configed.getResourceValue("Permission.modules.expires")
					+ "\n" + opsiExpiresDate);
				fMessage. setVisible(true);
			
				/*
				
					   
				JOptionPane.showMessageDialog(	mainFrame,
											   configed.getResourceValue("Permission.modules.expires")
											   + "\n" + opsiExpiresDate,
											   configed.getResourceValue("Permission.modules.title"),
											   JOptionPane.WARNING_MESSAGE);
				*/
			}
		}
		
		configed.fProgress.startWaiting();
		
		
		
		new Thread(){
			public void run()
			{
				initGui();
		
				mainFrame.initFirstSplitPane();
				
				waitCursorInitGui.stop();
				checkErrorList();
				if (configed.fProgress == null)
				{
					logging.warning(this, "configed.fProgress == null althought it should have been started");
				}
				else
				{
					configed.fProgress.setReady();
				}
				
				/*
				//try to ensure that we get the mainframe
				SwingUtilities.invokeLater(new Runnable(){
						public void run()
						{
							logging.info(this, "ensuring mainFrame is visible");
							mainFrame.setVisible(true);
							
							if (configed.isApplet)
							{
								try
								{
									Thread.sleep(5000);
								}
								catch(InterruptedException ex)
								{
								}
					
								adjustSize(10, appletHost.getSize().width-1, appletHost.getSize().height - 20);
							}
						}
					}
				);
				*/
				
			}
		}.start();
				
				
		
		
		/*
		if (reachableUpdater != null)
		{
		reachableUpdater.setSuspended(false);
		}
		*/
		
		
	}
	
	
	public void init()
	{
		logging.debug(this, "init");
		
		/*
		try{
				Thread.sleep(1000);
			}
			catch(InterruptedException ex)
			{
			}
		*/

		//we start with a language

		if (reachableUpdater != null)
		{
			reachableUpdater.setInterval(0);
		}


		InstallationStateTableModel.restartColumnDict();
		SWAuditEntry.setLocale();
		
		Vector<String> savedServers = readLocallySavedServerNames();

		login(savedServers);
	}


	private void adjustSize(int waitMs, int newW, int newH)
	{
		try{
			Thread.sleep(waitMs);
		}
		catch(InterruptedException ex){};

		mainFrame.allPane.setSize(newW, newH);
		mainFrame.allPane.validate();
	}

	protected void initData()
	{
		requirementsModel = new RequirementsTableModel(persist);
		generalDataChangedKeeper = new GeneralDataChangedKeeper();
		clientInfoDataChangedKeeper = new ClientInfoDataChangedKeeper();
		allControlMultiTablePanels = new Vector<ControlMultiTablePanel>();

	}

	protected void initSpecialTableProviders()
	{
		Vector<String> columnNames = new Vector<String>();



		columnNames.add("productId");
		columnNames.add("productName");

		//columnNames.add("depotId");

		//from OpsiPackage.appendValues
		columnNames.add("productType");
		columnNames.add("productVersion");
		columnNames.add("packageVersion");



		Vector<String> classNames = new Vector<String>();
		for (int i = 0; i < columnNames.size(); i++)
		{
			classNames.add("java.lang.String");
		}



		globalProductsTableProvider =
		    new DefaultTableProvider(
		        new ExternalSource(columnNames, classNames,
		                           new RowsProvider(){
			                           public void requestReload()
			                           {
				                           persist.productDataRequestRefresh();
			                           }
			                           public Vector<Vector<Object>> getRows()
			                           {
				                           return persist.getProductRows();
			                           }
		                           }
		                          )
		    );
	}



	protected void preloadData()
	// sets dataReady = true when finished
	{
		WaitCursor waitCursor = new WaitCursor( mainFrame.retrieveBasePane(), "preloadData" );


		persist.retrieveOpsiModules();
		//opsiModulesInfo  = persist.getOpsiModulesInfos();

		if (depotRepresentative == null)
			depotRepresentative = myServer;
		persist.setDepot(depotRepresentative);
		persist.depotChange();

		myDomain = persist.getDomain("");

		localbootProductnames = persist.getAllLocalbootProductNames();
		netbootProductnames = persist.getAllNetbootProductNames();
		persist.getProductIds();



		host_displayFields = persist.getHost_displayFields();
		persist.getProductOnClients_displayFieldsNetbootProducts();
		persist.getProductOnClients_displayFieldsLocalbootProducts();
		persist.configOptionsRequestRefresh();

		if( savedSearchesDialog != null)
		{
			savedSearchesDialog.resetModel();
		}


		//persist.getSoftwareAuditOnClients();
		//System.exit(0);


		/*
		HashSet<String> testProductgroup = new HashSet<String>();
		testProductgroup.add("access2");
		testProductgroup.add("acrobat9");
		persist.setProductGroup("test3", "testgroup3!", testProductgroup);
		*/
		productGroups = persist.getProductGroups();
		productGroupMembers = persist.getFProductGroup2Members();
		//logging.debug(this, "preloadData: productGroupMembers " + productGroupMembers);


		hwAuditConfig = persist.getOpsiHWAuditConf( Messages.getLocale().getLanguage() + "_" + Messages.getLocale().getCountry() );
		mainFrame.initHardwareInfo (hwAuditConfig);

		//persist.retrieveProductsAllDepots();
		persist.retrieveProducts();
		//persist.getAllLocalbootProductNames();
		//persist.getAllNetbootProductNames();

		possibleActions = persist.getPossibleActions(depotRepresentative);
		persist.retrieveProductPropertyDefinitions();
		persist.retrieveProductDependencies();
		persist.retrieveDepotProductProperties();

		persist.getInstalledSoftwareInformation();
		//persist.getSoftwareAuditOnClients();
		//persist.getClient2Software();



		dataReady = true;
		waitCursor.stop();
		mainFrame.enableAfterLoading();


	}


	public void showExternalDocument(String urlS)
	{
		try
		{
			URL url = new URL(urlS);
			if (configed.isApplet)
			{
				appletHost.getAppletContext().showDocument(url, "_blank");
			}
			else
			{
				Runtime rt = Runtime.getRuntime();
				String osName = System.getProperty("os.name" );
				if(osName.toLowerCase().startsWith("windows"))
				{
					String title = "";
					Process proc = rt.exec("cmd.exe /c start \"" + title + "\" \"" + urlS.replace("\\", "\\\\") + "\"");
				}
				else
					//Linux, we assume that there is a firefox and it will handle the url
				{
					String[] cmdarray = new String[]{"firefox", urlS};
					Process proc = rt.exec(cmdarray);
				}
			}
		}
		catch(Exception ex)
		{
			logging.error("showExternalDocument error " + ex, ex);
		}
	}


	public void setGroupLoading(boolean b)
	{
		groupLoading = b;
	}


	public void toggleColumnIPAddress()
	{
		boolean visible = persist.getHost_displayFields().get(  HostInfo.clientIpAddress_DISPLAY_FIELD_LABEL );
		persist.getHost_displayFields().put( HostInfo.clientIpAddress_DISPLAY_FIELD_LABEL , !visible);

		setRebuiltPclistTableModel(false, false);
		selectionPanel.initSortKeys();
		if (getSelectedClients().length > 0)
			selectionPanel.moveToValue(getSelectedClients()[0], 0);
	}

	public void toggleColumnHardwareAddress()
	{
		boolean visible = persist.getHost_displayFields().get( HostInfo.clientMacAddress_DISPLAY_FIELD_LABEL );
		persist.getHost_displayFields().put( HostInfo.clientMacAddress_DISPLAY_FIELD_LABEL, !visible);


		setRebuiltPclistTableModel(false, false);
		selectionPanel.initSortKeys();
		if (getSelectedClients().length > 0)
			selectionPanel.moveToValue(getSelectedClients()[0], 0);
	}

	public void toggleColumnSessionInfo()
	{
		boolean visible = persist.getHost_displayFields().get( HostInfo.clientSessionInfo_DISPLAY_FIELD_LABEL );
		persist.getHost_displayFields().put( HostInfo.clientSessionInfo_DISPLAY_FIELD_LABEL, !visible);

		mainFrame.iconButtonSessionInfo.setEnabled(!visible);

		setRebuiltPclistTableModel(false, false);
		selectionPanel.initSortKeys();
		if (getSelectedClients().length > 0)
			selectionPanel.moveToValue(getSelectedClients()[0], 0);
	}


	public void toggleColumnInventoryNumber()
	{
		boolean visible = persist.getHost_displayFields().get( HostInfo.clientInventoryNumber_DISPLAY_FIELD_LABEL );
		persist.getHost_displayFields().put( HostInfo.clientInventoryNumber_DISPLAY_FIELD_LABEL, !visible);


		setRebuiltPclistTableModel(false, false);
		selectionPanel.initSortKeys();
		if (getSelectedClients().length > 0)
			selectionPanel.moveToValue(getSelectedClients()[0], 0);
	}

	public void toggleColumnCreated()
	{
		boolean visible = persist.getHost_displayFields().get( HostInfo.created_DISPLAY_FIELD_LABEL );
		persist.getHost_displayFields().put( HostInfo.created_DISPLAY_FIELD_LABEL, !visible);


		setRebuiltPclistTableModel(false, false);
		selectionPanel.initSortKeys();
		if (getSelectedClients().length > 0)
			selectionPanel.moveToValue(getSelectedClients()[0], 0);
	}
	
	public void toggleColumnWANactive()
	{
		boolean visible = persist.getHost_displayFields().get( HostInfo.clientWanConfig_DISPLAY_FIELD_LABEL ); 
		persist.getHost_displayFields().put( HostInfo.clientWanConfig_DISPLAY_FIELD_LABEL, !visible);


		setRebuiltPclistTableModel(false, false);
		selectionPanel.initSortKeys();
		if (getSelectedClients().length > 0)
			selectionPanel.moveToValue(getSelectedClients()[0], 0);
	}
	
	public void toggleColumnUEFIactive()
	{
		boolean visible = persist.getHost_displayFields().get( HostInfo.clientUefiBoot_DISPLAY_FIELD_LABEL ); 
		persist.getHost_displayFields().put( HostInfo.clientUefiBoot_DISPLAY_FIELD_LABEL, !visible);


		setRebuiltPclistTableModel(false, false);
		selectionPanel.initSortKeys();
		if (getSelectedClients().length > 0)
			selectionPanel.moveToValue(getSelectedClients()[0], 0);
	}

	public void toggleColumnDepot()
	{
		boolean visible = persist.getHost_displayFields().get( HostInfo.depotOfClient_DISPLAY_FIELD_LABEL );
		persist.getHost_displayFields().put( HostInfo.depotOfClient_DISPLAY_FIELD_LABEL, !visible);


		setRebuiltPclistTableModel(false, false);
		selectionPanel.initSortKeys();
		if (getSelectedClients().length > 0)
			selectionPanel.moveToValue(getSelectedClients()[0], 0);
	}


	public boolean treeViewAllowed()
	{
		return true;

		/*
		if ( getOpsiModules() != null && getOpsiModules().get("treeview") != null 
			&& (Boolean) getOpsiModules().get("treeview"))

			return true;
			
		else

		{
			
			
			FTextArea f = new FTextArea(mainFrame, Globals.APPNAME + " - Information", true, 1);
			f.setMessage(configed.getResourceValue("ConfigedMain.TreeViewNotActive"));
			f.setSize(new Dimension(400, 400));
			f.setVisible(true);
			return false;
	}
		*/
	}


	public void handleGroupActionRequest()
	{

		if ( persist.isWithLocalImaging())
		{
			startGroupActionFrame();
		}

		else

		{
			FTextArea f = new FTextArea(mainFrame, Globals.APPNAME + " - Information", true, 1);
			f.setMessage( "not activated");//configed.getResourceValue("ConfigedMain.LicencemanagementNotActive"));
			f.setSize(new Dimension(400, 400));
			f.setVisible(true);
		}
	}


	private void startGroupActionFrame()
	{
		logging.info(this, "startGroupActionFrame clientsFilteredByTree "
		             + activatedGroupModel.getAssociatedClients()
		             + " active "  + activatedGroupModel.isActive());

		if (!activatedGroupModel.isActive())
		{
			FTextArea f = new FTextArea(mainFrame, Globals.APPNAME + " - Information", true, 1);
			f.setMessage( "no group selected");//configed.getResourceValue("ConfigedMain.LicencemanagementNotActive"));
			f.setSize(new Dimension(400, 400));
			f.setVisible(true);

			return;
		}

		if (groupActionFrame == null)
		{
			groupActionFrame = new FGroupActions(this, persist, mainFrame);
			groupActionFrame.setSize(licencesInitDimension);
			groupActionFrame.centerOnParent();

			allFrames.add(groupActionFrame);

		}



		groupActionFrame.start();//!groupActionFrame.isVisible());

		return;
	}

	public void handleProductActionRequest()
	{
		startProductActionFrame();

		/*
		if ( persist.isWithLocalImaging())
		{
			startProductActionFrame();
	}

		else

		{
			FTextArea f = new FTextArea(mainFrame, Globals.APPNAME + " - Information", true, 1);
			f.setMessage( "not activated");//configed.getResourceValue("ConfigedMain.LicencemanagementNotActive"));
			f.setSize(new Dimension(400, 400));
			f.setVisible(true);
	}
		*/
	}


	private void startProductActionFrame()
	{
		logging.info(this, "startProductActionFrame ");


		if (productActionFrame == null)
		{
			productActionFrame = new FProductActions(this, persist, mainFrame);
			productActionFrame.setSize(licencesInitDimension);
			productActionFrame.centerOnParent();
			allFrames.add(productActionFrame);

		}


		productActionFrame.start();//!productActionFrame.isVisible());


		return;
	}



	private void test4AllClients()
	{
		java.util.List<String> allHosts = persist.getHostInfoCollections().getOpsiHostNames();
		
		HashSet<String> selectValues = new HashSet<String>();
		int i = 0;
		
		for (final String host : allHosts)
		{
			i++;
			logging.info(this, "test host " + i + " " + host);
			selectValues.clear();
			selectValues.add(host);
			try{
				Thread.sleep(500);
			}
			catch(Exception ex)
			{
			}
			SwingUtilities.invokeLater(new Thread(){
					public void run()
					{
						setClient(host);
						//setRebuiltPclistTableModel(true, true, selectValues);
					}
				}
			);
		}
	}


	public void handleLicencesManagementRequest()
	{
		persist.retrieveOpsiModules();
		
		if ( persist.isWithLicenceManagement() )
		{
			toggleLicencesFrame();
		}

		else

		{
			FTextArea f = new FTextArea(mainFrame, Globals.APPNAME + " - Information", true, 1);
			f.setMessage(configed.getResourceValue("ConfigedMain.LicencemanagementNotActive"));
			f.setSize(new Dimension(400, 400));
			f.setVisible(true);
		}
		
	}


	public void toggleLicencesFrame()
	{
		if (licencesFrame == null)
		{
			initLicencesFrame();
			allFrames.add(licencesFrame);
			licencesFrame.setSize(licencesInitDimension);
			licencesFrame.setVisible(true);
			mainFrame.visualizeLicencesFramesActive(true);
			return;
		}

		licencesFrame.setVisible(true);//!licencesFrame.isVisible());
		mainFrame.visualizeLicencesFramesActive(licencesFrame.isVisible());
	}

	public void setEditingTarget(EditingTarget t)
	{
		logging.info(this, "setEditingTarget " + t);
		editingTarget = t;
		mainFrame.visualizeEditingTarget (t);
		// what else to do:
		switch (t)
		{
		case CLIENTS:
			logging.debug(this, "setEditingTarget preSaveSelectedClients " + preSaveSelectedClients);


			mainFrame.setConfigPanesEnabled(true);
			mainFrame.setConfigPaneEnabled (mainFrame.getTabIndex(configed.getResourceValue("MainFrame.jPanel_HostProperties")), false);
			mainFrame.setConfigPaneEnabled (mainFrame.getTabIndex(configed.getResourceValue("MainFrame.panel_ProductGlobalProperties")), false);
			mainFrame.setVisualViewIndex(saveClientsViewIndex);//viewClients);
			
			
			logging.debug(this, "setEditingTarget preSaveSelectedClients " + preSaveSelectedClients);

			if (!reachableUpdater.isInterrupted())
				reachableUpdater.interrupt();


			if (preSaveSelectedClients != null && preSaveSelectedClients.size() > 0)
				setSelectedClientsOnPanel(preSaveSelectedClients.toArray(new String[]{}));

			break;

		case DEPOTS:
			logging.info(this, "setEditingTarget  DEPOTS");

			if (!reachableUpdater.isInterrupted())
				reachableUpdater.interrupt();


			initServer();
			mainFrame.setConfigPanesEnabled(false);
			//logging.debug(this, " getTabIndex  " + (configed.getResourceValue("MainFrame.jPanel_NetworkConfig") ) );
			mainFrame.setConfigPaneEnabled (mainFrame.getTabIndex(configed.getResourceValue("MainFrame.jPanel_HostProperties")), true);
			mainFrame.setConfigPaneEnabled (mainFrame.getTabIndex(configed.getResourceValue("MainFrame.panel_ProductGlobalProperties")), true);
			//mainFrame.setVisualViewIndex(mainFrame.getTabIndex(configed.getResourceValue("MainFrame.jPanel_HostProperties")));

			logging.info(this, "setEditingTarget  call setVisualIndex  saved " + saveDepotsViewIndex + " resp. "
			             + mainFrame.getTabIndex(configed.getResourceValue("MainFrame.panel_ProductGlobalProperties")));

			mainFrame.setVisualViewIndex(saveDepotsViewIndex);
			//mainFrame.getTabIndex(configed.getResourceValue("MainFrame.panel_ProductGlobalProperties")));
			break;


		case SERVER:

			if (!reachableUpdater.isInterrupted())
				reachableUpdater.interrupt();

			initServer();
			mainFrame.setConfigPanesEnabled(false);
			//mainFrame.setConfigPaneEnabled (mainFrame.getTabIndex(configed.getResourceValue("MainFrame.jPanel_HostProperties")), false);
			//logging.debug(this, " getTabIndex  " + (configed.getResourceValue("MainFrame.jPanel_NetworkConfig") ) );
			mainFrame.setConfigPaneEnabled (mainFrame.getTabIndex(configed.getResourceValue("MainFrame.jPanel_NetworkConfig")), true);
			//mainFrame.setConfigPaneEnabled (mainFrame.getTabIndex(configed.getResourceValue("MainFrame.panel_ProductGlobalProperties")), false);
			mainFrame.setVisualViewIndex(saveServerViewIndex);
			//mainFrame.getTabIndex(configed.getResourceValue("MainFrame.jPanel_NetworkConfig")));
			break;


		default:
			break;
		}

		resetView(viewIndex);
	}
	
	int shutdowncount = 0;
	
	private void stoppAt(String location)
	{
		logging.info(this, "stoppAt " + location);
		FTextArea fMessage = new FTextArea(mainFrame, 
					"stopp",
					true,
					new String[]{"ok"}, 
					350, 150);
				fMessage.setMessage("stop and look at\n" + this.getClass().getName() + " :\n" +  location);
				fMessage.setVisible(true);
	}
		

	protected void actOnListSelection()
	{
		logging.info(this, "actOnListSelection");
		//logging.debug(this, " ListSelection valueChanged selection empty " + selectionPanel.isSelectionEmpty());
		//logging.debug(this, " ListSelection valueChanged selection count " + selectionPanel.getSelectedValues().size());
		/*
		JOptionPane.showMessageDialog(	mainFrame, 
				  "list selection value changed", //"not synchronous",
				  "debug",
				  JOptionPane.OK_OPTION);
		*/  

		//saveIfIndicated();
		checkSaveAll(true);

		checkErrorList();

		/*
		if (!groupLoading 							// we change a group not by group manager; any group manager choices are not more valid
		        && !changeListByToggleShowSelection)   // and not by the show selection toggle
		{
			//GroupsManager.getInstance(this).emptySelectvalues();
			logging.info(this, "empty select values");
		}
		*/

		logging.info(this, "selectionPanel.getSelectedValues().size(): "
		             +  selectionPanel.getSelectedValues().size());

		boolean groupingExists = selectionPanel.getSelectedValues().size() > 1;

		if ( mainFrame != null ) // when initializing the program the frame may not exist
		{
			logging.info(this, "ListSelectionListener valueChanged selectionPanel.isSelectionEmpty() "
			             + selectionPanel.isSelectionEmpty());

			mainFrame.saveGroupSetEnabled ( groupingExists );
			//mainFrame.deselectSetEnabled ( groupingExists );

			if ( selectionPanel.isSelectionEmpty() )
			{
				setSelectedClients((java.util.List) null);

				//mainFrame.setClientSelectionText("");
			}
			else
			{
				setSelectedClients ( selectionPanel.getSelectedValues() );
				//mainFrame.setClientSelectionText (getSelectedClientsString());
			}

			clientInDepot = "";

			// if (viewIndex == viewClients) 
				hostInfo.initialize();

			Map<String, HostInfo> pcinfos = persist.getHostInfoCollections().getMapOfPCInfoMaps();
			
			
			if (getSelectedClients().length == 1)
			{
				try
				{
					String selClient = getSelectedClients()[0];
					
					// if (viewIndex == viewClients)
					// {
						logging.debug(this, "selClient " + selClient + " infos from map " + ((Object) pcinfos));
						logging.info(this, "addOnListSelection selClient " + selClient);
						logging.info(this, "addOnListSelection pcinfo map  " + pcinfos.get(selClient)); 
						if (pcinfos.get(selClient) == null)
						{
							logging.info(this, "addOnListSelection pcinfo map  null for " + selClient);
							//should only occur when a nonexistent client is requested
						}
						else
						{
							
							hostInfo.produceFrom( pcinfos.get(selClient).getMap() )   ;
						}
					// }
					
				}
				catch (Exception ex)
				{
					logging.warning("valueChanged in ConfigedMain " + ex);
					logging.logTrace(ex);
					// "error occurred  "Ljava.lang.string cannot be cast do java.lang.string"
				}
				mainFrame.setClientInfoediting(true);
			}
			else
			{
				mainFrame.setClientInfoediting(false);
			}

			depotsOfSelectedClients = null; // initialize the following method
			Set depots = getDepotsOfSelectedClients();
			Iterator iter = depots.iterator();
			StringBuffer depotsAdded = new StringBuffer("");


			String singleDepot = "";

			if (iter.hasNext())
			{
				singleDepot = (String) iter.next();
				depotsAdded.append (singleDepot);
			}

			if (iter.hasNext())
				singleDepot = null;

			while (iter.hasNext())
			{
				String appS = (String) iter.next();
				depotsAdded.append(";\n"); depotsAdded.append(appS);
			}


			clientInDepot = depotsAdded.toString();

			// if (viewIndex == viewClients) 
			// {

				if (getSelectedClients().length == 1)
					mainFrame.setClientID(getSelectedClients()[0]);
				else
					mainFrame.setClientID("");
	
				hostInfo.resetGui(mainFrame);
	
				//mainFrame.setClientInDepotText(singleDepot);
	
				mainFrame.enableMenuItemsForClients(getSelectedClients().length);
			// }
			
			logging.info(this, "actOnListSelection update hosts status selectedClients " 
				+ getSelectedClients().length + " as well as " + selectionPanel.getSelectedValues().size());
			//+ ": " 	+ getSelectedClientsString());

			//mainFrame.getHostsStatusInfo().updateValues(null, selectionPanel.getSelectedValues().size(), getSelectedClientsString(),   clientInDepot );
			mainFrame.getHostsStatusInfo().updateValues(pcCount, getSelectedClients().length, getSelectedClientsString(),   clientInDepot );


			if (getSelectedClients().length > 0)
				activatedGroupModel.setActive(false);
			else
				activatedGroupModel.setActive(true);

			//persist.productpropertiesRequestRefresh(); moved to setSelectedClients


			/*
			TreePath first = adaptTreeSelection();

			if (first != null)
				treeClients.scrollPathToVisible(first);
			*/

			//request reloading of client list depending data

			requestRefreshDataForClientSelection();
		}
	}



	

	//ListSelectionListener for client list
	public void valueChanged (ListSelectionEvent e)
	{
		//logging.info(this,"----ListSelectionEvent on client list");

		//Ignore extra messages.
		if (e.getValueIsAdjusting()) return;

		actOnListSelection();

	}


	private String showIntArray (int[] a)
	{
		if  (a == null || a.length == 0) return " ";

		StringBuffer b = new StringBuffer();
		for (int n = 0; n < a.length; n++)
		{
			b.append (" "  + a[n]);
		}

		return b.toString();
	}

	
	
	protected void initMainFrame(  )
	// we call this after we have a PersistenceController
	{

		boolean packFrame = false;

		/*
		java.util.List<String> serverList = persist.getServers();
		if (serverList == null || serverList.size() < 1)
		{
			logging.error(" server list empty" );
			serverList = new ArrayList();
			serverList.add("");
		}
		*/
		myServer =  persist.getHostInfoCollections().getConfigServer();


		// create depotsList
		depotsList = new DepotsList(persist);
		

		if (depotsListSelectionListener ==  null)
		{
			logging.info(this, "create depotsListSelectionListener");
			depotsListSelectionListener =
			    new ListSelectionListener(){
			    	  	private int counter = 0; 
				    public void valueChanged(ListSelectionEvent e)
				    {
				    		counter++;
						logging.info(this, "============ depotSelection event count  " + counter);
					
						
						if (!e.getValueIsAdjusting())
							depotsList_valueChanged();

				    }
			    };
		

			    
		}


		fetchDepots();
		
		depotsList.addListSelectionListener( depotsListSelectionListener );
		depotsList.setInfo(depots);



		//String[] oldSelectedDepots = configed.savedStates.saveDepotSelection.deserialize();

		
		if ( oldSelectedDepots == null )
		{
			depotsList.setSelectedValue (myServer, true);
		}
		else
		{
			ArrayList<Integer> savedSelectedDepots = new ArrayList<Integer>();
			//we collect the indices of the old depots in the current list

			for (int i = 0; i < oldSelectedDepots.length; i++)
			{
				for (int j = 0; j < depotsList.getModel().getSize(); j++)
				{
					if (((String) depotsList.getModel().getElementAt(j)).equals( oldSelectedDepots[i] ))
						savedSelectedDepots.add(j);
				}
			}
			
			if (savedSelectedDepots.size() > 0)
			{
				int[] depotsToSelect = new int[savedSelectedDepots.size()];
				for (int j = 0; j < depotsToSelect.length; j++)
					depotsToSelect[j] = savedSelectedDepots.get(j); //conversion to int
	
				depotsList.setSelectedIndices(depotsToSelect);
			}
			else
			// if none of the old selected depots is in the list we select the config server
			{
				depotsList.setSelectedValue (myServer, true);
			}

		}



		//mainFrame.setChangedDepotSelectionActive(false) //too early
		depotsList_selectedIndices_lastFetched = depotsList.getSelectedIndices();

		//we correct the result of the first selection
		depotsList_selectionChanged = false;
		depotsList.setBackground(Globals.backgroundWhite);


		// create client selection panel
		selectionPanel = new JTableSelectionPanel ( this)
		                 {
			                 @Override
			                 protected void keyPressedOnTable(KeyEvent e)
			                 {
				                 //logging.info(this, "keypressedOnTable key event " + e);
				                 //logging.debug(this, "keypressed: modifiers, mask? " +  e.getModifiersEx() + ", " + KeyEvent.ALT_DOWN_MASK);
				                 if (e.getKeyCode() == KeyEvent.VK_SPACE)
					                 //&& ((e.getModifiersEx() & KeyEvent.ALT_DOWN_MASK) == KeyEvent.ALT_DOWN_MASK))
				                 {
					                 //logging.debug(this, "keypressed: space");
					                 startRemoteControlForSelectedClients();

					                 //dialogRemoteControl.commit(); //ohne weitere Abfrage
					                 //requestFocus(); //kehre zurück zum Hauptfenster
				                 }



				                 else if (e.getKeyCode() == KeyEvent.VK_F10)
				                 {
					                 logging.debug(this, "keypressed: f10");
					                 mainFrame.showPopupClients();
				                 }

			                 }
		                 };




		/*

		{


		                @Override
		                public void keyPressed(KeyEvent e)
		                {
		                 //logging.debug(this, "key event " + e);
		                 super.keyPressed(e);
		                 //logging.debug(this, "keypressed: modifiers, mask? " +  e.getModifiersEx() + ", " + KeyEvent.ALT_DOWN_MASK);
		                 if (e.getKeyCode() == KeyEvent.VK_SPACE)
			                 //&& ((e.getModifiersEx() & KeyEvent.ALT_DOWN_MASK) == KeyEvent.ALT_DOWN_MASK))
		                 {
			                 //logging.debug(this, "keypressed: space");
			                 startRemoteControlForSelectedClients();

			                 //dialogRemoteControl.commit(); //ohne weitere Abfrage
			                 //requestFocus(); //kehre zurück zum Hauptfenster
		                 }

		                

		                 else if (e.getKeyCode() == KeyEvent.VK_F10)
		                 {
			                 logging.debug(this, "keypressed: f10");
			                 mainFrame.showPopupClients();
		                 }

		                }

		                @Override
		                public void keyReleased(KeyEvent e)
		                {
		                 super.keyReleased(e);
		                }

		               };
		               
		              */


		selectionPanel.setModel(buildPclistTableModel(true));
		setSelectionPanelCols();

		selectionPanel.initSortKeys();

		mainFrame = new MainFrame(appletHost, this,  selectionPanel, depotsList, treeClients, multiDepot);

		// for passing it to message frames everywhere
		Globals.mainFrame = mainFrame;

		//setting the similar global values as well
		if (configed.isApplet)
		{
			Globals.container1 = appletHost.getContentPane();
			Globals.mainContainer = appletHost;
		}
		else
		{
			Globals.container1 = mainFrame;
			Globals.mainContainer = mainFrame;
		}


		/*
		Map<String, Boolean> itemsToDisable = new HashMap<String, Boolean>();

		itemsToDisable.put(MainFrame.ITEM_ADD_CLIENT, true);
		itemsToDisable.put(MainFrame.ITEM_DELETE_CLIENT, true);

		mainFrame.setDisabledMenuItems(itemsToDisable);
		*/

		mainFrame.enableMenuItemsForClients(0);



		//hwInfoFactory = new HwInfoFactory(hwAuditConf);

		//rearranging visual components
		if (packFrame)
		{
			mainFrame.pack();
		}
		else
		{
			mainFrame.validate();
		}


		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		GraphicsDevice[] gs = ge.getScreenDevices();

		int wTaken = MainFrame.fwidth;
		int hTaken = MainFrame.fheight;

		for (int i = 0; i < gs.length; i++)
		{
			DisplayMode dm = gs[i].getDisplayMode();
			logging.info(this, "width " + i + ": "  + dm.getWidth());
			if ( dm.getWidth() > wTaken )
			{
				wTaken = dm.getWidth();
				hTaken = dm.getHeight();
			}
			logging.info(this, "height " + i + ": "  + dm.getHeight());
		}


		final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		//int width = screenSize.width;
		//int height = screenSize.height;


		logging.info(this, "startSizing width, height " + wTaken + ", " + hTaken);


		int wDiff = wTaken - 30 - MainFrame.fwidth;
		if (wDiff < 0)
			wDiff = 0;
		int hDiff = hTaken- 30 - MainFrame.fheight;
		if (hDiff < 0)
			hDiff = 0;

		//take 2/3 from space > MainFrame.fwidth, MainFrame.fheight
		//after giving some pixels for taskbars

		final int width =MainFrame.fwidth + (wDiff * 2)/ 3;
		final int height = MainFrame.fheight + (hDiff * 2)/ 3;


		SwingUtilities.invokeLater(new Runnable() {
			                           public void run()
			                           {
				                           try{
					                           Thread.sleep(1);
				                           }
				                           catch(InterruptedException ex)
				                           {
				                           }
				                           try{
					                           mainFrame.startSizing(width, height);

					                           Dimension frameSize = mainFrame.getSize();
					                           if (frameSize.height > screenSize.height)
					                           {
						                           frameSize.height = screenSize.height;
					                           }
					                           if (frameSize.width > screenSize.width)
					                           {
						                           frameSize.width = screenSize.width;
					                           }
					                           mainFrame.setLocation((screenSize.width - frameSize.width) / 2, (screenSize.height - frameSize.height) / 2);
				                           }
				                           catch(Exception ex)
				                           {
					                           logging.info(this, "sizing or locating error " + ex);
				                           }


			                           }
		                           }
		                          );

		//center the frame:
		//Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();





		// init visual states
		logging.debug(this, "mainframe nearly initialized");

		//mainFrame.saveConfigurationsSetEnabled(false);
		mainFrame.saveGroupSetEnabled(false);
		//mainFrame.deselectSetEnabled (false);


		// have a go now


		if (appletHost == null) // we really use the frame of mainFrame
			mainFrame.setVisible(true);

		else
		{
			/* does not work
			mainFrame.allPane.setSize(appletHost.getSize().width-2, appletHost.getSize().height-2);
			mainFrame.allPane.validate();
			*/

			//does not always work
			/*
			SwingUtilities.invokeLater(
				new Runnable(){
					public void run(){
						mainFrame.allPane.setSize(appletHost.getSize().width-2, appletHost.getSize().height-2);
						mainFrame.allPane.validate();
					}
				}
			);
			*/
			//this hack seems to work better
			/*
			new Thread(){
					public void run()
					{
						//adjust(1000, appletHost.getSize().width-1, appletHost.getSize().height - 20);
						adjust(4000, appletHost.getSize().width-1, appletHost.getSize().height - 20);
					}
				}
			.start();
			*/

		}



	}


	protected void initLicencesFrame()
	{
		long startmillis = System.currentTimeMillis();
		logging.info(this, "initLicencesFrame start " + startmillis);
		WaitCursor waitCursor = new WaitCursor( mainFrame.retrieveBasePane(), mainFrame.getCursor(), "initLicencesFrame" );
		// general

		licencesFrame = new TabbedFrame(mainFrame, (TabController) this);
		Globals.frame1 = licencesFrame;
		if (!configed.isApplet)
		{
			Globals.container1 = licencesFrame.getContentPane();
		}
		/*is set beforehand:
		else
		{
			Globals.container1 = appletHost.getContentPane();
	}
		*/

		licencesFrame.setGlobals(Globals.getMap());
		licencesFrame.setTitle(Globals.APPNAME + "  " + myServer + ":  " + configed.getResourceValue("ConfigedMain.Licences"));


		licencesStatus = (LicencesTabStatus) ((TabController) this).getStartTabState();


		// global table providers
		Vector<String> columnNames = new Vector<String>();
		columnNames.add("licensePoolId"); columnNames.add("description");
		Vector<String> classNames = new Vector<String>();
		classNames.add("java.lang.String"); classNames.add("java.lang.String");

		licencePoolTableProvider =
		    new DefaultTableProvider(
		        new RetrieverMapSource(columnNames, classNames,
		                               new MapRetriever(){
			                               public Map retrieveMap()
			                               {
				                               return persist.getLicencepools();
			                               }
		                               })
		    );

		columnNames = new Vector<String>();
		columnNames.add("softwareLicenseId"); columnNames.add("licensePoolId"); columnNames.add("licenseKey");
		classNames = new Vector<String>();
		classNames.add("java.lang.String"); classNames.add("java.lang.String"); classNames.add("java.lang.String");

		licenceOptionsTableProvider =
		    new DefaultTableProvider(
		        new RetrieverMapSource(columnNames, classNames,
		                               new MapRetriever(){
			                               public Map retrieveMap()
			                               {
				                               return persist.getRelationsSoftwareL2LPool();
			                               }
		                               })
		    );

		columnNames = new Vector<String>();
		columnNames.add("licenseContractId"); columnNames.add("partner");
		columnNames.add("conclusionDate"); columnNames.add("notificationDate");
		columnNames.add("expirationDate"); columnNames.add("notes");
		classNames = new Vector<String>();
		classNames.add("java.lang.String"); classNames.add("java.lang.String");
		classNames.add("java.lang.String"); classNames.add("java.lang.String");
		classNames.add("java.lang.String"); classNames.add("java.lang.String");

		licenceContractsTableProvider =
		    new DefaultTableProvider(
		        new RetrieverMapSource(columnNames, classNames,
		                               new MapRetriever(){
			                               public Map retrieveMap()
			                               {
				                               //logging.debug(this, " --------- getLicenceContracts in retrieve Map");
				                               return persist.getLicenceContracts();
			                               }
		                               })
		    );

		columnNames = new Vector<String>();
		columnNames.add(LicenceEntry.idKEY); columnNames.add(LicenceEntry.licenceContractIdKEY); 
		columnNames.add(LicenceEntry.typeKEY); columnNames.add(LicenceEntry.maxInstallationsKEY); 
		columnNames.add(LicenceEntry.boundToHostKEY); columnNames.add(LicenceEntry.expirationDateKEY);
		
		classNames = new Vector<String>();
		classNames.add("java.lang.String"); classNames.add("java.lang.String");
		classNames.add("java.lang.String"); classNames.add("de.uib.utilities.ExtendedInteger"); //classNames.add("java.lang.Integer");
		classNames.add("java.lang.String"); classNames.add("java.lang.String");

		softwarelicencesTableProvider =
		    new DefaultTableProvider(
		        new RetrieverMapSource(columnNames, classNames,
		                               new MapRetriever(){
			                               public Map retrieveMap()
			                               {
				                               return persist.getSoftwareLicences();
			                               }
		                               })
		    );


		//panelAssignToLPools
		licencesPanelsTabNames.put(LicencesTabStatus.LICENCEPOOL, configed.getResourceValue("ConfigedMain.Licences.TabLicencepools"));
		controlPanelAssignToLPools = new ControlPanelAssignToLPools(persist, this);
		addClient (LicencesTabStatus.LICENCEPOOL, controlPanelAssignToLPools.getTabClient());
		allControlMultiTablePanels.add(controlPanelAssignToLPools);


		//panelEnterLicence
		licencesPanelsTabNames.put(LicencesTabStatus.ENTER_LICENCE, configed.getResourceValue("ConfigedMain.Licences.TabNewLicence"));
		controlPanelEnterLicence = new ControlPanelEnterLicence(persist, this);
		addClient (LicencesTabStatus.ENTER_LICENCE, controlPanelEnterLicence.getTabClient());
		allControlMultiTablePanels.add(controlPanelEnterLicence);


		//panelEditLicence
		licencesPanelsTabNames.put(LicencesTabStatus.EDIT_LICENCE, configed.getResourceValue("ConfigedMain.Licences.TabEditLicence"));
		controlPanelNewLicence = new ControlPanelNewLicence(persist, this);
		addClient(LicencesTabStatus.EDIT_LICENCE, controlPanelNewLicence.getTabClient());
		allControlMultiTablePanels.add(controlPanelNewLicence);


		//panelUsage
		licencesPanelsTabNames.put(LicencesTabStatus.USAGE, configed.getResourceValue("ConfigedMain.Licences.TabLicenceUsage"));
		controlPanelLicencesUsage = new ControlPanelLicencesUsage(persist, this);
		addClient(LicencesTabStatus.USAGE, controlPanelLicencesUsage.getTabClient());
		allControlMultiTablePanels.add(controlPanelLicencesUsage);

		//panelReconciliation
		licencesPanelsTabNames.put(LicencesTabStatus.RECONCILIATION, configed.getResourceValue("ConfigedMain.Licences.TabLicenceReconciliation"));
		controlPanelLicencesReconciliation = new ControlPanelLicencesReconciliation(persist, this);
		addClient(LicencesTabStatus.RECONCILIATION, controlPanelLicencesReconciliation.getTabClient());
		allControlMultiTablePanels.add(controlPanelLicencesReconciliation);


		// panelStatistics
		licencesPanelsTabNames.put(LicencesTabStatus.STATISTICS,  configed.getResourceValue("ConfigedMain.Licences.TabStatistics"));
		controlPanelLicencesStatistics = new ControlPanelLicencesStatistics(persist, this);
		addClient(LicencesTabStatus.STATISTICS, controlPanelLicencesStatistics.getTabClient());
		allControlMultiTablePanels.add(controlPanelLicencesStatistics);


		licencesFrame.start();
		licencesFrame.setSize(licencesInitDimension);

		//center the frame:
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		Dimension frameSize = licencesFrame.getSize();
		if (frameSize.height > screenSize.height)
		{
			frameSize.height = screenSize.height;
		}
		if (frameSize.width > screenSize.width)
		{
			frameSize.width = screenSize.width;
		}
		licencesFrame.setLocation((screenSize.width - frameSize.width) / 2, (screenSize.height - frameSize.height) / 2);

		waitCursor.stop();

		long endmillis = System.currentTimeMillis();
		logging.debug(this, "initLicencesFrame " + endmillis +  " diff " + (endmillis - startmillis) );
	}

	protected void login( Vector<String> savedServers )
	// returns true if we have a PersistenceController and are connected
	{
		/*
		if (configed.isApplet)
			// we should be authenticated, try connection and produce persistence controller
		{
			persist = DPassword.producePersistenceController(host);
			return true;
	}
		*/


		logging.debug(this, " create password dialog " );
		dpass = new DPassword(this);
		
		//set list of saved servers
		if (savedServers != null && savedServers.size() > 0)
			dpass.setServers(savedServers);
		
		//check if we started with preferred values
		if (HOST != null && !HOST.equals(""))
			dpass.setHost(HOST);
		if (USER != null)
			dpass.setUser(USER);
		if (PASSWORD != null)
			dpass.setPassword(PASSWORD);

		if (
		    ((HOST != null && USER != null && PASSWORD != null))
		)
		{
			// Auto login
			logging.info(this, "start with given credentials");
			//dpass.activate();
			//dpass.setVisible(true);
			dpass.try_connecting();
		}

		if ( persist == null || persist.getConnectionState().getState() != ConnectionState.CONNECTED )
		{
			logging.info(this, "become interactive");
			dpass.setVisible(true);
			dpass.activate();
			dpass.setModal(true);
			dpass.setAlwaysOnTop(true);
			//dpass will give back control and call loadDataAndGo
		}

		/*
		int countWait = 0;
		while  
		(
			!(PersistenceControllerFactory.getConnectionState().getState() == ConnectionState.CONNECTED)
		)
		{
			try
			{
				Thread.sleep (1000);
				countWait++;
				logging.info(this,  "countWait " + countWait + " waited,  we are in state " + " " + PersistenceControllerFactory.getConnectionState());
				
			}
			catch (InterruptedException ex)
			{
			}
		}
				
		
		//dpass sets a new PersistenceController; we check it:
		if ( persist != null && persist.getConnectionState().getState() == ConnectionState.CONNECTED )
		{
			dpass.setVisible(false);
			result = true;
		}
		

		logging.info(this, "------ we are connected in ConfigedMain");
		*/
	}

	public PersistenceController getPersistenceController()
	{
		return persist;
	}

	public void setPersistenceController (PersistenceController persis)
	{
		persist = persis;
	}

	public void setAppTitle (String s)
	{
		appTitle = s;
	}

	public String getAppTitle ()
	{
		return appTitle;
	}


	public RequirementsTableModel getRequirementsModel()
	{
		return requirementsModel;
	}


	private String formatDate(String original)
	{
		if (original != null)
		{
			String[] ls = original.split("");
			if (ls.length >= 15)
				return ls[1]+ls[2]+ls[3]+ls[4]+'-'+ls[5]+ls[6]+'-'+ls[7]+ls[8]+' '+ls[9]+ls[10]+':'+ls[11]+ls[12]+':'+ls[13]+ls[14];
			else
				return original;
		}
		else
			return "";
	}

	protected Map<String, Boolean> producePcListForDepots(String[] depots)
	{
		logging.info(this, " producePcListForDepots " + logging.getStrings(depots));
		Map <String, Boolean> m = persist.getHostInfoCollections().getPcListForDepots( depots );

		//logging.debug(this, " producePcListForDepots returns "  + m);

		if (m != null)
			pcCount = m.size();

		if (mainFrame != null)
		{
			mainFrame.getHostsStatusInfo().updateValues(pcCount, null, null, null);
			//persist.getHostInfoCollections().getCountClients() > 0
			//but we are testing:
			//selectionPanel.setMissingDataPanel( pcCount < 100);
			selectionPanel.setMissingDataPanel( 
				persist.getHostInfoCollections().getCountClients() == 0
				);
		}
			
			
			

		return m;
	}

	protected Map<String, Boolean> filterMap(Map<String, Boolean> map0, Set<String> filterset)
	{
		HashMap<String, Boolean> result = new HashMap<String, Boolean>();

		if (filterset == null)
			return result;


		Iterator iter = filterset.iterator();
		while (iter.hasNext())
		{
			String key = (String) iter.next();
			if (map0.containsKey(key))
			{
				result.put(key, map0.get(key));
				//logging.info(this, " added by filter "  + key + ", " + map0.get(key));
			}
		}

		return result;
	}

	/*
	private TreePath adaptTreeSelection()
{
		//building tree selection paths
		ArrayList<TreePath> selpathlist = new ArrayList<TreePath>();
		
		TreePath firstPath = null;
		
		for ( int j = 0; j < getSelectedClients().length; j++)
		{
			//logging.debug(this, "getInvertedTreePaths for getSelectedClients() " + j );
			
			ArrayList<TreePath> listforclient = treeClients.getInvertedTreePaths(getSelectedClients()[j]);
			
			//logging.debug(this, " ..  " + listforclient);
			
			for ( int k = 0; k < listforclient.size(); k++)
			{
				if (firstPath == null);
					firstPath = listforclient.get(k);
					
				selpathlist.add(listforclient.get(k));
			}
		}
		//selectedTreePaths = selpathlist.toArray(new TreePath[selpathlist.size()]);
		//resetSelectedTreeSubjects();
		//logging.debug(this, " new tree selection ");
		return firstPath;
}
	*/
	
	
	private int buildPclistTableModelCounter = 0;

	protected TableModel buildPclistTableModel(boolean rebuildTree)
	{
		logging.debug(this, " --------- buildPclistTableModel rebuildTree " + rebuildTree);
		DefaultTableModel model = new DefaultTableModel()
		                          {
			                          public boolean isCellEditable
			                          (int rowIndex,
			                           int columnIndex)
			                          { return false; }
		                          };

		Map<String, Boolean> pclist = new  HashMap<String, Boolean>();

		// dont make pclist global  and clear it here
		// since pclist is bound to a variable in persistencecontroller
		//  which will be cleared
		// logging.debug(this, " pclist " + pclist);

		Map<String, Boolean> unfilteredList = producePcListForDepots( getSelectedDepots() );

		logging.debug(this, " unfilteredList ");
		//+ unfilteredList);

		buildPclistTableModelCounter++;
		logging.info(this, "buildPclistTableModel, counter " + buildPclistTableModelCounter + "   rebuildTree  " + rebuildTree);

		if (rebuildTree)
		{
			logging.info(this, "------------ buildPclistTableModel, rebuildTree " + rebuildTree);
			String[] allPCs = new TreeMap<String, Boolean>(unfilteredList).keySet().toArray(new String[]{});

			logging.debug(this, "buildPclistTableModel, rebuildTree, allPCs  "+ logging.getStrings(allPCs));

			treeClients.clear();
			treeClients.setClientInfo( persist.getHostInfoCollections().getMapOfAllPCInfoMaps() );
			//persist.clientsWithFailedRequestRefresh();
			treeClients.produceTreeForALL(allPCs);
			//persist.clientsWithFailedRequestRefresh();
			//treeClients.produceTreeForFAILED(persist.getClientsWithFailed());//new String[]{"fscnoteb1.uib.local"});
			treeClients.produceAndLinkGroups( persist.getHostGroups()  );
			treeClients.associateClientsToGroups( allPCs, persist.getFObject2Groups() );
			//logging.info(this, "tree produced");
		}


		Map<String, Boolean> pclist0 = filterMap(unfilteredList, clientsFilteredByTree);

		logging.info(this, " filterClientList " + filterClientList);
		//logging.injectLogLevel(logging.LEVEL_INFO);
		if (filterClientList)
		{
			//logging.info(this,
			//	"selected depots " + logging.getStrings(getSelectedDepots()));


			//Map unfilteredList = producePcListForDepots( getSelectedDepots() );

			if (pclist0 != null)
			{
				Object[] pcEntries = pclist0.entrySet().toArray();


				logging.info(this, "buildPclistTableModel with filterCLientList " + 
				              "selected pcs " +getSelectedClients().length);

				for (int i = 0; i < pcEntries.length; i++)
				{
					Map.Entry ob = (Map.Entry) pcEntries[i];

					//logging.debug(this, " pcEntries [" +  i + "] " + ob);

					String key = (String) ob.getKey();
					for ( int j = 0; j < getSelectedClients().length; j++)
					{
						//logging.debug(this, "getSelectedClients()["+ j + "] " + getSelectedClients()[j] + " ... key " );

						if (getSelectedClients()[j].equals(key))
						{
							//logging.debug(this, " equals , therefore we take " + key);

							pclist.put(key, (Boolean) ob.getValue());
							break;
						}
					}
				}

			}
		}
		else
		{
			pclist = pclist0; //producePcListForDepots( getSelectedDepots() );

		}


		//adaptTreeSelection();


		//building table model
		Map<String, HostInfo> pcinfos = persist.getHostInfoCollections().getMapOfPCInfoMaps();


		if  (pclist != null)
		{

			host_displayFields = persist.getHost_displayFields();
			
			//test
			//host_displayFields.put("install by shutdown", true);
			//host_displayFields.put(HostInfo.clientUefiBoot_DISPLAY_FIELD_LABEL, true);
			//host_displayFields.put(HostInfo.clientWanConfig_DISPLAY_FIELD_LABEL, true);
			//host_displayFields.put(HostInfo.depotOfClient_DISPLAY_FIELD_LABEL, true);

			
			
			for (String field : host_displayFields.keySet())
			{
				if (host_displayFields.get(field))
					model.addColumn(configed.getResourceValue("ConfigedMain.pclistTableModel." + field));
			}
			
			logging.info(this, "buildPclistTableModel host_displayFields " + host_displayFields);
			/*
			int countDisplayFields = 0;
			for (String field : host_displayFields.keySet())
			{
				if ( host_displayFields.get( field ) )
					countDisplayFields++;
			}
			logging.info(this, "buildPclistTableModel host_displayFields count " + countDisplayFields);
			*/
			
			Object[] pcs = pclist.entrySet().toArray();
			for (int i = 0; i < pcs.length; i++)
			{
				Map.Entry ob = (Map.Entry) pcs[i];
				Object key = ob.getKey();

				//logging.debug(this, " pcList ---- " + i + " key " + key);
				HostInfo pcinfo = pcinfos.get(key);
				if (pcinfo == null)
					pcinfo = new HostInfo();
				
				HashMap<String, Object> rowmap = pcinfo. getDisplayRowMap0();
				
				String sessionValue = "";
				if (sessionInfo.get( key ) != null)
					sessionValue = "" + sessionInfo.get(key);;
				
				rowmap.put( HostInfo.clientSessionInfo_DISPLAY_FIELD_LABEL, sessionValue );
				rowmap.put( HostInfo.clientConnected_DISPLAY_FIELD_LABEL, (Boolean)reachableInfo.get(key) );

				ArrayList<Object> rowItems = new ArrayList<Object>();
				
				for (String field  : host_displayFields.keySet() )
				{
					//logging.info(this,  "buildPclistTableModel field, host_displayFields.get(field)), rowmap.get(field) "
					//	+ field + ", " + host_displayFields.get(field) + ", " +   rowmap.get(field) );
					if (host_displayFields.get(field))
						rowItems.add ( rowmap.get( field ) );
				}
				
				/*
				if (key.equals("fscnoteb1.uib.local") )
				{
					logging.info(this, "*** host_displayFields size, content " + host_displayFields.size() + ": " +  host_displayFields);
					logging.info(this, "*** rowmap size, content " + rowmap.size() + ": " +    rowmap);
					logging.info(this, "*** rowItems size, content " + rowItems.size() + ": " +   rowItems);
				}
				*/

				//logging.info(this,  "buildPclistTableModel rowItems.size() " + rowItems.size()); 
				
				model.addRow (rowItems.toArray());
			}
		}
		//logging.info(this, "buildPclistTableModel, model built, selected: " + getSelectedClientsInTable());
		
		logging.info(this, "buildPclistTableModel, model column count " + model.getColumnCount() );
		
		return model;
	}

	/**
	* selects a client
	*
	* @param clientname    
	*
	*/
	public void setClient(String clientname)
	{
		logging.info(this, "setClient " + clientname);
		if (clientname == null)
			setSelectedClientsOnPanel(new String[]{});
		else
		{
			setSelectedClientsOnPanel(new String[]{clientname});
			//implies:
			//setSelectedClientsArray(new String[]{clientname});
			actOnListSelection();
		}
	}


	static int activateGroupCounter = 0;
	
	/* one model rebuild too much
	private boolean activateGroupWithModelRebuild(String groupname, boolean groupMode)
	{
		activateGroupCounter++;
		logging.info(this, "activateGroup  " + groupname +  " activateGroupCounter " + activateGroupCounter);
		if (groupname == null)
			return false;

		if (!treeClients.groupNodesExists() || treeClients.getGroupNode(groupname) == null)
		{
			logging.warning("no group " + groupname);
			return false;
		}


		GroupNode node = treeClients.getGroupNode(groupname);


		TreePath path = treeClients.getPathToNode(node);

		//setGroupByTree(node, path);
		activateGroupByTreeWithModelRebuild(node, path);
		//logging.debug(this, "clientsFilteredByTree " + clientsFilteredByTree);

		logging.info(this, "expand activated  path "  + path);
		treeClients.expandPath(path);


		//System.exit(0);

		return true;
	}
	*/
	
	

	/**
	* activates a group
	*
	* @param groupname    
	*
	*/
	/*
	public boolean activateGroup(String groupname)
	{
	
		return activateGroupWithModelRebuild(groupname, true);
	}
	*/
	public boolean activateGroup(String groupname)
	{
		activateGroupCounter++;
		logging.info(this, "activateGroup  " + groupname +  " activateGroupCounter " + activateGroupCounter);
		if (groupname == null)
			return false;

		if (!treeClients.groupNodesExists() || treeClients.getGroupNode(groupname) == null)
		{
			logging.warning("no group " + groupname);
			return false;
		}


		GroupNode node = treeClients.getGroupNode(groupname);


		TreePath path = treeClients.getPathToNode(node);

		//setGroupByTree(node, path);
		activateGroupByTree(true, node, path);
		//logging.debug(this, "clientsFilteredByTree " + clientsFilteredByTree);

		logging.info(this, "expand activated  path "  + path);
		treeClients.expandPath(path);


		//System.exit(0);

		return true;
	}
	
	
	/**
	* activates a group and selects all clients
	*
	* @param groupname    
	*
	*/
	public void setGroup(String groupname)
	{
		logging.info(this, "setGroup " + groupname);
		
		if  (!activateGroup(groupname))
			return;

		setSelectedClientsCollectionOnPanel(clientsFilteredByTree);
	}




	protected void requestRefreshDataForClientSelection()
	{

		logging.info(this, "requestRefreshDataForClientSelection");
		requestReloadStatesAndActions();
		hostConfigs = null;
		persist.getEmptyLogfiles();
	}





	public void requestReloadStatesAndActions()
	{
		requestReloadStatesAndActions(false);
	}

	public void requestReloadStatesAndActions(boolean onlyUpdate)
	{
		logging.info(this, "requestReloadStatesAndActions , only updating " + onlyUpdate);
		persist.productpropertiesRequestRefresh();

		if (onlyUpdate)
			localbootStatesAndActionsUPDATE = true;
		else
			localbootStatesAndActions = null;

		netbootStatesAndActions = null;
	}



	public String[] getSelectedClients()
	{
		//logging.info(this, "getSelectedClients() " + Arrays.toString(selectedClients));
		return selectedClients;
	}

	protected void setSelectedClientsArray(String[] a)
	{
		logging.info(this, "getSelectedClients() up to now size " + logging.getSize(selectedClients));
		//logging.info(this, "setSelectedClientsArray() to  " + Arrays.toString(a));
		selectedClients = a;
		if (selectedClients == null)
		{
			pcName = null;
		}
		else if (selectedClients.length == 0)
		{
			pcName = "";
		}
		else
		{
			pcName = selectedClients[0];
		}
		//logging.info(this, "getSelectedClients() up to now " + Arrays.toString(selectedClients));
		


	}


	/** 
		transports the selected values of the selection panel
		to the outer world
	*/
	public ArrayList<String> getSelectedClientsInTable()
	{
		if (selectionPanel == null)
			return new ArrayList<String> ();

		return selectionPanel.getSelectedValues();
	}
	
	
	private void setSelectedClients(java.util.List<String> clientnames)
	{
		if (clientnames == null)
			logging.info(this, "setSelectedClients clientnames null");
		else
			logging.info(this, "setSelectedClients clientnames size " + clientnames.size());

		if (clientnames == null)
			return;

		if (clientnames.equals(saveSelectedClients))
		{
			logging.info(this, "setSelectedClients clientnames.equals(saveSelectedClients)");
			//return;
		}

		saveSelectedClients = clientnames;


		if (clientnames != null) requestRefreshDataForClientSelection();

		//init
		setSelectedClientsArray(new String[]{});

		if (clientnames != null && clientnames.size() > 0)
		{
			setSelectedClientsArray(clientnames.toArray(new String[clientnames.size()]) );
		}

		treeClients.produceActiveParents(getSelectedClients());

		clearLogPage();
		clearSoftwareInfoPage();

		if (getViewIndex() != viewClients) // change in selection not via clientpage (i.e. via tree)
		{
			logging.debug(this, "getSelectedClients  " + logging.getStrings(getSelectedClients())
			              + " ,  getViewIndex, viewClients: " +  getViewIndex() + ", " + viewClients);
			int newViewIndex = getViewIndex();
			resetView(newViewIndex);
		}

	}

	public Map<String, TreePath> getActiveTreeNodes()
	{
		return activeTreeNodes;
	}

	public ArrayList<TreePath> getActivePaths()
	{
		return activePaths;
	}


	public boolean getFilterClientList()
	{
		return filterClientList;
	}


	public void toggleFilterClientList()
	{
		changeListByToggleShowSelection = true;
		logging.info(this, "toggleFilterClientList   " + filterClientList);
		setFilterClientList(!filterClientList);
		changeListByToggleShowSelection = false;
	}
	
	private void setSelectionPanelCols()
	{
		logging.info(this, "setSelectionPanelCols ");
		
		final int iconColumnMaxWidth = 100;
		final int iconColumnPrefWidth = 70;
		
		if (persist.getHost_displayFields().get(HostInfo.clientConnected_DISPLAY_FIELD_LABEL))
		{
			int col = selectionPanel.getTableModel().findColumn(
			              configed.getResourceValue("ConfigedMain.pclistTableModel." + HostInfo.clientConnected_DISPLAY_FIELD_LABEL)
			          );

			javax.swing.table.TableColumn column = selectionPanel.getColumnModel().getColumn(col);
			
			column.setMaxWidth(iconColumnMaxWidth);
			column.setPreferredWidth(iconColumnPrefWidth);

			column.setCellRenderer(new BooleanIconTableCellRenderer(
			                           Globals.createImageIcon("images/new_network-connect2.png", ""),
			                           Globals.createImageIcon("images/new_network-disconnect.png", "")
			                       )
			                      );

			//new StandardTableCellRenderer());//selectionPanel.getDefaultRenderer(Boolean.class));
		}
		
		if (persist.getHost_displayFields().get( HostInfo.clientUefiBoot_DISPLAY_FIELD_LABEL ) )
		{
			
			Vector<String> columns = new Vector<String>();
			for (int i = 0; i < selectionPanel.getTableModel().getColumnCount(); i++)
			{
				columns.add( selectionPanel.getTableModel().getColumnName( i ) );
			}
			logging.info(this, "showAndSave columns are " + columns + ", search for " + HostInfo.clientUefiBoot_DISPLAY_FIELD_LABEL);
			
			int col = selectionPanel.getTableModel().findColumn(
			              configed.getResourceValue("ConfigedMain.pclistTableModel." +  HostInfo.clientUefiBoot_DISPLAY_FIELD_LABEL)
			          );

			logging.info(this, "setSelectionPanelCols ,  found col " + col);
			
			
			logging.info(this, "showAndSave found col " + col);
			
			
			if (col > -1)
			{
				javax.swing.table.TableColumn column = selectionPanel.getColumnModel().getColumn(col);
				logging.info(this, "setSelectionPanelCols  column " + column.getHeaderValue());
				column.setMaxWidth(iconColumnMaxWidth);
				column.setPreferredWidth(iconColumnPrefWidth);
				//System.exit(2);
				//column.setCellRenderer(new de.uib.utilities.table.gui.CheckBoxTableCellRenderer());
				column.setCellRenderer(
					new BooleanIconTableCellRenderer(
							Globals.createImageIcon("images/checked_withoutbox.png", ""),
			                        	null //Globals.createImageIcon("images/checked_box.png", "")
			                       )
			                      );
				
			}
		}
		
		if (persist.getHost_displayFields().get( HostInfo.clientWanConfig_DISPLAY_FIELD_LABEL) )
		{
			
			Vector<String> columns = new Vector<String>();
			for (int i = 0; i < selectionPanel.getTableModel().getColumnCount(); i++)
			{
				columns.add( selectionPanel.getTableModel().getColumnName( i ) );
			}
			logging.info(this, "showAndSave columns are " + columns + ", search for " + HostInfo.clientWanConfig_DISPLAY_FIELD_LABEL);
			
			
			
			int col = selectionPanel.getTableModel().findColumn(
			              configed.getResourceValue("ConfigedMain.pclistTableModel." + HostInfo.clientWanConfig_DISPLAY_FIELD_LABEL)
			          );

			logging.info(this, "setSelectionPanelCols ,  found col " + col);
			
			
			if (col > -1)
			{
				javax.swing.table.TableColumn column = selectionPanel.getColumnModel().getColumn(col);
				logging.info(this, "setSelectionPanelCols  column " + column.getHeaderValue() );
				column.setMaxWidth(iconColumnMaxWidth);
				column.setPreferredWidth(iconColumnPrefWidth);
				
				//column.setCellRenderer(new de.uib.utilities.table.gui.CheckBoxTableCellRenderer());
				column.setCellRenderer(
					new BooleanIconTableCellRenderer(
			                           Globals.createImageIcon("images/checked_withoutbox.png", ""),
			                           null //Globals.createImageIcon("images/checked_box.png", "")
			                       )
			                      );
			}

		}
		
		
		/*
		if (persist.getHost_displayFields().get( HostInfo.installByShutdown_DISPLAY_FIELD_LABEL ) )
		{
			int col = selectionPanel.getTableModel().findColumn(
			              configed.getResourceValue("ConfigedMain.pclistTableModel." + HostInfo.installByShutdown_DISPLAY_FIELD_LABEL )
			          );

			logging.info(this, "setSelectionPanelCols ");
			javax.swing.table.TableColumn column = selectionPanel.getColumnModel().getColumn(col);
			logging.info(this, "setSelectionPanelCols  column " + column);
			column.setMaxWidth(50);
			column.setPreferredWidth(40);
			
			column.setCellRenderer(new de.uib.utilities.table.gui.CheckBoxTableCellRenderer());

		}
		*/
		
		
		
	}
	
	protected void setRebuiltPclistTableModel()
	{
		setRebuiltPclistTableModel(true);
	}
	
	protected void setRebuiltPclistTableModel(boolean restoreSortKeys)
	{
		setRebuiltPclistTableModel(restoreSortKeys, true);
	}

	protected void setRebuiltPclistTableModel(boolean restoreSortKeys, boolean rebuildTree)
	{
		logging.info(this, "setRebuiltPclistTableModel, we have selected Set : " + selectionPanel.getSelectedSet());
		
		setRebuiltPclistTableModel(restoreSortKeys, true, selectionPanel.getSelectedSet());
	}


	private int reloadCounter = 0; 
	protected void setRebuiltPclistTableModel(boolean restoreSortKeys, boolean rebuildTree, Set<String> selectValues)
	{
		logging.info(this,
		             "setRebuiltPclistTableModel(boolean restoreSortKeys, boolean rebuildTree, Set selectValues)  : "
		             + restoreSortKeys + ", " + rebuildTree + ",  selectValues.size() " + logging.getSize(selectValues));
		
		/*
		if (selectValues == null)
		{
			if (rebuiltCounter > 0)
			{
				System.exit(0);
			}
			else
			{
				rebuiltCounter++;
			}
		}
		*/
			

		//groupPathActivatedByTree = null; //reset, changed by activateGroupByTree
		//try
		{
			java.util.List<String> valuesToSelect = null;
			if (selectValues != null)
				valuesToSelect = new ArrayList<String>(selectValues);
			
			//if (valuesToSelect == null)
			//		Set valuesToSelect = selectionPanel.getSelectedSet();
			java.util.List<? extends RowSorter.SortKey > saveSortKeys = selectionPanel.getSortKeys();

			logging.info(this, " setRebuiltPclistTableModel--- set model new, selected " + selectionPanel.getSelectedValues().size());
			//setSelectedClientsOnPanel(new String[]{});
			TableModel tm = buildPclistTableModel(rebuildTree);
			logging.info(this, "setRebuiltPclistTableModel --- got model selected " + selectionPanel.getSelectedValues().size());
			selectionPanel.removeListSelectionListener(this);
			//setSelectedClientsOnPanel(new String[]{});
			
			selectionPanel.setModel( tm );
			//selectionPanel.setSelectedValues(valuesToSelect);
			selectionPanel.addListSelectionListener(this);

			selectionPanel.initColumnNames();
			logging.debug(this, " --- model set  ");
			setSelectionPanelCols();

			if (restoreSortKeys) selectionPanel.setSortKeys (saveSortKeys);

			logging.info(this, "setRebuiltPclistTableModel set selected values in setRebuiltPclistTableModel() " + logging.getSize(valuesToSelect));
			logging.info(this, "setRebuiltPclistTableModel selected in selection panel" + logging.getSize(selectionPanel.getSelectedValues()));
			
			setSelectionPanelCols();

			setSelectedClientsCollectionOnPanel(valuesToSelect); //did lose the selection since last setting

			logging.info(this, "setRebuiltPclistTableModel selected in selection panel " + logging.getSize(selectionPanel.getSelectedValues()));
			
			reloadCounter++;
			logging.info(this, "setRebuiltPclistTableModel  reloadCounter " + reloadCounter);
							
			


		}
		//catch (Exception ex)
		//{
		//	logging.warning(this, " ---- setRebuiltPclistTableModel,  " + ex);
		//}
	}

	public void setFilterClientList(boolean b)
	{
		//logging.debug(this, " setFilterClientList " + filterClientList + ", new value: " + b);
		filterClientList = b;
		setRebuiltPclistTableModel();
		//setClientGroup(); destroyed selection when filtering

	}

	protected String getSelectedClientsString()
	{
		if (getSelectedClients() == null || getSelectedClients().length == 0 )
			return "";

		StringBuffer result = new StringBuffer();
		for ( int i  = 0; i < getSelectedClients().length - 1; i++)
		{
			result.append (getSelectedClients()[i]);
			//result.append ("; ");
			result.append (";\n");
		}

		result.append (getSelectedClients() [getSelectedClients().length - 1]);

		return result.toString();
	}

	protected Set getDepotsOfSelectedClients()
	{
		if (depotsOfSelectedClients == null)
		{
			depotsOfSelectedClients = new TreeSet();

			for ( int i  = 0; i < getSelectedClients().length ; i++)
			{
				//logging.debug(this, " selectedClient " + i + ": " + getSelectedClients()[i]);
				//logging.debug(this, "persist.getHostInfoCollections().getMapPcBelongsToDepot() " + persist.getHostInfoCollections().getMapPcBelongsToDepot());
				//logging.debug(this, "depotsOfSelectedClients" + depotsOfSelectedClients);
				if (persist.getHostInfoCollections().getMapPcBelongsToDepot().get(getSelectedClients()[i]) != null)
					depotsOfSelectedClients.add (persist.getHostInfoCollections().getMapPcBelongsToDepot().get(getSelectedClients()[i]));
			}
		}

		return depotsOfSelectedClients;
	}

	private void collectTheProductProperties( String productEdited )
	// we build
	// --
	// -- the map of the merged product properties from combining the properties of all selected clients

	{
		//logging.debug(this, "collectTheProductProperties for " + productEdited);
		mergedProductProperties = new HashMap();
		productProperties = new ArrayList(getSelectedClients().length);


		if ((getSelectedClients().length > 0) && (possibleActions.get(productEdited) != null))
		{
			// getSelectedClients()[0]

			//logging.debug(this, " =====  client " +  getSelectedClients()[0]);
			//logging.debug(this, " =====  productEdited " +  productEdited);
			Map<String, Object> productPropertiesFor1Client = persist.getProductproperties (getSelectedClients()[0],  productEdited );
			//logging.debug(this, " =====  " + productPropertiesFor1Client );
			if (productPropertiesFor1Client != null)
			{
				productProperties.add(productPropertiesFor1Client);

				Iterator iter = productPropertiesFor1Client.keySet().iterator();
				while (iter.hasNext())
				{
					// get next key - value - pair
					String key = (String) iter.next();
					//logging.debug(this, "ConfigedMain collectTheProductProperties   productPropertiesFor1Client.get(key) " +  productPropertiesFor1Client.get(key)
					//+ "  class: "  + productPropertiesFor1Client.get(key).getClass() );

					java.util.List value =  (java.util.List) productPropertiesFor1Client.get(key);

					// create a merger for it
					ListMerger merger = new ListMerger ( value );
					mergedProductProperties.put (key, merger);
				}

				// merge the other clients
				for (int i = 1; i < getSelectedClients().length; i++)
				{
					productPropertiesFor1Client = new HashMap();
					productPropertiesFor1Client = persist.getProductproperties (getSelectedClients()[i],  productEdited );
					//logging.debug(this, " =====  " + i + ":  client " +  getSelectedClients()[i]);
					productProperties.add(productPropertiesFor1Client);

					iter = productPropertiesFor1Client.keySet().iterator();

					while (iter.hasNext())
					{
						String key = (String) iter.next();
						java.util.List value = (java.util.List) productPropertiesFor1Client.get(key) ;

						//logging.debug(this, "------  key " + key  + " ::  value " + value);

						if ( mergedProductProperties.get(key) == null )
							// we need a new property. it is not common
						{
							ListMerger merger = new ListMerger ( value );
							//logging.debug(this, " new property, merger " + merger);
							merger.setHavingNoCommonValue();
							mergedProductProperties.put (key, merger);
						}
						else
						{
							Object merger = mergedProductProperties.get(key);
							//logging.debug(this, " reused merger " + merger + ", class " + merger.getClass().getName());
							//logging.debug(this, "+++  value to add " + value);
							//logging.debug(this, "+++  old value " + ((ListMerger)merger).getValue());
							ListMerger mergedValue = ((ListMerger) merger).merge (value);
							//logging.debug(this, "+++  mergedValue " + mergedValue);
							//logging.debug(this, "+++  mergedValue == ListMerger.NO_COMMON_VALUE : "
							//	+ (mergedValue == ListMerger.NO_COMMON_VALUE));

							// on merging we check if the value is the same as before
							mergedProductProperties.put (key, mergedValue);
						}
					}
				}
			}
		}


		//logging.debug(this, "collectTheProductProperties ready: " + mergedProductProperties);
	}

	protected void clearProductEditing()
	{
		mainFrame.panel_LocalbootProductsettings.clearEditing();
		mainFrame.panel_NetbootProductsettings.clearEditing();
	}
	
	protected void clearListEditors()
	{
		mainFrame.panel_LocalbootProductsettings.clearListEditors();
		mainFrame.panel_NetbootProductsettings.clearListEditors();
	}

	public void setProductEdited ( String productname )
	//called from ProductSettings
	{
		logging.debug(this, "setProductEdited " + productname);

		productEdited  = productname;


		if (clientProductpropertiesUpdateCollection != null)
		{
			try 	{updateCollection.remove (clientProductpropertiesUpdateCollection);}
			catch (Exception ex) 	{}
		}
		clientProductpropertiesUpdateCollection = null;

		if (clientProductpropertiesUpdateCollections.get(productEdited) == null)
			// have we got already a clientProductpropertiesUpdateCollection for this product?
			// if not, we  produce one
		{
			clientProductpropertiesUpdateCollection =
			    new ProductpropertiesUpdateCollection
			    (this, persist, getSelectedClients(), productEdited) ;

			clientProductpropertiesUpdateCollections.put (productEdited, clientProductpropertiesUpdateCollection);
			addToGlobalUpdateCollection (clientProductpropertiesUpdateCollection);


		}
		else
		{
			clientProductpropertiesUpdateCollection = (ProductpropertiesUpdateCollection)    clientProductpropertiesUpdateCollections.get(productEdited)  ;
		}


		collectTheProductProperties (productEdited);

		requirementsModel.setActualProduct(productEdited);

		logging.debug(this, " --- mergedProductProperties " + mergedProductProperties);
		//System.out.println ( " -- productEdited " +  productEdited + " ---  values map " +   persist.getProductPropertyValuesMap(productEdited));

		/*transcode product property descriptions
		Map productpropertyDescriptionsMap = persist.getProductPropertyDescriptionsMap(productEdited);
		if ( productpropertyDescriptionsMap != null  &&  !configed.get_serverCharset_equals_vm_charset()  )
		{
		   Iterator properties = productpropertyDescriptionsMap.keySet().iterator();
			 while ( properties.hasNext())
			{
				String property = (String) properties.next();
				String val = (String) productpropertyDescriptionsMap.get (property) ;
				if (val != null && !val.equals(""))
				{
				productpropertyDescriptionsMap.put ( property,  configed.encodeStringFromService( val ));
				}
			}
	}
		*/

		//logging.debug(this, "persist.getProductPropertyOptionsMap " + productEdited + ": " +  persist.getProductPropertyOptionsMap(productEdited));

		logging.debug(this, "setProductEdited " + productname + " client specific properties " +
		              persist.hasClientSpecificProperties(productname));


		mainFrame.panel_LocalbootProductsettings.initEditing(
		    productname,
		    persist.getProductTitle(productEdited),
		    persist.getProductInfo(productEdited),
		    persist.getProductHint(productEdited),
		    persist.getProductVersion(productEdited) + Globals.ProductPackageVersionSeparator.forDisplay() + persist.getProductPackageVersion(productEdited),
		    persist.getProductTimestamp(productEdited),

		    persist.getProductHavingClientSpecificProperties(),//persist.hasClientSpecificProperties(productname),
		    productProperties, // arraylist of the properties map of all selected clients
		    mergedProductProperties, // these properties merged to one map

		    //editmappanelx
		    persist.getProductPropertyOptionsMap(productEdited),

		    /*
		    //editmappanel
		    persist.getProductPropertyValuesMap(productEdited),
		    persist.getProductPropertyDescriptionsMap(productEdited),
		    persist.getProductPropertyDefaultsMap(productEdited),
		    */

		    clientProductpropertiesUpdateCollection);


		mainFrame.panel_NetbootProductsettings.initEditing(
		    productname,
		    persist.getProductTitle(productEdited),
		    persist.getProductInfo(productEdited),
		    persist.getProductHint(productEdited),
		    persist.getProductVersion(productEdited) + Globals.ProductPackageVersionSeparator.forDisplay() + persist.getProductPackageVersion(productEdited),
		    persist.getProductTimestamp(productEdited),

		    persist.getProductHavingClientSpecificProperties(),//persist.hasClientSpecificProperties(productname),
		    productProperties, // array of the properties map of all selected clients
		    mergedProductProperties, // these properties merged to one map

		    //editmappanelx
		    persist.getProductPropertyOptionsMap(productEdited),

		    /*
		    //editmappanel
		    persist.getProductPropertyValuesMap(productEdited),
		    persist.getProductPropertyDescriptionsMap(productEdited),
		    persist.getProductPropertyDefaultsMap(productEdited),
		    */

		    clientProductpropertiesUpdateCollection);

		//waitCursor.stop();

	}

	public int getViewIndex ()
	{
		return viewIndex;
	}




	/*
	private void rebuildSelectedTreeSubjects()
{
		selectedTreePaths =  treeClients.getSelectionPaths();
		logging.debug(this, "rebuildSelectedTreeSubjects: "  + selectedTreePaths);
		
		TreeSet <String> selClients = new TreeSet<String>();
		
		if (selectedTreePaths != null)
		{
			for (int i = 0; i <selectedTreePaths .length; i ++)
			{
				DefaultMutableTreeNode selNode 
				= (DefaultMutableTreeNode)  selectedTreePaths[i].getLastPathComponent();
			
				String nodeinfo = (String) selNode.getUserObject();
				//logging.debug(this, "nodeinfo " + nodeinfo);
				selClients.add(nodeinfo);
			}
		}
		logging.debug(this, "rebuildSelectedTreeSubjects: "  + logging.getStrings(selectedTreePaths) );
		setSelectedClientsOnPanel(selClients);
		if (selClients.size()>0)
		{
			Iterator iter = selClients.iterator();
			String client = null;
			while (iter.hasNext())
				client = (String) iter.next();
			selectionPanel.moveToValue(client, 0, true);
			//selectionPanel.fireListSelectionChanged(treeClients);
		}
}



	private void resetSelectedTreeSubjects()
{
		logging.debug(this, "resetSelectedTreeSubjects: "  + logging.getStrings(selectedTreePaths) );
		treeClients.setSelectionPaths(selectedTreePaths);
		//treeClients.scrollPathToVisible(clickPath);
}

	*/


	public boolean treeClients_mouseAction(boolean clicked, MouseEvent mouseEvent)
	{
		logging.debug(this,"treeClients_mouseAction");
		
		boolean result = true;

		if (!treeClients.isEnabled())
			return result;

		if (!(mouseEvent.getButton() == MouseEvent.BUTTON1))
			return result;

		int mouseRow = treeClients.getRowForLocation(mouseEvent.getX(), mouseEvent.getY());
		TreePath mousePath = treeClients.getPathForLocation(mouseEvent.getX(), mouseEvent.getY());

		if(mouseRow == -1)
			// clicked on area for expanding and contracting the node
		{
		}

		else
		{
			//logging.debug(this, "treeClients: single click on tree row " + mouseRow + " getting path " + mousePath);

			DefaultMutableTreeNode mouseNode
			= (DefaultMutableTreeNode)  mousePath.getLastPathComponent();


			//logging.debug(this, " selected paths " + logging.getStrings(treeClients.getSelectionPaths()));
			//logging.debug(this, " get paths between 5 and mouseRow " + logging.getStrings(treeClients.getPathBetweenRows(5, mouseRow)));

			if (!mouseNode.getAllowsChildren())
			{

				// we manage the active nodes

				/*
				if (!clicked) // just entered
			{
					
					//extend activation
					
				*/

				if (clicked)
				{
					if ( (activePaths.size() == 1) &&  ((DefaultMutableTreeNode)  activePaths.get(0).getLastPathComponent()).getAllowsChildren() )
						// on client activation a group activation is ended
					{
						clearTree();
					}

					else
					{
						if ((mouseEvent.getModifiersEx() & MouseEvent.SHIFT_DOWN_MASK) == MouseEvent.SHIFT_DOWN_MASK)
						{
							//logging.debug(this, " shift down, we extend to sequence

							clearTree();

							TreePath[] selTreePaths = treeClients.getSelectionPaths();

							for (int i = 0; i < selTreePaths.length; i++)
							{
								DefaultMutableTreeNode selNode = (DefaultMutableTreeNode)  selTreePaths[i].getLastPathComponent();
								activeTreeNodes.put((String) selNode.getUserObject(), selTreePaths[i] );
								activePaths.add(selTreePaths[i] );
								treeClients.collectParentIDsFrom(selNode);
							}
						}

						else if ((mouseEvent.getModifiersEx() & MouseEvent.CTRL_DOWN_MASK) == MouseEvent.CTRL_DOWN_MASK)
						{
							//logging.debug(this, " ctrl down, we add just the new one ");
						}
						else
						{
							//logging.debug(this, " one client is activated ")
							clearTree();
						}
					}

					activateClientByTree((String) mouseNode.getUserObject(), mousePath);
	
					setRebuiltPclistTableModel(true, false, clientsFilteredByTree);
					
					//setClient((String) mouseNode.getUserObject());


					//restore keys, do not rebuild tree, select clientsFilteredByTree

					//treeClients.setSelectionPaths(activePaths.toArray(new TreePath[]{}));

				}


			}
			else
			{
				if (clicked)
				{
					activateGroupByTree(false, mouseNode, mousePath);
					//activateGroupByTreeWithModelRebuild(mouseNode, mousePath);  tree construction fails!!
				}
			}


		}


		if(mouseEvent.getClickCount() == 2) {
			logging.debug(this, "treeClients: double click on tree row " + mouseRow + " getting path " + mousePath);

		}
		
		return result;

	}

	protected void initTree()
	{
		logging.debug(this, "initTree");
		activeTreeNodes = new HashMap<String, TreePath>();
		activePaths = new ArrayList<TreePath>();

		//de.uib.configed.tree.ClientTree.initializeStatics();
		treeClients = new de.uib.configed.tree.ClientTree(this);

	}


	private void activateClientByTree(String nodeObject, TreePath pathToNode)
	{
		logging.info(this, "activateClientByTree, nodeObject: " + nodeObject + ", pathToNode: " + pathToNode);
		activeTreeNodes.put( nodeObject, pathToNode );
		activePaths.add( pathToNode );

		treeClients.collectParentIDsFrom((DefaultMutableTreeNode) pathToNode.getLastPathComponent());

		treeClients.repaint();

		logging.debug(this, "activateClientByTree, activeTreeNodes " + activeTreeNodes);
		clientsFilteredByTree = activeTreeNodes.keySet();
		logging.debug(this, "activateClientByTree, clientsFilteredByTree " + clientsFilteredByTree);

		//since we select based on the tree view we disable the filter
		if (filterClientList)
			mainFrame.toggleClientFilterAction();

		//(new ArrayList(clientsFilteredByTree)); does not select

		//setSelectedClients(new ArrayList(clientsFilteredByTree));
		//setSelectedClientsOnPanel(clientsFilteredByTree); leads to problems

	}

	private void clearTree()
	{
		activeTreeNodes.clear();
		activePaths.clear();
		treeClients.initActiveParents();
	}

	private void setGroupByTree(DefaultMutableTreeNode node, TreePath pathToNode)
	{
		logging.debug(this, "setGroupByTree node, pathToNode " + node + ", " + pathToNode);
		clearTree();
		activeTreeNodes.put((String) node.getUserObject(), pathToNode);
		activePaths.add(pathToNode);

		clientsFilteredByTree = treeClients.collectLeafs(node);
		treeClients.repaint();

	}
	
	/* 
	private void activateGroupByTreeWithModelRebuild(DefaultMutableTreeNode node, TreePath pathToNode)
	{
		logging.info(this, "activateGroupByTreeWithModelRebuild, node: " + node + ", pathToNode : " + pathToNode);

		setGroupByTree(node, pathToNode);
		
		logging.info(this, "activateGroupByTreeWithModelRebuild, we have selected Set : " + selectionPanel.getSelectedSet());
		
		//setRebuiltPclistTableModel(true, false, null);
		setRebuiltPclistTableModel(true, false); //we set again selected clients
			
		activateGroupByTree(node, pathToNode);
		//activatedGroupModel.setActive(false); //revoke setting
	}
	*/
		

	private void activateGroupByTree(boolean preferringOldSelection, DefaultMutableTreeNode node, TreePath pathToNode)
	{
		logging.info(this, "activateGroupByTree, node: " + node + ", pathToNode : " + pathToNode);


		setGroupByTree(node, pathToNode);

		if  (
			preferringOldSelection //intended for reload, we cancel activating group 
			&&
			selectionPanel.getSelectedSet() != null 
			&& 
			selectionPanel.getSelectedSet().size() > 0 )
		{
			return;
		}
		//setSelectedClients(null);
		setRebuiltPclistTableModel(true, false, null);
		//with this, a selected client remains selected (but in bottom line, the group seems activated, not the client)
		//setRebuiltPclistTableModel(true, false);
		groupPathActivatedByTree = pathToNode;

		//mainFrame.setGroupName("" + node ); //+ " (" + pathToNode + ")");

		try{
			activatedGroupModel.setNode("" + node, node, pathToNode);
			activatedGroupModel.setDescription(treeClients.getGroups().get("" + node).get("description"));
			activatedGroupModel.setAssociatedClients(clientsFilteredByTree);
			activatedGroupModel.setActive(true);
		}
		catch(Exception ex)
		{
			logging.info(this, "activateGroupByTree, node: " + node + " exception : " + ex);
		}

	}
	
	
	
	

	public TreePath getGroupPathActivatedByTree()
	{
		return groupPathActivatedByTree;
	}

	public ActivatedGroupModel getActivatedGroupModel()
	{
		return activatedGroupModel;
	}


	public HashSet<String> getActiveParents()
	{
		if (treeClients == null)
			return new HashSet<String>();

		return treeClients.getActiveParents();
	}

	public boolean addGroup(de.uib.utilities.datastructure.StringValuedRelationElement newGroup)
	{
		return persist.addGroup(newGroup);
	}


	public boolean updateGroup(String groupId, Map<String, String> groupInfo)
	{
		return persist.updateGroup(groupId, groupInfo);
	}

	public boolean deleteGroup(String groupId)
	{
		return persist.deleteGroup(groupId);
	}

	public boolean removeHostGroupElements(java.util.List<Object2GroupEntry> entries)
	{
		return persist.removeHostGroupElements(entries);
	}
	
	public boolean removeObject2Group(String objectId, String groupId)
	{
		return persist.removeObject2Group(objectId, groupId);
	}

	public  boolean addObject2Group(String objectId, String groupId)
	{
		return persist.addObject2Group(objectId, groupId);
	}

	public void treeClients_selectedValueChanged(TreeSelectionEvent e)
	{
		logging.debug(this, "treeClients_selectedValueChanged");
	}

	public boolean setProductGroup(String groupId, String description, Set<String> productSet)
	{
		return persist.setProductGroup(groupId, description, productSet);
	}

	private void depotsList_valueChanged()
	{
		if (firstDepotListChange)
		{
			firstDepotListChange = false;
			return;
		}

		logging.info(this, "----    depotsList selection changed");

		changeDepotSelection();


		//Vector<String> saveSelectedDepotsV =  selectedDepotsV;


		/*
		if (getSelectedDepots().length == 0)
			// if nothing is selected set config server as depot
		{
			//logging.debug(this, "depotsList selection set to " + myServer);	
			//depotsList.setSelectedValue (myServer, true);
	}
		*/

		if (depotsList.getModel().getSize() > 1)
		{
			if (mainFrame != null)
				mainFrame.setChangedDepotSelectionActive(true);

			//when running after the first run, we deactivate buttons
		}

		depotsList_selectionChanged = true;
		depotsOfSelectedClients = null;

		selectedDepots = new String[depotsList.getSelectedValues().length];
		selectedDepotsV = new Vector<String>();


		for (int i = 0; i < depotsList.getSelectedValues().length; i++)
		{
			selectedDepots[i] = (String) depotsList.getSelectedValues()[i];
			selectedDepotsV.add(selectedDepots[i]);
		}

		logging.debug(this, "--------------------  selectedDepotsV         " +  selectedDepotsV);

		//configed.savedStates.setProperty("selectedDepots", Arrays.toString(selectedDepots));
		configed.savedStates.saveDepotSelection.serialize(selectedDepots);




		/*
		logging.info(this, "--------------------saveSelectedDepotsV  " + saveSelectedDepotsV);
		boolean changedSelectedDepotsV = 
			!selectedDepotsV.equals(saveSelectedDepotsV);
		logging.info(this, " selectedDepotsV not changed  " +  !changedSelectedDepotsV);

		if (!changedSelectedDepotsV)
			return;
		*/


		/* test of check of synchronicity
		TreeSet depotSet = new TreeSet();
		for (int i = 0; i < getSelectedDepots().length; i++)
		{ depotSet.add(getSelectedDepots()[i] ); }
		persist.areDepotsSynchronous(depotSet);
		*/



		/*
		SwingUtilities.invokeLater(new Runnable(){

			                           public void run()
			                           {
				                           initialTreeActivation();
			                           }
		                           }
		                          );;
		 */


		 
		try
		{
			logging.info(this, " depotsList_valueChanged, omitted initialTreeActivation");
			
			if (selectionPanel != null)
				initialTreeActivation();
			
		}
		catch(Exception ex)
		{
			logging.debug(this, "tree cannot be activated ");
			logging.logTrace(ex);
		}



	}

	private boolean checkSynchronous(java.util.Set depots)
	{
		//if (depots == null)
		//logging.debug(this, " ------------ depots null");

		if (depots.size() > 1 && !persist.areDepotsSynchronous(depots))
		{
			JOptionPane.showMessageDialog(	mainFrame,
			                               configed.getResourceValue("ConfigedMain.notSynchronous.text"), //"not synchronous",
			                               configed.getResourceValue("ConfigedMain.notSynchronous.title"),
			                               JOptionPane.OK_OPTION);

			return false;
		}

		return true;
	}



	protected boolean setDepotRepresentative()
	{
		logging.debug(this, "setDepotRepresentative");



		boolean result = true;

		if (getSelectedClients() == null || getSelectedClients().length == 0)
		{
			if (depotRepresentative == null)
				depotRepresentative = myServer;
		}
		else
		{
			Set depots = getDepotsOfSelectedClients();
			logging.info(this, "depots of selected clients:" + depots);

			String oldRepresentative = depotRepresentative;

			logging.debug(this, "setDepotRepresentative(), old representative: "  + depotRepresentative + " should be " );
			//+ persist.getDepot() );

			if (!checkSynchronous(depots))
				result = false;

			else
			{
				logging.debug(this, "setDepotRepresentative  start  "
				              + " up to now " + oldRepresentative + " old" + depotRepresentative + " equal "
				              + oldRepresentative.equals(depotRepresentative));


				depotRepresentative = null;

				logging.info(this, "setDepotRepresentative depotsOfSelectedClients " +getDepotsOfSelectedClients());

				Iterator depotsIterator = getDepotsOfSelectedClients().iterator();

				if (!depotsIterator.hasNext())
				{
					depotRepresentative = myServer;

					logging.debug(this, "setDepotRepresentative  without next change depotRepresentative "
					              + " up to now " + oldRepresentative + " new " + depotRepresentative + " equal "
					              + oldRepresentative.equals(depotRepresentative));


				}
				else
				{

					depotRepresentative = (String) depotsIterator.next();

					while (!depotRepresentative.equals(myServer) && depotsIterator.hasNext())
					{
						String depot = (String) depotsIterator.next();
						if ( depot.equals(myServer) )
						{
							depotRepresentative = myServer;
						}
					}
				}

				logging.debug (this, " --------------- depotRepresentative "  + depotRepresentative);

				logging.info(this, "setDepotRepresentative  change depotRepresentative "
				             + " up to now " + oldRepresentative + " new " + depotRepresentative + " equal "
				             + oldRepresentative.equals(depotRepresentative));


				if (!oldRepresentative.equals(depotRepresentative))
				{
					logging.info(this, " new depotRepresentative "  + depotRepresentative);
					persist.setDepot(depotRepresentative);
					//persist.productDataRequestRefresh(); //we should already have downloaded everything
					persist.depotChange();
					//persist.retrieveProductGlobalInfos();
					//persist.retrieveProducts();
					//persist.retrieveProductPropertyDefinitions();
					//persist.getProductGlobalInfos(depotRepresentative);
					//persist.productinfosRequestRefresh();
					//persist.productpropertyDefinitionsRequestRefresh();
					//persist.productdependenciesRequestRefresh();
					//persist.netbootProductNamesRequestRefresh();
					//persist.localbootProductNamesRequestRefresh();
				}

			}

		}

		return result;
	}


	protected ArrayList<String> getLocalbootProductDisplayFieldsList()
	{
		ArrayList<String> result = new ArrayList<String>();
		Iterator iter =displayFieldsLocalbootProducts .keySet().iterator();
		while (iter.hasNext())
		{
			String key = (String) iter.next();
			if (displayFieldsLocalbootProducts.get(key))
				result.add(key);
		}

		return result;
	}


	protected ArrayList<String> getNetbootProductDisplayFieldsList()
	{
		ArrayList<String> result = new ArrayList<String>();
		Iterator iter =displayFieldsNetbootProducts .keySet().iterator();
		while (iter.hasNext())
		{
			String key = (String) iter.next();
			if (displayFieldsNetbootProducts.get(key))
				result.add(key);
		}

		return result;
	}

	public LinkedHashMap<String, Boolean> getDisplayFieldsNetbootProducts()
	{
		return displayFieldsNetbootProducts;
	}

	public LinkedHashMap<String, Boolean> getDisplayFieldsLocalbootProducts()
	{
		return displayFieldsLocalbootProducts;
	}

	private boolean checkOneClientSelected()
	//enables and disables tabs depending if they make sense in other cases
	{
		logging.debug(this, "checkOneClientSelected() selectedClients " + logging.getStrings(getSelectedClients()));
		boolean result = true;


		if (getSelectedClients().length != 1)
		{
			/*
			JOptionPane.showMessageDialog(mainFrame, 
							configed.getResourceValue("ConfigedMain.pleaseSelectOneClient.text"), 
							configed.getResourceValue("ConfigedMain.pleaseSelectOneClient.title"),
							JOptionPane.OK_OPTION);
			*/
			result = false;
		}



		return result;
	}

	private void freeMemoryFromSearchData()
	{
		if (clientSelectionDialog != null)
		{
			//clientSelectionDialog.setReloadRequested();      
		}
	}

	protected boolean setLocalbootProductsPage()
	{
		logging.debug(this, "setLocalbootProductsPage() with filter " + configed.savedStates.saveLocalbootproductFilter.deserialize());


		Set<String> savedFilter
		=  (Set<String>) configed.savedStates.saveLocalbootproductFilter.deserialize();
		//, localboot display fields list " + getLocalbootProductDisplayFieldsList()

		//	+ " reload : " +  (localbootStatesAndActions == null) );


		if (!setDepotRepresentative())
			return false;

		//freeMemoryFromSearchData();


		//WaitCursor waitCursor = null;

		try
		{
			//waitCursor = new WaitCursor( mainFrame.retrieveBasePane(), mainFrame.getCursor() );

			clearProductEditing();
			//mainFrame.saveConfigurationsSetEnabled(true);

			//requestRefreshDataForClientSelection();

			//logging.debug(this, "setLocalbootProductsPage localbootStatesAndActions == null,

			if (localbootStatesAndActions == null
			        || istmForSelectedClientsLocalboot == null
			        || localbootStatesAndActionsUPDATE
			   )
				//we reload since at the moment we do not track changes if anyDataChanged
			{
				localbootStatesAndActionsUPDATE = false;
				//System.gc();
				//waitCursor = new WaitCursor( mainFrame.retrieveBasePane(), mainFrame.getCursor() );
				localbootStatesAndActions = persist.getMapOfLocalbootProductStatesAndActions(
				                                getSelectedClients());
				//waitCursor.stop();
				istmForSelectedClientsLocalboot= null;
			}


			clientProductpropertiesUpdateCollections = new HashMap();
			mainFrame.panel_LocalbootProductsettings.initAllProperties();

			logging.debug(this, "setLocalbootProductsPage,  depotRepresentative:" + depotRepresentative);
			possibleActions = persist.getPossibleActions(depotRepresentative);


			//we retrieve the properties for all clients and products
			//persist.retrieveProductPropertyDefinitions();

			//it is necessary to do this before resetting selection below (*) since there a listener is triggered
			//which loads the productProperties for each client separately

			// persist.productpropertiesRequestRefresh();/ /called if anyDataChanged
			persist.retrieveProductproperties(selectionPanel.getSelectedValues());


			Set<String> oldProductSelection = mainFrame.panel_LocalbootProductsettings.getSelectedIDs();
			logging.info(this, "setLocalbootProductsPage: oldProductSelection " + oldProductSelection);

			logging.debug(this, "setLocalbootProductsPage: collectChangedLocalbootStates " + collectChangedLocalbootStates);

			if (istmForSelectedClientsLocalboot == null)
			{
				//we rebuild only if we reloaded
				istmForSelectedClientsLocalboot = new InstallationStateTableModelFiltered (
				                                      getSelectedClients(),
				                                      this,
				                                      collectChangedLocalbootStates,
				                                      persist.getAllLocalbootProductNames(depotRepresentative),
				                                      localbootStatesAndActions,
				                                      possibleActions, //persist.getPossibleActions(depotRepresentative),
				                                      persist.getProductGlobalInfos(depotRepresentative),
				                                      //new ArrayList(productDisplayFields.keySet())
				                                      getLocalbootProductDisplayFieldsList(),
				                                      configed.savedStates.saveLocalbootproductFilter

				                                  );
			}

			
			try
			{
				mainFrame.panel_LocalbootProductsettings.setTableModel( (InstallationStateTableModelFiltered) istmForSelectedClientsLocalboot);
				mainFrame.panel_LocalbootProductsettings.setGroupsData(productGroups, productGroupMembers);

				logging.info(this, "resetFilter " +  configed.savedStates.saveLocalbootproductFilter.deserialize());
				//( (InstallationStateTableModelFiltered) istmForSelectedClientsLocalboot ).resetFilter();
				((PanelGroupedProductSettings) mainFrame.panel_LocalbootProductsettings).reduceToSet(savedFilter);

				logging.info(this, "setLocalbootProductsPage oldProductSelection -----------  " + oldProductSelection);
				mainFrame.panel_LocalbootProductsettings.setSelection(oldProductSelection); // (*)
			
				mainFrame.panel_LocalbootProductsettings.setSearchFields(
				    de.uib.configed.guidata.InstallationStateTableModel.localizeColumns(
				        getLocalbootProductDisplayFieldsList()
				    )
				);
				




			}
			catch (Exception ex)
			{
				logging.warning(" setLocalbootInstallationStateTableModel, exception Occurred" + ex);
				logging.logTrace(ex);
			}
			

			//waitCursor.stop();

			return true;
		}
		catch(Exception ex)
		{
			//if (waitCursor != null) waitCursor.stop();
			logging.error("Error in setLocalbootProductsPage: " + ex, ex);
			return false;
		}

	}

	protected boolean setNetbootProductsPage()
	{
		if (!setDepotRepresentative())
			return false;

		freeMemoryFromSearchData();

		//WaitCursor waitCursor = null;

		try
		{
			//waitCursor = new WaitCursor( mainFrame.retrieveBasePane(), mainFrame.getCursor() );

			clearProductEditing();
			//mainFrame.saveConfigurationsSetEnabled(true);

			//requestRefreshDataForClientSelection();

			long startmillis = System.currentTimeMillis();
			logging.debug(this, "setLocalbootProductsPage, # getMapOfNetbootProductStatesAndActions(selectedClients)  start " + startmillis);


			if (netbootStatesAndActions == null)
				//we reload since at the moment we do not track changes if anyDataChanged
			{
				//waitCursor = new WaitCursor( mainFrame.retrieveBasePane(), mainFrame.getCursor() );
				netbootStatesAndActions = persist.getMapOfNetbootProductStatesAndActions(getSelectedClients());
				//waitCursor.stop();
				istmForSelectedClientsNetboot = null;
			}
			long endmillis = System.currentTimeMillis();
			logging.debug(this, "setNetbootProductsPage, # getMapOfNetbootProductStatesAndActions(selectedClients)  end " +   endmillis +  " diff " + (endmillis - startmillis) );



			clientProductpropertiesUpdateCollections = new HashMap();
			mainFrame.panel_LocalbootProductsettings.initAllProperties();

			possibleActions = persist.getPossibleActions(depotRepresentative);

			//logging.debug(this, " +++ netbootproductStatesAndActions " + persist.getMapOfNetbootProductStatesAndActions(getSelectedClients()));
			Set<String> oldProductSelection = mainFrame.panel_NetbootProductsettings.getSelectedIDs();

			//we retrieve the properties for all clients and products

			//persist.productpropertiesRequestRefresh();  is called if anyDataChanged
			persist.retrieveProductproperties(selectionPanel.getSelectedValues());



			if (istmForSelectedClientsNetboot == null)
			{
				//we rebuild only if we reloaded
				istmForSelectedClientsNetboot = new InstallationStateTableModel(//Netbootproducts(
				                                    getSelectedClients(),
				                                    this,
				                                    collectChangedNetbootStates,
				                                    persist.getAllNetbootProductNames (depotRepresentative ),
				                                    netbootStatesAndActions,
				                                    possibleActions, //persist.getPossibleActions(depotRepresentative),
				                                    persist.getProductGlobalInfos(depotRepresentative),
				                                    getNetbootProductDisplayFieldsList()
				                                );
			}

			try
			{
				mainFrame.panel_NetbootProductsettings.setTableModel  (istmForSelectedClientsNetboot);
				mainFrame.panel_NetbootProductsettings.setSelection(oldProductSelection); // (*)
			}
			catch (Exception ex)
			{
				logging.error(" setNetbootInstallationStateTableModel,  exception Occurred", ex);
			}


			//waitCursor.stop();

			return true;
		}
		catch(Exception ex)
		{
			//if (waitCursor != null) waitCursor.stop();
			logging.error("Error in setNetbootProductsPage: " + ex, ex);
			return false;
		}


	}

	private Map<String, Object> mergeMaps(ArrayList<Map<String, Object>> collection)
	{
		HashMap<String, Object> mergedMap = new HashMap<String, Object>();
		if (collection == null || collection.size()  == 0)
			return mergedMap;

		Map<String, Object> mergeIn = collection.get(0);

		for ( String key : mergeIn.keySet() )
		{
			java.util.List value = (java.util.List) mergeIn.get(key);
			ListMerger merger = new ListMerger(value);
			mergedMap.put(key, merger);
		}

		//merge the other maps
		for (int i = 1; i < collection.size(); i++)
		{
			mergeIn = collection.get(i);

			for ( String key : mergeIn.keySet() )
			{
				java.util.List value = (java.util.List) mergeIn.get(key);

				if (mergedMap.get(key) == null)
					//new property
				{
					ListMerger merger = new ListMerger ( value );
					merger.setHavingNoCommonValue();
					mergedMap.put(key, merger);
				}

				else
				{
					ListMerger merger = (ListMerger) mergedMap.get(key);
					ListMerger mergedValue = merger.merge(value);
					mergedMap.put(key, mergedValue);
				}
			}
		}

		return mergedMap;
	}

	protected boolean setProductPropertiesPage()
	{
		logging.debug(this, "setProductPropertiesPage");

		if (editingTarget == EditingTarget.DEPOTS)
		{
			int saveSelectedRow = mainFrame.panel_ProductProperties.paneProducts.getSelectedRow();
			mainFrame.panel_ProductProperties.paneProducts.reset();

			/*
			if (
				mainFrame.panel_ProductProperties.paneProducts.getTableModel().getRowCount()>0
				&& mainFrame.panel_ProductProperties.paneProducts.getSelectedRowCount() == 0
				)
		{
				mainFrame.panel_ProductProperties.paneProducts.setSelectedRow(0);
		}
			*/


			/* missing product
			if (depotProductpropertiesUpdateCollection != null)
		{
				try 	{updateCollection.remove (depotProductpropertiesUpdateCollection);}
				catch (Exception ex) 	{}
		}
			depotProductpropertiesUpdateCollection = new ProductpropertiesUpdateCollection(this, persist, objectIds, product);
			updateCollection.add(depotProductpropertiesUpdateCollection);

			*/


			if (
			    mainFrame.panel_ProductProperties.paneProducts.getTableModel().getRowCount()>0
			)
			{
				if (saveSelectedRow == -1 || mainFrame.panel_ProductProperties.paneProducts.getTableModel().getRowCount() <= saveSelectedRow)
				{
					mainFrame.panel_ProductProperties.paneProducts.setSelectedRow(0);
				}
				else
				{
					mainFrame.panel_ProductProperties.paneProducts.setSelectedRow(saveSelectedRow);
				}
			}




			//mainFrame.panel_ProductProperties.refresh();
			return true;
		}
		else
			return false;
	}

	protected boolean setHostPropertiesPage()
	{
		logging.debug(this, "setHostPropertiesPage");
		//WaitCursor waitCursor = null;

		try
		{
			if (editingTarget == EditingTarget.DEPOTS)
			{
				//waitCursor = new WaitCursor( mainFrame.retrieveBasePane(), mainFrame.getCursor() );


				//Map<String, java.util.List<Object>> defaultValuesMap = persist.getConfigDefaultValues();

			
				LinkedHashMap<String, Map<String, Object>> depotPropertiesForPermittedDepots = persist.getDepotPropertiesForPermittedDepots();

				//waitCursor.stop();


				if (hostUpdateCollection != null)
				{
					try 	{updateCollection.remove (hostUpdateCollection);}
					catch (Exception ex) 	{}
				}
				hostUpdateCollection = new HostUpdateCollection(persist);
				addToGlobalUpdateCollection(hostUpdateCollection);
				
				

				mainFrame.panel_HostProperties.initMultipleHostsEditing(
				    configed.getResourceValue("PanelHostProperties.SelectHost"),
				    new DefaultComboBoxModel(new Vector(depotPropertiesForPermittedDepots.keySet())),
				    depotPropertiesForPermittedDepots,
				    hostUpdateCollection	,
				    persist.KEYS_OF_HOST_PROPERTIES_NOT_TO_EDIT

				);

			}

			return true;
		}
		catch(Exception ex)
		{
			//if (waitCursor != null) waitCursor.stop();
			logging.error("Error in setHostPropertiesPage: " + ex, ex);
			return false;
		}

	}
	
	
	private  void removeKeysStartingWith(Map<String, ? extends Object> m , Set<String> keystartersStrNotWanted)
	{
		Set<String> keysForDeleting  = new HashSet<String>();
		
		for (String start :  keystartersStrNotWanted)
		{
			for (String key : m.keySet())
			{
				if (key.startsWith( start ))
					keysForDeleting.add(key);
			}
		}
		
		for (String key : keysForDeleting)
		{
			m.remove( key );
		}
	}
	
		


	protected boolean setNetworkconfigurationPage()
	{
		logging.info(this, "setNetworkconfigurationPage ");// + Arrays.asList(saveSelectedClients));
		logging.info(this, "setNetworkconfigurationPage  getSelectedClients() " + Arrays.toString(getSelectedClients()));
		//WaitCursor waitCursor = null;

		try
		{
			if (editingTarget == EditingTarget.SERVER)
				objectIds = new String[] {myServer};
			else
				objectIds = getSelectedClients();
				
			/*
			if (getSelectedClients().length == 0)
				objectIds = new String[] {myServer};
			else
				objectIds = getSelectedClients();
			*/

			if (additionalconfigurationUpdateCollection != null)
			{
				try 	{updateCollection.remove (additionalconfigurationUpdateCollection);}
				catch (Exception ex) 	{}
			}
			additionalconfigurationUpdateCollection = new AdditionalconfigurationUpdateCollection(persist, objectIds);
			addToGlobalUpdateCollection(additionalconfigurationUpdateCollection);


			if (editingTarget == EditingTarget.SERVER)
			{
				//waitCursor = new WaitCursor( mainFrame.retrieveBasePane(), mainFrame.getCursor() );

				ArrayList additionalConfigs = new ArrayList(1);

				Map<String, java.util.List<Object>> defaultValuesMap = persist.getConfigDefaultValues();
				//logging.debug(this, "setNetworkconfigurationPage: defaultValuesMap "  + defaultValuesMap);
				additionalConfigs.add(defaultValuesMap);
				//additionalConfigs.add (persist.getConfig(myServer));
				//persist.getAdditionalConfiguration(myServer));

				//additionalconfigurationUpdateCollection.setDetermineConfigOptions(true);
				additionalconfigurationUpdateCollection.setMasterConfig(true);

				mainFrame.panel_HostConfig.initEditing(
				    //configed.getResourceValue("MainFrame.jLabel_AdditionalConfig")  + ":"
				    "  " + myServer + " (configuration server)",
				    (Map) additionalConfigs.get(0),
				    persist.getConfigOptions(),
				    additionalConfigs,
				    additionalconfigurationUpdateCollection,
				    true,
				    //editableOptions
				    persist.PROPERTYCLASSES_SERVER
				    );

				//waitCursor.stop();
			}

			else
			{

				//waitCursor = new WaitCursor( mainFrame.retrieveBasePane(), mainFrame.getCursor() );

				ArrayList<Map<String, Object>> additionalConfigs = new ArrayList<Map<String, Object>>(getSelectedClients().length);

				//persist.hostConfigsCheck(getSelectedClients());

				if (hostConfigs == null)
				{
					hostConfigs = new HashMap<String, Map<String, Object>>(); //serves as marker


					for (String client : getSelectedClients())
					{
						hostConfigs.put(client,  persist.getConfigs().get(client));
					}
				}

				logging.info(this, "additionalConfig fetch for " + Arrays.toString(getSelectedClients()));

				for (int i = 0; i < getSelectedClients().length; i++)
				{
					additionalConfigs.add (persist.getConfig(getSelectedClients()[i]));
					//logging.info(this, "additionalConfig " +  persist.getConfig(getSelectedClients()[i]));
					//with server defaults
				}

				Map<String, Object> mergedVisualMap = mergeMaps(additionalConfigs);
				
				Map<String,  de.uib.utilities.table.ListCellOptions> configOptions = persist.getConfigOptions();
				
				//logging.info(this, "setNetworkconfigurationPage configOptions keys " + configOptions.keySet() );
				
				//removeKeysStartingWith(configOptions, "user");
				//removeKeysStartingWith(mergedVisualMap, "user");
				//removeKeysStartingWith(mergedVisualMap, "configed");
				removeKeysStartingWith(mergedVisualMap, persist.CONFIG_KEYSTARTERS_NOT_FOR_CLIENTS);
				
	
				//logging.info(this, "setNetworkconfigurationPage configOptions keys " + configOptions.keySet() );

				mainFrame.panel_HostConfig.initEditing(
				    "  " + getSelectedClientsString(), //"",
				    mergedVisualMap,
				    configOptions,
				    additionalConfigs,
				    additionalconfigurationUpdateCollection,
				    false, //editableOptions
				    persist.PROPERTYCLASSES_CLIENT,
				    de.uib.utilities.datapanel.EditMapPanelX.PropertyHandlerType.RESETTING_VALUE_TO_DEFAULT
				    );

				//waitCursor.stop();


				/*
				if (!checkOneClientSelected())
			{
					viewIndex = viewClients;
			}
				else
			{
					waitCursor = new WaitCursor( mainFrame.retrieveBasePane(), mainFrame.getCursor() );
					
					
					ArrayList additionalConfigs = new ArrayList (1);
					additionalConfigs.add (persist.getConfig(selectedClients[0]));//persist.getAdditionalConfiguration(selectedClients[0]));
					
					mainFrame.initNetworkconfigEditing (
						selectedClients[0],
						(Map) additionalConfigs.get(0),
						persist.getConfigOptions(),
						additionalConfigs,
						additionalconfigurationUpdateCollection);
						
					waitCursor.stop();
			}
				*/

			}

			//logging.debug(this, " ------- we have an update collection of size  " + updateCollection.size() );

			return true;
		}
		catch(Exception ex)
		{
			//if (waitCursor != null) waitCursor.stop();
			logging.error("Error in setNetworkConfigurationPage: " + ex, ex);
			return false;
		}

	}

	protected void checkHwInfo()
	{
		if (hwInfoClientmap == null)
			hwInfoClientmap = new HashMap<String, Object>();
	}

	public void clearHwInfo()
	{
		checkHwInfo();
		hwInfoClientmap.clear();
	}


	protected boolean setHardwareInfoPage()
	{
		logging.debug(this, "setHardwareInfoPage for " + pcName);

		//WaitCursor waitCursor = null;

		try
		{
			//waitCursor = new WaitCursor(mainFrame.retrieveBasePane(),  mainFrame.getCursor() );

			if (pcName == null || !checkOneClientSelected() )
				mainFrame.setHardwareInfo ();
			else
			{
				checkHwInfo();
				Object hwInfo = hwInfoClientmap.get(pcName);
				if (hwInfo == null)
				{
					hwInfo = persist.getHardwareInfo ( pcName,  true );
					hwInfoClientmap.put(pcName, hwInfo);
				}
				mainFrame.setHardwareInfo (hwInfo, pcName);
			}

			//waitCursor.stop();

			return true;
		}
		catch(Exception ex)
		{
			//if (waitCursor != null) waitCursor.stop();
			logging.error("Error in setHardwareInfoPage: " + ex, ex);
			return false;
		}
	}


	protected void clearSoftwareInfoPage()
	{
		mainFrame.setSoftwareAudit("", null);
	}


	protected void checkSwInfo()
	{
		/*
		if (swInfoClientmap == null)
			swInfoClientmap = new HashMap<String,  java.util.List<Map<String, Object>>>();
		*/
	}


	public void clearSwInfo()
	{
		/*
		checkSwInfo();
		swInfoClientmap.clear();
		*/
	}


	protected boolean setSoftwareInfoPage()
	{
		logging.info(this, "setSoftwareInfoPage() pcName, checkOneClientSelected " + pcName + ", " + checkOneClientSelected() );
		//WaitCursor waitCursor = null;

		try
		{
			//waitCursor = new WaitCursor( mainFrame.retrieveBasePane(), mainFrame.getCursor() );

			if (pcName == null || !checkOneClientSelected() )
				mainFrame.setSoftwareAudit();
			else
			{
				mainFrame.setSoftwareAudit(pcName, persist.getSoftwareAudit ( pcName ));
			}

			//waitCursor.stop();

			return true;
		}
		catch(Exception ex)
		{
			//if (waitCursor != null) waitCursor.stop();
			logging.error("Error in setSoftwareInfoPage: " + ex, ex);
			return false;
		}
	}

	protected void clearLogPage()
	{
		mainFrame.setLogfilePanel( new HashMap<String, String>() ) ;
	}

	public boolean updateLogPage(String logtype)
	{
		//WaitCursor waitCursor = null;

		try
		{
			logging.debug(this, "updatelogpage");

			if (!checkOneClientSelected())
				return false;

			//waitCursor = new WaitCursor( mainFrame.retrieveBasePane(), mainFrame.getCursor() );

			persist.getLogfiles(pcName, logtype);

			//waitCursor.stop();
		}
		catch(Exception ex)
		{
			//if (waitCursor != null) waitCursor.stop();
			logging.error("Error in updateLogPage: " + ex, ex);

			return false;
		}
		return true;
	}

	public boolean logfileExists(String logtype)
	{
		if (
		    logfiles == null
		    ||
		    logfiles.get(logtype) == null
		    ||
		    logfiles.get(logtype).equals("")
		    ||
		    logfiles.get(logtype).equals(
		        configed.getResourceValue("MainFrame.TabActiveForSingleClient")
		    )
		)
			return false;

		return true;
	}


	public Map<String, String> getLogfilesUpdating(String logtypeToUpdate)
	{
		logging.info(this, "getLogfilesUpdating " + logtypeToUpdate);

		if (!checkOneClientSelected())
		{
			for (int i = 0; i < persist.getLogtypes().length; i++)
			{
				logfiles.put(persist.getLogtypes()[i],
				             configed.getResourceValue("MainFrame.TabActiveForSingleClient")
				            );
			}

			mainFrame.setLogfilePanel(logfiles);
		}
		else
		{

			try
			{
				//waitCursor = new WaitCursor( mainFrame.retrieveBasePane(), mainFrame.getCursor() );
				WaitCursor waitCursor = new WaitCursor( de.uib.configed.Globals.mainContainer, "getLogfilesUpdating" );
				logfiles = persist.getLogfiles ( pcName, logtypeToUpdate );
				waitCursor.stop();

				logging.debug(this, "log pages set");
			}
			catch(Exception ex)
			{
				//if (waitCursor != null) waitCursor.stop();
				logging.error("Error in setLogPage: " + ex, ex);
			}
		}

		return logfiles;
	}

	protected boolean setLogPage()
	{
		//WaitCursor waitCursor = null;

		logging.debug(this, "setLogPage(), selected clients: " + logging.getStrings(getSelectedClients()));


		try
		{
			/*
			if (!checkOneClientSelected())
		{
				viewIndex = viewClients;
		}
			else
			*/
			{
				logfiles = persist.getEmptyLogfiles();
				mainFrame.setUpdatedLogfilePanel("instlog");
				mainFrame.setLogview("instlog");
			}

			return true;

		}
		catch(Exception ex)
		{
			//if (waitCursor != null) waitCursor.stop();
			logging.error("Error in setLogPage: " + ex, ex);
			return false;
		}
	}


	protected boolean setView(int viewIndex)
	//extra tasks not done by resetView
	{
		switch (viewIndex)
		{
		case viewClients:
			{
				checkErrorList();
				//mainFrame.menuClientSelectionSetEnabled(true);
				//mainFrame.deselectSetEnabled(true);
				depotsList.setEnabled(true);
				break;
			}
		}

		return true;
	}

	/*
	public boolean checkedResetView(int viewIndex)
{
		checkSaveAll(true);
		return resetView(viewIndex);6
}
	*/

	public boolean resetView(int viewIndex)
	{
		//logging.info(this, "resetView to: " + viewIndex + ", selected " + getSelectedClientsInTable());
		logging.info(this, "resetView  getSelectedClients() " + Arrays.toString(getSelectedClients()));

		boolean result = true;


		switch (viewIndex)
		{
		case viewClients:
			{
				break;
			}

		case viewLocalbootProducts:
			{
				result = setLocalbootProductsPage();
				break;
			}

		case viewNetbootProducts:
			{
				result = setNetbootProductsPage();
				break;
			}

		case viewNetworkconfiguration:
			{
				result = setNetworkconfigurationPage();
				break;
			}

		case viewHardwareInfo:
			{
				result = setHardwareInfoPage();
				break;
			}

		case viewSoftwareInfo:
			{
				result = setSoftwareInfoPage();
				break;
			}

		case viewLog:
			{
				result = setLogPage();
				break;
			}

		case viewProductProperties:
			{
				result = setProductPropertiesPage();

				break;
			}

		case viewHostProperties:
			{
				result = setHostPropertiesPage();
				break;
			}

		}

		return result;

	}


	public void setVisualViewIndex(int i)
	{
		mainFrame.setVisualViewIndex(i);
	}


	public void setViewIndex (int visualViewIndex)
	{
		int oldViewIndex = viewIndex;

		logging.info(this, " visualViewIndex " + visualViewIndex + ", (old) viewIndex " + viewIndex);

		/*
		if (visualViewIndex  != viewClients)
		{
		                      if( clientSelectionDialog != null )
		                              clientSelectionDialog.setEnabled(false);
	}
		else
		{
		                      if( clientSelectionDialog != null )
		                              clientSelectionDialog.setEnabled(true);
	}
		*/



		//logging.debug(this, "--------------------------- new visual view index "  + visualViewIndex );
		boolean problem = false;

		requirementsModel.setActualProduct("");


		// if we are leaving some tab we check first if we possibly have to save something
		//checkSaveAll(true);

		logging.info(this, "setViewIndex anyDataChanged " + anyDataChanged
		            );

		if (anyDataChanged && (viewIndex == viewLocalbootProducts || viewIndex ==viewNetbootProducts))
		{
			if (depotsList_selectionChanged)
			{
				requestReloadStatesAndActions();
			}
			else
			{
				requestReloadStatesAndActions(true);
			}
		}

		saveIfIndicated();


		//logging.debug(this, "new visualviewIndex " + visualViewIndex);

		//we will only leave view 0 if a PC is selected


		//if ( (pcName == null || pcName.equals(""))

		//check if change of view index to the value of visualViewIndex can be allowed
		if (  visualViewIndex != viewClients  )
		{

			/*
			if (depotsList_selectionChanged)
		{
				// ask if new data shall be fetched

				int returnedOption = JOptionPane.NO_OPTION;

				if (multiDepot)
				{
					returnedOption = JOptionPane.showOptionDialog(	mainFrame,
					                 configed.getResourceValue("ConfigedMain.dataNotFetched.text"),
					                 configed.getResourceValue("ConfigedMain.dataNotFetched.title"),
					                 JOptionPane.YES_NO_OPTION,
					                 JOptionPane.QUESTION_MESSAGE,
					                 null, null, null);
				}

				if (returnedOption == JOptionPane.YES_OPTION)
				{
					setEditingTarget(EditingTarget.CLIENTS);
					visualViewIndex = viewClients;
					changeDepotSelection();
					//logging.debug(this, " ----------------  yes!");
				}


				else
					//otherwise set back selection:
				{
					depotsList.setSelectedIndices(depotsList_selectedIndices_lastFetched);
					//mark as unchanged
					mainFrame.setChangedDepotSelectionActive(false);
				}


				depotsList_selectionChanged = false;
		}
			*/


			if ( !
			        (
			            (visualViewIndex == viewClients)
			            ||
			            ((visualViewIndex == viewNetworkconfiguration) &&  (editingTarget == EditingTarget.SERVER))
			            ||
			            ((visualViewIndex == viewHostProperties) &&  (editingTarget == EditingTarget.DEPOTS))
			        )
			   )
			{
				logging.debug(this, " selected clients " + logging.getStrings(getSelectedClients()));

				if (getSelectedClients() == null)
					//should not occur
				{
					logging.debug(this, " getSelectedClients()  null" );

					problem = true;
					JOptionPane.showMessageDialog(	mainFrame,
					                               configed.getResourceValue("ConfigedMain.pleaseSelectPc.text"),
					                               configed.getResourceValue("ConfigedMain.pleaseSelectPc.title"),
					                               JOptionPane.OK_OPTION);
					viewIndex = viewClients;
				}

				/*
				else 
			{
					if (!setDepotRepresentative())
					{
					//handled in setLocalbootProductsPage/setNetbootProductsPage
						problem = true;
						viewIndex = viewClients;
					}
			}
				*/
			}

		}

		if (! problem && dataReady) // we have loaded the data
		{
			viewIndex = visualViewIndex;

			if (viewIndex  != viewClients)
			{
				//mainFrame.menuClientSelectionSetEnabled(false);
				//mainFrame.deselectSetEnabled(false);

				depotsList.setEnabled(false);

				//mainFrame.checkSelectedClientsView( getSelectedClients().length );
			}

			/*
			if (viewIndex  != 1)
		{
				mainFrame.saveConfigurationsSetEnabled(false);
		}
			*/




			logging.debug(this, "switch to viewIndex " + viewIndex);

			boolean result = true;

			result = setView(viewIndex);
			//tasks only needed when primarily called
			result = resetView(viewIndex);
			//task needed also when recalled

			if (!result)
			{
				viewIndex = oldViewIndex;
				logging.debug(" tab index could not be changed" );
			}

			if (viewIndex == viewClients)
			{
				if (reachableUpdater.isInterrupted())
					reachableUpdater.interrupt();

				mainFrame.enableMenuItemsForClients(getSelectedClients().length);
			}

			else
				mainFrame.enableMenuItemsForClients(-1);


			switch(editingTarget)
			{
			case CLIENTS:
				saveClientsViewIndex = viewIndex;
				break;

			case DEPOTS:
				saveDepotsViewIndex = viewIndex;
				break;

			case SERVER:
				saveServerViewIndex = viewIndex;
				break;
			}
			
			if (result)
				clearListEditors();
			//dont keep product editing across views
		}
	}

	protected void updateProductStates()
	{
		//localboot products
		logging.info(this, "updateProductStates: collectChangedLocalbootStates  "  + collectChangedLocalbootStates );
		//logging.info(this, "updateProductStates: collectChangedLocalbootStates  "  + collectChangedLocalbootStates );
		if (collectChangedLocalbootStates != null  &&
		        collectChangedLocalbootStates.keySet() != null &&
		        collectChangedLocalbootStates.keySet().size() > 0
		   )
		{
			//logging.info(this, "collectChangedLocalbootStates  keySet "  + collectChangedLocalbootStates.keySet() );
			Iterator it0 = collectChangedLocalbootStates.keySet().iterator();

			while (it0.hasNext())
			{
				String client = (String) it0.next();
				Map<String, Map<String, String>> clientValues =  (Map<String, Map<String, String>>)  collectChangedLocalbootStates.get(client);

				logging.debug(this, "updateProductStates, collectChangedLocalbootStates , client " + client + " values " + clientValues);

				if (clientValues.keySet() == null || clientValues.keySet().size() == 0)
					continue;

				Iterator it1 = clientValues.keySet().iterator();
				while (it1.hasNext())
				{
					String product = (String) it1.next();
					//logging.debug(this, "updateProductStates, collectChangedLocalbootStates , client " + client + " product " + product);
					Map<String, String> productValues = (Map<String, String>) clientValues.get(product);

					persist.updateProductOnClient(
					    client,
					    product,
					    OpsiPackage.TYPE_LOCALBOOT,
					    productValues
					);
				}
			}

			persist.updateProductOnClients(); // send the collected items

			//collectChangedLocalbootStates.clear();



		}


		//netboot products
		logging.debug(this, "collectChangedNetbootStates  "  + collectChangedNetbootStates );
		if (collectChangedNetbootStates != null &&
		        collectChangedNetbootStates.keySet() != null &&
		        collectChangedNetbootStates.keySet().size() > 0)
		{
			Iterator it0 = collectChangedNetbootStates.keySet().iterator();

			while (it0.hasNext())
			{
				String client = (String) it0.next();
				Map<String, Map<String, String>> clientValues =  (Map<String, Map<String, String>>)  collectChangedNetbootStates.get(client);

				if (clientValues.keySet() == null || clientValues.keySet().size() == 0)
					continue;

				Iterator it1 = clientValues.keySet().iterator();
				while (it1.hasNext())
				{
					String product = (String) it1.next();
					Map productValues = (Map) clientValues.get(product);

					persist.updateProductOnClient(
					    client,
					    product,
					    OpsiPackage.TYPE_NETBOOT,
					    productValues
					);
				}
			}

			persist.updateProductOnClients(); // send the collected items

			//collectChangedNetbootStates.clear();

		}


		if (istmForSelectedClientsNetboot != null)
			istmForSelectedClientsNetboot.clearCollectChangedStates();

		if (istmForSelectedClientsLocalboot != null)
			istmForSelectedClientsLocalboot.clearCollectChangedStates();
	}




	public void initServer()
	{
		checkSaveAll(true);
		preSaveSelectedClients = saveSelectedClients;
		logging.debug(this, "initServer() preSaveSelectedClients " + preSaveSelectedClients);
		setSelectedClients((java.util.List) null);
		logging.debug(this, "set selected values in initServer()");
		//setSelectedClientsOnPanel(new String[0]);
		//mainFrame.setVisualViewIndex(viewClients);

	}


	public String[] getSelectedDepots()
	{
		return selectedDepots;
	}
	
	public Vector<String> getSelectedDepotsV()
	{
		return selectedDepotsV;
	}
	
	public java.util.List<String> getAccessedDepots()
	{
		ArrayList<String> accessedDepots =new ArrayList<String>();
		for (String depot : selectedDepotsV)
		{
			if (persist.getDepotPermission( depot ))
				accessedDepots.add( depot );
		}
		
		return accessedDepots;
	}
		

	public java.util.List<String> getProductNames()
	{
		return localbootProductnames;
	}

	public java.util.List<String> getAllProductNames()
	{
		java.util.List<String> productnames = new ArrayList<String>(localbootProductnames);
		productnames.addAll(netbootProductnames);

		logging.info(this, "productnames " + productnames);

		return productnames;
	}

	protected String[] getDepotArray()
	{
		if (depots == null)
			return new String[]{};

		return new ArrayList<String> (depots.keySet()).toArray(new String[]{});
	}



	protected void fetchDepots()
	{
		logging.info(this, "fetchDepots");


		
		//for testing activated in 4.0.6.0.9
		if (depotsList.getListSelectionListeners().length > 0)
			depotsList.removeListSelectionListener(depotsListSelectionListener);
		
			
		depotsList.getSelectionModel().setValueIsAdjusting(true);

		String[] depotsList_selectedValues  =  getSelectedDepots();

		//logging.debug(this, " ----------  selected before fetch " + getSelectedDepots().length);

		depots = persist.getHostInfoCollections().getDepots();


		depotNamesLinked =  persist.getHostInfoCollections().getDepotNamesList();

		/*
		
		new LinkedList<String>();
		depotNamesLinked.add(myServer);

		Map<String, Object> configServerRecord = new HashMap<String, Object>();
		configServerRecord.put("id", myServer);
		configServerRecord.put("description", "opsi config server");

		
		if (depots == null || depots.size() < 1)
		{
			logging.warning(" depots list empty, selecting main configuration server" );
			depots = new HashMap<String, Map<String, Object>>();
			depots.put(myServer, configServerRecord);
		}
		else
		{
			depots.put(myServer, configServerRecord);
			logging.debug(this, "fetchDepots depots.keySet() " +depots.keySet());
			TreeSet<String> depotIdsSorted = new TreeSet<String>( depots.keySet() );
			depotIdsSorted.remove(myServer);
			for (String depotId : depotIdsSorted)
			{
				depotNamesLinked.add(depotId);
			}
		}
		*/


		logging.debug(this, "fetchDepots sorted depots " + depotNamesLinked);

		if (depots.size() == 1)
			multiDepot = false;
		else
			multiDepot = true;

		logging.debug(this, "we have multidepot " + multiDepot);

		depotsList.setListData(getLinkedDepots());


		logging.debug(this," ----------  selected after fetch " + getSelectedDepots().length);

		boolean[] depotsList_isSelected = new boolean[depotsList.getModel().getSize()];

		for (int j = 0; j < depotsList_selectedValues.length; j++)
		{
			// collect all indices where the value had been selected
			depotsList.setSelectedValue(depotsList_selectedValues[j], false);
			if (depotsList.getSelectedIndex() > -1)
				depotsList_isSelected[depotsList.getSelectedIndex()] = true;
		}

		for (int i = 0; i < depotsList_isSelected.length; i++)
		{
			// combine the selections to a new selection
			if (depotsList_isSelected[i])
				depotsList.addSelectionInterval(i,i);
		}


		//logging.debug(this, " ----------  selected after reselect " + getSelectedDepots().length);
		depotsList_selectedIndices_lastFetched = depotsList.getSelectedIndices();

		

		if (mainFrame != null)
			mainFrame.setChangedDepotSelectionActive(false);
		
		depotsList.getSelectionModel().setValueIsAdjusting(false);
		depotsList_selectionChanged = false;
		depotsList.addListSelectionListener(depotsListSelectionListener);

	}


	public Vector<String> getLinkedDepots()
	{
		return new Vector<String> (depotNamesLinked);
	}

	public String getConfigserver()
	{
		return myServer;
	}


	public void reloadLicensesData()
	{
		logging.info(this, " reloadLicensesData _______________________________ ");
		if (dataReady)
		{

			//persist.productIdsRequestRefresh();
			persist.licencesUsageRequestRefresh();
			persist.relations_auditSoftwareToLicencePools_requestRefresh();
			persist.reconciliationInfoRequestRefresh();
			//persist.productDataRequestRefresh();
			//System.gc();



			//WaitCursor waitCursor = new WaitCursor( mainFrame.retrieveBasePane(), mainFrame.getCursor() );

			persist.getRelationsWindowsSoftwareId2LPool();

			Iterator iter = allControlMultiTablePanels.iterator();
			while (iter.hasNext())
			{
				ControlMultiTablePanel cmtp = (ControlMultiTablePanel) iter.next();
				if (cmtp != null)
				{
					for (int i = 0; i < cmtp.getTablePanes().size(); i++)
					{
						PanelGenEditTable p = cmtp.getTablePanes().get(i);
						p.reload();
					}
				}
			}

			//waitCursor.stop();
		}
	}

	private void refreshClientListKeepingGroup()
	{
		if (dataReady)
			// dont do anything if we did not finish another thread for this
		{
			//requestRefreshDataForClientSelection();

			String oldGroupSelection = activatedGroupModel.getGroupName();
			logging.info(this, " ==== refreshClientListKeepingGroup oldGroupSelection " + oldGroupSelection);

			refreshClientList();
			activateGroup(oldGroupSelection);

			freeMemoryFromSearchData(); //we have to observe the changed client selection

			//preloadData();
		}

	}

	private void changeDepotSelection()
	{
		logging.info(this, "changeDepotSelection");
		if (mainFrame != null)
		{
			//by starting a thread the visual marker of changing in progress works 
			SwingUtilities.invokeLater(new Thread(){
				 	                    public void run()
				                           {
					                           mainFrame.setChangedDepotSelectionActive(true);
					                           refreshClientListKeepingGroup();
					                           mainFrame.setChangedDepotSelectionActive(false);
				                           }
			                           }
			                          );
		}
		else
			refreshClientListKeepingGroup();

		depotsList_selectionChanged = false;
	}


	private void cancelChangeDepotSelection()
	{
		logging.debug(this, "cancelChangeDepotSelection");
		mainFrame.setChangedDepotSelectionActive(false);
		depotsList_selectionChanged = false;
	}


	public void reload()
	{
		if (mainFrame != null)
		{
			mainFrame.setChangedDepotSelectionActive(false);
			SwingUtilities.invokeLater(new Thread(){
				                           public void run()
				                           {
					                           reloadData();
				                           }
			                           }
			                          );
		}
		else
			reloadData();
	}

	private void reloadData()
	{
		checkSaveAll(true);
		int saveViewIndex = getViewIndex();
		
		logging.info(this, " reloadData _______________________________  saveViewIndex " + saveViewIndex);

		//logging.debug(this, "init waitCursor, mainFrame " + mainFrame);
		WaitCursor.stopAll(); //stop all old waiting threads if there should be any left
		//WaitCursor waitCursor = new WaitCursor(mainFrame.retrieveBasePane(), mainFrame.getCursor() );

		

		ArrayList<String> selValuesList = selectionPanel.getSelectedValues();
		
		logging.info(this, "reloadData, selValuesList.size " + selValuesList.size());
		
		String[] savedSelectedValues = selValuesList.toArray(new String[selValuesList.size()]);

		if (selectionPanel != null)
			selectionPanel.removeListSelectionListener(this); //deactivate temporarily listening to list selection events

		//setEditingTarget(EditingTarget.CLIENTS);
		//mainFrame.setVisualViewIndex(viewClients);
		boolean saveFilterClientList = filterClientList;
		//filterClientList = false;

		if (dataReady)
			// dont do anything if we did not finish another thread for this
		{
			persist.userConfigurationRequestReload();
			persist.checkConfiguration();

			//mainFrame.panel_ProductProperties.paneProducts.requestReload();
			
			//persist.mapOfMethodSignaturesRequestRefresh(); we dont need update this
			persist.opsiInformationRequestRefresh();
			persist.hwAuditConfRequestRefresh();
			logging.info(this, "call installedSoftwareInformationRequestRefresh()");
			persist.installedSoftwareInformationRequestRefresh();
			persist.softwareAuditOnClientsRequestRefresh();

			persist.productDataRequestRefresh();
			
			//logging.info(this, "reloadData saveViewIndex == viewProductProperties? " + (saveViewIndex == viewProductProperties) );
			//if (saveViewIndex == viewProductProperties)
			
			logging.info(this, "reloadData _1");
			//mainFrame.panel_ProductProperties.paneProducts.requestReload();
				//calls again persist.productDataRequestRefresh()
			mainFrame.panel_ProductProperties.paneProducts.reload();
			logging.info(this, "reloadData _2");
			
			//if variable modelDataValid in GenTableModel has no function , the following statement is sufficient:
			//globalProductsTableProvider.requestReloadRows();
			//mainFrame.panel_ProductProperties.paneProducts.reset();


			//only for licenses, will be handled in another method
			//persist.relations_auditSoftwareToLicencePools_requestRefresh();
			//persist.reconciliationInfoRequestRefresh();

			requestRefreshDataForClientSelection();
			
			reloadHosts();
			//includes:
			
			//_/
			//persist.getHostInfoCollections().opsiHostsRequestRefresh();
			//_/
			//persist.hostGroupsRequestRefresh();
			//_/
			//persist.hostConfigsRequestRefresh();
			//_/
			///persist.hostGroupsRequestRefresh();
			//_/
			//persist.fObject2GroupsRequestRefresh();
			//_/
			//persist.fGroup2MembersRequestRefresh();
			
			persist.configOptionsRequestRefresh();
			persist.fProductGroup2MembersRequestRefresh();
			persist.auditHardwareOnHostRequestRefresh();
			
			
			//clearing softwareMap in OpsiDataBackend
			de.uib.configed.clientselection.backends.opsidatamodel.OpsiDataBackend.getInstance().setReloadRequested();
			
			clearSwInfo();
			clearHwInfo();
			System.gc();
			

			preloadData(); //sets dataReady

			

			logging.info(this, " in reload, we are in thread " + Thread.currentThread());
			setRebuiltPclistTableModel();


			//for licenses
			/*
			persist.getRelationsWindowsSoftwareId2LPool();

			Iterator iter = allControlMultiTablePanels.iterator();
			while (iter.hasNext())
		{
				ControlMultiTablePanel cmtp = (ControlMultiTablePanel) iter.next();
				if (cmtp != null)
				{ 	
					for (int i = 0; i < cmtp.getTablePanes().size(); i++)
					{
						PanelGenEditTable p = cmtp.getTablePanes().get(i);
						p.reload();
					}
				}
		}
			*/
			fetchDepots();
			persist.getHostInfoCollections().getAllDepots();
			persist.checkConfiguration();  // we do this again since we reloaded the configuration

		}

		setEditingTarget(editingTarget); // sets visual view index, therefore:
		mainFrame.setVisualViewIndex(saveViewIndex);
		//logging.info(this, "set selected values in reload()");
		//setSelectedClientsOnPanel(savedSelectedValues);

		//logging.debug(this, " selected clients " +  logging.getStrings(savedSelectedValues));
		//logging.debug(this, " selected depots " +  logging.getStrings(getSelectedDepots()));
		//logging.debug(this, "pc belongs to " +  persist.getHostInfoCollections().getMapPcBelongsToDepot());


		// if depot selection changed, we adapt the clients
		TreeSet<String> clientsLeft = new TreeSet<String>();

		
		for (String client  : savedSelectedValues)
		{
			//logging.debug(this, "client: " + client);

			if (persist.getHostInfoCollections().getMapPcBelongsToDepot().get(client) != null)
			{
				String clientDepot = persist.getHostInfoCollections().getMapPcBelongsToDepot().get(client);
				//logging.debug(this, " clientDepot " + clientDepot);

				if (selectedDepotsV.contains(clientDepot))
					clientsLeft.add(client);
			}
		}

		logging.info(this, "reloadData, selected clients now " + logging.getSize(clientsLeft));


		if (selectionPanel != null)  //no action before gui initialized
		{
			//reactivate selection listener

			logging.debug(this, " reset the values, particularly in list ");

			selectionPanel.addListSelectionListener(this);
			setSelectedClientsCollectionOnPanel(clientsLeft);

			if (clientsLeft.size() == 0) //no list select item is provided
				selectionPanel.fireListSelectionEmpty(this);

			//resetSelectedTreeSubjects();

		}
		
		logging.info(this, "reloadData, selected clients now, after resetting " + logging.getSize(selectedClients));

		//waitCursor.stop();
		//logging.debug(this, "stop waitCursor");

		//mainFrame.retrieveBasePane().setCursor (new Cursor(Cursor.DEFAULT_CURSOR));

		mainFrame.reloadServerMenu();
	}

	public HostsStatusInfo getHostsStatusInfo()
	{
		return mainFrame.getHostsStatusInfo();
	}
	
	public TableModel getSelectedClientsTableModel()
	{
		//selectionPanel.getSelectedRowsModel();
		return selectionPanel.getSelectedRowsModel();
	}
	

	public void addToGlobalUpdateCollection(UpdateCollection newCollection)
	{
		updateCollection.add(newCollection);
	}

	public GeneralDataChangedKeeper getGeneralDataChangedKeeper()
	{
		return generalDataChangedKeeper;
	}

	/*		============================================
				inner class generalDataChangedKeeper
	*/  
	public class GeneralDataChangedKeeper extends DataChangedKeeper
	{
		public void dataHaveChanged(Object source)
		{
			super.dataHaveChanged(source);
			logging.info(this, "dataHaveChanged from " + source);
			setDataChanged(super.isDataChanged()); // anyDataChanged in ConfigedMain
		}


		public boolean askSave()
		{
			boolean result = false;
			if (this.dataChanged)
			{
				if (fAskSaveProductConfiguration == null)
				{
					fAskSaveProductConfiguration
					= new FTextArea(mainFrame, Globals.APPNAME,
					                true,
					                new String[] {
					                    configed.getResourceValue("MainFrame.SaveChangedText.YES"),
					                    configed.getResourceValue("MainFrame.SaveChangedText.NO")});
					fAskSaveProductConfiguration.setMessage(
					    configed.getResourceValue("ConfigedMain.reminderSaveConfig"));

					fAskSaveProductConfiguration.setSize (new Dimension (300,200));

				}


				fAskSaveProductConfiguration.setVisible(true);

				/*
				try
			{
					fAskSaveProductConfiguration.setAlwaysOnTop(true);
			}
					catch(SecurityException secex)
			{
			}
				*/

				result = (fAskSaveProductConfiguration.getResult() == 1);

				fAskSaveProductConfiguration.setVisible(false);

			}

			return result;
		}

		public void save()
		{
			if (this.dataChanged) // || (updateCollection.accumulatedSize() > 0)
			{
				saveConfigs();
			}

			this.dataChanged = false;
			//clearUpdateCollectionAndTell();
		}

		public void cancel()
		{
			logging.info(this, "cancel");
			this.dataChanged = false;
			//persist.depotProductPropertiesRequestRefresh();
			//clearUpdateCollectionAndTell();
			cancelUpdateCollection();
		}
	}
	/*		============================================*/

	public ClientInfoDataChangedKeeper getClientInfoDataChangedKeeper()
	{
		return clientInfoDataChangedKeeper;
	}

	/*		============================================
				inner class ClientInfoDataChangedKeeper
	*/
	public class ClientInfoDataChangedKeeper extends DataChangedKeeper
	{
		Map<String, String> source;



		public void dataHaveChanged(Object source)
		{
			logging.info(this, "dataHaveChanged " + source);
			if (source instanceof Map)
			{
				this.source = (Map<String, String>)source;
				//logging.debug(this, "-------- dataHaveChanged , description "
				//+ this.source.get("description") + " notes " + this.source.get("notes"));
			}
			else
			{
				//logging.debug(this, "-------- dataHaveChanged , no map ");
				this.source = null;
			}

			super.dataHaveChanged(source);
			setDataChanged(super.isDataChanged());

			if (
			    (this.source) == null
			    ||
			    (this.source).size() == 0
			)

				setDataChanged(false);

			; // anyDataChanged in ConfigedMain
		}

		public boolean askSave()
		{
			boolean result = false;
			if (this.dataChanged)
			{
				if (fAskSaveChangedText== null)
				{
					fAskSaveChangedText
					= new FTextArea(mainFrame, Globals.APPNAME,
					                true,
					                new String[] {
					                    configed.getResourceValue("MainFrame.SaveChangedText.YES"),
					                    configed.getResourceValue("MainFrame.SaveChangedText.NO")});
					fAskSaveChangedText.setMessage(
					    configed.getResourceValue("MainFrame.SaveChangedText"));
					fAskSaveChangedText.setSize (new Dimension (300,200));
				}


				fAskSaveChangedText.setVisible(true);
				result = (fAskSaveChangedText.getResult() == 1);

				fAskSaveChangedText.setVisible(false);

			}

			return result;
		}

		public void save()
		{
			if (this.dataChanged && source != null
			        &&
			        getSelectedClients() != null && getSelectedClients().length == 1 // we handle only one client
			   )
			{

				//WaitCursor waitCursor = new WaitCursor();

				hostInfo.showAndSave(selectionPanel, mainFrame, persist, getSelectedClients()[0], source);
				//refreshClientList();

				source.clear();
				//we have to clear the map instead of nulling,
				//since otherwise changedClientInfo in MainFrame keep its value
				//such producing wrong values for other clients


				//waitCursor.stop();
			}

			//logging.info(this, "save, source " + source);
			this.dataChanged = false;


		}

	}

	/*		============================================ */

	public void setDataChanged(boolean b)
	{
		setDataChanged(b, true);
	}

	private void setDataChanged(boolean b, boolean show)
	{
		logging.debug(this, "setDataChanged " + b + ", " + show);
		anyDataChanged = b;
		//logging.debug(this, " ------- we should now show that data have changed");
		if (show)
			if (mainFrame != null) mainFrame.saveConfigurationsSetEnabled(b);
	}

	public void cancelChanges()
	{
		setDataChanged(false);
		generalDataChangedKeeper.cancel();
	}

	public int checkClose()
	// 0 save changes
	// 1 lose changes
	// 2 cancel closing
	{
		int returnedOption = JOptionPane.YES_OPTION;
		int result = 0;

		if (anyDataChanged)
		{
			returnedOption = JOptionPane.showOptionDialog(	mainFrame,
			                 configed.getResourceValue("ConfigedMain.saveBeforeCloseText"),
			                 Globals.APPNAME + " " + configed.getResourceValue("ConfigedMain.saveBeforeCloseTitle"),
			                 JOptionPane.YES_NO_CANCEL_OPTION,
			                 JOptionPane.QUESTION_MESSAGE,
			                 null, null, null);

			switch(returnedOption)
			{
			case  JOptionPane.YES_OPTION :
				{
					result = 0; break;
				}

			case JOptionPane.NO_OPTION :
				{
					result = 1; break;
				}
			case JOptionPane.CANCEL_OPTION :
				{
					result = 2; break;
				}
			}
		}

		logging.debug(this, "checkClose result " + result);
		return result;
	}


	//if data are changed then save or - after asking - abandon changes
	protected void saveIfIndicated()
	{
		logging.debug(this, "---------------- saveIfIndicated : anyDataChanged, " + anyDataChanged);

		if (!anyDataChanged)
			return;


		if (clientInfoDataChangedKeeper.askSave())
		{
			clientInfoDataChangedKeeper.save();
		}
		else //reset to old values
		{
			hostInfo.resetGui(mainFrame);
		}
		clientInfoDataChangedKeeper.unsetDataChanged();

		if (generalDataChangedKeeper.askSave())
			generalDataChangedKeeper.save();
		else
			generalDataChangedKeeper.cancel();


		generalDataChangedKeeper.unsetDataChanged();

		setDataChanged(false, true);
		clearUpdateCollectionAndTell();
	}


	//save if not otherwise stated
	public void checkSaveAll(boolean ask)
	{
		logging.debug(this, "----------------  checkSaveAll: anyDataChanged, ask  " + anyDataChanged + ", " + ask);

		if (anyDataChanged)
		{
			setDataChanged (false, false); // without showing, but must be on first place since we run in this method again



			if ( ask )
			{
				if (clientInfoDataChangedKeeper.askSave())
				{
					clientInfoDataChangedKeeper.save();
				}
				else //reset to old values
				{
					hostInfo.resetGui(mainFrame);
				}
			}
			else
			{
				clientInfoDataChangedKeeper.save();
			}

			if
			( !ask
			        || generalDataChangedKeeper.askSave()
			)
				generalDataChangedKeeper.save();

			/*
			if 
				( !ask 
				|| networkConfigDataChangedKeeper.askSave()
				)
				networkConfigDataChangedKeeper.save();

			if 
				( !ask 
				|| additionalConfigDataChangedKeeper.askSave()
				)
				additionalConfigDataChangedKeeper.save();
				
			*/

			setDataChanged (false, true);
		}
	}

	private class ReachableUpdater extends Thread
	{
		private boolean suspended = false;
		private int interval = 0;

		ReachableUpdater(Integer interval)
		{
			super();
			setInterval(interval);
		}

		public void setInterval(Integer interval)
		{
			int oldInterval = this.interval;

			if (interval == null)
				this.interval = 0;
			else
				this.interval = interval;

			if (oldInterval == 0 && this.interval > 0)
				start();
		}

		boolean isSuspended()
		{
			return suspended;
		}

		void setSuspended(boolean b)
		{
			suspended = b;
		}

		int getInterval()
		{
			return interval;
		}

		@Override
		public void run()
		{
			while (true)
			{
				logging.debug(this, " "
				              + " suspended , editingTarget, viewIndex " +

				              suspended + ", " + editingTarget + ", " + viewIndex

				             );
				          
				//boolean changed = false;
				if (
				    !suspended
				    &&
				    editingTarget ==  EditingTarget.CLIENTS
				    &&
				    viewIndex == viewClients
				)
				{
					//logging.debug(this, "updating");
					try
					{
						//we catch exceptions especially if we are on some updating process for the model

						if (persist.getHost_displayFields().get("clientConnected"))
						{
							Map<String, Object> saveReachableInfo = reachableInfo;
	
							reachableInfo = persist.reachableInfo(null);
							//update column
	
							reachableInfo = persist.reachableInfo(null);
	
							javax.swing.table.AbstractTableModel model = selectionPanel.getTableModel();
	
							int col = model.findColumn(
										  configed.getResourceValue("ConfigedMain.pclistTableModel.clientConnected")
									  );
	
	
							//logging.debug(this, "column count " + model.getColumnCount());
							for (int row = 0; row < model.getRowCount(); row ++)
							{
								String clientId = (String) model.getValueAt(row, 0);
								Boolean newInfo = (Boolean) reachableInfo.get(clientId);
	
								if (newInfo != null)
								{
	
										if ( saveReachableInfo.get(clientId)  == null)
										{
											//changed = true;
											model.setValueAt(newInfo, row, col);
										}
										
										
										else if (model.getValueAt(row, col) != null && !model.getValueAt(row, col).equals(newInfo))
										{
											//changed = true;
											model.setValueAt(newInfo, row, col);
			
											model.fireTableRowsUpdated(row, row);
											//if ordered by col the order does not change although the value changes
											//if the other way is wanted a global fireTableDataChanged (see below)
											//is necessary
										}
									}
	
	
							}
						}

						/*

						java.util.List selectedClients = getSelectedClientsInTable(); 
						if (changed) model.fireTableDataChanged();

						if (selectedClients != null && selectedClients.size() > 0)
							setSelectedClientsOnPanel(selectedClients);
							
						*/
					}
					catch(Exception ex)
					{
						logging.info(this, "we could not update the model");
					}

				}
				

				try
				{

					int millisecs = interval * 60 * 1000;
					logging.debug(this, "Thread going to sleep for ms " + millisecs);
					sleep(millisecs);
				}
				catch(InterruptedException ex)
				{
					logging.info(this, "Thread interrupted ");
				}
			}
		}
	}


	public void getReachableInfo()
	{
		//we put this into a thread since it may never end in case of a name resolving problem
		new Thread(){
			public void run()
			{
				//reachableUpdater.setSuspended(true);
				
				if (fShowReachableInfo == null)
				{
					fShowReachableInfo = new FShowList(null, Globals.APPNAME, false,
													new Object[]{configed.getResourceValue("ConfigedMain.reachableInfoCancel")}, 350, 100)
									  {
										  public void doAction1()
										  {
											  super.doAction1();
										  }
									  }
									 ;
					fShowReachableInfo.centerOn(Globals.mainContainer);
				}

				fShowReachableInfo.setMessage(configed.getResourceValue("ConfigedMain.reachableInfoRequested"));
				//fShowReachableInfo.centerOn(Globals.mainContainer);
				fShowReachableInfo.setAlwaysOnTop(true);
				fShowReachableInfo.setVisible(true);
				fShowReachableInfo.glassTransparency(true, 200, 100, 0.005f);
				fShowReachableInfo.toFront();


				reachableInfo = persist.reachableInfo(null); //it occurs  that this does not return

				/*
				try{
					sleep(10000);
			}
				catch(Exception ex)
				{
			}
				*/

				fShowReachableInfo.setVisible(false);
				//fShowReachableInfo.dispose();


				mainFrame.iconButtonReachableInfo.setEnabled(true);


				//update column
				if (persist.getHost_displayFields().get("clientConnected"))
				{
					javax.swing.table.AbstractTableModel model = selectionPanel.getTableModel();

					int col = model.findColumn(
					              configed.getResourceValue("ConfigedMain.pclistTableModel.clientConnected")
					          );

					for (int row = 0; row < model.getRowCount(); row ++)
					{
						String clientId = (String) model.getValueAt(row, 0);
						model.setValueAt(reachableInfo.get(clientId), row, col);
					}

					model.fireTableDataChanged();
				}


				//reachableUpdater.setSuspended(false);


			}
		}
		.start();


	}


	public void getSessionInfo(final boolean onlySelectedClients)
	{
		//we put this into a thread since it is very long lasting
		new Thread(){
			public void run()
			{
				final Thread me = this;

				FShowList fShow = new FShowList(null, Globals.APPNAME, false,
				                                new Object[]{configed.getResourceValue("ConfigedMain.sessionInfoCancel")}, 350, 100)
				                  {
					                  public void doAction1()
					                  {
						                  super.doAction1();
						                  //me.interrupt();
					                  }
				                  }
				                  ;

				fShow.setMessage(configed.getResourceValue("ConfigedMain.sessionInfoRequested"));
				fShow.centerOn(Globals.mainContainer);
				fShow.setAlwaysOnTop(true);
				fShow.setVisible(true);

				fShow.glassTransparency(true, 200, 100, 0.005f);
				//fShow.toFront();


				if (onlySelectedClients)
				{
					sessionInfo.putAll(persist.sessionInfo(getSelectedClients()));
				}

				else
					sessionInfo = persist.sessionInfo(null);

				fShow.setVisible(false);
				fShow.dispose();


				mainFrame.iconButtonSessionInfo.setEnabled(true);


				//update column
				if (persist.getHost_displayFields().get("clientSessionInfo"))
				{
					javax.swing.table.AbstractTableModel model = selectionPanel.getTableModel();

					int col = model.findColumn(
					              configed.getResourceValue("ConfigedMain.pclistTableModel.clientSessionInfo")
					          );

					for (int row = 0; row < model.getRowCount(); row ++)
					{
						String clientId = (String) model.getValueAt(row, 0);

						String value = sessionInfo.get(clientId);

						if (value == null)
							value = "";

						model.setValueAt(sessionInfo.get(clientId), row, col);
					}

					model.fireTableDataChanged();
				}

			}
		}
		.start();

	}


	public String getBackendInfos()
	{
		return persist.getBackendInfos();
	}

	public String getOpsiVersion()
	{
		return persist.getOpsiVersion();
	}

	public Map<String, RemoteControl> getRemoteControls()
	{
		return persist.getRemoteControls();
	}


	public void resetProductsForSelectedClients()
	{
		if (getSelectedClients() == null || getSelectedClients().length == 0)
			return;

		if  (!confirmActionForSelectedClients(
		            configed.getResourceValue("ConfigedMain.confirmResetProducts.question")
		        )
		    )
			return;

		//logging.debug(this, "-------- Now we should do the reset");



		//final WaitCursor waitCursor = new WaitCursor();

		persist.resetLocalbootProducts(getSelectedClients());

		//waitCursor.stop();

		requestReloadStatesAndActions(true);

	}
	
	public boolean freeAllPossibleLicencesForSelectedClients()
	{
		logging.info(this,  "freeAllPossibleLicencesForSelectedClients, count " +  getSelectedClients().length);
		
		
		if (getSelectedClients() == null || getSelectedClients().length == 0)
			return true;

		
		if  (!confirmActionForSelectedClients(
		            configed.getResourceValue("ConfigedMain.confirmFreeLicences.question")
		        )
		    )
		{
			return false;
		}
			
		for (String client : getSelectedClients())
		{
			//logging.debug(this, " freeAllPossibleLicencesForSelectedClients, client " + client);
			
			Map<String, java.util.List<LicenceUsageEntry>>
				fClient2LicencesUsageList = persist.getFClient2LicencesUsageList(); 
			
			//logging.info(this, " freeAllPossibleLicencesForSelectedClients, usages of client " + fClient2LicencesUsageList.get(client).size());
				
			for (LicenceUsageEntry m : fClient2LicencesUsageList.get(client))
			{	
				//logging.debug(this, " free " + m);
				persist.addDeletionLicenceUsage(
					client,
					m.getLicenceId(),
					m.getLicencepool()
				);
			}
		}
		
		return persist.executeCollectedDeletionsLicenceUsage();
		
	}


	public void callNewClientDialog()
	{

		/*
		if (!NewClientDialog.getInstance(this).macAddressFieldIsSet())
		{	
			
		 logging.debug(this, " --------------------------  signature of createClient  " +  ((java.util.List)persist.getMethodSignature("createClient")));

		  if ( ((java.util.List)persist.getMethodSignature("createClient")).contains("hardwareAddress") )
					NewClientDialog.getInstance(this).setMacAddressFieldVisible(true);
			else
				  NewClientDialog.getInstance(this).setMacAddressFieldVisible(false);
	}
		*/
		// java.util.List<String> groupList = persist.getHostGroupIds();
		// Collections.sort( groupList );
		// Vector<String> groupSelectionIds = new Vector(groupList);

		Collections.sort( localbootProductnames );
		Vector<String> vLocalbootProducts = new Vector(localbootProductnames);
		Collections.sort( netbootProductnames );
		Vector<String> vNetbootProducts = new Vector(netbootProductnames);

		NewClientDialog.getInstance(this, getLinkedDepots()).setVisible(true);
		NewClientDialog.getInstance().setGroupList(new Vector(persist.getHostGroupIds()));
		NewClientDialog.getInstance().setProductNetbootList(vNetbootProducts);
		NewClientDialog.getInstance().setProductLocalbootList(vLocalbootProducts);

		NewClientDialog.getInstance().setDomain(myDomain);
		// persist.opsiHostNamesRequestRefresh();
		NewClientDialog.getInstance().setHostNames(persist.getHostInfoCollections().getOpsiHostNames());

		try // in an applet context this may cause a security problem
		{
			NewClientDialog.getInstance().setAlwaysOnTop(true);
		}
		catch (Exception ex)
		{
		}

	}

	public void callChangeClientIDDialog()
	{
		if (getSelectedClients() == null || getSelectedClients().length != 1)
			return;

		FEditText fEdit = new FEditText(getSelectedClients()[0])
		                  {
			                  protected void commit()
			                  {
				                  super.commit();

				                  //final WaitCursor waitCursor = new WaitCursor();

				                  //new Thread (){
				                  //public void run()
				                  //{
				                  String newID = getText();

				                  logging.debug(this, "new name " + newID);

				                  persist.renameClient(getSelectedClients()[0], newID);

				                  refreshClientList(newID);



				                  //     }
				                  //}.start();

				                  //waitCursor.stop();
			                  }
		                  };

		fEdit.init();
		fEdit.setTitle(configed.getResourceValue("ConfigedMain.fChangeClientID.title") + " ("+ Globals.APPNAME + ")");
		fEdit.setSize(250, 120);
		fEdit.centerOn(Globals.mainContainer);
		fEdit.setSingleLine(true);
		fEdit.setModal(true);
		fEdit.setAlwaysOnTop(true);
		fEdit.setVisible(true);

	}


	public void callChangeDepotDialog()
	{
		if (getSelectedClients() == null || getSelectedClients().length == 0)
			return;

		FShowListWithComboSelect fChangeDepotForClients
		= new FShowListWithComboSelect(mainFrame, Globals.APPNAME
		                               + " " +  configed.getResourceValue( "ConfigedMain.fChangeDepotForClients.title" ),
		                               true,
		                               configed.getResourceValue( "ConfigedMain.fChangeDepotForClients.newDepot" ),
		                               getDepotArray(),
		                               new String[]{	configed.getResourceValue( "ConfigedMain.fChangeDepotForClients.OptionNO"  ),
		                                             configed.getResourceValue( "ConfigedMain.fChangeDepotForClients.OptionYES" ) }
		                              );


		fChangeDepotForClients.setLineWrap(false);

		StringBuffer messageBuffer = new StringBuffer( "\n" + configed.getResourceValue( "ConfigedMain.fChangeDepotForClients.Moving" ) + ": \n\n");


		for ( int i  = 0; i < getSelectedClients().length; i++)
		{
			messageBuffer.append (getSelectedClients()[i]);
			messageBuffer.append ("     (from: ");
			try{
				messageBuffer.append (persist.getHostInfoCollections().getMapPcBelongsToDepot().get(getSelectedClients()[i]).toString());
			}
			catch(Exception e)
			{
				logging.warning(this, "changeDepot for " + getSelectedClients()[i] + " " + e);
			}
				
			messageBuffer.append (") ");

			//result.append ("; ");
			messageBuffer.append ("\n");
		}


		fChangeDepotForClients.setSize(new Dimension (400, 250));
		fChangeDepotForClients.setMessage(messageBuffer.toString());

		fChangeDepotForClients.setVisible(true);


		if (fChangeDepotForClients.getResult() !=2)
			return;

		final String targetDepot = (String) fChangeDepotForClients.getChoice();

		if (targetDepot == null || targetDepot.equals(""))
			return;


		//final WaitCursor waitCursor = new WaitCursor();

		logging.debug(this, " start moving to another depot");

		
		persist.getHostInfoCollections().setDepotForClients(getSelectedClients(), targetDepot);

		checkErrorList();
		
		refreshClientListKeepingGroup();
		//refreshClientList(true);

		
		//selectionPanel.setModel (());

	}


	/* transferred to persistencecontroller
	protected void setDepotForClients(String[] clients, String depotId)
	{
		Map<String, Object> values = new HashMap<String, Object>();
		ArrayList depots = new ArrayList();
		values.put("clientconfig.depot.id", depots);
		ConfigName2ConfigValue config = new ConfigName2ConfigValue(null);
		//logging.debug(this, "setDepotForClients, values " + values);
		depots.add(depotId);
		config.put("clientconfig.depot.id", depots);
		for (int i = 0; i < clients.length; i++)
		{
			//collect data
			logging.debug(this, "setDepotForClients, client " + clients[i] +  ", configState " + config);
			persist.setAdditionalConfiguration(clients[i], config);
		}
		//send data
		persist.setAdditionalConfiguration(false);

	}
	*/

	/*
	protected void setDepotForClient(final String clientId, final String depotId)
{
		if (clientId != null && depotId != null && !clientId.equals("") && !depotId.equals(""))
		{
			Map networkConfig = persist.getNetworkConfiguration(clientId);
			networkConfig.put("depotId", depotId);
			persist.setNetworkConfiguration(clientId, networkConfig);
		}
}
	*/


	protected void refreshClientList()
	{
		logging.info(this, "refreshClientList");
		//persist.getHostInfoCollections().pclistRequestRefresh();
		producePcListForDepots(getSelectedDepots());

		setRebuiltPclistTableModel();
	}

	protected void initialTreeActivation(final String groupName)
	{
		logging.info(this, "initialTreeActivation");
		treeClients.expandPath(treeClients.getPathToALL());

		String oldGroupSelection = groupName;
		if (oldGroupSelection == null)
			oldGroupSelection = configed.savedStates.saveGroupSelection.deserialize();


		if ( oldGroupSelection != null && activateGroup(oldGroupSelection) )
			logging.info(this, "old group reset " + oldGroupSelection);
		else
			activateGroup(ClientTree.ALL_NAME);
	}


	protected void initialTreeActivation()
	{
		initialTreeActivation(null);
	}

	protected void refreshClientListActivateALL()
	{
		logging.info(this, "refreshClientListActivateALL");
		refreshClientList();
		activateGroup(ClientTree.ALL_NAME);
		//initialTreeActivation();
	}

	protected void refreshClientList(String selectClient)
	{
		logging.info(this, "refreshClientList " + selectClient);
		refreshClientListActivateALL();

		if (selectClient != null)
		{
			TreeSet<String> selectedList = new TreeSet<String>();
			logging.debug(this, "set client refreshClientList");

			setClient(selectClient);
		}

	}

	protected void refreshClientList(boolean resetSelection)
	{
		logging.info(this, "refreshClientList  resetSelecton " + resetSelection);
		refreshClientListActivateALL();

		if (resetSelection) setClientGroup();
	}
	
	public void reloadHosts()
	{
		persist.getHostInfoCollections().opsiHostsRequestRefresh();
		persist.hostConfigsRequestRefresh();
		persist.hostGroupsRequestRefresh();
		persist.fObject2GroupsRequestRefresh();
		persist.fGroup2MembersRequestRefresh(); //??
		refreshClientListKeepingGroup();
	}

	public void setInstallByShutdown(String clientname, boolean status)
	{
		String product = "opsi-client-agent";
		String aktivate = "off";
		String setup = "setup";
		if (status) 
			aktivate = "on";

		// ArrayList<String> shutdown_value = ( (ArrayList) persist.getCommonProductPropertyValues( new ArrayList(Arrays.asList(clientname)) , product, "on_shutdown_install" ) );

		// if ( (shutdown_value.get(0) != null) && !(shutdown_value.get(0).equals(aktivate))  )
		{
			persist.setCommonProductPropertyValue( new HashSet(Arrays.asList(clientname)), product, "on_shutdown_install" , Arrays.asList(aktivate) );
		
			// if (status== setup)
			// 		set.status= none

			Map<String, String> productValues = new HashMap<String,String>();
			productValues.put("actionRequest", setup);

			persist.updateProductOnClient(
			   			clientname,
			    		product,
			    		OpsiPackage.TYPE_LOCALBOOT,
			    		productValues
				);
			persist.updateProductOnClients();
		}
	}

	public void createClient(final String hostname, final String domainname,
	                         final String depotID, final String description, final String inventorynumber, final String notes,
	                         final String ipaddress, final String macaddress, 
	                         /*final boolean shutdownInstall,*/ final boolean uefiBoot, final boolean wanConfig, 
	                         final String group, final String productNetboot, final String productLocalboot)
	{
		//final WaitCursor waitCursor = new WaitCursor(mainFrame.retrieveBasePane(), mainFrame.getCursor() );

		if (persist.createClient(hostname, domainname, depotID, description, inventorynumber, notes, ipaddress, macaddress, /*shutdownInstall,*/ uefiBoot, wanConfig, group, productNetboot, productLocalboot))
		{
			String newClientID = hostname + "." + domainname;
			//is guarantueed by a config
			//else
			//	setDepotForClients(new String[]{newClientID}, myServer);

			checkErrorList();

			persist.getHostInfoCollections().addOpsiHostName(newClientID);
			//
			persist.fObject2GroupsRequestRefresh();
			
			refreshClientList(newClientID);
			//persist.getHostInfoCollections().getPcListForDepots(selectedDepots);
			
			activateGroup(group);
			
			/*
			if (depotID != null && !depotID.equals(""))
				persist.getHostInfoCollections().setDepotForClients(new String[]{newClientID}, depotID);
			*/


			// if (shutdownInstall)
			// {
			// 	String product = "opsi-client-agent";
			// 	persist.setCommonProductPropertyValue( new HashSet(Arrays.asList(newClientID)), product, "on_shutdown_install" , Arrays.asList("on") );
			// 	Map<String, String> productValues = new HashMap<String,String>();
			// 	productValues.put("actionRequest", "setup");

			// 	persist.updateProductOnClient(
			// 			   			newClientID,
			// 			    		product,
			// 			    		OpsiPackage.TYPE_LOCALBOOT,
			// 			    		productValues
			// 			);

			// 	persist.updateProductOnClients();				
			// 	hostInfo.setShutdownInstall(shutdownInstall);
			// 	persist.getHostInfoCollections().updateLocalHostInfo(newClientID, HostInfo.clientShutdownInstallKEY, shutdownInstall);
			// 	// persist.getHostInfoCollections().setLocalHostInfo(newClientID, depotID, hostInfo);
			// }
		}

		
		//waitCursor.stop();


		/*
		new Thread (){
			public void run()
			{
				if (persist.createClient(hostname, domainname, description, notes, ipaddress, macaddress))
				{
					final String newClientID = hostname + "." + domainname;
					if (depotID != null && !depotID.equals(""))
						setDepotForClients(new String[]{newClientID}, depotID);
					
					//is guarantueed by a config
					//else 
					//	setDepotForClients(new String[]{newClientID}, myServer);
			
					checkErrorList();
					
					persist.addOpsiHostName(newClientID);
					
					try
					{
						
						SwingUtilities.invokeLater(new Runnable()
							{
								public void run()
								{
									refreshClientList(newClientID);
								}
							}
						);
					}
					catch(Exception iex)
					{
						logging.info(this, "refreshClientList " + iex);
					}
				}
				
				waitCursor.stop();
			}
	}.start();

		*/

	}


	private abstract class ErrorListProducer extends Thread
	{
		String title;
		ErrorListProducer(String specificPartOfTitle)
		{
			String part = specificPartOfTitle;
			//int maxlen = 30;
			//if (part.length() > maxlen)
			//	part = part.substring(0, maxlen) + " ...";

			title = Globals.APPNAME + ":  " +
			        //" (" + part + ")";
			        part;
		}


		protected abstract java.util.List getErrors();

		@Override
		public void run()
		{
			FShowList fListFeedback = new FShowList(mainFrame,
			                                        title,
			                                        false, 0);
			fListFeedback.setFont(Globals.defaultFont);
			fListFeedback.setMessage("");
			fListFeedback.setButtonsEnabled(false);
			Cursor oldCursor = fListFeedback.getCursor();
			fListFeedback.setCursor(new Cursor(Cursor.WAIT_CURSOR));
			fListFeedback.setVisible(true);
			fListFeedback.glassTransparency(true, 1000, 200, 0.04f);

			java.util.List errors = getErrors();

			//logging.debug(this, "errors.size " + errors.size());

			if (errors.size() > 0)
			{
				//logging.debug(this, "errors found");
				fListFeedback.setLines(errors);
				fListFeedback.setCursor(oldCursor);
				fListFeedback.setButtonsEnabled(true);

				fListFeedback.setVisible(true);

				//logging.debug(this, "going interactive");
			}
			else
			{
				fListFeedback.leave();
				//logging.debug(this, "leaving");
			}

			fListFeedback = null;

		}
	}
	
	
	public void wakeUp(final String[] clients, String startInfo)
	{
		if (clients == null)
			return;
		
		logging.info(this, "wakeUp " + clients.length + " clients: " + startInfo);
		if (clients.length == 0)
			return;

		new ErrorListProducer(
		    configed.getResourceValue("ConfigedMain.infoWakeClients") + " " + startInfo 
		)
		{
			@Override
			protected java.util.List getErrors()
			{
				return persist.wakeOnLan(clients);
			}
		}.start();

	}
		

	public void wakeSelectedClients()
	{
		wakeUp( getSelectedClients(), "" );
	}


	public void wakeUpWithDelay(final int delaySecs,  final String[] clients, String startInfo)
	{
		if (clients == null)
			return;
		
		logging.info(this, "wakeUpWithDelay " + clients.length + " clients: " + startInfo + " delay secs " + delaySecs);
		
		if (clients.length == 0)
			return;

		final FWakeClients result =
			new FWakeClients(
					mainFrame,  
					Globals.APPNAME + ": " +  configed.getResourceValue("FWakeClients.title") 
						+ " " + startInfo  
						 ,
					persist			
				);
		
		new Thread(){
			public void run()
			{
				result.act(clients, delaySecs);
			}
		}.
		start();
	}
	
	public void wakeSelectedClientsWithDelay(final int delaySecs)
	{
		wakeUpWithDelay(delaySecs, getSelectedClients(), "");
	}

	/*
	new Thread(){
		public void run()
		{
			
			FShowList fListFeedback = new FShowList(mainFrame,
			"wake on lan", 
			false, 0);
			fListFeedback.setFont(Globals.defaultFont);
			fListFeedback.setMessage("");
			fListFeedback.setButtonsEnabled(false);
			Cursor oldCursor = fListFeedback.getCursor();
			//fListFeedback.setCursor(new Cursor(Cursor.WAIT_CURSOR));
			fListFeedback.setVisible(true);
			fListFeedback.glassTransparency(true, 1000, 200, 0.04f);
			
			for (int i = 0; i < getSelectedClients().length; i++)
			{
				fListFeedback.appendLine("trying to start " + getSelectedClients()[i]);
				persist.wakeOnLan(new String[] {getSelectedClients()[i]} );
				try
				{
					Thread.sleep(1000 * delaySecs);
				}
				catch(InterruptedException ies)
				{
				}
			}
			
			//fListFeedback.setVisible(false);
			//fListFeedback.dispose();
			//fListFeedback = null;
		}
}.start();

	*/



	public void deletePackageCachesOfSelectedClients()
	{
		if (getSelectedClients() == null || getSelectedClients().length == 0)
			return;

		new ErrorListProducer(
		    configed.getResourceValue("ConfigedMain.infoDeletePackageCaches")
		)
		{
			@Override
			protected java.util.List getErrors()
			{
				return persist. deletePackageCaches(getSelectedClients());
			}
		}.start();

	}

	public void fireOpsiclientdEventOnSelectedClients(final String event)
	{
		if (getSelectedClients() == null || getSelectedClients().length == 0)
			return;

		new ErrorListProducer(
		    //configed.getResourceValue("ConfigedMain.infoFireEvent") + event
		    "opsiclientd " + event
		)
		{
			@Override
			protected java.util.List getErrors()
			{
				return persist.fireOpsiclientdEventOnClients(event, getSelectedClients());
			}
		}.start();

	}

	public void showPopupOnSelectedClients (final String message)
	{
		if (getSelectedClients() == null || getSelectedClients().length == 0)
			return;


		new ErrorListProducer(
		    configed.getResourceValue("ConfigedMain.infoPopup")
		    + " " + message
		)
		{
			@Override
			protected java.util.List getErrors()
			{
				return persist.showPopupOnClients(
				           message,
				           getSelectedClients());
			}

		}.start();

	}


	private void initSavedSearchesDialog()
	{
		if( savedSearchesDialog == null )
		{
			logging.debug( this, "create SavedSearchesDialog");
			savedSearchesDialog = new SavedSearchesDialog()

			                      {
				                      @Override
				                      protected void commit()
				                      {
					                      super.commit();
					                      java.util.List<String> result = (java.util.List<String>) super.getValue();
					                      logging.info(this, "commit result == null " + (result == null));
					                      if( result != null )
					                      {
						                      logging.info(this, "result size " + result.size());
						                      setSelectedClientsCollectionOnPanel( result, true );
					                      }

				                      }

				                      @Override
				                      protected void removeSavedSearch(String name)
				                      {
					                      persist.deleteSavedSearch(name);
					                      super.removeSavedSearch(name);
				                      }

				                      @Override
				                      protected void reloadAction()
				                      {
					                      persist.configOptionsRequestRefresh();
					                      persist.auditHardwareOnHostRequestRefresh();
					                      resetModel();
				                      }

				                      @Override
				                      protected void addElement()
				                      {
					                      callClientSelectionDialog();
				                      }

				                      @Override
				                      protected void editSearch( String name )
				                      {
					                      callClientSelectionDialog();
					                      clientSelectionDialog.loadSearch( name );
				                      }
			                      }
			                      ;
			savedSearchesDialog.init(new Dimension(300, 400));
		}
		savedSearchesDialog.start();
	}

	public void clientSelectionGetSavedSearch()
	{
		logging.debug( this, "clientSelectionGetSavedSearch");
		initSavedSearchesDialog();

		try
		{
			java.awt.Point pointField = Globals.container1.getLocationOnScreen();
			savedSearchesDialog.setLocation((int) pointField.getX() + 30, (int) pointField.getY() + 20);
		}
		catch(IllegalComponentStateException ex)
		{
			logging.info(this, "clientSelectionGetSavedSearch " + ex);
		}


		savedSearchesDialog.setVisible(true);



		/*
		Object value = savedSearchesDialog.getValue();
		logging.debug( this, "clientSelectionGetSavedSearch value "+value );
		if( value == null )
			return;
		setSelectedClientsOnPanel( (java.util.List<String>) value );
		*/

	}


	private class MyDialogRemoteControl extends FDialogRemoteControl
	{
		public void appendLog(final String s)
		{
			SwingUtilities.invokeLater(new Runnable()
			                           {
				                           public void run()
				                           {
					                           if (s == null)
						                           loggingArea.setText("");
					                           else
					                           {
						                           loggingArea.append(s);
						                           loggingArea.setCaretPosition(loggingArea.getText().length());
					                           }
				                           }
			                           }
			                          );
		}

		@Override
		public void commit()
		{
			super.commit();
			setVisible(true);


			logging.debug(this, "getSelectedValue " + getSelectedList());

			appendLog(null);

			if (getSelectedList().size() > 0)
			{
				final String selected = "" + getSelectedList().get(0);

				for ( int j = 0; j < getSelectedClients().length; j++)
				{
					final int J = j;

					new Thread(){
						public void run()
						{
							String cmd = getValue(selected);
							//remoteControls.get(selected).getCommand();
							de.uib.utilities.script.Interpreter trans = new de.uib.utilities.script.Interpreter(
							            new String[]{"%host%","%ipaddress%", "%inventorynumber%", "%hardwareaddress%",
							                         "%opsihostkey%",
							                         "%depotid%", "%configserverid%"});

							trans.setCommand(cmd);

							HashMap<String, Object> values = new HashMap<String, Object>();
							String targetClient = getSelectedClients()[J];
							values.put("%host%", targetClient);

							HostInfo pcInfo = persist.getHostInfoCollections().getMapOfPCInfoMaps().get(targetClient);
							values.put("%ipaddress%", pcInfo.getIpAddress());
							values.put("%hardwareaddress%", pcInfo.getMacAddress());
							values.put("%inventorynumber%", pcInfo.getInventoryNumber());
							values.put("%opsihostkey%", pcInfo.getHostKey());
							values.put("%depotid%", pcInfo.getInDepot());

							String configServerId = myServer;
							if (myServer == null || myServer.equals(""))
							{
								myServer = "localhost";
							}
							values.put("%configserverid%", configServerId);

							trans.setValues(values);

							cmd = trans.interpret();

							java.util.List<String> parts = de.uib.utilities.script.Interpreter.splitToList(cmd);

							try
							{
								logging.debug(this, "startRemoteControlForSelectedClients, cmd: " + cmd + " splitted to " + parts );


								ProcessBuilder pb = new ProcessBuilder(parts);
								pb.redirectErrorStream(true);

								Process proc = pb.start();

								BufferedReader br = new BufferedReader(new InputStreamReader(
								                                           proc.getInputStream()
								                                       ));

								String line = null;
								while ( (line = br.readLine()) != null)
								{
									//System.out.println(getSelectedClients()[J] + " >" + line);
									appendLog(selected + " on " + getSelectedClients()[J] + " >" + line + "\n");
								}
								//System.out.println(getSelectedClients()[J] + " process exitValue " + proc.exitValue());
							}
							catch(Exception ex)
							{
								logging.error("Runtime error for command >>" + cmd + "<<, : "  + ex, ex);
							}
						}
					}.start();
				}


			}
		}
	}



	public void startRemoteControlForSelectedClients()
	{
		if (dialogRemoteControl == null)
			dialogRemoteControl = new MyDialogRemoteControl();

		if (remoteControls != getRemoteControls())
		{
			remoteControls = getRemoteControls();

			logging.debug(this, "remoteControls " + remoteControls);

			Map<String, String> entries = new LinkedHashMap<String, String>();
			Map<String, String> tooltips = new LinkedHashMap<String, String>();
			Map<String, String>	rcCommands = new HashMap<String, String>();
			Map<String, Boolean> commandsEditable = new HashMap<String, Boolean>();

			for (String key : remoteControls.keySet())
			{
				entries.put(key, key);
				RemoteControl rc = remoteControls.get(key);
				if (rc.getDescription() != null && rc.getDescription().length() > 0)
					tooltips.put(key, rc.getDescription());
				else
					tooltips.put(key, rc.getCommand());
				rcCommands.put(key, rc.getCommand());
				Boolean editable = Boolean.valueOf(rc.getEditable());
				//logging.debug(this, "remoteControl editable " + key + ",  "  + rc.getEditable() + ", " + editable);
				commandsEditable.put(key, editable);


			}

			//logging.debug(this, "startRemoteControlForSelectedClients " + entries + ", " + tooltips );


			dialogRemoteControl.setMeanings(rcCommands);
			dialogRemoteControl.setEditable(commandsEditable);

			dialogRemoteControl.setListModel(new DefaultComboBoxModel(
			                                     new Vector(new TreeSet(remoteControls.keySet()))
			                                     //new String[]{"xx"}
			                                 )
			                                );


			dialogRemoteControl.setCellRenderer(new ListCellRendererByIndex(
			                                        entries, tooltips,
			                                        null, -1, false, "")
			                                   );

			dialogRemoteControl.setTitle(Globals.APPNAME + ":  " + configed.getResourceValue("MainFrame.jMenuRemoteControl") );
			dialogRemoteControl.setModal(false);
			dialogRemoteControl.init();

		}

		dialogRemoteControl.resetValue();


		if (!configed.isApplet)
		{
			dialogRemoteControl.setLocation((int) mainFrame.getX() +40 , (int) mainFrame.getY() +40);
			dialogRemoteControl.setSize(mainFrame.fwidth, mainFrame.getHeight()/2);
		}
		else
		{
			dialogRemoteControl.setLocation((int) mainFrame.baseContainer.getX() +40 , (int) mainFrame.getY() +40);
			dialogRemoteControl.setSize(mainFrame.fwidth, mainFrame.baseContainer.getHeight()/2);
		}

		dialogRemoteControl.setVisible(true);
		dialogRemoteControl.setDividerLocation(0.8);
	}

	public void reloadServerMenu()
	{
		mainFrame.reloadServerMenu();
	}
	
	/**
	* Starts the execution of command
	* @param command
	*/
	public void startSSHOpsiServerExec(final SSHCommand command)
	{
		// if (!(Globals.isGlobalReadOnly()))
		// {
			logging.info(this, "startSSHOpsiServerExec isReadOnly false");
			final ConfigedMain m = this;
			new Thread()
			{
				public void run()
				{
					if (command.needParameter()) ((SSHCommandNeedParameter)command).startParameterGui(m);
					else  new SSHConnectExec(m, command);
					// if (!(de.uib.configed.Globals.isGlobalReadOnly()))
					// {
					// 	if (command instanceof SSHCommand_Template) exec_template((SSHCommand_Template) command);
					// 	else if (command.isMultiCommand()) exec_list((SSHMultiCommand)command);
					// 	else exec(command); 
					// }
				}
			}.start();
		// }
	}
	
	/**
	* Starts the config dialog
	*/
	public void startSSHConfigDialog()
	{
		// if (configed.sshkey != null)
		// 	SSHConfigDialog sshConfig = SSHConfigDialog.getInstance(mainFrame, this, sshkey, readLocallySavedServerNames());
		// else
		//SSHConfigDialog sshConfig = SSHConfigDialog.getInstance(mainFrame, this, readLocallySavedServerNames());
		SSHConfigDialog sshConfig = SSHConfigDialog.getInstance(mainFrame, this);
	}
	
	public SSHConfigDialog getSSHConfigDialog()
	{
		return SSHConfigDialog.getInstance(mainFrame, this);
	}
	
	/** Starts the control dialog */
	public void startSSHControlDialog()
	{
		SSHCommandControlDialog sshControl = SSHCommandControlDialog.getInstance(this);
	}
	
	/** Starts the terminal */
	public void startSSHOpsiServerTerminal()
	{
		final ConfigedMain m = this;
		
		new Thread(new Runnable() 
		{
			@Override
			public void run() 
			{
				SSHConnect ssh = new SSHConnectTerminal(m);
			}
		}).start();
	}

	private boolean confirmActionForSelectedClients(String confirmInfo)
	{
		FShowList fConfirmActionForClients= new FShowList(mainFrame, Globals.APPNAME, true,
		                                    new String[]{ 	configed.getResourceValue("buttonNO"),
		                                                   configed.getResourceValue("buttonYES") },
		                                    350, 400);

		fConfirmActionForClients.setMessage(
		    confirmInfo
		    + "\n\n" + getSelectedClientsString().replaceAll(";", "") ) ;


		fConfirmActionForClients.centerOn(Globals.mainContainer);
		fConfirmActionForClients.setAlwaysOnTop(true);
		fConfirmActionForClients.setVisible(true);


		if (fConfirmActionForClients.getResult() !=2)
			return false;

		return true;
	}

	public void shutdownSelectedClients ()
	{
		if (getSelectedClients() == null || getSelectedClients().length == 0)
			return;

		if  (
		    confirmActionForSelectedClients(
		        configed.getResourceValue("ConfigedMain.ConfirmShutdownClients.question")
		    )
		)
		{
			new ErrorListProducer(
			    configed.getResourceValue("ConfigedMain.infoShutdownClients")
			)
			{
				@Override
				protected java.util.List getErrors()
				{
					return persist.shutdownClients (getSelectedClients());
				}

			}.start();
		}
	}

	public void rebootSelectedClients ()
	{
		if (getSelectedClients() == null || getSelectedClients().length == 0)
			return;

		if  (
		    confirmActionForSelectedClients(
		        configed.getResourceValue("ConfigedMain.ConfirmRebootClients.question")
		    )
		)
		{
			new ErrorListProducer(
			    configed.getResourceValue("ConfigedMain.infoRebootClients")
			)
			{
				@Override
				protected java.util.List getErrors()
				{
					return persist.rebootClients (getSelectedClients());
				}
			}.start();
		}
	}

	public void deleteSelectedClients()
	{
		if (getSelectedClients() == null || getSelectedClients().length == 0)
			return;


		if  (!confirmActionForSelectedClients(
		            configed.getResourceValue("ConfigedMain.ConfirmDeleteClients.question")
		        )
		    )
			return;

		//logging.debug(this, "-------- Now we should do the deletion");

		//final WaitCursor waitCursor = new WaitCursor();

		for (int i = 0; i < getSelectedClients().length; i++) {
			persist.deleteClient(getSelectedClients()[i]);
		}
		
		if (getFilterClientList())
		{
			mainFrame.toggleClientFilterAction();
			//setFilterClientList(false);
		}
			

		refreshClientListKeepingGroup();
		//refreshClientList(true);

		selectionPanel.clearSelection();
		
	}


	public void callSaveGroupDialog()
	{
		java.util.List groupList = persist.getHostGroupIds();
		Collections.sort( groupList );
		Vector groupSelectionIds = new Vector(groupList);

		int i = groupSelectionIds.indexOf(groupname);

		if (i < 0)
		{
			groupSelectionIds.insertElementAt (TEMPGROUPNAME, 0);
			i = 0;
		}

		GroupnameChoice choiceDialog = new GroupnameChoice (configed.getResourceValue("ConfigedMain.saveGroup"), this, groupSelectionIds, i);

		if (choiceDialog.getResult() == 1 && !choiceDialog.getResultString().equals(""))
		{
			String newGroupName = choiceDialog.getResultString();
			IconNode newGroupNode = treeClients.makeSubgroupAt(null, newGroupName);
			if (newGroupNode == null) return;

			TreePath newGroupPath = treeClients.getPathToGROUPS().pathByAddingChild(newGroupNode);

			for (int j = 0; j < getSelectedClients().length; j++)
			{
				treeClients.copyClientTo(getSelectedClients()[j],
				                         null,  //from nowhere
				                         newGroupName,
				                         newGroupNode,
				                         newGroupPath
				                        );
			}

			treeClients.makeVisible(newGroupPath);
			treeClients.repaint();

			//persist.writeGroup(choiceDialog.getResultString(), getSelectedClients());
		}
	}

	public void callDeleteGroupDialog()
	{
		java.util.List groupList = persist.getHostGroupIds();
		Collections.sort( groupList );
		Vector groupSelectionIds = new Vector(groupList);

		int i = groupSelectionIds.indexOf(groupname);

		GroupnameChoice choiceDialog = new GroupnameChoice (configed.getResourceValue("ConfigedMain.deleteGroup"), this, groupSelectionIds, i);

		if (choiceDialog.getResult() == 1 && !choiceDialog.getResultString().equals("") )
		{
			persist.deleteGroup(choiceDialog.getResultString());
			persist.hostGroupsRequestRefresh();
			if( clientSelectionDialog != null )
				clientSelectionDialog.refreshGroups();
		}
	}

	public void setGroupname ( String name )
	{
		if (name == null || name.equals(""))
			groupname = TEMPGROUPNAME;
		else
			groupname = name;
	}

	public void callClientSelectionDialog()
	{
		initSavedSearchesDialog();

		/*
		if( clientSelectionDialog == null || clientSelectionDialog.isReloadRequested())
		{
			if (clientSelectionDialog != null)
			{
				clientSelectionDialog.leave();
			}
				
			clientSelectionDialog = new ClientSelectionDialog(this, selectionPanel, savedSearchesDialog);
	}
		*/

		if( clientSelectionDialog == null)
		{
			clientSelectionDialog = new ClientSelectionDialog(this, selectionPanel, savedSearchesDialog);
		}


		if (configed.isApplet)
		{
			clientSelectionDialog.setVisible(true);
			clientSelectionDialog.setVisible(false);
		}
		//GroupsManager.getInstance(this).setVisible(true);
		clientSelectionDialog.centerOn(Globals.mainContainer);
		clientSelectionDialog.setVisible(true);

		/*
		try 
		{
			clientSelectionDialog.setAlwaysOnTop(true);
	}
		catch (SecurityException ex)
		{
			logging.debug(this, "callClientSelectionDialog " + ex); 
	}
		*/
	}

	
	private void setSelectedClientsOnPanel(String[] selected)
	{
		if (selected != null)
		{
			logging.info(this, " setSelectedClientsOnPanel clients count "
			             +  selected.length);
		}
		else 
			logging.info(this, " setSelectedClientsOnPanel selected null");
			
		selectionPanel.removeListSelectionListener(this);
		selectionPanel.setSelectedValues(selected);
		setSelectedClientsArray(selected);
		selectionPanel.addListSelectionListener(this);
	}

	private void setSelectedClientsCollectionOnPanel(Collection<String> selected)
	{
		if (selected != null)
		{
			logging.info(this, "setSelectedClientsCollectionOnPanel clients count "
			             +  selected.size());
		}
		// try selectionPanel.removeListSelectionListener(this);
		selectionPanel.setSelectedValues(selected);
		
		logging.info(this, "setSelectedClientsCollectionOnPanel   selectionPanel.getSelectedValues().size() "
			+  selectionPanel.getSelectedValues().size());
			
			
		if (selected == null)
			setSelectedClientsArray(new String[0] );
		else
			setSelectedClientsArray(selected.toArray(new String[selected.size()]));
		// try selectionPanel.addListSelectionListener(this);
	}

	private void setSelectedClientsCollectionOnPanel(Collection<String> selected, boolean renewFilter)
	{
		boolean saveFilterClientList = false;

		if (renewFilter)
		{
			saveFilterClientList = getFilterClientList();
			if (saveFilterClientList)
				setFilterClientList(false);
		}

		setSelectedClientsCollectionOnPanel(selected);

		if (renewFilter)
		{
			if (saveFilterClientList)
				setFilterClientList(true);
		}
	}

	public void selectClientsByFailedAtSomeTimeAgo(String arg)
	{
		de.uib.configed.clientselection.SelectionManager manager
		= new de.uib.configed.clientselection.SelectionManager(null);

		if (arg == null || arg.equals(""))
		{
			manager.setSearch( de.uib.opsidatamodel.SavedSearches.SEARCHfailedAtAnyTimeS );
		}
		else
		{
			String timeAgo = DateExtendedByVars.dayValueOf(arg);
			String test = String.format(de.uib.opsidatamodel.SavedSearches.SEARCHfailedByTimeS, timeAgo);

			logging.info(this, "selectClientsByFailedAtSomeTimeAgo  test " + test);
			manager.setSearch( test );
		}

		java.util.List result = manager.selectClients();

		//logging.info(this, "selected size: " + result.size() );
		setSelectedClientsCollectionOnPanel( result, true );

	}


	public void selectClientsNotCurrentProductInstalled(java.util.List<String> selectedProducts)
	{
		logging.debug(this, "selectClientsNotCurrentProductInstalled, products " + selectedProducts);
		if (selectedProducts == null || selectedProducts.size() != 1)
			return;

		String productId = selectedProducts.get(0);
		String productVersion =  persist.getProductVersion(productId);
		String packageVersion =  persist.getProductPackageVersion(productId);

		logging.debug(this, "selectClientsNotCurrentProductInstalled product " + productId + ", " + productVersion + ", " + packageVersion);

		java.util.List<String> clientsToSelect = persist.getClientsWithOtherProductVersion(productId,productVersion, packageVersion);
		
		logging.info(this, "selectClientsNotCurrentProductInstalled clients found globally " + clientsToSelect.size());
		
		clientsToSelect.retainAll( producePcListForDepots( getSelectedDepots() ).keySet() );
			
		logging.info(this, "selectClientsNotCurrentProductInstalled clients found for selected depots " + clientsToSelect.size());

		//persist.retrieveProductproperties(clientsToSelect);

		setSelectedClientsCollectionOnPanel(clientsToSelect, true);
	}



	public void selectClientsWithFailedProduct(java.util.List<String> selectedProducts)
	{
		logging.debug(this, "selectClientsWithFailedProduct, products " + selectedProducts);
		if (selectedProducts == null || selectedProducts.size() != 1)
			return;

		de.uib.configed.clientselection.SelectionManager manager
		= new de.uib.configed.clientselection.SelectionManager(null);

		String test = String.format(
		                  de.uib.opsidatamodel.SavedSearches.SEARCHfailedProduct,
		                  selectedProducts.get(0)
		              );

		//logging.info(this, "selectClientsWithFailedProduct  test " + test);
		manager.setSearch( test );

		java.util.List result = manager.selectClients();

		logging.info(this, "selected: " + result);
		setSelectedClientsCollectionOnPanel( result, true );
	}





	public void setClientGroup()
	{
		boolean wasFiltered = false;
		//String[] saveSelectedClients = getSelectedClients();
		//logging.info(this,
		//	"save selectedclients " + logging.getStrings(saveSelectedClients));

		if (filterClientList)  // no group selection on the filtered list
		{
			setFilterClientList(false);
			wasFiltered = true;
		}

		//logging.debug(this, " setClientGroup() ");
		selectionPanel.clearSelection();

		//logging.debug(this, " depotsList.getSelectedValues() " + depotsList.getSelectedValues());
		//for (int j=0; j < depotsList.getSelectedValues().length ; j++)
		//	logging.debug(this, "" + depotsList.getSelectedValues()[j]);

		Map<String, Boolean> pclist = producePcListForDepots(getSelectedDepots());
		logging.debug(this, "setClientGroup pclist " + pclist);

		if (pclist != null)
		{
			Object[] pcs = pclist.entrySet().toArray();
			TreeSet<String> selectedList = new TreeSet<String>();
			for (int i = 0; i < pcs.length; i++)
			{
				Map.Entry ob = (Map.Entry) pcs[i];

				//logging.debug(this, " ----------- key " + i + ": " + ob.getKey() +  " value " +ob.getValue() );

				if ( (Boolean) ob.getValue() )
				{
					//logging.debug(this, "selected " + ob.getKey() + "  pclist index " + i + "  modelIndex  " +  selectionPanel.convertRowIndexToModel(i) + " viewIndex " + selectionPanel.convertRowIndexToView(i));
					//lsm.addSelectionInterval(i, i);
					selectedList.add((String)ob.getKey());
					//lsm.addSelectionInterval(selectionPanel.convertRowIndexToView(i), selectionPanel.convertRowIndexToView(i));
					//lsm.addSelectionInterval (selectionPanel.getModelToSortedView()[i], selectionPanel.getModelToSortedView()[i]);
				}
			}

			logging.debug(this, "set selected values in setClientGroup " + selectedList);
			setSelectedClientsCollectionOnPanel(selectedList, true);
		}

		if (wasFiltered)
		{
			filterClientList = true;

			//setSelectedClientsOnPanel(saveSelectedClients);

			setRebuiltPclistTableModel();
			//setFilterClientList(true); triggered recursion!!
		}

	}

	/*
	public void loadClientGroup (String groupId,  String productId, String installationStatus, String actionRequest, String productVersion, String packageVersion, Map hwFilter)
	//  retrieves the clients fitting to the conjunction of criteria
	//  deselects everything if all strings are null or empty
	{
		String actionRequest_querystring = "";
		if (!actionRequest.equals(""))
			actionRequest_querystring = actionRequest; //ActionRequest.getIDString( ActionRequest.getTypeFromDisplayString (actionRequest, ardDefault)) ;

		//Map pclist = persist.getHostInfoCollections().getPcListForDepots(getSelectedDepots());
		//logging.debug(this, " ---------- pclist before makeClientSelection " + pclist);
		persist.makeClientSelection("", depotsList.getSelectedValues(),
		                            groupId, productId,
		                            installationStatus, actionRequest_querystring,
		                            productVersion, packageVersion, hwFilter);

		setClientGroup();

		checkErrorList();
	}
	*/


	//interface LogEventObserver
	public void logEventOccurred(LogEvent event)
	{
		boolean found = false;

		if (allFrames == null)
			return;

		for (JFrame f : allFrames)
		{
			logging.debug(this, "log event occurred in frame f , is focused " + f.isFocused() + " " + f);
			if (f !=null)// && f.isFocused())
			{
				logging.checkErrorList(f);
				found = true;
				break;
			}

		}


		if (!found)
		{
			logging.checkErrorList(mainFrame);
		}

		/*
		if (licencesFrame != null && licencesFrame.isFocused())
		{
			logging.checkErrorList(licencesFrame);
	}
		else 
		{
			logging.checkErrorList(mainFrame);
	}
		*/

	}



	protected void saveConfigs()
	{
		// PersistenceController.getPersistenceController().selectedPcConfigurationWriteBack();
		logging.info(this, " --------  saveConfigs() ");

		updateProductStates();
		logging.info(this, "saveConfigs: collectChangedLocalbootStates " + collectChangedLocalbootStates);
		//setAllProductStates();
		//setAllProductActions();
		logging.info(this, " ------- we should now start working on the update collection of size  " + updateCollection.size() );
		updateCollection.doCall();
		checkErrorList();

		clearUpdateCollectionAndTell();
	}

	private void cancelUpdateCollection()
	{
		updateCollection.cancel();
	}

	private void clearUpdateCollectionAndTell()
	{
		logging.info(this, " --- we clear the update collection " + updateCollection.getClass());
		updateCollection.clearElements();
	}


	protected void checkErrorList()
	{
		logging.checkErrorList(mainFrame);
	}


	protected boolean checkSavedLicencesFrame()
	{
		if (allControlMultiTablePanels == null)
			return true;

		boolean change = false;
		boolean result = false;
		Iterator iter = allControlMultiTablePanels.iterator();
		while (!change && iter.hasNext())
		{
			ControlMultiTablePanel cmtp = (ControlMultiTablePanel) iter.next();
			if (cmtp != null)
			{
				Iterator iterP = cmtp.tablePanes.iterator();
				while (!change && iterP.hasNext())
				{
					PanelGenEditTable p = (PanelGenEditTable) iterP.next();
					change = p.isDataChanged();
				}
			}
		}

		if (change)
		{
			int returnedOption = JOptionPane.NO_OPTION;
			returnedOption = JOptionPane.showOptionDialog(	Globals.mainContainer,
			                 configed.getResourceValue("ConfigedMain.Licences.AllowLeaveApp"),
			                 configed.getResourceValue("ConfigedMain.Licences.AllowLeaveApp.title"),
			                 JOptionPane.YES_NO_OPTION,
			                 JOptionPane.QUESTION_MESSAGE,
			                 null, null, null);

			if (returnedOption == JOptionPane.YES_OPTION)
			{
				result = true;
			}

		}
		else
			result = true;

		return result;

	}

	public boolean closeInstance(boolean checkdirty)
	{
		logging.info(this, "start closing instance, checkdirty "  + checkdirty);
		
		boolean result = true;
		
		if (
			!FStartWakeOnLan.runningInstances.isEmpty()
		)
		{
			result = FStartWakeOnLan.runningInstances.askStop();
		}
		if (!result)
			return false;
		

		if (checkdirty)
		{
			int closeCheckResult = checkClose();
			result = (closeCheckResult < 2);
			if (!result)
				return result;

			if (closeCheckResult == 0) checkSaveAll(false);
		}

		if (mainFrame != null)
		{
			if (configed.isApplet)
				mainFrame.clear();

			mainFrame.setVisible(false);
			mainFrame.dispose();
			mainFrame = null;
		}

		if (dpass != null)
		{
			dpass.setVisible(false);
			dpass.dispose();
		}


		if (!checkSavedLicencesFrame())
		{
			licencesFrame.setVisible(true);
			result = false;
		}
		
		logging.info(this, "close instance result "  + result);

		return result;
	}


	public void finishApp(boolean checkdirty, int exitcode)
	{
		if (
			closeInstance(checkdirty)
		)
			configed.endApp(exitcode);
	}

}