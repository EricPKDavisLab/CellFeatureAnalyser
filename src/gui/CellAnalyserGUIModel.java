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

import featureobjects.ParentFeature;
import roiandoverlays.ParentFeatureTotalROI;
import ij.IJ;
import ij.ImagePlus;
import ij.gui.Overlay;
import ij.gui.PolygonRoi;
import ij.gui.Roi;
import io.ParentFeature_ROI;
import java.util.ArrayList;
import java.util.HashMap;
import main.CellFeatureAnalyser_;
import abstractprocessors.AbstractParentFeatureProcessor;
import drawoutline.ROIdrawAndSave2_;
import ij.ImageListener;
import java.io.File;
import java.util.HashSet;

/**
 *
 * The data structure passed between any of the
 * {@link AbstractParentFeatureProcessor}s.
 *
 * It will be used to store of the {@link ParentFeature}s as well as any other
 * data that needs to be passed between various processes within the whole
 * analysis layout.
 *
 * A reference to the current image will be stored within the model which will
 * be called by the {@link AbstractParentFeatureProcessor}s classes.
 *
 * @author mqbssep5
 */
public final class CellAnalyserGUIModel implements ImageListener {// extends HashMap<String,Object>

    /**
     * A Map used to store the {@link ParentFeature}s for each of the image data
     * sets.
     */
    private final HashMap< String, ArrayList<ParentFeature> > featureStore = new HashMap< String, ArrayList<ParentFeature>>();
    
    /**
     * A set which is used to store the name of every process which has been run 
     * on the data contained in the {@link CellAnalyserGUIModel}.
     * 
     * This will be used to determine if information across other {@link AbstractParentFeatureProcessor}
     * 
     * TODO: finish comments on this. 
     * 
     */
    private final HashSet<String> featureProcessorNames = new HashSet<String>();

    /**
     * The current image set being processed. Only one set will be open at a
     * time.
     */
    private ImagePlus imp;

    /**
     * The image file names to be analysed.
     */
    private final String[] imageFileNames;

    /**
     * File directory containing all of the images to be processed.
     */
    private final String imageFileDirectory;

    /**
     * The index of image currently loaded into the model. This will change from
     * 0 to n_files - 1.
     */
    private int currentImageIndex = 0;

    /**
     * Number of channels.
     */
    private int nchannels = 1;

    /**
     * The name of the folder that the output files of this Plugin will be saved to. 
     */
    public static String SAVE_PATH_EXT = "CellAnalysisFiles";

    private boolean currentWasClosed = false;

    /**
     * Constructor.
     *
     * @param imageFileDirectory the directory containing all of the images to
     * be processed.
     * @param imageNames the filenames of all the images to be used in the
     * processing.
     */
    public CellAnalyserGUIModel( String imageFileDirectory, String[] imageNames ) {
        this.imageFileNames = imageNames;
        this.imageFileDirectory = imageFileDirectory;
        initFeatureStore();
    }

    /**
     * Setter for the number of channels
     *
     * @param nchannels
     */
    public void setNchanels(int nchannels) {
        this.nchannels = nchannels;
    }

    /**
     * @return the number of channels in these data.
     */
    public int getNchannels() {
        return nchannels;
    }

    /**
     * Initialises the feature store for this data set.
     */
    private void initFeatureStore() {
        for (int i = 0; i < imageFileNames.length; i++) {
            featureStore.put(imageFileNames[i], new ArrayList<>());
        }
    }

    /**
     * @return closes and reopens the current image in its initial, unaltered
     * state.
     */
    public ImagePlus refreshCurrentImage() {
        openImage(currentImageIndex);
        return imp;
    }

    /**
     * @return returns a reference to the current image.
     */
    public ImagePlus getCurrentImageDataSet() {
        // Check if it has been opened yet (or closed prematurely). 
        if (imp == null || currentWasClosed) {
            openImage(currentImageIndex);
            currentWasClosed = false;
        }
        return imp;
    }

