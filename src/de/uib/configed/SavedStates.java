package de.uib.configed;

import de.uib.messages.*;
import de.uib.utilities.*;
import de.uib.utilities.logging.*;
import javax.swing.*;
import java.util.*;
import java.io.*;



public class SavedStates extends ClassifiedPropertiesStore
{
	public SaveInteger saveUsageCount;
	public SaveDepotSelection saveDepotSelection;
	public SaveString saveGroupSelection;
	//public final SaveSet saveLocalbootproductFilterset;
	public SessionSaveSet<String> saveLocalbootproductFilter;
	//public final SessionSaveSet<String> saveLocalbootproductSelection;
		//up to now not used
	
	public Map<String, SaveString> saveServerConfigs; 
	
	public SavedStates(File store)
	{
		super(store);
		saveUsageCount = new SaveInteger("saveUsageCount", 0, this);
		saveDepotSelection = new SaveDepotSelection(this);
		saveGroupSelection = new SaveString("groupname",  this);
		saveLocalbootproductFilter = new SessionSaveSet();
		//saveLocalbootproductSelection = new SessionSaveSet();
		saveServerConfigs = new HashMap<String, SaveString>();
		
	}
	
	
	public void store()
	{
		try{
			super.store(null);
		}
		catch(IOException iox)
		{
			logging.warning(this, "could not store saved states, " + iox);
		}
	}
		
	
	
	abstract class SaveState
	{
		String key;
		Object defaultValue;
		SavedStates states;
		
		SaveState()
		{
		}
		
		SaveState(String key, Object defaultValue, SavedStates states)
		{
			this.key = key;
			this.defaultValue = defaultValue;
			this.states = states;
			states.legalKeys.put(key, "");
		}
		
		public void serialize(Object ob)
		{
			states.store();
			//we store every time when we add an object
		}
		
			
		public Object deserialize()
		{
			return null;
		}
	}
	
	public class SessionSaveSet<T> extends SaveState 
	{
		Set<T> saveObject;
		
		SessionSaveSet()
		{
		}
		
		public void serialize(Object ob)
		{
			if (ob == null)
				saveObject = null;
			else
				saveObject = (Set<T>) ob;
		}
		
		public Object deserialize()
		{
			return saveObject;
		}
	}
	
	
	public class SaveSet extends SaveState
	{
		SaveSet(String key, SavedStates states)
		{
			super(key, "", states);
		}
		
		@Override
		public void serialize(Object  value)
		{
			Set val = null;
			if (value == null)
				val = new HashSet();
			else
				val = (Set) value;
			
			states.setProperty(key, val.toString());
			states.store();
		}
		
		@Override
		public Object deserialize()
		{
			return null;
		}
		
	}
	
	public class SaveString extends SaveState
	{
		SaveString(String key, SavedStates states)
		{
			super(key, "", states);
		}
		
		@Override
		public void serialize(Object  value)
		{
			states.setProperty(key, (String) value);
			states.store();
		}
		
		@Override
		public String deserialize()
		{
			if (states.getProperty(key, (String) defaultValue).equals(defaultValue))
				return null;
			
			return states.getProperty(key, (String) defaultValue);
		}
		
	}
	
	
	public class SaveInteger extends SaveState
	{
		SaveInteger(String key, Object defaultValue, SavedStates states)
		{
			super(key, defaultValue, states);
			if (!(defaultValue instanceof Integer))
				logging.error("default value must be Integer");
		}
		
		@Override
		public void serialize(Object  value)
		{
			states.setProperty(key, value.toString());
			states.store();
		}
		
		@Override
		public String deserialize()
		{
			logging.info(this, "deserialize states" + states);
			logging.info(this, "deserialize  getProperty " + states.getProperty(key, defaultValue.toString()));
			return states.getProperty(key, defaultValue.toString());
		}
		
	}
	
	public class SaveDepotSelection extends SaveState
	{
		SaveDepotSelection(SavedStates states)
		{
			super("selectedDepots", "", states);
		}
		
		@Override
		public void serialize(Object selectedDepots)
		{
			states.setProperty(key, Arrays.toString(
				(String[]) selectedDepots)
				);
			states.store();
		}
		
		@Override
		public String[] deserialize()
		{
			String s = states.getProperty(key, (String) defaultValue);
			if (s.equals(""))
				return null;
			
			s = s.substring(1);
			s = s.substring(0, s.length()-1);
			
			String[] parts = s.split(",");
			for (int i = 0; i < parts.length; i++)
			{
				parts[i] = parts[i].trim();
			}
			
			return parts;
		}
	}	
	
}
