/* 
 *
 * 	uib, www.uib.de, 2008
 * 
 *	author Rupert Röder 
 *
 */
 
package de.uib.utilities.table;


public class TableDeleteItem extends TableEditItem
{
	protected String keyClassName;
	
	public TableDeleteItem(String tableName, String keyColumnName, Object keyValue, String keyClassName)
	{
		super(tableName, null, null, null, keyColumnName, keyValue);
		this.keyClassName = keyClassName;
	}
	
	public String getSqlStatement()
	{
		String result = 
			"delete from " + tableName 
			+ " where '" + sqlEqual(keyColumnName, keyValue.toString(), keyClassName);
			
		return result;
		
	}
}
