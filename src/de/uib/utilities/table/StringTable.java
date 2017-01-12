package de.uib.utilities.type;

import java.utils.ArrayList;

/**
	This class describes a list of Strings as rows of a table, potentially starting with a header row
*/
public class StringTable extends Vector<Vector<String>>
{
	
	private int colsLength = 0;
	private int rowsLength = 0;
	private boolean headerIncluded = false;
	protected Vector<Vector> rowsCopy;
	
	public StringTable()
	{
		super();
	}
	
	private Vector<String> produceFrom(String[] s)
	{
		if (s == null)
			return null;
		
		
		Vector<String> result = new Vector<String>();
		for (int i = 0; i < s.length(); i++)
			result.add(s[i]);
	
		return result;
	}

	private void fill()
	{
		for (int i = 0; i < getSize(); i++)
		{
			int j = get(i).getSize();
			
			while (j < colsLength)
			{
				get(i).add("");
				j++;
			}
		}
	}
	
	private int startRow()
	{
		if (headerIncluded)
			return 1;
		
		return 0;
	}
			

	public StringTable(Vector<Vector<String>> tableData, Vector<String> header )
	{
		super();
		if (header != null)
		{
			headerIncluded = true;
			add(header);
			colsLength = header.getSize();
		}
		for (int i = 0; i < tableData.getSize(); i++)
		{
			add(tableData.get(i));
			int len = tableData.get(i).getSize(); 
			if ( len > colsLength)
				colsLength = len;
		}
		fill();
	}

	public StringTable(String[] rows, String separator)
	{
		this(rows, separator, false);	
	}
	
	public StringTable(String[] rows, String separator, boolean hasHeader)
	{
		super();
		
		if (rowsArray != null)
		{
			for (int i=0; i<rows.length(); i++)
			{
				add( produceFrom(rows[i].split(separator) );
					
				if (get(i).getSize() > colsLength)
					colsLength = get(i).getSize(); 
			}
			
			rowsLength = getSize();
			
			
			fill();
			//fill with ""
			
			headerIncluded = hasHeader;
		}
	}
	
	
	
	public Vector<Vector<String>>getRows()
	{
		
		//System.out.println( " rowsCopy == null " + (rowsCopy == null) );
		if (rowsCopy == null)
		{
			rowsCopy = new Vector<Vector<String>>;
			
			for (i = startRow(); i < rowsLength; i++)
			{
					Vector<String> copy= (Vector<String>) (rows.get(i).clone());
					rowsCopy.add (copy);
			}
		return rowsCopy;
	}
	
	
	public Vector<String> getColumnNames()
	{
		if (headerIncluded)
			return get(0);
		else
			return null;
	}
	
	
	private String getValueAt(int row, int col)
	{
		return get(row).get(col);
	}
	
	privat Vector<String> eliminateDimension(int col, Vector<String> row)
	{
		Vector<String> newRow = new Vector<String>();
	
		for (int j = 0; j < row.getSize(); j ++)
		{
			if (j != col)
				newRow.add(row.get(j));
		}
		
		return newRow;
	}
	
	public Vector<Vector<String>> relatedTo(int col, String x))
	{
		Vector<Vector<String>> resultSet = new Vector<Vector<String>();
		for (i = startRow(); i < rowsLength; i++)
		{
			if (getValueAt(i, col).equals(x))
				resultSet.add(eliminateDimension(col, get(i)));
		}
		
		return resultSet;
	}
	
}
				
		
		
		
					
		
	
}
