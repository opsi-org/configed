package de.uib.configed.type;

import java.util.*;
import javax.swing.*;
import de.uib.utilities.logging.*;

public class ConfigOption extends RetrievedMap
	implements de.uib.utilities.table.ListCellOptions
{
	
	public final static String referenceID = "configId";
	
	public final static String BOOL_TYPE = "BoolConfig";
	public final static String UNICODE_TYPE = "UnicodeConfig";
	
	
	public ConfigOption(Map<String, Object> retrieved)
	{
		super(retrieved);
		build();
	}
	
	@Override
	protected void build()
	{
		//overwrite values
		if  (retrieved == null || retrieved.get("possibleValues") == null)
			put("possibleValues", new ArrayList<Object>());
		else
			put("possibleValues", retrieved.get("possibleValues"));
		
		if  (retrieved == null || retrieved.get("defaultValues") == null)
			put("defaultValues", new ArrayList<Object>());
		else
			put("defaultValues", retrieved.get("defaultValues"));
		
		if  (retrieved == null || retrieved.get("description") == null)
			put("description", "");
		else
			put("description", retrieved.get("description"));
		
		if (retrieved == null || retrieved.get("type") == null)
		{
			logging.debug(this, "set default UnicodeConfig");
			put("type", "UnicodeConfig");
		}
		
		else 
			put("type", retrieved.get("type"));
		
		if (retrieved == null)
			put("selectionMode", ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		
		else if (retrieved.get("multiValue") == null )
			put("selectionMode", ListSelectionModel.SINGLE_SELECTION);
		
		else
		{
			if ((Boolean) retrieved.get("multiValue")) 
				put("selectionMode", ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
			else
				put("selectionMode", ListSelectionModel.SINGLE_SELECTION);
		}
				
		//if (retrieved.get("type") == null )
		//	put("classname", "java.lang.String");
		//else if (retrieved.get("type").equals("BoolConfig"))
		//	put("classname", "java.lang.Boolean");
		//else
			
		put("classname", "java.util.List");
			
		
		if (retrieved == null)
			put("editable", true);
		else if (retrieved.get("editable") == null )
			put("editable", false);
		else if ( (Boolean) retrieved.get("editable") )
			put("editable", true);
		else
			put("editable", false);
	}
		
	
	//======================
	//interface de.uib.utilities.table.ListCellOptions
	public java.util.List getPossibleValues()
	{
		return  (java.util.List) get("possibleValues");
	}
	
	public java.util.List getDefaultValues()
	{
		return  (java.util.List) get("defaultValues");
	}
	
	public void setDefaultValues(java.util.List values)
	{
		put("defaultValues", values);
	}
	
	public int getSelectionMode()
	{
		return (Integer) get("selectionMode");
	}
	
	public boolean isEditable()
	{
		return (Boolean) get("editable");
	}
	
	public String getDescription()
	{
		return  (String) get("description");
	}
	
	
	//======================
}
		



	
	

