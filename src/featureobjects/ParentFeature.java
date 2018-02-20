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

import ij.gui.PolygonRoi;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.HashMap;
import roiutil.RoiUtil;

/**
 * The parent class for a main feature found in a microscopy image which will 
 * contain its own set of {@link Feature}s such as spots/particles in the image. 
 * 
 * @author mqbssep5
 */
public class ParentFeature {

    protected PolygonRoi roi;
    
    /**
     * The localised roi for this feature for when the image is locally cropped 
     * by the features rectangular bounds to in cases where it is appropriate to 
     * speed up the processing. 
     */
    protected PolygonRoi shiftedRoi;
    
    protected int frame, x0, y0;

    protected int nchannels;
    
    /**
     * The file name of the data set this 
     */
    protected String imagename;
    
    /**
     * The Rectangle which bounds this feature.
     */
    protected Rectangle bounds;
    
    /**
     * Stores the lists of features for each channel.
     */
    private FeatureMapObj[] channelFeatures;
    
    private double pixelscale = 1.0;
    
    
    /**
     * A Map for storing numerical properties of this {@link ParentFeature}. 
     * These may include features of the actual parent, such as area, but also
     * stats related to all of the stored features stored in the feature map. 
     */
    protected HashMap< String, Double > numericalProperties = new HashMap<>();
    
    
    /**
     * Used to store other objects such as arrays of values etc.
     */
    protected HashMap< String, Object > objectProperties = new HashMap<>();

    /**
     * @param frame the SPECIFIC frame this parent feature is located within the data set. (index from 1 like ImageJ)
     * @param nchannels the TOTAL number of channels in the image data set. (index from 1 like ImageJ)
     * @param imagename the filename of the image that this feature was detected in.
     * @param outlineRoi the outline of the {@link AbstractParentFeature} 
     * @param pixelscale 
     */
    public ParentFeature( int frame, int nchannels, String imagename, PolygonRoi outlineRoi, double pixelscale ) {
        this.frame = frame;
        this.nchannels = nchannels;
        this.imagename = imagename;
        this.roi = outlineRoi; 
        channelFeatures = new FeatureMapObj[nchannels];
        for(int i = 0; i < nchannels; i++){
            channelFeatures[i] = new FeatureMapObj();
        }
        this.pixelscale = pixelscale;
        
    }

    /**
     * Lets different sets of features be added to this parent feature. 
     * @param channel the image channel this feature was found in. 
     * @param key the key ID for this for this feature. 
     * @param features the list of specific features. 
     */
    public void addFeatures( int channel, String key, ArrayList<Feature> features ) {
        if (channelFeatures == null) {
            channelFeatures = new FeatureMapObj[nchannels];
            for (int i = 0; i < nchannels; i++) {
                channelFeatures[i] = new FeatureMapObj();
            }
        }
        if( channel < 0 || channel >= nchannels ){
           // maybe throw an error in future. 
           return;
        }
        channelFeatures[channel].put(key, features);
    }
    
    /**
     * Getter method for the specified list of features. 
     * @param channel the image channel that this feature was found in. 
     * @param key the string ID that this set of features.
     * @return 
     */
    public ArrayList<Feature> getFeatures( int channel, String key ){
        return channelFeatures[channel].get(key);
    }
    
    /**
     * Getter for the numerical property of this parent feature.
     * @param key
     * @return the value assigned to this feature. 
     */
    public double getNumericPropertyValue( String key ){
        return numericalProperties.get(key);
    }
    
    /**
     * Adds the specified feature to the properties map.
     *
     * @param name An informative name for the feature
     * @param value the value for the
     */
    public void addNumericProperty( String name, Double value ) {
        numericalProperties.put(name, value);
    }    
    
    /**
     * Getter for the object property of this parent feature.
     * @param key
     * @return the value assigned to this feature. 
     */
    public Object getObjectPropertyValue( String key ){
        return objectProperties.get(key);
    }
    
    /**
     * Adds the specified feature to the properties map.
     *
     * @param name An informative name for the Object
     * @param object the value for the
     */
    public void addObjectProperty( String name, Object object ) {
        objectProperties.put(name, object);
    }        
    
    /**
     * @return the box bounds of this feature.
     */
    public Rectangle getBounds(){
        bounds = roi.getBounds();
        x0 = bounds.x;
        y0 = bounds.y;
        return bounds;
    }
    
    /**
     * @return the origin of the rectangle of the box which bounds the outline 
     * this feature. Used for cropping of images which may contain multiple {@link ParentFeature}s.
     */
    public int get_x0(){
        if(bounds == null){
            getBounds();
        }
        return x0;
    }
    
    /**
     * @return the origin of the rectangle of the box which bounds the outline 
     * this feature. Used for cropping of images which may contain multiple {@link ParentFeature}s.
     */    
    public int get_y0(){
        if(bounds == null){
            getBounds();
        }
        return y0;
    }    
    
    /**
     * @return the time frame index for this feature. Indexing is 1 based as is 
     * ImageJ. 
     */
    public int getFrame(){
        return frame;
    }
    
    /**
     * @return the number of channels in the data. 
     */
    public int getNchannels(){
        return nchannels;
    }
    
    /**
     * @return The outline of this {@link ParentFeature} in the original images 
     * coordinates. 
     */
    public PolygonRoi getFeatureBoundsOriginalImage(){
        return roi;
    }
    
    /**
     * @return The outline of this feature which has been translated by (-x0,-y0)
     * so that surrounds the feature when the image data has been cropped for 
     * speed of processing. 
     */
    public PolygonRoi getFeatureBoundsShifted(){
        if( shiftedRoi == null ){
            if( bounds == null ){
                // compute the bounds and also x0, y0.
                getBounds();
            }
            // Make sure we get a proper duplicate of the data points of the ROI
            // and not a reference to the original 
            shiftedRoi = RoiUtil.translatedCopy(roi, -x0, -y0);
        }
        return shiftedRoi;
    }
    
    /**
     * Getter method for all features assigned to the specified channel. 
     * @param i 
     * @return all the {@link Feature}s for the specified channel. Indexing starts at zero. 
     */
    public HashMap< String, ArrayList<Feature> > getAllFeaturesForChannel( int i ){
        return channelFeatures[i];
    }
    
    /**
     * @return the pixel size of the image this feature was found in. 
     */
    public double getPixelXYsize(){
        return pixelscale;
    }

    /**
     * A private class which is a direct extension of a HashMap< String, Object>.
     * The object is created in this way so that an array of the maps can be created. 
     */
    private class FeatureMapObj extends HashMap< String, ArrayList<Feature> > {

    }

}
