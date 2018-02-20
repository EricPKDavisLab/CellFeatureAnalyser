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

import abstractprocessors.AbstractParentFeatureProcessor;
import featuredetector.spotdetector.SpotDetectorChannelProcessor;
import featuredetector.spotdetector.SpotDetectorCommon;
import static featuremathandstats.distance.Feature2FeatureDistCalculator.D_NN_CENT;
import static featuremathandstats.distance.Feature2FeatureDistCalculator.D_NN_EDGE;
import static featuremathandstats.distance.Feature2FeatureDistCalculator.FRAC_NN;
import static featuremathandstats.distance.Feature2FeatureDistCalculator.HIST;
import static featuremathandstats.distance.Feature2FeatureDistCalculator.MEAN_EXT;
import static featuremathandstats.distance.Feature2FeatureDistCalculator.MED_EXT;
import static featuremathandstats.distance.Feature2FeatureDistCalculator.PFA_EXT;
import static featuremathandstats.distance.Feature2FeatureDistCalculator.RAND_EXT;
import static featuremathandstats.distance.Feature2FeatureDistCalculator.STD_EXT;
import static featuremathandstats.distance.Feature2FeatureDistCalculator.C_EXT;// 
import static featuremathandstats.distance.Feature2FeatureDistCalculator.PV;
import featureobjects.ParentFeature;
import gui.CellAnalyserGUIModel;
import gui.GUI;
import gui.JPanelSpeedy2ColBased;
import gui.TextFieldGetTextOps;
import ij.measure.ResultsTable;
import io.FeaturesAndParentFeaturesToResultsTable;
import java.util.ArrayList;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import rapidsimpleguibuilder.FastJComponents;
import rapidsimpleguibuilder.RapidBuilderJPanel2Column;

/**
 * The processor class used to compute the distance between features across channels.
 * 
 * This class will require that {@link Features} have already been detected by a 
 * detector in a previous stage. 
 * 
 * @author mqbssep5
 */
public class Feature2FeatureDistanceProcessor extends AbstractParentFeatureProcessor {

    private JPanelSpeedy2ColBased panel;
    
    private GUI gc;
    
    private FastJComponents fc;
    
    private JCheckBox chEdgeDistance, chDoPFA;//chUseCutOff,
    
    //private Dimension hsiz = GUI.HALF_WIDTH_DIM;    
    
    public static String NAME = "Feature Dist";
    
    private double threshdist = 0.1d; 
    
    private double maxHistDist = 2.0d, binwidth = 0.1d;
    
    private JTextField jtfThreshDist, jtfDoRandomisaion, jtfBinWidth, jtfMaxDistance;
    
    private boolean edge2edgeDistance = true, doPfaAnalysis = true;
    
    private int nfeatures;
    
    private int nrandomisations = 3;
    
    private int referenceChannel = 0;
    
    private ArrayList<ParentFeature> parentFeatures;
    
//    private JComboBox jComboChannel;
    
    private int nchannels;
    
    public Feature2FeatureDistanceProcessor(CellAnalyserGUIModel model) {
        super(model);
    }

