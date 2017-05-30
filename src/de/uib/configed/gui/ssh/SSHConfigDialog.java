package de.uib.configed.gui.ssh;

import de.uib.opsicommand.sshcommand.*;
import  de.uib.opsidatamodel.*;
import de.uib.configed.*;
import de.uib.configed.gui.*;
import de.uib.utilities.logging.*;
import java.awt.event.*;
import java.awt.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.border.*;
import java.io.*;
import java.util.*;
import de.uib.utilities.swing.*;
import java.beans.*;

public class SSHConfigDialog extends /*javax.swing.JDialog */ FGeneralDialog
{
	private GroupLayout gpl;
	private JPanel connectionPanel = new JPanel();
	private JPanel settingsPanel = new JPanel();
	private JPanel buttonPanel = new JPanel();

	// private final JFileChooser fchooser = new JFileChooser();
	private static JCheckBox cb_useDefault;
	private static JCheckBox cb_useKeyfile;
	private JCheckBox cb_useOuputColor;
	private JCheckBox cb_execInBackground;
	private JButton btn_save;
	private JButton btn_openChooser;
	private JButton btn_close;

	private JLabel lbl_serverConfig = new JLabel();
	private JLabel lbl_keyfile = new JLabel();
	private JLabel lbl_passphrase = new JLabel();
	private JLabel lbl_host = new JLabel();
	private JLabel lbl_user = new JLabel();
	private JLabel lbl_passw = new JLabel();
	private JLabel lbl_port = new JLabel();
	private JLabel lbl_connectionState = new JLabel();

	private ButtonGroup buttonGroup1;
	// private JRadioButton rb_cancel;
	private JRadioButton rb_passw;
	private JRadioButton rb_keyfile;
	
	//private java.util.Set<String> depots;
	// private JTextField tf_host;
	private static JComboBox<String> cb_host;
	private static JTextField tf_keyfile;
	private static JPasswordField tf_passphrase;
	private static JTextField tf_user;
	private static JTextField tf_port;
	private static JPasswordField tf_passw;
	private static boolean cb_useDefault_state;
	private static boolean cb_useOuputColor_state;
	private static boolean cb_execInBackground_state;
	// private Vector<String> localDepots = null;
	private MainFrame main;
	private ConfigedMain configedMain;
	private static SSHConfigDialog instance ;
	private static SSHConnectExec connection = null;
		

	private SSHConfigDialog(Frame owner, ConfigedMain cmain)
	{
		super(null,configed.getResourceValue("MainFrame.jMenuSSHConfig"), false);
		main = (MainFrame) owner;
		configedMain = cmain;
		connection = new SSHConnectExec(configedMain);
		this.centerOn(main);
		this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		init();
		// pack();
		this.setSize(500, 535);
		this.setVisible (true);
		cb_useDefault_state = cb_useDefault.isSelected();
		cb_useOuputColor_state = cb_useOuputColor.isSelected();
		cb_execInBackground_state = cb_execInBackground.isSelected();
		if (Globals.isGlobalReadOnly())
		{
			setComponentsEnabled_RO(false);
		}
	}

	public static SSHConfigDialog getInstance(Frame fr, ConfigedMain cmain)
	{
		if (instance == null) 
			instance = new SSHConfigDialog(fr, cmain);
		instance.setVisible(true);
		checkComponents();
		return instance;
	}
	
