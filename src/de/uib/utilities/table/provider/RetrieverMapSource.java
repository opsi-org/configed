/* 
 *
 * 	uib, www.uib.de, 2009
 * 
 *	author Rupert Röder 
 *
 */
 
package de.uib.utilities.table.provider;

import java.util.*;
import java.sql.*;
import de.uib.utilities.logging.*;

public class RetrieverMapSource extends MapSource
// the map is not given via a parameter but by a pointer to a function 
{
	protected MapRetriever retriever;
	
	public RetrieverMapSource(Vector<String> columnNames, Vector<String> classNames, MapRetriever retriever)
	{
		super(columnNames, classNames, null);
		this.retriever = retriever;
		rows = new Vector();
		
	}
	
	
	protected void fetchData()
	{
		table = retriever.retrieveMap();
		//System.out.println ( " -------- RetrieverMapSource fetchData() : " +        table);
		super.fetchData();
	}
}
	
