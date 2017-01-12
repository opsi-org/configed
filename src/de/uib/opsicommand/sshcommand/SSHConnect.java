package de.uib.opsicommand.sshcommand;
/*
 * configed - configuration editor for client work stations in opsi
 * (open pc server integration) www.opsi.org
 *
 * Copyright (C) 2000-2015 uib.de
 *
 * This program is free software; you can redistribute it 
 * and / or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * @author Anna Sucher
 * @version 1.0
 */
import com.jcraft.jsch.*;
import de.uib.configed.*;
import de.uib.configed.gui.*;
import de.uib.configed.gui.ssh.*;
import de.uib.configed.gui.GeneralFrame;


import java.awt.*;
import javax.swing.*;
import java.io.*;
import de.uib.utilities.logging.*;
import de.uib.opsicommand.sshcommand.*;
/**
* This Class creates a SSH connection to a server. 
**/
public class SSHConnect
{
	/** Hostname for server to connected with **/
	protected static String host;
	/** Username for server to connected as **/
	protected static String user;
	/** Port for server to connected as**/
	public final static String default_port = "22";
	protected static String port = default_port;
	/** Password for server and username**/
	protected static String password;
	/** If needed the sudo password **/
	protected String pw_sudo;
	/** If needed the root password **/
	protected String pw_root;
	private JSch jsch= null;  
	protected Session session = null;
	protected static ConfigedMain main;
	

	/**
	* Instanz for SSH connection {@link de.uib.configed.ConfigedMain}
	* @param main configed main class
	**/
	public SSHConnect(ConfigedMain main)
	{
		this.main = main;
		// if (main.SSHKEY != null)
		// 	useKeyfile = true;
	}
	
	/**
	* Check if result is not an error.
	* @param result Result
	* @return True - if result is not an error
	**/
	protected boolean isNotError(String result)
	{
		if (result.compareTo("error") == 0)
			return false;
		return true;
	}

	/**
	* Shows a message to the user.
	* @param msg Message
	**/
	protected void showMessage(String msg)
	{
		JOptionPane.showMessageDialog(null, msg);
		logging.info(this, "show message: " + msg);
	}
	/**
	* Test if already connected.
	* @return True - if connected
	**/
	protected boolean isConnected()
	{
		logging.info(this, "isConnected session" + session);
		if (session != null)
		{
			logging.info(this, "isConnected session.isConnected " + session.isConnected());
			if (session.isConnected()) return true;
		}
		return false;
	}

	/**
	* Calls {@link getSudoPass(Component)} with null.
	**/
	protected String getSudoPass()
	{		
		return getSudoPass(null);
	}
	/**
	* If newConfirmDialog is false and sudo password already given return sudo password. Overwise calls {@link getSudoPass(Component)}.
	* @param dialog 
	* @param newConfirmDialog true for entering new sudo password 
	**/
	protected String getSudoPass(Component dialog, boolean rememberPw)
	{
		logging.debug(this, "getSudoPass dialog " + dialog + " newConfirmDialog " + rememberPw);
		if ((rememberPw) && (pw_sudo != null)) return pw_sudo;
		return getSudoPass(dialog);
	}
	/**
	* Opens a confirm dialog for entering the sudo password.
	* @param dialog
	**/
	protected String getSudoPass(Component dialog)
	{
		if (dialog == null) dialog = de.uib.configed.Globals.mainFrame;
		logging.debug(this, "getSudoPass dialog " + dialog);
		final JPasswordField passwordField=new JPasswordField(10);
		passwordField.setEchoChar('*');
		final JOptionPane opPane = new JOptionPane(
			new Object[] {
				new JLabel(configed.getResourceValue("SSHConnection.sudoPassw1") ), 
				new JLabel(configed.getResourceValue("SSHConnection.sudoPassw2") ), 
				passwordField
			}, 
			JOptionPane.QUESTION_MESSAGE,
			JOptionPane.OK_CANCEL_OPTION)
		{
			@Override
			public void selectInitialValue()
			{
				super.selectInitialValue();
				((Component) passwordField).requestFocusInWindow();
			}
		};
		final JDialog jdialog = opPane.createDialog(dialog, configed.getResourceValue("SSHConnection.Config.jLabelPassword"));
		jdialog.setVisible(true);
		logging.debug(this, "getSudoPass joptiontype value " + opPane.getValue());
		logging.debug(this, "getSudoPass joptiontype ok option " + JOptionPane.OK_OPTION);
		if ( ((Integer)opPane.getValue()) == JOptionPane.OK_OPTION)
		{
			pw_sudo = String.valueOf(passwordField.getPassword());
			return pw_sudo;
		}
		
		return null;

	}

	/**
	* Check authentification data. If null pbjects use login data from configedmain.
	**/
	public static void checkUserData()
	{
		// logging.info(this, "checkUserData " + "");
		if (host == null) host=main.HOST;
		if (port == null) port = default_port;
		if (user == null) user=main.USER;
		if ((password == null) && (!useKeyfile)) password=main.PASSWORD;
		else if (password == null) password = "";
		// if (keyfilepath == null) keyfilepath=main.SSHKEY;
		// logging.debug(this, "checkUserData user " + user + " host "+ host);
	}
	/**
	* Sets authentificationsdata. 
	* @param h Hostname
	* @param u Username
	* @param ps Password
	**/
	public static void setUserData(String h, String u, String ps, String p)
	{
		host = h;
		port = p;
		user = u;
		password = ps;
		checkUserData();
	}

