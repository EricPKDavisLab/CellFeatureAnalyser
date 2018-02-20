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
package featuremathandstats.distance;

import arrayutil.List2Prims;
import arrayutil.NumericArrayListOps;
import featureobjects.Feature;
import featureobjects.FeatureOps;
import featureobjects.ParentFeature;
import ij.IJ;
import java.awt.Point;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Set;
import org.apache.commons.math3.stat.StatUtils;
import org.apache.commons.math3.stat.descriptive.rank.Median;
import org.apache.commons.math3.stat.inference.TestUtils;

/**
 * Computes the actual distances between the features across channels.
 *
 * @author mqbssep5
 */
public class Feature2FeatureDistCalculator implements Runnable {

    private final ParentFeature parentFeature;

    private final boolean edgeDistance;

    private final double pixelScale;

    public static String DISTANCE_MAP_CENT = "DISTANCE_MAP_CENT";

    public static String DISTANCE_MAP_EDGES = "DISTANCE_MAP_EDGES";

    public static String D_NN_EDGE = "NN_D_E";

    public static String D_NN_CENT = "NN_D_C";

    /**
     * The numerical ID of the nearest neighbour in the specified channel.
     * Computed using the edge to edge distance.
     */
    public static String NN_ID_EDGE = "NN_E_ID";

    /**
     * The numerical ID of the nearest neighbour in the specified channel.
     * Computed using the centroid distance.
     */
    public static String NN_ID_CENT = "NN_C_ID";

    public static String MED_EXT = "_MED";

    public static String MEAN_EXT = "_MEAN";

    public static String STD_EXT = "_SD";

    public static String FRAC_NN = "_FRAC";

    public static String RAND_EXT = "_RAND";

    public static String HIST = "_HIST";

    public static String HISTBINS = "_HISTBINS";

    public static String PV = "_PV";

    public static String PFA_CLOSE = "_PFA_CLOSE";

    public static String PFA_FAR = "_PFA_FAR";

    public static String PFA_EXT = "_PFA";

    public static String C_EXT = "_c";

    private final String featureName1;

    private final String featureName2;

    private int nRandomisations = 0;

    private final double threshdist;

    private final double hismax;

    private final double binWidth;

    private double[] histoBins;

    private boolean doPFA = false;

    private String[] pfaFeatureNames;

    /**
     * Constructor.
     *
     * By default this class will compute the centroid distance between
     *
     * @param parentFeature
     * @param edgeDistance compute the edge to edge distance between the
     * {@link Feature} this is more computationally expensive.
     * @param pixelScale
     * @param featureName1 the name of the feature set that we will compute the
     * distances from.
     * @param featureName2 the name of the feature set that we will compute the
     * distances from.
     * @param nRandomisations - the number nearest neighbour re-tests with the
     * features in randomised positions
     * @param threshdist
     * @param hismax
     * @param binWidth
     *
     *
     */
    public Feature2FeatureDistCalculator(ParentFeature parentFeature, boolean edgeDistance, double pixelScale, String featureName1, String featureName2, int nRandomisations, double threshdist, double hismax, double binWidth) {
        this.parentFeature = parentFeature;
        this.edgeDistance = edgeDistance;
        this.nRandomisations = nRandomisations;
        this.pixelScale = pixelScale;
        this.featureName1 = featureName1;
        this.featureName2 = featureName2;
        this.threshdist = threshdist;
        this.hismax = hismax;
        this.binWidth = binWidth;
    }

    /**
     * The names of the features we will compare with the proximal feature
     * analysis.
     *
     * @param pfaFeatureNames
     */
    public void setProximalFeatureAnalysisFeatures(String[] pfaFeatureNames) {
        this.pfaFeatureNames = pfaFeatureNames;
        doPFA = true;
    }

