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
package drawoutline;

import ij.IJ;
import ij.ImageListener;
import ij.ImagePlus;
import ij.gui.ImageCanvas;
import ij.gui.Overlay;
import ij.gui.Roi;
import ij.gui.Toolbar;
import ij.gui.YesNoCancelDialog;
import ij.io.DirectoryChooser;
import ij.io.RoiDecoder;
import ij.io.RoiEncoder;
import ij.plugin.PlugIn;
import ij.process.ImageProcessor;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ScrollPaneConstants;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableModel;
import org.apache.commons.io.FileUtils;

/**
 *
 * A PlugIn which lets the user select a folder containing a series .tif files.
 * These can be opened one at a time using an 'open next' and 'open previous'
 * buttons.
 *
 * The user can draw an ROI on each of the images and save the roi in a seperate
 * folder by pressing save.
 *
 * The roi files are given the same name as the original image but with the .roi
 * extension.
 *
 * @author mqbssep5
 */
public class ROIdrawAndSave2_ implements PlugIn, ActionListener, MouseListener, AdjustmentListener, ImageListener, ItemListener, WindowListener {

    private ImagePlus imp;

    private ImageCanvas ic;

    private String filepath, currentImageName;

    private String[] filenames;

    private JFrame frame;

    public Dimension labPref = new Dimension(200, 20);

    private JButton jbtOpenNext, jbtOpenPrevious, jbtSaveRoi, jbtOpenFileLoc, jbtSaveAll, jbtDelete;

    private JTable table;

    private JScrollPane tableScrollPane;

    private JCheckBox jcbUseThreshold;

    private JComboBox jcomboPreferredChannel;

    private int currentImageIndex = 0;

    private int nchannels = 1, prefViewingChannel = 1;

    public static String ROI_FOLDER_NAME = "ROI2";

    private HashMap< String, ArrayList<Roi>> roiMap;

    private JScrollBar jSliderThreshold1;

    private Overlay[] imageOverlays;

    private ImageProcessor ip;

    private boolean silentchange = false;

    private boolean useThreshold = true;

    private boolean removemode = false;

    private boolean filessaved = false;

    private int lastT = 1, lastZ = 1, lastC = 1;

    @Override
    public void run(String string) {

        doGUI();

    }

    /**
     * Initialises the GUI.
     */
    private void doGUI() {

        frame = new JFrame("ROI draw and save 2");
        frame.setModalExclusionType(Dialog.ModalExclusionType.NO_EXCLUDE);
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        //frame.setSize(new Dimension(300, 300));
        frame.setVisible(true);
        frame.setResizable(false);

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Throwable t) {
        }

        JPanel panel;
        panel = new JPanel();
        panel.setLayout(new GridBagLayout());

        // constraints for the panel 
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.insets = new Insets(5, 10, 5, 10);

        // Three buttons. Open next, get previous, save ROI.
        jbtOpenPrevious = jButtonFormatted("Open previous");
        jbtOpenNext = jButtonFormatted("Open next");
        jbtSaveRoi = jButtonFormatted("Keep ROI");
        jbtOpenFileLoc = jButtonFormatted("Select file location.");
        jbtSaveAll = jButtonFormatted("Save all");
        jbtDelete = jButtonFormatted("Delete");
        //jbtDelete.setEnabled(false);

        // Create the table
        //table = new JTable(filenames.length, 2);
        DefaultTableModel tm = new DefaultTableModel(new String[]{"Image", "ROI"}, 1);

        table = new JTable(tm);
        table.setDefaultEditor(Object.class, null);
        table.addMouseListener(this);

//        // add the values to the column
//        initialiseTableValues();
        tableScrollPane = new JScrollPane(table);
        tableScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        //tableScrollPane = new JScrollPane(table);
        jSliderThreshold1 = new JScrollBar(JScrollBar.HORIZONTAL, 100, 100, 0, 65535);
        jSliderThreshold1.setValue(100);
        jSliderThreshold1.addAdjustmentListener(this);

        jcbUseThreshold = new JCheckBox("Set threshold");
        jcbUseThreshold.setSelected(useThreshold);
        jcbUseThreshold.addItemListener(this);

