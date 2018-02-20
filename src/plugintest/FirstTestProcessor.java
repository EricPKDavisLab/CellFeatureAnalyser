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
package plugintest;

import abstractprocessors.AbstractParentFeatureProcessor;
import featureobjects.ParentFeatureOps;
import featureobjects.ParentFeature;
import gui.CellAnalyserGUIModel;
import gui.GUI;
import gui.JPanelSpeedy2ColBased;
import ij.ImagePlus;
import ij.gui.Overlay;
import java.awt.Dimension;
import java.util.ArrayList;
import javax.swing.JPanel;

/**
 * This is the first {@link AbstractParentFeatureProcessor}, it a first trial of 
 * various things the processors.
 * 
 * @author mqbssep5
 */
public class FirstTestProcessor extends AbstractParentFeatureProcessor{

    private ImagePlus imp;
    
    private ArrayList<ParentFeature> features;
    
    private int nFeatures;

    public FirstTestProcessor( CellAnalyserGUIModel model ) {
        super(model);
    }
    
    @Override
    public JPanel getSettingsPanel() {
        
        GUI gc = new GUI();
        
        Dimension siz1 = GUI.HALF_WIDTH_DIM;
        Dimension siz2 = GUI.THIRD_WIDTH_DIM;        
                 
        // The panel containing the features to test the JPanel. 
        JPanelSpeedy2ColBased pan3 = new JPanelSpeedy2ColBased();
        pan3.addTwoComponentsToRow(gc.jLabel("S1", siz1),gc.jTextFeild("0", siz2));

        pan3.setPreferredSize(GUI.GUI_MAIN_PANEL_DIM);
        pan3.setSize(GUI.GUI_MAIN_PANEL_DIM);
        
        return pan3;
    }

    @Override
    public void doProcess( ) {
             
        // Get reference to the current image which we will extract the features from. 
        imp = model.getCurrentImageDataSet();
        features = model.getCurrentImageSetParentFeatures();
        
        nFeatures = features.size();
 
        // Test some shit. 
        testFeatureCropper();
        
    }
    
    private void testFeatureCropper(){
        
        ImagePlus impLocal;
        for( int i = 0; i < nFeatures; i++ ){
            impLocal = ParentFeatureOps.getLocalisedFeatureImageDataAllChannels(features.get(i), imp);
            impLocal.show();
            Overlay ov = new Overlay( features.get(i).getFeatureBoundsShifted());
            impLocal.setOverlay(ov);
        }
        
        
    }

    @Override
    public String getName() {
        return "FTP";
    }

    @Override
    public void summariseAndSave() {
        System.out.println("Summarising " + "FTP");
    }
    
}
