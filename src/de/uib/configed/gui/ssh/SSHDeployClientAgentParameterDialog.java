package de.uib.configed.gui.ssh;

import de.uib.opsicommand.*;
import de.uib.opsicommand.sshcommand.*;
import java.awt.event.*;
import java.awt.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import java.io.*;
import java.util.*;
import java.nio.charset.Charset;
import java.util.regex.*;
import de.uib.configed.*;
import de.uib.configed.gui.*;
import de.uib.opsidatamodel.*;
import de.uib.utilities.logging.*;

public class SSHDeployClientAgentParameterDialog extends FGeneralDialog
{
	private GroupLayout layout;
	private JPanel inputPanel = new JPanel();
	private JPanel buttonPanel = new JPanel();
	private JPanel winAuthPanel = new JPanel();

	private JLabel lbl_client = new JLabel();
	private JLabel lbl_user = new JLabel();
	private JLabel lbl_passw = new JLabel();
	private JLabel lbl_userdata = new JLabel();
	private JLabel lbl_verbosity = new JLabel();
	private JLabel lbl_fullCommand = new JLabel();

	private JButton btn_execute;
	private JButton btn_close;
	private JButton btn_help;

	private JTextField tf_client;
	private JTextField tf_user;
	private JPasswordField tf_passw;
	private JCheckBox cb_passw_echo;
	private JButton btn_showPassw;
	private JComboBox cb_verbosity;
	private String defaultWinUser = "";
	
	final private int frameLength = 500;
	final private int frameHight = 330;
	private CommandDeployClientAgent commandDeployClientAgent = new CommandDeployClientAgent();
	private ConfigedMain main;

	public SSHDeployClientAgentParameterDialog()
	{
		this(null);
	}
	public SSHDeployClientAgentParameterDialog(ConfigedMain m)
	{
		super(null,configed.getResourceValue("SSHConnection.ParameterDialog.deploy-clientagent.title"), false);
		main = m;
		getDefaultAuthData();

		init();
		pack();
		this.centerOn(de.uib.configed.Globals.mainFrame);
		this.setBackground(Globals.backLightBlue);
		this.setSize(frameLength, frameHight);
		this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		this.setVisible (true);
		logging.info(this, "SSHDeployClientAgentParameterDialog build");
		
		setComponentsEnabled(! Globals.isGlobalReadOnly());
	}
	private void getDefaultAuthData()
	{
		Map<String, Object> configs = main.getPersistenceController().getConfig(main.getPersistenceController().getHostInfoCollections().getConfigServer());
		ArrayList<Object> result_config_list = (ArrayList<Object>) configs.get(main.getPersistenceController().KEY_SSH_DEFAULTWINUSER);
		if (result_config_list ==null || result_config_list.size() == 0)
		{
			// defaultWinUser = configed.getResourceValue("SSHConnection.ParameterDialog.deploy-clientagent.defaultWinUser");
			logging.info(this, "KEY_SSH_DEFAULTWINUSER not existing");
			// jMenuRemoteTerminal.setEnabled(main.getPersistenceController().KEY_SSH_SHELL_ACTIVE_defaultvalue);
			//the config will be created in this run of configed
		}
		else
		{
			defaultWinUser = (String) result_config_list.get(0);
			logging.info(this, "KEY_SSH_DEFAULTWINUSER "+ ((String) result_config_list.get(0)));
			// jMenuRemoteTerminal.setEnabled((Boolean) shell_list.get(0));
		}

		result_config_list = (ArrayList<Object>) configs.get(main.getPersistenceController().KEY_SSH_DEFAULTWINPW);
		if (result_config_list ==null || result_config_list.size() == 0)
		{
			// defaultWinUser = configed.getResourceValue("SSHConnection.ParameterDialog.deploy-clientagent.defaultWinUser");
			logging.info(this, "KEY_SSH_DEFAULTWINPW not existing");
			// jMenuRemoteTerminal.setEnabled(main.getPersistenceController().KEY_SSH_SHELL_ACTIVE_defaultvalue);
			//the config will be created in this run of configed
		}
		else
		{
			if (tf_passw == null)
			{
				tf_passw=new JPasswordField("nt123", 15);
				tf_passw.setEchoChar('*');
			}
			tf_passw.setText((String) result_config_list.get(0));
			logging.info(this, "key_ssh_shell_active ***confidential***");
			// jMenuRemoteTerminal.setEnabled((Boolean) shell_list.get(0));
		}
	}

	private void setComponentsEnabled(boolean value)
	{
		tf_client.setEnabled(value);
		tf_client.setEditable(value);

		tf_user.setEnabled(value);
		tf_user.setEditable(value);

		tf_passw.setEnabled(value);
		tf_passw.setEditable(value);

		cb_passw_echo.setEnabled(value);

		btn_showPassw.setEnabled(value);

		cb_verbosity.setEnabled(value);
		cb_verbosity.setEditable(value);

		btn_help.setEnabled(value);
		btn_execute.setEnabled(value);
	}

