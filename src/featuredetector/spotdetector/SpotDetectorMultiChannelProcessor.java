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
package featuredetector.spotdetector;

import abstractprocessors.AbstractParentFeatureProcessor;
import featuremathandstats.distance.Feature2FeatureDistanceProcessor;
import featureobjects.Feature;
import featureobjects.FeatureOps;
import featureobjects.ParentFeature;
import gui.CellAnalyserGUIModel;
import gui.GUI;
import ij.IJ;
import ij.ImagePlus;
import ij.gui.PolygonRoi;
import ij.measure.ResultsTable;
import ij.plugin.ChannelSplitter;
import io.FeaturesAndParentFeaturesToResultsTable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import org.apache.commons.math3.stat.StatUtils;
import org.apache.commons.math3.stat.descriptive.rank.Median;
import rapidsimpleguibuilder.FastJComponents;
import rapidsimpleguibuilder.RapidBuilderJPanel2Column;

/**
 * Performs spot detection in each channel.
 *
 * @author mqbssep5
 */
public class SpotDetectorMultiChannelProcessor extends AbstractParentFeatureProcessor implements ActionListener {

    public static String NAME = "Spot detection";

    private JTabbedPane tabbedPane;

    private SpotDetectorChannelProcessor[] channelDetectors;

    private ImagePlus imp;

    private ArrayList<ParentFeature> features;

    private int nChannels;

    private JButton jbtPreview;

    public static String MEAN_EXT = "_MEAN";

    public static String MED_EXT = "_MED";

    public static String STD_EXT = "_STD";

    public static String MIN_EXT = "_MIN";

    public static String MAX_EXT = "_MAX";
    
    public static String SUM_EXT = "_SUM";    

    public static String SPOT_DENSITY = "SPOT_DENSITY";

    public static String NSPOTS = "NSPOTS";

    
    public SpotDetectorMultiChannelProcessor( CellAnalyserGUIModel model ) {
        super(model);
    }

    @Override
    public JPanel getSettingsPanel() {

        GUI gc = new GUI();

        FastJComponents fc = new FastJComponents(GUI.TEXT_SIZE_STANDARD, this, null);
        
        nChannels = model.getNchannels();

        // Tabbed pane containing a tabb for each image channel. 
        tabbedPane = new JTabbedPane(JTabbedPane.TOP);

        //tabbedPane.setPreferredSize(d1);
        //tabbedPane.setSize(d1);

        channelDetectors = new SpotDetectorChannelProcessor[nChannels];
        // initialise the detectors and get the panels for each 
        for (int i = 0; i < nChannels; i++) {
            channelDetectors[i] = new SpotDetectorChannelProcessor(i);
            tabbedPane.addTab("Ch" + (i + 1), channelDetectors[i].getSettingsPanel());
        }

        // add the preview button to the bottom of the JPanel. 
        jbtPreview = fc.jButton("Preview");
//        JPanelSpeedyMultiColBased pan = new JPanelSpeedyMultiColBased();
//        // add the tabbed pane and the 
//        pan.addComponentDoubleColumnToEndRow(tabbedPane);
//        pan.addTwoComponentsToEndRow(jbtPreview, blank);

        int width = (int)tabbedPane.getPreferredSize().width + 2*GUI.GUI_INSET_X;


        JPanel pan = new JPanel();
        RapidBuilderJPanel2Column builder = new RapidBuilderJPanel2Column(pan,  width, GUI.GUI_HEIGHT, GUI.SMALL_COMPONENT_HEIGHT, GUI.GUI_INSET_X, GUI.GUI_INSET_Y);
        builder.addComponentDoubleColumnCustomHeight(tabbedPane);
        builder.packComponents();
        JPanel pan2 = new JPanel();
        builder = new RapidBuilderJPanel2Column(pan2, width, GUI.GUI_HEIGHT/10, GUI.SMALL_COMPONENT_HEIGHT, GUI.GUI_INSET_X, GUI.GUI_INSET_Y);
        builder.addTwoComponentsToRow(fc.blankSpace(),jbtPreview);  
        builder.packComponents();
        // combine the panels
        JPanel pan3 = new JPanel(); 
        builder = new RapidBuilderJPanel2Column(pan3, width, GUI.GUI_HEIGHT, GUI.SMALL_COMPONENT_HEIGHT, GUI.GUI_INSET_X, GUI.GUI_INSET_Y);
        builder.addComponentDoubleColumnCustomHeight(pan);
        builder.addComponentDoubleColumnCustomHeight(pan2);
        //builder.addSingleComponentRHS(jbtPreview);
        //pan.setPreferredSize(new Dimension((int) Math.round(0.9 * (double) GUI.GUI_WIDTH), (int) Math.round(1d * (double) GUI.GUI_HEIGHT)));
        builder.packComponents();
        return pan3;
    }

