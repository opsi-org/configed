package de.uib.configed.type;

import java.util.*;

public class NOMGroupRelation extends NOMRelation
{

	public NOMGroupRelation()
	{
		ClassIdentifier = "Group";
		attributes.add("id");
		attributes.add("description");
		attributes.add("notes");
	}
	
	public String getKey()
	{
		return values.get("id");
	}
	
}
