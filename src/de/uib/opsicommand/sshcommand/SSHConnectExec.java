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
	SSHConnectionOutputDialog outputDialog = null;
	private boolean multiCommand = false;
	ActionListener killProcessListener;
	public SSHConnectExec(SSHCommand sshcommand,  SSHConnectionOutputDialog dia)
	{
		this(null, sshcommand, dia);
	}
	public SSHConnectExec(ConfigedMain m, SSHCommand sshcommand)
	{
		this(m, sshcommand, null);
	}
	public SSHConnectExec(ConfigedMain m, SSHCommand sshcommand, SSHConnectionOutputDialog dia)
	{
		super(m);
		main = m;
		if (dia != null) setDialog(dia);
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
				if (outputDialog != null) outputDialog.append(configed.getResourceValue("SSHConnection.Exec.cannotAsReadonly"));
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
	public SSHConnectExec(SSHCommand sshcommand)
	{
		this(null, sshcommand);
	}
	public SSHConnectExec()
	{
		super(null);
		connect();
	}


	public SSHConnectionOutputDialog getDialog()
	{
		if (outputDialog != null)
			return outputDialog;
		return null;
	}
	public void setDialog(SSHConnectionOutputDialog dia)
	{
		outputDialog = dia;
	}

	public void exec_template(SSHCommand_Template command)
	{
		exec_list(command, true, null, true, true);
	}
	public void exec_template(SSHCommand_Template command, boolean sequential)
	{
		exec_list(command, true, null, sequential, true);
	}
	
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
		SSHConnectionExecDialog multiDialog = null;
		if (dialog != null) multiDialog = dialog;
		else multiDialog = new SSHConnectionExecDialog();
		outputDialog = multiDialog;
		final SSHConnectionExecDialog final_dia = multiDialog;
		
		final_dia.append(setAsInfoString(configed.getResourceValue("SSHConnection.Exec.dialog.commandlist") + "\n"));
		String defaultCommandsString = "";
		int anzahlCommands = ((SSHCommand_Template) commands).getOriginalCommands().size();
		for (int i=0; i< anzahlCommands; i++)
		{
			String com = ((SSHCommand_Template) commands).getOriginalCommands().get(i).getCommandRaw();
			if (i == anzahlCommands-1) defaultCommandsString = defaultCommandsString + com;
			else defaultCommandsString = defaultCommandsString +  com +  "   \n";
		}
		final_dia.append(setAsInfoString(defaultCommandsString + "\n"));
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
					for (SSHCommand co :  commandToExec.getCommands())
					{
						if (! found_error)
						{
							SSHCommand defaultCommand = co;
							pmethodHandler.parseParameter(co, caller);
							//co =  pmethodHandler.parseParameter(co, caller); ???????? sollte hier eigentlich stehen?! # nein! co wird vom phander verändert
							if (!pmethodHandler.canceled)
								exec(co, withGui, final_dia, sequential, rememberPw);
							else found_error=true;
						}
					}
					if (found_error) final_dia.append("\n" + configed.getResourceValue("SSHConnection.Exec.exitClosed"));
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
	// // private SSHCommand checkForParameter(SSHCommand command, final SSHConnectionOutputDialog fdia)
	// // {
	// // 	ArrayList<SSHCommand> commands = new ArrayList<SSHCommand>();
	// // 	commands.add(command);
	// // 	return checkForParameter(commands, fdia);
	// // 	return command;
	// // }
	// // private void checkForParameter(ArrayList<SSHCommand> commands, final SSHConnectionOutputDialog fdia)
	// private SSHCommand checkForParameter(SSHCommand command, final SSHConnectionOutputDialog fdia)
	// {
	// 	logging.info(this, "\n\n");
	// 	logging.info(this, "checkForParameter start ");
	// 	logging.info(this, "checkForParameter command: " + command.getCommand());
	// 	final SSHCommandParameterMethods pmethodHandler = SSHCommandFactory.getInstance(main).getParameterHandler();
	// 	final SSHConnectExec caller = this;
	// 	// new Thread()
	// 	// {
	// 	// 	public void run()
	// 	// 	{
	// 			pmethodHandler.canceled = false;
	// 			boolean found_error = false;
	// 			// for (SSHCommand co :  commands)
	// 			{
	// 				if (! found_error)
	// 				{
	// 					SSHCommand defaultCommand = command;
	// 					pmethodHandler.parseParameter(command, caller);
	// 					//co =  pmethodHandler.parseParameter(co, caller); ???????? sollte hier eigentlich stehen?! # nein! co wird vom phander verändert
	// 					command = pmethodHandler.parseParameter(command, caller);
	// 					System.out.println("===============================");
	// 					System.out.println("command: " + command.getCommand());
	// 					System.out.println("===============================");
	// 					if (!pmethodHandler.canceled)
	// 					{
	// 						// // exec(co, withGui, final_dia, sequential, rememberPw);
	// 						return command;
	// 					}
	// 					else found_error=true;
	// 				}
	// 			}
	// 			if (found_error) fdia.append("\n" + configed.getResourceValue("SSHConnection.Exec.exitClosed"));
	// 			// if (SSHCommandFactory.getInstance(main).ssh_always_exec_in_background)
	// 			// 	fdia.setVisible(true);
	// 	// 	}
	// 	// } .start();
	// 	logging.info(this, "\n\n");
	// 	logging.info(this, "checkForParameter command: " + command.getCommand());
	// 	logging.info(this, "checkForParameter end ");
	// 	return command;
	// }
	
	boolean FOUND_ERROR = false;

	public String exec(SSHCommand command)
	{ return exec(command, true, null, false, false); }
	public String exec(SSHCommand command, boolean withGui) 
	{ return exec(command, withGui, null, false, false); }
	public String exec(SSHCommand command, boolean withGui, SSHConnectionOutputDialog dialog)
	{ return exec(command, withGui, dialog, false, false); }
	
	public String exec(SSHCommand command, boolean withGui, SSHConnectionOutputDialog dialog, boolean sequential, boolean rememberPw) 
	{
		WaitCursor waitCursor = null;
		// command = checkForParameter(command, dialog);

		if (FOUND_ERROR)
		{
			logging.warning(this, "exec found error.");
			return "ERROR";	
		} 
		logging.info(this, "exec command " + command.getCommand());
		logging.debug(this, "exec withGui " + withGui);
		logging.debug(this, "exec dialog " + dialog);
		logging.debug(this, "exec isConnected " + isConnected());
		
		if (!(isConnected())) connect(command);
		
		if (withGui) 
		{
			if (dialog != null) outputDialog = dialog;
			else  
			{
				outputDialog = new SSHConnectionExecDialog(command);
			}
			if (SSHCommandFactory.getInstance(main).ssh_always_exec_in_background)
			{
				outputDialog.setVisible(false);
			}
			
			outputDialog.setTitle(configed.getResourceValue("SSHConnection.Exec.title") 
				+ " \"" + command.getCommand() + "\" ("+ this.user +"@"+this.host+")" );

			// if (SSHCommandFactory.getInstance(main).ssh_always_exec_in_background)
			// {
			// 	// outputDialog.pack();
			// 	outputDialog.setVisibility(true);
			// }
		}

		try 
		{	

			logging.info(this, "exec isConnected " + isConnected());
			SshCommandWorker task = new SshCommandWorker(command, outputDialog, withGui, rememberPw);
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
		SSHConnectionOutputDialog outputDialog;
		SSHConnectExec caller;

		boolean withGui;
		boolean rememberPw;
		boolean interruptChannel = false;
       		int retriedTimes = 1;
		
		SshCommandWorker(SSHCommand command, SSHConnectionOutputDialog outputDialog, boolean withGui, boolean rememberPw)
		{
			this(null, command, outputDialog, withGui, rememberPw);
		}
		SshCommandWorker(SSHConnectExec caller, SSHCommand command, SSHConnectionOutputDialog outputDialog, boolean withGui, boolean rememberPw)
		{
			this(caller, command, outputDialog, withGui, rememberPw, false);
		}

		SshCommandWorker(SSHConnectExec caller, SSHCommand command, SSHConnectionOutputDialog outputDialog, boolean withGui, boolean rememberPw, boolean interruptChannel)
		{
			super();
			if (caller != null) this.caller = caller; 
			this.command = command;
			this.outputDialog = outputDialog;
			this.withGui = withGui;
			this.rememberPw = rememberPw;
			this.interruptChannel= interruptChannel;
			retriedTimes = 1;
			// publishInfo(configed.getResourceValue("SSHConnection.Exec.dialog.commandlist") + "\n");

			// publishInfo("\n-------------------------------------------------------------------");
			publishInfo("\nexec:  " +this.command.getCommand() +"");
			publishInfo("\n---------------------------------------------------------------------------------------------------------------------------------------------------");
		}
		public boolean getInterruptedStatus()
		{
			return interruptChannel;
		}
		private void checkExitCode(int exitCode, boolean withGui, Channel channel)
		{
			String s = "\ncheckExitCode " + exitCode; 
			logging.debug(this, "publish " + s);
			publishInfo("\n---------------------------------------------------------------------------------------------------------------------------------------------------");
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
				if (withGui)  publishError(configed.getResourceValue("SSHConnection.Exec.exitError")+configed.getResourceValue("SSHConnection.Exec.exitCode") +" "+ exitCode);
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
			if (interruptChannel)
				if (caller != null)
				{
					interruptChannel(channel);
					disconnect();
					interruptChannel=true;
					try{Thread.sleep(50);} catch(Exception ee){}
				}
			publishInfo("\n---------------------------------------------------------------------------------------------------------------------------------------------------");
			publishInfo("---------------------------------------------------------------------------------------------------------------------------------------------------");
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
				final Channel channel= getSession().openChannel("exec");
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
					((SSHConnectionExecDialog)outputDialog).btn_killProcess.removeActionListener(killProcessListener);
					((SSHConnectionExecDialog)outputDialog).btn_killProcess.addActionListener(killProcessListener);
					publishInfo("result:" + outputDialog.ansiCodeEnd);
					publish("");
					outputDialog.setStartAnsi(Color.BLACK);
				}
				supw_retriedTimes = 0;
				while(true)
				{
					while(in.available() > 0)
					{
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
					if(channel.isClosed())
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
				setDialog(outputDialog);
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
					outputDialog.setStatusFinish();
					disconnect();
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
				// outputDialog.setVisible(true);
				for (String line: chunks) 
				{
					logging.debug(this, "process " + line);
					outputDialog.append(line + "\n");
					// outputDialog.append("\n");
				}
			}
		}

		protected void publishInfo(String s)
		{
			if (outputDialog != null)
				publish(setAsInfoString(s));
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
	}
}