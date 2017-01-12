package de.uib.utilities.table;
import java.util.*;

public class DefaultTableModelFilterCondition 
	implements TableModelFilterCondition
{
	
	protected TreeSet<Object> filterSet;
	protected int keyCol = -1;
	
	public DefaultTableModelFilterCondition(int keyCol)
	{
		this.keyCol = keyCol;
	}
	
	public void setFilter(TreeSet<Object> filterParam)
	{
		filterSet= filterParam;
	}
	
	public boolean test(Vector<Object> row)
	{
		if (filterSet == null)
		  return true;
		
		if (keyCol == -1)
		  return true;
		
		return filterSet.contains( row.get(keyCol) );
	}
}
