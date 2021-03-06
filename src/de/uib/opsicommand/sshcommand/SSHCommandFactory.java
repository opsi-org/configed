package de.uib.opsicommand.sshcommand;

/*
 * configed - configuration editor for client work stations in opsi
 * (open pc server integration) www.opsi.org
 *
 * Copyright (C) 2000-2016 uib.de
 *
 * This program is free software; you can redistribute it 
 * and / or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * @author Anna Sucher
 * @version 1.0
 */

import de.uib.configed.*;
import de.uib.configed.gui.*;
import java.util.*;
import org.json.*;
import de.uib.utilities.logging.*;
/**
* This Class handles  SSHCommands.
**/
public class SSHCommandFactory
{
	/** final string commands for linux terminal **/
	final public String str_replacement_dir = "*.dir.*";
										// http://stackoverflow.com/questions/948008/linux-command-to-list-all-available-commands-and-aliases
	final public String str_command_getLinuxCommands = "COMMANDS=`echo -n $PATH | xargs -d : -I {} find {} -maxdepth 1 -executable -type f -printf '%P\\n'` ; ALIASES=`alias | cut -d '=' -f 1`; echo \"$COMMANDS\"$'\\n'\"$ALIASES\" | sort -u "  ;
	final public String str_command_getDirectories = "ls --color=never -d *.dir.*/*/";
	final public String str_command_getOpsiFiles = "ls --color=never *.dir.*/*.opsi";
	final public String str_command_getVersions = "grep version: *.dir.* --max-count=2  ";
	final public String str_command_catDir = "cat *.dir.*OPSI/control | grep \"id: \"";
	final public String str_command_fileexists = "[ -f .filename. ] &&  rm .filename. && echo \"File .filename. removed\" || echo \"File did not exist\"";
	final public String str_command_fileexists_notremove = "[ -d .filename. ] && echo \"File exists\" || echo \"File not exist\"";
	// final public String str_command_filezsyncExists = "[ -f *.filename.*.zsync ] &&  rm *.filename.*.zsync && echo \"File *.filename.*.zsync removed\" || echo \"File *.filename.*.zsync did not exist\"";
	// final public String str_command_filemd5Exists = "[ -f *.filename.*.md5 ] &&  rm *.filename.*.md5 && echo \"File *.filename.*.md5 removed\" || echo \"File *.filename.*.md5 did not exist\"";
	final public String str_replacement_filename = ".filename.";
	final public String str_file_exists = "File exists";
	final public String str_file_not_exists = "File not exists";

	final public String opsipathVarRepository = "/var/lib/opsi/repository/";
	final public String opsipathVarDepot = "/var/lib/opsi/depot/";
	// final public String str_command_comparemd5 = " if [ -z $((cat *.product.*.md5" + ") | grep $(md5sum *.product.*  | head -n1 | cut -d \" \" -f1)) ] ;  then echo \"*.md5notequal.*\"; else echo \"*.md5equal.*\"; fi";
	// final public String str_replacement_product="*.product.*";
	// final public String str_replacement_equal= "*.md5equal.*";
	// final public String str_replacement_notequal= "*.md5notequal.*";

	/** ConfigedMain instance **/
	private ConfigedMain main;
	private MainFrame mainFrame;
	/** SSHCommandFactory instance **/
	private static SSHCommandFactory instance;
	/** List<Map<String,Object>> list elements are commands with key value pairs **/
	private java.util.List<Map<java.lang.String,java.lang.Object>>  commandlist;
	/** List<SSHCommand_Template> list elements are sshcommands **/
	private java.util.List<SSHCommand_Template>  sshcommand_list; 
	/** list of known menus **/
	private java.util.List<String>  list_knownMenus; 
	/** list of known parent menus **/
	private java.util.List<String>  list_knownParents; 

	/** static String for parent null ("Server-Konsole") **/
	final public static String parentNull = configed.getResourceValue("MainFrame.jMenuServer");
	/** static String for specific parent ("opsi") **/
	final public static String parentOpsi = configed.getResourceValue("MainFrame.jMenuOpsi");
	/** static String for new command ("<Neuer Befehl>") **/
	final public static String menuNew = configed.getResourceValue("SSHConnection.CommandControl.menuText_newCommand");
	/** default position is 0 **/
	final public int position_default = 0;
	
	/** default parameter replace id beginns with <<<  **/
	// public static String replacement_default_1 = "<<<";
	/** default parameter replace id ends with >>>  **/
	// public static String replacement_default_2 = ">>>";
	