	protected void init() 
	{
		inputPanel.setBackground(Globals.backLightBlue);
		buttonPanel.setBackground(Globals.backLightBlue);
		winAuthPanel.setBackground(Globals.backLightBlue);
		getContentPane().add(inputPanel, BorderLayout.CENTER);
		getContentPane().add(buttonPanel, BorderLayout.SOUTH);
		
		buttonPanel.setBorder(BorderFactory.createTitledBorder(""));
		inputPanel.setBorder(BorderFactory.createTitledBorder(""));
		winAuthPanel.setBorder(new LineBorder(de.uib.configed.Globals.blueGrey));
		inputPanel.setPreferredSize(new java.awt.Dimension(376, 220));
		{
			lbl_verbosity.setText(configed.getResourceValue("SSHConnection.ParameterDialog.jLabelVerbosity"));
			cb_verbosity = new JComboBox();
			cb_verbosity.setToolTipText(configed.getResourceValue("SSHConnection.ParameterDialog.tooltip.verbosity"));
			for (int i = 0; i < 5; i++) cb_verbosity.addItem(i);
			cb_verbosity.setSelectedItem(1);
			cb_verbosity.addItemListener(new ItemListener() 
			{
				@Override
				public void itemStateChanged(ItemEvent e) 
				{
					changeVerbosity();
				}
			});
		}
		{
			lbl_client.setText(configed.getResourceValue("SSHConnection.ParameterDialog.deploy-clientagent.jLabelClient"));
			tf_client = new JTextField();
			tf_client.setToolTipText(configed.getResourceValue("SSHConnection.ParameterDialog.deploy-clientagent.tooltip.tf_client"));
			tf_client.getDocument().addDocumentListener(new DocumentListener() 
			{
				public void changedUpdate(DocumentEvent documentEvent) { changeClient(); }
				public void insertUpdate(DocumentEvent documentEvent) { changeClient(); }
				public void removeUpdate(DocumentEvent documentEvent) { changeClient(); }
			});


			lbl_user.setText(configed.getResourceValue("SSHConnection.ParameterDialog.deploy-clientagent.jLabelUser"));
			tf_user = new JTextField(defaultWinUser);
			tf_user.setToolTipText(configed.getResourceValue("SSHConnection.ParameterDialog.deploy-clientagent.tooltip.tf_user"));
			tf_user.getDocument().addDocumentListener(new DocumentListener() 
			{
				public void changedUpdate(DocumentEvent documentEvent) { changeUser(); }
				public void insertUpdate(DocumentEvent documentEvent) { changeUser(); }
				public void removeUpdate(DocumentEvent documentEvent) { changeUser(); }
			});

			lbl_passw.setText(configed.getResourceValue("SSHConnection.ParameterDialog.deploy-clientagent.jLabelPassword"));
			tf_passw=new JPasswordField("nt123", 15);
			tf_passw.setEchoChar('*');

			cb_passw_echo = new JCheckBox(configed.getResourceValue("SSHConnection.ParameterDialog.deploy-clientagent.showPassword"));
			

			// btn_showPassw = new JButton("", Globals.createImageIcon("images/eye_open.png", ""));// Globals.createImageIcon("images/eye_open_close.png"));
			btn_showPassw = new JButton(configed.getResourceValue("SSHConnection.passwordButtonText"));
			// btn_showPassw.setPreferredSize(new Dimension(Globals.squareButtonWidth, Globals.buttonHeight));
			btn_showPassw.setPreferredSize(new Dimension(de.uib.configed.Globals.graphicButtonWidth +15,de.uib.configed.Globals.buttonHeight +3));
			btn_showPassw.setToolTipText(configed.getResourceValue("SSHConnection.ParameterDialog.deploy-clientagent.showPassword.tooltip"));
			btn_showPassw.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					changeEchoChar();
				}
			});

			tf_passw.getDocument().addDocumentListener(new DocumentListener() 
			{
				public void changedUpdate(DocumentEvent documentEvent) { changePassw(); }
				public void insertUpdate(DocumentEvent documentEvent) { changePassw(); }
				public void removeUpdate(DocumentEvent documentEvent) { changePassw(); }
			});
		}
		{
			lbl_userdata.setText(configed.getResourceValue("SSHConnection.ParameterDialog.deploy-clientagent.targetclient_authentication"));
		}
		
		/*{
			lbl_freeInput.setText(configed.getResourceValue("SSHConnection.ParameterDialog.jLabelFreeInput"));
			tf_freeInput = new JTextField();
			tf_freeInput.setToolTipText(configed.getResourceValue("SSHConnection.ParameterDialog.tooltip.freeInput"));
			// tf_freeInput.setText("");
			addListener(tf_freeInput);
		}*/
		{
			btn_help = new JButton("", Globals.createImageIcon("images/help.gif", ""));
			btn_help.setToolTipText( configed.getResourceValue("SSHConnection.buttonHelp") 	);
			btn_help.setText(configed.getResourceValue("SSHConnection.buttonHelp"));
			buttonPanel.add(btn_help);
			btn_help.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					doActionHelp();
				}
			});

			btn_execute = new JButton();
			buttonPanel.add(btn_execute);
			btn_execute.setText(configed.getResourceValue("SSHConnection.buttonExec"));
			if (!(Globals.isGlobalReadOnly()))
				btn_execute.addActionListener(new ActionListener()
				{
					public void actionPerformed(ActionEvent e)
					{
						// if (!(Globals.isGlobalReadOnly()))
						doAction1();
					}
				});

			btn_close = new JButton();
			buttonPanel.add(btn_close);
			btn_close.setText(configed.getResourceValue("SSHConnection.buttonClose"));
			btn_close.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					cancel();
				}
			});
		}
		{
			lbl_fullCommand.setText("opsi-deploy-client-agent ");
			updateCommand();
		}

		String[] clients_list = main.getSelectedClients();
		if (clients_list.length > 0)
		{
			StringBuffer clients = new StringBuffer();
			for (String c :clients_list)
			{
				clients.append(c);
				clients.append(" ");
			}
			tf_client.setText(clients.toString());
		}

		changeClient();
		changeUser();
		changePassw();
		// changeKeepClient();
		changeVerbosity();		
		
		initLayout();

	}

	private void updateCommand()
	{
		lbl_fullCommand.setText(commandDeployClientAgent.getCommand());
	}

	private void changeVerbosity()
	{
		commandDeployClientAgent.setVerbosity((int)cb_verbosity.getSelectedItem());
	}
	private void changeClient()
	{
		commandDeployClientAgent.setClient(tf_client.getText().trim());
		updateCommand();
	}
	private void changeUser()
	{
		if (!(tf_user.getText().equals(defaultWinUser))) 
			commandDeployClientAgent.setUser(tf_user.getText().trim());
		else commandDeployClientAgent.setUser("");
		updateCommand();
	}
	private void changePassw()
	{
		commandDeployClientAgent.setPassw(new String(tf_passw.getPassword()).trim());
		updateCommand();
	}
	
	boolean aktive = false;
	public void changeEchoChar()
	{
		if (aktive) 
		{
			aktive = false;
			((JPasswordField)tf_passw).setEchoChar('*');
		}
		else 
		{
			aktive = true;
			((JPasswordField)tf_passw).setEchoChar((char)0);
		}
	}

	public void setComponentsEditable(boolean value)
	{
		tf_user.setEnabled(value);
		tf_passw.setEnabled(value);
	}

	// /* This method gets called when button 2 is pressed */
	public void cancel()
	{
		super.doAction2();
	}
	// public void doAction2()
	// {
	// 	// setVisible(false);
		
	// 	this.setVisible (false);
	// 	this.dispose ();
	// }

	/* This method is called when button 1 is pressed */
	public void doAction1()
	{
		try
		{
			logging.info(this, "doAction1 deploy-clientagent ");
			SSHConnectExec ssh = new SSHConnectExec((SSHCommand)commandDeployClientAgent);
		}
		catch (Exception e)
		{
			logging.warning(this, "doAction1, exception occurred " + e);
			logging.logTrace(e);
		}
	}

	public void doActionHelp()
	{
		SSHConnectionExecDialog dia = commandDeployClientAgent.startHelpDialog();
		dia.setVisible(true);
	}


	private void initLayout()
	{
		int pref = GroupLayout.PREFERRED_SIZE;
		int max = Short.MAX_VALUE;
		GroupLayout.Alignment leading = GroupLayout.Alignment.LEADING;
		GroupLayout winAuthPanelLayout = new GroupLayout((JComponent)winAuthPanel);
		winAuthPanel.setLayout(winAuthPanelLayout);

		winAuthPanelLayout.setHorizontalGroup(winAuthPanelLayout.createSequentialGroup()
			.addGroup( winAuthPanelLayout.createParallelGroup(leading)
				.addGroup( winAuthPanelLayout.createSequentialGroup()
					.addGap(Globals.gapSize)
					.addGroup( winAuthPanelLayout.createParallelGroup(leading)
						.addGap(Globals.gapSize)
						.addGroup( winAuthPanelLayout.createSequentialGroup()
							.addGap(Globals.minGapSize)
							.addComponent(lbl_user)
						)
						.addGap(Globals.gapSize)
						.addGroup( winAuthPanelLayout.createSequentialGroup()
							.addGap(Globals.minGapSize)
							.addComponent(lbl_passw)
						)
						.addGap(Globals.gapSize)
					)
					.addGap(Globals.gapSize)
					.addGroup( winAuthPanelLayout.createParallelGroup(leading)
						.addGap(Globals.gapSize)
						.addComponent(tf_user, pref, pref, max)
						.addGap(Globals.gapSize)
						.addGroup( winAuthPanelLayout.createParallelGroup(leading)
							.addGap(Globals.gapSize)
							.addComponent(tf_passw, pref, pref, max)
							.addGap(Globals.gapSize)
							// .addComponent(btn_showPassw,pref,pref,pref)
							// .addGap(Globals.gapSize)
						)
						.addGap(Globals.gapSize)
					)
					.addGap(Globals.gapSize)
				)
			)
		);
		winAuthPanelLayout.setVerticalGroup(winAuthPanelLayout.createSequentialGroup()
			.addGroup( winAuthPanelLayout.createParallelGroup(leading)
				.addGroup(winAuthPanelLayout.createSequentialGroup()
					.addGap(Globals.gapSize)
					.addGroup( winAuthPanelLayout.createParallelGroup(leading)
						.addGap(Globals.gapSize)
						.addGroup( winAuthPanelLayout.createSequentialGroup()
							.addGap(Globals.minGapSize)
							.addComponent(lbl_user)
						)
						.addGap(Globals.gapSize)
						.addComponent(tf_user) 
					)
					.addGap(Globals.gapSize)
					.addGroup( winAuthPanelLayout.createParallelGroup(leading)
						.addGap(Globals.gapSize)
						.addGroup( winAuthPanelLayout.createSequentialGroup()
							.addGap(Globals.minGapSize)
							.addComponent(lbl_passw)
						)
						.addGap(Globals.gapSize)
						.addComponent(tf_passw,pref,pref, pref)
						.addGap(Globals.gapSize)
						// .addComponent(btn_showPassw,pref, pref,pref)
						// .addGap(Globals.gapSize)
					)
					.addGap(Globals.gapSize)
				)
			)
		);

		GroupLayout inputPanelLayout = new GroupLayout((JComponent)inputPanel);
		inputPanel.setLayout(inputPanelLayout);
		inputPanelLayout.setHorizontalGroup(inputPanelLayout.createSequentialGroup()
			.addGroup(inputPanelLayout.createParallelGroup()
				.addGroup(inputPanelLayout.createSequentialGroup()
					.addGroup(inputPanelLayout.createParallelGroup()
						.addGap(Globals.minGapSize)
						.addComponent(lbl_client,pref, pref, pref)
					)
					.addGap(Globals.gapSize)
					.addGroup(inputPanelLayout.createParallelGroup()
						.addComponent(tf_client,pref, pref, max)
					)
				)
				.addGap(Globals.gapSize)
				.addGroup(inputPanelLayout.createSequentialGroup()
					.addComponent(lbl_userdata,pref,pref,pref)
				)
				.addGap(Globals.gapSize)
				.addComponent(winAuthPanel, pref, pref, max)
				.addGap(Globals.gapSize)
				.addGroup(inputPanelLayout.createSequentialGroup()
					.addGroup(inputPanelLayout.createParallelGroup()
						.addComponent(lbl_verbosity,pref, pref, max)
					)
					.addGap(Globals.gapSize)
					.addGroup(inputPanelLayout.createParallelGroup()
						.addComponent(cb_verbosity,pref, pref, pref)
					)
				)
			)
			.addContainerGap()
		);
	
		inputPanelLayout.setVerticalGroup(inputPanelLayout.createSequentialGroup()
			.addGap(Globals.gapSize)
			.addGroup(inputPanelLayout.createParallelGroup(leading)
				.addGroup(inputPanelLayout.createSequentialGroup()
					.addGap(Globals.minGapSize)
					.addComponent(lbl_client, pref, pref, pref)
				)
				.addGap(Globals.gapSize)
				.addComponent(tf_client, leading, pref, pref, pref)
			)
			.addGap(Globals.gapSize)
			.addGroup(inputPanelLayout.createParallelGroup(leading)
				.addComponent(lbl_userdata,pref,pref,pref)
			)
			.addGap(Globals.gapSize)
			.addComponent(winAuthPanel, pref, pref, pref)
			.addGap(Globals.gapSize)
			.addGroup(inputPanelLayout.createParallelGroup(leading)
				.addGroup(inputPanelLayout.createSequentialGroup()
					.addGap(Globals.minGapSize)
			 		.addComponent(lbl_verbosity, pref, pref, pref)
			 	)
			 	.addGap(Globals.gapSize)
			 	.addComponent(cb_verbosity, leading, pref, pref, pref)
			)
		);
	}
}