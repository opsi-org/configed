/*
 * PanelLicencesReconciliation.java
 *
 */

package de.uib.configed.gui.licences;

import javax.swing.*;
import java.awt.event.*;
import de.uib.utilities.table.gui.*;
import de.uib.configed.*;

/**      
 * Copyright (C) 2008-2009 uib.de
 * @author roeder
 */
public class PanelLicencesReconciliation extends MultiTablePanel
{
	//public JTextField testfield;
	public PanelGenEditTable panelReconciliation;
	
	protected int minVSize = 50;
	protected int tablesMaxWidth = 1000;
	protected int buttonHeight = 15;
	protected int buttonWidth = 140;
	
	protected de.uib.configed.ControlPanelLicencesReconciliation licencesReconciliationController;

    /** Creates new form panelLicencesReconciliation */
    public PanelLicencesReconciliation(ControlPanelLicencesReconciliation licencesReconciliationController) {
		super(licencesReconciliationController);
		this.licencesReconciliationController = licencesReconciliationController;
        initComponents();
    }

	
    private void initComponents() {

        panelReconciliation = new PanelGenEditTable(configed.getResourceValue("ConfigedMain.Licences.SectiontitleReconciliation"), 
			tablesMaxWidth, false, 0, true);
		panelReconciliation.setListSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		
        javax.swing.GroupLayout layout = new javax.swing.GroupLayout((JPanel) this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
					// for testing purposes:
					//.addComponent(testfield, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
					.addComponent(panelReconciliation, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    )
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
				//.addComponent(testfield, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE)
                .addComponent(panelReconciliation, minVSize,  javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
				.addGap(Globals.vGapSize, Globals.vGapSize, Globals.vGapSize)
				)
			
        );
    }

	


}