	/** setting ssh_colored_output per default true**/
	public static boolean ssh_colored_output = true;
	/** setting ssh_always_exec_in_background per default false**/
	public static boolean ssh_always_exec_in_background = false;
	/** all static commands which need run-time parameter **/
	public static List<de.uib.opsicommand.sshcommand.SSHCommand> ssh_commands_param 
	= new LinkedList<de.uib.opsicommand.sshcommand.SSHCommand>(){{
		// add(new de.uib.opsicommand.sshcommand.CommandOpsiSetRights() );
		// add(new de.uib.opsicommand.sshcommand.CommandWget() );
		// add(new de.uib.opsicommand.sshcommand.CommandOpsimakeproductfile() );
		// add(new de.uib.opsicommand.sshcommand.CommandOpsiPackageManagerInstall() );
		// add(new de.uib.opsicommand.sshcommand.CommandOpsiPackageManagerUninstall() );
		// add(new de.uib.opsicommand.sshcommand.CommandDeployClientAgent() );
		// add(new de.uib.opsicommand.sshcommand.List_CommandGetDhcpdLog() );
	}};

	/** final static name of field "id" */
	final  public String command_map_id="id";
	/** final static name of field "menuText" */
	final  public String command_map_menuText="menuText";
	/** final static name of field "parentMenuText" */
	final  public String command_map_parentMenuText="parentMenuText";
	/** final static name of field "tooltipText" */
	final  public String command_map_tooltipText="tooltipText";
	/** final static name of field "position" */
	final  public String command_map_position="position";
	/** final static name of field "needSudo" */
	final  public String command_map_needSudo="needSudo";
	/** final static name of field "commands" */
	final  public String command_map_commands="commands";

	SSHConnectExec connection = null;
	public static String connected = configed.getResourceValue("SSHConnection.connected");
	public static String connected_not_allowed = configed.getResourceValue("SSHConnection.connected_not_allowed");
	public static String unknown = configed.getResourceValue("SSHConnection.unknown");
	public static String not_connected = configed.getResourceValue("SSHConnection.not_connected");
	
	// SSHCommandFactory.getInstance().sudo_text
	public static String sudo_failed_text = configed.getResourceValue("SSHConnection.sudoFailedText");
	public static String sudo_text = "sudo -S -p \"" + sudo_failed_text + "\" ";
	final private String opsisetrights = "opsi-set-rights";

	final public String sshusr = "<<!sshuser!>>";
	final public String sshhst = "<<!sshhost!>>";

	final public String confidential =  "***confidential***";
	ArrayList<String> createdProducts = new ArrayList<String>();
	SSHCommandParameterMethods pmethodHandler = null;
	/**
	* Factory Instance for SSH Command 
	* @param main {@link de.uib.configed.ConfigedMain} class
	**/
	private SSHCommandFactory(ConfigedMain main)
	{
		this.main = main;
		System.out.println("SSHComandFactory new instance");
		instance = this;
		addAditionalParamCommands();
		connection = new SSHConnectExec(this.main);
		pmethodHandler = SSHCommandParameterMethods.getInstance(this.main);
	}

	/**
	* Method allows only one instance
	* Design: Singelton-Pattern
	* @param main {@link de.uib.configed.ConfigedMain} class
	* @return SSHCommandFactory instance
	**/
	public static SSHCommandFactory getInstance(ConfigedMain m)
	{
		if (instance != null)
			return instance;
		else 
			return new SSHCommandFactory(m);
	}
	/**
	* Method allows only one instance
	* Design: Singelton-Pattern
	* @return SSHCommandFactory instance
	**/
	public static  SSHCommandFactory getInstance()
	{
		if (instance != null)
			return instance;
		else 
			return new SSHCommandFactory(null);
	}

	protected Set<String> allowedHosts = new HashSet<String>();
	