        // 
        jcomboPreferredChannel = new JComboBox(new String[]{"1"});
        jcomboPreferredChannel.addItemListener(this);

        int row = 0;
        addComponentSimple(c, panel, jbtOpenFileLoc, row, 0);
        addComponentSimple(c, panel, jbtSaveAll, row, 1);
        row++;
        addComponentDoubleColumn(c, panel, tableScrollPane, row, 0);
        row++;
        addComponentSimple(c, panel, jLabelformated("Preferred view channel"), row, 0);
        addComponentSimple(c, panel, jcomboPreferredChannel, row, 1);
        row++;
        addComponentSimple(c, panel, jcbUseThreshold, row, 0);
        addComponentSimple(c, panel, jSliderThreshold1, row, 1);
        row++;
        addComponentSimple(c, panel, jbtDelete, row, 0);
        addComponentSimple(c, panel, jbtSaveRoi, row, 1);
        row++;
        addComponentSimple(c, panel, jbtOpenPrevious, row, 0);
        addComponentSimple(c, panel, jbtOpenNext, row, 1);

        // add the panel and show the gui.
        frame.add(panel);
        frame.pack();
        frame.setVisible(true);
        frame.addWindowListener(this);

        // select the current colum in the table.
        table.setRowSelectionInterval(0, 0);

    }

    /**
     * Adds a components to the specified JPanel with defined grid constraints.
     *
     * @param c
     * @param panel
     * @param comp
     * @param gridy
     * @param gridx
     */
    private void addComponentSimple(GridBagConstraints c, JPanel panel, Component comp, int gridy, int gridx) {
        c.gridx = gridx;
        c.gridy = gridy;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridwidth = 1;
        c.weightx = 0.5;
        panel.add(comp, c);
    }

    /**
     * Adds a component across two columns.
     *
     * @param c
     * @param panel
     * @param comp
     * @param gridy
     * @param gridx
     */
    private void addComponentDoubleColumn(GridBagConstraints c, JPanel panel, Component comp, int gridy, int gridx) {
        c.gridx = gridx;
        c.gridy = gridy;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridwidth = 2;
        panel.add(comp, c);
    }

    /**
     * Creates a JButton with the preset preferred size etc. The action listener
     * for this class is also added to the button.
     *
     * @param s
     * @return
     */
    private JButton jButtonFormatted(String s) {
        JButton button = new JButton(s);
        button.addActionListener(this);
        button.setPreferredSize(labPref);
        return button;
    }

    /**
     * Creates a JLabel with the preset preferred size.
     *
     * @param s the label.
     * @return
     */
    private JLabel jLabelformated(String s) {
        JLabel jl = new JLabel(s);
        jl.setPreferredSize(labPref);
        return jl;
    }

    /**
     * Calls for the user to select a file location containing images as well as
     * setting up the table with file names etc.
     */
    private void setup() {

        boolean ok = getImagesAndSetUpGlobals();
        if (ok) {
            initialiseTableValues();
            openImage(0);
            nchannels = imp.getNChannels();
            // make sure we have the correct number of items in the combobox
            jcomboPreferredChannel.removeAllItems();
            for (int i = 0; i < nchannels; i++) {
                jcomboPreferredChannel.addItem("" + (i + 1));
            }
        }
    }

    /**
     * Initialised the image names the table.
     */
    private void initialiseTableValues() {
        //DefaultTableModel model = (DefaultTableModel) table.getModel();

        DefaultTableModel model = new DefaultTableModel(new String[]{"Image", "ROI"}, filenames.length);
        table.setModel(model);
        imageOverlays = new Overlay[filenames.length];
        // initalise the table values. 
        for (int i = 0; i < filenames.length; i++) {
            // image name
            model.setValueAt(filenames[i], i, 0);
            imageOverlays[i] = new Overlay();
        }
        // try and read in any files that may have been saved from previous sessions. 
        readInAllROIS();
    }

    /**
     * Lets the user select a file location containing the images.
     */
    private boolean getImagesAndSetUpGlobals() {

        // filepath of the images. It will only recongnise .tif files. 
        filepath = getDir();
        //filepath = "Y:\\epitkeathly\\TIRF\\170623-activation-NKG2A\\NKp30\\PLL\\tifs\\";

        if (filepath == null) {
            IJ.error("Invalid file location. ");
            return false;
        }
        if (filepath == "") {
            IJ.error("Invalid file location. ");
            return false;
        }

        // get the .tif file names.
        filenames = getTiffiles(filepath);

        if (filenames.length == 0) {
            IJ.error("No .tif files found in the specified location. ");
            return false;
        }

        // preffered ROI.
        //IJ.setTool(Toolbar.FREEROI);
        IJ.setTool(Toolbar.WAND);

        currentImageIndex = 0;
        // a map so store all of the rois in. 
        roiMap = new HashMap< String, ArrayList<Roi>>();
        // Now create a sub-folder the rois for each image data set and initialise 
        // the roi map for all image data sets. 
        for (int i = 0; i < filenames.length; i++) {
            roiMap.put(filenames[i], new ArrayList<Roi>());
        }
        // set this to false every time we read in a new set of images. 
        filessaved = false;

        return true;
    }

    /**
     * Returns the list of file names with a extension.
     *
     * @param directoryPath
     * @return
     */
    private String[] getTiffiles(String directoryPath) {

        if (directoryPath == null) {
            IJ.error("Null or invalid file path. ");
            return new String[0];
        }

        File ff = new File(directoryPath);// .listFiles().length
        File[] fl = ff.listFiles();
        // sort the files so that we get a consistent order. 
        Arrays.sort(fl);
        int nfiles = fl.length;

        String s;
        ArrayList<String> as = new ArrayList<String>();
        for (int i = 0; i < nfiles; i++) {
            s = fl[i].getName();
            if (s.endsWith(".tif")) {
                as.add(s);
            }
        }
        return (String[]) as.toArray(new String[as.size()]);
    }

    /**
     * Store the roi in the ROI map for all sets.
     */
    private void storeROI() {

        if (imp.getRoi() == null) {
            IJ.showMessage("No ROI found on image. ");
            return;
        }

        // get the ROI from the image and save it. 
        Roi roi = imp.getRoi();
        roi.setPosition(imp.getChannel(), 1, imp.getFrame());

        roiMap.get(currentImageName).add(roi);
        imageOverlays[currentImageIndex].add(roi);
        updateTableAddAndRemoveROIs();
        imp.killRoi();

    }

    /**
     * Updates the table when the number of ROIs for a specific dataset has been
     * added or removed.
     */
    private void updateTableAddAndRemoveROIs() {
        int nRois = roiMap.get(currentImageName).size();
        DefaultTableModel model = (DefaultTableModel) table.getModel();
        model.setValueAt("Stored: " + nRois, currentImageIndex, 1);
        imp.setOverlay(imageOverlays[currentImageIndex]);
        // any time we alter the roi list we will set this to false so that we are asked save. 
        filessaved = false;
    }

    /**
     * Save all of the ROIs.
     *
     * A new folder will be created in the original image directory names
     * "ROI2".
     *
     * Inside this folder will an folder for each of the image data sets in the
     * original file location.
     *
     * Any rois stored for each image set will be put into its respective
     * folder.
     *
     */
    private void saveAll() {

        // create a folder to put the ROIS in. 
        String mainSavePath = filepath + "\\" + ROI_FOLDER_NAME;//    new File("/dir/path").delete();;

        // Since we may have deleted some of the pre-existing ROIs we will delete 
        // the entire folder and create a new folder. 
        File f1 = new File(mainSavePath);
        if (f1.exists()) {
            try {
                FileUtils.cleanDirectory(f1);
            } catch (Exception e) {
                IJ.log("Error: " + e.getLocalizedMessage());
            }
        } else {
            //System.out.println(mainSavePath);
        }

        f1.mkdirs();

        String savePath;
        ArrayList<Roi> rois;
        Roi roi;
        RoiEncoder re;
        for (int i = 0; i < filenames.length; i++) {
            savePath = mainSavePath + "\\" + filenames[i];
            new File(savePath).mkdirs();
            rois = roiMap.get(filenames[i]);
            for (int j = 0; j < rois.size(); j++) {
                roi = rois.get(j);
                // save the ROI.
                re = new RoiEncoder(savePath + "\\" + "roi_" + filenames[i] + "_" + j + ".roi");
                try {
                    re.write(roi);
                    // change the status in the data table. 
                    DefaultTableModel model = (DefaultTableModel) table.getModel();
                    model.setValueAt("Saved " + (j + 1), i, 1);
                } catch (IOException ex) {
                    IJ.log("Error saving file: " + ex.getMessage());
                }
            }
        }
        filessaved = true;
    }

    /**
     * Tests the save location for any ROIs which have previously been saved.
     */
    private void readInAllROIS() {

        DefaultTableModel model = (DefaultTableModel) table.getModel();

        File f1 = new File(filepath + "\\" + ROI_FOLDER_NAME);
        if (!f1.exists()) {
            return;
        }
        // now try and read in the ROI files.
        File f2;
        String path;
        ArrayList<Roi> rois;
        for (int i = 0; i < filenames.length; i++) {
            path = filepath + "\\" + ROI_FOLDER_NAME + "\\" + filenames[i];
            f2 = new File(path);
            if (!f2.exists()) {
                model.setValueAt("None found", i, 1);
                continue;
            }
            // list of all files in this destination. 
            File[] filelist = f2.listFiles();
            // sort the files so that we get a consistent order. 
            Arrays.sort(filelist);
            int nfiles = filelist.length;
            String s;
            rois = roiMap.get(filenames[i]);
            int counter = 0;
            for (int j = 0; j < nfiles; j++) {
                s = filelist[j].getName();
                //System.out.println(s);
                if (s.endsWith(".roi")) {
                    try {
                        RoiDecoder rd = new RoiDecoder(filepath + "\\" + ROI_FOLDER_NAME + "\\" + filenames[i] + "\\" + s);
                        Roi roi = rd.getRoi();
                        rois.add(roi);
                        
                        counter++;                        
                        model.setValueAt(counter + " saved previously", i, 1);
                        imageOverlays[i].add(roi);
                    } catch (IOException ex) {
                        IJ.log("Error: " + ex.getLocalizedMessage());
                    }
                }
            }
            // we didnt find any in this location. 
            if (counter == 0) {
                model.setValueAt("None found", i, 1);
            }
        }

    }

    /**
     * Opens an image in the input list of image names. Either the next image,
     * previous image or the current image will be opened.
     *
     * @param dir = 1 for the next image, 0 for the same image again, -1 for the
     * previous image.
     */
    private void openImage(int indexNow) {

        //dir = (int)Math.signum(dir);
        if (indexNow < 0) {
            currentImageIndex = 0;
            IJ.showMessage("This is the first image in the folder list.");
        } else if (indexNow > (filenames.length - 1)) {
            currentImageIndex = filenames.length - 1;
            IJ.showMessage("This is the last image in the sequence.");
        } else {
            currentImageIndex = indexNow;
        }

        // select the current colum in the table.
        table.setRowSelectionInterval(currentImageIndex, currentImageIndex);

        // Check if the user has closed the image or not. 
        if (imp != null) {
            imp.changes = false;
            ImagePlus.removeImageListener(this);
            ic.removeMouseListener(this);
            imp.close();
            imp = null;
            ic = null;
        }
        // set the
        currentImageName = filenames[currentImageIndex];

        imp = IJ.openImage(filepath + "\\" + currentImageName);
        imp.show();
        imp.killRoi();
        imp.setOverlay(imageOverlays[currentImageIndex]);
        ImagePlus.addImageListener(this);
        ic = imp.getCanvas();

        ic.addMouseListener(this);
        // remove the edge pixels of the image so that the wand tool misses the 
        // edge.
        removeBorders();

        silentchange = true;
        // reset the max and min values for all slices in the image. 
        for (int i = 0; i < imp.getStackSize(); i++) {
            imp.setPosition(i + 1);
            IJ.resetMinAndMax(imp);
        }
        imp.setC(prefViewingChannel);

        // Check to see if we already have an ROI for this image. If so display 
        // it on the graph. 
        // try and read in an ROI for this image
        try {
            RoiDecoder rd = new RoiDecoder(filepath + "\\" + ROI_FOLDER_NAME + "\\" + filenames[currentImageIndex] + ".roi");
            Roi roi = rd.getRoi();
            imp.setRoi(roi);
//            DefaultTableModel model = (DefaultTableModel) table.getModel();
//            model.setValueAt("Previously saved", currentImageIndex, 1);
        } catch (IOException ex) {

        }

    }

    /**
     * Allows the user to click on a specific ROI and remove it from the set of
     * ROIs.
     */
    private void removeROIMode() {

        if (jbtDelete.isSelected()) {
            jbtDelete.setSelected(false);
            removemode = false;
            IJ.setTool(Toolbar.WAND);
        } else {
            jbtDelete.setSelected(true);
            removemode = true;
            IJ.setTool(Toolbar.HAND);
        }
        // enable/disable the other buttons      
        jbtSaveRoi.setEnabled(!removemode);
        jbtOpenFileLoc.setEnabled(!removemode);
        jbtSaveAll.setEnabled(!removemode);

    }

    /**
     * Deals with actually selecting and removing the rois that the user clicks
     * on.
     */
    private void roiDeleter() {

        if (!removemode) {
            return;
        }

        ArrayList<Roi> rois = roiMap.get(currentImageName);
        Iterator<Roi> it = rois.iterator();
        // pixel level position of the clicked pixel. 
        Point p = ic.getCursorLoc();
        Roi roi;
        // remove any roi which contains this pixel. 
        while (it.hasNext()) {
            roi = it.next();
            if (roi.contains(p.x, p.y)) {
                imageOverlays[currentImageIndex].remove(roi);
                it.remove();
            }
        }
        updateTableAddAndRemoveROIs();

    }

    /**
     * Sets the outline of the image the image borders to zero. We do this to
     * stop the wand ROI from using the edges of the images.
     */
    private void removeBorders() {

        // Set the border of the image to zero to make the wand tool work 
        Roi roiTop, roiBottom, roiLeft, roiRight;
        roiTop = new Roi(0, 0, imp.getWidth(), 1);
        roiBottom = new Roi(0, imp.getWidth() - 1, imp.getWidth(), 1);
        roiLeft = new Roi(0, 0, 1, imp.getHeight());
        roiRight = new Roi(imp.getWidth() - 1, 0, 1, imp.getHeight());

        for (int i = 0; i < imp.getStackSize(); i++) {
            try {
                silentchange = true;
                ip = imp.getStack().getProcessor(i + 1);
                ip.blurGaussian(1d);
                ip.setValue(0d);
                ip.setColor(0d);
                ip.fill(roiTop);
                ip.fill(roiBottom);
                ip.fill(roiLeft);
                ip.fill(roiRight);
            } finally {
                silentchange = false;
            }
        }

    }

    /**
     * Allows the threshold to be adjusted with the slider.
     */
    private void adjustThreshold() {

        if (imp == null) {
            return;
        }

        if (!jSliderThreshold1.getValueIsAdjusting()) {
            try {
                silentchange = true;
                double sliderVal = (int) jSliderThreshold1.getValue();
                ip = imp.getProcessor();
                if (ip == null) {
                    IJ.error("No open images. Press on the image in the table \n you would like to threshold. ");
                    return;
                }
                ip.setThreshold(0, sliderVal, ImageProcessor.RED_LUT);
                imp.updateAndDraw();
            } finally {
                silentchange = false;
            }
        }

    }

    /**
     *
     * @return true if the image slice was changed.
     */
    private boolean sliceChanged() {

        int currentT, currentC, currentZ;
        currentT = imp.getT();
        currentC = imp.getC();
        currentZ = imp.getZ();
        boolean hasChanged = false;
        if ((currentT != lastT) || (lastZ != currentZ) || (currentC != lastC)) {
            hasChanged = true;
        }
        lastT = currentT;
        lastC = currentC;
        lastZ = currentZ;
        return hasChanged;
    }

    /**
     * Resets the current slider min and max according if the user has selected
     * a different channel/frame/z-slice.
     */
    private void resetSlider() {

        // a silent change happens when the user changes the threshold.
        if (!silentchange && useThreshold && sliceChanged()) {
            ip = imp.getProcessor();
            double min, max, mean;
            min = ip.getMin();
            max = ip.getMax();
            mean = ip.getStatistics().mean;
//
//            int autothreshold = 0;//ip.getAutoThreshold();
//
//            System.out.println(" mean " + mean + " at " + autothreshold);

            jSliderThreshold1.setMinimum((int) Math.round(min));
            jSliderThreshold1.setMaximum((int) Math.round(max));
            jSliderThreshold1.setValue((int) Math.round(mean));
        }
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

    @Override
    public void actionPerformed(ActionEvent ae) {

        Object source = ae.getSource();
        if (source == jbtOpenNext) {
            // open the next image in the list of images 
            int ii = currentImageIndex + 1;
            openImage(ii);
        } else if (source == jbtOpenPrevious) {
            // open the previous image in the list of images. 
            int ii = currentImageIndex - 1;
            openImage(ii);
        } else if (source == jbtSaveRoi) {
            // save the roi 
            storeROI();
        } else if (source == jbtDelete) {
            // deals with removing rois. Disables most other buttons whilst in this mode. 
            removeROIMode();
        } else if (source == jbtOpenFileLoc) {
            // Read in images and initialise the table. 
            setup();
        } else if (source == jbtSaveAll) {
            // Save all of the ROIs with their respective image names from the map. 
            saveAll();
        }

    }

    @Override
    public void mouseClicked(MouseEvent me) {

        int nClicks;
        if (me.getSource() == table) {
            if (me.getButton() == 1) {
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

        Object source = me.getSource();

        if (source == ic) {
            // see is the user has clicked on one of the ROIs. 
            if (removemode) {
                roiDeleter();
            }
        }

    }

    @Override
    public void mouseEntered(MouseEvent me) {

    }

    @Override
    public void mouseExited(MouseEvent me) {

    }

    @Override
    public void adjustmentValueChanged(AdjustmentEvent ae) {

        if (ae.getSource() == jSliderThreshold1) {
            adjustThreshold();
        }
    }

    @Override
    public void imageOpened(ImagePlus ip) {

    }

    @Override
    public void imageClosed(ImagePlus ip) {

    }

    @Override
    public void imageUpdated(ImagePlus impp) {
        // reset the threshold siaplay range. 
        //System.out.println(" changed " + System.currentTimeMillis());

        if (impp == imp) {
            resetSlider();
        }

    }

    @Override
    public void itemStateChanged(ItemEvent ie) {
        if (ie.getSource() == jcbUseThreshold) {
            useThreshold = jcbUseThreshold.isSelected();
            if (useThreshold) {
                jSliderThreshold1.setEnabled(true);
            } else {
                jSliderThreshold1.setEnabled(false);
            }
        } else if (ie.getSource() == jcomboPreferredChannel) {
            // we will view this channel instead. 
            prefViewingChannel = jcomboPreferredChannel.getSelectedIndex() + 1;
            // update the image view 
            imp.setC(prefViewingChannel);
        }

    }

    @Override
    public void windowOpened(WindowEvent we) {

    }

    @Override
    public void windowClosing(WindowEvent we) {

        if (we.getSource() == frame) {
            // check with the user that we have saved the files. 
            if (!filessaved) {
                YesNoCancelDialog ync = new YesNoCancelDialog((Frame) frame, "Save files", "Would you like to save before closing?");
                if (ync.yesPressed()) {
                    saveAll();
                    frame.dispose();
                } else if (ync.cancelPressed()) {
                    // keep the window open. 
                } else {
                    frame.dispose();
                }
            }else{
                frame.dispose();
            }
        }

    }

    @Override
    public void windowClosed(WindowEvent we) {

    }

    @Override
    public void windowIconified(WindowEvent we) {

    }

    @Override
    public void windowDeiconified(WindowEvent we) {

    }

    @Override
    public void windowActivated(WindowEvent we) {

    }

    @Override
    public void windowDeactivated(WindowEvent we) {

    }

}
