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
package featureobjects;

import arrayutil.List2Prims;
import gui.CellAnalyserGUIModel;
import ij.ImagePlus;
import ij.measure.ResultsTable;
import ij.plugin.Duplicator;
import java.util.ArrayList;

/**
 * A class of static functions which can be used within other functions for
 * processing features.
 *
 * Examples being functions for:
 *
 * @author mqbssep5
 */
public class ParentFeatureOps {

    /**
     * Extracts the image data in the rectangle box which this {@link Parent}
     *
     * @param pf the feature
     * @param imp a reference to the original data set that this feature was
     * located in.
     * @return a crop of the image data for this feature. It will be a single
     * time frame, but will include all channels and all image z-slices.
     */
    public static ImagePlus getLocalisedFeatureImageDataAllChannels(ParentFeature pf, ImagePlus imp) {
        // set an roi. 
        imp.setRoi(pf.getBounds());
        // duplicate the data within this roi. 
        Duplicator dup = new Duplicator();
        ImagePlus impOut = dup.run(imp, 1, imp.getNChannels(), 1, imp.getNSlices(), pf.getFrame(), pf.getFrame());
        imp.killRoi();
        return impOut;
    }

    /**
     * Extracts the image data in the rectangle box which this {@link Parent}.
     *
     * Assumes the image has already been split into a single channel.
     *
     * @param pf the feature
     * @param imp a reference to the original data set that this feature was
     * located in.
     * @return a crop of the image data for this feature. It will be a single
     * time frame, but will include all channels and all image z-slices.
     */
    public static ImagePlus getLocalisedFeatureImageData(ParentFeature pf, ImagePlus imp) {
        // set an roi. 
        imp.setRoi(pf.getBounds());
        // duplicate the data within this roi. 
        Duplicator dup = new Duplicator();
        ImagePlus impOut = dup.run(imp, 1, 1, 1, imp.getNSlices(), pf.getFrame(), pf.getFrame());
        imp.killRoi();
        return impOut;
    }

    /**
     * Extracts the image data in the rectangle box which this
     * {@link ParentFeature} for the specified frame.
     *
     * @param pf the feature
     * @param imp a reference to the original data set that this feature was
     * located in.
     * @param channel the specified channel from this feature.
     * @return a crop of the image data for this feature. It will be a single
     * time frame, but will include all channels and all image z-slices.
     */
    public static ImagePlus getLocalisedFeatureImageDataSpecifiedChannel(ParentFeature pf, ImagePlus imp, int channel) {
        // set an roi. 
        imp.setRoi(pf.getBounds());
        // duplicate the data within this roi. 
        Duplicator dup = new Duplicator();
        ImagePlus impOut = dup.run(imp, channel, channel, 1, imp.getNSlices(), pf.getFrame(), pf.getFrame());
        imp.killRoi();
        return impOut;
    }

    /**
     * Goes through all {@link ParentFeature}s in the model and extracts the numerical 
     * value of the specified feature from each of the {@link ParentFeature} numerical 
     * feature map. 
     * @param model
     * @param numericalFeatureID
     * @return a list of doubles for the specified numerical feature 
     */
    public static ArrayList<Double> getSpecifiedNumericalFeatureAsDoubleList( CellAnalyserGUIModel model, String numericalFeatureID ){
        
        String[] imageNames;
        imageNames = model.getImageNames();
        
        ArrayList<ParentFeature> parentFeatures;
        ArrayList<Double> valueList = new ArrayList<>();
        
        double value;
        // all image data sets.
        for(String s:imageNames){
            // All the features found in for this set. 
            parentFeatures = model.getFeatures(s);
            for( int i = 0; i < parentFeatures.size(); i++ ){
                // extract the specified value. 
                value = parentFeatures.get(i).getNumericPropertyValue(numericalFeatureID);
                valueList.add(value);
            }
            
        }
        return valueList;
    }
    
    /**
     * Goes through all {@link ParentFeature}s in the model and extracts the numerical 
     * value of the specified feature from each of the {@link ParentFeature} numerical 
     * feature map. 
     * @param model
     * @param numericalFeatureID
     * @return an array of doubles for the specified numerical feature 
     */    
     public static double[] getSpecifiedNumericalFeatureAsDoubleArray( CellAnalyserGUIModel model, String numericalFeatureID ){
         ArrayList<Double> list = getSpecifiedNumericalFeatureAsDoubleList( model, numericalFeatureID );
         double[] values = List2Prims.doubleFromDouble(list);
         return values;
     }

}
