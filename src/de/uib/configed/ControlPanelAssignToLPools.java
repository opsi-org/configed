package de.uib.configed;

import java.util.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.table.*;
import javax.swing.event.*;
import de.uib.utilities.*;
import de.uib.utilities.swing.*;
import de.uib.utilities.swing.tabbedpane.*;
import de.uib.utilities.table.*;
import de.uib.utilities.table.updates.*;
import de.uib.utilities.table.provider.*;
import de.uib.configed.gui.licences.*;
import de.uib.opsidatamodel.PersistenceController;
import de.uib.utilities.logging.*;

public class ControlPanelAssignToLPools extends ControlMultiTablePanel
//Tab Licencepools
{
	
	PanelAssignToLPools thePanel;
	TableUpdateCollection updateCollection;
	
	GenTableModel modelLicencepools;
	GenTableModel modelProductId2LPool;
	GenTableModel modelWindowsSoftwareIds;
	
	TableModelFilterCondition windowsSoftwareIdsFilterCondition;
	
	ConfigedMain mainController;
	
	PersistenceController persist;
	
	public enum SoftwareShowMode {ALL, ASSIGNED, NOT_ASSIGNED};
	private SoftwareShowMode softwareShow = SoftwareShowMode.ALL; 
	
	public ControlPanelAssignToLPools(PersistenceController persist, ConfigedMain mainController)
	{
		thePanel = new PanelAssignToLPools(this) ;
		this.persist = persist;
		this.mainController = mainController; 
		init();
		
	}
	
	public TabClientAdapter getTabClient()
	{
		return thePanel;
	}
	
	
	private void setWindowsSoftwareIdsFromLicencePool()
	{
		logging.info(this, "thePanel.panelLicencepools.getSelectedRow() " + thePanel.panelLicencepools.getSelectedRow());

				
		if (thePanel.panelLicencepools.getSelectedRow() < 0)
			return;
		
		String licencePoolID = thePanel.panelLicencepools.getValueAt(
							thePanel.panelLicencepools.getSelectedRow(), 0).toString();
				
		modelWindowsSoftwareIds.setFilter(null);
		
		thePanel.fieldSelectedLicencePoolId.setText(licencePoolID);
		thePanel.fieldSelectedLicencePoolId.setToolTipText(licencePoolID);
		
		
		List windowsSoftwareIds =  persist.getSoftwareListByLicencePool(licencePoolID);
		

		logging.debug(this, "software ids " + windowsSoftwareIds);
		//thePanel.fieldCountAllWindowsSoftware
		
		thePanel.fieldCountAssignedWindowsSoftware.setText("0");
		thePanel.fieldCountAssignedAuditedSoftware.setText("0");
		thePanel.fieldCountAssignedWindowsSoftware.setToolTipText(" <html><br /></html>");
		if (windowsSoftwareIds != null)
		{
			thePanel.fieldCountAssignedWindowsSoftware.setText(""  + windowsSoftwareIds.size());
			
			StringBuffer b = new StringBuffer("<html><br />");
			for (Object ident : windowsSoftwareIds)
			{
				b.append(ident.toString());
				b.append("<br />");
			}
			b.append("</html>");
			thePanel.fieldCountAssignedWindowsSoftware.setToolTipText(b.toString());
		}
		
		if (windowsSoftwareIds == null)
			windowsSoftwareIds = new ArrayList();
		
		/* test
		windowsSoftwareIds = new ArrayList<String>();
		windowsSoftwareIds.add("Microsoft Office Office 64-bit Components 2010;14.0.6029.1000;;;x64");
		*/
		
		//if ( windowsSoftwareIds != null) // &&  windowsSoftwareIds.size() > 0 )
		//modelWindowsSoftwareIds.setFilter(new TreeSet( windowsSoftwareIds ));
		
		
		boolean usingFilter = modelWindowsSoftwareIds.isUsingFilter( GenTableModel.DEFAULT_FILTER_NAME );
		
		thePanel.fieldCountAssignedAuditedSoftware.setText("" + modelWindowsSoftwareIds.getRowCount());
		modelWindowsSoftwareIds.setUsingFilter( GenTableModel.DEFAULT_FILTER_NAME,  usingFilter);
		
		switch (softwareShow)
		{
			case ALL:
				windowsSoftwareIdsFilterCondition.setFilter(null); break;
				
			case ASSIGNED:
				windowsSoftwareIdsFilterCondition.setFilter(new TreeSet( windowsSoftwareIds )); 
			/*				
				break;
				
				
			case NOT_ASSIGNED:
				modelWindowsSoftwareIds.setInvertedFilter(new TreeSet( windowsSoftwareIds));
			*/
		}
		
		
		thePanel.panelWindowsSoftware.setAwareOfSelectionListener(false);
		thePanel.panelWindowsSoftware.setSelectedValues(windowsSoftwareIds, 0);
		
		if (windowsSoftwareIds.size() > 0)
			thePanel.panelWindowsSoftware.moveToValue(
				windowsSoftwareIds.get(windowsSoftwareIds.size()-1).toString(), 
				0, false);
		
		thePanel.panelWindowsSoftware.setDataChanged(false);
		thePanel.panelWindowsSoftware.setAwareOfSelectionListener(true);
		
	}
	
	
	public void validateWindowsSoftwareKeys()
	//called by valueChanged method of ListSelectionListener
	{
		if (thePanel.panelLicencepools.getSelectedRow() ==-1)
			return;
		
		String licencePoolID = thePanel.panelLicencepools.getValueAt(
							thePanel.panelLicencepools.getSelectedRow(), 0).toString();
				
		java.util.List windowsSoftwareIds =  persist.getSoftwareListByLicencePool(licencePoolID);
		//logging.debug(this, "software ids " + windowsSoftwareIds);
		
		java.util.List<String> selKeys = thePanel.panelWindowsSoftware.getSelectedKeys();
		
		if (selKeys == null)
		{
			thePanel.fieldCountAssignedAuditedSoftware.setText("0");
			return;
		}
		
		ArrayList<String> removeKeys = new ArrayList<String>();
		
		
		for (String key : selKeys)
		{
			//key is already assigned to a different licencePool?
			if  (
				persist.getLicencePoolBySoftwareId(key) != null
				&& !(persist.getLicencePoolBySoftwareId(key).equals(licencePoolID))
			)
			{	
				String info = configed.getResourceValue("PanelAssignToLPools.warningSoftwareAlreadyAssigned") 
				+ " \n" + persist.getLicencePoolBySoftwareId(key);
				String title = configed.getResourceValue("PanelAssignToLPools.warningSoftwareAlreadyAssigned.title"); 
				
				JOptionPane.showMessageDialog( thePanel, 
					info,
					title,
					JOptionPane.INFORMATION_MESSAGE);
				
				logging.info(" software with ident \"" + key + "\" already to license pool " +  persist.getLicencePoolBySoftwareId(key));
				removeKeys.add(key);
			}
		}
		//logging.debug(this, "removeKeys " + removeKeys);
		
		if (removeKeys.size() > 0)
		{
			selKeys.removeAll(removeKeys);
			//logging.debug(this, "after removal " + selKeys);
			
			thePanel.panelWindowsSoftware.setSelectedValues(selKeys, 0);
		}
		
		thePanel.fieldCountAssignedAuditedSoftware.setText("" + selKeys.size());
		
	}
		
	
	public void init()
	{
		updateCollection = new TableUpdateCollection();
		
		Vector<String> columnNames;
		Vector<String> classNames; 
	
		//--- panelLicencepools
		columnNames = new Vector<String>();
		columnNames.add("licensePoolId"); columnNames.add("description");
		classNames = new Vector<String>();
		classNames.add("java.lang.String"); classNames.add("java.lang.String");
		MapTableUpdateItemFactory updateItemFactoryLicencepools = new MapTableUpdateItemFactory(modelLicencepools, columnNames, classNames, 0);
		modelLicencepools =
			new GenTableModel(
				updateItemFactoryLicencepools,
				mainController.licencePoolTableProvider,
				/*
				new DefaultTableProvider(
					new RetrieverMapSource(columnNames, classNames,
						new MapRetriever(){
							public Map retrieveMap()
							{
								return persist.getLicencePools();
							}
						})
					),
				*/
			
				0, 
				(TableModelListener) thePanel.panelLicencepools,  
				updateCollection);
		updateItemFactoryLicencepools.setSource(modelLicencepools);
		
		tableModels.add(modelLicencepools);
		tablePanes.add(thePanel.panelLicencepools);
				
		modelLicencepools.reset();		
		thePanel.panelLicencepools.setTableModel(modelLicencepools);
		modelLicencepools.setEditableColumns(new int[]{0,1});
		thePanel.panelLicencepools.setEmphasizedColumns(new int[]{0,1});
		
		JMenuItemFormatted menuItemAddPool = new JMenuItemFormatted(configed.getResourceValue("ConfigedMain.Licences.NewLicencepool"));
		menuItemAddPool.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e)
			{
				Object[] a = new Object[2];
				a[0] = "";
				a[1] = "";
				modelLicencepools.addRow(a);
				//thePanel.panelLicencepools.moveToLastRow();
				thePanel.panelLicencepools.moveToValue("" + a[0], 0);
				
				//setting back the other tables is provided by ListSelectionListener 
				//thePanel.panelProductId2LPool.setSelectedValues(null, 0);
				//setWindowsSoftwareIdsFromLicencePool();
			}
		});
		
		thePanel.panelLicencepools.addPopupItem(menuItemAddPool);
		
		// special treatment of columns
		javax.swing.table.TableColumn col;
		
		
		//updates
		thePanel.panelLicencepools.setUpdateController(
			new MapItemsUpdateController(
				thePanel.panelLicencepools,
				modelLicencepools,
				new MapBasedUpdater(){
					public String sendUpdate(Map<String, Object> rowmap){
						
						//hack for avoiding unvoluntary reuse of a licence pool id
						boolean existsNewRow = 
						(
							mainController.licencePoolTableProvider.getRows().size() 
							< 
							modelLicencepools.getRowCount()
						);
						
						
						if ( 
							existsNewRow
							&&
							persist.getLicencepools().containsKey( (String) rowmap.get("licensePoolId") )
						)
						{
							//signalled even if only one of several rows fulfill the condition;
							//but we leave it until the service methods reflect the situation more accurately
							//logging.error("licence pool already existing");
							
							String info = configed.getResourceValue("PanelAssignToLPools.licencePoolIdAlreadyExists") 
								+ " \n(\"" +  rowmap.get("licensePoolId") + "\" ?)";
					
							String title = configed.getResourceValue("PanelAssignToLPools.licencePoolIdAlreadyExists.title"); 
							
							JOptionPane.showMessageDialog( thePanel, 
								info,
								title,
								JOptionPane.INFORMATION_MESSAGE);
								
							//modelLicencepools.reset();
							return null; //no success
						}
						
						if ( existsNewRow )
							modelLicencepools.requestReload();
						
						return persist.editLicencePool(
							(String) rowmap.get("licensePoolId"),
							(String) rowmap.get("description")
						);
					}
					public boolean sendDelete(Map<String, Object> rowmap){
						modelLicencepools.requestReload();
						return persist.deleteLicencePool(
							(String) rowmap.get("licensePoolId")
						);
					}
				},
				updateCollection
			)
		);
	
		
		
		//--- panelProductId2LPool
		columnNames = new Vector<String>();
		columnNames.add("licensePoolId"); columnNames.add("productId"); 
		classNames = new Vector<String>();
		classNames.add("java.lang.String"); classNames.add("java.lang.String"); 
		MapTableUpdateItemFactory updateItemFactoryProductId2LPool = new MapTableUpdateItemFactory(modelProductId2LPool, columnNames, classNames, 0); 
		modelProductId2LPool =
			new GenTableModel(
				updateItemFactoryProductId2LPool,
				new DefaultTableProvider(
					new RetrieverMapSource(columnNames, classNames,
						new MapRetriever(){
							public Map retrieveMap()
							{
								return persist.getRelationsProductId2LPool();
							}
						})
					),
			
				-1,  new int[]{0,1},
				(TableModelListener) thePanel.panelProductId2LPool,  
				updateCollection);
		updateItemFactoryProductId2LPool.setSource(modelProductId2LPool);
		
		tableModels.add(modelProductId2LPool);
		tablePanes.add(thePanel.panelProductId2LPool);
		
		modelProductId2LPool.reset();		
		thePanel.panelProductId2LPool.setTableModel(modelProductId2LPool);
		modelProductId2LPool.setEditableColumns(new int[]{0,1});
		thePanel.panelProductId2LPool.setEmphasizedColumns(new int[]{0,1});
		
		
		JMenuItemFormatted menuItemAddRelationProductId2LPool = new JMenuItemFormatted(configed.getResourceValue("ConfigedMain.Licences.NewRelationProductId2LPool"));
		menuItemAddRelationProductId2LPool.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e)
			{
				//logging.info(this, "actionPerformed" ); 
				Object[] a = new Object[2];
				a[0] = "";
				if (thePanel.panelLicencepools.getSelectedRow() > -1)
					a[0] = modelLicencepools.getValueAt
					(thePanel.panelLicencepools.getSelectedRowInModelTerms(), 0);
					
				a[1] = "";
				
				modelProductId2LPool.addRow(a);
				//thePanel.panelProductId2LPool.moveToLastRow();
				//logging.info(this, "addRelationProductId2LPool.addActionListener line with a[0] " + a[0]);
				thePanel.panelProductId2LPool.moveToValue("" + a[0], 0);
			}
		});
		
		thePanel.panelProductId2LPool.addPopupItem(menuItemAddRelationProductId2LPool);
		
		
		// special treatment of columns
		col=thePanel.panelProductId2LPool.getColumnModel().getColumn(0); 
		JComboBox comboLP0 = new JComboBox();
		comboLP0.setFont(Globals.defaultFontBig);
		//org.jdesktop.swingx.autocomplete.AutoCompleteDecorator.decorate(combo);
		//combo.setRenderer ();
		//col.setCellEditor(new DefaultCellEditor(combo));
		col.setCellEditor(
			new de.uib.utilities.table.gui.AdaptingCellEditor(
				comboLP0, 
				new de.uib.utilities.ComboBoxModeller(){
					//public ComboBoxModel getComboBoxModel(int row, int column){
					//	return new DefaultComboBoxModel(modelLicencepools.getOrderedColumn(0));
					public ComboBoxModel getComboBoxModel(int row, int column){
						
						Vector poolIds =  mainController.licencePoolTableProvider.getOrderedColumn(//1, 
								mainController.licencePoolTableProvider.getColumnNames().indexOf("licensePoolId"),
								false);
						
						
						//logging.debug(this, "retrieved poolIds: " + poolIds);
						
						if (poolIds.size() <= 1)
							poolIds.add("");
						//hack, since combo box shows nothing otherwise
						
						ComboBoxModel model = new DefaultComboBoxModel(
							poolIds); 
						
						//logging.debug(this, "got comboboxmodel  for poolIds, size " + model.getSize());
						
						return model;
					}
				}
			)
		);
		
		col=thePanel.panelProductId2LPool.getColumnModel().getColumn(1); 
		JComboBox comboLP1 = new JComboBox();
		comboLP1.setFont(Globals.defaultFontBig);
		//org.jdesktop.swingx.autocomplete.AutoCompleteDecorator.decorate(combo);
		//combo.setRenderer ();
		//col.setCellEditor(new DefaultCellEditor(combo));
		col.setCellEditor(
			new de.uib.utilities.table.gui.AdaptingCellEditor(
				comboLP1, 
				new de.uib.utilities.ComboBoxModeller(){
					public ComboBoxModel getComboBoxModel(int row, int column){
						return new DefaultComboBoxModel(new Vector(persist.getProductIds()));
					}
				}
			)
		);
		
		//updates
		thePanel.panelProductId2LPool.setUpdateController(
			new MapItemsUpdateController(
				thePanel.panelProductId2LPool,
				modelProductId2LPool,
				new MapBasedUpdater(){
					public String sendUpdate(Map<String, Object> m){
						//System.out.println(" sendUpdate, " + m.get("productId") + ", " + m.get("licensePoolId"));
						return persist.editRelationProductId2LPool(
							(String) m.get("productId"),
							(String) m.get("licensePoolId")
						);
					}
					public boolean sendDelete(Map<String, Object> m){
						modelProductId2LPool.requestReload();
						return persist.deleteRelationProductId2LPool(
							(String) m.get("productId"), 
							(String) m.get("licensePoolId")
							);
					}
				},
				updateCollection
			)
		);
		
		//--- panelWindowsSoftware
		
		columnNames = new Vector<String>(de.uib.configed.type.SWAuditEntry.getDisplayKeys());
		columnNames.remove("licenseKey");
		
		//logging.info(this, "panelWindowsSoftware columnNames " + columnNames);
		
		classNames = new Vector<String>();
		for (int i = 0; i < columnNames.size(); i++)
		{
			classNames.add("java.lang.String");
		}
		
		modelWindowsSoftwareIds =
			new GenTableModel(
				null,
				new DefaultTableProvider(
					new RetrieverMapSource(columnNames, classNames,
						new MapRetriever(){
							public Map retrieveMap()
							{
								persist.installedSoftwareInformationRequestRefresh();
								return persist.getInstalledSoftwareInformation();
							}
						})
					),
			
				0 /*columnNames.indexOf("ID")*/,  new int[]{},
				(TableModelListener) thePanel.panelWindowsSoftware,  
				updateCollection);
		
		tableModels.add(modelWindowsSoftwareIds);
		tablePanes.add(thePanel.panelWindowsSoftware);
		
		modelWindowsSoftwareIds.reset();		
		thePanel.panelWindowsSoftware.setTableModel(modelWindowsSoftwareIds);
		thePanel.panelWindowsSoftware.setListSelectionMode(	
			ListSelectionModel.MULTIPLE_INTERVAL_SELECTION
		);
		modelWindowsSoftwareIds.setEditableColumns(new int[]{});
		thePanel.panelWindowsSoftware.setEmphasizedColumns(new int[]{});
		
		Integer[] searchCols = new Integer[columnNames.size()];
		for (int j = 0; j < columnNames.size(); j++)
			searchCols[j] = j;
		
		thePanel.panelWindowsSoftware.setSearchColumns(searchCols);
		thePanel.panelWindowsSoftware.setSearchSelectMode(false);
		
		windowsSoftwareIdsFilterCondition = new DefaultTableModelFilterCondition( 0 );
		modelWindowsSoftwareIds.setFilterCondition( windowsSoftwareIdsFilterCondition );
		
		JMenuItemFormatted menuItemSoftwareShowAssigned = new JMenuItemFormatted(configed.getResourceValue("ConfigedMain.Licences.PopupWindowsSoftwareShowAssigned"));
		menuItemSoftwareShowAssigned.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e)
			{
				//save values
				softwareShow = SoftwareShowMode.ASSIGNED;
				
				setSWAssignments(true);
				/*
				boolean b = thePanel.panelWindowsSoftware.isDataChanged();
				thePanel.panelWindowsSoftware.setAwareOfSelectionListener(false);
				List selectedKeys = thePanel.panelWindowsSoftware.getSelectedKeys();
				//System.out.println(" toggle filter  " + selectedKeys);
				
				
				modelWindowsSoftwareIds.setFilter(new TreeSet(selectedKeys));
				modelWindowsSoftwareIds.setUsingFilter(true);
				
				//modelWindowsSoftwareIds.toggleFilter();
				
				thePanel.panelWindowsSoftware.setSelectedValues(selectedKeys, 0);
				thePanel.panelWindowsSoftware.setAwareOfSelectionListener(true);
				thePanel.panelWindowsSoftware.setDataChanged(b);
				*/
			}
		});
		
		/*
		JMenuItemFormatted menuItemSoftwareShowNotAssigned = new JMenuItemFormatted(configed.getResourceValue("ConfigedMain.Licences.PopupWindowsSoftwareShowNotAssigned"));
		menuItemSoftwareShowNotAssigned.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e)
			{
				softwareShow = SoftwareShowMode.NOT_ASSIGNED;
				//save values
				boolean b = thePanel.panelWindowsSoftware.isDataChanged();
				thePanel.panelWindowsSoftware.setAwareOfSelectionListener(false);
				List selectedKeys = thePanel.panelWindowsSoftware.getSelectedKeys();
				thePanel.panelWindowsSoftware.setSelectedValues(selectedKeys, 0);
				
				modelWindowsSoftwareIds.setInvertedFilter(new TreeSet(selectedKeys)); 
				modelWindowsSoftwareIds.setUsingFilter(true);
				
				
				
				//modelWindowsSoftwareIds.toggleFilter();
				
				thePanel.panelWindowsSoftware.setAwareOfSelectionListener(true);
				thePanel.panelWindowsSoftware.setDataChanged(b);
			}
		});
		*/
		
		
		
		JMenuItemFormatted menuItemSoftwareShowAll = new JMenuItemFormatted(configed.getResourceValue("ConfigedMain.Licences.PopupWindowsSoftwareShowAll"));
		menuItemSoftwareShowAll.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e)
			{
				softwareShow = SoftwareShowMode.ALL;
				setSWAssignments(false);
			}
		});
		
	
		thePanel.panelWindowsSoftware.addPopupItem(menuItemSoftwareShowAll);
		thePanel.panelWindowsSoftware.addPopupItem(menuItemSoftwareShowAssigned);
		//thePanel.panelWindowsSoftware.addPopupItem(menuItemSoftwareShowNotAssigned);
	
		
		
		
		// special treatment of columns
		col=thePanel.panelWindowsSoftware.getColumnModel()
			.getColumn(columnNames.indexOf("ID"));
		col.setMaxWidth(40);
		col.setHeaderValue("id ...");
		col=thePanel.panelWindowsSoftware.getColumnModel()
			.getColumn(columnNames.indexOf("architecture"));
		col.setMaxWidth(60);
		col=thePanel.panelWindowsSoftware.getColumnModel()
			.getColumn(columnNames.indexOf("language"));
		col.setMaxWidth(60);
		
		//updates
		thePanel.panelWindowsSoftware.setUpdateController(
			new SelectionMemorizerUpdateController(
				thePanel.panelLicencepools,
				0,
				thePanel.panelWindowsSoftware,
				modelWindowsSoftwareIds,
				new StrList2BooleanFunction(){
					public boolean sendUpdate(String poolId, List softwareIds){
						
						logging.debug(this, "sendUpdate poolId, softwareIds: " + poolId + ", " + softwareIds); 
						
						boolean result = persist.setWindowsSoftwareIds2LPool(
							poolId,
							softwareIds);
						
						
						setWindowsSoftwareIdsFromLicencePool();
						
						//modelWindowsSoftwareIds.setFilter(new TreeSet( softwareIds ));
						return result;
					}
				}
				)
			{
				public boolean cancelChanges()
				{
					setWindowsSoftwareIdsFromLicencePool();
					return true;
				}
			}
					
		);
		
		
		
		//combine
		thePanel.panelLicencepools.getListSelectionModel().addListSelectionListener(
			new ListSelectionListener()
			{
				public void valueChanged(ListSelectionEvent e) 
				{
					//Ignore extra messages.
					if (e.getValueIsAdjusting()) return;
					
					ListSelectionModel lsm =
					(ListSelectionModel)e.getSource();
					
					if (lsm.isSelectionEmpty()) {
						//logging.debug(this, "no rows selected");
					} 
					else
					{
						//int selectedRow = lsm.getMinSelectionIndex();
						String keyValue = thePanel.panelLicencepools.getValueAt(
							thePanel.panelLicencepools.getSelectedRow(), 0).toString();
						
						thePanel.panelProductId2LPool.setSelectedValues(null, 0);//clear selection
						thePanel.panelProductId2LPool.moveToValue(keyValue, 0);	
						
						setWindowsSoftwareIdsFromLicencePool();
						
					}
				}
			}
		);
	}
	
	
	private void setSWAssignments(boolean usingFilter)
	{
		//save values
		boolean b = thePanel.panelWindowsSoftware.isDataChanged();
		thePanel.panelWindowsSoftware.setAwareOfSelectionListener(false);
		List selectedKeys = thePanel.panelWindowsSoftware.getSelectedKeys();
		//System.out.println(" toggle filter  " + selectedKeys);
		
		if (usingFilter)
		{
			windowsSoftwareIdsFilterCondition.setFilter(new TreeSet(selectedKeys));
		}
		modelWindowsSoftwareIds.setUsingFilter( GenTableModel.DEFAULT_FILTER_NAME, usingFilter);
		
		//modelWindowsSoftwareIds.toggleFilter();
		
		thePanel.panelWindowsSoftware.setSelectedValues(selectedKeys, 0);
		
		if (selectedKeys != null && selectedKeys.size() > 0)
			thePanel.panelWindowsSoftware.moveToValue((String) selectedKeys.get(selectedKeys.size() -1), 0, false);
		
		thePanel.panelWindowsSoftware.setAwareOfSelectionListener(true);
		thePanel.panelWindowsSoftware.setDataChanged(b);
	}
	
	public void initializeVisualSettings()
	{
		super.initializeVisualSettings();
		windowsSoftwareIdsFilterCondition.setFilter(null);
		thePanel.panelWindowsSoftware.setDataChanged(false);
		
	}
	
	
	
	
	
}
