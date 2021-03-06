/* 
 *
 * (c) uib, www.uib.de, 2013
 *
 * author Rupert Röder
 */

package de.uib.utilities.datapanel;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.table.*;
import javax.swing.tree.*;
import javax.swing.event.*;

import java.util.*;

import de.uib.utilities.*;
import de.uib.utilities.tree.*;
import de.uib.utilities.logging.*;
import de.uib.utilities.swing.*;
import de.uib.utilities.table.*;
import de.uib.utilities.table.gui.*;
import de.uib.configed.*;
import de.uib.configed.gui.*;
import de.uib.configed.guidata.ListMerger;
import de.uib.utilities.pdf.DocumentToPdf;


public class EditMapPanelGrouped extends DefaultEditMapPanel
	implements TreeSelectionListener


// works on a map of pairs of type String - List
{
	
	protected JSplitPane splitPane;
	final int initialSplitLoc = 350;
	protected XTree tree;
	protected JPanel rightPane; 
	protected JLabel labelForRightPane;
	protected SimpleTreeModel treemodel;
	
	protected TreeMap<String, String> givenClasses;
	protected TreeSet<String> keyclasses;
	protected Map<String, String> tooltips4Keys;
	protected Map<String, AbstractEditMapPanel> partialPanels;
	protected TreeMap<String, Map<String, Object>> virtualLines;
	
	protected DocumentToPdf tableToPDF;
	
	protected int hGap = de.uib.utilities.Globals.hGapSize/2;
	protected int vGap = de.uib.utilities.Globals.vGapSize/2;
	
	
	public  EditMapPanelGrouped()
	{
		this(null);
	}
	
	public  EditMapPanelGrouped( TableCellRenderer tableCellRenderer)
	{
		this(tableCellRenderer, false);
	}
	
	public  EditMapPanelGrouped( TableCellRenderer tableCellRenderer, boolean keylistExtendible)
	{
		this(tableCellRenderer, keylistExtendible, true);
	}
	
	public  EditMapPanelGrouped( TableCellRenderer tableCellRenderer, boolean keylistExtendible, boolean keylistEditable)
	{
		this(tableCellRenderer, keylistExtendible, keylistEditable, false);
	}
	
	
	public  EditMapPanelGrouped( TableCellRenderer tableCellRenderer, 
		boolean keylistExtendible, 
		boolean keylistEditable,
		boolean reloadable)	
	{
		this(tableCellRenderer, keylistExtendible, keylistEditable, reloadable, (TreeMap<String, String>) null);
	}
	
	
	public  EditMapPanelGrouped( TableCellRenderer tableCellRenderer, 
		boolean keylistExtendible, 
		boolean keylistEditable,
		boolean reloadable,
		TreeMap<String, String> classesMap)
	{
		this(tableCellRenderer, keylistExtendible, keylistEditable, reloadable, 
			classesMap, (AbstractEditMapPanel.Actor) null);
	}
	
	public  EditMapPanelGrouped( TableCellRenderer tableCellRenderer, 
		boolean keylistExtendible, 
		boolean keylistEditable,
		boolean reloadable,
		final AbstractEditMapPanel.Actor actor)
	{
		this(tableCellRenderer, keylistExtendible, keylistEditable, reloadable, 
			null, actor);
	}
	
	public  EditMapPanelGrouped( TableCellRenderer tableCellRenderer, 
		boolean keylistExtendible, 
		boolean keylistEditable,
		boolean reloadable,
		TreeMap<String, String> classesMap,
		final AbstractEditMapPanel.Actor actor)
	{
		super(tableCellRenderer, keylistExtendible, keylistEditable, reloadable);
		buildPanel();
		this.actor = actor;
		givenClasses = classesMap;
		
			popup = new PopupMenuTrait(new Integer[]{
										PopupMenuTrait.POPUP_SAVE,
										PopupMenuTrait.POPUP_RELOAD
			})
				{
					public void action(int p)
					{
						logging.info(this, "( EditMapPanelGrouped ) popup " + p);
						
						switch(p)
						{
							case PopupMenuTrait.POPUP_RELOAD:
								reload();
								//actor.reloadData();
								break;
								
							case PopupMenuTrait.POPUP_SAVE:
								actor.saveData();
								break;
						}
						
					}
				}
			;
		}
		//else
		//	popup = new JPopupMenu();
		
		
	
	public void setClasses(TreeMap<String, String> classesMap)
	{
		givenClasses = classesMap;
	}
	
	
	protected void generateParts()
	{
		tooltips4Keys = givenClasses; 
		partialPanels = new HashMap<String, AbstractEditMapPanel>();
		
		for (String key : keyclasses)
		{
			//if (key.startsWith("user"))
			//	continue;
				
			EditMapPanelX editMapPanel = new EditMapPanelX(
					tableCellRenderer,
					keylistExtendible, keylistEditable, reloadable
					)
			{
				protected void reload()
				{
					javax.swing.tree.TreePath p = tree.getSelectionPath();
					int row = tree.getRowForPath(p); 
					
					actor.reloadData();
					logging.info(this, "reloaded, return to " + p);
					if (p != null)
					{
						//logging.info(this, "reloaded, return to " + row);
						tree.setExpandsSelectedPaths(true);
						tree.setSelectionInterval(row, row);
						tree.scrollRowToVisible(row);
					}
				}
					
				//@Override
				protected JPopupMenu definePopup()
				{
					logging.info(this, " (EditMapPanelGrouped) definePopup ");
					JPopupMenu result
					
						= new PopupMenuTrait(new Integer[]{
										PopupMenuTrait.POPUP_SAVE,
										PopupMenuTrait.POPUP_RELOAD,
										//PopupMenuTrait.POPUP_DELETE,
										PopupMenuTrait.POPUP_PDF
							})
								
							{
								public void action(int p)
								{
									switch(p)
									{
										case PopupMenuTrait.POPUP_RELOAD:
											reload();
											break;
										/*
										case PopupMenuTrait.POPUP_DELETE:
											actor.deleteData();
											break;
										*/
										case PopupMenuTrait.POPUP_SAVE:
											actor.saveData();
											break;
										case PopupMenuTrait.POPUP_PDF:
											createPDF();
											break;
									}
									
									
								}
							}
						;
					
					//result.addSeparator();
					/*result.addSeparator();
					JMenuItem popupRemoveClientEntry  = new JMenuItem("remove client entry");
					result.add( popupRemoveClientEntry );
					*/
					return result;
				}
			};
			
			editMapPanel.setCellEditor(SensitiveCellEditorForDataPanel.getInstance(key));
			editMapPanel.setActor(actor);
			
			
			partialPanels.put (key, editMapPanel);
		}
	}
	private void createPDF(){
		String client = "";
		
		
		client = tree.getSelectionPath().getPathComponent(0).toString().trim(); // client name
		// TODO get Depotname
		logging.info(this, "------------- create report");
		HashMap<String, String> metaData = new HashMap<String, String>();
		metaData.put("header", configed.getResourceValue("EditMapPanelGrouped.createPDF.title"));
		metaData.put("title",  configed.getResourceValue("Client: " + client));
		metaData.put("subject", "report of table");
		metaData.put("keywords", configed.getResourceValue("EditMapPanelGrouped.createPDF.title") + " " + client);
		
		tableToPDF = new DocumentToPdf (null, metaData); //  no filename, metadata

		tableToPDF.createContentElement("table", createJTableForPDF());
		
		tableToPDF.setPageSizeA4();  // 
		tableToPDF.toPDF(); //   create Pdf

	}
	
	private JTable createJTableForPDF(){
		DefaultTableModel tableModel = new DefaultTableModel();
		JTable jTable = new JTable( tableModel);
		Vector values;

		tableModel.addColumn(configed.getResourceValue("EditMapPanelGrouped.createJTableForPDF.property_name")); //"Property-Name");
    		tableModel.addColumn(configed.getResourceValue("EditMapPanelGrouped.createJTableForPDF.property_value")); //"Property-Wert");

		Vector<String> keys = mapTableModel.getKeys();
		Iterator it = keys.iterator();
		while(it.hasNext()) {
			String key = it.next().toString();
			String property = "";
			ListMerger listelem = (ListMerger) mapTableModel.getData().get(key);
			if (!listelem.isEmpty())
				property = listelem.getValue().get(0).toString();
			values = new Vector();
			//System.out.println(key + " :: " + property);
			// TODO search another possibility to exclude?
			if (!key.contains("saved_search")) {
				values.add(key);values.add(property);
				tableModel.addRow(values);
			}
		}
		return jTable;
	}

	protected void reload()
	{
		logging.info(this, "reload");
		javax.swing.tree.TreePath p = tree.getSelectionPath();
		int row = tree.getRowForPath(p); 
		
		actor.reloadData();
		logging.debug(this, "reloaded, return to " + p);
		if (p != null)
		{
			//logging.info(this, "reloaded, return to " + row);
			tree.setExpandsSelectedPaths(true);
			tree.setSelectionInterval(row, row);
			tree.scrollRowToVisible(row);
		}
	}
	
		
	
	@Override
	protected void buildPanel()
	{
		splitPane = new JSplitPane();
		
		splitPane.setBackground(Color.red);
		setBackground(de.uib.utilities.Globals.backNimbus);
		
		
		tree = new XTree();
		//tree.setToolTipText("help");
		ToolTipManager.sharedInstance().registerComponent(tree);
		
		//tree.putClientProperty("JTree.lineStyle", "Horizontal");
		tree.setCellRenderer(new SimpleIconNodeRenderer());
		tree.expandAll();
		//tree.setRootVisible(false);
		
		
		tree.addTreeSelectionListener(this);
		
		JScrollPane jScrollPaneTree = new JScrollPane(tree);
		jScrollPaneTree.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		//jScrollPaneTree.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		
		rightPane  = new JPanel();
		labelForRightPane = new JLabel("");
		rightPane.add(labelForRightPane);
		
		splitPane.setLeftComponent(jScrollPaneTree);
		splitPane.setRightComponent(rightPane);
		splitPane.setDividerLocation(initialSplitLoc);
		
		//splitPane.add(jScrollPaneTree);
		
		
			
		GroupLayout layout = new GroupLayout(this);
		setLayout(layout);
		
		layout.setHorizontalGroup(layout.createSequentialGroup()
			.addGap(hGap, hGap, hGap)
			.addComponent(splitPane, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
			.addGap(hGap, hGap, hGap)
		);
       		layout.setVerticalGroup(layout.createSequentialGroup()
			.addGap(vGap, vGap, vGap)
			.addComponent(splitPane, 50, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
			.addGap(vGap, vGap, vGap)
		);

	}
	
	
	protected void classify(Map<String, Object> data, TreeSet<String> classIds)
	{
		
		TreeMap<String, Object> mydata = new TreeMap<String, Object>(data);
		
		virtualLines = new TreeMap<String, Map<String, Object>>();
			
		
		for (String id : classIds.descendingSet())
		{
			virtualLines.put(id, new TreeMap<String, Object>());
		}
		
		virtualLines.put("", new TreeMap<String, Object>());
			
		String noValue = "NONE";
		
		if (data == null)
			return;
		
	
		//Iterator<String> iterCollect = classIds.descendingSet().iterator();
		NavigableSet<String> classIdsDescending = classIds.descendingSet();
		
		
		for ( String key : new TreeSet<String>(data.keySet()).descendingSet() )
		{
			logging.debug(this, "classify key ------- " + key);
			boolean foundClass = false;
			for (String idCollect : classIdsDescending)
			{
				if (key.startsWith(idCollect))
					{
						virtualLines.get(idCollect).put(key, data.get(key));
						logging.debug(this, "classify idCollect -------- " + idCollect);
						foundClass = true;
						break;
					}
			}
			
			if (!foundClass)
			{
				virtualLines.get("").put(key, data.get(key));
			}
			
			
			/*
			String partialKey = "";
			String remainder = key;
			
			int j = -1;
			int k = remainder.indexOf('.');
			while (k > j && !foundClass)
			{
				//componentKey = key.substring(j+1, k);
				partialKey = key.substring(0, k);
				remainder = key.substring(k+1);
				
				logging.debug(this, "classify partial " + partialKey);
				
				
				//logging.debug(this, "classify remainder " + remainder);
				j = k;
				k = j  + 1 + remainder.indexOf('.');
			}
			*/
			
		}
		
		//logging.info(this, "classify virtualLines " + virtualLines);
		
	}
	
	
	/** setting all data for displaying and editing 
	<br />
	@param  Map visualdata - the source for the table model 
	@param  Map optionsMap - the description for producing cell editors 
	*/
	@Override
	public void setEditableMap(Map<String, Object> visualdata, Map<String, ListCellOptions> optionsMap)
	{
		super.setEditableMap(visualdata, optionsMap);
		logging.debug(this, " setEditableMap, visualdata keys " + visualdata);
		if (visualdata != null)
		{
			logging.debug(this, " setEditableMap, visualdata keys " + visualdata.keySet());
			
			treemodel = new SimpleTreeModel(givenClasses.keySet(), tooltips4Keys);
			//treemodel.produce();
			tree.setModel(treemodel);
			tree.expandAll();
			
			logging.info(this, "generated keys " + treemodel.getGeneratedKeys());
			keyclasses = treemodel.getGeneratedKeys();
			
			generateParts();
			
			classify(visualdata, keyclasses);
			
			for (String key : keyclasses)
			{
				//if (key.startsWith("user"))
				//	continue;
				
				partialPanels.get(key).setEditableMap(
					virtualLines.get(key), optionsMap
					);
				
				partialPanels.get(key).mapTableModel.setObservers(
					this.mapTableModel.getObservers()
					);
				
			}
		
		}
		
	}
	
	//  apply method of superclass for all partial maps
	@Override
	public void setOptionsEditable( boolean b )
	{
		super.setOptionsEditable(b);
		
		
		for (String key : keyclasses)
		{
			//if (key.startsWith("user"))
			//	continue;
			partialPanels.get(key).setOptionsEditable(b);
		}
	}
	
	//  apply method of superclass for all partial maps 
	@Override
	public void setStoreData(Collection data)
	{
		super.setStoreData(data);
		//meaning mapTableModel.setStoreData(data);
		
		for (String key : keyclasses)
		{
			//if (key.startsWith("user"))
			//	continue;
			partialPanels.get(key).setStoreData(data);
		}
	}

	//  apply method of superclass for all partial maps
	@Override
	public void setUpdateCollection (Collection updateCollection)
	{
		super.setUpdateCollection( updateCollection );
		//meaning  mapTableModel.setUpdateCollection( updateCollection );
		
		for (String key : keyclasses)
		{
			//if (key.startsWith("user"))
			//	continue;
			partialPanels.get(key).setUpdateCollection(updateCollection);
		}
	}
	
	public void setPropertyHandlerType(EditMapPanelX.PropertyHandlerType t)
	{
		for (String key : keyclasses)
		{
			((EditMapPanelX)partialPanels.get(key)).setPropertyHandlerType(t);
		}
	}
	
	public void setLabel(String s)
	{
		if (treemodel == null)
			return;
		
		treemodel.setRootLabel(s);
	}
	
	protected String getCurrentKey()
	{
		javax.swing.tree.TreePath p = tree.getSelectionPath();
		if (p == null)
		{
			return null;
		}
		
		boolean isRoot = (p.getPathCount() == 1);
		
		if (isRoot)
			return null;

		return SimpleTreePath.dottedString(1, p); 
	}
	
	//TreeSelectionListener
	public void valueChanged(TreeSelectionEvent e) 
	{
		javax.swing.tree.TreePath p = tree.getSelectionPath();
		//logging.info(this, "valueChanged TreeSelectionEvent, path " + p);
		
		int divLoc = splitPane.getDividerLocation();
		
		if (p == null)
		{
			splitPane.setRightComponent(rightPane);
			splitPane.setDividerLocation(divLoc);
			return;
		}
		
		boolean isRoot = (p.getPathCount() == 1);
		
		if (isRoot)
			splitPane.setRightComponent(rightPane);
		
		else
		{
		
			String key = SimpleTreePath.dottedString(1, p);  //we start at 1 since we eliminate the root node
			//logging.info(this, "valueChanged TreeSelectionEvent, key "  + key);
			
			if (partialPanels.get(key) == null)
				splitPane.setRightComponent(rightPane);
			
			else
			{
				splitPane.setRightComponent(partialPanels.get(key));
			}
		}
		
		splitPane.setDividerLocation(divLoc);
		
		//labelForRightPane.setText(key);
	}
	

	
}
