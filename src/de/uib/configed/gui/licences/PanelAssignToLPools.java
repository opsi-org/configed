package de.uib.configed.gui.licences;

import javax.swing.*;
import java.awt.event.*;
import javax.swing.event.*;
import de.uib.configed.Globals;
import de.uib.utilities.table.gui.*;
import de.uib.configed.configed;
import de.uib.configed.*;
import de.uib.utilities.logging.*;


/**
 * Copyright (C) 2008-2015 uib.de
 * @author roeder
 */
public class PanelAssignToLPools extends MultiTablePanel
{
	public JTextField testfield; //for test purposes

	private JLabel titleWindowsSoftware;
	private JLabel labelSelectedLicencePoolId;
	public JLabel fieldSelectedLicencePoolId;
	private JLabel labelCountAllWindowsSoftware;
	public JLabel fieldCountAllWindowsSoftware;
	private JLabel labelCountAssignedWindowsSoftware;
	public JLabel fieldCountAssignedWindowsSoftware;
	private JLabel labelCountAssignedAuditedSoftware;
	public JLabel fieldCountAssignedAuditedSoftware;

	private JSplitPane splitPane;
	private JPanel topPane;
	private JPanel bottomPane;
	private int splitPaneHMargin = 1;

	private JPanel panelInfoWindowsSoftware;
	public PanelGenEditTable panelWindowsSoftware;
	public PanelGenEditTable panelLicencepools;
	public PanelGenEditTable panelProductId2LPool;

	protected int minVSize = 50;


	protected int tablesMaxWidth = 600;
	protected int tablesMaxHeight =Short.MAX_VALUE ;

	/** Creates new form panelAssignToLPools */
	public PanelAssignToLPools(ControlMultiTablePanel controller) {
		super(controller);
		initComponents();
	}

	private void initComponents()
	{

		//splitpane
		splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT );
		topPane = new JPanel();
		bottomPane = new JPanel();

		testfield = new JTextField("                     ");

		//construct content panes
		panelInfoWindowsSoftware = new JPanel();
		panelInfoWindowsSoftware.setBackground(Globals.backgroundWhite);

		titleWindowsSoftware = new JLabel(configed.getResourceValue(
		                                      "ConfigedMain.Licences.SectiontitleWindowsSoftware2LPool"));
		titleWindowsSoftware.setFont(Globals.defaultFontStandardBold);


		labelSelectedLicencePoolId = new JLabel(
		                                 configed.getResourceValue("PanelAssignToLPools.labelSelectedLicencePoolId"));
		//labelSelectedLicencePoolId.setPreferredSize(Globals.counterfieldDimension);
		labelSelectedLicencePoolId.setFont(Globals.defaultFont);

		fieldSelectedLicencePoolId = new JLabel("");
		fieldSelectedLicencePoolId.setPreferredSize(new java.awt.Dimension(250, Globals.lineHeight));
		fieldSelectedLicencePoolId.setFont(Globals.defaultFontStandardBold);

		/*
		labelCountAllWindowsSoftware = new JLabel(
			configed.getResourceValue("PanelAssignToLPools.labelCountAllWindowsSoftware"));
		//labelCountAllWindowsSoftware.setPreferredSize(Globals.counterfieldDimension);
		labelCountAllWindowsSoftware.setFont(Globals.defaultFont);

		fieldCountAllWindowsSoftware = new JLabel("0");
		fieldCountAllWindowsSoftware.setPreferredSize(Globals.shortlabelDimension);
		fieldCountAllWindowsSoftware.setFont(Globals.defaultFont);
		*/

		labelCountAssignedWindowsSoftware = new JLabel(
		                                        configed.getResourceValue("PanelAssignToLPools.labelCountAssignedWindowsSoftware"));
		//labelCountAssignedWindowsSoftware.setPreferredSize(Globals.counterfieldDimension);
		labelCountAssignedWindowsSoftware.setFont(Globals.defaultFont);

		fieldCountAssignedWindowsSoftware = new JLabel("0");
		fieldCountAssignedWindowsSoftware.setPreferredSize(Globals.shortlabelDimension);
		fieldCountAssignedWindowsSoftware.setFont(Globals.defaultFont);

		labelCountAssignedAuditedSoftware = new JLabel(
		                                        configed.getResourceValue("PanelAssignToLPools.labelCountAssignedAuditedSoftware"));
		//labelCountAssignedAuditedSoftware.setPreferredSize(Globals.counterfieldDimension);
		labelCountAssignedAuditedSoftware.setFont(Globals.defaultFont);

