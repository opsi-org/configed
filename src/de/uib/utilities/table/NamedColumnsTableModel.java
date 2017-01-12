package de.uib.utilities.table;
/*
*
* (c) uib, www.uib.de, 2008
 * Author: Rupert Röder
 *
 */
 
import de.uib.utilities.*;
import javax.swing.table.*;
import java.util.*;


public class NamedColumnsTableModel extends DefaultTableModel
{
	protected int rowLength;
	protected int colLength;
	protected java.util.Vector<String> columnNames;
	protected java.util.Vector<Object[]> rows;
	protected boolean[] columnIsEditable;
	
	
	
	public NamedColumnsTableModel()
	{
		this(null);
	}
	
	public NamedColumnsTableModel(java.util.Vector<String> columnNames)
	{
		super();
		rows = new java.util.Vector<Object[]>();
		setColumnNames(columnNames);
		columnIsEditable = new boolean[colLength];
		for (int i = 0; i < this.columnNames.size(); i++)
		{
			columnIsEditable[i] = false;
		}
			
	}
		
	public void setColumnNames(java.util.Vector<String> columnNames)
	{
		if (columnNames == null)
			this.columnNames = new java.util.Vector<String>();
		else
			this.columnNames = columnNames;
		colLength = this.columnNames.size();
	}
	
	
	public String getColumnName(int col) 
	{
        return columnNames.get(col);
    }
	
    public int getColumnCount() { return colLength; }
    
    public int getRowCount() { return rowLength;  }
    
    public Object getValueAt(int row, int col) {
		
		return ((Object[])(rows.get(row))) [col];
	}
	
	public boolean isCellEditable( int row, int col )
	{
		if (columnIsEditable[col])
			return true;
		
		return false;
	}
	
	public void setColumnEditable( String columnname, boolean b )
	{
		columnIsEditable[ getColumnIndex (columnname) ] = b;
	}
	
	
 	
	public void setValueAt(Object value, int row, int col) 
	{
		( (Object[])(rows.get(row) ) )[col] = value;
			
    	fireTableCellUpdated(row, col);
    }
	
	
	public void setValueAt(Object value, int row, String columnname)
	{
		int col = getColumnIndex(columnname);
		if (col == -1)
			logging.debugOut(logging.LEVEL_ERROR, "column  " + columnname + " existiert nicht");
		else
			setValueAt(value, row, col);
	}
	
	public Object getValueAt(int row, String columnname)
	{
		//System.out.println (" getValueAt " + row + ", '" + columnname + "'");
		int col = getColumnIndex(columnname);
		if (col == -1)
		{
			logging.debugOut(logging.LEVEL_ERROR, "column  " + columnname + " existiert nicht");
			return null;
		}
		else
		{
			return getValueAt(row, col);
			//Object result = getValueAt(row, col);
			//System.out.println( " result " + result);
			//return result;
		}
	}
	
   
    public void addRow()
	{
		rows.add(new Object[colLength]);
		rowLength++;
		fireTableRowsInserted(rowLength, rowLength);
    }
	
	public void addMapAsRow(Map<String, Object> m)
	{
		addRow();
		
		for (int i = 0; i < columnNames.size(); i++)
		{
			String key = (String) columnNames.get(i);
			setValueAt(m.get(key), rowLength-1, key);
		}
		
		/*
		Iterator iter = m.keySet().iterator();
		
		while (iter.hasNext())
		{
			String key = (String) iter.next();
			setValueAt(m.get(key), rowLength-1, key);
		}
		*/
	}
	
	public void addRows(Vector<Map> rows)
	{
		if ( rows != null)
		{
			for (int i = 0; i < rows.size(); i++) 
			{
				addMapAsRow(rows.get(i));
			}
		}
	}
	
    public void deleteRow(int rowNum)
    {
		//System.out.println (" remove key col " + keyColumnNo );
		
		if (rows.get(rowNum) == null)
		{
			System.out.println (" row null ");
		}
			
		rows.remove(rowNum);
		rowLength--;
    	fireTableRowsDeleted(rowNum,rowNum);
		
    	//System.out.println("Lösche Zeile " + (rowNum));
    }
	
	public void clear()
	{
		rows.removeAllElements();
		for (int i=0; i< rowLength; i++)
		{
			//System.out.println ( " lösche row " + i );
			fireTableRowsDeleted(i,i);
		}
		
		// should be equivalent, seems not to be
		// fireTableRowsDeleted(0, rowLength-1);
		rowLength = 0;
	}
	
	
	
	public int getColumnIndex(String columnName)
	{
		return columnNames.indexOf (columnName);
	}

}
	
