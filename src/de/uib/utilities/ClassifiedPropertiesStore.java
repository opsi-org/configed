package de.uib.utilities;

import de.uib.utilities.logging.*;
import java.util.*;
import java.io.*;

public abstract class ClassifiedPropertiesStore extends PropertiesStore
//there are only certain keys allowed, concrete classes specify the keys
{
	protected Map<String, String> legalKeys
	 = new HashMap<String, String>(); //keys with comments
	
	public ClassifiedPropertiesStore(File store)
	{
		super(store);
	}
	
	
	@Override
	public String getProperty(String key, String defaultValue)
	{
		if (!legalKeys.containsKey(key))
			logging.warning(this, "trying to use not configured key " + key);
		
		return getProp(key, defaultValue);
	}
	
	
	@Override
	public void setProperty(String key, String value)
	{
		if (legalKeys.containsKey(key))
			setProp(key, value);
		else
			logging.warning(this, "trying to use not configured key " + key);
	}
	
}
