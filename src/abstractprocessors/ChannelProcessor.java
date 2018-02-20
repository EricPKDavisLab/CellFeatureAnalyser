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
import javax.swing.JPanel;

/**
 * This abstract class is used to perform any task for data of a single specific 
 * channel. This could be an image processing task used for directly processing 
 * image data in one channel, or a task for processing some channel specific data
 * within one of the channels of a {@link ParentFeature}.  
 * 
 * @author mqbssep5
 */
public abstract class ChannelProcessor implements Runnable{

    public abstract JPanel getSettingsPanel();
    
    @Override
    public abstract void run();
    
    
    public abstract String name();
    
    
}
