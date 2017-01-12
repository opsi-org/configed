/*
 * opsi-configed - configuration editor for client work stations in opsi
 * (open pc server integration) www.opsi.org
 *
 * class Privilege
 * Copyright (C) 2016 uib.de
 *
 * author: Rupert Röder
 *
 * describes Privileges which can be attributed to users
 *
 */

 
 /* 
 	create table PRIVILEGE
 		(
 		
 			privilege_id INTEGER NOT NULL AUTO_INCREMENT,
 			privilege_name VARCHAR(300) NOT NULL UNIQUE,
 			privilege_description VARCHAR(2000),
 			privilege_lastchanged TIMESTAMP
 			
 			PRIMARY KEY (privilege_id)
 		}
 
 */

 
package de.uib.configed.type.user;

import java.util.*;
import javax.swing.*;
import de.uib.utilities.logging.*;



public class Privilege
{
	
}
