package de.uib.configed.gui;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;

import java.util.*;
import java.math.*;
import java.text.MessageFormat;

import de.uib.configed.*;
import de.uib.configed.type.*;
import de.uib.utilities.*;
import de.uib.utilities.logging.*;
import de.uib.utilities.pdf.DocumentToPdf;
import de.uib.utilities.swing.*;
import de.uib.utilities.table.*;
import de.uib.utilities.table.provider.*;
import de.uib.utilities.table.gui.*;




public class PanelSWInfo extends JPanel
{
	protected PanelGenEditTable panelTable;
	//protected JPanel subPanelButtons;
	protected JPanel subPanelTitle;
	
	protected JScrollPane jScrollPane;
	protected JTable jTable;
	protected SWInfoTableModel tableModel;
	protected GenTableModel modelSWInfo;
	protected JLabel jLabelTitle;
	protected JLabel labelTitle;
	
	protected String title = "";
	protected DatedRowList swRows;
	protected String hostId = "";
	protected boolean withPopup;
	
	protected PopupMenuTrait popupMenu;
	
	protected DocumentToPdf tableToPDF;
	
	protected int hGap = de.uib.utilities.Globals.hGapSize/2;
	protected int vGap = de.uib.utilities.Globals.vGapSize/2;
	protected int hLabel = de.uib.utilities.Globals.buttonHeight;
	
	protected ConfigedMain mainController;
	
	
	
	JCheckBox checkWithMsUpdates;
	protected boolean withMsUpdates = false;
	final static String FILTER_MS_UPDATES = "withMsUpdates";
	int indexOfColWindowsSoftwareID;
	
	de.uib.utilities.table.TableModelFilterCondition filterConditionWithMsUpdates =  
		new de.uib.utilities.table.TableModelFilterCondition()
		{
			public void setFilter(TreeSet<Object> filter)
			{
			}
			
			public boolean test(Vector<Object> row)
			{
				String entry = (String) row.get( indexOfColWindowsSoftwareID );
				boolean isKb =  entry.startsWith( "kb" );
				
				/*
				if (!isBK)
				{
					String[] parts = entry.split
					isKb = entry.endsWiths(
				}
				*/
					
				return !isKb;
				// on filtering active everything is taken if not isKb 
				
			}
		};
	
	JCheckBox checkWithMsUpdates2;
	protected boolean withMsUpdates2 = true;
	final static String FILTER_MS_UPDATES2 = "withMsUpdates2";
	
	final java.util.regex.Pattern patternWithKB = java.util.regex.Pattern.compile("\\{.*\\}\\p{Punct}kb.*");
	
	de.uib.utilities.table.TableModelFilterCondition filterConditionWithMsUpdates2 =  
		new de.uib.utilities.table.TableModelFilterCondition()
		{
			public void setFilter(TreeSet<Object> filter)
			{
			}
			
			public boolean test(Vector<Object> row)
			{
				String entry = (String) row.get( indexOfColWindowsSoftwareID );
				boolean isKb =  (patternWithKB.matcher(entry)).matches();
					
				return !isKb;
				// on filtering active everything is taken if not isKb 
				
			}
		};
	
	

	public PanelSWInfo (ConfigedMain mainController)
	{
		this(true, mainController);
	}
	
