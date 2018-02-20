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
package abstractprocessors;

import featureobjects.ParentFeature;
import gui.CellAnalyserGUIModel;
import javax.swing.JPanel;

/**
 * The base class for processing the specific data set. The idea is that the processor 
 * performs a specific process on the image data for each cell, or processing a task
 * on the data stored in the cells e.g. the distance between features in different
 * channels. The {@link AbstractParentFeatureProcessor}s will be applied in sequence as 
 * an array of processes to complete the full set of analysis on the cells. 
 * 
 * @author mqbssep5
 */
public abstract class AbstractParentFeatureProcessor {

    
    protected CellAnalyserGUIModel model;
    
    
    /**
     * Number of channels in the data involved. 
     * @param model the data structure passed to access the image data and features. 
     */
    public AbstractParentFeatureProcessor( CellAnalyserGUIModel model ){
        this.model = model;
    }    
    
    /**
     * @param model lets the data model be changed without re-initialising the
     * class. 
     */
    public void resetModel( CellAnalyserGUIModel model ){
        this.model = model;
    }
  
    /**
     * @return The panel used for defining the settings for this processor. 
     */
    public abstract JPanel getSettingsPanel();
    
    /**
     * This method should be called to perform the actual processing of the 
     * which may be for multiple {@link ParentFeature}s on a single multi-dimensional
     * data set. 
     */
    public abstract void doProcess();
    
    /**
     * @return The name of this processor. 
     */
    public abstract String getName();
    
    /**
     * This should be called if any of the processing performed within the has to be 
     * post processed and saved to a file. 
     */
    public abstract void summariseAndSave();
    
}
