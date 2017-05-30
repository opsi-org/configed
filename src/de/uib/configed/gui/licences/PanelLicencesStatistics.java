/*
 * PanelLicencesStatistics.java
 *
 */

package de.uib.configed.gui.licences;

import javax.swing.*;
import de.uib.utilities.table.gui.*;
import de.uib.configed.*;

/**
 * Copyright (C) 2008-2009 uib.de
 * @author roeder
 */
public class PanelLicencesStatistics extends MultiTablePanel
{
	//public JTextField testfield;
	public PanelGenEditTable panelStatistics;
	
	protected int minVSize = 50;


    /** Creates new form panelLicencesStatistics */
    public PanelLicencesStatistics(ControlMultiTablePanel controller) {
		super(controller);
        initComponents();
    }

    private void initComponents() {

		//testfield = new JTextField("      Übersicht               ");
        
		//System.out.println( "---------  init PanelLicencesStatistics");
		
        panelStatistics = new PanelGenEditTable(configed.getResourceValue("ConfigedMain.Licences.SectiontitleStatistics"), 
			1000, false, 0, true);
		panelStatistics.setListSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout((JPanel) this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
					// for testing purposes:
					//.addComponent(testfield, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
					.addComponent(panelStatistics, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
					)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
				//.addComponent(testfield, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE)
                .addComponent(panelStatistics, minVSize, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                //.addGap(Globals.vGapSize, Globals.vGapSize, Globals.vGapSize)
				.addGap(Globals.vGapSize, Globals.vGapSize, Globals.vGapSize)
				)
			
        );
    }



}
