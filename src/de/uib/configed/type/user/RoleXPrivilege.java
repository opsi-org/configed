/*
 * opsi-configed - configuration editor for client work stations in opsi
 * (open pc server integration) www.opsi.org
 *
 * class Role2Privilege
 * Copyright (C) 2016 uib.de
 *
 * author: Rupert Röder
 *
 * describes Privileges which can be attributed to users
 *
 */

 
 /* 
 	create table ROLE2PRIVILEGE
 	//role comprises privilege, as well as, privilege ist associated with role
	(
		r2p_id INTEGER NOT NULL AUTO_INCREMENT,
		r2p_role_id REFERENCES role (role_id),
		r2p_privilege_id REFERENCES privilege (privilege_id),
		r2p_description VARCHAR(2000),
		r2p_lastchanged TIMESTAMP
		
		PRIMARY KEY (r2p_role_id, r2p_privilege_id)
	}
 
 */

 
package de.uib.configed.type.user;

import java.util.*;
import javax.swing.*;
import de.uib.utilities.logging.*;


public class RoleXPrivilege

//produces role -> list<privilege>
{
	
}
