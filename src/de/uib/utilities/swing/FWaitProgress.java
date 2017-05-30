package de.uib.utilities.swing;


//Version:
//Copyright:    Copyright (c) 1999-2013 uib.de
//Autor:       Rupert Röder


import java.awt.*;
import javax.swing.*;
import java.awt.event.*;
import de.uib.utilities.*;
import de.uib.utilities.logging.*;
import de.uib.configed.configed;



public class FWaitProgress extends JFrame
			implements WindowListener, de.uib.utilities.observer.DataLoadingObserver
{

	JProgressBar jProgressBar1;
	JLabel jLabel1;
	protected String info;
	protected Component home;

	protected int waitUntilShow;
	protected int maxShowing;
	SetWaitCursor sWaitCursor;
	boolean sendingToFront = false;
	protected Object observingMesg = configed.getResourceValue("LoadingObserver.start");


	static final String waitString = " "; // configed.getResourceValue("FWaitProgress.label");

	String[] waitStrings;

	//int max = 60;

	int max = 200;

	private boolean showing = true;
	ThreadProgressBar threadProgress;

	public FWaitProgress(String title, String info, Component home, int waitUntilShow, int maxShowing)
	{
		logging.info(this, "created with title " + title);

		setTitle(title);

		this.info = info;
		this.home = home;

		this.waitUntilShow = waitUntilShow;
		this.maxShowing = maxShowing;

		addWindowListener(this);
		createGUI();
		start();
		setVisible(true);
		
		logging.info(this, "should be visible ");
		
		

		

	}

	public FWaitProgress(String title, String info, Component home, int waitUntilShow)
	{
		this (title, info, null, waitUntilShow, 0);
	}


	public FWaitProgress(String title, String info, int waitUntilShow)
	{
		this (title, info, null, waitUntilShow);
	}

	//DataLoadingObserver
	public void gotNotification(Object mesg)
	{
		observingMesg = mesg;
	}

	private void createGUI()
	{
		setIconImage(Globals.mainIcon);

		waitStrings = new String[]{
		                  waitString + "       ",
		                  waitString + " .     ",
		                  waitString + " ..    ",
		                  waitString + " ....  ",
		                  waitString + " ..... " };



		/* seems not to work
		UIManager.put("ProgressBar.background", Color.BLUE); 
			UIManager.put("ProgressBar.foreground", Color.CYAN);
			UIManager.put("ProgressBar.selectionBackground", Color.BLACK);
			UIManager.put("ProgressBar.selectionForeground", Color.GREEN);
			*/


		jProgressBar1 = new JProgressBar();

		jProgressBar1.setEnabled(true);
		jProgressBar1.setMaximum(max);

		UIDefaults defaults = new UIDefaults();
		defaults.put("ProgressBar[Enabled].foregroundPainter", new MyPainter( Globals.opsiLogoBlue ));
		defaults.put("ProgressBar[Enabled].backgroundPainter", new MyPainter( Globals.opsiLogoLightBlue ));
		jProgressBar1.putClientProperty("Nimbus.Overrides", defaults);


		jLabel1 = new JLabel();

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
		                                    .addComponent(jLabel1, 100, 300, Short.MAX_VALUE)
		                                    .addGap(10, 10, Short.MAX_VALUE)
		                                   )
		                          .addGroup(layout.createSequentialGroup()
		                                    .addGap(10, 10, 30)
		                                    .addComponent(jProgressBar1, 100, 350, Short.MAX_VALUE)
		                                    .addGap(10, 10, 30)
		                                   )
		                         );
		layout.setVerticalGroup( layout.createSequentialGroup()
		                         .addComponent(iconLabel, 150, 150, 150)
		                         .addComponent(jProgressBar1,  Globals.progressBarHeight, Globals.progressBarHeight, Globals.progressBarHeight)
		                         .addComponent(jLabel1, 30, 30, 30)

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


	public Dimension getPreferredSize()
	{
		return new Dimension (640, 65);
	}

	public void start()
	{
		showing = true;
		try
		{
			if (waitUntilShow == 0) setVisible(true);
			if (home != null) //waitCursor für home-Komponente starten
			{
				sWaitCursor = new SetWaitCursor(home);
				sWaitCursor.start();
			}

			if (info == null)
				setInfo(waitStrings[0]);
			else
				setInfo(info);



			//threadProgress = new ThreadProgressBar(this);
			// threadProgress.setPriority(Thread.MAX_PRIORITY);
			//threadProgress.start();
			//pureProgress = new Waiting (this);
			//pureProgress.start();

		}
		catch(Exception e)
		{
			logging.info(this, "ex " + e);
		}
	}

	public void stop()
	{
		logging.info(this, "stop");
		showing = false;
		setVisible(false);
	}

	public void setWaitUntilShow (int waitUntilShow)
	{
		this.waitUntilShow = waitUntilShow;
	}

	public void setInfo(String newInfo)
	{
		//logging.info(this, "newInfo " + newInfo);
		jLabel1.setText(newInfo);
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
		sendingToFront = true;
	}

	public void	windowDeactivated(WindowEvent e)
	{}

	public void	windowDeiconified(WindowEvent e)
	{}

	public void	windowIconified(WindowEvent e)
	{
		sendingToFront = false;
		//System.out.println ("sendingToFront " + sendingToFront);
	}

	public void	windowOpened(WindowEvent e)
	{}



	class ThreadProgressBar extends Thread
	{
		//FWaitProgress home;

		ThreadProgressBar (FWaitProgress home)
		{
			//this.home = home;
			//home.setVisible(false); //vgl. show() oben
		}

		public void run()
		{
			int progressIndex = 0;
			int i = 0;

			try
			{
				while (showing)
				{
					if (progressIndex % 4 == 0)
						logging.info(this, "progressIndex " + progressIndex);

					jProgressBar1.setValue(progressIndex);

					progressIndex++;


					if (progressIndex > waitUntilShow)
					{

						i++;
						if (i > 4) i = 0;

						//logging.info(this, "i " + i + " waitStrings " + waitStrings[i]);


						//setInfo( waitStrings[i]);

						setInfo("" + observingMesg + " " + waitStrings[i]);


						//update(getGraphics()); //seems to be necessary for the applet

						if (sendingToFront)
						{
							setVisible(true);
						}
					}

					//yield();
					if (showing) sleep(400);

					if (maxShowing > 0 &&  progressIndex > maxShowing)
						showing = false;

					if (jProgressBar1.getValue() >= jProgressBar1.getMaximum())
						showing = false;

					//System.out.println("progressIndex " + progressIndex);

				}

				sleep (10);
				jProgressBar1.setValue(0);
				jProgressBar1.setEnabled(false);
				setVisible (false);
				// System.out.println("progressIndex " + progressIndex);

			}
			catch (InterruptedException ex)
			{
			}

			if (sWaitCursor != null)
				sWaitCursor.showing = false;
			dispose();

		}
	}

}

class SetWaitCursor extends Thread
{
	Component home;
	boolean showing;

	SetWaitCursor (Component home)
	{ this.home = home;
		showing = true;
	}

	public void run()
	{
		int waitProgressindex = 0;
		try
		{
			while (showing)
			{
				if (home != null) home.setCursor(java.awt.Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
				sleep(100);
				waitProgressindex++;
				//System.out.println(" waitCursor showing " + waitProgressindex);

			}

			// restore cursor
			if (home != null) home.setCursor(java.awt.Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
		}
		catch (InterruptedException ex)
		{
		}
	}
}




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
