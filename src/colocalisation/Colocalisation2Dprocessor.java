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
package colocalisation;

import abstractprocessors.AbstractParentFeatureProcessor;

import featureobjects.ParentFeature;
import gui.CellAnalyserGUIModel;
import gui.GUI;
import gui.JPanelSpeedy2ColBased;
import gui.TextFieldGetTextOps;
import ij.measure.ResultsTable;
import io.FeaturesAndParentFeaturesToResultsTable;
import java.awt.Dimension;
import java.util.ArrayList;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 * Performs colocalization analysis using the {@link Features}, with PCC and Mander's 
 * overlap coefficients. 
 * 
 * @author mqbssep5
 */
public class Colocalisation2Dprocessor extends AbstractParentFeatureProcessor{

    public static String NAME = "Coloc";
    
    private JPanelSpeedy2ColBased panel;
    
    private GUI gc;
    
    private JTextField jtfDoRandomisaion;    
    
    private int nrandomisations = 10;
    
    private int referencechannel = 0;
    
    private int nchannels, nfeatures;
    
    private Dimension hsiz = GUI.HALF_WIDTH_DIM;       
    
    private JComboBox jComboChannel;
    
    private ArrayList<ParentFeature> parentFeatures;    
    
    private JCheckBox jbcShowRandimisedImages;
    
    private boolean showRandomisedImages = false;
    
    public Colocalisation2Dprocessor(CellAnalyserGUIModel model) {
        super(model);
    }

    @Override
    public JPanel getSettingsPanel() {
        
        panel = new JPanelSpeedy2ColBased();
        gc = new GUI();
        
        nchannels = model.getNchannels();
        String[] channels = new String[nchannels];
        for(int i = 0; i < nchannels; i++){
            channels[i] = ""+(i+1);
        }
        jComboChannel = gc.jComboBox(channels, hsiz);        
        jtfDoRandomisaion = gc.jTextFeild(""+nrandomisations, hsiz);
        // provide the option fot seeing the rendomised images.
        jbcShowRandimisedImages = gc.jCheckBox("Show randomised images", hsiz);
      
        panel.addTwoComponentsToRow(gc.jLabel("Reference channel", hsiz), jComboChannel);
        panel.addTwoComponentsToRow(gc.jLabel("n randomisations", hsiz), jtfDoRandomisaion);
        panel.addSingleComponentLHS(jbcShowRandimisedImages);
        
        
        return panel;
        
    }

    @Override
    public void doProcess() {
        
        // get the features for the current data set.
        parentFeatures = model.getCurrentImageSetParentFeatures();
        nfeatures = parentFeatures.size();
        
        ParentFeature currentFeature;
        
        // get the settings from the panel.
        referencechannel = jComboChannel.getSelectedIndex();
        nrandomisations = TextFieldGetTextOps.positiveIntegerOrDefault(jtfDoRandomisaion, nrandomisations);
        
        showRandomisedImages = jbcShowRandimisedImages.isSelected();
        
        // class which does the number crunching 
        FeatureColocaliszer2D coloc;
        for( int i = 0; i < nfeatures; i++ ){
            
            currentFeature = parentFeatures.get(i);
            // do the colocalisation 
            coloc = new FeatureColocaliszer2D(currentFeature, nrandomisations, referencechannel);
            coloc.showRandimisedImages(showRandomisedImages);
            coloc.doColocalisations();
            
        }
        
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public void summariseAndSave() {
       
        System.out.println("Summarising " + NAME);
        
        ResultsTable rt = FeaturesAndParentFeaturesToResultsTable.parentFeatureSpecifiedNumericalProperties2ResultsTable(model, FeatureColocaliszer2D.ALL_METRICS);
        rt.show("Colocalisation results");
        
        try{
            rt.save( model.getSaveFileDirectory() + "\\"+"Colocalisation_results.csv" );
        }catch(Exception e){
            System.out.println(e.getMessage());
        }        
        
    }
    
}
