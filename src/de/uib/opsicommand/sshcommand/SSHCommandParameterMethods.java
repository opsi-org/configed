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

import de.uib.configed.type.*;
import de.uib.configed.*;
import de.uib.configed.gui.*;
import de.uib.configed.gui.ssh.*;
import java.util.*;
import java.awt.*;
import javax.swing.*;
// import org.json.*;
import de.uib.utilities.logging.*;
import de.uib.opsidatamodel.*;
/**
* This Class handles  SSHCommands.
**/
public class SSHCommandParameterMethods extends SSHCommandParameterMethodsAbstractFacade
{


	/** default parameter replace id beginns with <<<  **/
	public static String replacement_default_1 = "<<<";
	/** default parameter replace id ends with >>>  **/
	public static String replacement_default_2 = ">>>";
	public static String param_splitter_default = "><";

	private ConfigedMain main;
	private MainFrame mainFrame;
	private static SSHCommandParameterMethods instance;
	// private String[] methods;
	public static final String method_interactiveElement = configed.getResourceValue("SSHConnection.CommandControl.cbElementInteractiv");
	public static final String method_getSelectedClientNames = configed.getResourceValue("SSHConnection.CommandControl.method.getSelectedClientNames");
	public static final String method_getSelectedClientIPs = configed.getResourceValue("SSHConnection.CommandControl.method.getSelectedClientIPs");
	public static final String method_getSelectedDepotNames = configed.getResourceValue("SSHConnection.CommandControl.method.getSelectedDepotNames");
	public static final String method_getSelectedDepotIPs = configed.getResourceValue("SSHConnection.CommandControl.method.getSelectedDepotIPs");
	public static final String method_getConfigServerName = configed.getResourceValue("SSHConnection.CommandControl.method.getConfigServerName");
	public static final String method_getConnectedSSHServerName = configed.getResourceValue("SSHConnection.CommandControl.method.getConnectedSSHServerName");


	public static Map<String, String> methods = new HashMap<String, String>(){{
		put(method_interactiveElement, method_interactiveElement);
		put(method_getSelectedClientNames, "getSelectedClientNames");
		put(method_getSelectedClientIPs, "getSelectedClientIPs");
		put(method_getSelectedDepotNames, "getSelectedDepotNames");
		put(method_getSelectedDepotIPs, "getSelectedDepotIPs");
		put(method_getConfigServerName, "getConfigServerName");
		put(method_getConnectedSSHServerName, "getConnectedSSHServerName");
	}};
	private String[] formats;

	private SSHCommandParameterMethods(ConfigedMain main)
	{
		this.main = main;
		instance = this;
		init();
	}

	

	public static SSHCommandParameterMethods getInstance(ConfigedMain m)
	{
		if (instance != null) return instance;
		else return new SSHCommandParameterMethods(m);
	}

	public static SSHCommandParameterMethods getInstance()
	{
		if (instance != null) return instance;
		else return new SSHCommandParameterMethods(null);
	}


	public String[] getParameterMethodLocalNames()
	{
		String[] mymethods = new String[methods.size()];
		int counter = 0;
		for (Map.Entry<String, String> entry : methods.entrySet())
		{
			mymethods[counter] = entry.getKey();
			counter++;
		}
		Arrays.sort(mymethods);
		return mymethods;
	}
	public String[] getParameterMethods()
	{
		String[] mymethods = new String[methods.size()];
		int counter = 0;
		for (Map.Entry<String, String> entry : methods.entrySet())
		{
			mymethods[counter] = entry.getValue();
			counter ++;
		}
		return mymethods;
	}
	public String getMethodFromName(String name)
	{
		for (Map.Entry<String, String> entry : methods.entrySet())
		{
			if (name.equals(entry.getKey()))
				return entry.getValue();
		}
		return name;
	}

	public String[] getParameterFormats()
	{
		if (formats != null)
			return formats;
		return null;
	}

	
	public boolean canceled;
	SSHConnectionOutputDialog outputDia = null;
	public SSHCommand parseParameter(final SSHCommand command, SSHConnect caller)
	{
		logging.info(this, "parseParameter command " + command.getCommandRaw());
		if (caller instanceof SSHConnectExec)
			outputDia = ((SSHConnectExec) caller).getDialog();
		else if (caller instanceof SSHConnectTerminal)
			outputDia = ((SSHConnectTerminal) caller).getDialog();
		ArrayList<String> params = command.getParameterList();
		if ((params != null) && (params.size() > 0))
			for (String param : params)
			{
				if (command.getCommandRaw().contains(param))
				{
					String[] splitted_parameter = splitParameter(param);
					String result = callMethod(splitted_parameter[0], splitted_parameter[1]);
					if (result == null) 
					{
						canceled = true;
					}
					else 
					{
						logging.debug(this, "parseParameter command " + command.getCommandRaw());
						logging.debug(this, "parseParameter param " + param);
						logging.debug(this, "parseParameter result " + result);
						((SSHCommand)command).setCommand(command.getCommandRaw().replace(param, result));
						logging.debug(this, "parseParameter command " + command.getCommandRaw());
					}
				}
				
			}
		logging.info(this, "parseParameter command " + command.getCommandRaw());
		return command;
	}

