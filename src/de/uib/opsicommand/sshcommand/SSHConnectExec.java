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
import de.uib.opsicommand.*;
import com.jcraft.jsch.*;
import de.uib.configed.*;
import de.uib.configed.gui.*;
import de.uib.configed.gui.ssh.*;
import de.uib.utilities.*;
import de.uib.utilities.thread.WaitCursor;
import de.uib.utilities.logging.*;

import java.awt.*;
import java.io.*;
import javax.swing.*;
import java.util.*;
import java.awt.event.*;

/**
* 
* @inheritDoc
* Class for executing commands.
*/
public class SSHConnectExec extends SSHConnect
{
	protected SSHConnectionExecDialog outputDialog = null;
	protected boolean multiCommand = false;
	protected ActionListener killProcessListener;
	public SSHConnectExec(SSHCommand sshcommand )
	{
		this(null, sshcommand);
	}
	public SSHConnectExec(ConfigedMain m )
	{
		super(m);
		FOUND_ERROR = false;
		main = m;
		// setDialog(SSHConnectionExecDialog.getInstance());
	}
	public SSHConnectExec(ConfigedMain m, SSHCommand sshcommand)
	{
		super(m);
		FOUND_ERROR = false;
		main = m;
		setDialog(SSHConnectionExecDialog.getInstance());
		if (main != null) connect(sshcommand);
		logging.info(this, "SSHConnectExec main " + main );
		logging.info(this, "SSHConnectExec sshcommand " + sshcommand.getCommand());
		starting(sshcommand);
	}
	public void starting(SSHCommand sshcommand)
	{
		if (!(isConnected())) connect(sshcommand);
		try 
		{
			if (!(de.uib.configed.Globals.isGlobalReadOnly()))
			{
				if (sshcommand instanceof SSHCommand_Template) exec_template((SSHCommand_Template) sshcommand);
				else if (sshcommand.isMultiCommand()) exec_list((SSHMultiCommand)sshcommand);
				else exec(sshcommand); 
			}
			else
			{
				logging.warning(this, configed.getResourceValue("SSHConnection.Exec.cannotAsReadonly"));
				if (outputDialog != null) outputDialog.append("[" + sshcommand.getId() + "] \t", configed.getResourceValue("SSHConnection.Exec.cannotAsReadonly"));
			}
		}
		catch (Exception e) 
		{
			logging.error(this, "SSHConnectExec Exception " +e);
			e.printStackTrace();
		}
		finally
		{
			disconnect();
			System.gc();
		}
	}
	// public SSHConnectExec(SSHCommand sshcommand)
	// {
	// 	this(null, sshcommand);
	// }
	public SSHConnectExec()
	{
		super(null);
		connect();
	}


	public SSHConnectionExecDialog getDialog()
	{
		if (outputDialog != null)
			return outputDialog;
		return null;
	}
	public void setDialog(SSHConnectionExecDialog dia)
	{
		outputDialog = dia;
	}

	public void exec_template(SSHCommand_Template command)
	{
		exec_list(command, true, null, true, true);
	}
	
	public void exec_template(SSHCommand_Template command, SSHConnectionExecDialog dia, boolean sequential)
	{
		exec_list(command, true, dia, sequential, true);
	}
	
