/* 
 *
 * 	uib, www.uib.de, 2008-2009
 * 
 *	author Rupert Röder 
 *
 */
 
package de.uib.utilities.table.updates;

public class SqlTableUpdateItemFactory
{
	protected Vector<String> columnNames;
	protected Vector<String> classNames;
	protected int keyCol;
	
	protected String keyColumnName;
	
	public SqlTableUpdateItemFactory(
			String tableName,
			Vector<String> columnNames, Vector<String> classNames,
			int keyCol)
	{
		this.columnNames = columnNames;
		this.classNames = classNames;
		this.keyCol = keyCol;
		keyColumnName = columnNames.get(keyCol);
	}
	
	public TableEditItem produceUpdateItem(Vector oldValues, Vector rowV)
	{
		return new SqlTableUpdateItem( 
				tableName, columnNames, classNames, 
				oldValues, rowV, keyCol) 
	}
	
	public TableEditItem produceInsertItem(Vector rowV)
	{
		return new SqlTableInsertItem(tableName, columnNames, classNames,
			rowV, keyCol);
	}
	
	public TableEditItem produceDeleteItem()// not yet implemented
	{
		return null;	
	}
	
}
	