    @Override
    public void run() {

        //System.out.println(" dthresh " + threshdist);
        int nchannels = parentFeature.getNchannels();

        ArrayList<Feature> set1, set2;
        String nameSet1, nameSet2;
        double dThresh = threshdist;
        double[] getMeanMedStdSet1, histo;
        String setPairNameCentroidNND, setPairNameEdgeNND;

        // compute the histograms 
        computeHistogramBins();

        // first compute the distances between the features in each of the channels. 
        for (int c1 = 0; c1 < nchannels; c1++) {
            // get the features we are going to compute the distances between. 
            set1 = parentFeature.getFeatures(c1, featureName1);
            for (int c2 = 0; c2 < nchannels; c2++) {
                if (c1 == c2) {
                    continue;
                }
                // the set we will compute the distance from. 
                set2 = parentFeature.getFeatures(c2, featureName2);
                // The names used as keys within the Features numerical property may for the channel to channel distance. 
//                nameSet1 = (C_EXT + (c1 + 1));
//                nameSet2 = (C_EXT + (c2 + 1));
                nameSet1 = ("" + (c1 + 1));
                nameSet2 = ("" + (c2 + 1));
                setPairNameCentroidNND = D_NN_CENT + C_EXT + "_" + nameSet1 + "-" + nameSet2;
                setPairNameEdgeNND = D_NN_EDGE + C_EXT + "_" + nameSet1 + "-" + nameSet2;

                // Compute the nearest neighbour distances between the sets. 
                computePairedDistances(set1, set2, nameSet1, nameSet2);

                // some global NND stats 
                getMeanMedStdSet1 = getMeanMedStd(set1, setPairNameCentroidNND, dThresh);
                parentFeature.addNumericProperty(setPairNameCentroidNND + MEAN_EXT, getMeanMedStdSet1[0]);
                parentFeature.addNumericProperty(setPairNameCentroidNND + MED_EXT, getMeanMedStdSet1[1]);
                parentFeature.addNumericProperty(setPairNameCentroidNND + STD_EXT, getMeanMedStdSet1[2]);
                parentFeature.addNumericProperty(setPairNameCentroidNND + FRAC_NN, getMeanMedStdSet1[3]);

                // now compute the histogram of the nearest neighbours
                histo = computeHistrogram(set1, setPairNameCentroidNND);

                // save the histogram as well
                parentFeature.addObjectProperty(setPairNameCentroidNND + HIST, Arrays.copyOf(histo, histoBins.length));
                parentFeature.addObjectProperty(setPairNameCentroidNND + HISTBINS, Arrays.copyOf(histoBins, histoBins.length));

                if (doPFA) {
                    // proximal feature analysis. 
                    HashMap<String, Double> pfa;
                    pfa = doPFAsetPair(set1, setPairNameCentroidNND, pfaFeatureNames);        
                    // store in the parent feature for saving later. 
                    parentFeature.addObjectProperty(setPairNameCentroidNND + "_" + PFA_EXT, pfa);
                    //
                    if(edgeDistance){
                       // store in the parent feature for saving later. 
                       parentFeature.addObjectProperty(setPairNameEdgeNND + "_" + PFA_EXT, pfa);
                    }
                }

            }
        }

        // stop here 
        if (nRandomisations == 0) {
            return;
        }

        //System.out.println(" started randomisations ");
        HashMap<String, ArrayList<Double>> statMap = new HashMap<String, ArrayList<Double>>();
        HashMap<String, double[][]> histogramValuesMap = new HashMap<String, double[][]>();

        long rand_seed;
        // Perform the same analysis but randomise the features of each set. 
        for (int i = 0; i < nRandomisations; i++) {
            // get all of the feature sets for each channel. 
            for (int c1 = 0; c1 < nchannels; c1++) {
                // the set we will keep fixed. 
                set1 = FeatureOps.duplicateFeatureSet(parentFeature.getFeatures(c1, featureName1));
                // compute distance to features in the other sets. 
                for (int c2 = 0; c2 < nchannels; c2++) {
                    if (c1 == c2) {
                        continue;
                    }
                    // random seed number for the random number generation. 
                    rand_seed = System.currentTimeMillis();
                    set2 = FeatureOps.randomizeFeaturePositions(parentFeature, parentFeature.getFeatures(c2, featureName2), rand_seed);

                    // The names used as keys within the Features numerical property may for the channel to channel distance. 
//                nameSet1 = (C_EXT + (c1 + 1));
//                nameSet2 = (C_EXT + (c2 + 1));
                    nameSet1 = ("" + (c1 + 1));
                    nameSet2 = ("" + (c2 + 1));
                    setPairNameCentroidNND = D_NN_CENT + C_EXT + "_" + nameSet1 + "-" + nameSet2;
                    setPairNameEdgeNND = D_NN_EDGE + C_EXT + "_" + nameSet1 + "-" + nameSet2;
                    
                    // compute the distance between the 
                    computePairedDistances(set1, set2, nameSet1, nameSet2);

                    // now compute the histogram of the nearest neighbours
                    histo = computeHistrogram(set1, setPairNameCentroidNND);
                    // save the histogram for this current iteration. 
                    addHistogram2map(histogramValuesMap, histo, setPairNameCentroidNND, i);

                    // Do some NND global stats
                    getMeanMedStdSet1 = getMeanMedStd(set1, setPairNameCentroidNND, dThresh);
                    addValueToStatMap(statMap, setPairNameCentroidNND + MEAN_EXT, getMeanMedStdSet1[0]);
                    addValueToStatMap(statMap, setPairNameCentroidNND + MED_EXT, getMeanMedStdSet1[1]);
                    addValueToStatMap(statMap, setPairNameCentroidNND + STD_EXT, getMeanMedStdSet1[2]);
                    addValueToStatMap(statMap, setPairNameCentroidNND + FRAC_NN, getMeanMedStdSet1[3]);
                }
            }
        }

        // now compute some stats for the randomised values
        Set<String> keys = statMap.keySet();
        double meanMean, pMean;
        double[] values;
        ArrayList<Double> vs;

        //System.out.println(" sitsize " + keys.size());
        for (String s : keys) {
            // walues for this 
            vs = statMap.get(s);
            // extract the values 
            values = List2Prims.doubleFromDouble(vs);
            meanMean = StatUtils.mean(values);
            pMean = TestUtils.tTest(parentFeature.getNumericPropertyValue(s), values);
            // store the values
            parentFeature.addNumericProperty(s + RAND_EXT, meanMean);
            parentFeature.addNumericProperty(s + RAND_EXT + PV, pMean);
        }

        // Compute the mean and standard deviation of the randomised histograms
        // and save in the parent features object map for saving later on. 
        Set<String> histoKeys = histogramValuesMap.keySet();
        double[][] binsMeanStdevs;
        for (String s : histoKeys) {
            // histograms for this
            double[][] histos = histogramValuesMap.get(s);
            binsMeanStdevs = computeMeanAndStdevHistogram(histos);
            // save the values 
            parentFeature.addObjectProperty(s + HIST + MEAN_EXT + RAND_EXT, Arrays.copyOf(binsMeanStdevs[1], binsMeanStdevs[1].length));
            parentFeature.addObjectProperty(s + HIST + STD_EXT + RAND_EXT, Arrays.copyOf(binsMeanStdevs[2], binsMeanStdevs[2].length));
        }

    }

