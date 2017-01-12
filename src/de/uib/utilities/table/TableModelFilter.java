/* 
 *
 * 	uib, www.uib.de, 2012
 * 
 *	author Rupert Röder
 *
 */
 

package de.uib.utilities.table;
import java.util.*;

public class TableModelFilter
{
	TableModelFilterCondition condition;
	boolean inverted = false;
	boolean inUse = true;
	
	public TableModelFilter(
			TableModelFilterCondition condition,
			boolean inverted,
			boolean used)
	{
		this.condition = condition;
		this.inverted = inverted;
		this.inUse = inUse;
	}
	
	public TableModelFilter(TableModelFilterCondition condition)
	{
		this(condition, false, true);
	}
	
	public TableModelFilter()
	{
		this(null, false, false);
	}
	
	public void setCondition(TableModelFilterCondition condition)
	{
		this.condition = condition;
	}
	
	public TableModelFilterCondition getCondition()
	{
		return condition;
	}
	
	public boolean isInUse()
	{
		return inUse;
	}
	
	public void setInUse(boolean b)
	{
		inUse = b;
	}
	
	public void setInverted(boolean b)
	{
		inverted = b;
	}
	
	
	public boolean test(Vector<Object> row)
	{
		if (!inUse || condition == null)
			return true;
		
		boolean testresult = condition.test(row);
		
		if (inverted)
		{
			return !testresult;
		}
		else
		{
			return testresult;
		}
	}
			
	@Override
	public String toString()
	{
		return "in use " + inUse + ", inverted " + inverted + " condition " + condition;
	}
	
}
			
			