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
import java.awt.Color;
import java.awt.Polygon;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

/**
 * An object used to represent a feature within a specific channel of a microscopy 
 * image. 
 * @author mqbssep5
 */
public class Feature {
    
    /**
     * A Map<String,Double> used to store numeric features associated with
     * this particle. (just like TrackMate)
     */
    private final HashMap<String, Double> numericProps = new HashMap<String, Double>();
    
    /**
     * A Map<String,Object> used to store numeric features associated with
     * this particle. (just like TrackMate)
     */
    private final HashMap<String, Object> feilds = new HashMap<String, Object>();    
    
    private Color drawcolor = Color.RED; 
    
    //private String featureType;
    
    private final int id;
    
    private PolygonRoi outline;
    
    private double yCentPix;
    
    private double xCentPix;
    
    /**
     * @param id a unique ID for this feature. 
     * @param outline 
     * @param xCentPix a centroid value in units of pixels (not scaled by physical)
     * @param yCentPix a centroid value in units of pixels (not scaled by physical)
     */
    public Feature( int id, PolygonRoi outline, double xCentPix, double yCentPix ){
        this.id = id;
        this.outline = outline;
        this.xCentPix = xCentPix;
        this.yCentPix = yCentPix;
    }
        
    /**
     * The intensity values of this Feature.
     */
    private double[] pixelIntensityValues;    
    
    /**
     * Setter method for the intensity values at each pixel within the feature. 
     * @param pixelIntensityValues 
     */
    public void setPixelValues( double[] pixelIntensityValues ) {
        this.pixelIntensityValues = Arrays.copyOf(pixelIntensityValues, pixelIntensityValues.length);
    }
    
    /**
     * Adds the specified feature to the properties map.
     *
     * @param name - An informative name for the feature
     * @param feature - the feature
     */
    public void addNumericFeature( String name, Double feature ) {
        numericProps.put(name, feature);
    }    
    
    /**
     * @param name
     * @return the specified feature.
     */
    public Double getNumericFeature( String name ) {
        return numericProps.get(name);
    }    
    
//    /**
//     * Returns a list of any keys 
//     * @param keyStart
//     * @return 
//     */
//    public ArrayList<String> getNumericFeatureStringsStartingWith( String keyStart ){
//        
//    }
//    
    /**
     * @return the full map of numeric features.
     */
    public HashMap< String, Double > getNumericFeatures(){
        return numericProps;
    }    
    
    /**
     * Adds the specified feature to the properties map.
     *
     * @param name - An informative name for the feature
     * @param feature - the feature
     */
    public void addObject( String name, Object feature ) {
        feilds.put(name, feature);
    }    
    
    /**
     * @param name
     * @return the specified feature.
     */
    public Object getObject( String name ) {
        return feilds.get(name);
    }    
    
    /**
     * @return the full map of numeric features.
     */
    public HashMap< String, Object > getObjects(){
        return feilds;
    }    
    
    /**
     * Setter for the color this Feature will be drawn in. 
     * @param color 
     */
    public void setDrawColor( Color color ){
        this.drawcolor = color;
    }
    
    /**
     * @return The PolygonRoi which defines the outline of this {@link Feature}
     */
    public PolygonRoi getOutLine(){
        return outline;
    }
    
    /**
     * Sets resets the centroid position of this spot to the  
     * @param x
     */
    public void setXpix( double x ){
        this.xCentPix = x;
    }
    
    /**
     * Sets resets the centroid position of this spot to the  
     * @param y
     */
    public void setYpix( double y ){
        this.yCentPix = y;
    }   
    
    /**
     * @return pixel level centroid position of this {@link Feature}
     */
    public double getXpix(){
        return xCentPix;
    }
    
    /**
     * @return pixel level centroid position of this {@link Feature}
     */    
    public double getYpix(){
        return yCentPix;
    }    
    
    /**
     * @return the numerical ID of this feature. 
     */
    public int getID(){
        return id;
    }
    
    /**
     * Setter for the draw colour of the outline of this {@link Feature}
     * @param col 
     */
    public void setDrawColour( Color col ){
        this.drawcolor = col;
    }
    
    /**
     * Getter for the draw color of the outline of this feature. 
     * @return 
     */
    public Color getColor(){
        return drawcolor;
    }
            
    public void resetOutLine( PolygonRoi p ){
        this.outline = p;
    }
    
    /**
     * @return returns a deep copy of this feature with all of its numerical properties.
     */
    public Feature duplicate(){
        
        Polygon p = outline.getPolygon();
        
        PolygonRoi dupOutline = new PolygonRoi(Arrays.copyOf(p.xpoints, p.xpoints.length), Arrays.copyOf(p.ypoints, p.ypoints.length), p.npoints, outline.getType());
        Feature dup = new Feature(id, dupOutline, xCentPix, yCentPix);
        if( pixelIntensityValues != null ){
            dup.setPixelValues(pixelIntensityValues);
        }
        dup.setDrawColour(drawcolor);
        // Now copy over the numerical properties. 
        Set<String> keySet = numericProps.keySet();
        Iterator<String> it = keySet.iterator();
        String s;
        while(it.hasNext()){
            s = it.next();
            dup.addNumericFeature(s, new Double(numericProps.get(s)));
        }
        
        // copy over the other fields. 
        keySet = feilds.keySet();
        it = keySet.iterator();

        Object o;
        while(it.hasNext()){
            s = it.next();
            o = feilds.get(s);
            if( o instanceof double[]){
                double[] dd = (double[])feilds.get(s);
                dup.addObject(s, Arrays.copyOf(dd, dd.length));
            }
            
        }
        
        
        
        return dup;
    }

    
    
    
}
