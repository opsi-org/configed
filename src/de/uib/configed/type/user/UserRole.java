/*
 * opsi-configed - configuration editor for client work stations in opsi
 * (open pc server integration) www.opsi.org
 *
 * class UserRole
 * Copyright (C) 2016 uib.de
 *
 * author: Rupert Röder
 *
 * describes a set of privileges which can be attributed to users
 *
 */

 
 /* 
 	create table USERROLE
 		(
 			role_id INTEGER NOT NULL AUTO_INCREMENT,
 			role_name VARCHAR(300) NOT NULL,
 			role_description VARCHAR(2000),
 			role_lastchanged TIMESTAMP
 			PRIMARY KEY (role_id,)
 		}
 
 */

 
package de.uib.configed.type.user;

import java.util.*;
import javax.swing.*;
import de.uib.utilities.logging.*;


public class UserRole
{
	//host.all.readonly true:
	
	public final static UserRole GLOBAL__VIEW_ONLY = new UserRole("GLOBAL_RESTRICTED");
	//host.all.readonly true && depotaccess.configured false
	
	public final static UserRole DEPOT__VIEW_ONLY = new UserRole("DEPOT_RESTRICTED_VIEW_ONLY");
	//host.all.readonly true && depotaccess.configured true
	
	
	
	//host.all.readonly false:
	
	public final static UserRole GLOBAL__ADMIN_ROLE = new UserRole("GLOBAL_ADMIN");
	// host.all.readonly false:&& opsiserver.write true && depotaccess.configured false
	
	public final static UserRole DEPOT__ADMIN_ROLE_WITH_SERVER = new UserRole("DEPOT_RESTRICTED_ADMIN_WITH_SERVERWRITE");
	// host.all.readonly false && opsiserver.write true && depotaccess.configured true
	
	public final static UserRole DEPOT__ADMIN_ROLE = new UserRole("DEPOT_RESTRICTED_ADMIN");
	// host.all.readonly false && opsiserver.write false ((&& depotaccess.configured true))
	
	static private UserRole instance; 
	
	protected String roleName;
	
	private UserRole(String roleName)
	{
		this.roleName = roleName;
	}
	
	@Override
	public String toString()
	{
		return roleName;
	}
		
	
	public static void determineRole(boolean allReadonly, boolean depotAccessConfigured, boolean opsiserverWrite)
	{
		instance = null; 
		
		if (allReadonly)
		{
			if (!depotAccessConfigured)
				instance = GLOBAL__VIEW_ONLY;
			else 
				instance = DEPOT__VIEW_ONLY;
			
		}
		
		else
			
		{
			if (opsiserverWrite)
			{
				if (!depotAccessConfigured)
					instance = GLOBAL__ADMIN_ROLE;
				else
					instance = DEPOT__ADMIN_ROLE_WITH_SERVER;
			}
			else
				instance = DEPOT__ADMIN_ROLE; 
		}
	}
	
	public static UserRole getRole()
	{
		return instance;
	}
	
}
