package de.uib.utilities.datastructure;

import java.util.*;
import de.uib.utilities.logging.*;

public class Relation extends ArrayList<StringValuedRelationElement>
{
	protected final List<String> attributes;
	protected final Set<String> attributeSet;


	protected Map<String, Map<String, Relation>> functionByAttribute;

	public Relation(List<String> attributes)
	{
		super();
		this.attributes = attributes;
		attributeSet = new HashSet(attributes);

		functionByAttribute = new HashMap<String, Map<String, Relation>>();

	}

	public Set<String> getAttributeSet()
	{
		return attributeSet;
	}

	/*
	public  RelationElement adapt(RelationElement rowmap)
{
		logging.info(this, "adapt " + rowmap);
		boolean resultIsReduced = false;
		Set<String> keys = rowmap.keySet();
		logging.info(this, "adapt keys " + keys);
		
		keys.removeAll(getAttributeSet());
		
		logging.info(this, "adapt keys, attribute set after removal " + keys);
		if (!keys.isEmpty())
		{
			resultIsReduced = true;
		
			Set<String> keysToRemove = new HashSet<String>(keys);
			
			logging.info(this, "adapt keys, attribute set after removal " + keysToRemove);
			
			for (String key : keysToRemove)
			{
				logging.info(this, "adapt remove " + key);
				rowmap.remove(key); //does not work
				logging.info(this, "adapt removed ?  " + rowmap);
			}
		}
		
		if (resultIsReduced)
			logging.info(this, "restrictAttributes " + rowmap);
		
		return rowmap;
}
	*/
	
	

	

	public StringValuedRelationElement integrateRaw(Map<String, Object> map)
	{
		StringValuedRelationElement rowmap = new StringValuedRelationElement(map);
		add( rowmap );

		return rowmap;
	}



	//for each attribute:
	//produce the function which maps any "key" value of this attribute to the list
	//of all entries which have this value
	public Map<String, Relation> getFunctionBy(String attribute)
	{
		Map<String, Relation> function = functionByAttribute.get(attribute);

		if (function != null)
			return function;

		function = new HashMap<String, Relation>();
		functionByAttribute.put(attribute, function);


		for (StringValuedRelationElement element : this)
		{
			String valueTakenAsKey = element.get(attribute);
			Relation valueList =  function.get(valueTakenAsKey);

			if (valueList == null)
			{
				valueList = new Relation(attributes);
				function.put(valueTakenAsKey, valueList);
			}

			valueList.add(element);

			//logging.info(this, " getFunctionBy " + attribute + ":  for key " + valueTakenAsKey + "  composed valueList " +valueList);

			//logging.info(this, " getFunctionBy " + attribute + ":   " + function);
		}



		return function;

	}






}