    /**
     * Computes the histogram maxima bins. e.g. bin 0 = binWidth, which
     * represents the interval [0,binWidth).
     */
    private void computeHistogramBins() {
        int nBins = (int) Math.ceil(hismax / binWidth);
        histoBins = new double[nBins];
        for (int i = 0; i < nBins; i++) {
            histoBins[i] = binWidth * (double) i + binWidth;
        }
    }

    /**
     *
     * @param featureSet the set of features
     * @param nndDistStatKey the key ID for nearest neighbour distance property
     * under which the nearest neighbour distance is stored.
     * @param propertyNames the names of the {@link Feature} properties that
     * will be extracted
     * @return
     */
    private HashMap< String, Double> doPFAsetPair( ArrayList<Feature> featureSet, String nndDistStatKey, String[] propertyNames ) {

        // Break into two sets based on the NND. 
        ArrayList<Feature> closeSet, farSet;
        closeSet = FeatureOps.getFeaturesBasedOnNumericalPropertyValue(featureSet, nndDistStatKey, threshdist, FeatureOps.KEEP_LESS_THAN);
        farSet = FeatureOps.getFeaturesBasedOnNumericalPropertyValue(featureSet, nndDistStatKey, threshdist, FeatureOps.KEEP_GREATER_THAN);
        // Now compute the mean and standard deviation of the specified feature
        // properties for the two sets. 
        HashMap<String, Double> output = new HashMap<>();

        //System.out.println(" n close " + closeSet.size() + " n far " + farSet.size() + " total " + set1.size() + " near+far " + (closeSet.size() + farSet.size()) );
        double[] close_data, far_data;
        double mean_close, mean_far, std_close, std_far;
        // String names that will be associated with the feature statistic. 
        String close_mean_name, far_mean_name, close_std_name, far_std_name;

        String currentProperty;
        for (int i = 0; i < propertyNames.length; i++) {
            // the name of the property will will be comparing between the two sets. 
            currentProperty = propertyNames[i];

            // get the data from both sets. 
            close_data = FeatureOps.getSpecifiedFeatureAsArray(closeSet, currentProperty);
            far_data = FeatureOps.getSpecifiedFeatureAsArray(farSet, currentProperty);

            // compute the stats 
            mean_close = StatUtils.mean(close_data);
            std_close = Math.sqrt(StatUtils.variance(close_data));

            mean_far = StatUtils.mean(far_data);
            std_far = Math.sqrt(StatUtils.variance(far_data));

            // Names used to store the info.  
            close_mean_name = nndDistStatKey + currentProperty + MEAN_EXT + PFA_CLOSE;
            close_std_name = nndDistStatKey + currentProperty + STD_EXT + PFA_CLOSE;
            far_mean_name = nndDistStatKey + currentProperty + MEAN_EXT + PFA_FAR;
            far_std_name = nndDistStatKey + currentProperty + STD_EXT + PFA_FAR;

            // Store the stats in the map. 
            output.put(close_mean_name, mean_close);
            output.put(close_std_name, std_close);
            output.put(far_mean_name, mean_far);
            output.put(far_std_name, std_far);
        }

        return output;
    }

