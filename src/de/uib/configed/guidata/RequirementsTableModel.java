package de.uib.configed.guidata;

import de.uib.utilities.*;
import de.uib.opsidatamodel.PersistenceController;
import java.util.*;


public class RequirementsTableModel
			extends javax.swing.table.AbstractTableModel
{

	final String initString = "";
	private String actualProduct = "";
	TreeSet keySet;
	Object[] keyArray;
	final Object[] zeroArray = new Object[]{};

	Map requMap;
	Map requBeforeMap;
	Map requAfterMap;
	Map requDeinstallMap;

	PersistenceController perCon;

	public RequirementsTableModel( PersistenceController persis)
	{
		perCon = persis;
		setActualProduct(""); //initializing
	}
	
	private void retrieveRequirements(String depotId, String product)
	{
		//if depotId == null the depot representative is used
		requMap       = perCon.getProductRequirements(depotId, product);
		requBeforeMap = perCon.getProductPreRequirements(depotId, product);
		requAfterMap  = perCon.getProductPostRequirements(depotId, product);
		requDeinstallMap  = perCon.getProductDeinstallRequirements(depotId, product);
	}		
	
	public void setActualProduct (String product)
	 //we assume that the productId determines the requirements since we are on a preselected depot
	{
		setActualProduct(null, product);
	}
		
	public void setActualProduct (String depotId, String product)
	{
		this.actualProduct = product;

		keySet = null;
		requMap = null;
		requBeforeMap = null;
		requAfterMap = null;
		requDeinstallMap = null;
		keyArray = zeroArray;

		if  ( product != null && !product.trim().equals("")  )
		{
			retrieveRequirements(depotId, product);
			
			keySet = new TreeSet();
			if (requMap != null && requMap.keySet() != null)
			{ keySet.addAll (new TreeSet (requMap.keySet()));}
			if (requBeforeMap != null && requBeforeMap.keySet() != null)
			{keySet.addAll(new TreeSet (requBeforeMap.keySet()));}
			if (requAfterMap != null  && requAfterMap.keySet() != null)
			{  keySet.addAll(new TreeSet (requAfterMap.keySet())); }
			if (requDeinstallMap != null && requDeinstallMap.keySet() != null)
			{  keySet.addAll(new TreeSet (requDeinstallMap.keySet())); }
			if (keySet != null)
			{  keyArray = keySet.toArray(); }
			else
			{ keyArray = zeroArray; }
		}
		
		fireTableDataChanged();

	}

	public int getColumnCount()
	{
		return 5;
	}

	public int getRowCount()
	{
		return keyArray.length;
	}

	public String getColumnName (int col)
	{
		String result = "";
		switch (col)
		{
		case 0 : result = " "; break;
			/*
			case 1 : result = "Benötigt (=on)"; break;
			case 2 : result = "Vorher benötigt"; break;
			case 3 : result = "Danach benötigt"; break;
			case 4 : result = "Bei deinstall"; break;
			*/
		case 1 : result = "required"; break;
		case 2 : result = "pre-required"; break;
		case 3 : result = "post-required"; break;
		case 4 : result = "on deinstall"; break;
		};

		return result;

	}

	public Object getValueAt(int row, int col)
	{
		String myKey = (String) keyArray[row];
		if (actualProduct == null || actualProduct.equals(""))
			return initString;

		Object result = null;

		switch (col)
		{
		case 0 : result = myKey; break;
		case 1 : if (requMap != null)
			{
				result = requMap.get(myKey); break;
			}
		case 2 :  if (requBeforeMap != null)
			{
				result = requBeforeMap.get(myKey); break;
			}
		case 3 : if (requAfterMap != null)
			{
				result = requAfterMap.get(myKey); break;
			}
		case 4 : if (requDeinstallMap != null)
			{
				result = requDeinstallMap.get(myKey); break;
			}
		}

		return result;
	}

}





