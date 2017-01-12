package de.uib.utilities.swing.observededit;

import de.uib.utilities.logging;
import de.uib.utilities.observer.*;

import javax.swing.*;

public class JComboBoxObserved extends JComboBox
{
	
	protected DataEditListener editListener;
	
	public JComboBoxObserved(ObservableSubject editingNotifier)
	{
		super();
		setMaximumRowCount(30);
		if (editingNotifier != null)
		{
			editListener = new DataEditListener(editingNotifier, this);
			addItemListener(editListener);
		}
		
		//setKeySelectionManager(new MyKeySelectionManager(getModel()));
			
	}
	
	public JComboBoxObserved()
	{
		this(null);
	}
	
	public void setWithFocusCheck(boolean b)
	{
		editListener.setWithFocusCheck(b);
	}

	public void setSelectedItemObserved(Object ob)
	{
		logging.debug(this, "setSelectedItemObserved " + ob);
		boolean saveWithFocusCheck = editListener.isWithFocusCheck();
		editListener.setWithFocusCheck(false);
		super.setSelectedItem(ob);
		editListener.setWithFocusCheck(saveWithFocusCheck);
		
	}
	
	
	protected class MyKeySelectionManager implements JComboBox.KeySelectionManager
	{
		
		ComboBoxModel model;
		
		public MyKeySelectionManager(ComboBoxModel model)
		{
			this.model = model;
		}
		
		public int selectionForKey(char aKey, ComboBoxModel aModel)
		{
			//test
			return 2;
		}
	}
		
}