    /**
     * Adds the value to the HashMap<String, ArrayList<Double>>. It initialises
     * the ArrayList<Double> if there hasnt already been one assigned for this
     * key.
     *
     * @param map
     * @param key
     * @param value
     */
    private void addValueToStatMap(HashMap<String, ArrayList<Double>> map, String key, Double value) {
        if (!map.containsKey(key)) {
            map.put(key, new ArrayList<Double>());
        }
        map.get(key).add(value);
    }

    /**
     * Adds the histogram to the array of histograms for this set combination.
     *
     * @param map
     * @param histValues
     * @param key
     * @param iteration
     */
    private void addHistogram2map(HashMap<String, double[][]> map, double[] histValues, String key, int iteration) {
        if (!map.containsKey(key)) {
            map.put(key, new double[nRandomisations][]);
        }
        double[][] values = map.get(key);
        values[iteration] = histValues;
    }

    /**
     * Computes the mean, median and standard deviation of the numerical feature
     * stored within the Features numerical feature map associated with the
     * specified key ID.
     */
    private double[] getMeanMedStd(ArrayList<Feature> freature, String keyID, double dthresh) {

        ArrayList<Double> values = FeatureOps.getSpecifiedFeatureNumericalPropertyAsArrayList(freature, keyID);
        // threshold the data. 
        NumericArrayListOps.removeElementsGreaterThan(values, dthresh);
        double[] d2 = List2Prims.doubleFromDouble(values);

        if (d2.length == 0) {
            return new double[]{dthresh, dthresh, Double.POSITIVE_INFINITY, 0d};
        }

        double mean, median, std, ratioNeighbours;
        mean = StatUtils.mean(d2);
        Median med = new Median();
        median = med.evaluate(d2);
        std = Math.sqrt(StatUtils.variance(d2));
        // the proportion of the set in close proximity to features in the second set. 
        ratioNeighbours = ((double) d2.length) / (double) freature.size();
        return new double[]{mean, median, std, ratioNeighbours};
    }

