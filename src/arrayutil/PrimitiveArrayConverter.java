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

/**
 * A class of static methods used to convert arrays of primitive data types 
 * @author mqbssep5
 */
public class PrimitiveArrayConverter {
 
    /**
     * @param d
     * @return a float copy of the double array. 
     */
    public static float[] double2float( double[] d ){
        int n = d.length;
        float[] f = new float[n];
        for( int i = 0; i < n; i++ ){
            f[i] = (float)d[i];
        }
        return f;
    }
    
    /**
     * @param f
     * @return a double copy of the float array. 
     */
    public static double[] float2double( float[] f ){
        int n = f.length;
        double[] d = new double[n];
        for( int i = 0; i < n; i++ ){
            d[i] = (double)f[i];
        }
        return d;
    }    
    
}