    @Override
    public JPanel getSettingsPanel() {
        
        panel = new JPanelSpeedy2ColBased();
        
        gc = new GUI();
        
        fc = new FastJComponents(GUI.TEXT_SIZE_STANDARD, null, null);
        
        // Distance metric options
        //cbEuclideanDist = gc.jCheckBox("Centroid", hsiz);
        chEdgeDistance = fc.jCheckBox("Edge to edge");
        chEdgeDistance.setSelected(edge2edgeDistance);
        chEdgeDistance.setEnabled(false);
        
        // for comparing the properties of features that are close or far away from each other.
        chDoPFA = fc.jCheckBox("Do PFA");
        chDoPFA.setSelected(doPfaAnalysis);
        
        //chUseCutOff = gc.jCheckBox("Use cut off distance", hsiz);
        
        nchannels = model.getNchannels();
        String[] channels = new String[nchannels];
        for(int i = 0; i < nchannels; i++){
            channels[i] = ""+(i+1);
        }
        
        //cbEuclideanDist.setSelected(true);
//        jComboChannel = gc.jComboBox(channels, hsiz);
        
        // text feild for cut off distance. 
        jtfThreshDist = fc.jTextFeild(""+threshdist);
        jtfDoRandomisaion = fc.jTextFeild(""+nrandomisations);
        
        jtfBinWidth = fc.jTextFeild(""+binwidth);
        jtfMaxDistance = fc.jTextFeild(""+maxHistDist);
        
//        panel.addComponentDoubleColumn( gc.jLabel("Additional distance metrics", GUIcomponents.HALF_WIDTH_DIM) );
//        //panel.addSingleComponentRHS(cbEuclideanDist);
//        panel.addSingleComponentLHS(chEdgeDistance);
        RapidBuilderJPanel2Column builder = new RapidBuilderJPanel2Column(panel, (int)0.9*GUI.GUI_WIDTH, GUI.GUI_HEIGHT, GUI.SMALL_COMPONENT_HEIGHT, GUI.GUI_INSET_X, GUI.GUI_INSET_Y);
//        builder.addTwoComponentsToRow(fc.blankSpace(), fc.blankSpace());
//        builder.addComponentDoubleColumnStandardHeight(new JSeparator(JSeparator.HORIZONTAL));
        builder.addSingleComponentLHS(fc.jLabelBold("Proximal feature analysis")); 
        builder.addSingleComponentLHS( chDoPFA );
        builder.addTwoComponentsToRow(fc.jLabel("Cut-off distance"), jtfThreshDist);
        builder.addComponentDoubleColumnStandardHeight(new JSeparator(JSeparator.HORIZONTAL));
        builder.addSingleComponentLHS(fc.jLabelBold("NND Histogram analysis"));
        builder.addTwoComponentsToRow(fc.jLabel("Max distance"), jtfMaxDistance);
        builder.addTwoComponentsToRow(fc.jLabel("Bin width"), jtfBinWidth);
        builder.addComponentDoubleColumnStandardHeight(new JSeparator(JSeparator.HORIZONTAL));
        builder.addTwoComponentsToRow(fc.jLabel("N randomisation tests"), jtfDoRandomisaion);
        //panel.addTwoComponentsToRow(gc.jLabel("Ref channel", hsiz), jComboChannel);
        builder.packComponents();
        
        return panel;
    }

    @Override
    public void doProcess() {
  
        getValues();
        // get the features for the current data set.
        parentFeatures = model.getCurrentImageSetParentFeatures();
        nfeatures = parentFeatures.size();
                
        ParentFeature pf;
        double pixelSize = model.getCurrentImageDataSet().getCalibration().pixelWidth;
        Feature2FeatureDistCalculator f2fDist;
              
        String[] pfaFeatures = new String[]{SpotDetectorCommon.SPOT_MEAN_VALUE, SpotDetectorCommon.SPOT_AREA, SpotDetectorCommon.SPOT_SUM_INTENSITY};//
        // Process the features. TODO. parallelize this in future. 
        for( int i = 0; i < nfeatures; i++ ){
           // get the process each feature. 
           pf = parentFeatures.get(i);
           // do the distance stats. 
           f2fDist = new Feature2FeatureDistCalculator( pf, edge2edgeDistance, pixelSize, SpotDetectorChannelProcessor.SPOT_FEATURE_NAME, SpotDetectorChannelProcessor.SPOT_FEATURE_NAME, nrandomisations,  threshdist, maxHistDist, binwidth );
           // 
           if(doPfaAnalysis){
               f2fDist.setProximalFeatureAnalysisFeatures(pfaFeatures);
           }
           
           f2fDist.run();
        }
        
    }
   
    /**
     * Get the values from the text boxes etc. 
     */
    private void getValues(){
        edge2edgeDistance = chEdgeDistance.isSelected();
        nrandomisations = TextFieldGetTextOps.positiveIntegerOrDefault(jtfDoRandomisaion, nrandomisations);
//        referenceChannel = jComboChannel.getSelectedIndex();
        threshdist = TextFieldGetTextOps.positiveDoubleOrDefault(jtfThreshDist, threshdist);
        binwidth = TextFieldGetTextOps.positiveDoubleOrDefault(jtfBinWidth, binwidth);
        maxHistDist = TextFieldGetTextOps.positiveDoubleOrDefault(jtfMaxDistance, maxHistDist);   
    }
    
    @Override
    public String getName() {
     return NAME;
    }

