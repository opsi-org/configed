package de.uib.opsicommand.sshcommand;

import com.jcraft.jsch.*;
import de.uib.configed.*;
import de.uib.configed.gui.*;
import de.uib.configed.gui.ssh.*;
import de.uib.utilities.*;
import de.uib.utilities.logging.*;

import java.awt.event.*;
import java.awt.*;
import javax.swing.*;
import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import java.util.*;

public class SSHConnectTerminal extends SSHConnect
{
	private JSch jsch= null;  
	Session session = null;
	ChannelShell channel = null;
	private SSHConnectionTerminalDialog dialog;
	private KeyListener inputKeyListener = null;
	private ActionListener connectionKeyListener = null;
	private OutputStream out = null;

	public SSHConnectTerminal(ConfigedMain main, SSHConnectionTerminalDialog dialog)
	{
		super(main);
		this.dialog = dialog;
		if (dialog == null)
			dialog = new SSHConnectionTerminalDialog(
				configed.getResourceValue("MainFrame.jMenuSSHTerminal")+ " " + this.user + "@" + this.host, 
				this,
				false /* visible = false*/
			);
		connect();
		// initListeners();
		// initInputFieldFromDialog();
		// initKillProcessButtonFromDialog();
		// dialog.setAutocompleteList(getList(getCompletionList(true, true)));
	}
	public SSHConnectTerminal(ConfigedMain main)
	{
		super(main);
		dialog = new SSHConnectionTerminalDialog(
			configed.getResourceValue("MainFrame.jMenuSSHTerminal")+ " " + this.user + "@" + this.host, 
			this,
			false /* visible = false*/
			);
		connect();
		// initListeners();
		// initInputFieldFromDialog();
		// initKillProcessButtonFromDialog();
	}
	public SSHConnectionOutputDialog getDialog()
	{
		if (dialog != null)
			return (SSHConnectionOutputDialog) dialog;
		return null;
	}
	String currentDirectory = "";
	boolean getCurrentDirectorySilent = false;
	class MyOutputPrinter extends PrintStream
	{
		SSHConnectionTerminalDialog theDialog;
		MyOutputPrinter(SSHConnectionTerminalDialog dialog, OutputStream out)
		{
			super(out);
			theDialog = dialog;
		}
		@Override
		public void write(byte[] buf, int off, int len) 
		{
			try {
				// if (getCurrentDirectorySilent)
				// {
				// 	currentDirectory = new String(buf, off, len, "UTF-8");
				// 	System.out.println("Set currentDirectory to:" + currentDirectory + ";");
				// 	if ( (!(currentDirectory.equals("p"))) && (!(currentDirectory.equals("w"))) && (!(currentDirectory.equals("d"))) && (!(currentDirectory.charAt(0) == 'd'))
				// 		&& (!(currentDirectory.equals("pw"))) && (!(currentDirectory.equals("pwd")))
				// 		)
				// 	{
				// 		getCurrentDirectorySilent = false;
				// 		// currentDirectory = currentDirectory.replace("\n", "") + "/";
				// 	}
				// }
				// else
				{
					// logging.info(this, "write ...... :");
					String str = new String(buf, off, len, "UTF-8");
					// if ((str != null) && (str.contains("\n")) )
					// {
					// 	logging.info(this, "write (1) found failured line with \"\\\\n\": ");
					// 	// str = str.replace("\n<", "");
					// }
					// if ((str != null) && (str.contains("<")) )
					// {
					// 	logging.info(this, "write (2) found failured line with \"<\": ");
					// 	// str = str.replace("<", "");
					// }
					// if ((str != null) && (str.contains("<")) && (str.contains("\n")) )
					// {
					// 	logging.info(this, "write (3) found failured line with \"\\n\" and \"<\": ");
					// 	if ( (str.length() > 2) && ( (str.charAt(len-1) == '\\') && (str.charAt(len) == 'n')) )
					// 	{
					// 		logging.info(this, "write (3.1) \"" + str  + "\"");
					// 		logging.info(this, "write (3.1) \"" + str  + "\"");
					// 	}
					// 	else
					// 	{
					// 		logging.info(this, "write (3.2)  str.charAt(len-3) \"" + str.charAt(len-3)  + "\"");
					// 		logging.info(this, "write (3.2)  str.charAt(len-2) \"" + str.charAt(len-2)  + "\"");
					// 		logging.info(this, "write (3.2)  str.charAt(len-1) \"" + str.charAt(len-1)  + "\"");
					// 		// logging.info(this, "write (3.2)  str.charAt(len) \"" + str.charAt(len)  + "\"");
					// 		logging.info(this, "write (3.2) \"" + str  + "\"");
					// 		str = str.replace("<", "<").replace("\n", "<newline>");
					// 		logging.info(this, "write (3.2) \"" + str  + "\"");
					// 	}
					// }
					// logging.info(this, "write \"" + str  + "\"");
					theDialog.append(str, theDialog.getInputField());
					// System.out.println("Set sth other " + new String(buf, off, len, "UTF-8"));
				}
			} catch  (UnsupportedEncodingException ue)
			{
				logging.warning(" UnsupportedEncodingException " + ue);
			}
		}
	}

