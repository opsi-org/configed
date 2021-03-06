/*
 * PanelNewLicence.java
 *
 */

package de.uib.configed.gui.licences;

import javax.swing.*;
import de.uib.configed.*;
import de.uib.utilities.table.gui.*;

/**
 *
 * @author roeder
 */
public class PanelNewLicence extends MultiTablePanel
{
	public JTextField testfield;
	public PanelGenEditTable panelKeys;
	public PanelGenEditTable panelSoftwarelicences;
	public PanelGenEditTable panelLicencecontracts;

	private JSplitPane splitPane;
	private JPanel topPane;
	private JPanel bottomPane;
	private int splitPaneHMargin = 1;

	protected int minVSize = 100;


	/** Creates new form PanelNewLicence */
	public PanelNewLicence(ControlMultiTablePanel controller) {
		super(controller);
		initComponents();
	}

	private void initComponents() {

		//testfield = new JTextField("                     ");

		panelKeys 
				= new PanelGenEditTable(configed.getResourceValue("ConfigedMain.Licences.SectiontitleLicenceOptionsView"),
		                                  0, true, 1, false,
		                                  new int[]{
		                                      PanelGenEditTable.POPUP_DELETE_ROW,
		                                      PanelGenEditTable.POPUP_SAVE,
		                                      PanelGenEditTable.POPUP_CANCEL,
		                                      PanelGenEditTable.POPUP_RELOAD
		                                  }
		                 );
		panelKeys.setListSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

		panelSoftwarelicences 
				= new PanelGenEditTable(configed.getResourceValue("ConfigedMain.Licences.SectiontitleSoftwarelicence"),
		                        0, true, 2, false,
		                        new int[]{
		                            PanelGenEditTable.POPUP_DELETE_ROW,
		                            PanelGenEditTable.POPUP_SAVE,
		                            PanelGenEditTable.POPUP_CANCEL,
		                            PanelGenEditTable.POPUP_RELOAD
		                        }
		                );
		panelSoftwarelicences.setListSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

		panelLicencecontracts 
				= new PanelGenEditTable(configed.getResourceValue("ConfigedMain.Licences.SectiontitleSelectLicencecontract"),
		                        0, true, 2, false,
		                        new int[]{
		                            PanelGenEditTable.POPUP_DELETE_ROW,
		                            PanelGenEditTable.POPUP_SAVE,
		                            PanelGenEditTable.POPUP_CANCEL,
		                            PanelGenEditTable.POPUP_RELOAD
		                        }
		                   );
		panelLicencecontracts.setListSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

		splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT );
		splitPane.setResizeWeight(0.5f);
		//splitPane.setDividerLocation(1f); //maximum for top when starting

		topPane = new JPanel();
		bottomPane = new JPanel();
		splitPane.setTopComponent(topPane);
		splitPane.setBottomComponent(bottomPane);

		javax.swing.GroupLayout layoutTopPane = new javax.swing.GroupLayout((JPanel) topPane);
		topPane.setLayout(layoutTopPane);
		layoutTopPane.setHorizontalGroup(
		    layoutTopPane.createSequentialGroup()
		    .addGap(10, 10, 10)
		    .addGroup(layoutTopPane.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
		              // for testing purposes:
		              //.addComponent(testfield, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
		              .addComponent(panelKeys, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
		              .addComponent(panelSoftwarelicences, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
		             )
		    .addGap(10, 10, 10)
		)
		;
		layoutTopPane.setVerticalGroup(
		    layoutTopPane.createSequentialGroup()
		    .addGap(Globals.vGapSize, Globals.vGapSize, Globals.vGapSize)
		    //.addComponent(testfield, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE)
		    .addComponent(panelKeys, minVSize, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
		    .addComponent(panelSoftwarelicences, minVSize, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
		    .addGap(Globals.vGapSize, Globals.vGapSize, Globals.vGapSize)
		)
		;

		javax.swing.GroupLayout layoutBottomPane = new javax.swing.GroupLayout((JPanel)  bottomPane);
		bottomPane.setLayout(layoutBottomPane);
		layoutBottomPane.setHorizontalGroup(
		    layoutBottomPane.createSequentialGroup()
		    .addGap(10, 10, 10)
		    .addGroup(layoutBottomPane.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
		              .addComponent(panelLicencecontracts, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
		             )
		    .addGap(10, 10, 10)
		)
		;
		layoutBottomPane.setVerticalGroup(
		    layoutBottomPane.createSequentialGroup()
		    .addGap(Globals.vGapSize, Globals.vGapSize, Globals.vGapSize)
		    .addComponent(panelLicencecontracts, minVSize, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
		    .addGap(Globals.vGapSize, Globals.vGapSize, Globals.vGapSize)
		)
		;


		javax.swing.GroupLayout layout = new javax.swing.GroupLayout((JPanel) this);
		this.setLayout(layout);
		layout.setHorizontalGroup(layout.createSequentialGroup()
		                          .addGap(splitPaneHMargin, splitPaneHMargin, splitPaneHMargin)
		                          .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
		                                    .addComponent(splitPane, 0, javax.swing.GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
		                                   )
		                          .addGap(splitPaneHMargin, splitPaneHMargin, splitPaneHMargin)
		                         );

		layout.setVerticalGroup(
		    layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
		    .addComponent(splitPane, 0, javax.swing.GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
		)
		;



		/*
		      javax.swing.GroupLayout layout = new javax.swing.GroupLayout((JPanel) this);
		      this.setLayout(layout);
		      layout.setHorizontalGroup(
		          layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
		          .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
		              .addContainerGap()
		              .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
					// for testing purposes:
					//.addComponent(testfield, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
		                  .addComponent(panelKeys, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
		                  .addComponent(panelSoftwarelicences, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
					.addComponent(panelLicencecontracts, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
					)
		              .addContainerGap())
		      );
		      layout.setVerticalGroup(
		          layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
		          .addGroup(layout.createSequentialGroup()
		              .addContainerGap()
				//.addComponent(testfield, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE)
		              .addComponent(panelKeys, minVSize, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
		              .addComponent(panelSoftwarelicences, minVSize, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
		              .addComponent(panelLicencecontracts, minVSize, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
				.addGap(Globals.vGapSize, Globals.vGapSize, Globals.vGapSize)
				)
			
		      );
		      */
	}



}