	private void checkComponentStates()
	{
		boolean state = compareStates();
		logging.info(this, "checkComponentStates setBtn_save setEnabled " + !state);
		btn_save.setEnabled(! state);
		setStateLabel(SSHCommandFactory.getInstance().getConnectionState());
	}
	private boolean compareStates()
	{
		logging.info(this, "compareStates " );
		// logging.info(this, "compareStates  (cb_useDefault_state != cb_useDefault.isSelected()) " 
			// + cb_useDefault_state + " != " + cb_useDefault.isSelected());
		if ( !cb_useDefault.isSelected())
		{
			if (! connection.getConnectedHost().equals(cb_host.getSelectedItem()))
			{
				logging.debug(this, "compareStates 1" );
				return false;
			}
			if (! connection.getConnectedUser().equals(tf_user.getText()))
			{
				logging.debug(this, "compareStates 2" );
				return false;
			}
			if (! connection.getConnectedPw().equals(new String(tf_passw.getPassword())))
			{
				logging.debug(this, "compareStates 3" );
				logging.debug(this, "connection.getPW " + connection.getConnectedPw());
				logging.debug(this, "tf.getPW " + new String(tf_passw.getPassword()));
				return false;
			}
			if ( (! connection.getConnectedPort().equals(tf_port.getText()))
				&& (!connection.isConnectedViaKeyfile()))
			{
				logging.debug(this, "compareStates 4" );
				return false;
			}
		}
		else
		{
			if (! connection.getConnectedHost().equals(configedMain.HOST))
			{
				logging.debug(this, "compareStates 5" );
				return false;
			}
			if (! connection.getConnectedPort().equals(connection.default_port))
			{
				logging.debug(this, "compareStates 6" );
				return false;
			}
			if (! connection.getConnectedUser().equals(configedMain.USER))
			{
				logging.debug(this, "compareStates 7" );
				return false;
			}
			if ( (! connection.getConnectedPw().equals(configedMain.PASSWORD))
				&& (! connection.isConnectedViaKeyfile()))
			{
				logging.debug(this, "compareStates 8" );
				return false;
			}
		}
		if ( connection.isConnectedViaKeyfile() != (cb_useKeyfile.isSelected()))
		{
			logging.debug(this, "compareStates 9" );
			return false;
		}
		else
			if (cb_useKeyfile.isSelected())
			{
				try
				{
					if (connection.getConnectedKeyfile() != null)
						if (! connection.getConnectedKeyfile().equals(tf_keyfile.getText()))
						{
							logging.debug(this, "compareStates 10" );
							return false;
						}
					String pp = new String(tf_passphrase.getText());
					if (connection.getConnectedPassphrase() != null)
						if (! connection.getConnectedPassphrase().equals(pp))
						{
							logging.debug(this, "compareStates 11" );
							return false;
						}
				} catch (Exception e)
				{
					logging.warning(this, "Error " + e);
					logging.logTrace(e);
				}
			}

		logging.debug(this, "compareStates  (factory.ssh_colored_output != cb_useOuputColor.isSelected()) " +  SSHCommandFactory.getInstance().ssh_colored_output + " != " + cb_useOuputColor.isSelected());
		if (SSHCommandFactory.getInstance().ssh_colored_output != cb_useOuputColor.isSelected())
		{
			logging.debug(this, "compareStates 12" );
			return false;
		}
		logging.debug(this, "compareStates  (factory.ssh_always_exec_in_background != cb_execInBackground.isSelected()) " +  SSHCommandFactory.getInstance().ssh_always_exec_in_background + " != " + cb_execInBackground.isSelected());
		if (SSHCommandFactory.getInstance().ssh_always_exec_in_background != cb_execInBackground.isSelected())
		{
			logging.debug(this, "compareStates 13" );
			return false;
		}
		return true;
	}

	public void setStateLabel(String str)
	{
		String text = "<html><font color='blue'>" + str +"</font></html>";
		if (str.equals(SSHCommandFactory.getInstance().connected))
			text = "<html><font color='green'>" + str +"</font></html>";
		else if (str.equals(SSHCommandFactory.getInstance().not_connected))
			text = "<html><font color='red'>" + str +"</font></html>";
		lbl_connectionState.setText(text);
		logging.debug(this, "setStateLabel setText " + text);
	}

