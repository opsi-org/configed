package de.uib.opsidatamodel;

import de.uib.utilities.logging.*;
import java.awt.Color;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.*;
import de.uib.configed.Globals;
import java.awt.*;
import javax.swing.*;
import javax.swing.plaf.basic.*;

public class ActionProgress
{
	public final static String KEY = "actionProgress";
	
	public final static int FAILED = 2;
	public final static int INSTALLING = 3;
	
	static Color failedTextColor = Color.white;
	static Color failedBackColor =  new Color(100,100,100);
	
	static Color installingTextColor = Color.black;
	static Color installingBackColor = Globals.backYellow;
	
	static Pattern patternPercent = Pattern.compile(".*[^\\d](\\d{1,3})%.*"); 
	
	public ActionProgress(String s)
	{
		
	}
	
	public static Color getBackgroundColor(String s)
	{
		if ( s.indexOf("failed") != -1 ) {
			return failedBackColor;
		}
		else if ( s.indexOf("installing") != -1 ) {
			return installingBackColor;
		}
		else {
			return Color.WHITE;
		}
	}
	
	public static Color getTextColor(String s)
	{
		if ( s.indexOf("failed") != -1 ) {
			return failedTextColor;
		}
		else if ( s.indexOf("installing") != -1 ) {
			return installingTextColor;
		}
		else {
			return Color.BLACK;
		}
	}
	
	public static Component getCellComponent(Component comp, String s) {
		Matcher m = patternPercent.matcher(s);
		if (m.matches()) {
			int percent = Integer.valueOf(m.group(1)).intValue();
			if      (percent < 0)   percent = 0;
			else if (percent > 100) percent = 100;
			JProgressBar progressBar = new JProgressBar(0, 100);
			progressBar.setStringPainted(true);
			progressBar.setValue(percent);
			progressBar.setString(s);
			progressBar.setBorderPainted(false);
			
			/*
			progressBar.setBackground(Color.WHITE);
			progressBar.setForeground(ActionProgress.getBackgroundColor(s));
			
			BasicProgressBarUI ui = new BasicProgressBarUI() {
				protected Color getSelectionBackground() {
					return Color.black; // string color over the background
				}
				protected Color getSelectionForeground() {
					return Color.black; // string color over the foreground
				}
			};
			progressBar.setUI(ui);
			*/
			
			return progressBar;
		}
		comp.setBackground( ActionProgress.getBackgroundColor(s) );
		comp.setForeground( ActionProgress.getTextColor(s) );
		return comp;
	}
}
