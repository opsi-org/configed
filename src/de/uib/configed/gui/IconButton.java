package de.uib.configed.gui;

import javax.swing.ImageIcon;
import javax.swing.JButton;

import de.uib.utilities.logging.*;
import de.uib.configed.Globals;

/**
 * Creates a button with an icon
 * <br><br>
 * @version 1.0
 * @author Anna Sucher
 */
public class IconButton extends JButton
{
	/**The status if button is active*/
	protected boolean activated;
	/**The status if button is/should be enabled*/
	protected boolean enabled;
	
	/**A description used for tooltip if button is active*/
	protected String tooltipActive;
	
	/**A description used for tooltip if button is inactive*/
	protected String tooltipInactive;
	
	/**The url for the image displayed if active*/
	protected String imageURLActive;
	
	/**The url for the image displayed if the cursor is hovering over the button */
	protected String imageURLOver;
	
	/**The url for the disabled image*/
	protected String imageURLDisabled;
	
	/**A description used for tooltips anyway*/
	protected String description;
	
	
	
	/**
	* Sets the parameter as global variables and create an icon with "createIconButton" method
	*
	* @param desc a description used for tooltips 
	* @param imageURLOver the url for the image displayed if the cursor is hovering over the button 
	* @param imageURLActive  the url for the image displayed if active
	* @param imageURLDisabled the url for the disabled image
	* @param enabled if true, sets the iconButton enabled status true; otherwise false
	*/
	public IconButton(String desc,String imageURLActive,String imageURLOver, String imageURLDisabled,boolean enabled)
	{
		super();
		this.tooltipActive = desc;
		this.tooltipInactive = desc;
		this.description = desc;
		this.imageURLActive = imageURLActive;
		this.imageURLOver = imageURLOver;
		this.imageURLDisabled = imageURLDisabled;
		this.enabled = enabled;
		createIconButton();
	}   
	
	/**
	* Sets the parameter as global variables and create an icon with "createIconButton" method<br>
	* Also sets the default value for enabled status "true"
	* @param desc a description used for tooltips
	* @param imageURLOver the url for the image displayed if the cursor is hovering over the button 
	* @param imageURLActive the url for the image displayed if active
	* @param imageURLDisabled the url for the disabled image
	*/
	public IconButton(String desc,String imageURLActive,String imageURLOver, String imageURLDisabled)
	{
		this(desc, imageURLActive, imageURLOver, imageURLDisabled, true);
	} 
	
	/**
	* Creates an icon with global variables <br>
	* (icon, description, preferred size, enabled status, selected icon and (if given) a disabled icon)
	*/
	public void createIconButton()
	
	{
		setIcon(Globals.createImageIcon(this.imageURLActive,""));
		setToolTipText(description);
		setPreferredSize(Globals.graphicButtonDimension);
		setEnabled(this.enabled);
		setSelectedIcon(Globals.createImageIcon(this.imageURLOver, "" ));
		if (imageURLDisabled.length()>3)
			setDisabledIcon(Globals.createImageIcon(this.imageURLDisabled, "" ));
	} 
	
	/**
	* Creates an icon with parameter
	* 
	* @param desc a description used for tooltips 
	* @param imageURLOver the url for the image displayed if the cursor is hovering over the button 
	* @param imageURLActive the url for the image displayed if active
	* @param imageURLDisabled the url for the disabled image
	* @param enabled if true, sets the enabled status true; otherwise false
	*/
	public void createIconButton(String desc,String imageURLActive,String imageURLOver, String imageURLDisabled,boolean enabled)
	
	{
		setIcon(Globals.createImageIcon(imageURLActive,""));
		setToolTipText(desc);
		setPreferredSize(Globals.graphicButtonDimension);
		setEnabled(enabled);
		setSelectedIcon(Globals.createImageIcon(imageURLOver, "" ));
		if (imageURLDisabled.length()>3)
			setDisabledIcon(Globals.createImageIcon(imageURLDisabled, "" ));
	} 
	
	
	/**
	* Sets an active and inactive tooltiptext
	* @param tipActive sets this tooltip if the button is active
	* @param tipInactive sets this tooltip if the button is inactive
	*/
	public void setToolTips(String tipActive, String tipInactive)
	{
		this.tooltipActive = tipActive;
		this.tooltipInactive = tipInactive;
	}
	
	/**
	* Sets an image for active icon button 
	* and an image if the curser is hovering over the button
	* @param imageURLActive the new url for the image displayed if active
	* @param imageURLOver the new url for the image displayed if the cursor is hovering over the button 
	*/
	public void setNewImage(String imageURLActive, String imageURLOver )
	{
		setIcon(Globals.createImageIcon(imageURLActive,""));
		setSelectedIcon(Globals.createImageIcon(imageURLOver, "" ));
	}
	
	/**
	* Sets the tooltiptext for active button if paramaterer "a" is true <br> 
	* and the tooltiptext for inactive button if parameter "a" is false
	* @param a sets the activate status for button used for tooltiptext
	*/
	public void setActivated(boolean a) 
	{
		activated = a;
		if (tooltipActive != null && tooltipInactive != null)
		{
			if (a)
				setToolTipText(tooltipActive);
			else
				setToolTipText(tooltipInactive);
		}
	}
	/**
	*@return current active status
	*/
	public boolean isActivated() {
		return activated;
	}
}