	protected void init() 
	{
		logging.info(this, "sshConfigDialog init ");
		connectionPanel.setBackground(Globals.backLightBlue);
		settingsPanel.setBackground(Globals.backLightBlue);
		buttonPanel.setBackground(Globals.backLightBlue);
		getContentPane().add(connectionPanel, BorderLayout.NORTH);
		getContentPane().add(settingsPanel, BorderLayout.CENTER);
		getContentPane().add(buttonPanel, BorderLayout.SOUTH);
		
		GroupLayout connectionPanelLayout = new GroupLayout((JComponent)connectionPanel);
		connectionPanel.setLayout(connectionPanelLayout);
		GroupLayout settingsPanelLayout = new GroupLayout((JComponent)settingsPanel);
		settingsPanel.setLayout(settingsPanelLayout);
		
		buttonPanel.setBorder(BorderFactory.createTitledBorder(""));
		
		connectionPanel.setBorder(BorderFactory.createTitledBorder(configed.getResourceValue("SSHConnection.Config.serverPanelTitle")));
		connectionPanel.setPreferredSize(new java.awt.Dimension(400, 350));
		
		settingsPanel.setBorder(BorderFactory.createTitledBorder(configed.getResourceValue("SSHConnection.Config.settingsPanelTitle")));
		{
			lbl_host = new JLabel();
			lbl_host.setText(configed.getResourceValue("SSHConnection.Config.jLabelHost"));
			
			cb_host = new JComboBox<String>();
			String host = connection.getConnectedHost();
			if (host == null) host = configedMain.HOST;
			
			cb_host.addItem(host);
				
			PersistenceController persist = PersistenceControllerFactory.getPersistenceController();
			Set<String> depots = persist.getDepotPropertiesForPermittedDepots().keySet();
			depots.remove(host); //remove login host name if identical with depot fqdn
			for (String depot : depots)
			{
				cb_host.addItem( depot );
			}
			// cb_host.setEditable(true);
		
			logging.debug(this, "init host " + host);
			cb_host.setSelectedItem(host);
			
			cb_host.addItemListener(new ItemListener()
			{
				@Override
				public void itemStateChanged(ItemEvent e) 
				{
					checkComponentStates();
				}
			});
		}
		{
			lbl_port = new JLabel();
			lbl_port.setText(configed.getResourceValue("SSHConnection.Config.jLabelPort"));
			tf_port = new JTextField( new CheckedDocument(/*allowedChars*/
				 new char[] { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', } ,5 ),
				String.valueOf("22"), 1 );
			tf_port.getDocument().addDocumentListener(new DocumentListener()
			{
				public void insertUpdate(DocumentEvent e) {
					checkComponentStates();
				}
				public void removeUpdate(DocumentEvent e) {
					checkComponentStates();
				}
				public void changedUpdate(DocumentEvent e) {
					//Plain text components do not fire these events
				}
			});
		}
		{
			lbl_user = new JLabel();
			lbl_user.setText(configed.getResourceValue("SSHConnection.Config.jLabelUser"));
			tf_user = new JTextField();
			tf_user.setText(ConfigedMain.USER);
			tf_user.getDocument().addDocumentListener(new DocumentListener()
			{
				public void insertUpdate(DocumentEvent e) {
					checkComponentStates();
				}
				public void removeUpdate(DocumentEvent e) {
					checkComponentStates();
				}
				public void changedUpdate(DocumentEvent e) {
					//Plain text components do not fire these events
				}
			});
		}
		{
			lbl_passw = new JLabel();
			lbl_passw.setText(configed.getResourceValue("SSHConnection.Config.jLabelPassword"));
			tf_passw = new JPasswordField();
			tf_passw.setText(ConfigedMain.PASSWORD);
			tf_passw.getDocument().addDocumentListener(new DocumentListener()
			{
				public void insertUpdate(DocumentEvent e) {
					checkComponentStates();
				}
				public void removeUpdate(DocumentEvent e) {
					checkComponentStates();
				}
				public void changedUpdate(DocumentEvent e) {
					//Plain text components do not fire these events
				}
			});
		}
		{
			btn_save = new IconButton( de.uib.configed.configed.getResourceValue("MainFrame.iconButtonSaveConfiguration"),
				"images/apply_over.gif", " ", "images/apply_disabled.gif",false);
			btn_close = new IconButton( de.uib.configed.configed.getResourceValue("MainFrame.iconButtonCancelChanges"),
				"images/cancel-32.png", "images/cancel_over-32.png", " ", true);

			buttonPanel.add(btn_save);
			logging.info(this, "actionlistener for button1 " + Globals.isGlobalReadOnly() );
			if (!(Globals.isGlobalReadOnly()))
			{
				btn_save.addActionListener(new ActionListener()
				{
					public void actionPerformed(ActionEvent e)
					{
						logging.debug(this, "actionPerformed on button1");
						doAction1();
					}
				});
			}
		}
			{
			btn_openChooser = new IconButton( de.uib.configed.configed.getResourceValue("MainFrame.iconOpenFileChooser"),
				"images/folder_16.png", " ", "images/folder_16.png",true);
			// btn_openChooser.setPreferredSize(Globals.smallButtonDimension);
			btn_openChooser.setPreferredSize(new Dimension(Globals.buttonWidth/4, Globals.buttonHeight));

			// buttonPanel.add(btn_openChooser);
			if (!(Globals.isGlobalReadOnly()))
				btn_openChooser.addActionListener(new ActionListener()
				{
					public void actionPerformed(ActionEvent e)
					{
						doActionOeffnen();
					}
				});
		}
		{
			buttonPanel.add(btn_close);
			btn_close.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					doAction2();
				}
			});
		}
		{
			lbl_keyfile = new JLabel();
			lbl_keyfile.setText(configed.getResourceValue("SSHConnection.Config.jLabelKeyfile"));
			tf_keyfile = new JTextField();
			tf_keyfile.setText(configedMain.SSHKEY);
		}
		{
			lbl_passphrase = new JLabel();
			lbl_passphrase.setText(configed.getResourceValue("SSHConnection.Config.jLabelPassphrase"));
			tf_passphrase = new JPasswordField();
			tf_passphrase.setEnabled(false);
			tf_passphrase.setText(configedMain.SSHKEYPASS);
			tf_passphrase.getDocument().addDocumentListener(new DocumentListener()
			{
				public void insertUpdate(DocumentEvent e) {
					checkComponentStates();
				}
				public void removeUpdate(DocumentEvent e) {
					checkComponentStates();
				}
				public void changedUpdate(DocumentEvent e) {
					//Plain text components do not fire these events
				}
			});
		}
		{
			cb_useKeyfile = new JCheckBox();
			cb_useKeyfile.setText(configed.getResourceValue("SSHConnection.Config.useKeyfile"));
			cb_useKeyfile.setSelected(false);
			tf_passw.setEnabled(false);
			tf_keyfile.setEnabled(false);
			cb_useKeyfile.addItemListener(new ItemListener() 
			{
				@Override
				public void itemStateChanged(ItemEvent e) 
				{
					Boolean value = false;
					if(e.getStateChange() == ItemEvent.SELECTED) 
					{
						value = true;
					}
					if (!cb_useDefault.isSelected())
						tf_passw.setEnabled(!value);
					btn_openChooser.setEnabled(value);
					tf_keyfile.setEnabled(value);
					tf_passphrase.setEnabled(value);
					checkComponentStates();
				}
			});
		}
		{
			cb_useDefault = new JCheckBox();
			cb_useDefault.setText(configed.getResourceValue("SSHConnection.Config.useDefaultAuthentication"));
			cb_useDefault.setSelected(true);
			setComponentsEditable(false);
			cb_useDefault.addItemListener(new ItemListener() 
			{
				@Override
				public void itemStateChanged(ItemEvent e) 
				{
					if(e.getStateChange() == ItemEvent.SELECTED) 
					{
						setComponentsEditable(false);
						cb_host.setSelectedItem(connection.getConnectedHost());
						tf_user.setText(connection.getConnectedUser());
						tf_passw.setText(connection.getConnectedPw());
						tf_port.setText(connection.getConnectedPort());
					}
					else setComponentsEditable(true);
					checkComponentStates();
				}
			});
			if (configedMain.SSHKEY != null)
			{
				cb_useKeyfile.setSelected(true);
				if (configedMain.SSHKEYPASS != null)
				{
					btn_openChooser.setEnabled(true);
					tf_passphrase.setEnabled(true);
					tf_passphrase.setText(ConfigedMain.SSHKEYPASS);
					connection.useKeyfile(true, tf_keyfile.getText(), new String(tf_passphrase.getPassword()));
				}
				connection.useKeyfile(true, tf_keyfile.getText());
				connection.setUserData((String) cb_host.getSelectedItem(), tf_user.getText(), new String(tf_passw.getPassword()), tf_port.getText() );
			}
			cb_useOuputColor = new JCheckBox();
			cb_useOuputColor.setText(configed.getResourceValue("SSHConnection.Config.coloredOutput"));
			cb_useOuputColor.setToolTipText(configed.getResourceValue("SSHConnection.Config.coloredOutput.tooltip"));
			cb_useOuputColor.setSelected(true);
			SSHCommandFactory.getInstance().ssh_colored_output = true;
			cb_useOuputColor.addItemListener(new ItemListener() 
			{
				@Override
				public void itemStateChanged(ItemEvent e) 
				{
					checkComponentStates();
				}
			});

			cb_execInBackground = new JCheckBox();
			cb_execInBackground.setText(configed.getResourceValue("SSHConnection.Config.AlwaysExecBackground"));
			cb_execInBackground.setToolTipText(configed.getResourceValue("SSHConnection.Config.AlwaysExecBackground.tooltip"));
			cb_execInBackground.setSelected(SSHCommandFactory.getInstance().ssh_always_exec_in_background);
			cb_execInBackground.addItemListener(new ItemListener() 
			{
				@Override
				public void itemStateChanged(ItemEvent e) 
				{
					checkComponentStates();
				}
			});
		}
		logging.debug(this, "sshConfigDialog building layout ");
		connectionPanelLayout.setHorizontalGroup(connectionPanelLayout.createSequentialGroup()
			.addGroup(connectionPanelLayout.createParallelGroup()
				.addComponent(cb_useDefault, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
				.addGroup(connectionPanelLayout.createSequentialGroup()
					.addGap(Globals.vGapSize*2)
					.addGroup(connectionPanelLayout.createParallelGroup()
						.addComponent(lbl_host,GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(lbl_port,GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(lbl_user,GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(lbl_passw,GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
						)
					.addGap(Globals.vGapSize)
					.addGroup(connectionPanelLayout.createParallelGroup()
						.addComponent(cb_host,GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
						.addComponent(tf_port,GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
						.addComponent(tf_user,GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
						.addComponent(tf_passw,GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
					)
					.addGap(Globals.vGapSize)
				)
				.addGap(Globals.vGapSize)
				.addComponent(cb_useKeyfile, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
				.addGroup(connectionPanelLayout.createSequentialGroup()
					.addGap(Globals.vGapSize*2)
					.addGroup(connectionPanelLayout.createParallelGroup()
						.addComponent(lbl_keyfile,GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(lbl_passphrase,GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
					)
					.addGap(Globals.vGapSize)
					.addGroup(connectionPanelLayout.createParallelGroup()
						.addGroup(connectionPanelLayout.createSequentialGroup()
							.addComponent(tf_keyfile,GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
							.addComponent(btn_openChooser,GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
						)
						.addComponent(tf_passphrase,GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
					)
					// .addGroup(connectionPanelLayout.createParallelGroup()
					// )
					.addGap(Globals.vGapSize)
				)
				.addComponent(lbl_connectionState, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
			)
			.addContainerGap()
		);
	
		connectionPanelLayout.setVerticalGroup(connectionPanelLayout.createSequentialGroup()
			.addGap(Globals.vGapSize)
			.addComponent(cb_useDefault, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
			.addGap(Globals.vGapSize)
			.addGroup(connectionPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
				.addComponent(cb_host, GroupLayout.Alignment.BASELINE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
				.addComponent(lbl_host, GroupLayout.Alignment.BASELINE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
			)
			.addGap(Globals.vGapSize)
			.addGroup(connectionPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
				.addComponent(tf_port, GroupLayout.Alignment.BASELINE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
				.addComponent(lbl_port, GroupLayout.Alignment.BASELINE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
			)
			.addGap(Globals.vGapSize)
			.addGroup(connectionPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
				.addComponent(tf_user, GroupLayout.Alignment.BASELINE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
				.addComponent(lbl_user, GroupLayout.Alignment.BASELINE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
			)
			.addGap(Globals.vGapSize)
			.addGroup(connectionPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
				.addGap(Globals.vGapSize)
				.addComponent(tf_passw, GroupLayout.Alignment.BASELINE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
				.addComponent(lbl_passw, GroupLayout.Alignment.BASELINE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
				.addGap(Globals.vGapSize)
			)
			.addGap(Globals.vGapSize)
			.addComponent(cb_useKeyfile, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
			.addGap(Globals.vGapSize)
			.addGroup(connectionPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
				.addGap(Globals.vGapSize)
				.addComponent(btn_openChooser, GroupLayout.Alignment.BASELINE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
				.addComponent(tf_keyfile, GroupLayout.Alignment.BASELINE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
				.addComponent(lbl_keyfile, GroupLayout.Alignment.BASELINE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
				.addGap(Globals.vGapSize)
			)
			.addGap(Globals.vGapSize)
			.addGroup(connectionPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
				.addGap(Globals.vGapSize)
				.addComponent(tf_passphrase, GroupLayout.Alignment.BASELINE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
				.addComponent(lbl_passphrase, GroupLayout.Alignment.BASELINE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
				.addGap(Globals.vGapSize)
			)
			.addGap(Globals.vGapSize)
			.addComponent(lbl_connectionState, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
			.addGap(Globals.vGapSize)
			.addContainerGap(70, 70)
		);

		settingsPanelLayout.setHorizontalGroup(settingsPanelLayout.createSequentialGroup()
			.addGroup(settingsPanelLayout.createParallelGroup()
				.addComponent(cb_useOuputColor,GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE )
				.addComponent(cb_execInBackground,GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE )
			)
		);
		settingsPanelLayout.setVerticalGroup(settingsPanelLayout.createSequentialGroup()
			.addComponent(cb_useOuputColor, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
			.addGap(10)
			.addComponent(cb_execInBackground, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
		);
	}

	private void setComponentsEnabled_RO(boolean value)
	{
		cb_useDefault.setEnabled(value);
		cb_useOuputColor.setEnabled(value);		
		cb_execInBackground.setEnabled(value);
		setComponentsEditable(value);
	}

	public void setComponentsEditable(boolean value)
	{
		cb_host.setEnabled(value);
		tf_port.setEnabled(value);
		tf_user.setEnabled(value);
		if ( cb_useKeyfile.isSelected() )
			tf_passw.setEnabled(false);
		else 
		{
			tf_passw.setEnabled(value);
		}
		if (cb_useDefault.isSelected())
			tf_passw.setEnabled(false);
	}



	/* This method is called when button 1 is pressed */
	public void doAction1()
	{
		logging.info(this, "doAction1  " );
		
		if (cb_useDefault.isSelected() )
		{
			logging.info(this, "doAction1  cb_useDefault.isSelected true");
			connection.setUserData(null, null, null, null);
			cb_host.setSelectedItem(connection.getConnectedHost());
			tf_user.setText(connection.getConnectedUser());
			tf_port.setText(connection.getConnectedPort());
			tf_passw.setText(connection.getConnectedPw());
		}
		else 
		{
			logging.info(this, "doAction1  cb_useDefault.isSelected false");
			String host = (String) cb_host.getSelectedItem();
			logging.info(this, "doAction1 host " + host);
			// if(((DefaultComboBoxModel)cb_host.getModel()).getIndexOf(host) == -1 )
			// 	cb_host.addItem(host);

			connection.setUserData(host, tf_user.getText(), new String(tf_passw.getPassword()), tf_port.getText() );
		}
		connection.useKeyfile(false);
		if (cb_useKeyfile.isSelected())
		{
			logging.info(this, "doAction1  cb_useKeyfile.isSelected true");
			logging.info(this, "set keyfile true keyfile " + tf_keyfile.getText());
			connection.useKeyfile(true, tf_keyfile.getText(), new String(tf_passphrase.getPassword()));
			tf_passw.setText("");
			connection.setPw("");
		}
		SSHCommandFactory factory = SSHCommandFactory.getInstance(configedMain);

		factory.testConnection(connection) ;
		// factory.testConnection(tf_user.getText(), (String) cb_host.getSelectedItem()) ;
		factory.ssh_colored_output = cb_useOuputColor.isSelected();
		factory.ssh_always_exec_in_background = cb_execInBackground.isSelected();
		cb_useDefault_state = cb_useDefault.isSelected();
		checkComponentStates();
		logging.info(this, "request focus");
	}
	/* This method gets called when button 2 is pressed */
	public void doAction2()
	{
		logging.info(this, "doAction2 cb_host.getSelectedItem() " + cb_host.getSelectedItem());
		
		super.doAction2();
	}
	public void doActionOeffnen()
	{
		final JFileChooser chooser = new JFileChooser("Verzeichnis wählen");
		chooser.setDialogType(JFileChooser.OPEN_DIALOG);
		chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
		// final File file = new File("/home");
		// chooser.setCurrentDirectory(file);
		String userDirLocation = System.getProperty("user.home");
		File userDir = new File(userDirLocation);
		// default to user directory
		chooser.setCurrentDirectory(userDir);

		chooser.setFileHidingEnabled(false);
		chooser.addPropertyChangeListener(new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent e)
			{
				if (e.getPropertyName().equals(JFileChooser.SELECTED_FILE_CHANGED_PROPERTY)
				    || e.getPropertyName().equals(JFileChooser.DIRECTORY_CHANGED_PROPERTY)) {
					final File f = (File) e.getNewValue();
				}
			}
		});

		chooser.setVisible(true);
		final int result = chooser.showOpenDialog(((Component) this));

		if (result == JFileChooser.APPROVE_OPTION) {
			File inputVerzFile = chooser.getSelectedFile();
			String inputVerzStr = inputVerzFile.getPath();
			tf_keyfile.setText(inputVerzStr);
		}
		logging.info(this, "doActionOeffnen canceled");
		chooser.setVisible(false);
	}
	private static void checkComponents()
	{
		if (cb_useDefault.isSelected() )
		{
			connection.setUserData(null, null, null, null);
		}
		cb_host.setSelectedItem(connection.getConnectedHost());
		tf_user.setText(connection.getConnectedUser());
		tf_passw.setText(connection.getConnectedPw());
		tf_port.setText(connection.getConnectedPort());
	}
}
