/*
 * JComboRenderedViaIndex
 * uses CellRendererByIndex
 *
 */

package de.uib.utilities.swing.observededit;

import javax.swing.*;
import java.util.*;

import de.uib.utilities.*;
import de.uib.utilities.swing.*;

import de.uib.utilities.observer.*;

public class JComboRenderedViaIndex<K> extends JComboBoxObserved
	implements Observer
{

	TreeSet<String> values;
	Map<K, String> indexedValues;
	Set<K> keysAllowed;
	Map<Character, K> charIndex;
	
	public JComboRenderedViaIndex()
	{
		super();
	}
	
	public JComboRenderedViaIndex(ObservableSubject editingNotifier)
	{
		super(editingNotifier);
		
		setKeySelectionManager(new MyKeySelectionManager(this, getModel()));
		
		
		/* test
		keysAllowed = new HashSet<K>();
		keysAllowed.add( (K) new Integer(1));
		keysAllowed.add( (K) new Integer(2));
		*/
	}
	
	
	
	protected class MyKeySelectionManager implements JComboBox.KeySelectionManager
	{
		
		ComboBoxModel model;
		JComboRenderedViaIndex<K> combo;
		
		public MyKeySelectionManager(JComboRenderedViaIndex<K> combo,  ComboBoxModel model)
		{
			this.model = model;
			this.combo = combo;
		}
		
		public int selectionForKey(char aKey, ComboBoxModel aModel)
		{
			//logging.debug(this, "selectionForKey " + aKey);
			combo.setFirstCharOccurence(aKey);
			return combo.getSelectedIndex();
		}
	}
	
	
	public void setFirstCharOccurence(Character c)
	{
		setSelectedItem(charIndex.get(c));
	}
	
	

	public void setOrdered(TreeSet<String> ordered)
	{
		values = ordered;
	}
	
	public void restrictKeys(java.util.Set keysAllowed)
	{
		this.keysAllowed = keysAllowed;
	}
	
	public void setMapping(Mapping<K, String> m)
	{
		Object saveSelectedItem = getSelectedItem();
		
		//logging.debug(this, "setMapping " + m);
		
		if (m == null)
			return;
		
		indexedValues = m.getMap();
		
		charIndex = new HashMap<Character, K>();
		
		values = new TreeSet<String>(Globals.getCollator()); //sort !
		values.addAll(m.getRange());
		
		ArrayList<K> keys = new ArrayList<K>();
		//add keys in the sequence of the values
		Iterator<String> iter = values.iterator();
		while (iter.hasNext())
		{
			String val = iter.next();
			K key = m.getInverseMap().get(val); 
			//logging.debug(this, " set mapping val >>" + val + "<<");
			
			if (val != null && val.length() > 0)
			{
				Character ch = val.toLowerCase().charAt(0);
				if (charIndex.get(ch) == null) //first occurence
					charIndex.put(ch, key);
			}
			
			/*
			logging.debug(this, " key, keysAllowed, containing " + key + ", " + keysAllowed + ", " 
				+ keysAllowed.contains(key));
			*/
			
			if ( key == null )
				logging.error(this, "value " + val + ": mapping does not fit to values");
			else
			{
				if (keysAllowed == null || keysAllowed.size() == 0 || keysAllowed.contains(key))
					keys.add(key);
			}
		}
		
		//logging.debug(this, "keys " + keys);
		setModel( new DefaultComboBoxModel( keys.toArray(new Integer[] {}) )  );
		//setModel( new DefaultComboBoxModel( new ArrayList(m.getDomain()).toArray() )  ) ;
		
		//if (m.getMap().get("") != null) m.getMap().put("", " ");
		// if the mapping delivers an empty string we have to correct it in order to get the combo working for it
		setRenderer(new CellRendererByIndex(m.getMap(), null, false));
		
		//logging.debug(this, "setRenderer by index, map " + m.getMap());
		//setRenderer(new DefaultListCellRenderer());
		
		if (
			((DefaultComboBoxModel)getModel()).
				getIndexOf(saveSelectedItem) 
			== -1
			)
			setSelectedItem(null);
		else
			setSelectedItem(saveSelectedItem);
	}
	
	//set tooltip
	@Override
	public void setSelectedItem(Object ob)
	{
		//logging.debug(this, "setSelectedItem " + ob);
		String showVal = "";
		if (ob == null)
			showVal = "";
		else
		{
			try
			{
				K key = (K) ob;
				showVal = indexedValues.get(key);
			}
			catch(Exception ex)
			{
				logging.info(this, "selectedItem " + ob + " ex: " + ex);
				showVal = "";
			}
		}
			
		setToolTipText(showVal);
		super.setSelectedItem(ob);
	}
	
	//interface observer
	public void update(Observable o, Object arg)
	{
		//logging.debug(this, "observable tells " + arg);
		setMapping((Mapping) arg);
	}
}