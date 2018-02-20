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
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import javax.swing.JPanel;

/**
 * An extension of the JPanel which already has a set {@link GridBagConstraints}
 * layout manager.
 *
 * It has a few methods used to easily add components to the JPanel.
 *
 * @author mqbssep5.
 */
public class JPanelSpeedyMultiColBased extends JPanel {

    private int vgap = 5;

    private int maxhgap = 10;

    private final Insets insets = new Insets(5, 5, 5, 5);

    private final GridBagConstraints c;

    private int row = 0;

    /**
     * Constructor.
     */
    public JPanelSpeedyMultiColBased() {
        c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.insets = insets;
        this.setLayout(new GridBagLayout());
    }

    /**
     * Resets the anchor for this panel. The default is
     * GridBagConstraints.NORTHWEST.
     *
     * @param anchor
     */
    public void setAnchor(int anchor) {
        c.anchor = anchor;
    }

    /**
     * Adds a components to the specified JPanel with defined grid constraints.
     *
     * @param comp component to be added.
     * @param row row this will be added to.
     */
    public void addComponent(Component comp, int row) {
        c.gridx = 0;
        c.gridy = row;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridwidth = 1;
//        c.weightx = 0.5;
        this.add(comp, c);
    }

    /**
     * Adds the list of components to the last row in the panel. 
     * @param comp 
     */
    public void addNcomponents2row( Component[] comp ) {
        JPanel pan = new JPanel(new GridLayout(1, comp.length));
        for (int i = 0; i < comp.length; i++) {
             pan.add(comp[i]);
        }
        addComponent(pan, row);   
        row++;
    }
    
    /**
     * @param comp array of components to be added. The preferred size, or set size
     * of the component will be accounted for when it is added to a row.  
     */
    public void addNcomponents2rowWidthWeighted( Component[] comp ) {
        JPanel pan = new JPanel(new GridBagLayout());  
        GridBagConstraints c2 = new GridBagConstraints();
        c2.anchor = GridBagConstraints.NORTHWEST;
        c2.insets = insets;
        c2.gridy = row;
        c2.gridwidth = 1;
        c2.fill = GridBagConstraints.HORIZONTAL;        
        
        for (int i = 0; i < comp.length; i++) {
             c2.gridx = i;
             if(comp[i] == null){
                 continue;
             }
             pan.add(comp[i]);
        }
        addComponent(pan, row);   
        row++;
    }    

    /**
     *
     * Adds two {@link Component}s to this JPanel to the last row in the JPanel
     *
     * @param compLeft
     * @param compRight
     */
    public void addTwoComponentsToEndRow( Component compLeft, Component compRight ) {
        addTwoComponentsToRow(compLeft, compRight, row);
        row++;
    }

    /**
     * Adds 3 components across the two columns of the panel.
     *
     * @param compLeft
     * @param compMid
     * @param compRight
     * @param row
     */
    public void addThreeComponents(Component compLeft, Component compMid, Component compRight, int row) {
        //JPanel pan = new JPanel(new FlowLayout());
        //JPanel pan = new JPanel(new GridLayout(1, 3));
        JPanel pan = new JPanel(new GridLayout(1, 3, (int) Math.round((double) maxhgap / 3d), vgap));
        pan.add(compLeft);
        pan.add(compMid);
        pan.add(compRight);
        //c.insets = new Insets(5, 50, 5, 50);
        // add this as a double column item.
        addComponentDoubleColumn(pan, row);
        c.insets = insets;
    }

    /**
     * Adds 3 components across the two columns of the panel.
     *
     * @param compLeft
     * @param compMid
     * @param compRight
     * @param row
     */
    public void addTwoComponentsToRow(Component compLeft, Component compMid, int row) {
        //JPanel pan = new JPanel(new FlowLayout());
        JPanel pan = new JPanel(new GridLayout(1, 2, (int) Math.round((double) maxhgap / 2d), vgap));
        pan.add(compLeft);
        pan.add(compMid);
        //c.insets = new Insets(5, 50, 5, 50);
        // add this as a double column item.
        addComponentDoubleColumn(pan, row);
    }

    /**
     * Adds 3 components across the two columns of the panel.
     *
     * @param compLeft
     * @param compRight

     */
    public void addTwoComponents2(Component compLeft, Component compRight) {
        JPanel pan = new JPanel(new FlowLayout());
        pan.add(compLeft);
        pan.add(compRight);

        //c.insets = new Insets(5, 50, 5, 50);
        // add this as a double column item.
        addComponentDoubleColumn(pan, row);
        row++;
    }

    /**
     * Adds 3 components across the two columns of the panel.
     *
     * @param compLeft
     * @param compMid
     * @param compRight
     */
    public void addThreeCompoenentsToEndRow(Component compLeft, Component compMid, Component compRight) {

        JPanel pan = new JPanel(new FlowLayout());
        pan.add(compLeft);
        pan.add(compMid);
        pan.add(compRight);
        // add this as a double column item.
        addComponentDoubleColumn(pan, row);
        row++;
    }

//    /**
//     * Adds a component across two columns to the specified row. 
//     *
//     * @param comp
//     * @param row
//     * @param col
//     */
//    public void addComponentDoubleColumn( Component comp, int row, int col ) {
//        c.gridx = col;
//        c.gridy = row;
//        c.gridwidth = 2;
//        this.add(comp, c);
//    }
    /**
     * Adds a component across two columns to the specified row.
     *
     * @param comp
     * @param row
     */
    public void addComponentDoubleColumn(Component comp, int row) {
        c.gridx = 0;
        c.gridy = row;
        c.gridwidth = 2;
        c.fill = GridBagConstraints.HORIZONTAL;
        this.add(comp, c);
    }

//    /**
//     * Adds a component across two columns to the specified row. 
//     *
//     * @param comp
//     * @param col
//     */
//    public void addComponentDoubleColumnToEndRow( Component comp, int col ) {
//        c.gridx = col;
//        c.gridy = row;
//        c.gridwidth = 2;
//        this.add(comp, c);
//        row++;
//    }    
    /**
     * Adds a component across two columns to the specified row.
     *
     * @param comp
     */
    public void addComponentDoubleColumnToEndRow(Component comp) {
        c.gridx = 0;
        c.gridy = row;
        c.gridwidth = 2;
        this.add(comp, c);
        row++;
    }

}
