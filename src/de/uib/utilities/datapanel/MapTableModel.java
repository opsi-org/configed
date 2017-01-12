/*
 * (c) uib, www.uib.de, 2009-2013
 *
 * author Rupert Röder
 */


package de.uib.utilities.datapanel;

import javax.swing.*;
import java.util.*;

import de.uib.utilities.*;
import de.uib.utilities.logging.*;
import java.text.Collator;
import de.uib.configed.*;

import de.uib.utilities.table.*;

public class MapTableModel extends javax.swing.table.AbstractTableModel
	implements DataChangedSubject
{
	
	protected Vector<DataChangedObserver> observers;
	
	protected Collection updateCollection;
	protected Collection<Map<String, Object>> storeData;
	protected boolean datachanged;
	
	private java.util.List<Object> showOnlyValues; // values set cannot be set for any key
	private java.util.Set<String> keysOfReadOnlyEntries; //keys which identify readonly entries
	
	
	
	public final static java.util.List nullLIST = new java.util.ArrayList();
	static {nullLIST.add(null);}
	
	protected Map<String, ListCellOptions> optionsMap;
	
	private SortedMap<String, Object> data; //shall be sorted
	private Map<String, Object> oridata; //we keep the original data for writing back changed values
	private Vector<String> keys;
	
	private ListModelProducerForVisualDatamap modelProducer;
	
	private boolean writeData = true;
	
	public MapTableModel() 
	{
		observers = new Vector();
	}
	
	public void setModelProducer(ListModelProducerForVisualDatamap modelProducer)
	{
		//logging.info(this, "setModelProducer " + modelProducer.getClass() + " value at 0,0 " + modelProducer.getClass(0,0));
		this.modelProducer = modelProducer;
	}
	
	public void setOptions( Map<String, ListCellOptions> optionsMap )
	{
		this.optionsMap = optionsMap;
	}
	
	
	public void setMap (Map<String, Object> data)
	{
		//if (data != null) Logging.debug(this, "###  has class " + data.getClass());
		this.data = null;
		if (data != null)
		{
			Collator myCollator = Collator.getInstance();
			myCollator.setStrength(Collator.PRIMARY);
			this.data =Collections.synchronizedSortedMap (new TreeMap (myCollator));
			this.data.putAll (data);
			keys = new Vector<String>(this.data.keySet());
			//logging.info(this, " -------  data keys " + keys);
		}
		oridata = data;
	}
	
	public Map<String, Object> getData()
	{
		return data;
	}
	
	@Override
	public String toString()
	{
		return getClass().getName() + ": " + data;
	}
	
	
	private void setNew ()
	{
		datachanged = false; //starting with a new set of data  
	}
	
	
	public Vector<String> getKeys()
	{
		return keys;
	}
	
	
	/**  set collection (e.g. of clients) where each member stores the changed data; we assume that it is a collection of maps
	@param  Collection data
	*/
	public void setStoreData(Collection<Map<String, Object>>  data)
	{
		if (data == null)
			logging.info(this, "setStoreData null ");
		else
			logging.info(this, "setStoreData size " + data.size());
		setNew();
		storeData = data;
	}
	
	public Collection<Map<String, Object>> getStoreData()
	{
		return storeData;
	}

	/** take a reference to a collection of maps that we will have to use for updating the data base
	@param  Collection updateCollection
	*/
	public void setUpdateCollection (Collection updateCollection)
	{
		this.updateCollection = updateCollection;
	}
	
	public Collection getUpdateCollection()
	{
		return updateCollection;
	}
	
	
	public void setReadOnlyEntries(java.util.Set<String> keys)
	{
		keysOfReadOnlyEntries = keys;
	}
	
	public void setShowOnlyValues(java.util.List<Object> showOnly)
	{
		showOnlyValues = showOnly;
	}
	
	public java.util.List<Object> getShowOnlyValues()
	{
		return showOnlyValues;
	}
	
	
	public void addEntry(String key, Object newval)
	{
		data.put (key, newval);
		oridata.put (key, newval);
		logging.debug(this, " keys " + keys);
		keys = new Vector(data.keySet());
		logging.debug(this, " new keys  " + keys);
		putEntryIntoStoredMaps(key,newval);
		fireTableDataChanged();
	}
	
	public void addEntry(String key)
	{
		ArrayList<Object> newval = new ArrayList();
		data.put (key, newval);
		oridata.put (key, newval);
		
		//Logging.debug(this, " keys " + keys);
		keys = new Vector<String>(data.keySet());
		//logging.debug(this, " new keys  " + keys);
		putEntryIntoStoredMaps(key,newval);
		fireTableDataChanged();
	}
	
	public void removeEntry(String key)
	{
		data.remove (key);
		oridata.remove (key);
		//logging.debug(this, "removeEntry, keys " + keys);
		keys = new Vector(data.keySet());
		//logging.debug(this, "removeEntry, new keys  " + keys);
		removeEntryFromStoredMaps(key);
		fireTableDataChanged();
		
	}
	
	/*
	public void setDataChanged (boolean b)
	{
		dataChanged = b;
	}
	
	public boolean isDataChanged ()
	{
		return dataChanged;
	}
	*/
	
	// table model
	public int getColumnCount()
	{
		return 2;
	}
	
	public int getRowCount()
	{
		int result = 0;
		if (data != null)
			result =  keys.size();
		return result;
	}
		
	public String getColumnName (int col)
	{
		String result = "";
		switch (col)
		{
			case 0 : result = configed.getResourceValue("EditMapPanel.ColumnHeaderName"); break;
			case 1 : result = configed.getResourceValue("EditMapPanel.ColumnHeaderValue"); break;
		};
		
		return result;
		
	}
	
	
	public Object getValueAt(int row, int col)
	{
		if (data == null)  return "";
		
		Object result = null;
		//logging.info(this, "getValueAt based on keys " + keys);
		//logging.info(this, "getValueAt:  " + row + ", " + col);
		//logging.info(this, "getValueAt storeData " + storeData);
		
		switch(col)
		{
			case 0 :  
				try
				{
					result = keys.get(row);  
				}
				catch(Exception ex)
				{
					logging.info(this, "keys " + keys + " row " + row);
					result = "";
				}
				break;
			case 1 :  
				//if ( (keys.get(row) ).indexOf("password") > -1)
				//	result = "***";
				//else
				result = data.get (keys.get(row)) ; 
				break; 
				//data.get (keys.get(row)).toString();  break;
		}
		return result;
	}

	/*
	* JTable uses this method to determine the default renderer/
	* editor for each cell.
	*/
	public Class getColumnClass(int c) {
		switch (c)
		{
			case 0: return "".getClass(); 
			case 1: return java.util.List.class;
		}
		return Object.class;
			//return getValueAt(0, c).getClass();
	}

	/*
	* We implement this method since the table is partially
	* editable.
	*/
	public boolean isCellEditable(int row, int col) {
		//The data/cell address is constant,
		//no matter where the cell appears onscreen.
		
		if (data == null) return false;
		
		if (col < 1) 
		{
			return false;
		} 
		else 
		{
			if (keysOfReadOnlyEntries != null
				&& keysOfReadOnlyEntries.contains(keys.get(row))
				)
				return false;
			else
				return true;
		}
	}
	
	void weHaveChangedStoredMaps()
	{
		if (!datachanged
			|| updateCollection.size() == 0 // updateCollection has been emptied since last change
		)
		{
			datachanged = true;
			// tell it to all registered DataChangedObservers
			notifyChange(); 
			//logging.debug(this, "weHaveChangedStoredMap, storeData: " + storeData);
			// we add the reference to the changed backend data only once to the updateCollection
			
			if (updateCollection == null)
				logging.debug(this, "updateCollection null - should not be");
			else
				updateCollection.addAll(storeData);
			
			//logging.debug(this, " ---  updateCollection: " + updateCollection + "  has size " + updateCollection.size());
		}
	}
		
	void removeEntryFromStoredMaps(String myKey)
	{
		if (storeData != null)
		{
			for (Map<String, Object> aStoreMap : storeData)
			{
					//( (Map) aStoreMap ).remove(myKey);
				aStoreMap .put(myKey, nullLIST);
			}
		
			//logging.debug(this, " ---  datachanged : " + datachanged );
			//logging.debug(this, " ---  updateCollection: " + updateCollection + "  has size " + updateCollection.size());
			weHaveChangedStoredMaps();
		}
	}
	
	//we put a new entry into each map in the given collection
	void putEntryIntoStoredMaps(String myKey, Object value)
	{
		logging.debug(this, "putEntryIntoStoredMaps myKey, value: " + myKey + ", " + value );
		//logging.debug(this, "putEntryIntoStoredMaps storeData " + storeData);
		if (storeData != null)
		{
			Iterator it = storeData.iterator();
			while (it.hasNext())
			{
				Object aStoreMap = it.next();
				
				if (!(aStoreMap instanceof Map))
				{
					if (aStoreMap == null)
					{
						logging.info(this, "EditMapPanel.setValueAt: we have some data null ");
					}
					else
					{
						logging.error(this, "EditMapPanel.setValueAt: backendData " + aStoreMap + " is not null and not a Map ");
					}
				}
				else
				{
					( (Map) aStoreMap ).put(myKey,value);
				}
			}
		
			//logging.debug(this, " ---  datachanged : " + datachanged );
			//logging.debug(this, " ---  updateCollection: " + updateCollection + "  has size " + updateCollection.size());
			
			weHaveChangedStoredMaps();
		}
	}
	
	public void setWrite()
	{
		writeData = true;
	}
	
	public void unsetWrite()
	{
		writeData = false;
	}
	
	
	/*
	* We need to implement this method since the table's
	* data can change.
	*/
	public void setValueAt(Object value, int row, int col) 
	{
		if (value == null)
		{
			logging.debug(this,"call set value in table at " + row + "," + col + " to null");
				return;
		}
		
		logging.debug(this,"Setting value in table at " + row + "," + col
											+ " to " + value
											+ " (an instance of "
											+ value.getClass() + ")"
											);
		//data[row][col] = value;   //this is the trivial version
		// actualPropName = (String) value; 
		
		//logging.debug(this, " oldValue " + getValueAt (row, col) + " class , " + getValueAt (row, col).getClass() );   
		if  ( getValueAt (row, col).equals ( value ) ||  getValueAt (row, col).toString().equals ( value.toString() ) )  
		{
			//logging.debug(this, " ------------ nothing changed, nothing to do ");
		}
		else
		{
			// logging.debug(this, " ------------ something changed ");
			if (col == 1)
			// check not necessary since, by virtue of the method isCellEditable (int,int),
			// we can only have come to here in this case
			{
				if (keys == null) //perhaps everything has changed to null in the meantime
				{
					//logging.debug(this, "----------  keys has vanished");
				}
				else
				{
					String myKey = (String) keys.get(row);
					//StringvaluedObject o = new StringvaluedObject ( "" + value );
					Object o = value;
					//Logging.debug(this,  " data.get(myKey) has type " + data.get(myKey).getClass().getName()
					// + " and value " +   data.get(myKey)); 
					//logging.debug(this, "the new o is " + value);
					
					// the internal view data:
					data.put (myKey,  o);
					//the external view data 
					oridata.put (myKey,  o); 
					logging.debug(this, "put into oridata for myKey o " + myKey + ": " + o); 
					//the data sources:
					
					modelProducer.updateData(oridata);
					
					if (writeData)
					{
						logging.debug(this, " -------  storeData " + value + " (class : " + value.getClass());
						putEntryIntoStoredMaps(myKey, value);
					}
				}
			}
		}
		fireTableCellUpdated(row, col);
	}
	
	/** writing a new value into the row with a certain key
	
		errors occur if the key is not among the given property names, or if a list of allowed values is given and 
		the value is not among them
	<br />
	@param  String key 
	@param  Object value	
	*/
	public void setValue(String key, Object value)
	{
		//logging.debug(this, "setValue key, value " + key + ", " + value);
		
		int row = keys.indexOf(key);
		
		if (row < 0)
		{
			logging.error("key not valid: " + key);
			return;
		}
		
		if (optionsMap.get(key) != null
			&& 
			(optionsMap.get(key)) instanceof java.util.List
			)
		{
			java.util.List valuelist = (java.util.List) optionsMap.get(key);
			
			if (
				valuelist.size() > 0  
				&&
				valuelist.indexOf(value) == -1
				)
			{
				//System.out.println("optionsMap.get(key) " + optionsMap.get(key));
				logging.error("EditMapPanel: value not allowed: " + value);
				return;
			}
		}
		
		setValueAt (value, row, 1);
	}
	
	
		
	// implementation of DataChangedSubject
	public void registerDataChangedObserver( DataChangedObserver o )
	{
		observers.addElement(o);
	}
	
	
	//for transport between a class family
	Vector<DataChangedObserver> getObservers()
	{
		return observers;
	}
	
	void setObservers(Vector<DataChangedObserver> observers)
	{
		this.observers = observers;
	}
	
	public void notifyChange()
	{
		//logging.debug(this, " --  we notify our observers ");
		logging.debug(this, "notifyChange, notify observers " + observers.size());
		for (int i=0; i < observers.size(); i++)
		{
			(observers.elementAt(i)).dataHaveChanged(this);
		}
		
	}
}




	