	@Override
	public void connect()
	{
		logging.info(this, "connect ...");
		try
		{
			jsch=new JSch(); 
			checkUserData();
			session = jsch.getSession(this.user,this.host, 22);
			logging.info(this, "connect user@host " + this.user + "@" + this.host);
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
				session.setPassword(this.password);
				logging.info(this, "connect useKeyfile " + useKeyfile + " use password …");
			}
			// Do not use StrictHostKeyChecking=no. See JSch SFTP security with session.setConfig(“StrictHostKeyChecking”, “no”);.   http://stackoverflow.com/questions/30178936/jsch-sftp-security-with-session-setconfigstricthostkeychecking-no
			session.setConfig("StrictHostKeyChecking", "no"); // otherwise exception if not in knwon_hosts or unknown fingerprint
			session.connect();
			channel = (ChannelShell) session.openChannel("shell");
			
			// dialog.append("\n");
			// naechste zeile activiert den Hinweis, falls die nicht die standard bash verwendet wird, soll der befehl bash ausgefuehrt werden..
			// dialog.append(configed.getResourceValue("SSHConnection.Terminal.note") + "\n\n", dialog.getInputField());
			logging.info(this, "Connect");

			// channel.setInputStream(new FilterInputStream(System.in));
			 // a hack for MS-DOS prompt on Windows.
			// channel.setInputStream(new FilterInputStream(System.in){
			// 	public int read(byte[] b, int off, int len)throws IOException{
			// 		return in.read(b, off, (len>124?124:len));
			// 	}
			// });
	
			// channel.setOutputStream(new MyOutputPrinter(dialog, System.out));
			// out = channel.getOutputStream();
			// channel = setChannels(new FilterInputStream(System.in){
			// 	public int read(byte[] b, int off, int len)throws IOException{
			// 		return in.read(b, off, (len>124?124:len));
			// 	}
			// }),
			// new MyOutputPrinter(dialog, System.out)
			// );
			channel = setStreams(channel);

			channel.setPtyType("dumb");
			// channel.setPty(false);
			// ((ChannelShell)channel).setPty(false);

			channel.connect();
			logging.info(this, "connect " + this.user + "@" + this.host);
			dialog.setVisible(true);
			dialog.setAutocompleteList(getList(getCompletionList(true, true)));
			
			logging.info(this, "SSHConnectTerminal connected");

			initListeners();
			initInputFieldFromDialog();
			initKillProcessButtonFromDialog();

			Thread.sleep(1000);
			exec("bash\n");
		}
		catch (Exception e)
		{
			logging.error(this, "SSHConnectTerminal connect exception" + e );
		}
	}
	public final void exec(String text)
	{
		try
		{
			logging.info(this, "exec out " + out);
			logging.info(this, "exec text " + text);
			if ((out != null)  && (text.trim().length() >= 0))
			{
				SSHCommand command = new Empty_Command(text);
				String ntext = SSHCommandFactory.getInstance(main).getParameterHandler().parseParameterToString(command, this);
				out.write(ntext.getBytes());
				logging.debug(this, " exec getPrivateStatus " + dialog.getPrivateStatus());
				logging.info(this, " exec text " + text);
				logging.info(this, " exec ntext " + ntext);
				if (!(dialog.getPrivateStatus()))
				{
					dialog.setPrivate(false);
				}
				else 
				{
					logging.debug(this, " exec addToHistory " + text);
					dialog.addToHistory(text.trim());
				}
				dialog.setLastHistoryIndex();
				out.flush();
			}
			// else logging.info(this, "empty input text");
		}
		catch (IOException ioe) 
		{
			logging.error(this, "SSHConnectTerminal exec ioexception " + ioe);
			ioe.printStackTrace();
		}
		catch (Exception e)
		{
			// e.printStackTrace();
			logging.error(this, "SSHConnectTerminal exec exception ");
			e.printStackTrace();
		}
	}
	private ChannelShell setStreams(ChannelShell ch) throws IOException
	{ return setStreams(ch, false);}
	private ChannelShell setStreams(ChannelShell ch, boolean silent) throws IOException
	{
		ch.setInputStream(new FilterInputStream(System.in){
			public int read(byte[] b, int off, int len)throws IOException{
				return in.read(b, off, (len>1024?1024:len));
			}
		});
		ch.setOutputStream(new MyOutputPrinter(dialog, System.out));
		out = ch.getOutputStream();
		return ch;
	}

	private void initKillProcessButtonFromDialog() 
	{
		final SSHConnectionTerminalDialog fdia = dialog;
		logging.info(this, "initKillProcessButtonFromDialog ");
		initListeners();
		((SSHConnectionTerminalDialog)this.dialog).btn_killProcess.removeActionListener(connectionKeyListener);
		((SSHConnectionTerminalDialog)this.dialog).btn_killProcess.addActionListener(connectionKeyListener);
	}

	public void initInputFieldFromDialog()
	{
		logging.info(this, "initInputFieldFromDialog ");	
		logging.info(this, "initInputFieldFromDialog inputField " + dialog.getInputField());
		initListeners();
		dialog.getInputField().removeKeyListener(inputKeyListener);
		dialog.getInputField().addKeyListener(inputKeyListener);
	}

	public final void clear()
	{
		dialog.getOutputField().setText("");
		dialog.getInputField().setText("");
		try 
		{
			if (out != null)
				out.write("\n".getBytes());
			else logging.warning(this, "Pipe closed");
		} catch (Exception e2)
		{ e2.printStackTrace();}
	}

	private void initListeners()
	{
		connectionKeyListener = new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				logging.info(this, "interrupt with btn ");
				exec(new String(new byte[] {3}) +"\n");
			}
		};
		inputKeyListener = new KeyListener() 
		{
			@Override public void keyTyped(KeyEvent e) { }
			@Override public void keyPressed(KeyEvent e){
				if ((e.getKeyCode() == KeyEvent.VK_C) && ((e.getModifiers() & KeyEvent.CTRL_MASK) != 0)) {
					logging.info(this, "interrupt with keys ");
					exec(new String(new byte[] {3}) +"\n");
				}
			} 
			@Override
			public void keyReleased(KeyEvent e)
			{
				int key=e.getKeyCode();
				JTextField textField = (JTextField) e.getSource();
				// if (textField.getText().trim().toLowerCase().equals("exit")) { // if(key==KeyEvent.VK_ENTER) dialog.cancel(); // ((Component) textField).requestFocusInWindow(); // } // else 
				if(key==KeyEvent.VK_ENTER)
				{
					logging.info(this, "initInputFieldFromDialog keyReleased ENTER ");
					logging.info(this, "initInputFieldFromDialog inputfield " + textField);
					logging.info(this, "initInputFieldFromDialog dialog " + dialog);
					if (textField.getText().trim().toLowerCase().equals("clear")) {
						if(key==KeyEvent.VK_ENTER) clear();
						((Component) textField).requestFocusInWindow();
					}
					// else if (textField.getText().trim().toLowerCase().equals("kill")) {
					// 	exec(new String(new byte[] {3}) +"\n");
					// 	((Component) textField).requestFocusInWindow();
					// 	dialog.getInputField().setText("");
					// }
					else {
						// String text = textField.getText() + "\n";
						exec(textField.getText() + "\n");
						// if (textField.getText().contains(" cd ") || textField.getText().contains("cd "))
						// {
							// java.util.List dirs = getList(getCompletionList(false, true));
							// if (commands_compgen != null) 
							// 	if (dirs != null)
							// 	{
							// 		if (dirs.addAll(commands_compgen))
							// 			dialog.setAutocompleteList(dirs);
							// 	}
							// 	else
							// 		dialog.setAutocompleteList(commands_compgen);

							// else  dialog.setAutocompleteList(getList(getCompletionList(true, true)));
							// dialog.setAutocompleteList(getList(getCompletionList(true, false)));
						// }	
						((Component) textField).requestFocusInWindow();
						dialog.getInputField().setText("");
					}
				}
				// else if (key == KeyEvent.VK_TAB)
				// {
					
				// }
				else if ((key==KeyEvent.VK_UP) || (key==KeyEvent.VK_KP_UP))
				{
					dialog.getInputField().setText(dialog.getPrevCommand_up());
					((Component) textField).requestFocusInWindow();
				}
				else if ((key==KeyEvent.VK_DOWN) || (key==KeyEvent.VK_KP_DOWN))
				{
					dialog.getInputField().setText(dialog.getPrevCommand_down());
					((Component) textField).requestFocusInWindow();
				}
			} 
		};
	}
	public ArrayList<String> commands_compgen;
	private String getCompletionList(boolean newCommands, boolean dirchanged) 
	{
		SSHConnectExec ssh = new SSHConnectExec();
		String result = "";
		if (newCommands)
		{
			// result = ssh.exec(new Empty_Command("compgen -c"  ), false, null, true, false);
			result = ssh.exec(new Empty_Command(
				// http://stackoverflow.com/questions/948008/linux-command-to-list-all-available-commands-and-aliases
				"COMMANDS=`echo -n $PATH | xargs -d : -I {} find {} -maxdepth 1 -executable -type f -printf '%P\\n'` ; ALIASES=`alias | cut -d '=' -f 1`; echo \"$COMMANDS\"$'\\n'\"$ALIASES\" | sort -u "  ), 
				false, null, true, false);
			if (result == null)
				logging.warning(this, "no commands could be found for autocompletion");

			commands_compgen = (ArrayList) getList(result);
			logging.debug(this, "getCompletionList commands compgen -c " + result);
		}

		// if (dirchanged)
		// {
		// 	// String pwd = ssh.exec(new Empty_Command("pwd" ), false, null, true, false).replace("\n", "");
		// 	try {
		// 		getCurrentDirectorySilent = true;
		// 		// exec("pwd\n");
		// 		if (out != null)
		// 		{
		// 			out.write("pwd\n".getBytes());
		// 			logging.debug(this, " exec getPrivateStatus " + dialog.getPrivateStatus());
		// 			out.flush();
		// 		}
		// 		try{Thread.sleep(50);} catch(Exception ee){}
		// 	// }
		// 	// catch (IOException ioe) 
		// 	// { ioe.printStackTrace();}

		// 	if (currentDirectory == null) return result;
			
		// 	currentDirectory = currentDirectory.replace("\n", "") + "/";
		// 	String com = "ls -aldU " + currentDirectory + "./*";
		// 	System.out.println("\n\nCommand: " + com);
		// 	String result_ls = ssh.exec(   new Empty_Command(com ),
		// 		 			false, null, true, false);
		// 	System.out.println("\ncurrentDirectory: " + currentDirectory + "./");
		// 	System.out.println("result_ls: " + result_ls);
		// 	String[] arr_result_dir = result_ls.split("\n");
		// 	String result_dir = "";

		// 	for (String l : arr_result_dir)
		// 	{
		// 		String line = l; //.replace("\\","\\\\");
		// 		System.out.println("line: " + line);
		// 		String dir = "" + line.split(currentDirectory + "/",2)[1];
		// 		System.out.println("DIR: " + dir);
		// 		result_dir = result_dir + dir + 	"\n";
		// 	}
		// 	result = result + "\n" + result_dir;
		// 	}
		// 	catch (Exception ioe) 
		// 	{ ioe.printStackTrace();}

		// }
		return result;
	}

	private java.util.List getList(String str)
	{
		if (str.equals("")) return null;
		String[] arr = str.split("\n");
		ArrayList<String> result = new ArrayList<String>();
		for (String s : arr)
			result.add(s);
		return result;
	}

	@Override
	public void disconnect()
	{
		logging.info(this, "disconnect");
		if (session != null)
			if (session.isConnected())
			{
				logging.info(this, "disconnect session");
				session.disconnect();
				this.session.disconnect();
				session = null;
			}
		if (channel != null)
			if (channel.isConnected())
			{
				logging.info(this, "disconnect channel");
				channel.disconnect();
				this.channel.disconnect();
				channel = null;
			}
	}
}
