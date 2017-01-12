package de.uib.utilities;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ComboBoxModel;

public class ColorsComboBoxModeller 
  implements ComboBoxModeller

// for testing
{

	String[] colors = new String[]{"red","green","blue","yellow"};
	
	String[] giveColors(int no)
	{
		if (no == 0)
			return new String[]{"red"};
		if (no == 1)
			return new String[]{"red","green"};
		if (no == 2)
			return new String[]{"red","green", "blue"};
		return colors;
	}
	
	public  ComboBoxModel getComboBoxModel (int row,  int column)
	{
		return  new DefaultComboBoxModel(giveColors(row));
	}

}

