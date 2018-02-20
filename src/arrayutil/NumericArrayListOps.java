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
package arrayutil;

import java.util.ArrayList;
import java.util.Iterator;

/**
 *
 * @author mqbssep5
 */
public class NumericArrayListOps {
    
    /**
     * Removes elements from the list which have a numerical value greater than
     * the pre defined threshold. 
     * @param values
     * @param threshold 
     */
    public static void removeElementsGreaterThan( ArrayList<Double> values, double threshold ){
        Iterator<Double> it = values.iterator();
        double v;
        while(it.hasNext()){
            v = it.next();
            if(Math.signum(v-threshold)>0.0){
                it.remove();
            }
        }
    }
    
   /**
     * Removes elements from the list which have a numerical value smaller than
     * the pre defined threshold. 
     * @param values
     * @param threshold 
     */
    public static void removeElementsSmallerThan( ArrayList<Double> values, double threshold ){
        Iterator<Double> it = values.iterator();
        double v;
        while(it.hasNext()){
            v = it.next();
            if(Math.signum(v-threshold)<0.0){
                it.remove();
            }
        }
    }
    
    /**
     * Extracts numbers from the input list which have a numerical value greater 
     * than the predefined threshold. 
     * @param values
     * @param threshold 
     * @return a list of any number in the input list that have a numerical value
     * greater than the threshold. 
     */
    public static ArrayList<Double> extractValuesGreaterThan( ArrayList<Double> values, double threshold ){
        
        Iterator<Double> it = values.iterator();
        double v;
        ArrayList<Double> outputValues = new ArrayList<>();
        while(it.hasNext()){
            v = it.next();
            if(Math.signum(v-threshold) > 0.0){
                outputValues.add(new Double(v));
            }
        }
        return outputValues;
    }    
    
    /**
     * Extracts numbers from the input list which have a numerical value less 
     * than the predefined threshold. 
     * @param values
     * @param threshold 
     * @return a list of any number in the input list that have a numerical value
     * less than the threshold. 
     */
    public static ArrayList<Double> extractValuesLessThan( ArrayList<Double> values, double threshold ){
        
        Iterator<Double> it = values.iterator();
        double v;
        ArrayList<Double> outputValues = new ArrayList<>();
        while(it.hasNext()){
            v = it.next();
            if(Math.signum(v-threshold) < 0.0){
                outputValues.add(new Double(v));
            }
        }
        return outputValues;
    }    
        
    
}
