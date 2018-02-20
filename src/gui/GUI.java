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

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

/**
 * A class with the general purpose Java {@link JComponent}s formatted for the
 * GUIs. 
 *
 * @author mqbssep5
 */
public class GUI {

    /**
     * Preferred width for the final GUI 
     */    
    public static final int GUI_WIDTH = 350;
    
    /**
     * Preferred height for the final GUI 
     */
    public static final int GUI_HEIGHT = 500;

    /**
     * Component x inset 
     */
    public static final int GUI_INSET_X = 5;
    
    /**
     * Component y inset 
     */
    public static final int GUI_INSET_Y = 2;    
    
    /**
     * The scale factor applied when splitting the width dimension into smaller 
     * equal fractions to prevent actual overlap. 
     */
    private static final double WIDTH_FACTOR = 0.95d;
    
    /**
     * The preferred height for small components such as buttons.
     */
    public static final int SMALL_COMPONENT_HEIGHT = 20;
    
    /**
     * The preferred size for the text. 
     */    
    public static final int TEXT_SIZE_STANDARD = 11;
    
//    /**
//     * The preferred dimensions for JPanels which will span the full GUI face.
//     */
//    public static final Dimension GUI_MAIN_PANEL_HOLDER_DIM = new Dimension((int)Math.round(1.2d*(double)GUI_WIDTH), (int)Math.round(1.05d*(double)GUI_HEIGHT) );    
    
    /**
     * The preferred dimensions for JPanels which will span the full GUI face.
     */
    public static final Dimension GUI_MAIN_PANEL_HOLDER_DIM = new Dimension( 360, 525 );        

    /**
     * The preferred dimensions for JPanels which will span the full GUI face.
     */
    public static final Dimension GUI_MAIN_PANEL_DIM = new Dimension( GUI_WIDTH, GUI_HEIGHT );
    
//    /**
//     * The preferred dimensions for {@link JComponent}s which will span approximately 
//     * half way across the main GUI.
//     * 
//     */
//    public static final Dimension FULL_WIDTH_DIM = new Dimension((int) Math.round(WIDTH_FACTOR * (double)GUI_WIDTH ), SMALL_COMPONENT_HEIGHT);    
    
    /**
     * The preferred dimensions for {@link JComponent}s which will span approximately 
     * half way across the main GUI.
     * 
     */
    public static final Dimension FULL_WIDTH_DIM = new Dimension( 285, SMALL_COMPONENT_HEIGHT );      

//    /**
//     * The preferred dimensions for {@link JComponent}s which will span approximately 
//     * half way across the main GUI.
//     * 
//     */
//    public static final Dimension HALF_WIDTH_DIM = new Dimension((int) Math.round(WIDTH_FACTOR * (double)GUI_WIDTH / 2d ), SMALL_COMPONENT_HEIGHT);
    
    /**
     * The preferred dimensions for {@link JComponent}s which will span approximately 
     * half way across the main GUI.
     * 
     */
    public static final Dimension HALF_WIDTH_DIM = new Dimension( 143 , SMALL_COMPONENT_HEIGHT );    

//    /**
//     * The preferred dimensions for {@link JComponent}s which will span approximately 
//     * a third of the way across the main GUI. 
//     */
//    public static final Dimension THIRD_WIDTH_DIM = new Dimension((int) Math.round(WIDTH_FACTOR * (double)GUI_WIDTH / 3d ) , SMALL_COMPONENT_HEIGHT);    
    
    /**
     * The preferred dimensions for {@link JComponent}s which will span approximately 
     * a third of the way across the main GUI. 
     */
    public static final Dimension THIRD_WIDTH_DIM = new Dimension( 95 , SMALL_COMPONENT_HEIGHT );     
    
//    /**
//     * The preferred dimensions for {@link JComponent}s which will span approximately 
//     * a quarter of the way across the main GUI. 
//     */
//    public static final Dimension QUARTER_WIDTH_DIM = new Dimension((int) Math.round(WIDTH_FACTOR * (double)GUI_WIDTH / 4d ), SMALL_COMPONENT_HEIGHT);        
    
    /**
     * The preferred dimensions for {@link JComponent}s which will span approximately 
     * a quarter of the way across the main GUI. 
     */
    public static final Dimension QUARTER_WIDTH_DIM = new Dimension( 72 , SMALL_COMPONENT_HEIGHT );     
    
    /**
     * Creates a JLabel with the preset preferred size.
     *
     * @param s the label.
     * @param preSize the preferred size for this label. Use one of the static
     * options within this class. e.g. {@link #HALF_WIDTH_DIM}, {@link #THIRD_WIDTH_DIM}, {@link #QUARTER_WIDTH_DIM}.
     * @return
     */
    public JLabel jLabel( String s, Dimension preSize ) {
        JLabel jl = new JLabel(s);
        jl.setPreferredSize(preSize);
        return jl;
    }

