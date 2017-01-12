/* 
 *
 * 	uib, www.uib.de, 2009-2014
 * 
 *	author Rupert Röder 
 *
 */
 
package de.uib.utilities.table.provider;

import java.util.*;
import de.uib.utilities.logging.*;


public class ReadOnlyTableProvider
	implements TableProvider
{
	protected TableSource source;
	protected Vector<String> columnNames;
	protected Vector<String> classNames;
	protected Vector<Vector> rows;
	
	public ReadOnlyTableProvider(TableSource source)
	{
		this.source = source;
	}
	
	public void setTableSource(TableSource source)
	{
		this.source = source;
	}
	
	public Vector<String> getColumnNames()
	{
		if (columnNames == null)
			columnNames = source.retrieveColumnNames();
			
		return columnNames;
	}
	
	public Vector<String> getClassNames()
	{
		if (classNames == null)
			classNames = source.retrieveClassNames();
		
		return classNames;
	}
	
	public Vector<Vector<Object>> getRows()
	{
		source.getRows();
	}
	
	// should initiate returning to the original data
	public void requestReturnToOriginal()
	{
		source.requestReload();
	}	
	
	// should initiate reloading the original data
	public void requestReloadRows()
	{
		source.requestReload();
	}
	
	//yields a column as ordered vector
	public Vector<String> getOrderedColumn(int col, boolean empty_allowed)
	{
		//logging.debug(this, "getOrderedColumn " + col + ", empty_allowed " + empty_allowed); 
		
		TreeSet<String> set = new TreeSet<String>();
		
		Vector<Vector> rows = getRows();
		for (int row = 0; row < rows.size(); row ++)
		{
			String val = (String) rows.get(row).get(col);
			//logging.debug(this, "getOrderedColumn(" + col + ")  row  " + row + ": " +val );
			if (empty_allowed || val != null  && !val.equals(""))
			{
				//logging.debug(this, "getOrderedColumn, added " +val );
				set.add((String) rows.get(row).get(col));
			}
		}
				
		Vector<String> result = new Vector<String>(set);
		
		//logging.debug(this, "getOrderedColumn, result " +result);
		
		return result;
	}
	
}
