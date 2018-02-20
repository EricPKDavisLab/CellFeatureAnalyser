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
import ij.gui.PolygonRoi;
import java.awt.Color;
import java.awt.Point;
import java.awt.Polygon;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

/**
 * A class of static methods for performing various operations on sets of
 * features.
 *
 * @author mqbssep5
 */
public class FeatureOps {

    /**
     * Option for thresholding values out of the set
     */
    public static int KEEP_GREATER_THAN = 1;

    /**
     * Upper limit for thesholding
     */
    public static int KEEP_LESS_THAN = -1;

    /**
     * Removes any features without their centroid inside of the ROI from the
     * list.
     *
     * @param features
     * @param roi
     */
    public static void removeFeaturesOutsideOfROI(ArrayList<Feature> features, PolygonRoi roi) {

        if (roi == null) {
            return;
        }
        Iterator<Feature> spotIt = features.iterator();
        Feature spot;
        while (spotIt.hasNext()) {
            spot = spotIt.next();
            if (roi.contains((int) Math.round(spot.getXpix()), (int) Math.round(spot.getYpix()))) {
                // Good. Keep it. 
            } else {
                // its outside, remove it. 
                spotIt.remove();
            }
        }
    }

    /**
     * Changes the Color of the {@link Feature} in the list to the define color.
     *
     * @param features
     * @param col
     */
    public static void setAllFeaturesDrawColorSameColor( ArrayList<Feature> features, Color col ) {
        for (Feature f : features) {
            f.setDrawColor(col);
        }
    }

    /**
     * Removes features from the list based on the value of a specific property
     * in the {@link Feature}s, numerical property map.
     *
     * @param features the list of detected features
     * @param keyID the String ID used to identify the value we want to
     * threshold against.
     * @param threshold_value the threshold level.
     * @param upperOrLowerLimit defines weather this is an upper or lower
     * threshold level. If the threshold value is an lower limit, then
     * upperOrLowerLimit = {@link #KEEP_GREATER_THAN}. Otherwise
     * upperOrLowerLimit = {@link #KEEP_LESS_THAN}.
     */
    public static void removeFeaturesBasedOnNumericalPropertyValue( ArrayList<Feature> features, String keyID, double threshold_value, int upperOrLowerLimit ) {

        Iterator<Feature> it = features.iterator();
        Feature f;
        double d;
        // remove values greater than 
        if (upperOrLowerLimit == KEEP_GREATER_THAN) {
            while (it.hasNext()) {
                f = it.next();
                d = f.getNumericFeature(keyID);
                if (d < threshold_value) {
                    it.remove();
                }
            }
        } else if (upperOrLowerLimit == KEEP_LESS_THAN) {
            while (it.hasNext()) {
                f = it.next();
                d = f.getNumericFeature(keyID);
                if (d > threshold_value) {
                    it.remove();
                }
            }
        }

    }

    /**
     * Returns a list of the features from the input list based on the value of
     * a specific property in the {@link Feature}s, numerical property map.
     *
     * @param features the list of detected features
     * @param keyID the String ID used to identify the value we want to
     * threshold against.
     * @param threshold_value the threshold level.
     * @param upperOrLowerLimit defines weather this is an upper or lower
     * threshold level. If the threshold value is an lower limit, then
     * upperOrLowerLimit = {@link #KEEP_GREATER_THAN}. Otherwise
     * upperOrLowerLimit = {@link #KEEP_LESS_THAN}.
     * @return a list containing references to {@link Feature}s in the original 
     * set that which met the defined thresholding criteria. 
     */
    public static ArrayList<Feature> getFeaturesBasedOnNumericalPropertyValue(ArrayList<Feature> features, String keyID, double threshold_value, int upperOrLowerLimit) {

        Iterator<Feature> it = features.iterator();
        Feature f;
        ArrayList<Feature> featureSubSet = new ArrayList<>();
        double d;
        // remove values greater than 
        if (upperOrLowerLimit == KEEP_GREATER_THAN) {
            while (it.hasNext()) {
                f = it.next();
                d = f.getNumericFeature(keyID);
                //if (d < threshold_value) {
                if(Math.signum(d-threshold_value) > 0.0){  
                    featureSubSet.add(f);
                }
            }
        } else if (upperOrLowerLimit == KEEP_LESS_THAN) {
            while (it.hasNext()) {
                f = it.next();
                d = f.getNumericFeature(keyID);
                //if (d > threshold_value) {
                if(Math.signum(d-threshold_value) < 0.0){                 
                    featureSubSet.add(f);
                }
            }
        }
        return featureSubSet;
    }

