/*
 * opsi-configed - configuration editor for client work stations in opsi
 * (open pc server integration) www.opsi.org
 *
 * class UserPrivilege
 * Copyright (C) 2016 uib.de
 *
 * author: Rupert Röder
 *
 * describes Privileges which can be attributed to users
 *
 */

 
 /* 
 	create table USER2ROLE
 	//user has role, as well as, role is fulfilled by user
 		(
 			u2r_id INTEGER NOT NULL AUTO_INCREMENT,
 			u2r_user_id REFERENCES user (user_id),
 			u2r_role_id REFERENCES role (role_id),
 			u2r_description VARCHAR(2000),
 			u2r_lastchanged TIMESTAMP
 			
 			PRIMARY KEY (u2r_user_id, u2r_role_id)
 		}
 
 */

 
package de.uib.configed.type.user;

import java.util.*;
import javax.swing.*;
import de.uib.utilities.logging.*;


public class UserXRole
//produces user -> list<role>
{
	Map<String, java.util.List<String>> user2Roles;
	
	public UserXRole
	{
	}
	
	public java.util.List<UserRole> getRoles(String username)
	{
		retrieveUserXRoles();
		
		return  user2Roles.get(username);
	}
	
	public void userXroleRequestRefresh()
	{
		user2Roles = null;
	}
	
	public void retrieveUserXRoles()
	{
		if (user2Roles =! null)
			return;
		
		user2Roles = new HashMap<String, java.util.List<String>>();
		
		for (Map<String, String> row : rows)
		{
			java.util.List<String> roles = user2Roles.get(username);
			
			if (roles == null)
			{
				roles = new ArrayList<String> ();
				user2Roles.put(roles);
			}
			
			roles.add(row.get(role));
		}
	}
	
	
}

