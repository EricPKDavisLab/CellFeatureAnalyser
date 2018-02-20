/*
 * Copyright 2018 mqbssep5.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package main;

import gui.GUI;
import gui.JPanelSpeedy2ColBased;
import ij.plugin.PlugIn;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.UIManager;
import abstractprocessors.AbstractParentFeatureProcessor;
import drawoutline.ROIdrawAndSave2_;
import gui.CellAnalyserGUIModel;
import ij.IJ;
import ij.ImagePlus;
import ij.io.DirectoryChooser;
import ij.plugin.frame.PlugInFrame;
import io.ImageIOutils;
import io.ParentFeature_ROI;
import java.awt.BorderLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import javax.swing.JComboBox;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableModel;
import pluginmanager.PluginLoaderV1;

/**
 * This is the actual ImageJ PlugIn class for the {@link CellFeatureAnalyser_}
 * package.
 * 
 * It contains the main frame for the GUI and also the code for running the more
 * heavy duty processes in additional threads. 
 *
 * @author mqbssep5
 */
public class CellFeatureAnalyser_ extends PlugInFrame implements PlugIn, ActionListener, MouseListener, ItemListener, PropertyChangeListener {

    private PlugInFrame frame;

    private JTabbedPane tabbedPane;

    private GUI gc = new GUI();

    private JButton jbtRunOne, jbtRunAll;

    public static String ROI_FOLDER_NAME = "\\ROI\\";

    private AbstractParentFeatureProcessor[] processors;

    private CellAnalyserGUIModel model;

    private JTable table;

    private DefaultTableModel tableModel;

    private JScrollPane tableScrollPane;

    private JPanelSpeedy2ColBased tablePanel;

    private JButton jbtLoadFiles, jbtOpenNext, jbtOpenPrevious, jbChangePackage;

    private String[] filenames;

    private String fileDirectoryPath;

    private final String[] defaultTableHeadings = new String[]{"Image", "#Features", "Status"};

    private int currentImageIndex = -1;

    private int progressBarMax = 0;

    private boolean isFirstLoad = true;

    private JProgressBar jProgressBar;

    private JTextField jtfStatus;

    private JComboBox jComboAnalysisPackage;

    private String pluginOption;

    private int nchannels;

    private PluginLoaderV1 pluginLoader;

    private SwingWorker workerThread;

    /**
     * The supported file types.
     */
    private static final String[] ACCEPTED_FILE_TYPES = new String[]{".tif", ".tiff"};

//    /**
//     * @param args the command line arguments
//     */
//    public static void main(String[] args) {
//        CellFeatureAnalyser_ cc = new CellFeatureAnalyser_("CellFeatureAnalyser");
//        cc.setVisible(true);
//        //cc.doGUI();
//    }

    public CellFeatureAnalyser_(String title) {
        super(title);
    }

    public CellFeatureAnalyser_() {
        super("CellFeatureAnalyser");
    }

    @Override
    public void run(String string) {

        // Load in the plugins
        pluginLoader = new PluginLoaderV1();
        pluginOption = pluginLoader.getAnalysisSreamNames()[0];
        // Create the GUI for the 
        doGUI();
    }

    /**
     * Create the GUI.
     */
    private void doGUI() {

        frame = this;
        frame.setResizable(false);

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Throwable t) {
        }

        // The gui panel for handelling the input data. 
        initTable();

        JPanel pan1 = tablePanel;
        pan1.setSize(GUI.GUI_MAIN_PANEL_HOLDER_DIM);

        // Tabbed pane where the plugins will be stored.
        tabbedPane = new JTabbedPane(JTabbedPane.TOP);
        tabbedPane.setPreferredSize(new Dimension((int) Math.round(1.2 * (double) GUI.FULL_WIDTH_DIM.width), (int) Math.round(0.89 * (double) GUI.GUI_HEIGHT)));
        //tabbedPane.setPreferredSize( gc.scaleDims(GUIcomponents.GUI_MAIN_PANEL_DIM,1.05d,1.05d) );
        JPanel startUpPanel = new JPanel(new BorderLayout());
        startUpPanel.add(gc.jLabelBold("Select an image file location.", GUI.FULL_WIDTH_DIM), BorderLayout.LINE_START);
        startUpPanel.setPreferredSize(GUI.GUI_MAIN_PANEL_DIM);
        tabbedPane.addTab("Start up", startUpPanel);