    /**
     * Returns a new set of duplicate feature objects.
     *
     * @param features
     * @return a new set of duplicate feature objects.
     */
    public static ArrayList<Feature> duplicateFeatureSet(ArrayList<Feature> features) {
        ArrayList<Feature> newSet = new ArrayList<>(features.size());
        Feature f;
        for (int i = 0; i < features.size(); i++) {
            f = features.get(i).duplicate();
            newSet.add(f);
        }
        return newSet;
    }

    /**
     * Shifts the centroid position, as well as its outline, to the new
     * position.
     *
     * @param f the feature to be
     * @param x the new x position in units of pixels (can be sub pixel)
     * @param y the new y position in units of pixels (can be sub pixel)
     *
     */
    public static void recentreFeature2D( Feature f, double x, double y ) {

        double xOrg, yOrg;
        xOrg = f.getXpix();
        yOrg = f.getYpix();

        PolygonRoi roi = f.getOutLine();

        int dx = (int) Math.round(x - xOrg);
        int dy = (int) Math.round(y - yOrg);

        Polygon p = roi.getPolygon();
        p.translate(dx, dy);
        f.resetOutLine(new PolygonRoi(p, roi.getType()));
        f.setXpix(x);
        f.setYpix(y);

    }

    /**
     * Randomises the position of all {@link Feature}s in the input list, within
     * the bounds of the {@link ParentFeature}.
     *
     * @param pf
     * @param fretures
     * @param randseed
     * @return
     */
    public static ArrayList<Feature> randomizeFeaturePositions(ParentFeature pf, ArrayList<Feature> fretures, long randseed) {

        ArrayList<Feature> freturesRandomised = duplicateFeatureSet(fretures);

        // Get the region in which we can organise the positions of the features. 
        PolygonRoi featureBounds = pf.getFeatureBoundsShifted();

        // Get the pixel positions of all pixels inside of the 
        Point[] posiblePositions = featureBounds.getContainedPoints();

        int nFeatures = fretures.size();

        Random r = new Random(randseed);

        double xNew, yNew;
        double nRandomPositions = posiblePositions.length;
        int indx;
        Point p;
        Feature f;
        for (int i = 0; i < nFeatures; i++) {
            indx = (int) Math.floor(r.nextDouble() * (nRandomPositions));
            p = posiblePositions[indx];
            // move the centre of the feature.
            f = freturesRandomised.get(i);
            recentreFeature2D(f, p.x, p.y);
        }

        return freturesRandomised;
    }

    /**
     * Extracts the desired numerical property from all of the features.
     *
     * @param features
     * @param keyID
     * @return
     */
    public static ArrayList<Double> getSpecifiedFeatureNumericalPropertyAsArrayList(ArrayList<Feature> features, String keyID) {
        int nFeatures = features.size();
        ArrayList<Double> output = new ArrayList<>(nFeatures);
        for (int i = 0; i < nFeatures; i++) {
            output.add(features.get(i).getNumericFeature(keyID));
        }
        return output;
    }

    /**
     * Extracts the specified numerical property from each of the
     * {@link Feature}s numerical property map in the input array and returns it
     * as a double[] array.
     *
     * This is useful for computing stats etc...
     *
     * @param features
     * @param keyID
     * @return
     */
    public static double[] getSpecifiedFeatureAsArray(ArrayList<Feature> features, String keyID) {

        double[] output = List2Prims.doubleFromDouble(getSpecifiedFeatureNumericalPropertyAsArrayList(features, keyID));

        return output;
    }

}