    /**
     * Creates a JLabel with the preset preferred size.
     *
     * @param s the label.
     * @param preSize the preferred size for this label. Use one of the static
     * options within this class. e.g. {@link #HALF_WIDTH_DIM}, {@link #THIRD_WIDTH_DIM}, {@link #QUARTER_WIDTH_DIM}.
     * @return
     */
    public JLabel jLabelBold( String s, Dimension preSize ) {
        JLabel jl = new JLabel(s);
        Font f = new Font(jl.getFont().getName(), Font.BOLD, 12);
        jl.setFont(f);
        jl.setPreferredSize(preSize);
        return jl;
    }

    /**
     * Formatted text field for our GUI.
     *
     * @param s text in the field. 
     * @param preSize the preferred size for this label. Use one of the static
     * options within this class. e.g. {@link #HALF_WIDTH_DIM}, {@link #THIRD_WIDTH_DIM}, {@link #QUARTER_WIDTH_DIM}.
     * @return
     */
    public JTextField jTextFeild( String s, Dimension preSize ) {
        JTextField tf = new JTextField(s);
        tf.setPreferredSize(preSize);
        //tf.setPreferredSize(HALF_WIDTH_DIM);
        return tf;
    }
    
    /**
     * Creates a JButton with the preset preferred size etc. The action listener
     * for this class is also added to the button.
     *
     * @param s string label for this button. 
     * @param al action listener for this button. 
     * @param preSize the preferred size for this label. Use one of the static
     * options within this class. e.g. {@link #HALF_WIDTH_DIM}, {@link #THIRD_WIDTH_DIM}, {@link #QUARTER_WIDTH_DIM}.
     * @return
     */
    public JButton jButton( String s, ActionListener al, Dimension preSize ) {
        JButton button = new JButton(s);
        button.addActionListener(al);
        if(preSize == null){
            
        }else{
           button.setPreferredSize( preSize );
        }
        
        //button.setSize( preSize );
        //button.setPreferredSize(HALF_WIDTH_DIM);
        return button;
    }    
    
    /**
     * Creates a radio button with the desired formatting.
     *
     * @param label
     * @param al
     * @param preSize the preferred size for this label. Use one of the static
     * options within this class. e.g. {@link #FULL_WIDTH_DIM} {@link #HALF_WIDTH_DIM}, {@link #THIRD_WIDTH_DIM}, {@link #QUARTER_WIDTH_DIM}.
     * @return a radio button with the desired formatting.
     */
    public JRadioButton jRadioButton( String label, ActionListener al, Dimension preSize ) {
        JRadioButton jrb = new JRadioButton(label);
        jrb.addActionListener( al );
        jrb.setPreferredSize( preSize );
        return jrb;
    }    
    
    /**
     * Creates a {@link JCheckBox} with the desired formatting.
     *
     * @param label
     * @param preSize the preferred size for this label. Use one of the static
     * options within this class. e.g. {@link #FULL_WIDTH_DIM} {@link #HALF_WIDTH_DIM}, {@link #THIRD_WIDTH_DIM}, {@link #QUARTER_WIDTH_DIM}.
     * @return a radio button with the desired formatting.
     */
    public JCheckBox jCheckBox( String label, Dimension preSize ) {
        JCheckBox jrb = new JCheckBox(label);
        jrb.setPreferredSize( preSize );
        return jrb;
    }      

    /**
     * Creates a formatted JCombobox. 
     * @param items string list of items for in the box. 
     * @param preSize the preferred size for this label. Use one of the static
     * options within this class. e.g. {@link #FULL_WIDTH_DIM} {@link #HALF_WIDTH_DIM}, {@link #THIRD_WIDTH_DIM}, {@link #QUARTER_WIDTH_DIM}.
     * @return returns a formatted combo box. 
     */
    public JComboBox jComboBox( String[] items, Dimension preSize ){
        JComboBox jc = new JComboBox(items);
        jc.setPreferredSize(preSize);
        return jc;
    }
    
    /**
     * Used for filling blank spaces with to make the alignment of some components 
     * in the panels easier. 
     * @param preSize
     * @return 
     */
    public JComponent blankSpace( Dimension preSize ){
        JLabel jl = new JLabel("");
        jl.setPreferredSize(preSize);
        return jl;
    }
    
    /**
     * A method for enabling or disabling all the components on a JPanel. 
     * @param panel JPanel containing the components. 
     * @param enable set true to enable. false to disable. 
     */
    public void jPanelComponentsEnabledDisable( JPanel panel, boolean enable ){
        if(panel instanceof JPanelSpeedyMultiColBased){
            System.out.println("Need to write write specific function for this JPanelSpeedyMultiColBased panels. ");
        }
        Component[] c = panel.getComponents();
        for( int i = 0; i < c.length; i++ ){
            c[i].setEnabled(enable);
        }
    }
    
    /**
     * Returns a new {@link Dimension} with scaled by the provided factors. 
     * @param d
     * @param scaleX
     * @param scaleY
     * @return 
     */
    public Dimension scaleDims( Dimension d, double scaleX, double scaleY ){
        return new Dimension((int)(d.width*scaleX), (int)(d.getHeight()*scaleY) );
    }
    
}
