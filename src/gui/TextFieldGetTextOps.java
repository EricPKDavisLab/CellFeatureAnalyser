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
package gui;

import javax.swing.JTextField;

/**
 * A class of static options for reading from text boxes and returning the value
 * in the appropriate numerical form. Many of the classes also possible handle
 * errors due to non-numeric inputs and will return a default value in addition 
 * to changing the value in the {@link JTextField} to this default value. 
 * 
 * @author mqbssep5
 */
public class TextFieldGetTextOps {
    
    
    /**
     * Returns the input of the {@link JTextField} as a double value entered in the
     * as updating the text field with the default value if there are any errors 
     * in the input box.
     *
     * If the value is non-numeric then the default value will be returned. 
     * 
     * @param jtf the text field containing the number. 
     * @param default_value the default value this box should be reset to if the
     * input was non-numeric or negative.  
     * 
     * @return the input of the {@link JTextField} as a double value subject to
     * a positivity constraint. If there is an error then the default value is returned.
     * 
     * The value inside the text field is also updated accordingly if the are any errors. 
     * 
     */
    public static double doubleOrDefault( JTextField jtf, double default_value ){
        
        double output;
        
        try{
           output = Double.parseDouble(jtf.getText());
        }catch( Exception e ){// we dont care about the actual error in this case. 
           output = default_value;
        }
        if( Math.signum(default_value) < 0){
            output = default_value;
        }
        // reset the value in the text box. 
        jtf.setText(Double.toString(output));
        return output;
    }        
    
    /**
     * Returns the input of the {@link JTextField} as a double value subject to
     * a positivity constraint as well as updating the text field with the default value
     * if there are any errors in the input box.
     *
     * If the value is negative or non-numeric then the default value will be returned. 
     * 
     * @param jtf the text field containing the number. 
     * @param default_value the default value this box should be reset to if the
     * input was non-numeric or negative.  
     * 
     * @return the input of the {@link JTextField} as a double value subject to
     * a positivity constraint. If there is an error then the default value is returned.
     * 
     * The value inside the text field is also updated accordingly if the are any errors. 
     * 
     */
    public static double positiveDoubleOrDefault( JTextField jtf, double default_value ){
        
        double output;
        
        try{
           output = Double.parseDouble(jtf.getText());
        }catch( Exception e ){// we dont care about the actual error in this case. 
           output = default_value;
        }
        if( Math.signum(output) < 0){
            output = default_value;
        }
        // reset the value in the text box. 
        jtf.setText(Double.toString(output));
        return output;
    }
    
    
    /**
     * Returns the input of the {@link JTextField} as a double value subject to
     * a negativity constraint as well as updating the text field with the default value
     * if there are any errors in the input box.
     *
     * If the value is positive or non-numeric then the default value will be returned. 
     * 
     * @param jtf the text field containing the number. 
     * @param default_value the default value this box should be reset to if the
     * input was non-numeric or negative.  
     * 
     * @return the input of the {@link JTextField} as a double value subject to
     * a positivity constraint. If there is an error then the default value is returned.
     */
    public static double negativeDoubleOrDefault( JTextField jtf, double default_value ){
        
        double output;
        
        try{
           output = Double.parseDouble(jtf.getText());
        }catch( Exception e ){// we dont care about the actual error in this case. 
           output = default_value;
        }
        if( Math.signum(output) > 0 ){
            output = default_value;
        }
        // reset the value in the text box. 
        jtf.setText(Double.toString(output));
        return output;
    }    
    
    /**
     * Returns the input of the {@link JTextField} as a double value subject to
     * a positivity constraint as well as updating the text field with the default value
     * if there are any errors in the input box.
     *
     * If the value is negative or non-numeric then the default value will be returned. 
     * 
     * @param jtf the text field containing the number. 
     * @param default_value the default value this box should be reset to if the
     * input was non-numeric or negative.  
     * 
     * @return the input of the {@link JTextField} as a double value subject to
     * a positivity constraint. If there is an error then the default value is returned.
     */
    public static int positiveIntegerOrDefault( JTextField jtf, int default_value ){
        
        int output;
        
        try{
           output = Integer.parseInt(jtf.getText());
        }catch( Exception e ){// we dont care about the actual error in this case. 
           output = default_value;
        }
        if( Math.signum(output) < 0 ){
            output = default_value;
        }
        // reset the value in the text box. 
        jtf.setText(Integer.toString(output));
        return output;
    }
    
    /**
     * Returns the input of the {@link JTextField} as a integer value subject to
     * a negativity constraint as well as updating the text field with the default value
     * if there are any errors in the input box. 
     *
     * If the value is positive or non-numeric then the default value will be returned. 
     * 
     * @param jtf the text field containing the number. 
     * @param default_value the default value this box should be reset to if the
     * input was non-numeric or negative.  
     * 
     * @return the input of the {@link JTextField} as a integer value subject to
     * a positivity constraint. If there is an error then the default value is returned.
     */
    public static int negativeIntegerOrDefault( JTextField jtf, int default_value ){
      
        int output;
        try{
           output = Integer.parseInt(jtf.getText());
        }catch( Exception e ){// we dont care about the actual error in this case. 
           output = default_value;
        }
        if( Math.signum(output) > 0 ){
            output = default_value;
        }
        // reset the value in the text box. 
        jtf.setText(Integer.toString(output));
        return output;
    }    
    
}