	public String parseParameterToString(SSHCommand command, SSHConnect caller)
	{
		SSHCommand c = parseParameter(command, caller);
		return c.getCommandRaw();
	}
	public String testParameter(String param)
	{
		String[] splitted_parameter = splitParameter(param);
		String result = callMethod(splitted_parameter[0], splitted_parameter[1]);
		if (result == null)
			return configed.getResourceValue("SSHConnection.CommandControl.parameterTest.failed");
		return result;
	}
	
	private void init()
	{
		formats = new String[9];
		formats[0] = "x y z ...";
		formats[1] = "x,y,z,...";
		formats[2] = "[x,y,z,...]";
		formats[3] = "'x' 'y' 'z' '...'";
		formats[4] = "'x', 'y', 'z', '...'";
		formats[5] = "['x','y','z','...']";
		formats[6] = "\"x\" \"y\" \"z\" \"...\"";
		formats[7] = "\"x\",\"y\",\"z\",\"...\"";
		formats[8] = "[\"x\",\"y\",\"z\",\"...\"]";
	}

	public String[] splitParameter(String m)
	{
		logging.info(this, "splitParameter param " + m);
		if ( (m.startsWith(replacement_default_1)) && (m.contains(replacement_default_2)))
		{
			m = m.replace(replacement_default_1, "")
				.replace(replacement_default_2, "");
		}
		
		logging.info(this, "splitParameter param " + m);
		String[] splitted = new String[2];
		splitted[0] = m;
		splitted[1] = "";
		

		if (m.contains(param_splitter_default))
		{
			splitted[0] = m.split(param_splitter_default)[0];
			logging.info(this, "splitParameter method " + splitted[0]);
			// splitted[0] = getTranslatedMethod(splitted[0]);
			logging.info(this, "splitParameter method " + splitted[0]);
			splitted[1] = m.split(param_splitter_default)[1];
			logging.info(this, "splitParameter format " + splitted[1]);
		}
		return splitted;

	}
	public String getTranslatedMethod(String localeMethod)
	{
		String method = "";
		for (Map.Entry<String, String> entry : methods.entrySet())
		{
			if (entry.getKey().equals(localeMethod))
				method = entry.getValue();
		}
		return method;

	}
	private String callMethod(String method, String format)
	{
		logging.info(this, "callMethod method " + method + " format " + format);
		String result = "";
		method = method.trim();
		if (method.equals(methods.get(method_getSelectedClientNames)))
		{
			logging.info(this, "getSelected_clientnames " + getSelected_clientnames());
			result = formatResult(getSelected_clientnames(), format);
		}
		else if  (method.equals(methods.get(method_getSelectedClientIPs)))
		{
			logging.info(this, "getSelected_clientIPs " + getSelected_clientIPs());
			result = formatResult(getSelected_clientIPs(), format);
		}
		else if  (method.equals(methods.get(method_getSelectedDepotNames)))
		{
			result = formatResult(getSelected_depotnames(), format);
		}
		else if  (method.equals(methods.get(method_getSelectedDepotIPs)))
		{
			result = formatResult(getSelected_depotIPs(), format);
		}
		else if  (method.equals(methods.get(method_getConfigServerName)))
		{
			result = formatResult(getConfig_serverName(), format);
		}
		else if  (method.equals(methods.get(method_getConnectedSSHServerName)))
		{
			result = formatResult(getConfig_sshserverName(), format);
		}
		else if (format.equals(""))
		{
			result = getUserText(method, outputDia);
			logging.info(this, "callMethod replace \"" + method + "\" with \"" + result + "\"");	
		}

		return result;
	}
	private String formatResult(String result, String format)
	{
		String[] strarr= new String[1];
		strarr[0] = result;
		return formatResult(strarr, format);
	}
	private String formatResult(String[] result, String format)
	{
		String formated_result = "";
		String f = format.replaceAll(" ", "");
		logging.info(this, "callMethod format f " + f);
		switch (f)
		{
			case "xyz":
			case "xyz...":
				formated_result = Arrays.toString(result).replace("[","").replaceAll(",", " ").replace("]", "");
				break;
			case "x,y,z":
			case "x,y,z,...":
				formated_result = Arrays.toString(result).replace("[","").replace("]", "");
				break;
			case "[x,y,z]":
			case "[x,y,z,...]":
				formated_result = Arrays.toString(result);
				break;

			case "'x''y''z'":
			case "'x''y''z''...'":
				logging.info(this, "formatResult switch case [3] " + "'x''y''z''...'"  + " || " + "'x''y''z'");
				formated_result = createFormattedDataSourceString(result, "'", brackets_none, " ");
				break;
			case "'x','y','z'":
			case "'x','y','z','...'":
				logging.info(this, "formatResult switch case [3] " + "'x''y''z''...'"  + " || " + "'x''y''z'");
				formated_result = createFormattedDataSourceString(result, "'", brackets_none, ",");
				break;
			case "\"x\"\"y\"\"z\"":
			case "\"x\"\"y\"\"z\"\"...\"":
				logging.info(this, "formatResult switch case [4] " + "\"x\"\"y\"\"z\"\"...\""  + " || " + "\"x\"\"y\"\"z\"");
				formated_result = createFormattedDataSourceString(result, "\"", brackets_none, " ");
				break;
			case "\"x\",\"y\",\"z\"":
			case "\"x\",\"y\",\"z\",\"...\"":
				logging.info(this, "formatResult switch case [5] " + "\"x\",\"y\",\"z\",\"...\""  + " || " + "\"x\",\"y\",\"z\"");
				formated_result = createFormattedDataSourceString(result, "\"", brackets_none, ",");
				break;
			case "['x','y','z']":
			case "['x','y','z','...']":
				logging.info(this, "formatResult switch case [5] " +  "['x','y','z']"  + " || " + "['x','y','z','...']");
				formated_result = createFormattedDataSourceString(result, "'", brackets_square, ",");
				break;
			case "[\"x\",\"y\",\"z\"]":
			case "[\"x\",\"y\",\"z\",\"...\"]":
				logging.info(this, "formatResult switch case [5] " + "[\"x\",\"y\",\"z\"]"  + " || " + "[\"x\",\"y\",\"z\",\"...\"]");
				formated_result = createFormattedDataSourceString(result, "\"", brackets_square, ",");
				break;
			default: 
				logging.warning(this, "cannot format into \"" + format + "\" with \"" + Arrays.toString(result) + "\"");
				break;
		}
		return formated_result;
	}
	final String brackets_none = " x ";
	final String brackets_square = "[x]";

