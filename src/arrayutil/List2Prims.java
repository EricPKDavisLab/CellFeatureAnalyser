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

import java.util.List;

/**
 * A class with static methods for converting {@link List} containing numeric values to
 * double[] and float[] arrays, which are often the format required for computing 
 * stats etc in numeric libraries. 
 * 
 * @author mqbssep5
 */
public class List2Prims {
    
    /**
     * Converts the numbers in the array to double[] array of the same length. 
     * @param list list of numbers 
     * @return a double[] containing the converted values in the list. 
     */
    public static double[] doubleFromDouble( List<Double> list ){
        int n = list.size();
        double[] d = new double[n];
        for( int i = 0; i < n; i++ ){
            d[i] = list.get(i);
        }
        return d;
    }
    
    /**
     * Converts the numbers in the array to float[] array of the same length. 
     * @param list list of numbers 
     * @return a double[] containing the converted values in the list. 
     */
    public static float[] floatFromDouble( List<Double> list ){
        int n = list.size();
        float[] d = new float[n];
        for( int i = 0; i < n; i++ ){
            d[i] = list.get(i).floatValue();
        }
        return d;
    }    
    
    /**
     * Converts the numbers in the array to double[] array of the same length. 
     * @param list list of numbers 
     * @return a double[] containing the converted values in the list. 
     */
    public static double[] doubleFromFloat( List<Float> list ){
        int n = list.size();
        double[] d = new double[n];
        for( int i = 0; i < n; i++ ){
            d[i] = list.get(i);
        }
        return d;
    }
    
    /**
     * Converts the numbers in the array to float[] array of the same length. 
     * @param list list of numbers 
     * @return a double[] containing the converted values in the list. 
     */
    public static float[] floatFromFloat( List<Float> list ){
        int n = list.size();
        float[] d = new float[n];
        for( int i = 0; i < n; i++ ){
            d[i] = list.get(i);
        }
        return d;
    }      
    
}
