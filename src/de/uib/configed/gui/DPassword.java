/*
 * configed - configuration editor for client work stations in opsi
 * (open pc server integration) www.opsi.org
 *
 * Copyright (C) 2000-2016 uib.de
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 */

package de.uib.configed.gui;

import de.uib.configed.*;
import java.awt.*;
import javax.swing.*;
import javax.swing.event.*;
import java.awt.event.*;
import java.net.*;
import javax.swing.border.*;
import java.io.*;
import java.util.*;
import java.text.MessageFormat;
import de.uib.utilities.thread.WaitCursor;
import de.uib.utilities.logging.*;
import de.uib.utilities.swing.*;
import de.uib.opsidatamodel.*;
import de.uib.opsicommand.ConnectionState;



/**
 *  DPassword
 * description: A JDialog for logging in
 * copyright:     Copyright (c) 2000-2016
 * organization: uib.de
 * @author  D. Oertel; R. Roeder 
 */
public class DPassword extends JDialog //implements Runnable
{
	private final String TESTSERVER = "";
	private final String TESTUSER = "";
	private final String TESTPASSWORD = "";
	private final static int SECS_WAIT_FOR_CONNECTION = 100;
	final long TIMEOUT_MS = SECS_WAIT_FOR_CONNECTION * 1000; //5000 reproducable error 
	private boolean localApp;
	
	private final long ESTIMATED_TOTAL_WAIT_MILLIS = 10000;
	
	
	ConfigedMain main; //controller
	PersistenceController persis;
	Cursor saveCursor;
	
	
	
