package de.uib.configed.gui;

/*
 * configed - configuration editor for client work stations in opsi
 * (open pc server integration) www.opsi.org
 *
 * Copyright (C) 2000-2010 uib.de
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 */

import de.uib.configed.*;

//import de.uib.opsidatamodel.PersistenceController;  // needed for update_version_display
import java.awt.*;
import java.awt.event.*;
import java.awt.im.*;
import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.ListCellRenderer;
import javax.swing.DefaultListCellRenderer;
import javax.swing.table.*;
import javax.swing.tree.*;
import de.uib.configed.tree.*;
import de.uib.configed.type.*;
import de.uib.messages.*;

import javax.swing.border.*;

import java.net.URL;

import javax.swing.event.*;

import java.util.*;
//import de.uib.utilities.StringvaluedObject;


public class TestFrame extends JFrame 
{
	
	final int fwidth = 600;
	final int fwidth_lefthanded = 420;
	final int fwidth_righthanded = fwidth - fwidth_lefthanded;
	final int splitterLeftRight = 15;

	
	final int fheight =  450;
	
	final int labelproductselection_width =  200;
	final int labelproductselection_height = 40;
	final int line_height = 23;
	
	//final int widthColumnServer = 110; //130;
	
	protected String oldDescription;
	protected String oldInventoryNumber;	
	protected String oldOneTimePassword;	
	protected String oldNotes;
	protected String oldMacAddress;
	
	
	
	class GlassPane extends JButton
	{
		GlassPane()
		{
			super();
			System.out.println( "glass pane initialized");
			setVisible(true);
			setOpaque(true);
			
			addActionListener(new ActionListener()
				{
					public void actionPerformed(ActionEvent e)
					{
						System.out.println( "action event on glass pane");
					}
				}
				);
			
			addKeyListener(new KeyAdapter(){
					public void keyPressed(KeyEvent e)
					{
						System.out.println( "key typed on glass pane");
					}
			});
			addMouseListener(new MouseAdapter(){
					public void mouseClicked(MouseEvent e)
					{
						System.out.println( "mouse on glass pane");
					}
			});
			
			
			
		}
		
		
		public void paintComponent(Graphics g)
		{
			((Graphics2D) g).setComposite(
				AlphaComposite.getInstance(AlphaComposite.SRC_OVER, (float) 0.5)
				);
				
			g.setColor(new Color(230,230,250));
			g.fillRect(0,0,getWidth(), getHeight());
		}
		
		
		
	}
	GlassPane glass;
		
	JPanel content;	
	
	public TestFrame( )
	{
		
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE); // we handle it in the window listener method
		
	
		glass = new GlassPane();
		
		guiInit();
		
	}
	
	
	
	private void guiInit() 
	{
	
		setGlassPane(glass);
		setLayout(new BorderLayout());
		JPanel sub1 = new JPanel();
		sub1.setBackground(Color.blue);
		JLabel label1 = new JLabel("on mainframe north");
		label1.setForeground(Color.red);
		
		label1.addMouseListener(new MouseAdapter(){
				public void mouseClicked(MouseEvent e)
				{
					System.out.println("mouse clickedn on label1");
				}
		});
		sub1.add(label1);

		JPanel sub2 = new JPanel();
		JLabel label2 = new JLabel("on mainframe center");
		label2.setForeground(Color.red);
		label2.addMouseListener(new MouseAdapter(){
				public void mouseClicked(MouseEvent e)
				{
					System.out.println("mouse clickedn on label2");
				}
		});
		sub2.add(label2);
		
		
		add(sub1, BorderLayout.NORTH);
		add(sub2, BorderLayout.CENTER);
		
		setSize(fwidth, fheight);
		glass.setSize(fwidth, fheight);
		glass.setVisible(true);
		glass.setOpaque(false);
		setGlassPane(glass);
		
		pack();
		
		setVisible(true);
		
	}
	
	public static void main(String[] args)
	{
		TestFrame instance = new TestFrame();
	}
	
	
}







