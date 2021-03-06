/* 
 *
 * 	uib, www.uib.de, 2009
 * 
 *	author Rupert Röder 
 *
 */
 
package de.uib.utilities.table.provider;

import java.util.*;

public interface TableSource
{
	Vector<String> retrieveColumnNames();
	
	Vector<String> retrieveClassNames();
	
	//we get a new version
	Vector<Vector<Object>> retrieveRows();
	
	//Map<String, java.util.List<String>> getFunction(Integer defIndex, Integer valIndex);
	
	void setRowCounting(boolean b);
	
	boolean isRowCounting();
	
	String getRowCounterName();
	
	void requestReload();
	
	void structureChanged();
}
