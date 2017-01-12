package de.uib.opsidatamodel.permission;

import java.util.*;
import de.uib.utilities.logging.*;
import de.uib.configed.*;
import de.uib.configed.type.*;

public interface OpsiPermission
{
	
	public enum HostTypeOfPrivilege{ ALL, DEPOT, SERVER }
	public enum ActionPrivilege{ READ_ONLY, READ_WRITE }
	
	public static String CONFIGKEY_STR_USER = "user";
	public static String CONFIGKEY_STR_HOST = "host";
	public static String CONFIGKEY_STR_PRIVILEGE = "privilege";
	public static String CONFIGKEY_STR_DEPOT = "depotaccess";
	public static String CONFIGKEY_STR_DEPOTLIST = "depots";
	public static String CONFIGKEY_STR_DEPOTACCESSCONTROLLED = "configured";
	public static String CONFIGKEY_STR_SERVER = "opsiserver";
	public static String CONFIGKEY_STR_READWRITE = "write";
	public static String CONFIGKEY_STR_ALLHOSTS = "all";
	public static String CONFIGKEY_STR_READONLY = "registered_readonly";
	
	
	
	//permit restrictable action
	//public java.util.List<Object> reduceList(  java.util.List<Object> checkList )
	public String getInfo();
	public String signalCause();
	
	public ActionPrivilege allowsAction();
	public HostTypeOfPrivilege isFor();
}




