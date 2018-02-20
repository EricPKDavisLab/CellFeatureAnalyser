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
package main;

import ij.ImageJ;
import ij.plugin.PlugIn;

/**
 *
 * @author mqbssep5
 */
public class Main {
    
     /**
     * @param args the command line arguments
     */
    public static void main( String[] args ) {
        
        ImageJ ij = new ImageJ();
        ij.setVisible(true);
//        
        PlugIn cc = new CellFeatureAnalyser_("CellFeatureAnalyser");
//        PlugIn cc1 = new ROIdrawAndSave2_();
//

        cc.run("");
        //cc1.run("");
                
//     String directoryPath = "Y:\\epitkeathly\\analysis\\functionTests\\find_files\\find_multi_files_in_seperate_folders\\folder1\\";
//     ImageIOutils.getSpecifiedFileNamesWithinFolders(directoryPath, new String[]{".txt"} );


    }   
    
}