	class WaitInfo extends JFrame
		implements de.uib.utilities.thread.WaitingSleeper
	{
		JLabel waitLabel;
		JProgressBar waitingProgressBar;
		long timeOutMillis;
		
		WaitInfo( long timeOutMillis )
		{
			logging.info(this, "created with timeout " + timeOutMillis);
			this.timeOutMillis = timeOutMillis;
			
			setIconImage (Globals.mainIcon);
			
			
			
			addWindowListener ( new WindowAdapter()
										 {
											 public void windowClosing (WindowEvent e)
											 {
											 	 if (persis != null)
											 	 {
													 if ( persis.getConnectionState().getState() == ConnectionState.STARTED_CONNECTING )
														 persis.setConnectionState(  new ConnectionState (ConnectionState.INTERRUPTED)  ); //we stop the connect thread as well
												}
												
												 setCursor(saveCursor);
												 //System.out.println ("set " + persi.getConnectionState());
	
											 }
										 });
			//setSize (350,100);
			setTitle(Globals.APPNAME + " login");
			waitLabel = new JLabel();
			//waitLabel.setPreferredSize (new Dimension (200, 25));
			waitLabel.setText (configed .getResourceValue("DPassword.WaitInfo.label"));
				
			waitingProgressBar = new JProgressBar();
			//waitingProgressBar.setPreferredSize (new Dimension (100, 10));
			//waitingProgressBar.setToolTipText( configed.getResourceValue("FStartWakeOnLan.timeElapsed.toolTip") );
			waitingProgressBar.setValue(0);
			waitingProgressBar.setEnabled(true);
			//waitingProgressBar.setMaximum(max);
	
			UIDefaults defaults = new UIDefaults();
			defaults.put("ProgressBar[Enabled].foregroundPainter", new ProgressBarPainter( de.uib.configed.Globals.opsiLogoBlue ));
			defaults.put("ProgressBar[Enabled].backgroundPainter", new ProgressBarPainter( de.uib.configed.Globals.opsiLogoLightBlue ));
			waitingProgressBar.putClientProperty("Nimbus.Overrides", defaults);

			JPanel cPanel = new JPanel();  
			GroupLayout cLayout = new GroupLayout(cPanel);
			cPanel.setLayout(cLayout);
			cLayout.setVerticalGroup(cLayout.createSequentialGroup()
				.addGap(Globals.vGapSize)
				.addGroup(cLayout.createSequentialGroup()
					.addGap(Globals.vGapSize/2, Globals.vGapSize, Globals.vGapSize)
					.addComponent(waitingProgressBar,  Globals.progressBarHeight, Globals.progressBarHeight, Globals.progressBarHeight)
					.addGap(Globals.vGapSize/2, Globals.vGapSize, Globals.vGapSize)
					.addComponent(waitLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
					.addGap(Globals.vGapSize/2, Globals.vGapSize, Globals.vGapSize)
					)
				.addGap(Globals.vGapSize/2)
			);
			cLayout.setHorizontalGroup(cLayout.createSequentialGroup()
				.addGap(Globals.hGapSize/2)
				.addGroup(cLayout.createParallelGroup()
					.addGroup(cLayout.createSequentialGroup()
						.addGap(Globals.hGapSize, Globals.hGapSize, Short.MAX_VALUE)
						.addComponent(waitingProgressBar, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
						.addGap(Globals.hGapSize, Globals.hGapSize, Short.MAX_VALUE)
					)
					.addGroup(cLayout.createSequentialGroup()
							.addGap(Globals.hGapSize, Globals.hGapSize, Short.MAX_VALUE)
							.addComponent(waitLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
							.addGap(Globals.hGapSize, Globals.hGapSize, Short.MAX_VALUE)
					)
				)
				.addGap(Globals.hGapSize/2)
			);
			
			getContentPane().add(cPanel);
				
			/*
			getContentPane().setLayout(new FlowLayout());
			getContentPane().add(waitingProgressBar);
			getContentPane().add(waitLabel);
			*/
			
			pack();
			//setVisible(true);
			setAlwaysOnTop(true);
		}
		
		
		//interface WaitingSleeper
	
		public void actAfterWaiting()
		{
			//setCursor(saveCursor);
			waitCursor.stop();
			
			if (PersistenceControllerFactory.getConnectionState().getState() == ConnectionState.CONNECTED)
			//if ( persis.getConnectionState().getState() == ConnectionState.CONNECTED )
			{
				//we can finish
				logging.info(this, "connected with persis " + persis);
				
			
			
				main.setPersistenceController (persis);
		
				MessageFormat messageFormatMainTitle = new MessageFormat( configed.getResourceValue("ConfigedMain.appTitle") );
				main.setAppTitle(
					messageFormatMainTitle.format(
						new Object[] { Globals.APPNAME, fieldHost.getSelectedItem(), fieldUser.getText() } ) );
				this.setVisible(false);
				main.loadDataAndGo();
			}
			else 
			{
				setVisible(true);
				if (PersistenceControllerFactory.getConnectionState().getState() == ConnectionState.INTERRUPTED)
				//if  (persis.getConnectionState().getState() == ConnectionState.INTERRUPTED )
				{
					// return to password dialog
					logging.info(this, "interrupted");
				}
				else
				{
					logging.info(this, "not connected, timeout or not authorized");
					
					
					MessageFormat messageFormatDialogContent = new MessageFormat(
								configed.getResourceValue("DPassword.noConnectionMessageDialog.content") );
				
					if (
						waitingTask != null
						&& waitingTask.isTimeoutReached()
					)
						messageFormatDialogContent = new MessageFormat("Timeout in connecting");
						
		
					JOptionPane.showMessageDialog(
						this,
						messageFormatDialogContent.format(
							new Object[] { PersistenceControllerFactory.getConnectionState().getMessage() } ),
						configed.getResourceValue("DPassword.noConnectionMessageDialog.title"),
						JOptionPane.INFORMATION_MESSAGE);
				}
		
		
				passwordField.setText("");
				if (PersistenceControllerFactory.getConnectionState().getMessage().indexOf ("authorized") > -1 )
				//if (persis.getConnectionState().getMessage().indexOf ("authorized") > -1 )
				{
					logging.info(this, "(not) authorized");
					
					fieldUser.requestFocus();
					fieldUser.setCaretPosition(fieldUser.getText().length());
				}
				else
				{
					fieldHost.requestFocus();
				}
				
				activate();
			}
		}
			
		public JProgressBar getProgressBar()
		{
			return waitingProgressBar;
		}
		public JLabel getLabel()
		{
			return waitLabel;
		}
		
		public long getStartActionMillis()
		{
			return new GregorianCalendar().getTimeInMillis();
		}
		public long getWaitingMillis()
		{
			return timeOutMillis;
		}
		public long getOneProgressBarLengthWaitingMillis()
		{
			return ESTIMATED_TOTAL_WAIT_MILLIS;
		}
		public String setLabellingStrategy(long millisLevel)
		{
			return waitLabel.getText();
		}
	
		
	}
	
 
			
	WaitCursor waitCursor;
	boolean connected = false;
	Globals dm;

	Dimension screenSize;
	WaitInfo waitInfo; 
	de.uib.utilities.thread.WaitingWorker waitingTask;
	
	JPanel panel;
	
	Containership containership;
	
	int defaultBlinkRate;
	
	JLabel jLabelVersion = new JLabel();

	JLabel jLabelUser = new JLabel();
	JTextField fieldUser = new JTextField();

	JPasswordField passwordField = new JPasswordField();
	JLabel jLabelPassword = new JLabel();

	JLabel jLabelHost = new JLabel();
	JComboBox fieldHost = new JComboBox();

	JPanel jPanelParameters1;
	JPanel jPanelParameters2;

	JPanel jPanelButtons = new JPanel();
	FlowLayout flowLayoutButtons = new FlowLayout();

	JButton jButtonCommit = new JButton();
	JButton jButtonCancel = new JButton();

	TitledBorder titledBorder1;

	JPanel jPanelRadioButtons = new JPanel();
	FlowLayout flowLayoutRadioButtons = new FlowLayout();
	//JRadioButton jRadioButton_ssh = new JRadioButton();
	JRadioButton jRadioButton_ssh2 = new JRadioButton();
	//JRadioButton jRadioButton_ftp = new JRadioButton();
	JRadioButton jRadioButton_localfs = new JRadioButton();

	//myAuthenticator myAuth = new myAuthenticator();
	MyKeyListener myKeyListener = new MyKeyListener(this);
	ButtonGroup buttonGroup1 = new ButtonGroup();

	public void setHost(String host)
	{
		if (host == null)
			host = "";
		fieldHost.setSelectedItem(host);
		fieldUser.requestFocus();

	}
	
	public void setServers(Vector<String> hosts)
	{
		fieldHost.setModel(new DefaultComboBoxModel( hosts ));
		((JTextField)fieldHost.getEditor().getEditorComponent()).
			setCaretPosition(((String)(fieldHost.getSelectedItem())).length());
	}
		

	public void setUser(String user)
	{
		if (user == null)
			user = "";
		fieldUser.setText(user);
		passwordField.requestFocus();
	}

	public void setPassword(String password)
	{
		if (password == null)
			password = "";
		passwordField.setText(password);
	}

	public DPassword(Frame frame, String title, boolean modal, ConfigedMain main )
	{
		super(frame, title, modal);
		this.main = main;
		
		
		screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		
		try
		{
			guiInit();
			pack();
			setVisible(true);
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
	}

	public DPassword( ConfigedMain main  )
	{
		this(null, "", false, main);
	}

	public DPassword(Frame frame, ConfigedMain main  )
	{
		this(frame, "", false, main);
	}

	static void addComponent( Container cont,
	                          GridBagLayout gbl,
	                          Component c,
	                          int x, int y,
	                          int width, int height,
	                          double weightx, double weighty )
	{
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.fill = GridBagConstraints.BOTH;
		gbc.gridx = x; gbc.gridy = y;
		gbc.gridwidth = width; gbc.gridheight = height;
		gbc.weightx = weightx; gbc.weighty = weighty;
		gbl.setConstraints( c, gbc );
		cont.add( c );
	}


	public void activate()
	{
		logging.info(this, "activate");
		
		/*
		if (waitInfo != null)
		{
			//klll old waitInfo
			waitInfo.dispose();
			waitInfo = null;
		}
		*/
		
		panel.setEnabled(true);
		jButtonCommit.setEnabled(true);
	}

	void guiInit() throws Exception
	{
		MessageFormat messageFormatTitle = new MessageFormat( configed.getResourceValue("DPassword.title") );
		setTitle( messageFormatTitle.format( new Object[] { Globals.APPNAME } ) );


		setIconImage (Globals.mainIcon);

		titledBorder1 = new TitledBorder("");


		panel = new JPanel();
		panel.setEnabled(false);
		//panel.setMinimumSize(new Dimension(450, 250));
		//panel.setPreferredSize(new Dimension(450, 250));
		//panel.setToolTipText("");

		Border padding = BorderFactory.createEmptyBorder(10,10,10,10);
		panel.setBorder(padding);

		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		this.setLocation((screenSize.width - 450) / 2, 200);

		jLabelHost.setText(configed.getResourceValue("DPassword.jLabelHost") );
		
		
		containership = new Containership(panel);
		
		defaultBlinkRate = fieldUser.getCaret().getBlinkRate();

		//fieldHost.setMargin(new Insets(0, 3, 0, 3));
		fieldHost.setEditable(true);
		fieldHost.setSelectedItem("");
		fieldHost.addKeyListener(myKeyListener);

		jLabelUser.setText( configed.getResourceValue("DPassword.jLabelUser") );

		fieldUser.setText(TESTUSER);
		fieldUser.addKeyListener(myKeyListener);
		//fieldUser.setText(System.getProperty("user.name"));
		//System.out.println(passwordField.getPassword());
		fieldUser.setMargin(new Insets(0, 3, 0, 3));

		jLabelPassword.setText( configed.getResourceValue("DPassword.jLabelPassword") );

		passwordField.setText(TESTPASSWORD);
		passwordField.addKeyListener(myKeyListener);
		passwordField.setMargin(new Insets(0, 3, 0, 3));
		/*passwordField.addComponentListener(new java.awt.event.ComponentAdapter()
	{
			public void componentShown(ComponentEvent e)
			{
				passwordField_componentShown(e);
			}
	});*/


		JCheckBox checkGzip = new JCheckBox(configed.getResourceValue("DPassword.checkGzip") , de.uib.opsicommand.JSONthroughHTTP.gzipTransmission);
		checkGzip.addItemListener(new ItemListener(){
			                          public void itemStateChanged(ItemEvent e)
			                          {
				                          //logging.debug(this, "itemStateChanged " + e);
				                          de.uib.opsicommand.JSONthroughHTTP.gzipTransmission =
				                              (e.getStateChange() == ItemEvent.SELECTED);

				                          logging.debug(this, "itemStateChanged " + de.uib.opsicommand.JSONthroughHTTP.gzipTransmission);;
			                          }
		                          });

		final JTextField fieldRefreshMinutes = new JTextField("" + configed.refreshMinutes);
		fieldRefreshMinutes.setToolTipText(configed.getResourceValue("DPassword.pullReachableInfoTooltip"));
		fieldRefreshMinutes.setPreferredSize(new Dimension(Globals.shortlabelDimension));
		fieldRefreshMinutes.setHorizontalAlignment(JTextField.RIGHT);
		fieldRefreshMinutes.getDocument().addDocumentListener(new DocumentListener(){

			        private void setRefreshMinutes()
			        {
				        String s = fieldRefreshMinutes.getText();

				        try{
					        configed.refreshMinutes = Integer.valueOf(s);

				        }
				        catch(NumberFormatException ex)
				        {
					        fieldRefreshMinutes.setText("");
				        }
			        }

			        public void changedUpdate(DocumentEvent e)
			        {
				        //logging.debug(this, "++ changedUpdate on " );
				        setRefreshMinutes();
			        }

			        public void insertUpdate(DocumentEvent e)
			        {
				        //logging.debug(this, "++ insertUpdate on " );
				        setRefreshMinutes();
			        }

			        public void removeUpdate(DocumentEvent e)
			        {
				        //logging.debug(this, "++ removeUpdate on " );
				        setRefreshMinutes();
			        }
		        }
		                                                     );


		jPanelParameters1= new PanelLinedComponents(new JComponent[]
		                   {
		                       checkGzip
		                   }
		                                           );
		jPanelParameters2= new PanelLinedComponents(new JComponent[]
		                   {
		                       new JLabel(configed.getResourceValue("DPassword.pullReachableInfo"))
		                       , fieldRefreshMinutes
		                       , new JLabel(configed.getResourceValue("DPassword.pullReachableInfoMinutes"))
		                   }
		                                           );


		jPanelButtons.setLayout(flowLayoutButtons);

		jButtonCommit.setText( configed.getResourceValue("DPassword.jButtonCommit") );
		jButtonCommit.setMaximumSize(new Dimension(100, 20));
		jButtonCommit.setPreferredSize(new Dimension(100, 20));
		jButtonCommit.setToolTipText("");
		jButtonCommit.setSelected(true);
		jButtonCommit.addActionListener(new java.awt.event.ActionListener()
		                                {
			                                public void actionPerformed(ActionEvent e)
			                                {
				                                jButtonCommit_actionPerformed(e);
			                                }
		                                });

		jButtonCancel.setText( configed.getResourceValue("DPassword.jButtonCancel") );
		jButtonCancel.setMaximumSize(new Dimension(100, 20));
		jButtonCancel.setPreferredSize(new Dimension(100, 20));
		jButtonCancel.setToolTipText("");
		jButtonCancel.addActionListener(new java.awt.event.ActionListener()
		                                {
			                                public void actionPerformed(ActionEvent e)
			                                {
				                                jButtonCancel_actionPerformed(e);
			                                }
		                                });

		jPanelButtons.add(jButtonCommit);

		//if (!configed.isApplet)
		jPanelButtons.add(jButtonCancel);

		//jPanelRadioButtons.setLayout(flowLayoutRadioButtons);
		//jPanelRadioButtons.add(jRadioButton_ssh2);
		//jPanelRadioButtons.add(jRadioButton_localfs);

		//							x  y  w  h  wx   wy




		GroupLayout gpl = new GroupLayout(panel);
		panel.setLayout(gpl);

		gpl.setVerticalGroup(gpl.createSequentialGroup()
		                     .addComponent(jLabelVersion, GroupLayout.PREFERRED_SIZE,  GroupLayout.PREFERRED_SIZE,  GroupLayout.PREFERRED_SIZE)
		                     .addGap(Globals.lineHeight)
		                     .addComponent(jLabelHost, GroupLayout.PREFERRED_SIZE,  GroupLayout.PREFERRED_SIZE,  GroupLayout.PREFERRED_SIZE)
		                     .addGap(2)
		                     .addComponent(fieldHost, Globals.lineHeight, Globals.lineHeight, Globals.lineHeight)
		                     .addGap(Globals.lineHeight)
		                     .addComponent(jLabelUser, GroupLayout.PREFERRED_SIZE,  GroupLayout.PREFERRED_SIZE,  GroupLayout.PREFERRED_SIZE)
		                     .addGap(2)
		                     .addComponent(fieldUser, Globals.lineHeight, Globals.lineHeight, Globals.lineHeight)
		                     .addGap(2)
		                     .addComponent(jLabelPassword, GroupLayout.PREFERRED_SIZE,  GroupLayout.PREFERRED_SIZE,  GroupLayout.PREFERRED_SIZE)
		                     .addGap(2)
		                     .addComponent(passwordField, Globals.lineHeight, Globals.lineHeight, Globals.lineHeight)
		                     .addGap(Globals.lineHeight)
		                     .addComponent(jPanelParameters1, (int) (1.2 * Globals.lineHeight), (int) (1.2 * Globals.lineHeight),  (int) (1.2 * Globals.lineHeight))
		                     .addComponent(jPanelParameters2, (int) (1.2 * Globals.lineHeight), (int) (1.2 * Globals.lineHeight),  (int) (1.2 * Globals.lineHeight))
		                     .addGap(Globals.lineHeight)
		                     .addComponent(jPanelButtons, GroupLayout.PREFERRED_SIZE,  GroupLayout.PREFERRED_SIZE,  GroupLayout.PREFERRED_SIZE)
		                    );

		gpl.setHorizontalGroup(gpl.createParallelGroup()
		                       .addGroup(gpl.createSequentialGroup()
		                                 .addGap(Globals.hGapSize, 40, Short.MAX_VALUE)
		                                 .addComponent(jLabelVersion, GroupLayout.PREFERRED_SIZE,  GroupLayout.PREFERRED_SIZE,  GroupLayout.PREFERRED_SIZE)
		                                 .addGap(Globals.hGapSize, 40, Short.MAX_VALUE)
		                                )
		                       .addGroup(gpl.createSequentialGroup()
		                                 .addGap(Globals.vGapSize)
		                                 .addComponent(jLabelHost, GroupLayout.PREFERRED_SIZE,  GroupLayout.PREFERRED_SIZE,  GroupLayout.PREFERRED_SIZE)
		                                 )
		                       .addGroup(gpl.createSequentialGroup()
		                                 .addComponent(fieldHost, GroupLayout.PREFERRED_SIZE,  GroupLayout.PREFERRED_SIZE,  Short.MAX_VALUE)
		                                 )
		                       .addGroup(gpl.createSequentialGroup()
		                                 .addGap(Globals.vGapSize)  
		                                 .addComponent(jLabelUser, GroupLayout.PREFERRED_SIZE,  GroupLayout.PREFERRED_SIZE,  GroupLayout.PREFERRED_SIZE)
		                                 )
		                       .addGroup(gpl.createSequentialGroup()
		                                 .addComponent(fieldUser, GroupLayout.PREFERRED_SIZE,  GroupLayout.PREFERRED_SIZE,  Short.MAX_VALUE)
		                                 )
		                       .addGroup(gpl.createSequentialGroup()
		                                 .addGap(Globals.vGapSize)
		                                 .addComponent(jLabelPassword, GroupLayout.PREFERRED_SIZE,  GroupLayout.PREFERRED_SIZE,  GroupLayout.PREFERRED_SIZE)
		                                 )
		                       .addGroup(gpl.createSequentialGroup()
		                                 .addComponent(passwordField, GroupLayout.PREFERRED_SIZE,  GroupLayout.PREFERRED_SIZE,  Short.MAX_VALUE)
		                                 )
		                       .addGroup(gpl.createSequentialGroup()
		                                 .addGap(Globals.vGapSize)
		                                 .addComponent(jPanelParameters1, GroupLayout.PREFERRED_SIZE,  GroupLayout.PREFERRED_SIZE,  Short.MAX_VALUE)
		                                 .addGap(Globals.vGapSize)
		                                 )
         		               .addGroup(gpl.createSequentialGroup()
		                                 .addGap(Globals.vGapSize)
		                                 .addComponent(jPanelParameters2, GroupLayout.PREFERRED_SIZE,  GroupLayout.PREFERRED_SIZE,  Short.MAX_VALUE)
		                                 )
		                      .addGroup(gpl.createSequentialGroup()
		                                 .addGap(Globals.vGapSize)
		                                 .addComponent(jPanelButtons, GroupLayout.PREFERRED_SIZE,  GroupLayout.PREFERRED_SIZE,  Short.MAX_VALUE)
		                                 )
		                      );

		this.getContentPane().add(panel);



		Containership csPanel = new Containership(getContentPane());

		csPanel.doForAllContainedCompisOfClass
		("setBackground", new Object[]{Globals.backLightBlue}, JPanel.class);

		/*
		csPanel.doForAllContainedCompis
		("setBackground", new Object[]{ java.awt.Color.yellow });//Globals.backLightBlue});
		*/
		//myAuth.setDefault(myAuth);
		//Authenticator.setDefault(myAuth);

		//fieldUser.setText(System.getProperty("user.name"));
		//jLabelVersion.setText("opsi configuration editor, version "  + Globals.VERSION + "  date " + Globals.VERDATE);
		//jLabelVersion.setText( sprintf( configed.getResourceValue("DPassword.jLabelVersion"), Globals.VERSION, Globals.VERDATE) );

		MessageFormat messageFormatVersion = new MessageFormat( configed.getResourceValue("DPassword.jLabelVersion") );
		jLabelVersion.setText( messageFormatVersion.format( new Object[] {Globals.VERSION, Globals.VERDATE, Globals.VERHASHTAG} ) );

		String strOS = System.getProperty("os.name");
		String osVersion = System.getProperty("os.version");
		logging.debugOut ( 3, " OS " + strOS + "  Version " + osVersion );
		String host= TESTSERVER; //"";
		/*
		if (strOS.startsWith("Windows") && (osVersion.compareToIgnoreCase("4.0") >= 0))
	{
		Process process = Runtime.getRuntime().exec("cmd.exe /q /c echo %HOST%");
		BufferedReader br = new BufferedReader( new InputStreamReader(process.getInputStream()));
		host = br.readLine();
		br.close();
	}
		*/

		//process = Runtime.getRuntime().exec("cmd.exe /c echo %UNAME%");
		//br = new BufferedReader( new InputStreamReader(process.getInputStream()));
		//String uname = br.readLine();

		pack();

		//System.out.println(" ----  host:" + host + "--");
		if (host.equals (""))
		{
			setHost("localhost");
			fieldHost.requestFocus();
			((JTextField)fieldHost.getEditor().getEditorComponent()).
			setCaretPosition(((String)(fieldHost.getSelectedItem())).length());
		}


		saveCursor = getCursor();
	}
	
	@Override
	public  void setCursor(Cursor c)
	{
		super.setCursor(c);
		try
		{
			containership.doForAllContainedCompis ("setCursor", new Object[]{c});
		}
		catch(Exception ex)
		{
			logging.warning(this, "containership error " + ex);
			logging.logTrace(ex);
		}
		
		/*
		if (c.equals(saveCursor))
		{
			fieldHost.getCaret().setBlinkRate(defaultBlinkRate);
			fieldUser.getCaret().setBlinkRate(defaultBlinkRate);
			passwordField.getCaret().setBlinkRate(defaultBlinkRate);
		}
		else
		{
			fieldHost.getCaret().setBlinkRate(0);
			fieldUser.getCaret().setBlinkRate(0);
			passwordField.getCaret().setBlinkRate(0);
		}
		*/
			
	}


	public void ok_action()
	{

		// we make first a waitCursor and a waitInfo window

		if (waitCursor != null)
			waitCursor.stop(); //we want only one running instance

		//waitCursor = new WaitCursor(this, "ok_action");
		//??we dont need this wait cursor instance; and it seems not to finish correctly
	
		try_connecting();
		//waitInfo.toFront();
	}
	/*
	static public PersistenceController producePersistenceController(String server)
{
		PersistenceController persis =  PersistenceControllerFactory.getNewPersistenceController(server, "", "");
		persis.setConnectionState(new ConnectionState (ConnectionState.STARTED_CONNECTING));
		persis.makeConnection();
		if ( persis.getConnectionState().getState() == ConnectionState.CONNECTED )
			return persis;
		
		return null;
}
	*/
	
	
	public void try_connecting()
	{
		logging.info(this,  "started  try_connecting");
		jButtonCommit.setEnabled(false);
		
		//saveCursor = getCursor();
		//setCursor(new Cursor(Cursor.WAIT_CURSOR));
		
		waitCursor = new WaitCursor(this, "ok_action");
		

		ConfigedMain.HOST = (String)fieldHost.getSelectedItem();
		ConfigedMain.USER = fieldUser.getText();
		ConfigedMain.PASSWORD = String.valueOf( passwordField.getPassword() );
		logging.info(this, "invoking PersistenceControllerFactory host, user, " +  
			//.password " +
			fieldHost.getSelectedItem() + ", " + fieldUser.getText()
			//+ ", " + String.valueOf( passwordField.getPassword())
			);

		if (
			waitingTask != null
			&& 
			!waitingTask.isReady()
		)
		{	
			
			logging.info(this, "old waiting task not ready");
			return;
		}
		
		
		if (waitInfo != null)
		{
			//klll old waitInfo
			waitInfo.dispose();
			waitInfo = null;
		}
		
		//final WaitInfoString waitInfoString = new WaitInfoString();
		
		waitInfo = new WaitInfo( TIMEOUT_MS );
		Dimension frameSize = waitInfo.getSize();
		
		//center
		waitInfo.setLocation ((screenSize.width - frameSize.width) / 2, (screenSize.height - frameSize.height) / 2);
		waitInfo.setVisible(true);
		
	
		logging.info(this, "we are in EventDispatchThread "  + SwingUtilities.isEventDispatchThread() );
		logging.info(this, "we are in applet "  + configed.isApplet );
		logging.info(this, "  Thread.currentThread() "  + Thread.currentThread());
		localApp = ("" + Thread.currentThread()).indexOf ("main]") > -1;
		logging.info(this, "is local app  "  + localApp);
		if ( localApp && ! configed.isApplet )
		{	
			
			waitInfo.setAlwaysOnTop(true);
			logging.info(this, "start WaitingWorker");
			waitingTask = new de.uib.utilities.thread.WaitingWorker((de.uib.utilities.thread.WaitingSleeper)waitInfo);
			//waitingTask.addPropertyChangeListener(this);
			waitingTask.execute();

		
			new Thread(){
				public void run()
				{
					logging.info(this, "get persis");
					persis =  PersistenceControllerFactory.getNewPersistenceController
						  ((String) fieldHost.getSelectedItem(), fieldUser.getText(), String.valueOf( passwordField.getPassword() ));
					
					logging.info(this, "got persis, == null " + (persis == null));
					/*
					long TIMEOUT = 100000; //ms
					long interval = 2000;
					long waited = 0;
					
					while (
							PersistenceControllerFactory.getConnectionState() == ConnectionState.ConnectionUndefined
							&& 
							waited <  TIMEOUT
						)
					{
						try
						{
							Thread.sleep(interval);
							waited = waited + interval;
							logging.info(this, "waited for persis: " + waited);
						}
						catch (Exception waitException)
						{}
					}
					
					if (waited >= TIMEOUT)
						logging.error(" no connection");
					
					*/
					logging.info(this, "waitingTask can be set to ready");
					waitingTask.setReady();
						  
				}
			}.start();
		}
		else
		{
			
			
					persis =  PersistenceControllerFactory.getNewPersistenceController
						  ((String) fieldHost.getSelectedItem(), fieldUser.getText(), String.valueOf( passwordField.getPassword() ));
						  
					
					long interval = 2000;
					long waited = 0;
					
					while (
							(PersistenceControllerFactory.getConnectionState() == ConnectionState.ConnectionUndefined)
							&& 
							waited <  TIMEOUT_MS
						)
					{
						try
						{
							Thread.sleep(interval);
							waited = waited + interval;
						}
						catch (Exception waitException)
						{}
					}
					
					if (waited >= TIMEOUT_MS)
						logging.error(" no connection");
							
					
					waitInfo.actAfterWaiting();
		}
			
	}

	
	
	

	/*
	public void try_connecting()
	{
		logging.info(this,  "started  try_connecting");
		setCursor(new Cursor(Cursor.WAIT_CURSOR));

		ConfigedMain.HOST = (String)fieldHost.getSelectedItem();
		ConfigedMain.USER = fieldUser.getText();
		ConfigedMain.PASSWORD = String.valueOf( passwordField.getPassword() );
		logging.info(this, "invoking PersistenceControllerFactory host, user, " +  
			//.password " +
			fieldHost.getSelectedItem() + ", " + fieldUser.getText()
			//+ ", " + String.valueOf( passwordField.getPassword())
			);

		if (waitInfo != null)
		{
			waitInfo.dispose();
			waitInfo = null;
		}
		
		final WaitInfoString waitInfoString = new WaitInfoString();
		
		waitInfo = new WaitInfo( 10000 );
		Dimension frameSize = waitInfo.getSize();
		
		//center
		waitInfo.setLocation ((screenSize.width - frameSize.width) / 2, (screenSize.height - frameSize.height) / 2);
		waitInfo.setVisible(true);
	
		
		waitingTask = new de.uib.utilities.thread.WaitingWorker((de.uib.utilities.thread.WaitingSleeper)waitInfo);
		//waitingTask.addPropertyChangeListener(this);
		waitingTask.execute();
		

		
		
		
		// we have to create yet another thread if we want to wait here if this call comes back 

		new Thread(){
			public void run(){
				int waitMs = 200;
				int numberOfWaitIntervals = SECS_WAIT_FOR_CONNECTION  * 1000 / waitMs;
				int countWait = 0;
		
				int i = 0;
		
				while ( 
						(
							PersistenceControllerFactory.getConnectionState().getState() == ConnectionState.UNDEFINED
							||
							PersistenceControllerFactory.getConnectionState().getState()== ConnectionState.STARTED_CONNECTING
						)
						&& countWait < numberOfWaitIntervals 
						&&  waiting)  
				{
					try
					{
						countWait++;
						Thread.sleep (waitMs);
						
						logging.info(this,  "countWait " + countWait + " waited,  we are in state " + " " + PersistenceControllerFactory.getConnectionState());
						i++;
						logging.debug(this, "waiting " + i);

						
					}
					catch (InterruptedException ex)
					{
					}
				}
				
				waiting = false;
				
				
				
				logging.info(this,  "countWait " + countWait + " waited,  we got to state " + " " + PersistenceControllerFactory.getConnectionState());
			}
		}.start();
		
		if (waitInfo == null)
			logging.info(this,  "waitInfo null");
			
		else
				{
					setVisible(false);
					logging.info(this,  "waitInfo set visible");
					waitInfo.setVisible(true);
					//waitInfo.dispose();
					//waitInfo = null;
				}

		persis =  PersistenceControllerFactory.getNewPersistenceController
		          ((String) fieldHost.getSelectedItem(), fieldUser.getText(), String.valueOf( passwordField.getPassword() ));
		          
				

		waiting = false; //the waitInfo thread can stop
		logging.info(this, "waiting false");
		
		waitCursor.stop();
		
		if (waitInfo != null )
				{
					waitInfo.setVisible(false);
					//waitInfo.dispose();
					//waitInfo = null;
				}

		setCursor(saveCursor);
		if (PersistenceControllerFactory.getConnectionState().getState() == ConnectionState.CONNECTED)
		//if ( persis.getConnectionState().getState() == ConnectionState.CONNECTED )
		{
			//we can finish
			logging.info(this, "connected");
		
			main.setPersistenceController (persis);
	
			MessageFormat messageFormatMainTitle = new MessageFormat( configed.getResourceValue("ConfigedMain.appTitle") );
			main.setAppTitle(
			    messageFormatMainTitle.format(
			        new Object[] { Globals.APPNAME, fieldHost.getSelectedItem(), fieldUser.getText() } ) );
			this.setVisible(false);
		}
		else 
		{
			setVisible(true);
			if (PersistenceControllerFactory.getConnectionState().getState() == ConnectionState.INTERRUPTED)
			//if  (persis.getConnectionState().getState() == ConnectionState.INTERRUPTED )
			{
				// return to password dialog
				logging.info(this, "interrupted");
			}
			else
			{
				logging.info(this, "not connected, presumably not authorizeds");
				
				MessageFormat messageFormatDialogContent = new MessageFormat(
							configed.getResourceValue("DPassword.noConnectionMessageDialog.content") );
	
				JOptionPane.showMessageDialog(
					this,
					messageFormatDialogContent.format(
						new Object[] { persis.getConnectionState().getMessage() } ),
					configed.getResourceValue("DPassword.noConnectionMessageDialog.title"),
					JOptionPane.INFORMATION_MESSAGE);
			}
	
	
			passwordField.setText("");
			if (PersistenceControllerFactory.getConnectionState().getMessage().indexOf ("authorized") > -1 )
			//if (persis.getConnectionState().getMessage().indexOf ("authorized") > -1 )
			{
				logging.info(this, "(not) authorized");
				
				fieldUser.requestFocus();
				fieldUser.setCaretPosition(fieldUser.getText().length());
			}
			else
			{
				fieldHost.requestFocus();
			}
		}
	}
	*/

	void jButtonCommit_actionPerformed(ActionEvent e)
	{
		ok_action();
	}

	void end_program()
	{
		main.finishApp(false,0);
	}

	void jButtonCancel_actionPerformed(ActionEvent e)
	{
		if (waitCursor != null)
			waitCursor.stop();
		end_program();
	}

	void passwordField_componentShown(ComponentEvent e)
	{
		passwordField.requestFocus();
	}


	// paint-Methoden überschreiben, um beim Focus-Setzen das "letzte Wort" zu behalten
	/*public void paint(Graphics g)
{
		super.paint(g);
		passwordField.requestFocus();
}*/

	protected void processWindowEvent(WindowEvent e)
	{
		super.processWindowEvent(e);
		if (e.getID() == WindowEvent.WINDOW_CLOSING)
		{
			end_program();
		}
	}

	class MyKeyListener extends KeyAdapter
	{
		DPassword myHome;
		MyKeyListener (DPassword home)
		{
			myHome = home;
		}

		public void keyPressed (KeyEvent e)
		{
			if (e.getKeyCode() == 10) //Return
			{
				myHome.ok_action();
			}
			else if (e.getKeyCode() == 27) // Escape
			{
				myHome.end_program();
			}
		}
	}
			

}


