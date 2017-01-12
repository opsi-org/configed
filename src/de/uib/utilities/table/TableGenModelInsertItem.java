/* 
 *
 * 	uib, www.uib.de, 2008-2009
 * 
 *	author Rupert Röder 
 *
 */
 
package de.uib.utilities.table;

import java.util.*;
import javax.swing.table.*;

public class TableGenModelInsertItem extends TableInsertItem 
{
	Vector<String> columnNames;
	Vector<String> classNames;
	Vector dataRow;
	int keyCol;
	TableModel model;
	int rowIndexInAddedRows;
	
	public TableGenModelInsertItem(
		TableModel model, 
		String tableName, 
		Vector<String> columnNames, Vector<String> classNames, Vector dataRow,
		int keyCol
		)
	{
		super(tableName, columnNames, classNames, dataRow, keyCol);
		this.model = model;
	}
	
	public TableModel getModel()
	{
		return model;
	}
	
}
