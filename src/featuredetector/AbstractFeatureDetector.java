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
package featuredetector;

import featureobjects.Feature;
import featureobjects.ParentFeature;
import ij.process.ImageProcessor;
import java.util.ArrayList;


/**
 * Super class for the feature detector. These features will be channel specific 
 * features that are located within a {@link ParentFeature}. 
 * 
 * The constructors for the sub-classes of this will also have 
 * to pass the image data in some form, e.g. a single 2D {@link ImageProcessor} 
 * or an array of {@link ImageProcessor}s, or an {@link ImagePlus} etc. 
 * 
 * @author mqbssep5
 */
public abstract class AbstractFeatureDetector implements Runnable{

    protected final ParentFeature parentFeature;
    
    /**
     * Constructor. The constructors for the sub-classes of this will also have 
     * to pass the image data in some form, e.g. a single 2D {@link ImageProcessor} 
     * or an array of {@link ImageProcessor}s, or an {@link ImagePlus}, and any 
     * variables required for the detection process. 
     * 
     * @param parentFeature the parent feature which the 
     */
    public AbstractFeatureDetector( ParentFeature parentFeature ){
        this.parentFeature = parentFeature;
    }
    
    @Override
    public abstract void run();
    
    
    public abstract ArrayList<Feature> getFeatures();
    
    
}