	public PanelSWInfo (boolean withPopup, ConfigedMain mainController)
	{
		this.withPopup =  withPopup;
		this.mainController  = mainController;
		initTable();
		buildPanel();
	}
	
	
	protected void initTable()
	{
		labelTitle = new JLabel();
		labelTitle.setFont(de.uib.utilities.Globals.defaultFontBold);
		
		panelTable = new PanelGenEditTable("title",
			0, false, 0, true,
			    new int[]{
			    	//PanelGenEditTable.POPUP_RELOAD,
			    	//PanelGenEditTable.POPUP_FLOATINGCOPY,
			    	//PanelGenEditTable.POPUP_PDF
			    },
			    true
			    )
			{ 
				/*
				@Override
				protected void floatExternal()
				{
					floatExternalX();
				}
				
				@Override 
				public void reload()
				{
					//mainController.getPersistenceController().installedSoftwareInformationRequestRefresh();
					//mainController.getPersistenceController().softwareAuditOnClientsRequestRefresh();
					super.reload();
				}
				*/
			}
		;
					
		
		panelTable.setTitle("");
		//panelTable.setSearchColumnsAll(); we need the model for setting all columns, therefore postponed
		panelTable.setColumnSelectionAllowed(false); //up to now, true is destroying search function 
		
		panelTable.setListSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		
		panelTable.setSearchSelectMode(true);
		panelTable.setSearchMode(SearchPane.FULL_TEXT_SEARCH);
	
		
		Vector<String> columnNames;
		Vector<String> classNames; 
			
		columnNames = new Vector<String>(SWAuditClientEntry.KEYS);
		columnNames.remove(0);
		classNames = new Vector<String>();
		int[] finalColumns = new int[columnNames.size()];
		for (int i = 0; i < columnNames.size(); i++)
		{
			classNames.add("java.lang.String");
			finalColumns[i] = i;
		}
		
		modelSWInfo = 
			new GenTableModel(
				null, //no updates
				new DefaultTableProvider(
						new RetrieverMapSource(columnNames, classNames,
							new MapRetriever(){
								public Map<String, Map> retrieveMap()
								{
									logging.info(this, "retrieving data for " + hostId);
									Map<String, Map> tableData = mainController.getPersistenceController().retrieveSoftwareAuditData (hostId);
									logging.info(this, "retrieved size  " + tableData.keySet().size());
									
									if (tableData == null || tableData.keySet().size() == 0)
										setTitle ( de.uib.configed.configed.getResourceValue("PanelSWInfo.noScanResult") );
									else
									
										setTitle("Scan " + 
										
											mainController.getPersistenceController().getLastSoftwareAuditModification(hostId)
										);
									
									return tableData; 
								}
							})
						),	
				-1,
				finalColumns,
				null,
				null);

		
		indexOfColWindowsSoftwareID = columnNames.indexOf( SWAuditEntry.WINDOWSsOFTWAREid );
		modelSWInfo.chainFilter(FILTER_MS_UPDATES, new TableModelFilter( filterConditionWithMsUpdates ) );
		modelSWInfo.reset();
		modelSWInfo.setUsingFilter(FILTER_MS_UPDATES, withMsUpdates);
		modelSWInfo.toggleFilter( FILTER_MS_UPDATES );
		panelTable.setDataChanged( false );
		checkWithMsUpdates = new JCheckBox("", withMsUpdates);
		checkWithMsUpdates.setForeground(de.uib.configed.Globals.blue);
		checkWithMsUpdates.addItemListener(new ItemListener(){
				public void itemStateChanged(ItemEvent e)
				{
					if (modelSWInfo == null)
						return;
					
					//logging.debug(this, "itemStateChanged, selected " + checkWithMsUpdates.isSelected() + " ... --- "  + e);
					
					boolean saveDataChanged = panelTable.isDataChanged();
					withMsUpdates= checkWithMsUpdates.isSelected();
					modelSWInfo.toggleFilter( FILTER_MS_UPDATES );
					panelTable.setDataChanged( saveDataChanged );
				}
			}
		);
		
		modelSWInfo.chainFilter(FILTER_MS_UPDATES2, new TableModelFilter( filterConditionWithMsUpdates2 ) );
		modelSWInfo.reset();
		modelSWInfo.setUsingFilter(FILTER_MS_UPDATES2, withMsUpdates2);
		modelSWInfo.toggleFilter( FILTER_MS_UPDATES2 );
		panelTable.setDataChanged( false );
		checkWithMsUpdates2 = new JCheckBox("", withMsUpdates2);
		checkWithMsUpdates2.setForeground(de.uib.configed.Globals.blue);
		checkWithMsUpdates2.addItemListener(new ItemListener(){
				public void itemStateChanged(ItemEvent e)
				{
					if (modelSWInfo == null)
						return;
					
					//logging.debug(this, "itemStateChanged, selected " + checkWithMsUpdates.isSelected() + " ... --- "  + e);
					
					boolean saveDataChanged = panelTable.isDataChanged();
					withMsUpdates2= checkWithMsUpdates2.isSelected();
					modelSWInfo.toggleFilter( FILTER_MS_UPDATES2 );
					panelTable.setDataChanged( saveDataChanged );
				}
			}
		);
		
		
		
		subPanelTitle = new JPanel();
		
		JLabel labelWithMSUpdates = new JLabel( configed.getResourceValue("PanelSWInfo.withMsUpdates") );
		JLabel labelWithMSUpdates2 = new JLabel( configed.getResourceValue("PanelSWInfo.withMsUpdates2") );
		
		subPanelTitle.setBackground(de.uib.configed.Globals.backLightBlue);
		
		GroupLayout layoutSubPanelTitle = new GroupLayout(subPanelTitle);
		subPanelTitle.setLayout(layoutSubPanelTitle);
		
		layoutSubPanelTitle.setHorizontalGroup( layoutSubPanelTitle.createSequentialGroup()
			.addGap(hGap, hGap, hGap)
			.addGroup(layoutSubPanelTitle.createParallelGroup()
				.addComponent(labelTitle, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
				.addGroup( layoutSubPanelTitle.createSequentialGroup()
					.addComponent(labelWithMSUpdates, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
					.addGap(hGap, hGap, hGap)
					.addGap(hGap, hGap, hGap)
					.addComponent(checkWithMsUpdates, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
				)
			)
			.addGap(hGap, hGap, hGap)
			.addGroup( layoutSubPanelTitle.createSequentialGroup()
					.addComponent(labelWithMSUpdates2, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
					.addGap(hGap, hGap, hGap)
					.addGap(hGap, hGap, hGap)
					.addComponent(checkWithMsUpdates2, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
				)
			.addGap(hGap, hGap, hGap)
		);
		layoutSubPanelTitle.setVerticalGroup(  layoutSubPanelTitle.createSequentialGroup()
			.addGap(vGap, vGap, vGap)
			.addComponent(labelTitle, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
			.addGap(vGap, vGap, vGap)
			//.addGap(vGap, vGap, vGap)
			.addGroup( layoutSubPanelTitle.createParallelGroup( GroupLayout.Alignment.CENTER )
				.addComponent(labelWithMSUpdates, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
				.addComponent(checkWithMsUpdates, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
				.addComponent(labelWithMSUpdates2, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
				.addComponent(checkWithMsUpdates2, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
			)
			.addGap(vGap, vGap, vGap)
			.addGap(vGap, vGap, vGap)
		);
			
		
		//subPanelTitle.setBorder(BorderFactory.createLineBorder(de.uib.configed.Globals.blueGrey));
		
		
		
		panelTable.setTableModel( modelSWInfo);
		panelTable.setSearchColumnsAll();
		
		panelTable.getColumnModel().getColumn(0).setPreferredWidth(400);
		panelTable.getColumnModel().getColumn(1).setPreferredWidth(200);
		panelTable.getColumnModel().getColumn(2).setPreferredWidth(100);
	
		
	}
	
	protected void buildPanel()
	{
		
		jLabelTitle = new JLabel("");
		jLabelTitle.setOpaque(true);
		jLabelTitle.setBackground(de.uib.utilities.Globals.backgroundLightGrey);
		
		tableModel = new SWInfoTableModel();
		
		/*logging.info(this, "tableModel cols hopefully   " + SWAuditEntry.KEYS);//.getKeys());
		for (int i = 0; i<tableModel.getColumnCount(); i++)
		{
			logging.info(this, "tableModel col " + i + " " + tableModel.getColumnName(i));
		}
		System.exit(0);
		*/
		
		jTable = new JTable( tableModel, null );
		
		jTable.setAutoCreateRowSorter(true);
		TableRowSorter tableSorter = (TableRowSorter)jTable.getRowSorter();
		ArrayList<RowSorter.SortKey> list = new ArrayList<RowSorter.SortKey>(1);
		list.add(new RowSorter.SortKey(0, SortOrder.ASCENDING));
		tableSorter.setSortKeys(list);
		tableSorter.sort();
		
		jTable.setDefaultRenderer(Object.class, new StandardTableCellRenderer());
		jTable.getColumnModel().getColumn(0).setPreferredWidth(400);
		jTable.getColumnModel().getColumn(1).setPreferredWidth(200);
		jTable.getColumnModel().getColumn(2).setPreferredWidth(100);
		jTable.getTableHeader().setDefaultRenderer(new ColorHeaderCellRenderer(jTable.getTableHeader().getDefaultRenderer()));
		jTable.setColumnSelectionAllowed(true);
		jTable.setRowSelectionAllowed(true);
		jTable.setDragEnabled(true);
		jScrollPane = new JScrollPane(jTable);
		jScrollPane.getViewport().setBackground(de.uib.utilities.Globals.backLightBlue);
		
		GroupLayout layoutEmbed = new GroupLayout(this);
		setLayout(layoutEmbed);
		
		layoutEmbed.setHorizontalGroup(
			layoutEmbed.createSequentialGroup()
               .addGap(hGap, hGap, hGap)
               .addGroup(layoutEmbed.createParallelGroup()
               	   /*
               	   .addGroup(layoutEmbed.createSequentialGroup()
               	   	   .addGap(hGap, hGap, hGap)
               	   	   .addComponent(jLabelTitle,  javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
               	   	   .addGap(hGap, hGap, hGap)
               	   	)
               	   */
               	   //.addComponent(jScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
               	   .addComponent(subPanelTitle, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
               	   //.addComponent(subPanelButtons, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
               	   .addComponent(panelTable, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
               )
               .addGap(hGap, hGap, hGap)
             );
        
        layoutEmbed.setVerticalGroup(
			layoutEmbed.createSequentialGroup()
				//.addGap(vGap, vGap, vGap)
				//.addComponent(jLabelTitle,  hLabel, hLabel, hLabel)
				//.addGap(vGap, vGap, vGap)
				//.addComponent(jScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
				.addGap(vGap, vGap, vGap)
				.addComponent(subPanelTitle, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE) 
				//.addComponent(subPanelButtons,  javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE) 
				.addComponent(panelTable, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
				.addGap(vGap, vGap, vGap)
			);
	
		
		
		if (withPopup)
		{
			popupMenu = new PopupMenuTrait(new Integer[] {PopupMenuTrait.POPUP_RELOAD, PopupMenuTrait.POPUP_PDF, PopupMenuTrait.POPUP_FLOATINGCOPY})
				{
					public void action(int p)
					{
						switch(p)
						{
							case PopupMenuTrait.POPUP_RELOAD:
								reload();
								break;
								
							case PopupMenuTrait.POPUP_FLOATINGCOPY:
								floatExternalX();
								break;
							case PopupMenuTrait.POPUP_PDF:
								logging.info(this, "------------- create report");
								HashMap<String, String> metaData = new HashMap<String, String>();
								MessageFormat mf = new MessageFormat( configed.getResourceValue("PanelSWInfo.jLabel_title") );
								metaData.put("header", " " + mf.format( new String[] { hostId, swRows.getDate()} ));
								title ="";
								if (mainController.getHostsStatusInfo().getInvolvedDepots().length()!=0) {
									title = title + "Depot : " +mainController.getHostsStatusInfo().getInvolvedDepots();
								}
								if (mainController.getHostsStatusInfo().getSelectedClientNames().length()!=0) {
									title = title + "; Client: " + mainController.getHostsStatusInfo().getSelectedClientNames();
								}
								metaData.put("title",  title);
								metaData.put("subject", "report of table");
								metaData.put("keywords", "software inventory");
								tableToPDF = new DocumentToPdf (null, metaData); //  no filename, metadata
								
								ArrayList list = new ArrayList<Integer>();
								list.add(0); // column(s) 
								de.uib.utilities.pdf.DocumentElementToPdf.setAlignmentLeft(list);
								
								tableToPDF.createContentElement("table", jTable);
								tableToPDF.setPageSizeA4_Landscape();  // 
								tableToPDF.toPDF(); //   create Pdf
								break;
				
						}
					}
					
				}
			;
								
			popupMenu.addPopupListenersTo(new JComponent[]{this, panelTable.getTheTable(), jTable, jScrollPane, jScrollPane.getViewport()});
		}
	
	}
	
	
		
	public void setTitle(String s)
	{
		title = "  " + s + " " + title;
		labelTitle.setText(s);
		//labelTitle.setText("Scan " + s);
		//panelTable.setTitle(s);
	}
	
	
	
	/** overwrite in subclasses */
	protected void reload()
	{
		logging.debug(this, "reload action");
	}
	
	
	protected void floatExternalX()
	{
		
		PanelSWInfo copyOfMe;
		de.uib.configed.gui.GeneralFrame  externalView;
		
		copyOfMe = new PanelSWInfo(false, mainController);
		copyOfMe.setSoftwareInfo(hostId, swRows);
		
		externalView = new de.uib.configed.gui.GeneralFrame(null, title, false);
		externalView.addPanel(copyOfMe);
		externalView.setup();
		externalView.setSize(this.getSize());
		externalView.centerOn(de.uib.configed.Globals.mainFrame);
	
		externalView.setVisible(true);
	}
	
	
	
	
	public void setSoftwareInfo (String hostId, DatedRowList swRows)
	{
		logging.info(this, "setSoftwareInfo for " + hostId + " -- " );
		
		this.hostId = "" + hostId;
		
		String timeS = "" + de.uib.utilities.Globals.getToday();
		String[] parts = timeS.split(":");
		if (parts.length > 2)
			timeS = parts[0] + ":" + parts[1];
		
		
		//panelTable.setTitle("(no software audit data, checked at time:  " + timeS + ")" );
		
		
		panelTable.reload();
		
		
		
		
		title = hostId;
		//this.hostId = hostId;
		this.swRows = swRows;
		
		jLabelTitle.setText(" (no software audit data, checked at time:  " + timeS + ")");
		
		if (swRows == null)
		{
			tableModel.setData(new DatedRowList());
			return;
		}
		
		if (swRows.getDate() != null)
		{
			MessageFormat mf = new MessageFormat( configed.getResourceValue("PanelSWInfo.jLabel_title") );
			jLabelTitle.setText(" " + mf.format( new String[] { hostId, swRows.getDate()} ));
			title = title + "   " + configed.getResourceValue("PanelSWInfo.title");
		}
		
		//jLabelTitle.setText(" " + swRows.getDate()); 
		
		tableModel.setData(swRows);
		
	}
	
	public class SWInfoTableModel extends AbstractTableModel
	{
		private java.util.List<String[]> data;
		//private String dateS;
		private final String[] header = {
			configed.getResourceValue("PanelSWInfo.tableheader_displayName"),
			configed.getResourceValue("PanelSWInfo.tableheader_softwareId"),
			configed.getResourceValue("PanelSWInfo.tableheader_displayVersion")
		};
		
		public SWInfoTableModel()
		{
			super();
			data = new ArrayList();
		}
		
		public void setData(DatedRowList datedList)
		{
			this.data = datedList.getRows();
			//dateS = datedList.getDate();
			
			fireTableDataChanged();
		}
		
		public int getRowCount()
		{
			return data.size();
		}
		
		public int getColumnCount()
		{
			return SWAuditClientEntry.getDisplayKeys().size(); //not key "ID";
		}
		
		public String getColumnName(int column) {
			return SWAuditClientEntry.getDisplayKey(column + 1);
		}
		
		public Object getValueAt(int row, int col)
		{
			return (/*encodeString */ (String[]) data.get(row))[col + 1];
		}
	}
	

}