        // add a blank tab to the 
        jbtRunOne = gc.jButton("Run single", this, GUI.HALF_WIDTH_DIM);
        jbtRunAll = gc.jButton("Run all", this, GUI.HALF_WIDTH_DIM);
        // set these as non active until the user has selected a analysis plugin. 
        jbtRunOne.setEnabled(true);
        jbtRunAll.setEnabled(true);

        // Panel used to prepare the analysis stream
        jComboAnalysisPackage = gc.jComboBox(pluginLoader.getAnalysisSreamNames(), GUI.HALF_WIDTH_DIM);
        jComboAnalysisPackage.addItemListener(this);
        jbChangePackage = gc.jButton("Set pipeline", this, GUI.HALF_WIDTH_DIM);

        jbChangePackage.setEnabled(false);
        jComboAnalysisPackage.setEnabled(false);

        // The panel used to hold the JTabbedPane. 
        JPanelSpeedy2ColBased pan2 = new JPanelSpeedy2ColBased();
        pan2.setPreferredSize(GUI.GUI_MAIN_PANEL_HOLDER_DIM);
        pan2.addTwoComponentsToRow(jbChangePackage, jComboAnalysisPackage);
        pan2.addComponentDoubleColumn(tabbedPane);
        pan2.addTwoComponentsToRow(jbtRunAll, jbtRunOne);