	public void exec_template(SSHCommand_Template command, boolean sequential)
	{
		exec_list(command, true, null, sequential, true);
	}
	protected boolean interruptChannel = false;
	public void exec_list(SSHMultiCommand commands) 
	{ exec_list(commands, true, null, false, true); }
	public void exec_list(SSHMultiCommand commands, boolean sequential)
	{ exec_list(commands, true, null, sequential, true); }
	public void exec_list(SSHMultiCommand commands,SSHConnectionExecDialog dialog, boolean sequential)
	{ exec_list(commands, true, dialog, sequential, true); }
	public void exec_list(final SSHMultiCommand commands,final boolean withGui, SSHConnectionExecDialog dialog,final boolean sequential,final boolean rememberPw)
	{
		logging.debug(this, "exec_list commands[" + ((SSHCommand)commands).getId() + "] withGui[" + withGui + "] sequential[" + sequential + "] dialog[" + dialog + "]");
		multiCommand = true;
		interruptChannel = false;
		commandInfoName = commands.getMainName();
		SSHConnectionExecDialog multiDialog = null;
		if (dialog != null)
		{
			logging.debug(this, "exec_list, take given dialog");
			multiDialog = dialog;
		}
		else 
		{
			logging.debug(this, "exec_list, create SSHConnectionExecDialog");
			multiDialog = SSHConnectionExecDialog.getInstance();
		}
		outputDialog = multiDialog;
		final SSHConnectionExecDialog final_dia = multiDialog;
		
		String defaultCommandsString = "";
		int anzahlCommands = ((SSHCommand_Template) commands).getOriginalCommands().size();
		// final_dia.append("["+ configed.getResourceValue("SSHConnection.Exec.dialog.commandlist").trim() +"]     ", "");
		for (int i=0; i< anzahlCommands; i++)
		{
			String com = ((SSHCommand_Template) commands).getOriginalCommands().get(i).getCommandRaw();
			// if (i == anzahlCommands-1) 
			// {
			// // 	final_dia.append("                 ", com);
				// defaultCommandsString = defaultCommandsString + com;
			// }
			// else 
			// {
			// 	// final_dia.append("                 ", com + " \n");
				defaultCommandsString = defaultCommandsString +  com +  "   \n";
			// }
		}
		final_dia.append("["+ configed.getResourceValue("SSHConnection.Exec.dialog.commandlist").trim() +"]     ", setAsInfoString( "" + defaultCommandsString + "\n\n"));
		if (SSHCommandFactory.getInstance(main).ssh_always_exec_in_background)
		{
			multiDialog.setVisible(false);
			final_dia.setVisible(false);
		}
		try {

			final SSHMultiCommand commandToExec = commands;
			logging.info(this, "exec_list command " + commands);
			logging.info(this, "exec_list commandToExec " + commandToExec);
			final SSHCommandParameterMethods pmethodHandler = SSHCommandFactory.getInstance(main).getParameterHandler();
			final SSHConnectExec caller = this;
			FOUND_ERROR = false;
			new Thread()
			{
				public void run()
				{
					pmethodHandler.canceled = false;
					boolean found_error = false;
					LinkedList<SSHCommand> commands_list = (LinkedList) commandToExec.getCommands();
					for (SSHCommand co :  commandToExec.getCommands())
					{
						if (! found_error)
						{
							SSHCommand defaultCommand = co;
							// pmethodHandler.parseParameter(co, caller);
							co =  pmethodHandler.parseParameter(co, caller); //???????? sollte hier eigentlich stehen?! # nein! co wird vom phander verändert
							if (!pmethodHandler.canceled)
							{
								if (co instanceof SSHSFTPCommand)
								{
									SSHConnectSCP sftp = new SSHConnectSCP(commandInfoName);
									sftp.exec(co, withGui, final_dia, sequential, rememberPw, commands_list.indexOf(co)+1,  commands_list.size());
								}
								else exec(co, withGui, final_dia, sequential, rememberPw, commands_list.indexOf(co)+1,  commands_list.size());
							}
							else found_error=true;
						}
					}
					if (found_error) 
					{
						final_dia.append("[" + configed.getResourceValue("SSHConnection.Exec.dialog.commandlist") + "]     ", 
						"" + configed.getResourceValue("SSHConnection.Exec.exitClosed"));
					}
					if (SSHCommandFactory.getInstance(main).ssh_always_exec_in_background)
						final_dia.setVisible(true);
				}
			} .start();
			logging.info(this, "exec_list command " + commands);
			logging.info(this, "exec_list commandToExec " + commandToExec);
			System.gc();
		} catch (Exception e) 
		{ logging.warning("exception: " + e);}
		finally
		{
			disconnect();
			System.gc();
		}
	}
	// protected String commandInfoName = null;
	protected boolean FOUND_ERROR = false;
	public String exec(SSHCommand command)
	{ return exec(command, true, null, false, false); }
	public String exec(SSHCommand command, boolean withGui) 
	{
		FOUND_ERROR = false; 
		return exec(command, withGui, null, false, false);
	}
	public String exec(SSHCommand command, boolean withGui, SSHConnectionExecDialog dialog)
	{ return exec(command, withGui, dialog, false, false); }
	
