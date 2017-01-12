package de.uib.configed.gui;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import java.util.*;

import de.uib.configed.*;
import de.uib.configed.tree.*;
import de.uib.utilities.*;
import de.uib.utilities.logging.*;
import de.uib.utilities.swing.*;


public class PanelHWByAuditDriver extends JPanel
{
	protected JLabel jLabelTitle;
	
	protected int hGap = de.uib.utilities.Globals.hGapSize/2;
	protected int vGap = de.uib.utilities.Globals.vGapSize/2;
	protected int hLabel = de.uib.utilities.Globals.buttonHeight;

	
	protected String byAuditPath;
	
	protected JTextField fieldVendor;
	protected JTextField fieldLabel;
	protected JTextField fieldByAuditPath;
	
	protected String title;
	
	JButton buttonUploadDrivers;
	
	FDriverUpload fDriverUpload;

	private class ShowTextField extends JTextField
	{
		ShowTextField()
		{
			super();
			setEditable(false);
		}
		public void setText(String s)
		{
			super.setText(s);
			setCaretPosition(0);
		}
	}


	public PanelHWByAuditDriver(String title)
	{
		this.title = title;
		buildPanel();
	}
	
	protected void buildPanel()
	{
		//setLayout(new BorderLayout());

		jLabelTitle = new JLabel(title);
		jLabelTitle.setOpaque(true);
		
		
		fieldVendor = new ShowTextField();
		fieldVendor.setBackground(de.uib.utilities.Globals.backgroundLightGrey);

		fieldLabel = new ShowTextField();
		fieldLabel.setBackground(de.uib.utilities.Globals.backgroundLightGrey);
		
		fieldByAuditPath = new ShowTextField();
		
		JLabel labelInfo = new JLabel( configed.getResourceValue("PanelHWInfo.byAuditDriverLocationLabels") );
		JLabel labelVendor = new JLabel("(vendor)");
		JLabel labelLabel = new JLabel("(model/product)");
		JLabel labelPath = new JLabel( configed.getResourceValue("PanelHWInfo.byAuditDriverLocationPath") );
		
		buttonUploadDrivers = new JButton("",  de.uib.configed.Globals.createImageIcon("images/upload2product.png", "" ));
		buttonUploadDrivers.setSelectedIcon( de.uib.configed.Globals.createImageIcon("images/upload2product.png", "" ) );
		buttonUploadDrivers.setToolTipText(configed.getResourceValue("CompleteWinProducts.execute"));
		
		buttonUploadDrivers.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent e)
				{
					startDriverUploadFrame();
				}
			}
		);
		
		//JPanel panelByAuditInfo = new PanelLinedComponents(compis);
		//JPanel panelByAuditInfo= new JPanel();
		GroupLayout layoutByAuditInfo = new GroupLayout(this);
		this.setLayout(layoutByAuditInfo);
		int lh = de.uib.utilities.Globals.lineHeight -4;
		layoutByAuditInfo.setVerticalGroup(
			layoutByAuditInfo.createSequentialGroup()
				.addGap(vGap, vGap, vGap)
				.addGroup(layoutByAuditInfo.createParallelGroup()
					.addComponent(labelInfo, lh, lh, lh)
					//.addComponent(labelVendor, lh, lh, lh)
					.addComponent(fieldVendor, lh, lh, lh)
					//.addComponent(labelLabel, lh, lh, lh)
					.addComponent(fieldLabel, lh, lh, lh)
					.addComponent(labelPath, lh, lh, lh)
					.addComponent(fieldByAuditPath, lh, lh, lh)
					.addComponent(buttonUploadDrivers, lh, lh, lh)
				)
				//.addGap(vGap/2, vGap/2, vGap/2)
			)
		;
		
		layoutByAuditInfo.setHorizontalGroup(
			layoutByAuditInfo.createSequentialGroup()
				.addGap(hGap, hGap, hGap)
				.addComponent(labelInfo, 5, GroupLayout.PREFERRED_SIZE,  GroupLayout.PREFERRED_SIZE)
				.addGap(hGap, hGap, hGap)
				//.addComponent(labelVendor, 5, GroupLayout.PREFERRED_SIZE,  GroupLayout.PREFERRED_SIZE)
				//.addGap(hGap/2, hGap/2, hGap)
				.addComponent(fieldVendor, de.uib.utilities.Globals.buttonWidth/2, de.uib.utilities.Globals.buttonWidth,  de.uib.utilities.Globals.buttonWidth)
				.addGap(hGap/2, hGap, hGap)
				//.addComponent(labelLabel, 5, GroupLayout.PREFERRED_SIZE,  GroupLayout.PREFERRED_SIZE)
				//.addGap(hGap/2, hGap/2, hGap/2)
				.addComponent(fieldLabel, de.uib.utilities.Globals.buttonWidth/2, de.uib.utilities.Globals.buttonWidth, de.uib.utilities.Globals.buttonWidth)
				.addGap(hGap, hGap, hGap)
				.addComponent(buttonUploadDrivers, de.uib.configed.Globals.graphicButtonWidth, de.uib.configed.Globals.graphicButtonWidth,  de.uib.configed.Globals.graphicButtonWidth)
				.addGap(hGap, hGap, hGap)
				.addComponent(labelPath, 5, GroupLayout.PREFERRED_SIZE,  GroupLayout.PREFERRED_SIZE)
				.addGap(hGap/2, hGap/2, hGap/2)
				.addComponent(fieldByAuditPath, de.uib.utilities.Globals.buttonWidth/2, de.uib.utilities.Globals.buttonWidth, Short.MAX_VALUE)
				.addGap(hGap, hGap, hGap)
			)
		;
		
		
		setBackground(de.uib.utilities.Globals.backgroundLightGrey);
		
	}

	public void setTitle(String s)
	{
		title = s;
	}
	
	public void emptyByAuditStrings()
	{
		byAuditPath = "";
		fieldVendor.setText("");
		fieldLabel.setText("");
		fieldByAuditPath.setText("");
	}
	
	
	private String eliminateIllegalPathChars(String path)
	{
		final String toReplace = "<>?\":|\\/*";
		final char replacement = '_';
		
		if (path == null)
			return null;
		
		char[] chars = path.toCharArray();
		for (int i = 0; i < chars.length; i++)
		{
			if (toReplace.indexOf(chars[i]) > -1)
				chars[i] = replacement;
		}
		
		return new String(chars);
	}
	
	public void setByAuditFields(
		String vendorStringCOMPUTER_SYSTEM,
		String vendorStringBASE_BOARD,
		String modelString,
		String productString)
	{
		if (!vendorStringCOMPUTER_SYSTEM.equals(""))
		{
			byAuditPath = eliminateIllegalPathChars(vendorStringCOMPUTER_SYSTEM)
				+ "/" + eliminateIllegalPathChars(modelString);
			fieldVendor.setText(vendorStringCOMPUTER_SYSTEM);
			fieldLabel.setText(modelString);
		}
		else
		{
			byAuditPath = eliminateIllegalPathChars(vendorStringBASE_BOARD)
				+ "/" + eliminateIllegalPathChars(productString);
			fieldVendor.setText(vendorStringBASE_BOARD);
			fieldLabel.setText(productString);
		}
		
		fieldByAuditPath.setText(byAuditPath);
	}		
	
	
	private void startDriverUploadFrame()
	{
		if (fDriverUpload == null)
		{
			fDriverUpload = new FDriverUpload();
		}
		fDriverUpload.show();
		fDriverUpload.setSize(de.uib.configed.Globals.helperFormDimension);
		fDriverUpload.centerOnParent();
	}
			

	
}