	public static void setPw(String ps)
	{
		password = ps;
		checkUserData();
	}
	/**
	* @return String of connected host
	*/
	public boolean isConnectedViaKeyfile()
	{
		return useKeyfile;
	}
	public String getConnectedKeyfile()
	{
		return keyfilepath;
	}
	public String getConnectedPassphrase()
	{
		return keyfilepassphrase;
	}
	public String getConnectedHost()
	{
		checkUserData();
		return host;
	}

	/**
	* @return String of connected user
	*/
	public static String getConnectedUser()
	{
		checkUserData();
		return user;
	}

	/**
	* @return String of connected port
	*/
	public static String getConnectedPort()
	{
		return port;
	}

	/**
	* @return String of connected password
	*/
	public static String getConnectedPw()
	{
		checkUserData();
		return password;
	}
	protected static boolean useKeyfile;
	protected static String keyfilepath = "";
	protected static String keyfilepassphrase = "";
	public static void useKeyfile(boolean v)
	{ useKeyfile(v, null, null); }
	public static void useKeyfile(boolean v, String k)
	{ useKeyfile(v, k, null); }
	public static void useKeyfile(boolean v, String k, String p)
	{
		useKeyfile = v;
		keyfilepath = k;
		keyfilepassphrase = p;
		logging.info("useKeyfile " + v + " now keyfilepath " + keyfilepath);
	}
	/**
	* Calls {@link connect(SSHCommand)} with null command.
	**/
	public void connect()
	{
		logging.info(this, "connect " + "null");
		connect(null);
	}

	public boolean  connectTest()
	{
		return connect(new Empty_Command("test", "test", "", false));
	}
	/**
	* Connect to server und check if command (if given) needs root rights call {@link getRootPassword(Component)}.
	* @param command Command
	* @return True - if successful
	**/
	public boolean connect(SSHCommand command)
	{
		logging.info(this, "================================================");
		logging.info(this, "================================================");
		logging.info(this, "================================================");
		logging.info(this, "================================================");
		logging.info(this, "================================================");
		logging.info(this, "================================================");
		logging.info(this, "================================================");
		logging.info(this, "================================================");
		logging.info(this, "================================================");
		logging.info(this, "================================================");
		logging.info(this, "================================================");
		logging.info(this, "================================================");
		if (command != null)
			logging.info(this, "connect command " + command.getMenuText() );
		else logging.info(this, "connect command null" );
		try
		{
			jsch=new JSch(); 
			checkUserData();
			logging.info(this, "connect this.host " + this.host);
			logging.info(this, "connect this.user " + this.user);

			if (this.useKeyfile)
			{
				if (keyfilepassphrase != "")
					jsch.addIdentity(this.keyfilepath, this.keyfilepassphrase);
				jsch.addIdentity(this.keyfilepath);
				logging.info(this, "connect this.keyfilepath " + this.keyfilepath);
				logging.info(this, "connect useKeyfile " + this.useKeyfile + " addIdentity " + this.keyfilepath);
				session = jsch.getSession(this.user,this.host, Integer.valueOf(this.port));
			}
			else
			{
				session = jsch.getSession(this.user,this.host, Integer.valueOf(this.port));
				logging.info(this, "connect this.password ***confidential*** " );
				session.setPassword(this.password);
				logging.info(this, "connect useKeyfile " + useKeyfile + " use password …");
			}
			session.setConfig("StrictHostKeyChecking", "no");
			session.connect();
			logging.info(this, "connect " + this.user + "@" + this.host);
			return true;
		}
		catch (com.jcraft.jsch.JSchException authfail)
		{
			retriedTimes_auth =  retry(retriedTimes_auth, authfail);
			if (retriedTimes_auth >=3 ) 
			{
				logging.warning(this, "connect Authentication failed. " + authfail);
				return false;
			}else connect(command);
		}
		catch (Exception e)
		{
			retriedTimes_jschex =  retry(retriedTimes_jschex, e);
			if (retriedTimes_jschex >=3 ) 
			{
				logging.warning(this, "connect error: " + e);
				return false;
			}
			else connect(command);
		}
		return false;
	}
	private int retry(int retriedTimes, Exception e)
	{
		if (retriedTimes >=3 )
		{
			retriedTimes=1;
			// logging.error(this, "connect Exception " + e);
			logging.warning(this, "" + e.getStackTrace());
			e.printStackTrace();
		}
		else
		{
			logging.warning(this, "seems to be a session exception " + e.getStackTrace());
			retriedTimes= retriedTimes+1;	
		}
		return retriedTimes;
	}
	private int retriedTimes_jschex = 1;
	private int retriedTimes_auth = 1;

	/**
	* Get current session
	* @return the current jsch.session
	*/
	protected Session getSession()
	{
		logging.info(this, "getSession " + session);
		return session;
	}

	public void interruptChannel(Channel _channel)
	{
		interruptChannel(_channel, true);
	}
	// http://stackoverflow.com/questions/22476506/kill-process-before-disconnecting
	public void interruptChannel(Channel _channel, boolean kill)
	{
		try
		{
			logging.info(this, "interruptChannel _channel " + _channel);
			_channel.sendSignal("2");
			if (kill) _channel.sendSignal("9");
			logging.info(this, "interrupted");
		}
		catch (Exception e)
		{
			logging.error("Failed interrupting channel", e);
		}
	}

	/**
	* Disconnect from server.
	**/
	public void disconnect()
	{
		if (isConnected())
			session.disconnect();
		logging.debug(this, "disconnect");
	}
}