    /**
     * @return the list of features for this image set.
     */
    public ArrayList<ParentFeature> getCurrentImageSetParentFeatures() {
        return featureStore.get(imageFileNames[currentImageIndex]);
    }

    /**
     * Gets the list of features for the defined image set. Indexing starts with
     * 1. Will return an empty set of the image has not been read in yet.
     *
     * @param index
     * @return the list of features for this image set.
     */
    public ArrayList<ParentFeature> getFeatures(int index) {
        return featureStore.get(imageFileNames[index]);
    }

    /**
     * Getter for the features for this image data set.
     *
     * @param imageSetName the name of the image data set which contains the set
     * of features.
     * @return the list of features for this image set.
     */
    public ArrayList<ParentFeature> getFeatures(String imageSetName) {
        return featureStore.get(imageSetName);
    }

    /**
     * It replaces any feature currently associated with this name.
     *
     * @param imageSetName the name of the image data set which contains the set
     * of features.
     * @param features
     */
    public void setFeatures(String imageSetName, ArrayList<ParentFeature> features) {
        featureStore.put(imageSetName, features);
    }

    /**
     * @return number of image data sets.
     */
    public int getNImages() {
        return imageFileNames.length;
    }

    /**
     * @return the list of file names all of the images that have been
     * processed.
     */
    public String[] getImageNames() {
        return imageFileNames;
    }

    /**
     * Opens an image in the input list of image names. Either the next image,
     * previous image or the current image will be opened.
     *
     * @param imageIndex
     *
     */
    public void openImage( int imageIndex ) {

        if (imageFileNames == null) {
            return;
        }
        if (imageFileNames.length == 0) {
            return;
        }
        // do some saftey checks on the numbers. 
        imageIndex = Math.max(0, imageIndex);
        imageIndex = Math.min(imageIndex, (imageFileNames.length - 1));

        currentImageIndex = imageIndex;

        // Check if the user has closed the image already.
        if (imp != null) {
            imp.changes = false;
            imp.close();
            imp.removeImageListener(this);
            imp = null;
        }

        imp = IJ.openImage(imageFileDirectory + "\\" + imageFileNames[currentImageIndex]);
        imp.show();

        // remove any pre existing overlays or ROIs. 
        imp.killRoi();
        imp.setOverlay(new Overlay());
        IJ.resetMinAndMax(imp);
        imp.addImageListener(this);
        // 
        nchannels = imp.getNChannels();

        // don't replace the features if we have already read them in. 
        if (!featureStore.get(imageFileNames[currentImageIndex]).isEmpty()) {
            updateOverlay();
            return;
        }

        // Load in our ROIs. We will then use them to iniailise the ParentFeatures 
        // for this data set. If their are no ROIs for this data set then we will 
        // assume that the whole feild of view is a Feature.
        // check which version of ROI saving we did 
        File f2 = new File(imageFileDirectory + CellFeatureAnalyser_.ROI_FOLDER_NAME);
        File f3 = new File(imageFileDirectory + ROIdrawAndSave2_.ROI_FOLDER_NAME);
        String roiPath;

        ParentFeature_ROI pfa;
        ArrayList<Roi> rois = new ArrayList<>();
        if (f3.exists()) {
            // Now initialise the ParentFeature list for this data set if they have not yet been initialised yet. 
            roiPath = imageFileDirectory + ROIdrawAndSave2_.ROI_FOLDER_NAME;
            pfa = new ParentFeature_ROI(roiPath);
            rois = pfa.getROIs2(imageFileNames[currentImageIndex]);    
        } else {
            roiPath = imageFileDirectory + CellFeatureAnalyser_.ROI_FOLDER_NAME;
            pfa = new ParentFeature_ROI(roiPath);
            rois = pfa.getROIs(imageFileNames[currentImageIndex]);            
        }

        ArrayList<ParentFeature> features;
        double pixelscale = imp.getCalibration().pixelWidth;
        //
        if (!rois.isEmpty()) {
            // create the features from the ROIs.
            features = initialiseParentFeaturesFromROIs(rois, nchannels, imageFileNames[currentImageIndex], pixelscale);
        } else {
            // Assume we want to use the entire image. 
            features = initialiseParentFeaturesImageSize(imp, imageFileNames[currentImageIndex], pixelscale);
        }
        // Assign the features to this image name. 
        setFeatures(imageFileNames[currentImageIndex], features);

        updateOverlay();
    }

