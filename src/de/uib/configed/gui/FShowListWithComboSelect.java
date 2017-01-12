package de.uib.configed.gui;

/**
 * FShowList
 * Copyright:     Copyright (c) 2001-2006
 * Organisation:  uib
 * @author Rupert Röder
 */
 
import de.uib.configed.*;
import java.awt.*;
import javax.swing.*;
import java.awt.event.*;
import de.uib.utilities.logging.*;
import de.uib.utilities.swing.*;

/** This class is intended to show a list in text area
*/
public class FShowListWithComboSelect extends FShowList
{
	Object[] choices;
	JComboBox combo;
	JLabel labelChoice;
	 
	public  FShowListWithComboSelect(JFrame owner, String title, boolean modal, String choiceTitle, Object[] choices, Object[] buttonList)
	{
		super (owner, title, modal, buttonList);
		//JPanel panelChoice = new JPanel(new BorderLayout());
		labelChoice = new JLabel(choiceTitle + ": ");
		labelChoice.setOpaque(true);
		labelChoice.setBackground(Globals.backgroundLightGrey);
		//panelChoice.add(labelChoice, BorderLayout.WEST);
		combo = new JComboBox (choices);
		combo.setFont(Globals.defaultFontBold);
		//panelChoice.add(combo, BorderLayout.CENTER);
		HorizontalPositioner panelChoice = new HorizontalPositioner (new SurroundPanel(labelChoice), combo);
		allpane.add(panelChoice, BorderLayout.NORTH);
	}
 
 	public Object getChoice()
	{
		return combo.getSelectedItem();
	}
}
 


