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

/**
 * A class with static options/variables which may be common for multiple detectors
 * that detect similar size/shaped e.g. small spot shaped points in microscopy 
 * images. 
 * 
 * e.g. Two detectors may have options for the same set of segmentation methods. 
 * 
 * @author mqbssep5
 */
public class SpotDetectorCommon {
    
    /**
     *
     */
    public static final int INTENSITY_BASED_WATERSHED_SEGMENTATION = 1;

    public static final int CONNECTED_COMPONENT_SEGMENTATION = 2;
    
    public static final String COM_X = "COM_X";

    public static final String COM_Y = "COM_Y";

    public static final String COM_X_PIX = "COM_X_PIX";

    public static final String COM_Y_PIX = "COM_Y_PIX";

    public static final String SPOT_AMPLITUDE = "SPOT_AMPLITUDE";

    public static final String SPOT_MEAN_VALUE = "SPOT_MEAN_VALUE";

    public static final String SPOT_AREA = "SPOT_AREA";

    public static final String SPOT_BG_USED = "SPOT_BG_USED";

    public static final String SPOT_SUM_INTENSITY = "SPOT_SUM_INTENSITY";

    public static final String SPOT_INTENSITY_PIXELS = "SPOT_INTENSITY_PIXELS";

    public static final String SPOT_PERIMETER = "SPOT_PERIMETER";

    public static final String SPOT_CIRCULARITY = "SPOT_CIRCULARITY";    
    
    public static final String SPOT_ID = "SPOT_ID";
    
    
}
