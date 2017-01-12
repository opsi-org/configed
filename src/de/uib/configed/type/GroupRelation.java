package de.uib.configed.type;

import lombok.*;
import java.util.*;

public class GroupRelation extends Relation
{
	public GroupRelation() 
	{
		attributes.add("groupId");
		attributes.add("description");
		attributes.add("notes");
	}
	
	
	public String getKey(RelationElement values)
	{
		return values.get("groupId");
	}
	
}