	public void setAllowedHosts(Collection<String> allowed)
	{
		allowedHosts.addAll (allowed );
	}
	public Set<String> getAllowedHosts()
	{return allowedHosts;}
	private void addAditionalParamCommands()
	{
		ssh_commands_param.add(new de.uib.opsicommand.sshcommand.CommandOpsiPackageManagerInstall() );
		ssh_commands_param.add(new de.uib.opsicommand.sshcommand.CommandOpsiPackageManagerUninstall() );
		ssh_commands_param.add(new de.uib.opsicommand.sshcommand.CommandOpsimakeproductfile() );
		ssh_commands_param.add(new de.uib.opsicommand.sshcommand.CommandWget() );
		ssh_commands_param.add(new de.uib.opsicommand.sshcommand.CommandModulesUpload() );

		// Funktioniert nicht wie gewünscht. Optionaler Parameter "<<<....>>>" wird nicht abgefragt.
		// ssh_commands_param.add(new de.uib.opsicommand.sshcommand.CommandOpsiSetRights() ); 
		// SSHCommand csetrights = new de.uib.opsicommand.sshcommand.CommandOpsiSetRights();
		// LinkedList<String> coms = new LinkedList<String>()
		// {{
		// 	add(csetrights.getCommandRaw());
		// }};
		// ssh_commands_param.add(new SSHCommand_Template(csetrights, new LinkedList<String>(){{add(csetrights.getCommandRaw());}} ));
		ssh_commands_param.add(new de.uib.opsicommand.sshcommand.CommandOpsiSetRights());
		// ssh_commands_param.add(new SSHCommand_Template(opsisetrights, coms, configed.getResourceValue("SSHConnection.command.opsisetrights"), true, null, configed.getResourceValue("SSHConnection.command.opsisetrights.tooltip"), 110));
		ssh_commands_param.add(new de.uib.opsicommand.sshcommand.CommandDeployClientAgent() );
	}
	public ArrayList<String> getProductHistory()
	{
		return createdProducts;
	}
	public void addProductHistory(String prod)
	{
		createdProducts.add(prod);
	}
	public SSHCommandParameterMethods getParameterHandler()
	{ 
		if (pmethodHandler != null) return pmethodHandler;
		else 
		{
			pmethodHandler = SSHCommandParameterMethods.getInstance(this.main);
			return pmethodHandler;
		}
	}

	public void setMainFrame(MainFrame mf)
	{
		if (mf != null)
			mainFrame = mf;
	}
	/** 
	* Testing the confd-method 'SSHCommand_getObjects' 
	* @return True if method exists
	**/
	public boolean checkSSHCommandMethod()
	{
		return main.getPersistenceController().checkSSHCommandMethod("SSHCommand_getObjects");
	}
	/**
	* Sets the commandlist to null
	**/
	public void retrieveSSHCommandListRequestRefresh()
	{
		logging.info(this, "retrieveSSHCommandListRequestRefresh commandlist null");
		commandlist=null;
	}

	
	/**
	* Builds a SSHCommand_Template uses the given parameter
	* @param id : String
	* @param pmt (parent menu text) : String
	* @param mt (menu text): String
	* @param ttt (tooltip text): String
	* @param p (position): int
	* @param ns (needSudo): boolean
	* @param c (commands): LinkedList<String>
	* @return new SSHCommand_Template 
	**/
	public SSHCommand_Template buildSSHCommand(String id, String pmt, String mt, String ttt, int p, boolean ns, LinkedList<String> c)
	{
		SSHCommand_Template com = new SSHCommand_Template(id, 
			c, // Achtung Reihenfolge der Elemente in Arrays c könnte sich ändern !" toList = ArrayList! JsonArray muss nicht sortiert sein!"
			mt, ns, pmt, ttt, p);
		return com;
	}

	/**
	* retrieve commandlist from persistencecontroller (if commandlist is null) 
	*  and build sshcommandlist
	* @return java.util.List<SSHCommand_Template> 
	**/
	public java.util.List<SSHCommand_Template> retrieveSSHCommandList()
	{
		logging.info(this, "retrieveSSHCommandList ");
		if (commandlist == null)
			commandlist = main.getPersistenceController().retrieveCommandList();

		sshcommand_list = new java.util.ArrayList<SSHCommand_Template>();
		list_knownMenus = new java.util.ArrayList<String>();
		list_knownParents = new java.util.ArrayList<String>();

		for (Map<java.lang.String, java.lang.Object> map : commandlist) 
		{
			SSHCommand_Template com = buildSSHCommand( 
				((String)map.get(command_map_id)),
				((String)map.get(command_map_parentMenuText)), 
				((String)map.get(command_map_menuText)), 
				((String)map.get(command_map_tooltipText)), 
				((int)map.get(command_map_position)), 
				((boolean)map.get(command_map_needSudo)),
				null
			);
			if (map.get(command_map_commands) != null)
			{
				// Achtung Reihenfolge könnte sich ändern !" toList = ArrayList! JsonArray muss nicht sortiert sein!"
				LinkedList com_commands = new LinkedList ( ((JSONArray)map.get(command_map_commands)).toList() );
				com.setCommands(com_commands);
			}
			list_knownMenus.add(com.getMenuText());
			
			String parent = com.getParentMenuText();
			if (parent== null )
				parent = parentNull;
			if (!list_knownParents.contains(parent))
				list_knownParents.add(parent);
			
			sshcommand_list.add(com);
		}
		return sshcommand_list;
	}
	/**
	* Sort all menu names alphabeticly 
	* @return java.util.List<String> sorted list_knownMenus
	**/
	public java.util.List<String> getSSHCommandMenuNames()
	{
		if (commandlist == null)
			commandlist = main.getPersistenceController().retrieveCommandList();
		Collections.sort(list_knownMenus, new Comparator<String>() 
		{
			@Override
			public int compare(String s1, String s2) 
			{
				return s1.compareToIgnoreCase(s2);
			}
		});
		return list_knownMenus;
	}

