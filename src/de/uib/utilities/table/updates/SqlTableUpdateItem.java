/* 
 *
 * 	uib, www.uib.de, 2008
 * 
 *	author Rupert Röder 
 *
 */
 
package de.uib.utilities.table;

import java.util.*;


public class SqlTableUpdateItem extends SqlTableEditItem
{
	String colName;
	Object value;
	Object keyValue;
	String className;
	String keyColumnClassName;
	
	public SqlTableUpdateItem(
			String tableName, Vector<String> columnNames, Vector<String> classNames, 
			Vector<Object> oldValues, Vector<Object> rowV, int keyCol)
	{
		super(tableName, columnNames, classNames, oldValues, rowV, keyCol);
		this.colName = colName;
		this.value = value;
		className = classNames.get(columnNames.indexOf(colName));
		keyColumnClassName = classNames.get(columnNames.indexOf(keyColumnName));
		keyValue = null;
		if (oldValues == null)
			keyValue = rowV.get(keyCol);
		else
			keyValue = oldValues.get(keyCol);
	}
	
	public String getSqlStatement()
	{
		StringBuffer sql = new StringBuffer("update " + tableName + " set " );
		
		boolean firstset = true;
		
		for (int j = 0; j < columnNames.size(); j++)
		{
			if (j != keyCol)
			{
				if (firstset)
					firstset = false;
				else
					sql.append(", ");
				
				sql.append sqlEqual(columnNames.get(j), row.get(j).toString(), classNames.get(j))
			}
		}
		
		sql.append(" where " + sqlEqual(keyColumnName, keyValue.toString(), keyColumnClassName))
				
		return sql.toString();
	}
	
	public String getSqlStatementForCurrentData()
	{
		//System.out.println(" current data lookup : keyColumnName " + keyColumnName + " keyValue " + keyValue);
		String result = 
			"select " + getColumnList()
			+ " from " + tableName  
			+ " where " + sqlEqual(keyColumnName, keyValue.toString(), keyColumnClassName);
		
		//System.out.println (result);	
		return result;
		
	}
	
	
}
