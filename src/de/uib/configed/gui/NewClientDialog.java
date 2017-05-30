package de.uib.configed.gui;
/**
 * NewClientDialog
 * Copyright:     Copyright (c) 2006-2015
 * Organisation:  uib
 * @author Jan Schneider, Rupert Roeder, Anna Sucher
 */


import de.uib.configed.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import java.util.Vector;
import java.util.List;
import java.util.Collections;
import de.uib.opsidatamodel.*;
import de.uib.utilities.logging.*;

import de.uib.utilities.swing.*;

public class NewClientDialog extends FGeneralDialog
{
	private ConfigedMain main;
	protected JPanel panel;
	protected GroupLayout gpl;
	protected JTextField jTextHostname;
	protected JTextField jTextDomainname;
	protected JComboBox jComboDepots;
	protected JTextField jTextDescription;
	protected JTextField jTextInventoryNumber;
	protected JTextArea jTextNotes;
	protected JComboBox jComboPrimaryGroup;
	protected JComboBox jComboNetboot;
	protected JComboBox jComboLocalboot;
	protected JTextField macAddressField;
	protected JTextField ipAddressField;
	protected JCheckBox jCheckUefi;
	protected JCheckBox jCheckWan;
	protected JCheckBox jCheckShutdownInstall;
	protected Vector<String> depots;

	protected Vector<String> groupList;
	protected Vector<String> localbootProducts;
	protected Vector<String> netbootProducts;


	private static NewClientDialog instance;
	private static String defaultDomain = "";
	private boolean uefiboot;
	private boolean wanConfig;
	private boolean shutdownInstall;
	protected boolean multidepot;

	protected java.util.List<String> existingHostNames;


	//private static boolean macAddressFieldVisible = false;
	//private static boolean macAddressFieldVisibleSet = false;
	
	protected int wLeftLabel = Globals.buttonWidth + 20;


	private NewClientDialog(ConfigedMain main, Vector<String> depots)
	{
		super (	null,
		        configed.getResourceValue("NewClientDialog.title") + " (" + Globals.APPNAME + ")",
		        false,
		        new String[]{
		            configed.getResourceValue("NewClientDialog.buttonCreate"),
		            configed.getResourceValue("NewClientDialog.buttonClose") },
		        600, 500);
		setDefaultCloseOperation (JDialog.HIDE_ON_CLOSE);
		this.main = main;


		if (depots != null && depots.size()>1)
		{
			multidepot = true;
		}
		this.depots = depots;

		init();
		pack();
	}

	public static NewClientDialog getInstance(ConfigedMain main, Vector<String> depots)
	{
		if (instance == null)
		{
			instance = new NewClientDialog( main, depots);
			instance.init();
		}
		else {
			//instance.init();
		}
		return instance;
	}

	public static NewClientDialog getInstance()
	{
		//instance.init();
		return instance;
	}

	public void closeNewClientDialog()
	{
		if (instance != null)
		{
			instance.setVisible(false);
		}
	}

	/*
	public boolean macAddressFieldIsSet()
	{
		return macAddressFieldVisibleSet;
	}


	public void setMacAddressFieldVisible(boolean b)
	{
		macAddressFieldVisibleSet = true;  //we do this once
		macAddressFieldVisible =  b;
		repaint();
	}
	*/

	public void setDomain(String s)
	{
		jTextDomainname.setText(s);
		defaultDomain = s;
	}

	public void setHostNames (java.util.List<String> existingHostNames)
	{
		this.existingHostNames = existingHostNames;
	}

	public void setGroupList(Vector<String> groupList)
	{
		DefaultComboBoxModel model = (DefaultComboBoxModel)jComboPrimaryGroup.getModel();
		model.removeAllElements();
		model.addElement(null);
		for (String group : groupList)
			model.addElement(group);
		jComboPrimaryGroup.setModel(model);
		jComboPrimaryGroup.setSelectedIndex(0);
	}
	public void setProductNetbootList(Vector<String> productList)
	{
		DefaultComboBoxModel model = (DefaultComboBoxModel)jComboNetboot.getModel();
		model.removeAllElements();
		model.addElement(null);
		for (String product : productList)
			model.addElement(product);
		jComboNetboot.setModel(model);
		jComboNetboot.setSelectedIndex(0);
	}
	public void setProductLocalbootList(Vector<String> productList)
	{
		DefaultComboBoxModel model = (DefaultComboBoxModel)jComboLocalboot.getModel();
		model.removeAllElements();
		model.addElement(null);
		for (String product : productList)
			model.addElement(product);
		jComboLocalboot.setModel(model);
		jComboLocalboot.setSelectedIndex(0);
	}

