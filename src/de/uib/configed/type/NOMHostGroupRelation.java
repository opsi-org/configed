package de.uib.configed.type;

import java.util.*;

public class NOMHostGroupRelation extends NOMGroupRelation
{
	public NOMGroupRelation()
	{
		ClassIdentifier = "HostGroup";
		attributes.add("parentGroup Id");
	}
}