		fieldCountAssignedAuditedSoftware = new JLabel("0");
		fieldCountAssignedAuditedSoftware.setPreferredSize(Globals.shortlabelDimension);
		fieldCountAssignedAuditedSoftware.setFont(Globals.defaultFontStandardBold);


		GroupLayout layoutPanelInfo = new javax.swing.GroupLayout(panelInfoWindowsSoftware);
		panelInfoWindowsSoftware.setLayout(layoutPanelInfo);

		layoutPanelInfo.setHorizontalGroup(
		    layoutPanelInfo.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
		    .addGroup(layoutPanelInfo.createSequentialGroup()
		              .addGap(Globals.hGapSize/2, Globals.hGapSize/2, Globals.hGapSize/2)
		              .addGroup(layoutPanelInfo.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
		                        .addComponent(titleWindowsSoftware, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE,javax.swing.GroupLayout.PREFERRED_SIZE)
		                        .addGroup(layoutPanelInfo.createSequentialGroup()
		                                  .addComponent(labelSelectedLicencePoolId,
		                                                0, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE)
		                                  .addGap(Globals.hGapSize/2, Globals.hGapSize/2, Globals.hGapSize/2)
		                                  .addComponent(fieldSelectedLicencePoolId,
		                                                GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
		                                  //.addGap(Globals.hGapSize, Globals.hGapSize, Globals.hGapSize)
		                                  //.addComponent(labelCountAllWindowsSoftware,
		                                  //	0, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE)
		                                  //.addGap(Globals.hGapSize/2, Globals.hGapSize/2, Globals.hGapSize/2)
		                                  //.addComponent(fieldCountAllWindowsSoftware,
		                                  //	GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
		                                  .addGap(Globals.hGapSize, Globals.hGapSize, Globals.hGapSize)
		                                  .addComponent(labelCountAssignedWindowsSoftware,
		                                                0, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE)
		                                  .addGap(Globals.hGapSize/2, Globals.hGapSize/2, Globals.hGapSize/2)
		                                  .addComponent(fieldCountAssignedWindowsSoftware,
		                                                GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
		                                  .addGap(Globals.hGapSize, Globals.hGapSize, Globals.hGapSize)
		                                  .addComponent(labelCountAssignedAuditedSoftware,
		                                                0, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE)
		                                  .addGap(Globals.hGapSize/2, Globals.hGapSize/2, Globals.hGapSize/2)
		                                  .addComponent(fieldCountAssignedAuditedSoftware,
		                                                GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
		                                 )
		                       )
		             )
		);

		layoutPanelInfo.setVerticalGroup(layoutPanelInfo.createSequentialGroup()
		                                 .addContainerGap()
		                                 .addComponent(titleWindowsSoftware)
		                                 .addGroup(layoutPanelInfo.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
		                                           .addComponent(labelSelectedLicencePoolId, Globals.lineHeight,Globals.lineHeight,Globals.lineHeight)
		                                           .addComponent(fieldSelectedLicencePoolId, Globals.lineHeight,Globals.lineHeight,Globals.lineHeight)
		                                           //.addComponent(labelCountAllWindowsSoftware, Globals.lineHeight,Globals.lineHeight,Globals.lineHeight)
		                                           //.addComponent(fieldCountAllWindowsSoftware, Globals.lineHeight,Globals.lineHeight,Globals.lineHeight)
		                                           .addComponent(labelCountAssignedWindowsSoftware, Globals.lineHeight,Globals.lineHeight,Globals.lineHeight)
		                                           .addComponent(fieldCountAssignedWindowsSoftware,Globals.lineHeight,Globals.lineHeight,Globals.lineHeight)
		                                           .addComponent(labelCountAssignedAuditedSoftware, Globals.lineHeight,Globals.lineHeight,Globals.lineHeight)
		                                           .addComponent(fieldCountAssignedAuditedSoftware,Globals.lineHeight,Globals.lineHeight,Globals.lineHeight)
		                                          )
		                                );

		panelLicencepools = new PanelGenEditTable(configed.getResourceValue("ConfigedMain.Licences.SectiontitleLicencepools"),
		                    tablesMaxWidth, true, 1, false,
		                    new int[]{
		                        PanelGenEditTable.POPUP_DELETE_ROW,
		                        PanelGenEditTable.POPUP_SAVE,
		                        PanelGenEditTable.POPUP_CANCEL,
		                        PanelGenEditTable.POPUP_RELOAD
		                        //,
		                        //PanelGenEditTable.POPUP_PDF 
		                    }
		                                         );

		panelProductId2LPool = new PanelGenEditTable(configed.getResourceValue("ConfigedMain.Licences.SectiontitleProductId2LPool"),
		                       tablesMaxWidth, true, 1, false,
		                       new int[]{
		                           PanelGenEditTable.POPUP_DELETE_ROW,
		                           PanelGenEditTable.POPUP_SAVE,
		                           PanelGenEditTable.POPUP_CANCEL,
		                           PanelGenEditTable.POPUP_RELOAD
		                       }
		                                            );

		boolean switchLineColors = true;

		panelWindowsSoftware = new PanelGenEditTable("",
		                       //configed.getResourceValue("ConfigedMain.Licences.SectiontitleWindowsSoftware2LPool"),
		                       0, true, 2, switchLineColors,
		                       new int[]{
		                           PanelGenEditTable.POPUP_RELOAD
		                       },
		                       true

		                                            )
		                       {
			                       //ListSelectionListener
			                       public void valueChanged(javax.swing.event.ListSelectionEvent e)
			                       {
				                       super.valueChanged(e);
				                       ((ControlPanelAssignToLPools)controller).validateWindowsSoftwareKeys();
			                       }

			                       // MouseListener
			                       public void mouseClicked(MouseEvent e)
			                       {
				                       super.mouseClicked(e);

				                       javax.swing.table.TableColumn col=panelWindowsSoftware.getColumnModel()
				                                                         .getColumn(0);
				                       col.setMaxWidth(200);
			                       }
		                       };


		javax.swing.GroupLayout layoutTopPane = new javax.swing.GroupLayout((JPanel)  topPane);
		topPane.setLayout(layoutTopPane);
		layoutTopPane.setHorizontalGroup(
		    layoutTopPane.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
		    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layoutTopPane.createSequentialGroup()
		              .addContainerGap()
		              .addGroup(layoutTopPane.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
		                        // for testing purposes:
		                        //.addComponent(testfield, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
		                        .addComponent(panelLicencepools, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
		                        .addComponent(panelProductId2LPool, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
		                       )
		              .addContainerGap())
		);
		layoutTopPane.setVerticalGroup(
		    layoutTopPane.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
		    .addGroup(layoutTopPane.createSequentialGroup()
		              .addContainerGap()
		              //.addComponent(testfield, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE)
		              .addComponent(panelLicencepools, minVSize, javax.swing.GroupLayout.DEFAULT_SIZE, tablesMaxHeight)
		              //.addGap(Globals.vGapSize, Globals.vGapSize, Globals.vGapSize)
		              .addComponent(panelProductId2LPool, minVSize, javax.swing.GroupLayout.DEFAULT_SIZE, tablesMaxHeight)

		              .addContainerGap()
		             )
		    .addGap(Globals.vGapSize, Globals.vGapSize, Globals.vGapSize)
		);



		javax.swing.GroupLayout layoutBottomPane = new javax.swing.GroupLayout((JPanel) bottomPane);
		bottomPane.setLayout(layoutBottomPane);
		layoutBottomPane.setHorizontalGroup(
		    layoutBottomPane.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
		    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layoutBottomPane.createSequentialGroup()
		              .addContainerGap()
		              .addGroup(layoutBottomPane.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
		                        .addComponent(panelInfoWindowsSoftware, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE,  Short.MAX_VALUE)
		                        .addComponent(panelWindowsSoftware, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
		                       )
		              .addContainerGap())
		);
		layoutBottomPane.setVerticalGroup(
		    layoutBottomPane.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
		    .addGroup(layoutBottomPane.createSequentialGroup()
		              .addContainerGap()
		              .addComponent(panelInfoWindowsSoftware)
		              .addComponent(panelWindowsSoftware, minVSize, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
		              .addContainerGap()
		             )
		);



		splitPane.setTopComponent(topPane);
		splitPane.setBottomComponent(bottomPane);

		add(splitPane);



		javax.swing.GroupLayout layout = new javax.swing.GroupLayout((JPanel) this);
		this.setLayout(layout);
		layout.setHorizontalGroup(layout.createSequentialGroup()
		                          .addGap(splitPaneHMargin, splitPaneHMargin, splitPaneHMargin)
		                          .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
		                                    .addComponent(splitPane, 0, javax.swing.GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
		                                   )
		                          .addContainerGap()
		                          .addGap(splitPaneHMargin, splitPaneHMargin, splitPaneHMargin)
		                         );

		layout.setVerticalGroup(
		    layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
		    .addComponent(splitPane, 0, javax.swing.GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
		)
		;


	}



}
