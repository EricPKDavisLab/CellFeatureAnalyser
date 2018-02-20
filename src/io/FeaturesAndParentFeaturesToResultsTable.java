/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io;

import featureobjects.Feature;
import featureobjects.FeatureOps;
import featureobjects.ParentFeature;
import gui.CellAnalyserGUIModel;
import ij.measure.ResultsTable;
import java.util.ArrayList;
import org.apache.commons.math3.stat.StatUtils;
import org.apache.commons.math3.stat.descriptive.rank.Median;

/**
 * A class containing static methods for exporting various data stored within
 * the list of parent feature models.
 *
 * @author mqbssep5
 */
public class FeaturesAndParentFeaturesToResultsTable {

    public static String MEAN_EXT = "_MEAN";

    public static String MED_EXT = "_MED";

    public static String STD_EXT = "_STD";

    public static String MIN_EXT = "_MIN";

    public static String MAX_EXT = "_MAX";

    /**
     * Extracts the list of {@link Feature}s with the specified featureListID
     * stored in each channel of the {@link ParentFeature}s Map of feature
     * lists. It then saves all of the numerical values stored for each of the
     * string featureIDs within the {@link Feature}s.
     *
     * @param model
     * @param featureListID the name of the list used to store a specific set of
     * features.
     * @param featureIDs the names of the numerical attributes stored within the
     * {@link Feature}s. e.g. COM_X, COM_Y etc...
     * @return
     */
    public static ResultsTable parentFeatureSpecifiedFeatureList2stats2ResultsTable(CellAnalyserGUIModel model, String featureListID, String[] featureIDs) {

        // number of image data sets. 
        int nSets = model.getNImages();
        // names of all of the image data sets. 
        String[] imageNames = model.getImageNames();
        // list of the parent features found in each data set. 
        ArrayList<ParentFeature> parentFeatures;
        // list of features 
        ArrayList<Feature> featuresList;

        ParentFeature pf;
        // results table we will be writing to. 
        ResultsTable rt = new ResultsTable();

        int nfeatureIDs = featureIDs.length;
        int nchannels = model.getNchannels();

        // for all image data sets. 
        for (int s = 0; s < nSets; s++) {
            // get all of the features for this data set. 
            parentFeatures = model.getFeatures(imageNames[s]);
            // extract the relevant variables from each of the parent features 
            for (int i = 0; i < parentFeatures.size(); i++) {
                // get the current feature. 
                pf = parentFeatures.get(i);
                // get the features stored in each channel. 
                for (int c = 0; c < nchannels; c++) {
                    // list of features for this channel. 
                    featuresList = pf.getFeatures(c, featureListID);
                    if (featuresList == null) {
                        // Detection of features was not performed in this channel. 
                        continue;
                    }
                    // all features in the list. 
                    for (Feature f : featuresList) {
                        // extract the desired variables from the parent features feature map.
                        // add to the table.
                        rt.incrementCounter();
                        rt.addValue("Image", imageNames[s]);
                        rt.addValue("Feature", (i + 1));
                        rt.addValue("Channel", (c + 1));
                        for (int fid = 0; fid < nfeatureIDs; fid++) {
                            //System.out.println(" " + featureIDs[fid]);
                            rt.addValue(featureIDs[fid], f.getNumericFeature(featureIDs[fid]));
                        }
                    }
                }
            }
        }

        return rt;
    }