    @Override
    public void doProcess() {

        // Get the data and the parent features for this set. 
        imp = model.getCurrentImageDataSet();
        features = model.getCurrentImageSetParentFeatures();

        ImagePlus[] impChannels = ChannelSplitter.split(imp);

        // process each channel 
        for (int i = 0; i < nChannels; i++) {
            channelDetectors[i].setData(impChannels[i], features, i);
            channelDetectors[i].run();
            model.updateOverlay();
        }

    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public void actionPerformed(ActionEvent ae) {
        // this will run a preview without doing the full series of processes. 
        if (ae.getSource() == jbtPreview) {
            doProcess();
            int selectedPanel = tabbedPane.getSelectedIndex();
            imp.setC(selectedPanel + 1);
            IJ.resetMinAndMax(imp);
        }
    }

    @Override
    public void summariseAndSave() {

        // The name of the list of features we want to save for each channel. 
        String featureNames = SpotDetectorChannelProcessor.SPOT_FEATURE_NAME;
        // The names of the things we want to save for each of the features 
        String[] detectedFeatureIDs = SpotDetectorChannelProcessor.SPOT_FEATURE_NUMERIC_PROPERTY_NAMES;

        // Check if we have used Feature2FeatureDistanceProcessor in the analysis chain. 
        // If so we will want to store the NND from this. 
        if (model.getFeatureProcessorNames().contains(Feature2FeatureDistanceProcessor.NAME)) {
            ArrayList<String> temp = new ArrayList<>();
            for (String s : detectedFeatureIDs) {
                temp.add(s);
            }
            // Add the NND and overlap. 

        }

        ResultsTable rt = FeaturesAndParentFeaturesToResultsTable.parentFeatureSpecifiedFeatureList2stats2ResultsTable(model, featureNames, detectedFeatureIDs);
        rt.show("Spot features");

        try {
            rt.save(model.getSaveFileDirectory() + "\\" + "Spot_features.csv");
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        ResultsTable rt2 = computeSpotStatsForAllParentFeaturesToResultsTable(model, featureNames, detectedFeatureIDs);
        rt2.show("Spot features stats");

        try {
            rt2.save(model.getSaveFileDirectory() + "\\" + "Spot_features_stats.csv");
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

    }

    /**
     * A method which computes the following statistics: mean, standard
     * deviation, median, min, and max. For the spot features in each of the
     * {@link ParentFeature}s
     *
     * @param model
     * @param featureListID the name of the list used to store a specific set of
     * features.
     * @param featureIDs the names of the numerical attributes stored within the
     * {@link Feature}s. e.g. COM_X, COM_Y etc...
     * @return
     */
    public static ResultsTable computeSpotStatsForAllParentFeaturesToResultsTable(CellAnalyserGUIModel model, String featureListID, String[] featureIDs) {

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
        double mean, stdev, median, min, max, totalArea, pixelSize, density, nspots, sum;
        String sid;
        PolygonRoi proi;
        Median m = new Median();
        // for all image data sets. 
        for (int s = 0; s < nSets; s++) {
            // get all of the features for this data set. 
            parentFeatures = model.getFeatures(imageNames[s]);

            // extract the relevant variables from each of the parent features 
            for (int i = 0; i < parentFeatures.size(); i++) {
                // get the current feature. 
                pf = parentFeatures.get(i);
                proi = pf.getFeatureBoundsOriginalImage();
                pixelSize = pf.getPixelXYsize();
                totalArea = proi.getContainedPoints().length * (pixelSize * pixelSize);
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
                        values = FeatureOps.getSpecifiedFeatureAsArray(featuresList, sid);
                        // extract the desired variables from the features feature map.
                        // compute the stats
                        mean = StatUtils.mean(values);
                        stdev = Math.sqrt(StatUtils.variance(values));
                        median = m.evaluate(values);
                        min = StatUtils.min(values);
                        max = StatUtils.max(values);
                        sum = StatUtils.sum(values);
                        // add to the table. 
                        rt.addValue(sid + MEAN_EXT, mean);
                        rt.addValue(sid + STD_EXT, stdev);
                        rt.addValue(sid + MED_EXT, median);
                        rt.addValue(sid + MIN_EXT, min);
                        rt.addValue(sid + MAX_EXT, max);
                        rt.addValue(sid + SUM_EXT, max);
                    }
                    // number of detected spots/ 
                    nspots = (double) featuresList.size(); //
                    rt.addValue(NSPOTS, nspots);
                    // compute the density of features for this channel. 
                    density = nspots / totalArea;
                    // save the density. 
                    rt.addValue(SPOT_DENSITY, density);
                }
            }
        }

        return rt;
    }

}
