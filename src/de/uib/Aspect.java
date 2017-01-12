import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.*;
import java.io.*;
import de.uib.configed.gui.*;

public aspect Aspect {
	
	private Hashtable<String, Component> allComponents = new Hashtable<>();
	private Hashtable<Component, String> allStrings = new Hashtable<>();
	
	private Recorder recorder;
	
	private pointcut dialog():
		call(void JOptionPane.*(..));
		
	
	
	void around(): dialog() {
		return;
	}
	
	before(FGeneralDialog f): execution(*.new(..)) && target(f) {
		if(f.isModal()) {
			System.out.println(f.getModalityType());
			f.setModalityType(Dialog.ModalityType.MODELESS);
		}
	}

	private pointcut constructor():
		call(*.new(..));
		
	after() returning(Component component): constructor() {
	
		if(component instanceof FGeneralDialog)
			((FGeneralDialog) component).leave();
			
			
		if(allStrings.get(component) != null)
			return;
			
		addComponent(component);
		
		if(component instanceof Container)
			addAllComponents((Container) component);
	}
	
	private void addAllComponents(Container container) {
		addComponent(container);
		
		Component[] components = container.getComponents();
		for(Component component : components) {
			if(component instanceof Container)
				addAllComponents((Container) component);
			else
				addComponent(component);
		}
	}
	
	public static String getHierarchyOfComponent(Component component) {
		String hierarchy = component.getClass().getSimpleName().toString();
		Component parent = component;
		while((parent = parent.getParent()) != null)
			hierarchy += parent.getClass().getSimpleName();

		return hierarchy;
	}
	
	private synchronized void addComponent(Component component) {
		if(allStrings.get(component) != null)
			return;
			
		String hierarchy = getHierarchyOfComponent(component);
		
		int hierarchyNumber = 0;
		while(allComponents.get(hierarchy + hierarchyNumber) != null)
			hierarchyNumber++;
		
		hierarchy += hierarchyNumber;
		
		allComponents.put(hierarchy, component);
		allStrings.put(component, hierarchy);
	}
	
	public void hierarchyChanged(HierarchyEvent he) {
		if(he.getChangeFlags() != HierarchyEvent.PARENT_CHANGED)
			return;
		
		Component component = he.getComponent();
		if(component == null)
			return;
		
		String hierarchy = allStrings.get(component);
		
		if(hierarchy != null && !hierarchy.contains(getHierarchyOfComponent(component))) {
			
			allStrings.remove(component);
			allComponents.remove(hierarchy);
		}
		
		addComponent(component);
	}
	
	public Aspect() {
		Toolkit.getDefaultToolkit().addAWTEventListener(new AWTEventListener() {
			public void eventDispatched(AWTEvent e) {
				hierarchyChanged((HierarchyEvent) e);
			}
		}, AWTEvent.HIERARCHY_EVENT_MASK);
		
		recorder = new Recorder(allComponents, allStrings);
	}
}