	public String exec(SSHCommand command, boolean withGui, SSHConnectionExecDialog dialog, boolean sequential, boolean rememberPw) 
	{ return exec(command, withGui, dialog, false, false, 1, 1); }
	
	public String exec(SSHCommand command, boolean withGui, SSHConnectionExecDialog dialog, boolean sequential, boolean rememberPw, int commandnumber, int maxcommandnumber) 
	{
		WaitCursor waitCursor = null;
		// command = checkForParameter(command, dialog);

		if (FOUND_ERROR)
		{
			logging.warning(this, "exec found error.");
			return command.get_ERROR_TEXT();
		}

		logging.info(this, "exec command " + command.getSecuredCommand());
		logging.debug(this, "exec withGui " + withGui);
		logging.debug(this, "exec dialog " + dialog);
		logging.debug(this, "exec isConnected " + isConnected());
		
		if (!(isConnected())) connect(command);
		
		if (withGui) 
		{
			if (dialog != null) outputDialog = dialog;
			else  outputDialog = SSHConnectionExecDialog.getInstance();

			if (SSHCommandFactory.getInstance(main).ssh_always_exec_in_background)
				outputDialog.setVisible(false);
			// outputDialog.append(getConnectedUser() + "@" + getConnectedHost() + "\n");
			outputDialog.setTitle(
				configed.getResourceValue("SSHConnection.Exec.title") + " "
				+ configed.getResourceValue("SSHConnection.Exec.dialog.commandoutput")
				+ "  ("+ this.user +"@"+this.host+")" );

		}
		else outputDialog = null;

		try 
		{	

			logging.info(this, "exec isConnected " + isConnected());
			SshCommandWorker task = new SshCommandWorker(command, outputDialog, withGui, rememberPw);
			task.setMaxCommandNumber(maxcommandnumber);
			task.setCommandNumber(commandnumber);
			task.execute();
			logging.info(this, "execute was called");

			if (sequential) return task.get();
			if (SSHCommandFactory.getInstance(main).ssh_always_exec_in_background)
				if (!multiCommand)
					if (withGui)
					{
						outputDialog.setVisible(false);
					}
			System.gc();
			if (withGui) return "finish";
			else return task.get();
		} 
		catch (java.lang.NullPointerException npe) 
		{ 
			logging.error(this, "exec NullPointerException " + npe);
			npe.printStackTrace();
		}
		catch (Exception e) 
		{ 
			logging.error(this, "exec Exception " + e);
			e.printStackTrace();
		}
		return null;
	}
	// @Override
	// public void testConnection(String user, String host)
	// {
	// 	System.out.println("///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////");
	// 	System.out.println("///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////");
	// 	System.out.println("/////////////////////////////////////////////////////////////////////////////start///////////////////////////////////////////////////////////////////////////////////////////");
	// 	System.out.println("///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////");
	// 	boolean result = connection.connectTest();
	// 	if (result)
	// 	{
	// 		logging.info(this, "testConnection connected");
	// 		connection_state = connected;

	// 		updateConnectionInfo(connection_state);
	// 	}
	// 	else if (!result)
	// 	{
	// 		logging.info(this, "testConnection not connected");
	// 		connection_state = not_connected;
	// 		updateConnectionInfo(connection_state);
	// 		logging.warning(this, "cannot connect to " + user + "@" + host);
	// 	}
	// 	else
	// 	{
	// 		connection_state = unknown;
	// 		updateConnectionInfo(connection_state);
	// 	}
	// 	logging.info(this, "testConnection connection state " + connection_state);
	// 	System.out.println("///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////");
	// 	System.out.println("///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////");
	// 	System.out.println("////////////////////////////////////////////////////////////////////////////end//////////////////////////////////////////////////////////////////////////////////////////////");
	// 	System.out.println("///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////");
	// }
	protected String setAsInfoString(String s)
	{
		if (outputDialog != null)
			if (s.length() > 0)
				if (s != "\n")
					return outputDialog.ansiCodeInfo+ s;
		return  s;
	}
	
