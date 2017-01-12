package de.uib.utilities.swing.observededit;

import de.uib.utilities.logging;
import de.uib.utilities.observer.*;

import javax.swing.*;

public class JCheckBoxObserved extends JCheckBox
{
	
	protected DataEditListener editListener;
	
	
	public JCheckBoxObserved(String info, boolean selected)
	{
		super(info, selected);
	}
	
	public JCheckBoxObserved(String info, boolean selected, ObservableSubject editingNotifier)
	{
		this(info, selected);
		if (editingNotifier != null)
		{
			editListener = new DataEditListener(editingNotifier, this);
			addItemListener(editListener);
		}
	}
	
	public void setWithFocusCheck(boolean b)
	{
		editListener.setWithFocusCheck(b);
	}

	public void setSelectedObserved(boolean b)
	{
		logging.debug(this, "setSelectedObserved " + b);
		boolean saveWithFocusCheck = editListener.isWithFocusCheck();
		editListener.setWithFocusCheck(false);
		super.setSelected(b);
		editListener.setWithFocusCheck(saveWithFocusCheck);
		
	}
	
}