	/**
	* Sort all parent menus alphabeticly 
	* @return java.util.List<String> sorted list_knownParents
	**/
	public java.util.List<String> getSSHCommandMenuParents()
	{
		if (commandlist == null)
			commandlist = main.getPersistenceController().retrieveCommandList();
		Collections.sort(list_knownParents, new Comparator<String>() 
		{
			@Override
			public int compare(String s1, String s2) 
			{
				return s1.compareToIgnoreCase(s2);
			}
		});
		return list_knownParents;
	}


	/**
	* Sorts all SSHCommands by position, after that sorts by there parent menus (keep position order in parent menus).
	* @return java.util.LinkedHashMap<String,java.util.List<SSHCommand_Template>> sortedComs
	**/
	public java.util.LinkedHashMap<String,java.util.List<SSHCommand_Template>> getSSHCommandMapSortedByParent()
	{
		if (commandlist == null)
			commandlist = main.getPersistenceController().retrieveCommandList();
		
		logging.info(this, "getSSHCommandMapSortedByParent sorting commands ");
		Collections.sort(sshcommand_list);

		java.util.LinkedHashMap<String,java.util.List<SSHCommand_Template>> sortedComs= new LinkedHashMap<String,java.util.List<SSHCommand_Template>>();
		sortedComs.put(parentNull , new LinkedList<SSHCommand_Template>());
		sortedComs.put(parentOpsi , new LinkedList<SSHCommand_Template>());
		
		for(SSHCommand_Template com: sshcommand_list)
		{
			String parent = com.getParentMenuText();
			if ((parent == null) || (parent.trim().equals(""))) 
				parent = parentNull;
			java.util.List parentList = new LinkedList<SSHCommand_Template>();
			if (sortedComs.containsKey(parent)) 
				parentList = sortedComs.get(parent);
			else 
				sortedComs.put(parent, parentList) ;

			if (!(parentList.contains(com)))
				parentList.add(com);
		}
		return sortedComs;
	}

	/**
	* Get list of SSHCommands which need run-time parameter
	* @return java.util.List<SSHCommand> ssh_commands_param
	**/
	public java.util.List<SSHCommand> getSSHCommandParameterList()
	{
		logging.info(this, "getSSHCommandParameterList ");		
		return ssh_commands_param;
	}

	/**
	* Search sshcommand with given menu name
	* @param menu
	* @return SSHCommand_Template
	**/
	public SSHCommand_Template getSSHCommandByMenu(String menu)
	{
		if (sshcommand_list == null) 	commandlist = main.getPersistenceController().retrieveCommandList();
		for (SSHCommand_Template c : sshcommand_list)
			if (c.getMenuText().equals(menu)) return c;
		return null;
	}



	/**
	* Build an Map of key-value-pairs from a SSHCommand_Template
	* @param SSHCommand_Template 
	* @return Map<String,Object> command
	**/
	private Map<String,Object> buildCommandMap(SSHCommand_Template c)
	{
		Map<String, Object> com = new HashMap<String, Object>();
		// com.put("id", c.getId());
		com.put(command_map_menuText, c.getMenuText());
		com.put(command_map_parentMenuText, c.getParentMenuText());
		com.put(command_map_tooltipText, c.getToolTipText());
		com.put(command_map_position, c.getPriority());
		com.put(command_map_needSudo, c.needSudo());
		return com;
	}

