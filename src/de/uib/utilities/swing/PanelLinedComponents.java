//Titel:		PanelLinedComponents
//Copyright: 	Copyright (c) 2011
//Autor:		Martina Hammel, Rupert Röder
//Organisation:	uib
//Beschreibung:		

package de.uib.utilities.swing;

import javax.swing.*;
import de.uib.utilities.*;

public class PanelLinedComponents extends JPanel
{
	
	protected JComponent[] components;
	
	public PanelLinedComponents(JComponent[] components)
	{
		this.components = components;
		//setBackground(Globals.backLightYellow);
		defineLayout();
	}
		
	protected void defineLayout()
	{
		GroupLayout layout = new GroupLayout(this);
		setLayout(layout);
		
		GroupLayout.SequentialGroup hGroup = layout.createSequentialGroup();
		hGroup.addGap( Globals.hGapSize );
		if (components != null)
		{
			for (int j = 0; j <components.length; j++)
			{
				hGroup.addComponent( components[j], 10, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE ); 
				hGroup.addGap( Globals.hGapSize );
			}
		}
		layout.setHorizontalGroup( hGroup );
		
		GroupLayout.SequentialGroup vGroup = layout.createSequentialGroup();
		vGroup.addGap( Globals.vGapSize /2 );
		
		GroupLayout.ParallelGroup vGroup1 = layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER);
			
		if (components != null)
		{
			for (int j = 0; j <components.length; j++)
			{
				vGroup1.addComponent( components[j], Globals.lineHeight, Globals.lineHeight, Globals.lineHeight );
			}
		}
		
		vGroup.addGroup( vGroup1 );
		
		vGroup.addGap( Globals.vGapSize / 2);
		layout.setVerticalGroup( vGroup );
	}
			

}	