        // construct a JPanel to put everything on. 
        JPanel totalPanel = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.insets = new Insets(5, 5, 5, 5);
        // add the two panels side by side to the total JPanel.
        c.gridx = 0;
        c.gridy = 0;
        totalPanel.add(pan1, c);
        c.gridx = 1;
        c.gridy = 0;
        totalPanel.add(pan2, c);
        // Add the total Panel to our PlugInFrame
        frame.add(totalPanel);
        frame.pack();
        frame.setVisible(true);

    }

    /**
     * Initialises the table for the main GUI.
     */
    private void initTable() {

        // build the GUI.
        tablePanel = new JPanelSpeedy2ColBased();
        // the class which contains many of the formatted components for the gui
        gc = new GUI();
        // buttons
        Dimension siz = GUI.HALF_WIDTH_DIM;
        jbtLoadFiles = gc.jButton("Select data", this, siz);
        jbtOpenNext = gc.jButton("Open next", this, siz);
        jbtOpenPrevious = gc.jButton("Open previous", this, siz);

        // table model for getting things into and out of the table. 
        tableModel = new DefaultTableModel(defaultTableHeadings, 10);

        table = new JTable(tableModel);
        table.setDefaultEditor(Object.class, null);// stops manual editing of the items in the table. 
        table.addMouseListener(this);

        // add the values to the column
        tableScrollPane = new JScrollPane(table);
        tableScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        //tableScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        tableScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
        tableScrollPane.setPreferredSize(new Dimension((int) Math.round(0.95 * (double) GUI.GUI_WIDTH), (int) Math.round(0.79 * (double) GUI.GUI_HEIGHT)));

        jProgressBar = new JProgressBar(0, 1);
        jtfStatus = gc.jTextFeild("Status: Idle", GUI.FULL_WIDTH_DIM);
        jtfStatus.setEditable(false);
        // add the items to the table. 
        tablePanel.addSingleComponentLHS(jbtLoadFiles);
        tablePanel.addComponentDoubleColumn(tableScrollPane);
        tablePanel.addComponentDoubleColumn(jProgressBar);
        tablePanel.addComponentDoubleColumn(jtfStatus);
        tablePanel.addTwoComponentsToRow(jbtOpenPrevious, jbtOpenNext);

    }

    /**
     * A method which prompts the user to select a folder containing the images.
     */
    private void getImageNamesAndCheckForROIs() {

        // get the file names and put into the table. 
        fileDirectoryPath = getDir();
//        fileDirectoryPath = "Y:\\epitkeathly\\Pippa STED pSignalling\\Ania\\Inhibition\\all_in_one\\";
//        fileDirectoryPath = "Y:\\epitkeathly\\analysis\\STEDcoloc\\smallColocTestFiles\\";

        // do some checks on the file path selected by the user. 
        if ( fileDirectoryPath == null ) {
            IJ.error("No file path selected");
            return;
        }

        File f = new File(fileDirectoryPath);
        if (f.exists() && !f.isDirectory()) {
            IJ.error("File path entered does not exist.");
            return;
        }
        // All should be ok so lets get some files. 
        filenames = ImageIOutils.getFilesNames(fileDirectoryPath, ACCEPTED_FILE_TYPES);

        if (filenames.length == 0) {
            IJ.error("No image files found in the specified location. ");
            return;
        }

        // Reset the table model so that we have enough rows for all the image names. 
        tableModel = new DefaultTableModel(defaultTableHeadings, filenames.length);
        table.setModel(tableModel);
        // Put the file names in for this image 
        for (int i = 0; i < filenames.length; i++) {
            // Image name
            tableModel.setValueAt(filenames[i], i, 0);
        }
        // Now check for ROI files in the same folder. 
        File f2 = new File(fileDirectoryPath + CellFeatureAnalyser_.ROI_FOLDER_NAME);
        File f3 = new File(fileDirectoryPath + ROIdrawAndSave2_.ROI_FOLDER_NAME);
        if (f2.exists() && !f2.isDirectory()) {
            // We couldn't find any ROIs, therefore we will fill the table appropriately
            for (int i = 0; i < filenames.length; i++) {
                // Image name
                tableModel.setValueAt("N/A", i, 1);
            }
        } else if (f3.exists()) {
            ParentFeature_ROI pfe = new ParentFeature_ROI(fileDirectoryPath + ROIdrawAndSave2_.ROI_FOLDER_NAME);
            // We have some ROIs, fill the table with the number we have for each 
            // image.
            for (int i = 0; i < filenames.length; i++) {
                // Image name
                tableModel.setValueAt("" + pfe.getROIs2(filenames[i]).size(), i, 1);
            }
        } else {
            ParentFeature_ROI pfe = new ParentFeature_ROI(fileDirectoryPath + CellFeatureAnalyser_.ROI_FOLDER_NAME);
            // We have some ROIs, fill the table with the number we have for each 
            // image.
            for (int i = 0; i < filenames.length; i++) {
                // Image name
                tableModel.setValueAt("" + pfe.getROIs(filenames[i]).size(), i, 1);
            }
        }

        // close any existing data sets we have from the previous load.
        if (!isFirstLoad) {
            closeAnyExistingDataWindows();
        }

        // Initialise the new program main data structure. 
        model = new CellAnalyserGUIModel(fileDirectoryPath, filenames);

        openImage(0);
//        // set up the processors. 
        if (isFirstLoad) {
            // we will want to check this again if the user tried to refresh the file locations. 
            nchannels = model.getNchannels();
            String selected;
            selected = (String) jComboAnalysisPackage.getSelectedItem();
            processors = pluginLoader.getProcessingStream(model, selected);
            // Initialise the processors 
            setUpAnalysisStream();
            isFirstLoad = false;
            jbChangePackage.setEnabled(true);
        } else if (isNewDataFileCompatableWithCurrentSetup()) {
            // we do not need to update the GUI panel. 
            resetProcessorModels();
        } else {
            // re-set the analysis processor streams and GUI. 
            setProcessorStream();
        }
    }

    /**
     * Closes any windows associated with the GUI.
     */
    private void closeAnyExistingDataWindows() {
        ImagePlus impT = model.getCurrentImageDataSet();
        if (impT != null) {
            impT.changes = false;
            impT.close();
        }
        model = null;
    }

    /**
     * @return if the number of channels in the new data model is the same as
     * the previous run. In this case the
     * {@link AbstractParentFeatureProcessor}s do not need to be refreshed.
     */
    private boolean isNewDataFileCompatableWithCurrentSetup() {
        if (model == null) {
            return false;
        }
        if (model.getNchannels() != nchannels) {
            return false;
        }
        return true;
    }

    /**
     * Opens an image in the input list of image names. Either the next image,
     * previous image or the current image will be opened.
     *
     * @param dir = 1 for the next image, 0 for the same image again, -1 for the
     * previous image.
     */
    private void openImage( int indexNow ) {

        if (filenames == null) {
            return;
        }
        if (filenames.length == 0) {
            return;
        }

        if (indexNow < 0) {
            currentImageIndex = 0;
            IJ.showMessage("This is the first image in the folder list.");
        } else if (indexNow > (filenames.length - 1)) {
            currentImageIndex = filenames.length - 1;
            IJ.showMessage("This is the last image in the sequence.");
        } else {
            currentImageIndex = indexNow;
        }
        // Use the function within the gui model to open the image sets. 
        model.openImage(indexNow);
        // select the current colum in the table.
        table.setRowSelectionInterval(currentImageIndex, currentImageIndex);
    }

    /**
     * Selects the defined row.
     *
     * @param index
     */
    public void highlightTableRow( int index ) {
        // select the current colum in the table.
        table.setRowSelectionInterval(index, index);
    }

    /**
     * Replaces the current text in the status column for the specified row.
     *
     * @param index
     * @param message
     */
    public void setStatusCellInTable( int index, String message ) {
        tableModel.setValueAt(message, index, 2);
    }

    /**
     * Uses the ImageJ Directory chooser to allow the user to choose a directory
     * with the images files in.
     *
     * @return
     */
    private String getDir() {
        String dir;
        DirectoryChooser dr1 = new DirectoryChooser("Select a image file directory.");
        try {
            dir = dr1.getDirectory();
        } catch (Exception e) {
            dir = null;
        }
        return dir;
    }

    /**
     * Resets the max value in the progress bar.
     *
     * @param newMax
     * @param status
     */
    private void resetProgressBarAndGUIstatus(int newMax, String status) {
        progressBarMax = newMax;
        jProgressBar.setMinimum(0);
        jProgressBar.setMaximum(progressBarMax);
        jProgressBar.setValue(0);
        jtfStatus.setText("Status: " + status);
    }

    /**
     * Set the value of the progress bar as well as the message put in the
     * status bar below it.
     *
     * @param value
     * @param status
     */
    private void setProgressBarAndGUIstatus(int value, String status) {
        value = Math.min(progressBarMax, value);
        jProgressBar.setValue(value);
        jtfStatus.setText("Status: " + status);
    }

    /**
     * Initialises all of the processes that will be applied to the datasets and
     * the
     */
    private void setUpAnalysisStream() {
        tabbedPane.removeAll();
        // build the JTabbedPane with our processors. 
        for (int i = 0; i < processors.length; i++) {
            tabbedPane.addTab(processors[i].getName(), processors[i].getSettingsPanel());
        }
    }

    /**
     * Updates the data model to the exsting processors.
     */
    private void resetProcessorModels() {
        for (AbstractParentFeatureProcessor p : processors) {
            p.resetModel(model);
        }
    }

    /**
     * Runs the complete chain of processing tasks on the image set currently
     * selected.
     */
    private void runSingleImage() {
        // get the data model. 
        if (model == null) {
            return;
        }
        int nProcesses = processors.length;
        resetProgressBarAndGUIstatus(nProcesses, "Running all on single data set.");
        // initilaise the worker thread to do the processing outside of the GUIs event dispactch thread.
        workerThread = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                // check if the image data has been modified by faffing around. 
                if (model.getCurrentImageDataSet().changes == true) {
                    // if so then re-load the data. 
                    model.refreshCurrentImage();
                }
                // Then lets process the current through all processes.    
                for (int i = 0; i < nProcesses; i++) {
                    setStatusCellInTable(model.getCurrentImageIndex(), "Processing " + (i + 1) + "/" + (nProcesses));
                    setProgressBarAndGUIstatus((i + 1), "Processing set " + (model.getCurrentImageIndex() + 1) + " with process " + (i + 1));
                    processors[i].doProcess();
                    // store this as we may need to access this for sumarising the data later. 
                    model.addProcessorNameToList(processors[i].getName());
                }
                setStatusCellInTable(model.getCurrentImageIndex(), "Complete");
                resetProgressBarAndGUIstatus(1, "Idle");
                return null;
            }
        };
        workerThread.addPropertyChangeListener(this);
        workerThread.execute();
    }

    /**
     * Runs the complete chain of processing tasks on all of the images.
     */
    private void runAllImage() {

        // get the data model. 
        if (model == null) {
            return;
        }

        int nProcesses = processors.length;
        int nDataSets;
        nDataSets = model.getNImages();

        resetProgressBarAndGUIstatus(nProcesses * nDataSets, "Running all processes");
        // initialise a new worker thread for processing outside of the event dispatch thread.
        workerThread = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                boolean dirmade;

                int counter = 1;
                String s;
                // Run all the processes on all of the images. 
                for (int i = 0; i < nDataSets; i++) {
                    model.openImage(i);
                    // set the number of features in the table if it has been modifed at all.
                    tableModel.setValueAt("" + model.getCurrentImageSetParentFeatures().size(), i, 1);
                    highlightTableRow(i);
                    //setStatusCellInTable( i, "Processing" );
                    // Then lets process the current through all processes.    
                    for (int p = 0; p < nProcesses; p++) {
                        // update the status bars.
                        setStatusCellInTable(i, "Processing " + (p + 1) + "/" + (nProcesses));
                        setProgressBarAndGUIstatus(counter, "Processing data set " + (i + 1) + "/" + nDataSets + " with process " + (p + 1) + "/" + nProcesses);
                        processors[p].doProcess();
                        if (i == 0) {
                            // store this as we may need to access this for sumarising the data later from across different processors. 
                            model.addProcessorNameToList(processors[p].getName());
                        }
                        counter++;
                    }
                    setStatusCellInTable(i, "Complete");
                }
                System.gc();
                // now sumarise the data. 
                dirmade = new File(model.getSaveFileDirectory()).mkdirs();
                resetProgressBarAndGUIstatus(nProcesses, "Summarizing all data");
                // Then lets process the current through all processes.    
                for (int p = 0; p < nProcesses; p++) {
                    IJ.showProgress(p + 1, nProcesses);
                    IJ.showStatus("Summarising " + processors[p].getName());
                    setProgressBarAndGUIstatus(p + 1, "Summarizing process " + (p + 1));
                    processors[p].summariseAndSave();
                }
                IJ.showProgress(1, 1);
                resetProgressBarAndGUIstatus(1, "Complete");
                return null;
            }
        };
        workerThread.addPropertyChangeListener(this);
        workerThread.execute();
    }

    /**
     * If the user wants to change the processor analysis stream then let them
     * do it.
     */
    private void setProcessorStream() {
        // the processors have been selected. 
        if (!jbChangePackage.isSelected()) {
            // wait for the user to select the analysis package. 
            jbChangePackage.setSelected(true);
            setRunEnabled(false);
        } else {
            // Then see if what the user has selected. 
            jbChangePackage.setSelected(false);
            setRunEnabled(true);
            String selected;
            selected = (String) jComboAnalysisPackage.getSelectedItem();
            if (selected.equalsIgnoreCase(pluginOption)) {//!isFirstLoad &&
                // dont refresh the plugin flow if we havent changed it since the 
                // last run. 
//                return;
            }
            //
            processors = pluginLoader.getProcessingStream(model, selected);
            pluginOption = selected;
            // update the GUI.
            setUpAnalysisStream();
        }
    }

    /**
     * Disables the run buttons whilst the processor stream is changed.
     *
     * @param enabeled
     */
    private void setRunEnabled( boolean enabeled ) {
        jbtRunAll.setEnabled(enabeled);
        jbtRunOne.setEnabled(enabeled);
        jComboAnalysisPackage.setEnabled(!enabeled);
        jbtOpenNext.setEnabled(enabeled);
        jbtOpenPrevious.setEnabled(enabeled);
    }

    @Override
    public void actionPerformed(ActionEvent ae) {

        if (ae.getSource() == jbChangePackage) {
            setProcessorStream();
        } else if (ae.getSource() == jbtRunOne) {
            //IJ.log("Run single button pressed " + System.currentTimeMillis());
            runSingleImage();
        } else if (ae.getSource() == jbtRunAll) {
            //IJ.log("Run all button pressed " + System.currentTimeMillis());
            runAllImage();
        } else if (ae.getSource() == jbtLoadFiles) {
            getImageNamesAndCheckForROIs();
        } else if (ae.getSource() == jbtOpenNext) {
            // open the next image. 
            currentImageIndex = currentImageIndex + 1;
            openImage(currentImageIndex);
        } else if (ae.getSource() == jbtOpenPrevious) {
            // open the previous image. 
            currentImageIndex = currentImageIndex - 1;
            openImage(currentImageIndex);
        }
    }

    @Override
    public void mouseClicked(MouseEvent me) {
        int nClicks;
        // mouse events for the table containing the 
        if (me.getSource() == table) {
            if (me.getButton() == 1) {
                // a double click lets the user open an image in the table. 
                nClicks = me.getClickCount();
                if (nClicks == 2) {
                    int index = table.getSelectedRow();
                    openImage(index);
                }
            }
        }
    }

    @Override
    public void mousePressed(MouseEvent me) {

    }

    @Override
    public void mouseReleased(MouseEvent me) {

    }

    @Override
    public void mouseEntered(MouseEvent me) {

    }

    @Override
    public void mouseExited(MouseEvent me) {

    }

    @Override
    public void itemStateChanged(ItemEvent ie) {
        if (ie.getSource() == jComboAnalysisPackage) {

        }
    }

    @Override
    public void propertyChange(PropertyChangeEvent pce) {
        if (pce.getNewValue() == SwingWorker.StateValue.STARTED) {
            setRunEnabled(false);
        } else if (pce.getNewValue() == SwingWorker.StateValue.DONE) {
            setRunEnabled(true);
        }
    }

}