	private  String createFormattedDataSourceString(String[] strArr, String begin_end_element, String begin_end_str, String separator)
	{
		String formated_result = "!!!Error!!!";
		try
		{
			strArr = replaceElements(strArr, begin_end_element);
			logging.info(this, "createFormattedDataSourceString[ ]  strArr " + Arrays.toString(strArr));
			formated_result = createStringOfArray(strArr, begin_end_str, separator);
			logging.info(this, "createFormattedDataSourceString[ ] formated_result " + formated_result);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return formated_result;
	}
	private String[] replaceElements(String[] strArrToReplace, String begin_end_ofElement)
	{
		for (int i = 0; i<strArrToReplace.length; i++)
		{
			strArrToReplace[i] = strArrToReplace[i].replace(strArrToReplace[i], 
										begin_end_ofElement 
										+ strArrToReplace[i] 
										+ begin_end_ofElement);
			logging.info(this, "formatResult[] result[i] " + strArrToReplace[i]);
		}
		return strArrToReplace;
	}
	private String createStringOfArray(String[] strArrToReplace, String begin_end_ofStr, String separator)
	{
		String result = "error";
		// String beginStr = begin_end_ofStr.split("x")[0];
		// String endStr = begin_end_ofStr.split("x")[1];
		logging.info(this, "createStringOfArray strArrToReplace " + strArrToReplace);
		logging.info(this, "createStringOfArray strArrToReplace.length " + strArrToReplace.length + "if statement: " + (strArrToReplace.length > 1));
		if (strArrToReplace.length > 1)
		{
			result = Arrays.toString(strArrToReplace)
					.replace("[", begin_end_ofStr.split("x")[0])
					.replaceAll(",", separator)
					.replace("]", begin_end_ofStr.split("x")[1]);
		} else
		{
			result = Arrays.toString(strArrToReplace)
					.replace("[", begin_end_ofStr.split("x")[0])
					.replace("]", begin_end_ofStr.split("x")[1]); 
		}
		logging.info(this, "createStringOfArray result " + result);
		return result;
	}

	public String arrayToStringAsList(Object[] list)
	{
		return Arrays.toString(list).replace("[","").replace("]", "");
	}
	public String arrayToString(Object[] list)
	{
		return Arrays.toString(list).replace("[","").replaceAll(",", " ").replace("]", "");
	}

	protected String getUserText(String text, Component dialog)
	{
		if (dialog == null) dialog = de.uib.configed.Globals.mainFrame;
		logging.debug(this, "getUserText text " + text);
		final JTextField field=new JTextField();
		// field.setEchoChar('*');
		final JOptionPane opPane = new JOptionPane(
			new Object[] {
				new JLabel(text), 
				field
			}, 
			JOptionPane.QUESTION_MESSAGE,
			JOptionPane.OK_CANCEL_OPTION)
		{
			@Override
			public void selectInitialValue()
			{
				super.selectInitialValue();
				((Component) field).requestFocusInWindow();
			}
		};
		final JDialog jdialog = opPane.createDialog(dialog, 
			de.uib.configed.Globals.APPNAME + " " + configed.getResourceValue("SSHConnection.ParameterDialog.Input"));
		jdialog.setSize(400, 150);
		jdialog.setVisible(true);
		
		if ( ((Integer)opPane.getValue()) == JOptionPane.OK_OPTION)
			return field.getText().trim();
		return null;
	}


	public String getConfig_serverName()
	{
		LinkedList<String> depots = main.getPersistenceController().getHostInfoCollections().getDepotNamesList();
		for (String depot : depots)
			if (depot.startsWith(main.HOST))
			{
				logging.debug(this, "getConfig_serverName " + main.HOST);
				return depot;
			}

		logging.debug(this, "getConfig_serverName " + main.HOST);
		//// peristancecontroller methods for depot :
		// public Map<String, Map<String, Object>> getDepots()
		// public LinkedList<String> getDepotNamesList()
		// public Map<String, Map<String, Object>> getAllDepots()
		return main.HOST;
	}

	public String getConfig_sshserverName()
	{
		logging.debug(this, "getConfig_sshserverName " + SSHCommandFactory.getInstance(main).getConnection().getConnectedHost());
		return SSHCommandFactory.getInstance(main).getConnection().getConnectedHost();
	}

	public String[] getSelected_clientIPs()
	{
		logging.debug(this, "getSelected_clientIPs " + Arrays.toString(main.getSelectedClients()) );
		String[] clientnames = new String[main.getSelectedClients().length];
		System.arraycopy( main.getSelectedClients(), 0, clientnames, 0, main.getSelectedClients().length );
		if (clientnames != null) logging.debug(this, "getSelected_clientIPs clientlist != null ");
		else logging.debug(this, "getSelected_clientIPs clientlist == null ");
		String[] clientIPs = new String[clientnames.length];
		int counter = 0;
		for (String name : clientnames)
		{
			HostInfo hostInfo = main.getPersistenceController().getHostInfoCollections().getMapOfAllPCInfoMaps().get(name);
			if (hostInfo != null)
			{
				clientIPs[counter] = hostInfo.getIpAddress();
				counter++;
			}
			else logging.debug(this, "getSelected_clientIPs host " + name + " HostInfo null");

		}
		return clientIPs;
	}
	public String[] getSelected_clientnames()
	{
		logging.debug(this, "getSelected_clientnames  " + Arrays.toString(main.getSelectedClients()) );
		String[] clientnames = new String[main.getSelectedClients().length];
		System.arraycopy( main.getSelectedClients(), 0, clientnames, 0, main.getSelectedClients().length );
		return clientnames;
	}

	public String[] getSelected_depotnames()
	{
		logging.debug(this, "getSelected_depotnames  "  + main.getSelectedDepots());
		return main.getSelectedDepots();
	}

	public String[] getSelected_depotIPs()
	{
		logging.debug(this, "getSelected_depotIPs "  + main.getSelectedDepots());
		String[] depotnames = new String[main.getSelectedDepots().length];
		System.arraycopy( main.getSelectedDepots(), 0, depotnames, 0, main.getSelectedDepots().length );
		if (depotnames != null) logging.debug(this, "getSelected_depotIPs depotnames != null ");
		else logging.debug(this, "getSelected_depotIPs depotnames == null ");
		String[] depotIPs = new String[depotnames.length];
		int counter = 0;
		for (String name : depotnames)
		{
			String depotip = ((String) main.getPersistenceController().getHostInfoCollections().getDepots().get(name).get(HostInfo.clientIpAddressKEY));
			logging.info(this, "getSelected_depotIPs host " + name + " depotip " + depotip);
			if (depotip != null)
			{
				depotIPs[counter] = depotip;
				counter++;
			}
		}
		return depotIPs;
	}
}