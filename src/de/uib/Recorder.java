import javax.swing.text.*;
import java.io.*;
import java.time.*;
import java.util.*;
import java.util.logging.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.table.*;
import javax.swing.*;
import java.util.logging.*;

public class Recorder {
	
	public static final int RECORDMODE_DONOTHING = 0;
	public static final int RECORDMODE_RECORD = 1;
	public static final int RECORDMODE_REPLAY = 2;
	
	private static final String PATH_PROPERTIES = "./tests/gui/properties.txt";
	private static final String PROPERTY_RECORDMODE = "recordmode";
	private static final String PROPERTY_RECORDMODE_DONOTHING = "donothing";
	private static final String PROPERTY_RECORDMODE_RECORD = "record";
	private static final String PROPERTY_RECORDMODE_REPLAY = "replay";
	private static final String PROPERTY_PATH_RECORDFILE = "pathrecordfile";
	
	private static final String SEPARATOR = ";";
	private static final String ACTION_KEYEVENT = "keyevent";
	private static final String ACTION_MOUSEEVENT = "mouseevent";
	private static final String ACTION_SLEEP = "sleep";
	
	private static final Logger log = Logger.getLogger(Recorder.class.getName());
	
	private String pathRecordfile = "./record.txt";
	private File recordFile;
	private PrintWriter recordStream;
	private Instant timestampLastAction = Instant.now();
	
	private int recordMode = Recorder.RECORDMODE_DONOTHING;
	
	private Hashtable<String, Component> allComponents;
	private Hashtable<Component, String> allStrings;
	
	public Recorder(Hashtable<String, Component> allComponents, 
						Hashtable<Component, String> allStrings) {
		
		this.allComponents = allComponents;
		this.allStrings = allStrings;
		
		setProperties();
		
		recordFile = new File(pathRecordfile);
		
		switch(recordMode) {
			case RECORDMODE_RECORD:
				prepareRecording();
				break;
				
			case RECORDMODE_REPLAY:
				startReplay();
				break;
		}
	}
	
	private void setProperties() {
		
		Properties properties = new Properties();
		try {
			File propertyFile = new File(PATH_PROPERTIES);
			InputStream propertyStream = new FileInputStream(propertyFile);
			properties.load(propertyStream);
			
		} catch(IOException ioe) {
			log.severe("Properties cannot be loaded");
			System.exit(0);
		}
		
		String stringRecordMode = properties.getProperty(PROPERTY_RECORDMODE,
														PROPERTY_RECORDMODE_DONOTHING);

		if(stringRecordMode.equals(PROPERTY_RECORDMODE_DONOTHING))
			recordMode = Recorder.RECORDMODE_DONOTHING;
			
		else if(stringRecordMode.equals(PROPERTY_RECORDMODE_RECORD))
			recordMode = Recorder.RECORDMODE_RECORD;
		
		else if(stringRecordMode.equals(PROPERTY_RECORDMODE_REPLAY))
			recordMode = Recorder.RECORDMODE_REPLAY;
			
		pathRecordfile = properties.getProperty(PROPERTY_PATH_RECORDFILE, pathRecordfile);
	}
	
	public void record(KeyEvent e) {
		recordTimegap();
		
		Component component = e.getComponent();
		int id = e.getID();
		int keyCode = e.getKeyCode();
		String hierarchy = allStrings.get(component);
		
		if(hierarchy == null) {
	//		System.out.println(component);
	//		System.out.println();
			return;
		}
		
		recordStream.print(ACTION_KEYEVENT + SEPARATOR);
		recordStream.print(id + SEPARATOR);
		recordStream.print(keyCode + SEPARATOR);
		recordStream.println(hierarchy + SEPARATOR);
		recordStream.flush();
	}
	
	public void record(MouseEvent e) {
		recordTimegap();
		
		Component component = (Component) e.getSource();

		int id = e.getID();
		long when = e.getWhen();
		int modifiers = e.getModifiers();
		Point point = e.getPoint();
		int clickCount = e.getClickCount();
		boolean isPopupTrigger = e.isPopupTrigger();
		int button = e.getButton();
		String hierarchy = allStrings.get(component);
		
		if(hierarchy == null) {
		//	System.out.println(component);
		//	System.out.println();
			return;
		}
		
		recordStream.print(ACTION_MOUSEEVENT + SEPARATOR);
		recordStream.print(id + SEPARATOR);
		recordStream.print(when + SEPARATOR);
		recordStream.print(modifiers + SEPARATOR);
		recordStream.print(point.x + SEPARATOR);
		recordStream.print(point.y + SEPARATOR);
		recordStream.print(clickCount + SEPARATOR);
		recordStream.print(isPopupTrigger + SEPARATOR);
		recordStream.print(button + SEPARATOR);
		recordStream.println(hierarchy + SEPARATOR);
		recordStream.flush();
	}
	