    /**
     * Computes the mean and standard deviation of the histogram.
     *
     * @param histo
     * @return
     */
    private double[][] computeMeanAndStdevHistogram(double[][] histo) {

        int nBins = histo[0].length;
        double[] means, stdevs, bins;
        means = new double[nBins];
        stdevs = new double[nBins];
        bins = new double[nBins];

        int nhistos = histo.length;
        double[] binValues = new double[nhistos];
        double m, sd;
        for (int i = 0; i < nBins; i++) {
            // extract all the values for the current bin. 
            for (int j = 0; j < nhistos; j++) {
                binValues[j] = histo[j][i];
            }
            // compute the mean and standard deviation. 
            m = StatUtils.mean(binValues);
            sd = Math.sqrt(StatUtils.variance(binValues, m));
            means[i] = m;
            stdevs[i] = sd;
            // record the bins
            bins[i] = binWidth * (double) i + binWidth;
        }

        return new double[][]{bins, means, stdevs};
    }

    /**
     * Compute the histogram for the specified property.
     *
     * @param freature
     * @param keyID
     * @return
     */
    private double[] computeHistrogram(ArrayList<Feature> freature, String keyID) {

        int nBins = (int) Math.ceil(hismax / binWidth);
        ArrayList<Double> values = FeatureOps.getSpecifiedFeatureNumericalPropertyAsArrayList(freature, keyID);
        double[] d1 = List2Prims.doubleFromDouble(values);

        double[] hist = new double[nBins];

        int bin;
        for (int i = 0; i < d1.length; i++) {
            bin = (int) Math.floor((double) nBins * (d1[i] / hismax));// 
            bin = Math.min(nBins - 1, bin);
            hist[bin]++;
        }
        return hist;
    }