    /**
     * Initialises a list of {@link ParentFeatures} based on a set of rois.
     *
     * @param rois
     * @param nchanels
     * @param imageName
     * @return list of features for this image data set.
     */
    private ArrayList<ParentFeature> initialiseParentFeaturesFromROIs(ArrayList<Roi> rois, int nchanels, String imageName, double pixelsize) {

        ArrayList<ParentFeature> featureList = new ArrayList<>();
        PolygonRoi roi;
        ParentFeature feature;
        for (int i = 0; i < rois.size(); i++) {
            // PolygonRois required for this. 
            roi = (PolygonRoi) rois.get(i);
            // 
            feature = new ParentFeature(roi.getTPosition(), nchanels, imageName, roi, pixelsize);
            featureList.add(feature);
        }
        return featureList;
    }

    /**
     * Initialises a list of {@link ParentFeatures} based on filling
     *
     * @param imp
     * @param nchanels
     * @param imageName
     * @return list of features for this image data set.
     */
    private ArrayList<ParentFeature> initialiseParentFeaturesImageSize(ImagePlus imp, String imageName, double pixelsize) {

        ArrayList<ParentFeature> featureList = new ArrayList<>();
        // 
        int nchannels = imp.getNChannels();
        int nframes = imp.getNFrames();
        int width = imp.getWidth();
        int height = imp.getHeight();

        PolygonRoi roi;
        ParentFeature feature;
        for (int i = 0; i < nframes; i++) {
            // Initialise a new PolygonRoi covering the entire image. 
            roi = new PolygonRoi(new int[]{0, width, width, 0}, new int[]{0, 0, height, height}, 4, PolygonRoi.POLYGON);
            // create a feature for each frame. 
            feature = new ParentFeature(i + 1, nchannels, imageName, roi, pixelsize);
            featureList.add(feature);
        }
        return featureList;
    }

    /**
     * @param s the name of the {@link AbstractParentFeatureProcessor} that has 
     * been run on the data within this {@link CellAnalyserGUIModel}. 
     */
    public void addProcessorNameToList( String s ){
        featureProcessorNames.add(s);
    }
    
    /**
     * @return A set of the names of the {@link AbstractParentFeatureProcessor} 
     * that have been run on the data in this {@link CellAnalyserGUIModel}. 
     */
    public HashSet<String> getFeatureProcessorNames(){
        return featureProcessorNames;
    }
    
    /**
     * @return the index of the image currently selected/being processed.
     */
    public int getCurrentImageIndex() {
        return currentImageIndex;
    }

    public String getOriginalDirectory() {
        return imageFileDirectory;
    }

    public String getSaveFileDirectory() {
        return imageFileDirectory + "//" + SAVE_PATH_EXT;
    }

    /**
     * Updates the overlay for this image set.
     */
    public void updateOverlay() {
        Overlay overlay = new Overlay();
        ParentFeatureTotalROI roi = new ParentFeatureTotalROI(imp, featureStore.get(imageFileNames[currentImageIndex]));
        overlay.add(roi);
        imp.setOverlay(overlay);
    }

    @Override
    public void imageOpened(ImagePlus ip) {

    }

    @Override
    public void imageClosed(ImagePlus ip) {
        if (ip == imp) {
            //System.out.println(" closed current ");
            currentWasClosed = true;
        }
    }

    @Override
    public void imageUpdated(ImagePlus ip) {

    }

}