	protected void init()
	{
		//int width = 300;

		panel = new JPanel();
		gpl = new GroupLayout(panel);
		panel.setLayout(gpl);
		panel.setBackground(Globals.backLightBlue);

		JLabel jLabelHostname = new JLabel();
		jLabelHostname.setText( configed.getResourceValue("NewClientDialog.hostname") );
		jTextHostname = new JTextField(new CheckedDocument(
		            		/*allowedChars*/ new char[] { '-', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
		            									  'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm',
		            									  'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z'} ,
		            						-1 ),"",17);
		jTextHostname.setToolTipText( configed.getResourceValue("NewClientDialog.hostnameRules") );

		JLabel jLabelDomainname = new JLabel();
		jLabelDomainname.setText( configed.getResourceValue("NewClientDialog.domain") );
		jTextDomainname = new JTextField(defaultDomain);
		

		JLabel jLabelDescription = new JLabel();
		jLabelDescription.setText( configed.getResourceValue("NewClientDialog.description") );
		jTextDescription = new JTextField();
		
		JLabel jLabelInventoryNumber = new JLabel();
		jLabelInventoryNumber.setText( configed.getResourceValue("NewClientDialog.inventorynumber") );
		jTextInventoryNumber = new JTextField();

		JLabel jLabelDepot = new JLabel();
		jLabelDepot.setText( configed.getResourceValue("NewClientDialog.belongsToDepot") );
		jComboDepots = new JComboBox(depots);
		jComboDepots.setFont(Globals.defaultFontBig);

		JLabel labelPrimaryGroup = new JLabel(configed.getResourceValue("NewClientDialog.primaryGroup"));
		jComboPrimaryGroup  = new JComboBox(new String[]{"a","ab"});
		jComboPrimaryGroup.setMaximumRowCount(10);
		jComboPrimaryGroup.setFont(Globals.defaultFontBig);

		JLabel jLabelNetboot = new JLabel();
		jLabelNetboot.setText( configed.getResourceValue("NewClientDialog.netbootProduct") );
		jComboNetboot = new JComboBox(new String[] {"a","ab"});
		jComboNetboot.setMaximumRowCount(10);
		jComboNetboot.setFont(Globals.defaultFontBig);

		JLabel jLabelLocalboot = new JLabel();
		jLabelLocalboot.setText( configed.getResourceValue("NewClientDialog.localbootProduct") );
		jComboLocalboot = new JComboBox(new String[] {"a","ab"});
		jComboLocalboot.setMaximumRowCount(10);
		jComboLocalboot.setFont(Globals.defaultFontBig);
		jComboLocalboot.setEnabled(false);

		JLabel jLabelNotes = new JLabel();
		jLabelNotes.setText( configed.getResourceValue("NewClientDialog.notes") );
		jTextNotes = new JTextArea();
		jTextNotes.addFocusListener( new FocusListener(){
			                             public void  focusGained(FocusEvent e){
				                             jTextNotes.setText(jTextNotes.getText().trim());
				                             // remove tab at end of text, inserted by navigating while in the panel
			                             }

			                             public void  focusLost(FocusEvent e){}
		                             }
		                           );

		jTextNotes.addKeyListener(this);
		// we shall extend the KeyListener from the superclass method for jTextNotes to handle backtab (below)

		jTextNotes.getDocument().addDocumentListener( new DocumentListener(){
			        public void  changedUpdate(DocumentEvent e) {}
			        public void  insertUpdate(DocumentEvent e) {
				        try
				        {
					        //System.out.println (" --------->" + e.getDocString newPiece = e.getDocument().getText(e.getOffset(), e.getLength());
					        String newPiece = e.getDocument().getText(e.getOffset(), e.getLength());
					        logging.debug(this, " --------->" + newPiece + "<");

					        //if ( (e.getDocument().getText(e.getOffset(), e.getLength()) ).equals ("\t") )
					        if ( newPiece.equals ("\t") )
					        {
						        //System.out.println ("tab");
						        macAddressField.requestFocus();
					        }
				        }
				        catch(javax.swing.text.BadLocationException ex)
				        {
				        }

			        }
			        public void  removeUpdate(DocumentEvent e) {}
		        }
		                                            );


		jTextNotes.setBorder(BorderFactory.createLineBorder(new Color(122,138,153)));

		JLabel labelInfoMac = new JLabel(configed.getResourceValue("NewClientDialog.infoMac"));
		labelInfoMac.setFont(Globals.defaultFontBig);


		JLabel labelInfoIP = new JLabel(configed.getResourceValue("NewClientDialog.infoIpAddress"));
		labelInfoIP.setFont(Globals.defaultFontBig);

		JLabel jLabelMacAddress = new JLabel();
		jLabelMacAddress.setText(configed.getResourceValue("NewClientDialog.HardwareAddress") );
		macAddressField = new JTextField(
		                      new SeparatedDocument(
		                          /*allowedChars*/ new char[] { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' } ,
		                          12, ':', 2, true),
		                      "",
		                      17);


		JLabel jLabelIpAddress = new JLabel();
		jLabelIpAddress.setText(configed.getResourceValue("NewClientDialog.IpAddress") );
		ipAddressField = new JTextField(
		                     new SeparatedDocument(
		                         /*allowedChars*/ new char[] { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '.' } ,
		                         12, '.', 3, false),
		                     "",
		                     24);
		
		
		jCheckUefi = new JCheckBox();
		jCheckUefi.setText(configed.getResourceValue("NewClientDialog.boottype"));
		if (!main.getPersistenceController().isWithUEFI())
		{
			jCheckUefi.setText(configed.getResourceValue("NewClientDialog.boottype_not_activated"));
			jCheckUefi.setEnabled(false);
		}


		jCheckWan = new JCheckBox();
		jCheckWan.setText(configed.getResourceValue("NewClientDialog.vpnConfig"));
		if (!main.getPersistenceController().isWithWAN())
		{
			jCheckWan.setText(configed.getResourceValue("NewClientDialog.vpn_not_activated"));
			jCheckWan.setEnabled(false);
		}

		jCheckShutdownInstall = new JCheckBox();
		jCheckShutdownInstall.setText(configed.getResourceValue("NewClientDialog.installByShutdown"));
		// if (!main.getPersistenceController().isWithWAN())
		// {
		// 	jCheckShutdownInstall.setText(configed.getResourceValue("NewClientDialog.installByShutdown"));
		// 	jCheckShutdownInstall.setEnabled(false);
		// }
		
		gpl.setHorizontalGroup(gpl.createParallelGroup()
			///////HOSTNAME
			.addGroup(gpl.createSequentialGroup()
				.addGroup(gpl.createParallelGroup()
					.addGroup(gpl.createSequentialGroup()
						.addGap(Globals.vGapSize,Globals.vGapSize,Globals.vGapSize)
						.addComponent(jLabelHostname, Globals.buttonWidth , Globals.buttonWidth, Short.MAX_VALUE)
					)
					.addGroup(gpl.createSequentialGroup()
						.addGap(Globals.minVGapSize,Globals.minVGapSize,Globals.minVGapSize)
						.addComponent(jTextHostname, Globals.buttonWidth , Globals.buttonWidth , Short.MAX_VALUE)
					)
				)
				.addGap(Globals.vGapSize/2,Globals.vGapSize/2,Globals.vGapSize/2)
				.addGroup(gpl.createParallelGroup()
					.addGroup(gpl.createSequentialGroup()
						.addGap(Globals.vGapSize,Globals.vGapSize,Globals.vGapSize)
						.addComponent(jLabelDomainname, Globals.buttonWidth , Globals.buttonWidth, Short.MAX_VALUE)
					)
					.addGroup(gpl.createSequentialGroup()
						.addGap(Globals.minVGapSize,Globals.minVGapSize,Globals.minVGapSize)
						.addComponent(jTextDomainname, Globals.buttonWidth , Globals.buttonWidth , Short.MAX_VALUE)
					)
				)
				.addGap(Globals.minVGapSize,Globals.minVGapSize,Globals.minVGapSize)
			)
			///////DESCRIPTION + INVENTORY
			.addGroup(gpl.createSequentialGroup()
				.addGap(Globals.hGapSize,Globals.hGapSize,Globals.hGapSize)
				.addComponent(jLabelDescription,  wLeftLabel , wLeftLabel, wLeftLabel)
				.addGap(Globals.hGapSize,Globals.hGapSize,Globals.hGapSize)
				.addComponent(jTextDescription,  Globals.buttonWidth , GroupLayout.PREFERRED_SIZE , Short.MAX_VALUE)
				.addGap(Globals.minVGapSize,Globals.minVGapSize,Globals.minVGapSize)
			)
			.addGroup(gpl.createSequentialGroup()
				.addGap(Globals.hGapSize,Globals.hGapSize,Globals.hGapSize)
				.addComponent(jLabelInventoryNumber,  wLeftLabel ,  wLeftLabel, wLeftLabel )
				.addGap(Globals.hGapSize,Globals.hGapSize,Globals.hGapSize)
				.addComponent(jTextInventoryNumber ,  Globals.buttonWidth , GroupLayout.PREFERRED_SIZE , Short.MAX_VALUE  )
				.addGap(Globals.minVGapSize,Globals.minVGapSize,Globals.minVGapSize)
			)
			
			///////NOTES
			.addGroup(gpl.createSequentialGroup()
				.addGap(Globals.vGapSize,Globals.vGapSize,Globals.vGapSize)
				.addComponent(jLabelNotes,  GroupLayout.PREFERRED_SIZE , GroupLayout.PREFERRED_SIZE , GroupLayout.PREFERRED_SIZE )
				.addGap(Globals.minVGapSize,Globals.minVGapSize,Globals.minVGapSize)
			)
			.addGroup(gpl.createSequentialGroup()
				.addGap(Globals.vGapSize-2,Globals.vGapSize-2,Globals.vGapSize-2)
				.addComponent(jTextNotes,  Globals.buttonWidth , GroupLayout.PREFERRED_SIZE , Short.MAX_VALUE )
				.addGap(Globals.vGapSize-2,Globals.vGapSize-2,Globals.vGapSize-2)
			)
			///////MAC-ADDRESS
			.addGroup(gpl.createSequentialGroup()
				.addGap(Globals.vGapSize,Globals.vGapSize,Globals.vGapSize)
				.addComponent(jLabelMacAddress,  GroupLayout.PREFERRED_SIZE , GroupLayout.PREFERRED_SIZE , GroupLayout.PREFERRED_SIZE )
				.addGap(Globals.vGapSize,Globals.vGapSize,Globals.vGapSize)
				.addComponent(labelInfoMac,  Globals.buttonWidth , GroupLayout.PREFERRED_SIZE , Short.MAX_VALUE )
				.addGap(Globals.minVGapSize,Globals.minVGapSize,Globals.minVGapSize)
			)
			.addGroup(gpl.createSequentialGroup()
				.addGap(Globals.minVGapSize,Globals.minVGapSize,Globals.minVGapSize)
				.addComponent(macAddressField,  Globals.firstLabelWidth , Globals.firstLabelWidth , Globals.firstLabelWidth )
				.addGap(Globals.minVGapSize,Globals.minVGapSize,Globals.minVGapSize)
			)
			.addGroup(gpl.createSequentialGroup()
				.addGap(Globals.vGapSize,Globals.vGapSize,Globals.vGapSize)
				.addComponent(jLabelIpAddress,  GroupLayout.PREFERRED_SIZE , GroupLayout.PREFERRED_SIZE , GroupLayout.PREFERRED_SIZE )
				.addGap(Globals.vGapSize,Globals.vGapSize,Globals.vGapSize)
				.addComponent(labelInfoIP, Globals.buttonWidth , GroupLayout.PREFERRED_SIZE , Short.MAX_VALUE )
				.addGap(Globals.minVGapSize,Globals.minVGapSize,Globals.minVGapSize)
			)
			.addGroup(gpl.createSequentialGroup()
				.addGap(Globals.minVGapSize,Globals.minVGapSize,Globals.minVGapSize)
				.addComponent(ipAddressField, Globals.firstLabelWidth , Globals.firstLabelWidth , Globals.firstLabelWidth )
				.addGap(Globals.minVGapSize,Globals.minVGapSize,Globals.minVGapSize)
			)
			///////InstallByShutdown
			.addGroup(gpl.createSequentialGroup()
				.addGap(Globals.hGapSize,Globals.hGapSize,Globals.hGapSize)
				.addComponent(jCheckShutdownInstall, Globals.buttonWidth , GroupLayout.PREFERRED_SIZE , Short.MAX_VALUE )
				.addGap(Globals.minVGapSize,Globals.minVGapSize,Globals.minVGapSize)
			)	
			///////UEFI
			.addGroup(gpl.createSequentialGroup()
				.addGap(Globals.hGapSize,Globals.hGapSize,Globals.hGapSize)
				.addComponent(jCheckUefi, Globals.buttonWidth , GroupLayout.PREFERRED_SIZE , Short.MAX_VALUE )
				.addGap(Globals.minVGapSize,Globals.minVGapSize,Globals.minVGapSize)
			)
			///////WAN
			.addGroup(gpl.createSequentialGroup()
				.addGap(Globals.hGapSize,Globals.hGapSize,Globals.hGapSize)
				.addComponent(jCheckWan, Globals.buttonWidth , GroupLayout.PREFERRED_SIZE , Short.MAX_VALUE )
				.addGap(Globals.minVGapSize,Globals.minVGapSize,Globals.minVGapSize)
			)
			//depot
			.addGroup( gpl.createSequentialGroup()
				.addGap(Globals.hGapSize,Globals.hGapSize,Globals.hGapSize)
				.addComponent(jLabelDepot,  wLeftLabel, wLeftLabel, wLeftLabel)
				.addGap(Globals.hGapSize,Globals.hGapSize,Globals.hGapSize)
				.addComponent(jComboDepots, Globals.buttonWidth, Globals.buttonWidth, 2*Globals.buttonWidth)
				.addGap(Globals.minVGapSize,Globals.minVGapSize,Globals.minVGapSize)
			)
			//group
			.addGroup( gpl.createSequentialGroup()
				.addGap(Globals.hGapSize,Globals.hGapSize,Globals.hGapSize)
				.addComponent(labelPrimaryGroup,  wLeftLabel , wLeftLabel, wLeftLabel)
				.addGap(Globals.hGapSize,Globals.hGapSize,Globals.hGapSize)
				.addComponent(jComboPrimaryGroup, Globals.buttonWidth, Globals.buttonWidth, 2*Globals.buttonWidth)
				.addGap(Globals.minVGapSize,Globals.minVGapSize,Globals.minVGapSize)
			)
			//netboot
			.addGroup( gpl.createSequentialGroup()
				.addGap(Globals.hGapSize,Globals.hGapSize,Globals.hGapSize)
				.addComponent(jLabelNetboot,  wLeftLabel, wLeftLabel, wLeftLabel)
				.addGap(Globals.hGapSize,Globals.hGapSize,Globals.hGapSize)
				.addComponent(jComboNetboot, Globals.buttonWidth, Globals.buttonWidth, 2*Globals.buttonWidth)
				.addGap(Globals.minVGapSize,Globals.minVGapSize,Globals.minVGapSize)
			)
			//localboot
			// .addGroup( gpl.createSequentialGroup()
			// 	.addGap(Globals.hGapSize,Globals.hGapSize,Globals.hGapSize)
			// 	.addComponent(jLabelLocalboot,  wLeftLabel , wLeftLabel, wLeftLabel)
			// 	.addGap(Globals.hGapSize,Globals.hGapSize,Globals.hGapSize)
			// 	.addComponent(jComboLocalboot, Globals.buttonWidth, Globals.buttonWidth, 2*Globals.buttonWidth)
			// 	.addGap(Globals.minVGapSize,Globals.minVGapSize,Globals.minVGapSize)
			// )
		
		);
		gpl.setVerticalGroup(gpl.createSequentialGroup()
			/////// HOSTNAME
			.addGap(Globals.minVGapSize,Globals.minVGapSize,Globals.minVGapSize)
			.addGroup( gpl.createParallelGroup()
				.addComponent(jLabelHostname)
				.addComponent(jLabelDomainname)
			)
			.addGroup( gpl.createParallelGroup( GroupLayout.Alignment.CENTER )
				.addComponent(jTextHostname, Globals.lineHeight,Globals.lineHeight,Globals.lineHeight)
				.addComponent(jTextDomainname, Globals.lineHeight,Globals.lineHeight,Globals.lineHeight)
			)
			/////// DESCRIPTION
			.addGap(Globals.minVGapSize,Globals.minVGapSize,Globals.minVGapSize)
			.addGroup( gpl.createParallelGroup( GroupLayout.Alignment.CENTER )
				.addComponent(jLabelDescription)
				.addComponent(jTextDescription, Globals.lineHeight,Globals.lineHeight,Globals.lineHeight)
			)
			/////// INVENTORY NUMBER
			.addGap(Globals.minVGapSize,Globals.minVGapSize,Globals.minVGapSize)
			.addGroup( gpl.createParallelGroup(GroupLayout.Alignment.CENTER )
				.addComponent(jLabelInventoryNumber )
				.addComponent(jTextInventoryNumber, Globals.lineHeight,Globals.lineHeight,Globals.lineHeight)
			)
			/////// NOTES
			.addGap(Globals.minVGapSize/2,Globals.minVGapSize/2,Globals.minVGapSize)
			.addComponent(jLabelNotes)
			.addComponent(jTextNotes)
			
			/////// MAC-ADDRESS
			.addGap(Globals.vGapSize,Globals.vGapSize,Globals.vGapSize)
			.addGroup( gpl.createParallelGroup()
				.addComponent(jLabelMacAddress)
				.addComponent(labelInfoMac)
			)
			.addComponent(macAddressField , Globals.lineHeight,Globals.lineHeight,Globals.lineHeight)
			/////// IP-ADDRESS
			.addGap(Globals.minVGapSize,Globals.minVGapSize,Globals.minVGapSize)
			.addGroup( gpl.createParallelGroup()
				.addComponent(jLabelIpAddress)
				.addComponent(labelInfoIP)
			)
			.addComponent(ipAddressField,Globals.lineHeight,Globals.lineHeight,Globals.lineHeight)
			
			/////// UEFI
			.addGap(Globals.minVGapSize,Globals.minVGapSize,Globals.minVGapSize)
			.addComponent(jCheckShutdownInstall,Globals.lineHeight,Globals.lineHeight,Globals.lineHeight)

			/////// UEFI
			.addGap(Globals.minVGapSize,Globals.minVGapSize,Globals.minVGapSize)
			.addComponent(jCheckUefi,Globals.lineHeight,Globals.lineHeight,Globals.lineHeight)
			
			/////// WAN
			.addGap(Globals.minVGapSize,Globals.minVGapSize,Globals.minVGapSize)
			.addComponent(jCheckWan,Globals.lineHeight,Globals.lineHeight,Globals.lineHeight)
			
			/////// depot 
			.addGap(Globals.vGapSize/2,Globals.vGapSize/2,Globals.vGapSize/2)
			.addGroup( gpl.createParallelGroup(GroupLayout.Alignment.CENTER )
				.addComponent(jLabelDepot, Globals.lineHeight,Globals.lineHeight,Globals.lineHeight)
				.addComponent(jComboDepots, Globals.lineHeight,Globals.lineHeight,Globals.lineHeight)
			)
			/////// group 
			.addGap(Globals.vGapSize/2,Globals.vGapSize/2,Globals.vGapSize/2)
			.addGroup( gpl.createParallelGroup(GroupLayout.Alignment.CENTER )
				.addComponent(labelPrimaryGroup, Globals.lineHeight,Globals.lineHeight,Globals.lineHeight)
				.addComponent(jComboPrimaryGroup, Globals.lineHeight,Globals.lineHeight,Globals.lineHeight)
			)
			/////// netboot 
			.addGap(Globals.vGapSize/2,Globals.vGapSize/2,Globals.vGapSize/2)
			.addGroup( gpl.createParallelGroup(GroupLayout.Alignment.CENTER )
				.addComponent(jLabelNetboot, Globals.lineHeight,Globals.lineHeight,Globals.lineHeight)
				.addComponent(jComboNetboot, Globals.lineHeight,Globals.lineHeight,Globals.lineHeight)
			)
			/////// localboot 
			// .addGap(Globals.vGapSize/2,Globals.vGapSize/2,Globals.vGapSize/2)
			// .addGroup( gpl.createParallelGroup(GroupLayout.Alignment.CENTER )
			// 	.addComponent(jLabelLocalboot, Globals.lineHeight,Globals.lineHeight,Globals.lineHeight)
			// 	.addComponent(jComboLocalboot, Globals.lineHeight,Globals.lineHeight,Globals.lineHeight)
			// )
			// .addGap(Globals.vGapSize,Globals.vGapSize,Globals.vGapSize)
			
		);
	
		scrollpane.getViewport().add(panel);
		pack();
		centerOn(Globals.mainContainer);
	}


	/* This method is called when button 1 is pressed */
	public void doAction1()
	{

		result = 1;

		String hostname = jTextHostname.getText();
		String domainname = jTextDomainname.getText();
		String depotID = (String) jComboDepots.getSelectedItem();
		String description = jTextDescription.getText();
		String inventorynumber = jTextInventoryNumber.getText();
		String notes = jTextNotes.getText().trim();
		String macaddress = macAddressField.getText();
		String ipaddress = ipAddressField.getText();
		String group = (String) jComboPrimaryGroup.getSelectedItem();
		String netbootProduct = (String) jComboNetboot.getSelectedItem();
		String localbootProduct = (String) jComboLocalboot.getSelectedItem();

		/*
		System.out.println("hostname: " + hostname);
		System.out.println("domainname: " + domainname);
		System.out.println("description: " + description);
		System.out.println("notes: " + notes);
		System.out.println("macaddress: " + macaddress);
		System.out.println("ipaddress: " + ipaddress);
		*/
		if (hostname == null || hostname.equals(""))
		{
			return;
		}
		if (domainname == null || domainname.equals(""))
		{
			return;
		}

		//logging.debug(this, "doAction1 host, existingHostNames.contains host " + hostname + ". " + domainname  + ", "
		//	 +existingHostNames.contains(hostname + "." + domainname));

		String opsiHostKey = hostname + "." + domainname;
		if (existingHostNames != null && existingHostNames.contains(opsiHostKey))
		{

			if (depots.contains(opsiHostKey))
			{
				JOptionPane.showMessageDialog(this,
				                              opsiHostKey + "\n" + configed.getResourceValue("NewClientDialog.OverwriteDepot.Message"),
				                              configed.getResourceValue("NewClientDialog.OverwriteDepot.Title") + " (" + Globals.APPNAME + ")",
				                              JOptionPane.WARNING_MESSAGE);
				return;
			}



			FTextArea fQuestion = new FTextArea(Globals.mainFrame,
			                                    configed.getResourceValue("NewClientDialog.OverwriteExistingHost.Question")
			                                    + " (" + Globals.APPNAME + ") ",
			                                    true,
			                                    new String[]{
			                                        configed.getResourceValue("FGeneralDialog.no"),
			                                        configed.getResourceValue("FGeneralDialog.yes")
			                                    },
			                                    350, 100);
			StringBuffer message = new StringBuffer("");
			message.append( configed.getResourceValue("NewClientDialog.OverwriteExistingHost.Message0") );
			message.append(" \"");
			message.append(opsiHostKey);
			message.append("\" \n");
			message.append( configed.getResourceValue("NewClientDialog.OverwriteExistingHost.Message1") );
			fQuestion.setMessage(message.toString());
			fQuestion.centerOn(this);
			fQuestion.setAlwaysOnTop(true);
			fQuestion.setVisible(true);

			if (fQuestion.getResult() == 1)
				return;

		}

		if (hostname.length() > 15)
		{
			FTextArea fQuestion = new FTextArea(Globals.mainFrame,
			                                    configed.getResourceValue("NewClientDialog.IgnoreNetbiosRequirement.Question")
			                                    + " (" + Globals.APPNAME + ") ",
			                                    true,
			                                    new String[]{
			                                        configed.getResourceValue("FGeneralDialog.no"),
			                                        configed.getResourceValue("FGeneralDialog.yes")
			                                    },
			                                    350, 100);
			StringBuffer message = new StringBuffer("");
			message.append( configed.getResourceValue("NewClientDialog.IgnoreNetbiosRequirement.Message") );
			//message.append(" \"");
			//message.append(hostname);
			//message.append("\" \n");
			fQuestion.setMessage(message.toString());
			fQuestion.centerOn(this);
			fQuestion.setAlwaysOnTop(true);
			fQuestion.setVisible(true);

			if (fQuestion.getResult() == 1)
				return;
		}
		
		boolean onlyNumbers = true;
		int i = 0;
		while (onlyNumbers && i<hostname.length())
		{
			if (!Character.isDigit(hostname.charAt(i)))
				onlyNumbers = false;
			i++;
		}
			
		if (onlyNumbers)
		{
			FTextArea fQuestion = new FTextArea(Globals.mainFrame,
			                                    configed.getResourceValue("NewClientDialog.IgnoreOnlyDigitsRequirement.Question")
			                                    + " (" + Globals.APPNAME + ") ",
			                                    true,
			                                    new String[]{
			                                        configed.getResourceValue("FGeneralDialog.no"),
			                                        configed.getResourceValue("FGeneralDialog.yes")
			                                    },
			                                    350, 100);
			StringBuffer message = new StringBuffer("");
			message.append( configed.getResourceValue("NewClientDialog.IgnoreOnlyDigitsRequirement.Message") );
			//message.append(" \"");
			//message.append(hostname);
			//message.append("\" \n");
			fQuestion.setMessage(message.toString());
			fQuestion.centerOn(this);
			fQuestion.setAlwaysOnTop(true);
			fQuestion.setVisible(true);

			if (fQuestion.getResult() == 1)
				return;
		}

		if (main.getPersistenceController().isWithUEFI())
		{
			uefiboot = false;
			if (jCheckUefi.getSelectedObjects() != null)
			{
				uefiboot = true;
			}
		}

		if (main.getPersistenceController().isWithWAN())
		{
			wanConfig = false;
			if (jCheckWan.getSelectedObjects() != null)
			{
				wanConfig = true;
			}
		}
		// shutdownInstall = false;
		// if (jCheckShutdownInstall.getSelectedObjects() != null)
		// {
		// 	shutdownInstall = true;
		// }

		main.createClient(hostname, domainname, 
			depotID, description, inventorynumber, notes, 
			ipaddress, macaddress, //shutdownInstall,
			uefiboot, wanConfig, //uefiboot
			group, netbootProduct, localbootProduct
			);

		if (jCheckShutdownInstall.getSelectedObjects() != null)
		{
			main.setInstallByShutdown(opsiHostKey, true);
		}
		//setVisible(false);
	}

	/* This method gets called when button 2 is pressed */
	public void doAction2()
	{
		result = 2;
		setVisible(false);
	}

	public void  keyPressed(KeyEvent e)
	{
		if (e.getSource() == jTextNotes
		        &&
		        (e.getModifiersEx() & InputEvent.SHIFT_DOWN_MASK) == InputEvent.SHIFT_DOWN_MASK
		        &&  e.getKeyCode() == KeyEvent.VK_TAB
		   )
		{
			jTextDescription.requestFocusInWindow();
			//e.consume();
		}

		else
		{
			super.keyPressed(e);
		}
	}

}