    /**
     * Computes the distance of all the points in Set1 to all the points in
     * Set2.
     *
     * If a spot A in set 1 has one or more spots in set 2 within a radial
     * distance of 'dThresh' then these spots are saved internally in Spot A's
     * property map.
     *
     * A TreeMap<Double, Spot> distMap is used to save the spots in that are
     * within the proximity of each spot, where the key is the distance between
     * the spots and Spot a reference to the neighbouring spot.
     *
     * @param spotsCH1 the reference spot set
     * @param spotsCH2 the set of candidate spot
     * @param chRef a string name of this set (e.g. CH1) to be concatenated with
     * {@link DISTANCE_MAP}
     * @param chCand a string name for this set (e.g. CH1) to be concatenated
     * with {@link DISTANCE_MAP}
     */
    private void computePairedDistances(ArrayList<Feature> spotsCH1, ArrayList<Feature> spotsCH2, String chRef, String chCand) {

        double dist;
        //double distThresh2 = dThresh * dThresh;
        // for each spot compute distance from all other spots in the other set. 
        // If the distance is less than the threshold distance then record it in 
        // a distance map. 
        // positions of pixels inside the points
        Point[] innerPointsRef, innerPointsCand;

        //
        double[] distanceAndOverlap;
        double dEdge, dEdgeMin, dMin;
        int idEdgeClosest, idCentClosest;
        int nRefSpots = spotsCH1.size();
        int loopCounter = 0;

        for (Feature refSpot : spotsCH1) {

            // get the pixel locations inside of this polygon
            innerPointsRef = refSpot.getOutLine().getContainedPoints();

            // closest neighbour values. 
            dEdgeMin = Double.POSITIVE_INFINITY;
            dMin = Double.POSITIVE_INFINITY;

            // the IDs of the closest spots. Set to -1 to start with indicating no spot assigned yet. 
            idEdgeClosest = -1;
            idCentClosest = -1;

            double refX, refY;
//            refX = refSpot.getNumericFeature(LoGSpotDetector2D.COM_X);
//            refY = refSpot.getNumericFeature(LoGSpotDetector2D.COM_Y);

            refX = refSpot.getXpix() * pixelScale;
            refY = refSpot.getYpix() * pixelScale;
            for (Feature candSpot : spotsCH2) {

                // compute the distance
                dist = Math.sqrt(square(refX - (candSpot.getXpix() * pixelScale)) + square(refY - (candSpot.getYpix() * pixelScale)));

                if (edgeDistance) {
                    // get the pixel locations inside of this polygon
                    innerPointsCand = candSpot.getOutLine().getContainedPoints();
                    // compute the distance between the edge points. 
                    distanceAndOverlap = closestEdgeDistance(innerPointsRef, innerPointsCand, pixelScale);
                    dEdge = distanceAndOverlap[0];
                    // update the min distance stats and ID of the closest spot. 
                    if (dEdge < dEdgeMin) {
                        dEdgeMin = dEdge;
                        idEdgeClosest = candSpot.getID();
                    }
                }
                // update the min distance. 
                if (dist < dMin) {
                    dMin = dist;
                    idCentClosest = candSpot.getID();
                }

            }

            if (edgeDistance) {
                // Also save the NN stats
                refSpot.addNumericFeature(D_NN_EDGE + C_EXT + "_" + chRef + "-" + chCand, dEdgeMin);
                refSpot.addNumericFeature(NN_ID_EDGE + C_EXT + "_" + chRef + "-" + chCand, (double) idEdgeClosest); 
            }

            refSpot.addNumericFeature(D_NN_CENT + C_EXT + "_" + chRef + "-" + chCand, dMin);
            refSpot.addNumericFeature(NN_ID_CENT + C_EXT + "_" + chRef + "-" + chCand, (double) idCentClosest);

            loopCounter++;
            IJ.showStatus("Performing distance calculations from " + chRef + " to " + chCand);
            IJ.showProgress(loopCounter, nRefSpots - 1);

        }

    }

    /**
     * Does two things
     *
     * 1) Computes the distance of all points in one set to all of the points in
     * the next set. The idea is that we find the distance between the points
     * which are closest to each other (Euclidean distance).
     *
     * 2) Finds the number of points between the sets which have the same
     * position. e.g. the number of overlapping positions.
     *
     * 3) Computes the percentage of points in set 1 that overlap with the
     * points in set 2.
     *
     *
     * @param points1
     * @param points2
     * @return a double[]{d_min,nOverlap,p_overlap}
     */
    private double[] closestEdgeDistance(Point[] points1, Point[] points2, double pixelScale) {

        double dMin = Double.POSITIVE_INFINITY;
        double d = dMin;

        int n1, n2;
        n1 = points1.length;
        n2 = points2.length;
        int x1, y1, x2, y2;
        int nOverlap = 0;
        // compute the distance between each point in 
        for (int i = 0; i < n1; i++) {
            // 
            x1 = points1[i].x;
            y1 = points1[i].y;
            for (int j = 0; j < n2; j++) {
                //
                x2 = points2[j].x;
                y2 = points2[j].y;
                // compute the square distance 
                d = square(x1 - x2) + square(y1 - y2);
                // updates the min distance between points
                if (d < dMin) {
                    dMin = d;
                }
                // count the number of overlapping positions (check this way to avoid rounding errors with doubles.)
                if ((x1 == x2) && (y1 == y2)) {
                    nOverlap++;
                }
            }
        }
        // units of distance and not pixels.
        dMin = Math.sqrt(dMin) * pixelScale;

        // compute the percentage of points of points that overlap. 
        double p = 100d * (double) nOverlap / (double) n1;

        return new double[]{dMin, (double) nOverlap, p};

    }

    /**
     * Returns the square of x.
     *
     * @param x
     * @return the square of x.
     */
    private double square(double x) {
        return x * x;
    }

}
