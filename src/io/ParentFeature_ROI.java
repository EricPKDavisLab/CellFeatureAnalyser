/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io;

import drawoutline.ROIdrawAndSave2_;
import featureobjects.ParentFeature;
import ij.IJ;
import ij.gui.Roi;
import ij.io.RoiDecoder;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * A class for reading in ROIs which have previously been saved for each image
 * and initialises a {@link ParentFeature} from it.
 *
 * Currently the
 *
 * @author mqbssep5
 */
public class ParentFeature_ROI {

    private final String filePath;

    /**
     * Constructor.
     *
     * @param filePath The file path to the folder containing the ROI files.
     */
    public ParentFeature_ROI( String filePath ) {
        this.filePath = filePath;
    }

    /**
     * 
     * TO BE UPDATED.
     *
     * Gets the single roi with the file name = imageName.roi
     *
     * Will return an empty array if no ROI is found for this filename.
     *
     * @param imageName
     * @return
     */
    public ArrayList<Roi> getROIs( String imageName ) {

        ArrayList<Roi> output = new ArrayList<>();

        RoiDecoder rd = new RoiDecoder( filePath + "\\" + imageName + ".roi" );
        Roi roi = null;
        try {
            roi = rd.getRoi();
        } catch (Exception e) {
            return output;
        }
        // Check if the roi has been assigned to a specific frame/whatever 
        if (roi.getCPosition() == 0 || roi.getZPosition() == 0 || roi.getTPosition() == 0) {
            // set this to the first frame. 
            roi.setPosition(1, 1, 1);
        }
        output.add(roi);

        return output;
    }

    /**
     * Reads in the ROIs associated with the specific image name in the folder 
     * specified in the constructor. This may read in several ROIs if they were saved
     * using the {@link ROIdrawAndSave2_} PlugIn. 
     *
     * For each data set, all ROIs will be located in a folder with the same name 
     * as the original data set. 
     * 
     * 
     * @param imageName
     * @return A list of ROIs for the corresponding image name. An empty list will
     * be returned if a folder with the specified file name does not exist. 
     */
    public ArrayList<Roi> getROIs2( String imageName ) {

        // now try and read in the ROI files.
        File f2;

        ArrayList<Roi> output = new ArrayList<>();
        String path = filePath + "\\" + imageName;
        f2 = new File(path);
        if (!f2.exists()) {
           // return an empty list 
           return output;
        }
        // list of all files in this destination. 
        File[] fl = f2.listFiles();
        // sort the files so that we get a consistent order. 
        Arrays.sort(fl);
        int nfiles = fl.length;
        String s;
        RoiDecoder rd;
        for (int j = 0; j < nfiles; j++) {
            s = fl[j].getName();
            //System.out.println(s);
            if (s.endsWith(".roi")) {
                try {
                    rd = new RoiDecoder( path + "\\" + s );
                    Roi roi = rd.getRoi();
                    output.add(roi);
                } catch (IOException ex) {
                    IJ.log("Error: " + ex.getLocalizedMessage());
                }
            }
        }
        return output;
    }

}
