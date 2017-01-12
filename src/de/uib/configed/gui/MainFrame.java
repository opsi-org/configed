package de.uib.configed.gui;

/**
 * configed - configuration editor for client work stations in opsi
 * (open pc server integration) www.opsi.org
 *
 * Copyright (C) 2000-2016 uib.de
 *
 * This program is free software; you can redistribute it 
 * and / or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 */

import de.uib.configed.*;

//import de.uib.opsidatamodel.PersistenceController;  // needed for update_version_display
import java.awt.*;
import java.awt.event.*;
import java.awt.im.*;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableModel;
import javax.swing.JTable;
import javax.swing.ListCellRenderer;
import javax.swing.DefaultListCellRenderer;
import javax.swing.table.*;
import javax.swing.tree.*;

import de.uib.configed.tree.*;
import de.uib.configed.type.*;
import de.uib.messages.*;

import javax.swing.border.*;

import java.net.URL;

import javax.swing.event.*;

import java.util.*;

import de.uib.utilities.pdf.DocumentToPdf;
import de.uib.utilities.thread.WaitCursor;
import de.uib.configed.gui.productpage.*;
import de.uib.configed.gui.hwinfopage.*;
import de.uib.utilities.swing.*;
import de.uib.utilities.table.gui.*;
import de.uib.utilities.selectionpanel.*;
import de.uib.utilities.datapanel.*;
import de.uib.utilities.logging.*;
import de.uib.utilities.observer.RunningInstancesObserver;


import de.uib.opsicommand.sshcommand.*;

//import de.uib.utilities.StringvaluedObject;


