/* 
 *
 * 	uib, www.uib.de, 2009-2015
 * 
 *	author Rupert Röder 
 *
 */
 
package de.uib.utilities.table.provider;

import java.util.*;
import java.sql.*;
import de.uib.utilities.logging.*;

public class MapSource implements TableSource
// based on a regular map (rows indexed by a String key)
// of maps (representing the rows as pairs columnname - value)
// "regular" means that all rows have identical structure (missing 
// columns may represent null values)
// column 0 of the table is the key of the outer map (therefore the first classname
// has to be String)
{
	protected Vector<String> columnNames;
	
	protected Vector<String> classNames;
	
	protected Map<String, Map> table;
	
	protected Vector<Vector<Object>> rows;
	
	protected boolean reloadRequested = true;
	
	public MapSource(Vector<String> columnNames, Vector<String> classNames, Map<String, Map> table)
	{
		this.columnNames = columnNames;
		this.classNames = classNames;
		
		this.table = table;
		rows = new Vector();
		
	}
	
	private static boolean dynInstanceOf (Object ob, Class cl)
	{
		return cl.isAssignableFrom( ob.getClass() );	
	}
	
	protected void fetchData()
	{
		rows.clear();
		//logging.debug(this, "MapSource fetchData() : " + table);
		
		//logging.info(this, "fetchData , columns " + columnNames);
		
		Iterator iter = table.keySet().iterator();
		while (iter.hasNext())
		{
			String key = (String) iter.next();
			Vector vRow = new Vector();
			
			Map mRow = table.get(key);
			
			//System.out.println ( " -------- key '" + key + "', mRow = " + mRow );
			
			
			//vRow.add(key);
			//previously we assumed that column 0 hold the key
			
			for (int i = 0; i < columnNames.size(); i++)
			{
				Object ob = mRow.get(columnNames.get(i));
				vRow.add (ob);
				
				//logging.debug(this, " getting ob to column " + i + ", " + columnNames.get(i) + " ob:" + ob);
				
				if (ob == null)
				{
					if (mRow.containsKey(columnNames.get(i)))
						logging.debug(this,  "fetchData row " + mRow + " no value in column  " +  columnNames.get(i));
					else
					{
						logging.warning(this, "fetchData row " + mRow + " ob == null, possibly the column name is not correct, column " 
							+ i + ", " + columnNames.get(i));
					}
				}
				else
				{
					try{
						//System.out.println( "??? is " + ob + " dyninstance class of " + classNames.get(i));
						Class cl = Class.forName( classNames.get(i) );
						if (! dynInstanceOf(ob, cl ) )
						{
							//Class.forName( classNames.get(i) ) ).isAssignableFrom ( ob.getClass() ) )
							// e.g. java.lang.String valueInColumnI = ob; works!
							logging.warning(this, "MapSource fetchData(): data type does not fit");
							logging.info(this, " ob "  + ob + " class " + ob.getClass().getName());
							logging.info(this, "class should be " + cl);
						}
							
							
							
					}catch(java.lang.NullPointerException ex){
						logging.warning(this, " " + ex + ", could not get dyninstance " 
								+ i + ", " + columnNames.get(i));
					}catch(Exception ex){
						logging.error("MapSource fetchData(): class " + classNames.get(i) + " not found, "  + ex);
					}
				}
			}
			
			rows.add(vRow);
			
			
		}	
	}
	
	public Vector<String> retrieveColumnNames()
	{
		return columnNames;
	}
	
	public Vector<String> retrieveClassNames()
	{
		return classNames;
	}
	
	public Vector<Vector<Object>> retrieveRows()
	{
		if (reloadRequested)
		{
			fetchData();
			reloadRequested = false;
		}
		//System.out.println (" --- MapSource retrieveRows() rows.size(): " + rows.size());
		return rows;
	}
	
	public void requestReload()
	{
		reloadRequested = true;
	}
	
	@Override
	public String getRowCounterName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isRowCounting() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setRowCounting(boolean b) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void structureChanged() {
		// TODO Auto-generated method stub
		
	}
	
	
	
}
	
