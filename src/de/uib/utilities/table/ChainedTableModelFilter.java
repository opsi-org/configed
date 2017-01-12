/* 
 *
 * 	uib, www.uib.de, 2012
 * 
 *	author Rupert Röder
 *
 */
 

package de.uib.utilities.table;
import java.util.*;

public class ChainedTableModelFilter extends TableModelFilter
{
	LinkedHashMap<String, TableModelFilter> chain;
	
	public ChainedTableModelFilter()
	{
		chain = new LinkedHashMap<String, TableModelFilter>();
	}
		
	public ChainedTableModelFilter add(String filterName, TableModelFilter filter)
	{
		chain.put(filterName, filter);
		return this;
	}
	
	public void clear()
	{
		chain.clear();
	}
	
	public boolean hasFilterName(String name)
	{
		return chain.containsKey(name);
	}
	
	
	public TableModelFilter getElement(String name)
	{
		return chain.get(name);
	}
	
	public boolean test(Vector<Object> row)
	{
		if (!inUse)
			return true;
		
		boolean testresult = true;
		
		for (String filterName : chain.keySet())
		{
			testresult = testresult && chain.get(filterName).test(row);
		}
		
		if (inverted)
		{
			testresult = !testresult;
		}
		
		
		return testresult;
	}
			
	
}
			
			