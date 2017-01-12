package de.uib.utitlities.swing;

public class JLabelledButton extends JButton
{
	JPanel panel;
	JLabel label;
	String text;
	
	
	public JLabelledButton(String text, Icon icon)
	{
		super(icon);
		this.text = text;
		createGui();
	}
	
	@Override
	public setText(String s)
	{
		label.setText(s);
	}
	
	@Override
	public String getText()
	{
		return label.getText();
	}

	protected void createGui()
	{
		panel = new JPanel();
		label = new JLabel(text);
		
	}
}
		
	
	
		
	
