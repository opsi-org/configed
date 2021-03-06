package de.uib.opsicommand;

import java.util.Map;
import java.util.List;
import org.json.*;

public abstract class Executioner
{
	public abstract ConnectionState getConnectionState();
	public abstract void setConnectionState (ConnectionState state);
	public abstract boolean doCall (OpsiMethodCall omc);
	public abstract JSONObject retrieveJSONObject(OpsiMethodCall omc);
    	public abstract List<JSONObject> retrieveJSONObjects(List<OpsiMethodCall> omcList);
	public abstract JSONObject jsonMap (Map m);
	public abstract Object getValueFromJSONObject(Object o, String key);
	public abstract Object jsonArray(List l);
	public abstract List getListResult (OpsiMethodCall omc);
	public abstract List<String> getStringListResult (OpsiMethodCall omc);
	public abstract List<List<String>>getListOfStringLists(OpsiMethodCall omc);
	public abstract Map<String, Object> getMapResult (OpsiMethodCall omc);
	public abstract Map getMapOfLists ( OpsiMethodCall omc);
	public abstract Map getMapOfMaps ( OpsiMethodCall omc);
	public abstract List<Map<String, Object>> getListOfMaps( OpsiMethodCall omc);
	public abstract List<Map<String, String>> getListOfStringMaps( OpsiMethodCall omc);
	public abstract Map<String, Object> getMap_Object(OpsiMethodCall omc);
	public abstract Map<String, Map<String, Object>> getMap2_Object(OpsiMethodCall omc);
	public abstract Map<String, Map<String, Map<String, Object>>> getMap3_Object(OpsiMethodCall omc);
	public abstract Map<String, Map<String, String>> getStringMappedObjectsByKey(
		OpsiMethodCall omc, String key, 
		String[] sourceVars, String[] targetVars,
		Map<String, String> translateValues);
	public abstract Map<String, Map<String, String>> getStringMappedObjectsByKey(
		OpsiMethodCall omc, String key, String[] sourceVars, String[] targetVars);
	public abstract Map<String, Map<String, String>> getStringMappedObjectsByKey(
		OpsiMethodCall omc, String key);
	public abstract Map getMapOfListsOfMaps ( OpsiMethodCall omc);
	public abstract List getListOfMapsOfListsOfMaps ( OpsiMethodCall omc);
	public abstract String getStringResult (OpsiMethodCall omc);
	public abstract boolean getBooleanResult (OpsiMethodCall omc);
	public abstract Map<String, Object> getMapFromItem (Object s);
	public abstract List getListFromItem (String s);
	public abstract String getStringValueFromItem (Object s);
	//public abstract Object deriveStandard(Object ob);
	public final static Executioner NONE = new NONEexecutioner();
}
   
   
