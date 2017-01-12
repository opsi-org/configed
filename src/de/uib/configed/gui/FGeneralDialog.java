package de.uib.configed.gui;

/**
 * FGeneralDialog
 * Copyright:     Copyright (c) 2001-2016
 * Organisation:  uib
 * @author Rupert Röder
 */

import de.uib.configed.*;
import java.awt.*;
import java.util.*;
import javax.swing.*;
import java.awt.event.*;
import de.uib.utilities.logging.*;
import de.uib.utilities.swing.*;
import de.uib.utilities.observer.RunningInstancesObserver;
import de.uib.utilities.observer.RunningInstances;

public class FGeneralDialog extends JDialog
			implements ActionListener, KeyListener
{
	
	/*
	public static class DialogCollector extends HashMap<FGeneralDialog, String>
	{
		private java.util.List<DialogInstancesFollower> followers;
		
		DialogCollector()
		{
			followers = new ArrayList<DialogInstancesFollower>();
		}
			
		public void addFollower(DialogInstancesFollower aFollower)
		{
			followers.add(aFollower);
		}
		
		public void removeFollower(DialogInstancesFollower aFollower)
		{
			followers.remove(aFollower);
		}
			
		
		@Override
		public String put(FGeneralDialog key, String v)
		{
			String result =  super.put(key, v);
			for (DialogInstancesFollower aFollower : followers)
			{
				aFollower.instancesChanged();
			}
			return result;
		}
		
		
		@Override
		public String remove(Object key)
		{
			String result = super.remove(key);
			for (DialogInstancesFollower aFollower : followers)
			{
				aFollower.instancesChanged();
			}
			return result;
		}
		
	}
	
	public static interface DialogInstancesFollower
	{
		public void instancesChanged();
		
		public void actOnDialogInstances();
	}
	
	public static DialogCollector existingInstances;
	
	static {existingInstances = new DialogCollector();}
	*/
	
	boolean shiftPressed = true;

	protected FadingMirror glass;
	protected JPanel allpane = new JPanel();

	protected JScrollPane scrollpane;
	protected JPanel topPane = new JPanel();
	protected JPanel southPanel = new JPanel();
	//JTextArea jTextArea1 = new JTextArea();
	protected JButton jButton1 = new JButton();
	protected JButton jButton2 = new JButton();
	protected JButton jButton3 = new JButton();
	static final int DEFAULT = 1;
	static final int OK = 1;
	static final int NO = 2;

	private static int defaultPreferredWidth  = 250;
	private static int defaultPreferredHeight = 150;

	protected int preferredWidth ;
	protected int preferredHeight;

	protected String button1Text = configed.getResourceValue("FGeneralDialog.ok");
	protected String button2Text = configed.getResourceValue("FGeneralDialog.ignore");
	protected String button3Text = configed.getResourceValue("FGeneralDialog.empty");
	
	protected Object[] buttonNames;


	protected int noOfButtons = 3;

	Color myHintYellow = new java.awt.Color(255, 255, 230);


	int result = 1;
	int value1 = OK;
	int value2 = NO;


	protected JPanel jPanelButtonGrid = new JPanel();
	protected JPanel additionalPane;
	protected GridLayout gridLayout1 = new GridLayout();
	protected BorderLayout borderLayout1 = new BorderLayout();
	protected FlowLayout flowLayout1 = new FlowLayout();
	//JLabel jLabel1 = new JLabel();

	JFrame owner;
	
	protected JProgressBar waitingProgressBar; //for use in derived classes

	public FGeneralDialog(JFrame owner, String title, JPanel pane)
	{
		super(owner, false);
		logging.info(this, "created by constructor 1");
		registerWithRunningInstances();
		this.owner = owner;
		setTitle (title);
		setFont(Globals.defaultFont);
		setIconImage (Globals.mainIcon);
		additionalPane  = pane;
	}
	
	protected void registerWithRunningInstances()
	{
		logging.info(this, "registerWithRunningInstances");
		//if ( !isModal() )
			FEditObject.runningInstances.add(this, "");
	}
		

	protected void setup()
	{
		initComponents();
		guiInit();
		pack();
	}

	public FGeneralDialog(Frame owner, String title, boolean modal)
	{
		super(owner, modal);
		logging.info(this, "created by constructor 2");
		registerWithRunningInstances();
		setTitle (title);
		setFont(Globals.defaultFont);
		setIconImage (Globals.mainIcon);

		additionalPane = new JPanel();
		setup();
		
		
	}

	public FGeneralDialog(Frame owner, String title, boolean modal, int lastButtonNo)
	{
		this (owner, title, modal, new String[]{configed.getResourceValue("FGeneralDialog.ok"),  configed.getResourceValue("FGeneralDialog.ignore")}, lastButtonNo, defaultPreferredWidth, defaultPreferredHeight);
	}

	/*
	public FInfoDialog(Frame owner, String title, String message, boolean modal, int lastButtonNo)
{
	this (owner, title, modal, lastButtonNo);
	setMessage ();
}

	*/


	public FGeneralDialog(Frame owner, String title, boolean modal, Object[] buttonList)
	{
		this (owner, title, modal, buttonList, defaultPreferredWidth, defaultPreferredHeight);
	}

	public FGeneralDialog(Frame owner, String title, boolean modal, Object[] buttonList, int preferredWidth, int preferredHeight)
	{
		this (owner, title, modal, buttonList, buttonList.length, preferredWidth, preferredHeight);
	}

	public FGeneralDialog(Frame owner, String title, boolean modal, Object[] buttonList, int lastButtonNo, int preferredWidth, int preferredHeight)
	{
		super(owner, modal);
		logging.info(this, "created by constructor 3");
		registerWithRunningInstances();

		setIconImage (Globals.mainIcon);

		glass = new FadingMirror();
		setGlassPane(glass);

		this.noOfButtons = lastButtonNo;
		this.buttonNames = buttonList;
		setButtons();

		this.preferredWidth = preferredWidth;
		this.preferredHeight = preferredHeight;

		setTitle (title);
		setFont(Globals.defaultFont);

		initComponents();
		guiInit();


	}


	public int getResult()
	{
		return result;
	}
	
	
	protected void setButtons()
	{
		logging.info(this, "setButtons " +  java.util.Arrays.asList( buttonNames ) );
		button1Text = (String) buttonNames[0];

		if (noOfButtons> 1)
		{ 
			button2Text = (String) buttonNames[1];
			jButton2.setText( button2Text ); 
		}

		if( noOfButtons > 2 )
		{
			button3Text = (String) buttonNames[2];
			jButton3.setText( button3Text );
		}
	}
	

	protected void initComponents()
	{
		if (additionalPane == null)
			additionalPane = new JPanel();
		additionalPane.setVisible(false);
		
		//setButtons();
	}

	private void guiInit()
	{
		allpane.setLayout(borderLayout1);
		allpane.setBackground(Globals.backLightBlue);//Globals.nimbusBackground);//Globals.backgroundWhite);//(myHintYellow);
		allpane.setPreferredSize (new Dimension(preferredWidth, preferredHeight));
		allpane.setBorder(BorderFactory.createEtchedBorder());

		initScrollPane();
		initOther_protected();
		jButton1.setFont(Globals.defaultFont);
		jButton1.setPreferredSize(new Dimension(Globals.buttonWidth, Globals.buttonHeight - 2));//(new Dimension(91, 20));
		jButton1.setText(button1Text);
		jButton2.setFont(Globals.defaultFont);
		jButton2.setPreferredSize(new Dimension(Globals.buttonWidth, Globals.buttonHeight - 2));//(new Dimension(91, 20));
		jButton2.setText(button2Text);
		jButton3.setFont(Globals.defaultFont);
		jButton3.setPreferredSize(new Dimension(Globals.buttonWidth, Globals.buttonHeight - 2));//(new Dimension(91, 20));
		jButton3.setText(button3Text);
		jPanelButtonGrid.setLayout(gridLayout1);
		southPanel.setOpaque(false);

		jPanelButtonGrid.setOpaque(false);

		getContentPane().add(allpane);

		GroupLayout southLayout = new GroupLayout(southPanel);
		southPanel.setLayout(southLayout);

		southLayout.setHorizontalGroup(
		    southLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
		    .addGroup(southLayout.createSequentialGroup()
		              .addGap(Globals.hGapSize/2, Globals.hGapSize, Short.MAX_VALUE)
		              .addComponent(jPanelButtonGrid, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
		              .addGap(Globals.hGapSize/2, Globals.hGapSize, Short.MAX_VALUE)
		             )
		    .addGroup(southLayout.createSequentialGroup()
		              .addContainerGap()
		              .addComponent(additionalPane)
		              .addContainerGap()
		             )
		)
		;

		southLayout.setVerticalGroup(
		    southLayout.createSequentialGroup()
		    .addGap(Globals.vGapSize/2, Globals.vGapSize/2, Globals.vGapSize/2)
		    .addComponent(additionalPane, Globals.lineHeight, Globals.lineHeight, Globals.lineHeight)
		    .addGap(Globals.vGapSize, Globals.vGapSize, Globals.vGapSize)
		    .addComponent(jPanelButtonGrid, Globals.lineHeight, Globals.lineHeight, Globals.lineHeight)
		    .addGap(Globals.vGapSize/2, Globals.vGapSize/2, Globals.vGapSize/2)
		)
		;

		allpane.add(southPanel, BorderLayout.SOUTH);

		southPanel.add(jPanelButtonGrid, null);
		jPanelButtonGrid.add(jButton1, null);
		//jPanelButtonGrid.add(jLabel1, null);
		if (noOfButtons > 1)
		{
			jPanelButtonGrid.add(jButton2, null);}
		if( noOfButtons > 2 )
			jPanelButtonGrid.add(jButton3, null );


		//jTextArea1.addKeyListener(this);
		jButton1.addKeyListener(this);
		jButton2.addKeyListener(this);
		jButton3.addKeyListener(this);

		jButton1.addActionListener(this);
		jButton2.addActionListener(this);
		jButton3.addActionListener(this);

		pack();
		centerOn(null);
	}


	private int intHalf (double x)
	{
		return (int) (x/2);
	}


	public void centerOn(Component master)
	{
		int startX = 0;
		int startY = 0;

		Point masterOnScreen = null;

		boolean centerOnMaster = (master != null);

		if (centerOnMaster)
		{
			try
			{
				masterOnScreen = master.getLocationOnScreen();
			}
			catch(Exception ex)
			{
				logging.debug(this, "centerOn " + master  + " ex: " + ex);
				centerOnMaster = false;
			}
		}

		if (!centerOnMaster)
		{
			//center on Screen

			Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();


			startX = (screenSize.width  - getSize().width)/ 2;

			startY = (screenSize.height - getSize().height)/2;

		}
		else
		{
			//logging.debug(this, "centerOn (int) masterOnScreen.getX()  " + (int) masterOnScreen.getX());
			//logging.debug(this, "centerOn (int) masterOnScreen.getY()  " + (int) masterOnScreen.getY());
			//logging.debug(this, "centerOn master.getWidth()  " +  master.getWidth() / 2);
			//logging.debug(this, "centerOn master.getHeight()  " +  master.getHeight() / 2) ;
			//logging.debug(this, "centerOn this.getSize() " + getSize());

			//logging.debug(this, "centerOn " + master.getClass() + ", " + master);

			startX = (int) masterOnScreen.getX() +  intHalf ( master.getWidth() )   -  intHalf( getSize().getWidth() );
			startY = (int) masterOnScreen.getY() +  intHalf ( master.getHeight() )  -  intHalf( getSize().getHeight() );

			//problem: in applet in windows, we may leave the screen
			Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
			//logging.debug(this, "centerOn screenSize " + screenSize);


			if (startX + getSize().width > screenSize.width)
				startX = screenSize.width - getSize().width;

			if (startY + getSize().height > screenSize.height)
				startY = screenSize.height - getSize().height;

		}

		setLocation(startX, startY);
	}

	protected void initOther_protected()
	{
		topPane = new JPanel();
		topPane.setLayout(flowLayout1);
		//System.out.println("FInfoDialog initTopPane is working");
	}

	protected void initScrollPane()
	{
		if (scrollpane == null)
			scrollpane = new JScrollPane();

		scrollpane.setBackground(Color.white);
		scrollpane.setOpaque(false);

		allpane.add(scrollpane, BorderLayout.CENTER);
	}

	public void paint (Graphics g)
	{ super.paint (g);
		jButton1.requestFocus();
	}

	public void doAction1()
	{
		logging.debug(this, "doAction1");
		result = 1;
		leave();
	}

	public void doAction2()
	{
		result = 2;
		leave();
	}

	public void doAction3()
	{
		result = 3;
		leave();
	}

	public void leave ()
	{
		setVisible (false);

		dispose ();
		FEditObject.runningInstances.forget(this);
	}

	public void setButtonsEnabled(boolean b)
	{
		jButton1.setEnabled(b);
		jButton2.setEnabled(b);
	}

	// Events
	// window

	protected void processWindowEvent(WindowEvent e)
	{
		if(e.getID() == WindowEvent.WINDOW_CLOSING)
		{
			result = DEFAULT;
			leave ();
		}
		else
			super.processWindowEvent(e);
	}



	// KeyListener

	public void keyPressed (KeyEvent e)
	{
		if (e.getKeyCode() == KeyEvent.VK_SHIFT)
		{ shiftPressed = true;
			// System.out.println ("shift pressed");
		}
		else
		{
			if (e.getKeyCode() == KeyEvent.VK_ENTER)
			{
				//System.out.println ("ENTER");
				if (e.getSource() == jButton1)
				{
					//System.out.println (".... on Button1. ");
					doAction1();
				}
				else if (e.getSource () == jButton2)
				{
					doAction2();
					//System.out.println (".... on Button2 ");
				}
				else if( e.getSource() == jButton3 )
				{
					doAction3();
				}
			}
		}
	}

	public void keyReleased (KeyEvent e)
	{
		if (e.getKeyCode() == KeyEvent.VK_SHIFT)
		{ shiftPressed = false;
			//System.out.println ("shift released");
		}

	}

	public void keyTyped (KeyEvent e)
	{
		//System.out.println ("KeyEvent ... " + 	e.getKeyChar) );
	}


	//ActionListener
	public void actionPerformed (ActionEvent e)
	{
		//System.out.println ("ActionEvent ...... ");
		if (e.getSource() == jButton1)
		{
			//System.out.println (".... on Button1. ");
			doAction1();}

		else if (e.getSource () == jButton2)
		{ doAction2();
			//System.out.println (".... on Button2 ");
		}
		else if( e.getSource() == jButton3 )
		{
			doAction3();
		}
	}

	public void glassTransparency(boolean vanishing, int initialWaitMs, int delayMs, float step)
	{
		glass.setVisible(true);
		glass.setOpaque(false);
		glass.setStep(step);
		glass.setDirection(vanishing);
		glass.setDelay(initialWaitMs, delayMs);
		glass.begin();
	}


	public static class FadingMirror extends JPanel
				implements ActionListener
	{
		private float opacity = 1f;
		private float step = 0.3f;
		private javax.swing.Timer fadeTimer;
		private int initialDelay = 100;
		private int delay = 100;
		private boolean vanishing = true;


		public void setDirection(boolean vanishing)
		{
			this.vanishing = vanishing;

			if (vanishing)
				opacity = 1f;
			else
				opacity = 0f;
		}

		public void setStep(float f)
		{
			step = f;
		}

		public void setDelay(int initialDelayMs, int delayMs)
		{
			initialDelay = initialDelayMs;
			delay = delayMs;
		}

		public void begin()
		{
			fadeTimer = new javax.swing.Timer(initialDelay, this);
			fadeTimer.setDelay(delay);
			fadeTimer.start();
		}

		public void actionPerformed(ActionEvent e)
		{
			//logging.debug(this, "fade, opacity " + opacity);

			if (vanishing)
			{
				opacity -= step;
				if (opacity < 0)
				{
					opacity = 0;
					if (fadeTimer != null)
					{
						fadeTimer.stop();
						fadeTimer = null;
					}
				}
			}
			else
			{
				opacity += step;

				if (opacity > 1)
				{
					opacity = 1;
					
					if (fadeTimer != null)
					{
						fadeTimer.stop();
						fadeTimer = null;
					}
				}
			}

			repaint();
		}

		public void paintComponent(Graphics g)
		{
			((Graphics2D) g).setComposite(
			    AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacity)
			);

			g.setColor(new Color(230,230,250));
			g.fillRect(0,0,getWidth(), getHeight());
		}
	}


}
