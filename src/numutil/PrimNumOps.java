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
package numutil;

/**
 * A class with a number of common operations that can be applied to primitive
 * data types. 
 * 
 * e.g. 
 * 
 * @author mqbssep5
 */
public class PrimNumOps {
 
    /**
     * Scales the int number by the double value and returns as a in:
     * 
     * (int) Math.round(scaler*(double)num)
     * 
     * @param num
     * @param scaler
     * @return (int) Math.round(scaler*(double)num)
     */
    public static int scaleInt(int num, double scaler ){
        return (int) Math.round(scaler*(double)num);
    }
    
}
