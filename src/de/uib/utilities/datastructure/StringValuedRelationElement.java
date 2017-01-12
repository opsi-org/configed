package de.uib.utilities.datastructure;

import java.util.*;


//very similar to TableEntry
public class StringValuedRelationElement extends RelationElement<String, String>
{
	
	public final static String NULLDATE = "0000-00-00";
	
	public StringValuedRelationElement()
	{
		super();
	}
	
	
	public StringValuedRelationElement(StringValuedRelationElement rowmap)
	{
		super(rowmap);
	}
	
	
	public StringValuedRelationElement(Map<String, Object> map)
	{
		this();
		produceFrom(map);
	}
	
	/*
	public String getString(String key)
	{
		return super.get(key);
	}
	*/
	
	protected void produceFrom(Map<String, Object> map)
	{
		for (String attribute : allowedAttributes)
		{
			if (map.get(attribute) != null)
			{
				if (map.get(attribute) instanceof String)
					put(attribute, (String) map.get(attribute));
				else //create String object by toString() method
					put(attribute, "" + map.get(attribute));


				if (get(attribute).startsWith(NULLDATE))
					put(attribute, "");


			}
			else
			{
				put(attribute, "");
			}
		}
	}

	
	
			
}