	private class SshCommandWorker extends SwingWorker<String, String> 
	//first parameter class is return type of doInBackground
	//second is element type of the list which is used by process
	{
		SSHCommand command ;
		SSHConnectionExecDialog outputDialog;
		SSHConnectExec caller;

		boolean withGui;
		boolean rememberPw;
		boolean interruptChannelWorker = false;
       		int retriedTimes = 1;
       		int command_number = -1;
       		int max_command_number = -1;
		
		SshCommandWorker(SSHCommand command, SSHConnectionExecDialog outputDialog, boolean withGui, boolean rememberPw)
		{
			this(null, command, outputDialog, withGui, rememberPw);
		}
		SshCommandWorker(SSHConnectExec caller, SSHCommand command, SSHConnectionExecDialog outputDialog, boolean withGui, boolean rememberPw)
		{
			this(caller, command, outputDialog, withGui, rememberPw, false);
		}

		SshCommandWorker(SSHConnectExec caller, SSHCommand command, SSHConnectionExecDialog outputDialog, boolean withGui, boolean rememberPw, boolean interruptChannel)
		{
			super();
			if (caller != null) this.caller = caller; 
			this.command = command;
			this.outputDialog = outputDialog;
			this.withGui = withGui;
			if (!withGui) this.outputDialog = null;
			this.rememberPw = rememberPw;
			this.interruptChannelWorker= interruptChannel;
			retriedTimes = 1;
			// publishInfo(configed.getResourceValue("SSHConnection.Exec.dialog.commandlist") + "\n");

			// publishInfo("\n-------------------------------------------------------------------");
			publishInfo("---------------------------------------------------------------------------------------------------------------------------------------------------");
			publishInfo("exec:  " +this.command.getSecuredCommand());
		}
		public void setMaxCommandNumber(int mc)
		{
			this.max_command_number = mc;
		}
		public void setCommandNumber(int cn)
		{
			this.command_number = cn;
		}

		public boolean getInterruptedStatus()
		{
			return interruptChannelWorker;
		}
		private void checkExitCode(int exitCode, boolean withGui, Channel channel)
		{
			String s = "checkExitCode " + exitCode; 
			logging.debug(this, "publish " + s);
			publishInfo("---------------------------------------------------------------------------------------------------------------------------------------------------");
			if (this.command_number != -1 && this.max_command_number != -1)
				publishInfo(configed.getResourceValue("SSHConnection.Exec.commandcountertext")
					.replace("xX0Xx", Integer.toString(this.command_number))
					.replace("xX1Xx", Integer.toString(this.max_command_number)));
			publishInfo(s);
			if (exitCode == 127) 
			{
				logging.info(this, "exec exit code 127 (command does not exists).");
				logging.debug(configed.getResourceValue("SSHConnection.Exec.exit127"));
				if (withGui)
				{
					publishError(configed.getResourceValue("SSHConnection.Exec.exit127"));
					logging.info(this, "2. publish");
				}
			}
			else if (exitCode != 0) 
			{
				FOUND_ERROR = true;
				logging.info(this, "exec exit code " + exitCode + ".");
				logging.debug(this, configed.getResourceValue("SSHConnection.Exec.exitError")+configed.getResourceValue("SSHConnection.Exec.exitCode") +" "+ exitCode);
				if (withGui)  
				{
					publishError(configed.getResourceValue("SSHConnection.Exec.exitError")+configed.getResourceValue("SSHConnection.Exec.exitCode") +" "+ exitCode);
				}
			}
			else if (exitCode == 0)
			{
				logging.info(this, "exec exit code 0");
				logging.debug(this, configed.getResourceValue("SSHConnection.Exec.exitNoError"));
				logging.debug(this, configed.getResourceValue("SSHConnection.Exec.exitPlsCheck"));
				if (withGui) 
				{
					publishInfo(configed.getResourceValue("SSHConnection.Exec.exitNoError"));
					publishInfo(configed.getResourceValue("SSHConnection.Exec.exitPlsCheck"));
				}
			} else 
			{
				FOUND_ERROR = true;
				logging.debug(this, configed.getResourceValue("SSHConnection.Exec.exitUnknown"));
				logging.debug(this, configed.getResourceValue("SSHConnection.Exec.exitPlsCheck"));
				if (withGui) 
				{
					publishError(configed.getResourceValue("SSHConnection.Exec.exitUnknown"));
					publishError(configed.getResourceValue("SSHConnection.Exec.exitPlsCheck"));
				}
			}
			if (interruptChannelWorker)
				if (caller != null)
				{
					interruptChannel(channel);
					disconnect();
					interruptChannel=true;
					interruptChannelWorker=true;
					try{Thread.sleep(50);} catch(Exception ee){}
				}
			// publishInfo("---------------------------------------------------------------------------------------------------------------------------------------------------");
			// publishInfo("---------------------------------------------------------------------------------------------------------------------------------------------------");
			publishInfo("---------------------------------------------------------------------------------------------------------------------------------------------------");
		}
		
		boolean pwsuccess = false;
		int supw_retriedTimes = 0;
		
		@Override
		public String doInBackground() throws java.net.SocketException
		{
			StringBuffer buf = new StringBuffer();
			try
			{
				logging.info(this, "doInBackground getSession " + getSession());
				
				if (!(isConnected())) connect();
				final Channel channel = getSession().openChannel("exec");
				// if (! (((String)command.getCommand().trim()).endsWith("&")))
					// ((ChannelExec)channel).setPty(true);
				((ChannelExec)channel).setErrStream(System.err);
				((ChannelExec)channel).setCommand(command.getCommand());
				final OutputStream out = channel.getOutputStream();	
				final InputStream in = channel.getInputStream();
				channel.connect();
				killProcessListener = new ActionListener()
				{
					public void actionPerformed(ActionEvent e)
					{
						interruptChannel(channel);
						disconnect();
						interruptChannel=true;
						interruptChannelWorker=true;
						try{Thread.sleep(50);} catch(Exception ee){}
					}
				};
				
				logging.info(this, "doInBackground start waiting for answer");
				int size = 1024*1024;
				// int size = 500;
				byte[] tmp = new byte[size];
				int progress = 0;
				if (outputDialog != null)
				{
					((SSHConnectionExecDialog)outputDialog).removeKillProcessListener(killProcessListener);
					((SSHConnectionExecDialog)outputDialog).addKillProcessListener(killProcessListener);
					// publishInfo("result:" + outputDialog.ansiCodeEnd);
					// publish("");
					// outputDialog.setStartAnsi(Color.BLACK);
				}
				supw_retriedTimes = 0;
				while(true)
				{
					while(in.available() > 0)
					{
						// outputDialog.setStartAnsi(Color.BLACK);
						int i = in.read(tmp, 0, size);
						logging.debug(this,"doInBackground i " + i);
						if (i<0)break;
						String str = new String(tmp, 0, i, "UTF-8");
						// if ( (command.needSudo() ) && (str.equals(configed.getResourceValue("SSHConnection.sudoFailedText"))) )
						// if ( (command.needSudo() ) && (str.equals(SSHCommandFactory.getInstance().sudo_failed_text)) )
						if ( (command.needSudo() ) &&  (str.contains(SSHCommandFactory.getInstance().sudo_failed_text)) )
						{
							String pw = "";
							if (supw_retriedTimes >= 1)
								pw = getSudoPass(outputDialog);
							else
								pw = getSudoPass(outputDialog, rememberPw);

							if (pw == null)
							{
								logging.info(this, "exec ready (1)");
								FOUND_ERROR = true;
								publish(configed.getResourceValue("SSHConnection.Exec.exitClosed"));
								if (outputDialog != null) 
									outputDialog.setStatusFinish();
								return null;
							}
							else 
							{
								out.write((pw + "\n").getBytes());
								out.flush();
								supw_retriedTimes += 1; 
							}
						}
						if (withGui) 
						{

							for (String line : str.split("\n"))
							{
								// outputDialog.setStartAnsi(Color.BLACK);
								logging.debug(this, " doInBackground publish " + progress + ": " + line);
								publish(new String(line));
								progress++;
								try{Thread.sleep(50);} catch(Exception ee){}
							}
						}
						else {
							// System.out.println("withgui else in forloop");
							for (String line : str.split("\n"))
								logging.debug(this, "line: " + line);
						}
						buf.append(str);
					}
					if(channel.isClosed() || interruptChannel ||  interruptChannelWorker)
					{
						if ((in.available()>0) && (!interruptChannel)) continue; 
						checkExitCode(channel.getExitStatus(), withGui, channel);
						if (channel.getExitStatus() != 0) 
						{
							logging.info(this, "exec ready (2)");
							FOUND_ERROR = true;
							if (outputDialog != null) outputDialog.setStatusFinish();
							return null;
						}
						break;
					}
				}
				try{Thread.sleep(1000);}
				catch(Exception ee){}
				if (outputDialog != null) setDialog(outputDialog);
				logging.info(this, "exec ready (0)");
			}

			catch(JSchException jschex)
			{
				if (retriedTimes >=3 )
				{
					retriedTimes=1;
					logging.warning(this, "jsch exception " + jschex );
					logging.logTrace(jschex);
					publishError(jschex.toString());
					return "";
				}
				else
				{
					logging.warning(this, "jsch exception " + jschex );
					retriedTimes= retriedTimes+1;
					connect();
					doInBackground();
				}
			}
			catch(IOException ex)
			{
				logging.warning(this, "SSH IOException " + ex);
				logging.logTrace(ex);
				publishError(ex.toString());
			}
			catch(Exception e)
			{
				logging.warning(this, "SSH Exception " + e);
				logging.logTrace(e);
				publishError(e.getMessage());
			}
			if (outputDialog != null) 
				if (!multiCommand)
				{
					outputDialog.setStatusFinish(getCommandName());
					// disconnect();
				}
			System.gc();
			return buf.toString();
		}
		@Override
		protected void process(java.util.List<String> chunks) 
		{
			logging.debug(this, "chunks " + chunks.size());

			if (outputDialog != null)
			{
				outputDialog.setStartAnsi(Color.BLACK);
				// outputDialog.setVisible(true);
				for (String line: chunks) 
				{
					logging.debug(this, "process " + line);
					outputDialog.append(getCommandName(), line + "\n");
					// outputDialog.append("\n");
				}
			}
		}

		protected void publishInfo(String s)
		{
			if (outputDialog != null)
			{
				publish(setAsInfoString(s));
				outputDialog.setStartAnsi(Color.BLACK);
			}
		}

		protected void publishError(String s)
		{
			if (outputDialog != null)
				if (s.length() > 0)
					if (s != "\n")
						s = outputDialog.ansiCodeError + s;
			publish(s);
		}
	
		@Override
		protected void done() 
		{
			logging.info(this, "done");
		}

		private String getCommandName()
		{
			String commandinfo = "[" + this.command.getMenuText() + "]";
			if (this.command_number != -1 && this.max_command_number != -1)
				if ((commandInfoName != null) && (!commandInfoName.equals("")))
					commandinfo = "[" + commandInfoName + "("  +Integer.toString(this.command_number) + "/" + Integer.toString(this.max_command_number) + ")]";
				else 
					commandinfo = "[" + this.command.getMenuText() + "("  +Integer.toString(this.command_number) + "/" + Integer.toString(this.max_command_number) + ")]";

			return commandinfo;
		}
	}
}