package utils;

import javax.swing.*;
import java.text.*;

public class FormattedTextFieldVerifier extends InputVerifier 
{
	public boolean verify(JComponent input) 
	{
		if (input instanceof JFormattedTextField) 
		{
			JFormattedTextField ftf = (JFormattedTextField)input;
			JFormattedTextField.AbstractFormatter formatter = ftf.getFormatter();
			if (formatter != null) 
			{
				String text = ftf.getText();
				try 
				{
					formatter.stringToValue(text);
					return true;
				} catch (ParseException pe) {
					return false;
				}
			}
		}
		return true;
	}
	public boolean shouldYieldFocus(JComponent input) 
	{
		return verify(input);
	}

}
