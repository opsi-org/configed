/* 
 *
 * (c) uib, www.uib.de, 2009-2013
 *
 * author Rupert Röder
 */


package de.uib.utilities.datapanel;

import javax.swing.*;
import java.util.*;

import de.uib.utilities.*;
import de.uib.utilities.logging.*;

import de.uib.utilities.table.*;
import de.uib.utilities.swing.*;

public abstract class AbstractEditMapPanel extends JPanel
{
	protected MapTableModel mapTableModel;
	
	protected boolean reloadable = false;
	protected boolean showToolTip = true;
	
	protected boolean keylistExtendible = false; 
	protected boolean keylistEditable = true;
	
	protected boolean optionsEditable = true;
	
	protected Actor actor;
		
	protected JPopupMenu popup;
	protected JPopupMenu popupEditOptions;
	protected JPopupMenu popupNoEditOptions;
	
	public static class Actor{
		protected void reloadData()
		{
			logging.debug(this, "reloadData");
		}
		protected void saveData()
		{
			logging.debug(this, "saveData");
		}
		protected void deleteData()
		{
			logging.info(this, "deleteData");
		}
	}
	
	public AbstractEditMapPanel()
	{
		actor = new Actor();
		mapTableModel = new MapTableModel();
		//mapTableModel.setData(null);
	}
	
	public AbstractEditMapPanel(boolean keylistExtendible, 
		boolean keylistEditable,
		boolean reloadable)
	{
		this();
		this.keylistExtendible = keylistExtendible;
		this.keylistEditable = keylistEditable;
		this.reloadable = reloadable;
		//mapTableModel.setData(null);
	}

	public void setActor(Actor actor)
	{
		this.actor = actor;
	}
		
	
	protected abstract void buildPanel();
	
	public abstract void resetDefaults();
	
	public abstract void setVoid();
	
	public MapTableModel getTableModel()
	{
		return mapTableModel;
	}
	
	public Vector<String> getNames()
	{
		return mapTableModel.getKeys();
	}
	
	public void setShowToolTip(boolean b)
	{
		showToolTip = b;
	}
	
	public void registerDataChangedObserver( DataChangedObserver o )
	{
		mapTableModel.registerDataChangedObserver(o);
	}
	
	/**  set collection (e.g. of clients) where each member stores the changed data; we assume that it is a collection of maps
	@param  Collection data
	*/
	public void setStoreData(Collection data)
	{
		mapTableModel.setStoreData(data);
	}

	/** take a reference to a collection of maps that we will have to use for updating the data base
	@param  Collection updateCollection
	*/
	public void setUpdateCollection (Collection updateCollection)
	{
		mapTableModel.setUpdateCollection( updateCollection );
	}
	
		
	public void setReadOnlyEntries(java.util.Set<String> keys)
	{
		mapTableModel.setReadOnlyEntries(keys);
	}
	
	public void setShowOnlyValues(java.util.List<Object> showOnly)
	{
		mapTableModel.setShowOnlyValues( showOnly );
	}
	
	protected void logPopupElements(JPopupMenu popup)
	{
		MenuElement[] popupElements = popup.getSubElements();
		int size = popupElements.length;
		logging.info(this, "logPopupElements " +size);
		for (int i = 0; i < size;  i++)
		{
			logging.info(this, "logPopupElements " +   ((JMenuItem) popupElements[i]).getText());
		}
	}
	
	/*
	private void removeOldPopupElements()
	{
		MenuElement[] popupElements = popup.getSubElements();
		int size = popupElements.length;
		logging.info(this, "removeOldPopupElements " +size);
		for (int i = size-1; i >=0;  i--)
		{
			logging.info(this, "removeOldPopupElements " +   ((JMenuItem) popupElements[i]).getText());
			popup.remove(i);
		}
	}
	
	public void setPopupConfiguration(LinkedList<JMenuItem>menuItems) 
	{
		logPopupElements();
		//removePopupElements();
		
		
		logging.info(this, "setPopupConfiguration, set menuitems " +menuItems.size() );
		for (JMenuItem item : menuItems)
		{
			logging.info(this, "setPopupConfiguration, set " +  item.getText());
			//popup.add( item );
		}
		
	}
	
	public void setPopupConfiguration(JPopupMenu menu) 
	{
		logPopupElements();
		//removePopupElements();
		
		
		MenuElement[] menuElements = menu.getSubElements();
		logging.info(this, "setPopupConfiguration, set menu " +menuElements.length); 
		for (int i = 0; i < menuElements.length; i++)
		{
			logging.info(this, "setPopupConfiguration, set menu " +  ((JMenuItem) menuElements[i]).getText());
			//popup.add( (JMenuItem) menuElements[i] );
		}
		
	}
	*/
		
	public void setOptionsEditable( boolean b )
	{
		logging.info(this, "AbstractEditMapPanel.setOptionsEditable " + b);
		
		if (b)
		{
			popup = popupEditOptions;
		}
		else
		{
			popup = popupNoEditOptions;
		}
	}
	
	/** setting all data for displaying and editing 
	<br />
	@param  Map visualdata - the source for the table model 
	@param  Map optionsMap - the description for producing cell editors 
	*/
	abstract public void setEditableMap(Map<String, Object> visualdata, Map<String, ListCellOptions> optionsMap);
	
	
	/** setting a label 
	<br />
	@param String s  - label text
	*/
	abstract public void setLabel(String s);
	
	
	abstract public void init();
}
	
	

	