	/**
	* Create or update an command (update local lists)
	* @param SSHCommand_Template command
	**/
	public boolean saveSSHCommand(SSHCommand_Template command)
	{	
		logging.info(this, "saveSSHCommand command " + command.toString());
		List<Object> jsonObjects = new ArrayList<Object>();
		try
		{
			JSONObject jsComMap = new JSONObject(buildCommandMap(command));
			JSONArray jsComArrCom = new JSONArray(((SSHMultiCommand)command).getCommandsRaw() );
			jsComMap.put(command_map_commands, jsComArrCom);
			jsonObjects.add(jsComMap);
		} catch (Exception e)
		{
			e.printStackTrace();
		}
		

		if (list_knownMenus.contains(command.getMenuText()))
		{
			logging.info(this, "saveSSHCommand sshcommand_list.contains(command) true");
			if (main.getPersistenceController().updateSSHCommand( jsonObjects )) //;
			{
				((SSHCommand_Template) sshcommand_list
					.get(
						sshcommand_list.indexOf(
							getSSHCommandByMenu(command.getMenuText())
						)
					)
				)
				.update(command);
				return true;
			}
		}
		else
		{
			logging.info(this, "saveSSHCommand sshcommand_list.contains(command) false");
			if (main.getPersistenceController().createSSHCommand( jsonObjects ))
			{
				sshcommand_list.add(command);
				list_knownMenus.add(command.getMenuText());
				return true;
			}
		}
		return false;
	}
	public boolean isSSHCommandEqualSavedCommand(SSHCommand_Template command)
	{
		if (list_knownMenus.contains(command.getMenuText()))
		{ 
			logging.info(this, "isSSHCommandEqualSavedCommand compare command " + command.toString());
			logging.debug(this, "isSSHCommandEqualSavedCommand with found " + ((SSHCommand_Template) sshcommand_list
					.get( sshcommand_list.indexOf( getSSHCommandByMenu(command.getMenuText())))));
			logging.debug(this, "isSSHCommandEqualSavedCommand equals " + ((SSHCommand_Template) sshcommand_list
					.get(sshcommand_list.indexOf(getSSHCommandByMenu(command.getMenuText()))))
					.equals(command));
			
			if (((SSHCommand_Template) sshcommand_list
					.get(
						sshcommand_list.indexOf(
							getSSHCommandByMenu(command.getMenuText())
						)
					)
				).equals(command))
			{
				return true;
			} 
			else return false;
		}
		return false;
	}

	/**
	* Delete the command with given menu text 
	* @param String menu
	**/
	public void deleteSSHCommandByMenu(String menu)
	{
		logging.info(this, "deleteSSHCommand menu " + menu);
		// return 
		List<String> jsonObjects = new ArrayList<String>();
		jsonObjects.add(menu);
		if (main.getPersistenceController().deleteSSHCommand(jsonObjects))
		{
		 	sshcommand_list.remove(getSSHCommandByMenu(menu));
			list_knownMenus.remove(menu);
		}
	}

	/**
	* Reload configed menu server-konsole
	*/
	public void reloadServerMenu()
	{
		new Thread()
		{
			public void run()
			{
				main.reloadServerMenu();
			}
		}.start();
	}
	String connection_state = "unknown";
	public SSHConnect getConnection()
	{
		return connection;
	}
	public String getConnectionState()
	{
		return connection_state;
	}
	public void testConnection(SSHConnectExec connection)
	{
		this.connection.disconnect();
		this.connection = connection;
		testConnection(connection.getConnectedUser(), connection.getConnectedHost());
	}
	public void testConnection(String user, String host)
	{
		SSHCommand com = new Empty_Command("ls", "ls", "", false);
		String result = this.connection.exec(com, false);

		logging.debug(this, "result: " + result);
		logging.debug(this, "connection: " + connection);
		logging.debug(this, "connection connected: " + connection.isConnected());
		logging.debug(this, "host: " + connection.getConnectedHost());
		logging.debug(this, "user: " + connection.getConnectedUser());

		if ((connection.isConnected()) && ((result == null)  || (result.trim().equals("null"))))
		{
			logging.info(this, "testConnection not allowed");
			connection_state = connected_not_allowed;
			logging.warning(this, "cannot connect to " + user + "@" + host);
		}
		else if ((result == null) || (com.equals(com.get_ERROR_TEXT())))
		{
			logging.info(this, "testConnection not connected");
			connection_state = not_connected;
			logging.warning(this, "cannot connect to " + user + "@" + host);
		}
		else if (!result.equals(com.get_ERROR_TEXT()))
		{
			logging.info(this, "testConnection connected");
			connection_state = connected;
		}
		else connection_state = unknown;
		updateConnectionInfo(connection_state);
		logging.info(this, "testConnection connection state " + connection_state);
	}

	public void updateConnectionInfo(String status)
	{
		logging.info(this, "mainFrame " + mainFrame);
		logging.info(this, "Globals.mainFrame " + Globals.mainFrame);

		logging.info(this, "status " + status);
		if (mainFrame == null)
			((MainFrame)Globals.mainFrame).updateSSHConnectedInfoMenu(status);
		else
			mainFrame.updateSSHConnectedInfoMenu(status);
	}
}