/* 
 *
 * (c) uib, www.uib.de, 2009-2013
 *
 * author Rupert Röder
 */

package de.uib.utilities.datapanel;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.table.*;
import javax.swing.event.*;
import java.util.*;
import de.uib.utilities.*;
import de.uib.utilities.logging.*;
import de.uib.utilities.swing.*;
import de.uib.utilities.table.*;
import de.uib.utilities.table.gui.*;
import de.uib.configed.*;
import de.uib.configed.gui.*;


public class EditMapPanelX extends DefaultEditMapPanel implements FocusListener
// works on a map of pairs of type String - List
{
	//GUI
	JScrollPane jScrollPane;
	JTable table;
	
	JMenuItem popupItemDeleteEntry;
	JMenuItem popupItemAddStringListEntry;
	JMenuItem popupItemAddBooleanListEntry;
	
	ToolTipManager ttm;
	
	
	protected boolean markDeviation = true;
	
	public EditMapPanelX()
	{
		this(null);
	}
	
	public EditMapPanelX( TableCellRenderer tableCellRenderer)
	{
		this (tableCellRenderer, false);
	}
	
	public EditMapPanelX( TableCellRenderer tableCellRenderer, boolean keylistExtendible)
	{
		this (tableCellRenderer, keylistExtendible, true);
	}
	
	public EditMapPanelX( TableCellRenderer tableCellRenderer, boolean keylistExtendible, boolean keylistEditable)
	{
		this (tableCellRenderer, keylistExtendible, keylistEditable, false);
	}
		
	public EditMapPanelX( TableCellRenderer tableCellRenderer, 
		boolean keylistExtendible, 
		boolean keylistEditable,
		boolean reloadable)
	{
		super(tableCellRenderer, keylistExtendible, keylistEditable, reloadable);
		
		
		ttm = ToolTipManager.sharedInstance();
		ttm.setEnabled(true);
		ttm.setInitialDelay(de.uib.utilities.Globals.toolTipInitialDelayMs);
		ttm.setDismissDelay(de.uib.utilities.Globals.toolTipDismissDelayMs);
		ttm.setReshowDelay(de.uib.utilities.Globals.toolTipReshowDelayMs);
		
		
		setLayout(new BorderLayout());
		buildPanel();
		
		initPopup();
		
		table.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		
		//editorfield.addFocusListener(this);
	}
	
	protected void prepareColumns()
	{
		editableColumn = table.getColumnModel().getColumn(1);
		
		//defaultCellEditor = new DefaultCellEditor(new JTextField());
		editorfield = new JComboBox();
		editorfield.setEditable(true);
		defaultCellEditor = new XCellEditor(editorfield);
		theCellEditor = defaultCellEditor;
		
		if (tableCellRenderer == null)
			editableColumn.setCellRenderer(new ColorTableCellRenderer());
		else
			editableColumn.setCellRenderer(tableCellRenderer);
		
		
		if (theCellEditor != null && theCellEditor instanceof SensitiveCellEditor)
			((SensitiveCellEditor) theCellEditor).finish();
			
		theCellEditor = new SensitiveCellEditor (modelProducer);
		((SensitiveCellEditor) theCellEditor).setForbiddenValues(mapTableModel.getShowOnlyValues() );
		editableColumn.setCellEditor(theCellEditor);
	}
	
	@Override 
	protected void initPopup()
	{
		super.initPopup();
		MouseListener popupListener = new utils.PopupMouseListener(popup);
		table.addMouseListener(popupListener);
		
		//((PopupMenuTrait)popup).addPopupListenersTo(new JComponent[]{table});
		
		logging.info(this, "initPopup (keylistExtendible || keylistEditable " + keylistExtendible + " || " + keylistEditable);
		
		if (keylistExtendible || keylistEditable)
		{
			table.getTableHeader().setToolTipText(configed.getResourceValue("EditMapPanel.PopupMenu.EditableToolTip"));
			//popup = new JPopupMenu();
			
			//MouseListener popupListener = new utils.PopupMouseListener(popup);
			//table.addMouseListener(popupListener);
			 
			popupItemAddStringListEntry = new JMenuItem(configed.getResourceValue("EditMapPanel.PopupMenu.AddEntry"));
			popup.add(popupItemAddStringListEntry);
			popupItemAddStringListEntry.addActionListener(
				new ActionListener()
				{
					public void actionPerformed(ActionEvent e){
						
						addEntryFor("java.lang.String");
					}
						
				}
			);
			popupItemAddBooleanListEntry = new JMenuItem(configed.getResourceValue("EditMapPanel.PopupMenu.AddBooleanEntry"));
			popup.add(popupItemAddBooleanListEntry);
			popupItemAddBooleanListEntry.addActionListener(
				new ActionListener()
				{
					public void actionPerformed(ActionEvent e){
						
						addEntryFor("java.lang.Boolean");
					}
						
				}
				
			);
			if (keylistEditable) //saving not implemented in persistencecontroller since new key is globally 
			{
				popupItemDeleteEntry = new JMenuItem(configed.getResourceValue("EditMapPanel.PopupMenu.RemoveEntry"));
				popup.add(popupItemDeleteEntry);
				popupItemDeleteEntry.addActionListener(
					new ActionListener()
					{
						public void actionPerformed(ActionEvent e){
							if (table.getSelectedRowCount() == 0)
							{
								//JOptionPane.showInternalMessageDialog(	table, 
								//configed.getResourceValue("EditMapPanel.RowToRemoveMustBeSelected"));
								
								FTextArea fAsk = 
								new FTextArea(null, 
									de.uib.utilities.Globals.APPNAME, "", true, 1); 
								fAsk.setSize (new Dimension (200,200));
								fAsk.setModal(true);
								fAsk.setMessage (configed.getResourceValue("EditMapPanel.RowToRemoveMustBeSelected"));
								
								fAsk.setVisible(true);
							}
							else
							{
								if (names != null)
								{
									//logging.debug(this, "remove entry " + names.elementAt( table.getSelectedRow() ) );
									removeProperty(names.elementAt( table.getSelectedRow()).toString());
								}
							}
						}
					}
				);
			}
		}
	}
		
	
	protected void buildPanel()
	{
		TableCellRenderer colorized = new ColorTableCellRenderer();
		
		table = new JTable(mapTableModel){
			public Component prepareRenderer(TableCellRenderer renderer, int rowIndex, int vColIndex) 
			{
				Component c = super.prepareRenderer(renderer, rowIndex, vColIndex);
				if (c != null && c instanceof JComponent && showToolTip) {
					JComponent jc = (JComponent)c;
					
					String propertyName = (String) names.get(rowIndex);
					//Logging.debug(this, " ---- we are in prepareRenderer ");
					
					String tooltip = null;
					
					if (  
						propertyName != null  
						&&  defaultsMap != null
						&&  defaultsMap.get(propertyName) != null
						)
					{
						tooltip = "default: " + defaultsMap.get(propertyName) ;
					}
						
					if (  
						propertyName != null 
						&& descriptionsMap != null
						&& descriptionsMap.get(propertyName) != null)
					{
						tooltip = tooltip + "\n\n" +  descriptionsMap.get(propertyName) ;
					}
						
					jc.setToolTipText(  de.uib.utilities.Globals.makeHTMLlines( tooltip ) );
					
					//check equals with default
					logging.info(this, "prepareRenderer rowIndex " + rowIndex);
					Object defaultValue = defaultsMap.get(table.getValueAt(rowIndex,0));
					Object gotValue = table.getValueAt(rowIndex, 1);
				
					/*
					logging.debug(this, "prepareRenderer " 
						+ " defaultsMap.get(table.getValueAt(rowIndex,0)) " + defaultValue
						+ " table.getValueAt(rowIndex, 1) " + gotValue);
					*/
					
					if (defaultValue == null)
					{
						jc.setForeground(Color.red);
						jc.setToolTipText( configed.getResourceValue("EditMapPanel.MissingDefaultValue"));
					}
					else
					{
						if (markDeviation && !defaultValue.equals(gotValue))
						{
							java.awt.Font gotFont  = jc.getFont();
							gotFont = gotFont.deriveFont(Font.BOLD);
							jc.setFont(gotFont);
						}
					}
					
				}
				return c;
			}
		
		};
		
		table.setDefaultRenderer(Object.class,  colorized);
		table.setRowHeight(de.uib.utilities.Globals.lineHeight);
		table.setShowGrid(true); 
		table.setGridColor(Color.white);
		
		
		jScrollPane = new JScrollPane(table);
		//jScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		add (  jScrollPane, BorderLayout.CENTER );
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
		
		prepareColumns();
		
		if (optionsMap != null)
			modelProducer = new  ListModelProducerForVisualDatamap (table, optionsMap, visualdata);
		
		mapTableModel.setModelProducer( 
			(ListModelProducerForVisualDatamap) modelProducer);
	}
		
		
	
	
	
	private boolean checkKey(String s)
	{
		boolean ok = false;
		
		if (s != null && !s.equals(""))
		{
			ok = true;
			
			if (names.indexOf(s) > -1)
			{
				ok = 
				
				( 
					JOptionPane.OK_OPTION == 
					JOptionPane.showConfirmDialog(
					de.uib.configed.Globals.mainContainer,
					"Ein Eintrag mit diesem Namen existiert bereits. Überschreiben des bisherigen Eintrags?", 
					de.uib.utilities.Globals.APPNAME,
					JOptionPane.OK_CANCEL_OPTION)
				);
			}
			
		}
		
		return ok;
	}
		
	
	private void addEntryFor(final String classname)
	{
		String initial = "";
		int row = table.getSelectedRow();
		if (row > -1)
			initial = (String) table.getValueAt(row, 0);
		//logging.debug(this, "adding an entry , starting with " + x);
		
		
		FEditText fed = new FEditText(initial,  configed.getResourceValue("EditMapPanel.KeyToAdd")){
			
			@Override
			protected void commit()
			{
				super.commit();
				String s = getText();
				
				if (checkKey(s))
				{
					setVisible(false);
					if (classname.equals("java.lang.Boolean"))
						addBooleanProperty(s);
					else
						addEmptyProperty(s);
					
				}
				
			}
		};
				
		fed.setModal(true);
		fed.setSingleLine(true);
		fed.select(0, initial.length());
		fed.setTitle(de.uib.utilities.Globals.APPNAME );
		fed.init(new Dimension(300, 50));
		
		if (row > -1)
		{
			Rectangle rect = table.getCellRect(row, 0, true);
			Point tablePoint = table.getLocationOnScreen();
		
			fed.setLocation((int) tablePoint.getX()  + (int) rect.getX() + 50, (int) tablePoint.getY() +  (int) rect.getY() +  de.uib.configed.Globals.lineHeight );
		}
		else
			fed.centerOn(de.uib.configed.Globals.mainContainer);
		
		fed.setVisible(true);
	}
	
	
	public void addEmptyProperty(String key)
	{
		ArrayList val = new ArrayList();
		val.add("");
		addProperty (key, val	);
		optionsMap.put(key, DefaultListCellOptions.getNewEmptyListCellOptions());
		mapTableModel.setMap(mapTableModel.getData());
		((ListModelProducerForVisualDatamap) modelProducer).setData(
			optionsMap, mapTableModel.getData()	);
	}
	
	public void addBooleanProperty(String key)
	{
		ArrayList val = new ArrayList();
		val.add(false);
		addProperty (key, val	);
		optionsMap.put(key, DefaultListCellOptions.getNewBooleanListCellOptions());
		mapTableModel.setMap(mapTableModel.getData());
		((ListModelProducerForVisualDatamap) modelProducer).setData(
			optionsMap, mapTableModel.getData()	);
	}
	
	/** adding an entry to the table model and, finally, to the table
	@param String key
	@param Object value (if null then an empty String is the value)
	*/
	protected void addProperty(String key, Object newval)
	{
		mapTableModel.addEntry (key, newval);
		names = mapTableModel.getKeys();
		mapTableModel.fireTableDataChanged();
	}
	
	/** deleting an entry 
	@param String key  - the key to delete
	@param Object value  - if null then an empty String is the value
	*/
	public void removeProperty(String key)
	{
		mapTableModel.removeEntry (key);
		names = mapTableModel.getKeys();
		mapTableModel.fireTableDataChanged();
	}
	
	
	private Map testData()
	{
		HashMap hm = new HashMap();
		hm.put ("key 1", "value 1");
		hm.put ("key 2", "value 2");
		return hm;
	}
	
	public void stopEditing()
	{
		if (table.isEditing())  // we prefer not to cancel cell editing
		{
			table.getCellEditor().stopCellEditing();
		}
	}
	
	// =================  FocusListener
	public void focusGained(FocusEvent e) 
	{
		// System.out.println("++++++++++++++++ Focus gained  isEditing " + table.isEditing());
	}
	
	public void focusLost(FocusEvent e) 
	{
		//System.out.println("---------------------- Focus lost isEditing " + table.isEditing());
		stopEditing();
	}
	
	

	
		
}
