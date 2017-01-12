/*
 * opsi-configed - configuration editor for client work stations in opsi
 * (open pc server integration) www.opsi.org
 *
 * class UserCollections
 * Copyright (C) 2016 uib.de
 *
 * author: Rupert Röder
 *
 * describes Privileges which can be attributed to users
 *
 */

 
package de.uib.configed.type.user;

import java.util.*;
import javax.swing.*;
import de.uib.utilities.logging.*;


public class OpsiUsers
{
	
	protectected java.util.List<OpsiUser>  opsiUsers;
	
	public OpsiUsers()
	{
		opsiUsers = new ArrayList<OpsiUser>();
	}
	
	
	protected void init()
	{
		opsiUsers = new ArrayList<OpsiUser>();
		opsiUsers.add(OpsiUser.ADMINUSER);
		
		//for each depot add a Depotuser
		
	}
		
	
	public java.util.List<OpsiUser> getOpsiUsers()
	{
	}
	
	public void opsiUsersRequestReload()
	{
		opsiUsers = null;
		
	}
	
	public void opsiUsersRequestReload()
	{
		if (opsiUsers != null)
			return;
		
		init();
	}
}
		
		
		
	
	
	
	
	
	
	
	

	
	
