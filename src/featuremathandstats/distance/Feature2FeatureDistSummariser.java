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

import featureobjects.ParentFeature;
import gui.CellAnalyserGUIModel;
import ij.IJ;
import ij.gui.Plot;
import ij.measure.ResultsTable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * Summarises the results for all {@link ParentFeature}s from the
 * {@link Feature2FeatureDistCalculator}.
 *
 * @author mqbssep5
 */
public class Feature2FeatureDistSummariser {

    private final CellAnalyserGUIModel model;

    public Feature2FeatureDistSummariser(CellAnalyserGUIModel model) {
        this.model = model;
    }

    /**
     * The input variables are the names used to store the NND histograms in the
     * {@link }
     *
     * @param binNames
     * @param histNames
     * @param randMeanNames
     * @param randStds
     */
    public void summariseHistograms(String[] binNames, String[] histNames, String[] randMeanNames, String[] randStds) {

        int nSets = model.getNImages();

        String[] imageNames = model.getImageNames();

        ArrayList<ParentFeature> parentFeatures;
        ParentFeature pf;

        double[] bins;
        pf = model.getFeatures(imageNames[0]).get(0);
        bins = (double[]) pf.getObjectPropertyValue(binNames[0]);

        // maps to store the original and randomised histograms. 
        LinkedHashMap<String, ArrayList<double[]>> hitosOrg, histoRands;
        hitosOrg = new LinkedHashMap<>();
        histoRands = new LinkedHashMap<>();

        // get all of the historgrams and all of the 
        for (int s = 0; s < nSets; s++) {
            // get all of the features for this data set. 
            parentFeatures = model.getFeatures(imageNames[s]);
            // extract the relevant variables from each of the parent features 
            for (int i = 0; i < parentFeatures.size(); i++) {
                pf = parentFeatures.get(i);
                // extract the desired variables from the parent features feature map.
                for (int f = 0; f < histNames.length; f++) {
                    // give them the same name for now
                    add2Map(hitosOrg, histNames[f], (double[]) pf.getObjectPropertyValue(histNames[f]));
                    add2Map(histoRands, histNames[f], (double[]) pf.getObjectPropertyValue(randMeanNames[f]));
                }
            }
        }

        System.out.println("mapS " + hitosOrg.size() + " mas2 " + histoRands.size());

        Set<String> keys = hitosOrg.keySet();
        Iterator<String> it = keys.iterator();

        ArrayList<double[]> orgSet, randSet;
        String s;
        double[][] meanAndSTDorg, meanAndSTDrands;
        while (it.hasNext()) {
            s = it.next();
            orgSet = hitosOrg.get(s);
            randSet = histoRands.get(s);

            // Save the individual histograms into a results table. 
            saveAllHists2ResultsTable( orgSet, bins, s );
            saveAllHists2ResultsTable( randSet, bins, "_RAND_" + s );            
            
            // compute the mean and standard deviation of all of thease histograms. 
            meanAndSTDorg = Feature2FeatureDistHistogram.computeMeanAndStandardDeviationOfNormalisedHistos(orgSet);
            meanAndSTDrands = Feature2FeatureDistHistogram.computeMeanAndStandardDeviationOfNormalisedHistos(randSet);
//            meanAndSTDorg = Feature2FeatureDistHistogram.computeMeanAndStdevHistogram(orgSet);
//            meanAndSTDrands = Feature2FeatureDistHistogram.computeMeanAndStdevHistogram(randSet);

            //
            Plot p = Feature2FeatureDistHistogram.compareTwoMeanHistogramsWithErrors(bins, meanAndSTDorg[0], meanAndSTDorg[1], meanAndSTDrands[0], meanAndSTDrands[1], "Mean Histograms: " + s, "Original", "Randomised");
            p.show();

            ResultsTable rt = means2ResultsTable(bins, meanAndSTDorg, meanAndSTDrands);
            try {
                rt.save(model.getSaveFileDirectory() + "\\MeanDistHistos_" + s + ".csv");
            } catch (Exception e) {
                IJ.log("Problem saving " + e.getMessage());
            }

        }

    }

