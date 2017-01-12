package de.uib.configed.gui.ssh;

import de.uib.utilities.*;
import de.uib.utilities.logging.*;

import de.uib.configed.*;
import de.uib.configed.gui.*;
import de.uib.opsicommand.sshcommand.*;

import java.awt.*;
import java.awt.event.*;
import javax.swing.text.*;

import java.io.*;
import java.io.InputStream.*;
import java.io.OutputStream.*;

import javax.swing.*;
import javax.swing.GroupLayout.*;

import java.util.*;
import java.util.ArrayList.*;

public class SSHConnectionTerminalDialog extends SSHConnectionOutputDialog
{
	
	private JLabel lbl_userhost = new JLabel();
	private JTextField tf_command;
	private JCheckBox cb_privat;
	private JPanel parameterPanel;
	// private JButton btn_changeHelpPanelStatus;
	public JButton btn_killProcess;
	private JButton btn_executeCommand;
	private final SSHConnectTerminal terminal;
	private ArrayList<String> commandHistory = new ArrayList<String>();
	private int historyAddIndex = 0;
	private int historyGetIndex = 0;
	private boolean helpPanelStatus = true;
	private Dimension btn_dim = new Dimension(de.uib.configed.Globals.graphicButtonWidth +15,de.uib.configed.Globals.buttonHeight +3 );
	private Dimension thissize = new Dimension(810,550);
	public SSHConnectionTerminalDialog(String title, final SSHConnectTerminal terminal, boolean visible) 
	{	
		super(title);
		this.setVisible (visible);
		this.terminal = terminal;
		parameterPanel = new SSHCommandControlParameterMethodsPanel(this);
		((SSHCommandControlParameterMethodsPanel)parameterPanel).setGapSize(
			de.uib.configed.Globals.gapSize, de.uib.configed.Globals.gapSize, 
			de.uib.configed.Globals.gapSize, 0);
		((SSHCommandControlParameterMethodsPanel)parameterPanel).initLayout();
		((SSHCommandControlParameterMethodsPanel)parameterPanel).repaint();
		((SSHCommandControlParameterMethodsPanel)parameterPanel).revalidate();
		initGUI();
		this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		this.centerOn(de.uib.configed.Globals.mainFrame);
		this.setSize(this.thissize);
		// if (terminal != null)  terminal.exec("bash\n");
		btn_close.removeActionListener(closeListener);
		this.closeListener = new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				terminal.disconnect();
				cancel();
			}
		};
		btn_close.addActionListener(this.closeListener);
		setComponentsEnabled_RO(! de.uib.configed.Globals.isGlobalReadOnly());
		((Component) tf_command).requestFocusInWindow();
		((JTextField) tf_command).requestFocus();
		((JTextField) tf_command).setCaretPosition(((JTextField) tf_command).getText().length());
		logging.info(this, "SSHConnectionTerminalDialog build ");
		this.addComponentListener(new ComponentAdapter() 
		{
			public void componentResized(ComponentEvent e)
			{
				super.componentResized(e);
				setOutSize();
				// append("", tf_command); // try to set scrollpane to end of textpane and focus on tf_command
			}
		});

	}
	public SSHConnectionTerminalDialog(String title, SSHConnectTerminal terminal) 
	{	
		this(title, terminal, true);
	}
	public SSHConnectionTerminalDialog(String title, boolean visible)
	{
		this(title, null, visible);
	}
	public SSHConnectionTerminalDialog(String title)
	{
		this(title, true);
	}
	private void setComponentsEnabled_RO(boolean value)
	{
		logging.info(this, "setComponentsEnabled_RO value " + value);
		tf_command.setEnabled(value);
		btn_executeCommand.setEnabled(value);
		btn_killProcess.setEnabled(value);
	}
	
	
	private void setOutSize()
	{
		double no_output_Height = (de.uib.configed.Globals.gapSize*4) + tf_command.getHeight() + parameterPanel.getHeight() + btn_dim.getHeight();
		this.thissize = this.getSize();
		double w = this.thissize.getWidth()-(de.uib.configed.Globals.gapSize*2); 
		double h = this.thissize.getHeight() - no_output_Height;
		Dimension output_size = new Dimension( );
		output_size.setSize( w, h);
		this.output.setSize(output_size);
		this.output.setPreferredSize(output_size);
		this.output.setMaximumSize(output_size);
		this.jScrollPane.setSize(output_size);
		this.jScrollPane.setPreferredSize(output_size);
		this.jScrollPane.setMaximumSize(output_size);
		this.revalidate();
		this.repaint();
	}

	public JTextField getInputField()
	{
		if (tf_command == null)
			return null;
		return tf_command;
	}
	public boolean getPrivateStatus()
	{
		return passwordMode;
	}
	public void setPrivate(boolean pr)
	{
		logging.info(this, "setPrivate " + pr);
		if(pr) changeEchoChar('*');
		else changeEchoChar((char)0);
	}
	public void setLastHistoryIndex()
	{
		if (commandHistory.size() >0)
			historyGetIndex = commandHistory.size();
	}

	public void addToHistory(String co)
	{
		if ((co != null) && (!co.trim().equals("")) )
		{
			logging.debug(this, "addToHistory \"" + co + "\" at index " + historyAddIndex + " getIndex " + (historyAddIndex+1));
			commandHistory.add(historyAddIndex, co);
			historyAddIndex = historyAddIndex+1;
			historyGetIndex = historyAddIndex;
		}
	}
	public String getPrevCommand_up()
	{
		logging.debug(this, "getPrevCommand_up historySize " + commandHistory.size() + " getIndex " + historyGetIndex);
		if (commandHistory.size() <= 0)
			return "";
		if (historyGetIndex-1 < 0)
		{
			historyGetIndex = 0;
			if (commandHistory.get(historyGetIndex) != null)
				return commandHistory.get(historyGetIndex);
			else return "";
		}
		historyGetIndex = historyGetIndex-1;
		return commandHistory.get(historyGetIndex);
	}
	public String getPrevCommand_down()
	{
		logging.debug(this, "getPrevCommand_down historySize " + commandHistory.size() + " getIndex " + historyGetIndex);
		if ((historyGetIndex+1) >= commandHistory.size()) 
		{
			historyGetIndex = commandHistory.size();
			return "";
		}
		historyGetIndex = historyGetIndex+1;
		return commandHistory.get(historyGetIndex);
	}

	public JTextPane getOutputField()
	{
		if (output == null)
			return null;
		return output;
	}

	private boolean passwordMode = false;
	private void initGUI()  	
	{
		logging.info(this, "initGUI ");
		tf_command = new JPasswordField()
		{
			public void addNotify()
			{
				super.addNotify();
				requestFocusInWindow();
			}
		}; 
		tf_command.setPreferredSize(new Dimension( de.uib.configed.Globals.firstLabelWidth + de.uib.configed.Globals.gapSize, de.uib.configed.Globals.lineHeight));

		
		((Component) tf_command).requestFocusInWindow();
		((JTextField) tf_command).requestFocus();
		((JTextField) tf_command).setCaretPosition(((JTextField) tf_command).getText().length());

		cb_privat = new JCheckBox(configed.getResourceValue("SSHConnection.passwordButtonText"));
		cb_privat.setPreferredSize(btn_dim);
		if (!(de.uib.configed.Globals.isGlobalReadOnly()))
			cb_privat.addItemListener(new ItemListener() 
			{
				@Override
				public void itemStateChanged(ItemEvent e) {
					if (passwordMode) 
					{
						changeEchoChar('*');
						removeAutocompleteListener();
						passwordMode = false;
					}
					else 
					{
						changeEchoChar((char)0);
						if (terminal.commands_compgen != null)
							setAutocompleteList(terminal.commands_compgen);
						passwordMode=true;
					}
				}
			});
		changeEchoChar((char)0);
		passwordMode=true;
		final SSHConnectionTerminalDialog caller = this;
		if (!(de.uib.configed.Globals.isGlobalReadOnly()))
			((SSHCommandControlParameterMethodsPanel)parameterPanel).getButtonTest().addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					((SSHCommandControlParameterMethodsPanel)parameterPanel).doActionTestParam(caller);
				}
			});

		if (!(de.uib.configed.Globals.isGlobalReadOnly()))
			((SSHCommandControlParameterMethodsPanel)parameterPanel).getButtonAdd().addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					((SSHCommandControlParameterMethodsPanel)parameterPanel).doActionParamAdd((JTextComponent) tf_command);
				}
			});
		// btn_changeHelpPanelStatus= new de.uib.configed.gui.IconButton(
		// 	de.uib.configed.configed.getResourceValue("SSHConnection.CommandControl.btnShowActionHelp") ,
		// 	"images/help.gif", "images/help.gif", "images/help.gif",true
		// );
		// btn_changeHelpPanelStatus.setPreferredSize(btn_dim);
		// if (!(de.uib.configed.Globals.isGlobalReadOnly()))
		// 	btn_changeHelpPanelStatus.addActionListener(new ActionListener()
		// 	{
		// 		public void actionPerformed(ActionEvent e)
		// 		{
		// 			showPanel();
		// 		}
		// 	});
		btn_killProcess= new de.uib.configed.gui.IconButton(
			de.uib.configed.configed.getResourceValue("SSHConnection.buttonKillProcess") ,
			"images/edit-delete.png", "images/edit-delete.png", "images/edit-delete.png",true
		);
		btn_killProcess.setPreferredSize(btn_dim);
		btn_killProcess.setToolTipText(configed.getResourceValue("SSHConnection.buttonKillProcess"));
		

		btn_executeCommand= new de.uib.configed.gui.IconButton(
			de.uib.configed.configed.getResourceValue("SSHConnection.CommandControl.btnExecuteCommand") ,
			"images/execute.png", "images/execute.png", "images/execute.png",true
		);
		btn_executeCommand.setPreferredSize(btn_dim);
		if (!(de.uib.configed.Globals.isGlobalReadOnly()))
			btn_executeCommand.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					String text = getInputField().getText() + "\n";
					if (terminal != null) 
					{
						terminal.exec(text);
						((Component) getInputField()).requestFocusInWindow();
						getInputField().setText("");
					}
				}
			});
		
		try{
			createLayout();
		} catch (java.lang.NullPointerException npe)
		{
			logging.error("NullPointerException in createLayout ");
			logging.error("looks like a thread problem");
			logging.error("" + npe);
		}
		catch (Exception e)
		{
			logging.error("Exception in createLayout ");
			logging.error("looks like a thread problem");
			logging.error("" + e);
		}
		this.setSize(this.getWidth(), this.getHeight() + parameterPanel.getHeight() );
		setCenterLayout();
		this.helpPanelStatus = false;
	}
	private Autocomplete autoComplete;
	public void setAutocompleteList(java.util.List<String> list)
	{
		if (list == null) return;
		final String COMMIT_ACTION = "commit";
		// Without this, cursor always leaves text field
		tf_command.setFocusTraversalKeysEnabled(false);
		if (autoComplete != null) tf_command.getDocument().removeDocumentListener( autoComplete );
		
		if (list != null)
		{
			autoComplete = new Autocomplete(tf_command, list);
			tf_command.getDocument().addDocumentListener(autoComplete);

			// Maps the tab key to the commit action, which finishes the autocomplete
			// when given a suggestion
			tf_command.getInputMap().put(KeyStroke.getKeyStroke("TAB"), COMMIT_ACTION);
			tf_command.getActionMap().put(COMMIT_ACTION, autoComplete.new CommitAction());
		}
	}
	public void removeAutocompleteListener()
	{
		if (autoComplete != null) tf_command.getDocument().removeDocumentListener( autoComplete );
	}

	private void showPanel() 
	{
		logging.info(this, "showPanel helpPanelStatus " + helpPanelStatus);
		if (this.helpPanelStatus)
		{
			setCenterLayout();
			this.mainPanel.setSize(this.mainPanel.getWidth(), this.mainPanel.getHeight() + this.parameterPanel.getHeight() );
			this.setSize(this.getWidth(), this.getHeight() + this.parameterPanel.getHeight() );
		}
		else 
		{
			setCenterLayout();
			this.mainPanel.setSize(this.mainPanel.getWidth(), this.mainPanel.getHeight()-this.parameterPanel.getHeight());
			this.setSize(this.getWidth(), this.getHeight()-this.parameterPanel.getHeight());
		}
		this.helpPanelStatus = !this.helpPanelStatus;
		this.repaint();
		this.revalidate();
	}

	protected void createLayout()
	{
		logging.info(this, "createLayout ");
		int pref = GroupLayout.PREFERRED_SIZE;
		int max = Short.MAX_VALUE;
		GroupLayout.Alignment leading = GroupLayout.Alignment.LEADING;
		int gap = de.uib.configed.Globals.gapSize;
		int mgap = de.uib.configed.Globals.minGapSize;
		
		konsolePanelLayout.setVerticalGroup(konsolePanelLayout.createSequentialGroup()
			.addGap(gap)
			.addComponent(jScrollPane,pref, pref,max)
			.addGap(gap)
			.addGroup(konsolePanelLayout.createParallelGroup()
				.addGap(gap)
				// .addComponent(btn_changeHelpPanelStatus,pref, pref,pref )
				.addComponent(cb_privat,pref, pref,pref )
				.addGroup(konsolePanelLayout.createSequentialGroup()
					// .addGap(2)
					.addComponent(tf_command,pref, pref,pref )
				)
				.addGap(gap)
				.addComponent(btn_executeCommand,pref, pref,pref )
				.addComponent(btn_killProcess,pref, pref,pref )
				.addComponent(btn_close,pref, pref,pref )
				.addGap(gap)
			)
			.addGap(gap)
		);

		konsolePanelLayout.setHorizontalGroup(konsolePanelLayout.createParallelGroup()
			.addGroup(konsolePanelLayout.createSequentialGroup()
				.addGap(gap)
				.addComponent(jScrollPane,pref, pref,max)
				.addGap(gap)
			)
			.addGroup(konsolePanelLayout.createSequentialGroup()
				.addGap(gap)
				// .addComponent(btn_changeHelpPanelStatus,pref, pref,pref )
				.addComponent(cb_privat,pref, pref,pref )
				.addComponent(tf_command,pref, pref,max)
				.addGap(gap)
				.addComponent(btn_executeCommand,pref, pref,pref )
				.addComponent(btn_killProcess,pref, pref,pref )
				.addComponent(btn_close,pref, pref,pref )
				.addGap(gap)
			)
		);
		setCenterLayout();
	}
	private void setCenterLayout()
	{
		mainPanelLayout.setAutoCreateGaps(true);
		if (this.helpPanelStatus)
		{
			mainPanelLayout.setHorizontalGroup( mainPanelLayout.createParallelGroup()
				.addComponent(this.inputPanel)
				.addComponent(this.parameterPanel)
			);
			mainPanelLayout.setVerticalGroup( mainPanelLayout.createSequentialGroup()
				.addComponent(this.inputPanel)
				.addComponent(this.parameterPanel)
			);
		}else
		{
			mainPanelLayout.setHorizontalGroup( mainPanelLayout.createParallelGroup()
				.addComponent(this.inputPanel)
			);
			mainPanelLayout.setVerticalGroup( mainPanelLayout.createSequentialGroup()
				.addComponent(this.inputPanel)
			);
		}
		parameterPanel.setVisible(this.helpPanelStatus);
	}
	public void changeEchoChar(char c)
	{
		// if (passwordMode) 
		logging.debug(this, "changeEchoChar char " + c);
		((JPasswordField)tf_command).setEchoChar(c);
		logging.debug(this, "changeEchoChar checkbox set Selected " + passwordMode);
		cb_privat.setSelected(passwordMode);

	}

}