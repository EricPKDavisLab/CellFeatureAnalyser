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
package gui;

import ij.IJ;
import ij.io.DirectoryChooser;
import io.ImageIOutils;
import io.ParentFeature_ROI;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ScrollPaneConstants;
import javax.swing.table.DefaultTableModel;
import main.CellFeatureAnalyser_;
import abstractprocessors.AbstractParentFeatureProcessor;

/**
 * This is the first phase vital part of the GUI where the user selects the
 * image file location and initialises the table for showing the selected files
 * etc.
 * 
 * It also initialised the {@link CellAnalyserGUIModel} which is used to store 
 * all of the features and pass data between the different {@link AbstractParentFeatureProcessor}s.
 * 
 *
 * @author mqbssep5
 */
public final class GUIInitialPanel implements ActionListener, MouseListener {

    private JTable table;

    private DefaultTableModel tableModel;

    private JScrollPane tableScrollPane;

    private JPanelSpeedy2ColBased tablePanel;

    private GUI gc;

    private JButton jbtLoadFiles, jbtOpenNext, jbtOpenPrevious;

    private String[] filenames;

    private String fileDirectoryPath;
    
    private final String[] defaultTableHeadings = new String[]{"Image", "#Features", "Status"};
    
    private int currentImageIndex = -1;
    
    private CellAnalyserGUIModel mainGUImodel;
    
    /**
     * The supported file types. 
     */
    private static final String[] ACCEPTED_FILE_TYPES = new String[]{".tif",".tiff"}; 

    public GUIInitialPanel() {
        initAll();
    }

    private void initAll() {

        // build the GUI.
        tablePanel = new JPanelSpeedy2ColBased();
        // the chass which contains many of the formatted components for the gui
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
        tableScrollPane.setPreferredSize(new Dimension((int) Math.round(0.95 * (double) GUI.GUI_WIDTH), (int) Math.round(0.9 * (double) GUI.GUI_HEIGHT)));

        // add the items to the table. 
        tablePanel.addSingleComponentLHS( jbtLoadFiles );
        tablePanel.addComponentDoubleColumn( tableScrollPane );
        tablePanel.addTwoComponentsToRow( jbtOpenPrevious, jbtOpenNext );

    }

    /**
     * A method which prompts the user to select a folder containing the images. 
     */
    private void getImageNamesAndCheckForROIs() { 
        
        // get the file names and put into the table. 
        //fileDirectoryPath = getDir();
        
        fileDirectoryPath = "Y:\\epitkeathly\\Pippa STED pSignalling\\Ania\\Inhibition\\all_in_one\\";
        
        // do some checks on the file path selected by the user. 
        if (fileDirectoryPath == null) {
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
        for( int i = 0; i < filenames.length; i++ ){
            // Image name
            tableModel.setValueAt(filenames[i], i, 0);
        }
        
        // Now check for ROI files in the same folder. 
        File f2 = new File(fileDirectoryPath + CellFeatureAnalyser_.ROI_FOLDER_NAME);
        if (f2.exists() && !f2.isDirectory()) {
            // We couldn't find any ROIs, therefore we will fill the table appropriately
            for (int i = 0; i < filenames.length; i++) {
                // Image name
                tableModel.setValueAt("N/A", i, 1);
            }  
        }else{      
            ParentFeature_ROI pfe = new ParentFeature_ROI(fileDirectoryPath + CellFeatureAnalyser_.ROI_FOLDER_NAME);         
            // We have some ROIs, fill the table with the number we have for each 
            // image.
            for (int i = 0; i < filenames.length; i++) {
                // Image name
                tableModel.setValueAt(""+ pfe.getROIs(filenames[i]).size(), i, 1);
            }
        }
        
        // Initialise the new program main data structure. 
        mainGUImodel = new CellAnalyserGUIModel(fileDirectoryPath, filenames); 
        openImage(0);
    }
    
    /**
     * @return the model used to handle all of the data 
     */
    public CellAnalyserGUIModel getModel(){
        return mainGUImodel;
    }
    
    /**
     * Opens an image in the input list of image names. Either the next image,
     * previous image or the current image will be opened.
     *
     * @param dir = 1 for the next image, 0 for the same image again, -1 for the
     * previous image.
     */
    private void openImage( int indexNow ) {

        if(filenames == null){
            return;
        }
        if(filenames.length == 0){
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
        mainGUImodel.openImage(indexNow);
        // select the current colum in the table.
        table.setRowSelectionInterval(currentImageIndex, currentImageIndex);
    }      

    /**
     * Selects the defined row.
     * @param index 
     */
    public void highlightRow( int index ){
        // select the current colum in the table.
        table.setRowSelectionInterval(index, index);
    }
    
    /**
     * Replaces the current text in the status column for the specified row. 
     * @param index
     * @param message 
     */
    public void setStatus( int index, String message ){
        tableModel.setValueAt( message, index, 2 );
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
     * @return the JPanel with all of the components on
     */
    public JPanel getPanel() {
        return (JPanel) tablePanel;
    }

    @Override
    public void actionPerformed(ActionEvent ae) {

        if (ae.getSource() == jbtLoadFiles) {
            getImageNamesAndCheckForROIs();
        } else if (ae.getSource() == jbtOpenNext) {
            // open the next image. 
            currentImageIndex = currentImageIndex + 1;
            openImage( currentImageIndex );
        } else if (ae.getSource() == jbtOpenPrevious) {
            // open the previous image. 
            currentImageIndex = currentImageIndex - 1;
            openImage( currentImageIndex );
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
    
}
