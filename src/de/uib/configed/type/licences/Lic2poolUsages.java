package de.uib.configed.type.licences;
import java.util.*;
import de.uib.configed.type.*;
import de.uib.utilities.logging.*;

public class  Lic2poolUsages extends TreeMap<String, java.util.List<LicenceUsageEntry>>
{
	Map<String, java.util.List<String>> lic2poolClientusages;
	
	public Lic2poolUsages()
	{
		 lic2poolClientusages = new TreeMap<String, java.util.List<String>>();
	}
		
	public void addUsage(LicenceUsageEntry entry)
	{
		java.util.List<LicenceUsageEntry> usages = get(entry.getLic4pool());
		java.util.List<String> clientusages = lic2poolClientusages.get(entry.getLic4pool());
		
		if (usages == null)
		{
			usages = new ArrayList<LicenceUsageEntry>();
			put(entry.getLic4pool(), usages);
		}
		if (clientusages == null)
		{
			clientusages = new ArrayList<String>();
			lic2poolClientusages.put(entry.getLic4pool(), clientusages);
		}
		
		usages.add(entry);
		
		if (entry.getClientId() != null)
			clientusages.add(entry.getClientId());
	}
	
	
	public  java.util.List<String> getClientUsages(String lic4pool)
	{
		return  lic2poolClientusages.get(lic4pool);
	}
	
	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		sb.append(getClass().getName());
		sb.append(":");
		for (String key : this.keySet())
		{
			sb.append("\n");
			sb.append(key);
			sb.append(":");
			
			if (get(key).size() > 0)
				sb.append(get(key).get(0).getLic4pool());
		}
		return sb.toString();
	}
		
			
			
	
		
}
	