	private void recordTimegap() {
		
		Instant tmp = Instant.now();
		Duration duration = Duration.between(timestampLastAction, tmp);
		recordStream.print(ACTION_SLEEP + SEPARATOR);
		recordStream.println(duration.toMillis() + SEPARATOR);
		recordStream.flush();
		timestampLastAction = tmp;
	}
	
	private void prepareRecording() {
		try {
		
		recordStream = new PrintWriter(recordFile, "UTF-8");
		} catch(Exception e) {
			log.severe("Recordstream cannot be loaded to write record");
			System.exit(0);
		}
		
		Toolkit.getDefaultToolkit().addAWTEventListener(new AWTEventListener() {
			public void eventDispatched(AWTEvent e)
			{
				if(e instanceof KeyEvent)
					record((KeyEvent) e);
					
				else if(e instanceof MouseEvent)
					record((MouseEvent) e);
			}
		}, AWTEvent.MOUSE_EVENT_MASK | AWTEvent.KEY_EVENT_MASK | AWTEvent.MOUSE_MOTION_EVENT_MASK);
	}
	
	public void startReplay() {
		new ReplayThread().start();
	}
	
	private class ReplayThread extends Thread {
		
		@Override
		public void run() {
			BufferedReader br;
			try {
				br = new BufferedReader(new FileReader(recordFile));
				String action;
				while((action = br.readLine()) != null)
					doAction(action);
					
				System.exit(0);
			} catch(FileNotFoundException e) {
				log.severe("Recordfile cannot be found to replay the record");
				System.exit(0);
			} catch(IOException e) {
				log.warning("Recordfile cannot be loaded or read out");
			}
		}
		
		private void doAction(String action) {
			String[] parts = action.split(SEPARATOR);

			if(parts[0].equals(ACTION_KEYEVENT))
				doKeyEvent(parts);
			
			else if(parts[0].equals(ACTION_MOUSEEVENT))
				doMouseEvent(parts);
				
			else if(parts[0].equals(ACTION_SLEEP))
				doSleep(parts);
		}
		
		private void doKeyEvent(String[] parts) {
			int id = Integer.parseInt(parts[1]);
			int keyCode = Integer.parseInt(parts[2]);
			String hierarchy = parts[3];
			
			Component component = allComponents.get(hierarchy);
			if(component == null) {
				System.out.println("null");
				return;
			}
			
			if(!component.hasFocus())
				component.requestFocus();
				
			try {
				if(id == KeyEvent.KEY_PRESSED)
					new Robot().keyPress(keyCode);
				
				else if(id == KeyEvent.KEY_RELEASED)
					new Robot().keyRelease(keyCode);
			} catch(Exception e) {}
		}
		
		private void doMouseEvent(String[] parts) {
			int id = Integer.parseInt(parts[1]);
			long when = Long.parseLong(parts[2]);
			int modifiers = Integer.parseInt(parts[3]);
			int pointX = Integer.parseInt(parts[4]);
			int pointY = Integer.parseInt(parts[5]);
			int clickCount = Integer.parseInt(parts[6]);
			boolean isPopupTrigger = Boolean.parseBoolean(parts[7]);
			int button = Integer.parseInt(parts[8]);
			String hierarchy = parts[9];
			
			Component component = allComponents.get(hierarchy);
			if(component == null) {
				System.out.println("null");
				return;
			}
			
			MouseEvent mouseEvent = new MouseEvent(component, id, when, modifiers,
												pointX, pointY, clickCount, isPopupTrigger, button);
			
			component.dispatchEvent(mouseEvent);
		}
		
		private void doSleep(String[] parts) {
			
			try {
				long waitTimeMillis = Long.parseLong(parts[1]);
				
				Thread.sleep(waitTimeMillis);
			} catch(Exception e) {}
		}
	}
}
