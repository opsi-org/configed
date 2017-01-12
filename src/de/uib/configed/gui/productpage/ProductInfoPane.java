/*
 * ProductInfoPanes.java
 *
 * (open pc server integration) www.opsi.org
 *
 * Copyright (C) 2000-2014 uib.de
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *09, 13:31:36
 
 */

package de.uib.configed.gui.productpage;


import de.uib.configed.*;
import de.uib.configed.gui.*;
import de.uib.configed.type.*;
import de.uib.utilities.swing.*;
import de.uib.utilities.logging.*;
import org.jdesktop.swingx.*;
import java.util.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import de.uib.configed.guidata.RequirementsTableModel;

/**
 *
 * @author roeder
 */
public class ProductInfoPane extends javax.swing.JSplitPane
	implements de.uib.utilities.DataChangedObserver
{
	
	private JXPanel productDescriptionsPanel;
	public javax.swing.JLabel jLabelPackageVersion;
	private javax.swing.JLabel jLabelProductAdvice;
	private javax.swing.JLabel jLabelProductDescription;
	protected javax.swing.JLabel jLabelProductID;
	protected javax.swing.JLabel jLabelProductVersion;
	protected javax.swing.JLabel jLabelLabelProductVersion;
	protected javax.swing.JLabel jLabelProductName;
	private javax.swing.JLabel jLabelProductDependencies;
	

	private DefaultPanelEditProperties panelEditProperties;
	
	private javax.swing.JScrollPane jScrollPaneProductAdvice;
	private javax.swing.JScrollPane jScrollPaneProductInfo;
	private javax.swing.JScrollPane dependenciesPanel;
	protected javax.swing.JTable dependenciesTable;
	protected RequirementsTableModel dependenciesTableModel;
	protected javax.swing.JTextArea jTextAreaProductAdvice;
	protected javax.swing.JTextArea jTextAreaProductInfo;
	//public javax.swing.JTextField jTextField_SelectedClients;
	
	private int minLabelVSize = 0;
	private int minTableVSize = 40;
	private int minGapVSize = 2;
	private int minVSize 	= 10;
	private int prefVSize 	= 20;
	private int vGapSize	= 5;
	private int hGapSize = 2;
	
	
	private int minHSize = 50;
	private int prefHSize = 80;
	
	protected String productName="";
	Map<String, Boolean>specificPropertiesExisting;
	
	protected ConfigedMain mainController;
	
	
	/** Creates new ProductInfoPane */
	public ProductInfoPane(ConfigedMain mainController,
		DefaultPanelEditProperties panelEditProperties
		)
	{
		super(JSplitPane.VERTICAL_SPLIT);
		this.mainController = mainController;
		this.panelEditProperties = panelEditProperties;
		initComponents();
	}
		
	/** Creates new ProductInfoPane */
	public ProductInfoPane(
		DefaultPanelEditProperties panelEditProperties
		)
	{
		this(null, panelEditProperties);
	}

	
	private void initComponents() 
	{
		//jTextField_SelectedClients = new javax.swing.JTextField();
		jLabelProductName = new javax.swing.JLabel();
		jLabelProductID = new javax.swing.JLabel();
		jLabelProductVersion = new javax.swing.JLabel();
		jLabelLabelProductVersion = new javax.swing.JLabel();
		//jLabelPackageVersion = new javax.swing.JLabel();
		jLabelProductDescription = new javax.swing.JLabel();
		jScrollPaneProductInfo = new javax.swing.JScrollPane();
		jTextAreaProductInfo = new javax.swing.JTextArea();
		jLabelProductAdvice = new javax.swing.JLabel();
		jScrollPaneProductAdvice = new javax.swing.JScrollPane();
		jTextAreaProductAdvice = new javax.swing.JTextArea();
		jLabelProductDependencies = new javax.swing.JLabel();
		dependenciesPanel = new javax.swing.JScrollPane();
		
		//init dummies in advance for layout purposes
		dependenciesTable = new javax.swing.JTable();
		
		/*
		jTextField_SelectedClients.setEditable(false);
		jTextField_SelectedClients.setFont(Globals.defaultFontBig);
		jTextField_SelectedClients.setText(" ");
		jTextField_SelectedClients.setBackground(Globals.backgroundLightGrey);
		*/
		
		//jLabelProductName.setFont(Globals.defaultFontBig);
		//jLabelProductName.setText( configed.getResourceValue("MainFrame.labelProductId") );
		
		jLabelProductID.setText(" ");
		//jLabelProductID.setFont(Globals.defaultFont);
		jLabelProductID.setFont(Globals.defaultFontStandardBold);
		jLabelProductName.setText(" ");
		jLabelProductName.setFont(Globals.defaultFontBold);
		
		
		jLabelLabelProductVersion.setText("");
		jLabelLabelProductVersion.setFont(Globals.defaultFontBig);
		jLabelLabelProductVersion.setText(configed.getResourceValue("ProductInfoPane.jLabelProductVersion") + " ");
		jLabelProductVersion.setFont(Globals.defaultFontStandardBold);
		
		jLabelProductDescription.setFont(Globals.defaultFontStandardBold);
		jLabelProductDescription.setPreferredSize(new Dimension (prefHSize, Globals.lineHeight));
		jLabelProductDescription.setText( configed.getResourceValue("ProductInfoPane.jLabelProductDescription") );
		
		jTextAreaProductInfo.setColumns(20);
		jTextAreaProductInfo.setRows(5);
		
		jTextAreaProductInfo.setEditable(false);
		jTextAreaProductInfo.setWrapStyleWord(true);
		jTextAreaProductInfo.setLineWrap(true);
		jTextAreaProductInfo.setFont(Globals.defaultFont);
		jTextAreaProductInfo.setBackground(Globals.backgroundLightGrey);
		
		
		jScrollPaneProductInfo.setViewportView(jTextAreaProductInfo);
		jScrollPaneProductInfo.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		//jScrollPaneProductInfo.setVerticalScrollBarPolicy( JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		jScrollPaneProductInfo.setVerticalScrollBarPolicy( JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		
		jLabelProductAdvice.setText( configed.getResourceValue("ProductInfoPane.jLabelProductAdvice") );
		jLabelProductAdvice.setFont(Globals.defaultFontStandardBold);
		
		jTextAreaProductAdvice.setColumns(20);
		jTextAreaProductAdvice.setRows(5);
		
		jTextAreaProductAdvice.setEditable(false);
		jTextAreaProductAdvice.setWrapStyleWord(true);
		jTextAreaProductAdvice.setLineWrap(true);
		jTextAreaProductAdvice.setFont(Globals.defaultFont);
		jTextAreaProductAdvice.setBackground(Globals.backgroundLightGrey);
		
		jScrollPaneProductAdvice.setViewportView(jTextAreaProductAdvice);
		jScrollPaneProductAdvice.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		//jScrollPaneProductAdvice.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		jScrollPaneProductAdvice.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		
		jLabelProductDependencies.setText( configed.getResourceValue("ProductInfoPane.jLabelProductDependencies")  + ": ");
		jLabelProductDependencies.setFont(Globals.defaultFontBig);
		
		dependenciesTable.setBackground(Globals.backgroundLightGrey);
		dependenciesPanel.setViewportView(dependenciesTable);
		dependenciesPanel.getViewport().setBackground(Globals.backgroundLightGrey);
		
		productDescriptionsPanel = new JXPanel();
		/*
		productDescriptionsPanel.setBackgroundPainter(new org.jdesktop.swingx.painter.AbstractPainter()
			{
				@Override
				public void doPaint( Graphics2D g, Object obj, int width, int height)
				{
					g.setPaint(de.uib.configed.Globals.backBlue);
					g.fillRect(0,0,width, height);
				}
			}
		);
		*/

		javax.swing.GroupLayout layoutDescriptionsPanel = new javax.swing.GroupLayout(productDescriptionsPanel);
		productDescriptionsPanel.setLayout(layoutDescriptionsPanel);
		
		layoutDescriptionsPanel.setHorizontalGroup(
			layoutDescriptionsPanel.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				
				.addComponent(jLabelProductID, minHSize, prefHSize, Short.MAX_VALUE)
				
				.addGroup(layoutDescriptionsPanel.createSequentialGroup()
					.addGap(Globals.hGapSize,Globals.hGapSize,Globals.hGapSize)
					.addComponent(jLabelLabelProductVersion, minHSize, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
					.addComponent(jLabelProductVersion, minHSize, prefHSize, Short.MAX_VALUE)
					.addGap(Globals.minHGapSize,Globals.minHGapSize,Globals.minHGapSize)
				)
				/*
				.addGroup(layoutDescriptionsPanel.createSequentialGroup()
					.addGap(Globals.hGapSize,Globals.hGapSize,Globals.hGapSize)
					.addComponent(jLabelProductDescription, minHSize, prefHSize, Short.MAX_VALUE)
					.addGap(Globals.minHGapSize,Globals.minHGapSize,Globals.minHGapSize)
				)
				*/
					
				.addGroup(layoutDescriptionsPanel.createSequentialGroup()
					.addGap(Globals.hGapSize,Globals.hGapSize,Globals.hGapSize)
					.addComponent(jLabelProductName, minHSize, prefHSize, Short.MAX_VALUE)
					.addGap(Globals.minHGapSize,Globals.minHGapSize,Globals.minHGapSize)
				)
				//.addComponent(jLabelPackageVersion, minHSize, prefHSize, Short.MAX_VALUE)
				.addComponent(jScrollPaneProductInfo, minHSize, prefHSize, Short.MAX_VALUE)
				
				/*
				.addGroup(layoutDescriptionsPanel.createSequentialGroup()
					.addGap(Globals.hGapSize,Globals.hGapSize,Globals.hGapSize)
					.addComponent(jLabelProductAdvice, minHSize, prefHSize, Short.MAX_VALUE)
					.addGap(Globals.minHGapSize,Globals.minHGapSize,Globals.minHGapSize)
				)
				*/
				.addComponent(jScrollPaneProductAdvice, minHSize, prefHSize, Short.MAX_VALUE)
				
				.addGroup(layoutDescriptionsPanel.createSequentialGroup()
					.addGap(Globals.hGapSize,Globals.hGapSize,Globals.hGapSize)
					.addComponent(jLabelProductDependencies, minHSize, prefHSize, Short.MAX_VALUE)
					.addGap(Globals.minHGapSize,Globals.minHGapSize,Globals.minHGapSize)
				)
					
				.addComponent(dependenciesPanel, minHSize, prefHSize, Short.MAX_VALUE)
			)
		;
		layoutDescriptionsPanel.setVerticalGroup(
			layoutDescriptionsPanel.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
			.addGroup(layoutDescriptionsPanel.createSequentialGroup()
				//.addComponent(jTextField_SelectedClients, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
				.addComponent(jLabelProductID, minLabelVSize, prefVSize, javax.swing.GroupLayout.PREFERRED_SIZE)
				.addGap(minGapVSize, minGapVSize, minGapVSize)
				.addComponent(jLabelProductName, minLabelVSize, Globals.buttonHeight, Globals.buttonHeight)
				.addGap(minGapVSize, minGapVSize, minGapVSize)
				.addGroup(layoutDescriptionsPanel.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
					.addComponent(jLabelLabelProductVersion, minLabelVSize, Globals.buttonHeight, Globals.buttonHeight)
					.addComponent(jLabelProductVersion, minLabelVSize, Globals.buttonHeight, Globals.buttonHeight)
				)
				.addGap(vGapSize, vGapSize, vGapSize)
				//.addComponent(jLabelProductDescription, minLabelVSize, Globals.buttonHeight, Globals.buttonHeight)
				//.addGap(minGapVSize, minGapVSize, minGapVSize)
				.addComponent(jScrollPaneProductInfo, minVSize, prefVSize, 4*prefVSize)
				.addGap(vGapSize, 2* vGapSize, 2 * vGapSize)
				//.addComponent(jLabelProductAdvice, minLabelVSize, Globals.buttonHeight, Globals.buttonHeight)
				//.addGap(minGapVSize, minGapVSize, minGapVSize)
				.addComponent(jScrollPaneProductAdvice, minVSize, prefVSize, 4*prefVSize)
				.addGap(vGapSize, vGapSize, vGapSize)
				.addComponent(jLabelProductDependencies, Globals.buttonHeight, Globals.buttonHeight, Globals.buttonHeight)
				//.addGap(minGapVSize, minGapVSize, minGapVSize)
				.addComponent(dependenciesPanel, 3*minVSize, 3*prefVSize, 10*prefVSize)
				.addGap(minGapVSize, vGapSize, vGapSize)
			)
		);
	
		setTopComponent(productDescriptionsPanel);
		
		//treat the south panel
		
		setTopComponent(productDescriptionsPanel);
		setBottomComponent(panelEditProperties);
		setDividerLocation(250);
		
	}
	
	
	protected String fillEmpty(String content)
	{
		if (content == null || content.equals("") || content.equals("-"))
			return " ";
		
		return content;
	}
	
	
	//access type 1 to dependenies table, setting model in MainController
	public JTable getDependenciesTable()
	{
		return dependenciesTable;	
	}
	
	
	public void setAdvice(String s)
	{
		jTextAreaProductAdvice.setText(s);
	}
	
	public void setInfo(String s)
	{	
		jTextAreaProductInfo.setText(s);
		jTextAreaProductInfo.setCaretPosition(0);
	}
	
	public void setId(String s) 
	{
		jLabelProductID.setText(s); 
		productName = s;
	}
		
	public void setProductVersion(String s)
	{
		jLabelProductVersion.setText( fillEmpty(s) );
	}
	
	/*
	public void setPackageVersion(String s)
	{
		jLabelPackageVersion.setText( configed.getResourceValue("MainFrame.JLabel_PackageVersion") + " " +  fillEmpty(s) );
	}
	*/
	
	public void setID(String s) 
	{
		jLabelProductID.setText(s  + ":");
		productName = s;
	}
	
	public void setName(String s) 
	{
		jLabelProductName.setText(s); 
	}
	
	
	public void setSpecificPropertiesExisting(Map<String, Boolean>specificPropertiesExisting)
	{
		//this.specificPropertiesExisting = specificPropertiesExisting;
		panelEditProperties.setSpecificPropertiesExisting(productName, specificPropertiesExisting);
		
	}
	
	protected void setPropertyResetActivated(boolean b)
	{
		panelEditProperties.setPropertyResetActivated(b);
	}
	
	public void setGrey(boolean b)
	{
		float alpha = (float) 1.0f;
		if (b)
		{
			alpha = (float) .1f;
		}
		
		if (productDescriptionsPanel != null) productDescriptionsPanel.setAlpha(alpha);
		
		if (panelEditProperties != null) panelEditProperties.setAlpha(alpha);
	}
	
	
	public void setEditValues(
		String productId,
		String productVersion,
		String packageVersion,
		String depotId
		/*
		,
		Map<String, Boolean> specificPropertiesExisting, 
		Collection storableProductProperties, 
		Map editableProductProperties,
				
		//editmappanelx
		Map<String, de.uib.utilities.table.ListCellOptions> productpropertyOptionsMap,
		ProductpropertiesUpdateCollection updateCollection)
		*/
	)
	{
		
		setGrey(false);
		setId(productId);
		//setName(productTitle);
		//setInfo(productInfo);
		setProductVersion(productVersion + "-" + packageVersion);
		//setAdvice(productHint);
		
		if (mainController != null)
		{
			String versionInfo = OpsiPackage.produceVersionInfo(productVersion, packageVersion);
			OpsiProductInfo info = mainController.getPersistenceController()
				.getProduct2versionInfo2infos()
				.get(productId).get(versionInfo);
			logging.info(this, "got product infos  productId, versionInfo:  " + productId + ", " + versionInfo + ": " + info);
			
			setName(info.getProductName());
			setInfo(info.getDescription());
			setAdvice(info.getAdvice());
			if (dependenciesTableModel  == null)
			{
				dependenciesTableModel = new RequirementsTableModel(
					mainController.getPersistenceController()
				);
				dependenciesTable.setModel( dependenciesTableModel );
			}
			dependenciesTableModel.setActualProduct(depotId, productId);
			
		}
		
		/*setSpecificPropertiesExisting(specificPropertiesExisting);
	
		propertiesPanel.setEditableMap( 
			
			//visualMap (merged for different clients)
			editableProductProperties,
			productpropertyOptionsMap
			);
			
		propertiesPanel.setStoreData( storableProductProperties );
		propertiesPanel.setUpdateCollection (updateCollection);
		}
		*/
	}
	
	
	public void clearEditing()
	{
		
		setGrey(false);
		setId("");
		setProductVersion("");
		
		setName("");
		setInfo("");
		dependenciesTableModel = new RequirementsTableModel(
					mainController.getPersistenceController()
				);
		dependenciesTable.setModel( dependenciesTableModel );
	}
		
	
	//
	//DataChangedObserver
	public void dataHaveChanged(Object source )
	{
		//logging.debug(this, "dataHaveChanged " + source );
		if (source instanceof de.uib.utilities.datapanel.EditMapPanelX)
		{
			specificPropertiesExisting.put(productName, true);
			setPropertyResetActivated(true);
		}
	}



}