public class MainFrame extends JFrame
			implements WindowListener, KeyListener, ActionListener, RunningInstancesObserver<JDialog>
{

	protected int dividerLocationCentralPane = 300;
	protected int minHSizeTreePanel = 150;

	public final static int fwidth = 800;
	public final static int fheight =  600;

	final int fwidth_lefthanded = 420;
	final int fwidth_righthanded = fwidth - fwidth_lefthanded;
	final int splitterLeftRight = 15;

	final int dividerLocationClientTreeMultidepot = 200;
	final int dividerLocationClientTreeSingledepot = 50;

	final int labelproductselection_width =  200;
	final int labelproductselection_height = 40;
	final int line_height = 23;




	//final int widthColumnServer = 110; //130;

	//protected String oldDescription;
	//protected String oldInventoryNumber;
	//protected String oldOneTimePassword;
	protected String oldNotes;
	//protected String oldMacAddress;


	protected HashMap<String, String> changedClientInfo = new HashMap();

	protected DocumentToPdf tableToPDF;

	ConfigedMain main;

	public SizeListeningPanel allPane;

	//menu system

	public static final String ITEM_ADD_CLIENT
	= "add client";
	public static final String ITEM_DELETE_CLIENT
	= "remove client";
	public static final String ITEM_FREE_LICENCES
	= "free licences for client";

	Map<String, java.util.List<JMenuItem>> menuItemsHost;

	//Map<String, java.util.List<JMenuItem>> menuItemsOpsiclientdExtraEvent = new HashMap<String, java.util.List<JMenuItem>>();



	JMenuBar jMenuBar1 = new JMenuBar();

	JMenu jMenuFile;
	JMenuItem jMenuFileExit;
	JMenuItem jMenuFileSaveConfigurations;
	JMenuItem jMenuFileReload;
	JMenuItem jMenuFileLanguage;


	JMenu jMenuClients = new JMenu();
	JMenuItem jMenuResetProductOnClient = new JMenuItem();
	JMenuItem jMenuAddClient = new JMenuItem();
	JMenuItem jMenuDeleteClient = new JMenuItem();
	JMenuItem jMenuFreeLicences = new JMenuItem();
	JMenuItem jMenuDeletePackageCaches = new JMenuItem();
	JMenu jMenuWakeOnLan;
	//JMenu jMenuScheduledWOL;
	JMenuItem jMenuDirectWOL = new JMenuItem();
	JMenuItem jMenuNewScheduledWOL = new JMenuItem();
	JMenuItem jMenuShowScheduledWOL = new JMenuItem();
	JMenuItem jMenuOpsiClientdEvent;
	JMenuItem jMenuShowPopupMessage = new JMenuItem();
	JMenuItem jMenuRequestSessionInfo = new JMenuItem();
	JMenuItem jMenuShutdownClient = new JMenuItem();
	JMenuItem jMenuRebootClient = new JMenuItem();
	JMenuItem jMenuChangeDepot = new JMenuItem();
	JMenuItem jMenuChangeClientID = new JMenuItem();

	JMenu jMenuServer = new JMenu();
	JMenuItem jMenuRemoteTerminal = new JMenuItem();
	JMenuItem jMenuSSHConfig = new JMenuItem();
	JMenuItem jMenuSSHConnection = new JMenuItem();
	JMenuItem jMenuSSHCommandControl = new JMenuItem();
	
	LinkedHashMap<String, Integer> labelledDelays;

	Map<String, String> searchedTimeSpans;
	Map<String, String> searchedTimeSpansText;

	//JCheckBoxMenuItem jCheckBoxMenuItem_displayClientList = new JCheckBoxMenuItem();
	JCheckBoxMenuItem jCheckBoxMenuItem_showCreatedColumn = new JCheckBoxMenuItem();
	JCheckBoxMenuItem jCheckBoxMenuItem_showWANactiveColumn = new JCheckBoxMenuItem();
	JCheckBoxMenuItem jCheckBoxMenuItem_showIPAddressColumn = new JCheckBoxMenuItem();
	JCheckBoxMenuItem jCheckBoxMenuItem_showInventoryNumberColumn = new JCheckBoxMenuItem();
	JCheckBoxMenuItem jCheckBoxMenuItem_showHardwareAddressColumn = new JCheckBoxMenuItem();
	JCheckBoxMenuItem jCheckBoxMenuItem_showSessionInfoColumn = new JCheckBoxMenuItem();
	JCheckBoxMenuItem jCheckBoxMenuItem_showUefiBoot = new JCheckBoxMenuItem();
	JCheckBoxMenuItem jCheckBoxMenuItem_showDepotColumn = new JCheckBoxMenuItem();
	JMenuItem jMenuRemoteControl = new JMenuItem();

	JMenuItem[] clientMenuItemsDependOnSelectionCount = new JMenuItem[]
	        {
	            jMenuResetProductOnClient,
	            jMenuAddClient,
	            jMenuDeleteClient,
	            jMenuFreeLicences,
	            //jMenuWakeOnLan,
	            //jMenuShowPopupMessage,
	            //jMenuRequestSessionInfo,
	            //jMenuShutdownClient,
	            //jMenuRebootClient,
	            //jMenuOpsiClientdEvent,
	            jMenuChangeDepot,
	            jMenuChangeClientID,
	            //jMenuRemoteControl
	        };

	JMenu jMenuClientselection = new JMenu();
	JMenuItem jMenuClientselectionGetGroup = new JMenuItem();
	JMenuItem jMenuClientselectionGetSavedSearch = new JMenuItem();
	JMenuItem jMenuClientselectionNotCurrentProduct = new JMenuItem();
	JMenuItem jMenuClientselectionFailedProduct = new JMenuItem();
	JMenu jMenuClientselectionFailedInPeriod = new JMenu();
	//JMenuItem jMenuClientselectionSaveGroup = new JMenuItem();
	//JMenuItem jMenuClientselectionDeleteGroup = new JMenuItem();
	JMenuItem jMenuClientselectionDeselect = new JMenuItem();
	JCheckBoxMenuItem jMenuClientselectionToggleClientFilter = new JCheckBoxMenuItem();


	JMenu jMenuFrames = new JMenu();
	JMenuItem jMenuFrameLicences = new JMenuItem();
	JMenuItem jMenuFrameWorkOnProducts = new JMenuItem();
	JMenuItem jMenuFrameWorkOnGroups = new JMenuItem();
	JMenuItem jMenuFrameShowDialogs = new JMenuItem();

	JMenu jMenuHelp = new JMenu();
	JMenuItem jMenuHelpSupport = new JMenuItem();
	JMenuItem jMenuHelpDoc = new JMenuItem();
	JMenuItem jMenuHelpDocSpecial = new JMenuItem();
	JMenuItem jMenuHelpForum = new JMenuItem();
	JMenuItem jMenuHelpInternalConfiguration = new JMenuItem();
	JMenuItem jMenuHelpAbout = new JMenuItem();
	JMenuItem jMenuHelpOpsiVersion = new JMenuItem();
	JMenuItem jMenuHelpOpsiModuleInformation = new JMenuItem();
	JMenuItem jMenuHelpServerInfoPage = new JMenuItem();
	JMenu jMenuHelpLoglevel = new JMenu();
	
	JRadioButtonMenuItem[] rbLoglevelItems = new JRadioButtonMenuItem[logging.LEVEL_DONT_SHOW_IT];

	JPopupMenu popupClients = new JPopupMenu();
	JMenuItemFormatted popupResetProductOnClient = new JMenuItemFormatted();
	JMenuItemFormatted popupAddClient = new JMenuItemFormatted();
	JMenuItemFormatted popupDeleteClient = new JMenuItemFormatted();
	JMenuItemFormatted popupFreeLicences = new JMenuItemFormatted();
	JMenuItemFormatted popupDeletePackageCaches = new JMenuItemFormatted();
	JMenu popupWakeOnLan = new JMenu(
	                           configed.getResourceValue("MainFrame.jMenuWakeOnLan")
	                       );
	JMenuItemFormatted popupWakeOnLanDirect  = new JMenuItemFormatted();
	JMenuItemFormatted popupWakeOnLanScheduler  = new JMenuItemFormatted();


	//JMenu subOpsiClientdEvent = new JMenu();
	JMenu menuPopupOpsiClientdEvent = new JMenu(
	                                      configed.getResourceValue("MainFrame.jMenuOpsiClientdEvent")
	                                  );
	JMenuItemFormatted popupShowPopupMessage = new JMenuItemFormatted();
	JMenuItemFormatted popupRequestSessionInfo = new JMenuItemFormatted();
	JMenuItemFormatted popupShutdownClient = new JMenuItemFormatted();
	JMenuItemFormatted popupRebootClient = new JMenuItemFormatted();
	JMenuItemFormatted popupChangeDepot = new JMenuItemFormatted();
	JMenuItemFormatted popupChangeClientID = new JMenuItemFormatted();
	JMenuItemFormatted popupRemoteControl = new JMenuItemFormatted();

	JMenuItemFormatted[] clientPopupsDependOnSelectionCount = new JMenuItemFormatted[]
	        {
	            popupResetProductOnClient,
	            popupAddClient,
	            popupDeleteClient,
	            popupFreeLicences,
	            //popupWakeOnLan,
	            popupShowPopupMessage,
	            popupRequestSessionInfo,
	            popupDeletePackageCaches,
	            popupRebootClient,
	            popupShutdownClient,
	            //menuPopupOpsiClientdEvent,
	            popupChangeDepot,
	            popupChangeClientID,
	            popupRemoteControl
	        };


	//JCheckBoxMenuItem popupDisplayClientList = new JCheckBoxMenuItem();
	JCheckBoxMenuItem popupShowCreatedColumn = new JCheckBoxMenuItem();
	JCheckBoxMenuItem popupShowWANactiveColumn = new JCheckBoxMenuItem();
	JCheckBoxMenuItem popupShowIPAddressColumn = new JCheckBoxMenuItem();
	JCheckBoxMenuItem popupShowHardwareAddressColumn = new JCheckBoxMenuItem();
	JCheckBoxMenuItem popupShowSessionInfoColumn = new JCheckBoxMenuItem();
	JCheckBoxMenuItem popupShowInventoryNumberColumn = new JCheckBoxMenuItem();
	JCheckBoxMenuItem popupShowUefiBoot = new JCheckBoxMenuItem();
	JCheckBoxMenuItem popupShowDepotColumn = new JCheckBoxMenuItem();

	JMenuItemFormatted popupSelectionGetGroup = new JMenuItemFormatted();
	JMenuItemFormatted popupSelectionGetSavedSearch = new JMenuItemFormatted();
	//JMenuItemFormatted popupSelectionSaveGroup = new JMenuItemFormatted();
	//JMenuItemFormatted popupSelectionDeleteGroup = new JMenuItemFormatted();
	JMenuItemFormatted popupSelectionDeselect = new JMenuItemFormatted();
	JCheckBoxMenuItem popupSelectionToggleClientFilter = new JCheckBoxMenuItem();

	JMenuItemFormatted popupRebuildClientList = new JMenuItemFormatted(
	            configed.getResourceValue("PopupMenuTrait.reload"),
	            de.uib.configed.Globals.createImageIcon("images/reload16.png", ""));
	JMenuItemFormatted popupCreatePdf = new JMenuItemFormatted(configed.getResourceValue("FGeneralDialog.pdf"),
	                                    de.uib.configed.Globals.createImageIcon("images/acrobat_reader16.png", ""));


	JPopupMenu popupLocalbootProducts = new JPopupMenu();
	JPopupMenu popupNetbootProducts = new JPopupMenu();
	JPopupMenu popupHardwareAudit = new JPopupMenu();
	JPopupMenu popupSoftwareAudig = new JPopupMenu();
	JPopupMenu popupNetworkConfig = new JPopupMenu();
	JPopupMenu popupLogfiles = new JPopupMenu();


	JPopupMenu popupDepotList = new JPopupMenu();
	JMenuItemFormatted popupCommitChangedDepotSelection = new JMenuItemFormatted();
	JMenuItemFormatted popupCancelChangedDepotSelection = new JMenuItemFormatted();

	JPanel iconBarPane;

	JPanel iconPane0;

	JPanel iconPaneTargets;
	JButton jButtonServerConfiguration;
	JButton jButtonDepotsConfiguration;
	JButton jButtonClientsConfiguration;
	JButton jButtonWorkOnGroups;
	JButton jButtonWorkOnProducts;

	JPanel iconPaneExtraFrames;

	JButton jButtonLicences;

	JPanel iconPane1;
	//JButton buttonWindowStack; may be it will get a revival at a different place
	IconButton iconButtonReload;
	IconButton iconButtonReloadLicenses;
	IconButton iconButtonNewClient;
	IconButton iconButtonSaveGroup;/*gibts nicht**/
	IconButton iconButtonSetGroup;
	IconButton iconButtonSaveConfiguration;
	IconButton iconButtonCancelChanges;
	IconButton iconButtonToggleClientFilter;
	public IconButton iconButtonReachableInfo;
	public IconButton iconButtonSessionInfo;

	JPanel proceeding;

	/*
	protected IconButton buttonCommitChangedDepotSelection;
	protected IconButton buttonCancelChangedDepotSelection;
	*/

	protected JButton buttonSelectDepotsWithEqualProperties;
	protected JButton buttonSelectDepotsAll;


	BorderLayout borderLayout1 = new BorderLayout();
	GroupLayout contentLayout;
	JTabbedPane jTabbedPaneConfigPanes = new JTabbedPane(); // new ClippedTitleTabbedPane();
	JSplitPane panel_Clientselection; // = new JSplitPane();


	private HostsStatusPanel statusPane;


	public PanelGroupedProductSettings panel_LocalbootProductsettings;
	public PanelProductSettings panel_NetbootProductsettings;
	public PanelHostConfig panel_HostConfig;
	public PanelHostProperties panel_HostProperties;
	public PanelProductProperties panel_ProductProperties;

	//PanelJSONData showHardwareLog_version1 = new PanelJSONData();
	de.uib.configed.gui.hwinfopage.PanelHWInfo showHardwareLog_version2;
	de.uib.configed.gui.hwinfopage.PanelHWInfo showHardwareLog_NotFound;
	JLabel labelNoHardware;
	de.uib.configed.gui.hwinfopage.PanelHWInfo showHardwareLog = showHardwareLog_NotFound;
	JLabel labelNoSoftware;
	Panelreinst panelReinstmgr = new Panelreinst();

	PanelSWInfo showHSoftwareLog_Available;
	JPanel showSoftwareLog_NotFound;
	JPanel showSoftwareLog;

	PanelTabbedDocuments showLogfiles;

	JPanel jPanel_Schalterstellung;

	//protected ProductInfoPane  localbootProductInfo;
	//protected ProductInfoPane  netbootProductInfo;

	//EditMapPanel localboot_productPropertiesPanel;
	//EditMapPanel netboot_productPropertiesPanel;

	JTextField jTextFieldConfigdir = new JTextField();
	JButton jButtonFileChooserConfigdir = new JButton();
	JPanel jPanel3 = new JPanel();
	JLabel jLabel_Clientname = new JLabel();
	JCheckBox jCheckBoxSorted = new JCheckBox();
	JButton jButtonSaveList = new JButton();
	JPanel jPanel_ButtonSaveList = new JPanel();
	String[] options = new String[]{"off","on","setup"};
	JComboBox jComboBoxProductValues = new JComboBox(options);

	JLabel jLabel_property = new JLabel();
	ButtonGroup buttonGroupRequired = new ButtonGroup();
	JRadioButton jRadioRequiredAll = new JRadioButton();
	JRadioButton jRadioRequiredOff = new JRadioButton();

	static private boolean inRefresh = false;
	static private boolean settingSchalter = false;
	JButton jBtnAllOff = new JButton();
	//JButton jBtnCopyTemplate = new JButton();

	//JButton jBtnRefresh = new JButton();

	JTableSelectionPanel panelClientlist;
	boolean shiftpressed = false;
	//TableColumnModel clientlistColumnModel;
	JLabel jLabel_Hostinfos = new JLabel();
	FlowLayout flowLayout1 = new FlowLayout();
	JLabel jLabelPath = new JLabel();
	private boolean starting = true;
	//JLabel jLabelDepot = new JLabel(configed.getResourceValue("MainFrame.jLabelDepot"));//"Depot(s): ");
	JTextArea jFieldInDepot;
	JLabel labelHost;
	JLabel labelHostID;
	JCheckBox cbUefiBoot;
	JCheckBox cbWANConfig;
	// JCheckBox cbInstallByShutdown;
	JLabel jLabel_InstallByShutdown;
	JButton btnAktivateInstallByShutdown;
	JButton btnDeaktivateInstallByShutdown;

	JTextEditorField jTextFieldDescription;
	JTextEditorField jTextFieldInventoryNumber;
	JTextArea jTextAreaNotes;
	JTextEditorField macAddressField;
	JTextEditorField jTextFieldOneTimePassword;
	JScrollPane scrollpaneNotes;


	JPopupMenu jPopupMenu = new JPopupMenu();

	protected FShowList fListSelectedClients;
	//protected FTextArea fAskSaveChangedText;


	JPanel jPanelChooseDomain;
	//JTabbedPane jTabbedPaneConfigPanes; // cf. above
	JPanel panelTreeClientSelection;
	JPanel jPanelProductsConfig;

	DepotsList depotslist;
	boolean multidepot = false;
	JScrollPane scrollpaneDepotslist;

	ClientTree treeClients;
	JScrollPane scrollpaneTreeClients;


	JPanel clientPane;
	Containership csClientPane;

	int splitterPanelClientSelection = 0;
	int prefClientPaneW = 100;
	int clientPaneW;

	//ComponentListener clientPaneComponentListener;

	public Container baseContainer;

	class GlassPane extends JComponent
	{
		GlassPane()
		{
			super();
			logging.debug(this, "glass pane initialized");
			setVisible(true);
			setOpaque(true);
			addKeyListener(new KeyAdapter(){
				               public void keyTyped(KeyEvent e)
				               {
					               logging.debug(this, "key typed on glass pane");
				               }
			               });
			addMouseListener(new MouseAdapter(){
				                 public void mouseClicked(MouseEvent e)
				                 {
					                 logging.info(this, "mouse on glass pane");
				                 }
			                 });

		}


		public void paintComponent(Graphics g)
		{
			((Graphics2D) g).setComposite(
			    AlphaComposite.getInstance(AlphaComposite.SRC_OVER, (float) 0.5)
			);

			g.setColor(new Color(230,230,250));
			g.fillRect(0,0,getWidth(), getHeight());
		}



	}

	GlassPane glass;

	public MainFrame(  JApplet appletHost, ConfigedMain main,  JTableSelectionPanel selectionPanel, DepotsList depotsList, ClientTree treeClients, boolean multidepot)
	{
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE); // we handle it in the window listener method

		this.multidepot = multidepot;

		panelClientlist = selectionPanel;
		//selectionPanel.setPreferredSize(new Dimension(fwidth_lefthanded, fheight));
		//clientlistColumnModel = selectionPanel.getColumnModel();

		this.treeClients = treeClients;

		this.depotslist = depotsList;

		this.main = main;


		if (!configed.isApplet)
		{
			baseContainer = this.getContentPane();

		}
		else
		{
			baseContainer = appletHost.getContentPane();
		}

		de.uib.utilities.Globals.masterFrame = baseContainer;

		glass = new GlassPane();

		guiInit(appletHost);
		initData();

		UIManager.put("OptionPane.yesButtonText", configed.getResourceValue("UIManager.yesButtonText"));
		UIManager.put("OptionPane.noButtonText", configed.getResourceValue("UIManager.noButtonText"));
		UIManager.put("OptionPane.cancelButtonText", configed.getResourceValue("UIManager.cancelButtonText"));

		FEditObject.runningInstances.addObserver( (RunningInstancesObserver) this);



	}
	
	@Override
	public void setVisible(boolean b)
	{
		logging.info(this, "setVisible " + b);
		super.setVisible(b);
	}

	private void initData()
	{
		statusPane.updateValues(0, null, null, null);
	}

	public HostsStatusInfo getHostsStatusInfo()
	{
		return statusPane;
	}

	public Container retrieveBasePane()
	//for setting cursor
	{
		return baseContainer;
	}

	public void initFirstSplitPane()
	{
		panel_Clientselection.setDividerLocation( 0.8 );
	}



	/*
	private class SizeListeningScrollPane extends JScrollPane implements ComponentListener
{
			Component componentToAdapt;
			int adaptWidth;
			int adaptHeight;
			
			SizeListeningScrollPane(Component view, Component toAdapt, int adaptWidth, int adaptHeight)
			{
				super(view);
				componentToAdapt = toAdapt;
				this.adaptWidth = adaptWidth;
				this.adaptHeight = adaptHeight;
				addComponentListener(this);
			}
			//ComponentListener implementation
			
			public void  componentHidden(ComponentEvent e)
			{}
			
			public void  componentMoved(ComponentEvent e)
			{}
			
			public void  componentResized(ComponentEvent e)
			{
				logging.debug(this, "repairSizes");
				int newWidth = componentToAdapt.getWidth(); //old width
				int newHeight = componentToAdapt.getHeight(); //old height
				if (adaptWidth > 0)
					newWidth = getWidth() + adaptWidth;
				
				if (adaptHeight > 0)
					newHeight = getHeight() + adaptHeight;
				
				//System.out.println (" -- resizing to " + newWidth + ", " + newHeight);
				componentToAdapt.setPreferredSize(new Dimension(newWidth, newHeight));
			}
			
			public void  componentShown(ComponentEvent e)
			{
			}
}
	*/

	public class SizeListeningPanel extends JPanel implements ComponentListener
	{
		SizeListeningPanel()
		{
			addComponentListener(this);
		}
		//ComponentListener implementation

		public void  componentHidden(ComponentEvent e)
		{}

		public void  componentMoved(ComponentEvent e)
		{}


		private void moveDivider1(JSplitPane splitpane, JComponent rightpane,
		                          int min_right_width, int min_left_width, int max_right_width)
		{
			if (splitpane == null || rightpane == null)
				return;

			int dividerLocation = splitpane.getDividerLocation();
			//dividerLocation initially was (fwidth_lefthanded + splitterLeftRight);
			int sizeOfRightPanel = (int) rightpane.getSize().getWidth();
			int missingSpace = min_right_width - sizeOfRightPanel;
			if (missingSpace > 0 && dividerLocation > min_left_width)
			{
				splitpane.setDividerLocation(dividerLocation - missingSpace);
				//System.out.println (" reset divider location ");
			}

			//logging.info(this, "moveDivider1 ");

			if (sizeOfRightPanel > max_right_width)
			{
				splitpane.setDividerLocation(dividerLocation + (sizeOfRightPanel - max_right_width));
			}


		}

		private void moveDivider2(JSplitPane splitpane, JComponent rightpane, int min_left_width)
		{
			if (splitpane == null || rightpane == null)
				return;

			int completeWidth = (int) splitpane.getSize().getWidth();
			int sizeOfRightPanel = (int) rightpane.getSize().getWidth();
			int preferred_right_width = (int) rightpane.getPreferredSize().getWidth();

			//logging.info(this, "moveDivider2 preferred_right_width " + preferred_right_width);

			int dividerabslocation = completeWidth - preferred_right_width - splitterLeftRight ;

			if (dividerabslocation < min_left_width)
				dividerabslocation = min_left_width;

			if (dividerabslocation > completeWidth - 20)
				dividerabslocation = completeWidth - 20;

			// result < 0 splitpane resets itself
			splitpane.setDividerLocation(dividerabslocation);
		}


		public void  componentResized(ComponentEvent e)
		{
			//logging.info(this, "componentResized");

			try{
				repairSizes();
			}
			catch(Exception ex)
			{
				logging.info(this, "componentResized " + ex);
			}
			logging.debug(this, "componentResized ready");

		}

		public void  componentShown(ComponentEvent e)
		{
		}

		public void repairSizes()
		{
			//repair sizes when the frame is resized

			if (panel_Clientselection == null)
				return;

			splitterPanelClientSelection = panel_Clientselection.getSize().width - clientPaneW;

			//clientPane.removeComponentListener(clientPaneComponentListener);
			//panel_Clientselection.setDividerLocation(splitterPanelClientSelection);

			moveDivider1(panel_Clientselection, clientPane, (int)(fwidth_righthanded * 0.2), 200, (int)(fwidth_righthanded*1.5));

			//clientPane.addComponentListener(clientPaneComponentListener);

			//moveDivider2(panel_LocalbootProductsettings, localbootProductInfo, 200);

			//moveDivider2(panel_NetbootProductsettings, netbootProductInfo, 200);

			//moveDivider(panel_LocalbootProductsettings, localbootProductInfo, (int)fwidth_righthanded/2 + 40, 130, fwidth_righthanded_compi + 80);

			//moveDivider(panel_NetbootProductsettings, netbootProductInfo, (int)fwidth_righthanded/2 + 40, 130, fwidth_righthanded_compi + 80);
		}

	}


	//------------------------------------------------------------------------------------------
	//configure interaction
	//------------------------------------------------------------------------------------------
	//menus

	private void setupMenuLists()
	{

		menuItemsHost = new HashMap<String, java.util.List<JMenuItem>>();
		menuItemsHost.put(ITEM_ADD_CLIENT, new ArrayList<JMenuItem>());
		menuItemsHost.put(ITEM_DELETE_CLIENT, new ArrayList<JMenuItem>());
		menuItemsHost.put(ITEM_FREE_LICENCES, new ArrayList<JMenuItem>());

		/*
		menuItemsOpsiclientdExtraEvent = new HashMap<String, java.util.List<JMenuItem>>();
		if (main.getPersistenceController().getOpsiclientdExtraEvents() != null)
		{
			for (String event : main.getPersistenceController().getOpsiclientdExtraEvents())
			{
				menuItemsOpsiclientdExtraEvent.put(event, new ArrayList<JMenuItem>());
			}
		}
		*/
	}


	private void setupMenuFile()
	{
		jMenuFile = new JMenu();
		jMenuFileExit = new JMenuItem();
		jMenuFileSaveConfigurations = new JMenuItem();
		jMenuFileReload = new JMenuItem();
		jMenuFileLanguage = new JMenu(); //submenu

		jMenuFile.setText( configed.getResourceValue("MainFrame.jMenuFile") );

		jMenuFileExit.setText( configed.getResourceValue("MainFrame.jMenuFileExit") );
		jMenuFileExit.addActionListener(new ActionListener()
		                                {
			                                public void actionPerformed(ActionEvent e)
			                                {
				                                exitAction();
			                                }
		                                });

		jMenuFileSaveConfigurations.setText( configed.getResourceValue("MainFrame.jMenuFileSaveConfigurations") );
		jMenuFileSaveConfigurations.addActionListener(new ActionListener()
		        {
			        public void actionPerformed(ActionEvent e)
			        {
				        saveAction();
			        }
		        });

		jMenuFileReload.setText( configed.getResourceValue("MainFrame.jMenuFileReload") );
		//jMenuFileReload.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0));

		jMenuFileReload.addActionListener(new ActionListener()
		                                  {
			                                  public void actionPerformed(ActionEvent e)
			                                  {
				                                  //System.out.println( " action event on jMenuFileReload ");
				                                  reloadAction();
				                                  if (iconButtonReloadLicenses.isEnabled())
					                                  reloadLicensesAction();
			                                  }
		                                  });

		jMenuFileLanguage.setText(configed.getResourceValue("MainFrame.jMenuFileChooseLanguage"));
		ButtonGroup groupLanguages = new ButtonGroup();

		String selectedLocale = Messages.getSelectedLocale();
		logging.debug(this, "selectedLocale " + selectedLocale);

		for (final String localeName : Messages.getLocaleInfo().keySet())
		{
			ImageIcon localeIcon = null;
			String imageIconName = Messages.getLocaleInfo().get(localeName);
			if (imageIconName != null && imageIconName.length() > 0)
			{
				try
				{
					localeIcon = new ImageIcon(Messages.class.getResource(imageIconName));
				}
				catch(Exception ex)
				{
					logging.info(this, "icon not found: " + imageIconName  + ", " + ex);
				}
			}


			JMenuItem menuItem = new JRadioButtonMenuItem(localeName, localeIcon);
			logging.debug(this, "selectedLocale " + selectedLocale);
			menuItem.setSelected(selectedLocale.equals(localeName));
			jMenuFileLanguage.add(menuItem);
			groupLanguages.add(menuItem);
			menuItem.addActionListener(new ActionListener(){
				                           public void actionPerformed(ActionEvent e)
				                           {
					                           main.closeInstance(true);
					                           de.uib.messages.Messages.setLocale(localeName);
					                           new Thread(){
						                           public void run()
						                           {
							                           configed.startWithLocale();
						                           }
					                           }.start();

					                           //we put it into to special thread to avoid invokeAndWait runtime error
				                           }
			                           }
			                          );
		}

		/*
		jMenuFileLanguage.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				//System.out.println( " action event on jMenuFileReload ");
				main.closeInstance(true);
				de.uib.messages.Messages.setLocale("tr");
				configed.startWithLocale();
			}
	});
		*/




		jMenuFile.add(jMenuFileSaveConfigurations);
		jMenuFile.add(jMenuFileReload);
		jMenuFile.add(jMenuFileLanguage);

		if (!configed.isApplet)
		{
			//jMenuFile.add(jMenuFileLanguage);
			jMenuFile.add(jMenuFileExit);
		}
	}

	private void initMenuData()
	{
		labelledDelays = new LinkedHashMap<String, Integer>();
		labelledDelays.put("0 sec", 0);
		labelledDelays.put("5 sec", 5);
		labelledDelays.put("20 sec", 20);
		labelledDelays.put("1 min", 60);
		labelledDelays.put("2 min", 120);
		labelledDelays.put("10 min", 600);
		labelledDelays.put("20 min", 1200);
		labelledDelays.put("1 h", 3600);

		searchedTimeSpans = new LinkedHashMap<String, String>();

		final String TODAY = "today";
		final String SINCE_YESTERDAY = "since yesterday";
		final String LAST_3_DAYS = "last 3 days";
		final String LAST_7_DAYS = "last 7 days";
		final String LAST_MONTH = "last month";
		final String ANY_TIME = "at any time";

		searchedTimeSpans.put(TODAY, "%minus0%");
		searchedTimeSpans.put(SINCE_YESTERDAY, "%minus1%");
		searchedTimeSpans.put(LAST_3_DAYS,  "%minus2%");
		searchedTimeSpans.put(LAST_7_DAYS, "%minus7%");
		searchedTimeSpans.put(LAST_MONTH, "%minus31%");
		searchedTimeSpans.put(ANY_TIME, "");


		searchedTimeSpansText = new LinkedHashMap<String, String>();

		searchedTimeSpansText.put(TODAY, configed.getResourceValue("MainFrame.TODAY"));
		searchedTimeSpansText.put(SINCE_YESTERDAY, configed.getResourceValue("MainFrame.SINCE_YESTERDAY"));
		searchedTimeSpansText.put(LAST_3_DAYS,  configed.getResourceValue("MainFrame.LAST_3_DAYS"));
		searchedTimeSpansText.put(LAST_7_DAYS, configed.getResourceValue("MainFrame.LAST_7_DAYS"));
		searchedTimeSpansText.put(LAST_MONTH, configed.getResourceValue("MainFrame.LAST_MONTH"));
		searchedTimeSpansText.put(ANY_TIME, configed.getResourceValue("MainFrame.ANY_TIME"));


	}

	private void setupMenuClients()
	{
		jMenuClients.setText( configed.getResourceValue("MainFrame.jMenuClients") );


		jCheckBoxMenuItem_showCreatedColumn.setText(configed.getResourceValue("MainFrame.jMenuShowCreatedColumn"));
		jCheckBoxMenuItem_showCreatedColumn.setState(main.host_displayFields.get( HostInfo.created_DISPLAY_FIELD_LABEL ) );

		jCheckBoxMenuItem_showCreatedColumn.addItemListener(new ItemListener()
		        {
			        public void itemStateChanged (ItemEvent e)
			        {
				        main.toggleColumnCreated();
			        }
		        });
		

		jCheckBoxMenuItem_showWANactiveColumn.setText(configed.getResourceValue("MainFrame.jMenuShowWanConfig"));
		//jCheckBoxMenuItem_showWANactiveColumn.setState(main.host_displayFields.get( HostInfo.clientWanConfig_DISPLAY_FIELD_LABEL ) ); 

		jCheckBoxMenuItem_showWANactiveColumn.addItemListener(new ItemListener()
		        {
			        public void itemStateChanged (ItemEvent e)
			        {
				        main.toggleColumnWANactive();
			        }
		        });


		jCheckBoxMenuItem_showIPAddressColumn.setText(configed.getResourceValue("MainFrame.jMenuShowIPAddressColumn"));
		jCheckBoxMenuItem_showIPAddressColumn.setState(main.host_displayFields.get( HostInfo.clientIpAddress_DISPLAY_FIELD_LABEL ) );

		jCheckBoxMenuItem_showIPAddressColumn.addItemListener(new ItemListener()
		        {
			        public void itemStateChanged (ItemEvent e)
			        {
				        main.toggleColumnIPAddress();
			        }
		        });

		jCheckBoxMenuItem_showHardwareAddressColumn .setText(configed.getResourceValue("MainFrame.jMenuShowHardwareAddressColumn"));
		jCheckBoxMenuItem_showHardwareAddressColumn .setState(main.host_displayFields.get( HostInfo.clientMacAddress_DISPLAY_FIELD_LABEL ) );

		jCheckBoxMenuItem_showHardwareAddressColumn .addItemListener(new ItemListener()
		        {
			        public void itemStateChanged (ItemEvent e)
			        {
				        main.toggleColumnHardwareAddress();
			        }
		        });

		jCheckBoxMenuItem_showSessionInfoColumn .setText(configed.getResourceValue("MainFrame.jMenuShowSessionInfoColumn"));
		jCheckBoxMenuItem_showSessionInfoColumn .setState(main.host_displayFields.get( HostInfo.clientSessionInfo_DISPLAY_FIELD_LABEL ));

		jCheckBoxMenuItem_showSessionInfoColumn .addItemListener(new ItemListener()
		        {
			        public void itemStateChanged (ItemEvent e)
			        {
				        main.toggleColumnSessionInfo();
			        }
		        });


		jCheckBoxMenuItem_showInventoryNumberColumn.setText(configed.getResourceValue("MainFrame.jMenuShowInventoryNumberColumn"));
		jCheckBoxMenuItem_showInventoryNumberColumn.setState(main.host_displayFields.get( HostInfo.clientInventoryNumber_DISPLAY_FIELD_LABEL ) );

		jCheckBoxMenuItem_showInventoryNumberColumn.addItemListener(new ItemListener()
		        {
			        public void itemStateChanged (ItemEvent e)
			        {
				        main.toggleColumnInventoryNumber();
			        }
		        });
		
		
		jCheckBoxMenuItem_showUefiBoot.setText(configed.getResourceValue("MainFrame.jMenuShowUefiBoot"));
		jCheckBoxMenuItem_showUefiBoot.setState(main.host_displayFields.get( HostInfo.clientUefiBoot_DISPLAY_FIELD_LABEL ));

		jCheckBoxMenuItem_showUefiBoot.addItemListener(new ItemListener()
		        {
			        public void itemStateChanged (ItemEvent e)
			        {
				        main.toggleColumnUEFIactive();
				        //popupShowUefiBoot.setState(main.host_displayFields.get( HostInfo.clientUefiBoot_DISPLAY_FIELD_LABEL ));
			        }
		        });

		
		jCheckBoxMenuItem_showDepotColumn.setText(configed.getResourceValue("MainFrame.jMenuShowDepotOfClient"));
		jCheckBoxMenuItem_showDepotColumn.setState(main.host_displayFields.get( HostInfo.depotOfClient_DISPLAY_FIELD_LABEL ));

		jCheckBoxMenuItem_showDepotColumn.addItemListener(new ItemListener()
		        {
			        public void itemStateChanged (ItemEvent e)
			        {
				        main.toggleColumnDepot();
			        }
		        });


		/*
		jCheckBoxMenuItem_displayClientList.setText(configed.getResourceValue("MainFrame.jMenuShowSelectedClients"));

		setClientSelectionText("");  // creates fListSelectedClients with empty content
		jCheckBoxMenuItem_displayClientList.addItemListener(new ItemListener()
		{
			public void itemStateChanged (ItemEvent e)
			{ 
				if (fListSelectedClients != null) 
					fListSelectedClients.setVisible(jCheckBoxMenuItem_displayClientList.isSelected());
			}
	});
		*/


		jMenuChangeDepot.setText( configed.getResourceValue("MainFrame.jMenuChangeDepot") );

		jMenuChangeDepot.addActionListener(new ActionListener()
		                                   {
			                                   public void actionPerformed(ActionEvent e)
			                                   {
				                                   changeDepotAction();
			                                   }
		                                   });

		jMenuChangeClientID.setText( configed.getResourceValue("MainFrame.jMenuChangeClientID") );

		jMenuChangeClientID.addActionListener(new ActionListener()
		                                      {
			                                      public void actionPerformed(ActionEvent e)
			                                      {
				                                      changeClientIDAction();
			                                      }
		                                      });

		jMenuResetProductOnClient.setText( configed.getResourceValue("MainFrame.jMenuResetProductOnClient") );

		jMenuResetProductOnClient.addActionListener(new ActionListener()
		        {
			        public void actionPerformed(ActionEvent e)
			        {
				        resetProductOnClientAction();
			        }
		        });

		jMenuAddClient.setText( configed.getResourceValue("MainFrame.jMenuAddClient") );
		jMenuAddClient.addActionListener(new ActionListener()
		                                 {
			                                 public void actionPerformed(ActionEvent e)
			                                 {
				                                 addClientAction();
			                                 }
		                                 });


		menuItemsHost.get(ITEM_ADD_CLIENT).add(jMenuAddClient);



		jMenuWakeOnLan = new JMenu(
		                     configed.getResourceValue("MainFrame.jMenuWakeOnLan")
		                 );


		jMenuDirectWOL.setText( configed.getResourceValue("MainFrame.jMenuWakeOnLan.direct")  );
		jMenuDirectWOL.addActionListener(
		    new ActionListener()
		    {
			    public void actionPerformed(ActionEvent e)
			    {
				    wakeOnLanAction();
			    }
		    }
		);

		jMenuWakeOnLan.add(jMenuDirectWOL);



		/*
		jMenuScheduledWOL = new JMenu(
		                        configed.getResourceValue("MainFrame.jMenuWakeOnLan.scheduler")
		                    );

		jMenuWakeOnLan.add(jMenuScheduledWOL);
		*/



		jMenuNewScheduledWOL.setText( configed.getResourceValue("MainFrame.jMenuWakeOnLan.scheduler")  );
		final MainFrame f = this;
		jMenuNewScheduledWOL.addActionListener(
		    new ActionListener(){
			    public void actionPerformed(ActionEvent e)
			    {
				    FStartWakeOnLan fStartWakeOnLan = new FStartWakeOnLan(f, Globals.APPNAME + ": " + configed.getResourceValue("FStartWakeOnLan.title"), main);
				    fStartWakeOnLan.centerOn(f);
				    //fStartWakeOnLan.setup();
				    fStartWakeOnLan.setVisible(true);
				    fStartWakeOnLan.setPredefinedDelays(labelledDelays);
				    //logging.info(this, "hostSeparationByDepots "
				    //	main.getPersistenceController().getHostSeparationByDepots( main.getSelectedClients() ) );

				    fStartWakeOnLan.setClients();

			    }
		    }
		);

		jMenuWakeOnLan.add( jMenuNewScheduledWOL );

		jMenuWakeOnLan.addSeparator();

		jMenuShowScheduledWOL.setEnabled(false);
		jMenuShowScheduledWOL.setText(  configed.getResourceValue("MainFrame.jMenuWakeOnLan.showRunning") );
		jMenuShowScheduledWOL.addActionListener(
		    new ActionListener()
		    {
			    public void actionPerformed(ActionEvent e)
			    {
				    logging.info(this,  "actionPerformed");
				    executeCommandOnInstances( "arrange", FEditObject.runningInstances.getAll() );
			    }

		    }
		);



		jMenuWakeOnLan.add( jMenuShowScheduledWOL );
		//jMenuScheduledWOL.addSeparator();





		/*
		jMenuWakeOnLan.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				wakeOnLanAction();
			}
	});
		*/


		jMenuDeletePackageCaches.setText( configed.getResourceValue("MainFrame.jMenuDeletePackageCaches"));
		jMenuDeletePackageCaches.addActionListener(new ActionListener()
		        {
			        public void actionPerformed(ActionEvent e)
			        {
				        deletePackageCachesAction();
			        }
		        });

		jMenuOpsiClientdEvent= new JMenu(
		                           configed.getResourceValue("MainFrame.jMenuOpsiClientdEvent")
		                       );

		for (final String event : main.getPersistenceController().getOpsiclientdExtraEvents())
		{
			JMenuItem item = new JMenuItem(event);
			item.setFont(Globals.defaultFont);

			item.addActionListener(new ActionListener()
			                       {
				                       public void actionPerformed(ActionEvent e)
				                       {
					                       fireOpsiclientdEventAction(event);
				                       }
			                       });

			jMenuOpsiClientdEvent.add(item);
		}



		jMenuShowPopupMessage.setText(configed.getResourceValue("MainFrame.jMenuShowPopupMessage"));
		jMenuShowPopupMessage.addActionListener(new ActionListener()
		                                        {
			                                        public void actionPerformed(ActionEvent e)
			                                        {
				                                        showPopupOnClientsAction();
			                                        }
		                                        });




		jMenuShutdownClient.setText(configed.getResourceValue("MainFrame.jMenuShutdownClient"));
		jMenuShutdownClient.addActionListener(new ActionListener()
		                                      {
			                                      public void actionPerformed(ActionEvent e)
			                                      {
				                                      shutdownClientsAction();
			                                      }
		                                      });

		jMenuRequestSessionInfo.setText(configed.getResourceValue("MainFrame.jMenuRequestSessionInfo"));
		jMenuRequestSessionInfo.addActionListener(new ActionListener()
		        {
			        public void actionPerformed(ActionEvent e)
			        {
				        getSessionInfo(true);
			        }
		        });


		jMenuRebootClient.setText(configed.getResourceValue("MainFrame.jMenuRebootClient"));
		jMenuRebootClient.addActionListener(new ActionListener()
		                                    {
			                                    public void actionPerformed(ActionEvent e)
			                                    {
				                                    rebootClientsAction();
			                                    }
		                                    });

		jMenuDeleteClient.setText(configed.getResourceValue("MainFrame.jMenuDeleteClient") );
		jMenuDeleteClient.addActionListener(new ActionListener()
		                                    {
			                                    public void actionPerformed(ActionEvent e)
			                                    {
				                                    deleteClientAction();
			                                    }
		                                    });

		menuItemsHost.get(ITEM_DELETE_CLIENT).add(jMenuDeleteClient);

		jMenuFreeLicences.setText(configed.getResourceValue("MainFrame.jMenuFreeLicences") );
		jMenuFreeLicences.addActionListener(new ActionListener()
		                                    {
			                                    public void actionPerformed(ActionEvent e)
			                                    {
				                                    freeLicencesAction();
			                                    }
		                                    });

		menuItemsHost.get(ITEM_FREE_LICENCES).add(jMenuFreeLicences);

		jMenuRemoteControl.setText(configed.getResourceValue("MainFrame.jMenuRemoteControl") );
		jMenuRemoteControl.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F7, 0));
		//produces a global reaction when pressing space
		jMenuRemoteControl.addActionListener(new ActionListener()
		                                     {
			                                     public void actionPerformed(ActionEvent e)
			                                     {
				                                     //logging.debug(this, "action event: " + e);
				                                     remoteControlAction();
			                                     }
		                                     });

		jMenuClients.add (jMenuWakeOnLan);
		jMenuClients.add(jMenuOpsiClientdEvent);
		jMenuClients.add(jMenuShowPopupMessage);
		jMenuClients.add(jMenuRequestSessionInfo);
		jMenuClients.add(jMenuDeletePackageCaches);

		jMenuClients.addSeparator();

		jMenuClients.add(jMenuShutdownClient);
		jMenuClients.add(jMenuRebootClient);
		jMenuClients.add(jMenuRemoteControl);

		jMenuClients.addSeparator();

		//--
		jMenuClients.add (jMenuDeleteClient);
		jMenuClients.add (jMenuAddClient);
		jMenuClients.add (jMenuResetProductOnClient);
		jMenuClients.add (jMenuChangeClientID);
		if (multidepot)
			jMenuClients.add (jMenuChangeDepot);

		jMenuClients.addSeparator();

		//--
		//jMenuClients.add (jCheckBoxMenuItem_displayClientList);

		jMenuClients.add (jCheckBoxMenuItem_showWANactiveColumn);
		jMenuClients.add (jCheckBoxMenuItem_showIPAddressColumn);
		jMenuClients.add (jCheckBoxMenuItem_showHardwareAddressColumn);
		jMenuClients.add (jCheckBoxMenuItem_showSessionInfoColumn);
		jMenuClients.add (jCheckBoxMenuItem_showInventoryNumberColumn);
		jMenuClients.add (jCheckBoxMenuItem_showCreatedColumn);
		jMenuClients.add (jCheckBoxMenuItem_showUefiBoot);
		jMenuClients.add (jCheckBoxMenuItem_showDepotColumn);
	}
	
	public void updateSSHConnectedInfoMenu(String status)
	{
		SSHCommandFactory factory = SSHCommandFactory.getInstance(main);
		String connectiondata = factory.getConnection().getConnectedUser() + "@" + factory.getConnection().getConnectedHost();
		jMenuSSHConnection.setText(connectiondata.trim() + " " + factory.unknown);
		jMenuSSHConnection.setForeground(Globals.unknownBlue);
		if (status.equals(factory.not_connected))
		{
			jMenuSSHConnection.setForeground(Globals.actionRed);
			jMenuSSHConnection.setText(connectiondata.trim() + " " + factory.not_connected);
		}
		else if (status.equals(factory.connected))
		{
			jMenuSSHConnection.setForeground(Globals.okGreen); 
			jMenuSSHConnection.setText(connectiondata.trim() + " " + factory.connected );
		}
	}

	public void reloadServerMenu()
	{
		setupMenuServer();
	}

	private boolean getBoolConfigValue(String key, boolean defaultvalue, Map<String, Object> configs )
	{
		ArrayList<Object> list = (ArrayList<Object>) configs.get(key);
		if (list ==null || list.size() == 0)
		{
			logging.info(this, "setupMenuServer getBoolConfigValue key " + key +" not existing. get default value " ); // + defaultvalue);
			list = (ArrayList<Object>) configs.get(key.replace(main.USER, ""));
			if (list ==null || list.size() == 0)
			{
				logging.info(this, "setupMenuServer getBoolConfigValue key " + key +" not existing. set to default value " + defaultvalue);
				return defaultvalue;
			}
			else
			{
				logging.info(this, "setupMenuServer getBoolConfigValue key " + key +" active " + ((Boolean) list.get(0)));
				return (Boolean) list.get(0);
			}
			// return defaultvalue;
		}
		else
		{
			logging.info(this, "setupMenuServer getBoolConfigValue key " + key +" active " + ((Boolean) list.get(0)));
			return (Boolean) list.get(0);
		}
	}
	/**
	* Get existing (sorted) sshcommands and build the menu "server-konsole" (include config, control and terminal dialog)
	* also check the depot configs for setting the field editable (or not)
	**/
	private void setupMenuServer()
	{
		logging.info(this, "setupMenuServer ");
		final SSHCommandFactory factory = SSHCommandFactory.getInstance(main);
		
		factory.setMainFrame(this);
		JMenu  menuOpsi = new JMenu();
		menuOpsi.setText(factory.parentOpsi);

		jMenuServer.removeAll();
		jMenuServer.setText( factory.parentNull );
		boolean isReadOnly = de.uib.configed.Globals.isGlobalReadOnly();
		boolean methodsExists = factory.checkSSHCommandMethod();
		

		logging. info(this, "setupMenuServer add configpage");
		jMenuSSHConfig = new JMenuItem(); 
		jMenuSSHConfig.setText(configed.getResourceValue("MainFrame.jMenuSSHConfig") );
		jMenuSSHConfig.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				startSSHConfigAction();
			}
		});
		
		jMenuSSHConnection.setEnabled(false);
		factory.testConnection(factory.getConnection().getConnectedUser(), factory.getConnection().getConnectedHost());
		
		logging. info(this, "setupMenuServer add terminal");
		jMenuRemoteTerminal = new JMenuItem(); 
		jMenuRemoteTerminal.setText(configed.getResourceValue("MainFrame.jMenuSSHTerminal") );
		jMenuRemoteTerminal.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				if (factory.getConnectionState().equals(factory.not_connected))
					logging.error(this, "Please check connection. State now: " + factory.getConnectionState());
				else if (factory.getConnectionState().equals(factory.unknown))
					logging.error(this, "Please check connection. State now: " + factory.getConnectionState());
				else
					remoteSSHTerminalAction();
			}
		});


		if (factory.checkSSHCommandMethod() )
		{
			logging. info(this, "setupMenuServer add commandcontrol");
			jMenuSSHCommandControl = new JMenuItem(); 
			jMenuSSHCommandControl.setText(configed.getResourceValue("MainFrame.jMenuSSHCommandControl") );
			jMenuSSHCommandControl.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{

					startSSHControlAction();
				}
			});
		}
		// SSHCommandControlDialog
		// jMenuServer.add(jMenuRemoteExec);
		jMenuServer.add(jMenuSSHConnection);
		jMenuServer.add(jMenuSSHConfig);
		if (factory.checkSSHCommandMethod() )
			jMenuServer.add(jMenuSSHCommandControl);
		// jMenuServer.addSeparator();
		jMenuServer.addSeparator();
		jMenuServer.add(jMenuRemoteTerminal);
		jMenuServer.addSeparator();
		// jMenuServer.add(jMenuSSHConfig);
		
		String user_identifier = main.getPersistenceController().KEY_USER_ROOT + ".{" + main.USER + "}.";
		Map<String, Object> configs = main.getPersistenceController().getConfig(main.getPersistenceController().getHostInfoCollections().getConfigServer());
		boolean commandsAreDeactivated = (! getBoolConfigValue(user_identifier + main.getPersistenceController().KEY_SSH_COMMANDS_ACTIVE, 
										main.getPersistenceController().KEY_SSH_CONFIG_ACTIVE_defaultvalue, configs));
		logging.info(this, "setupMenuServer commandsAreDeactivated " + commandsAreDeactivated);

		if ( methodsExists)
		{
			factory.retrieveSSHCommandListRequestRefresh();
			java.util.List<SSHCommand_Template> sshcommands =  factory.retrieveSSHCommandList();
			java.util.LinkedHashMap<String,java.util.List<SSHCommand_Template>> sortedComs = factory.getSSHCommandMapSortedByParent();
			
			logging. debug(this, "setupMenuServer add commands to menu commands sortedComs " + sortedComs);
			boolean firstParentGroup = true;
			boolean commands_exists=false;
			for (Map.Entry<String, java.util.List<SSHCommand_Template>> entry : sortedComs.entrySet()) 
			{
				String parentMenuName = entry.getKey();
				LinkedList<SSHCommand_Template> list_com = new LinkedList<SSHCommand_Template>( entry.getValue() );
				Collections.sort(list_com);
				JMenu parentMenu  = new JMenu();
				parentMenu.setText(parentMenuName);
				if (!(parentMenuName.equals(factory.parentNull)))
					firstParentGroup=false;

				for (final SSHCommand_Template com: list_com)
				{
					commands_exists=true;
					JMenuItem jMenuItem= new JMenuItem();
					jMenuItem.setText(com.getMenuText());
					jMenuItem.setToolTipText(com.getToolTipText());
					jMenuItem.addActionListener(new ActionListener()
					{
						public void actionPerformed(ActionEvent e)
						{
							if (factory.getConnectionState().equals(factory.not_connected))
								logging.error(this, "Please check connection. State now: " + factory.getConnectionState());
							else if (factory.getConnectionState().equals(factory.unknown))
								logging.error(this, "Please check connection. State now: " + factory.getConnectionState());
							else remoteSSHExecAction(com);
						}
					});

					if (parentMenuName.equals(factory.parentNull) )
					{
						jMenuServer.add(jMenuItem);
					}
					else 
					{
						parentMenu.add(jMenuItem);
						if (parentMenuName.equals(factory.parentOpsi))
						{
							menuOpsi = parentMenu;
							jMenuServer.add(menuOpsi);
						}
						else jMenuServer.add(parentMenu);
					}
					if (isReadOnly ) jMenuItem.setEnabled(false);
					if (commandsAreDeactivated) jMenuItem.setEnabled(false);
				}
				if (firstParentGroup)
					if (commands_exists)
						jMenuServer.addSeparator();
				firstParentGroup=false;
					
			}
			menuOpsi.addSeparator();
		}
		else jMenuServer.add(menuOpsi);
		java.util.List<SSHCommand> commands = factory.getSSHCommandParameterList();
		logging. info(this, "setupMenuServer add parameterDialogs to opsi commands" + commands);
		for (final SSHCommand command : commands)
		{
			JMenuItem jMenuOpsiCommand = new JMenuItem();
			jMenuOpsiCommand.setText(command.getMenuText());
			jMenuOpsiCommand.setToolTipText(command.getToolTipText());
			jMenuOpsiCommand.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					if (factory.getConnectionState().equals(factory.not_connected))
						logging.error(this, "You are not connected!");
					else
						remoteSSHExecAction(command);
				}
			});
			if (! jMenuServer.isMenuComponent(menuOpsi))
				jMenuServer.add(menuOpsi);
			menuOpsi.add(jMenuOpsiCommand);
			if (isReadOnly ) jMenuOpsiCommand.setEnabled(false);
			if (commandsAreDeactivated) jMenuOpsiCommand.setEnabled(false);
		}

		
		logging. info(this, "setupMenuServer create/read command menu configs");
		jMenuRemoteTerminal.setEnabled(getBoolConfigValue(user_identifier + main.getPersistenceController().KEY_SSH_SHELL_ACTIVE, 
									main.getPersistenceController().KEY_SSH_SHELL_ACTIVE_defaultvalue, configs));
		jMenuSSHConfig.setEnabled(getBoolConfigValue(user_identifier + main.getPersistenceController().KEY_SSH_CONFIG_ACTIVE, 
									main.getPersistenceController().KEY_SSH_CONFIG_ACTIVE_defaultvalue, configs));
		jMenuSSHCommandControl.setEnabled(getBoolConfigValue(user_identifier + main.getPersistenceController().KEY_SSH_CONTROL_ACTIVE, 
									main.getPersistenceController().KEY_SSH_CONTROL_ACTIVE_defaultvalue, configs));
		jMenuServer.setEnabled(getBoolConfigValue(user_identifier + main.getPersistenceController().KEY_SSH_MENU_ACTIVE, 
									main.getPersistenceController().KEY_SSH_MENU_ACTIVE_defaultvalue, configs));
	}

	private void setupMenuGrouping()
	{
		jMenuClientselection.setText( configed.getResourceValue("MainFrame.jMenuClientselection") );

		/*
		jMenuClientselectionSaveGroup.setText( configed.getResourceValue("MainFrame.jMenuClientselectionSaveGroup") );
		jMenuClientselectionSaveGroup.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				saveGroupAction();
			}
	});

		*/


		jMenuClientselectionGetGroup.setText( configed.getResourceValue("MainFrame.jMenuClientselectionGetGroup") );
		jMenuClientselectionGetGroup.addActionListener(new ActionListener()
		        {
			        public void actionPerformed(ActionEvent e)
			        {
				        callSelectionDialog();
			        }
		        });

		jMenuClientselectionGetSavedSearch.setText( configed.getResourceValue("MainFrame.jMenuClientselectionGetSavedSearch") );
		jMenuClientselectionGetSavedSearch.addActionListener(new ActionListener()
		        {
			        public void actionPerformed( ActionEvent e )
			        {
				        main.clientSelectionGetSavedSearch();
			        }
		        });

		jMenuClientselectionNotCurrentProduct.setText( configed.getResourceValue("MainFrame.jMenuClientselectionFindClientsWithOtherProductVersion") );
		jMenuClientselectionNotCurrentProduct.addActionListener(new ActionListener()
		        {
			        public void actionPerformed(ActionEvent e)
			        {
				        groupByNotCurrentProductVersion();
			        }
		        });

		jMenuClientselectionFailedProduct.setText( configed.getResourceValue("MainFrame.jMenuClientselectionFindClientsWithFailedForProduct") );
		jMenuClientselectionFailedProduct.addActionListener(new ActionListener()
		        {
			        public void actionPerformed(ActionEvent e)
			        {
				        groupByFailedProduct();
			        }
		        });

		jMenuClientselectionFailedInPeriod.setText(
		    configed.getResourceValue("MainFrame.jMenuClientselectionFindClientsWithFailedInTimespan")
		);

		for (final String key : searchedTimeSpans.keySet())
		{
			JMenuItem item = new JMenuItemFormatted( searchedTimeSpansText.get( key )   );
			item.setFont(Globals.defaultFont);

			item.addActionListener( new ActionListener()
			                        {
				                        public void actionPerformed(ActionEvent e)
				                        {
					                        main.selectClientsByFailedAtSomeTimeAgo(
					                            searchedTimeSpans.get(key)
					                        );
				                        }
			                        }
			                      );

			jMenuClientselectionFailedInPeriod.add(item);
		}


		/*
		jMenuClientselectionDeselect.setText( configed.getResourceValue("MainFrame.jMenuClientselectionDeselect") );
		jMenuClientselectionDeselect.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				noGroupingAction();
			}
	});

		*/

		jMenuClientselectionToggleClientFilter.setText( configed.getResourceValue("MainFrame.jMenuClientselectionToggleClientFilter") );
		jMenuClientselectionToggleClientFilter.setState(false);
		jMenuClientselectionToggleClientFilter.addActionListener(new ActionListener()
		        {
			        public void actionPerformed(ActionEvent e)
			        {
				        toggleClientFilterAction();
			        }
		        });

		jMenuClientselection.add(jMenuClientselectionGetGroup);
		jMenuClientselection.add(jMenuClientselectionGetSavedSearch);

		jMenuClientselection.addSeparator();

		jMenuClientselection.add(jMenuClientselectionNotCurrentProduct);
		jMenuClientselection.add(jMenuClientselectionFailedProduct);
		jMenuClientselection.add( jMenuClientselectionFailedInPeriod );



		//jMenuClientselection.add(jMenuClientselectionSaveGroup);
		//----------
		jMenuClientselection.addSeparator();
		jMenuClientselection.add(jMenuClientselectionDeselect);
		jMenuClientselection.add(jMenuClientselectionToggleClientFilter);
	}


	private void setupMenuFrames()
	{
		jMenuFrames.setText( configed.getResourceValue("MainFrame.jMenuFrames"));

		jMenuFrameLicences.setText( configed.getResourceValue("MainFrame.jMenuFrameLicences") );
		jMenuFrameLicences.setEnabled(false);
		jMenuFrameLicences.addActionListener ( this );


		jMenuFrameWorkOnProducts.setText( configed.getResourceValue("MainFrame.jMenuFrameWorkOnProducts") );
		jMenuFrameWorkOnProducts.addActionListener(this);


		jMenuFrameWorkOnGroups.setText( configed.getResourceValue("MainFrame.jMenuFrameWorkOnGroups") );
		jMenuFrameWorkOnGroups.setVisible(
		    main.getPersistenceController().isWithLocalImaging()
		);
		jMenuFrameWorkOnGroups.addActionListener(this);


		jMenuFrameShowDialogs.setText( configed.getResourceValue("MainFrame.jMenuFrameShowDialogs") );
		jMenuFrameShowDialogs.setEnabled(false);
		jMenuFrameShowDialogs.addActionListener(
		    new ActionListener()
		    {
			    public void actionPerformed(ActionEvent e)
			    {
				    logging.info(this,  "actionPerformed");
				    executeCommandOnInstances( "arrange", FEditObject.runningInstances.getAll() );
			    }

		    }
		);

		jMenuFrames.add( jMenuFrameLicences );
		jMenuFrames.add( jMenuFrameWorkOnProducts );
		jMenuFrames.add( jMenuFrameWorkOnGroups );
		jMenuFrames.addSeparator();
		jMenuFrames.add( jMenuFrameShowDialogs );

	}

	private void setupMenuHelp()
	{
		jMenuHelp.setText( configed.getResourceValue("MainFrame.jMenuHelp") );

		jMenuHelpDoc.setText(configed.getResourceValue("MainFrame.jMenuDoc"));
		jMenuHelpDoc.addActionListener(new ActionListener(){
			                               public void actionPerformed(ActionEvent e)
			                               {
				                               main.showExternalDocument(Globals.opsiDocpage);
			                               }
		                               });
		jMenuHelp.add(jMenuHelpDoc);


		jMenuHelpForum.setText(configed.getResourceValue("MainFrame.jMenuForum"));
		jMenuHelpForum.addActionListener(new ActionListener(){
			                                 public void actionPerformed(ActionEvent e)
			                                 {
				                                 main.showExternalDocument(Globals.opsiForumpage);
			                                 }
		                                 });
		jMenuHelp.add(jMenuHelpForum);

		jMenuHelpSupport.setText(configed.getResourceValue("MainFrame.jMenuSupport"));
		jMenuHelpSupport.addActionListener(new ActionListener(){
			                                   public void actionPerformed(ActionEvent e)
			                                   {
				                                   main.showExternalDocument(Globals.opsiSupportpage);
			                                   }
		                                   });
		jMenuHelp.add(jMenuHelpSupport);



		jMenuHelp.addSeparator();

		jMenuHelpOpsiVersion.setText(
		    configed.getResourceValue("MainFrame.jMenuHelpOpsiService")
		    +  ": " + main.getOpsiVersion()
		);
		jMenuHelpOpsiVersion.setEnabled(false);
		jMenuHelpOpsiVersion.setForeground(Globals.lightBlack);

		//dummy entry just for displaying the version
		/*
		jMenuHelpOpsiVersion.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
			}
	});
		*/
		jMenuHelp.add(jMenuHelpOpsiVersion);

		jMenuHelpOpsiModuleInformation.setText( configed.getResourceValue("MainFrame.jMenuHelpOpsiModuleInformation") );
		if (main.getOpsiVersion().length() == 0 || main.getOpsiVersion().charAt(0) == '<' || main.getOpsiVersion().compareTo("3.4") < 0)
		{
			jMenuHelpOpsiModuleInformation.setEnabled(false);
		}
		else
		{
			jMenuHelpOpsiModuleInformation.addActionListener(new ActionListener()
			        {
				        public void actionPerformed(ActionEvent e)
				        {
					        showOpsiModules();
				        }
			        });
		}

		jMenuHelp.add(jMenuHelpOpsiModuleInformation);

		jMenuHelpInternalConfiguration.setText( configed.getResourceValue("MainFrame.jMenuHelpInternalConfiguration") );
		jMenuHelpInternalConfiguration.addActionListener(new ActionListener()
		        {
			        public void actionPerformed(ActionEvent e)
			        {
				        showBackendConfigurationAction();
			        }
		        });
		jMenuHelp.add(jMenuHelpInternalConfiguration);


		ActionListener selectLoglevelListener = new ActionListener()
		                                        {
			                                        public void actionPerformed(ActionEvent e)
			                                        {
				                                        for (int i = 0; i < logging.LEVEL_DONT_SHOW_IT; i++)
				                                        {
					                                        if (e.getSource() == rbLoglevelItems[i])
					                                        {
						                                        rbLoglevelItems[i].setSelected(true);
						                                        logging.setAktDebugLevel(i);
					                                        }
					                                        else
					                                        {
						                                        if (rbLoglevelItems[i] != null)
							                                        rbLoglevelItems[i].setSelected(false);
					                                        }
				                                        }
			                                        }
		                                        };

		jMenuHelpLoglevel.setText(configed.getResourceValue("MainFrame.jMenuLoglevel"));

		for (int i = logging.LEVEL_ERROR; i < logging.LEVEL_DONT_SHOW_IT; i++)
		{
			rbLoglevelItems[i] = new JRadioButtonMenuItem(""+i);
			String commented = configed.getResourceValue("MainFrame.jMenuLoglevel." + i);
			if (!configed.getResourceValue("MainFrame.jMenuLoglevel." + i).equals("MainFrame.jMenuLoglevel." + i))
				rbLoglevelItems[i].setText(commented);

			jMenuHelpLoglevel.add(rbLoglevelItems[i]);
			if (i == logging.AKT_DEBUG_LEVEL)
				rbLoglevelItems[i].setSelected(true);

			rbLoglevelItems[i].addActionListener(selectLoglevelListener);
		}
		jMenuHelp.add(jMenuHelpLoglevel);
		
		jMenuHelpServerInfoPage.setText("opsi server InfoPage");
		
		jMenuHelpServerInfoPage.addActionListener( new ActionListener()
			{ 
				public void actionPerformed(ActionEvent e)
				        {
					        showInfoPage();
				        }
			  });
				
		
		//jMenuHelp.add(jMenuHelpServerInfoPage);


		jMenuHelp.addSeparator();

		jMenuHelpAbout.setText( configed.getResourceValue("MainFrame.jMenuHelpAbout") );
		jMenuHelpAbout.addActionListener(new ActionListener()
		                                 {
			                                 public void actionPerformed(ActionEvent e)
			                                 {
				                                 //main.getPersistenceController().makeConnection(); //just checking the connection
				                                 showAboutAction();
			                                 }
		                                 });

		jMenuHelp.add(jMenuHelpAbout);
	}

	//------------------------------------------------------------------------------------------
	//icon pane
	private void setupIcons1()
	{
		//buttonWindowStack = new JButton(Globals.createImageIcon("images/stackWindows1.png",""));
		//buttonWindowStack.setText("12");
		//buttonWindowStack.setHorizontalAlignment(SwingConstants.TRAILING);

		/*
		buttonWindowStack = new IconButton(
		                        "fenster anordnen",
		                        "images/stackWindows1.png",
		                        "images/stackWindows1.png",
		                        "");
		buttonWindowStack.setEnabled(false);
		buttonWindowStack.addActionListener(
			new java.awt.event.ActionListener()
		   {
			   public void actionPerformed(ActionEvent e)
			   {
					executeCommandOnInstances( "arrange", FEditObject.runningInstances.getAll() );
			   }
		   });
		   */


		iconButtonReload = new IconButton(
		                       de.uib.configed.configed.getResourceValue("MainFrame.iconButtonReload"),
		                       "images/reload.gif",
		                       "images/reload_over.gif",
		                       " ");

		iconButtonReloadLicenses = new IconButton(
		                               de.uib.configed.configed.getResourceValue("MainFrame.iconButtonReloadLicensesData"),
		                               "images/reload_licenses.png",
		                               "images/reload_licenses_over.png",
		                               " ",
		                               false);
		iconButtonReloadLicenses.setVisible(false);

		iconButtonNewClient = new IconButton(
		                          de.uib.configed.configed.getResourceValue("MainFrame.iconButtonNewClient"),
		                          "images/newClient.gif",
		                          "images/newClient_over.gif",
		                          " ");

		iconButtonSetGroup = new IconButton(
		                         de.uib.configed.configed.getResourceValue("MainFrame.iconButtonSetGroup"),
		                         "images/setGroup.gif",
		                         "images/setGroup_over.gif",
		                         " ");
		iconButtonSaveConfiguration = new IconButton(
		                                  de.uib.configed.configed.getResourceValue("MainFrame.iconButtonSaveConfiguration"),
		                                  "images/apply_over.gif",
		                                  " ",
		                                  "images/apply_disabled.gif",
		                                  false);

		iconButtonCancelChanges = new IconButton(
		                              de.uib.configed.configed.getResourceValue("MainFrame.iconButtonCancelChanges"),
		                              "images/cancel-32.png",
		                              "images/cancel_over-32.png",
		                              " ",
		                              false);

		iconButtonReachableInfo = new IconButton(
		                              de.uib.configed.configed.getResourceValue("MainFrame.iconButtonReachableInfo"),
		                              "images/new_networkconnection.png",
		                              "images/new_networkconnection.png",
		                              "images/new_networkconnection.png",
		                              main.host_displayFields.get("clientConnected"));



		iconButtonSessionInfo = new IconButton(
		                            de.uib.configed.configed.getResourceValue("MainFrame.iconButtonSessionInfo"),
		                            "images/system-users-query.png",
		                            "images/system-users-query_over.png",
		                            "images/system-users-query_over.png",
		                            main.host_displayFields.get("clientSessionInfo"));


		iconButtonToggleClientFilter = new IconButton(
		                                   de.uib.configed.configed.getResourceValue("MainFrame.iconButtonToggleClientFilter"),
		                                   "images/view-filter_disabled-32.png",
		                                   "images/view-filter_over-32.png",
		                                   "images/view-filter-32.png",
		                                   true);

		iconButtonSaveGroup = new IconButton(
		                          de.uib.configed.configed.getResourceValue("MainFrame.iconButtonSaveGroup"),
		                          "images/saveGroup.gif",
		                          "images/saveGroup_over.gif",
		                          " ");



		iconButtonReload.addActionListener(new java.awt.event.ActionListener()
		                                   {
			                                   public void actionPerformed(ActionEvent e)
			                                   {
				                                   reloadAction();
			                                   }
		                                   });
		iconButtonReloadLicenses.addActionListener(new java.awt.event.ActionListener()
		        {
			        public void actionPerformed(ActionEvent e)
			        {
				        reloadLicensesAction();
			        }
		        });
		iconButtonNewClient.addActionListener(new java.awt.event.ActionListener()
		                                      {
			                                      public void actionPerformed(ActionEvent e)
			                                      {
				                                      addClientAction();
			                                      }
		                                      });
		iconButtonSetGroup.addActionListener(new java.awt.event.ActionListener()
		                                     {
			                                     public void actionPerformed(ActionEvent e)
			                                     {
				                                     callSelectionDialog();
			                                     }
		                                     });
		iconButtonSaveConfiguration.addActionListener(new java.awt.event.ActionListener()
		        {
			        public void actionPerformed(ActionEvent e)
			        {
				        saveAction();
			        }
		        });
		iconButtonCancelChanges.addActionListener(new java.awt.event.ActionListener()
		        {
			        public void actionPerformed(ActionEvent e)
			        {
				        cancelAction();
			        }
		        });
		iconButtonReachableInfo.addActionListener(new java.awt.event.ActionListener()
		        {
			        public void actionPerformed(ActionEvent e)
			        {
				        getReachableInfo();
			        }
		        });
		iconButtonSessionInfo.addActionListener(new java.awt.event.ActionListener()
		                                        {
			                                        public void actionPerformed(ActionEvent e)
			                                        {
				                                        getSessionInfo(false);
			                                        }
		                                        });
		iconButtonToggleClientFilter.addActionListener(new java.awt.event.ActionListener()
		        {
			        public void actionPerformed(ActionEvent e)
			        {
				        toggleClientFilterAction();
			        }
		        });
		iconButtonSaveGroup.addActionListener(new java.awt.event.ActionListener()
		                                      {
			                                      public void actionPerformed(ActionEvent e)
			                                      {
				                                      saveGroupAction();
			                                      }
		                                      });


		proceeding = new JPanel();
		ActivityPanel activity = new ActivityPanel();
		proceeding.add(activity);
		new Thread(activity).start();
		proceeding.setToolTipText("activity indicator");
	}


	/**
	 * Invoked when task's progress property changes.
	 
	public void propertyChange(PropertyChangeEvent evt) {
	    if ("progress" == evt.getPropertyName()) {
	        int progress = (Integer) evt.getNewValue();
	        proceeding.setIndeterminate(false);
	        proceeding.setValue(progress);
	        //taskOutput.append(String.format(
	          //          "Completed %d%% of task.\n", progress));
	    }
}
	*/
	//------------------------------------------------------------------------------------------
	//context menus

	private void setupPopupMenuClientsTab()
	{
		/*
		popupDisplayClientList.setText(configed.getResourceValue("MainFrame.jMenuShowSelectedClients"));
		popupDisplayClientList.setFont(Globals.defaultFontBig);

		setClientSelectionText("");  // creates fListSelectedClients with empty content
		popupDisplayClientList.addItemListener(new ItemListener()
		{
			public void itemStateChanged (ItemEvent e)
			{ 
				if (fListSelectedClients != null) 
					fListSelectedClients.setVisible(popupDisplayClientList.isSelected());
			}
	});
		*/

		popupShowCreatedColumn.setText(configed.getResourceValue("MainFrame.jMenuShowCreatedColumn"));
		popupShowCreatedColumn.setState(main.host_displayFields.get("clientCreated"));

		popupShowCreatedColumn.addItemListener(new ItemListener()
		                                       {
			                                       public void itemStateChanged (ItemEvent e)
			                                       {
				                                       main.toggleColumnCreated();
			                                       }
		                                       });

		popupShowWANactiveColumn.setText(configed.getResourceValue("MainFrame.jMenuShowWanConfig"));
		//popupShowWANactiveColumn.setState(main.host_displayFields.get( HostInfo.clientWanConfig_DISPLAY_FIELD_LABEL ) ); 

		popupShowWANactiveColumn.addItemListener(new ItemListener()
		        {
			        public void itemStateChanged (ItemEvent e)
			        {
				        main.toggleColumnWANactive();
			        }
		        });


		

		popupShowIPAddressColumn.setText(configed.getResourceValue("MainFrame.jMenuShowIPAddressColumn"));
		popupShowIPAddressColumn.setState(main.host_displayFields.get("clientIPAddress"));

		popupShowIPAddressColumn.addItemListener(new ItemListener()
		        {
			        public void itemStateChanged (ItemEvent e)
			        {
				        main.toggleColumnIPAddress();
			        }
		        });

		popupShowHardwareAddressColumn.setText(configed.getResourceValue("MainFrame.jMenuShowHardwareAddressColumn"));
		popupShowHardwareAddressColumn.setState(main.host_displayFields.get("clientHardwareAddress"));

		popupShowHardwareAddressColumn.addItemListener(new ItemListener()
		        {
			        public void itemStateChanged (ItemEvent e)
			        {
				        main.toggleColumnHardwareAddress();
			        }
		        });

		popupShowSessionInfoColumn.setText(configed.getResourceValue("MainFrame.jMenuShowSessionInfoColumn"));
		popupShowSessionInfoColumn.setState(main.host_displayFields.get("clientSessionInfo"));

		popupShowSessionInfoColumn.addItemListener(new ItemListener()
		        {
			        public void itemStateChanged (ItemEvent e)
			        {
				        main.toggleColumnSessionInfo();
			        }
		        });

		popupShowInventoryNumberColumn.setText(configed.getResourceValue("MainFrame.jMenuShowInventoryNumberColumn"));
		popupShowInventoryNumberColumn.setState(main.host_displayFields.get("clientInventoryNumber"));

		popupShowInventoryNumberColumn.addItemListener(new ItemListener()
		        {
			        public void itemStateChanged (ItemEvent e)
			        {
				        main.toggleColumnInventoryNumber();
			        }
		        });
		
		
		popupShowUefiBoot.setText(configed.getResourceValue("MainFrame.jMenuShowUefiBoot"));
		popupShowUefiBoot.setState(main.host_displayFields.get( HostInfo.clientUefiBoot_DISPLAY_FIELD_LABEL ));

		popupShowUefiBoot.addItemListener(new ItemListener()
		        {
			        public void itemStateChanged (ItemEvent e)
			        {
				        main.toggleColumnUEFIactive();
			        }
		        });

		
		popupShowDepotColumn.setText(configed.getResourceValue("MainFrame.jMenuShowDepotOfClient"));
		popupShowDepotColumn.setState(main.host_displayFields.get( HostInfo.depotOfClient_DISPLAY_FIELD_LABEL ));

		popupShowDepotColumn.addItemListener(new ItemListener()
		        {
			        public void itemStateChanged (ItemEvent e)
			        {
				        main.toggleColumnDepot();
			        }
		        });

		

		popupChangeDepot.setText( configed.getResourceValue("MainFrame.jMenuChangeDepot") );

		popupChangeDepot.addActionListener(new ActionListener()
		                                   {
			                                   public void actionPerformed(ActionEvent e)
			                                   {
				                                   changeDepotAction();
			                                   }
		                                   });


		popupChangeClientID.setText( configed.getResourceValue("MainFrame.jMenuChangeClientID") );

		popupChangeClientID.addActionListener(new ActionListener()
		                                      {
			                                      public void actionPerformed(ActionEvent e)
			                                      {
				                                      changeClientIDAction();
			                                      }
		                                      });

		popupResetProductOnClient.setText( configed.getResourceValue("MainFrame.jMenuResetProductOnClient") );

		popupResetProductOnClient.addActionListener(new ActionListener()
		        {
			        public void actionPerformed(ActionEvent e)
			        {
				        resetProductOnClientAction();
			        }
		        });

		popupAddClient.setText( configed.getResourceValue("MainFrame.jMenuAddClient") );

		popupAddClient.addActionListener(new ActionListener()
		                                 {
			                                 public void actionPerformed(ActionEvent e)
			                                 {
				                                 addClientAction();
			                                 }
		                                 });

		menuItemsHost.get(ITEM_ADD_CLIENT).add(popupAddClient);

		popupWakeOnLan.setText(configed.getResourceValue("MainFrame.jMenuWakeOnLan"));

		popupWakeOnLanDirect.setText(configed.getResourceValue("MainFrame.jMenuWakeOnLan.direct") );
		popupWakeOnLanDirect.addActionListener(new ActionListener()
		                                       {
			                                       public void actionPerformed(ActionEvent e)
			                                       {
				                                       wakeOnLanAction();
			                                       }
		                                       });
		popupWakeOnLan.add( popupWakeOnLanDirect );

		final MainFrame f = this;
		popupWakeOnLanScheduler.setText( configed.getResourceValue("MainFrame.jMenuWakeOnLan.scheduler") );
		popupWakeOnLanScheduler.addActionListener(new ActionListener()
		        {
			        public void actionPerformed(ActionEvent e)
			        {
				        FStartWakeOnLan fStartWakeOnLan = new FStartWakeOnLan(f, Globals.APPNAME + ": " + configed.getResourceValue("FStartWakeOnLan.title"), main);
				        fStartWakeOnLan.centerOn(f);
				        //fStartWakeOnLan.setup();
				        fStartWakeOnLan.setVisible(true);
				        fStartWakeOnLan.setPredefinedDelays(labelledDelays);

				        fStartWakeOnLan.setClients();
			        }
		        });
		popupWakeOnLan.add( popupWakeOnLanScheduler );




		/*
		for (final String label : labelledDelays.keySet())
		{
			JMenuItem item = new JMenuItemFormatted( label );
			item.setFont(Globals.defaultFont);
			
			
			if (labelledDelays.get( label ) == 0)
			{
				item.addActionListener(new ActionListener()
					{
						public void actionPerformed(ActionEvent e)
						{
							wakeOnLanAction();
						}
					}
				);
			}
			else
			{
				
				item.addActionListener(new ActionListener()
					{
						public void actionPerformed(ActionEvent e)
						{
							wakeOnLanActionWithDelay( labelledDelays.get( label ) );
						}
					}
				);
			}
			
			popupWakeOnLan.add(item);
	}


		popupWakeOnLan.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				wakeOnLanAction();
			}
	});
		*/

		popupDeletePackageCaches.setText(configed.getResourceValue("MainFrame.jMenuDeletePackageCaches"));
		popupDeletePackageCaches.addActionListener(new ActionListener()
		        {
			        public void actionPerformed(ActionEvent e)
			        {
				        deletePackageCachesAction();
			        }
		        });



		//subOpsiClientdEvent = new JMenu("abcd");
		//configed.getResourceValue("MainFrame.jMenuOpsiClientdEvent")
		//);
		/*
		for (final String event : main.getPersistenceController().getOpsiclientdExtraEvents())
		{
			JMenuItem item = new JMenuItemFormatted(event);
			item.setFont(Globals.defaultFont);
			
			item.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					fireOpsiclientdEventAction(event);
				}
			});
			
			subOpsiClientdEvent.add(item);
	}
		*/

		popupShowPopupMessage.setText(configed.getResourceValue("MainFrame.jMenuShowPopupMessage"));
		popupShowPopupMessage.addActionListener(new ActionListener()
		                                        {
			                                        public void actionPerformed(ActionEvent e)
			                                        {
				                                        showPopupOnClientsAction();
			                                        }
		                                        });

		popupRequestSessionInfo.setText(configed.getResourceValue("MainFrame.jMenuRequestSessionInfo"));
		popupRequestSessionInfo.addActionListener(new ActionListener()
		        {
			        public void actionPerformed(ActionEvent e)
			        {
				        getSessionInfo(true);
			        }
		        });


		popupShutdownClient.setText(configed.getResourceValue("MainFrame.jMenuShutdownClient"));
		popupShutdownClient.addActionListener(new ActionListener()
		                                      {
			                                      public void actionPerformed(ActionEvent e)
			                                      {
				                                      shutdownClientsAction();
			                                      }
		                                      });

		popupRebootClient.setText(configed.getResourceValue("MainFrame.jMenuRebootClient"));
		popupRebootClient.addActionListener(new ActionListener()
		                                    {
			                                    public void actionPerformed(ActionEvent e)
			                                    {
				                                    rebootClientsAction();
			                                    }
		                                    });


		popupDeleteClient.setText( configed.getResourceValue("MainFrame.jMenuDeleteClient") );
		popupDeleteClient.addActionListener(new ActionListener()
		                                    {
			                                    public void actionPerformed(ActionEvent e)
			                                    {
				                                    deleteClientAction();
			                                    }
		                                    });


		menuItemsHost.get(ITEM_DELETE_CLIENT).add(popupDeleteClient);


		popupFreeLicences.setText( configed.getResourceValue("MainFrame.jMenuFreeLicences") );
		popupFreeLicences.addActionListener(new ActionListener()
		                                    {
			                                    public void actionPerformed(ActionEvent e)
			                                    {
				                                    freeLicencesAction();
			                                    }
		                                    });


		menuItemsHost.get(ITEM_FREE_LICENCES).add(popupFreeLicences);


		popupRemoteControl.setText( configed.getResourceValue("MainFrame.jMenuRemoteControl") );

		popupRemoteControl.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0));
		popupRemoteControl.addActionListener(new ActionListener()
		                                     {
			                                     public void actionPerformed(ActionEvent e)
			                                     {
				                                     remoteControlAction();
			                                     }
		                                     });


		/*
		popupSelectionSaveGroup.setText( configed.getResourceValue("MainFrame.jMenuClientselectionSaveGroup") );
		popupSelectionSaveGroup.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				saveGroupAction();
			}
	});
		*/


		popupSelectionGetGroup.setText( configed.getResourceValue("MainFrame.jMenuClientselectionGetGroup") );
		popupSelectionGetGroup.addActionListener(new ActionListener()
		        {
			        public void actionPerformed(ActionEvent e)
			        {
				        callSelectionDialog();
			        }
		        });

		popupSelectionGetSavedSearch.setText( configed.getResourceValue("MainFrame.jMenuClientselectionGetSavedSearch") );
		popupSelectionGetSavedSearch.addActionListener(new ActionListener()
		        {
			        public void actionPerformed(ActionEvent e)
			        {
				        main.clientSelectionGetSavedSearch();
			        }
		        });


		/*
		popupSelectionDeselect.setText( configed.getResourceValue("MainFrame.jMenuClientselectionDeselect") );
		popupSelectionDeselect.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				noGroupingAction();
			}
	});
		*/

		// pdf generating
		popupCreatePdf.setText( configed.getResourceValue("FGeneralDialog.pdf") );
		popupCreatePdf.addActionListener(new ActionListener()
		                                 {
			                                 public void actionPerformed(ActionEvent e)
			                                 {
				                                 createPdf();
			                                 }
		                                 });
		//

		popupSelectionToggleClientFilter.setText( configed.getResourceValue("MainFrame.jMenuClientselectionToggleClientFilter") );
		popupSelectionToggleClientFilter.setState(false);
		popupSelectionToggleClientFilter.setFont(Globals.defaultFontBig);

		popupSelectionToggleClientFilter.addActionListener(new ActionListener()
		        {
			        public void actionPerformed(ActionEvent e)
			        {
				        toggleClientFilterAction();
			        }
		        });


		popupRebuildClientList.addActionListener(new ActionListener()
		        {
			        public void actionPerformed(ActionEvent e)
			        {
				        main.reloadHosts();
			        }
		        });




		//----

		popupClients.add(popupWakeOnLan);

		menuPopupOpsiClientdEvent = new JMenu(
		                                configed.getResourceValue("MainFrame.jMenuOpsiClientdEvent")
		                            );

		for (final String event : main.getPersistenceController().getOpsiclientdExtraEvents())
		{
			JMenuItem item = new JMenuItemFormatted(event);
			item.setFont(Globals.defaultFont);

			item.addActionListener(new ActionListener()
			                       {
				                       public void actionPerformed(ActionEvent e)
				                       {
					                       fireOpsiclientdEventAction(event);
				                       }
			                       });


			menuPopupOpsiClientdEvent.add(item);
		}


		popupClients.add(menuPopupOpsiClientdEvent);
		popupClients.add(popupShowPopupMessage);
		popupClients.add(popupRequestSessionInfo);
		popupClients.add(popupDeletePackageCaches);

		popupClients.addSeparator();


		popupClients.add(popupShutdownClient);
		popupClients.add(popupRebootClient);
		popupClients.add(popupRemoteControl);

		popupClients.addSeparator();

		//--
		popupClients.add(popupAddClient);
		popupClients.add(popupDeleteClient);
		popupClients.add(popupResetProductOnClient);
		popupClients.add(popupFreeLicences);
		popupClients.add(popupChangeClientID);
		if (multidepot)
			popupClients.add (popupChangeDepot);
		popupClients.addSeparator();
		//popupClients.add(popupDisplayClientList);
		popupClients.add(popupShowWANactiveColumn);
		popupClients.add(popupShowIPAddressColumn);
		popupClients.add(popupShowHardwareAddressColumn);
		popupClients.add(popupShowSessionInfoColumn);
		popupClients.add(popupShowInventoryNumberColumn);
		popupClients.add(popupShowCreatedColumn);
		popupClients.add(popupShowUefiBoot);
		popupClients.add(popupShowDepotColumn);

		//----
		popupClients.addSeparator();
		popupClients.add(popupSelectionGetGroup);
		popupClients.add(popupSelectionGetSavedSearch);

		//popupClients.add(popupSelectionSaveGroup);
		//popupClients.add(popupSelectionDeleteGroup);
		popupClients.addSeparator();
		popupClients.add(popupSelectionDeselect);
		popupClients.add(popupSelectionToggleClientFilter);

		//popupClients.addSeparator();
		popupClients.add(popupRebuildClientList);
		popupClients.add(popupCreatePdf);

	}
	public void createPdf() {
		TableModel tm = main.getSelectedClientsTableModel();
		JTable jTable = new JTable (tm);
		//System.out.println("Gruppe in createPDF: " + statusPane.getGroupName());
		try
		{
			HashMap<String, String> metaData = new HashMap<String, String>();
			String title = configed.getResourceValue("MainFrame.ClientList");
			//group: " + statusPane.getGroupName()
			//jTable;
			if (statusPane.getGroupName().length()!=0) {
				title = title + ": " + statusPane.getGroupName();
			}
			metaData.put("header", title);
			title ="";
			if (statusPane.getInvolvedDepots().length()!=0) {
				title = title + "Depot(s) : " +statusPane.getInvolvedDepots();
			}
			/*
			if (statusPane.getSelectedClientNames().length()!=0) {
				title = title + "; Clients: " + statusPane.getSelectedClientNames();
		}
			*/
			metaData.put("title", title );
			metaData.put("subject", "report of table");
			metaData.put("keywords", "");
			tableToPDF = new DocumentToPdf (null, metaData); //  no filename, metadata

			tableToPDF.createContentElement("table", jTable);

			tableToPDF.setPageSizeA4_Landscape();  //
			tableToPDF.toPDF(); //   create Pdf

		}
		catch (Exception ex)
		{
			logging.error("pdf printing error " + ex);
		}
	}


	private void setupPopupMenuHardwareAuditTab()
	{

	}

	private void setupPopupMenuSoftwareAuditTab()
	{

	}

	private void setupPopupMenuNetworkConfigTab()
	{

	}

	private void setupPopupMenuLogfilesTab()
	{

	}





	//------------------------------------------------------------------------------------------
	//------------------------------------------------------------------------------------------

	public void clear()
	{
		baseContainer.remove(allPane);
	}

	private void guiInit(JApplet appletHost)
	{
		if (!configed.isApplet)
			this.addWindowListener(this);
		this.setFont(Globals.defaultFont);
		this.setIconImage (Globals.mainIcon);

		//setIconImage(Toolkit.getDefaultToolkit().createImage(Frame1.class.getResource("[your symbol]")));

		allPane = new SizeListeningPanel();
		//allPane = (JPanel) this.getallPane();

		//contentLayout = new GroupLayout(allPane);
		//allPane.setLayout(contentLayout);
		allPane.setLayout(borderLayout1);


		baseContainer.add(allPane);


		initMenuData();

		setupMenuLists();

		setupMenuFile();
		setupMenuGrouping();
		setupMenuClients();
		setupMenuServer();
		setupMenuFrames();
		setupMenuHelp();


		jMenuBar1.add(jMenuFile);
		//jMenuBar1.add(jMenuFileLanguage);
		jMenuBar1.add(jMenuClientselection);
		jMenuBar1.add(jMenuClients);
		jMenuBar1.add(jMenuServer);
		jMenuBar1.add(jMenuFrames);
		jMenuBar1.add(jMenuHelp);

		if (appletHost == null)
			this.setJMenuBar(jMenuBar1);
		else
			appletHost.setJMenuBar( jMenuBar1);


		setupPopupMenuClientsTab();
		//setupPopupMenuLocalbootProductsTab();
		//setupPopupMenuNetbootProductsTab();
		setupPopupMenuHardwareAuditTab();
		setupPopupMenuSoftwareAuditTab();
		setupPopupMenuNetworkConfigTab();
		setupPopupMenuLogfilesTab();

		//clientPane
		clientPane = new JPanel();

		/*
		clientPaneComponentListener = new ComponentAdapter(){
				public void  componentResized(ComponentEvent e)
				{
					clientPaneW = getSize().width;
					
					logging.info(this, "componentResized new width " + clientPaneW 
						 );
					
				}
			}
		;

				
		clientPane.addComponentListener( clientPaneComponentListener );
		*/

		clientPane.setPreferredSize( new Dimension (fwidth_righthanded,  fheight + 40));
		clientPane.setBorder( Globals.createPanelBorder() );
		//new LineBorder(Globals.backBlue, 2, true));
		csClientPane = new Containership(clientPane);

		GroupLayout layoutClientPane = new GroupLayout(clientPane);
		clientPane.setLayout(layoutClientPane);

		labelHost = new JLabel((Icon) Globals.createImageIcon("images/client.png", ""), SwingConstants.LEFT);
		labelHost.setPreferredSize(Globals.buttonDimension);

		labelHostID = new JLabel("");
		labelHostID.setFont(Globals.defaultFontStandardBold);

		//JLabel labelBelongsTo  = new JLabel("In Depot");
		//labelBelongsTo.setPreferredSize(Globals.buttonDimension);

		JLabel labelClientDescription = new JLabel ( configed.getResourceValue("MainFrame.jLabelDescription") );
		labelClientDescription.setPreferredSize(Globals.buttonDimension);

		JLabel labelClientInventoryNumber = new JLabel ( configed.getResourceValue("MainFrame.jLabelInventoryNumber") );
		labelClientInventoryNumber.setPreferredSize(Globals.buttonDimension);

		JLabel labelClientNotes = new JLabel ( configed.getResourceValue("MainFrame.jLabelNotes") );
		//jLabelClientNotes.setFont(Globals.defaultFontStandardBold);

		JLabel labelClientMacAddress = new JLabel ( configed.getResourceValue("MainFrame.jLabelMacAddress") );
		//jLabelClientMacAddress.setFont(Globals.defaultFontStandardBold);

		JLabel labelOneTimePassword = new JLabel  ( configed.getResourceValue("MainFrame.jLabelOneTimePassword" ) );

		jFieldInDepot = new JTextArea();
		jFieldInDepot.setEditable(false);
		jFieldInDepot.setFont(Globals.defaultFontBig);
		jFieldInDepot.setBackground(Globals.backgroundLightGrey);

		jTextFieldDescription = new JTextEditorField("");
		jTextFieldDescription.setEditable(true);
		jTextFieldDescription.setPreferredSize(Globals.textfieldDimension);
		jTextFieldDescription.setFont(Globals.defaultFontBig);
		jTextFieldDescription.addKeyListener(this);

		jTextFieldInventoryNumber = new JTextEditorField("");
		jTextFieldInventoryNumber.setEditable(true);
		jTextFieldInventoryNumber.setPreferredSize(Globals.textfieldDimension);
		jTextFieldInventoryNumber.setFont(Globals.defaultFontBig);
		jTextFieldInventoryNumber.addKeyListener(this);

		jTextAreaNotes = new JTextArea("");
		jTextAreaNotes.setEditable(true);
		jTextAreaNotes.setLineWrap(true);
		jTextAreaNotes.setWrapStyleWord(true);
		jTextAreaNotes.setFont(Globals.defaultFontBig);
		GraphicsEnvironment.getLocalGraphicsEnvironment();
		Font font = new Font("LucidaSans", Font.PLAIN, 40);
		jTextAreaNotes.addKeyListener(this);

		scrollpaneNotes = new JScrollPane (jTextAreaNotes);
		scrollpaneNotes.setPreferredSize(Globals.textfieldDimension);
		scrollpaneNotes.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		scrollpaneNotes.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

		macAddressField = new JTextEditorField(
		                      new SeparatedDocument(
		                          /*allowedChars*/ new char[] { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' } ,
		                          12, ':', 2, true),
		                      "",
		                      17);
		//new SeparatedField(6, 2, 2, ':', new char[] { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' } );
		macAddressField.addKeyListener(this);


		// JLabel labelUefiBoot = new JLabel(  configed.getResourceValue("Uefi Boot" )  );
		cbUefiBoot = new JCheckBox( configed.getResourceValue("Uefi Boot" ) );
		cbUefiBoot.setSelected(false);
		cbUefiBoot.setEnabled(true);
		if (!main.getPersistenceController().isWithUEFI())
		{
			cbUefiBoot.setText(configed.getResourceValue("NewClientDialog.boottype_not_activated"));
			cbUefiBoot.setEnabled(false);
		}
		cbUefiBoot.addActionListener(this);



		// JLabel labelWANConfig = new JLabel(  configed.getResourceValue("vpnConfig" )  );
		cbWANConfig = new JCheckBox(configed.getResourceValue("WAN Konfiguration" ));
		cbWANConfig.setSelected(false);
		cbWANConfig.setEnabled(true);
		if (!main.getPersistenceController().isWithWAN())
		{
			cbWANConfig.setText(configed.getResourceValue("NewClientDialog.vpn_not_activated"));
			cbWANConfig.setEnabled(false);
		}
		cbWANConfig.addActionListener(this);


		// cbInstallByShutdown = new JCheckBox(configed.getResourceValue("NewClientDialog.installByShutdown" ));
		// cbInstallByShutdown.setSelected(false);
		// cbInstallByShutdown.setEnabled(true);
		// cbInstallByShutdown.addActionListener(this);
		jLabel_InstallByShutdown = new JLabel(configed.getResourceValue("NewClientDialog.installByShutdown" ));
		btnAktivateInstallByShutdown = new JButton(de.uib.configed.configed.getResourceValue("NewClientDialog.installByShutdown_activate") );
		btnAktivateInstallByShutdown.addActionListener(new ActionListener()	{
			        public void actionPerformed(ActionEvent e)
			        {
				        String clientID = ( (MainFrame) Globals.mainFrame ).getClientID();
				        if ( (clientID != null) && (clientID.length() > 0))
				        {
					        /*
					        ArrayList<String> shutdown_value = ( (ArrayList) main.getPersistenceController()
					        														.getCommonProductPropertyValues( 
					        															new ArrayList(Arrays.asList(clientID)) , 
					        															"opsi-client-agent", 
					        															"on_shutdown_install" 
					        														) 
					        												);
					        											
					        	does not produce the default vale
					        */

					        ArrayList<String> shutdown_value =
					            (ArrayList) main.getPersistenceController().getProductproperties(
					                clientID, "opsi-client-agent")
					            .get("on_shutdown_install");

					        logging.info(this, "shutdown_value " + shutdown_value);

					        if (
					            shutdown_value.size() == 0 ||
					            ((shutdown_value.get(0) != null) && !(shutdown_value.get(0).equals("on")))
					        )
					        {
						        int reply = JOptionPane.showConfirmDialog(
						                        Globals.mainFrame,
						                        configed.getResourceValue("MainFrame.JOptionPane_installByShutdown_question_on" ),
						                        configed.getResourceValue("MainFrame.JOptionPane_installByShutdown_title" ),
						                        JOptionPane.YES_NO_OPTION);
						        if (reply==JOptionPane.YES_OPTION)
						        {
							        main.setInstallByShutdown(clientID, true );
							        main.requestReloadStatesAndActions();
						        }
					        }
				        }
			        }
		        });

		// btnDeaktivateInstallByShutdown = new JButton(configed.getResourceValue("NewClientDialog.installByShutdown_deactivate" ));
		btnDeaktivateInstallByShutdown = new JButton( de.uib.configed.configed.getResourceValue("NewClientDialog.installByShutdown_deactivate"));
		btnDeaktivateInstallByShutdown.setEnabled(true);
		btnDeaktivateInstallByShutdown.addActionListener(new ActionListener()	{
			        public void actionPerformed(ActionEvent e)
			        {
				        String clientID = ( (MainFrame) Globals.mainFrame ).getClientID();
				        if ( (clientID != null) && (clientID.length() > 0))
				        {
					        /*
					        ArrayList<String> shutdown_value = ( (ArrayList) main.getPersistenceController()
					        														.getCommonProductPropertyValues( 
					        															new ArrayList(Arrays.asList(clientID)) , 
					        															"opsi-client-agent", 
					        															"on_shutdown_install" 
					        														) 
					        												);
					        											
					        	does not produce the default value
					        */

					        ArrayList<String> shutdown_value =
					            (ArrayList) main.getPersistenceController().getProductproperties(
					                clientID, "opsi-client-agent")
					            .get("on_shutdown_install");

					        logging.info(this, "shutdown_value " + shutdown_value);

					        if (
					            shutdown_value.size() == 0 ||
					            ((shutdown_value.get(0) != null) && !(shutdown_value.get(0).equals("off")))
					        )
					        {
						        int reply = JOptionPane.showConfirmDialog(
						                        Globals.mainFrame,
						                        configed.getResourceValue("MainFrame.JOptionPane_installByShutdown_question_off" ),
						                        configed.getResourceValue("MainFrame.JOptionPane_installByShutdown_title" ),
						                        JOptionPane.YES_NO_OPTION);
						        if (reply==JOptionPane.YES_OPTION)
						        {
							        main.setInstallByShutdown(clientID, false );
							        main.requestReloadStatesAndActions();
						        }
					        }

				        }
			        }
		        });

		/*
		cbUefiBoot.setEnabled(true); //only for colors, therefore we remove/disable listeners
		cbUefiBoot.addKeyListener(new KeyAdapter(){
				public void keyPressed(KeyEvent e)
				{
					e.consume();
					//logging.info(this, " " + e);
				}
			}
		);

		for (int i = 0; i < cbUefiBoot.getMouseListeners().length; i++)
		{
			cbUefiBoot.removeMouseListener(cbUefiBoot.getMouseListeners()[i]);
	}
		*/




		//cbUefiBoot.setText(configed.getResourceValue("Uefi Boot") /*, STATUS*/);

		jTextFieldOneTimePassword = new JTextEditorField("");
		jTextFieldOneTimePassword.addKeyListener(this);


		/* ------------------------ ANNA */
		layoutClientPane.setHorizontalGroup( layoutClientPane.createParallelGroup()
		                                     /////// HOST
		                                     .addGroup(layoutClientPane.createSequentialGroup()
		                                               .addComponent(labelHost, 0 , GroupLayout.PREFERRED_SIZE , Short.MAX_VALUE )
		                                               .addGap(Globals.minHGapSize,Globals.minHGapSize,Globals.minHGapSize)
		                                              )
		                                     .addGroup(layoutClientPane.createSequentialGroup()
		                                               .addGap(Globals.minHGapSize,Globals.minHGapSize,Globals.minHGapSize)
		                                               .addComponent(labelHostID, 0 , GroupLayout.PREFERRED_SIZE , Short.MAX_VALUE )
		                                               .addGap(Globals.minHGapSize,Globals.minHGapSize,Globals.minHGapSize)
		                                              )
		                                     /////// DESCRIPTION
		                                     .addGroup(layoutClientPane.createSequentialGroup()
		                                               .addGap(Globals.hGapSize,Globals.hGapSize,Globals.hGapSize)
		                                               .addComponent(labelClientDescription, 0 , GroupLayout.PREFERRED_SIZE , Short.MAX_VALUE )
		                                               .addGap(Globals.minHGapSize,Globals.minHGapSize,Globals.minHGapSize)
		                                              )
		                                     .addGroup(layoutClientPane.createSequentialGroup()
		                                               .addGap(Globals.minHGapSize,Globals.minHGapSize,Globals.minHGapSize)
		                                               .addComponent(jTextFieldDescription, 0 , GroupLayout.PREFERRED_SIZE , Short.MAX_VALUE )
		                                               .addGap(Globals.minHGapSize,Globals.minHGapSize,Globals.minHGapSize)
		                                              )
		                                     /////// INVENTORY NUMBER
		                                     .addGroup(layoutClientPane.createSequentialGroup()
		                                               .addGap(Globals.hGapSize,Globals.hGapSize,Globals.hGapSize)
		                                               .addComponent(labelClientInventoryNumber, 0 , GroupLayout.PREFERRED_SIZE , Short.MAX_VALUE )
		                                               .addGap(Globals.minHGapSize,Globals.minHGapSize,Globals.minHGapSize)
		                                              )
		                                     .addGroup(layoutClientPane.createSequentialGroup()
		                                               .addGap(Globals.minHGapSize,Globals.minHGapSize,Globals.minHGapSize)
		                                               .addComponent(jTextFieldInventoryNumber, 0 , GroupLayout.PREFERRED_SIZE , Short.MAX_VALUE )
		                                               .addGap(Globals.minHGapSize,Globals.minHGapSize,Globals.minHGapSize)
		                                              )
		                                     /////// MAC ADDRESS
		                                     .addGroup(layoutClientPane.createSequentialGroup()
		                                               .addGap(Globals.hGapSize,Globals.hGapSize,Globals.hGapSize)
		                                               .addComponent(labelClientMacAddress, 0 , GroupLayout.PREFERRED_SIZE , Short.MAX_VALUE )
		                                               .addGap(Globals.minHGapSize,Globals.minHGapSize,Globals.minHGapSize)
		                                              )
		                                     .addGroup(layoutClientPane.createSequentialGroup()
		                                               .addGap(Globals.minHGapSize,Globals.minHGapSize,Globals.minHGapSize)
		                                               .addComponent(macAddressField,Globals.firstLabelWidth , Globals.firstLabelWidth , Globals.firstLabelWidth )
		                                               .addGap(Globals.minHGapSize,Globals.minHGapSize,Globals.minHGapSize)
		                                              )
		                                     /////// INSTALL BY SHUTDOWN
		                                     .addGroup(layoutClientPane.createSequentialGroup()
		                                               .addGap(Globals.hGapSize,Globals.hGapSize,Globals.hGapSize)
		                                               .addComponent(jLabel_InstallByShutdown/*, 0 , Globals.buttonWidth , Globals.buttonWidth */)
		                                               .addGap(Globals.minHGapSize,Globals.hGapSize,Globals.hGapSize)
		                                               .addComponent(btnAktivateInstallByShutdown) //, 0 , Globals.buttonHeight , Globals.buttonHeight )
		                                               .addGap(Globals.minHGapSize,Globals.minHGapSize,Globals.minHGapSize)
		                                               .addComponent(btnDeaktivateInstallByShutdown)//, 0 , Globals.buttonWidth/4 , Globals.buttonHeight )
		                                               // .addGap(Globals.minHGapSize,Globals.minHGapSize,Globals.minHGapSize)
		                                              )

		                                     /////// UEFI BOOT
		                                     .addGroup(layoutClientPane.createSequentialGroup()
		                                               .addGap(Globals.hGapSize,Globals.hGapSize,Globals.hGapSize)
		                                               .addComponent(cbUefiBoot, 0 , GroupLayout.PREFERRED_SIZE , GroupLayout.PREFERRED_SIZE )
		                                               .addGap(Globals.hGapSize,Globals.hGapSize,Globals.hGapSize)
		                                              )
		                                     /////// WAN CONFIG
		                                     .addGroup(layoutClientPane.createSequentialGroup()
		                                               .addGap(Globals.hGapSize,Globals.hGapSize,Globals.hGapSize)
		                                               .addComponent(cbWANConfig, 0 , GroupLayout.PREFERRED_SIZE , GroupLayout.PREFERRED_SIZE )
		                                               .addGap(Globals.hGapSize,Globals.hGapSize,Globals.hGapSize)
		                                              )


		                                     // .addGap(Globals.hGapSize,Globals.hGapSize,Globals.hGapSize)
		                                     /////// ONE TIME PASSWORD
		                                     .addGroup(layoutClientPane.createSequentialGroup()
		                                               .addGap(Globals.hGapSize,Globals.hGapSize,Globals.hGapSize)
		                                               .addComponent(labelOneTimePassword, 0 , GroupLayout.PREFERRED_SIZE , Short.MAX_VALUE )
		                                               .addGap(Globals.minHGapSize,Globals.minHGapSize,Globals.minHGapSize)
		                                              )
		                                     .addGroup(layoutClientPane.createSequentialGroup()
		                                               .addGap(Globals.minHGapSize,Globals.minHGapSize,Globals.minHGapSize)
		                                               .addComponent(jTextFieldOneTimePassword, 0 , GroupLayout.PREFERRED_SIZE , Short.MAX_VALUE )
		                                               .addGap(Globals.minHGapSize,Globals.minHGapSize,Globals.minHGapSize)
		                                              )
		                                     /////// NOTES
		                                     .addGroup(layoutClientPane.createSequentialGroup()
		                                               .addGap(Globals.hGapSize,Globals.hGapSize,Globals.hGapSize)
		                                               .addComponent(labelClientNotes, 0 , GroupLayout.PREFERRED_SIZE , Short.MAX_VALUE )
		                                               .addGap(Globals.minHGapSize,Globals.minHGapSize,Globals.minHGapSize)
		                                              )
		                                     .addGroup(layoutClientPane.createSequentialGroup()
		                                               .addGap(Globals.minHGapSize,Globals.minHGapSize,Globals.minHGapSize)
		                                               .addComponent(scrollpaneNotes, 0 , GroupLayout.PREFERRED_SIZE , Short.MAX_VALUE )
		                                               .addGap(Globals.minHGapSize,Globals.minHGapSize,Globals.minHGapSize)
		                                              )
		                                   );
		/**/


		/*layoutClientPane.setHorizontalGroup(
			layoutClientPane.createSequentialGroup()
				.addGap(Globals.hGapSize, Globals.hGapSize, Globals.hGapSize)
				.addGroup(layoutClientPane.createParallelGroup()
					.addGroup(layoutClientPane.createSequentialGroup()
						.addComponent(labelHost, 30, 30,  30)
						.addGap(2, 4, 4)
						.addComponent(labelHostID, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,  GroupLayout.PREFERRED_SIZE)
					)
					.addComponent(labelClientDescription, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,   GroupLayout.PREFERRED_SIZE)
					.addComponent(jTextFieldDescription, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,  Short.MAX_VALUE)
					.addComponent(labelClientInventoryNumber, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,   GroupLayout.PREFERRED_SIZE)
					.addComponent(jTextFieldInventoryNumber, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,  Short.MAX_VALUE)
					.addComponent(labelClientMacAddress, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,   GroupLayout.PREFERRED_SIZE)
					.addComponent(macAddressField, 0, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
					.addComponent(cbUefiBoot, 0, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
					.addComponent(labelOneTimePassword, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
					.addComponent(jTextFieldOneTimePassword, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,  Short.MAX_VALUE)
					.addComponent(labelClientNotes, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,   GroupLayout.PREFERRED_SIZE)
					.addComponent(scrollpaneNotes, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,  Short.MAX_VALUE)
					
				)
				.addGap(Globals.hGapSize, Globals.hGapSize, Globals.hGapSize)
			)
		;
		*/

		/* ------------------------ ANNA*/
		layoutClientPane.setVerticalGroup(layoutClientPane.createSequentialGroup()
		                                  /////// HOST
		                                  .addGap(Globals.minVGapSize,Globals.minVGapSize,Globals.minVGapSize)
		                                  .addComponent(labelHost)
		                                  .addComponent(labelHostID, Globals.lineHeight,Globals.lineHeight,Globals.lineHeight)
		                                  /////// DESCRIPTION
		                                  .addGap(Globals.minVGapSize,Globals.minVGapSize,Globals.minVGapSize)
		                                  .addComponent(labelClientDescription)
		                                  .addComponent(jTextFieldDescription, Globals.lineHeight,Globals.lineHeight,Globals.lineHeight)
		                                  /////// INVENTORY NUMBER
		                                  .addGap(Globals.minVGapSize,Globals.minVGapSize,Globals.minVGapSize)
		                                  .addComponent(labelClientInventoryNumber)
		                                  .addComponent(jTextFieldInventoryNumber, Globals.lineHeight,Globals.lineHeight,Globals.lineHeight)
		                                  /////// MAC ADDRESS
		                                  .addGap(Globals.minVGapSize,Globals.minVGapSize,Globals.minVGapSize)
		                                  .addComponent(labelClientMacAddress)
		                                  .addComponent(macAddressField, Globals.lineHeight,Globals.lineHeight,Globals.lineHeight)

		                                  ////// INSTALL BY SHUTDOWN
		                                  .addGroup( layoutClientPane.createParallelGroup(GroupLayout.Alignment.CENTER )
		                                             .addGap(Globals.minVGapSize*2,Globals.minVGapSize*2,Globals.minVGapSize*2)
		                                             .addComponent(jLabel_InstallByShutdown)
		                                             // .addGroup( layoutClientPane.createParallelGroup(GroupLayout.Alignment.CENTER )
		                                             .addGap(Globals.minVGapSize*2,Globals.minVGapSize*2,Globals.minVGapSize*2)
		                                             .addComponent(btnAktivateInstallByShutdown)
		                                             .addGap(Globals.minVGapSize*2,Globals.minVGapSize*2,Globals.minVGapSize*2)
		                                             .addComponent(btnDeaktivateInstallByShutdown)
		                                           )
		                                  /////// UEFI BOOT & WAN Config
		                                  .addGap(Globals.minVGapSize,Globals.minVGapSize,Globals.minVGapSize)
		                                  .addComponent(cbUefiBoot)
		                                  .addGap(Globals.minVGapSize,Globals.minVGapSize,Globals.minVGapSize)
		                                  .addComponent(cbWANConfig)

		                                  /////// ONE TIME PASSWORD
		                                  .addGap(Globals.minVGapSize*2,Globals.minVGapSize*2,Globals.minVGapSize*2)
		                                  .addComponent(labelOneTimePassword)
		                                  .addComponent(jTextFieldOneTimePassword, Globals.lineHeight,Globals.lineHeight,Globals.lineHeight)
		                                  /////// NOTES
		                                  .addGap(Globals.minVGapSize,Globals.minVGapSize,Globals.minVGapSize)
		                                  .addComponent(labelClientNotes)
		                                  .addComponent(scrollpaneNotes, Globals.lineHeight,GroupLayout.PREFERRED_SIZE ,Short.MAX_VALUE)
		                                 );
		/**/

		/*
		layoutClientPane.setVerticalGroup(
			layoutClientPane.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addGroup(layoutClientPane.createSequentialGroup()
				.addGap(Globals.vGapSize, Globals.vGapSize, Globals.vGapSize)
				.addGroup(layoutClientPane.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
					.addComponent(labelHost, 0, GroupLayout.PREFERRED_SIZE,  GroupLayout.PREFERRED_SIZE)
					.addComponent(labelHostID, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,  GroupLayout.PREFERRED_SIZE)
				)
				.addGap(0, Globals.vGapSize/2, Globals.vGapSize)
				.addComponent(labelClientDescription, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,  GroupLayout.PREFERRED_SIZE)
				.addComponent(jTextFieldDescription, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
				.addGap(0, Globals.vGapSize/2, Globals.vGapSize)
				.addComponent(labelClientInventoryNumber, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,  GroupLayout.PREFERRED_SIZE)
				.addComponent(jTextFieldInventoryNumber, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
				.addGap(0, Globals.vGapSize/2, Globals.vGapSize)
				.addComponent(labelClientMacAddress, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,   GroupLayout.PREFERRED_SIZE)
				.addComponent(macAddressField, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,  GroupLayout.PREFERRED_SIZE)
				.addGap(0, Globals.vGapSize/2, Globals.vGapSize)
				.addComponent(cbUefiBoot, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,  GroupLayout.PREFERRED_SIZE)
				.addGap(0, Globals.vGapSize/2, Globals.vGapSize)
				.addComponent(labelOneTimePassword, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,   GroupLayout.PREFERRED_SIZE)
				.addComponent(jTextFieldOneTimePassword, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,   GroupLayout.PREFERRED_SIZE)
				.addGap(0, Globals.vGapSize/2, Globals.vGapSize)
				.addComponent(labelClientNotes, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,   GroupLayout.PREFERRED_SIZE)
				.addComponent(scrollpaneNotes, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,  Short.MAX_VALUE)
				)
			)
		;

		*/

		jPanel3.setBorder(BorderFactory.createEtchedBorder());
		jPanel3.setLayout(new BorderLayout());

		jLabel_Clientname.setFont(Globals.defaultFontBig);
		jLabel_Clientname.setText( configed.getResourceValue("MainFrame.jLabel_Clientname") );
		//jLabel_Productselection.setFont(Globals.defaultFontBig);
		jCheckBoxSorted.setSelected(true);
		jCheckBoxSorted.setText( configed.getResourceValue("MainFrame.jCheckBoxSorted") );
		jCheckBoxSorted.addActionListener(new java.awt.event.ActionListener()
		                                  {
			                                  public void actionPerformed(ActionEvent e)
			                                  {

			                                  }
		                                  });

		jButtonSaveList.setText( configed.getResourceValue("MainFrame.jButtonSaveList") );
		jButtonSaveList.setBackground(Globals.backBlue);
		jButtonSaveList.addActionListener(new java.awt.event.ActionListener()
		                                  {
			                                  public void actionPerformed(ActionEvent e)
			                                  {
				                                  jButtonSaveList_actionPerformed(e);
			                                  }
		                                  });

		jRadioRequiredAll.setMargin(new Insets(0, 0, 0, 0));
		jRadioRequiredAll.setAlignmentY((float) 0.0);
		jRadioRequiredAll.setText( configed.getResourceValue("MainFrame.jRadioRequiredAll") );
		jRadioRequiredOff.setMargin(new Insets(0, 0, 0, 0));
		jRadioRequiredOff.setSelected(true);
		jRadioRequiredOff.setText( configed.getResourceValue("MainFrame.jRadioRequiredOff") );
		jRadioRequiredOff.setToolTipText("");

		jLabelPath.setText( configed.getResourceValue("MainFrame.jLabelPath") );
		jLabel_Hostinfos.setText( configed.getResourceValue("MainFrame.jLabel_Hostinfos") );




		buttonGroupRequired.add(jRadioRequiredAll);
		buttonGroupRequired.add(jRadioRequiredOff);

		jComboBoxProductValues.setBackground(Globals.backBlue);
		jComboBoxProductValues.addActionListener(new java.awt.event.ActionListener()
		        {
			        public void actionPerformed(ActionEvent e)
			        {
				        jComboBoxProductValues_actionPerformed(e);
			        }
		        });


		depotslist.setMaximumSize(new Dimension(200, 400));

		scrollpaneDepotslist = new JScrollPane();
		scrollpaneDepotslist.getViewport().add(depotslist);
		scrollpaneDepotslist.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		scrollpaneDepotslist.setPreferredSize(depotslist.getMaximumSize());


		/*
		depotslist.addMouseListener(new utils.PopupMouseListener(popupDepotList));
		popupDepotList.add(popupCommitChangedDepotSelection);
		popupDepotList.add(popupCancelChangedDepotSelection);

		popupCommitChangedDepotSelection.setText(de.uib.configed.configed.getResourceValue("MainFrame.buttonChangeDepot") ) ;
		popupCommitChangedDepotSelection.setToolTipText(
			de.uib.configed.configed.getResourceValue("MainFrame.buttonChangeDepot.tooltip")
			);
			
		popupCommitChangedDepotSelection.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				logging.debug(this, "actionPerformed " + e);
				main.changeDepotSelection();
			}
	});

		popupCommitChangedDepotSelection.setEnabled(false);

		popupCancelChangedDepotSelection.setText(de.uib.configed.configed.getResourceValue("MainFrame.buttonCancelDepot") ) ;
		popupCancelChangedDepotSelection.setToolTipText(
			de.uib.configed.configed.getResourceValue("MainFrame.buttonCancelDepot.tooltip")
			);
			
		popupCancelChangedDepotSelection.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				logging.debug(this, "actionPerformed " + e);
				main.cancelChangeDepotSelection();
				
			}
	});

		popupCancelChangedDepotSelection.setEnabled(false);
		*/

		//setChangedDepotSelectionActive(false); is not initialized
		//must not be set (otherwise the embedding scrollpane does not scroll)
		//depotslist.setPreferredSize(new Dimension(widthColumnServer, line_height));
		depotslist.setFont(Globals.defaultFont);
		//labelDepotServer.setPreferredSize(new Dimension(widthColumnServer, line_height));

		JLabel labelDepotServer = new JLabel("Depot-Server ");
		labelDepotServer.setOpaque(true);
		//labelDepotServer.setBackground(Globals.backgroundWhite); //backTabsColor);
		labelDepotServer.setBackground(Globals.backLightBlue);
		labelDepotServer.setFont(Globals.defaultFontStandardBold);



		//popupDepots = new JPopupMenu();
		//depotslist.setComponentPopupMenu(popupDepots);

		buttonSelectDepotsWithEqualProperties = new JButton("",
		                                        Globals.createImageIcon("images/equalplus.png", ""));
		buttonSelectDepotsWithEqualProperties.setToolTipText(
		    configed.getResourceValue("MainFrame.buttonSelectDepotsWithEqualProperties")
		);
		Globals.formatButtonSmallText( buttonSelectDepotsWithEqualProperties );
		buttonSelectDepotsWithEqualProperties.addActionListener(this);

		buttonSelectDepotsAll = new JButton("",
		                                    Globals.createImageIcon("images/plusplus.png", ""));
		buttonSelectDepotsAll.setToolTipText(
		    configed.getResourceValue("MainFrame.buttonSelectDepotsAll")
		);
		Globals.formatButtonSmallText( buttonSelectDepotsAll );
		buttonSelectDepotsAll.addActionListener(this);


		/*
		buttonCommitChangedDepotSelection = new IconButton(
			configed.getResourceValue("MainFrame.buttonChangeDepot.tooltip"),
			"images/depot_activate.png",                                        
			"images/depot_activate_disabled.png",
			"images/depot_activate_disabled.png",
			false);
		buttonCommitChangedDepotSelection.setPreferredSize(new Dimension(Globals.squareButtonWidth, Globals.buttonHeight));
		buttonCommitChangedDepotSelection.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					main.changeDepotSelection();
				}
			}
		);

		buttonCancelChangedDepotSelection = new IconButton(
			configed.getResourceValue("MainFrame.buttonCancelDepot.tooltip"),
			"images/cancel22_small.png",
			"images/cancel22_small.png",
			"images/cancel22_small.png",
			false);
		buttonCancelChangedDepotSelection.setPreferredSize(new Dimension(Globals.squareButtonWidth, Globals.buttonHeight));
		buttonCancelChangedDepotSelection.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent e)
				{
					main.cancelChangeDepotSelection();
				}
			}
		);
		*/


		treeClients.setFont(Globals.defaultFont);
		//treeClients.setMaximumSize(new Dimension(Short.MAX_VALUE, Short.MAX_VALUE));

		scrollpaneTreeClients = new JScrollPane();
		/*
			{
				public JScrollBar createVerticalScrollBar()
				{
					//Dimension minDim = new Dimension(10,2);
					final JScrollBar result = new JScrollBar();
					 
					 BoundedRangeModel rangeModel =  new DefaultBoundedRangeModel(2,5,1,40);
					 rangeModel.addChangeListener(
					 	 new ChangeListener()
					 	 {
					 	 	 public void stateChanged( ChangeEvent e )
					 	 	 {
					 	 	 	 logging.info(this, "in " + result + " changeEvent " + e);
					 	 	 }
					 	 }
					 );
					 	 	 
					 result.setModel(rangeModel);
					 
					
					 
					 logging.info(this, " just created");
					 
					 return result;
				}
			};


		*/

		scrollpaneTreeClients.getViewport().add(treeClients);
		scrollpaneTreeClients.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		scrollpaneTreeClients.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
		scrollpaneTreeClients.setPreferredSize(treeClients.getMaximumSize());

		logging.info(this, "scrollpaneTreeClients.getVerticalScrollBar().getMinimum() " +

		             scrollpaneTreeClients.getVerticalScrollBar().getMinimum()

		            );

		logging.info(this, "scrollpaneTreeClients.getVerticalScrollBar().getMinimumSize() " +

		             scrollpaneTreeClients.getVerticalScrollBar().getMinimumSize()

		            );

		//scrollpaneTreeClients.getVerticalScrollBar().setMinimumSize( null ); //new Dimension(2,2) );
		logging.info(this, "scrollpaneTreeClients.getVerticalScrollBar().getMinimumSize() " +

		             scrollpaneTreeClients.getVerticalScrollBar().getMinimumSize()

		            );
		JSplitPane splitpaneClientSelection
		= new JSplitPane(JSplitPane.VERTICAL_SPLIT, false,
		                 scrollpaneDepotslist,
		                 scrollpaneTreeClients);

		logging.info(this, "multidepot " + multidepot);
		if (multidepot)
			splitpaneClientSelection.setDividerLocation( dividerLocationClientTreeMultidepot );
		else
			splitpaneClientSelection.setDividerLocation( dividerLocationClientTreeSingledepot );



		//logging.info(this, "treeClients.getMaximumSize() " + treeClients.getMaximumSize());
		//logging.info(this, "depotslist.getMaximumSize() " + depotslist.getMaximumSize());

		//System.exit(0);
		panelTreeClientSelection = new JPanel();
		GroupLayout layoutPanelTreeClientSelection = new GroupLayout(panelTreeClientSelection);
		panelTreeClientSelection.setLayout(layoutPanelTreeClientSelection);


		layoutPanelTreeClientSelection.setHorizontalGroup(
		    layoutPanelTreeClientSelection.createSequentialGroup()
		    .addGap(Globals.hGapSize/2, Globals.hGapSize/2, Globals.hGapSize/2)
		    .addGroup(layoutPanelTreeClientSelection.createParallelGroup(GroupLayout.Alignment.LEADING)
		              .addGroup(layoutPanelTreeClientSelection.createSequentialGroup()
		                        .addGap(10)
		                        .addComponent(labelDepotServer, 50, GroupLayout.PREFERRED_SIZE,  Short.MAX_VALUE)
		                        .addGap(10)
		                        .addComponent(buttonSelectDepotsWithEqualProperties, Globals.squareButtonWidth, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
		                        .addComponent(buttonSelectDepotsAll, Globals.squareButtonWidth, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
		                        //.addComponent(buttonCommitChangedDepotSelection, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
		                        //.addComponent(buttonCancelChangedDepotSelection, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
		                        .addGap(10, 10, 10)
		                       )
		              .addComponent(splitpaneClientSelection, minHSizeTreePanel, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
		              //.addComponent(scrollpaneDepotslist, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
		              //.addComponent(scrollpaneTreeClients, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
		             )
		    //.addGap(Globals.hGapSize/2, Globals.hGapSize/2, Globals.hGapSize/2)
		    //.addComponent(groupActionPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
		    .addGap(Globals.hGapSize/2, Globals.hGapSize/2, Globals.hGapSize/2)
		)
		;

		layoutPanelTreeClientSelection.setVerticalGroup(
		    layoutPanelTreeClientSelection.createParallelGroup(GroupLayout.Alignment.LEADING)
		    //.addComponent(groupActionPanel,  Globals.vGapSize/2, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)

		    .addGroup(layoutPanelTreeClientSelection.createSequentialGroup()
		              .addGap(Globals.vGapSize/2, Globals.vGapSize/2, Globals.vGapSize/2)
		              .addGroup(layoutPanelTreeClientSelection.createParallelGroup(GroupLayout.Alignment.CENTER)
		                        .addComponent(labelDepotServer, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
		                        .addComponent(buttonSelectDepotsWithEqualProperties,  GroupLayout.Alignment.TRAILING, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
		                        .addComponent(buttonSelectDepotsAll,  GroupLayout.Alignment.TRAILING, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
		                        //.addComponent(buttonCommitChangedDepotSelection, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
		                        //.addComponent(buttonCancelChangedDepotSelection, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
		                       )
		              .addComponent(splitpaneClientSelection, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
		              //.addComponent(scrollpaneDepotslist, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
		              //.addComponent(scrollpaneTreeClients, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
		             )
		)
		;


		jButtonServerConfiguration = new JButton ("", Globals.createImageIcon("images/opsiconsole_deselected.png", "" ));
		jButtonServerConfiguration.setSelectedIcon (Globals.createImageIcon("images/opsiconsole.png", "" ));
		jButtonServerConfiguration.setPreferredSize(Globals.modeSwitchDimension);
		jButtonServerConfiguration.setToolTipText(configed.getResourceValue("MainFrame.labelServerConfiguration"));

		jButtonDepotsConfiguration = new JButton ("", Globals.createImageIcon("images/opsidepots_deselected.png", "" ));
		jButtonDepotsConfiguration.setSelectedIcon (Globals.createImageIcon("images/opsidepots.png", "" ));
		jButtonDepotsConfiguration.setPreferredSize(Globals.modeSwitchDimension);
		jButtonDepotsConfiguration.setToolTipText(configed.getResourceValue("MainFrame.labelDepotsConfiguration"));

		jButtonClientsConfiguration = new JButton ("", Globals.createImageIcon("images/opsiclients_deselected.png", "" ));
		jButtonClientsConfiguration.setSelectedIcon (Globals.createImageIcon("images/opsiclients.png", "" ));
		jButtonClientsConfiguration.setPreferredSize(Globals.modeSwitchDimension);
		jButtonClientsConfiguration.setToolTipText(configed.getResourceValue("MainFrame.labelClientsConfiguration"));

		jButtonLicences = new JButton ("", Globals.createImageIcon("images/licences_deselected.png", ""));
		jButtonLicences.setEnabled(false);
		jButtonLicences.setSelectedIcon (Globals.createImageIcon("images/licences.png", "" ));
		jButtonLicences.setPreferredSize(Globals.modeSwitchDimension);
		jButtonLicences.setToolTipText(configed.getResourceValue("MainFrame.labelLicences"));

		jButtonServerConfiguration.addActionListener ( this );
		jButtonDepotsConfiguration.addActionListener ( this );
		jButtonClientsConfiguration.addActionListener ( this );
		jButtonLicences.addActionListener ( this );

		jButtonWorkOnGroups = new JButton( "", Globals.createImageIcon("images/group_all_unselected_40.png", "" ));
		jButtonWorkOnGroups.setSelectedIcon( Globals.createImageIcon("images/group_all_selected_40.png", "" ) );
		jButtonWorkOnGroups.setPreferredSize(Globals.modeSwitchDimension);
		jButtonWorkOnGroups.setToolTipText(configed.getResourceValue("MainFrame.labelWorkOnGroups"));

		jButtonWorkOnGroups.setEnabled(
		    //main.getPersistenceController().isWithScalability1()
		    //||
		    main.getPersistenceController().isWithLocalImaging()
		    //true
		);
		jButtonWorkOnGroups.addActionListener(this);



		jButtonWorkOnProducts = new JButton( "", Globals.createImageIcon("images/packagebutton.png", "" ));
		jButtonWorkOnProducts.setSelectedIcon( Globals.createImageIcon("images/packagebutton.png", "" ) );
		jButtonWorkOnProducts.setPreferredSize(Globals.modeSwitchDimension);
		jButtonWorkOnProducts.setToolTipText(configed.getResourceValue("MainFrame.labelWorkOnProducts"));

		/*
		jButtonWorkOnProducts.setEnabled(
			main.getPersistenceController().isWithScalability1()
			||
			main.getPersistenceController().isWithLocalImaging()
		);
		*/
		jButtonWorkOnProducts.addActionListener(this);



		iconPaneTargets = new JPanel();
		iconPaneTargets.setBorder(new LineBorder(Globals.blueGrey, 1, true));

		GroupLayout layoutIconPaneTargets = new GroupLayout(iconPaneTargets);
		iconPaneTargets.setLayout(layoutIconPaneTargets);

		layoutIconPaneTargets.setHorizontalGroup(
		    layoutIconPaneTargets.createParallelGroup(GroupLayout.Alignment.CENTER)
		    .addGroup(layoutIconPaneTargets.createSequentialGroup()
		              .addGap(Globals.hGapSize, Globals.hGapSize, Globals.hGapSize)
		              .addComponent(jButtonClientsConfiguration, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
		              .addGap(Globals.hGapSize, Globals.hGapSize, Globals.hGapSize)
		              //.addComponent(jButtonWorkOnGroups, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
		              //.addGap(Globals.hGapSize, Globals.hGapSize, Globals.hGapSize)
		              .addComponent(jButtonDepotsConfiguration, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
		              .addGap(Globals.hGapSize, Globals.hGapSize, Globals.hGapSize)
		              .addComponent(jButtonServerConfiguration, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
		              .addGap(Globals.hGapSize, Globals.hGapSize, Globals.hGapSize)
		             )
		);
		layoutIconPaneTargets.setVerticalGroup(
		    layoutIconPaneTargets.createParallelGroup(GroupLayout.Alignment.LEADING)
		    .addGroup(layoutIconPaneTargets.createSequentialGroup()
		              .addGap(Globals.vGapSize/2, Globals.vGapSize/2, Globals.vGapSize/2)
		              .addGroup(layoutIconPaneTargets.createParallelGroup(GroupLayout.Alignment.BASELINE)
		                        .addComponent(jButtonClientsConfiguration, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
		                        .addComponent(jButtonDepotsConfiguration, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
		                        .addComponent(jButtonServerConfiguration, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
		                        //.addComponent(jButtonWorkOnGroups, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
		                       )
		              .addGap(Globals.vGapSize/2, Globals.vGapSize/2, Globals.vGapSize/2)
		             )
		);


		iconPaneExtraFrames = new JPanel();
		iconPaneExtraFrames.setBorder(new LineBorder(Globals.blueGrey, 1, true));

		GroupLayout layoutIconPaneExtraFrames = new GroupLayout(iconPaneExtraFrames);
		iconPaneExtraFrames.setLayout(layoutIconPaneExtraFrames);

		layoutIconPaneExtraFrames.setHorizontalGroup(
		    layoutIconPaneExtraFrames.createParallelGroup(GroupLayout.Alignment.CENTER)
		    .addGroup(layoutIconPaneExtraFrames.createSequentialGroup()
		              .addGap(Globals.hGapSize, Globals.hGapSize, Globals.hGapSize)
		              .addComponent(jButtonWorkOnGroups, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
		              .addGap(Globals.hGapSize, Globals.hGapSize, Globals.hGapSize)
		              .addComponent(jButtonWorkOnProducts, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
		              .addGap(Globals.hGapSize, Globals.hGapSize, Globals.hGapSize)
		              .addComponent(jButtonLicences, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
		              .addGap(Globals.hGapSize, Globals.hGapSize, Globals.hGapSize)
		             )
		);
		layoutIconPaneExtraFrames.setVerticalGroup(
		    layoutIconPaneExtraFrames.createParallelGroup(GroupLayout.Alignment.LEADING)
		    .addGroup(layoutIconPaneExtraFrames.createSequentialGroup()
		              .addGap(Globals.vGapSize/2, Globals.vGapSize/2, Globals.vGapSize/2)
		              .addGroup(layoutIconPaneExtraFrames.createParallelGroup(GroupLayout.Alignment.BASELINE)
		                        .addComponent(jButtonWorkOnGroups, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
		                        .addComponent(jButtonWorkOnProducts, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
		                        .addComponent(jButtonLicences, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
		                       )
		              .addGap(Globals.vGapSize/2, Globals.vGapSize/2, Globals.vGapSize/2)
		             )
		);


		iconPane0 = new JPanel();

		GroupLayout layoutIconPane0 = new GroupLayout(iconPane0);
		iconPane0.setLayout(layoutIconPane0);

		layoutIconPane0.setHorizontalGroup(
		    layoutIconPane0.createParallelGroup(GroupLayout.Alignment.LEADING)
		    .addGroup(layoutIconPane0.createSequentialGroup()
		              .addGap(Globals.hGapSize, Globals.hGapSize, Globals.hGapSize)
		              .addComponent(iconPaneTargets, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
		              .addGap(Globals.hGapSize, Globals.hGapSize, Globals.hGapSize)
		              .addComponent(iconPaneExtraFrames, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
		              .addGap(Globals.hGapSize, Globals.hGapSize, Globals.hGapSize)
		             )
		);
		layoutIconPane0.setVerticalGroup(
		    layoutIconPane0.createParallelGroup(GroupLayout.Alignment.CENTER)
		    .addGroup(layoutIconPane0.createSequentialGroup()
		              .addGap(Globals.vGapSize/2, Globals.vGapSize/2, Globals.vGapSize/2)
		              .addGroup(layoutIconPane0.createParallelGroup(GroupLayout.Alignment.CENTER)
		                        .addComponent(iconPaneTargets, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
		                        .addComponent(iconPaneExtraFrames, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
		                       )
		              .addGap(Globals.vGapSize/2, Globals.vGapSize/2, Globals.vGapSize/2)
		             )
		);


		setupIcons1();
		iconPane1 = new JPanel();

		GroupLayout layoutIconPane1 = new GroupLayout(iconPane1);
		iconPane1.setLayout(layoutIconPane1);

		layoutIconPane1.setHorizontalGroup(
		    layoutIconPane1.createParallelGroup(GroupLayout.Alignment.LEADING)
		    .addGroup(layoutIconPane1.createSequentialGroup()
		              .addGap(Globals.hGapSize/2, Globals.hGapSize, Globals.hGapSize)
		              //.addComponent(buttonWindowStack, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
		              //.addGap(Globals.hGapSize/2, Globals.hGapSize/2,  Globals.hGapSize/2)
		              .addComponent(iconButtonReload, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
		              .addGap(Globals.hGapSize/2, Globals.hGapSize/2, Globals.hGapSize/2)
		              .addComponent(iconButtonReloadLicenses, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
		              .addGap(Globals.hGapSize/2, Globals.hGapSize/2, Globals.hGapSize/2)
		              .addComponent(iconButtonNewClient, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
		              .addGap(Globals.hGapSize/2, Globals.hGapSize/2, Globals.hGapSize/2)
		              //.addComponent(iconButtonSaveGroup, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
		              //.addGap(Globals.hGapSize/2, Globals.hGapSize/2, Globals.hGapSize/2)
		              .addComponent(iconButtonSetGroup, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
		              .addGap(Globals.hGapSize/2, Globals.hGapSize/2, Globals.hGapSize/2)
		              .addComponent(iconButtonSaveConfiguration, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
		              //.addGap(2, 2, 2)
		              //.addComponent(iconButtonCancelChanges, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
		              //.addGap(2, 2, 2)
		              .addGap(Globals.hGapSize/2, Globals.hGapSize/2, Globals.hGapSize/2)
		              .addComponent(iconButtonToggleClientFilter, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
		              .addGap(Globals.hGapSize/2, Globals.hGapSize/2, Globals.hGapSize/2)
		              .addComponent(iconButtonReachableInfo, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
		              .addGap(Globals.hGapSize/2, Globals.hGapSize/2, Globals.hGapSize/2)
		              .addComponent(iconButtonSessionInfo, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
		              .addGap(Globals.hGapSize/2, 2*Globals.hGapSize, 2*Globals.hGapSize)
		              .addComponent(proceeding, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
		              .addGap(Globals.hGapSize/2, Globals.hGapSize/2, Globals.hGapSize/2)
		             )
		);
		layoutIconPane1.setVerticalGroup(
		    layoutIconPane1.createParallelGroup(GroupLayout.Alignment.LEADING)
		    .addGroup(layoutIconPane1.createSequentialGroup()
		              .addGap(Globals.vGapSize/2, Globals.vGapSize/2, Globals.vGapSize/2)
		              .addGroup(layoutIconPane1.createParallelGroup(GroupLayout.Alignment.CENTER)
		                        //.addComponent(buttonWindowStack, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
		                        .addComponent(iconButtonReload, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
		                        .addComponent(iconButtonReloadLicenses, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
		                        .addComponent(iconButtonNewClient, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
		                        //.addComponent(iconButtonSaveGroup, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
		                        .addComponent(iconButtonSetGroup, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
		                        .addComponent(iconButtonSaveConfiguration, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
		                        //.addComponent(iconButtonCancelChanges, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
		                        .addComponent(iconButtonReachableInfo, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
		                        .addComponent(iconButtonSessionInfo, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
		                        .addComponent(iconButtonToggleClientFilter, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
		                        .addComponent(proceeding, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
		                       )
		              .addGap(Globals.vGapSize/2, Globals.vGapSize/2, Globals.vGapSize/2)
		             )
		);



		/*
		iconBarPane = new JPanel();

		FlowLayout flowLayoutIconBarPane = new FlowLayout();
		flowLayoutIconBarPane.setAlignment(FlowLayout.LEFT);
		iconBarPane = new JPanel(flowLayoutIconBarPane);

		iconBarPane.add(iconPane1);
		iconBarPane.add(iconPane0);

		*/

		iconBarPane = new JPanel();
		iconBarPane.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();

		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 1.0;
		c.gridx = 0;
		c.gridy = 0;
		iconBarPane.add(iconPane1, c);

		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 0.0;
		c.gridx = 1;
		c.gridy = 0;
		iconBarPane.add(iconPane0, c);


		JSplitPane centralPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, false,
		                                        panelTreeClientSelection,
		                                        jTabbedPaneConfigPanes);
		centralPane.setDividerLocation(dividerLocationCentralPane);




		// statusPane

		statusPane = new HostsStatusPanel();


		allPane.add(iconBarPane, BorderLayout.NORTH);
		allPane.add( centralPane, BorderLayout.CENTER);
		allPane.add( statusPane, BorderLayout.SOUTH);

		/*
		if we use a GroupLayout instead,
		correct scrolling in centralPane is prevented; 

		contentLayout.setHorizontalGroup(
			contentLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addComponent(iconBarPane, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,  Short.MAX_VALUE)
				.addComponent(centralPane,  GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,  Short.MAX_VALUE)
				.addComponent(statusPane, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,  Short.MAX_VALUE)
			)
		;

		contentLayout.setVerticalGroup(
			contentLayout.createSequentialGroup()
				.addGap(Globals.hGapSize/2, Globals.hGapSize/2, Globals.hGapSize/2)
				.addComponent(iconBarPane, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,  GroupLayout.PREFERRED_SIZE)
				.addGap(Globals.hGapSize/2, Globals.hGapSize/2, Globals.hGapSize/2)
				.addComponent(centralPane, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,  Short.MAX_VALUE)
				.addGap(Globals.hGapSize/2, Globals.hGapSize/2, Globals.hGapSize/2)
				.addComponent(statusPane, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,  GroupLayout.PREFERRED_SIZE)
				.addGap(Globals.hGapSize/2, Globals.hGapSize/2, Globals.hGapSize/2)
			)
		;
		*/


		// tab panes

		jTabbedPaneConfigPanes.addChangeListener(new javax.swing.event.ChangeListener()
		        {
			        public void stateChanged(ChangeEvent e)
			        {
				        // report state change request to
				        int visualIndex =  jTabbedPaneConfigPanes.getSelectedIndex();

				        // report state change request to controller
				        //if ( localboot_productPropertiesPanel != null)
				        //	localboot_productPropertiesPanel.stopEditing();

				        //if (netboot_productPropertiesPanel != null)
				        //	netboot_productPropertiesPanel.stopEditing();

				        main.setViewIndex(visualIndex);

				        //System.out.println ("--------------------------- new view index resulting "  + newStateIndex );

				        // retrieve the state index finally produced by main
				        int newStateIndex = main.getViewIndex();



				        //if the controller did not accept the new index  set it back
				        //observe that we get a recursion since we initiate  another state change
				        //the recursion breaks since main.setViewIndex does not yield a different value
				        if (visualIndex != newStateIndex)
				        {
					        jTabbedPaneConfigPanes.setSelectedIndex( newStateIndex );
				        }
			        }
		        });

		// --- panel_Clientselection

		panelClientlist.addMouseListener(new utils.PopupMouseListener(popupClients));





		panel_Clientselection =
		    new JSplitPane ( JSplitPane.HORIZONTAL_SPLIT,
		                     panelClientlist,
		                     clientPane
		                   );
		/*new HorizontalPositioner
		(     
			 panelClientlist,
			 new SurroundPanel( jPanel_PCInfo)
		)*/

		/*
		JScrollPane scrollpaneClientSelection = new JScrollPane(
								panel_Clientselection,
								ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER,
								ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS
								);
		*/


		/*
		jTabbedPaneConfigPanes.addTab
			( 
			configed.getResourceValue("MainFrame.panel_Clientselection"),
			Globals.createImageIcon("images/clientselection.png", "" ),
			panel_Clientselection
			//scrollpaneClientSelection
			);
		*/

		jTabbedPaneConfigPanes.insertTab
		(
		    configed.getResourceValue("MainFrame.panel_Clientselection"),
		    Globals.createImageIcon("images/clientselection.png", "" ),
		    panel_Clientselection,
		    "",
		    ConfigedMain.viewClients
		);


		panel_LocalbootProductsettings = new PanelGroupedProductSettings(configed.getResourceValue("MainFrame.panel_LocalbootProductsettings"),main, main.getDisplayFieldsLocalbootProducts());


		/*
		panel_LocalbootProductsettings 
			= new JSplitPane ( JSplitPane.HORIZONTAL_SPLIT,
				jScrollPane_LocalbootProductlist,
				//new JScrollPane(localbootProductInfo)
				localbootProductInfo
				);
		*/
		//panel_LocalbootProductsettings.setDividerLocation(fwidth_lefthanded - splitterLeftRight);


		panel_NetbootProductsettings  = new PanelProductSettings(configed.getResourceValue("MainFrame.panel_NetbootProductsettings"), main, main.getDisplayFieldsNetbootProducts())
		                                {
			                                @Override
			                                protected void init()
			                                {
				                                super.init();
				                                //subOpsiClientdEvent.setVisible(false);
				                                showPopupOpsiclientdEvent(false);
			                                }

		                                }
		                                ;

		/*
		jTabbedPaneConfigPanes.addTab
			( 
			configed.getResourceValue("MainFrame.panel_LocalbootProductsettings"),
			Globals.createImageIcon("images/package.png", "" ),
			panel_LocalbootProductsettings
			
			);
		*/

		jTabbedPaneConfigPanes.insertTab
		(
		    configed.getResourceValue("MainFrame.panel_LocalbootProductsettings"),
		    Globals.createImageIcon("images/package.png", "" ),
		    panel_LocalbootProductsettings,
		    "",
		    ConfigedMain.viewLocalbootProducts
		);


		/*
		jTabbedPaneConfigPanes.addTab
			( 
			configed.getResourceValue("MainFrame.panel_NetbootProductsettings"),
			Globals.createImageIcon("images/bootimage.png", "" ),
			panel_NetbootProductsettings
			
			);
		*/

		jTabbedPaneConfigPanes.insertTab
		(
		    configed.getResourceValue("MainFrame.panel_NetbootProductsettings"),
		    Globals.createImageIcon("images/bootimage.png", "" ),
		    panel_NetbootProductsettings,
		    "",
		    ConfigedMain.viewNetbootProducts
		);


		panel_HostConfig = new PanelHostConfig()
		                   {
			                   @Override
			                   protected void reloadHostConfig()
			                   {
				                   super.reloadHostConfig();
				                   main.cancelChanges();

				                   main.getPersistenceController().configOptionsRequestRefresh();
				                   //main.requestReloadConfigsForSelectedClients();
				                   main.getPersistenceController().hostConfigsRequestRefresh();
				                   main.resetView(ConfigedMain.viewNetworkconfiguration);
			                   }


			                   //overwrite in subclasses
			                   protected void saveHostConfig()
			                   {
				                   super.saveHostConfig();
				                   main.checkSaveAll(false);
			                   }

		                   };

		panel_HostConfig.registerDataChangedObserver(main.getGeneralDataChangedKeeper());

		/*
		jTabbedPaneConfigPanes.addTab
			( 
			configed.getResourceValue("MainFrame.jPanel_NetworkConfig"),
			Globals.createImageIcon("images/config_pro.png", "" ),
			panel_HostConfig
			);
		*/

		jTabbedPaneConfigPanes.insertTab
		(
		    configed.getResourceValue("MainFrame.jPanel_NetworkConfig"),
		    Globals.createImageIcon("images/config_pro.png", "" ),
		    panel_HostConfig,
		    "",
		    ConfigedMain.viewNetworkconfiguration
		);
		//System.out.println( " -- getTabIndex  " + (configed.getResourceValue("MainFrame.jPanel_NetworkConfig") ) );
		//System.out.println( " --  =  " + getTabIndex  (configed.getResourceValue("MainFrame.jPanel_NetworkConfig") ) );


		labelNoHardware = new JLabel ();
		labelNoHardware.setFont(Globals.defaultFontBig);


		showHardwareLog_NotFound = new de.uib.configed.gui.hwinfopage.PanelHWInfo(main);
		showHardwareLog_NotFound.add (labelNoHardware);
		showHardwareLog_NotFound.setBackground(Globals.backgroundLightGrey);

		showHSoftwareLog_Available =new PanelSWInfo(main){
			                            @Override
			                            protected void reload()
			                            {
				                            super.reload();
				                            main.clearSwInfo();
				                            main.getPersistenceController().installedSoftwareInformationRequestRefresh();
				                            main.getPersistenceController().softwareAuditOnClientsRequestRefresh();
				                            main.resetView(ConfigedMain.viewSoftwareInfo);
			                            }
		                            };

		/*
		jTabbedPaneConfigPanes.addTab
			( 
			configed.getResourceValue("MainFrame.jPanel_hardwareLog"),
			Globals.createImageIcon("images/hwaudit.png", "" ),
			showHardwareLog
			);
		*/

		jTabbedPaneConfigPanes.insertTab
		(
		    configed.getResourceValue("MainFrame.jPanel_hardwareLog"),
		    Globals.createImageIcon("images/hwaudit.png", "" ),
		    showHardwareLog,
		    "",
		    ConfigedMain.viewHardwareInfo
		);


		labelNoSoftware = new JLabel ();
		labelNoSoftware.setFont(Globals.defaultFontBig);
		showSoftwareLog_NotFound = new JPanel(new FlowLayout());
		showSoftwareLog_NotFound.add (labelNoSoftware);
		showSoftwareLog_NotFound.setBackground(Globals.backgroundLightGrey);

		showSoftwareLog = showSoftwareLog_NotFound;

		/*
		jTabbedPaneConfigPanes.addTab(
			configed.getResourceValue("MainFrame.jPanel_softwareLog"), 
			Globals.createImageIcon("images/swaudit.png", "" ),
			showSoftwareLog
			);
		*/

		jTabbedPaneConfigPanes.insertTab(
		    configed.getResourceValue("MainFrame.jPanel_softwareLog"),
		    Globals.createImageIcon("images/swaudit.png", "" ),
		    showSoftwareLog,
		    "",
		    ConfigedMain.viewSoftwareInfo
		);



		showLogfiles = new PanelTabbedDocuments(Globals.logtypes,
		                                        //null)
		                                        configed.getResourceValue("MainFrame.DefaultTextForLogfiles"))
		               {
			               @Override
			               public void loadDocument(String logtype)
			               {
			               	 super.loadDocument(logtype);
			               	 logging.info(this, "loadDocument logtype " + logtype);
				               setUpdatedLogfilePanel(logtype);
			               }
		               };

		/*
		jTabbedPaneConfigPanes.addTab(
			configed.getResourceValue("MainFrame.jPanel_logfiles"), 
			Globals.createImageIcon("images/logfile.png", "" ),
			showLogfiles
			);
		*/

		jTabbedPaneConfigPanes.insertTab(
		    configed.getResourceValue("MainFrame.jPanel_logfiles"),
		    Globals.createImageIcon("images/logfile.png", "" ),
		    showLogfiles,
		    "",
		    ConfigedMain.viewLog
		);



		showLogfiles.addChangeListener(new javax.swing.event.ChangeListener()
		                               {
			                               public void stateChanged(ChangeEvent e)
			                               {
				                               //logging.debug(this, " stateChanged " + e);
				                               logging.debug(this, " new logfiles tabindex " + showLogfiles.getSelectedIndex());

				                               String logtype = Globals.logtypes[showLogfiles.getSelectedIndex()];

				                               //logfile empty?
				                               if (!main.logfileExists(logtype))
					                               setUpdatedLogfilePanel(logtype);

			                               }
		                               }
		                              );


		panel_ProductProperties = new PanelProductProperties(main);
		panel_ProductProperties.propertiesPanel.registerDataChangedObserver(main.getGeneralDataChangedKeeper());

		/*
		jTabbedPaneConfigPanes.addTab
			( 
			configed.getResourceValue("MainFrame.panel_ProductGlobalProperties"),
			Globals.createImageIcon("images/config_pro.png", "" ),
			panel_ProductProperties
			);
		*/

		jTabbedPaneConfigPanes.insertTab
		(
		    configed.getResourceValue("MainFrame.panel_ProductGlobalProperties"),
		    Globals.createImageIcon("images/config_pro.png", "" ),
		    panel_ProductProperties,
		    "",
		    ConfigedMain.viewProductProperties
		);

		logging.info(this, "added tab  " + configed.getResourceValue("MainFrame.panel_ProductGlobalProperties")
		             + " index "
		             + jTabbedPaneConfigPanes.indexOfTab(configed.getResourceValue("MainFrame.panel_ProductGlobalProperties"))
		            );


		panel_HostProperties = new PanelHostProperties();
		panel_HostProperties.registerDataChangedObserver(main.getGeneralDataChangedKeeper());

		/*
		jTabbedPaneConfigPanes.addTab
			( 
			configed.getResourceValue("MainFrame.jPanel_HostProperties"),
			Globals.createImageIcon("images/config_pro.png", "" ),
			panel_HostProperties
			);
		*/

		jTabbedPaneConfigPanes.insertTab
		(
		    configed.getResourceValue("MainFrame.jPanel_HostProperties"),
		    Globals.createImageIcon("images/config_pro.png", "" ),
		    panel_HostProperties,
		    "",
		    ConfigedMain.viewHostProperties
		);


		logging.info(this, "added tab  " + configed.getResourceValue("MainFrame.jPanel_HostProperties")
		             + " index "
		             + jTabbedPaneConfigPanes.indexOfTab(configed.getResourceValue("MainFrame.jPanel_HostProperties"))
		            );





		jTabbedPaneConfigPanes.setSelectedIndex(0);


		setTitle (main.getAppTitle());


		Containership csjPanel_allContent = new Containership (allPane);

		/*
		csjPanel_allContent.doForAllContainedCompisOfClass 
		("setDragEnabled", new Object[]{true},new Class[] {boolean.class}, javax.swing.text.JTextComponent.class);
		*/

		// set colors of panels
		csjPanel_allContent.doForAllContainedCompisOfClass
		("setBackground", new Object[]{Globals.backLightBlue}, JPanel.class);

		//groupActionPanel.setBackground(Globals.backgroundWhite);


		Containership cspanel_LocalbootProductsettings = new Containership (panel_LocalbootProductsettings);
		cspanel_LocalbootProductsettings.doForAllContainedCompisOfClass ("setBackground", new Object[]{Globals.backgroundLightGrey},
		        VerticalPositioner.class); //JPanel.class);
		panel_LocalbootProductsettings.setBackground(Globals.backgroundLightGrey);

		Containership cspanel_NetbootProductsettings = new Containership (panel_NetbootProductsettings);
		cspanel_NetbootProductsettings.doForAllContainedCompisOfClass ("setBackground", new Object[]{Globals.backgroundLightGrey},
		        VerticalPositioner.class); //JPanel.class);
		panel_NetbootProductsettings.setBackground(Globals.backgroundLightGrey);

		//iconPane0.setBackground(Globals.backgroundLightGrey);
		iconPane0.setBackground(Globals.backLightBlue);
		iconBarPane.setBackground(Globals.backLightBlue);
		iconPane1.setBackground(Globals.backLightBlue);
		panelTreeClientSelection.setBackground(Globals.backLightBlue);
		statusPane.setBackground(Globals.backLightBlue);
		//clientPane.setBackground(Globals.backLightBlue);

		setSize(fwidth, fheight);
		glass.setSize(fwidth, fheight);
		glass.setVisible(true);
		glass.setOpaque(true);
		setGlassPane(glass);

		pack();

	}


	public void startSizing(int width, int height)
	{
		logging.info(this, "startSizing width, height " + width + ", " + height);
		setSize(width, height );


		//splitterPanelClientSelection = fwidth_lefthanded + splitterLeftRight;
		splitterPanelClientSelection = panel_Clientselection.getSize().width - prefClientPaneW;
		panel_Clientselection.setDividerLocation(splitterPanelClientSelection);

		//panel_Clientselection.setDividerLocation(splitterPanelClientSelection);
	}



	public void showPopupClients()
	{

		Rectangle rect = panelClientlist.getCellRect(panelClientlist.getSelectedRow(), 0, false);
		popupClients.show(panelClientlist, rect.x + (rect.width / 2), rect.y + (rect.height/2));
	}


	public void setConfigPanesEnabled(boolean b)
	{
		for (int i = 0; i < jTabbedPaneConfigPanes.getTabCount(); i++)
		{
			jTabbedPaneConfigPanes.setEnabledAt(i, b);
		}
	}


	public void setVisualViewIndex ( int i)
	{
		if ( i >= 0 &&  i <  jTabbedPaneConfigPanes.getTabCount())
		{
			jTabbedPaneConfigPanes.setSelectedIndex( i);
		}
	}

	public void setConfigPaneEnabled(int tabindex, boolean b)
	{
		jTabbedPaneConfigPanes.setEnabledAt(tabindex, b);
	}

	public int getTabIndex(String tabname)
	{
		return(jTabbedPaneConfigPanes.indexOfTab(tabname));
	}


	//-- helper methods for interaction
	public void saveConfigurationsSetEnabled(boolean b)
	{
		// System.out.println (" ------- we should now show in the menu that data have changed");

		if (Globals.isGlobalReadOnly() && b) return;

		jMenuFileSaveConfigurations.setEnabled(b);
		iconButtonSaveConfiguration.setEnabled(b);
		iconButtonCancelChanges.setEnabled(b);
	}

	public void saveGroupSetEnabled(boolean b)
	{
		//jMenuClientselectionSaveGroup.setEnabled(b);
		iconButtonSaveGroup.setEnabled(b);
	}


	//----------------------------------------------------------------------------------------
	//action methods for visual interactions
	public void wakeOnLanActionWithDelay(int secs)
	{
		//main.wakeSelectedClients();
		main.wakeSelectedClientsWithDelay(secs);
	}

	public void wakeOnLanAction()
	{
		main.wakeSelectedClients();
	}


	public void deletePackageCachesAction()
	{
		main.deletePackageCachesOfSelectedClients();
	}


	public void fireOpsiclientdEventAction(String event)
	{
		main.fireOpsiclientdEventOnSelectedClients(event);
	}

	public void showPopupOnClientsAction ()
	{
		FEditText fText = new FEditText("", configed.getResourceValue("MainFrame.writePopupMessage"))
		                  {
			                  protected void commit(){
				                  super.commit();
				                  main.showPopupOnSelectedClients(getText());
			                  }
		                  };

		fText.setAreaDimension(new Dimension(350, 150));
		fText.init();
		fText.setVisible(true);
		fText.centerOn(this);
	}

	public void shutdownClientsAction()
	{
		main.shutdownSelectedClients();
	}

	public void rebootClientsAction()
	{
		main.rebootSelectedClients();
	}

	public void deleteClientAction()
	{
		main.deleteSelectedClients();
	}

	public void freeLicencesAction()
	{
		logging.info(this, "freeLicencesAction ");
		main.freeAllPossibleLicencesForSelectedClients();
	}

	public void remoteControlAction()
	{
		logging.debug(this, "jMenuRemoteControl");
		main.startRemoteControlForSelectedClients();
	}
	/**
	* Calls method from configedMain to start the execution of given command
	* @param SSHCommand command
	*/
	public void remoteSSHExecAction(SSHCommand command)
	{
		logging.debug(this, "jMenuRemoteSSHExecAction");
		main.startSSHOpsiServerExec(command);
	}

	/**
	* Calls method from configedMain to start the terminal
	*/
	public void remoteSSHTerminalAction()
	{
		logging.debug(this, "jMenuRemoteSSHTerminalAction");
		main.startSSHOpsiServerTerminal();
	}
	/**
	* Calls method from configedMain to start the config dialog
	*/
	public void startSSHConfigAction()
	{
		logging.debug(this, "jMenuSSHConfigAction");
		main.startSSHConfigDialog();
	}
	/**
	* Calls method from configedMain to start the command control dialog
	*/
	public void startSSHControlAction()
	{
		logging.debug(this, "jMenuSSHControlAction");
		main.startSSHControlDialog();
	}

	public void toggleClientFilterAction()
	{
		main.toggleFilterClientList();
		jMenuClientselectionToggleClientFilter.setState( main.getFilterClientList() );
		popupSelectionToggleClientFilter.setState( main.getFilterClientList() );

		if ( !main.getFilterClientList())
			iconButtonToggleClientFilter.setIcon(Globals.createImageIcon("images/view-filter_disabled-32.png", "" ));
		else
			iconButtonToggleClientFilter.setIcon(Globals.createImageIcon("images/view-filter-32.png", "" ));
		//setActivated( !main.getFilterClientList() );

	}


	public void exitAction()
	{
		main.finishApp(true,0);
	}

	public void saveAction()
	{
		main.checkSaveAll(false);
	}

	public void cancelAction()
	{
		main.cancelChanges();
	}

	public void getSessionInfo(final boolean onlySelectedClients)
	{
		iconButtonSessionInfo.setEnabled(false);
		try
		{
			SwingUtilities.invokeLater(new Runnable()
			                           {
				                           public void run()
				                           {
					                           main.getSessionInfo(onlySelectedClients);
					                           //iconButtonSessionInfo.setEnabled(true);
				                           }
			                           }
			                          )
			;
		}
		catch(Exception ex)
		{
			logging.debug(this, "Exception " + ex);
		}
	}


	public void getReachableInfo()
	{
		iconButtonReachableInfo.setEnabled(false);
		try
		{
			SwingUtilities.invokeLater(new Runnable()
			                           {
				                           public void run()
				                           {
					                           main.getReachableInfo();
					                           //iconButtonReachableInfo.setEnabled(true);
					                           //will be called in thread which is spawn in main
				                           }
			                           }
			                          )
			;
		}
		catch(Exception ex)
		{
			logging.debug(this, "Exception " + ex);
		}
	}

	public void callSelectionDialog()
	{
		main.callClientSelectionDialog();
	}

	private java.util.List<String> getProduct(Vector<String> completeList)
	{
		FEditList fList = new FEditList();
		fList.setListModel(new DefaultComboBoxModel( completeList ));
		fList.setTitle(Globals.APPNAME + ": " + configed.getResourceValue("MainFrame.productSelection"));
		fList.init();

		if (!configed.isApplet)
		{
			fList.setLocation((int) this.getX() +40 , (int) this.getY() +40);
			fList.setSize(fwidth/2, this.getHeight());
		}
		else
		{
			fList.setLocation((int) baseContainer.getX() +40 , (int) baseContainer.getY() +40);
			fList.setSize(fwidth/2, baseContainer.getHeight());
		}

		fList.setModal(true);
		fList.setVisible(true);

		logging.debug(this, "fList getSelectedValue " + fList.getSelectedList());

		return (java.util.List<String>) fList.getSelectedList();
	}

	private void groupByNotCurrentProductVersion()
	{
		java.util.List<String> products = getProduct( new  Vector<String>(new TreeSet<String>(main.getProductNames()) ) );

		if (products.size() > 0)
			main.selectClientsNotCurrentProductInstalled( products );
		//java.util.Arrays.asList("javavm"));
	}

	private void groupByFailedProduct()
	{
		java.util.List<String> products = getProduct( new  Vector<String>(new TreeSet<String>(main.getProductNames()) ) );

		if (products.size() > 0)
			main.selectClientsWithFailedProduct( products );
		//java.util.Arrays.asList("javavm"));
	}

	public void saveGroupAction()
	{
		main.callSaveGroupDialog();
	}

	public void deleteGroupAction()
	{
		main.callDeleteGroupDialog();
	}

	/*
	public void noGroupingAction()
{
		main.loadClientGroup("", "", "", "", "", "", new HashMap()); 
}
	*/

	public void deselectSetEnabled(boolean b)
	{
		jMenuClientselectionDeselect.setEnabled (b);
	}

	public void menuClientSelectionSetEnabled(boolean b)
	{
		jMenuClientselectionGetGroup.setEnabled(b);
		jMenuClientselectionGetSavedSearch.setEnabled(b);
		jMenuClientselectionNotCurrentProduct.setEnabled(b);
		iconButtonSetGroup.setEnabled(b);
	}

	public void reloadAction()
	{
		main.reload();
	}

	public void reloadLicensesAction()
	{
		main.reloadLicensesData();
		main.licencesFrame.setVisible(true);

	}

	public void checkMenuItemsDisabling()
	{
		//for (String itemName : menuItemsHost.keySet())


		if (menuItemsHost == null)
		{
			logging.info(this, "checkMenuItemsDisabling: menuItemsHost not yet enabled");
			return;
		}


		java.util.List<String> disabledClientMenuEntries = main.getPersistenceController().getDisabledClientMenuEntries();

		if (disabledClientMenuEntries != null)
		{

			for (String menuActionType : disabledClientMenuEntries)
			{
				for (JMenuItem menuItem : menuItemsHost.get(menuActionType) )
				{
					logging.debug(this, "disable " +  menuActionType + ", "  +  menuItem);
					menuItem.setEnabled( false );
				}
			}

			iconButtonNewClient.setEnabled(
			    !disabledClientMenuEntries.contains(ITEM_ADD_CLIENT)
			);
		}
	}


	private void initializeMenuItemsForClientsDependentOnSelectionCount()
	{
		for (int i = 0; i < clientMenuItemsDependOnSelectionCount.length; i++)
		{
			clientMenuItemsDependOnSelectionCount[i].setEnabled(false);
		}
		for (int i = 0; i < clientPopupsDependOnSelectionCount.length; i++)
		{
			clientPopupsDependOnSelectionCount[i].setEnabled(false);
			//logging.debug(this, " i"  + i + " : " + clientPopupsDependOnSelectionCount[i].getText());
		}


		// checkMenuItemsDisabling(); produces NPEs since method seems to be called sometimes
		// before the menu is built completely


	}

	public void enableMenuItemsForClients(int countSelectedClients)
	{
		logging.debug(this, " enableMenuItemsForClients, countSelectedClients " + countSelectedClients);


		initializeMenuItemsForClientsDependentOnSelectionCount();


		if (countSelectedClients < 0)
		{
			checkMenuItemsDisabling();
			return;
		}

		if (countSelectedClients == 0)
		{
			jMenuAddClient.setEnabled(true);
			popupAddClient.setEnabled(true);
		}
		else
		{
			if (countSelectedClients >= 1)
			{
				for (int i = 0; i < clientMenuItemsDependOnSelectionCount.length; i++)
				{
					clientMenuItemsDependOnSelectionCount[i].setEnabled(true);
				}
				for (int i = 0; i < clientPopupsDependOnSelectionCount.length; i++)
				{
					clientPopupsDependOnSelectionCount[i].setEnabled(true);
				}
			}


			if (countSelectedClients == 1)
			{
				jMenuChangeClientID.setEnabled(true);
				popupChangeClientID.setEnabled(true);
			}

		}

		checkMenuItemsDisabling();

	}

	public void resetProductOnClientAction()
	{
		main.resetProductsForSelectedClients();
	}

	public void addClientAction()
	{
		main.callNewClientDialog();
	}

	public void changeClientIDAction()
	{
		main.callChangeClientIDDialog();
	}

	public void changeDepotAction()
	{
		main.callChangeDepotDialog();
	}

	public void showBackendConfigurationAction()
	{

		FEditorPane backendInfoDialog = new FEditorPane( this,
		                                Globals.APPNAME + ":  "  + configed.getResourceValue( "MainFrame.InfoInternalConfiguration"),
		                                false, new String[] { configed.getResourceValue("MainFrame.InfoInternalConfiguration.close") },
		                                800, 600);
		backendInfoDialog.insertHTMLTable (main.getBackendInfos(), "" );
		//backendInfoDialog.setSize (new Dimension (400, 400));

		backendInfoDialog.setVisible(true);
	}


	public void showAboutAction()
	{
		Frame1_Infodialog dlg = new Frame1_Infodialog(this);
		Dimension dlgSize = dlg.getPreferredSize();
		Dimension frmSize = getSize();
		Point loc = getLocation();
		dlg.setLocation((frmSize.width - dlgSize.width) / 2 + loc.x, (frmSize.height - dlgSize.height) / 2 + loc.y);
		dlg.setModal(true);
		dlg.setAlwaysOnTop(true);
		dlg.setVisible(true);
	}

	public void showOpsiModules()
	{
		FTextArea f = new FTextArea(this, configed.getResourceValue("MainFrame.jMenuHelpOpsiModuleInformation"), true, 1);
		StringBuffer message = new StringBuffer();
		Map<String, Object> modulesInfo = main.getPersistenceController().getOpsiModulesInfos();

		int count = 0;
		for (String key : modulesInfo.keySet() ) 
		{
			count++;
			message.append("\n " + key + ": " + modulesInfo.get(key) );
		}
		f.setSize(new Dimension(300, 50 + count * 25));

		f.setMessage(message.toString());
		f.setVisible(true);
	}
	
	public void showInfoPage()
	{
		FEditorPane fEditPane = new  FEditorPane(this, "opsi server infoPage", false, new String[]{"ok"}, 500,  400);
		fEditPane.setPage("https://" + main.getConfigserver() + ":4447/info");
		
		//fEditPane.setPage("https://google.de");
		fEditPane.setVisible(true);
	}

	//----------------------------------------------------------------------------------------

	void jComboBoxProductValues_actionPerformed(ActionEvent e)
	{
		if ( ! settingSchalter)
		{
			String currentkey, newvalue;
			/*
			if (jListProducts.getSelectedValue() != null)
		{
			currentkey = jListProducts.getSelectedValue().toString();
			currentkey = currentkey.copyValueOf(currentkey.toCharArray(),0,currentkey.indexOf("="));
			newvalue = jComboBoxProductValues.getSelectedItem().toString();
			logging.debugOut(this, logging.LEVEL_DONT_SHOW_IT, "jComboBoxProductValues_actionPerformed: set "+currentkey+"="+newvalue);
			//dm.setPcProfileValueWithRequired (currentkey,newvalue,jRadioRequiredAll.isSelected());
			//PersistenceController.getPersistenceController().setPcProductSwitchWithRequired (currentkey, newvalue, jRadioRequiredAll.isSelected());
			checkErrorList();
			dm.setDirty(true);
			refreshProductlist();
		}
			*/
		}
	}

	void jButtonSaveList_actionPerformed(ActionEvent e)
	{
		main.checkSaveAll(false);
	}


	/* WindowListener implementation */
	public void windowClosing (WindowEvent e)
	{
		main.finishApp(true, 0);
	}
	public void windowOpened (WindowEvent e)
	{;}
	public void windowClosed (WindowEvent e)
	{;}
	public void windowActivated (WindowEvent e)
	{;}
	public void windowDeactivated (WindowEvent e)
	{;}
	public void windowIconified (WindowEvent e)
	{;}
	public void windowDeiconified (WindowEvent e)
	{;}


	// ChangeListener implementation
	public void stateChanged(ChangeEvent e)
	{
		if (e.getSource() == cbUefiBoot)
		{

			changedClientInfo.put(HostInfo.clientUefiBootKEY,
			                      ((Boolean)cbUefiBoot.isSelected()).toString()
			                     );

			//main.getClientInfoDataChangedKeeper().dataHaveChanged(changedClientInfo);

			logging.info(this, "changedClientInfo : " + changedClientInfo);
		}
		else if (e.getSource() == cbWANConfig)
		{
			JOptionPane.showMessageDialog(this, "MainFrame.stateChanged(cbWANConfig)");
			changedClientInfo.put(HostInfo.clientWanConfigKEY,
			                      ((Boolean)cbWANConfig.isSelected()).toString()
			                     );

			main.getClientInfoDataChangedKeeper().dataHaveChanged(changedClientInfo);

			logging.info(this, "changedClientInfo : " + changedClientInfo);
		}
		// else if (e.getSource() == cbInstallByShutdown)
		// {
		// 	JOptionPane.showMessageDialog(this, "MainFrame.stateChanged(cbInstallByShutdown)");
		// 	changedClientInfo.put(HostInfo.clientShutdownInstallKEY,
		// 		((Boolean)cbInstallByShutdown.isSelected()).toString()
		// 		);

		// 	main.getClientInfoDataChangedKeeper().dataHaveChanged(changedClientInfo);

		// 	logging.info(this, "changedClientInfo : " + changedClientInfo);
		// }
	}

	protected void arrangeWs(Set<JDialog> frames)
	{

		if (frames == null)
			return;

		int transpose = 20;

		for (java.awt.Window f : frames)
		{
			transpose = transpose + Globals.lineHeight;

			if (f != null)
			{
				f.setVisible(true);
				f.setLocation(
				    getLocation().x + transpose,
				    getLocation().y + transpose
				);
			}
		}
	}



	// RunningInstancesObserver
	public void instancesChanged(Set<JDialog> instances)
	{
		//logging.info(this, "instancesChanged, we have instances " + instances);
		boolean existJDialogInstances = (instances != null && instances.size() > 0);

		if (jMenuShowScheduledWOL != null)
		{
			jMenuShowScheduledWOL.setEnabled(existJDialogInstances);
		}
		if (jMenuFrameShowDialogs != null)
		{
			jMenuFrameShowDialogs.setEnabled(existJDialogInstances);
		}

		/*
		if (buttonWindowStack != null)
		{
			buttonWindowStack.setEnabled(existJDialogInstances);
	}
		*/
	}

	public void executeCommandOnInstances(String command, Set<JDialog> instances)
	{
		logging.info(this, "executeCommandOnInstances " + command + " for count instances " + instances.size());
		switch (command)
		{
		case "arrange":
			arrangeWs(instances);
			break;
		}
	}



	//MouseListener implementation
public void mouseClicked(MouseEvent e) {}
	public void mouseEntered(MouseEvent e)  {}
	public void mouseExited(MouseEvent e) {}
	public void mousePressed(MouseEvent e) {}
	public void mouseReleased(MouseEvent e) {}


	// KeyListener implementation
	public void  keyPressed(KeyEvent e)
	{
		/*
		if (e.getSource() == macAddressField)
		{
			
			
			logging.debug(this, "MainFrame keyPressed on macAddressField " + e.getKeyChar());
			
			if (e.getKeyCode() == KeyEvent.VK_ESCAPE)
			{
				
			
			//if (e.getKeyCode() == KeyEvent.VK_BACK_SPACE)
			//	e.consume();
			
			//dont delete with backspace since we  ruin our mask
	}
		*/

	}

	public  void  keyReleased(KeyEvent e)
	{
		if (e.getSource() == jTextFieldDescription)
		{
			if (jTextFieldDescription.isChangedText())
			{
				changedClientInfo.put("description", jTextFieldDescription.getText());
			}
			else
			{
				changedClientInfo.remove("description");
			}
			main.getClientInfoDataChangedKeeper().dataHaveChanged(changedClientInfo);
		}

		else if (e.getSource() == jTextFieldInventoryNumber)
		{
			//logging.debug(this, " keyPressed on fieldinventorynumber , text, old text  "  + jTextFieldInventoryNumber.getText() + ", " + oldInventoryNumber);
			if (jTextFieldInventoryNumber.isChangedText())
				changedClientInfo.put("inventoryNumber", jTextFieldInventoryNumber.getText());
			else
				changedClientInfo.remove("inventoryNumber");

			main.getClientInfoDataChangedKeeper().dataHaveChanged(changedClientInfo);
		}

		else if (e.getSource() == jTextFieldOneTimePassword)
		{
			if (jTextFieldOneTimePassword.isChangedText())
			{
				changedClientInfo.put("oneTimePassword", jTextFieldOneTimePassword.getText());
			}
			else
			{
				changedClientInfo.remove("oneTimePassword");
			}
			main.getClientInfoDataChangedKeeper().dataHaveChanged(changedClientInfo);

		}


		else if (e.getSource() == jTextAreaNotes)
		{
			if (!jTextAreaNotes.getText().equals(oldNotes))
			{
				changedClientInfo.put("notes", jTextAreaNotes.getText());
			}
			else
			{
				changedClientInfo.remove("notes");
			}
			main.getClientInfoDataChangedKeeper().dataHaveChanged(changedClientInfo);
		}

		else if (e.getSource() == macAddressField)
		{
			//System.out.println (" -- key event from macAddressField , oldMacAddress " + oldMacAddress
			//	+ ",  address " +  macAddressField.getText() );
			logging.debug(this, " keyPressed on macAddressField, text " + macAddressField.getText());

			if (macAddressField.isChangedText())
			{
				changedClientInfo.put("hardwareAddress", macAddressField.getText());
			}
			else
			{
				changedClientInfo.remove("hardwareAddress");
				//main.getClientInfoDataChangedKeeper().dataHaveChanged(changedClientInfo);
			}
			main.getClientInfoDataChangedKeeper().dataHaveChanged(changedClientInfo);


		}



	}

	public  void  keyTyped(KeyEvent e)
	{
	}


	// ActionListener implementation
	public void actionPerformed(ActionEvent e)
	{
		if (e.getSource() == cbUefiBoot)
		{
			logging.debug(this, "actionPerformed on cbUefiBoot");

			changedClientInfo.put(HostInfo.clientUefiBootKEY,
			                      ((Boolean)cbUefiBoot.isSelected()).toString()
			                     );

			main.getClientInfoDataChangedKeeper().dataHaveChanged(changedClientInfo);
		}
		else if (e.getSource() == cbWANConfig)
		{
			logging.debug(this, "actionPerformed on cbWANConfig");

			changedClientInfo.put(HostInfo.clientWanConfigKEY,
			                      ((Boolean)cbWANConfig.isSelected()).toString()
			                     );
			main.getClientInfoDataChangedKeeper().dataHaveChanged(changedClientInfo);
		}
		// else if (e.getSource() == cbInstallByShutdown)
		// {
		// 	logging.debug(this, "actionPerformed on cbInstallByShutdown");

		// 	changedClientInfo.put(HostInfo.clientShutdownInstallKEY,
		// 		((Boolean)cbInstallByShutdown.isSelected()).toString()
		// 		);
		// 	main.getClientInfoDataChangedKeeper().dataHaveChanged(changedClientInfo);
		// }
		else if (e.getSource() == jButtonClientsConfiguration)
		{
			main.setEditingTarget (ConfigedMain.EditingTarget.CLIENTS);
		}

		else if (e.getSource() == jButtonDepotsConfiguration)
		{
			main.setEditingTarget (ConfigedMain.EditingTarget.DEPOTS);
		}

		else if (e.getSource() == jButtonServerConfiguration)
		{
			main.setEditingTarget (ConfigedMain.EditingTarget.SERVER);
		}

		else if (
		    e.getSource() == jButtonLicences
		    ||
		    e.getSource() == jMenuFrameLicences
		)
		{
			main.handleLicencesManagementRequest();

			//main.toggleLicencesFrame();
		}

		else if (
		    e.getSource() == jButtonWorkOnGroups
		    ||
		    e.getSource() == jMenuFrameWorkOnGroups
		)
		{
			main.handleGroupActionRequest();

		}

		else if (
		    e.getSource() == jButtonWorkOnProducts
		    ||
		    e.getSource() == jMenuFrameWorkOnProducts
		)
		{
			main.handleProductActionRequest();

			//main.toggleLicencesFrame();
		}

		else if (e.getSource() ==  buttonSelectDepotsAll)
		{
			logging.info(this, "action on buttonSelectDepotsAll");
			//depotslist.setSelectionInterval(0, depotslist.getModel().getSize() - 1);
			depotslist.selectAll();
		}

		else if (e.getSource() ==  buttonSelectDepotsWithEqualProperties)
		{
			logging.info(this, "action on buttonSelectDepotsWithEqualProperties");

			if (depotslist.getSelectedIndex() > -1)
			{
				String depotSelected = (String) depotslist.getSelectedValue();
				java.util.List<String> depotsWithEqualStock
				= main.getPersistenceController().getAllDepotsWithIdenticalProductStock(depotSelected);
				depotslist.addToSelection(depotsWithEqualStock);

			}
		}


	}

	public void enableAfterLoading()
	{
		jButtonLicences.setEnabled(true);
		jMenuFrameLicences.setEnabled(true);
	}


	public void visualizeLicencesFramesActive(boolean b)
	{
		jButtonLicences.setSelected(b);
		iconButtonReloadLicenses.setVisible(true);
		iconButtonReloadLicenses.setEnabled(true);

		//jButtonLicences.setOpaque(false);
	}

	public void visualizeEditingTarget (ConfigedMain.EditingTarget t)
	{
		switch (t)
		{
		case CLIENTS:
			jButtonClientsConfiguration.setSelected(true);
			jButtonDepotsConfiguration.setSelected(false);
			jButtonServerConfiguration.setSelected(false);
			//System.out.println ( " 2 jButtonLicences == null " + (jButtonLicences == null));
			//jLabelLicences.setForeground (Globals.greyed);
			break;

		case DEPOTS:
			jButtonDepotsConfiguration.setSelected(true);
			jButtonServerConfiguration.setSelected(false);
			jButtonClientsConfiguration.setSelected(false);
			//jButtonLicences.setSelected(false);
			//jLabelLicences.setForeground (Globals.greyed);
			break;

		case SERVER:
			jButtonServerConfiguration.setSelected(true);
			jButtonDepotsConfiguration.setSelected(false);
			jButtonClientsConfiguration.setSelected(false);
			//jButtonLicences.setSelected(false);
			//jLabelLicences.setForeground (Globals.greyed);
			break;

			/*case LICENCES:
			System.out.println(" tabbed pane visible false");
				jButtonServerConfiguration.setSelected(false);
			jButtonClientsConfiguration.setSelected(false);
			jButtonLicences.setSelected(true);
			jLabelServerConfiguration.setForeground (Globals.greyed);
			jLabelClientsConfiguration.setForeground (Globals.greyed);
			jLabelLicences.setForeground (Globals.blue);
			break;
			*/

		default: break;
		}
	}


	public void producePanelReinstmgr ( String pcname, Vector images)
	{
		panelReinstmgr.startFor (pcname, images);
	}


	public void initHardwareInfo (java.util.List config)
	{
		if (showHardwareLog_version2 == null)
		{
			showHardwareLog_version2 = new de.uib.configed.gui.hwinfopage.PanelHWInfo(main){
				                           @Override
				                           protected void reload()
				                           {
					                           super.reload();
					                           main.clearHwInfo();
					                           //WaitCursor waitCursor = new WaitCursor(tree);
					                           //otherwise we get a wait cursor only in table component
					                           main.resetView(ConfigedMain.viewHardwareInfo);
				                           }
			                           };
		}
		showHardwareLog_version2.setHardwareConfig (config );
	}

	private void showHardwareInfo()
	{
		jTabbedPaneConfigPanes.setComponentAt
		(jTabbedPaneConfigPanes.indexOfTab(configed.getResourceValue("MainFrame.jPanel_hardwareLog")),
		 showHardwareLog);

		/*
		SwingUtilities.invokeLater(
			new Runnable()
			{
				public void run()
				{
					Globals.mainContainer.repaint();
				}
			}
		);
		*/
	}

	public void setHardwareInfo()
	{
		labelNoHardware.setText(configed.getResourceValue("MainFrame.TabActiveForSingleClient"));
		showHardwareLog = showHardwareLog_NotFound;
		showHardwareInfo();
	}

	public void setHardwareInfo (Object hardwareInfo, String pc)
	{
		//logging.debug(this, "setHardwareInfo " + hardwareInfo);
		//labelNoHardware.setText(configed.getResourceValue("MainFrame.NoHardwareConfiguration"));

		if (hardwareInfo == null)
			showHardwareLog_version2.setHardwareInfo( null,  configed.getResourceValue("MainFrame.NoHardwareConfiguration"));
		else
			showHardwareLog_version2.setHardwareInfo( (Map) hardwareInfo, null);


		showHardwareLog = showHardwareLog_version2;
		showHardwareLog.setTitle(pc + "   " +   configed.getResourceValue("PanelHWInfo.title"));

		/*
			if (hardwareInfo instanceof Map)
		{
			//System.out.println (" ------------- we should get a version2 hardware info"); 
			showHardwareLog_version2.setHardwareInfo( (Map) hardwareInfo);
			showHardwareLog = showHardwareLog_version2;
			showHardwareLog.setTitle(pc + "   " +   configed.getResourceValue("PanelHWInfo.title"));
	}

		else
			showHardwareLog = showHardwareLog_NotFound;
		*/

		//System.out.println("setComponentAt  >>" + configed.getResourceValue("MainFrame.jPanel_hardwareLog") + "<<");
		showHardwareInfo();

	}


	private void showSoftwareAudit()
	{
		jTabbedPaneConfigPanes.setComponentAt
		(jTabbedPaneConfigPanes.indexOfTab(configed.getResourceValue("MainFrame.jPanel_softwareLog")),
		 showSoftwareLog);

		SwingUtilities.invokeLater(
		    new Runnable()
		    {
			    public void run()
			    {
				    Globals.mainContainer.repaint();
			    }
		    }
		);
	}

	public void setSoftwareAudit()
	{

		labelNoSoftware.setText(configed.getResourceValue("MainFrame.TabActiveForSingleClient"));
		showSoftwareLog = showSoftwareLog_NotFound;
		showSoftwareAudit();
	}


	public void setSoftwareAudit(String hostId, DatedRowList softwareInfo)
	{
		labelNoSoftware.setText(configed.getResourceValue("MainFrame.NoSoftwareConfiguration"));
		/*
		if (softwareInfo == null || softwareInfo.isEmpty())
		{
			//logging.debug(this, "set null SoftwareAudit for " + hostId);
			showSoftwareLog = showSoftwareLog_NotFound;
			
	}
		else
		*/
		{
			logging.debug(this, "setSoftwareAudit for " + hostId);
			showHSoftwareLog_Available.setSoftwareInfo(hostId, softwareInfo);
			showSoftwareLog = showHSoftwareLog_Available;
		}
		showSoftwareAudit();
	}

	public void setUpdatedLogfilePanel(String logtype)
	{
		logging.info(this, "setUpdatedLogfilePanel " + logtype);
		//WaitCursor waitCursor = new WaitCursor( de.uib.configed.Globals.mainContainer, "setUpdatedLogFilePanel" );
		setLogfilePanel(main.getLogfilesUpdating(logtype));
		//waitCursor.stop();
	}


	public void setLogfilePanel(final Map<String,String> logs)
	{
		jTabbedPaneConfigPanes.setComponentAt
		(jTabbedPaneConfigPanes.indexOfTab(configed.getResourceValue("MainFrame.jPanel_logfiles")),
		 showLogfiles);

		//showLogfiles.setDocuments(logs);

		//SwingUtilities.invokeLater( new Runnable(){
		//	                            public void run(){
				                            //WaitCursor waitCursor = new WaitCursor( retrieveBasePane(), "setLogFilePanel" );
				                            showLogfiles.setDocuments(logs, statusPane.getSelectedClientNames());
				                            //waitCursor.stop();
		//	                            }
		//                           });


	}

	public void setLogview(String logtype)
	{
		int i = Arrays.asList(de.uib.configed.Globals.logtypes).indexOf(logtype);
		if (i < 0)
			return;

		showLogfiles.setSelectedIndex(i);
	}

	//client field editing
	public void setClientDescriptionText( String s)
	{
		jTextFieldDescription.setText( s );
		jTextFieldDescription.setCaretPosition(0);
		//oldDescription = s;
		//changedClientInfo.put("description", s);
	}

	public void setClientInventoryNumberText( String s)
	{
		jTextFieldInventoryNumber.setText( s );
		jTextFieldInventoryNumber.setCaretPosition(0);
		//oldInventoryNumber = s;
		//changedClientInfo.put("inventoryNumber", s);
	}

	public void setClientOneTimePasswordText( String s)
	{
		jTextFieldOneTimePassword.setText( s );
		jTextFieldOneTimePassword.setCaretPosition(0);
		//oldOneTimePassword = s;
		//changedClientInfo.put("oneTimePassword", s);
	}

	public void setClientNotesText( String s)
	{
		jTextAreaNotes.setText(s);
		jTextAreaNotes.setCaretPosition(0);
		oldNotes = s;
		//changedClientInfo.put("notes", s);
	}

	public void setClientMacAddress( String s)
	{
		macAddressField.setText(s);
		//oldMacAddress = s;
		//changedClientInfo.put("hardwareAddress", s);
	}

	public void setUefiBoot(boolean b)
	{
		cbUefiBoot.setSelected(b);
	}

	public void setWANConfig(boolean b)
	{
		cbWANConfig.setSelected(b);
	}

	// public void setShutdownInstall(boolean b)
	// {
	// 	cbInstallByShutdown.setSelected(b);
	// }

	public void setClientID(String s)
	{
		labelHostID.setText(s);
	}

	public String getClientID()
	{
		return labelHostID.getText();
	}

	public void setClientInfoediting (boolean ba)
	{
		boolean b = ba;
		if (Globals.isGlobalReadOnly()) b = false; 
		
		labelHost.setEnabled(b);

		boolean b1 = false;

		if (b && !main.getPersistenceController().isGlobalReadOnly())
			b1 = true;

		jTextFieldDescription.setEnabled(b);
		jTextFieldDescription.setEditable(b1);
		jTextFieldInventoryNumber.setEnabled(b);
		jTextFieldInventoryNumber.setEditable(b1);
		jTextFieldOneTimePassword.setEnabled(b);
		jTextFieldOneTimePassword.setEditable(b1);
		jTextAreaNotes.setEnabled(b);
		jTextAreaNotes.setEditable(b1);
		macAddressField.setEnabled(b);
		macAddressField.setEditable(b1);
		cbUefiBoot.setEnabled(b);
		cbWANConfig.setEnabled(b);
		// cbInstallByShutdown.setEnabled(b);
		btnAktivateInstallByShutdown.setEnabled(b);
		btnDeaktivateInstallByShutdown.setEnabled(b);


		if (b)
		{
			jTextFieldDescription.setToolTipText(null);
			jTextFieldInventoryNumber.setToolTipText(null);
			jTextFieldOneTimePassword.setToolTipText(null);
			jTextAreaNotes.setToolTipText(null);
			jTextFieldDescription.setBackground(Globals.backgroundWhite);
			jTextFieldInventoryNumber.setBackground(Globals.backgroundWhite);
			jTextFieldOneTimePassword.setBackground(Globals.backgroundWhite);
			jTextAreaNotes.setBackground(Globals.backgroundWhite);
			macAddressField.setBackground(Globals.backgroundWhite);
			jLabel_InstallByShutdown.setForeground(Globals.lightBlack);
			cbUefiBoot.setBackground(Globals.backgroundWhite);
			cbWANConfig.setBackground(Globals.backgroundWhite);
			// cbInstallByShutdown.setBackground(Globals.backgroundWhite);
		}
		else
		{
			jTextFieldDescription.setToolTipText(configed.getResourceValue("MainFrame.Only_active_for_a_single_client"));
			jTextFieldInventoryNumber.setToolTipText(configed.getResourceValue("MainFrame.Only_active_for_a_single_client"));
			jTextFieldOneTimePassword.setToolTipText(configed.getResourceValue("MainFrame.Only_active_for_a_single_client"));
			jTextAreaNotes.setToolTipText(configed.getResourceValue("MainFrame.Only_active_for_a_single_client"));
			jTextFieldDescription.setBackground(Globals.backgroundLightGrey);
			jTextFieldInventoryNumber.setBackground(Globals.backgroundLightGrey);
			jTextFieldOneTimePassword.setBackground(Globals.backgroundLightGrey);
			jTextAreaNotes.setBackground(Globals.backgroundLightGrey);
			jLabel_InstallByShutdown.setForeground(Globals.greyed);
			macAddressField.setBackground(Globals.backgroundLightGrey);
			cbUefiBoot.setBackground(Globals.backgroundLightGrey);
			cbWANConfig.setBackground(Globals.backgroundLightGrey);
			// cbInstallByShutdown.setBackground(Globals.backgroundLightGrey);
		}

	}

	/*
	private void showPopupMenu(JMenuItem[] items, Component c, int x, int y)
{
		jPopupMenu = new JPopupMenu();
		for (int i=0; i<items.length; i++) {
			jPopupMenu.add( items[i] );
		}
		jPopupMenu.show(c, x, y);
}
	*/


	public void setChangedDepotSelectionActive(boolean active)
	{
		if (active)
			depotslist.setBackground(Globals.backLightYellow);
		else
			depotslist.setBackground(Globals.backgroundWhite);

		//colorize as hint that we have changed the depots selection

		/*
		buttonCommitChangedDepotSelection.setEnabled(active);
		buttonCancelChangedDepotSelection.setEnabled(active);
		popupCommitChangedDepotSelection.setEnabled(active);
		popupCancelChangedDepotSelection.setEnabled(active);
		*/

	}



	@Override
	public void paint(java.awt.Graphics g)
	{
		try{
			super.paint(g);
		}
		catch(java.lang.ClassCastException ex)
		{
			logging.warning(this, "the ugly well known exception " + ex);
			WaitCursor.stopAll();
		}
	}

}







