package de.uib.configed.gui.productpage;

/*
 * configed - configuration editor for client work stations in opsi
 * (open pc server integration) www.opsi.org
 *
 * Copyright (C) 2000-2011 uib.de
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 */

import de.uib.configed.*;
import de.uib.configed.gui.*;
import de.uib.configed.productgroup.*;
import de.uib.opsidatamodel.datachanges.*;

import javax.swing.*;
import javax.swing.event.*;

import java.awt.*;
import java.awt.event.*;
import java.util.*;

import javax.swing.table.*;

import com.itextpdf.text.Element;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;

import java.io.File;

import de.uib.utilities.pdf.DocumentToPdf;
import de.uib.utilities.swing.*;
import de.uib.utilities.swing.list.*;
import de.uib.utilities.table.gui.*;
import de.uib.utilities.datapanel.*;
import de.uib.utilities.logging.*;
import de.uib.configed.guidata.*;
import de.uib.configed.gui.helper.*;
import de.uib.opsidatamodel.productstate.*;


public class PanelProductSettings extends JSplitPane
implements RowSorterListener
{
	
	public static final java.util.List<RowSorter.SortKey> sortkeysDefault = new ArrayList<RowSorter.SortKey> ();
	static{
		sortkeysDefault. add(new RowSorter.SortKey(0, SortOrder.ASCENDING));
	}
	
	JScrollPane paneProducts;
	public JTable tableProducts;
	JPanel topPane;
	
	ProductInfoPane  infoPane; // right pane
	protected DefaultPanelEditProperties panelEditProperties;
	AbstractEditMapPanel propertiesPanel;
	
	protected int hMin = 200;
	
	final int fwidth_lefthanded = 600; 
	final int splitterLeftRight = 15;
	final int fheight =  450;
	
		
	final int fwidth_column_productname    = 170;
	final int fwidth_column_productcompletename    = 170;
	final int fwidth_column_productstate = 60;
	final int fwidth_column_productposition = 40; 
	//final int fwidth_column_productinstallationstatus     = 100;
	//final int fwidth_column_productaction   = 100;
	final int fwidth_column_productsequence = fwidth_column_productposition;
	final int fwidth_column_versionInfo = fwidth_column_productstate;
	final int fwidth_column_productversion = fwidth_column_productstate;
	final int fwidth_column_packageversion = fwidth_column_productstate;
	final int fwidth_column_installationInfo = fwidth_column_productstate;
	
	
	//TableCellRenderer productTableCellRenderer; // was default
	ListCellRenderer standardListCellRenderer;
	
	
	TableCellRenderer productNameTableCellRenderer;
	TableCellRenderer productCompleteNameTableCellRenderer;

	TableCellRenderer targetConfigurationTableCellRenderer;
	TableCellRenderer installationStatusTableCellRenderer;
	TableCellRenderer actionProgressTableCellRenderer;
	TableCellRenderer lastActionTableCellRenderer;
	TableCellRenderer actionResultTableCellRenderer;
	TableCellRenderer actionRequestTableCellRenderer;
	ColoredTableCellRendererByIndex priorityclassTableCellRenderer;
	ColoredTableCellRenderer productsequenceTableCellRenderer;
	ColoredTableCellRenderer productversionTableCellRenderer;
	ColoredTableCellRenderer packageversionTableCellRenderer;
	
	ColoredTableCellRenderer versionInfoTableCellRenderer;
	ColoredTableCellRenderer installationInfoTableCellRenderer;
	
	ColoredTableCellRenderer positionTableCellRenderer;
	ColoredTableCellRenderer lastStateChangeTableCellRenderer;
	

	
	TableCellRenderer propertiesTableCellRenderer;
	
	private boolean packageGroupsVisible = false;
	
	protected LinkedHashMap<String, Boolean> productDisplayFields;
	
	protected ArrayList<String> selectedProducts;
	
	JPopupMenu popup;
	JMenu subOpsiclientdEvent;
	JMenuItem itemOnDemand;
	
	protected DocumentToPdf tableToPDF;
	protected String title;

	 
	protected ConfigedMain mainController;
	
	public PanelProductSettings( String title, ConfigedMain mainController, LinkedHashMap<String, Boolean> productDisplayFields,
		boolean packageGroupsVisible)
	{
		super(JSplitPane.HORIZONTAL_SPLIT);
		this.title = title;
		this.mainController  = mainController;
		this.productDisplayFields = productDisplayFields;
		init();
		
		setDividerLocation(fwidth_lefthanded);
		setResizeWeight(0.5);
	
	}
		
	public PanelProductSettings( String title, ConfigedMain mainController, LinkedHashMap<String, Boolean> productDisplayFields)
	{
		this(title, mainController, productDisplayFields, false);
	}
	
	
	protected void initTopPane()
	{
		topPane = new JPanel();
		topPane.setVisible(true);
	}
	
	protected void init()
	{	
		tableProducts = new JTable();
		tableProducts.setDragEnabled(true);
		
		initTopPane();
		
		selectedProducts = new ArrayList<String>();
		
		paneProducts = new JScrollPane();
		
		paneProducts.getViewport().add(tableProducts);
		paneProducts.setPreferredSize( new Dimension (fwidth_lefthanded ,  fheight + 40));
		paneProducts.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		
		tableProducts.setBackground(Globals.backgroundWhite);
		tableProducts.setShowHorizontalLines(true);
		tableProducts.setGridColor(Color.WHITE);
		
	
		
		//final PanelProductSettings THIS = this;
	
		
		tableProducts.getSelectionModel().addListSelectionListener(new ListSelectionListener() 
		{
			public void valueChanged(ListSelectionEvent e) 
			{
				//logging.debug(this, "-----------------tableProducts ListSelectionListener valueChanged");
				
				//Ignore extra messages.
				if (e.getValueIsAdjusting()) return;
				
				//mainController.clearProductEditing();
				clearEditing();
				//selectedProducts.clear();
				
				ListSelectionModel lsm =
				(ListSelectionModel)e.getSource();
				if (lsm.isSelectionEmpty()) {
					logging.debug(this, "no rows selected");
					
				}
				else 
				{
					int selectedRow = lsm.getMinSelectionIndex();
					if (selectedRow != lsm.getMaxSelectionIndex())
					{
						//multiselection
						
					}
						
					else
					{
						logging.debug(this, "selected " + selectedRow);
						logging.debug(this, "selected modelIndex " + convertRowIndexToModel (selectedRow));
						logging.debug(this, "selected  value at " + tableProducts.getModel().getValueAt(  convertRowIndexToModel (selectedRow), 0) );
						mainController.setProductEdited( (String)  tableProducts.getModel().getValueAt(  convertRowIndexToModel (selectedRow) , 0 ));
					}
					
					/*
					int[] selection = tableProducts.getSelectedRows();
					//logging.debug(this, "selection " + java.util.Arrays.toString(selection));
					for (int i = 0; i < selection.length; i++) 
					{
						selectedProducts.add( 
							(String) tableProducts.getValueAt( selection[i] , 0 ) 
						);
					}
					*/
				}
				
				//logging.info(this, "selected: " + THIS.getSelectedIDs());
				
			}
		});
		
		
		tableProducts.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		
		standardListCellRenderer = new StandardListCellRenderer();
		//productTableCellRenderer = new  ProductTableCellRendererDefault();
		
		productNameTableCellRenderer = new StandardTableCellRenderer("")
		{
			public Component getTableCellRendererComponent(
				JTable table,
				Object value,            // value to display
				boolean isSelected,      // is the cell selected
				boolean hasFocus,
				int row,
				int column)
			{
				Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
				
				if ( (c == null) || !(c instanceof JComponent) )
					return c;
				
				JComponent jc = (JComponent) c;
				
				String stateChange = ((IFInstallationStateTableModel)(table.getModel())).getLastStateChange(
					convertRowIndexToModel(row)
					);
				
				//logging.debug(this, " ------ " + table.getModel().getValueAt(row, 0)  + " - stateChange " + stateChange);
				
				if (stateChange == null)
					stateChange = "";
				
				stateChange = table.getValueAt(row, column).toString()
				+ ", " + configed.getResourceValue("InstallationStateTableModel.lastStateChange") + ": " + stateChange;
			
				jc.setToolTipText( stateChange );
				
				return jc;
			}
		};
		
		productCompleteNameTableCellRenderer = new StandardTableCellRenderer("");
		
		String iconsDir = null;
		
		if (de.uib.configed.Globals.showIconsInProductTable)
			iconsDir = "images/productstate/targetconfiguration";
		targetConfigurationTableCellRenderer = new ColoredTableCellRendererByIndex
		(	
					de.uib.opsidatamodel.productstate.TargetConfiguration.getLabel2DisplayLabel(),
					iconsDir, 
					false, InstallationStateTableModel.getColumnTitle(ProductState.KEY_targetConfiguration) + ": "
			)
		;
		
		if (de.uib.configed.Globals.showIconsInProductTable)
			iconsDir = "images/productstate/installationstatus";
		installationStatusTableCellRenderer = new ColoredTableCellRendererByIndex
			(	
					de.uib.opsidatamodel.productstate.InstallationStatus.getLabel2TextColor(),
					de.uib.opsidatamodel.productstate.InstallationStatus.getLabel2DisplayLabel(),
					iconsDir,
					false,  InstallationStateTableModel.getColumnTitle(ProductState.KEY_installationStatus) + ": "
			)
		;
		
		class ActionProgressTableCellRenderer extends ColoredTableCellRendererByIndex
		{
			ActionProgressTableCellRenderer (Map<String,String> mapOfStringValues, String imagesBase, boolean showOnlyIcon, String tooltipPrefix)
			{
				super(mapOfStringValues, imagesBase, showOnlyIcon, tooltipPrefix);
			}
				//overwrite the renderer in order to get the behaviour:
				// - if the cell value is not empty or null, display the installing gif
				// - write the cell value text as tooltip
			public Component getTableCellRendererComponent(
				JTable table,
				Object value,            // value to display
				boolean isSelected,      // is the cell selected
				boolean hasFocus,
				int row,
				int column)
			{
				//logging.debug(this,  "value >" + value + "<");
				Component result = null;
				if (value != null   && !value.equals("")
					&& !value.toString().equals("null") 
					&& !value.toString().equalsIgnoreCase("none") 
					&& !value.toString().equalsIgnoreCase(Globals.CONFLICTSTATEstring) 
					)
				{
					result = super.getTableCellRendererComponent(table, "installing", isSelected, hasFocus, row, column);
					
					((JLabel)result).setToolTipText(
						Globals.fillStringToLength(tooltipPrefix + " "+value + " ", FILL_LENGTH)
						);
					
				}
				else if (value != null &&  value.toString().equalsIgnoreCase(Globals.CONFLICTSTATEstring)) 
				{
					result = super.getTableCellRendererComponent(table, Globals.CONFLICTSTATEstring, isSelected, hasFocus, row, column);
					
					((JLabel)result).setToolTipText(
						Globals.fillStringToLength(tooltipPrefix + " "+Globals.CONFLICTSTATEstring + " ", FILL_LENGTH)
						);
				}
				
				else 
				{
					result = super.getTableCellRendererComponent(table, "none", isSelected, hasFocus, row, column);
					
					((JLabel)result).setToolTipText(
						Globals.fillStringToLength(tooltipPrefix + " "+ActionProgress.getDisplayLabel(ActionProgress.NONE) + " ", FILL_LENGTH)
						);
				}
				
					
				return result;
			}
		}	
		
		if (de.uib.configed.Globals.showIconsInProductTable)
			iconsDir = "images/productstate/actionprogress";
		
		actionProgressTableCellRenderer = new ActionProgressTableCellRenderer
			(	
				de.uib.opsidatamodel.productstate.ActionProgress.getLabel2DisplayLabel(),
				iconsDir,
				false, InstallationStateTableModel.getColumnTitle(ProductState.KEY_actionProgress) + ": "
			)
		;
		
		if (de.uib.configed.Globals.showIconsInProductTable)
			iconsDir = "images/productstate/actionresult";
		
		actionResultTableCellRenderer = new ColoredTableCellRendererByIndex
			(	
				de.uib.opsidatamodel.productstate.ActionResult.getLabel2DisplayLabel(),
				iconsDir,
				false, InstallationStateTableModel.getColumnTitle(ProductState.KEY_actionResult) + ": "
			)
		;
		
		if (de.uib.configed.Globals.showIconsInProductTable)
			iconsDir = "images/productstate/lastaction";
		
		lastActionTableCellRenderer =  new ColoredTableCellRendererByIndex
			(	
				de.uib.opsidatamodel.productstate.ActionRequest.getLabel2DisplayLabel(),
				iconsDir,
				false, InstallationStateTableModel.getColumnTitle(ProductState.KEY_lastAction) + ": "
			)
		;
		
		if (de.uib.configed.Globals.showIconsInProductTable)
			iconsDir = "images/productstate/actionrequest";
		
		actionRequestTableCellRenderer = new ColoredTableCellRendererByIndex
			(	
				de.uib.opsidatamodel.productstate.ActionRequest.getLabel2TextColor(),
				de.uib.opsidatamodel.productstate.ActionRequest.getLabel2DisplayLabel(),
				iconsDir,
				false, InstallationStateTableModel.getColumnTitle(ProductState.KEY_actionRequest) + ": "
			)
		;
		
		priorityclassTableCellRenderer = new ColoredTableCellRendererByIndex
			(	
				de.uib.opsidatamodel.productstate.ActionSequence.getLabel2DisplayLabel(),
				null, 
				false, InstallationStateTableModel.getColumnTitle(ProductState.KEY_actionSequence) + ": " 
				
			)
		;
		
		lastStateChangeTableCellRenderer = new ColoredTableCellRenderer(InstallationStateTableModel.getColumnTitle(ProductState.KEY_lastStateChange));
		
		productsequenceTableCellRenderer = new ColoredTableCellRenderer(InstallationStateTableModel.getColumnTitle(ProductState.KEY_position));
		
		productversionTableCellRenderer  = new ColoredTableCellRenderer(InstallationStateTableModel.getColumnTitle(ProductState.KEY_productVersion));
		
		packageversionTableCellRenderer = new ColoredTableCellRenderer(InstallationStateTableModel.getColumnTitle(ProductState.KEY_packageVersion));
	
		versionInfoTableCellRenderer  
			= new ColoredTableCellRenderer(InstallationStateTableModel.getColumnTitle(ProductState.KEY_versionInfo))
			{
				public Component getTableCellRendererComponent(
					JTable table,
					Object value,            // value to display
					boolean isSelected,      // is the cell selected
					boolean hasFocus,
					int row,
					int column)
				{
					Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
					
					if (value != null 
						&& value instanceof String
					)
					{
						String val = (String) value;
						if (val.equals(""))
							return c;
						
						if (val.equals(InstallationStateTableModel.CONFLICTstring)) 
						{
							c.setBackground(Globals.CONFLICTSTATEcellcolor); //result.setForeground (lightBlack);
							c.setForeground(Globals.CONFLICTSTATEcellcolor);
						}
						else
						{
							
							String productId = (String) table.getModel().getValueAt(table.convertRowIndexToModel(row), 0);
							IFInstallationStateTableModel istm = (IFInstallationStateTableModel)(table.getModel());  
							
							String serverProductVersion = "";
							
							if ( istm.getGlobalProductInfos().get(productId) == null )
								logging.warning(this, " istm.getGlobalProductInfos()).get(productId) == null for productId " + productId );
							else
								serverProductVersion = serverProductVersion  + ((istm.getGlobalProductInfos()).get(productId)).get(de.uib.opsidatamodel.productstate.ProductState.KEY_versionInfo);
						
							if (!val.equals(serverProductVersion))
								c.setForeground(Color.red);
						}
					}
						
					return c;
				}
			};
		
		installationInfoTableCellRenderer  
			= new ColoredTableCellRenderer(InstallationStateTableModel.getColumnTitle(ProductState.KEY_installationInfo))
			
			{
				public Component getTableCellRendererComponent(
					JTable table,
					Object value,            // value to display
					boolean isSelected,      // is the cell selected
					boolean hasFocus,
					int row,
					int column)
				{
					Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
					
					if (value != null 
						&& value instanceof String
					)
					{
						String val = (String) value;
						if  (
							val.startsWith(ActionResult.getLabel2DisplayLabel().get(ActionResult.getLabel(ActionResult.FAILED)))
						)
							c.setForeground(Color.red);
							
						else if (
							val.startsWith(ActionResult.getLabel2DisplayLabel().get(ActionResult.getLabel(ActionResult.SUCCESSFUL)))
						)
						
						c.setForeground(Globals.okGreen);
							
					}
						
					return c;
				}
			};
		
		
		
		/*
		JSplitPane leftPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT)
		{
			@Override	
			public int getMinimumDividerLocation()
			{
				return 10;
			}
		};
			
		leftPane.setTopComponent(topPane);
		leftPane.setBottomComponent(paneProducts);
		leftPane.setDividerLocation(topPane.getPreferredSize().height);
		leftPane.setResizeWeight((double) 0);
		*/
		
		
		JPanel leftPane  = new JPanel();
		GroupLayout layoutLeftPane = new GroupLayout(leftPane);
		leftPane.setLayout(layoutLeftPane);
		
		layoutLeftPane.setHorizontalGroup(
			layoutLeftPane.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addComponent(topPane, hMin, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
				.addComponent(paneProducts, hMin, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
			);
		
		layoutLeftPane.setVerticalGroup(
			layoutLeftPane.createSequentialGroup()
				.addComponent(topPane, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
				.addComponent(paneProducts, 100, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
			);
		
		
		
		setLeftComponent(leftPane);
		
		
		propertiesPanel = new EditMapPanelX(new PropertiesTableCellRenderer(), false, false, false);
		((EditMapPanelX) propertiesPanel).setCellEditor(SensitiveCellEditorForDataPanel.getInstance(this.getClass().getName().toString()));
		propertiesPanel.registerDataChangedObserver(mainController.getGeneralDataChangedKeeper());
		
		panelEditProperties = new PanelEditClientProperties(mainController, propertiesPanel);
		infoPane = new ProductInfoPane(panelEditProperties);
		
		propertiesPanel.registerDataChangedObserver(infoPane);
		
		infoPane.getDependenciesTable().setModel(mainController.getRequirementsModel()); 
		
		
		setRightComponent(infoPane);
		//setDividerLocation(fwidth_lefthanded - splitterLeftRight);
		
		producePopupMenu(productDisplayFields);
		
		paneProducts.addMouseListener(new utils.PopupMouseListener(popup));
		tableProducts.addMouseListener(new utils.PopupMouseListener(popup));
		
		//ableProducts.getRowSorter().setSortKeys(PanelProductSettings.sortkeysDefault);
	}
	
	public void initAllProperties()
	{
		propertiesPanel.init();
		infoPane.setInfo("");
		infoPane.setAdvice("");
	}
	
	
	
	protected void producePopupMenu(final Map<String, Boolean> checkColumns)
	{
		popup = new JPopupMenu("");
		
		//LinkedHashMap<String, JMenuItem> menuItems = new LinkedHashMap<String, JMenuItem>();  
		
		JMenuItem save = new JMenuItemFormatted();
		save.setText(configed.getResourceValue("ConfigedMain.saveConfiguration"));
		save.setFont(Globals.defaultFont);
		save.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					mainController.checkSaveAll(false);
					mainController.requestReloadStatesAndActions();
				}
			}
		);
		popup.add(save);
		
		
		
		
		itemOnDemand = new JMenuItemFormatted();
		itemOnDemand.setText(configed.getResourceValue("ConfigedMain.OpsiclientdEvent_on_demand"));
		itemOnDemand.setFont(Globals.defaultFont);
		itemOnDemand.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					mainController.fireOpsiclientdEventOnSelectedClients(
						mainController.getPersistenceController().OPSI_CLIENTD_EVENT_on_demand
						);
				}
			}
		);
		popup.add(itemOnDemand);
		
		/*
		subOpsiclientdEvent = new JMenu(
			configed.getResourceValue("ConfigedMain.OpsiclientdEvent")
		);
		
		
		subOpsiclientdEvent.setFont(Globals.defaultFont);
		
		for (final String event : mainController.getPersistenceController().getOpsiclientdExtraEvents())
		{
			JMenuItemFormatted item = new JMenuItemFormatted(event);
			item.setFont(Globals.defaultFont);
			
			item.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					mainController.fireOpsiclientdEventOnSelectedClients(event);
				}
			});
			
			subOpsiclientdEvent.add(item);
		}
		
		popup.add(subOpsiclientdEvent);
		
		*/
		showPopupOpsiclientdEvent(true);
		
		
		
		JMenuItem reload = new JMenuItemFormatted();
		//reload.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0)); does not find itscontext
		reload.setText(configed.getResourceValue("ConfigedMain.reloadTable"));
		reload.setIcon(de.uib.configed.Globals.createImageIcon("images/reload16.png", ""));
		reload.setFont(Globals.defaultFont);
		reload.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					logging.info(this, "------------- reload action");
					reloadAction();
				}
			}
		);
		popup.add(reload);
		
		JMenuItem createReport = new JMenuItemFormatted();
		createReport.setText(configed.getResourceValue("PanelProductSettings.pdf"));
		createReport.setIcon(de.uib.configed.Globals.createImageIcon("images/acrobat_reader16.png", ""));
		createReport.setFont(Globals.defaultFont);
		createReport.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					logging.info(this, "------------- create report");
					HashMap<String, String> metaData = new HashMap<String, String>();
					String disp_clients = "\n";
					
					// TODO: getFilter
					//       display, if filter is active, 
					//		 display selected productgroup
					//       depot server, selected clients out of statusPane
					
					metaData.put("header", title);
					metaData.put("subject", title);
					title ="";
					if (mainController.getHostsStatusInfo().getInvolvedDepots().length()!=0) {
						title = title + "Depot : " +mainController.getHostsStatusInfo().getInvolvedDepots();
					}
					if (mainController.getHostsStatusInfo().getSelectedClientNames().length()!=0) {
						title = title + "; Clients: " + mainController.getHostsStatusInfo().getSelectedClientNames();
					}
					metaData.put("title", title);
					metaData.put("keywords", "product settings");
					tableToPDF = new DocumentToPdf (null, metaData); //  no filename, metadata
					// set alignment left
					ArrayList list = new ArrayList<Integer>();
					list.add(0); // column 
					de.uib.utilities.pdf.DocumentElementToPdf.setAlignmentLeft(list);
					// only relevant rows
					tableToPDF.createContentElement("table", strippTable(tableProducts));
					tableToPDF.setPageSizeA4_Landscape();  // 
					tableToPDF.toPDF(); //   create Pdf
				}
			}
		);
		popup.add(createReport);

		
		/*
		popup.addSeparator();
		
		JMenuItem findClientsWithOtherProductVersion = new JMenuItemFormatted();
		findClientsWithOtherProductVersion.setText(configed.getResourceValue("ConfigedMain.findClientsWithOtherProductVersion"));
		findClientsWithOtherProductVersion.setFont(Globals.defaultFont);
		findClientsWithOtherProductVersion.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					mainController.selectClientsNotCurrentProductInstalled(selectedProducts);
				}
			}
		);
		popup.add(findClientsWithOtherProductVersion);
		
		// is extremely slow 
		*/
		
		
		JMenu sub = new JMenu(configed.getResourceValue("ConfigedMain.columnVisibility"));
		sub.setFont(Globals.defaultFont);
		popup.addSeparator();
		popup.add(sub);
		
		Iterator iter = checkColumns.keySet().iterator();
		
		while (iter.hasNext())
		{
			final String columnName = (String) iter.next(); 
			
			if (columnName.equals("productId"))
				//fixed column
				continue;
				
			JMenuItem item = new JCheckBoxMenuItem();
			item.setText(InstallationStateTableModel.getColumnTitle(columnName));
			item.setFont(Globals.defaultFont);
			((JCheckBoxMenuItem) item).setState(checkColumns.get(columnName));
			item.addItemListener(new ItemListener()
				{
					public void itemStateChanged(ItemEvent e)
					{
						boolean oldstate = checkColumns.get(columnName);
						checkColumns.put(columnName, !oldstate);
						mainController.requestReloadStatesAndActions();
						mainController.resetView(mainController.getViewIndex());
					}
				}
			);
			
			sub.add(item);
		}
	}
		
	protected JTable strippTable(JTable jTable) {
		boolean dontStrippIt;
		Vector<String[]> data = new Vector<String[]>();
		String[] headers = new String[jTable.getColumnCount()];
		for (int i=0; i<jTable.getColumnCount(); i++) {
			headers[i] = jTable.getColumnName(i);
		}
		

		for (int j=0; j< jTable.getRowCount()  ; j++) {
			dontStrippIt = false;
	    	String[] actCol = new String [jTable.getColumnCount()];
	    	for (int i=0; i<jTable.getColumnCount(); i++) {

		    	String s = "";
		    	try {
		    		s = jTable.getValueAt(j, i).toString();
		    	}
		    	catch (Exception ex){ // nullPointerException, cell empty
		    		s = "";
		    	}	
		    	actCol[i] = s;
		    	jTable.getColumnName(i);
		    	switch (jTable.getColumnName(i)) {
		    	case "Stand":
		    		if (!s.equals("not_installed"))
		    			dontStrippIt = dontStrippIt || true;
		    		break;
		    	case "Report":
		    		if (!s.equals(""))
		    			dontStrippIt = dontStrippIt || true;
		    		break;
		    	case "Angefordert":
		    		if (!s.equals("none"))
		    			dontStrippIt = dontStrippIt || true;
		    		break;
		    	
		    	}
		    }
		    if (dontStrippIt) {
		    	data.add(actCol);
		    }
		
		}
		// create jTable with selected rows
		int rows = data.size();
        int cols = jTable.getColumnCount();
        String[][] strippedData = new String[rows][cols];
        for (int i=0; i<data.size(); i++) {
        	strippedData[i] = data.get(i);
        }
		JTable strippedTable = new JTable(strippedData, headers);

		return strippedTable;
	}
	
	protected void reloadAction()
	{
		mainController.requestReloadStatesAndActions();
		mainController.resetView(mainController.getViewIndex());
		mainController.setDataChanged(false);
	}
	

	public void showPopupOpsiclientdEvent(boolean visible)
	{
		//subOpsiclientdEvent.setVisible(visible);
		itemOnDemand.setVisible(visible);
	}
	
			
		
	public void clearSelection()
	{
		tableProducts.clearSelection();
	}
	
	
	
	public void setSelection(Set selectedIDs)
	{
		clearSelection();
		if (selectedIDs != null)
		{
			if (selectedIDs.size() == 0 && tableProducts.getRowCount() > 0)
			{
				tableProducts.addRowSelectionInterval(0, 0);
				//show first product if no product given
				logging.info(this, "setSelection 0");
			}
			else
			{
				for (int row = 0; row < tableProducts.getRowCount(); row++)
				{
					Object productId = tableProducts.getValueAt(row, 0);
					if (selectedIDs.contains(productId))
						tableProducts.addRowSelectionInterval(row, row);
				}
			}
		}
	}
	
	
	public Set<String> getSelectedIDs()
	{
		HashSet<String> result = new HashSet();
		
		int[] selection = tableProducts.getSelectedRows();
		
		
		for (int i = 0; i < selection.length; i++)
		{
			result.add((String) tableProducts.getValueAt(selection[i], 0));
		}
		
		return result;
	}
	
	
	public int convertRowIndexToModel(int row)
	{
		return tableProducts.convertRowIndexToModel(row);
	}
	
	private class StringComparator implements Comparator<String>
	{
		public int compare(String o1, String o2)
		{
			//logging.info(this, "compare " + o1 + " to " + o2);
			return o1.compareTo(o2);
		}
	}
	
	public void setTableModel (IFInstallationStateTableModel istm)
	{
		tableProducts.setRowSorter(null); //delete old row sorter before setting new model 
		tableProducts.setModel(  istm );
		
		//tableProducts.setAutoCreateRowSorter(true);
		//try bugfix:
		
		final StringComparator myComparator = new StringComparator();
	
		TableRowSorter<TableModel> sorter 
		= new TableRowSorter<TableModel>(tableProducts.getModel())
		{
			
			protected boolean useToString(int column)
			{
				try{
					return super.useToString(column);
				}
				catch (Exception ex)
				{
					logging.info (this, "------------------- no way to string"); 
					return false;
				}
			}
			
			public Comparator<?> getComparator(int column)
			{
				try{
					if (column == 0)
					{
						return  myComparator;
					}
					else
					{
						return super.getComparator(column);
					}
				}
				catch (Exception ex)
				{
					logging.info(this, "------------------- not getting comparator "); 
					return null;
				}
					
				//NullPointerException at java.lang.Class.isAssignableFrom
			}
		}
		
		;
		//sorter.setComparator(0, de.uib.utilities.Globals.getCollator());
		
		tableProducts.setRowSorter(sorter);
		sorter.addRowSorterListener(this);

		
		
		
		
		//tableProducts.getTableHeader().setToolTipText(configed.getResourceValue("MainFrame.tableheader_tooltip"));
		tableProducts.getTableHeader().setDefaultRenderer
			(new ColorHeaderCellRenderer(tableProducts.getTableHeader().getDefaultRenderer()));
		
		//---
		
		
		logging.debug(this, " tableProducts columns  count " + tableProducts.getColumnCount());
		Enumeration enumer = tableProducts.getColumnModel().getColumns();
		
		while (enumer.hasMoreElements())
			logging.debug(this, " tableProducts column  " +  ((TableColumn) enumer.nextElement()).getHeaderValue());
		
		int colIndex = -1;
		
		if ( (colIndex = istm.getColumnIndex(ProductState.KEY_productId)) > -1 )
		{
			TableColumn nameColumn =  tableProducts.getColumnModel().getColumn(colIndex);
			nameColumn.setPreferredWidth(fwidth_column_productname);
			nameColumn.setCellRenderer(productNameTableCellRenderer);
		}
		
		if ( (colIndex = istm.getColumnIndex(ProductState.KEY_productName)) > -1 )
		{
			TableColumn completeNameColumn =  tableProducts.getColumnModel().getColumn(colIndex);
			completeNameColumn.setPreferredWidth(fwidth_column_productcompletename);
			completeNameColumn.setCellRenderer(productCompleteNameTableCellRenderer);
		}
		
		if ( (colIndex = istm.getColumnIndex(ProductState.KEY_targetConfiguration)) > -1)
		{
			TableColumn targetColumn =  tableProducts.getColumnModel().getColumn(colIndex);
			
			String iconsDir = null;
			if (de.uib.configed.Globals.showIconsInProductTable)
				iconsDir = "images/productstate/targetconfiguration"; 
			
			JComboBox targetCombo = new JComboBox();
			targetCombo.setRenderer(standardListCellRenderer);
			//targetColumn.setCellEditor(new AdaptingCellEditor(targetCombo,  istm));
			targetColumn.setCellEditor(
				new AdaptingCellEditorValuesByIndex(
					targetCombo,  
					istm,
					de.uib.opsidatamodel.productstate.TargetConfiguration.getLabel2DisplayLabel(),
					iconsDir
					)
				)
			;
			targetColumn.setPreferredWidth(fwidth_column_productstate);
			targetColumn.setCellRenderer(targetConfigurationTableCellRenderer);
		}
		
		if ( (colIndex = istm.getColumnIndex(ProductState.KEY_installationStatus)) > -1)
		{
			TableColumn statusColumn =  tableProducts.getColumnModel().getColumn(colIndex);
			
			String iconsDir = null;
			if (de.uib.configed.Globals.showIconsInProductTable)
				iconsDir = "images/productstate/installationstatus"; 
			
			JComboBox statesCombo = new JComboBox();
			statesCombo.setRenderer(standardListCellRenderer);
			//statusColumn.setCellEditor(new AdaptingCellEditor(statesCombo,  istm));
			statusColumn.setCellEditor(
				new AdaptingCellEditorValuesByIndex(
					statesCombo,  
					istm,
					de.uib.opsidatamodel.productstate.InstallationStatus.getLabel2DisplayLabel(),
					iconsDir
					)
				)
			;
			statusColumn.setPreferredWidth(fwidth_column_productstate);
			statusColumn.setCellRenderer(installationStatusTableCellRenderer);
		}
		
		if ( (colIndex = istm.getColumnIndex(ProductState.KEY_actionProgress)) > -1)
		{
			TableColumn progressColumn = tableProducts.getColumnModel().getColumn(colIndex);
			
			progressColumn.setPreferredWidth(fwidth_column_productstate);
			progressColumn.setCellRenderer(actionProgressTableCellRenderer);
		}
		
		if ( (colIndex = istm.getColumnIndex(ProductState.KEY_lastAction)) > -1)
		{
			TableColumn lastactionColumn = tableProducts.getColumnModel().getColumn(colIndex);
			lastactionColumn.setPreferredWidth(fwidth_column_productstate);
			lastactionColumn.setCellRenderer(lastActionTableCellRenderer);
		}
		
		if ( (colIndex = istm.getColumnIndex(ProductState.KEY_actionResult)) > -1) 
		{
			TableColumn actionresultColumn = tableProducts.getColumnModel().getColumn(colIndex);
			actionresultColumn.setPreferredWidth(fwidth_column_productstate);
			actionresultColumn.setCellRenderer(actionResultTableCellRenderer);
		}
		
		if ( (colIndex = istm.getColumnIndex(ProductState.KEY_actionRequest)) > -1)
		{
			
			TableColumn actionColumn =  tableProducts.getColumnModel().getColumn(colIndex);
			
			String iconsDir = null;
			if (de.uib.configed.Globals.showIconsInProductTable)
				iconsDir = "images/productstate/actionrequest";
			
			JComboBox actionsCombo = new JComboBox();
			actionsCombo.setRenderer(standardListCellRenderer);
			actionColumn.setCellEditor(
				new AdaptingCellEditorValuesByIndex(
					actionsCombo,  
					istm,
					de.uib.opsidatamodel.productstate.ActionRequest.getLabel2DisplayLabel(),
					iconsDir
					)
				)
			;
			actionColumn.setPreferredWidth(fwidth_column_productstate);
			actionColumn.setCellRenderer(actionRequestTableCellRenderer); //productTableCellRenderer);
		}
		
		
		if ( (colIndex = istm.getColumnIndex(ProductState.KEY_lastStateChange) )  > -1 ) 
		{
			TableColumn laststatechangeColumn =  tableProducts.getColumnModel().getColumn(colIndex);
			laststatechangeColumn.setPreferredWidth(fwidth_column_productsequence);
			
			//laststatechangeColumn.setHorizontalAlignment(SwingConstants.LEFT);
			laststatechangeColumn.setCellRenderer(lastStateChangeTableCellRenderer);
			
			/*
			if (sorter instanceof DefaultRowSorter)
			{
				((DefaultRowSorter) sorter).setComparator(colIndex, new de.uib.utilities.IntComparatorForStrings());
			}
			*/
			
		}
		
		
		if ( (colIndex = istm.getColumnIndex(ProductState.KEY_actionSequence)) > -1)
		{
			TableColumn priorityclassColumn =  tableProducts.getColumnModel().getColumn(colIndex);
			priorityclassColumn.setPreferredWidth(fwidth_column_productsequence);
			
			priorityclassTableCellRenderer.setHorizontalAlignment(SwingConstants.RIGHT);
			priorityclassColumn.setCellRenderer(priorityclassTableCellRenderer);
			
			if (sorter instanceof DefaultRowSorter)
			{
				((DefaultRowSorter) sorter).setComparator(colIndex, new de.uib.utilities.IntComparatorForStrings());
			}
			
			priorityclassColumn.setCellRenderer(priorityclassTableCellRenderer);
			
		}
		
		if ( (colIndex = istm.getColumnIndex(ProductState.KEY_productPriority)) > -1)
		{
			TableColumn priorityclassColumn =  tableProducts.getColumnModel().getColumn(colIndex);
			priorityclassColumn.setPreferredWidth(fwidth_column_productsequence);
			
			priorityclassTableCellRenderer.setHorizontalAlignment(SwingConstants.RIGHT);
			priorityclassColumn.setCellRenderer(priorityclassTableCellRenderer);
			
			//if (sorter instanceof DefaultRowSorter)
			{
				((DefaultRowSorter) sorter).setComparator(colIndex, new de.uib.utilities.IntComparatorForStrings());
			}
		}
		
		if ( (colIndex = istm.getColumnIndex(ProductState.KEY_position)) > -1)
		{
			TableColumn productsequenceColumn =  tableProducts.getColumnModel().getColumn(colIndex);
			productsequenceColumn.setPreferredWidth(fwidth_column_productsequence);
			
			productsequenceTableCellRenderer.setHorizontalAlignment(SwingConstants.RIGHT);
			productsequenceColumn.setCellRenderer(productsequenceTableCellRenderer);
			
			//if (sorter instanceof DefaultRowSorter)
				//((DefaultRowSorter) sorter).setComparator(colIndex, new de.uib.utilities.IntComparatorForStrings());
				// we already have Integer
			
		}
		
		
		if ( (colIndex = istm.getColumnIndex(ProductState.KEY_productVersion)) > -1)
		{
			TableColumn productversionColumn =  tableProducts.getColumnModel().getColumn(colIndex);
			productversionColumn.setPreferredWidth(fwidth_column_productversion);
			productversionColumn.setCellRenderer( productversionTableCellRenderer );
		}
		
		if ( (colIndex = istm.getColumnIndex(ProductState.KEY_packageVersion)) > -1)
		{
			//System.out.println("******* colIndex for packageVersion " + colIndex);
			TableColumn packageversionColumn =  tableProducts.getColumnModel().getColumn(colIndex);
			packageversionColumn.setPreferredWidth(fwidth_column_packageversion);
			packageversionColumn.setCellRenderer( packageversionTableCellRenderer );
		}
		
		
		if ( (colIndex = istm.getColumnIndex(ProductState.KEY_versionInfo)) > -1)
		{
			TableColumn versionInfoColumn =  tableProducts.getColumnModel().getColumn(colIndex);
			versionInfoColumn.setPreferredWidth(fwidth_column_versionInfo);
			versionInfoColumn.setCellRenderer( versionInfoTableCellRenderer );
		}
		
		if ( (colIndex = istm.getColumnIndex(ProductState.KEY_installationInfo)) > -1)
		{
			TableColumn installationInfoColumn =  tableProducts.getColumnModel().getColumn(colIndex);
			installationInfoColumn.setPreferredWidth(fwidth_column_installationInfo);
			installationInfoColumn.setCellRenderer( installationInfoTableCellRenderer );
			
			JComboBox installationInfoCombo = new JComboBox();
			//installationInfoCombo.setEditable(true);
			/*
			try
			{
				JTextField field = (JTextField) installationInfoCombo.getEditor();
				field.getCaret().setBlinkRate(0);
			}
			catch(Exception ex)
			{
				logging.debug(this, "installationInfoCombo.getEditor() " + ex);
			}
			*/
 
			installationInfoCombo.setRenderer(standardListCellRenderer);
			
			DynamicCellEditor cellEditor = new DynamicCellEditor(
					installationInfoCombo,  
					istm,
					InstallationInfo.defaultDisplayValues
					)
             ;
                
			installationInfoColumn.setCellEditor(cellEditor);
		}
		
		sorter.setSortKeys(sortkeysDefault);
				
	}
	
	public void initEditing(
				String productID,
				String productTitle, 
				String productInfo, 
				String productHint,
				String productVersion,
				//String productPackageversion,
				String productCreationTimestamp,
				
				Map<String, Boolean> specificPropertiesExisting, 
				Collection storableProductProperties, 
				Map editableProductProperties,
				
				//editmappanelx
				Map<String, de.uib.utilities.table.ListCellOptions> productpropertyOptionsMap,
				
				/*
				//editmappanel
				Map productpropertiesValuesMap,
				Map productpropertiesDescriptionsMap,
				Map productpropertiesDefaultsMap,
				*/
				
				
				ProductpropertiesUpdateCollection updateCollection)
	
	{
		infoPane.setGrey(false);
		infoPane.setId(productID);
		infoPane.setName(productTitle);
		infoPane.setInfo(productInfo);
		infoPane.setProductVersion(productVersion);
		//infoPane.setPackageVersion(productPackageversion);
		infoPane.setAdvice(productHint);
		
		
		infoPane.setSpecificPropertiesExisting(specificPropertiesExisting);
		
		Globals.checkCollection(this, "initEditing", "editableProductProperties ", editableProductProperties);
		Globals.checkCollection(this, "initEditing", "productpropertyOptionsMap", productpropertyOptionsMap);
		
		propertiesPanel.setEditableMap( 
			
			//visualMap (merged for different clients)
			editableProductProperties,
			productpropertyOptionsMap
			
			/*
			editableProductProperties,
			
			//editmappanelx
			//productpropertiesOptionsMap
			
			//editmappanel
			productpropertiesValuesMap, 
			productpropertiesDescriptionsMap,  
			productpropertiesDefaultsMap
			*/
			);
			
		propertiesPanel.setStoreData( storableProductProperties );
		propertiesPanel.setUpdateCollection (updateCollection);
		
	}
	
	public void clearListEditors()
	{
		if (propertiesPanel instanceof EditMapPanelX)
			((EditMapPanelX) propertiesPanel).cancelOldCellEditing();
	}
	
	public void clearEditing()
	{
		
		initEditing( 
					"", //String productTitle, 
					"", //String productInfo, 
					"", //String productHint,
					"", //String productVersion,
					"", //String productPackageversion,
					"", //String productCreationTimestamp,
					
					null, //new HashMap<String, Boolean>,
					null, //Collection storableProductProperties,
					
					null, //Map editableProductProperties,
					
					//editmappanelx
					null, //Map<String, de.uib.utilities.table.ListCellOptions> productpropertyOptionsMap
					
					/*
					//editmappanel
					null, //Map productpropertiesValuesMap,
					null, //Map productpropertiesDescriptionsMap,
					null, //Map productpropertiesDefaultsMap,
					*/
					
					null //ProductpropertiesUpdateCollection updateCollection)
				 );
		infoPane.setGrey(true);
	}
	
	
	//RowSorterListener for table row sorter
	public void sorterChanged(RowSorterEvent e)
	{
		logging.debug(this, "RowSorterEvent " + e);
	}
	
}

