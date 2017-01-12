/* 
 *
 * (c) uib, www.uib.de, 2009-2016
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
import de.uib.utilities.tree.*;
import de.uib.utilities.logging.*;
import de.uib.utilities.swing.*;
import de.uib.utilities.table.*;
import de.uib.utilities.table.gui.*;
import de.uib.configed.*;
import de.uib.configed.gui.*;


public class EditMapPanelX extends DefaultEditMapPanel  
	implements FocusListener
// works on a map of pairs of type String - List
{
	//GUI
	JScrollPane jScrollPane;
	JTable table;
	
	TableColumn editableColumn;
	TableCellEditor theCellEditor;
	JComboBox editorfield;
	TableCellEditor defaultCellEditor;
	
	
	ListModelProducer modelProducer;
	
	
	MouseListener popupEditOptionsListener;
	MouseListener popupNoEditOptionsListener;
	
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
		//logging.info(this, "EditMapPanelX " +  keylistExtendible + ",  " +  keylistEditable + ",  " + reloadable);
		ttm = ToolTipManager.sharedInstance();
		ttm.setEnabled(true);
		ttm.setInitialDelay(de.uib.utilities.Globals.toolTipInitialDelayMs);
		ttm.setDismissDelay(de.uib.utilities.Globals.toolTipDismissDelayMs);
		ttm.setReshowDelay(de.uib.utilities.Globals.toolTipReshowDelayMs);
		
		
		buildPanel();
		table.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		table.setRowHeight(de.uib.utilities.Globals.tableRowHeight);
		
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
		
		//editorfield.addFocusListener(this);
		
		popupEditOptions = definePopup();
		popupNoEditOptions = definePopup();
		popup = popupEditOptions; 

		
		popupEditOptionsListener = new utils.PopupMouseListener(popupEditOptions);
		table.addMouseListener(popupEditOptionsListener);
		jScrollPane.getViewport().addMouseListener(popupEditOptionsListener);
			
		popupNoEditOptionsListener = new utils.PopupMouseListener(popupNoEditOptions);
		table.addMouseListener(popupNoEditOptionsListener);
		jScrollPane.getViewport().addMouseListener(popupNoEditOptionsListener);
		
		//((PopupMenuTrait)popup).addPopupListenersTo(new JComponent[]{table});
		
		logging.info(this, "keylistExtendible || keylistEditable " +  keylistExtendible + ", " + keylistEditable);
			 
		if (keylistExtendible || keylistEditable)
		{
			popupEditOptions.addSeparator();
			
			table.getTableHeader().setToolTipText(configed.getResourceValue("EditMapPanel.PopupMenu.EditableToolTip"));
			//popup = new JPopupMenu();
			
			//MouseListener popupListener = new utils.PopupMouseListener(popup);
			//table.addMouseListener(popupListener);
			 
			popupItemAddStringListEntry = new JMenuItem(configed.getResourceValue("EditMapPanel.PopupMenu.AddEntrySingleSelection"));
			popupEditOptions.add(popupItemAddStringListEntry);
			popupItemAddStringListEntry.addActionListener(
				new ActionListener()
				{
					public void actionPerformed(ActionEvent e){
						
						addEntryFor("java.lang.String", false);
					}
						
				}
			);
			
			popupItemAddStringListEntry = new JMenuItem(configed.getResourceValue("EditMapPanel.PopupMenu.AddEntryMultiSelection"));
			popupEditOptions.add(popupItemAddStringListEntry);
			popupItemAddStringListEntry.addActionListener(
				new ActionListener()
				{
					public void actionPerformed(ActionEvent e){
						
						addEntryFor("java.lang.String", true);
					}
						
				}
			);
			
			popupItemAddBooleanListEntry = new JMenuItem(configed.getResourceValue("EditMapPanel.PopupMenu.AddBooleanEntry"));
			popupEditOptions.add(popupItemAddBooleanListEntry);
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
				popupEditOptions.add(popupItemDeleteEntry);
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
	
	
	protected JPopupMenu definePopup()
	{
		JPopupMenu result = new JPopupMenu();
		
		if (reloadable)
		{
			result = new PopupMenuTrait(new Integer[]{PopupMenuTrait.POPUP_RELOAD})
				{
					public void action(int p)
					{
						switch(p)
						{
							case PopupMenuTrait.POPUP_RELOAD:
								actor.reloadData();
								break;
						}
					}
				}
			;
		}
		
		return result;
	}
		

	@Override
	protected void buildPanel()
	{
		setLayout(new BorderLayout());
		
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
						tooltip = "default: ";
						//logging.info(this, "propertyName" + propertyName );
						//logging.info(this, "defaultsMap" + defaultsMap );
						//logging.info(this, "defaultsMap.get .." + defaultsMap.get(propertyName) );
						if ( de.uib.configed.Globals.isKeyForSecretValue( propertyName ) )
							tooltip = tooltip + de.uib.configed.Globals.STARRED_STRING;
						else 
							tooltip = tooltip + defaultsMap.get(propertyName) ;
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
					//logging.info(this, "prepareRenderer rowIndex " + rowIndex);
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
						
						java.awt.Font gotFont  = jc.getFont();
						gotFont = gotFont.deriveFont(Font.BOLD);
						jc.setFont(gotFont);
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
					
					if (
						vColIndex == 1 
						&& 
						de.uib.configed.Globals.isKeyForSecretValue(
						(String) mapTableModel.getValueAt(rowIndex, 0)
						)
					)
					{
						if (jc instanceof JLabel)
						{
							((JLabel) jc).setText( de.uib.configed.Globals.STARRED_STRING );
						}
						else if (jc instanceof javax.swing.text.JTextComponent)
						{
							((javax.swing.text.JTextComponent) jc).setText( de.uib.configed.Globals.STARRED_STRING );
						}
						else
							CellAlternatingColorizer.colorizeSecret(jc);
							
					}
						
				}
				return c;
			}
		
		};
		
		table.setDefaultRenderer(Object.class,  colorized);
		table.setRowHeight(de.uib.utilities.Globals.lineHeight);
		table.setShowGrid(true); 
		table.setGridColor(Color.white);
		//table.setBackground(de.uib.utilities.Globals.backNimbus);
		
		
		table.addMouseWheelListener(new MouseWheelListener(){
				public void mouseWheelMoved( MouseWheelEvent e )
				{
					//logging.debug(this, "MouseWheelEvent " + e);
					
					int selRow = -1;
					
					if (table.getSelectedRows() == null || table.getSelectedRows().length == 0)
					{
						selRow = -1;
					}
					
					else
						selRow = table.getSelectedRows()[0];
					
					
					//logging.debug(this, "MouseWheelEvent  sel Row " + selRow);
					
					int diff =  e.getWheelRotation();
					
					selRow = selRow + diff;
					//logging.debug(this, "MouseWheelEvent  sel Row " + selRow);
					
					if (selRow >= table.getRowCount())
						selRow = table.getRowCount() -1;
					
					int startRow = 0;
					
					if (selRow < startRow)
						selRow = startRow;
					
					setSelectedRow(selRow);
					
				}
			}
		);
	
		
		jScrollPane = new JScrollPane(table);
		jScrollPane.getViewport().setBackground(de.uib.utilities.Globals.backLightBlue);
		//jScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		add (  jScrollPane, BorderLayout.CENTER );
	}
	
	@Override
	public void init()
	{
		setEditableMap(null, null);
	}
		
	/** setting all data for displaying and editing 
	<br />
	@param  Map visualdata - the source for the table model 
	@param  Map optionsMap - the description for producing cell editors 
	*/


	public void setCellEditor(SensitiveCellEditor cellEditor)
	{
		theCellEditor = cellEditor;
	}

	@Override
	public void setEditableMap(Map<String, Object> visualdata, Map<String, ListCellOptions> optionsMap)
	{
		super.setEditableMap(visualdata, optionsMap);
		
		cancelOldCellEditing();
		
		//logging.debug(this, "construct cell editor");
		
		
		//theCellEditor = SensitiveCellEditor.getInstance();
		
		
		if (optionsMap != null)
			modelProducer = new  ListModelProducerForVisualDatamap (table, optionsMap, visualdata);
		
		logging.debug(this, "setEditableMap set modelProducer  == null " + (modelProducer == null));
		if (modelProducer != null)
		{
			logging.debug(this, "setEditableMap test modelProducer " + modelProducer.getClass());
			logging.debug(this, "setEditableMap test modelProducer " + modelProducer.getClass(0,0));
		}
			
		mapTableModel.setModelProducer( 
			(ListModelProducerForVisualDatamap) modelProducer);
		
		if (theCellEditor instanceof SensitiveCellEditor)
		{
			
			((SensitiveCellEditor) theCellEditor).setModelProducer(modelProducer);
			
			((SensitiveCellEditor) theCellEditor).setForbiddenValues(mapTableModel.getShowOnlyValues() );
		
			((SensitiveCellEditor) theCellEditor).re_init();
		}
			
			
		editableColumn.setCellEditor(theCellEditor);
		
		
		//setNew();
		//mapTableModel.fireTableDataChanged();
		
		
		if (optionsMap != null)
		{
			modelProducer = new  ListModelProducerForVisualDatamap (table, optionsMap, visualdata);
		
			logging.debug(this, "setEditableMap set new modelProducer " + modelProducer.getClass());
			logging.debug(this, "setEditableMap test modelProducer " + modelProducer.getClass(0,0));
		}
		
		
		mapTableModel.setModelProducer( 
			(ListModelProducerForVisualDatamap) modelProducer);
		
		((SensitiveCellEditor) theCellEditor).setModelProducer(modelProducer);
		((SensitiveCellEditor) theCellEditor).setForbiddenValues(mapTableModel.getShowOnlyValues() );
		editableColumn.setCellEditor(theCellEditor);
		
		
	}
	
	public void cancelOldCellEditing()
	{
		
		if (theCellEditor != null ) // && data != null)
		{
			theCellEditor.cancelCellEditing(); //don't shift the old editing state to a new product
			//theCellEditor.stopCellEditing(); //here we get null value errors since the state "hangs"
			if (theCellEditor instanceof SensitiveCellEditor)
				((SensitiveCellEditor) theCellEditor).hideListEditor();	
			//((DefaultCellEditor) theCellEditor).getComponent().setVisible(false);
		}
		
		
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
		addEntryFor(classname, false);
	}
	
	private void addEntryFor(final String classname, final boolean multiselection)
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
					{
						if (multiselection)
							addEmptyPropertyMultiSelection(s);
						else
							addEmptyProperty(s);
					}
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
	
	public void addEmptyPropertyMultiSelection(String key)
	{
		ArrayList val = new ArrayList();
		val.add("");
		addProperty (key, val	);
		optionsMap.put(key, DefaultListCellOptions.getNewEmptyListCellOptionsMultiSelection());
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
	final protected void addProperty(String key, Object newval)
	{
		mapTableModel.addEntry (key, newval);
		names = mapTableModel.getKeys();
		mapTableModel.fireTableDataChanged();
	}
	
	/** deleting an entry 
	@param String key  - the key to delete
	@param Object value  - if null then an empty String is the value
	*/
	final public void removeProperty(String key)
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
	
	protected void setSelectedRow(int row)
	{
		table.setRowSelectionInterval(row, row); 
		//System.out.println(" --- view row selected " + row);
		showSelectedRow();
	}
	
	protected void showSelectedRow()
	{
		int row = table.getSelectedRow();
		if (row != -1)
			table.scrollRectToVisible(table.getCellRect(row, 0, false));
	}		
	
	
	
		
	@Override
	public void setOptionsEditable( boolean b )
	{
		if (b)
		{
			table.removeMouseListener(popupNoEditOptionsListener);
			table.addMouseListener(popupEditOptionsListener);
			jScrollPane.getViewport().addMouseListener(popupEditOptionsListener);
		}
		else
		{
			table.removeMouseListener(popupEditOptionsListener);
			table.addMouseListener(popupNoEditOptionsListener);
			jScrollPane.getViewport().addMouseListener(popupNoEditOptionsListener);
		}
	}
			
}