    @Override
    public void summariseAndSave() {
        
        // Compute the labels used for saving the relevent features within the 
        // ParentFeature map as they will have been saved in the original function. 

        // Names associated with the binary, near-far, global spatial realtions
        ArrayList<String> nndNameList = new ArrayList<>(); 
        ArrayList<String> nndRandomisedNameList = new ArrayList<>();
        
        // Names associated with the binned and histogrammed data. 
        ArrayList<String> histoNames = new ArrayList<>();
        ArrayList<String> histoNamesBins = new ArrayList<>();
        ArrayList<String> histoNamesRandomisedMeans = new ArrayList<>();
        ArrayList<String> histoNamesRandomisedErrors = new ArrayList<>();
        
        // names associated with the PFA analysis. 
        ArrayList<String> pfaAnalysisNames = new ArrayList<>(); 
        
        String nameSet1, nameSet2;
        String coupleNameCentroidNND, coupleNameEdgeNND;
        for (int c1 = 0; c1 < nchannels; c1++) {
            for (int c2 = 0; c2 < nchannels; c2++) {
                if (c1 == c2) {
                    continue;
                }
                // 
//                nameSet1 = (C_EXT + (c1 + 1));
//                nameSet2 = (C_EXT + (c2 + 1));
                nameSet1 = ("" + (c1 + 1));
                nameSet2 = ("" + (c2 + 1));
                coupleNameCentroidNND  = D_NN_CENT + C_EXT + "_" + nameSet1 + "-" + nameSet2;       
                coupleNameEdgeNND  = D_NN_EDGE + C_EXT + "_" + nameSet1 + "-" + nameSet2;                 
                
                // 
                nndNameList.add(coupleNameCentroidNND + MEAN_EXT);
                nndNameList.add(coupleNameCentroidNND + MED_EXT);
                nndNameList.add(coupleNameCentroidNND + STD_EXT);
                nndNameList.add(coupleNameCentroidNND + FRAC_NN);     
                
                nndRandomisedNameList.add(coupleNameCentroidNND + MEAN_EXT + RAND_EXT);
                nndRandomisedNameList.add(coupleNameCentroidNND + MED_EXT + RAND_EXT);
                nndRandomisedNameList.add(coupleNameCentroidNND + STD_EXT + RAND_EXT);
                nndRandomisedNameList.add(coupleNameCentroidNND + FRAC_NN + RAND_EXT);                  
                // After statsitical testing. 
                nndRandomisedNameList.add(coupleNameCentroidNND + MEAN_EXT + RAND_EXT + PV);
                nndRandomisedNameList.add(coupleNameCentroidNND + MED_EXT + RAND_EXT + PV);
                nndRandomisedNameList.add(coupleNameCentroidNND + STD_EXT + RAND_EXT + PV);
                nndRandomisedNameList.add(coupleNameCentroidNND + FRAC_NN + RAND_EXT + PV);  
                
                // names of the histograms
                histoNames.add( coupleNameCentroidNND + HIST );
                histoNamesBins.add( coupleNameCentroidNND + Feature2FeatureDistCalculator.HISTBINS );
                histoNamesRandomisedMeans.add( coupleNameCentroidNND + HIST + Feature2FeatureDistCalculator.MEAN_EXT + RAND_EXT );
                histoNamesRandomisedErrors.add( coupleNameCentroidNND + HIST + Feature2FeatureDistCalculator.STD_EXT + RAND_EXT );     
                
                // prosimity feature analysis. 
                pfaAnalysisNames.add( coupleNameCentroidNND + "_" + PFA_EXT );
                pfaAnalysisNames.add( coupleNameEdgeNND + "_" + PFA_EXT );
                
            }
        }
        
        // Global nearest neighbour distance. 
        // Combine the lists for original and randomised data so that it appears in the same table. 
        nndNameList.addAll(nndRandomisedNameList);
        String[] names = nndNameList.toArray(new String[nndNameList.size()]);
        
        ResultsTable rt = FeaturesAndParentFeaturesToResultsTable.parentFeatureSpecifiedNumericalProperties2ResultsTable( model, names );
        //rt.show("Global NND results");
        try{
            rt.save(model.getSaveFileDirectory()+"\\"+"NND_results.csv");
        }catch(Exception e){
            System.out.println(e.getMessage());
        }
        
        // Save the NND histograms. 
        String[] binNnames, orgHistNames, randHistNames, ransHistSTDs;
        orgHistNames = histoNames.toArray(new String[histoNames.size()]);
        binNnames = histoNamesBins.toArray(new String[histoNamesBins.size()]);
        randHistNames = histoNamesRandomisedMeans.toArray(new String[histoNamesRandomisedMeans.size()]);
        ransHistSTDs = histoNamesRandomisedErrors.toArray( new String[histoNamesRandomisedErrors.size()] );
        
        // Use another class to save the hisograms.
        Feature2FeatureDistSummariser ss = new Feature2FeatureDistSummariser(model);
        ss.summariseHistograms( binNnames, orgHistNames, randHistNames, ransHistSTDs );
        
        if(doPfaAnalysis){
            ss.sumarisePFA(pfaAnalysisNames.toArray(new String[pfaAnalysisNames.size()]));
        }
 
    }
    
}
