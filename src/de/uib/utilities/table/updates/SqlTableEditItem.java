/* 
 *
 * 	uib, www.uib.de, 2008
 * 
 *	author Rupert Röder 
 *
 */
 
package de.uib.utilities.table.updates;

import java.util.*;

public class SqlTableEditItem extends TableEditItem
{
	Vector<String> columnNames;
	Vector<String> classNames;
	Vector<Object> oldValues;
	Vector<Object> row;
	String keyColumnName;
	String keyColumnClassName;
	int keyCol;
	String tableName;
	String colsList; 
	
	public SqlTableEditItem(
		String tableName, Vector<String> columnNames, Vector<String> classNames, 
		Vector<Object> oldValues, Vector<Object> row, int keyCol)
	{
		this.columnNames = columnNames;
		this.classNames = classNames;
		this.oldValues = oldValues;
		this.keyColumnName = columnNames.get(keyCol);
		this.keyColumnClassName = classNames.get(keyCol);
		this.keyCol = keyCol;
		this.tableName = tableName;
		
		StringBuffer cols = new StringBuffer(" ");
		for (int j=0; j < columnNames.size(); j++)
		{
			if (j>0)
				cols.append(", ");
			
			cols.append(columnNames.get(j));
		}
		cols.append(" ");
		colsList = cols.toString();
	}
	
	protected String sqlEqual(String colName, String value, String className)
	{
		String result = " " + colName + " = ";
		
		if (className.equals("java.lang.String"))
		{
			result = result + "'" + value + "' ";
		}
		else
		{
			result = result + value + " ";
		}
		
		return result;
	}
	
	public String getTableName()
	{
		return tableName;
	}
	
	public Vector<String> getColumns()
	{
		return columnNames;
	}
	
	public String getColumnList()
	{
		return colsList;
	}
	
	public Vector<String> getOldValues()
	{
		return oldValues;
	}
	
	public abstract String getSqlStatement();
}