    /**
     * Puts the histograms stored in the ArrayList and puts them into a results
     * table.
     *
     * @param histos
     * @param bins
     */
    private void saveAllHists2ResultsTable( ArrayList<double[]> histos, double[] bins, String setID ) {

        ResultsTable rt = new ResultsTable();

        // number of bins in the histogram. 
        int nBins = bins.length;
        int nCols = histos.size();

        // Write the bins to the results table
        for (int i = 0; i < nBins; i++) {
            rt.incrementCounter();
            // Record the bin value in the first column
            rt.addValue("Bin", bins[i]);
            // Then the rest of the values from the same element in each of the 
            // arrays in the histograms. 
            for (int c = 0; c < nCols; c++) {
                rt.addValue("s_" + (c + 1), histos.get(c)[i]);
            }
        }

        try {
            rt.save(model.getSaveFileDirectory() + "\\NNDall_" + setID + ".csv");
        } catch (Exception e) {
            IJ.log("Problem saving " + e.getMessage());
        }

    }

    /**
     * Stores the mean and standard deviation of the histograms, for both
     * original randomised and non-randomised histograms from the nearest
     * neighbour analysis into a ImageJ results table.
     *
     * @param bins
     * @param meanAndSTDorg
     * @param meanAndSTDrands
     * @return
     */
    private ResultsTable means2ResultsTable(double[] bins, double[][] meanAndSTDorg, double[][] meanAndSTDrands) {

        ResultsTable rt = new ResultsTable();
        int n = meanAndSTDorg[0].length;

        double[] meansOrg, stdOrg, meansRand, stdRand;

        meansOrg = meanAndSTDorg[0];
        stdOrg = meanAndSTDorg[1];
        meansRand = meanAndSTDrands[0];
        stdRand = meanAndSTDrands[1];

        for (int i = 0; i < n; i++) {
            rt.incrementCounter();
            rt.addValue("Bin", bins[i]);
            rt.addValue("MeanOrg", meansOrg[i]);
            rt.addValue("STDOrg", stdOrg[i]);
            rt.addValue("MeanRand", meansRand[i]);
            rt.addValue("STDRand", stdRand[i]);
        }

        return rt;

    }

    private void add2Map(Map< String, ArrayList<double[]>> map, String key, double[] value) {
        if (!map.containsKey(key)) {
            map.put(key, new ArrayList<>());
        }
        map.get(key).add(value);
    }

    public void sumarisePFA(String[] pfaStatAndCombinationNames) {
        int nSets = model.getNImages();

        String[] imageNames = model.getImageNames();

        ArrayList<ParentFeature> parentFeatures;
        ParentFeature pf;

        String pfaName;

        ResultsTable rt = new ResultsTable();
        HashMap<String, Double> pfaMap;

        Set<String> names;
        // get all of the historgrams and all of the 
        for (int s = 0; s < nSets; s++) {
            // get all of the features for this data set. 
            parentFeatures = model.getFeatures(imageNames[s]);
            // extract the relevant variables from each of the parent features 
            for (int i = 0; i < parentFeatures.size(); i++) {
                // 
                pf = parentFeatures.get(i);
                // add to the results table. 
                rt.incrementCounter();
                rt.addValue("ImageName", imageNames[s]);
                rt.addValue("Feature No.", (i + 1));
                // extract the desired variables from the parent features feature map.
                for (int f = 0; f < pfaStatAndCombinationNames.length; f++) {
                    // 
                    pfaName = pfaStatAndCombinationNames[f];
                    // give them the same name for now
                    pfaMap = (HashMap<String, Double>) pf.getObjectPropertyValue(pfaName);
                    names = pfaMap.keySet();
                    // add these values to a row in the table
                    for (String sn : names) {
                        rt.addValue(sn, pfaMap.get(sn));
                    }

                }
            }
        }

        try {
            rt.save(model.getSaveFileDirectory() + "\\" + "PFA_analysis.csv");
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

    }

}