    /**
     * TODO: put the comments on this.
     *
     * @param model
     * @param featureListID the name of the list used to store a specific set of
     * features.
     * @param featureIDs the names of the numerical attributes stored within the
     * {@link Feature}s. e.g. COM_X, COM_Y etc...
     * @return
     */
    public static ResultsTable parentFeatureSpecifiedFeatureList2ResultsTable(CellAnalyserGUIModel model, String featureListID, String[] featureIDs) {

        // number of image data sets. 
        int nSets = model.getNImages();
        // names of all of the image data sets. 
        String[] imageNames = model.getImageNames();
        // list of the parent features found in each data set. 
        ArrayList<ParentFeature> parentFeatures;
        // list of features 
        ArrayList<Feature> featuresList;

        ParentFeature pf;
        // results table we will be writing to. 
        ResultsTable rt = new ResultsTable();

        int nfeatureIDs = featureIDs.length;
        int nchannels = model.getNchannels();

        double[] values;
        double mean, stdev, median, min, max;
        String sid;
        // for all image data sets. 
        for (int s = 0; s < nSets; s++) {
            // get all of the features for this data set. 
            parentFeatures = model.getFeatures(imageNames[s]);
            // extract the relevant variables from each of the parent features 
            for (int i = 0; i < parentFeatures.size(); i++) {
                // get the current feature. 
                pf = parentFeatures.get(i);
                // get the features stored in each channel. 
                for (int c = 0; c < nchannels; c++) {
                    // list of features for this channel. 
                    featuresList = pf.getFeatures(c, featureListID);
                    if (featuresList == null) {
                        // Detection of features was not performed in this channel. 
                        continue;
                    }
                    // compute all the stats for this feature in this channel. 
                    rt.incrementCounter();
                    rt.addValue("Image", imageNames[s]);
                    rt.addValue("Feature", (i + 1));
                    rt.addValue("Channel", (c + 1));
                  
                    for (int fid = 0; fid < nfeatureIDs; fid++) {
                        sid = featureIDs[fid];
                        values = FeatureOps.getSpecifiedFeatureAsArray(featuresList, sid );
                        // extract the desired variables from the features feature map.
                        // compute the stats
                        mean = StatUtils.mean(values);
                        stdev = Math.sqrt(StatUtils.variance(values));
                        Median m = new Median();
                        median = m.evaluate(values);
                        min = StatUtils.min(values);
                        max = StatUtils.max(values);
                        // add to the table. 
                        rt.addValue(sid+MEAN_EXT, mean);
                        rt.addValue(sid+STD_EXT, stdev);
                        rt.addValue(sid+MED_EXT, median);       
                        rt.addValue(sid+MIN_EXT, min); 
                        rt.addValue(sid+MAX_EXT, max);                         
                    }

                }
            }
        }

        return rt;
    }

    /**
     * Returns an ImageJ results table which tabulates the specified list of
     * numerical features saved in the {@link ParentFeature}s numerical property
     * map.
     *
     * @param model data model containing the list of features
     * @param featureIDs the string key IDs used to name specific features
     * stored in the {@link ParentFeature}s numerical property map.
     * @return The table containing the results.
     */
    public static ResultsTable parentFeatureSpecifiedNumericalProperties2ResultsTable(CellAnalyserGUIModel model, String[] featureIDs) {

        // number of image data sets. 
        int nSets = model.getNImages();

        // names of all of the image data sets. 
        String[] imageNames = model.getImageNames();
        // list of the parent features found in each data set. 
        ArrayList<ParentFeature> parentFeatures;

        ParentFeature pf;
        // results table we will be writing to. 
        ResultsTable rt = new ResultsTable();

        int nfeatureIDs = featureIDs.length;
        for (int s = 0; s < nSets; s++) {
            // get all of the features for this data set. 
            parentFeatures = model.getFeatures(imageNames[s]);
            // extract the relevant variables from each of the parent features 
            for (int i = 0; i < parentFeatures.size(); i++) {
                // get the current feature. 
                pf = parentFeatures.get(i);
                rt.incrementCounter();
                rt.addValue("Image", imageNames[s]);
                rt.addValue("Feature", (i + 1));
                // extract the desired variables from the parent features feature map.
                for (int f = 0; f < nfeatureIDs; f++) {
                    rt.addValue(featureIDs[f], pf.getNumericPropertyValue(featureIDs[f]));
                }
            }
        }

        return rt;
    }

}
