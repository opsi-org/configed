package de.uib.utilities.swing;

/**
 * FLoadingWaiter
 * Copyright:     Copyright (c) 2016
 * Organisation:  uib
 * @author Rupert Röder
 */

import java.util.*;
import java.awt.*;
import javax.swing.*;
import java.awt.event.*;
import de.uib.utilities.*;
import de.uib.utilities.logging.*;
import de.uib.utilities.thread.*;
import de.uib.configed.configed;
//import de.uib.utilities.swing.timeedit.*;



public class FLoadingWaiter extends JFrame
			implements 
			WindowListener, 
			de.uib.utilities.observer.DataLoadingObserver,
			WaitingSleeper
{

	private final long WAITING_MILLIS_FOR_LOADING = 50000;
	private final long ESTIMATED_TOTAL_WAIT_MILLIS = 10000;
	JProgressBar progressBar;
	JLabel infoLabel;
	protected String info;
	protected static WaitInfoString waitInfoString; 

	protected Object observingMesg = configed.getResourceValue("LoadingObserver.start");


	String[] waitStrings;
	int waitStringsIndex = -1;

	//int max = 60;

	int max = 200;

	private boolean showing = true;
	
	private WaitingWorker worker;
	
	class MyPainter implements Painter<JProgressBar> {

		private final Color color;
	
		public MyPainter(Color c1) {
			this.color = c1;
		}
		@Override
		public void paint(Graphics2D gd, JProgressBar t, int width, int height) {
			gd.setColor(color);
			gd.fillRect(0, 0, width, height);
		}
	}
	
	public FLoadingWaiter(String title, String startMessage)
	{
		this(title);
		observingMesg = startMessage;;
	}
	
	
	public FLoadingWaiter(String title)
	{
		super(title);
		
		addWindowListener(this);
		createGUI();
		setVisible(true);
		logging.info(this, "should be visible ");
		if (waitInfoString == null)
			waitInfoString = new WaitInfoString();
		
		worker = new WaitingWorker(this);
		/*
		{
			@Override
			protected void process( java.util.List<Long> listOfMillis )
			{
				super.process(listOfMillis);
				statusLabel.setText("abc");
			}
		}
		*/
			
		
	}
	
	public void stopWaiting()
	{
		worker.stop();
	}

	
	//DataLoadingObserver
	public void gotNotification(Object mesg)
	{
		observingMesg = mesg;
	}

	private void createGUI()
	{
		setIconImage(Globals.mainIcon);

		/* seems not to work
		UIManager.put("ProgressBar.background", Color.BLUE); 
			UIManager.put("ProgressBar.foreground", Color.CYAN);
			UIManager.put("ProgressBar.selectionBackground", Color.BLACK);
			UIManager.put("ProgressBar.selectionForeground", Color.GREEN);
			*/


		progressBar = new JProgressBar();

		progressBar.setEnabled(true);
		progressBar.setMaximum(max);

		UIDefaults defaults = new UIDefaults();
		defaults.put("ProgressBar[Enabled].foregroundPainter", new MyPainter( Globals.opsiLogoBlue ));
		defaults.put("ProgressBar[Enabled].backgroundPainter", new MyPainter( Globals.opsiLogoLightBlue ));
		progressBar.putClientProperty("Nimbus.Overrides", defaults);

		infoLabel = new JLabel();

		JPanel panel = new JPanel();
		panel.setBackground(Globals.backLightBlue);

		GroupLayout layout = new GroupLayout(panel);
		panel.setLayout(layout);

		ImageIcon icon = de.uib.configed.Globals.createImageIcon("images/configed_icon.png","");
		JLabel iconLabel = new JLabel(icon);

		layout.setHorizontalGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
		                          .addComponent(iconLabel, 150, 150, 150)
		                          .addGroup(layout.createSequentialGroup()
		                                    .addGap(10, 10, Short.MAX_VALUE)
		                                    .addComponent(infoLabel, 100, 300, Short.MAX_VALUE)
		                                    .addGap(10, 10, Short.MAX_VALUE)
		                                   )
		                          .addGroup(layout.createSequentialGroup()
		                                    .addGap(10, 10, 30)
		                                    .addComponent(progressBar, 100, 350, Short.MAX_VALUE)
		                                    .addGap(10, 10, 30)
		                                   )
		                         );
		layout.setVerticalGroup( layout.createSequentialGroup()
		                         .addComponent(iconLabel, 150, 150, 150)
		                         .addComponent(progressBar,  Globals.progressBarHeight, Globals.progressBarHeight, Globals.progressBarHeight)
		                         .addComponent(infoLabel, 30, 30, 30)

		                       );

		this.getContentPane().add(panel);

		setSize(new Dimension(400, 250));

		//pack();


		// zentrieren
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		Dimension frameSize = this.getSize();
		if (frameSize.height > screenSize.height)
			frameSize.height = screenSize.height;
		if (frameSize.width > screenSize.width)
			frameSize.width = screenSize.width;
		this.setLocation((screenSize.width - frameSize.width) / 2, (screenSize.height - frameSize.height) / 2);
	}


	public void  startWaiting()
	{
		worker.execute();
	}
	
	
	public void setReady()
	{
		worker.setReady();
	}
		
	
	//WaitingSleeper
	public void actAfterWaiting()
	{
		setVisible(false);
		//dispose();
	}
	public JProgressBar getProgressBar()
	{
		return progressBar;
	}
	public JLabel getLabel()
	{
		return infoLabel;
	}
	public long getStartActionMillis()
	{
		return new GregorianCalendar().getTimeInMillis();
	}
	public long getWaitingMillis()
	{
		return WAITING_MILLIS_FOR_LOADING;
	}
	
	public long getOneProgressBarLengthWaitingMillis()
	{
		return ESTIMATED_TOTAL_WAIT_MILLIS;
	}
	
	public String setLabellingStrategy(long millisLevel)
	{
		logging.debug(this, "setLabellingStrategy millis " + millisLevel);
		return "" + observingMesg
		+ " " +  waitInfoString.next(); //??produces strings with ascii null  
		//+ waitStrings[ longVal.intValue() % waitStrings.length];
		//waitStrings[0];
	}


	//windowListener
	public void	windowActivated(WindowEvent e)
	{}

	public void	windowClosed(WindowEvent e)
	{
		//System.out.println(" WindowEvent windowClosed ");
		showing = false;
	}

	public void	windowClosing(WindowEvent e)
	{
		//sendingToFront = true;
	}

	public void	windowDeactivated(WindowEvent e)
	{}

	public void	windowDeiconified(WindowEvent e)
	{}

	public void	windowIconified(WindowEvent e)
	{
		//sendingToFront = false;
		//System.out.println ("sendingToFront " + sendingToFront);
	}

	public void	windowOpened(WindowEvent e)
	{}

}


