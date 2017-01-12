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

public class SqlSource implements TableSource
{
	protected Vector<String> columnNames;
	
	protected Vector<String> classNames;
	
	protected Vector<Vector> rows;
	
	private Connection con;
	private String sqlS; 
	
	protected ResultSet rs;
	protected ResultSetMetaData metaData;
	private Statement stmt;
	private int colLength;
	protected boolean reloadRequested = true;
	
	public SqlSource()
	{
	}
	
	public SqlSource(Connection con, String sqlS)
	{
		this.con = con;
		this.sqlS = sqlS;
	}
	
	public void setConnection(Connection con)
	{
		this.con = con;
		this.sqlS = sqlS;
	}
	
	public void setSqlS(String sqlS)
	{
		this.sqlS = sqlS;
	}
	
	public void requestReload()
	{
		reloadRequested = true;
	}
	
	protected void work()
	{
		Statement stmt = null;
		
		logging.debug(this, "Sql Source for \"" + sqlS + "\": loading data");
		
		try
		{
			stmt = con.createStatement(
					ResultSet.TYPE_SCROLL_INSENSITIVE,
					ResultSet.CONCUR_READ_ONLY);
			rs =  stmt.executeQuery(sqlS);

		} catch (SQLException e) {
			logging.debugOut(1, "Sql Source for \"" + sqlS + "\": " + e.toString());
		}
		
		useMetaData();
		fetchData();
		
		try
		{
			if (stmt != null)
				stmt.close();
		} catch (SQLException e) {
			logging.debugOut(1, "Sql Source for \"" + sqlS + "\": " + e.toString());
		}
		
	}
	
	private void useMetaData () {
		int i;
		try 
		{
			metaData = rs.getMetaData();
			colLength = metaData.getColumnCount();
			columnNames = new Vector<String>();
			
			colLength = metaData.getColumnCount();
			columnNames = new Vector<String>();
			for (i=0;i<colLength;i++) {
				columnNames.add( metaData.getColumnName(i+1) );
				logging.debug(this, " column name " + columnNames.get(i) );
			}
			
			classNames = new Vector<String>();
			for (i=0;i<colLength;i++) {
				classNames.add( metaData.getColumnClassName(i+1) );
				logging.debug(this, " class name " + classNames.get(i) );
			}
		} 
		catch (SQLException e) {
			logging.debugOut(1, "Sql Source for \"" + sqlS + "\", useMetaData() : " + e.toString());
		}
	}
	
	
	protected void fetchData()
	{
		if (rows == null)
			rows = new Vector<Vector>();
		else
			rows.clear();
		
		function = new Map<Object, java.util.List<Object>>();
		
		try
		{
			rs.beforeFirst();
			while (rs.next()) 
			{
				Vector row = new Vector();
				for (int i=0; i< colLength; i++)
				{
					row.add(rs.getString(i+1));
				}
				rows.add(row);
				
				java.util.List<String> list = function.get(rs.getString(defIndex));
				if (list == null)
				{
					list = new ArrayList<String>();
					function.put(rs.getString(defIndex), list);
				}
				
				list.add(function.getString(valIndex);
				
			}
			
		}
		catch (SQLException e) {
			logging.debugOut(1, "Sql Source for \"" + sqlS + "\", fetchData() : " + e.toString());
		}
	}
	
	public Vector<String> retrieveColumnNames()
	{
		//System.out.println( "-- SqlSource retrieveColumnNames, reloadRequested  " + reloadRequested);
		if (reloadRequested)
		{
			work();
			reloadRequested = false;
		}
		
		//System.out.println( "-- columnNames " + columnNames);
		
		return columnNames;
	}
	
	public Vector<String> retrieveClassNames()
	{
		if (reloadRequested)
		{
			work();
			reloadRequested = false;
		}
		
		return classNames;
	}
	
	public Vector<Vector> retrieveRows()
	{
		if (reloadRequested)
		{
			work();
			reloadRequested = false;
		}
		
		return rows;
	}
}
