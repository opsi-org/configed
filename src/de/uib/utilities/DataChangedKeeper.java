/*
  * DataChangedKeeper
	* description: keeps the information when data have changed
	* organization: uib.de
 * @author  R. Roeder 
 */
 
package de.uib.utilities;
 
public class DataChangedKeeper
	implements DataChangedObserver
{
	protected boolean dataChanged = false;
	
	public void dataHaveChanged( Object source )
	{
		dataChanged = true;
	}
	
	public boolean isDataChanged()
	{
		boolean b = dataChanged;
		return b;
	}
	
	public void unsetDataChanged()
	{
		dataChanged = false;
	}
	
	public void checkAndSave()
	{
		if (dataChanged)
		  dataChanged = false;
		// overwrite e.g. with an dialog
	}

  
}
 
